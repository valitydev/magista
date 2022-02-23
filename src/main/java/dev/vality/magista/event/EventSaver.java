package dev.vality.magista.event;

import dev.vality.magista.exception.NotFoundException;
import dev.vality.magista.exception.StorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Created by tolkonepiu on 31/10/2016.
 */
public class EventSaver implements Runnable {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final BlockingQueue<Future<Processor>> queue;
    private final long timeout;
    private volatile boolean isRun = true;

    public EventSaver(BlockingQueue<Future<Processor>> queue, long timeout) {
        this.queue = queue;
        this.timeout = timeout;
    }

    public void stop(Thread runningThread) {
        isRun = false;
        runningThread.interrupt();
    }

    @Override
    public void run() {
        while (isRunning()) {
            try {
                try {
                    Future<Processor> future = queue.peek();
                    if (future != null) {
                        Processor processor = future.get();
                        processor.execute();
                        queue.take();
                    } else {
                        TimeUnit.MILLISECONDS.sleep(timeout);
                    }
                } catch (ExecutionException | StorageException ex) {
                    if (ex instanceof ExecutionException) {
                        log.error("The handler threw an error, event processing cannot continue", ex);
                    } else {
                        log.warn("Failed to save event after handling, retrying (timeout = {})...", timeout, ex);
                    }
                    TimeUnit.MILLISECONDS.sleep(timeout);
                } catch (NotFoundException ex) {
                    queue.take();
                    log.warn("Failed to save event after handling, skipped...", ex);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private boolean isRunning() {
        return isRun && !Thread.currentThread().isInterrupted();
    }
}
