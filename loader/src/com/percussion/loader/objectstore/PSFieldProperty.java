/*[ PSFieldProperty.java ]*****************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.loader.objectstore;

import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.loader.IPSLoaderErrors;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class is used to encapsulate a field property, name and value pair
 */
public class PSFieldProperty extends PSLoaderComponent
{
   /**
    * Constructs the object from a given parameters.
    *
    * @name The name of the field. It may not be <code>null</code> or empty.
    *
    * @value The value of the specified field. It may be <code>null</code> or
    *    empty.
    *
    * @valueType The type of the specified value. It must be one of the values
    *    in <code>VALUE_TYPE_XXX</code>.
    */
   public PSFieldProperty(String name, String value, String valueType)
   {
      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name may not be null or empty");

      m_name = name;
      setValue(value);
      setValueType(valueType);
   }

   /**
    * Create this object from its XML representation
    *
    * @param source The source XML element, may not be <code>null</code>.
    */
   public PSFieldProperty(Element source) throws PSUnknownNodeTypeException
   {
      if (source == null)
         throw new IllegalArgumentException("source may not be null");

      fromXml(source);
   }

   /**
    * Get the name.
    *
    * @return The name of the object, never <code>null</code> or empty.
    */
   public String getName()
   {
      return m_name;
   }

   /**
    * Get the value.
    *
    * @return The value, never <code>null</code>, but may be empty.
    */
   public String getValue()
   {
      return m_value;
   }

   /**
    * Get the value type.
    *
    * @return The value type, one of the values in <code>VALUE_TYPE_XXX</code>.
    */
   public String getValueType()
   {
      return m_valueType;
   }

   /**
    * Set the value.
    *
    * @param value The to be set value, may be <code>null</code> or empty.
    */
   public void setValue(String value)
   {
      m_value = (value == null) ? "" : value;
   }

   /**
    * Set the value type.
    *
    * @param valueType The to be set value type. It must be one of the values
    *    in <code>VALUE_TYPE_XXX</code>.
    */
   public void setValueType(String valueType)
   {
      if (! isValidValueType(valueType))
         throw new IllegalArgumentException(
            "valueType must be one of the values in VALUE_TYPE_XXX");

      m_valueType = valueType;
   }

   /**
    * Serializes this object's state to its XML representation.  The format is:
    * <pre><code>
    * &lt;!ELEMENT PSXFieldProperty (Value)>
    * &lt;!ATTLIST PSXFieldProperty
    *     name CDATA #REQUIRED
    *     valueType CDATA #REQUIRED
    * >
    * &lt;!ELEMENT Value (#PCDATA)>
    * </code></pre>
    *
    * See {@link PSLoaderComponent#toXml(Document)} for more info.
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");

      Element root = doc.createElement(XML_NODE_NAME);
      root.setAttribute(XML_ATTR_NAME, m_name);
      root.setAttribute(XML_ATTR_VALUETYPE, m_valueType);

      PSXmlDocumentBuilder.addElement(doc, root, XML_VALUE_NODE, m_value);

      return root;
   }

   // see PSLoaderComponent#fromXml(Element)
   public void fromXml(Element sourceNode) throws PSUnknownNodeTypeException
   {
      validateElement(sourceNode, XML_NODE_NAME);

      m_name = getRequiredAttribute(sourceNode, XML_ATTR_NAME);
      String type = getRequiredAttribute(sourceNode, XML_ATTR_VALUETYPE);
      if (! isValidValueType(type))
         throw new PSUnknownNodeTypeException(
            IPSLoaderErrors.INVALID_VALUE_TYPE, type);

      m_valueType = type;

      PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);
      Element valueEl = getNextRequiredElement(tree, XML_VALUE_NODE,
         PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
      m_value = tree.getElementData(valueEl);
   }

   // see PSLoaderComponent#hashCode()
   public int hashCode()
   {
      return m_name.hashCode() + m_value.hashCode() + m_valueType.hashCode();
   }

   // see PSLoaderComponent#equals(Object)
   public boolean equals(Object obj)
   {
      if (! (obj instanceof PSFieldProperty))
         return false;

      PSFieldProperty obj2 = (PSFieldProperty) obj;

      return m_name.equals(obj2.m_name) &&
             m_value.equals(obj2.m_value) &&
             m_valueType.equals(obj2.m_valueType);
   }

   /**
    * Determines whether the specified type is a valid value type.
    *
    * @param valueType The to be checked value type. It may not be
    *    <code>null</code>.
    *
    * @return <code>true</code> if the specified value type is one of the
    *    values in <code>VALUE_TYPE_XXX</code>; otherwise return
    *    <code>false</code>.
    */
   public boolean isValidValueType(String valueType)
   {
      if (valueType == null)
         throw new IllegalArgumentException(
            "valueType may not be null or empty.");

      return (valueType.equalsIgnoreCase(VALUE_TYPE_LITERAL) ||
              valueType.equalsIgnoreCase(VALUE_TYPE_DATE) ||
              valueType.equalsIgnoreCase(VALUE_TYPE_NUMBER) ||
              valueType.equalsIgnoreCase(VALUE_TYPE_VARIABLE) ||
              valueType.equalsIgnoreCase(VALUE_TYPE_XPATH));
   }

   /**
    * The name of the field, initialized by constructor, never
    * <code>null</code> or empty after that.
    */
   private String m_name;

   /**
    * The value of the field, initialized by constructor, never
    * <code>null</code>, but may be empty after that.
    */
   private String m_value;

   /**
    * The type of the value. Initialized by ctor, never <code>null</code>
    * and must be one of the values in <code>VALUE_TYPE_XXX</code> after that.
    */
   private String m_valueType;

   // Private constants for XML attribute and element name
   final private String XML_ATTR_NAME = "name";
   final private String XML_ATTR_VALUETYPE = "valueType";
   final private String XML_VALUE_NODE = "Value";

   /**
    * Root node name of this object's XML representation.
    */
   public static final String XML_NODE_NAME = "PSXFieldProperty";

   /**
    * The constant for the literal type value
    */
   public static final String VALUE_TYPE_LITERAL = "Literal";

   /**
    * The constant for the date type value
    */
   public static final String VALUE_TYPE_DATE = "Date";

   /**
    * The constant for the number type value
    */
   public static final String VALUE_TYPE_NUMBER = "Number";

   /**
    * The constant for the (extractor) variable type value
    */
   public static final String VALUE_TYPE_VARIABLE = "Variable";

   /**
    * The constant for the XPath type value
    */
   public static final String VALUE_TYPE_XPATH = "XPath";

}
