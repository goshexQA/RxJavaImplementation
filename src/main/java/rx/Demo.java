package rx;

import rx.core.*;
import rx.schedulers.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class Demo {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== RxJava Implementation Demo ===\n");
        
        // Simple example
        System.out.println("Simple Observable example:");
        Observable.just(1, 2, 3, 4, 5)
            .map(x -> x * 2)
            .filter(x -> x > 5)
            .subscribe(
                item -> System.out.println("Got: " + item),
                error -> System.err.println("Error: " + error),
                () -> System.out.println("Complete!\n")
            );
        
        // Multithreading example
        System.out.println("Multithreading example:");
        CountDownLatch latch = new CountDownLatch(1);
        
        Observable.range(1, 3)
            .subscribeOn(new IOScheduler())
            .map(x -> {
                System.out.println("Processing " + x + " in: " + Thread.currentThread().getName());
                return x * 10;
            })
            .observeOn(new SingleThreadScheduler())
            .subscribe(
                item -> System.out.println("Result " + item + " in: " + Thread.currentThread().getName()),
                error -> System.err.println("Error: " + error),
                () -> {
                    System.out.println("All done!");
                    latch.countDown();
                }
            );
        
        latch.await(2, TimeUnit.SECONDS);
    }
}
