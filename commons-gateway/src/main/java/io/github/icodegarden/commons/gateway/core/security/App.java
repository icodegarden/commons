package io.github.icodegarden.commons.gateway.core.security;

import java.util.HashSet;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Getter
@Setter
@ToString
public class App {

	private String appId;
	private String appKey;
	private String appName;
	private String flowTag;

	private Set<String> methods = new HashSet<>();
}
