/******************************************************************************
 *
 * [ PSControlValueFileExistsValidator.java ]
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
import org.eclipse.swt.widgets.Text;

import java.io.File;

/**
 * A validator used for the <code>Text</code> control or any
 * of its subclasses to validate that the context presents an existing
 * file.
 *
 * @author Andriy Palamarchuk
 */
public class PSControlValueFileExistsValidator
      implements IPSControlValueValidator
{

   public String validate(PSControlInfo controlInfo)
   {
      if (controlInfo == null)
      {
         throw new IllegalArgumentException("The control cannot be null.");  //$NON-NLS-1$
      }
      if (!(controlInfo.getControl() instanceof Text))
      {
         throw new IllegalArgumentException(
               "The control must be an instance of the Text control.");  //$NON-NLS-1$
      }

      final String text = ((Text) controlInfo.getControl()).getText();
      if (StringUtils.isBlank(text))
      {
         return null;
      }

      final File file = new File(text);
      final String message = PSMessages.getString(
            "PSControlValueFileExistsValidator.error.not.file", //$NON-NLS-1$
            new Object[]{controlInfo.getDisplayName()});
      if (!file.exists() || !file.isFile() || !file.canRead())
      {
         return message;
      }
      return null;
   }
}
