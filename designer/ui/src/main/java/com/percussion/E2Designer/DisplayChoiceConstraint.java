/*[ DisplayChoiceConstraint.java ]********************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Validates local display choices for duplicate and empty labels and values.
 */
public class DisplayChoiceConstraint implements ValidationConstraint
{
   /**
    * implementing definition from interface ValidationConstraint
    * @return String - error string and an empty string if
    * no error is found.
    */
   public String getErrorText()
   {
      if (m_duplicateLabels)
      {
         return sm_res.getString("duplicateChoiceLabels");
      }
      if (m_emptyLabels)
      {
         return sm_res.getString("emptyChoiceLabels");
      }
      if (m_duplicateValues)
      {
         return sm_res.getString("duplicateChoiceValues");
      }
      if (m_emptyValues)
      {
         return sm_res.getString("emptyChoiceValues");
      }
      return "";
   }

   /**
    * implementing definition from interface ValidationConstraint.
    * @param jTable - runtime type of this object should be <code>JTable</code>,
    *    if not the method silently exits without performing any validation.
    *    Column count has to be 2. The first column is assumed to be the label
    *    column and the second column is assumed to be the value column.
    *
    * @throws ValidationException - If duplicate labels or values exist or empty
    *    value exist for a label or empty label exist for a value.
    */
   public void checkComponent(Object jTable) throws ValidationException
   {
      if (jTable instanceof JTable)
      {
         TableModel tbm = ((JTable)jTable).getModel();
         int colCount = tbm.getColumnCount();
         int rowCount = tbm.getRowCount();
         List labelList = new ArrayList();
         List valueList = new ArrayList();
         for (int k = 0; k < rowCount; k++)
         {
            String chLabel = tbm.getValueAt(k,0) == null ?
              "" : (String)tbm.getValueAt(k,0);
            String chValue = tbm.getValueAt(k,1) == null ?
              "" : (String)tbm.getValueAt(k,1);
           
            //If both are empty do not leave the row
            if(chLabel.trim().length() == 0 && chValue.trim().length() == 0)
            {
               continue;
            }
            if(chLabel.trim().length() == 0 && chValue.trim().length() != 0)
            {
               m_emptyLabels = true;
               throw new ValidationException(
                  sm_res.getString("emptyChoiceLabels"));
            }

            if(chLabel.trim().length() != 0 && chValue.trim().length() == 0)
            {
               m_emptyValues = true;
               throw new ValidationException(
                  sm_res.getString("emptyChoiceValues"));
            }

            if(labelList.contains(chLabel.toUpperCase()))
            {
               m_duplicateLabels = true;
               throw new ValidationException(
                  sm_res.getString("duplicateChoiceLabels"));
            }

            labelList.add(chLabel.toUpperCase());

            if(valueList.contains(chValue.toUpperCase()))
            {
               m_duplicateValues = true;
               throw new ValidationException(
                  sm_res.getString("duplicateChoiceValues"));
            }
            valueList.add(chValue.toUpperCase());
         }
      }
      else
         return;
   }      // End method checkComponent()

   /**
    * Flag to hold true if there are duplicate column values otherwise false
    */
   private boolean m_duplicateLabels = false;

   /**
    * Flag to hold true if there are duplicate column values otherwise false
    */
   private boolean m_emptyLabels = false;
   /**
    * Flag to hold true if there are duplicate column values otherwise false
    */
   private boolean m_duplicateValues = false;
   /**
    * Flag to hold true if there are duplicate column values otherwise false
    */
   private boolean m_emptyValues = false;

   /**
    * create static resource bundle object
    */
   static ResourceBundle sm_res = null;
   static
   {
      try
      {
         sm_res = ResourceBundle.getBundle(
            "com.percussion.E2Designer.ValidationResources",
             Locale.getDefault()
         );
       }
       catch(MissingResourceException mre)
       {
         mre.printStackTrace();
       }
   }
}


