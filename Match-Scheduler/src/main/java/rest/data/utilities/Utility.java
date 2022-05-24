package rest.data.utilities;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import rest.data.entities.AppRole;
import rest.data.entities.AppUser;
import rest.repos.AppUserRepo;
import rest.services.AppUserService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class Utility {
    private static final int ACCESS_TOKEN_EXPIRES_IN_MINUTES = 60;
    private static final int REFRESH_TOKEN_EXPIRES_IN_MINUTES = 300;
    private static final Algorithm algorithm = Algorithm.HMAC256("secret".getBytes());

    public static TokenData getTokens(HttpServletRequest request, User user) {
        String access_token = Utility.getAccessToken(request, user);
        String refresh_token = Utility.getRefreshToken(request, user);
        TokenData result = new TokenData();
        result.setAccessToken(access_token);
        result.setRefreshToken(refresh_token);
        return result;
    }

    private static String getAccessToken(HttpServletRequest request, User user) {
        return JWT.create()
                .withSubject(user.getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRES_IN_MINUTES * 60 * 1000))
                .withIssuer(request.getRequestURI().toString())
                .withClaim("roles", user.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
                .sign(algorithm);
    }

    public static String getNewAccessToken(HttpServletRequest request, AppUser user) {
        return JWT.create()
                .withSubject(user.getEmail())
                .withExpiresAt(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRES_IN_MINUTES * 60 * 1000))
                .withIssuer(request.getRequestURI().toString())
                .withClaim("roles", user.getRoles().stream().map(AppRole::getName).collect(Collectors.toList()))
                .sign(algorithm);
    }

    private static String getRefreshToken(HttpServletRequest request, User user) {
        return JWT.create()
                .withSubject(user.getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRES_IN_MINUTES * 60 * 1000))
                .withIssuer(request.getRequestURI().toString())
                .withClaim("roles", user.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
                .sign(algorithm);
    }

    public static UsernamePasswordAuthenticationToken getTokenForFilter(String authorizationHeader) {
        String token = authorizationHeader.substring("Bearer ".length());
        JWTData result = getDataFromJWT(token);
        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
        Arrays.stream(result.getRoles()).forEach(role -> {
            authorities.add(new SimpleGrantedAuthority(role));
        });
        return new UsernamePasswordAuthenticationToken(result.getEmail(), null, authorities);
    }

    public static JWTData getDataFromJWT(String token) {
        JWTData result = new JWTData();
        Algorithm algorithm = Algorithm.HMAC256("secret".getBytes());
        JWTVerifier verifier = JWT.require(algorithm).build();
        DecodedJWT decodedJWT = verifier.verify(token);
        String email = decodedJWT.getSubject();
        String[] roles = decodedJWT.getClaim("roles").asArray(String.class);
        result.setEmail(email);
        result.setRoles(roles);
        return result;
    }

    public static HttpServletResponse writeResponseBodyRefreshToken(HttpServletRequest request ,HttpServletResponse response, AppUserService userService, String refresh_token) throws IOException {
        JWTData jwtData = Utility.getDataFromJWT(refresh_token);
        AppUser user = userService.findByEmail(jwtData.getEmail());
        String access_token = Utility.getNewAccessToken(request, user);
        TokenData token = new TokenData();
        token.setAccessToken(access_token);
        token.setRefreshToken(refresh_token);
        response.setContentType(APPLICATION_JSON_VALUE);
        new ObjectMapper().writeValue(response.getOutputStream(), token);
        return response;
    }

    public static HttpServletResponse writeResponseBodyError(HttpServletResponse response, String message) throws IOException {
        Map<String, String> error = new HashMap<>();
        response.setStatus(FORBIDDEN.value());
        error.put("error_message", message);
        response.setContentType(APPLICATION_JSON_VALUE);
        new ObjectMapper().writeValue(response.getOutputStream(), error);
        return response;
    }
}
