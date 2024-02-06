package com.itm.space.backendresources.service;

import com.itm.space.backendresources.api.request.UserRequest;
import com.itm.space.backendresources.api.response.UserResponse;
import com.itm.space.backendresources.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleMappingResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;

import javax.ws.rs.core.Response;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.UUID;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {
    @Mock
    private Keycloak keycloakClient;
    @Mock
    private UserMapper userMapper;
    @InjectMocks
    private UserServiceImpl userService;
    @Mock
    private UsersResource usersResource;
    @Mock
    private UserResource oneUserResource;
    @Mock
    private RealmResource realmResource;
    @Mock
    private RoleMappingResource roleMappingResource;
    @Mock
    private MappingsRepresentation mappingsRepresentation;
    @Value("${keycloak.realm}$")
    private String realm;

    private final String id = "26300dae-74a7-4d94-acae-cada5af24ee7";

    private  UserRequest userRequest;
    private UserResponse userResponse;
    @BeforeEach
    public void setUp(){
        userRequest = new UserRequest("username", "email@mail.ru", "123456", "firstName", "lastName");
        userResponse = new UserResponse("firstName", "lastName", "email@mail.ru", Collections.emptyList(), Collections.emptyList());
    }

    @Test
    public void createUserTest() throws Exception {
        when(keycloakClient.realm(realm)).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.create(any())).thenReturn(Response.created(new URI(id)).build());

        userService.createUser(userRequest);

        verify(keycloakClient).realm(realm);
        verify(realmResource).users();
        verify(usersResource).create(any());

    }

    @Test
    public void getUserByIdTest() {
        UserRepresentation userRepresentation = new UserRepresentation();
        RoleRepresentation roleRepresentation = new RoleRepresentation();
        GroupRepresentation groupRepresentation = new GroupRepresentation();

        when((keycloakClient.realm(realm))).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.get(id)).thenReturn(oneUserResource);

        userRepresentation.setId(id);
        userRepresentation.setFirstName("firstName");
        userRepresentation.setLastName("lastName");
        userRepresentation.setEmail("email@mail.ru");

        when(oneUserResource.toRepresentation()).thenReturn(userRepresentation);
        when(oneUserResource.roles()).thenReturn(roleMappingResource);
        when(roleMappingResource.getAll()).thenReturn(mappingsRepresentation);

        roleRepresentation.setName("testRole");
        when(mappingsRepresentation.getRealmMappings()).thenReturn(List.of(roleRepresentation));

        groupRepresentation.setName("testGroup");
        when((oneUserResource).groups()).thenReturn(List.of(groupRepresentation));

        when(userMapper.userRepresentationToUserResponse(userRepresentation,
                List.of(roleRepresentation), List.of(groupRepresentation))).thenReturn(userResponse);

        UserResponse userResponse1 = userService.getUserById(UUID.fromString(id));

        assertAll(
                () -> assertThat(userResponse1.getFirstName()).isEqualTo(userResponse.getFirstName()),
                () -> assertThat(userResponse1.getLastName()).isEqualTo(userResponse.getLastName()),
                () -> assertThat(userResponse1.getEmail()).isEqualTo(userResponse.getEmail()),
                () -> assertThat(userResponse1.getRoles()).isEqualTo(userResponse1.getRoles()),
                () -> assertThat(userResponse1.getGroups()).isEqualTo(userResponse.getGroups())
        );
    }

    @Test
    public void preparePasswordRepresentationTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        String password = "123456";

        Method method = UserServiceImpl.class.getDeclaredMethod("preparePasswordRepresentation", String.class);
        method.setAccessible(true);

        CredentialRepresentation credentialRepresentation = (CredentialRepresentation)
                method.invoke(userService, password);

        assertFalse(credentialRepresentation.isTemporary());
        assertEquals(CredentialRepresentation.PASSWORD, credentialRepresentation.getType());
        assertEquals(password, credentialRepresentation.getValue());

    }

    @Test
    public void prepareUserRepresentationTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
        credentialRepresentation.setTemporary(false);
        credentialRepresentation.setType(CredentialRepresentation.PASSWORD);
        credentialRepresentation.setValue("123456");

        Method method = UserServiceImpl.class.getDeclaredMethod("prepareUserRepresentation",
                UserRequest.class, CredentialRepresentation.class);
        method.setAccessible(true);


        UserRepresentation userRepresentation = (UserRepresentation) method
                .invoke(userService, userRequest, credentialRepresentation);

        assertAll(
                () -> assertNotNull(userRepresentation),
                () -> assertEquals("username", userRepresentation.getUsername()),
                () -> assertEquals("email@mail.ru", userRepresentation.getEmail()),
                () -> assertEquals(List.of(credentialRepresentation), userRepresentation.getCredentials()),
                () -> assertEquals("firstName", userRepresentation.getFirstName()),
                () -> assertEquals("lastName", userRepresentation.getLastName())
        );
    }
}
