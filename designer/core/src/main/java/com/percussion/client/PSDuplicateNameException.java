/******************************************************************************
 *
 * [ PSDuplicateNameException.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client;

import com.percussion.client.error.PSClientException;
import org.apache.commons.lang.StringUtils;

/**
 * This exception is thrown when an attempt is made to add a named object to
 * a parent that already has an child by that name.
 *
 * @version 6.0
 * @author paulhoward
 */
public class PSDuplicateNameException extends PSClientException
{
   /**
    * Only ctor.
    * @param name Never <code>null</code> or empty.
    * @param objectType May be <code>null</code>
    */
   public PSDuplicateNameException(String name, PSObjectType objectType)
   {
      super(PSErrorCodes.DUPLICATE_NAME, new Object[] {name, objectType}, null);
      if (StringUtils.isBlank(name))
      {
         throw new IllegalArgumentException("name cannot be null or empty");  
      }
   }
   
   /**
    * Required for serialization.
    */
   private static final long serialVersionUID = 759116797030654671L;
}
