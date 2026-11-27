package uk.gov.hmcts.cp.services;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.AzureNamedKeyCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.data.tables.TableClient;
import com.azure.data.tables.TableClientBuilder;
import com.azure.data.tables.models.TableEntity;
import com.azure.data.tables.models.TableEntityUpdateMode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import reactor.core.publisher.Mono;
import tools.jackson.databind.json.JsonMapper;
import uk.gov.hmcts.cp.entities.input.ReportRequest;
import uk.gov.hmcts.cp.entities.output.ReportResult;
import uk.gov.hmcts.cp.properties.FabricProperties;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.headerDoesNotExist;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@ExtendWith(MockitoExtension.class)
class AuditReportDispatchTest {
    private static final String REFERENCE = "20261120-Synthetic";
    private static final String ETAG = "W/\"synthetic-version\"";
    private static final FabricProperties FABRIC = new FabricProperties(
            "https://fabric.invalid", "/items/{ITEM_ID}/jobs", "pipeline", "workspace");
    private static final ReportRequest REQUEST = new ReportRequest(
            "synthetic-auditor", "auditor@example.invalid", REFERENCE, "2026-11-19", "2026-11-20",
            true, null, null, "CASE", "synthetic-case", "TEST0000001", null, null);
    private static final String JOB_LOCATION = "/items/pipeline/jobs/instances/synthetic-job";

    @Mock
    private TableClient table;

    @Mock
    private ClientHttpRequestFactory requestFactory;

    @Test
    void marksRequestFailedWhenFabricDnsFails() throws Exception {
        when(requestFactory.createRequest(any(), any())).thenThrow(new UnknownHostException("fabric.invalid"));
        insertReturns(ETAG);

        assertTrue(service(RestClient.builder().requestFactory(requestFactory)).requestReport(REQUEST).isEmpty());

        final ArgumentCaptor<TableEntity> pending = ArgumentCaptor.forClass(TableEntity.class);
        verify(table).createEntityWithResponse(pending.capture(), isNull(), eq(Context.NONE));
        assertEquals("PENDING", pending.getValue().getProperty("pipelineStatus"));
        assertNotNull(pending.getValue().getProperty("submittedAt"));
        assertFailedUpdate();
    }

    @ParameterizedTest
    @EnumSource(value = HttpStatus.class, names = {"BAD_REQUEST", "FORBIDDEN", "NOT_FOUND", "TOO_MANY_REQUESTS"})
    void marksRequestFailedWhenFabricRejectsSubmission(final HttpStatus status) {
        insertReturns(ETAG);
        final RestClient.Builder builder = RestClient.builder();
        final MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(method(HttpMethod.POST)).andRespond(withStatus(status).body("synthetic diagnostic"));

        assertTrue(service(builder).requestReport(REQUEST).isEmpty());

        server.verify();
        assertFailedUpdate();
    }

    @Test
    void sendsIntactWeakEtagWhenFabricReturns404() {
        final List<HttpRequest> sent = new ArrayList<>();
        final HttpClient transport = request -> {
            sent.add(request);
            final HttpResponse response = mock(HttpResponse.class);
            final HttpHeaders headers = new HttpHeaders();
            if (request.getHttpMethod() == com.azure.core.http.HttpMethod.POST) {
                headers.set(HttpHeaderName.ETAG, ETAG);
                when(response.getBodyAsBinaryData()).thenReturn(BinaryData.fromBytes(new byte[0]));
            }
            when(response.getRequest()).thenReturn(request);
            when(response.getHeaders()).thenReturn(headers);
            when(response.getStatusCode()).thenReturn(204);
            return Mono.just(response);
        };
        final TableClient realTable = new TableClientBuilder().tableName("reportrequests")
                .endpoint("https://synthetic.table.core.windows.net")
                .credential(new AzureNamedKeyCredential("synthetic", "c3ludGhldGlj"))
                .httpClient(transport).buildClient();
        final RestClient.Builder builder = RestClient.builder();
        final MockRestServiceServer fabricServer = MockRestServiceServer.bindTo(builder).build();
        fabricServer.expect(method(HttpMethod.POST)).andRespond(withStatus(HttpStatus.NOT_FOUND));
        final JsonMapper mapper = JsonMapper.builder().build();
        final AuditReportsService actual = new AuditReportsService(builder.build(), realTable, mapper, null, FABRIC,
                new TokenRequestContext(), null,
                ignored -> new AccessToken("synthetic-token", OffsetDateTime.now().plusMinutes(10)));

        assertTrue(actual.requestReport(REQUEST).isEmpty());

        fabricServer.verify();
        assertEquals(2, sent.size());
        final HttpRequest merge = sent.getLast();
        assertEquals("PATCH", merge.getHttpMethod().name());
        assertEquals(ETAG, merge.getHeaders().getValue(HttpHeaderName.IF_MATCH));
        final var body = mapper.readTree(merge.getBodyAsBinaryData().toString());
        assertEquals("FAILED", body.path("pipelineStatus").asString());
        assertEquals("", body.path("downloadUrl").asString());
    }

    @ParameterizedTest
    @EnumSource(value = HttpStatus.class,
                names = {"REQUEST_TIMEOUT", "CONFLICT", "INTERNAL_SERVER_ERROR", "GATEWAY_TIMEOUT", "ACCEPTED"})
    void leavesPendingWhenJobAcceptanceIsUnknown(final HttpStatus status) {
        insertReturns(ETAG);
        final RestClient.Builder builder = RestClient.builder();
        final MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(method(HttpMethod.POST)).andRespond(withStatus(status));

        assertTrue(service(builder).requestReport(REQUEST).isEmpty());

        server.verify();
        assertNoStatusUpdate();
    }

    @Test
    void leavesPendingWhenFabricConnectionTimesOut() throws Exception {
        insertReturns(ETAG);
        when(requestFactory.createRequest(any(), any())).thenThrow(new SocketTimeoutException());

        assertTrue(service(RestClient.builder().requestFactory(requestFactory)).requestReport(REQUEST).isEmpty());

        assertNoStatusUpdate();
    }

    @Test
    void marksRequestFailedWhenTokenAcquisitionFailsBeforeDispatch() {
        insertReturns(ETAG);
        final AuditReportsService service = new AuditReportsService(
                RestClient.builder().requestFactory(requestFactory).build(), table, JsonMapper.builder().build(),
                null, FABRIC, new TokenRequestContext(), null,
                ignored -> { throw new IllegalStateException("synthetic token failure"); });

        assertTrue(service.requestReport(REQUEST).isEmpty());

        verifyNoInteractions(requestFactory);
        assertFailedUpdate();
    }

    @Test
    void doesNotStartFabricOrChangeAnExistingRowIfInsertFails() {
        when(table.createEntityWithResponse(any(), isNull(), eq(Context.NONE)))
                .thenThrow(new IllegalStateException("synthetic duplicate row"));

        assertTrue(service(RestClient.builder().requestFactory(requestFactory)).requestReport(REQUEST).isEmpty());

        verifyNoInteractions(requestFactory);
        assertNoStatusUpdate();
    }

    @Test
    void doesNotUpdateWithoutARowVersion() throws Exception {
        insertReturns(null);
        when(requestFactory.createRequest(any(), any())).thenThrow(new UnknownHostException("fabric.invalid"));

        assertTrue(service(RestClient.builder().requestFactory(requestFactory)).requestReport(REQUEST).isEmpty());

        assertNoStatusUpdate();
    }

    @Test
    void doesNotRetryOrOverwriteIfConditionalUpdateFails() throws Exception {
        insertReturns(ETAG);
        when(requestFactory.createRequest(any(), any())).thenThrow(new UnknownHostException("fabric.invalid"));
        when(table.updateEntityWithResponse(any(), eq(TableEntityUpdateMode.MERGE), eq(true), isNull(), eq(Context.NONE)))
                .thenThrow(new IllegalStateException("synthetic version mismatch"));

        assertTrue(service(RestClient.builder().requestFactory(requestFactory)).requestReport(REQUEST).isEmpty());

        assertFailedUpdate();
        verify(table, never()).getEntity("1", REFERENCE);
    }

    @Test
    void returnsJobIdWhenFabricAcceptsSubmission() {
        insertReturns(ETAG);
        final RestClient.Builder builder = RestClient.builder().defaultHeader("CJSCPPUID", "synthetic-caller");
        final MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer synthetic-token"))
                .andExpect(headerDoesNotExist("CJSCPPUID"))
                .andRespond(withStatus(HttpStatus.ACCEPTED).header("Location", JOB_LOCATION));

        assertEquals(new ReportResult("synthetic-job", REFERENCE), service(builder).requestReport(REQUEST).orElseThrow());

        server.verify();
        assertNoStatusUpdate();
    }

    private void assertFailedUpdate() {
        final ArgumentCaptor<TableEntity> patch = ArgumentCaptor.forClass(TableEntity.class);
        verify(table).updateEntityWithResponse(
                patch.capture(), eq(TableEntityUpdateMode.MERGE), eq(true), isNull(), eq(Context.NONE));
        assertEquals("1", patch.getValue().getPartitionKey());
        assertEquals(REFERENCE, patch.getValue().getRowKey());
        assertEquals(ETAG, patch.getValue().getETag());
        assertEquals("FAILED", patch.getValue().getProperty("pipelineStatus"));
        assertEquals("", patch.getValue().getProperty("downloadUrl"));
        assertEquals(5, patch.getValue().getProperties().size());
    }

    private void assertNoStatusUpdate() {
        verify(table, never()).updateEntityWithResponse(any(), any(), any(Boolean.class), any(), any());
    }

    private void insertReturns(final String etag) {
        final HttpHeaders headers = new HttpHeaders();
        if (etag != null) {
            headers.set(HttpHeaderName.ETAG, etag);
        }
        when(table.createEntityWithResponse(any(), isNull(), eq(Context.NONE)))
                .thenReturn(new SimpleResponse<>(null, 204, headers, null));
    }

    private AuditReportsService service(final RestClient.Builder builder) {
        return new AuditReportsService(builder.build(), table, JsonMapper.builder().build(), null, FABRIC,
                new TokenRequestContext(), null,
                ignored -> new AccessToken("synthetic-token", OffsetDateTime.now().plusMinutes(10)));
    }
}
