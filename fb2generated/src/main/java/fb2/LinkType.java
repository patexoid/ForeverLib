//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0-b170531.0717 
//         See <a href="https://jaxb.java.net/">https://jaxb.java.net/</a> 
//         Any modifications to this file will be lost upon recompilation of the source schema. 
//         Generated on: 2018.10.10 at 08:33:42 PM CEST 
//


package fb2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlMixed;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * Generic hyperlinks. Cannot be nested. Footnotes should be implemented by links referring to additional bodies in the same document
 * 
 * <p>Java class for linkType complex type.
 * 
 * <p>The following schema fragment specifies the expected         content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="linkType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *         &lt;element name="strong" type="{http://www.gribuser.ru/xml/fictionbook/2.0}styleLinkType"/&gt;
 *         &lt;element name="emphasis" type="{http://www.gribuser.ru/xml/fictionbook/2.0}styleLinkType"/&gt;
 *         &lt;element name="style" type="{http://www.gribuser.ru/xml/fictionbook/2.0}styleLinkType"/&gt;
 *         &lt;element name="strikethrough" type="{http://www.gribuser.ru/xml/fictionbook/2.0}styleLinkType"/&gt;
 *         &lt;element name="sub" type="{http://www.gribuser.ru/xml/fictionbook/2.0}styleLinkType"/&gt;
 *         &lt;element name="sup" type="{http://www.gribuser.ru/xml/fictionbook/2.0}styleLinkType"/&gt;
 *         &lt;element name="code" type="{http://www.gribuser.ru/xml/fictionbook/2.0}styleLinkType"/&gt;
 *         &lt;element name="image" type="{http://www.gribuser.ru/xml/fictionbook/2.0}inlineImageType"/&gt;
 *       &lt;/choice&gt;
 *       &lt;attribute ref="{http://www.w3.org/1999/xlink}type"/&gt;
 *       &lt;attribute ref="{http://www.w3.org/1999/xlink}href use="required""/&gt;
 *       &lt;attribute name="type" type="{http://www.w3.org/2001/XMLSchema}token" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "linkType", propOrder = {
    "content"
})
public class LinkType {

    @XmlElementRefs({
        @XmlElementRef(name = "strong", namespace = "http://www.gribuser.ru/xml/fictionbook/2.0", type = JAXBElement.class),
        @XmlElementRef(name = "emphasis", namespace = "http://www.gribuser.ru/xml/fictionbook/2.0", type = JAXBElement.class),
        @XmlElementRef(name = "style", namespace = "http://www.gribuser.ru/xml/fictionbook/2.0", type = JAXBElement.class),
        @XmlElementRef(name = "strikethrough", namespace = "http://www.gribuser.ru/xml/fictionbook/2.0", type = JAXBElement.class),
        @XmlElementRef(name = "sub", namespace = "http://www.gribuser.ru/xml/fictionbook/2.0", type = JAXBElement.class),
        @XmlElementRef(name = "sup", namespace = "http://www.gribuser.ru/xml/fictionbook/2.0", type = JAXBElement.class),
        @XmlElementRef(name = "code", namespace = "http://www.gribuser.ru/xml/fictionbook/2.0", type = JAXBElement.class),
        @XmlElementRef(name = "image", namespace = "http://www.gribuser.ru/xml/fictionbook/2.0", type = JAXBElement.class)
    })
    @XmlMixed
    protected List<Serializable> content;
    @XmlAttribute(name = "type", namespace = "http://www.w3.org/1999/xlink")
    protected String linkType;
    @XmlAttribute(name = "href", namespace = "http://www.w3.org/1999/xlink", required = true)
    protected String href;
    @XmlAttribute(name = "type")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String type;

    /**
     * Generic hyperlinks. Cannot be nested. Footnotes should be implemented by links referring to additional bodies in the same document Gets the value of the content property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the content property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getContent().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link StyleLinkType }{@code >}
     * {@link JAXBElement }{@code <}{@link StyleLinkType }{@code >}
     * {@link JAXBElement }{@code <}{@link StyleLinkType }{@code >}
     * {@link JAXBElement }{@code <}{@link StyleLinkType }{@code >}
     * {@link JAXBElement }{@code <}{@link StyleLinkType }{@code >}
     * {@link JAXBElement }{@code <}{@link StyleLinkType }{@code >}
     * {@link JAXBElement }{@code <}{@link StyleLinkType }{@code >}
     * {@link JAXBElement }{@code <}{@link InlineImageType }{@code >}
     * {@link String }
     * 
     * 
     */
    public List<Serializable> getContent() {
        if (content == null) {
            content = new ArrayList<Serializable>();
        }
        return this.content;
    }

    /**
     * Gets the value of the linkType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLinkType() {
        if (linkType == null) {
            return "simple";
        } else {
            return linkType;
        }
    }

    /**
     * Sets the value of the linkType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLinkType(String value) {
        this.linkType = value;
    }

    /**
     * Gets the value of the href property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHref() {
        return href;
    }

    /**
     * Sets the value of the href property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHref(String value) {
        this.href = value;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setType(String value) {
        this.type = value;
    }

}
