/*[ PSMimeTypeDef.java ]*******************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader.objectstore;

import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.loader.PSLoaderException;

import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Represents a {@link #PSComponentList} with a single 'name'
 * attribute. A <code>PSMimeTypeDef</code> contains a list of extensions
 * representing file types.
 *    A mime type is the unique identifier used for different file types
 * when conveyed across a MIME-based protocol such as MIME
 * e-mail or HTTP.
 */
public class PSMimeTypeDef extends PSLoaderComponent
   implements java.io.Serializable
{
   /**
    * Constructs the object from the following parameters.
    *
    * @param name The value of the 'name' attribute, may not be
    *    <code>null</code>
    *    or empty.
    *
    * @param ext An {@link #PSExtensionDef} to add. Never <code>null</code>.
    *
    * @throws IllegalArgumentException if any parameter is invalid.
    */
   public PSMimeTypeDef(String name, PSExtensionDef ext)
   {
      if (ext == null)
         throw new IllegalArgumentException("ext must not be null");

      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name may not be null or empty");

      m_strName = name;
      addExtension(ext);
   }

   /**
    *
    * @param name
    * @param extList
    */
   public PSMimeTypeDef(String name, List extList)
   {
      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name may not be null or empty");
      if (extList == null || extList.size() == 0)
         throw new IllegalArgumentException(
            "list of extensions cannot be null or empty");
      m_extensions.clear();
      int len = extList.size();
      for(int z = 0; z < len; z++)
         m_extensions.addComponent(new PSExtensionDef((String)extList.get(z)));
      m_strName = name;
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
   public PSMimeTypeDef(Element source)
      throws PSUnknownNodeTypeException, PSLoaderException
   {
      if (source == null)
         throw new IllegalArgumentException("source may not be null");

      fromXml(source);
   }

   /**
    * Get the value of the 'name' attribute.
    *
    * @return The value of the 'name' attribute,
    *    never <code>null</code> or empty.
    */
   public String getName()
   {
      return m_strName;
   }

   /**
    * Get the list of mime types.
    *
    * @return An iterator over one or more <code>PSExtensionDef</code>
    *    objects, never <code>null</code>, and will contain at least
    *    one mime type.
    */
   public Iterator getExtensions()
   {
      return m_extensions.getComponents();
   }


   /**
    * Accessor to set the component list of <code>PSExtensionDef</code>
    * objects <code>m_extensions</code>.
    *
    * @return {@link PSComponentList} of <code>PSExtensionDef</code> objects.
    * Never <code>null</code>, must have at least one <code>PSExtensionDef</
    * code> object.
    */
   public PSComponentList getExtensionsList()
   {
      return m_extensions;
   }

   /**
    * Mutator to set the component list of <code>PSExtensionDef</code>
    * objects <code>m_extensions</code>.
    *
    * @param list {@link #PSComponentList} of <code>PSExtensionDef</code>
    *    objects. Never <code>null</code>, must have at least one <code>
    *    PSExtensionDef</code> object.
    *
    * @throws IllegalArgumentException if <code>list</code> is <code>null
    *    </code> or if it is the wrong type of list.
    * @throws IllegalStateException if <code>list</code> is empty.
    */
   public void setExtensions(PSComponentList list)
   {
      if (list == null)
         throw new IllegalArgumentException("list must not be null");

      if (list.isEmpty())
         throw new IllegalStateException("list must not be empty");

      if (!(list.getXmlNodeName().equals(m_extensions.getXmlNodeName())))
         throw new IllegalArgumentException(
            "list must be a list of PSExtensionDef objects");

      m_extensions = list;
   }

   /**
    * Add an extension.
    *
    * @param ext The {@link PSExtensionDef} to be added,
    *    may not <code>null</code>.
    *
    * @throws IllegalArgumentException If <code>ext</code> is
    *    <code>null</code>.
    */
   public void addExtension(PSExtensionDef ext)
   {
      if (ext == null)
         throw new IllegalArgumentException("ext may not be null");

      m_extensions.addComponent(ext);
   }

   /**
    * Serializes this object's state to its XML representation.  The format is:
    * <pre><code>
    * &lt;!ELEMENT PSXMimeType (PSXExtension+)>
    * &lt;!ATTLIST PSXMimeType
    *     name CDATA #REQUIRED
    * >
    *
    * &lt;!ELEMENT PSXExtension EMPTY >
    * &lt;!ATTLIST PSXExtension
    *     name CDATA #REQUIRED
    * >
    * </code></pre>
    *
    * @throws IllegalArgumentException If <code>doc</code> is <code>null</code>
    *
    * @throws IllegalStateException If <code>m_extensions</code>
    * is empty.
    *
    * See {@link PSLoaderComponent#toXml(Document)} for more info.
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");

      if (m_extensions.isEmpty())
         throw new IllegalStateException(
            "extensions must contain at least one extension.");

      Element root = m_extensions.toXml(doc);
      root.setAttribute(XML_ATTR_NAME, m_strName);

      return root;
   }

   /**
    * @see PSLoaderComponent#fromXml(Element)
    */
   public void fromXml(Element sourceNode)
      throws PSUnknownNodeTypeException, PSLoaderException
   {
      validateElement(sourceNode, this.XML_NODE_NAME);
      m_strName = getRequiredAttribute(sourceNode, XML_ATTR_NAME);
      m_extensions.fromXml(sourceNode);
   }

   /**
    * PSLoaderComponent#hashCode()
    */
   public int hashCode()
   {
      return m_strName.hashCode() + m_extensions.hashCode();
   }

   /**
    * @see PSLoaderComponent#equals(Object)
    */
   public boolean equals(Object obj)
   {
      if (obj == null || !(obj instanceof PSMimeTypeDef))
         return false;

      PSMimeTypeDef obj2 = (PSMimeTypeDef) obj;

      return m_strName.equals(obj2.m_strName) &&
         m_extensions.equals(obj2.m_extensions);
   }

   /**
    * The XML node name of the component list, initialized by constructor,
    * never <code>null</code> or empty after that.
    */
   final public static String XML_NODE_NAME = "PSXMimeTypeDef";

   /**
    * The value of the 'name' attribute, initialized by constructor, never
    * <code>null</code> or empty after that.
    */
   private String m_strName;

   /**
    * A list of extensions, initialized in definition,
    * never <code>null</code>, after this must contains at least one
    * extension.
    */
   protected PSComponentList m_extensions = new PSComponentList(
      XML_NODE_NAME, PSExtensionDef.XML_NODE_NAME, PSExtensionDef.class);

   // Private constants for XML attribute and element name
   final static protected String XML_ATTR_NAME = "name";
}
