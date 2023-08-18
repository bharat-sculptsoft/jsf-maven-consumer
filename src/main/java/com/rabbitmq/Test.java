package com.rabbitmq;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.EnumMaster.Queues;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

public class Test {

	private static final String[] QUEUE_NAMES = {"Product_Save_Queue", "Employee_Save_Queue"}; // Replace with your queue names

    public void startListening() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost"); // RabbitMQ server host

        try (Connection connection = factory.newConnection()) {
			for (Queues queue : Queues.values()) {
				String queueName = queue.getQueueName();

                Channel channel = connection.createChannel();
                channel.queueDeclare(queueName, true, false, false, null);

                System.out.println("Listening to queue: " + queueName);

                DeliverCallback deliverCallback = getDevliveryCallBack(queueName);

                channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {});
            }

            // Keep the thread running to continuously listen for messages
            Thread.currentThread().join();
        } catch (IOException | TimeoutException | InterruptedException e) {
            e.printStackTrace();
        }
    }

	private DeliverCallback getDevliveryCallBack(String queueName) {
		return (consumerTag, delivery) -> {
		   try {
			   String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
			    System.out.println("Received message from " + queueName + ": " + message);
			    
				processMessage(queueName, message);

		    }catch (Exception e) {

		    }
		
		};

	}
	public void processMessage(String queueName, String message) {

		try {
			switch (Queues.getByQueueName(queueName)) {
			case PRODUCT_SAVE_QUEUE:
				productSaveExecutor(message);
				break;
			case EMPLOYEE_SAVE_QUEUE:
				employeeSaveExecutor(message);
				break;
			default:
				break;
			}
		} catch (Exception e) {
			throw e;
		}

	}
	private void productSaveExecutor(String message) {
		System.out.println("product details saved--" + message);
	}

	private void employeeSaveExecutor(String message) {
		System.out.println("employee details saved--" + message);
	}

    public static void main(String[] args) {
    	Test listener = new Test();
    	ExecutorService executor = Executors.newFixedThreadPool(5);
		executor.execute(() -> {
			listener.startListening();
		});
		System.out.println("main thread start");
       // listener.startListening();
    }
}
