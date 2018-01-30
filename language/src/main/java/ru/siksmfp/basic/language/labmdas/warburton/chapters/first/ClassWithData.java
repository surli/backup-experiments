package ru.siksmfp.basic.language.labmdas.warburton.chapters.first;

/**
 * Created by Artyom Karnov on 26.11.16.
 * artyom-karnov@yandex.ru
 **/
class ClassWithData {
    private int data;

    public ClassWithData(int data) {
        this.data = data;
    }

    public int getData() {
        return data;
    }

    public void sum(ClassWithData secondSummand) {
        data = data + secondSummand.getData();
    }

}