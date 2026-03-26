package uk.gov.hmcts.cp.integration;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tools.jackson.databind.ObjectMapper;
import uk.gov.hmcts.cp.services.CaseSearchService;
import uk.gov.hmcts.cp.services.MaterialSearchService;
import uk.gov.hmcts.cp.services.UserSearchService;

import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WireMockTest(httpPort = 8080)
class ControllersIntegrationTest extends IntegrationTestBase {

    @MockitoBean
    CaseSearchService caseSearchService;

    @MockitoBean
    UserSearchService userSearchService;

    @MockitoBean
    MaterialSearchService materialSearchService;

    @BeforeEach
    void setUp() {
        stubFor(WireMock.get("/usersgroups-query-api/query/api/rest/usersgroups/users/logged-in-user/permissions").
                willReturn(okForContentType(
                        "application/vnd.usersgroups.get-logged-in-user-permissions+json",
                        new ObjectMapper().writeValueAsString(Map.of(
                                "groups", List.of(Map.of(
                                        "groupId", "123",
                                        "groupName", "Auditors",
                                        "prosecutingAuthority", "CPS"
                                )),
                                "switchableRoles", List.of(),
                                "permissions", List.of(Map.of())
                        ))
                ))
        );
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
