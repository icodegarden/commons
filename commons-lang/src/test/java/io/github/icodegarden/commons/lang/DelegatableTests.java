package io.github.icodegarden.commons.lang;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * 
 * @author Fangfang.Xu
 *
 */
class DelegatableTests {

	private class D1 extends ArrayList implements Delegatable {

	}

	private class D2 implements Delegatable {
		Delegatable d;

		D2(Delegatable d) {
			this.d = d;
		}

		@Override
		public Delegatable getDelegatable() {
			return d;
		}
	}

	D1 d1 = new D1();
	D2 d2 = new D2(d1);

	@Test
	public void testinstanceOf() throws Exception {
		boolean instanceOf = d2.instanceOf(List.class);
		assertTrue(instanceOf);
	}

	@Test
	public void testinstanceOf2() throws Exception {
		List ofType = d2.ofType(List.class);
		assertNotNull(ofType);
	}
}
