/******************************************************************************
 *
 * [ PSContentTypeNameValidator.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.validators;

import com.percussion.utils.string.PSStringUtils;

/**
 * This validator is used to validate the Content Type name that used
 * the <code>Text</code> control. The invalid characters for the Content Type
 * names are defined by {@link PSStringUtils#validateContentTypeName(String)}
 */
public class PSContentTypeNameValidator 
   extends PSControlValueTextIdValidator
{
   @Override
   public Character getInvalidChar(String str)
   {
      return PSStringUtils.validateContentTypeName(str);
   }
}
