package com.fededev.cloudstorage.infraestructure.security.utils;

import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fededev.cloudstorage.infraestructure.security.CreateJWTTokenDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.JWT;

import java.time.Instant;
import java.util.UUID;

@Component
public class JwtUtils {

    @Value("${jwt.expiration}")
    private long expiration;

    @Value("${jwt.secret}")
    private String key;

    @Value("${jwt.issuer}")
    private String issuer;

    public String generateToken(CreateJWTTokenDto dto){
        Algorithm algorithm = Algorithm.HMAC256(this.key);

        Instant now = Instant.now();
        Instant exp = now.plusSeconds(this.expiration);

        return JWT.create()
                .withIssuer(this.issuer)
                .withIssuedAt(now)
                .withExpiresAt(exp)
                .withJWTId(UUID.randomUUID().toString())
                .withNotBefore(now)
                .withClaim("email", dto.email())
                .withClaim("deleted", dto.deleted())
                .withSubject(dto.userId().toString())
                .sign(algorithm);
    }

    public DecodedJWT validateToken(String token){
        Algorithm algorithm = Algorithm.HMAC256(key);

        JWTVerifier verifier = JWT.require(algorithm)
                .withIssuer(this.issuer)
                .build();

        DecodedJWT decodedJWT = verifier.verify(token);

        return decodedJWT;
    }



}
