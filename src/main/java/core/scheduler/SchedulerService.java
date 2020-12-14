package core.scheduler;

import akka.Done;
import akka.actor.CoordinatedShutdown;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.typesafe.config.Config;
import core.common.ServerConfig;
import core.common.ServerMode;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Singleton
public class SchedulerService {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger logger = LoggerFactory.getLogger(SchedulerService.class);

/*  VALUES -------------------------------------------------------------------------------------------------------------*/

    @Inject
    @SuppressWarnings("unchecked")
    public SchedulerService(Scheduler scheduler, CoordinatedShutdown coordinatedShutdown, ServerConfig serverConfig, Config config) {
        this.scheduler = scheduler;

        coordinatedShutdown.addTask(CoordinatedShutdown.PhaseBeforeServiceUnbind(), "scheduler-stop", () -> {
            try {
                this.scheduler.clear();
            } catch (Exception e) {
                logger.error("Error stopping scheduler", e);
            }
            return CompletableFuture.completedFuture(Done.done());
        });

        try {

            logger.info("constructor - scheduling jobs");

            List<String> packages = new ArrayList<>();

            try {
                packages = config.getStringList("scheduler.jobs");
            } catch (Exception e) {
                logger.warn("constructor - error loading scheduled jobs configuration", e);
            }

            ConfigurationBuilder builder = new ConfigurationBuilder().setScanners(new TypeAnnotationsScanner(), new SubTypesScanner());

            packages.forEach(pack -> builder.addUrls(ClasspathHelper.forPackage(pack)));

            Set<Class<?>> classes = new Reflections(builder).getTypesAnnotatedWith(Scheduled.class);

            ServerMode mode = serverConfig.getMode();

            classes.forEach(cls -> {

                if (Job.class.isAssignableFrom(cls)) {

                    Class<? extends Job> job = (Class<? extends Job>) cls; // Cast to job

                    // Check Restriction
                    Restrict restrict = job.getAnnotation(Restrict.class);
                    if (restrict != null) {
                        List<ServerMode> arrays = new ArrayList<>(Arrays.asList(restrict.value()));
                        if (!arrays.contains(mode))
                            return; // If this job is restricted in this mode skip the scheduling
                    }

                    // Check Scheduled Anotation
                    Scheduled annotation = job.getAnnotation(Scheduled.class);
                    String value = annotation.value();

                    logger.debug("constructor - scheduling job: '{}' with schedule: '{}'", job.getSimpleName(), value);

                    try {

                        JobKey jobKey = JobKey.jobKey(job.getSimpleName() + "_JobKey");

                        if (this.scheduler.checkExists(jobKey)) {
                            throw new SchedulerException("Job with key: " + jobKey.getName() + " already exists.");
                        }

                        this.scheduler.scheduleJob(JobBuilder.newJob(job).withIdentity(jobKey).build(),
                                TriggerBuilder.newTrigger().withIdentity(TriggerKey.triggerKey(job.getSimpleName() + "_TriggerKey")).startNow()
                                        .withSchedule(CronScheduleBuilder.cronSchedule(value))
                                        .build());

                    } catch (SchedulerException e) {
                        logger.error("constructor", e);
                    }
                } else {
                    logger.warn("constructor - class {} does not implement org.quartz.Job, thus cannot be scheduled", cls.getName());
                }
            });

            // Nastartování scheduleru
            this.scheduler.start();

        } catch (NullPointerException e) {

        } catch (Exception e) {
            logger.error("constructor", e);
        }
    }

    public Scheduler scheduler;

    public void schedule(JobDefinition jobDefinition) {
        logger.info("schedule - scheduling new job: {}", jobDefinition.getJobKey());
        try {
            this.scheduler.scheduleJob(
                    JobBuilder.newJob(jobDefinition.getJob()).withIdentity(jobDefinition.getJobKey()).usingJobData(new JobDataMap(jobDefinition.getDataMap())).build(),
                    TriggerBuilder.newTrigger().withIdentity(jobDefinition.getJobKey()).withSchedule(jobDefinition.getSchedule()).startNow().build());
        } catch (Exception e) {
            logger.error("schedule", e);
        }

    }

    public void unschedule(String jobKey) {
        logger.info("unschedule - unscheduling job: {}", jobKey);
        try {
            this.scheduler.deleteJob(new JobKey(jobKey));
        } catch (Exception e) {
            logger.error("unschedule", e);
        }
    }

    public boolean isScheduled(String jobKey) {
        try {
            return this.scheduler.checkExists(new JobKey(jobKey));
        } catch (Exception e) {
            logger.error("isScheduled", e);
            return false;
        }
    }

    public void show_all_jobs() throws SchedulerException {

        for (String groupName : scheduler.getJobGroupNames()) {

            for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {

                String jobName = jobKey.getName();
                String jobGroup = jobKey.getGroup();

                //get job's trigger
                List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);

                for(Trigger trigger : triggers) {
                    Date nextFireTime = triggers.get(0).getNextFireTime();
                    System.out.println("[jobName] : " + jobName + " [groupName] : " + jobGroup + " - " + nextFireTime);
                }
            }
        }
    }

    /**
     * Return Trigger Job where jobKey_sub_part is a part of name of Scheduler Job
     * For example "update-instance-" return list of all jobs waiting for running
     *
     * @param jobKey_sub_part
     * @return
     * @throws SchedulerException
     */
    public List<Trigger> get_Job(String jobKey_sub_part) throws SchedulerException {
        List<Trigger> triggers_jobs = new ArrayList<>();
        for (String groupName : scheduler.getJobGroupNames()) {
            for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {

                if(jobKey.getName().contains(jobKey_sub_part)){
                    triggers_jobs.add(scheduler.getTriggersOfJob(jobKey).get(0));
                }
            }
        }

        return triggers_jobs;
    }

    /**
     * Converts java Date to Cron schedule.
     * @param date the cron expression will be build from.
     * @return cron like schedule.
     */
    public static CronScheduleBuilder toCron(Date date) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        Integer second  = calendar.get(Calendar.SECOND);
        Integer minute  = calendar.get(Calendar.MINUTE);
        Integer hour    = calendar.get(Calendar.HOUR_OF_DAY);
        Integer day     = calendar.get(Calendar.DAY_OF_MONTH);
        Integer month   = calendar.get(Calendar.MONTH) + 1; // Months start from zero
        Integer year    = calendar.get(Calendar.YEAR);

        String cron = second.toString() + " " + minute.toString() + " " + hour.toString() + " " + day.toString() + " " + month.toString() + " ? " + year.toString();

        logger.debug("toCron: expression = {}", cron);

        return CronScheduleBuilder.cronSchedule(cron);
    }
}