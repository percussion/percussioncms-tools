/* *****************************************************************************
 *
 * [ PSLoaderDescriptor.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *******************************************************************************/
package com.percussion.loader.objectstore;

import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.loader.PSLoaderException;
import com.percussion.xml.PSXmlTreeWalker;

import java.io.File;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * Encapsulates a loader descriptor, it contains all necessary definitions for
 * each content loader operation.
 */
public class PSLoaderDescriptor extends PSContentLoaderDefs 
   implements java.io.Serializable
{
   /**
    * Constructs the object from a given path.
    *
    * @param path The path information to which all files for this descriptor
    *    will be saved to. The descriptor name is the last name in the
    *    pathname's name sequence, may not be <code>null</code> or empty.
    *
    * @throws IllegalArgumentException if <code>path</code> is invalid.
    */
   public PSLoaderDescriptor(String path)
   {
      setPath(path);
   }

   /**
    * Create this object from its XML representation
    *
    * @param source The source element.  See {@link #toXml(Document)} for
    * the expected format.  May not be <code>null</code>.
    *
    * @throws IllegalArgumentException If <code>source</code> is
    *    <code>null</code>.
    * @throws PSUnknownNodeTypeException <code>source</code> is malformed.
    */
   public PSLoaderDescriptor(Element source)
      throws PSUnknownNodeTypeException, PSLoaderException
   {
      if (source == null)
         throw new IllegalArgumentException("source may not be null");

      fromXml(source);
   }

   /**
    * Set the path and name for the descriptor.
    * 
    * @param path The path information to which all files for this descriptor
    *    will be saved to. The descriptor name is the last name in the
    *    pathname's name sequence, may not be <code>null</code> or empty.
    *
    * @throws IllegalArgumentException if <code>path</code> is invalid.
    */
   public void setPath(String path)
   {
      if (path == null || path.trim().length() == 0)
         throw new IllegalArgumentException("path may not be null or empty");

      m_path = path;
      File file = new File(m_path);
      m_name = file.getName();
   }
   
   /**
    * Get the name of the descriptor. Must call {@link #setPath(String)} first 
    * if the path has not been set yet.
    *
    * @return The name of the object, never <code>null</code> or empty.
    * 
    * @throws IllegalStateException if the name has not been set yet.
    */
   public String getName()
   {
      if (m_name == null)
         throw new IllegalStateException("m_name has not been set yet");

      return m_name;
   }

   /**
    * Get The path information to which all files for this descriptor will be
    * saved to. Must call {@link #setPath(String)} first if the path has not
    * been set yet.
    *
    * @return The path information, never <code>null</code> or empty.
    * 
    * @throws IllegalStateException if the path has not been set yet.
    */
   public String getPath()
   {
      if (m_path == null)
         throw new IllegalStateException("m_path has not been set yet");
         
      return m_path;
   }

   /**
    * Set the content selector definition.
    *
    * @param selectorDef The to be set content selector definition, may not
    *    <code>null</code>.
    *
    * @throws IllegalArgumentException If <code>selectorDef</code> is
    *    <code>null</code>.
    */
   public void setContentSelectorDef(PSContentSelectorDef selectorDef)
   {
      if (selectorDef == null)
         throw new IllegalArgumentException("selectorDef may not be null");

      m_selectorDef = selectorDef;
   }

   /**
    * Get the content selector definition.
    *
    * @return The content selector definition, never <code>null</code>
    *
    * @throws IllegalStateException If content selector definition has not been
    *    set.
    */
   public PSContentSelectorDef getContentSelectorDef()
   {
      if (m_selectorDef == null)
         throw new IllegalStateException("m_selectorDef has not been set");

      return m_selectorDef;
   }

   /**
    * Get the content loader definition.
    *
    * @return The content loader definition, never <code>null</code>
    *
    * @throws IllegalStateException If content loader definition has not been
    *    set.
    */
   public PSLoaderDef getLoaderDef()
   {
      return m_loaderDef;
   }

   /**
    * Set the content loader definition.
    *
    * @param loaderDef The to be set content loader definition, may not
    *    <code>null</code>.
    *
    * @throws IllegalArgumentException If <code>loaderDef</code> is
    *    <code>null</code>.
    */
   public void setLoaderDef(PSLoaderDef loaderDef)
   {
      if (loaderDef == null)
         throw new IllegalArgumentException("loaderDef may not be null");

      m_loaderDef = loaderDef;
   }

   /**
    * Serializes this object's state to its XML representation.  The format is:
    * <pre><code>
    * &lt;!ELEMENT PSXLoaderDescriptor ( Connection, PSXContentSelectorDef,
    *              Extractors, PSXContentLoaderDef, FieldTransformations?,
    *              ItemTransformations?, PSXLoggingDef, PSXErrorHandlingDef)>
    * &lt;!ATTLIST PSXLoaderDescriptor
    *     name CDATA #REQUIRED
    *     path CDATA #REQUIRED
    * >
    * </code></pre>
    *
    * See {@link PSLoaderComponent#toXml(Document)} for more info.
    *
    * @throws IllegalStateException if one of the required element has not
    *    been set.
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");

      // validate required data members
      if (m_selectorDef == null)
         throw new IllegalStateException("m_selector may not be null");
     /** if (m_loaderDef == null)
         throw new IllegalStateException("m_loaderDef may not be null");*/

      Element root = doc.createElement(XML_NODE_NAME);

      root.appendChild(m_conDef.toXml(doc));
      root.appendChild(m_selectorDef.toXml(doc));
      root.appendChild(getAllExtractorDefs().toXml(doc));
      root.appendChild(m_loaderDef.toXml(doc));

      if ( m_fieldTransDef != null )
         root.appendChild(m_fieldTransDef.toXml(doc));
      if ( m_itemTransDef != null )
         root.appendChild(m_itemTransDef.toXml(doc));

      root.appendChild(m_logDef.toXml(doc));

      root.appendChild(m_errorHandlingDef.toXml(doc));

      return root;
   }

   // see PSLoaderComponent#fromXml(Element)
   public void fromXml(Element sourceNode)
      throws PSUnknownNodeTypeException, PSLoaderException
   {
      validateElement(sourceNode, XML_NODE_NAME);

      PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);

      //get connection definition
      Element conEl = getNextRequiredElement(tree,
        PSConnectionDef.XML_NODE_NAME,
        PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
      m_conDef = new PSConnectionDef(conEl);

      tree.setCurrent(sourceNode);
      Element selectorEl = getNextRequiredElement(tree,
         PSContentSelectorDef.XML_NODE_NAME,
         PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
      m_selectorDef = new PSContentSelectorDef(selectorEl);

      // get the correct selector definition object
      String pluginClass = m_selectorDef.getPlugInClass();
      if (pluginClass.equals(PSFileSelectorDef.PLUGIN_CLASS))
         m_selectorDef = new PSFileSelectorDef(m_selectorDef);

      // get list of extractor defs
      Element listEl = getNextRequiredElement(tree, XML_EXTRACTORS,
         PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS);
      loadExtractorDefs(listEl);

      Element loaderEl = getNextRequiredElement(tree, PSLoaderDef.XML_NODE_NAME,
         PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS);
      m_loaderDef = new PSLoaderDef(loaderEl);

      // load both field & item transformers if there is any
      listEl = loadTransformations(tree);

      // load the log definition
      m_logDef = new PSLogDef(listEl);

      // load the error handling definition
      Element errHandleDefEl = getNextRequiredElement(tree,
         PSErrorHandlingDef.XML_NODE_NAME,
         PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS);
      m_errorHandlingDef = new PSErrorHandlingDef(errHandleDefEl);
   }

   // see PSLoaderComponent#hashCode()
   public int hashCode()
   {
      return m_conDef.hashCode() +
         m_selectorDef.hashCode() + m_staticExtractorDefs.hashCode() +
         m_itemExtractorDefs.hashCode() + m_fieldTransDef.hashCode() +
         m_itemTransDef.hashCode() + m_logDef.hashCode() +
         m_errorHandlingDef.hashCode() + 
         (m_name == null ? 0 : m_name.hashCode()) +
         (m_path == null ? 0 : m_path.hashCode());
   }

   // see PSLoaderComponent#equals(Object)
   public boolean equals(Object obj)
   {
      if ( obj == null || (! (obj instanceof PSLoaderDescriptor)) )
         return false;

      PSLoaderDescriptor obj2 = (PSLoaderDescriptor) obj;

      return m_selectorDef.equals(obj2.m_selectorDef) &&
         m_conDef.equals(obj2.m_conDef) &&
         m_staticExtractorDefs.equals(obj2.m_staticExtractorDefs) &&
         m_itemExtractorDefs.equals(obj2.m_itemExtractorDefs) &&
         ((m_fieldTransDef == obj2.m_fieldTransDef) || 
               (m_fieldTransDef != null && 
                     m_fieldTransDef.equals(obj2.m_fieldTransDef))) &&
         ((m_itemTransDef == obj2.m_itemTransDef) || 
               (m_itemTransDef != null && 
                     m_itemTransDef.equals(obj2.m_itemTransDef))) &&
         m_logDef.equals(obj2.m_logDef) &&
         m_errorHandlingDef.equals(obj2.m_errorHandlingDef) &&
         ((m_name == obj2.m_name) ||
          ((m_name != null) && m_name.equals(obj2.m_name))) &&
         ((m_path == obj2.m_path) ||
          ((m_path != null) && m_path.equals(obj2.m_path)));
   }

   /**
    * Root node name of this object's XML representation.
    */
   public static final String XML_NODE_NAME = "PSXLoaderDescriptor";

   /**
    * The name of the descriptor. Modified by {@ link #setPath(String)}, never
    * <code>null</code> or empty after that.
    */
   private transient String m_name = null;

   /**
    * The path information to which all files for this descriptor will be
    * saved to. Modified by {@ link #setPath(String)}, never 
    * <code>null</code> after that.
    */
   private transient String m_path = null;

   /**
    * The definition of content selector, may be <code>null</code> if not set
    * yet.
    */
   private PSContentSelectorDef m_selectorDef = null;

   /**
    * The definition of content loader, may be <code>null</code> if not set yet
    */
   private PSLoaderDef m_loaderDef = null;

   // Private constants for XML attribute and element name
   final private String XML_ATTR_NAME = "name";
   final private String XML_ATTR_PATH = "path";
}
