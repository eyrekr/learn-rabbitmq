package org.rx.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import org.rx.tool.Just;
import org.rx.tool.Seq;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

public class ManagementApi {
    private static final String RABBITMQ_MANAGEMENT_API_URL = "http://localhost:15672/api/queues";
    private final HttpClient client = HttpClient.newHttpClient();
    private final Gson gson = new GsonBuilder().setLenient().setPrettyPrinting().create();

    public Seq<Queue> queues() {
        return Just.get(() -> {
            final var encodedCredentials = Base64.getEncoder().encodeToString("guest:guest".getBytes(StandardCharsets.UTF_8));
            final var request = HttpRequest.newBuilder()
                    .uri(URI.create(RABBITMQ_MANAGEMENT_API_URL))
                    .header("Authorization", "Basic " + encodedCredentials)
                    .build();
            final var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return Seq.fromArray(gson.fromJson(response.body(), Queue[].class));
        });
    }

    public record Queue(
            Map<String, Object> arguments,
            @SerializedName("auto_delete") boolean autoDelete,
            @SerializedName("consumer_capacity") long consumerCapacity,
            @SerializedName("consumer_utilisation") long consumerUtilisation,
            @SerializedName("consumers") long consumers,
            boolean durable,
            boolean exclusive,
            @SerializedName("exclusive_consumer_tag") String exclusiveConsumerTag,
            @SerializedName("idle_since") String idleSince,
            long memory,
            long messages,
            String name,
            String node,
            long reductions,
            String state,
            String type,
            String vhost) {
    }

    public record Stats(
            long ack,
            long deliver,
            @SerializedName("deliver_get") long deliverGet,
            @SerializedName("deliver_no_akc") long deliverNotAck,
            long get,
            @SerializedName("get_empty") long getEmpty,
            @SerializedName("get_no_ack") long getNoAck,
            long publish,
            long redeliver,
            long messages) {
    }
}
