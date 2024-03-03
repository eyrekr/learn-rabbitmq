package org.rx;

import org.rx.api.ManagementApi;

public class App {

    /**
     * {@code  docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3}
     */
    public static void main(String[] args) throws Exception {
        final String queueName = "hello";
        new Sender(queueName).send("HELLO RABBIT");
        new ManagementApi().queues().print(
                "\n",
                q -> String.format("%-30s  durable: %b  auto-delete: %b  messages: %,5d  state: %s",
                        q.name(),
                        q.durable(),
                        q.autoDelete(),
                        q.messages(),
                        q.state()));
        new Receiver(queueName).receive();
    }
}
