package ru.siksmfp.basic.language.labmdas.warburton.chapters.first;

/**
 * Created by Artyom Karnov on 27.11.16.
 * artyom-karnov@yandex.ru
 **/
@FunctionalInterface
interface WorkerInterface {
    public void doSomeWork();
}

// TODO: 27.11.16 http://viralpatel.net/blogs/lambda-expressions-java-tutorial/ 
public class FirstCustomFunctionalInterface {

    public static void execute(WorkerInterface worker) {
        worker.doSomeWork();
    }

    public static void main(String[] args) {

        //invoke doSomeWork using Annonymous class
        execute(new WorkerInterface() {
            @Override
            public void doSomeWork() {
                System.out.println("Worker invoked using Anonymous class");
            }
        });

        //invoke doSomeWork using Lambda expression
        execute(() -> System.out.println("Worker invoked using Lambda expression"));
    }

}