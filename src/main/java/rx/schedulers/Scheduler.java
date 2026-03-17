package rx.schedulers;

public interface Scheduler {
    void execute(Runnable task);
    Worker createWorker();
    
    interface Worker {
        void schedule(Runnable task);
        void dispose();
    }
}
