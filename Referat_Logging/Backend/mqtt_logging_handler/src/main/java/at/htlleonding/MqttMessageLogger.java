package at.htlleonding;

import io.smallrye.reactive.messaging.mqtt.ReceivingMqttMessageMetadata;
import io.vertx.mqtt.messages.impl.MqttPublishMessageImpl;
import io.vertx.mutiny.mqtt.messages.MqttPublishMessage;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Metadata;
import org.jboss.logging.Logger;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@ApplicationScoped
public class MqttMessageLogger {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yy HH:mm:ss");

    private static final Logger LOG = Logger.getLogger(MqttMessageLogger.class);

    @Incoming("mqtt")
    public CompletionStage<Void> onMqttMessage(Message<String> message) {

        String payload = message.getPayload();

        System.out.println("\n" + LocalDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis()), ZoneId.systemDefault()).format(formatter));

        System.out.println("Received message: " + payload);

        Optional<String> topicNameOpt = message.getMetadata()
                .get(ReceivingMqttMessageMetadata.class)
                .map(metadata -> metadata.getMessage().topicName());

        topicNameOpt.ifPresent(topicName -> {
            String logMessage = topicName + " " + payload;
            System.out.println("Sent to topic: " + topicName);

            if (topicName.contains("/debug")) {
                LOG.debug(logMessage);
            } else if (payload.contains("offline") || payload.contains("online")) {
                LOG.warn(logMessage);
            } else if (payload.contains("error") || payload.contains("failure")) {
                LOG.error(logMessage);
            } else {
                LOG.info(logMessage);
            }
        });

        return CompletableFuture.completedFuture(null);
    }
}