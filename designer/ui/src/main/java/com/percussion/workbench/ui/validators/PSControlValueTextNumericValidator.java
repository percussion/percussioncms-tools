/******************************************************************************
 *
 * [ PSControlValueTextNumericValidator.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
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
 * of its subclasses to validate that the contents is a
 * numeric value.
 * @author erikserating
 */
public class PSControlValueTextNumericValidator
   implements IPSControlValueValidator
{   

   /* 
    * @see com.percussion.workbench.ui.validators.IPSControlValueValidator
    * #validate(com.percussion.workbench.ui.util.PSControlInfo)
    */
   public String validate(PSControlInfo controlInfo)
   {
      if(controlInfo == null)
         throw new IllegalArgumentException(
            "The control cannot be null.");  //$NON-NLS-1$
      Control control = controlInfo.getControl();
      if(!(control instanceof Text))
         throw new IllegalArgumentException(
            "The control must be an instance of the Text control.");  //$NON-NLS-1$
      String value = ((Text)control).getText();
      if(value.trim().length() == 0)
         return null; // no value so skip validation
      try
      {
         Double.parseDouble(value);
      }
      catch (NumberFormatException e)
      {
         return PSMessages.getString(
               "PSControlValueTextNumericValidator.error.not.numeric", //$NON-NLS-1$
               new Object[]{controlInfo.getDisplayName()}); 
      }

      return null;
   }

}
