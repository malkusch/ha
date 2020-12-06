package de.malkusch.ha.monitoring.infrastructure;

import java.io.IOException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
final class OfflinePoller implements Poller {

    private final Poller poller;

    @Override
    public void update() throws InterruptedException {
        try {
            poller.update();
        } catch (IOException e) {
            log.warn("Failed polling {}", poller);
        }
    }
}
