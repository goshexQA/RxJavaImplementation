import rx.core.*;
import rx.schedulers.*;
import rx.subscription.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public class FinalTest {
    private static int passed = 0;
    private static int failed = 0;
    private static int total = 0;
    
    public static void main(String[] args) {
        System.out.println("=================================");
        System.out.println("    RXJAVA FINAL TESTS");
        System.out.println("=================================\n");
        
        testObservableCreation();
        testMapOperator();
        testFilterOperator();
        testFlatMapOperator();
        testOperatorChain();
        testSubscribeOn();
        testObserveOn();
        testErrorHandling();
        testCompositeDisposable();
        testEmptyObservable();
        testJustOperator();
        
        System.out.println("\n=================================");
        System.out.println("           RESULTS");
        System.out.println("=================================");
        System.out.println("Total tests: " + total);
        System.out.println("Passed: " + passed);
        System.out.println("Failed: " + failed);
        System.out.println("Success rate: " + (total > 0 ? (passed * 100 / total) : 0) + "%");
        
        if (failed == 0) {
            System.out.println("\n? ALL TESTS PASSED!");
        } else {
            System.out.println("\n? SOME TESTS FAILED!");
        }
    }
    
    private static void assertEquals(Object expected, Object actual, String testName) {
        total++;
        boolean condition;
        if (expected instanceof Integer && actual instanceof Integer) {
            condition = ((Integer) expected).intValue() == ((Integer) actual).intValue();
        } else if (expected instanceof String && actual instanceof String) {
            condition = expected.equals(actual);
        } else {
            condition = expected == actual;
        }
        
        if (condition) {
            System.out.println("? " + testName + " - PASSED");
            passed++;
        } else {
            System.out.println("? " + testName + " - FAILED (expected: " + expected + ", got: " + actual + ")");
            failed++;
        }
    }
    
    private static void assertTrue(boolean condition, String testName) {
        total++;
        if (condition) {
            System.out.println("? " + testName + " - PASSED");
            passed++;
        } else {
            System.out.println("? " + testName + " - FAILED");
            failed++;
        }
    }
    
    private static void testObservableCreation() {
        Observable<Integer> observable = Observable.create(emitter -> {
            emitter.onNext(1);
            emitter.onNext(2);
            emitter.onNext(3);
            emitter.onComplete();
        });
        
        AtomicInteger counter = new AtomicInteger(0);
        
        observable.subscribe(
            item -> counter.incrementAndGet(),
            error -> {},
            () -> {}
        );
        
        assertEquals(3, counter.get(), "Observable creation");
    }
    
    private static void testMapOperator() {
        Observable<Integer> observable = Observable.just(1, 2, 3, 4, 5);
        AtomicInteger result = new AtomicInteger(0);
        
        observable
            .map(x -> x * 2)
            .subscribe(
                item -> result.addAndGet(item),
                error -> {},
                () -> {}
            );
        
        assertEquals(30, result.get(), "Map operator");
    }
    
    private static void testFilterOperator() {
        Observable<Integer> observable = Observable.range(1, 10);
        AtomicInteger counter = new AtomicInteger(0);
        
        observable
            .filter(x -> x % 2 == 0)
            .subscribe(
                item -> counter.incrementAndGet(),
                error -> {},
                () -> {}
            );
        
        assertEquals(5, counter.get(), "Filter operator");
    }
    
    private static void testFlatMapOperator() {
        Observable<Integer> observable = Observable.just(1, 2, 3);
        AtomicInteger counter = new AtomicInteger(0);
        
        observable
            .flatMap(x -> Observable.range(1, x))
            .subscribe(
                item -> counter.incrementAndGet(),
                error -> {},
                () -> {}
            );
        
        assertEquals(6, counter.get(), "FlatMap operator");
    }
    
    private static void testOperatorChain() {
        // ??????????? ????: ????? ?? 1 ?? 20
        // filter(x > 10) -> 11,12,13,14,15,16,17,18,19,20
        // map(x * 2) -> 22,24,26,28,30,32,34,36,38,40
        // filter(x % 4 == 0) -> 24,28,32,36,40
        // sum = 24+28+32+36+40 = 160 (?????????!)
        
        Observable<Integer> observable = Observable.range(1, 20);
        AtomicInteger sum = new AtomicInteger(0);
        
        observable
            .filter(x -> x > 10)           // ????? ????? ?????? 10
            .map(x -> x * 2)                // ???????? ?? 2
            .filter(x -> x % 4 == 0)        // ????????? ??????? 4
            .subscribe(
                item -> {
                    System.out.println("Chain item: " + item);
                    sum.addAndGet(item);
                },
                error -> {},
                () -> {}
            );
        
        // ????????? ?????????: 24+28+32+36+40 = 160
        assertEquals(160, sum.get(), "Operator chain");
    }
    
    private static void testSubscribeOn() {
        try {
            CountDownLatch latch = new CountDownLatch(1);
            AtomicReference<String> threadName = new AtomicReference<>();
            
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
            
            latch.await(1, TimeUnit.SECONDS);
            assertTrue(threadName.get() != null && threadName.get().startsWith("rx-io"), "SubscribeOn");
        } catch (Exception e) {
            assertTrue(false, "SubscribeOn - exception: " + e.getMessage());
        }
    }
    
    private static void testObserveOn() {
        try {
            CountDownLatch latch = new CountDownLatch(1);
            AtomicReference<String> threadName = new AtomicReference<>();
            
            Observable.just(1)
                .subscribeOn(new IOScheduler())
                .observeOn(new ComputationScheduler())
                .subscribe(
                    item -> {
                        threadName.set(Thread.currentThread().getName());
                        latch.countDown();
                    },
                    error -> {},
                    () -> {}
                );
            
            latch.await(1, TimeUnit.SECONDS);
            assertTrue(threadName.get() != null && threadName.get().startsWith("rx-computation"), "ObserveOn");
        } catch (Exception e) {
            assertTrue(false, "ObserveOn - exception: " + e.getMessage());
        }
    }
    
    private static void testErrorHandling() {
        Observable<Integer> observable = Observable.create(emitter -> {
            emitter.onNext(1);
            emitter.onNext(2);
            emitter.onError(new RuntimeException("Test error"));
        });
        
        AtomicInteger counter = new AtomicInteger(0);
        AtomicReference<Throwable> errorRef = new AtomicReference<>();
        
        observable
            .map(x -> x * 2)
            .subscribe(
                item -> counter.incrementAndGet(),
                error -> errorRef.set(error),
                () -> {}
            );
        
        assertEquals(2, counter.get(), "Error handling - count");
        assertTrue(errorRef.get() != null && 
                  "Test error".equals(errorRef.get().getMessage()), 
                  "Error handling - error message");
    }
    
    private static void testCompositeDisposable() {
        CompositeDisposable composite = new CompositeDisposable();
        AtomicInteger counter = new AtomicInteger(0);
        
        Disposable d = new Disposable() {
            @Override
            public void dispose() {
                counter.incrementAndGet();
            }
            
            @Override
            public boolean isDisposed() {
                return false;
            }
        };
        
        composite.add(d);
        composite.dispose();
        
        assertEquals(1, counter.get(), "CompositeDisposable - dispose called");
        assertTrue(composite.isDisposed(), "CompositeDisposable - isDisposed");
    }
    
    private static void testEmptyObservable() {
        Observable<Integer> observable = Observable.create(emitter -> {
            emitter.onComplete();
        });
        
        AtomicInteger counter = new AtomicInteger(0);
        AtomicBoolean completed = new AtomicBoolean(false);
        
        observable.subscribe(
            item -> counter.incrementAndGet(),
            error -> {},
            () -> completed.set(true)
        );
        
        assertEquals(0, counter.get(), "Empty observable - no items");
        assertTrue(completed.get(), "Empty observable - completed");
    }
    
    private static void testJustOperator() {
        Observable<Integer> observable = Observable.just(1, 2, 3, 4, 5);
        AtomicInteger counter = new AtomicInteger(0);
        
        observable.subscribe(
            item -> counter.incrementAndGet(),
            error -> {},
            () -> {}
        );
        
        assertEquals(5, counter.get(), "Just operator");
    }
}
