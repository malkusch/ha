package de.malkusch.ha.shared.infrastructure.http;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.time.Duration;

import org.slf4j.Logger;

import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.FailsafeException;
import net.jodah.failsafe.FailsafeExecutor;
import net.jodah.failsafe.RetryPolicy;

final class RetryingHttpClient extends HttpClientProxy {

	private final FailsafeExecutor<HttpResponse> retry;
	private static final Logger LOGGER = getLogger(RetryingHttpClient.class);

	RetryingHttpClient(HttpClient client, Duration delay, int retries) {
		super(client);

		var policy = new RetryPolicy<HttpResponse>();
		policy.handle(IOException.class);
		policy.withDelay(delay);
		policy.withMaxRetries(retries);
		policy.onRetry(it -> LOGGER.warn("Retrying", it.getLastFailure()));
		retry = Failsafe.with(policy);
	}

	@Override
	HttpResponse proxied(Operation op) throws IOException, InterruptedException {
		try {
			return retry.get(op::send);

		} catch (FailsafeException e) {
			var cause = e.getCause();

			if (cause instanceof IOException) {
				throw (IOException) cause;

			} else if (cause instanceof InterruptedException) {
				throw (InterruptedException) cause;

			} else {
				throw e;
			}
		}
	}
}