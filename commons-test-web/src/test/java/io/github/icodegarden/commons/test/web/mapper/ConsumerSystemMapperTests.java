package io.github.icodegarden.commons.test.web.mapper;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import io.github.icodegarden.commons.lang.util.SystemUtils;
import io.github.icodegarden.commons.test.web.pojo.persistence.ConsumerSystemPO;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Transactional
@SpringBootTest
public class ConsumerSystemMapperTests {

	@Autowired
	private ConsumerSystemMapper consumerSystemMapper;

	@Test
	public void findOne() throws Exception {
		ConsumerSystemPO po = new ConsumerSystemPO();
		po.setId(System.currentTimeMillis());
		po.setName("1-" + System.currentTimeMillis() + "");
		po.setAppId(System.currentTimeMillis() + "");
		po.setEmail("e");
		po.setSaslPassword("aaa");
		po.setSaslUsername("aaa");

		po.setCreatedBy("xff");
		po.setCreatedAt(SystemUtils.now());
		po.setUpdatedBy("xff");
		po.setUpdatedAt(po.getUpdatedAt());
		consumerSystemMapper.add(po);

		ConsumerSystemPO findOne = consumerSystemMapper.findOne(po.getId(), null);
		Assertions.assertThat(findOne).isNotNull();
	}
}