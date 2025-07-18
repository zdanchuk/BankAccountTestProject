package com.example;

import java.time.LocalDateTime;

/**
 * An interface that provides the current date and time.
 * This abstraction allows for injecting a fixed clock for testing.
 */
public interface DateProvider {
    LocalDateTime now();
}