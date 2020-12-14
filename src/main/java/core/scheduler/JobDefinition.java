package core.scheduler;

import org.quartz.Job;
import org.quartz.ScheduleBuilder;
import org.quartz.Trigger;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static core.scheduler.SchedulerService.toCron;

public class JobDefinition {

    private final String jobKey;
    private final Class<? extends Job> job;

    private Map<String, String> dataMap = new HashMap<>();
    private ScheduleBuilder scheduleBuilder;

    public JobDefinition(String jobKey, Class<? extends Job> job) {
        this.jobKey = jobKey;
        this.job = job;
    }

    public String getJobKey() {
        return jobKey;
    }

    public Class<? extends Job> getJob() {
        return job;
    }

    public Map<String, String> getDataMap() {
        return this.dataMap;
    }

    @SuppressWarnings("unchecked")
    public <T extends Trigger> ScheduleBuilder<T> getSchedule() {
        return (ScheduleBuilder<T>) this.scheduleBuilder;
    }

    public JobDefinition setDate(Date date) {
        this.scheduleBuilder = toCron(date);
        return this;
    }

    public JobDefinition setScheduleBuilder(ScheduleBuilder scheduleBuilder) {
        this.scheduleBuilder = scheduleBuilder;
        return this;
    }

    public JobDefinition setData(String key, String value) {
        this.dataMap.put(key, value);
        return this;
    }
}
