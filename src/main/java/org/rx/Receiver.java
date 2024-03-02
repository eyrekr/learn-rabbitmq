package org.rx;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.Closeable;

import static org.rx.Silence.silently;

class Receiver implements Closeable {
    final String queueName;
    final Connection connection;
    final Channel channel;

    Receiver(String queueName) throws Exception {
        this.queueName = queueName;
        final ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");
        this.connection = connectionFactory.newConnection();
        this.channel = connection.createChannel();
    }

    void receive() throws Exception {
        // make sure the queue exists
        channel.queueDeclare(queueName, false, false, false, null);
        System.out.println("Waiting for messages...");
        channel.basicConsume( // this is non-blocking!
                queueName,
                true,
                (consumerTag, message) -> {
                    System.out.printf("%s: %s\n", consumerTag, new String(message.getBody()));
                },
                consumerTag -> {
                });
    }

    @Override
    public void close() {
        silently(channel::close);
        silently(connection::close);
    }
}
