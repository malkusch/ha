package de.malkusch.ha.notification.model;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@EqualsAndHashCode
public final class Notification {
    final String message;

    @Override
    public String toString() {
        return message;
    }
}
