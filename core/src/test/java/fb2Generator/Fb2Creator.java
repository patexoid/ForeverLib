package fb2Generator;


import fb2.AnnotationType;
import fb2.BodyType;
import fb2.FictionBook;
import fb2.GenreType;
import fb2.SectionType;
import fb2.SequenceType;
import fb2.TextFieldType;
import fb2.TitleInfoType;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigInteger;

/**
 * Created by Alexey on 11/5/2016.
 */
public class Fb2Creator {
    private final TitleInfoType titleInfo;
    private final Marshaller jaxbMarshaller;
    private FictionBook fbook;
    private AnnotationType annotation;
    private BodyType body;
    private SectionType section;


    public Fb2Creator(String title) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(FictionBook.class);
            jaxbMarshaller = jaxbContext.createMarshaller();
        } catch (JAXBException e) {
            throw new UnsupportedOperationException();
        }

        fbook = new FictionBook();
        FictionBook.Description description = new FictionBook.Description();
        titleInfo = new TitleInfoType();

        TextFieldType titleType = new TextFieldType();
        titleType.setValue(title);
        titleInfo.setBookTitle(titleType);
        description.setTitleInfo(titleInfo);
        fbook.setDescription(description);
    }

    public Fb2Creator addAuthor(String firstName, String middleName, String lastName, String homePage, String email) {
        TitleInfoType.Author author = new TitleInfoType.Author();
        author.getContent().add(new JAXBElement<>(new QName("", "first-name"), String.class, firstName));
        author.getContent().add(new JAXBElement<>(new QName("", "middle-name"), String.class, middleName));
        author.getContent().add(new JAXBElement<>(new QName("", "last-name"), String.class, lastName));
        author.getContent().add(new JAXBElement<>(new QName("", "home-page"), String.class, homePage));
        author.getContent().add(new JAXBElement<>(new QName("", "email"), String.class, email));
        titleInfo.getAuthor().add(author);
        return this;
    }

    public Fb2Creator addAuthor(String firstName, String middleName, String lastName) {
        return addAuthor(firstName, middleName, lastName, null, null);
    }

    public Fb2Creator addGenre(String genre) {
        TitleInfoType.Genre genreType = new TitleInfoType.Genre();
        genreType.setValue(GenreType.valueOf(genre));
        titleInfo.getGenre().add(genreType);
        return this;
    }

    public Fb2Creator addSequence(String sequenceName, int number) {
        SequenceType sequence = new SequenceType();
        sequence.setName(sequenceName);
        sequence.setNumber(BigInteger.valueOf(number));
        titleInfo.getSequence().add(sequence);
        return this;
    }

    public Fb2Creator addContent(String content){
        if(body==null) {
           nextSection();
        }
        section.getPOrImageOrPoem().add(new JAXBElement<>(new QName("p"), String.class,content));
        fbook.setBody(body);
        return this;
    }

    public Fb2Creator nextSection(){
        if(body==null) {
            body = new BodyType();
        }
        if(section==null){
            section = new SectionType();
            body.getSection().add(section);
        }
        return this;
    }

    public Fb2Creator addAnnotationLine(String annotationLine) {
        if (annotation == null) {
            annotation = new AnnotationType();
            titleInfo.setAnnotation(annotation);
        }
        annotation.getPOrPoemOrCite().add(new JAXBElement<>(new QName("", "p"), String.class, annotationLine));
        return this;
    }

    public InputStream getFbook() {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            jaxbMarshaller.marshal(fbook, out);

            return new ByteArrayInputStream(out.toByteArray());
        } catch (JAXBException e) {
            throw new UnsupportedOperationException();
        }
    }
}
