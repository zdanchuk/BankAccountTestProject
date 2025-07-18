package com.example;

import java.time.LocalDateTime;

public class SystemClock implements DateProvider {
    @Override
    public LocalDateTime now() {
        return LocalDateTime.now();
    }
}