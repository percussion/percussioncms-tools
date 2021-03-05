/*[ PSLoaderDef.java ]*********************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader.objectstore;

import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.loader.PSLoaderException;
import com.percussion.loader.PSLoaderUtils;
import com.percussion.xml.PSXmlTreeWalker;

import java.util.Iterator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Represents a PSLoaderDef xml object.
 */
public class PSLoaderDef extends PSLoaderComponent 
   implements java.io.Serializable
{
   /**
    * Constructs the object from the given parameters.
    *
    * @param strName The value of the 'name' attribute, may not be
    *    <code>null</code> or empty.
    *
    * @param strClass The value of the 'class' attribute, may not be
    *    <code>null</code> or empty.
    *
    * @throws IllegalArgumentException if any parameter is invalid.
    */
   public PSLoaderDef(String strName, String strClass)
   {
      if (strName == null || strName.trim().length() == 0)
         throw new IllegalArgumentException(
            "strName may not be null or empty");

      if (strClass == null || strClass.trim().length() == 0)
         throw new IllegalArgumentException(
            "strClass may not be null or empty");

      m_strName = strName;
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
   public PSLoaderDef(Element source)
      throws PSUnknownNodeTypeException, PSLoaderException
   {
      if (source == null)
         throw new IllegalArgumentException("source may not be null");

      fromXml(source);
   }

   /**
    * Get the value of the <code>XML_ATT_NAME</code> attribute.
    *
    * @return The value of the <code>XML_ATT_NAME</code> attribute,
    *    never <code>null</code> or empty.
    */
   public String getName()
   {
      return m_strName;
   }

   /**
    * Get the value of the <code>XML_ATT_CLASS</code> attribute.
    *
    * @return The value of the <code>XML_ATT_CLASS</code> attribute,
    *    never <code>null</code> or empty.
    */
   public String getPlugInClass()
   {
      return m_strClass;
   }

   /**
    * Get the list of PSProperty objects.
    *
    * @return An iterator over one or more <code>PSPropertyDef</code>
    *    objects, never <code>null</code>, but may be empty.
    */
   public Iterator getProperties()
   {
      return m_properties.getComponents();
   }

   /**
    * Add a property.
    *
    * @param comp The {@link PSProperty} to be added,
    *    may not <code>null</code>.
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
    * Serializes this object's state to its XML representation.
    * The format is:
    * &lt;!ELEMENT Loader (Properties?, User?, Password?)&gt;
    * &lt;!ATTLIST Loader
    * name CDATA #REQUIRED
    * class CDATA #REQUIRED
    * &gt;
    * &lt;!--
    * A user name.
    * --&gt;
    * &lt;!ELEMENT User (#PCDATA)&gt;
    * &lt;!--
    * A password.
    * encrypted - specifies whther or not the password is encrypted.
    * --&gt;
    * &lt;!ELEMENT Password (#PCDATA)&gt;
    * &lt;!ATTLIST Password
    * encrypted (yes | no) "yes"
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
      root.setAttribute(XML_ATTR_NAME, m_strName);
      root.setAttribute(XML_ATTR_CLASS, m_strClass);

      /**
       * Write out the component list objects below
       */

     if (!m_properties.isEmpty())
         root.appendChild(m_properties.toXml(doc));

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
      m_strClass = getRequiredAttribute(sourceNode, XML_ATTR_CLASS);

      PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);
      Element listEl = tree.getNextElement(
         PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);

      // Load the component list objects
      if ((listEl != null) &&
          (listEl.getNodeName().equals(XML_NODE_PROPERTIES)))
      {
            m_properties.fromXml(listEl);
      }
   }

   /**
    * PSLoaderComponent#hashCode()
    */
   public int hashCode()
   {
      return m_strName.hashCode()
         + m_strClass.hashCode()
         + m_properties.hashCode();
   }

   /**
    * @see PSLoaderComponent#equals(Object)
    */
   public boolean equals(Object obj)
   {
      if (obj == null || !(obj instanceof PSLoaderDef))
         return false;

      PSLoaderDef obj2 = (PSLoaderDef) obj;

      return m_strName.equals(obj2.m_strName)
         && m_strClass.equals(obj2.m_strClass)
         && m_properties.equals(obj2.m_properties);
   }

   /**
    * Copy construction.
    *
    * @param obj2 The to be copied or cloned object, must be an instance of
    *    <code>PSLoaderDef</code>.
    */
   public void copyFrom(PSLoaderDef obj2)
   {
      if (!(obj2 instanceof PSLoaderDef))
         throw new IllegalArgumentException(
            "obj2 must be an instance of PSLoaderDef");

      m_strName = obj2.m_strName;
      m_strClass = obj2.m_strClass;
      m_properties = obj2.m_properties;
   }

  /**
   * Get the value of a property.
   *
   * @return The name of the property, may not <code>null</code> or empty.
   *
   * @throws PSLoaderException if cannot find the property
   */
   protected PSProperty getProperty(String name) throws PSLoaderException
   {
      return PSLoaderUtils.getProperty(name, getProperties());
   }


   /**
    * The XML node name of this object.
    */
   final public static String XML_NODE_NAME = "PSXLoaderDef";


   /**
    * The value of the <code>XML_ATTR_NAME</code>
    * attribute, initialized in constructor,
    * Never <code>null</code>, and never empty.
    */
   protected String m_strName;

   /**
    * The value of the <code>XML_ATTR_CLASS</code>
    * attribute, initialized in constructor,
    * Never <code>null</code>, and never empty.
    */
   protected String m_strClass;

   /**
    * A list of actions, initialized in definition,
    * never <code>null</code>, may be empty.
    */
   protected PSComponentList m_properties = new PSComponentList(
      XML_NODE_PROPERTIES, PSProperty.XML_NODE_NAME, PSProperty.class);

   // Private constants for XML attribute and element name
   final static protected String XML_ATTR_NAME = "name";
   final static protected String XML_ATTR_CLASS = "class";
}
