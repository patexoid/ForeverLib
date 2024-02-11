package com.patex.forever.service;


import com.optimaize.langdetect.LanguageDetector;
import com.optimaize.langdetect.LanguageDetectorBuilder;
import com.optimaize.langdetect.i18n.LdLocale;
import com.optimaize.langdetect.ngram.NgramExtractors;
import com.optimaize.langdetect.profiles.LanguageProfile;
import com.optimaize.langdetect.profiles.LanguageProfileReader;
import com.optimaize.langdetect.text.CommonTextObjectFactories;
import com.optimaize.langdetect.text.TextObjectFactory;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class LanguageService {

    private final TextObjectFactory textObjectFactory = CommonTextObjectFactories.forDetectingOnLargeText();
    private final LanguageDetector langDetector = createLangDetector();

    @SafeVarargs
    @SneakyThrows
    public final Optional<String> detectLang(Supplier<String>... textSupplier) {
        return Stream.of(textSupplier).
                map(Supplier::get).
                filter(Objects::nonNull).
                filter(s -> s.length() > 300).
                map(textObjectFactory::forText).
                map(langDetector::detect).
                filter(com.google.common.base.Optional::isPresent).
                map(com.google.common.base.Optional::get).
                map(LdLocale::getLanguage).findFirst();
    }

    @SneakyThrows
    private LanguageDetector createLangDetector() {
        List<LanguageProfile> languageProfiles = new LanguageProfileReader().readAllBuiltIn();
        return LanguageDetectorBuilder.create(NgramExtractors.standard())
                .withProfiles(languageProfiles)
                .build();
    }
}


