package fb2Generator;


import fb2.AnnotationType;
import fb2.BodyType;
import fb2.FictionBook;
import fb2.GenreType;
import fb2.InlineImageType;
import fb2.ObjectFactory;
import fb2.PType;
import fb2.SectionType;
import fb2.SequenceType;
import fb2.TextFieldType;
import fb2.TitleInfoType;
import org.apache.commons.lang3.StringUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigInteger;

/**
 * Created by Alexey on 11/5/2016.
 */
public class Fb2Creator {
    private static final ObjectFactory F = new ObjectFactory();
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
        FictionBook.Description description = F.createFictionBookDescription();
        titleInfo = F.createTitleInfoType();

        titleInfo.setBookTitle(getTextFieldType(title));
        description.setTitleInfo(titleInfo);
        fbook.setDescription(description);
    }

    private Fb2Creator addAuthor(String firstName, String middleName, String lastName, String homePage, String email) {
        TitleInfoType.Author author = F.createTitleInfoTypeAuthor();

        if (StringUtils.isNotEmpty(firstName))
            author.getContent().add(F.createAuthorTypeFirstName(getTextFieldType(firstName)));
        if (StringUtils.isNotEmpty(middleName))
            author.getContent().add(F.createAuthorTypeMiddleName(getTextFieldType(middleName)));
        if (StringUtils.isNotEmpty(lastName))
            author.getContent().add(F.createAuthorTypeLastName(getTextFieldType(lastName)));
        if (StringUtils.isNotEmpty(homePage))
            author.getContent().add(F.createAuthorTypeHomePage(homePage));
        if (StringUtils.isNotEmpty(email))
            author.getContent().add(F.createAuthorTypeEmail(email));
        titleInfo.getAuthor().add(author);
        return this;
    }

    private TextFieldType getTextFieldType(String firstName) {
        TextFieldType firstNameTextFieldType = F.createTextFieldType();
        firstNameTextFieldType.setValue(firstName);
        return firstNameTextFieldType;
    }

    public Fb2Creator setCoverpage(String href) {
        TitleInfoType.Coverpage coverpage = F.createTitleInfoTypeCoverpage();
        InlineImageType imageType = new InlineImageType();
        imageType.setHref(href);
        coverpage.getImage().add(imageType);
        titleInfo.setCoverpage(coverpage);
        return this;
    }


    public Fb2Creator addAuthor(String firstName, String middleName, String lastName) {
        return addAuthor(firstName, middleName, lastName, null, null);
    }

    public Fb2Creator addGenre(String genre) {
        TitleInfoType.Genre genreType = F.createTitleInfoTypeGenre();
        genreType.setValue(GenreType.fromValue(genre));
        titleInfo.getGenre().add(genreType);
        return this;
    }

    public Fb2Creator addSequence(String sequenceName, Integer number) {
        SequenceType sequence = F.createSequenceType();
        sequence.setName(sequenceName);
        if (number != null)
            sequence.setNumber(BigInteger.valueOf(number));
        titleInfo.getSequence().add(sequence);
        return this;
    }


    public Fb2Creator addContent(String content) {
        if (body == null) {
            nextSection();
        }
        PType pType = F.createPType();
        pType.getContent().add(content);
        section.getPOrImageOrPoem().add(F.createSectionTypeP(pType));
        fbook.setBody(body);
        return this;
    }


    public Fb2Creator addBinary(String href, byte[] content, String contentType) {
        FictionBook.Binary binary = F.createFictionBookBinary();
        binary.setId(href);
        binary.setContentType(contentType);
        binary.setValue(content);
        fbook.getBinary().add(binary);
        return this;
    }

    public Fb2Creator nextSection() {
        if (body == null) {
            body = F.createBodyType();
        }
        if (section == null) {
            section = F.createSectionType();
            body.getSection().add(section);
        }
        return this;
    }

    public Fb2Creator addAnnotationPLine(String pLine) {
        if (annotation == null) {
            annotation = F.createAnnotationType();
            titleInfo.setAnnotation(annotation);
        }
        PType pType = F.createPType();
        pType.getContent().add(pLine);
        F.createAnnotationTypeP(pType);
        annotation.getPOrPoemOrCite().add(F.createAnnotationTypeP(pType));
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
