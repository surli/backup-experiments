package ru.siksmfp.basic.language.labmdas.warburton.chapters.four;


import ru.siksmfp.basic.language.labmdas.warburton.data.Artist;
import ru.siksmfp.basic.language.labmdas.warburton.data.SampleData;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Artem Karnov @date 01.02.2017.
 * artem.karnov@t-systems.com
 */
public class PerformanceInfo implements Performance {
    private List<Artist> membersOfPerformance;
    private String name;

    public PerformanceInfo(String name) {
        membersOfPerformance = new ArrayList<>();
        membersOfPerformance.add(SampleData.georgeHarrison);
        membersOfPerformance.add(SampleData.theBeatles);
        this.name = name;
    }

    public static void main(String[] args) {
        Performance performance = new PerformanceInfo("Central");
        List<Artist> list = performance.getMusicians().collect(Collectors.toList());
        Runnable runnable = () -> {
            for (Artist artist : list) {
                System.out.println(artist.getName());
            }
        };
        runnable.run();

    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Stream<Artist> getMusicians() {
        return membersOfPerformance.stream().flatMap(artist -> Stream.concat(Stream.of(artist), artist.getMembers()));
    }
}
