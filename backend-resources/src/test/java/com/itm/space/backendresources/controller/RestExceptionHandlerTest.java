package com.itm.space.backendresources.controller;

import com.itm.space.backendresources.BaseIntegrationTest;
import com.itm.space.backendresources.api.request.UserRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.test.context.support.WithMockUser;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WithMockUser(authorities = "ROLE_MODERATOR")
class RestExceptionHandlerTest extends BaseIntegrationTest {

    @Test
    void handleExceptionTest() throws Exception {
        String id = "26300dae-74a7-4d94-acae-cada5af24ee7";
        mvc.perform(get("/api/users/" + id))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void handleInvalidArgumentTest() throws Exception {

        UserRequest testUser = new UserRequest("username",
                null, "123456",
                 "firstName", "lastName");
        MockHttpServletResponse response = mvc.perform(requestWithContent(post("/api/users"), testUser))
                .andReturn().getResponse();
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());

//        String testContent = """
//                { "username": "username",
//                "email": "null",
//                "password": "123456",
//                "firstName":"firstName",
//                "lastName": "lastName" }""";
//        mvc.perform(post("/api/users")
//                        .content(testContent)
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isBadRequest());
    }
}
