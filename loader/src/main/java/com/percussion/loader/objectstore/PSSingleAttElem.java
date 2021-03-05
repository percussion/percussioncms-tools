/*[ PSSingleAttElem.java ]*****************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.loader.objectstore;

import com.percussion.design.objectstore.PSUnknownNodeTypeException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * The class to encapsulate a node of xml that has no children
 * and only contains a single attribute. 
 */
public abstract class PSSingleAttElem extends PSLoaderComponent
   implements java.io.Serializable
{
   /**
    * Constructs the object from the given parameters.
    * 
    * @param value the value of the 'name' attribute. Must not be
    *    <code>null</code> or empty.
    *
    * @throws IllegalArgumentException if any parameter is invalid.
    */
   public PSSingleAttElem(String strValue)
   {
      if (strValue == null || strValue.trim().length() == 0)
         throw new IllegalArgumentException(
            "strValue may not be null or empty");

      m_strAttValue = strValue;
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
   public PSSingleAttElem(Element source) throws PSUnknownNodeTypeException
   {
      if (source == null)
         throw new IllegalArgumentException("source may not be null");

      fromXml(source);
   }

   /**
    * Get the 'name' attribute value.
    *
    * @return The value of the object, may not be <code>null</code> or empty.
    */
   public String getAttValue()
   {
      return m_strAttValue;
   }

   /**
    * Serializes this object's state to its XML representation.  The format is:
    * <pre><code>
    * <!ELEMENT PSXXXX EMPTY>
    * <!ATTLIST PSXXXX
    *    name CDATA #REQUIRED
    * >
    * </code></pre>
    *
    * See {@link PSLoaderComponent#toXml(Document)} for more info.
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");

      Element root = doc.createElement(getXmlNodeName());
      root.setAttribute(XML_ATTR_NAME, m_strAttValue);
       
      return root;
   }

   /**
    * @see PSLoaderComponent#fromXml(Element)
    */
   public void fromXml(Element sourceNode) throws PSUnknownNodeTypeException
   {
      validateElement(sourceNode, getXmlNodeName());

      m_strAttValue = getRequiredAttribute(sourceNode, XML_ATTR_NAME);
   }

   /**
    * @see PSLoaderComponent#hashCode()
    */   
   public int hashCode()
   {
      return m_strAttValue.hashCode();
   }

   /**
    * @see PSLoaderComponent#equals(Object)
    */ 
   public boolean equals(Object obj)
   {
      if (obj == null)
         return false;

      PSSingleAttElem obj2 = (PSSingleAttElem) obj;

      return m_strAttValue.equals(obj2.m_strAttValue);
   }


   /**
    * Get the XML node name or tag 
    * 
    * @return The XML node name, never <code>null</code> or empty.
    */
   protected abstract String getXmlNodeName();

   /**
    * The value of the name attribute, initialized by constructor, may not be
    * <code>null</code> or empty.
    */
   protected String m_strAttValue;

   // Private constants for XML attribute and element name
   final private String XML_ATTR_NAME = "name";
}
