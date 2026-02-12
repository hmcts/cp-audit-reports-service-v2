package uk.gov.hmcts.cp.http;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.hmcts.cp.http.base.ApiTestBase;
import uk.gov.hmcts.cp.openapi.model.GetCaseIdsForMaterialIds200Response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MaterialSearchApiTest extends ApiTestBase<GetCaseIdsForMaterialIds200Response> {

    public MaterialSearchApiTest() {
        super(GetCaseIdsForMaterialIds200Response.class);
    }

    @Test
    void materials_endpoint_returns_material_for_materialId() {

        // When
        final ResponseEntity<GetCaseIdsForMaterialIds200Response> response =
                get("/material/id?materialIds=456");

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getResults().size());
        assertEquals("456", response.getBody().getResults().get(0).getMaterialId());
    }

    @Test
    void materials_endpoint_returns_materials_for_materialIds() {

        // When
        final ResponseEntity<GetCaseIdsForMaterialIds200Response> response =
                get("/material/id?materialIds=567,678");

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().getResults().size());
        assertEquals("567", response.getBody().getResults().get(0).getMaterialId());
        assertEquals("678", response.getBody().getResults().get(1).getMaterialId());
    }

    @Test
    void materials_endpoint_returns_400_error_for_missing_materialIds() {

        // When
        final HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () ->
                get("/material/id"));

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }
}
