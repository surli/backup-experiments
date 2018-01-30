package ru.siksmfp.basic.language.labmdas.warburton.chapters.first.exers;

import javax.swing.text.DateFormatter;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Created by Artyom Karnov on 26.11.16.
 * artyom-karnov@yandex.ru
 **/
public class Dates {
    public DateFormatter dateFormatter;

    public Dates() {
        synchronized (new Object()) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat();
            simpleDateFormat.applyPattern("dd-MM-yyyy");
            dateFormatter = new DateFormatter(simpleDateFormat);
        }
    }

    public static void main(String[] args) throws ParseException {
        Dates dates = new Dates();
        System.out.println(dates.dateFormatter.stringToValue("06-02-1971"));
    }
}
