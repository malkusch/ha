package de.malkusch.ha.automation.model.room;

import java.time.Duration;

import de.malkusch.ha.automation.model.Rule;
import de.malkusch.ha.automation.model.climate.CO2;
import de.malkusch.ha.automation.model.climate.ClimateService;
import de.malkusch.ha.automation.model.climate.Dust;
import de.malkusch.ha.shared.model.ApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class OpenWindowRule implements Rule {

    private final ClimateService climateService;
    private final RoomRepository rooms;
    private final Duration evaluationRate;
    private final CO2Threshold co2Threshold;
    private final Dust.PM2_5 buffer;

    public static record CO2Threshold(CO2 best, CO2 old, CO2 unhealthy) {
    }

    @Override
    public void evaluate() throws Exception {

        var outsideDust = climateService.outsideDust();
        log.debug("Outside dust is {}", outsideDust);
        var evaluation = new Evalutation(outsideDust.pm2_5());

        for (var room : rooms.findAll()) {
            var co2 = climateService.co2(room.id);
            var dust = climateService.dust(room.id);

            if (co2.isPresent() && dust.isPresent()) {
                evaluation.evaluate(room, co2.get(), dust.get().pm2_5());
            } else if (co2.isPresent() && !dust.isPresent()) {
                evaluation.evaluate(room, co2.get());
            } else if (!co2.isPresent() && dust.isPresent()) {
                evaluation.evaluate(room, dust.get().pm2_5());
            }
        }
    }

    @RequiredArgsConstructor
    private class Evalutation {
        private final Dust.PM2_5 outsideDust;

        private void evaluate(Room room, CO2 co2, Dust.PM2_5 dust) throws ApiException {
            log.debug("Room {}: co2 = {}, dust = {}", room, co2, dust);
            
            if (dust.isGreaterThan(outsideDust.plus(buffer))) {
                log.debug("dust.isGreaterThan(outsideDust.plus(buffer)");
                room.signalOpenWindows();

            } else if (co2.isGreaterThan(co2Threshold.unhealthy)) {
                log.debug("co2.isGreaterThan(co2Threshold.unhealthy)");
                room.signalOpenWindows();
                
            } else if (co2.isGreaterThan(co2Threshold.old)) {
                log.debug("co2.isGreaterThan(co2Threshold.old)");
                if (dust.isLessThan(outsideDust)) {
                    log.debug("dust.isLessThan(outsideDust)");
                    room.signalCloseWindows();
                    
                } else {
                    room.signalOldAir();
                }
            } else if (co2.isLessThan(co2Threshold.best)) {
                log.debug("co2.isLessThan(co2Threshold.best)");
                room.signalCloseWindows();
            }
        }

        private void evaluate(Room room, CO2 co2) throws ApiException {
            if (co2.isGreaterThan(co2Threshold.unhealthy)) {
                room.signalOpenWindows();
            } else if (co2.isGreaterThan(co2Threshold.old)) {
                room.signalOldAir();
            } else if (co2.isLessThan(co2Threshold.best)) {
                room.signalCloseWindows();
            }
        }

        private void evaluate(Room room, Dust.PM2_5 dust) throws ApiException {
            if (dust.isGreaterThan(outsideDust.plus(buffer))) {
                room.signalOpenWindows();
            } else {
                room.signalCloseWindows();
            }
        }
    }

    @Override
    public Duration evaluationRate() {
        return evaluationRate;
    }
}
