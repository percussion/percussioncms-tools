/******************************************************************************
 *
 * [ PSAAClientActionException.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.content.ui.aa.actions;

import org.apache.commons.lang.StringUtils;

import java.lang.reflect.InvocationTargetException;

/**
 * An exception that is used when a error or exception occurs
 * when executing an AA client action.
 * If constructed with a nested {@link InvocationTargetException} extracts the
 * nested exception from it.
 */
public class PSAAClientActionException extends Exception
{

   // see base class for detail
   public PSAAClientActionException()
   {
      super();
   }

   // see base class for detail
   public PSAAClientActionException(String message, Throwable cause)
   {
      super(message, maybeGetNestedException(cause));
   }

   // see base class for detail
   public PSAAClientActionException(String message)
   {
      super(message);
   }

   // see base class for detail
   public PSAAClientActionException(Throwable cause)
   {
      super(maybeGetNestedException(cause));
   }

   /**
    * If the passed exception is {@link InvocationTargetException},
    * returns the nested exception, otherwise returns the specified exception.
    * Is static, so it can be called from a constructor.
    * @param t the exception to extract cause exception from.
    * If <code>null</code>, the method returns null.
    */
   private static Throwable maybeGetNestedException(Throwable t)
   {
      return t instanceof InvocationTargetException ? t.getCause() : t;
   }

   /**
    * Creates a message from this exception message and the cause.
    * @param message this exception message. Can be <code>null</code> or empty.
    * @param causeMessage the message of the cause exception.
    * Can be <code>null</code> or empty.
    */
   private String composeMessageFromCause(
         final String message, final String causeMessage)
   {
      if (getCause() == null)
      {
         return message;
      }
      else
      {
         return StringUtils.isBlank(message) ? causeMessage : message;  
      }
   }

   /**
    * {@inheritDoc}
    * This implementation excludes cause exception class name if cause is
    * specified.
    */
   @Override
   public String getMessage()
   {
      return composeMessageFromCause(super.getMessage(),
            getCause() == null ? null : getCause().getMessage());
   }

   /**
    * {@inheritDoc}
    * This implementation excludes cause exception class name if cause is
    * specified.
    */
   @Override
   public String getLocalizedMessage()
   {
      return composeMessageFromCause(super.getLocalizedMessage(),
            getCause() == null ? null : getCause().getLocalizedMessage());
   }
}
