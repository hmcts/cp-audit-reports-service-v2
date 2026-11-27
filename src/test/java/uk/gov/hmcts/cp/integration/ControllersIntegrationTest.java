package uk.gov.hmcts.cp.integration;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tools.jackson.databind.ObjectMapper;
import uk.gov.hmcts.cp.services.CaseSearchService;
import uk.gov.hmcts.cp.services.MaterialSearchService;
import uk.gov.hmcts.cp.services.ReportBundleService;
import uk.gov.hmcts.cp.services.UserSearchService;

import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WireMockTest(httpPort = 8080)
class ControllersIntegrationTest extends IntegrationTestBase {
    private static final String BUNDLE_REFERENCE = "20261127-abc123";
    private static final String BUNDLE_ROUTE = "/reports/bundle";
    private static final Map<String, String> CREATE_AUDIT =
            Map.of("object", "Crime Platform Audit", "action", "create");

    @MockitoBean
    CaseSearchService caseSearchService;

    @MockitoBean
    UserSearchService userSearchService;

    @MockitoBean
    MaterialSearchService materialSearchService;

    @MockitoBean
    ReportBundleService reportBundleService;

    @BeforeEach
    void setUp() {
        stubPermissions("Auditors", List.of());
    }

    private void stubPermissions(final String groupName, final List<Map<String, String>> permissions) {
        stubFor(WireMock.get("/usersgroups-query-api/query/api/rest/usersgroups/users/logged-in-user/permissions").
                willReturn(okForContentType(
                        "application/vnd.usersgroups.get-logged-in-user-permissions+json",
                        new ObjectMapper().writeValueAsString(Map.of(
                                "groups", List.of(Map.of(
                                        "groupId", "123",
                                        "groupName", groupName,
                                        "prosecutingAuthority", "CPS"
                                )),
                                "switchableRoles", List.of(),
                                "permissions", permissions
                        ))
                ))
        );
    }

    @Test
    void auditorWithoutAuditPermissionCannotDownloadBundle() throws Exception {

        mockMvc.perform(get(BUNDLE_ROUTE).param("reference", BUNDLE_REFERENCE).header("CJSCPPUID", "someId"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(reportBundleService);
    }

    @Test
    void nonAuditorWithAuditPermissionCannotDownloadBundle() throws Exception {
        stubPermissions("Crown Prosecutors", List.of(CREATE_AUDIT));

        mockMvc.perform(get(BUNDLE_ROUTE).param("reference", BUNDLE_REFERENCE).header("CJSCPPUID", "someId"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(reportBundleService);
    }

    @Test
    void standardAuditorCanDownloadBothWorkbooksAsOnePrivateAttachment() throws Exception {
        stubPermissions("Auditors", List.of(CREATE_AUDIT));
        final byte[] archive = {1, 2, 3};
        when(reportBundleService.download(BUNDLE_REFERENCE)).thenReturn(archive);

        mockMvc.perform(get(BUNDLE_ROUTE).param("reference", BUNDLE_REFERENCE)
                        .header("CJSCPPUID", "someId").accept("application/zip"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/zip"))
                .andExpect(content().bytes(archive))
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(header().string("Content-Disposition",
                        "attachment; filename=\"audit-report-" + BUNDLE_REFERENCE + "-workbooks.zip\""));
    }

    @Test
    void case_search_by_ids() throws Exception {

        mockMvc.perform(get("/case/id?caseIds=123,456").header("CJSCPPUID", "someId")).
                andDo(print()).
                andExpect(status().isOk()).
                andExpect(jsonPath("$.results").isArray());
    }

    @Test
    void case_search_by_urns() throws Exception {

        mockMvc.perform(get("/case/urn?caseUrns=abc,def").header("CJSCPPUID", "someId")).
                andDo(print()).
                andExpect(status().isOk()).
                andExpect(jsonPath("$.results").isArray());
    }

    @Test
    void user_search_by_ids() throws Exception {

        mockMvc.perform(get("/user/id?userIds=123,456").header("CJSCPPUID", "someId")).
                andDo(print()).
                andExpect(status().isOk()).
                andExpect(jsonPath("$.results").isArray());
    }

    @Test
    void user_search_by_emails() throws Exception {

        mockMvc.perform(get("/user/email?emails=no@where.com").header("CJSCPPUID", "someId")).
                andDo(print()).
                andExpect(status().isOk()).
                andExpect(jsonPath("$.results").isArray());
    }

    @Test
    void material_search_by_ids() throws Exception {

        mockMvc.perform(get("/material/id?materialIds=123,456").header("CJSCPPUID", "someId")).
                andDo(print()).
                andExpect(status().isOk()).
                andExpect(jsonPath("$.results").isArray());
    }
}
