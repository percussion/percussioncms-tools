/*[ PSTransformationDef.java ]*************************************************
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
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Encapsulates a transformation definition.
 */
public class PSTransformationDef extends PSLoaderNamedComponent
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
   public PSTransformationDef(String name, String pluginClass)
   {
      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name may not be null or empty");
      if (pluginClass == null || pluginClass.trim().length() == 0)
         throw new IllegalArgumentException(
            "pluginClass may not be null or empty");

      m_strName = name;
      m_strPluginClass = pluginClass;
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
   public PSTransformationDef(Element source)
      throws PSUnknownNodeTypeException, PSLoaderException
   {
      if (source == null)
         throw new IllegalArgumentException("source may not be null");

      fromXml(source);
   }

   // implement PSLoaderNamedComponent#getName()
   public String getName()
   {
      return m_strName;
   }

   // implement PSLoaderNamedComponent#setName(String)
   public void setName(String name)
   {
      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name may not be null or empty");

      m_strName = name;
   }

   /**
    * Get the plugin class name of the selector
    *
    * @return The plugin class name of the selector, never <code>null</code> 
    *    or empty.
    */
   public String getPlugInClass()
   {
      return m_strPluginClass;
   }

   /**
    * Get the list of paramater definitions.
    *
    * @return An iterator over zero or more <code>PSParamDef</code>
    *    objects, never <code>null</code>, but may be empty.
    */
   public Iterator getParameters()
   {
      return m_parameters.getComponents();
   }

   /**
    * Get the <code>List</code> of parameter definitions
    * 
    * @return The <code>List</code> of parameter definitions, never
    * <code>null</code>, but may be empty.
    */
   public List getParameterList()
   {
      return m_parameters.getComponentList();
   }
   
   /**
    * Add a parameter.
    *
    * @param p The {@link PSParamDef} to be added, may not <code>null</code>
    *
    * @throws IllegalArgumentException If <code>p</code> is
    * <code>null</code>.
    */
   public void addParameter(PSParamDef p)
   {
      if (p == null)
         throw new IllegalArgumentException("parameter may not be null");

      m_parameters.addComponent(p);
   }

   /**
    * Serializes this object's state to its XML representation.  The format is:
    * <pre><code>
    * &lt;!ELEMENT PSTransformationDef (PSXParamDefs*)>
    * &lt;!ATTLIST PSTransformationDef
    *     name CDATA #REQUIRED
    *     class CDATA #REQUIRED
    * >
    * &lt;!ELEMENT PSXParamDef (Description)>
    * &lt;!ATTLIST PSXParamDef
    *     name CDATA #REQUIRED
    *     type CDATA #REQUIRED
    * >
    * &lt;!ELEMENT Description (#PCDATA)>
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

      Element root = doc.createElement(getXmlNode());
      root.setAttribute(XML_ATTR_NAME, m_strName);
      root.setAttribute(XML_ATTR_CLASS, m_strPluginClass);

      if (!m_parameters.isEmpty())
         root.appendChild(m_parameters.toXml(doc));

      return root;
   }

   /**
    * @see PSLoaderComponent#fromXml(Element)
    */ 
   public void fromXml(Element sourceNode)
      throws PSUnknownNodeTypeException, PSLoaderException
   {
      validateElement(sourceNode, getXmlNode());

      m_strName = getRequiredAttribute(sourceNode, XML_ATTR_NAME);
      m_strPluginClass = getRequiredAttribute(sourceNode, XML_ATTR_CLASS);

      PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);
      Element listEl;
      listEl = tree.getNextElement(PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
      
      if (listEl != null)
         m_parameters.fromXml(listEl);
   }

   /**
    * Get the XML node name for the current object.
    * 
    * @return XML node name, never <code>null</code> or empty.
    */
   protected String getXmlNode()
   {
      return XML_NODE_NAME;
   }
   
   /**
    * PSLoaderComponent#hashCode()
    */
   public int hashCode()
   {
      return m_strName.hashCode() + m_strPluginClass.hashCode() +
             m_parameters.hashCode();
   }

   /**
    * @see PSLoaderComponent#equals(Object)
    */
   public boolean equals(Object obj)
   {
      if (obj == null || (! (obj instanceof PSTransformationDef)))
         return false;

      PSTransformationDef obj2 = (PSTransformationDef) obj;

      return m_strName.equals(obj2.m_strName) &&
             m_strPluginClass.equals(obj2.m_strPluginClass) &&
             m_parameters.equals(obj2.m_parameters); 
   }

   /**
    * The XML node name of the component list, initialized by constructor,
    * never <code>null</code> or empty after that.
    */
   final public static String XML_NODE_NAME = "PSXTransformationDef";

   /**
    * The name of the component list, initialized by constructor, never
    * <code>null</code> or empty after that.
    */
   protected String m_strName;

   /**
    * The fully qualified class name of the plugin content selector, 
    * initialized by constructor, never <code>null</code> or empty after that.
    */
   protected String m_strPluginClass;

   /**
    * A list of paramdef's, initialized in definition, 
    * never <code>null</code>, but may be 
    * empty after that.
    */
   protected PSComponentList m_parameters = new PSComponentList(
      XML_NODE_PARAMS, PSParamDef.XML_NODE_NAME, PSParamDef.class);

   // Private constants for XML attribute and element name
   final static protected String XML_ATTR_NAME = "name";
   final static protected String XML_ATTR_CLASS = "class";
   final static protected String XML_NODE_PARAMS = "ParamDefs";
}
