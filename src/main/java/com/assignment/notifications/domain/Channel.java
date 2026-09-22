package com.assignment.notifications.domain;

public enum Channel {
    EMAIL,
    SMS
    // Adding a channel means a new enum value + a ChannelProvider bean.
    // PUSH and LETTER were in scope on paper but I cut them to keep two
    // channels genuinely working instead of four stubbed out - see README.
}
