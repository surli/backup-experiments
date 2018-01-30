package ru.siksmfp.basic.language.labmdas.my.data;

import java.util.Collection;

/**
 * @author Artem Karnov @date 17.02.2017.
 * artem.karnov@t-systems.com
 */
public class Citizen {
    private long id;
    private String name;
    private String secondName;
    private String thirdName;
    private Citizen mother;
    private Citizen father;
    private int age;
    private Sex sex;
    private Profession profession;
    private Collection<Work> works;

    public Citizen(String name, String secondName, String thirdName, Citizen mother,
                   Citizen father, int age, Sex sex, Profession profession, Collection<Work> works) {
        this.name = name;
        this.secondName = secondName;
        this.thirdName = thirdName;
        this.mother = mother;
        this.father = father;
        this.age = age;
        this.sex = sex;
        this.profession = profession;
        this.works = works;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSecondName() {
        return secondName;
    }

    public void setSecondName(String secondName) {
        this.secondName = secondName;
    }

    public String getThirdName() {
        return thirdName;
    }

    public void setThirdName(String thirdName) {
        this.thirdName = thirdName;
    }

    public Citizen getMother() {
        return mother;
    }

    public void setMother(Citizen mother) {
        this.mother = mother;
    }

    public Citizen getFather() {
        return father;
    }

    public void setFather(Citizen father) {
        this.father = father;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public Sex getSex() {
        return sex;
    }

    public void setSex(Sex sex) {
        this.sex = sex;
    }

    public Profession getProfession() {
        return profession;
    }

    public void setProfession(Profession profession) {
        this.profession = profession;
    }

    public Collection<Work> getWorks() {
        return works;
    }

    public void setWorks(Collection<Work> works) {
        this.works = works;
    }
}
