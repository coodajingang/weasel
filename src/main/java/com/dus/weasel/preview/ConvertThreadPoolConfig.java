package com.dus.weasel.preview;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConvertThreadPoolConfig {
	private static final AtomicInteger threadIndex = new AtomicInteger(0);
	
	@Bean(value = "convertThreadPool")
	public ExecutorService convertThreadPool() {
		ThreadFactory threadFactory = new ThreadFactory() {
			
			@Override
			public Thread newThread(Runnable r) {
				Thread thread = new Thread(r, "convert-thread-" + threadIndex.incrementAndGet());
				thread.setDaemon(true);
				return thread;
			}
		};
		
		ExecutorService pool = new ThreadPoolExecutor(2, 4, 0L, TimeUnit.MILLISECONDS, 
				new ArrayBlockingQueue<Runnable>(10), threadFactory, new ThreadPoolExecutor.CallerRunsPolicy());
		return pool;
	}
}
