package core.scheduler;

import com.google.inject.AbstractModule;
import org.quartz.Scheduler;

public class SchedulerModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(Scheduler.class).toProvider(SchedulerProvider.class);
    }
}
