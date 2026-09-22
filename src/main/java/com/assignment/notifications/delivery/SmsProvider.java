package com.assignment.notifications.delivery;

import com.assignment.notifications.config.DeliveryProperties;
import com.assignment.notifications.domain.Channel;
import com.assignment.notifications.domain.Notification;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Component
public class SmsProvider implements ChannelProvider {

    private final DeliveryProperties properties;

    public SmsProvider(DeliveryProperties properties) {
        this.properties = properties;
    }

    @Override
    public Channel channel() {
        return Channel.SMS;
    }

    @Override
    public ProviderResult send(Notification notification) {
        String digitsOnly = notification.getRecipient().replaceAll("[^0-9+]", "");
        if (digitsOnly.length() < 8) {
            return ProviderResult.permanent("invalid phone number: " + notification.getRecipient());
        }
        if (ThreadLocalRandom.current().nextDouble() < properties.getSimulatedFailureRate()) {
            return ProviderResult.retryable("simulated carrier timeout");
        }
        return ProviderResult.success("accepted by simulated SMS gateway");
    }
}
