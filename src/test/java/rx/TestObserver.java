import rx.core.*;
import java.util.function.Consumer;

public class TestObserver<T> implements Observer<T> {
    private final Consumer<? super T> onNextConsumer;
    private final Consumer<? super Throwable> onErrorConsumer;
    private final Runnable onCompleteRunnable;
    
    public TestObserver(Consumer<? super T> onNext) {
        this(onNext, error -> {}, () -> {});
    }
    
    public TestObserver(Consumer<? super T> onNext, 
                        Consumer<? super Throwable> onError, 
                        Runnable onComplete) {
        this.onNextConsumer = onNext;
        this.onErrorConsumer = onError;
        this.onCompleteRunnable = onComplete;
    }
    
    @Override
    public void onNext(T item) {
        onNextConsumer.accept(item);
    }
    
    @Override
    public void onError(Throwable throwable) {
        onErrorConsumer.accept(throwable);
    }
    
    @Override
    public void onComplete() {
        onCompleteRunnable.run();
    }
}
