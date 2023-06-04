package de.malkusch.ha.automation.model.scooter.charging;

import static org.apache.commons.lang3.ArrayUtils.addAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import de.malkusch.ha.automation.model.electricity.Capacity;
import de.malkusch.ha.automation.model.electricity.Electricity;
import de.malkusch.ha.automation.model.electricity.Watt;
import de.malkusch.ha.automation.model.geo.Distance;
import de.malkusch.ha.automation.model.geo.DistanceCalculator;
import de.malkusch.ha.automation.model.scooter.BalancingService;
import de.malkusch.ha.automation.model.scooter.Scooter;
import de.malkusch.ha.automation.model.scooter.Scooter.State;
import de.malkusch.ha.automation.model.scooter.ScooterWallbox;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ChargingRuleTest {

    @Mock
    ScooterWallbox wallbox;

    @Mock
    BalancingService balancingService;

    @Mock
    Scooter scooter;

    @Mock
    Electricity electricity;

    @Mock
    DistanceCalculator distanceCalculator;

    ChargingRule rule;

    @BeforeEach
    void setupRule() {
        List<ChargingStrategy> strategies = Arrays.asList(//
                new ChargingStrategy_0_StopScooterStates(),
                new ChargingStrategy_1_StopScooterNotNearWallbox(scooter, wallbox, distanceCalculator,
                        new Distance(MAX_DISTANCE)),
                new ChargingStrategy_3_StopMaximumCharge(charge(MAXIMUM_CHARGE)), //
                new ChargingStrategy_4_StartMinimumCharge(charge(MINIMUM_START_CHARGE), charge(MINIMUM_STOP_CHARGE)), //
                new ChargingStrategy_5_StartExcessCharging(charge(EXCESS_START_CHARGE), new Watt(EXCESS_START_EXCESS)), //
                new ChargingStrategy_6_StopExcessCharging(new Watt(STOP_EXCESS))
        //
        );
        var contextFactory = new ContextFactory(scooter, electricity, ANY_DURATION, balancingService);
        rule = new ChargingRule(ANY_DURATION, contextFactory, strategies, wallbox);
    }

    private static final Duration ANY_DURATION = Duration.ofMinutes(1);

    private static final double MINIMUM_START_CHARGE = 0.3;
    private static final double MINIMUM_STOP_CHARGE = 0.5;

    private static final double EXCESS_START_CHARGE = 0.7;
    private static final int EXCESS_START_EXCESS = 500;

    private static final int STOP_EXCESS = 50;

    private static final double MAXIMUM_CHARGE = 0.8;

    private static final int MAX_DISTANCE = 15;

    static final Scenario ANY_CHARGING = new Scenario(State.CHARGING, new Distance(3), charge(0.75),
            excessProduction(500), recentExcess(1000), charging(true));

    static final Scenario UNCHANGED_CHARGING = ANY_CHARGING.withCharge(EXCESS_START_CHARGE + 0.1)
            .withExcess(EXCESS_START_EXCESS - 1).withDistance(MAX_DISTANCE - 1);

    static final Scenario UNCHANGED_NOT_CHARGING = UNCHANGED_CHARGING.stopCharging();

    static final Scenario EXCESS_CHARGING = UNCHANGED_CHARGING.startExcessLoading();
    static final Scenario STOPPED_EXCESS = EXCESS_CHARGING.stopExcessLoading().stopCharging();
    static final Scenario MINIMUM_CHARGING = STOPPED_EXCESS.belowMinimumCharge().startCharging();
    static final Scenario STOPPED_MAXIMUM = EXCESS_CHARGING.withMaximumCharge().stopCharging();
    static final Scenario STOPPED_DISTANCE = MINIMUM_CHARGING.withFarDistance().stopCharging();
    static final Scenario OFFLINE_MINIMUM_CHARGING = MINIMUM_CHARGING.withOffline().stopCharging();
    static final Scenario OFFLINE_EXCESS_CHARGING = EXCESS_CHARGING.withOffline().stopCharging();

    final static TestCase[] STOP_EXCESS_CASES = { //
            started(STOPPED_EXCESS.belowMinimumCharge()), //
            started(STOPPED_EXCESS.startExcessLoading()), //

            unchanged(EXCESS_CHARGING.withExcess(EXCESS_START_EXCESS - 1)), //
            unchanged(EXCESS_CHARGING.withExcess(STOP_EXCESS + 1)), //

            stoped(EXCESS_CHARGING.stopExcessLoading()), //
    };

    final static TestCase[] START_EXCESS_CASES = { //
            started(STOPPED_EXCESS.startExcessLoading()), //

            unchanged(STOPPED_EXCESS.startExcessLoading().withCharge(EXCESS_START_CHARGE + 0.1)), //
            unchanged(STOPPED_EXCESS.startExcessLoading().withExcess(EXCESS_START_EXCESS - 1)), //
            unchanged(EXCESS_CHARGING.withExcess(EXCESS_START_EXCESS - 1)),
            unchanged(EXCESS_CHARGING.withCharge(EXCESS_START_CHARGE + 0.1)),

            stoped(EXCESS_CHARGING.withMaximumCharge()), //
            stoped(EXCESS_CHARGING.withFarDistance()), //
            stoped(EXCESS_CHARGING.stopExcessLoading()), //
            stoped(EXCESS_CHARGING.withOffline()), //
    };

    final static TestCase[] START_MINIMUM_CASES = { //
            started(STOPPED_EXCESS.belowMinimumCharge()), //
            started(EXCESS_CHARGING.stopCharging().belowMinimumCharge()), //

            unchanged(STOPPED_EXCESS.withCharge(MINIMUM_START_CHARGE + 0.1)), //
            unchanged(MINIMUM_CHARGING.withCharge(MINIMUM_START_CHARGE + 0.1).stopExcessLoading()), //
            unchanged(MINIMUM_CHARGING.withCharge(MINIMUM_STOP_CHARGE - 0.1).stopExcessLoading()), //

            stoped(MINIMUM_CHARGING.withCharge(MINIMUM_STOP_CHARGE + 0.1).stopExcessLoading()), //
            stoped(MINIMUM_CHARGING.withOffline()), //
            stoped(MINIMUM_CHARGING.withFarDistance()), //
    };

    final static TestCase[] STOP_MAXIMUM_CASES = { //
            started(STOPPED_MAXIMUM.belowMinimumCharge()), //
            started(STOPPED_MAXIMUM.startExcessLoading()), //

            unchanged(STOPPED_MAXIMUM.withExcess(EXCESS_START_EXCESS + 1)), //
            unchanged(EXCESS_CHARGING.withCharge(MAXIMUM_CHARGE - 0.1)), //
            unchanged(STOPPED_MAXIMUM.withCharge(MAXIMUM_CHARGE - 0.1)), //
            unchanged(STOPPED_MAXIMUM.withCharge(MAXIMUM_CHARGE - 0.1).withExcess(EXCESS_START_EXCESS + 1)), //

            stoped(MINIMUM_CHARGING.withMaximumCharge()), //
            stoped(EXCESS_CHARGING.withMaximumCharge()), //
    };

    final static TestCase[] STOP_DISTANCE_CASES = { //
            started(STOPPED_DISTANCE.withDistance(MAX_DISTANCE - 1).belowMinimumCharge()), //
            started(STOPPED_DISTANCE.withDistance(MAX_DISTANCE - 1).startExcessLoading()), //

            unchanged(EXCESS_CHARGING.withDistance(MAX_DISTANCE - 1)), //
            unchanged(MINIMUM_CHARGING.withDistance(MAX_DISTANCE - 1)), //
            unchanged(STOPPED_DISTANCE.belowMinimumCharge()), //
            unchanged(STOPPED_DISTANCE.startExcessLoading()), //

            stoped(MINIMUM_CHARGING.withFarDistance()), //
            stoped(EXCESS_CHARGING.withFarDistance()), //
    };

    final static TestCase[] STOP_STATE_CASES = { //
            started(OFFLINE_MINIMUM_CHARGING.withState(State.READY_TO_CHARGE)), //
            started(OFFLINE_MINIMUM_CHARGING.withState(State.CHARGING)), //
            started(OFFLINE_EXCESS_CHARGING.withState(State.READY_TO_CHARGE)), //
            started(OFFLINE_EXCESS_CHARGING.withState(State.CHARGING)), //

            unchanged(EXCESS_CHARGING.withState(State.READY_TO_CHARGE)), //
            unchanged(EXCESS_CHARGING.withState(State.CHARGING)), //

            stoped(MINIMUM_CHARGING.withOffline()), //
            stoped(EXCESS_CHARGING.withOffline()), //
            stoped(MINIMUM_CHARGING.withState(State.BATTERY_DISCONNECTED)), //
            stoped(EXCESS_CHARGING.withState(State.BATTERY_DISCONNECTED)), //
    };

    public static TestCase[] TEST_CASES() {
        return addAll(
                addAll(addAll(STOP_EXCESS_CASES, START_EXCESS_CASES), addAll(START_MINIMUM_CASES, STOP_MAXIMUM_CASES)),
                addAll(STOP_DISTANCE_CASES, STOP_STATE_CASES));
    }

    @ParameterizedTest
    @MethodSource("TEST_CASES")
    void shouldStartExcessCharge(TestCase testCase) throws Exception {
        given(testCase.scenario);

        rule.evaluate();

        assertExpectation(testCase.expectation);
    }

    private static Capacity charge(double charge) {
        return new Capacity(charge);
    }

    private static Watt excessProduction(int excessProduction) {
        return new Watt(excessProduction);
    }

    private static Watt recentExcess(int recentExcess) {
        return new Watt(recentExcess);
    }

    private static boolean charging(boolean charging) {
        return charging;
    }

    record TestCase(Expectation expectation, Scenario scenario) {
    }

    private static TestCase unchanged(Scenario scenario) {
        return new TestCase(Expectation.UNCHANGED, scenario);
    }

    private static TestCase stoped(Scenario scenario) {
        return new TestCase(Expectation.STOPPED, scenario);
    }

    private static TestCase started(Scenario scenario) {
        return new TestCase(Expectation.STARTED, scenario);
    }

    enum Expectation {
        UNCHANGED, STARTED, STOPPED
    }

    private void assertExpectation(Expectation expectation) throws Exception {
        switch (expectation) {
        case UNCHANGED -> assertUnchanged();
        case STARTED -> assertStartCharging();
        case STOPPED -> assertStopCharging();
        }
    }

    private void assertUnchanged() throws Exception {
        verify(wallbox, never()).startCharging();
        verify(wallbox, never()).stopCharging();
    }

    private void assertStartCharging() throws Exception {
        verify(wallbox).startCharging();
        verify(wallbox, never()).stopCharging();
    }

    private void assertStopCharging() throws Exception {
        verify(wallbox).stopCharging();
        verify(wallbox, never()).startCharging();
    }

    record Scenario(State state, Distance distance, Capacity charge, Watt excessProduction, Watt recentExcess,
            boolean charging) {

        Scenario withDistance(int distance) {
            return new Scenario(state, new Distance(distance), charge, excessProduction, recentExcess, charging);
        }

        Scenario withCharge(double charge) {
            return new Scenario(state, distance, ChargingRuleTest.charge(charge), excessProduction, recentExcess,
                    charging);
        }

        Scenario withCharging(boolean charging) {
            return new Scenario(state, distance, charge, excessProduction, recentExcess, charging);
        }

        Scenario withState(State state) {
            return new Scenario(state, distance, charge, excessProduction, recentExcess, charging);
        }

        Scenario withExcess(int excess) {
            return new Scenario(state, distance, charge, ChargingRuleTest.excessProduction(excess),
                    ChargingRuleTest.recentExcess(excess), charging);
        }

        Scenario startExcessLoading() {
            return withExcess(EXCESS_START_EXCESS + 1).withCharge(EXCESS_START_CHARGE - 0.1);
        }

        Scenario startCharging() {
            return withState(State.CHARGING).withCharging(true);
        }

        Scenario stopCharging() {
            return withState(State.READY_TO_CHARGE).withCharging(false);
        }

        Scenario stopExcessLoading() {
            return withExcess(STOP_EXCESS - 1);
        }

        Scenario belowMinimumCharge() {
            return withCharge(MINIMUM_START_CHARGE - 0.1);
        }

        Scenario withMaximumCharge() {
            return withCharge(MAXIMUM_CHARGE + 0.1);
        }

        Scenario withOffline() {
            return withState(State.OFFLINE);
        }

        Scenario withFarDistance() {
            return withDistance(MAX_DISTANCE + 1);
        }
    }

    void given(Scenario scenario) throws Exception {
        when(scooter.state()).thenReturn(scenario.state);
        when(scooter.charge()).thenReturn(scenario.charge);
        when(distanceCalculator.between(any(), any())).thenReturn(scenario.distance);
        when(electricity.excessProduction()).thenReturn(scenario.excessProduction);
        when(electricity.excessProduction(any(), any())).thenReturn(scenario.recentExcess);
        when(wallbox.isCharging()).thenReturn(scenario.charging);
    }
}
