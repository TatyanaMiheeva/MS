package com.itm.space.backendresources.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.itm.space.backendresources.BaseIntegrationTest;
import com.itm.space.backendresources.api.request.UserRequest;
import lombok.SneakyThrows;
import org.apache.http.client.methods.RequestBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleMappingResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.MappingsRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.test.context.support.WithMockUser;
import javax.ws.rs.core.Response;
import java.net.URISyntaxException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;



@SpringBootTest
@WithMockUser(username = "username", password = "123456", authorities = "ROLE_MODERATOR")
public class UserControllerTest extends BaseIntegrationTest {
    @MockBean
    private Keycloak keycloak;
    @Value("${keycloak.realm}")
    private String realm;
    @Mock
    private RoleMappingResource roleMappingResource;
    @Mock
    private MappingsRepresentation mappingsRepresentation;
    private UserRequest userRequest;
    private RealmResource realmResource;
    private UsersResource usersResource;
    private UserRepresentation userRepresentation;
    private UserResource userResource;
    private String id;

    @BeforeEach
    void setUp() {
        userRequest = new UserRequest("username", "email@mail.ru", "123456", "firstName", "lastName");
        realmResource = mock(RealmResource.class);
        usersResource = mock(UsersResource.class);
        userRepresentation = mock(UserRepresentation.class);
        userResource = mock(UserResource.class);
        id = "26300dae-74a7-4d94-acae-cada5af24ee7";
    }


    @Test
    public void userCreatedTest() throws Exception {
        when(keycloak.realm(realm)).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.create(any())).thenReturn(Response.status(Response.Status.CREATED).build());
        when(userRepresentation.getId()).thenReturn(String.valueOf(UUID.fromString(id)));
        MockHttpServletResponse response = mvc.perform(requestWithContent(post("/api/users"), userRequest))
                .andReturn().getResponse();
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        verify(keycloak).realm(realm);
        verify(realmResource).users();
        verify(usersResource).create(any(UserRepresentation.class));
    }

    @Test
    public void getUserByIdTest() throws Exception {

        when(keycloak.realm(realm)).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.get(String.valueOf(id))).thenReturn(userResource);

        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setId(id);
        userRepresentation.setFirstName("firstName");
        userRepresentation.setLastName("lastName");
        userRepresentation.setEmail("email@mail.ru");

        when(userResource.toRepresentation()).thenReturn(userRepresentation);
        when(userResource.roles()).thenReturn(roleMappingResource);
        when(roleMappingResource.getAll()).thenReturn(mappingsRepresentation);

        MockHttpServletResponse response = mvc.perform(get("/api/users/" + id))
                .andDo(print())
                .andExpect(jsonPath("$.firstName").value("firstName"))
                .andReturn()
                .getResponse();
        assertEquals(HttpStatus.OK.value(), response.getStatus());
    }

    @Test
    public void helloControllerTest() throws Exception {
        MockHttpServletResponse mockHttpServletResponse = mvc.perform(get("/api/users/hello"))
                .andReturn()
                .getResponse();
        assertEquals(HttpStatus.OK.value(), mockHttpServletResponse.getStatus());
        assertEquals("username", mockHttpServletResponse.getContentAsString());
    }
}
