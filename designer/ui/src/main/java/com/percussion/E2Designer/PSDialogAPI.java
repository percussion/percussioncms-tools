/*[ PSDialogAPI.java ]*********************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer;

import java.awt.*;

/**
 * Defines a standard API for getting and setting data on <code>PSDialog</code>
 * objects.  Can be used by objects responsible for displaying many types of
 * dialogs.
 */
public abstract class PSDialogAPI extends PSDialog
{
   /**
    * Pass-through constructor to <code>PSDialog</code>
    */
   public PSDialogAPI()
   {
   }


   /**
    * Pass-through constructor to <code>PSDialog</code>
    */
   public PSDialogAPI(Dialog d)
   {
      super( d );
   }


   /**
    * Pass-through constructor to <code>PSDialog</code>
    */
   public PSDialogAPI(Frame f)
   {
      super( f );
   }


   /**
    * Gets the object that encapsulates the information from this dialog.
    * 
    * @return An object that contains the state of the dialog or <code>null
    * </code> if the dialog was cancelled.
    */
   public abstract Object getData();


   /**
    * Sets the object that will be edited by this dialog.
    * 
    * @param model object to be edited, not <code>null</code> and must be valid
    * 
    * @throws IllegalArgumentException if <code>model</code> is not a valid 
    * object for this dialog.
    * @see #isValidModel(Object)
    */
   public abstract void setData(Object model);


   /**
    * Checks the <code>model</code> to see if it represents an object that this
    * dialog can edit.
    * 
    * @param model object to check for editability.  May be <code>null</code>,
    * but no dialog should consider <code>null</code> to be valid.
    * 
    * @return <code>true</code> if the object is editable by this dialog;
    * <code>false</code> if object is <code>null</code> or not editable.
    */
   public abstract boolean isValidModel(Object model);


   /**
    * Prepares the dialog for creating a new object, by clearing all controls
    * and setting them to their defaults.
    */
   public abstract void reset();
}
