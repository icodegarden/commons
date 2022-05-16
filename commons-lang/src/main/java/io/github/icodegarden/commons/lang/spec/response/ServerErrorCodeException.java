package io.github.icodegarden.commons.lang.spec.response;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.util.ClassUtils;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class ServerErrorCodeException extends ErrorCodeException {
	private static final long serialVersionUID = 1L;

	private static final Logger log = LoggerFactory.getLogger(ServerErrorCodeException.class);

	private static String applicationName = "NotConfig";

	static {
		if (ClassUtils.isPresent("io.github.icodegarden.commons.springboot.SpringContext",
				ClassUtils.getDefaultClassLoader())) {
			try {
				Class<?> springContextClass = ClassUtils.forName(
						"io.github.icodegarden.commons.springboot.SpringContext", ClassUtils.getDefaultClassLoader());
				Method getApplicationContextMethod = springContextClass.getDeclaredMethod("getApplicationContext");
				Object applicationContext = getApplicationContextMethod.invoke(null);
				Class<?> applicationContextClass = ClassUtils.forName("org.springframework.context.ApplicationContext",
						ClassUtils.getDefaultClassLoader());
				Method getBeanMethod = applicationContextClass.getMethod("getBean", Class.class);
				Environment env = (Environment) getBeanMethod.invoke(applicationContext, Environment.class);
				String applicationName = env.getRequiredProperty("spring.application.name");
				ServerErrorCodeException.configApplicationName(applicationName);
			} catch (Exception e) {
				log.error("WARN ex on init configApplicationName", e);
			}
		}
	}

	public static final String CODE = "20000";
	public static final String MSG = "Service Currently Unavailable";

	public static void configApplicationName(String applicationName) {
		ServerErrorCodeException.applicationName = applicationName;
	}

	public ServerErrorCodeException(Throwable cause) {
		super(CODE, MSG, String.format("server.%s.unknown-error", applicationName), MSG, cause);
	}

	public ServerErrorCodeException(String error_point, Throwable cause) {
		super(CODE, MSG, String.format("server.%s.%s-error", applicationName, error_point), MSG, cause);
	}

	public ServerErrorCodeException(String error_point, String sub_msg, Throwable cause) {
		super(CODE, MSG, String.format("server.%s.%s-error", applicationName, error_point), sub_msg, cause);
	}

	ServerErrorCodeException(String sub_code, String sub_msg) {
		super(CODE, MSG, sub_code, sub_msg);
	}

	@Override
	public int httpStatus() {
		return 500;
	}
}
