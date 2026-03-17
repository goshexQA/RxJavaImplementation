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
        Observable<Integer> observable = Observable.create(emitter -> {
            emitter.onNext(1);
            emitter.onNext(2);
            emitter.onNext(3);
            emitter.onComplete();
        });
        
        AtomicInteger counter = new AtomicInteger(0);
        AtomicBoolean completed = new AtomicBoolean(false);
        
        observable.subscribe(new Observer<Integer>() {
            @Override
            public void onNext(Integer item) {
                counter.incrementAndGet();
            }
            
            @Override
            public void onError(Throwable throwable) {
                fail("Should not error");
            }
            
            @Override
            public void onComplete() {
                completed.set(true);
            }
        });
        
        assertEquals(3, counter.get());
        assertTrue(completed.get());
    }
    
    @Test
    @DisplayName("Test map operator")
    public void testMapOperator() {
        Observable<Integer> observable = Observable.just(1, 2, 3, 4, 5);
        AtomicInteger result = new AtomicInteger(0);
        
        observable
            .map(x -> x * 2)
            .subscribe(new Observer<Integer>() {
                @Override
                public void onNext(Integer item) {
                    result.addAndGet(item);
                }
                
                @Override
                public void onError(Throwable throwable) {
                    fail("Should not error");
                }
                
                @Override
                public void onComplete() {}
            });
        
        assertEquals(30, result.get());
    }
    
    @Test
    @DisplayName("Test filter operator")
    public void testFilterOperator() {
        Observable<Integer> observable = Observable.range(1, 10);
        AtomicInteger counter = new AtomicInteger(0);
        
        observable
            .filter(x -> x % 2 == 0)
            .subscribe(new Observer<Integer>() {
                @Override
                public void onNext(Integer item) {
                    counter.incrementAndGet();
                }
                
                @Override
                public void onError(Throwable throwable) {
                    fail("Should not error");
                }
                
                @Override
                public void onComplete() {}
            });
        
        assertEquals(5, counter.get());
    }
    
    @Test
    @DisplayName("Test flatMap operator")
    public void testFlatMapOperator() {
        Observable<Integer> observable = Observable.just(1, 2, 3);
        AtomicInteger counter = new AtomicInteger(0);
        
        observable
            .flatMap(x -> Observable.range(1, x))
            .subscribe(new Observer<Integer>() {
                @Override
                public void onNext(Integer item) {
                    counter.incrementAndGet();
                }
                
                @Override
                public void onError(Throwable throwable) {
                    fail("Should not error");
                }
                
                @Override
                public void onComplete() {}
            });
        
        assertEquals(6, counter.get());
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
            .subscribe(new Observer<Integer>() {
                @Override
                public void onNext(Integer item) {
                    sum.addAndGet(item);
                }
                
                @Override
                public void onError(Throwable throwable) {
                    fail("Should not error");
                }
                
                @Override
                public void onComplete() {}
            });
        
        assertEquals(96, sum.get());
    }
    
    @Test
    @DisplayName("Test subscribeOn")
    public void testSubscribeOn() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> threadName = new AtomicReference<>();
        
        Observable.just(1)
            .subscribeOn(new IOScheduler())
            .subscribe(new Observer<Integer>() {
                @Override
                public void onNext(Integer item) {}
                
                @Override
                public void onError(Throwable throwable) {}
                
                @Override
                public void onComplete() {
                    threadName.set(Thread.currentThread().getName());
                    latch.countDown();
                }
            });
        
        latch.await(1, TimeUnit.SECONDS);
        assertTrue(threadName.get().startsWith("rx-io"));
    }
    
    @Test
    @DisplayName("Test observeOn")
    public void testObserveOn() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> observeThread = new AtomicReference<>();
        
        Observable.just(1)
            .subscribeOn(new IOScheduler())
            .observeOn(new ComputationScheduler())
            .subscribe(new Observer<Integer>() {
                @Override
                public void onNext(Integer item) {
                    observeThread.set(Thread.currentThread().getName());
                }
                
                @Override
                public void onError(Throwable throwable) {}
                
                @Override
                public void onComplete() {
                    latch.countDown();
                }
            });
        
        latch.await(1, TimeUnit.SECONDS);
        assertTrue(observeThread.get().startsWith("rx-computation"));
    }
    
    @Test
    @DisplayName("Test error handling")
    public void testErrorHandling() {
        Observable<Integer> observable = Observable.create(emitter -> {
            emitter.onNext(1);
            emitter.onNext(2);
            emitter.onError(new RuntimeException("Test error"));
            emitter.onNext(3);
        });
        
        AtomicInteger counter = new AtomicInteger(0);
        AtomicReference<Throwable> errorRef = new AtomicReference<>();
        AtomicBoolean completed = new AtomicBoolean(false);
        
        observable
            .map(x -> x * 2)
            .subscribe(new Observer<Integer>() {
                @Override
                public void onNext(Integer item) {
                    counter.incrementAndGet();
                }
                
                @Override
                public void onError(Throwable throwable) {
                    errorRef.set(throwable);
                }
                
                @Override
                public void onComplete() {
                    completed.set(true);
                }
            });
        
        assertEquals(2, counter.get());
        assertNotNull(errorRef.get());
        assertEquals("Test error", errorRef.get().getMessage());
        assertFalse(completed.get());
    }
    
    @Test
    @DisplayName("Test CompositeDisposable")
    public void testCompositeDisposable() {
        CompositeDisposable composite = new CompositeDisposable();
        AtomicInteger counter1 = new AtomicInteger(0);
        AtomicInteger counter2 = new AtomicInteger(0);
        
        Disposable d1 = new Disposable() {
            @Override
            public void dispose() {
                counter1.incrementAndGet();
            }
            
            @Override
            public boolean isDisposed() {
                return false;
            }
        };
        
        Disposable d2 = new Disposable() {
            @Override
            public void dispose() {
                counter2.incrementAndGet();
            }
            
            @Override
            public boolean isDisposed() {
                return false;
            }
        };
        
        composite.add(d1);
        composite.add(d2);
        composite.dispose();
        
        assertEquals(1, counter1.get());
        assertEquals(1, counter2.get());
        assertTrue(composite.isDisposed());
    }
    
    @Test
    @DisplayName("Test empty Observable")
    public void testEmptyObservable() {
        Observable<Integer> observable = Observable.create(emitter -> {
            emitter.onComplete();
        });
        
        AtomicInteger counter = new AtomicInteger(0);
        AtomicBoolean completed = new AtomicBoolean(false);
        
        observable.subscribe(new Observer<Integer>() {
            @Override
            public void onNext(Integer item) {
                counter.incrementAndGet();
            }
            
            @Override
            public void onError(Throwable throwable) {
                fail("Should not error");
            }
            
            @Override
            public void onComplete() {
                completed.set(true);
            }
        });
        
        assertEquals(0, counter.get());
        assertTrue(completed.get());
    }
}
