package com.patex.plural;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

//TODO temporary solution need to make it more universal(javacc???)
public class PluralChooserFactory {

    private final Map<String, Function<Integer, Integer>> chooseFunc = new HashMap<>();

     {
        chooseFunc.put("en", count -> count == 1 ? 0 : 1);
        chooseFunc.put("uk", PluralChooserFactory::uk_ru_fchooser);
        chooseFunc.put("ru", PluralChooserFactory::uk_ru_fchooser);
    }

    private final Map<String, PluralChooser> choosers = new HashMap<>();

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

    PluralChooser getFormChooser(Locale locale) {
        String language = locale.getLanguage();
        PluralChooser pluralChooser = choosers.get(language);
        if (pluralChooser == null) {
            synchronized (chooseFunc) {
                pluralChooser = choosers.get(language);
                if (pluralChooser == null) {
                    pluralChooser = createChooser(locale);
                    choosers.put(language, pluralChooser);
                }
            }
        }
        return pluralChooser;
    }

    private PluralChooser createChooser(Locale locale)  {
        try {
            String language = locale.getLanguage();
            PluralChooser chooser = new PluralChooser(chooseFunc.getOrDefault(language, i -> 0), locale);
            InputStream resStream = PluralChooserFactory.class.getResourceAsStream("/plural_" + language + ".txt");
            if (resStream != null)
                try (BufferedReader buff = new BufferedReader(new InputStreamReader(resStream))) {
                    buff.lines().forEach(chooser::putWord);
                }
            return chooser;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
