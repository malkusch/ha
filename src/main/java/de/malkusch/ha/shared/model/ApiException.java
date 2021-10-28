package de.malkusch.ha.shared.model;

public final class ApiException extends Exception {
    private static final long serialVersionUID = -1092772763472749684L;

    public ApiException(Throwable cause) {
        super(cause);
    }

    public ApiException(String message) {
        super(message);
    }

    public ApiException(String message, Throwable cause) {
        super(message, cause);
    }
}