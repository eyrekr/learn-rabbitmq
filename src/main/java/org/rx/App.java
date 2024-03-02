package org.rx;

public class App {

    /**
     * {@code  docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3}
     */
    public static void main(String[] args) throws Exception {
        final String queueName = "hello";
        new Sender(queueName).send("HELLO RABBIT");
        new Receiver(queueName).receive();
    }
}
