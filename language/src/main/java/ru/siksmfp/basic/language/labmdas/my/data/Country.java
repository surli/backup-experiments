package ru.siksmfp.basic.language.labmdas.my.data;

import java.util.Collection;

/**
 * @author Artem Karnov @date 17.02.2017.
 * artem.karnov@t-systems.com
 */
public class Country {
    private Collection<Citizen> citizensList;
    private Collection<Work> worksList;
    private Collection<Profession> professionsList;

    public Collection<Citizen> getCitizensList() {
        return citizensList;
    }

    public void setCitizensList(Collection<Citizen> citizensList) {
        this.citizensList = citizensList;
    }

    public Collection<Work> getWorksList() {
        return worksList;
    }

    public void setWorksList(Collection<Work> worksList) {
        this.worksList = worksList;
    }

    public Collection<Profession> getProfessionsList() {
        return professionsList;
    }

    public void setProfessionsList(Collection<Profession> professionsList) {
        this.professionsList = professionsList;
    }
}
