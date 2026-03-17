package rx.core;

public interface Emitter<T> {
    void onNext(T item);
    void onError(Throwable throwable);
    void onComplete();
}
