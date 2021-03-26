/******************************************************************************
 *
 * [ PSControlValueTextWhitespaceValidator.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.validators;

import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.util.PSControlInfo;
import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

/**
 * A validator used for the <code>Text</code> control or any
 * of its subclasses to validate that no whitespace exists within
 * the string.
 * @author erikserating
 */
public class PSControlValueTextWhitespaceValidator 
   implements IPSControlValueValidator
{
   
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
      String value = ((Text) control).getText();
      if(!value.equals(StringUtils.deleteWhitespace(value)))
      {
         Object[] args = new Object[]{
            controlInfo.getDisplayName()};
         return PSMessages.getString(
            "PSControlValueTextWhitespaceValidator.error.no.whitespace", //$NON-NLS-1$
            args); 
      }
      return null;
   }
        

}
