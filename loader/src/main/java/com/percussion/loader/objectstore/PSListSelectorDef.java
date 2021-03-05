/*[ PSListSelectorDef.java ]***************************************************
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

import org.w3c.dom.Element;

/**
 * Encapsulates the definition of the list content selector
 */
public class PSListSelectorDef extends PSContentSelectorDef
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
    * @throws PSLoaderException if the property of <code>CONTENT_LIST</code>
    *    not exist or any other error occurs.
    */
   public PSListSelectorDef(PSContentSelectorDef cs)
      throws PSUnknownNodeTypeException, PSLoaderException
   {
      // Cannot validate the "cs" argument before call this(String), otherwise
      // need to define PSFileSelectorDef() constructor. Not check the argument
      // for now,
      super(cs.getName(), PLUGIN_CLASS);

      if (! cs.getPlugInClass().equals(PLUGIN_CLASS))
         throw new IllegalArgumentException(
            "cs.getPlugInClass() is not " + PLUGIN_CLASS);

      m_properties = cs.m_properties;
      m_searchRoots = cs.m_searchRoots;
      
      // make sure the CONTENT_LIST exists
      getContentList();
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
   public PSListSelectorDef(Element source)
      throws PSUnknownNodeTypeException, PSLoaderException
   {
      super(source);
      
      // make sure the CONTENT_LIST exists
      getContentList();
   }
   
   /**
    * Get the content list.
    * 
    * @return The fully qualified XML file name, which contains the content 
    *    list, never <code>null</code> or empty.
    *    
    * @throws PSLoaderException if the property of <code>CONTENT_LIST</code>
    *    not exist.
    */
   public String getContentList() throws PSLoaderException
   {
      return getProperty(CONTENT_LIST).getValue();
   }
   
   /**
    * Set the content list.
    * 
    * @param contentList The fully qualified XML file name, which contains the 
    *    content list, it may not be <code>null</code> or empty.
    *    
    * @throws PSLoaderException if the property of <code>CONTENT_LIST</code> not
    *    exists.
    */
   public void setContentList(String contentList) throws PSLoaderException
   {
      if ( (contentList == null) || (contentList.trim().length() == 0) )
         throw new IllegalArgumentException("contentList may not be null");
      
      PSProperty prop = getProperty(CONTENT_LIST);
      prop.setValue(contentList);
   }
   
  /**
   * Get the value of a property.
   *
   * @return The name of the property, may not <code>null</code> or empty.
   *
   * @throws PSLoaderException if cannot find the property
   */
   public PSProperty getProperty(String name) throws PSLoaderException
   {
      return PSLoaderUtils.getProperty(name, getProperties());
   }


   /**
    * The fully qualified class of the file selector plugin object.
    */
   final public static String PLUGIN_CLASS =
      "com.percussion.loader.selector.PSListContentSelector";

   /**
    * The property name that contains the XML file name of the content list
    */
   final public static String CONTENT_LIST = "ContentList";
}
