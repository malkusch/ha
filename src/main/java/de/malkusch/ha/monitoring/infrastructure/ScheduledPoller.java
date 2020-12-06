package de.malkusch.ha.monitoring.infrastructure;

import java.io.IOException;

import javax.annotation.PostConstruct;

import org.springframework.scheduling.annotation.Scheduled;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
final class ScheduledPoller implements Poller {

    private final Poller poller;

    @Override
    @Scheduled(fixedRateString = "${monitoring.updateRate}")
    @PostConstruct
    public void update() throws IOException, InterruptedException {
        poller.update();
    }
}
