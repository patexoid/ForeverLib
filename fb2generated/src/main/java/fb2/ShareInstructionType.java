//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0-b170531.0717 
//         See <a href="https://jaxb.java.net/">https://jaxb.java.net/</a> 
//         Any modifications to this file will be lost upon recompilation of the source schema. 
//         Generated on: 2018.10.10 at 08:33:42 PM CEST 
//


package fb2;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;


/**
 * In-document instruction for generating output free and payed documents
 * 
 * <p>Java class for shareInstructionType complex type.
 * 
 * <p>The following schema fragment specifies the expected         content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="shareInstructionType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *         &lt;element name="part" type="{http://www.gribuser.ru/xml/fictionbook/2.0}partShareInstructionType"/&gt;
 *         &lt;element name="output-document-class" type="{http://www.gribuser.ru/xml/fictionbook/2.0}outPutDocumentType"/&gt;
 *       &lt;/choice&gt;
 *       &lt;attribute name="mode" use="required" type="{http://www.gribuser.ru/xml/fictionbook/2.0}shareModesType" /&gt;
 *       &lt;attribute name="include-all" use="required" type="{http://www.gribuser.ru/xml/fictionbook/2.0}docGenerationInstructionType" /&gt;
 *       &lt;attribute name="price" type="{http://www.w3.org/2001/XMLSchema}float" /&gt;
 *       &lt;attribute name="currency" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "shareInstructionType", propOrder = {
    "partOrOutputDocumentClass"
})
public class ShareInstructionType {

    @XmlElements({
        @XmlElement(name = "part", type = PartShareInstructionType.class),
        @XmlElement(name = "output-document-class", type = OutPutDocumentType.class)
    })
    protected List<Object> partOrOutputDocumentClass;
    @XmlAttribute(name = "mode", required = true)
    protected ShareModesType mode;
    @XmlAttribute(name = "include-all", required = true)
    protected DocGenerationInstructionType includeAll;
    @XmlAttribute(name = "price")
    protected Float price;
    @XmlAttribute(name = "currency")
    protected String currency;

    /**
     * Gets the value of the partOrOutputDocumentClass property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the partOrOutputDocumentClass property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPartOrOutputDocumentClass().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PartShareInstructionType }
     * {@link OutPutDocumentType }
     * 
     * 
     */
    public List<Object> getPartOrOutputDocumentClass() {
        if (partOrOutputDocumentClass == null) {
            partOrOutputDocumentClass = new ArrayList<Object>();
        }
        return this.partOrOutputDocumentClass;
    }

    /**
     * Gets the value of the mode property.
     * 
     * @return
     *     possible object is
     *     {@link ShareModesType }
     *     
     */
    public ShareModesType getMode() {
        return mode;
    }

    /**
     * Sets the value of the mode property.
     * 
     * @param value
     *     allowed object is
     *     {@link ShareModesType }
     *     
     */
    public void setMode(ShareModesType value) {
        this.mode = value;
    }

    /**
     * Gets the value of the includeAll property.
     * 
     * @return
     *     possible object is
     *     {@link DocGenerationInstructionType }
     *     
     */
    public DocGenerationInstructionType getIncludeAll() {
        return includeAll;
    }

    /**
     * Sets the value of the includeAll property.
     * 
     * @param value
     *     allowed object is
     *     {@link DocGenerationInstructionType }
     *     
     */
    public void setIncludeAll(DocGenerationInstructionType value) {
        this.includeAll = value;
    }

    /**
     * Gets the value of the price property.
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    public Float getPrice() {
        return price;
    }

    /**
     * Sets the value of the price property.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setPrice(Float value) {
        this.price = value;
    }

    /**
     * Gets the value of the currency property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCurrency() {
        return currency;
    }

    /**
     * Sets the value of the currency property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCurrency(String value) {
        this.currency = value;
    }

}
