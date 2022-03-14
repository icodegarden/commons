package io.github.icodegarden.commons.nio.concurrent;
//package com.mycode.rpc.nio;
//
//import java.io.IOException;
//import java.util.concurrent.BlockingQueue;
//import java.util.concurrent.LinkedBlockingQueue;
//import java.util.concurrent.ThreadFactory;
//import java.util.concurrent.ThreadPoolExecutor;
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.atomic.AtomicInteger;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import com.mycode.rpc.Heartbeatable;
//import com.mycode.rpc.nio.MessageHandlerStrategy.Channel;
//
///**
// * 
// * @author Fangfang.Xu
// *
// */
//public class PooledMessageHandlerStrategy extends MessageHandlerStrategy {
//	private static final Logger log = LoggerFactory.getLogger(PooledMessageHandlerStrategy.class);
//	
//	private static final ThreadPoolExecutor THREADPOOL = new ThreadPoolExecutor(20, 50, 120, TimeUnit.SECONDS,
//			new LinkedBlockingQueue<Runnable>(100), new ThreadFactory() {
//				protected final AtomicInteger mThreadNum = new AtomicInteger(1);
//
//				@Override
//				public Thread newThread(Runnable runnable) {
//					String name = "Nio-ServerSide-MessageHandlerStrategy-" + mThreadNum.getAndIncrement();
//					Thread ret = new Thread(runnable, name);
//					return ret;
//				}
//			}, new ThreadPoolExecutor.CallerRunsPolicy());
//	
//	private BlockingQueue<ExchangeMessage> blockingQueue = new LinkedBlockingQueue<ExchangeMessage>();// max
//
//	public PooledMessageHandlerStrategy(Heartbeatable heartbeat, MessageHandler messageHandler, Channel channel) {
//		super(heartbeat, messageHandler, channel);
//		
//		SyncMessageHandlerStrategy sync = new SyncMessageHandlerStrategy(heartbeat, messageHandler, channel);
//		new Thread("Nio-ServerSide-PooledMessageHandlerStrategy") {
//			@Override
//			public void run() {
//				for (;;) {
//					try {
//						ExchangeMessage response = blockingQueue.take();
//						sync.sendResponse(response);
//					} catch (Exception e) {
//						log.error("ex in handle message", e);
//					}
//				}
//			}
//		}.start();
//	}
//
//	@Override
//	public void handle(Channel channel, ExchangeMessage message) throws IOException {
//		THREADPOOL.execute(() -> {
//			ExchangeMessage response = doBiz(message);
//			if (response == null) {
//				return;
//			}
//			try {
//				blockingQueue.put(response);
//			} catch (InterruptedException e) {
//			}
//		});
//	}
//}