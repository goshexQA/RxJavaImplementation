package rx.core;

import rx.schedulers.Scheduler;
import rx.operators.MapObservable;
import rx.operators.FilterObservable;
import rx.operators.FlatMapObservable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class Observable<T> {
    
    public static <T> Observable<T> create(Consumer<Emitter<T>> emitterConsumer) {
        return new Observable<T>() {
            @Override
            protected void subscribeActual(Observer<? super T> observer) {
                Emitter<T> emitter = new Emitter<T>() {
                    private volatile boolean disposed = false;
                    
                    @Override
                    public void onNext(T item) {
                        if (!disposed) {
                            observer.onNext(item);
                        }
                    }
                    
                    @Override
                    public void onError(Throwable throwable) {
                        if (!disposed) {
                            observer.onError(throwable);
                        }
                    }
                    
                    @Override
                    public void onComplete() {
                        if (!disposed) {
                            observer.onComplete();
                        }
                    }
                };
                
                try {
                    emitterConsumer.accept(emitter);
                } catch (Exception e) {
                    observer.onError(e);
                }
            }
        };
    }
    
    public static Observable<Integer> range(int start, int count) {
        return create(emitter -> {
            for (int i = start; i < start + count; i++) {
                emitter.onNext(i);
            }
            emitter.onComplete();
        });
    }
    
    public Disposable subscribe(Observer<? super T> observer) {
        subscribeActual(observer);
        return new Disposable() {
            private volatile boolean disposed = false;
            
            @Override
            public void dispose() {
                disposed = true;
            }
            
            @Override
            public boolean isDisposed() {
                return disposed;
            }
        };
    }
    
    public Disposable subscribe(Consumer<? super T> onNext, 
                               Consumer<? super Throwable> onError, 
                               Runnable onComplete) {
        Observer<T> observer = new Observer<T>() {
            @Override
            public void onNext(T item) {
                onNext.accept(item);
            }
            
            @Override
            public void onError(Throwable throwable) {
                onError.accept(throwable);
            }
            
            @Override
            public void onComplete() {
                onComplete.run();
            }
        };
        return subscribe(observer);
    }
    
    protected abstract void subscribeActual(Observer<? super T> observer);
    
    public <R> Observable<R> map(Function<? super T, ? extends R> mapper) {
        return new MapObservable<>(this, mapper);
    }
    
    public Observable<T> filter(Predicate<? super T> predicate) {
        return new FilterObservable<>(this, predicate);
    }
    
    public <R> Observable<R> flatMap(Function<? super T, ? extends Observable<? extends R>> mapper) {
        return new FlatMapObservable<>(this, mapper);
    }
    
    public Observable<T> subscribeOn(Scheduler scheduler) {
        return new Observable<T>() {
            @Override
            protected void subscribeActual(Observer<? super T> observer) {
                scheduler.execute(() -> Observable.this.subscribeActual(observer));
            }
        };
    }
    
    public Observable<T> observeOn(Scheduler scheduler) {
        return new Observable<T>() {
            @Override
            protected void subscribeActual(Observer<? super T> observer) {
                Observable.this.subscribeActual(new Observer<T>() {
                    @Override
                    public void onNext(T item) {
                        scheduler.execute(() -> observer.onNext(item));
                    }
                    
                    @Override
                    public void onError(Throwable throwable) {
                        scheduler.execute(() -> observer.onError(throwable));
                    }
                    
                    @Override
                    public void onComplete() {
                        scheduler.execute(observer::onComplete);
                    }
                });
            }
        };
    }
}
