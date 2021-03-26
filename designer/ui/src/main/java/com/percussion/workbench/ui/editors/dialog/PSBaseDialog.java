/*******************************************************************************
 *
 * [ PSaseDialog.java ]
 * 
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.workbench.ui.editors.dialog;

import com.percussion.client.IPSReference;
import org.eclipse.swt.widgets.Shell;

/**
 * Manages the loading/saving of the data object and lock.
 * 
 * @version 6.0
 * @author Paul Howard
 */
public abstract class PSBaseDialog extends PSDialog
{
   /**
    * The dialog will load the data associated with the supplied reference. It
    * can be retrieved by the derived class by calling {@link #getData()}. The
    * derived class should verify that the object returned by {@link #getData()}
    * is of the correct type before returning.
    * 
    * @param parent Never <code>null</code>.
    * 
    * @param ref The object to be edited. Never <code>null</code>.
    */
   public PSBaseDialog(Shell parent, @SuppressWarnings("unused") IPSReference ref)
   {
      super(parent);
   }

   /**
    * Release the lock on the data and call <code>super</code>.
    * 
    * @see org.eclipse.jface.window.Window#close()
    */
   @Override
   public boolean close()
   {
      return super.close();
   }

   /**
    * Release the lock on the data and call <code>super</code>.
    */
   @Override
   protected void cancelPressed()
   {
      super.cancelPressed(); 
   }

   /**
    * Calls {@link #accepted()} and if this returns <code>true</code>, the
    * data is saved, the lock is released and the dialog is dismissed.
    */
   @Override
   protected void okPressed()
   {
      super.okPressed();
   }

   /**
    * The data from the reference supplied in the ctor is loaded and locked in
    * the ctor (to verify it can be successfully locked.)
    * 
    * @return Never <code>null</code>. The returned type is dependent upon
    * the type of the supplied reference.
    */
   public Object getData()
   {
      return null;
   }  
   

   /**
    * Should perform necessary validation and transfer data from the dialog to
    * the object.
    * 
    * @return <code>true</code> if validation succeeded and the object is
    * ready to save, <code>false</code> otherwise. In the latter case, the
    * user should be notified of the problem. The dialog will not be closed if
    * this method returns <code>false</code>.
    */
   protected abstract boolean accepted();

}