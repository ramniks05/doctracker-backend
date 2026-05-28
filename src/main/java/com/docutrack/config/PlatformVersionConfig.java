package com.docutrack.config;

public record PlatformVersionConfig(
    int minBuild,
    int latestBuild,
    String minVersion,
    String latestVersion,
    boolean forceUpdate,
    boolean softUpdate,
    String title,
    String message,
    String storeUrl,
    String releaseNotes
) {}
