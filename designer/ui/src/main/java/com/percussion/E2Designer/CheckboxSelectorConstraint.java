/*[ CheckboxSelectorConstraint.java ]******************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Constraint to validate the selected items in a JTable containing CheckBoxes
 * in the first column, and thus Boolean values in the first column of
 * the model.  Ensures that at least one value is set to True in the first
 * column of the model.
 */
public class CheckboxSelectorConstraint implements ValidationConstraint
{

   /**
    * Method definition to validate the value of the TableModel passed
    * in.  Validates that at least one item in the first column is True
    *
    * @param suspect a JTable containing Boolean Values in the first
    * column of it's model.
    * @roseuid 3A06CE850157
    */
   public void checkComponent(Object suspect) throws ValidationException
   {
      if (!(suspect instanceof JTable))
         throw new IllegalArgumentException(
            "suspect must be instance of JTable");
            
      TableModel tm = ((JTable)suspect).getModel();

      // walk table entries
      for (int i = 0; i < tm.getRowCount(); i++)
      {
         // check row's value for first column
         if (((Boolean)tm.getValueAt(i, 0)).booleanValue())
            return;
      }

      // if we're here, none were true
      throw new ValidationException();

   }

   /**
    * Method definition to return the error message to be posted by the
    * warning Dialog when a component contains an invalid value.
    * @roseuid 3A06CE8B034B
    */
   public String getErrorText()
   {
      return ms_res.getString("noneSelected");
   }

   /**
    * Default constructor.  Used for checking that the list has at least one selected item.
    */
   public CheckboxSelectorConstraint()
   {
   }

   //create static resource bundle object
   static ResourceBundle ms_res = null;
   static
   {
      try
      {
          ms_res = ResourceBundle.getBundle( "com.percussion.E2Designer.ValidationResources",
                                         Locale.getDefault() );
      }catch(MissingResourceException mre)
      {
          System.out.println( mre );
      }
   }

}
