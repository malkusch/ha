package de.malkusch.ha.automation.model;

public final class NotFoundException extends Exception {
    private static final long serialVersionUID = -1092772763472749684L;

    public NotFoundException(String message) {
        super(message);
    }
}