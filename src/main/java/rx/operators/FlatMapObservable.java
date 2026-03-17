package rx.operators;

import rx.core.Observable;
import rx.core.Observer;
import rx.core.Disposable;
import rx.subscription.CompositeDisposable;
import java.util.function.Function;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;

public class FlatMapObservable<T, R> extends Observable<R> {
    private final Observable<T> source;
    private final Function<? super T, ? extends Observable<? extends R>> mapper;
    
    public FlatMapObservable(Observable<T> source, 
                            Function<? super T, ? extends Observable<? extends R>> mapper) {
        this.source = source;
        this.mapper = mapper;
    }
    
    @Override
    protected void subscribeActual(Observer<? super R> observer) {
        CompositeDisposable disposables = new CompositeDisposable();
        AtomicInteger active = new AtomicInteger(1);
        AtomicBoolean completed = new AtomicBoolean(false);
        
        Observer<T> mainObserver = new Observer<T>() {
            @Override
            public void onNext(T item) {
                active.incrementAndGet();
                
                try {
                    Observable<? extends R> inner = mapper.apply(item);
                    
                    Disposable innerDisposable = inner.subscribe(new Observer<R>() {
                        @Override
                        public void onNext(R innerItem) {
                            observer.onNext(innerItem);
                        }
                        
                        @Override
                        public void onError(Throwable throwable) {
                            observer.onError(throwable);
                        }
                        
                        @Override
                        public void onComplete() {
                            if (active.decrementAndGet() == 0 && completed.get()) {
                                observer.onComplete();
                            }
                        }
                    });
                    
                    disposables.add(innerDisposable);
                    
                } catch (Exception e) {
                    observer.onError(e);
                }
            }
            
            @Override
            public void onError(Throwable throwable) {
                observer.onError(throwable);
            }
            
            @Override
            public void onComplete() {
                completed.set(true);
                if (active.decrementAndGet() == 0) {
                    observer.onComplete();
                }
            }
        };
        
        disposables.add(source.subscribe(mainObserver));
    }
}
