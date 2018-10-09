package de.otto.edison.jobs.service;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

public class JobMutexGroupTest {

    @Test
    public void shouldAddAllJobTypes() {
        final JobMutexGroup group = new JobMutexGroup("Product Import Jobs", "FullImport", "DeltaImport");
        assertThat(group.getJobTypes(), contains("FullImport", "DeltaImport"));
    }

}