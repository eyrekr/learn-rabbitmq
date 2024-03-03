package org.rx;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rabbitmq.client.ConnectionFactory;

import java.util.concurrent.atomic.AtomicBoolean;


class Receiver {
    private final String queueName;
    private final ConnectionFactory connectionFactory;
    private final Gson gson = new GsonBuilder().setLenient().setPrettyPrinting().create();
    private final AtomicBoolean endOfWorld = new AtomicBoolean();

    Receiver(String queueName) {
        this.queueName = queueName;
        this.connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");
    }

    void receive() throws Exception {
        try (final var connection = connectionFactory.newConnection();
             final var channel = connection.createChannel()) {
            // make sure the queue exists
            channel.queueDeclare(queueName, false, false, false, null);
            System.out.println("Receiver> Waiting for messages...");
            while (!endOfWorld.get()) {
                System.out.print(".");
                channel.basicConsume( // this is non-blocking!
                        queueName,
                        true,
                        (consumerTag, message) -> {
                            final var body = new String(message.getBody());
                            if ("QUIT".equals(body)) endOfWorld.set(true);
                            /*
                            System.out.printf("%s: %s\n%s\n%s\n",
                                    consumerTag,
                                    body,
                                    gson.toJson(message.getEnvelope()),
                                    gson.toJson(message.getProperties()));
                             */
                            System.out.println(body);
                        },
                        consumerTag -> {
                        });
            }
        }
        System.out.println("Receiver> Stopped receiving");
    }
}
