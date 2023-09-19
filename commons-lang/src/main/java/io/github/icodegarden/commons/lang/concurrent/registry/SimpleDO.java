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
class SimpleDO<ID> {

	private ID id;
	private Integer index;
	public SimpleDO(ID id, Integer index) {
		super();
		this.id = id;
		this.index = index;
	}
	
}
