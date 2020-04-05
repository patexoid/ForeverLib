package com.patex.zombie.service;


import com.patex.plural.PluralResourceBundle;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

@Component
public class Resources {

    private final Map<Locale, PluralResourceBundle> bundles = new HashMap<>();

    public String get(Locale locale, String key, Object... objects) {
        return getBundle(locale).get(key, objects);
    }

    public PluralResourceBundle getBundle(Locale locale) {
        PluralResourceBundle bundle = bundles.get(locale);
        if (bundle == null) {
            synchronized (bundles) {
                bundle = bundles.computeIfAbsent(locale,
                        loc -> new PluralResourceBundle(ResourceBundle.getBundle("zbundle", loc, new UTF8Control()))
                );
            }

        }
        return bundle;
    }

    public class UTF8Control extends ResourceBundle.Control {
        public ResourceBundle newBundle
                (String baseName, Locale locale, String format, ClassLoader loader, boolean reload)
                throws IllegalAccessException, InstantiationException, IOException
        {
            // The below is a copy of the default implementation.
            String bundleName = toBundleName(baseName, locale);
            String resourceName = toResourceName(bundleName, "properties");
            ResourceBundle bundle = null;
            InputStream stream = null;
            if (reload) {
                URL url = loader.getResource(resourceName);
                if (url != null) {
                    URLConnection connection = url.openConnection();
                    if (connection != null) {
                        connection.setUseCaches(false);
                        stream = connection.getInputStream();
                    }
                }
            } else {
                stream = loader.getResourceAsStream(resourceName);
            }
            if (stream != null) {
                try {
                    // Only this line is changed to make it to read properties files as UTF-8.
                    bundle = new PropertyResourceBundle(new InputStreamReader(stream, "UTF-8"));
                } finally {
                    stream.close();
                }
            }
            return bundle;
        }
    }
}
