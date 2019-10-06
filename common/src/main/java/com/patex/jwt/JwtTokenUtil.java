package com.patex.jwt;

import com.patex.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class JwtTokenUtil {
    public static final long JWT_TOKEN_VALIDITY = 50 * 60 * 60;
    public static final String AUTHORITIES = "authorities";


    private final String secret;

    public JwtTokenUtil(@Value("${jwt.secret}") String secret) {
        this.secret = secret;
    }

    public User getUser(String token) {
        Claims claims = getAllClaimsFromToken(token);
        final Date expiration = claims.getExpiration();
        boolean enabled = !expiration.before(new Date());
        String userName = claims.getSubject();
        @SuppressWarnings("unchecked")
        List<String> authorities = (List<String>) claims.get(AUTHORITIES);
        return new User(userName, enabled, authorities, token);
    }

    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
    }

    public String generateToken(User userDetails) {
        List<String> authorities = userDetails.getAuthorities();
        String username = userDetails.getUsername();
        return generateToken(username, authorities);
    }

    public String generateToken(String username,List<String> authorities) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(AUTHORITIES, authorities);
        return doGenerateToken(claims, username);
    }

    public String generateToken(String username,String... authorities) {
        return generateToken(username, Arrays.asList(authorities));
    }


    private String doGenerateToken(Map<String, Object> claims, String subject) {
        return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY * 1000))
                .signWith(SignatureAlgorithm.HS512, secret).compact();
    }

}
