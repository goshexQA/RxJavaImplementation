package rx;

import org.junit.jupiter.api.*;
import rx.core.*;
import rx.schedulers.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import static org.junit.jupiter.api.Assertions.*;

public class ObservableTest {
    
    @Test
    public void testBasicObservable() {
        Observable<Integer> observable = Observable.create(emitter -> {
            emitter.onNext(1);
            emitter.onNext(2);
            emitter.onNext(3);
            emitter.onComplete();
        });
        
        AtomicInteger counter = new AtomicInteger(0);
        
        observable.subscribe(
            item -> counter.incrementAndGet(),
            error -> fail("Error: " + error),
            () -> {}
        );
        
        assertEquals(3, counter.get());
    }
    
    @Test
    public void testMapOperator() {
        Observable<Integer> observable = Observable.range(1, 3);
        AtomicInteger result = new AtomicInteger(0);
        
        observable
            .map(x -> x * 2)
            .subscribe(item -> result.addAndGet(item));
        
        assertEquals(12, result.get()); // 2+4+6=12
    }
    
    @Test
    public void testFilterOperator() {
        Observable<Integer> observable = Observable.range(1, 5);
        AtomicInteger counter = new AtomicInteger(0);
        
        observable
            .filter(x -> x % 2 == 0)
            .subscribe(item -> counter.incrementAndGet());
        
        assertEquals(2, counter.get()); // 2 и 4
    }
    
    @Test
    public void testSubscribeOn() throws InterruptedException {
        Observable<Integer> observable = Observable.range(1, 3);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> threadName = new AtomicReference<>();
        
        observable
            .subscribeOn(new IOScheduler())
            .subscribe(
                item -> {},
                error -> {},
                () -> {
                    threadName.set(Thread.currentThread().getName());
                    latch.countDown();
                }
            );
        
        latch.await(1, TimeUnit.SECONDS);
        assertTrue(threadName.get().startsWith("rx-io"));
    }
    
    @Test
    public void testErrorHandling() {
        Observable<Integer> observable = Observable.create(emitter -> {
            emitter.onNext(1);
            emitter.onError(new RuntimeException("Test error"));
        });
        
        AtomicReference<Throwable> errorRef = new AtomicReference<>();
        
        observable
            .map(x -> x * 2)
            .subscribe(
                item -> {},
                error -> errorRef.set(error),
                () -> fail("Should not complete")
            );
        
        assertNotNull(errorRef.get());
        assertEquals("Test error", errorRef.get().getMessage());
    }
}
