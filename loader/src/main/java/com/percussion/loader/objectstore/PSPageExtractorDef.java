/*[ PSPageExtractorDef.java ]**************************************************
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
 * Encapsulates the definition for <code>PSPageExtractor</code>.
 */
public class PSPageExtractorDef extends PSItemExtractorDef
   implements java.io.Serializable
{
   /**
    * Create this object from its XML representation
    *
    * @param source The source element.  See {@link #toXml(Document)} for
    * the expected format.  May not be <code>null</code>.
    *
    * @throws IllegalArgumentException If <code>source</code> is
    * <code>null</code>.
    * @throws PSUnknownNodeTypeException <code>source</code> is malformed.
    * @throws PSLoaderException if there is no <code>BODY_ONLY</code> property.
    */
   public PSPageExtractorDef(Element source)
      throws PSUnknownNodeTypeException, PSLoaderException
   {
      super(source);
   }

   /**
    * Set the encoding that is used to convert bytes to string for the page
    * content.
    * 
    * @param encoding The encoding, must not be <code>null</code> or empty.
    */
   public void setEncoding(String encoding)
   {
      if (encoding == null || encoding.trim().length() == 0)
         throw new IllegalArgumentException("encoding must not be null");
         
      PSProperty encProperty = PSLoaderUtils.getOptionalProperty(ENCODING, 
         getProperties());
      
      if (encProperty == null)
         addProperty(new PSProperty(ENCODING, encoding));
      else
         encProperty.setValue(encoding);
   }

   /**
    * Get the encoding property value.
    * 
    * @return The value of the encoding propety. It may be <code>null</code>
    *    if the encoding property does not exist.
    */   
   public String getEncoding()
   {
      PSProperty encProperty = PSLoaderUtils.getOptionalProperty(ENCODING, 
         getProperties());
      
      return (encProperty == null) ? null : encProperty.getValue();
   }
   
   /**
    * The related page extractor class for this extractor definition
    */
   final static public String PLUGIN_CLASS =
      "com.percussion.loader.extractor.PSPageExtractor";
   
   /**
    * Encoding property name.
    */
   public final static String ENCODING = "Content Encoding";   
}
