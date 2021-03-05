/*[ PSItemExtractorDef.java ]**************************************************
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
 * This is a parent class for all item extractor definitions,
 * which are not static extractors. The item extractor definition requires
 * a <code>CONTENT_TYPE_NAME</code> property.
 */
public class PSItemExtractorDef extends PSExtractorDef
   implements java.io.Serializable
{
   /**
    * Create this object from its XML representation
    *
    * @param source The source element.  See {@link #toXml(Document)} for
    * the expected format.  May not be <code>null</code>.
    *
    * @throws IllegalArgumentException If <code>source</code> is
    * <code>null</code> or the type attribute is not <code>TYPE_ITEM</code>.
    *
    * @throws PSUnknownNodeTypeException <code>source</code> is malformed.
    */
   public PSItemExtractorDef(Element source)
      throws PSUnknownNodeTypeException, PSLoaderException
   {
      super(source);

      if (! m_strType.equals(TYPE_ITEM))
         throw new IllegalArgumentException("m_strType must be " + TYPE_ITEM);

      // make sure the content type name exists
      getContentTypeName();
   }

   /**
    * The property name which contains the content type name.
    */
   final static public String CONTENT_TYPE_NAME = "ContentTypeName";

}
