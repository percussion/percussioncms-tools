/*[ PSFileSelectorDef.java ]***************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader.objectstore;

import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.loader.PSLoaderException;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.util.Iterator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Encapsulates a file selector definition
 */
public class PSFileSelectorDef extends PSContentSelectorDef
   implements java.io.Serializable
{
   /**
    * Constructs an object from a generic <code>PSContentSelectorDef</code>
    *
    * @param cs The to be cloned object. It's plugin class must be
    *    <code>PLUGIN_CLASS</code>.
    *
    * @throws PSUnknownNodeTypeException if encounter a malformed XML during
    *    the process.
    * @throws PSLoaderException if any other error occurs.
    */
   public PSFileSelectorDef(PSContentSelectorDef cs)
      throws PSUnknownNodeTypeException, PSLoaderException
   {
      // Cannot validate the "cs" argument before call this(String), otherwise
      // need to define PSFileSelectorDef() constructor. Not check the argument
      // for now,
      super(cs.getName(), PLUGIN_CLASS);

      if (! cs.getPlugInClass().equals(PLUGIN_CLASS))
         throw new IllegalArgumentException(
            "cs.getPlugInClass() is not " + PLUGIN_CLASS);

      // clone the search root objects
      Iterator searchRoots = cs.getSearchRoots();
      Document doc = PSXmlDocumentBuilder.createXmlDocument();

      while (searchRoots.hasNext())
      {
         PSSearchRoot sr = (PSSearchRoot)searchRoots.next();
         PSFileSearchRoot fsr = new PSFileSearchRoot(sr.toXml(doc));
         addSearchRoot(fsr);
      }

      // shallow copy the properties if there is any
      m_properties = cs.m_properties;
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
   public PSFileSelectorDef(Element source)
         throws PSUnknownNodeTypeException, PSLoaderException
   {
      super(source);
   }

   /**
    * Override the method of the base class
    */
   protected void setSearchRootList()
   {
      m_searchRoots = new PSComponentList(XML_NODE_SEARCHROOTS,
         PSSearchRoot.XML_NODE_NAME, PSFileSearchRoot.class);
   }

   /**
    * The fully qualified class of the file selector plugin object.
    */
   final public static String PLUGIN_CLASS =
      "com.percussion.loader.selector.PSFileSelector";
}
