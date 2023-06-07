//package io.github.icodegarden.commons.test.web;
//
//import javax.sql.DataSource;
//
//import org.springframework.jdbc.datasource.ConnectionHolder;
//import org.springframework.jdbc.datasource.DataSourceTransactionManager;
//import org.springframework.jdbc.datasource.JdbcTransactionObjectSupport;
//import org.springframework.transaction.TransactionDefinition;
//import org.springframework.transaction.TransactionException;
//import org.springframework.transaction.reactive.AbstractReactiveTransactionManager;
//import org.springframework.transaction.reactive.GenericReactiveTransaction;
//import org.springframework.transaction.reactive.TransactionSynchronizationManager;
//import org.springframework.transaction.support.DefaultTransactionStatus;
//
//import reactor.core.publisher.Mono;
//
//public class MyReactiveTransactionManager extends AbstractReactiveTransactionManager {
//
//	private final ExtendDataSourceTransactionManager dataSourceTransactionManager;
//
//	public MyReactiveTransactionManager(ExtendDataSourceTransactionManager dataSourceTransactionManager) {
//		this.dataSourceTransactionManager = dataSourceTransactionManager;
//	}
//
//	public static class ExtendDataSourceTransactionManager extends DataSourceTransactionManager {
//		private static final long serialVersionUID = 1L;
//		
//		public ExtendDataSourceTransactionManager(DataSource dataSource) {
//			super(dataSource);
//		}
//
//		protected Object doGetTransaction1() {
//			return doGetTransaction();
//		}
//
//		protected void doBegin1(Object transaction, TransactionDefinition definition) {
//			doBegin(transaction, definition);
//		}
//
//		protected void doCommit1(DefaultTransactionStatus status) {
//			doCommit(status);
//		}
//
//		protected void doRollback1(DefaultTransactionStatus status) {
//			doRollback(status);
//		}
//	}
//
//	@Override
//	protected Object doGetTransaction(TransactionSynchronizationManager synchronizationManager)
//			throws TransactionException {
//		return dataSourceTransactionManager.doGetTransaction1();
//	}
//
//	@Override
//	protected Mono<Void> doBegin(TransactionSynchronizationManager synchronizationManager, Object transaction,
//			TransactionDefinition definition) throws TransactionException {
//		return Mono.defer(() -> {
//			dataSourceTransactionManager.doBegin1(transaction, definition);
//			
////			JdbcTransactionObjectSupport txObject = (JdbcTransactionObjectSupport)transaction;
////			ConnectionHolder connectionHolder = txObject.getConnectionHolder();
////			return Mono.just(connectionHolder.getConnection());
//			return Mono.empty();
//		});
//	}
//
//	@Override
//	protected Mono<Void> doCommit(TransactionSynchronizationManager synchronizationManager,
//			GenericReactiveTransaction status) throws TransactionException {
//		return Mono.defer(() -> {
//			DefaultTransactionStatus defaultTransactionStatus = new DefaultTransactionStatus(status.getTransaction(),
//					status.isNewTransaction(), status.isNewSynchronization(), status.isReadOnly(), status.isDebug(),
//					status.getSuspendedResources());
//			dataSourceTransactionManager.doCommit1(defaultTransactionStatus);
//			return Mono.empty();
//		});
//	}
//
//	@Override
//	protected Mono<Void> doRollback(TransactionSynchronizationManager synchronizationManager,
//			GenericReactiveTransaction status) throws TransactionException {
//		return Mono.defer(() -> {
//			DefaultTransactionStatus defaultTransactionStatus = new DefaultTransactionStatus(status.getTransaction(),
//					status.isNewTransaction(), status.isNewSynchronization(), status.isReadOnly(), status.isDebug(),
//					status.getSuspendedResources());
//			dataSourceTransactionManager.doRollback1(defaultTransactionStatus);
//			return Mono.empty();
//		});
//	}
//
//}
