package de.malkusch.ha.automation.model.shutters;

import java.time.Duration;

import de.malkusch.ha.shared.model.ApiException;

public final class Blind extends Shutter {

    public Blind(ShutterId id, Api api, Duration delay, DirectSunLightRange directSunLightRange)
            throws ApiException, InterruptedException {
        super(id, api, delay, directSunLightRange);
    }
}
