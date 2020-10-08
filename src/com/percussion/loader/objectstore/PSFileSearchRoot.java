/*[ PSFileSearchRoot.java ]****************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader.objectstore;

import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.loader.PSLoaderException;

import org.w3c.dom.Element;

/**
 * Encapsulates a file search root definition
 */
public class PSFileSearchRoot extends PSSearchRoot 
   implements java.io.Serializable
{
   /**
    * Construct a file search root with a given name, default properties and
    * no filter.
    * 
    * @param name The name of the object, may not <code>null</code> or empty.
    * @param searchRoot The search root of the object, may not <code>null</code>
    *    or empty.
    * @param recurse The recurse setting, <code>true</code> if allow process
    *    sub-directories; <code>false</code> otherwise.
    * 
    * @throws IllegalArgumentException if any parameter is invalid.
    */
   public PSFileSearchRoot(String name, String searchRoot, boolean recurse)
   {
      super(name);

      if (searchRoot == null || searchRoot.trim().length() == 0)
         throw new IllegalArgumentException(
            "searchRoot may not be null or empty");
               
      setSearchRoot(searchRoot);
      //addProperty(new PSProperty(XML_SEARCHROOT_NAME, searchRoot));
      addProperty(new PSProperty(XML_RECURSE_NAME, 
         (recurse ? XML_TRUE : XML_FALSE)));
   }
   
   /**
    * @see {@link PSSearchRoot(Element)}
    */
   public PSFileSearchRoot(Element source)
      throws PSUnknownNodeTypeException, PSLoaderException
   {
      super(source);
   }

   /**
    * Get the search root setting.
    * 
    * @return The search root path, never <code>null</code> or empty.
    *    
    * @throws IllegalStateException if property of 
    * <code>XML_RECURSE_NAME</code> does not exist.
    */
   public String getSearchRoot()
   {
      PSProperty property = getProperty(XML_SEARCHROOT_NAME);
      if (property == null)
         throw new IllegalStateException("missing search root property");
         
      return property.getValue();
   }
   
   /**
    * Set the search root property. If property does not exist it adds one. 
    * 
    * @param searchRoot The search root of the object, may not <code>null</code>
    *    or empty.
    *
    */
   public void setSearchRoot(String searchRoot)
   {
      if (searchRoot == null || searchRoot.trim().length() == 0)
         throw new IllegalArgumentException(
            "searchRoot may not be null or empty");

      // Value must in url form 
      searchRoot = searchRoot.replace('\\', '/');

      PSProperty property = getProperty(XML_SEARCHROOT_NAME);
      if (property == null)
      {         
         addProperty(new PSProperty(XML_SEARCHROOT_NAME, searchRoot));   
         return;
      }
               
      property.setValue(searchRoot);
   }
   
   /**
    * Get the recurse setting.
    * 
    * @return <code>true</code> if allow process sub-directories; 
    *    <code>false</code> otherwise.
    *    
    * @throws IllegalStateException if property of 
    * <code>XML_RECURSE_NAME</code> does not exist.
    */
   public boolean doRecurse()
   {
      PSProperty property = getProperty(XML_RECURSE_NAME);
      if (property == null)
         throw new IllegalStateException("missing recurse property");
         
      return property.getValue().equals(XML_TRUE);
   }

   /**
    * Set the recurse process sub-directories.
    * 
    * @param doRecurse The to be set recurse value, <code>true</code> if
    *    allow process sub-directories, <code>false</code> otherwise.
    *    
    * @throws IllegalArgumentException if property of 
    * <code>XML_RECURSE_NAME</code> does not exist.
    */
   public void setDoRecurse(boolean doRecurse)
   {
      String sValue = doRecurse ? XML_TRUE : XML_FALSE;

      PSProperty property = getProperty(XML_RECURSE_NAME);
      if (property == null)
         throw new IllegalStateException("missing recures property");
         
      property.setValue(sValue);
   }

   /**
    * The property name of the search root
    */
   final static public String XML_SEARCHROOT_NAME = "Search Root";
   
   /**
    * The property name of the recurse
    */
   final static public String XML_RECURSE_NAME = "Recurse";
}
