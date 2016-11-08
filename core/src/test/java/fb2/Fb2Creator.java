package fb2;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.*;

/**
 * Created by Alexey on 11/5/2016.
 */
public class Fb2Creator {
    private FictionBook fbook;
    private final TitleInfo titleInfo;
    private Annotation annotation;
    private final Marshaller jaxbMarshaller;


    public Fb2Creator(String title){
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(FictionBook.class);
            jaxbMarshaller = jaxbContext.createMarshaller();
        } catch (JAXBException e) {
            throw new UnsupportedOperationException();
        }

        fbook = new FictionBook();
        Description description = new Description();
        titleInfo = new TitleInfo();
        titleInfo.setBookTitle(title);
        description.setTitleInfo(titleInfo);
        fbook.setDescription(description);
    }

    public Fb2Creator addAuthor(String firstName, String middleName, String lastName, String homePage, String email) {
        titleInfo.getAuthor().add(new Author(firstName, middleName, lastName, homePage, email));
        return this;
    }

    public Fb2Creator addAuthor(String firstName, String middleName, String lastName) {
        return addAuthor(firstName, middleName, lastName, null, null);
    }

    public Fb2Creator addGenre(String genre) {
        titleInfo.getGenre().add(genre);
        return this;
    }

    public Fb2Creator addSequence(String sequence, int number) {
        titleInfo.getSequence().add(new Sequence(sequence, number));
        return this;
    }


    public Fb2Creator addAnnotationLine(String annotationLine){
        if(annotation==null) {
            annotation = new Annotation();
            titleInfo.setAnnotation(annotation);
        }
        annotation.getP().add(annotationLine);
        return this;
    }

    public InputStream getFbook(){
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            jaxbMarshaller.marshal(fbook, out);

            return new ByteArrayInputStream(out.toByteArray());
        } catch (JAXBException e) {
            throw new UnsupportedOperationException();
        }
    }
}
