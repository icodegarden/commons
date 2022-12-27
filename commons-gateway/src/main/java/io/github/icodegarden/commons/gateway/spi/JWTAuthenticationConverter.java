package io.github.icodegarden.commons.gateway.spi;

import org.springframework.security.core.Authentication;

import com.auth0.jwt.interfaces.DecodedJWT;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface JWTAuthenticationConverter {

	Authentication convertAuthentication(DecodedJWT decodedJWT);
}
