package com.patex.plural;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

public class PluralChooser {

    private final Function<Integer, Integer> formNumber;
    private final Locale locale;

    private Map<String, String[]> allWords = new HashMap<>();

    public PluralChooser(Function<Integer, Integer> formNumber, Locale locale) {
        this.formNumber = formNumber;
        this.locale = locale;
    }

    public void putWord(String... wordForms) {
        wordForms = Arrays.stream(wordForms).map(s -> s.toLowerCase(locale)).toArray(String[]::new);
        allWords.put(wordForms[0], wordForms);
    }

    public String getForm(String word, int count) {
        String[] words = allWords.get(word.toLowerCase());
        if (words != null) {
            String result = words[formNumber.apply(count)];
            if (Character.isUpperCase(word.charAt(0))) {
                if (word.length() == 1 || Character.isUpperCase(word.charAt(1))) {
                    result = result.toUpperCase(locale);
                } else {
                    result = result.substring(0, 1).toUpperCase(locale) + result.substring(1);
                }
            }
            return result;
        }
        return word;
    }
}
