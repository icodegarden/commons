package io.github.icodegarden.commons.gateway.spi.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.util.StringUtils;

import com.auth0.jwt.interfaces.DecodedJWT;

import io.github.icodegarden.commons.gateway.spi.JWTAuthenticationConverter;
import io.github.icodegarden.commons.springboot.loadbalancer.FlowTagLoadBalancer;
import io.github.icodegarden.commons.springboot.security.SpringUser;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class DefaultJWTAuthenticationConverter implements JWTAuthenticationConverter {

	public Authentication convertAuthentication(DecodedJWT decodedJWT) {
//		decodedJWT.getSubject();
		Long id = decodedJWT.getClaim("id").asLong();
		String username = decodedJWT.getClaim("username").asString();
		String platformRole = decodedJWT.getClaim("platformRole").asString();
		String flowTagRequired = decodedJWT.getClaim(FlowTagLoadBalancer.HTTPHEADER_FLOWTAG_REQUIRED).asString();
		String flowTagFirst = decodedJWT.getClaim(FlowTagLoadBalancer.HTTPHEADER_FLOWTAG_FIRST).asString();

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
			Map<String, Object> details = new HashMap<String, Object>(2, 1);
			details.put(FlowTagLoadBalancer.HTTPHEADER_FLOWTAG_REQUIRED, flowTagRequired);
			details.put(FlowTagLoadBalancer.HTTPHEADER_FLOWTAG_FIRST, flowTagFirst);
			authenticationToken.setDetails(details);
		}

		return authenticationToken;
	}

//	public <T> T getClaim(String name, Class<T> cla) {
//		return decodedJWT.getClaim(name).as(cla);
//	}
//
//	public LocalDateTime getExpiresAt() {
//		Date date = decodedJWT.getExpiresAt();
//		Instant instant = date.toInstant();
//		ZoneId zone = ZoneId.systemDefault();
//		return LocalDateTime.ofInstant(instant, zone);
//	}
}
