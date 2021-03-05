/*[ PSNameValuePair.java ]*****************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader.objectstore;

import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * Abstract class to encapsulate a name-value pair
 */
public abstract class PSNameValuePair extends PSLoaderComponent
   implements java.io.Serializable
{
   /**
    * Constructs the object from a given name and value.
    *
    * @param name The name of the pair, may not be <code>null</code> or empty.
    * @param value The value of the pair, may not be <code>null</code> or empty.
    *
    * @throws IllegalArgumentException if any parameter is invalid.
    */
   public PSNameValuePair(String name, String value)
   {
      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name may not be null or empty");
      if (value == null || value.trim().length() == 0)
         throw new IllegalArgumentException("value may not be null or empty");

      m_name = name;
      m_value = value;
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
   public PSNameValuePair(Element source) throws PSUnknownNodeTypeException
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
    * @return The value of the object, may be <code>null</code> or empty.
    */
   public String getValue()
   {
      return m_value;
   }

   /**
    * Set the value.
    * 
    * @param value The to be set value, may be <code>null</code> or empty.
    */
   public void setValue(String value)
   {
      m_value = value;
   }
   
   /**
    * Serializes this object's state to its XML representation.  The format is:
    * <pre><code>
    * &lt;!ELEMENT PSXXXX (Value)>
    * &lt;!ATTLIST PSXXXX
    *     name CDATA #REQUIRED
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

      Element root = doc.createElement(getXmlNodeName());
      root.setAttribute(XML_ATTR_NAME, m_name);
      
      PSXmlDocumentBuilder.addElement(doc, root, XML_VALUE_NODE, m_value);      
      
      return root;
   }

   // see PSLoaderComponent#fromXml(Element)
   public void fromXml(Element sourceNode) throws PSUnknownNodeTypeException
   {
      validateElement(sourceNode, getXmlNodeName());

      m_name = getRequiredAttribute(sourceNode, XML_ATTR_NAME);

      PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);
      Element valueEl = getNextRequiredElement(tree, XML_VALUE_NODE, 
         PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
      m_value = tree.getElementData(valueEl);
   }

   // see PSLoaderComponent#hashCode()
   public int hashCode()
   {
      return m_name.hashCode() + ((m_value == null) ? 1 : m_value.hashCode());
   }

   // see PSLoaderComponent#equals(Object)
   public boolean equals(Object obj)
   {
      if (obj == null)
      {
         return false;
      }
      else
      {
         PSNameValuePair obj2 = (PSNameValuePair) obj;
   
         return m_name.equals(obj2.m_name) && m_value.equals(obj2.m_value);
      }
   }


   /**
    * Get the XML node name or tag 
    * 
    * @return The XML node name, never <code>null</code> or empty.
    */
   protected abstract String getXmlNodeName();

   /**
    * The name of the object, initialized by constructor, never
    * <code>null</code> or empty after that.
    */
   protected String m_name;

   /**
    * The value of the object, initialized by constructor, may be
    * <code>null</code> or empty after that.
    */
   protected String m_value;

   // Private constants for XML attribute and element name
   final private String XML_ATTR_NAME = "name";
   final private String XML_VALUE_NODE = "Value";
}
