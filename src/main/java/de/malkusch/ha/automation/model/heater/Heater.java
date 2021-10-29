package de.malkusch.ha.automation.model.heater;

import de.malkusch.ha.shared.model.ApiException;

public interface Heater {

    public static enum HotWaterMode {
        ECO, LOW, HIGH, OWNPROGRAM, OFF;
    }

    public void switchHotWaterMode(HotWaterMode mode) throws ApiException, InterruptedException;

    public HotWaterMode currentHotWaterMode() throws ApiException, InterruptedException;

    public HotWaterMode ownProgramHotWaterMode() throws ApiException, InterruptedException;

    public Temperature hotwaterHighTemperature() throws ApiException, InterruptedException;

    public void changeHotwaterHighTemperature(Temperature temperature) throws ApiException, InterruptedException;

    public static enum HeaterProgram {
        NIGHT, DAY
    }

    public HeaterProgram currentHeaterProgram() throws ApiException, InterruptedException;

    public void changeTemporaryHeaterTemperatur(Temperature temperature) throws ApiException, InterruptedException;

    public void resetTemporaryHeaterTemperatur() throws ApiException, InterruptedException;

    public Temperature dayTemperature() throws ApiException, InterruptedException;

    public boolean isHeating() throws ApiException, InterruptedException;

    public boolean isWinter();
}
