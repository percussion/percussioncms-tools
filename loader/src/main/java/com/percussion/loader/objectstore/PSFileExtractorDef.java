/*[ PSFileExtractorDef.java ]**************************************************
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
 * Encapsulates the definition for <code>PSFileExtractor</code>.
 */
public class PSFileExtractorDef extends PSItemExtractorDef
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
    * @throws PSLoaderException if there is no mime type definition.
    */
   public PSFileExtractorDef(Element source)
      throws PSUnknownNodeTypeException, PSLoaderException
   {
      super(source);
   }

   /**
    * The related file extractor class for this extractor definition
    */
   final static public String PLUGIN_CLASS =
      "com.percussion.loader.extractor.PSFileExtractor";
}
