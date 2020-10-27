/*[ PSSearchRoot.java ]********************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader.objectstore;

import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.loader.PSLoaderException;
import com.percussion.xml.PSXmlTreeWalker;

import java.util.Iterator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Encapsulates a generic search root definition
 */
public class PSSearchRoot extends PSLoaderComponent
   implements java.io.Serializable
{
   /**
    * Constructs the object from a given name.
    *
    * @param name The name of the object, may not be <code>null</code>
    *    or empty.
    *
    * @throws IllegalArgumentException if any parameter is invalid.
    */
   public PSSearchRoot(String name)
   {
      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name may not be null or empty");

      m_name = name;
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
    * @trhows PSLoaderException if any other error occurs.
    */
   public PSSearchRoot(Element source)
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
      return m_name;
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
    * Get a specified property.
    *
    * @param name The name of the specified property, may not
    *    <code>null</code> or empty.
    *
    * @return The specified property object; <code>null</code> if cannot find
    *    one.
    */
   public PSProperty getProperty(String name)
   {
      if (name == null || name.trim().length() == 0)
         throw new IllegalStateException("name may not be null or empty");

      return (PSProperty) getNameValuePair(name, getProperties());
   }

   /**
    * Get a specified filter.
    *
    * @param name The name of the specified filter, may not
    *    <code>null</code> or empty.
    *
    * @return The specified filter object; <code>null</code> if cannot find
    *    one.
    */
   public PSFilter getFilter(String name)
   {
      if (name == null || name.trim().length() == 0)
         throw new IllegalStateException("name may not be null or empty");

      return (PSFilter) getNameValuePair(name, getFilters());
   }

   /**
    * Get a specified name-value pair object.
    *
    * @param name The name of the specified object, assume not
    *    <code>null</code> or empty.
    *
    * @return The specified pair object; <code>null</code> if cannot find
    *    one.
    */
   private PSNameValuePair getNameValuePair(String name, Iterator list)
   {
      while (list.hasNext())
      {
         PSNameValuePair pair = (PSNameValuePair)list.next();
         if (pair.getName().equals(name))
            return pair;
      }
      return null;
   }

   /**
    * Adds a new filter to the list of filters.
    *
    * @param name, name of the filter, may not be <code>null</code>.
    * @param value, value of the filter, may not be <code>null</code>.
    *
    * @throws IllegalArgumentExceptiom if the arguments are invalid.
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
    * Set value for an existing property.
    *
    * @param name The name of the property, may not <code>null</code> or empty,
    *    the property must be exist.
    * @param value The to be set value.
    *
    * @throws IllegalArgumentException If <code>name</code> is invalid.
    */
   public void setProperty(String name, String value)
   {
      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name may not be null or empty");

      PSProperty prop = getProperty(name);
      if ( prop == null)
         throw new IllegalArgumentException("property not found, name: " +name);

       prop.setValue(value);
   }

   /**
    * Set value for an existing filter.
    *
    * @param name The name of the filter, may not <code>null</code> or empty,
    *    the filter must be exist.
    * @param value The to be set value.
    *
    * @throws IllegalArgumentException If <code>name</code> is invalid.
    */
   public void setFilter(String name, String value)
   {
      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name may not be null or empty");

      PSFilter filter = getFilter(name);
      if ( filter == null)
         throw new IllegalArgumentException("filter not found, name: " + name);

       filter.setValue(value);
   }

   /**
    * Clear or remove all properties;
    */
   public void clearProperties()
   {
      m_properties.clear();
   }

   /**
    * Adds a property to property's list.
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
    * Adds a filter to filter's list.
    *
    * @param filter The to be added filter, may not <code>null</code>
    *
    * @throws IllegalArgumentException If <code>filter</code> is
    * <code>null</code>.
    */
   public void addFilter(PSFilter filter)
   {
      if (filter == null)
         throw new IllegalArgumentException("filter may not be null");

      m_filters.addComponent(filter);
   }

   /**
    * Get the list of filters.
    *
    * @return An iterator over zero or more <code>PSFilter</code>
    * objects, never <code>null</code>, but may be empty.
    */
   public Iterator getFilters()
   {
      return m_filters.getComponents();
   }

   /**
    * Clear or remove all filters;
    */
   public void clearFilters()
   {
      m_filters.clear();
   }

   /**
    * Serializes this object's state to its XML representation.  The format is:
    * <pre><code>
    * &lt;!ELEMENT PSXSearchRoot (Propertes?, PSXFilters?)>
    * &lt;!ATTLIST PSXSearchRoot
    *     name CDATA #REQUIRED
    * >
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

      if (! m_properties.isEmpty())
         root.appendChild(m_properties.toXml(doc));

      if (! m_filters.isEmpty())
         root.appendChild(m_filters.toXml(doc));

      return root;
   }

   // see PSLoaderComponent#fromXml(Element)
   public void fromXml(Element sourceNode)
      throws PSUnknownNodeTypeException, PSLoaderException
   {
      validateElement(sourceNode, XML_NODE_NAME);

      m_name = getRequiredAttribute(sourceNode, XML_ATTR_NAME);

      PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);
      Element compEl;
      compEl = tree.getNextElement(PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
      if (compEl != null)
      {
         if (compEl.getNodeName().equals(XML_NODE_PROPERTIES))
         {
            m_properties.fromXml(compEl);

            compEl = tree.getNextElement(
               PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS);
            if (compEl != null && compEl.getNodeName().equals(XML_NODE_FILTERS))
               m_filters.fromXml(compEl);
         }
      }
   }

   // see PSLoaderComponent#hashCode()
   public int hashCode()
   {
      return m_name.hashCode() + m_properties.hashCode() + m_filters.hashCode();
   }

   // see PSLoaderComponent#equals(Object)
   public boolean equals(Object obj)
   {
      if ((obj instanceof PSSearchRoot))
      {
         PSSearchRoot obj2 = (PSSearchRoot) obj;

         return m_name.equals(obj2.m_name) &&
             m_properties.equals(obj2.m_properties) &&
             m_filters.equals(obj2.m_filters);
      }
      else
      {
         return false;
      }
   }

   /**
    * Shallow copy or clone a given search root object
    *
    * @param obj2 The to be copied or cloned object, must be an instance of
    *    <code>PSSearchRoot</code>.
    */
   public void copyFrom(PSSearchRoot obj2)
   {
      if (! (obj2 instanceof PSSearchRoot))
         throw new IllegalArgumentException(
            "obj2 must be an instance of PSSearchRoot");

      m_name  = obj2.m_name;
      m_properties  = obj2.m_properties;
      m_filters = obj2.m_filters;
   }

   /**
    * The XML node name of the component list, initialized by constructor,
    * never <code>null</code> or empty after that.
    */
   final static public String XML_NODE_NAME = "PSXSearchRoot";

   /**
    * The name of the component list, initialized by constructor, never
    * <code>null</code> or empty after that.
    */
   private String m_name;

   /**
    * A list of properties, never <code>null</code>, but may be empty.
    */
   private PSComponentList m_properties = new PSComponentList(
      XML_NODE_PROPERTIES, PSProperty.XML_NODE_NAME, PSProperty.class);

   /**
    * A list of filters, never <code>null</code>, but may be empty.
    */
   private PSComponentList m_filters = new PSComponentList(XML_NODE_FILTERS,
         PSFilter.XML_NODE_NAME, PSFilter.class);

   // Private constants for XML attribute and element name
   final static private String XML_ATTR_NAME = "name";
}
