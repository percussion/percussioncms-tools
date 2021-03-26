/*[ PSParamDef.java ]**********************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader.objectstore;

import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.loader.PSLoaderException;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Encapsulates a parameter definition.
 */
public class PSParamDef extends PSLoaderComponent 
   implements java.io.Serializable
{
   /**
    * Constructs the object from a given name and its given parameters.
    *
    * @param name The name of the object, may not be <code>null</code>
    *    or empty.
    * @param type the object type of the parameter, may not be
    *    <code>null</code> or empty.
    * @param description a string describing the parameter, may 
    *    not be <code>null</code>, but may be empty.
    *
    * @throws IllegalArgumentException if any parameter is invalid.
    */
   public PSParamDef(String name, String type, String description)
   {
      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name may not be null or empty");
      if (type == null || type.trim().length() == 0)
         throw new IllegalArgumentException(
               "Parameter type may not be null or empty");
      if (description == null)
         throw new IllegalArgumentException(
            "Description may not be null");
      
      m_strName = name;
      m_strType = type;
      m_strDescription = description;
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
   public PSParamDef(Element source)
      throws PSUnknownNodeTypeException, PSLoaderException
   {
      if (source == null)
         throw new IllegalArgumentException("source may not be null");

      fromXml(source);
   }

   /**
    * Get the name of the object.
    *
    * @return The name of the object, never <code>null</code> or empty.
    */
   public String getName()
   {
      return m_strName;
   }

   /**
    * Get the type of the parameter
    *
    * @return The type of the parameter, never <code>null</code> 
    *    or empty.
    */
   public String getType()
   {
      return m_strType;
   }

   /**
    * Get optional value of the parameter.
    * 
    * @return The value of the parameter, may be <code>null</code> if the
    *    parameter does not have a value.
    */
   public String getValue()
   {
      return m_value;
   }
   
   /**
    * Set the optional value of the object.
    * 
    * @param value The to be set value, may be <code>null</code> or empty.
    */
   public void setValue(String value)
   {
      m_value = value;
   }
   
   /**
    * Get the description.
    *
    * @return the string description of the parameter, 
    *    never <code>null</code>, may be empty.
    */
   public String getDescription()
   {
      return m_strDescription;
   }

   /**
    * Serializes this object's state to its XML representation.  The format is:
    * <pre><code>
    * <!ELEMENT PSParamDef (Description, Value?)>
    * <!ATTLIST PSParamDef
    *     name CDATA #REQUIRED
    *     type CDATA #REQUIRED
    * >
    * <!ELEMENT Description (#PCDATA)>
    * <!ELEMENT Value (#PCDATA)>
    * </code></pre>
    *
    * @throws IllegalArgumentException If <code>doc</code> is <code>null</code>
    *    
    * See {@link PSLoaderComponent#toXml(Document)} for more info.
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");

      Element root = doc.createElement(XML_NODE_NAME);
      root.setAttribute(XML_ATTR_NAME, m_strName);
      root.setAttribute(XML_ATTR_TYPE, m_strType);

      PSXmlDocumentBuilder.addElement(doc,
         root,
         XML_NODE_DESC,
         m_strDescription);
      
      if (m_value != null)
      {
         PSXmlDocumentBuilder.addElement(doc,
            root,
            XML_NODE_VALUE,
            m_value);
      }
      
      return root;
   }

   /**
    * @see PSLoaderComponent#fromXml(Element)
    */  
   public void fromXml(Element sourceNode)
      throws PSUnknownNodeTypeException, PSLoaderException
   {
      validateElement(sourceNode, XML_NODE_NAME);

      m_strName = getRequiredAttribute(sourceNode, XML_ATTR_NAME);
      m_strType = getRequiredAttribute(sourceNode, XML_ATTR_TYPE);

      PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);
      Element listEl;
      Element descEl = getNextRequiredElement(tree, XML_NODE_DESC, 
         PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
      m_strDescription = tree.getElementData(descEl);
      
      Element valueEl;
      valueEl = tree.getNextElement(PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS);
      if (valueEl != null)
      {
         if (! valueEl.getNodeName().equals(XML_NODE_VALUE))
         {
            Object[] args = { XML_NODE_VALUE, valueEl.getNodeName() };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
         }      
         m_value = tree.getElementData(valueEl);
      }
   }

   /**
    * PSLoaderComponent#hashCode()
    */
   public int hashCode()
   {
      return m_strName.hashCode() + m_strType.hashCode() +
             m_strDescription.hashCode() + 
             ((m_value == null) ? 0 : m_value.hashCode());
   }

   /**
    * @see PSLoaderComponent#equals(Object)
    */ 
   public boolean equals(Object obj)
   {
      if (obj == null)
         return false;

      PSParamDef obj2 = (PSParamDef) obj;

      if (m_value == null)
      {
         return m_strName.equals(obj2.m_strName) &&
                m_strType.equals(obj2.m_strType) &&
                m_strDescription.equals(obj2.m_strDescription); 
      }
      else
      {
         return m_strName.equals(obj2.m_strName) &&
                m_strType.equals(obj2.m_strType) &&
                m_strDescription.equals(obj2.m_strDescription) &&
                m_value.equals(obj2.m_value); 
      }
   }
   
   /**
    * The XML node name of the object, initialized by constructor,
    * never <code>null</code> or empty after that.
    */
   final public static String XML_NODE_NAME = "PSXParamDef";

   /**
    * The name value, initialized by constructor, never
    * <code>null</code> or empty after that.
    */
   private String m_strName;

   /**
    * The type value, initialized by constructor, never
    * <code>null</code> or empty after that.
    */
   private String m_strType;

   /**
    * The description value, initialized by constructor, never
    * <code>null</code> but may be empty after that.
    */
   private String m_strDescription;

   /**
    * The value of the object, it may be <code>null</code> if not set.
    */
   private String m_value = null;
   
   // Private constants for XML attribute and element name
   final static protected String XML_ATTR_NAME = "name";
   final static protected String XML_ATTR_TYPE = "type";
   final static protected String XML_NODE_DESC = "Description";
   final static protected String XML_NODE_VALUE = "Value";
}
