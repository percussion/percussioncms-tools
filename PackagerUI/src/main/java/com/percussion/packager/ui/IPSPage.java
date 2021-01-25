/******************************************************************************
 *
 * [ IPSPage.java ]
 * 
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.packager.ui;

import com.percussion.packager.ui.model.PSPackagerClientModel;

import java.util.List;



/**
 * This interface represents a page in a editor and common functionality
 * that needs to occur such as loading the fields and updating the model.
 * @author erikserating
 *
 */
public interface IPSPage
{
   /**
    * Load field values from the passed in model.
    * @param model the data model, cannot be <code>null</code>.
    */
   public void load(PSPackagerClientModel model);
   
   /**
    * Update the data model from the page fields.
    * @param model the data model, cannot be <code>null</code>.
    */
   public void update(PSPackagerClientModel model);
   
   /**
    * Validate field data before it gets to the model.
    * @return List of validation errors. May be <code>null</code> if
    * no validation errors occurred.
    */   
   public List<String> validateData();  
   
}
