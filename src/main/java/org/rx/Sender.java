package org.rx;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

class Sender {
    final String queueName;
    final ConnectionFactory connectionFactory;

    Sender(final String queueName) {
        this.queueName = queueName;
        connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");
    }

    void send(final String message) throws Exception {
        try (final Connection connection = connectionFactory.newConnection();
             final Channel channel = connection.createChannel()) {
            // make sure the queue exists
            channel.queueDeclare(queueName, false, false, false, null);
            System.out.println("Sending a message...");
            channel.basicPublish("", queueName, null, message.getBytes());
        }
    }
}
