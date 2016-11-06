
package fb2;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{}genre" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{}author" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{}book-title"/>
 *         &lt;element ref="{}sequence" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{}annotation"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "genre",
    "author",
    "bookTitle",
    "sequence",
    "annotation"
})
@XmlRootElement(name = "title-info")
public class TitleInfo {

    protected List<String> genre;
    protected List<Author> author;
    @XmlElement(name = "book-title", required = true)
    protected String bookTitle;
    protected List<Sequence> sequence;
    @XmlElement(required = true)
    protected Annotation annotation;

    public TitleInfo() {
    }

    /**
     * Gets the value of the genre property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the genre property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getGenre().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getGenre() {
        if (genre == null) {
            genre = new ArrayList<String>();
        }
        return this.genre;
    }

    /**
     * Gets the value of the author property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the author property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAuthor().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Author }
     * 
     * 
     */
    public List<Author> getAuthor() {
        if (author == null) {
            author = new ArrayList<Author>();
        }
        return this.author;
    }

    /**
     * Gets the value of the bookTitle property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBookTitle() {
        return bookTitle;
    }

    /**
     * Sets the value of the bookTitle property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBookTitle(String value) {
        this.bookTitle = value;
    }

    /**
     * Gets the value of the sequence property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the sequence property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSequence().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Sequence }
     * 
     * 
     */
    public List<Sequence> getSequence() {
        if (sequence == null) {
            sequence = new ArrayList<Sequence>();
        }
        return this.sequence;
    }

    /**
     * Gets the value of the annotation property.
     * 
     * @return
     *     possible object is
     *     {@link Annotation }
     *     
     */
    public Annotation getAnnotation() {
        return annotation;
    }

    /**
     * Sets the value of the annotation property.
     * 
     * @param value
     *     allowed object is
     *     {@link Annotation }
     *     
     */
    public void setAnnotation(Annotation value) {
        this.annotation = value;
    }

}
