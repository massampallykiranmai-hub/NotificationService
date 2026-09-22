package com.assignment.notifications.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Config settings for retries and worker behavior loaded from application.yml or env vars.
 * Keeping these configurable lets us adjust batch sizes and retry limits per environment
 * without needing to rebuild or redeploy code.
 */
@Component
@ConfigurationProperties(prefix = "delivery")
public class DeliveryProperties {

    // Attempts per notification before giving up and marking it FAILED
    private int maxAttempts = 4;

    // Base for exponential backoff: attempt N waits base * 2^(N-1) seconds
    private int backoffBaseSeconds = 5;

    // How often the worker polls for due row
    private long pollIntervalMs = 1000;

    // Max rows claimed per poll
    private int batchSize = 10;

    // Fraction of simulated provider calls that fail transiently (0..1).
    private double simulatedFailureRate = 0.3;

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }
    public int getBackoffBaseSeconds() {
        return backoffBaseSeconds;
    }
    public void setBackoffBaseSeconds(int backoffBaseSeconds) {
        this.backoffBaseSeconds = backoffBaseSeconds;
    }
    public long getPollIntervalMs() {
        return pollIntervalMs;
    }
    public void setPollIntervalMs(long pollIntervalMs) {
        this.pollIntervalMs = pollIntervalMs; }
    public int getBatchSize() {
        return batchSize;
    }
    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }
    public double getSimulatedFailureRate() {
        return simulatedFailureRate; }
    public void setSimulatedFailureRate(double simulatedFailureRate) {
        this.simulatedFailureRate = simulatedFailureRate;
    }
}
