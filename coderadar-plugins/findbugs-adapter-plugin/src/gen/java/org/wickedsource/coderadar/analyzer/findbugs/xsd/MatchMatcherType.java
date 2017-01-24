//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// �nderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2017.01.24 um 10:39:08 PM CET 
//


package org.wickedsource.coderadar.analyzer.findbugs.xsd;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse f�r MatchMatcherType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="MatchMatcherType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{}MatcherType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{}Matcher" maxOccurs="unbounded"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="classregex" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="class" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MatchMatcherType", propOrder = {
    "matcher"
})
public class MatchMatcherType
    extends MatcherType
{

    @XmlElementRef(name = "Matcher", type = JAXBElement.class)
    protected List<JAXBElement<? extends MatcherType>> matcher;
    @XmlAttribute(name = "classregex")
    protected String classregex;
    @XmlAttribute(name = "class")
    protected String clazz;

    /**
     * Gets the value of the matcher property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the matcher property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMatcher().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link OrMatcherType }{@code >}
     * {@link JAXBElement }{@code <}{@link ClassMatcherType }{@code >}
     * {@link JAXBElement }{@code <}{@link PackageMatcherType }{@code >}
     * {@link JAXBElement }{@code <}{@link RankMatcherType }{@code >}
     * {@link JAXBElement }{@code <}{@link AndMatcherType }{@code >}
     * {@link JAXBElement }{@code <}{@link BugCodeMatcherType }{@code >}
     * {@link JAXBElement }{@code <}{@link BugMatcherType }{@code >}
     * {@link JAXBElement }{@code <}{@link FieldMatcherType }{@code >}
     * {@link JAXBElement }{@code <}{@link NotMatcherType }{@code >}
     * {@link JAXBElement }{@code <}{@link LastVersionMatcherType }{@code >}
     * {@link JAXBElement }{@code <}{@link LocalMatcherType }{@code >}
     * {@link JAXBElement }{@code <}{@link DesignationMatcherType }{@code >}
     * {@link JAXBElement }{@code <}{@link FirstVersionMatcherType }{@code >}
     * {@link JAXBElement }{@code <}{@link MatcherType }{@code >}
     * {@link JAXBElement }{@code <}{@link MatchMatcherType }{@code >}
     * {@link JAXBElement }{@code <}{@link PriorityMatcherType }{@code >}
     * {@link JAXBElement }{@code <}{@link MethodMatcherType }{@code >}
     * {@link JAXBElement }{@code <}{@link BugPatternMatcherType }{@code >}
     * 
     * 
     */
    public List<JAXBElement<? extends MatcherType>> getMatcher() {
        if (matcher == null) {
            matcher = new ArrayList<JAXBElement<? extends MatcherType>>();
        }
        return this.matcher;
    }

    /**
     * Ruft den Wert der classregex-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getClassregex() {
        return classregex;
    }

    /**
     * Legt den Wert der classregex-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setClassregex(String value) {
        this.classregex = value;
    }

    /**
     * Ruft den Wert der clazz-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getClazz() {
        return clazz;
    }

    /**
     * Legt den Wert der clazz-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setClazz(String value) {
        this.clazz = value;
    }

}
