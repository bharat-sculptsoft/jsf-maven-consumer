package com.rabbitmq;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.rabbitmq.EnumMaster.Queues;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.Delivery;

public class RabbitMqConsumer {
	private static final Logger logger = LogManager.getLogger(RabbitMqConsumer.class);

	private final ConnectionFactory connectionFactory;
	private static final int MAX_RETRIES = 3;
	private static final long RETRY_DELAY_MS = 1000;

	public RabbitMqConsumer() {
		Properties properties = new Properties();
		connectionFactory = new ConnectionFactory();
		try /*
			 * (InputStream input =
			 * ClassLoader.getSystemResourceAsStream("application.properites"))
			 */ {
			//properties.load(input);
			/*
			 * connectionFactory.setHost(properties.getProperty("rabbitmq.host"));
			 * connectionFactory.setPort(Integer.decode(properties.getProperty(
			 * "rabbitmq.port")));
			 * connectionFactory.setUsername(properties.getProperty("rabbitmq.username"));
			 * connectionFactory.setPassword(properties.getProperty("rabbitmq.password"));
			 */
			connectionFactory.setHost("localhost");
			connectionFactory.setPort(5672);
			connectionFactory.setUsername("guest");
			connectionFactory.setPassword("guest");
			logger.debug("Initialized RabbitMq connection...");

		} catch (Exception e) {
			logger.error("Exception occured when initializing RabbitMq connection exception:{}", e.getMessage());
			e.printStackTrace();
		}

	}

	public void init() {

		try (Connection connection = connectionFactory.newConnection(); Channel channel = connection.createChannel()) {
			for (Queues queue : Queues.values()) {
				String queueName = queue.getQueueName();
				channel.queueDeclare(queueName, true, false, false, null);
				channel.basicConsume(queueName, true, getDeliverCallback(queueName), consumerTag -> {
				});
			}
			Thread.currentThread().join();

		} catch ( IOException | TimeoutException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	private DeliverCallback getDeliverCallback(String queueName) {

		return new DeliverCallback() {
			private String lastReceivedMessage;

			@Override
			public void handle(String consumerTag, Delivery delivery) {
				try {
					String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
					lastReceivedMessage = message;
					logger.info("Received message in queue:{} ,message:{}", queueName, message);
					processMessage(queueName, message);
					logger.info("Processed message sucessfully, queue:{}", queueName);

					// channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
				} catch (Exception e) {
					e.printStackTrace();
					logger.error("Exception occurred when receiving message to queue:{} ,exception:{}", queueName,
							e.getMessage());
					handleException(lastReceivedMessage, queueName);
				}
			}
		};
	}

	public void processMessage(String queueName, String message) {

		try {
			logger.info("Processing message for queue:{} ,message:{}", queueName, message);

			Queues byName = Queues.getByQueueName(queueName);
			if (byName == Queues.PRODUCT_SAVE_QUEUE) {
				productSaveExecutor(message);
			} else if (byName == Queues.EMPLOYEE_SAVE_QUEUE) {
				employeeSaveExecutor(message);
			}
		} catch (Exception e) {
			logger.error("Exception occurred when processing message to queue:{} ,exception:{}", queueName,
					e.getMessage());
			throw e;
		}

	}

	private synchronized void handleException(String lastMessage, String queueName) {

		int retries = 0;
		boolean retrying = true;

		while (retrying && retries < MAX_RETRIES) {
			try {
				logger.info("Retrying attempt for queue:{} ,retryCount:{} ,message:{}", queueName, (retries + 1),
						lastMessage);

				Thread.sleep(RETRY_DELAY_MS);

				processMessage(queueName, lastMessage);

				retrying = false;
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("Retrying due to:{} for queue:{} ,retryCount:{} ,message:{}", e.getMessage(), queueName,
						(retries + 1), lastMessage);
				retries++;
			}
		}

		if (retrying) {
			logger.error("All retries failed,for queue:{} ,retryCount:{} ,message:{}", queueName, (retries + 1),
					lastMessage);
// Reject the message and optionally requeue or send to a dead-letter queue
			// channel.basicReject(delivery.getEnvelope().getDeliveryTag(), true);

		}
	}

	private void productSaveExecutor(String message) {
		int i = 1 / 0;
		logger.info("product details saved--{}", message);
	}

	private void employeeSaveExecutor(String message) {
		logger.info("employee details saved--{}", message);
	}

}
