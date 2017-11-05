package com.patex.plural;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Locale;

public class PluralMessageFormatTest {


    private PluralChooserFactory factory ;

    @Before
    public void setUp(){
        factory = new PluralChooserFactory();
    }

    @Test
    public void testSimplePlural() {
        PluralChooser chooser = factory.getFormChooser(Locale.ENGLISH);
        chooser.putWord("book", "books");
        PluralMessageFormat messageFormat = new PluralMessageFormat(chooser);
        Assert.assertEquals("1 book", messageFormat.format("{0} <p:book>", 1));
        Assert.assertEquals("2 books", messageFormat.format("{0} <p:book>", 2));
    }

    @Test
    public void testPluralWithIndex() {
        PluralChooser chooser = factory.getFormChooser(Locale.ENGLISH);
        chooser.putWord("book", "books");
        PluralMessageFormat messageFormat = new PluralMessageFormat(chooser);
        Assert.assertEquals("1 books", messageFormat.format("{0} <p:{1}book>", 1, 2));
        Assert.assertEquals("2 book", messageFormat.format("{0} <p:{1}book>", 2, 1));
    }

    @Test
    public void testSimplePlurals() {
        PluralChooser chooser = factory.getFormChooser(Locale.ENGLISH);
        chooser.putWord("book", "books");
        chooser.putWord("sequence", "sequences");
        PluralMessageFormat messageFormat = new PluralMessageFormat(chooser);
        Assert.assertEquals("10 books 2 sequences", messageFormat.format("{0} <p:book> {1} <p:sequence>", 10, 2));
    }

    @Test
    public void testSimplePluralRu() {
        PluralChooser chooser = factory.getFormChooser(new Locale("ru"));
        chooser.putWord("книга", "книги", "книг");
        PluralMessageFormat messageFormat = new PluralMessageFormat(chooser);
        Assert.assertEquals("1 книга", messageFormat.format("{0} <p:книга>", 1));
        Assert.assertEquals("21 книга", messageFormat.format("{0} <p:книга>", 21));

        Assert.assertEquals("2 книги", messageFormat.format("{0} <p:книга>", 2));
        Assert.assertEquals("24 книги", messageFormat.format("{0} <p:книга>", 24));

        Assert.assertEquals("5 книг", messageFormat.format("{0} <p:книга>", 5));
        Assert.assertEquals("11 книг", messageFormat.format("{0} <p:книга>", 11));
        Assert.assertEquals("36 книг", messageFormat.format("{0} <p:книга>", 36));

    }
}
