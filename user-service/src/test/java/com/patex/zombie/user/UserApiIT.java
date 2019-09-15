package com.patex.zombie.user;


import com.patex.jwt.JwtTokenUtil;
import com.patex.model.User;
import com.patex.zombie.user.controller.UserCreateRequest;
import com.patex.zombie.user.entities.UserRepository;
import com.patex.zombie.user.service.UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {UserApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration(exclude = DataSourceAutoConfiguration.class)
public class UserApiIT {

    public static final String USERNAME = "user";
    public static final String PASSWORD = "password";
    public static final String TEST_AUTHORITY = "ROLE_TEST_AUTHORITY";

    @LocalServerPort
    int randomPort;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private JwtTokenUtil tokenUtil;

    @Before
    public void setUp() throws Exception {
        when(userRepository.save(any())).then(o -> o.getArguments()[0]);
    }

    @Test
    public void shouldCreateAdminUser() {
        String host = "http://localhost:" + randomPort;
        RestTemplate template = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername(USERNAME);
        request.setPassword(PASSWORD);
        request.setAuthorities(Collections.singletonList(TEST_AUTHORITY));
        HttpEntity<UserCreateRequest> requestEntity = new HttpEntity<>(request, headers);
        ResponseEntity<User> exchange = template.exchange(host + "/user/create", HttpMethod.POST, requestEntity, User.class);
        User createdUser = exchange.getBody();
        assertEquals(USERNAME, createdUser.getUsername());
        assertEquals(3, createdUser.getAuthorities().size());
        assertTrue(createdUser.getAuthorities().contains(TEST_AUTHORITY));
        assertTrue(createdUser.getAuthorities().contains(UserService.ADMIN_AUTHORITY));
        assertTrue(createdUser.getAuthorities().contains(UserService.USER));
    }

    @Test
    public void shouldCreateUser() {
        String host = "http://localhost:" + randomPort;
        RestTemplate template = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokenUtil.generateToken("machine", Collections.singletonList(UserService.ADMIN_AUTHORITY)));
        headers.setContentType(MediaType.APPLICATION_JSON);
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername(USERNAME);
        request.setPassword(PASSWORD);
        request.setAuthorities(Collections.singletonList(TEST_AUTHORITY));
        HttpEntity<UserCreateRequest> requestEntity = new HttpEntity<>(request, headers);
        ResponseEntity<User> exchange = template.exchange(host + "/user/create", HttpMethod.POST, requestEntity, User.class);
        User createdUser = exchange.getBody();
        assertEquals(USERNAME, createdUser.getUsername());
        assertEquals(2, createdUser.getAuthorities().size());
        assertTrue(createdUser.getAuthorities().contains(TEST_AUTHORITY));
        assertTrue(createdUser.getAuthorities().contains(UserService.USER));
    }


}
