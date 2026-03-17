package rx.core;

public interface Disposable {
    void dispose();
    boolean isDisposed();
}
