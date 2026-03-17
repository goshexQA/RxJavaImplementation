package rx;

import org.junit.jupiter.api.*;
import rx.core.*;
import rx.schedulers.*;
import rx.subscription.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import static org.junit.jupiter.api.Assertions.*;

public class RxJavaTest {
    
    @Test
    @DisplayName("Test Observable creation and subscription")
    public void testObservableCreation() {
        // Given
        Observable<Integer> observable = Observable.create(emitter -> {
            emitter.onNext(1);
            emitter.onNext(2);
            emitter.onNext(3);
            emitter.onComplete();
        });
        
        AtomicInteger counter = new AtomicInteger(0);
        AtomicBoolean completed = new AtomicBoolean(false);
        
        // When
        observable.subscribe(
            item -> counter.incrementAndGet(),
            error -> fail("Should not error"),
            () -> completed.set(true)
        );
        
        // Then
        assertEquals(3, counter.get());
        assertTrue(completed.get());
    }
    
    @Test
    @DisplayName("Test map operator")
    public void testMapOperator() {
        // Given
        Observable<Integer> observable = Observable.just(1, 2, 3, 4, 5);
        AtomicInteger result = new AtomicInteger(0);
        
        // When
        observable
            .map(x -> x * 2)
            .subscribe(item -> result.addAndGet(item));
        
        // Then (2+4+6+8+10 = 30)
        assertEquals(30, result.get());
    }
    
    @Test
    @DisplayName("Test filter operator")
    public void testFilterOperator() {
        // Given
        Observable<Integer> observable = Observable.range(1, 10);
        AtomicInteger counter = new AtomicInteger(0);
        
        // When
        observable
            .filter(x -> x % 2 == 0)
            .subscribe(item -> counter.incrementAndGet());
        
        // Then (2,4,6,8,10 - 5 numbers)
        assertEquals(5, counter.get());
    }
    
    @Test
    @DisplayName("Test flatMap operator")
    public void testFlatMapOperator() {
        // Given
        Observable<Integer> observable = Observable.just(1, 2, 3);
        AtomicInteger counter = new AtomicInteger(0);
        
        // When
        observable
            .flatMap(x -> Observable.range(1, x))
            .subscribe(item -> counter.incrementAndGet());
        
        // Then (1, 1,2, 1,2,3 = 6 items)
        assertEquals(6, counter.get());
    }
    
    @Test
    @DisplayName("Test chain of operators")
    public void testOperatorChain() {
        // Given
        Observable<Integer> observable = Observable.range(1, 20);
        AtomicInteger sum = new AtomicInteger(0);
        
        // When
        observable
            .filter(x -> x > 10)
            .map(x -> x * 2)
            .filter(x -> x % 4 == 0)
            .subscribe(item -> sum.addAndGet(item));
        
        // Then (12,14,16,18,20)*2 filtered by %4: 12*2=24, 16*2=32, 20*2=40 -> sum=96
        assertEquals(96, sum.get());
    }
    
    @Test
    @DisplayName("Test subscribeOn with different schedulers")
    public void testSubscribeOn() throws InterruptedException {
        // Given
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> threadName = new AtomicReference<>();
        
        // When
        Observable.just(1)
            .subscribeOn(new IOScheduler())
            .subscribe(
                item -> {},
                error -> {},
                () -> {
                    threadName.set(Thread.currentThread().getName());
                    latch.countDown();
                }
            );
        
        // Then
        latch.await(1, TimeUnit.SECONDS);
        assertTrue(threadName.get().startsWith("rx-io"));
    }
    
    @Test
    @DisplayName("Test observeOn switches threads")
    public void testObserveOn() throws InterruptedException {
        // Given
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> observeThread = new AtomicReference<>();
        
        // When
        Observable.just(1)
            .subscribeOn(new IOScheduler())
            .observeOn(new ComputationScheduler())
            .subscribe(
                item -> observeThread.set(Thread.currentThread().getName()),
                error -> {},
                () -> latch.countDown()
            );
        
        // Then
        latch.await(1, TimeUnit.SECONDS);
        assertTrue(observeThread.get().startsWith("rx-computation"));
    }
    
    @Test
    @DisplayName("Test error handling")
    public void testErrorHandling() {
        // Given
        Observable<Integer> observable = Observable.create(emitter -> {
            emitter.onNext(1);
            emitter.onNext(2);
            emitter.onError(new RuntimeException("Test error"));
            emitter.onNext(3);
        });
        
        AtomicInteger counter = new AtomicInteger(0);
        AtomicReference<Throwable> errorRef = new AtomicReference<>();
        AtomicBoolean completed = new AtomicBoolean(false);
        
        // When
        observable
            .map(x -> x * 2)
            .subscribe(
                item -> counter.incrementAndGet(),
                error -> errorRef.set(error),
                () -> completed.set(true)
            );
        
        // Then
        assertEquals(2, counter.get());
        assertNotNull(errorRef.get());
        assertEquals("Test error", errorRef.get().getMessage());
        assertFalse(completed.get());
    }
    
    @Test
    @DisplayName("Test Disposable - unsubscribe")
    public void testDisposable() {
        // Given
        Observable<Integer> observable = Observable.range(1, 100);
        AtomicInteger counter = new AtomicInteger(0);
        
        // When
        Disposable disposable = observable.subscribe(
            item -> {
                counter.incrementAndGet();
                if (counter.get() >= 5) {
                    // Simulate unsubscribe
                    throw new RuntimeException("Stop");
                }
            },
            error -> {},
            () -> {}
        );
        
        // Then
        assertTrue(counter.get() >= 5 || disposable.isDisposed());
    }
    
    @Test
    @DisplayName("Test CompositeDisposable")
    public void testCompositeDisposable() {
        // Given
        CompositeDisposable composite = new CompositeDisposable();
        AtomicInteger counter1 = new AtomicInteger(0);
        AtomicInteger counter2 = new AtomicInteger(0);
        
        Disposable d1 = () -> counter1.incrementAndGet();
        Disposable d2 = () -> counter2.incrementAndGet();
        
        // When
        composite.add(d1);
        composite.add(d2);
        composite.dispose();
        
        // Then
        assertEquals(1, counter1.get());
        assertEquals(1, counter2.get());
        assertTrue(composite.isDisposed());
    }
    
    @Test
    @DisplayName("Test empty Observable")
    public void testEmptyObservable() {
        // Given
        Observable<Integer> observable = Observable.create(emitter -> {
            emitter.onComplete();
        });
        
        AtomicInteger counter = new AtomicInteger(0);
        AtomicBoolean completed = new AtomicBoolean(false);
        
        // When
        observable.subscribe(
            item -> counter.incrementAndGet(),
            error -> fail("Should not error"),
            () -> completed.set(true)
        );
        
        // Then
        assertEquals(0, counter.get());
        assertTrue(completed.get());
    }
    
    @Test
    @DisplayName("Test never Observable")
    public void testNeverObservable() throws InterruptedException {
        // Given
        Observable<Integer> observable = Observable.create(emitter -> {
            // Do nothing
        });
        
        AtomicBoolean anyEvent = new AtomicBoolean(false);
        
        // When
        Disposable disposable = observable.subscribe(
            item -> anyEvent.set(true),
            error -> anyEvent.set(true),
            () -> anyEvent.set(true)
        );
        
        // Then
        Thread.sleep(100);
        assertFalse(anyEvent.get());
        assertFalse(disposable.isDisposed());
    }
    
    @Test
    @DisplayName("Test scheduler performance comparison")
    public void testSchedulerPerformance() throws InterruptedException {
        int taskCount = 100;
        CountDownLatch ioLatch = new CountDownLatch(taskCount);
        CountDownLatch compLatch = new CountDownLatch(taskCount);
        CountDownLatch singleLatch = new CountDownLatch(taskCount);
        
        Scheduler io = new IOScheduler();
        Scheduler comp = new ComputationScheduler();
        Scheduler single = new SingleThreadScheduler();
        
        long startTime = System.currentTimeMillis();
        
        // IO tasks
        for (int i = 0; i < taskCount; i++) {
            io.execute(() -> {
                try { Thread.sleep(5); } catch (InterruptedException e) {}
                ioLatch.countDown();
            });
        }
        
        // CPU tasks
        for (int i = 0; i < taskCount; i++) {
            comp.execute(() -> {
                double result = 0;
                for (int j = 0; j < 1000; j++) {
                    result += Math.sin(j) * Math.cos(j);
                }
                compLatch.countDown();
            });
        }
        
        // Single thread tasks
        for (int i = 0; i < taskCount; i++) {
            single.execute(() -> {
                try { Thread.sleep(5); } catch (InterruptedException e) {}
                singleLatch.countDown();
            });
        }
        
        assertTrue(ioLatch.await(5, TimeUnit.SECONDS));
        assertTrue(compLatch.await(5, TimeUnit.SECONDS));
        assertTrue(singleLatch.await(5, TimeUnit.SECONDS));
        
        long totalTime = System.currentTimeMillis() - startTime;
        System.out.println("Performance test completed in: " + totalTime + "ms");
    }
}
