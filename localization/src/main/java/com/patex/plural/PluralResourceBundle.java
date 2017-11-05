package com.patex.plural;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Enumeration;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class PluralResourceBundle extends ResourceBundle {

    private static Logger log = LoggerFactory.getLogger(PluralResourceBundle.class);
    private static PluralChooserFactory defaultFactory = new PluralChooserFactory();
    private final ResourceBundle resourceBundle;
    private final PluralMessageFormat pluralMessageFormat;


    public PluralResourceBundle(ResourceBundle resourceBundle, PluralChooserFactory factory) {
        this.resourceBundle = resourceBundle;
        Locale locale = resourceBundle.getLocale();
        PluralChooser formChooser = factory.getFormChooser(locale);
        pluralMessageFormat = new PluralMessageFormat(formChooser);
    }

    public PluralResourceBundle(ResourceBundle resourceBundle) {
        this(resourceBundle, defaultFactory);
    }

    @Override
    public Enumeration<String> getKeys() {
        return resourceBundle.getKeys();
    }

    @Override
    public Object handleGetObject(String key) {
        try {
            return String.valueOf(resourceBundle.getObject(key));
        } catch (MissingResourceException e) {
            log.warn("missing resource:" + key);
        }
        return key;
    }

    public String get(String key, Object... objects) {
        try {
            String value = String.valueOf(resourceBundle.getObject(key));
            return pluralMessageFormat.format(value, objects);
        } catch (MissingResourceException e) {
            log.warn("missing resource:" + key);
        }
        return key;
    }

}
