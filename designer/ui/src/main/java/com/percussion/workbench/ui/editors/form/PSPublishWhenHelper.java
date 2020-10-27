/******************************************************************************
 *
 * [ PSPublishWhenHelper.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form;

import com.percussion.client.objectstore.PSUiAssemblyTemplate;
import com.percussion.services.assembly.IPSAssemblyTemplate.PublishWhen;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.controls.PSRadioAndCheckBoxes;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * Manages UI to select publishing option for templates.
 * Is created to share mime types management logic between different forms.
 *
 * @author Andriy Palamarchuk
 */
public class PSPublishWhenHelper
{
   /**
    * Creates UI controls.
    */
   public void initRadio(final Composite container)
   {
      m_radio =
         new PSRadioAndCheckBoxes(container,
               LABEL, SWT.SEPARATOR | SWT.HORIZONTAL | SWT.RADIO);

      m_radio.addEntry(getMessage("PSPublishWhenHelper.publish.always")); //$NON-NLS-1$
      m_radio.addEntry(getMessage("PSPublishWhenHelper.publish.default")); //$NON-NLS-1$
      m_radio.addEntry(getMessage("PSPublishWhenHelper.publish.never")); //$NON-NLS-1$
      m_radio.layoutControls();
   }
   
   /**
    * Initiates UI from the template.
    */
   public void loadControlValues(final PSUiAssemblyTemplate template)
   {
      if (template.getPublishWhen().equals(PublishWhen.Unspecified))
      {
         m_radio.setSelection(PublishWhenChoice.ALWAYS.ordinal());
      }
      else
      {
         m_radio.setSelection(
               PublishWhenChoice.valueOf(template.getPublishWhen()).ordinal());
      }
   }
   
   /**
    * Sets template properties specified by UI.
    */
   public void updateTemplate(final PSUiAssemblyTemplate template)
   {
      template.setPublishWhen(
            PublishWhenChoice.valueOf(m_radio.getSelectedIndex()).getPublishWhen());
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
    * Publish button indexes.
    */
   public enum PublishWhenChoice {
      ALWAYS(PublishWhen.Always),
      DEFAULT(PublishWhen.Default),
      NEVER(PublishWhen.Never);

      private PublishWhenChoice(final PublishWhen publishWhen)
      {
         m_publishWhen = publishWhen;
      }
      
      /**
       * Choice associated with the format.
       */
      private static PublishWhenChoice valueOf(PublishWhen publishWhen)
      {
         for (final PublishWhenChoice format : values())
         {
            if (format.m_publishWhen.equals(publishWhen))
            {
               return format;
            }
         }
         throw new IllegalArgumentException(
               "There is no choice for output format " + publishWhen); //$NON-NLS-1$
      }

      /**
       * Format choice on the given index. 
       */
      private static PublishWhenChoice valueOf(int ordinal)
      {
         for (PublishWhenChoice format : values())
         {
            if (format.ordinal() == ordinal)
            {
               return format;
            }
         }
         throw new IndexOutOfBoundsException(
               "Index " + ordinal + " was not found in enum " + PublishWhenChoice.class); //$NON-NLS-1$ //$NON-NLS-2$
      }
      
      /**
       * Output format associated with this button.
       */
      private PublishWhen getPublishWhen()
      {
         return m_publishWhen;
      }

      private final PublishWhen m_publishWhen;
   }

   /**
    * The radio label.
    */
   public static final String LABEL = getMessage("PSPublishWhenHelper.label.publish"); //$NON-NLS-1$
   
   /**
    * Radio button group providing publish options choice.
    */
   private PSRadioAndCheckBoxes m_radio;
}
