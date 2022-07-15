package org.sms.io.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.function.Function;

@Component
@Slf4j
public class ClientJwtTokenUtility {

    public UserToken getTokenFromString(String token) {
        UserToken userToken = null;

        try {
            final Claims tokenClaims = getClaimsFromToken(token, Function.identity());
            userToken = new UserToken(tokenClaims);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return userToken;
    }

    private <T> T getClaimsFromToken(String token, Function<Claims, T> claimsResolver) {
        Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaimsFromToken(String token) {
        final String noSignatureToken = token.replaceFirst("[^\\.]*$", "");
        return (Claims) Jwts.parser().parse(noSignatureToken).getBody();
    }
}
