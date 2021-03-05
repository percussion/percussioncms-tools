/******************************************************************************
 *
 * [ PSExtractorDef.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.loader.objectstore;

import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.loader.PSLoaderException;
import com.percussion.loader.PSLoaderUtils;
import com.percussion.xml.PSXmlTreeWalker;

import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Represents a PSExtractorDef xml object.
 */
public class PSExtractorDef extends PSLoaderNamedComponent
   implements java.io.Serializable
{
   /**
    * Constructs the object from the given parameters.
    *
    * @param name The value of the 'name' attribute, may not be
    *    <code>null</code> or empty.
    *
    * @param type The value of the 'type' attribute, may not be
    *    <code>null</code> or empty and may be either {@link #TYPE_STATIC} or
    *    {@link #TYPE_ITEM}. Defaults to {@link #TYPE_STATIC}.
    *
    * @param strClass The value of the 'Class' attribute, may not be
    *    <code>null</code> or empty.
    *
    * @throws IllegalArgumentException if any parameter is invalid.
    */
   public PSExtractorDef(String name, String type, String strClass)
   {
      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException(
            "name may not be null or empty");

      if (type == null || type.trim().length() == 0)
         throw new IllegalArgumentException(
            "type may not be null or empty");

      if (strClass == null || strClass.trim().length() == 0)
         throw new IllegalArgumentException(
            "strClass may not be null or empty");

      if (type == null)
         throw new IllegalArgumentException(
            "type may not be null");

      if (type.equalsIgnoreCase(TYPE_ITEM))
         m_strType = TYPE_ITEM;
      else
         m_strType = TYPE_STATIC;

      m_name = name;
      m_strClass = strClass;
   }

   /**
    * Create this object from its XML representation
    *
    * @param source The source element.  See {@link #toXml(Document)} for
    * the expected format.  May not be <code>null</code>.
    *
    * @throws IllegalArgumentException If <code>source</code> is
    * <code>null</code>.
    * @throws PSUnknownNodeTypeException <code>source</code> is malformed.
    */
   public PSExtractorDef(Element source)
      throws PSUnknownNodeTypeException, PSLoaderException
   {
      if (source == null)
         throw new IllegalArgumentException("source may not be null");

      fromXml(source);
   }

   /**
    * Get the plugin class name of the extractor
    *
    * @return The plugin class name of the extractor, never <code>null</code>
    *    or empty.
    */
   public String getPlugInClass()
   {
      return m_strClass;
   }

   /**
    * Get the value of the 'name' attribute.
    *
    * @return The value of the 'name' attribute,
    *    never <code>null</code> or empty.
    */
   public String getName()
   {
      return m_name;
   }
   
   /**
    * Set the name of the extractor definition.
    * 
    * @param name The to be set new name, it may not be <code>null</code> or
    *    empty.
    */
   public void setName(String name)
   {
      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name may not be null");
         
      m_name = name;
   }

   /**
    * Get the value of the 'type' attribute.
    *
    * @return The value of the 'type' attribute,
    *    never <code>null</code> or empty. Always
    *    either 'staticItem' or 'item' defaulting
    *    to 'staticItem'.
    */
   public String getType()
   {
      return m_strType;
   }

   /**
    * Determines whether it is an static type.
    *
    * @return <code> if it is an static type; <code>false</code> otherwise.
    */
   public boolean isStaticType()
   {
      return m_strType.equals(TYPE_STATIC);
   }

   /**
    * Get the list of PSProperty objects.
    *
    * @return An iterator over one or more <code>PSProperty</code>
    *    objects, never <code>null</code>, but may be empty.
    */
   public Iterator getProperties()
   {
      return m_properties.getComponents();
   }

  /**
   * Get the property of the content type. This method should only be used for 
   * item extractor definition, not for static extractor definition.
   *
   * @return The property of the content type, never <code>null</code> or empty.
   *
   * @throws PSLoaderException if cannot find property of
   *    {@link 
   *    com.percussion.loader.objectstore.PSItemExtractorDef#CONTENT_TYPE_NAME}.
   *    
   * @throws IllegalStateException if it is a static extractor definition.
   */
   public PSProperty getContentTypeProperty()
      throws PSLoaderException
   {
      if (isStaticType())
         throw new IllegalStateException(
            "Static extractor definition may not have content type");
      
      return getProperty(PSItemExtractorDef.CONTENT_TYPE_NAME);
   }
   
  /**
   * Convenience method, calls {@link #getContentTypeProperty()
   * getContentTypeProperty().getValue()}
   */
   public String getContentTypeName() 
      throws PSLoaderException
   {
      return getContentTypeProperty().getValue();
   }


   /**
    * Get the list of Field Property objects.
    *
    * @return An iterator over one or more <code>PSFieldProperty</code>
    *    objects, never <code>null</code>, but may be empty.
    */
   public Iterator getFieldProperties()
   {
      return m_fieldProperties.getComponents();
   }

   /**
    * Set field properties to the supplied list.
    * 
    * @param fields The new set of field properties, an iterator over
    *    zero or more <code>PSFieldProperty</code> objects. It may not be 
    *    <code>null</code>.
    */
   public void setFieldProperties(Iterator fields)
   {
      m_fieldProperties.clear();
      m_fieldProperties.addComponents(fields);
   }
   
   /**
    * Add a property.
    *
    * @param comp the to be added property, may not <code>null</code>.
    *
    * @throws IllegalArgumentException If <code>comp</code> is
    *    <code>null</code>.
    */
   public void addProperty(PSProperty comp)
   {
      if (comp == null)
         throw new IllegalArgumentException("comp may not be null");

      m_properties.addComponent(comp);
   }

   /**
    * Add a field property.
    *
    * @param fieldProperty The to be added field property, may not be 
    *    <code>null</code>.
    */
   public void addFieldProperty(PSFieldProperty fieldProperty)
   {
      if (fieldProperty == null)
         throw new IllegalArgumentException("fieldProperty may not be null");

      m_fieldProperties.addComponent(fieldProperty);
   }

   /**
    * Get the list of <code>PSMimeTypeDef</code> objects.
    *
    * @return An iterator over one or more <code>PSMimeTypeDef</code>
    *    objects, never <code>null</code>, but may be empty.
    */
   public Iterator getMimeTypes()
   {
      return m_mimeTypes.getComponents();
   }

   /**
    * Get the list of <code>PSMimeTypeDef</code> objects.
    *
    * @return A list of <code>PSMimeTypeDef</code> objects,
    *    never <code>null</code>, but may be empty.
    */
   public List getMimeTypeList()
   {
      return m_mimeTypes.getComponentList();
   }

   /**
    * Add a PSMimeTypeDef.
    *
    * @param comp the to be added mime type,  may not <code>null</code>.
    *
    * @throws IllegalArgumentException If <code>comp</code> is
    *    <code>null</code>.
    */
   public void addMimeType(PSMimeTypeDef comp)
   {
      if (comp == null)
         throw new IllegalArgumentException("comp may not be null");

      m_mimeTypes.addComponent(comp);
   }

   /**
    * Get the list of PSFilter objects.
    *
    * @return An iterator over one or more <code>PSFilter</code>
    *    objects, never <code>null</code>, but may be empty.
    */
   public Iterator getFilters()
   {
      return m_filters.getComponents();
   }

   /**
    * Add a PSFilter.
    *
    * @param comp the to be added filter, may not <code>null</code>.
    *
    * @throws IllegalArgumentException If <code>comp</code> is
    *    <code>null</code>.
    */
   public void addFilter(PSFilter comp)
   {
      if (comp == null)
         throw new IllegalArgumentException("comp may not be null");

      m_filters.addComponent(comp);
   }

   /**
    * Get the filter list.
    *
    * @return The filter list, never <code>null</code>, may be empty.
    */
   public PSComponentList getFiltersList()
   {
      return m_filters;
   }

   /**
    * Get the list of PSTransformationDef objects.
    *
    * @return An iterator over one or more <code>PSTransformationDef</code>
    *    objects, never <code>null</code>, but may be empty.
    */
   public Iterator getFieldTransformations()
   {
      return m_fieldTrans.getTransformations();
   }

   /**
    * Get field transformation list.
    *
    * @return The field transformation list, never <code>null</code>, but may
    *    be empty.
    */
   public PSComponentList getFieldTransformationsList()
   {
      return m_fieldTrans.getTransformationsList();
   }

   /**
    * Add a transformation definition.
    *
    * @param comp the to be added transformation definition, it may not be
    *    <code>null</code>.
    *
    * @param bItem A boolean, if <code>true</code> then add this
    *    transformation to the items list, otherwise the field
    *    list.
    *
    * @throws IllegalArgumentException If <code>comp</code> is
    *    <code>null</code>.
    */
   public void addTransformaton(PSTransformationDef comp, boolean bItem)
   {
      if (comp == null)
         throw new IllegalArgumentException("comp may not be null");

      if (bItem)
         m_itemTrans.addTransformation(comp);
      else
         m_fieldTrans.addTransformation(comp);
   }

   /**
    * Adds a new filter to the list of filters.
    *
    * @param name name of the filter, may not be <code>null</code>.
    * @param value value of the filter, may not be <code>null</code>.
    */
   public void addFilters(String name, String value)
   {
      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name may not be null or empty");
      if (value == null || value.trim().length() == 0)
         throw new IllegalArgumentException("value may not be null or empty");
      addFilter(new PSFilter(name, value));
   }

   /**
    * Set the workflow definition.
    * 
    * @param workflowDef The to be set workflow definition, it may be 
    *    <code>null</code>.
    */
   public void setWorkflowDef(PSWorkflowDef workflowDef)
   {
      m_workflowDef = workflowDef;
   }
   
   /**
    * Remove all existing filters;
    */
   public void clearFilters()
   {
      m_filters.clear();
   }

   /**
    * Get the list of PSTransformationDef objects.
    *
    * @return An iterator over one or more <code>PSTransformationDef</code>
    *    objects, never <code>null</code>, but may be empty.
    */
   public Iterator getItemTransformations()
   {
      return m_itemTrans.getTransformations();
   }

   /**
    * Get the item transformation list.
    *
    * @return The item transformation list, never <code>null</code>, but may
    *    be empty.
    */
   public PSComponentList getItemTransformationsList()
   {
      return m_itemTrans.getTransformationsList();
   }

   /**
    * Get the workflow definition.
    *
    * @return The workflow definition. It may be <code>null</code>.
    */
   public PSWorkflowDef getWorkflowDef()
   {
      return m_workflowDef;
   }

   /**
    * Serializes this object's state to its XML representation.
    * The format is:
    * <pre><code>
    * &lt;!ELEMENT PSXExtractorDef (FieldProperties?, Properties?, MimeTypes?, 
    * Filters?, PSXFieldTransformationsDef?, PSXItemTransformationsDef?, 
    * PSXWorkflowDef?)&gt;
    * &lt;!ATTLIST Extractor
    * name CDATA #REQUIRED
    * type (staticItem | item) "staticItem"
    * class CDATA #REQUIRED
    * &gt;
    * </code></pre>
    *
    * @throws IllegalArgumentException If <code>doc</code>
    * is <code>null</code>
    *
    *
    * See {@link PSLoaderComponent#toXml(Document)} for more info.
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");

      Element root = doc.createElement(XML_NODE_NAME);
      root.setAttribute(XML_ATTR_NAME, m_name);
      root.setAttribute(XML_ATTR_TYPE, m_strType);
      root.setAttribute(XML_ATTR_CLASS, m_strClass);

      /**
       * Write out the component list objects below
       */

      if (!m_fieldProperties.isEmpty())
         root.appendChild(m_fieldProperties.toXml(doc));

      if (!m_properties.isEmpty())
         root.appendChild(m_properties.toXml(doc));

      if (!m_mimeTypes.isEmpty())
         root.appendChild(m_mimeTypes.toXml(doc));

      if (!m_filters.isEmpty())
         root.appendChild(m_filters.toXml(doc));

      if (!m_fieldTrans.isEmpty())
         root.appendChild(m_fieldTrans.toXml(doc));

      if (!m_itemTrans.isEmpty())
         root.appendChild(m_itemTrans.toXml(doc));

      if (m_workflowDef != null)
         root.appendChild(m_workflowDef.toXml(doc));

      return root;
   }

   /**
    * @see PSLoaderComponent#fromXml(Element)
    */
   public void fromXml(Element sourceNode)
      throws PSUnknownNodeTypeException, PSLoaderException
   {
      validateElement(sourceNode, XML_NODE_NAME);

      m_name = getRequiredAttribute(sourceNode, XML_ATTR_NAME);
      m_strType = getRequiredAttribute(sourceNode, XML_ATTR_TYPE);
      m_strClass = getRequiredAttribute(sourceNode, XML_ATTR_CLASS);

      PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);
      Element listEl = tree.getNextElement(
         PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);

      // Load the component list objects
      while (listEl != null)
      {
         if (listEl.getNodeName().equals(XML_NODE_FIELDPROPERTIES))
         {
            m_fieldProperties.fromXml(listEl);
         }
         else if (listEl.getNodeName().equals(XML_NODE_PROPERTIES))
         {
            m_properties.fromXml(listEl);
         }
         else if (listEl.getNodeName().equals(XML_NODE_FILTERS))
         {
            m_filters.fromXml(listEl);
         }
         else if (listEl.getNodeName().equals(XML_NODE_MIMETYPES))
         {
            m_mimeTypes.fromXml(listEl);
         }
         else if (listEl.getNodeName().equals(
            PSFieldTransformationsDef.XML_NODE_NAME))
         {
            m_fieldTrans.fromXml(listEl);
         }
         else if (listEl.getNodeName().equals(
            PSItemTransformationsDef.XML_NODE_NAME))
         {
            m_itemTrans.fromXml(listEl);
         }
         else if (listEl.getNodeName().equals(PSWorkflowDef.XML_NODE_NAME))
         {
            m_workflowDef = new PSWorkflowDef(listEl);
         }

         // Keep loading next element
         listEl = tree.getNextElement(
            PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS);
      }
   }

   /**
    * PSLoaderComponent#hashCode()
    */
   public int hashCode()
   {
      return m_name.hashCode()
         + m_strClass.hashCode()
         + m_strType.hashCode()
         + m_properties.hashCode()
         + m_fieldProperties.hashCode()
         + m_mimeTypes.hashCode()
         + m_filters.hashCode()
         + m_fieldTrans.hashCode()
         + m_itemTrans.hashCode()
         + ((m_workflowDef == null) ? 0 : m_workflowDef.hashCode());
   }

   /**
    * @see PSLoaderComponent#equals(Object)
    */
   public boolean equals(Object obj)
   {
      if (!(obj instanceof PSExtractorDef))
         return false;

      PSExtractorDef obj2 = (PSExtractorDef) obj;

      return m_name.equals(obj2.m_name)
         && m_strClass.equals(obj2.m_strClass)
         && m_strType.equals(obj2.m_strType)
         && m_properties.equals(obj2.m_properties)
         && m_fieldProperties.equals(obj2.m_fieldProperties)
         && m_mimeTypes.equals(obj2.m_mimeTypes)
         && m_filters.equals(obj2.m_filters)
         && m_fieldTrans.equals(obj2.m_fieldTrans)
         && m_itemTrans.equals(obj2.m_itemTrans)
         && ((m_workflowDef == obj2.m_workflowDef) ||
             (m_workflowDef != null && 
              m_workflowDef.equals(obj2.m_workflowDef)));
   }

   /**
    * Copy construction.
    *
    * @param obj2 The to be copied or cloned object, must be an instance of
    *    <code>PSExtractorDef</code>.
    */
   public void copyFrom(PSExtractorDef obj2)
   {
      if (!(obj2 instanceof PSExtractorDef))
         throw new IllegalArgumentException(
            "obj2 must be an instance of PSExtractorDef");

      m_name = obj2.m_name;
      m_strClass = obj2.m_strClass;
      m_strType = obj2.m_strType;
      m_properties = obj2.m_properties;
      m_fieldProperties = obj2.m_fieldProperties;
      m_filters = obj2.m_filters;
      m_mimeTypes = obj2.m_mimeTypes;
      m_fieldTrans = obj2.m_fieldTrans;
      m_itemTrans = obj2.m_itemTrans;
      m_workflowDef = obj2.m_workflowDef;
   }


   /**
    * Get the field property of a supplied field name.
    * 
    * @param fieldName The (internal) field name, not the name of display or 
    *    label. It may not be <code>null</code>. It is case insensitive.
    *    
    * @return The field property, it may be <code>null</code> if not exist.
    */
   public PSFieldProperty getFieldProperty(String fieldName)
   {
      Iterator fields = getFieldProperties();
      while (fields.hasNext())
      {
         PSFieldProperty field = (PSFieldProperty)fields.next();
         if (field.getName().equalsIgnoreCase(fieldName))
            return field;
      }
      return null;
   }
   
   /**
    * Convenient method, get the value of the specified field name.
    * 
    * @param fieldName The (internal) field name, not the name of display or 
    *    label. It may not be <code>null</code>. It is case insensitive.
    *    
    * @return The field value, it may be <code>null</code> if not exist.
    */
   public String getFieldValue(String fieldName)
   {
      PSFieldProperty field = getFieldProperty(fieldName);
      if (field != null)
         return field.getValue();
      else
         return null;
   }
   
  /**
   * Get the value of a property.
   *
   * @return The name of the property, may not <code>null</code> or empty.
   *
   * @throws PSLoaderException if cannot find the property
   */
   public PSProperty getProperty(String name) throws PSLoaderException
   {
      return PSLoaderUtils.getProperty(name, getProperties());
   }

   /**
    * The XML node name of this object.
    */
   final public static String XML_NODE_NAME = "PSXExtractorDef";

   /**
    * The value of the 'Class' attribute, initialized in constructor,
    * Never <code>null</code>, and never empty.
    */
   protected String m_strClass;

   /**
    * The value of the 'name' attribute, initialized in constructor,
    * Never <code>null</code>, and never empty.
    */
   protected String m_name;

   /**
    * The value of the 'type' attribute, initialized in definition,
    * Never <code>null</code>, and never empty, and defaults to
    * {@link #TYPE_STATIC}.
    */
   protected String m_strType = TYPE_STATIC;

   /**
    * A list of properties, initialized in definition,
    * never <code>null</code>, may be empty.
    */
   protected PSComponentList m_properties = new PSComponentList(
      XML_NODE_PROPERTIES, PSProperty.XML_NODE_NAME, PSProperty.class);

   /**
    * A list of mime types, initialized in definition,
    * never <code>null</code>, may be empty.
    */
   protected PSComponentList m_mimeTypes = new PSComponentList(
      XML_NODE_MIMETYPES, PSMimeTypeDef.XML_NODE_NAME, PSMimeTypeDef.class);

   /**
    * A list of filters, initialized in definition,
    * never <code>null</code>, may be empty.
    */
   protected PSComponentList m_filters = new PSComponentList(
      XML_NODE_FILTERS, PSFilter.XML_NODE_NAME, PSFilter.class);

   /**
    * A list of transformations for fields, initialized in definition,
    * never <code>null</code>, may be empty.
    */
   protected PSFieldTransformationsDef m_fieldTrans =
      new PSFieldTransformationsDef();

   /**
    * A list of transformations for items, initialized in definition,
    * never <code>null</code>, may be empty.
    */
   protected PSItemTransformationsDef m_itemTrans =
      new PSItemTransformationsDef();

   /**
    * The workflow definition, it may be <code>null</code>.
    */
   protected PSWorkflowDef m_workflowDef = null;
   
   /**
    * A list of properties, initialized in definition,
    * never <code>null</code>, may be empty.
    */
   protected PSComponentList m_fieldProperties = new PSComponentList(
      XML_NODE_FIELDPROPERTIES, PSFieldProperty.XML_NODE_NAME, 
      PSFieldProperty.class);

      
   // Public constants for XML attribute default values
   final static public String TYPE_STATIC = "staticItem";
   final static public String TYPE_ITEM = "item";

   // Private constants for XML attribute and element name
   final static protected String XML_ATTR_NAME = "name";
   final static protected String XML_ATTR_TYPE = "type";
   final static protected String XML_ATTR_CLASS = "class";
}
