package rx.operators;

import rx.core.Observable;
import rx.core.Observer;
import java.util.function.Predicate;

public class FilterObservable<T> extends Observable<T> {
    private final Observable<T> source;
    private final Predicate<? super T> predicate;
    
    public FilterObservable(Observable<T> source, Predicate<? super T> predicate) {
        this.source = source;
        this.predicate = predicate;
    }
    
    @Override
    protected void subscribeActual(Observer<? super T> observer) {
        source.subscribe(new Observer<T>() {
            @Override
            public void onNext(T item) {
                try {
                    if (predicate.test(item)) {
                        observer.onNext(item);
                    }
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
                observer.onComplete();
            }
        });
    }
}
