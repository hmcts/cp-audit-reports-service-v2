package uk.gov.hmcts.cp.integration;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.cp.services.CaseSearchService;
import uk.gov.hmcts.cp.services.MaterialSearchService;
import uk.gov.hmcts.cp.services.UserSearchService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ControllersIntegrationTest extends IntegrationTestBase {

    @MockitoBean
    CaseSearchService caseSearchService;

    @MockitoBean
    UserSearchService userSearchService;

    @MockitoBean
    MaterialSearchService materialSearchService;

    @Test
    void case_search_by_ids() throws Exception {

        mockMvc.perform(get("/case/id?caseIds=123,456")).
                andDo(print()).
                andExpect(status().isOk()).
                andExpect(jsonPath("$.results").isArray());
    }

    @Test
    void case_search_by_urns() throws Exception {

        mockMvc.perform(get("/case/urn?caseUrns=abc,def")).
                andDo(print()).
                andExpect(status().isOk()).
                andExpect(jsonPath("$.results").isArray());
    }

    @Test
    void user_search_by_ids() throws Exception {

        mockMvc.perform(get("/user/id?userIds=123,456")).
                andDo(print()).
                andExpect(status().isOk()).
                andExpect(jsonPath("$.results").isArray());
    }

    @Test
    void user_search_by_emails() throws Exception {

        mockMvc.perform(get("/user/email?emails=no@where.com")).
                andDo(print()).
                andExpect(status().isOk()).
                andExpect(jsonPath("$.results").isArray());
    }

    @Test
    void material_search_by_ids() throws Exception {

        mockMvc.perform(get("/material/id?materialIds=123,456")).
                andDo(print()).
                andExpect(status().isOk()).
                andExpect(jsonPath("$.results").isArray());
    }
}
