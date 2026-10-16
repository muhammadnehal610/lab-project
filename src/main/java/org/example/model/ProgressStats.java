package org.example.model;

public record ProgressStats(
        int totalProblems,
        int totalSolved,
        int totalEasy,
        int easySolved,
        int totalMedium,
        int mediumSolved,
        int totalHard,
        int hardSolved
) {}