/*[ PSExcludeException.java ]**************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader;

import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.error.PSException;

import org.w3c.dom.Element;

/**
 * Exceptions of this type will be thrown from item transformers to indicate to
 * the manager that the current item has to be excluded from the upload process.
 */
public class PSExcludeException extends PSLoaderException
{
   /**
    * @see {@link com.percussion.loader.PSLoaderException(int, Object)}
    */
   public PSExcludeException(int msgCode, Object singleArg)
   {
      super(msgCode, singleArg);
   }

   /**
    * @see {@link com.percussion.loader.PSLoaderException(int, Object[])}
    */
   public PSExcludeException(int msgCode, Object[] arrayArgs)
   {
      super(msgCode, arrayArgs);
   }

   /**
    * @see {@link com.percussion.loader.PSLoaderException(int)}
    */
   public PSExcludeException(int msgCode)
   {
      super(msgCode);
   }

   /**
    * @see {@link com.percussion.loader.PSLoaderException(PSException)}
    */
   public PSExcludeException(PSException ex)
   {
      super(ex);
   }

   /**
    * @see {@link com.percussion.loader.PSLoaderException(Element)}
    */
   public PSExcludeException(Element source) 
      throws PSUnknownNodeTypeException
   {
      super(source);
   }

   /**
    * @see {@link com.percussion.loader.PSLoaderException#getXmlNodeName()}
    */
   protected String getXmlNodeName()
   {
      return "PSXExcludeException";
   }
}
