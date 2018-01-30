package ru.siksmfp.basic.language.labmdas.warburton.chapters.second.exers;

import ru.siksmfp.basic.language.labmdas.warburton.data.Artist;
import ru.siksmfp.basic.language.labmdas.warburton.data.SampleData;

import java.util.List;
import java.util.stream.Stream;

/**
 * Created by Artyom Karnov on 28.11.16.
 * artyom-karnov@yandex.ru
 **/
public class InternalIterator {
    public static void main(String[] args) {
        int totalMembers = 0;
        for (Artist artist : SampleData.getThreeArtists()) {
            Stream<Artist> members = artist.getMembers();
            totalMembers += members.count();
        }
        System.out.println(totalMembers);
        InternalIterator internalIterator = new InternalIterator();
        System.out.println(internalIterator.internalIterator(SampleData.getThreeArtists()));
    }

    public int internalIterator(List<Artist> artists) {
        int result = 0;
        result = (int) artists.stream().flatMap(artist -> artist.getMembers()).count();
        return result;
    }
}
