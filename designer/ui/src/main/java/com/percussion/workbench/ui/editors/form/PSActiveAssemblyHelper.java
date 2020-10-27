/******************************************************************************
 *
 * [ PSActiveAssemblyHelper.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form;

import com.percussion.client.objectstore.PSUiAssemblyTemplate;
import com.percussion.services.assembly.IPSAssemblyTemplate.AAType;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.controls.PSRadioAndCheckBoxes;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * Manages UI to select active assembly format for templates.
 *
 * @author Andriy Palamarchuk
 */
public class PSActiveAssemblyHelper
{
   /**
    * Initiates UI from the template.
    */
   public void loadControlValues(PSUiAssemblyTemplate template)
   {
      getRadio().setSelection(
            AATypeChoice.valueOf(template.getActiveAssemblyType()).ordinal());
   }

   /**
    * Sets template properties specified by UI.
    */
   public void updateTemplate(PSUiAssemblyTemplate template)
   {
      template.setActiveAssemblyType(
            AATypeChoice.valueOf(getRadio().getSelectedIndex()).getAAType());
   }

   /**
    * Creates UI controls.
    */
   public void initRadio(final Composite container)
   {
      m_radio =
         new PSRadioAndCheckBoxes(container,
               LABEL,
               SWT.SEPARATOR | SWT.HORIZONTAL | SWT.RADIO);

      m_radio.addEntry(getMessage("PSTemplateGeneralPage.assemblyFormat.normal")); //$NON-NLS-1$
      m_radio.addEntry(getMessage("PSTemplateGeneralPage.assemblyFormat.autoIndex")); //$NON-NLS-1$
      m_radio.addEntry(getMessage("PSTemplateGeneralPage.assemblyFormat.noHtml")); //$NON-NLS-1$
      m_radio.layoutControls();
   }
   
   /**
    * Convenience method to get string resource.
    */
   private static String getMessage(final String key)
   {
      return PSMessages.getString(key);
   }

   /**
    * The radio button for publishing type selection.
    */
   public PSRadioAndCheckBoxes getRadio()
   {
      return m_radio;
   }

   /**
    * Active assembly format button indexes.
    */
   enum AATypeChoice {
      NORMAL(AAType.Normal),
      AUTOINDEX(AAType.AutoIndex),
      NONHTML(AAType.NonHtml);

      private AATypeChoice(final AAType type)
      {
         m_type = type;
      }
      
      /**
       * Choice associated with the format.
       */
      private static AATypeChoice valueOf(AAType type)
      {
         for (final AATypeChoice format : values())
         {
            if (format.m_type.equals(type))
            {
               return format;
            }
         }
         throw new IllegalArgumentException(
               "There is no choice for output format " + type); //$NON-NLS-1$
      }

      /**
       * Format choice on the given index. 
       */
      private static AATypeChoice valueOf(int ordinal)
      {
         for (AATypeChoice format : values())
         {
            if (format.ordinal() == ordinal)
            {
               return format;
            }
         }
         throw new IndexOutOfBoundsException(
               "Index " + ordinal + " was not found in enum " + AATypeChoice.class); //$NON-NLS-1$ //$NON-NLS-2$
      }
      
      /**
       * Output format associated with this button.
       */
      private AAType getAAType()
      {
         return m_type;
      }

      private final AAType m_type;
   }
   
   /**
    * The radio control label.
    */
   public static final String LABEL =
         getMessage("PSTemplateGeneralPage.label.activeAssemblyFormat"); //$NON-NLS-1$
   
   /**
    * Radio group to select active assembly format.
    */
   private PSRadioAndCheckBoxes m_radio;
}
