package rx.schedulers;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class IOScheduler implements Scheduler {
    private final ExecutorService executor;
    
    public IOScheduler() {
        ThreadFactory threadFactory = new ThreadFactory() {
            private final AtomicInteger counter = new AtomicInteger(1);
            
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "rx-io-" + counter.getAndIncrement());
                thread.setDaemon(true);
                return thread;
            }
        };
        this.executor = Executors.newCachedThreadPool(threadFactory);
    }
    
    @Override
    public void execute(Runnable task) {
        executor.execute(task);
    }
    
    @Override
    public Worker createWorker() {
        return new IoWorker(executor);
    }
    
    private static class IoWorker implements Worker {
        private final ExecutorService executor;
        private volatile boolean disposed = false;
        
        IoWorker(ExecutorService executor) {
            this.executor = executor;
        }
        
        @Override
        public void schedule(Runnable task) {
            if (!disposed) {
                executor.execute(task);
            }
        }
        
        @Override
        public void dispose() {
            disposed = true;
        }
    }
}
