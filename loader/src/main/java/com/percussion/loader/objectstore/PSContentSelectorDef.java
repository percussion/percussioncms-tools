/*[ PSContentSelectorDef.java ]************************************************
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
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Encapsulates a content selector definition
 */
public class PSContentSelectorDef extends PSLoaderComponent
   implements java.io.Serializable
{
   /**
    * Constructs the object from a given name and its class name.
    *
    * @param name The name of the object, may not be <code>null</code>
    *    or empty.
    * @param pluginClass A fully qualified class name of the specified
    *    plugin selector, may not be <code>null</code> or empty.
    *
    * @throws IllegalArgumentException if any parameter is invalid.
    */
   public PSContentSelectorDef(String name, String pluginClass)
   {
      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name may not be null or empty");
      if (pluginClass == null || pluginClass.trim().length() == 0)
         throw new IllegalArgumentException(
            "pluginClass may not be null or empty");

      m_name = name;
      m_pluginClass = pluginClass;
      setSearchRootList();
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
   public PSContentSelectorDef(Element source)
      throws PSUnknownNodeTypeException, PSLoaderException
   {
      if (source == null)
         throw new IllegalArgumentException("source may not be null");

      setSearchRootList();
      fromXml(source);
   }

   /**
    * Get the name of the object.
    *
    * @return The name of the object, never <code>null</code> or empty.
    */
   public String getName()
   {
      return m_name;
   }

   /**
    * Get the plugin class name of the selector
    *
    * @return The plugin class name of the selector, never <code>null</code>
    *    or empty.
    */
   public String getPlugInClass()
   {
      return m_pluginClass;
   }

   /**
    * Get the list of search roots.
    *
    * @return An iterator over zero or more <code>PSSearchRoot</code>
    * objects, never <code>null</code>, but may be empty.
    */
   public Iterator getSearchRoots()
   {
      return m_searchRoots.getComponents();
   }

   /**
    * Get the list of search roots.
    *
    * @return A List of zero or more <code>PSSearchRoot</code>
    * objects, never <code>null</code>, but may be empty.
    */
   public List getSearchRootList()
   {
      return m_searchRoots.getComponentList();
   }

   /**
    * Adds a search root to the list of search roots.
    *
    * @param searchRoot The to be added search root, may not <code>null</code>
    *
    * @throws IllegalArgumentException If <code>searchRoot</code> is
    * <code>null</code>.
    */
   public void addSearchRoot(PSSearchRoot searchRoot)
   {
      if (searchRoot == null)
         throw new IllegalArgumentException("searchRoot may not be null");

      m_searchRoots.addComponent(searchRoot);
   }

   /**
    * Get the list of properties.
    *
    * @return An iterator over zero or more <code>PSProperty</code>
    * objects, never <code>null</code>, but may be empty.
    */
   public Iterator getProperties()
   {
      return m_properties.getComponents();
   }

   /**
    * Determines whether the checksum will be calculated by this selector.
    * 
    * @return <code>true</code> if the selector will calculate the checksum
    *    for each scanned item (or file).
    */
   public boolean calcChecksum()
   {
      PSProperty prop = PSLoaderUtils.getOptionalProperty(CALC_CHECKSUM, 
         getProperties());
      
      if (prop != null)
         return prop.getValue().equalsIgnoreCase(XML_TRUE);
      else
         return false;
   }
   
   /**
    * Adds a property to the list of properties.
    *
    * @param property The to be added property, may not <code>null</code>
    *
    * @throws IllegalArgumentException If <code>property</code> is
    * <code>null</code>.
    */
   public void addProperty(PSProperty property)
   {
      if (property == null)
         throw new IllegalArgumentException("property may not be null");

      m_properties.addComponent(property);
   }

   /**
    * Serializes this object's state to its XML representation.  The format is:
    * <pre><code>
    * &lt;!ELEMENT PSXContentSelectorDef (Properties?, PSXSearchRoots?)>
    * &lt;!ATTLIST PSXContentSelectorDef
    *     name CDATA #REQUIRED
    *     class CDATA #REQUIRED
    * >
    * &lt;!ELEMENT Properties (PSXProperty+)>
    * &lt;!ELEMENT PSXSearchRoots (PSXSearchRoot+)>
    * </code></pre>
    *
    * @throws IllegalArgumentException If <code>doc</code> is <code>null</code>
    * @throws IllegalStateException if <code>m_searchRoots</code> is empty.
    *
    * See {@link PSLoaderComponent#toXml(Document)} for more info.
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");

      Element root = doc.createElement(XML_NODE_NAME);
      root.setAttribute(XML_ATTR_NAME, m_name);
      root.setAttribute(XML_ATTR_CLASS, m_pluginClass);

      if (! m_properties.isEmpty())
         root.appendChild(m_properties.toXml(doc));
      if (! m_searchRoots.isEmpty())
         root.appendChild(m_searchRoots.toXml(doc));

      return root;
   }

   // see PSLoaderComponent#fromXml(Element)
   public void fromXml(Element sourceNode)
      throws PSUnknownNodeTypeException, PSLoaderException
   {
      validateElement(sourceNode, XML_NODE_NAME);

      m_name = getRequiredAttribute(sourceNode, XML_ATTR_NAME);
      m_pluginClass = getRequiredAttribute(sourceNode, XML_ATTR_CLASS);

      PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);
      Element listEl;
      listEl = tree.getNextElement(PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
      if (listEl != null)
      {
         // get the properties
         if ( listEl.getNodeName().equals(m_properties.getXmlNodeName()) )
         {
            m_properties.fromXml(listEl);

            // get next possible search roots
            listEl = tree.getNextElement(
               PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS);
         }

         // get the search roots
         if (listEl != null &&
             listEl.getNodeName().equals(m_searchRoots.getXmlNodeName()))
         {
            m_searchRoots.fromXml(listEl);
         }
      }
   }

   // see PSLoaderComponent#hashCode()
   public int hashCode()
   {
      return m_name.hashCode() + m_pluginClass.hashCode() +
             m_searchRoots.hashCode() + m_properties.hashCode();
   }

   // see PSLoaderComponent#equals(Object)
   public boolean equals(Object obj)
   {
      if ( obj == null || (! (obj instanceof PSContentSelectorDef)) )
      {
         return false;
      }
      else
      {
         PSContentSelectorDef obj2 = (PSContentSelectorDef) obj;

         return m_name.equals(obj2.m_name) &&
                m_pluginClass.equals(obj2.m_pluginClass) &&
                m_searchRoots.equals(obj2.m_searchRoots) &&
                m_properties.equals(obj2.m_properties);
      }
   }
   
   /**
    * Set the search root list, may be override by derived classes if needed
    */
   protected void setSearchRootList()
   {
      m_searchRoots = new PSComponentList(
      XML_NODE_SEARCHROOTS, PSSearchRoot.XML_NODE_NAME, PSSearchRoot.class);
   }

   /**
    * The XML node name of the component list, initialized by constructor,
    * never <code>null</code> or empty after that.
    */
   public final static String XML_NODE_NAME = "PSXContentSelectorDef";

   /**
    * The property name for calculating checksum. The value of it may be
    * "yes" or "no". Set this property value to "yes" will greatly affect
    * the scanning process, which should not be needed for migrating content.
    */
   final public static String CALC_CHECKSUM = "Calculate checksum";

   /**
    * The name of the component list, initialized by constructor, never
    * <code>null</code> or empty after that.
    */
   private String m_name;

   /**
    * The fully qualified class name of the plugin content selector,
    * initialized by constructor, never <code>null</code> or empty after that.
    */
   private String m_pluginClass;

   /**
    * A list of search roots, indirectly initialized by constructor
    * (through <code>setComponent</code>), never <code>null</code>, but may be
    * empty after that.
    */
   protected PSComponentList m_searchRoots;

   /**
    * A list of properties, never <code>null</code>, but may be empty.
    */
   protected PSComponentList m_properties = new PSComponentList(
      XML_NODE_PROPERTIES, PSProperty.XML_NODE_NAME, PSProperty.class);


   // Private constants for XML attribute and element name
   final static protected String XML_ATTR_NAME = "name";
   final static protected String XML_ATTR_CLASS = "class";
   final static protected String XML_NODE_SEARCHROOTS = "PSXSearchRoots";
}
