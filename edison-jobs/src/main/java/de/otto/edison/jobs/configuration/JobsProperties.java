package de.otto.edison.jobs.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration properties used to configure the behaviour of module edison-jobs.
 *
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "edison.jobs")
public class JobsProperties {
    private boolean externalTrigger = true;
    private int threadCount = 10;
    private Cleanup cleanup = new Cleanup();
    private Status status = new Status();

    public boolean isExternalTrigger() {
        return externalTrigger;
    }

    public void setExternalTrigger(boolean externalTrigger) {
        this.externalTrigger = externalTrigger;
    }

    public int getThreadCount() {
        return threadCount;
    }

    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }

    public Cleanup getCleanup() {
        return cleanup;
    }

    public void setCleanup(Cleanup cleanup) {
        this.cleanup = cleanup;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public static class Cleanup {
        int numberOfToKeep = 100;
        int markDeadAfter = 30;

        public int getNumberOfToKeep() {
            return numberOfToKeep;
        }

        public void setNumberOfToKeep(int numberOfToKeep) {
            this.numberOfToKeep = numberOfToKeep;
        }

        public int getMarkDeadAfter() {
            return markDeadAfter;
        }

        public void setMarkDeadAfter(int markDeadAfter) {
            this.markDeadAfter = markDeadAfter;
        }
    }

    public static class Status {
        private boolean enabled = true;

        @NestedConfigurationProperty
        private Map<String,String> calculator = new HashMap<>();

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public Map<String, String> getCalculator() {
            return calculator;
        }

        public void setCalculator(Map<String, String> calculator) {
            this.calculator = calculator;
        }
    }
}
