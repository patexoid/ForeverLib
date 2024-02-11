package com.patex.forever.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.Locale;

@Data
public class UserConfig {

    private Long telegramChatId;

    private String lang;

    @JsonIgnore
    public Locale getLocale() {
        if (lang != null) {
            return new Locale(lang);
        }
        return null;
    }
}
