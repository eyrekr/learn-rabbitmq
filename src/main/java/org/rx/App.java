package org.rx;

import org.rx.api.ManagementApi;
import org.rx.tool.Just;

import static org.rx.tool.Just.async;
import static org.rx.tool.Just.shutdown;

public class App {
    private final String queueName = "hello";
    private final ManagementApi managementApi = new ManagementApi();

    private App receive() {
        async(() -> {
            new Receiver(queueName).receive();
            Thread.getAllStackTraces().keySet().stream()
                    .map(thread -> String.format("%-20s %s %s",
                            thread.getState(),
                            thread.isDaemon() ? "D" : " ",
                            thread.getName()))
                    .forEach(System.out::println);
        });
        return this;
    }

    private App send() {
        async(() -> {
            final Sender sender = new Sender(queueName);
            for (int i = 0; i < 4; i++) {
                sender.send("HELLO RABBIT");
                Just.sleep(10);
            }
            sender.send("QUIT");
        });
        return this;
    }

    private App printQueues() {
        managementApi.queues()
                .map(q -> String.format("%-50s  durable: %b  auto-delete: %b  messages: %,5d  state: %s",
                        q.name(),
                        q.durable(),
                        q.autoDelete(),
                        q.messages(),
                        q.state()))
                .each(System.out::println);
        return this;
    }


    /**
     * {@code  docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3}
     */
    public static void main(String[] args) {
        new App().printQueues().receive().send();
        shutdown();
    }
}
