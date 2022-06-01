package de.malkusch.ha.automation.infrastructure.prometheus;

import java.math.BigDecimal;
import java.time.LocalDate;

import de.malkusch.ha.shared.model.ApiException;

public interface Prometheus {

    BigDecimal query(String query, LocalDate date) throws ApiException, InterruptedException;

    default BigDecimal query(String query) throws ApiException, InterruptedException {
        return query(query, null);
    }
}
