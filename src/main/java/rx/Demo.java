package rx;

import rx.core.*;
import rx.schedulers.*;

public class Demo {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== RxJava Implementation Demo ===\n");
        
        // Пример 1: Базовый Observable
        System.out.println("1. Базовый Observable:");
        Observable<Integer> observable1 = Observable.create(emitter -> {
            for (int i = 1; i <= 5; i++) {
                emitter.onNext(i);
            }
            emitter.onComplete();
        });
        
        observable1.subscribe(
            item -> System.out.println("Получено: " + item),
            error -> System.err.println("Ошибка: " + error),
            () -> System.out.println("Завершено!\n")
        );
        
        // Пример 2: Использование операторов map и filter
        System.out.println("2. Операторы map и filter:");
        Observable.range(1, 10)
            .filter(x -> x % 2 == 0)
            .map(x -> x * x)
            .subscribe(
                item -> System.out.println("Квадрат четного: " + item),
                error -> System.err.println("Ошибка: " + error),
                () -> System.out.println("Завершено!\n")
            );
        
        // Пример 3: Многопоточность с Schedulers
        System.out.println("3. Многопоточность:");
        CountDownLatch latch = new CountDownLatch(1);
        
        Observable.range(1, 3)
            .subscribeOn(new IOScheduler())
            .map(x -> {
                System.out.println("Обработка " + x + " в потоке: " + 
                    Thread.currentThread().getName());
                return x * 10;
            })
            .observeOn(new SingleThreadScheduler())
            .subscribe(
                item -> System.out.println("Получен результат: " + item + 
                    " в потоке: " + Thread.currentThread().getName()),
                error -> System.err.println("Ошибка: " + error),
                () -> {
                    System.out.println("Все операции завершены!");
                    latch.countDown();
                }
            );
        
        latch.await(2, TimeUnit.SECONDS);
        
        // Пример 4: FlatMap
        System.out.println("\n4. FlatMap пример:");
        Observable.range(1, 3)
            .flatMap(x -> Observable.range(1, x))
            .subscribe(
                item -> System.out.print(item + " "),
                error -> System.err.println("Ошибка: " + error),
                () -> System.out.println("\nFlatMap завершен!")
            );
    }
}
