package com.patex.storage;


import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import lombok.SneakyThrows;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

class GuicePropertyModule extends AbstractModule {

    public static final String DATA_STORAGE_PROPERTIES = "/dataStorage.properties";

    @Override

    @SneakyThrows
    protected void configure() {
        Properties properties = new Properties();
        properties.load(getClass().getResourceAsStream(DATA_STORAGE_PROPERTIES));
        if (new File("." +DATA_STORAGE_PROPERTIES).exists()) {
            properties.load(new FileInputStream("." + DATA_STORAGE_PROPERTIES));
        }
        properties.forEach((name, value) -> bind(String.class).
                annotatedWith(Names.named((String) name)).
                toInstance((String) value));
    }
}
