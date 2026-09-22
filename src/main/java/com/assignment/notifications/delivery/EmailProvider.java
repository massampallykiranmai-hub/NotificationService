package com.assignment.notifications.delivery;

import com.assignment.notifications.config.DeliveryProperties;
import com.assignment.notifications.domain.Channel;
import com.assignment.notifications.domain.Notification;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Component
public class EmailProvider implements ChannelProvider {

    private final DeliveryProperties properties;

    public EmailProvider(DeliveryProperties properties) {
        this.properties = properties;
    }

    @Override
    public Channel channel() {
        return Channel.EMAIL;
    }

    @Override
    public ProviderResult send(Notification notification) {
        // A malformed address is never going to start working on retry -
        // that's the permanent/retryable distinction the whole retry model
        // hinges on. Everything else is simulated as transient.
        if (!notification.getRecipient().contains("@")) {
            return ProviderResult.permanent("invalid email address: " + notification.getRecipient());
        }
        if (ThreadLocalRandom.current().nextDouble() < properties.getSimulatedFailureRate()) {
            return ProviderResult.retryable("simulated SMTP timeout");
        }
        return ProviderResult.success("accepted by simulated email gateway");
    }
}
