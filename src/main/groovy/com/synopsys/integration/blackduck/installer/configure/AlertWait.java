package com.synopsys.integration.blackduck.installer.configure;

import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.client.IntHttpClient;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.request.Response;
import org.apache.commons.lang3.time.DurationFormatUtils;

import java.io.IOException;
import java.time.Duration;

public class AlertWait {
    private final IntLogger intLogger;
    private final int timeoutInSeconds;
    private final IntHttpClient intHttpClient;
    private final Request alertRequest;

    public AlertWait(IntLogger intLogger, int timeoutInSeconds, IntHttpClient intHttpClient, Request alertRequest) {
        this.intLogger = intLogger;
        this.timeoutInSeconds = timeoutInSeconds;
        this.intHttpClient = intHttpClient;
        this.alertRequest = alertRequest;
    }

    public boolean waitForAlert() throws InterruptedException {
        int attempts = 0;
        long start = System.currentTimeMillis();

        Duration currentDuration = Duration.ofMillis(0);
        Duration maximumDuration = Duration.ofMillis(timeoutInSeconds * 1000);
        while (currentDuration.compareTo(maximumDuration) <= 0) {
            intLogger.info(String.format("Checking the Alert server...(try #%s, elapsed: %s)", attempts, DurationFormatUtils.formatDurationHMS(currentDuration.toMillis())));
            try (Response response = intHttpClient.execute(alertRequest)) {
                // at the moment, any valid http response is considered healthy
                intLogger.info(String.format("Alert server responded with %s - this means it is online!", response.getStatusCode()));
                return true;
            } catch (IntegrationException | IOException e) {
                intLogger.info(String.format("Exception trying to verify Alert. This may be okay as Alert may not be available yet: ", e.getMessage()));
            }

            intLogger.info(String.format("The Alert server is not responding successfully yet, waiting 30 seconds and trying again. (%s)"));
            Thread.sleep(30000);
            attempts++;
            currentDuration = Duration.ofMillis(System.currentTimeMillis() - start);
        }

        return false;
    }
}
