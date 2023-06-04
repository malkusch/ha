package de.malkusch.ha.automation.infrastructure.prometheus;

import static java.util.Locale.ENGLISH;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;

import de.malkusch.ha.automation.infrastructure.prometheus.Prometheus.AggregationQuery.Aggregation;
import de.malkusch.ha.shared.model.ApiException;

public interface Prometheus {

    BigDecimal query(Query query, LocalDate end) throws ApiException, InterruptedException;

    BigDecimal query(Query query, Instant time) throws ApiException, InterruptedException;

    BigDecimal query(Query query) throws ApiException, InterruptedException;

    public static interface Query {
        String promQL();

        default Subquery subquery(Duration range, Duration resolution) {
            return new Subquery(this, range, resolution);
        }

        default Subquery subquery(Duration range) {
            return new Subquery(this, range);
        }

        default Query query(String query) {
            return new SimpleQuery(query, promQL());
        }
    }

    public static record AggregationQuery(Subquery subquery, Aggregation aggregation) implements Query {

        public static enum Aggregation {
            MINIMUM, P25, MAXIMUM, P75, P95, COUNT, DELTA
        }

        public AggregationQuery(String query, Duration range, Aggregation aggregation) {
            this(new SimpleQuery(query), range, aggregation);
        }

        public AggregationQuery(Query query, Duration range, Aggregation aggregation) {
            this(new Subquery(query, range), aggregation);
        }

        @Override
        public String promQL() {
            var query = switch (aggregation) {
            case MINIMUM -> "min_over_time(%s)";
            case P25 -> "quantile_over_time(0.25, %s)";
            case P75 -> "quantile_over_time(0.75, %s)";
            case P95 -> "quantile_over_time(0.95, %s)";
            case MAXIMUM -> "max_over_time(%s)";
            case COUNT -> "count_over_time(%s)";
            case DELTA -> "delta(%s)";
            };
            return String.format(query, subquery.promQL());
        }

    }

    public static record SimpleQuery(String promQL) implements Query {
        public SimpleQuery(String format, Object... arguments) {
            this(String.format(ENGLISH, format, arguments));
        }
    }

    public static record Subquery(Query query, Duration range, Duration resolution) implements Query {

        public Subquery(Query query, Duration range) {
            this(query, range, null);
        }

        public Subquery(String query, Duration range) {
            this(new SimpleQuery(query), range);
        }

        @Override
        public String promQL() {
            return String.format("%s[%s:%s]", query.promQL(), timeDuration(range), timeDuration(resolution));
        }

        private static String timeDuration(Duration duration) {
            return duration == null ? "" : duration.toSeconds() + "s";
        }

        public AggregationQuery aggregate(Aggregation aggregation) {
            return new AggregationQuery(this, aggregation);
        }
    }
}
