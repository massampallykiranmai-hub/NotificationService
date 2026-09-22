package com.assignment.notifications.delivery;

import com.assignment.notifications.domain.Channel;
import com.assignment.notifications.domain.Notification;

/**
 * One implementation is used for each channel. In production, a real
 * implementation could call SendGrid, Twilio, or another service. In this
 * project, the channels are simulated so everything will work offline with just
 * `docker compose up`. To add a new channel, create an implementation and
 * register it as a bean. The worker and retry logic do not need to change.
 */
public interface ChannelProvider {
    Channel channel();
    ProviderResult send(Notification notification);
}
