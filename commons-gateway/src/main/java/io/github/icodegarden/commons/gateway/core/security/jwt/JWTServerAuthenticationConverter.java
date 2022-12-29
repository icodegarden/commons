package io.github.icodegarden.commons.gateway.core.security.jwt;

import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;

import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;

import io.github.icodegarden.commons.gateway.spi.JWTAuthenticationConverter;
import io.github.icodegarden.commons.gateway.spi.JWTTokenExtractor;
import io.github.icodegarden.commons.lang.spec.response.ClientParameterInvalidErrorCodeException;
import io.github.icodegarden.commons.springboot.exception.ErrorCodeAuthenticationException;
import reactor.core.publisher.Mono;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class JWTServerAuthenticationConverter implements ServerAuthenticationConverter {

	private final String secretKey;
	private final JWTTokenExtractor jwtTokenExtractor;
	private final JWTAuthenticationConverter jwtAuthenticationConverter;

	public JWTServerAuthenticationConverter(String secretKey, JWTTokenExtractor jwtTokenExtractor,
			JWTAuthenticationConverter jwtAuthenticationConverter) {
		this.secretKey = secretKey;
		this.jwtTokenExtractor = jwtTokenExtractor;
		this.jwtAuthenticationConverter = jwtAuthenticationConverter;
	}

	@Override
	public Mono<Authentication> convert(ServerWebExchange exchange) {
		return Mono.defer(() -> {
			String jwt = jwtTokenExtractor.extract(exchange);

			if (StringUtils.hasText(jwt)) {
				try {
					DecodedJWT decodedJWT = JWTDecoder.decode(secretKey, jwt);
					Authentication authentication = jwtAuthenticationConverter.convertAuthentication(decodedJWT);
					return Mono.just(authentication);
				} catch (TokenExpiredException e) {
					throw new ErrorCodeAuthenticationException(new ClientParameterInvalidErrorCodeException(
							ClientParameterInvalidErrorCodeException.SubPair.INVALID_SIGNATURE.getSub_code(),
							"Not Authenticated, Token Expired"));
				} catch (JWTDecodeException | SignatureVerificationException e) {
					throw new ErrorCodeAuthenticationException(new ClientParameterInvalidErrorCodeException(
							ClientParameterInvalidErrorCodeException.SubPair.INVALID_SIGNATURE.getSub_code(),
							"Not Authenticated, Token Invalid"));
				} catch (JWTVerificationException e) {
					throw new AuthenticationServiceException("Verification Token Error", e);
				}
			}

			return Mono.empty();
		});
	}
}