package com.example.githuborgsnapshot.controller;

import java.time.Instant;

public record ApiError(
        String path,
        int status,
        String error,
        String message,
        Instant timestamp
) {}
