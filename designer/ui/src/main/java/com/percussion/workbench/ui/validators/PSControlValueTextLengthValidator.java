/******************************************************************************
*
* [ PSControlValueTextLengthValidator.java ]
*
* COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
* All rights reserved. This material contains unpublished, copyrighted
* work including confidential and proprietary information of Percussion.
*
*****************************************************************************/
package com.percussion.workbench.ui.validators;

import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.util.PSControlInfo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

/**
 * A validator used for the <code>Text</code> control or any
 * of its subclasses to validate that the specified minimum or/and
 * maximum length is correct.
 * @author erikserating
 */
public class PSControlValueTextLengthValidator 
   implements IPSControlValueValidator
{

   /**
    * Ctor to create a <code>Text</code>control validator that validates 
    * minimum and maximum length.
    * 
    * @param min the minimum string length of the control's value
    * , if set to -1 then there is no minimum length constraint.
    * @param max the maximum string length of the control's value
    * , if set to -1 then there is no maximum length constraint.
    */
   public PSControlValueTextLengthValidator(int min, int max)
   {
      m_min = min;
      m_max = max;
   }
   
   /* 
    * @see com.percussion.workbench.ui.validators.IPSControlValueValidator
    * #validate(org.eclipse.swt.widgets.Control)
    */
   public String validate(PSControlInfo controlInfo)
   {
      if(controlInfo == null)
         throw new IllegalArgumentException("The control cannot be null.");  //$NON-NLS-1$
      Control control = controlInfo.getControl();
      if(!(control instanceof Text))
         throw new IllegalArgumentException(
            "The control must be an instance of the Text control.");  //$NON-NLS-1$
      int len =  ((Text)control).getText().trim().length();
      if(m_min > 0 && len < m_min)
      {
         Object[] args = new Object[]{
            controlInfo.getDisplayName(), String.valueOf(m_min)};
         return PSMessages.getString(
            "PSControlValueTextLengthValidator.error.minimum.length",
            args); //$NON-NLS-1$
      }
      if(m_max > 0 && len > m_max)
      {
         Object[] args = new Object[]{
            controlInfo.getDisplayName(), String.valueOf(m_max)};
         return PSMessages.getString(
            "PSControlValueTextLengthValidator.error.maximum.length",
            args); //$NON-NLS-1$
      }
      return null;
   }
   
   public int getMin()
   {
      return m_min;
   }
   
   public int getMax()
   {
      return m_max;
   }
   
   /* 
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object obj)
   {
      if(obj == null)
         return false;
      if(!(obj instanceof PSControlValueTextLengthValidator))
         return false;
      PSControlValueTextLengthValidator o = 
         (PSControlValueTextLengthValidator)obj;
      if(m_min == o.getMin() && m_max == o.getMax())
         return true;
      return false;
   }
   
   /* 
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode()
   {
     return m_min + m_max;
   }
   
   
   /**
    * The minimum length value
    */
   private int m_min = -1;
   
   /**
    * The maximum length value
    */
   private int m_max = -1;

   

   
      

}
