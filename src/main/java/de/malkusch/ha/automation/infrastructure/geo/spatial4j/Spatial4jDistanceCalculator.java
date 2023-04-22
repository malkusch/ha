package de.malkusch.ha.automation.infrastructure.geo.spatial4j;

import org.locationtech.spatial4j.context.SpatialContext;
import org.locationtech.spatial4j.distance.DistanceUtils;
import org.locationtech.spatial4j.shape.Point;
import org.springframework.stereotype.Service;

import de.malkusch.ha.automation.model.geo.Distance;
import de.malkusch.ha.automation.model.geo.DistanceCalculator;
import de.malkusch.ha.automation.model.geo.Location;

@Service
final class Spatial4jDistanceCalculator implements DistanceCalculator {

    private final SpatialContext context = SpatialContext.GEO;

    @Override
    public Distance between(Location a, Location b) {
        return distance(context.calcDistance(point(a), point(b)));
    }

    private Distance distance(double distance) {
        return new Distance(distance * DistanceUtils.DEG_TO_KM * 1000);
    }

    private Point point(Location location) {
        return context.getShapeFactory().pointLatLon(location.latitude(), location.longitude());
    }

}
