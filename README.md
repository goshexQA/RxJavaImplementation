# RxJava Implementation

Собственная реализация базовых компонентов RxJava.

## Структура проекта

- x.core - базовые компоненты (Observer, Observable, Disposable)
- x.schedulers - планировщики потоков
- x.operators - операторы преобразования
- x.subscription - управление подписками

## Возможности

- Базовые реактивные компоненты
- Операторы map, filter, flatMap
- Планировщики (IO, Computation, Single)
- Управление подписками
- Обработка ошибок

## Пример использования

`java
Observable.range(1, 10)
    .filter(x -> x % 2 == 0)
    .map(x -> x * x)
    .subscribeOn(new IOScheduler())
    .observeOn(new SingleThreadScheduler())
    .subscribe(
        System.out::println,
        Throwable::printStackTrace,
        () -> System.out.println("Complete!")
    );
Сборка проекта
Gradle
bash
gradle build
Maven
bash
mvn clean compile
