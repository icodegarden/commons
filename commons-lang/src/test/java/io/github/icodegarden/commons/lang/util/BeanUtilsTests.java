//package io.github.icodegarden.commons.lang.util;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.doReturn;
//import static org.mockito.Mockito.mock;
//
//import java.util.Arrays;
//import java.util.LinkedList;
//import java.util.List;
//
//import org.junit.jupiter.api.Test;
//
//import io.github.icodegarden.commons.lang.NamedObject;
//import io.github.icodegarden.commons.lang.NamedObjectReader;
//import io.github.icodegarden.commons.lang.NamesCachedObjectReader;
//import io.github.icodegarden.commons.lang.registry.RegisteredInstance;
//
///**
// * 
// * @author Fangfang.Xu
// *
// */
//class BeanUtilsTests {
//	
//	class UserParent {
//		protected String pname;
//	}
//	
//	class User extends UserParent {
//		private Long id;
//		private String name;
//		private Group group;
//	}
//	
//	class Group{
//		private String name;
//	}
//
//	@Test
//	void copyProperties() throws Exception {
//		Group group = new Group();
//		group.name = "g1";
//		
//		User user = new User();
//		user.pname = "pn";
//		user.id = 1000L;
//		user.name = "xff";
//		user.group = group;
//
//		User user2 = new User();
//		BeanUtils.copyProperties(user, user2);
//		
//		assertThat(user2.id).isEqualTo(user.id);
//		assertThat(user2.name).isEqualTo(user.name);
//		assertThat(user2.group).isEqualTo(user.group);
//		assertThat(user2.pname).isEqualTo(user.pname);
//	}
//
//}
