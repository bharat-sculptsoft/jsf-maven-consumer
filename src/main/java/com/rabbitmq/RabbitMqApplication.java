package com.rabbitmq;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RabbitMqApplication implements ServletContextListener {
	private static final Logger logger = LogManager.getLogger(RabbitMqConsumer.class);

	/*
	 * public static void main(String[] args) { logger.info("thread---" +
	 * Thread.currentThread().getName()); ExecutorService executor =
	 * Executors.newFixedThreadPool(5); executor.execute(() -> { new
	 * RabbitMqConsumer().init(); }); logger.info("main thread start"); }
	 * 
	 */
	private ExecutorService executorService;

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		executorService = Executors.newFixedThreadPool(5); // Create a thread pool

		System.out.println("contextInitialized..");

		executorService.submit(() -> {
			new RabbitMqConsumer().init();

		});

	}

	@Override
	public void contextDestroyed(ServletContextEvent event) {
		executorService.shutdown(); // Shutdown the thread pool
	}

}
