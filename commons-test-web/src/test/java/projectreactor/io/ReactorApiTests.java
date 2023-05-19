package projectreactor.io;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscription;

import reactor.core.Disposable;
import reactor.core.Exceptions;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.FluxSink.OverflowStrategy;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ParallelFlux;
import reactor.core.publisher.SignalType;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.EmitFailureHandler;
import reactor.core.publisher.Sinks.EmitResult;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class ReactorApiTests {

	@Test
	void subscribe() throws Exception {
		Flux<Integer> ints = Flux.range(1, 3);
		/**
		 * 这种空白消费，没有任何输出
		 */
		ints.subscribe();
		/**
		 * 这种实际消费，输出1，2，3
		 */
		ints.subscribe(i -> System.out.println(i));

		ints = Flux.range(1, 4).map(i -> {
			if (i != 3)
				return i;
			throw new RuntimeException("Got to 3");
		});
		/**
		 * errorConsumer： 输出1、2 ， 3消费异常，4不会收到，不会中断后续代码
		 */
		ints.subscribe(i -> System.out.println(i), error -> System.err.println("Error: " + error));
		/**
		 * 没有errorConsumer： 输出1、2 ， 3日志打印异常，4不会收到，不会中断后续代码
		 */
		ints.subscribe(i -> System.out.println(i));
		/**
		 * completeConsumer：当消费全部成功后触发
		 */
		ints.subscribe(i -> System.out.println(i), error -> System.err.println("Error " + error),
				() -> System.out.println("Done"));
	}

	@Test
	void baseSubscriber() throws Exception {
		/**
		 * BaseSubscriber适用于匿名类无法满足的场景。<br>
		 * BaseSubscriber的实例（或其子类）是一次性的，这意味着如果BaseSubscribeer订阅了第二个发布服务器，它将取消对第一个发布服务器的订阅。这是因为两次使用实例会违反Reactive
		 * Streams规则，即不能并行调用Subscriber的onNext方法。因此，只有在对Publisher#subscribe（Subscriber）的调用中直接声明匿名实现时，匿名实现才是好的。
		 */
		class SampleSubscriber<T> extends BaseSubscriber<T> {
			/**
			 * 该方法订阅时触发
			 */
			public void hookOnSubscribe(Subscription subscription) {
				System.out.println("Subscribed");
				/*
				 * 初始请求的元素个数，5表示5个，不request则无法收到订阅信息
				 */
				request(1);
			}

			/**
			 * 每个元素的处理
			 */
			public void hookOnNext(T value) {
				System.out.println(value);
				/*
				 * 继续请求的元素个数
				 */
				request(1);
			}

			@Override
			protected void hookOnError(Throwable throwable) {
				super.hookOnError(throwable);
			}

			@Override
			protected void hookOnComplete() {
				super.hookOnComplete();
			}

			@Override
			protected void hookOnCancel() {
				super.hookOnCancel();
			}

			@Override
			protected void hookFinally(SignalType type) {
				super.hookFinally(type);
			}
		}

		SampleSubscriber<Integer> ss = new SampleSubscriber<Integer>();
		Flux<Integer> ints = Flux.range(1, 4);
		/**
		 * doOnRequest：每次request后都会触发。<br>
		 * 注意doOnRequest后会返回新的对象
		 */
		ints = ints.doOnRequest(r -> System.out.println("request of " + r));
		ints.subscribe(ss);
	}

	/**
	 * generate同步创建
	 */
	@Test
	void generate() throws Exception {
		Flux<String> flux = Flux.generate(() -> 0, (state, sink) -> {
			/**
			 * 产生元素
			 */
			sink.next("3 x " + state + " = " + 3 * state);
			if (state == 10) {
				sink.complete();
			}
			/**
			 * 累加
			 */
			return state + 1;
		}, state -> System.out.println(state)/* state最后的值 */);

		flux.subscribe(str -> System.out.println(str));
	}

	/**
	 * create用于异步多线程创建
	 */
	@Test
	void create() throws Exception {
		class MyThread extends Thread {
			final FluxSink sink;
			final List list;

			MyThread(FluxSink sink, List list) {
				this.sink = sink;
				this.list = list;
			}

			public void run() {
				list.forEach(l -> {
					sink.next(l);
				});
			};
		}

		Flux<Object> flux = Flux.create(sink -> {
			/**
			 * 该回调方法是异步的，在订阅时才触发
			 */

			MyThread t1 = new MyThread(sink, Arrays.asList("1-1", "1-2", "1-3", "1-4", "1-5"));
			MyThread t2 = new MyThread(sink, Arrays.asList("2-1", "2-2", "2-3", "2-4", "2-5"));

			t1.start();
			t2.start();
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}

			/**
			 * 一旦调用complete就不会再产生元素，如果不调用complete也能订阅到元素了
			 */
//			sink.complete();

			/**
			 * 如果不调用complete，可以继续创建元素，订阅端一直可以收到新元素
			 */
//			for (;;) {
//				sink.next(System.currentTimeMillis() + "");
//			}
		}, OverflowStrategy.ERROR);

		/**
		 * 最终具有多线程合并的结果
		 */
		flux.subscribe(i -> {
			System.out.println(i);
//			try {
//				Thread.sleep(200);
//			} catch (Exception e) {
//			}
		});

		Thread.sleep(500);
	}

	/**
	 * push用于异步单线程创建
	 */
	@Test
	void push() throws Exception {
	}

	/**
	 * handle有点像map，不同的是map需要返回null，handle则可以不要null
	 */
	@Test
	void handle() throws Exception {
		Flux<String> alphabet = Flux.just(-1, 30, 13, 9, 20).handle((i/* 元素 */, sink) -> {
			/**
			 * 以下代码只当数字是可以转字母时有值
			 */
			String letter = alphabet(i);
			if (letter != null) {
				sink.next(letter);
			}
		});

		alphabet.subscribe(System.out::println);
	}

	private String alphabet(int letterNumber) {
		if (letterNumber < 1 || letterNumber > 26) {
			return null;
		}
		int letterIndexAscii = 'A' + letterNumber - 1;
		return "" + (char) letterIndexAscii;
	}

	@Test
	void publishOn() throws Exception {
		System.out.println(Thread.currentThread().getName());

		Scheduler s = Schedulers.newParallel("parallel-scheduler", 4); // 1 创建Scheduler

		final Flux<String> flux = Flux.range(1, 4)//
				.map(i -> i + "(" + Thread.currentThread().getName() + ")") // 2 第一个map在5对应的匿名线程上执行
				.publishOn(s) // 3 publish切换到1对应的线程
				.map(i -> i + "(" + Thread.currentThread().getName() + ")"); // 4 第二个map执行在1对应的线程

		new Thread(() -> flux.subscribe(i -> System.out.println(i + "(" + Thread.currentThread().getName() + ")")))
				.start();// 5 这个匿名线程是发生订阅的线程。打印发生在最新的执行上下文上，该上下文来自publishOn。

		Thread.sleep(500);
	}

	@Test
	void subscribeOn() throws Exception {
		System.out.println(Thread.currentThread().getName());

		Scheduler s = Schedulers.newParallel("parallel-scheduler", 4); // 1 创建Scheduler

		final Flux<String> flux = Flux.range(1, 4)//
				.map(i -> i + "(" + Thread.currentThread().getName() + ")") // 2 第一个map在1对应的线程
				.subscribeOn(s) // 3 subscribe切换到1对应的线程，在5订阅时就起作用
				.map(i -> i + "(" + Thread.currentThread().getName() + ")"); // 4 第二个map跟第一个map一样

		new Thread(() -> flux.subscribe(i -> System.out.println(i + "(" + Thread.currentThread().getName() + ")")))
				.start(); // 5 这个匿名线程是最初进行订阅的线程，但subscribeOn会立即将其转移到四个调度程序线程之一

		Thread.sleep(500);
	}

	@Test
	void onErrorReturn() throws Exception {
		Flux.just(1, 2, 0, 4)//
				.map(i -> "100 / " + i + " = " + (100 / i)) //
				.doOnComplete(() -> System.out.println("Completed"))// 由于中间遇到了异常，所以不会触发
				/**
				 * onErrorReturn系列遇到异常时返回xx
				 */
//				.onErrorReturn("Divided by zero :(")//
				.onErrorReturn(Exception.class, "Divided by zero :(")//
//				.onErrorReturn(e -> e instanceof Exception, "Divided by zero :(")//
				.subscribe(i -> System.out.println(i + "(" + Thread.currentThread().getName() + ")"));
	}

	@Test
	void onErrorComplete() throws Exception {
		Flux.just(1, 2, 0, 4)//
				.map(i -> "100 / " + i + " = " + (100 / i)) //
				.doOnComplete(() -> System.out.println("Completed"))// 由于中间遇到了异常，所以不会触发
				/**
				 * onErrorComplete系列遇到异常时直接中断，但不会触发doOnComplete
				 */
//				.onErrorComplete()
				.onErrorComplete(Exception.class)
//				.onErrorComplete(e -> e instanceof Exception)
				.subscribe(i -> System.out.println(i + "(" + Thread.currentThread().getName() + ")"));
	}

	@Test
	void onErrorResume() throws Exception {
		Flux.just(1, 2, 0, 4)//
				.map(i -> "100 / " + i + " = " + (100 / i)) //
				.doOnComplete(() -> System.out.println("Completed"))// 由于中间遇到了异常，所以不会触发
				/**
				 * onErrorResume系列遇到异常时fallback(fallback可以运行ok，也可以抛出异常例如Flux.error)，但不会触发doOnComplete
				 */
//				.onErrorResume(e -> Mono.just(e.getMessage() + "->Resume"))
				.onErrorResume(Exception.class, e -> Mono.just(e.getMessage() + "->Resume"))
//				.onErrorResume(e -> e instanceof Exception, e -> Mono.just(e.getMessage() + "->Resume"))
				.subscribe(i -> System.out.println(i + "(" + Thread.currentThread().getName() + ")"));
	}

	@Test
	void onErrorContinue() throws Exception {
		Flux.just(1, 2, 0, 4)//
				.map(i -> "100 / " + i + " = " + (100 / i)) //
				.doOnComplete(() -> System.out.println("Completed"))// 会触发
				/**
				 * onErrorContinue系列遇到异常时fallback消费异常，切继续下一个元素的消费
				 */
//				.onErrorContinue((e, i) -> System.out.println(e + "   " + i))
				.onErrorContinue(Exception.class, (e, i) -> System.out.println(e + "   " + i))
//				.onErrorContinue(e -> e instanceof Exception, (e, i) -> System.out.println(e + "   " + i))
				.subscribe(i -> System.out.println(i + "(" + Thread.currentThread().getName() + ")"));
	}

	@Test
	void onErrorMap() throws Exception {
		Flux.just(1, 2, 0, 4)//
				.map(i -> "100 / " + i + " = " + (100 / i)) //
				.doOnComplete(() -> System.out.println("Completed"))// 由于中间遇到了异常，所以不会触发
				/**
				 * onErrorMap系列遇到异常时只能转换为另一个异常
				 */
//				.onErrorMap(e -> new RuntimeException(e))
				.onErrorMap(Exception.class, e -> new RuntimeException(e))
//				.onErrorMap(e -> e instanceof Exception, e -> new RuntimeException(e))
				.subscribe(i -> System.out.println(i + "(" + Thread.currentThread().getName() + ")"));
	}

	@Test
	void onErrorStop() throws Exception {
		// 未知
	}

	@Test
	void doOnError() throws Exception {
		Flux.just(1, 2, 0, 4)//
				.map(i -> "100 / " + i + " = " + (100 / i)) //
				.doOnComplete(() -> System.out.println("Completed"))// 由于中间遇到了异常，所以不会触发
				/**
				 * doOnError系列遇到异常时可以处理异常，但异常会继续传播（抛出）
				 */
//				.doOnError(e -> System.out.println(e))
				.doOnError(Exception.class, e -> System.out.println(e))
//				.doOnError(e -> e instanceof Exception, e -> System.out.println(e))
				.onErrorReturn("doOnError then onErrorReturn")// 由于异常被继续传播，这里也能收到
				.subscribe(i -> System.out.println(i + "(" + Thread.currentThread().getName() + ")"));
	}

	@Test
	void doOnNext() throws Exception {
		Flux.just(1, 2, 0, 4)//
				.map(i -> "100 / " + i + " = " + (100 / i)) //
				.doOnComplete(() -> System.out.println("Completed"))// 由于中间遇到了异常，所以不会触发
				/**
				 * 每个元素，先执行doOnNext然后才被订阅消费。异常不会进doOnNext
				 */
				.doOnNext(i -> System.out.println(i + "(" + Thread.currentThread().getName() + ")doOnNext"))
				.onErrorReturn("doOnError then onErrorReturn")//
				.subscribe(i -> System.out.println(i + "(" + Thread.currentThread().getName() + ")"));
	}

	@Test
	void doFinally() throws Exception {
		Flux.just(1, 2, 0, 4)//
				.map(i -> "100 / " + i + " = " + (100 / i)) //
				.doOnComplete(() -> System.out.println("Completed"))// 由于中间遇到了异常，所以不会触发
				/**
				 * doFinally相当于finally，任何情况下都会触发
				 */
				.doFinally(signalType -> System.out.println("doFinally " + signalType))//
				.subscribe(i -> System.out.println(i + "(" + Thread.currentThread().getName() + ")"));
	}

	/**
	 * using 可以用于自动清理，像 try-with-resource
	 */
	@Test
	void using() throws Exception {
		AtomicBoolean isDisposed = new AtomicBoolean();
		Disposable disposableInstance = new Disposable() {
			@Override
			public void dispose() {
				isDisposed.set(true);
			}

			@Override
			public String toString() {
				return "DISPOSABLE";
			}
		};

		Flux.using(() -> disposableInstance, disposable -> Flux.just(disposable.toString()), Disposable::dispose)
				.subscribe(i -> System.out.println(i + "(" + Thread.currentThread().getName() + ")"));
	}

	@Test
	void retry() throws Exception {
		Flux.interval(Duration.ofMillis(250))//
				.map(input -> {
					if (input < 3)
						return "tick " + input;
					throw new RuntimeException("boom");
				})//
				/**
				 * retry是从头开始的，所以Flux的消费会2次从头开始
				 */
				.retry(1)//
				.elapsed() // elapsed 能关联出上一个元素的时间差ms
				.subscribe(System.out::println, e -> System.out.println(e));

		Thread.sleep(2100);
	}

	@Test
	void retryWhen() throws Exception {
		AtomicInteger errorCount = new AtomicInteger();
		Flux.interval(Duration.ofMillis(250))//
				.map(input -> {
					if (input < 3)
						return "tick " + input;
					throw new RuntimeException("boom");
				})//
				.doOnError(e -> errorCount.incrementAndGet())//
				/**
				 * retryWhen 可以自定义重试条件
				 */
				.retryWhen(Retry.from(companion -> companion.map(retrySignal -> {
					System.out.println("totalRetries:" + retrySignal.totalRetries());// 从0开始的
					if (retrySignal.totalRetries() < 3)
						return retrySignal.totalRetries();
					else
//						return -1;//这也可以终止重试，但消费无法感知异常
						throw Exceptions.propagate(retrySignal.failure());
				})))//
				.elapsed() // elapsed 能关联出上一个元素的时间差ms
				.subscribe(System.out::println, e -> System.out.println(e));

		Thread.sleep(5000);
		System.out.println("errorCount:" + errorCount);
	}

	/**
	 * 可以用于多线程生成元素
	 */
	@Test
	void sinks() throws Exception {
		Sinks.Many<Long> replaySink = Sinks.many().replay().all();

		// thread1
		replaySink.emitNext(1L, EmitFailureHandler.FAIL_FAST);

		// thread2, later
		replaySink.emitNext(2L, EmitFailureHandler.FAIL_FAST);

		// thread3, concurrently with thread 2
		// would retry emitting for 2 seconds and fail with EmissionException if
		// unsuccessful
		replaySink.emitNext(3L, EmitFailureHandler.busyLooping(Duration.ofSeconds(2)));

		// thread3, concurrently with thread 2
		// would return FAIL_NON_SERIALIZED
		EmitResult result = replaySink.tryEmitNext(4L);

		replaySink.asFlux()
		/**
		 * take系列是订阅的条件，这里加了<10条件会使后面的emitNext的元素订阅不到
		 */
//				.takeWhile(i -> i < 10)//
				.log()//
				.subscribe(i -> System.out.println(i + "(" + Thread.currentThread().getName() + ")"));

		new Thread() {
			public void run() {
				for (long i = 0; i < 10; i++) {
					replaySink.emitNext(i * 10, EmitFailureHandler.FAIL_FAST);
				}
			};
		}.start();

		/**
		 * 多线程之间不能并发的emit，否则报错
		 */
		Thread.sleep(100);

		new Thread() {
			public void run() {
				for (long i = 0; i < 10; i++) {
					replaySink.emitNext(i * 100, EmitFailureHandler.FAIL_FAST);
				}
			};
		}.start();

		Thread.sleep(100);
		replaySink.emitComplete(EmitFailureHandler.FAIL_FAST);
	}

	@Test
	void hot() throws Exception {
		/**
		 * 冷的，从头全部消费一遍
		 */
//		Sinks.Many<String> hotSource= Sinks.many().replay().all();

		/**
		 * 热的，从订阅开始只能消费到新元素
		 */
		Sinks.Many<String> hotSource = Sinks.many().multicast().directBestEffort();

		Flux<String> hotFlux = hotSource.asFlux().map(String::toUpperCase);

		hotFlux.subscribe(d -> System.out.println("Subscriber 1 to Hot Source: " + d));

		hotSource.emitNext("blue", EmitFailureHandler.FAIL_FAST);
		hotSource.tryEmitNext("green").orThrow();

		/**
		 * 新的订阅只能收到热的
		 */
		hotFlux.subscribe(d -> System.out.println("Subscriber 2 to Hot Source: " + d));

		hotSource.emitNext("orange", EmitFailureHandler.FAIL_FAST);// 热的
		hotSource.emitNext("purple", EmitFailureHandler.FAIL_FAST);// 热的
		hotSource.emitComplete(EmitFailureHandler.FAIL_FAST);
	}

	/**
	 * 把多个Flux合并压缩成1个Flux
	 */
	@Test
	void zip() {
		Flux<String> stringFlux1 = Flux.just("a", "b", "c", "d", "e");
		Flux<String> stringFlux2 = Flux.just("f", "g", "h", "i");
		Flux<String> stringFlux3 = Flux.just("1", "2", "3", "4");
		// 方法一zipWith
		stringFlux1.zipWith(stringFlux2)//
				.subscribe(i -> System.out.println(i + "(" + Thread.currentThread().getName() + ")"));
		
		System.out.println("-----------------------");
		
		// 方法二zip
		Flux.zip(stringFlux1, stringFlux2, stringFlux3)//
				.subscribe(i -> System.out.println(i + "(" + Thread.currentThread().getName() + ")"));
	}
	
	@Test
	void merge() throws InterruptedException, ExecutionException {
		Mono<String> mono1 = Mono.fromCallable(()->{
			System.out.println("run mono1-"+System.currentTimeMillis());
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
			}
			return "abc";
		});
		
		Mono<String> mono2 = Mono.fromCallable(()->{
			System.out.println("run mono2-"+System.currentTimeMillis());
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
			}
			return "def";
		});
		
//		Flux<String> flux = Flux.merge(mono1,mono2);
		
		Flux<Object> flux = Flux.empty();
		
//		Flux<String> flux = Flux.from(mono1);
		flux = flux.mergeWith(mono1);
		System.out.println(flux);
		
		flux = flux.mergeWith(mono2);
		
		flux = Flux.merge(mono1,mono2);
		
		Flux<Integer> range = Flux.range(0, 10);
		range.parallel(11).subscribe(i->{
			System.out.println("run mono2-"+i);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		});
		
//		ParallelFlux<Object> parallel = flux.parallel(4);
		
//		parallel.subscribe();
		
//		flux.subscribeOn(Schedulers.boundedElastic()).subscribe();
		
		Thread.sleep(10000);
		
//		mono1.subscribeOn(Schedulers.newSingle("")).subscribe(s->{
//			System.out.println(s);
//		});
//		mono2.subscribeOn(Schedulers.newSingle("")).subscribe(s->{
//			System.out.println(s);
//		});
		
//		CompletableFuture<String> f1 = mono1.subscribeOn(Schedulers.newSingle("")).toFuture();
//		CompletableFuture<String> f2 = mono2.subscribeOn(Schedulers.newSingle("")).toFuture();
//		
//		Thread.sleep(3000);
//		System.out.println(".....");
//		
//		long start = System.currentTimeMillis();
//		System.out.println(f1.get());
//		System.out.println(f2.get());
//		
//		
//		System.out.println(System.currentTimeMillis()-start);
////		System.out.println(block);
		
	}
}
