package de.malkusch.ha.automation.infrastructure.dehumidifier.midea_python;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Arrays;

import de.malkusch.ha.automation.model.ApiException;
import de.malkusch.ha.automation.model.dehumidifier.Dehumidifier.DehumidifierId;
import lombok.extern.slf4j.Slf4j;

@Slf4j
final class Session implements AutoCloseable {

    private final Process process;
    private final BufferedReader stdout;
    private final OutputStream stdin;

    public Session(DehumidifierId id, String loginAccount, String password) throws ApiException {
        try {
            process = new ProcessBuilder("python3", "/home/malkusch/tmp/midea_inventor_dehumidifier/dehumi_control.py",
                    "-e", loginAccount, "-p", password).redirectErrorStream(true).start();
            stdout = new BufferedReader(new InputStreamReader(process.getInputStream()));
            stdin = process.getOutputStream();

            readUntilPrompt();
            enter(Command.LOGIN);
            enter(Command.SET_ID, id.getId());
            enter(Command.GET_STATUS);
        } catch (IOException e) {
            throw new ApiException("Failed to start midea session for device " + id, e);
        }
    }

    public static enum Command {
        LOGIN("0"), LIST("1"), SET_ID("2"), GET_STATUS("3"), ON("4"), OFF("5"), FAN_SPEED("6"), EXIT("99");

        private Command(String value) {
            this.bytes = (value + "\n").getBytes(US_ASCII);
        }

        private final byte[] bytes;
    }

    public void enter(Command command, String... arguments) throws ApiException {
        try {
            log.debug("{} {}", command, Arrays.toString(arguments));
            stdin.write(command.bytes);
            for (var arg : arguments) {
                stdin.write((arg + "\n").getBytes(US_ASCII));
            }
            stdin.flush();

            var result = readUntilPrompt();
            if (result.contains("ERROR") || !process.isAlive()) {
                throw new ApiException(String.format("%s failed: %s ", command, result));
            }

        } catch (IOException e) {
            throw new ApiException("Failed to execute " + command, e);
        }
    }

    private static final String PROMPT = "99: Exit";

    private String readUntilPrompt() throws IOException {
        var buffer = new StringBuilder();
        for (var line = ""; line != null && !line.startsWith(PROMPT); line = stdout.readLine()) {
            buffer.append(line);
            buffer.append("\n");
        }
        return buffer.toString();
    }

    @Override
    public void close() throws ApiException, InterruptedException {
        enter(Command.EXIT);
        try (stdout; stdin) {
            if (process.waitFor(5, SECONDS)) {
                return;
            }
            process.destroy();
            if (process.waitFor(3, SECONDS)) {
                return;
            }
            if (!process.destroyForcibly().waitFor(2, SECONDS)) {
                throw new ApiException("Cannot stop Midea Python process");
            }

        } catch (IOException e) {
            throw new ApiException("Cannot stop Midea Python process", e);
        }
    }
}
