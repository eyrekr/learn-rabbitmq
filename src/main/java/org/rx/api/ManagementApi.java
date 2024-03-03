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
            final String encodedCredentials = Base64.getEncoder().encodeToString("guest:guest".getBytes(StandardCharsets.UTF_8));
            final HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(RABBITMQ_MANAGEMENT_API_URL))
                    .header("Authorization", "Basic " + encodedCredentials)
                    .build();
            final HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return Seq.fromArray(gson.fromJson(response.body(), Queue[].class));
        });
    }

    public record Queue(
            Map<String, Object> arguments,
            @SerializedName("auto_delete") boolean autoDelete,
            @SerializedName("consumer_capacity") int consumerCapacity,
            @SerializedName("consumer_utilisation") int consumerUtilisation,
            @SerializedName("consumers") int consumers,
            boolean durable,
            boolean exclusive,
            @SerializedName("exclusive_consumer_tag") String exclusiveConsumerTag,
            @SerializedName("idle_since") String idleSince,
            int memory,
            int messages,
            String name,
            String node,
            int reductions,
            String state,
            String type,
            String vhost) {
    }

    public record Stats(
            int ack,
            int deliver,
            @SerializedName("deliver_get") int deliverGet,
            @SerializedName("deliver_no_akc") int deliverNotAck,
            int get,
            @SerializedName("get_empty") int getEmpty,
            @SerializedName("get_no_ack") int getNoAck,
            int publish,
            int redeliver,
            int messages) {
    }
}
