/******************************************************************************
 *
 * [ IPSControlValueValidator.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.validators;

import com.percussion.workbench.ui.util.PSControlInfo;

/**
 * Used for validating the value in controls that are passed into the validate
 * method.
 * 
 * @author erikserating
 */
public interface IPSControlValueValidator
{
   /**
    * Validate the value of the control passed in. It is expected that many of
    * validator concrete classes will be specific to a certain type of control
    * and should verify that the expected control type is passed in.
    * 
    * @param controlInfo the control info object passed in, cannot be
    * <code>null</code>.
    * 
    * @return an error message <code>String</code> that describes the problem
    * if validation fails or <code>null</code> if validation passes
    * successfully.
    */
   public String validate(PSControlInfo controlInfo);
}
