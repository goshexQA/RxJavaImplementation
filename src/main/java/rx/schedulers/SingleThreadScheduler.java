package rx.schedulers;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class SingleThreadScheduler implements Scheduler {
    private final ExecutorService executor;
    
    public SingleThreadScheduler() {
        ThreadFactory threadFactory = new ThreadFactory() {
            private final AtomicInteger counter = new AtomicInteger(1);
            
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "rx-single-" + counter.getAndIncrement());
                thread.setDaemon(true);
                return thread;
            }
        };
        this.executor = Executors.newSingleThreadExecutor(threadFactory);
    }
    
    @Override
    public void execute(Runnable task) {
        executor.execute(task);
    }
    
    @Override
    public Worker createWorker() {
        return new SingleWorker(executor);
    }
    
    private static class SingleWorker implements Worker {
        private final ExecutorService executor;
        private volatile boolean disposed = false;
        
        SingleWorker(ExecutorService executor) {
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
