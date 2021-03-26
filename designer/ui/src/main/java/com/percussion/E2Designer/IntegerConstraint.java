/*[ IntegerConstraint.java ]***************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer;

//import java.awt.Component;

import javax.swing.*;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/** Handles all integer validation.  Uses Integer public constants Integer.MAX_VALUE
  * and Integer.MIN_VALUE to designate "infinite". (meaning only one boundary, min
  * or max)
  *
  * @see java.lang.Integer
*/

public class IntegerConstraint implements ValidationConstraint
{

   //create static resource bundle object
   static ResourceBundle sm_res = null;
   static
   {
      try
      {
          sm_res = ResourceBundle.getBundle( "com.percussion.E2Designer.ValidationResources",
                                         Locale.getDefault() );
      }catch(MissingResourceException mre)
      {
          System.out.println( mre );
      }
   }

/** Constructs a basic IntegerConstraint object that handles only valid integers, no
  * range checking.
*/

  public IntegerConstraint() {}

/** Constructs an IntegerConstraint object that handles valid integers and checks
  * a specified range.  Enter Integer.MAX_VALUE for max if no maximum boundary
  * is required, and enter Integer.MIN_VALUE for min if no minimum boundary is
  * is desired.
  *
  * @param max input for the range maximum
  * @param min input for the range minimum
  * @exception IllegalArgumentException whenever max and min equals
*/

  public IntegerConstraint(int max, int min) throws IllegalArgumentException
  {
     if (max > min)
     {
        m_rangeMax = new Integer(max);  m_rangeMin = new Integer(min);
     }
     else if (max < min)
     {
        m_rangeMax = new Integer(min);  m_rangeMin = new Integer(max);
     }
     else
        throw new IllegalArgumentException("Max and Min cannot equal!");
  }

  // implementing definition from interface ValidationConstraint
  public String getErrorText()
  {
     if (m_errorMsg[1] == null && m_errorMsg[0] == null)
        return MessageFormat.format(sm_res.getString("notInteger"), m_errorMsg);
     else if (m_errorMsg[1] == null)
     {
        if (m_errorMsg[0] == m_rangeMin)
           return MessageFormat.format(sm_res.getString("lessThanMin") + " {0,number,integer}", m_errorMsg);
        else //if (m_errorMsg[0] == "max")
           return MessageFormat.format(sm_res.getString("moreThanMax") + " {0,number,integer}", m_errorMsg);
     }
     else
        return MessageFormat.format(sm_res.getString("notInRange") + " {0, number,integer} " + sm_res.getString("and") + " {1,number,integer} " + sm_res.getString("inclusive"), m_errorMsg);
  }

  // implementing definition from interface ValidationConstraint
  public void checkComponent(Object suspect) throws ValidationException
  {
     Integer value;
     // clear old msg if any
     m_errorMsg[0] = null;
     m_errorMsg[1] = null;

  // Validating the input for correct integer input (ie: no characters or special
  // characters.
     if (suspect instanceof JTextField)
     {
        try {
            value = new Integer(((JTextField)suspect).getText());
        }
        catch (NumberFormatException e)
        {
           throw new ValidationException();
        }
     }
     else if (suspect instanceof JComboBox)
     {
        try {
            value = new Integer(((JComboBox)suspect).getEditor().getItem().toString());
        }
        catch (NumberFormatException e)
        {
           throw new ValidationException();
        }
     }
     else   // should never happen...  so throw ValidationException!
         value = new Integer(0);

  // Making sure the value is within range provided in constraints
   if (m_rangeMax != null || m_rangeMin != null)
   {
     if (m_rangeMax.intValue() == Integer.MAX_VALUE) // only minimum is set
     {
        if (m_rangeMin.intValue() > value.intValue())
        {
           m_errorMsg[0] = m_rangeMin;
           throw new ValidationException();
        }
     }
     else if (m_rangeMin.intValue() == Integer.MIN_VALUE) // only maximum is set
     {
        if (m_rangeMax.intValue() < value.intValue())
        {
           m_errorMsg[0] = m_rangeMax;
           throw new ValidationException();
        }
     }
     else   // have both max and min
     {
        if ((m_rangeMin.intValue() > value.intValue()) || (m_rangeMax.intValue() < value.intValue()))
        {
           m_errorMsg[0] = m_rangeMin;
           m_errorMsg[1] = m_rangeMax;
           throw new ValidationException();
        }
     }
   }
  }   // End method checkComponent()

  private Integer  m_rangeMax = null;
  private Integer  m_rangeMin = null;
  private Object[] m_errorMsg = {null, null};
}

 
