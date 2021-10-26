package de.malkusch.ha.automation.model.heater;

import java.io.IOException;

public interface Heater {

    public static enum HotWaterMode {
        ECO, LOW, HIGH, OWNPROGRAM, OFF;
    }

    public void switchHotWaterMode(HotWaterMode mode) throws IOException, InterruptedException;

    public HotWaterMode currentHotWaterMode() throws IOException, InterruptedException;

    public HotWaterMode ownProgramHotWaterMode() throws IOException, InterruptedException;

    public static enum HeaterProgram {
        NIGHT, DAY
    }

    public HeaterProgram currentHeaterProgram() throws IOException, InterruptedException;

    public void changeTemporaryHeaterTemperatur(Temperature temperature) throws IOException, InterruptedException;

    public void resetTemporaryHeaterTemperatur() throws IOException, InterruptedException;

    public Temperature dayTemperature() throws IOException, InterruptedException;

}
