
package fb2;

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
 *         &lt;element ref="{}title-info"/>
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
    "titleInfo"
})
@XmlRootElement(name = "description")
public class Description {

    @XmlElement(name = "title-info", required = true)
    protected TitleInfo titleInfo;

    public Description() {
    }

    /**
     * Gets the value of the titleInfo property.
     * 
     * @return
     *     possible object is
     *     {@link TitleInfo }
     *     
     */
    public TitleInfo getTitleInfo() {
        return titleInfo;
    }

    /**
     * Sets the value of the titleInfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link TitleInfo }
     *     
     */
    public void setTitleInfo(TitleInfo value) {
        this.titleInfo = value;
    }

}
