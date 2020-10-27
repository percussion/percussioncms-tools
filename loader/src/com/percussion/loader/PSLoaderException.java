/*[ PSLoaderException.java ]***************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader;

import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.error.PSException;
import com.percussion.error.PSStandaloneException;

import org.w3c.dom.Element;


/**
 * Exception class used to report general exceptions for loader application, 
 * or may be subclassed if necessary.  Handles formatting of messages stored in
 * the PSLoaderErrorStringBundle resource bundle using error codes and 
 * arguments. Localization is also supported.
 */
public class PSLoaderException extends PSStandaloneException
{
   /**
    * @see {@link com.percussion.error.PSStandaloneException(int, Object)
    */
   public PSLoaderException(int msgCode, Object singleArg)
   {
      super(msgCode, singleArg);
   }

   /**
    * @see {@link com.percussion.error.PSStandaloneException(int, Object[])
    */
   public PSLoaderException(int msgCode, Object[] arrayArgs)
   {
      super(msgCode, arrayArgs);
   }

   /**
    * @see {@link com.percussion.error.PSStandaloneException(int)
    */
   public PSLoaderException(int msgCode)
   {
      super(msgCode);
   }

   /**
    * @see {@link com.percussion.error.PSStandaloneException(PSException)
    */
   public PSLoaderException(PSException ex)
   {
      super(ex);
   }

   /**
    * @see 
    * {@link com.percussion.error.PSStandaloneException(PSStandaloneException)
    */
   public PSLoaderException(PSStandaloneException ex)
   {
      super(ex);
   }

   /**
    * @see {@link com.percussion.error.PSStandaloneException(Element)
    */
   public PSLoaderException(Element source) throws PSUnknownNodeTypeException
   {
      super(source);
   }

   /**
    * @see {@link com.percussion.error.getResourceBundleBaseName()
    */
   protected String getResourceBundleBaseName()
   {
      return "com.percussion.loader.PSLoaderErrorStringBundle";
   }

   /**
    * @see {@link com.percussion.error.getXmlNodeName()
    */
   protected String getXmlNodeName()
   {
      return "PSXLoaderException";
   }

}



