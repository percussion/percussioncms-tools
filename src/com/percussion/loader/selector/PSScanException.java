/*[ PSScanException.java ]*****************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader.selector;

import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.error.PSException;
import com.percussion.loader.PSLoaderException;

import org.w3c.dom.Element;

/**
 * Exceptions of this type will be thrown on errors dealing with scanning 
 * errors in selectors.
 */
public class PSScanException extends PSLoaderException
{
   /**
    * @see {@link com.percussion.loader.PSLoaderException(int, Object)}
    */
   public PSScanException(int msgCode, Object singleArg)
   {
      super(msgCode, singleArg);
   }

   /**
    * @see {@link com.percussion.loader.PSLoaderException(int, Object[])}
    */
   public PSScanException(int msgCode, Object[] arrayArgs)
   {
      super(msgCode, arrayArgs);
   }

   /**
    * @see {@link com.percussion.loader.PSLoaderException(int)}
    */
   public PSScanException(int msgCode)
   {
      super(msgCode);
   }

   /**
    * @see {@link com.percussion.loader.PSLoaderException(PSException)}
    */
   public PSScanException(PSException ex)
   {
      super(ex);
   }

   /**
    * @see {@link com.percussion.loader.PSLoaderException(Element)}
    */
   public PSScanException(Element source) 
      throws PSUnknownNodeTypeException
   {
      super(source);
   }

   /**
    * @see {@link com.percussion.loader.PSLoaderException#getXmlNodeName()}
    */
   protected String getXmlNodeName()
   {
      return "PSScanException";
   }
}
