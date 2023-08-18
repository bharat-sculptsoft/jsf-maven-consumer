package com.rabbitmq;

public class EnumMaster {

	public enum Queues {
		PRODUCT_SAVE_QUEUE("Product_Save_Queue","Product_Save_Routing_Key"), EMPLOYEE_SAVE_QUEUE("Employee_Save_Queue","Employee_Save_Routing_Key");

		private String queueName;
		private String routingKey;


		private Queues(String queueName,String routingKey) {
			this.queueName = queueName;
			this.routingKey = routingKey;
		}

		public String getQueueName() {
			return this.queueName;
		}

		public String getRoutingKey() {
			return this.routingKey;
		}
		public static Queues getByQueueName(String name) {
			for (Queues n : values()) {
				if (n.getQueueName().equals(name))
					return n;
			}
			return null;
		}
	}
}
