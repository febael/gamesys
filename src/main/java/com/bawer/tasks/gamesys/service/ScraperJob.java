package com.bawer.tasks.gamesys.service;

import com.bawer.tasks.gamesys.repository.jdbc.h2.ConversionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
class ScraperJob {

    private static final Logger logger = LoggerFactory.getLogger(ScraperJob.class);

    private final List<ConversionTask> tasks;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    @Autowired
    ScraperJob(List<ConversionTask> tasks) {
        this.tasks = tasks;
    }

    void start() {
        if (tasks == null || tasks.isEmpty()) {
            throw new RuntimeException("No tasks to work with, refusing to start");
        }
        scheduler.schedule(this::getLatestAndSave, 1, TimeUnit.MILLISECONDS);
    }

    private void getLatestAndSave() {
        for (Iterator<ConversionTask> it = tasks.iterator(); it.hasNext();) {
            var task = it.next();
            boolean successfulUpdate = task.execute();
            if (!successfulUpdate) {
                logger.warn("removing task as it failed");
                it.remove();
            }
        }
        if (tasks.isEmpty()) {
            logger.warn("Not rescheduling as no tasks are available");
        } else {
            scheduler.schedule(this::getLatestAndSave, 1, TimeUnit.MINUTES);
        }
    }

    void end() {
        logger.info("Cancelling waiting tasks and terminating");
        scheduler.shutdownNow();
    }
}
