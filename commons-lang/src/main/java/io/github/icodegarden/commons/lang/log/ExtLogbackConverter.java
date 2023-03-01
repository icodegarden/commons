package io.github.icodegarden.commons.lang.log;

import java.lang.reflect.Method;

import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import ch.qos.logback.classic.pattern.MessageConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import io.github.icodegarden.commons.lang.trace.TraceCtx;
import io.github.icodegarden.commons.lang.util.SystemUtils;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class ExtLogbackConverter extends MessageConverter {

	private static Method getUserIdM;
	private static Method getUsernameM;
	private static Method getRequestIdM;

	static {
		try {
			if (ClassUtils.isPresent("io.github.icodegarden.commons.springboot.security.SecurityUtils", null)) {
				Class<?> cla = ClassUtils.forName("io.github.icodegarden.commons.springboot.security.SecurityUtils",
						null);
				getUserIdM = cla.getDeclaredMethod("getUserId");

				getUsernameM = cla.getDeclaredMethod("getUsername");
			}

			if (ClassUtils.isPresent("io.github.icodegarden.commons.springboot.web.util.WebUtils", null)) {
				Class<?> cla = ClassUtils.forName("io.github.icodegarden.commons.springboot.web.util.WebUtils", null);
				getRequestIdM = cla.getDeclaredMethod("getRequestId");
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private TraceCtx traceCtx = TraceCtx.get();

	@Override
	public String convert(ILoggingEvent event) {
		try {
			StringBuilder sb = new StringBuilder(150);

			sb.append("serverName:").append(SystemUtils.Server.getServerName());

			String traceId = traceCtx.traceId();
			if (StringUtils.hasText(traceId)) {
				sb.append(",traceId:").append(traceId);
			}

			String spanId = traceCtx.spanId();
			if (StringUtils.hasText(spanId)) {
				sb.append(",spanId:").append(spanId);
			}

			String userId = getUserId();
			if (StringUtils.hasText(userId)) {
				sb.append(",userId:").append(userId);
			}

			String username = getUsername();
			if (StringUtils.hasText(username)) {
				sb.append(",username:").append(username);
			}

			String requestId = getRequestId();
			if (StringUtils.hasText(requestId)) {
				sb.append(",requestId:").append(requestId);
			}

			Object[] args = event.getArgumentArray();
			if (args != null) {
				for (int i = 0; i < args.length; i++) {
					Object arg = args[i];
					if (arg instanceof LogExt) {
						LogExt ext = (LogExt) arg;
						sb.append(",").append(ext.key()).append(":").append(ext.value());
					}
				}
			}
			return sb.toString();
		} catch (Exception e) {
			return "convert:error cause " + e.toString();
		}
	}

	private static String getUserId() throws Exception {
		if (getUserIdM != null) {
			Object userId = getUserIdM.invoke(null);
			if (userId != null) {
				return userId.toString();
			}
		}
		return null;
	}

	private static String getUsername() throws Exception {
		if (getUsernameM != null) {
			Object username = getUsernameM.invoke(null);
			if (username != null) {
				return username.toString();
			}
		}
		return null;
	}

	private static String getRequestId() throws Exception {
		if (getRequestIdM != null) {
			Object requestId = getRequestIdM.invoke(null);
			if (requestId != null) {
				return requestId.toString();
			}
		}
		return null;
	}

}