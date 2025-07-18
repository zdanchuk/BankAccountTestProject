package com.example;

import java.time.LocalDateTime;

/**
 * A test-only implementation of DateProvider that allows for manually setting
 * the time before each operation. This provides fine-grained control for testing.
 */
public class MutableClock implements DateProvider {

    private LocalDateTime currentTime;

    /**
     * Sets the timestamp that the next call to now() will return.
     * @param time The timestamp to be returned.
     */
    public void setTime(LocalDateTime time) {
        this.currentTime = time;
    }

    @Override
    public LocalDateTime now() {
        if (currentTime == null) {
            throw new IllegalStateException("Time has not been set on MutableClock. Call setTime() before the operation.");
        }
        return currentTime;
    }
}