//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.04.25 at 04:25:33 PM EDT 
//


package com.percussion.workbench.ui.views.hierarchy.xmlbind;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;


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
 *         &lt;element ref="{}CatalogProperty" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{}Handler" minOccurs="0"/>
 *         &lt;element ref="{}Node" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{}Catalog" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="type" default="object">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *             &lt;enumeration value="class"/>
 *             &lt;enumeration value="object"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="model" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="allowedTypes">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKENS">
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="defaultInstanceTree" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="isReference" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="displayLabelSource" default="objectName">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *             &lt;enumeration value="objectName"/>
 *             &lt;enumeration value="objectLabel"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="catalogObjectTypes" type="{http://www.w3.org/2001/XMLSchema}NMTOKENS" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "catalogProperty",
    "handler",
    "node",
    "catalog"
})
@XmlRootElement(name = "Catalog")
public class Catalog {

    @XmlElement(name = "CatalogProperty")
    protected List<CatalogProperty> catalogProperty;
    @XmlElement(name = "Handler")
    protected Handler handler;
    @XmlElement(name = "Node")
    protected List<Node> node;
    @XmlElement(name = "Catalog")
    protected List<Catalog> catalog;
    @XmlAttribute(name = "type")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String type;
    @XmlAttribute(name = "name")
    protected String name;
    @XmlAttribute(name = "model")
    protected String model;
    @XmlAttribute(name = "allowedTypes")
    protected List<String> allowedTypes;
    @XmlAttribute(name = "defaultInstanceTree")
    protected Boolean defaultInstanceTree;
    @XmlAttribute(name = "isReference")
    protected Boolean isReference;
    @XmlAttribute(name = "displayLabelSource")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String displayLabelSource;
    @XmlAttribute(name = "catalogObjectTypes")
    @XmlSchemaType(name = "NMTOKENS")
    protected List<String> catalogObjectTypes;

    /**
     * Gets the value of the catalogProperty property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the catalogProperty property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCatalogProperty().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CatalogProperty }
     * 
     * 
     */
    public List<CatalogProperty> getCatalogProperty() {
        if (catalogProperty == null) {
            catalogProperty = new ArrayList<CatalogProperty>();
        }
        return this.catalogProperty;
    }

    /**
     * Gets the value of the handler property.
     * 
     * @return
     *     possible object is
     *     {@link Handler }
     *     
     */
    public Handler getHandler() {
        return handler;
    }

    /**
     * Sets the value of the handler property.
     * 
     * @param value
     *     allowed object is
     *     {@link Handler }
     *     
     */
    public void setHandler(Handler value) {
        this.handler = value;
    }

    /**
     * Gets the value of the node property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the node property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getNode().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Node }
     * 
     * 
     */
    public List<Node> getNode() {
        if (node == null) {
            node = new ArrayList<Node>();
        }
        return this.node;
    }

    /**
     * Gets the value of the catalog property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the catalog property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCatalog().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Catalog }
     * 
     * 
     */
    public List<Catalog> getCatalog() {
        if (catalog == null) {
            catalog = new ArrayList<Catalog>();
        }
        return this.catalog;
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
        if (type == null) {
            return "object";
        } else {
            return type;
        }
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

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the model property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getModel() {
        return model;
    }

    /**
     * Sets the value of the model property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setModel(String value) {
        this.model = value;
    }

    /**
     * Gets the value of the allowedTypes property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the allowedTypes property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAllowedTypes().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getAllowedTypes() {
        if (allowedTypes == null) {
            allowedTypes = new ArrayList<String>();
        }
        return this.allowedTypes;
    }

    /**
     * Gets the value of the defaultInstanceTree property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isDefaultInstanceTree() {
        if (defaultInstanceTree == null) {
            return true;
        } else {
            return defaultInstanceTree;
        }
    }

    /**
     * Sets the value of the defaultInstanceTree property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDefaultInstanceTree(Boolean value) {
        this.defaultInstanceTree = value;
    }

    /**
     * Gets the value of the isReference property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isIsReference() {
        if (isReference == null) {
            return false;
        } else {
            return isReference;
        }
    }

    /**
     * Sets the value of the isReference property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsReference(Boolean value) {
        this.isReference = value;
    }

    /**
     * Gets the value of the displayLabelSource property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDisplayLabelSource() {
        if (displayLabelSource == null) {
            return "objectName";
        } else {
            return displayLabelSource;
        }
    }

    /**
     * Sets the value of the displayLabelSource property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDisplayLabelSource(String value) {
        this.displayLabelSource = value;
    }

    /**
     * Gets the value of the catalogObjectTypes property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the catalogObjectTypes property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCatalogObjectTypes().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getCatalogObjectTypes() {
        if (catalogObjectTypes == null) {
            catalogObjectTypes = new ArrayList<String>();
        }
        return this.catalogObjectTypes;
    }

}