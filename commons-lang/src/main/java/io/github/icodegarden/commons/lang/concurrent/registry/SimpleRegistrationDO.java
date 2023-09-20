package io.github.icodegarden.commons.lang.concurrent.registry;

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
public class SimpleRegistrationDO<ID> {

	private ID id;
	private Integer index;
	
	public SimpleRegistrationDO(ID id, Integer index) {
		super();
		this.id = id;
		this.index = index;
	}
	
}
