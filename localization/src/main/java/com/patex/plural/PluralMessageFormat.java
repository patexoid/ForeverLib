package com.patex.plural;

import java.text.MessageFormat;

public class PluralMessageFormat {


    private final PluralChooser chooser;

    public PluralMessageFormat(PluralChooser chooser) {
        this.chooser = chooser;
    }

    public String format(String source, Object... args) {
        return format(source.toCharArray(), args);
    }


    @SuppressWarnings("StatementWithEmptyBody")
    public String format(char[] source, Object... args) {
        StringBuilder result = new StringBuilder(source.length);
        int argIndex = -1;
        //find nearest left argument index
        for (int i = 0; i < source.length; i++) {
            if (source[i] == '{') {
                int indexBegin = i + 1;
                result.append(source[i++]);
                int indexEnd = -1;
                for (; source[i] != '}'; i++) {
                    if (source[i] == ',') {
                        indexEnd = i;
                    }
                    result.append(source[i]);
                }
                if (indexEnd == -1) {
                    indexEnd = i;
                }
                argIndex = Integer.valueOf(String.valueOf(source, indexBegin, indexEnd - indexBegin));
                result.append(source[i]);
            //find plural format
            } else if (source[i] == '<' && source[i + 1] == 'p' && source[i + 2] == ':') {
                i += 3;
                //if index argument set directly
                if (source[i] == '{') {
                    int indexBegin = i + 1;
                    for (; source[i] != '}'; i++) {
                    }
                    int indexEnd = i;
                    String s = String.valueOf(source, indexBegin, indexEnd - indexBegin);
                    argIndex = Integer.valueOf(s);
                    i++;
                }
                int wordBegin = i;
                for (; source[i] != '>'; i++) {
                }
                int end = i;

                String word = String.copyValueOf(source, wordBegin, end - wordBegin);
                String form = chooser.getForm(word, (Integer) args[argIndex]);
                result.append(form);
            } else {
                result.append(source[i]);
            }
        }
        return MessageFormat.format(result.toString(), args);
    }

}
