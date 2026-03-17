package rx;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import rx.core.*;
import rx.schedulers.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public class ObservableTest {
    
    @Test
    @DisplayName("Test basic Observable creation")
    public void testBasicObservable() {
        Observable<Integer> observable = Observable.create(emitter -> {
            emitter.onNext(1);
            emitter.onNext(2);
            emitter.onNext(3);
            emitter.onComplete();
        });
        
        AtomicInteger counter = new AtomicInteger(0);
        AtomicBoolean completed = new AtomicBoolean(false);
        
        observable.subscribe(
            item -> counter.incrementAndGet(),
            error -> fail("Should not error: " + error),
            () -> completed.set(true)
        );
        
        assertEquals(3, counter.get());
        assertTrue(completed.get());
    }
    
    @Test
    @DisplayName("Test map operator")
    public void testMapOperator() {
        Observable<Integer> observable = Observable.range(1, 3);
        AtomicInteger result = new AtomicInteger(0);
        
        observable
            .map(x -> x * 2)
            .subscribe(
                item -> result.addAndGet(item),
                error -> fail("Should not error: " + error),
                () -> {}
            );
        
        assertEquals(12, result.get()); // 2 + 4 + 6 = 12
    }
    
    @Test
    @DisplayName("Test filter operator")
    public void testFilterOperator() {
        Observable<Integer> observable = Observable.range(1, 5);
        AtomicInteger counter = new AtomicInteger(0);
        
        observable
            .filter(x -> x % 2 == 0)
            .subscribe(
                item -> counter.incrementAndGet(),
                error -> fail("Should not error: " + error),
                () -> {}
            );
        
        assertEquals(2, counter.get()); // 2 and 4
    }
    
    @Test
    @DisplayName("Test flatMap operator")
    public void testFlatMapOperator() {
        Observable<Integer> observable = Observable.just(1, 2, 3);
        AtomicInteger counter = new AtomicInteger(0);
        
        observable
            .flatMap(x -> Observable.range(1, x))
            .subscribe(
                item -> counter.incrementAndGet(),
                error -> fail("Should not error: " + error),
                () -> {}
            );
        
        assertEquals(6, counter.get()); // 1 + 2 + 3 = 6 items
    }
    
    @Test
    @DisplayName("Test subscribeOn scheduler")
    public void testSubscribeOn() throws InterruptedException {
        Observable<Integer> observable = Observable.range(1, 1);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> threadName = new AtomicReference<>();
        
        observable
            .subscribeOn(new IOScheduler())
            .subscribe(
                item -> {},
                error -> fail("Should not error: " + error),
                () -> {
                    threadName.set(Thread.currentThread().getName());
                    latch.countDown();
                }
            );
        
        latch.await(1, TimeUnit.SECONDS);
        assertTrue(threadName.get().startsWith("rx-io"), 
            "Thread should be from IO scheduler: " + threadName.get());
    }
    
    @Test
    @DisplayName("Test observeOn scheduler")
    public void testObserveOn() throws InterruptedException {
        Observable<Integer> observable = Observable.just(1);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> threadName = new AtomicReference<>();
        
        observable
            .subscribeOn(new IOScheduler())
            .observeOn(new ComputationScheduler())
            .subscribe(
                item -> {
                    threadName.set(Thread.currentThread().getName());
                    latch.countDown();
                },
                error -> fail("Should not error: " + error),
                () -> {}
            );
        
        latch.await(1, TimeUnit.SECONDS);
        assertTrue(threadName.get().startsWith("rx-computation"), 
            "Thread should be from Computation scheduler: " + threadName.get());
    }
    
    @Test
    @DisplayName("Test error handling")
    public void testErrorHandling() {
        Observable<Integer> observable = Observable.create(emitter -> {
            emitter.onNext(1);
            emitter.onError(new RuntimeException("Test error"));
        });
        
        AtomicInteger counter = new AtomicInteger(0);
        AtomicReference<Throwable> errorRef = new AtomicReference<>();
        AtomicBoolean completed = new AtomicBoolean(false);
        
        observable
            .map(x -> x * 2)
            .subscribe(
                item -> counter.incrementAndGet(),
                error -> errorRef.set(error),
                () -> completed.set(true)
            );
        
        assertEquals(1, counter.get());
        assertNotNull(errorRef.get());
        assertEquals("Test error", errorRef.get().getMessage());
        assertFalse(completed.get());
    }
    
    @Test
    @DisplayName("Test operator chain")
    public void testOperatorChain() {
        Observable<Integer> observable = Observable.range(1, 20);
        AtomicInteger sum = new AtomicInteger(0);
        
        observable
            .filter(x -> x > 10)
            .map(x -> x * 2)
            .filter(x -> x % 4 == 0)
            .subscribe(
                item -> sum.addAndGet(item),
                error -> fail("Should not error: " + error),
                () -> {}
            );
        
        assertEquals(160, sum.get(), "Sum should be 160"); // 24+28+32+36+40 = 160
    }
    
    @Test
    @DisplayName("Test empty Observable")
    public void testEmptyObservable() {
        Observable<Integer> observable = Observable.create(emitter -> {
            emitter.onComplete();
        });
        
        AtomicInteger counter = new AtomicInteger(0);
        AtomicBoolean completed = new AtomicBoolean(false);
        
        observable.subscribe(
            item -> counter.incrementAndGet(),
            error -> fail("Should not error: " + error),
            () -> completed.set(true)
        );
        
        assertEquals(0, counter.get());
        assertTrue(completed.get());
    }
    
    @Test
    @DisplayName("Test just operator")
    public void testJustOperator() {
        Observable<String> observable = Observable.just("a", "b", "c");
        AtomicInteger counter = new AtomicInteger(0);
        StringBuilder result = new StringBuilder();
        
        observable.subscribe(
            item -> {
                counter.incrementAndGet();
                result.append(item);
            },
            error -> fail("Should not error: " + error),
            () -> {}
        );
        
        assertEquals(3, counter.get());
        assertEquals("abc", result.toString());
    }
}
