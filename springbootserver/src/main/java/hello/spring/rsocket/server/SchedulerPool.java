package hello.spring.rsocket.server;

import org.springframework.util.StringUtils;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

public class SchedulerPool {

    private static SchedulerPool instance = new SchedulerPool();

    public static SchedulerPool getInstance() {
        return instance;
    }

    private Scheduler[] schedulers;

    public SchedulerPool() {
        schedulers = new Scheduler[Schedulers.DEFAULT_POOL_SIZE];
        for (int i = 0; i < schedulers.length; i++) {
            schedulers[i] = Schedulers.newSingle("client-" + i);
        }
    }

    public Scheduler getScheduler(long id) {
        int index = (int) (id % schedulers.length);
        return schedulers[index];
    }

    public Scheduler getScheduler(String id) {
        long idLong = 0;
        if(!isLong(id)) {
            idLong = id.hashCode();
        } else {
            idLong = Long.parseLong(id);
        }
        return getScheduler(idLong);
    }

    private boolean isLong(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            long d = Long.parseLong(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

}
