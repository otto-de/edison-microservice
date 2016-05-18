package de.otto.edison.jobs.service;

import de.otto.edison.jobs.definition.JobDefinition;

import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;

/**
 * A group of mutually exclusive jobs.
 *
 * Created by guido on 18.05.16.
 */
public class JobMutexGroup {
    private final String groupName;
    private final Set<String> jobTypes;

    public JobMutexGroup(final String groupName, final String jobType, final String... moreJobTypes) {
        this.groupName = groupName;
        this.jobTypes = new HashSet<String>() {{
            add(jobType);
            addAll(asList(moreJobTypes));
        }};
    }

    /**
     *
     * @return name of the group
     */
    public String getGroupName() {
        return groupName;
    }

    /**
     *
     * @return the set of mutually exclusive {@link JobDefinition#jobType() job types}.
     */
    public Set<String> getJobTypes() {
        return jobTypes;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JobMutexGroup that = (JobMutexGroup) o;

        if (groupName != null ? !groupName.equals(that.groupName) : that.groupName != null) return false;
        return !(jobTypes != null ? !jobTypes.equals(that.jobTypes) : that.jobTypes != null);

    }

    @Override
    public int hashCode() {
        int result = groupName != null ? groupName.hashCode() : 0;
        result = 31 * result + (jobTypes != null ? jobTypes.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "JobMutexGroup{" +
                "groupName='" + groupName + '\'' +
                ", jobTypes=" + jobTypes +
                '}';
    }
}
