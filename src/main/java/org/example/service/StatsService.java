package org.example.service;

import org.example.model.ProgressStats;
import org.example.repository.StatsRepository;

public class StatsService {

    private final StatsRepository statsRepository;

    public StatsService() {
        this.statsRepository = new StatsRepository();
    }

    public ProgressStats getUserProgress(int userId) {
        if (userId <= 0) {
            return new ProgressStats(0, 0, 0, 0, 0, 0 , 0 , 0);
        }
        return statsRepository.fetchUserProgress(userId);
    }
}