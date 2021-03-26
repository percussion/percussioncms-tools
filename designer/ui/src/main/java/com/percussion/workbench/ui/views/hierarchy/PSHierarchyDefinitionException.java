/*******************************************************************************
 *
 * [ PSHierarchyDefinitionException.java ]
 * 
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.workbench.ui.views.hierarchy;

import java.text.MessageFormat;

/**
 * This class is used to provide information about misconfigurations within the
 * hierarchy definition XML files.
 *
 * @author paulhoward
 */
public class PSHierarchyDefinitionException extends Exception
{
   /**
    * 
    */
   public static final String BAD_ATTRIBUTE_VALUE_TEMPLATE = 
      "Unknown value for the ''{0}'' attribute of the ''{1}'' element: ''{2}''."
      + " Expected ''{3}''";
   
   public PSHierarchyDefinitionException(String message)
   {
      super(message);
   }

   public PSHierarchyDefinitionException(String template, String[] params)
   {
      super(createMessage(template, params));
   }

   private static String createMessage(String template, String[] params)
   {
      return MessageFormat.format(template, (Object[]) params);
   }

   public PSHierarchyDefinitionException(String message, Throwable cause)
   {
      super(message, cause);
   }

   public PSHierarchyDefinitionException(String template, String[] params, 
         Throwable cause)
   {
      super(createMessage(template, params), cause);
   }

   /**
    * In support of serialization.
    */
   private static final long serialVersionUID = 665832818993550418L;
}
