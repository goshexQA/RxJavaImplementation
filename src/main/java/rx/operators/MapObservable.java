package rx.operators;

import rx.core.Observable;
import rx.core.Observer;
import java.util.function.Function;

public class MapObservable<T, R> extends Observable<R> {
    private final Observable<T> source;
    private final Function<? super T, ? extends R> mapper;
    
    public MapObservable(Observable<T> source, Function<? super T, ? extends R> mapper) {
        this.source = source;
        this.mapper = mapper;
    }
    
    @Override
    protected void subscribeActual(Observer<? super R> observer) {
        source.subscribe(new Observer<T>() {
            @Override
            public void onNext(T item) {
                try {
                    observer.onNext(mapper.apply(item));
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
