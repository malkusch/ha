package de.malkusch.ha.automation.model.heater;

import java.io.IOException;

public interface Heater {

    public static enum HotWaterMode {
        ECO, LOW, HIGH, OWNPROGRAM;
    }

    public void switchHotWaterMode(HotWaterMode mode) throws IOException, InterruptedException;

    public HotWaterMode currentHotWaterMode() throws IOException, InterruptedException;
    
    public HotWaterMode ownProgramHotWaterMode() throws IOException, InterruptedException;
}
