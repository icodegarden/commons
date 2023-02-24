package io.github.icodegarden.commons.test.web.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;

import io.github.icodegarden.commons.test.web.mapper.ConsumerSystemMapper;
import io.github.icodegarden.commons.test.web.pojo.persistence.ConsumerSystemPO;
import reactor.core.publisher.Mono;

@Service
public class ConsumerSystemService {

	@Autowired
	ConsumerSystemMapper consumerSystemMapper;
	
	// single TransactionalOperator shared amongst all methods in this instance
    private final TransactionalOperator transactionalOperator;

    // use constructor-injection to supply the ReactiveTransactionManager
    public ConsumerSystemService(ReactiveTransactionManager transactionManager) {
        this.transactionalOperator = TransactionalOperator.create(transactionManager);
    }


	public Mono<Object> create() {
		return Mono.from( Mono.fromCallable(() -> {
			ConsumerSystemPO po = new ConsumerSystemPO();
			po.setId(System.currentTimeMillis());
			po.setActived(false);
			po.setAppId(System.currentTimeMillis() + "");
			po.setEmail("abc");
			po.setName(System.currentTimeMillis() + "");
			po.setSaslPassword("aaa");
			po.setSaslUsername("aaa");

			consumerSystemMapper.add(po);
			
//			int i=1/0;

			return po.getId();
		}).as(transactionalOperator::transactional));

	}
}
