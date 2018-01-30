package ru.siksmfp.basic.language.labmdas.warburton.chapters.second.exers;


import ru.siksmfp.basic.language.labmdas.warburton.data.Album;
import ru.siksmfp.basic.language.labmdas.warburton.data.SampleData;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Artyom Karnov on 27.11.16.
 * artyom-karnov@yandex.ru
 **/
public class AlbumsWithTraksWhichLengthLessThan {
    public static void main(String[] args) {
        for (Album album : getAdjustedAlbums(SampleData.albums)) {
            System.out.println(album.getName());
        }
    }

    public static List<Album> getAdjustedAlbums(Stream<Album> album) {
        List<Album> result = new ArrayList<>();
        result = album.filter(current -> current.getTracks().toArray().length <= 3)
                .collect(Collectors.toList());
        return result;
    }
}
