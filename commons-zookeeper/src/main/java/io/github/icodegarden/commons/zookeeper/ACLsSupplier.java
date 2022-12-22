package io.github.icodegarden.commons.zookeeper;

import java.util.List;
import java.util.function.Supplier;

import org.apache.zookeeper.data.ACL;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface ACLsSupplier extends Supplier<List<ACL>> {

	static class NullACLsSupplier implements ACLsSupplier{
		@Override
		public List<ACL> get() {
			return null;
		}
	}
}
