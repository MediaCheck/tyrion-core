package core.scheduler;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;

@Singleton
public class SchedulerProvider implements Provider<Scheduler> {
    private Scheduler scheduler;

    @Inject
    public SchedulerProvider(SchedulerJobFactory jobFactory) throws SchedulerException {
        scheduler = StdSchedulerFactory.getDefaultScheduler();
        scheduler.setJobFactory(jobFactory);
    }

    @Override
    public Scheduler get() {
        return scheduler;
    }
}