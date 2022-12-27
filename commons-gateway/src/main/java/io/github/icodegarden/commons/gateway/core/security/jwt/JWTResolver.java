package io.github.icodegarden.commons.gateway.core.security.jwt;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.util.StringUtils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import io.github.icodegarden.commons.springboot.security.SpringUser;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class JWTResolver {

	private static final Map<String, JWTVerifier> JWT_VERIFIER_MAP = new HashMap<String, JWTVerifier>();

	private DecodedJWT decodedJWT;

	public JWTResolver(JWTConfig jwtProperties, String jwt) throws JWTVerificationException {
		String secretKey = jwtProperties.getSecretKey();
		JWTVerifier v = JWT_VERIFIER_MAP.get(secretKey);
		if (v == null) {
			v = JWT.require(Algorithm.HMAC256(secretKey)).build();
			JWT_VERIFIER_MAP.put(secretKey, v);
		}

		decodedJWT = v.verify(jwt);
	}

	public Authentication getAuthentication() {
//		decodedJWT.getSubject();
		Long id = decodedJWT.getClaim("id").asLong();
		String username = decodedJWT.getClaim("username").asString();
		String platformRole = decodedJWT.getClaim("platformRole").asString();
		String flowTagRequired = decodedJWT.getClaim("flowTagRequired").asString();
		String flowTagFirst = decodedJWT.getClaim("flowTagFirst").asString();

		Collection<GrantedAuthority> authoritys;
		if (platformRole != null && !platformRole.isEmpty()) {
			authoritys = Arrays.asList(new SimpleGrantedAuthority(platformRole));
		} else {
			authoritys = Collections.emptyList();
		}

		SpringUser userDetails = new SpringUser(id.toString(), username, "", authoritys);
		PreAuthenticatedAuthenticationToken authenticationToken = new PreAuthenticatedAuthenticationToken(userDetails,
				"", authoritys);
		if (StringUtils.hasText(flowTagRequired) || StringUtils.hasText(flowTagFirst)) {
			Map<String, Object> details = new HashMap<String, Object>(1, 1);
			details.put("flowTagRequired", flowTagRequired);
			details.put("flowTagFirst", flowTagFirst);
			authenticationToken.setDetails(details);
		}

		return authenticationToken;
	}

	public <T> T getClaim(String name, Class<T> cla) {
		return decodedJWT.getClaim(name).as(cla);
	}

	public LocalDateTime getExpiresAt() {
		Date date = decodedJWT.getExpiresAt();
		Instant instant = date.toInstant();
		ZoneId zone = ZoneId.systemDefault();
		return LocalDateTime.ofInstant(instant, zone);
	}
}
