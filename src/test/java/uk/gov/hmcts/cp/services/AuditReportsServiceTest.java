package uk.gov.hmcts.cp.services;

import com.azure.core.http.rest.PagedIterable;
import com.azure.data.tables.TableClient;
import com.azure.data.tables.implementation.EntityPaged;
import com.azure.data.tables.models.TableEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuditReportsServiceTest {

    @Mock
    TableClient tableClient;

    @Mock
    EntityPaged<TableEntity> tableEntities;

    ObjectMapper mapper = JsonMapper.builder().configure(
            DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false
    ).build();

    AuditReportsService underTest;

    @Test
    void test_getReports() {

        underTest = new AuditReportsService(null, tableClient, mapper, null, null, null, null, null);

        // Given
        when(tableClient.listEntities()).thenReturn(new PagedIterable<>(() -> tableEntities));
        when(tableEntities.getValue()).thenReturn(List.of(
                new TableEntity("partitionKey", "rowKey").
                        setProperties(Map.of(
                                "userEmail", "no@where.com",
                                "pipelineJobId", "12345"
                        ))
        ));

        // When
        var result = underTest.getReports();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertNotNull(result.getFirst());

        assertEquals("12345", result.getFirst().pipelineJobId());
        assertEquals("no@where.com", result.getFirst().userEmail());
    }
}
