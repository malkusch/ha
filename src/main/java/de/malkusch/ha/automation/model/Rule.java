package de.malkusch.ha.automation.model;

import java.time.Duration;

public interface Rule {

    void evaluate() throws Exception;

    Duration evaluationRate();
}
