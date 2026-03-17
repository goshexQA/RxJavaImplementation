package rx.subscription;

import rx.core.Disposable;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class CompositeDisposable implements Disposable {
    private final Set<Disposable> disposables = ConcurrentHashMap.newKeySet();
    private volatile boolean disposed = false;
    
    public void add(Disposable disposable) {
        if (disposed) {
            disposable.dispose();
        } else {
            disposables.add(disposable);
        }
    }
    
    @Override
    public void dispose() {
        disposed = true;
        disposables.forEach(Disposable::dispose);
        disposables.clear();
    }
    
    @Override
    public boolean isDisposed() {
        return disposed;
    }
}
