/******************************************************************************
 *
 * [ PSControlValueRequiredValidator.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.validators;

import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.controls.PSMuttBoxControl;
import com.percussion.workbench.ui.controls.PSRadioAndCheckBoxes;
import com.percussion.workbench.ui.controls.PSSlushBucket;
import com.percussion.workbench.ui.util.PSControlInfo;
import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;

/**
 * Validates that a control value exists or is in other words required.
 * This validator works <code>Combo</code>, Slushbucket, MuttBox,
 * <code>List</code> and
 * <code>Text</code> controls or any of their subclasses.
 * @author erikserating
 *
 */
public class PSControlValueRequiredValidator 
   implements IPSControlValueValidator
{

   /* 
    * @see com.percussion.workbench.ui.validators.IPSControlValueValidator
    * #validate(com.percussion.workbench.ui.util.PSControlInfo)
    */
   public String validate(PSControlInfo controlInfo)
   {
      if(controlInfo == null)
      throw new IllegalArgumentException("The control cannot be null.");
      
      Control control = controlInfo.getControl();
      boolean hasValue = false;
      if(control instanceof Combo)
      {
         Combo combo = (Combo)control;
         boolean isReadOnly = (combo.getStyle() & SWT.READ_ONLY) != 0;
         if(combo.getSelectionIndex() != -1)
            hasValue = true;
         else if(!isReadOnly && StringUtils.isNotBlank(combo.getText()))
            hasValue = true;
      }
      else if(control instanceof List )
      {
         if(((List)control).getSelectionCount() > 0)
            hasValue = true;
      }
      else if (control instanceof Text)
      {
         if(((Text)control).getText().trim().length() > 0)
            hasValue = true;
      }
      else if (control instanceof PSSlushBucket)
      {
         if(!((PSSlushBucket)control).getSelections().isEmpty())
            hasValue = true;
      }
      else if (control instanceof PSRadioAndCheckBoxes)
      {
         if(((PSRadioAndCheckBoxes)control)
            .getSelectedIndices() != null)
            hasValue = true;
      }
      else if(control instanceof PSMuttBoxControl)
      {
         if(((PSMuttBoxControl)control)
            .getSelections().size() > 0)
            hasValue = true;
      }
      else
      {
         throw new IllegalArgumentException(
            "The control must be a Combo, List, Slushbucket, Radio/Check" //$NON-NLS-1$
            + " box, MuttBox, or Text type control"); //$NON-NLS-1$
      }
      if(!hasValue)
      {
         Object[] args = new Object[]{controlInfo.getDisplayName()};
         return PSMessages.getString(
            "PSControlValueRequiredValidator.error.required", args); //$NON-NLS-1$
      }
      return null;
   }

   

}
