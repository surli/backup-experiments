package ru.siksmfp.basic.language.labmdas.warburton.chapters.second.exers;


import ru.siksmfp.basic.language.labmdas.warburton.data.SampleData;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Artyom Karnov on 27.11.16.
 * artyom-karnov@yandex.ru
 **/
public class ArtistsAndNations {
    public static void main(String[] args) {
        ArtistsAndNations artistsAndNations = new ArtistsAndNations();
        System.out.println(artistsAndNations.getArtistInfo("John Coltrane"));
    }

    public List<String> getArtistInfo(String name) {
        List<String> result = new ArrayList<>();
        result = SampleData.threeArtists().filter(artist -> artist.getName().equals(name))
                .map(artist -> "Members: {" + artist.getMembers().collect(Collectors.toList()) + "} Nation" + artist.getNationality())
                .collect(Collectors.toList());
        return result;
    }


}
