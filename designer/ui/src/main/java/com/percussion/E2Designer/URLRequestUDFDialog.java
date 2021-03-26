/*[ URLRequestUDFDialog.java ]*************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer;

import com.percussion.design.objectstore.PSExtensionCall;
import com.percussion.design.objectstore.PSUrlRequest;

import javax.swing.*;

/**
 * This derviation of URLRequestDialog is used for creating UDF links as
 * <code>PSExtensionCall</code> objects (a child element of
 * <code>PSUrlRequest</code>).  It hides the name panel and overrides getData.
 *
 * @todo add a constructor for editing?
 */
public class URLRequestUDFDialog extends URLRequestDialog
{
   /**
    * Initializes a newly created <code>URLRequestUDFDialog</code> object with
    * default values.
    */
   public URLRequestUDFDialog()
   {
      super();
   }

   /**
    * Sets the dialog to an edit state, loading the values from the specified
    * model into the controls.
    *
    * @param model object to be edited by the dialog; cannot be <code>null
    * </code>
    * @throws IllegalArgumentException if model is <code>null</code> or invalid
    */
   public void setData(Object model)
   {
      if (!myIsValidModel( model ))
         throw new IllegalArgumentException( "Cannot provide a invalid model" );

      super.setData( new PSUrlRequest(null, (PSExtensionCall) model) );
   }

   /**
    * Returns just the PSExtensionCall child of the PSUrlRequest object
    * built by the super-class
    *
    * @return OSExtensionCall if dialog valid, otherwise <code>null</code>
    */
   public Object getData()
   {
      Object o = super.getData();
      if (o != null)
      {
         PSExtensionCall pscall = ((PSUrlRequest)o).getConverter();
         OSExtensionCall oscall = null;
         if (pscall != null)
            oscall = new OSExtensionCall(pscall, null);
         return oscall;
      }
      else
         return null;
   }


   /**
    * Checks to see if the specified object is editable by this dialog.
    * 
    * @return <code>true</code> if <code>model</code> instanceof <code>
    * PSExtensionCall</code> with an extension reference of either <code>
    * sys_MakeAbsLink</code> or <code>sys_MakeIntLink</code>; 
    * <code>false</code> otherwise.
    */ 
   public boolean isValidModel(Object model)
   {
      return myIsValidModel( model );
   }


   /**
    * Checks to see if the specified object is editable by this dialog.  This
    * method is used by <code>setData</code> because it cannot be overriden.
    * (If <code>setData</code> called <code>isValidModel</code>, then if
    * a derived class called <code>super.setData</code>, the super class might
    * use the derived class' <code>isValidModel</code> instead -- which would
    * cause a false result if the derived class had different requirements than
    * the super class.)
    * 
    * @return <code>true</code> if <code>model</code> instanceof <code>
    * PSExtensionCall</code> with an extension reference of either <code>
    * sys_MakeAbsLink</code> or <code>sys_MakeIntLink</code>; 
    * <code>false</code> otherwise.
    */ 
   private boolean myIsValidModel(Object model)
   {
      if (model instanceof PSExtensionCall)
      {
         PSExtensionCall udf = (PSExtensionCall) model;
         String canonicalName = udf.getExtensionRef().getFQN();
         if ( (canonicalName.equals( MAKE_ABS_LINK )) || 
              (canonicalName.equals( MAKE_INT_LINK )) )
            return true;
      }
      return false;
   }
   

   /**
    * Since PSExtensionCall does not support a name, there is no need for it
    * on this dialog.
    *
    * @return a new, empty JPanel
    */
   protected JPanel createNamePanel()
   {
      return new JPanel();
   }
}
