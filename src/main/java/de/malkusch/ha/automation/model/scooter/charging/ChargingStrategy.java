package de.malkusch.ha.automation.model.scooter.charging;

import de.malkusch.ha.automation.model.scooter.charging.ChargingStrategy.Evaluation.Reason;
import de.malkusch.ha.automation.model.scooter.charging.ChargingStrategy.Evaluation.Request;
import de.malkusch.ha.automation.model.scooter.charging.ContextFactory.Context;

abstract class ChargingStrategy implements Comparable<ChargingStrategy> {

    abstract public Evaluation evaluate(Context context) throws Exception;

    protected Evaluation start(String reason) {
        return new Evaluation(Request.START, reason(reason));
    }

    protected Evaluation stop(String reason) {
        return new Evaluation(Request.STOP, reason(reason));
    }

    private Reason reason(String description) {
        return new Reason(getClass().getSimpleName(), description);
    }

    static record Evaluation(Request request, Reason reason) {
        static enum Request {
            START, STOP, NONE
        }

        static record Reason(String id, String description) {

            @Override
            public String toString() {
                return String.format("[%s] %s", id, description);
            }
        }

        static final Evaluation NONE = new Evaluation(Request.NONE, new Reason("NONE", "nothing matched"));

        @Override
        public String toString() {
            return String.format("%s - %s", request, reason);
        }
    }

    @Override
    public int compareTo(ChargingStrategy other) {
        return getClass().getSimpleName().compareTo(other.getClass().getSimpleName());
    }
}
