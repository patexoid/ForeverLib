package com.patex.zombie.user;

import com.patex.jwt.JwtTokenUtil;
import com.patex.model.Creds;
import com.patex.model.User;
import com.patex.zombie.user.entities.AuthorityEntity;
import com.patex.zombie.user.entities.UserEntity;
import com.patex.zombie.user.entities.UserRepository;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {UserApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration(exclude = DataSourceAutoConfiguration.class)
public class CreateTokenIt {

    @LocalServerPort
    int randomPort;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private JwtTokenUtil tokenUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;
    public static final String USERNAME = "user";
    public static final String PASSWORD = "password";
    public static final String TEST_AUTHORITY = "ROLE_TEST_AUTHORITY";

    @Before
    public void setUp() throws Exception {
        UserEntity userEntity = new UserEntity(USERNAME, passwordEncoder.encode(PASSWORD));
        userEntity.getAuthorities().add(new AuthorityEntity(TEST_AUTHORITY));
        when(userRepository.findById(USERNAME)).thenReturn(Optional.of(userEntity));
    }

    @Test
    public void shouldReturnToken() {
        String host = "http://localhost:" + randomPort;
        RestTemplate template = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(tokenUtil.generateToken("machine", Collections.singletonList("ROLE_GET_TOKEN")));
        HttpEntity<Creds> requestEntity = new HttpEntity<>(new Creds(USERNAME, PASSWORD), headers);
        ResponseEntity<String> exchange = template.exchange(host + "/authenticate", HttpMethod.POST, requestEntity, String.class);
        String jwt = exchange.getBody();

        User user = tokenUtil.getUser(jwt);
        assertEquals(USERNAME, user.getUsername());
        assertEquals(1, user.getAuthorities().size());
        assertEquals(TEST_AUTHORITY, user.getAuthorities().get(0));
    }

    @Test
    public void shouldReturn401() {
        String host = "http://localhost:" + randomPort;
        RestTemplate template = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(tokenUtil.generateToken("machine", Collections.singletonList("ROLE_GET_TOKEN")));
        HttpEntity<Creds> requestEntity = new HttpEntity<>(new Creds(USERNAME, "WrongPassword"), headers);
        ResponseEntity<String> exchange = template.exchange(host + "/authenticate", HttpMethod.POST, requestEntity, String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getStatusCode());
    }
}
