package com.patex.plural;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

//TODO temporary solution need to make it more universal(javacc???)
public class PluralChooserFactory {

    public static Map<String, Function<Integer, Integer>> chooseFunc = new HashMap<>();

    static {
        chooseFunc.put("en", count -> count == 1 ? 0 : 1);
        chooseFunc.put("uk", PluralChooserFactory::uk_ru_fchooser);
        chooseFunc.put("ru", PluralChooserFactory::uk_ru_fchooser);
    }

    private final Map<String, PluralChooser> choosers= new HashMap<>();

    private static Integer uk_ru_fchooser(Integer count) {
        int lastDigit = count % 10;
        if (lastDigit == 1 && count % 100 != 11) {
            return 0;
        } else if (lastDigit >= 2 && lastDigit <= 4) {
            return 1;
        } else {
            return 2;
        }
    }

    public synchronized PluralChooser getFormChooser(Locale locale) {
        String language = locale.getLanguage();
        PluralChooser pluralChooser = choosers.get(language);
        if(pluralChooser==null){
            pluralChooser=createChooser(locale);
            choosers.put(language, pluralChooser);
        }
        return pluralChooser;
    }

    private PluralChooser createChooser(Locale locale) {
        String language = locale.getLanguage();
        PluralChooser chooser = new PluralChooser(chooseFunc.getOrDefault(language, i -> 0), locale);
        URL resource = PluralChooserFactory.class.getResource("/plural_" + language + ".txt");
        if (resource != null) {
            try {
                List<String> lines = Files.readAllLines(Paths.get(resource.toURI()));
                lines.stream().map(s -> s.split(",")).forEach(chooser::putWord);
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
            }
        }
        return chooser;
    }
}
