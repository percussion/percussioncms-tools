/******************************************************************************
 *
 * [ PSLocationHelper.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form;

import com.percussion.client.objectstore.PSUiAssemblyTemplate;
import com.percussion.workbench.ui.PSMessages;
import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import static com.percussion.workbench.ui.IPSUiConstants.LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET;
import static com.percussion.workbench.ui.IPSUiConstants.LABEL_HSPACE_OFFSET;

/**
 * Manages UI to specify location prefix, suffix.
 *
 * @author Andriy Palamarchuk
 */
public class PSLocationHelper
{
   /**
    * Initializes the UI.
    * @return the UI control.
    */
   public Composite initUI(Composite container)
   {
      final Composite pane = new Composite(container, SWT.NONE);
      pane.setLayout(new FormLayout());

      final Label textLabel = new Label(pane, SWT.NONE);
      {
         final FormData formData = new FormData();
         formData.left = new FormAttachment(0, 0);
         formData.top = new FormAttachment(0, 0);
         textLabel.setLayoutData(formData);
      }
      textLabel.setText(getMessage("PSLocationHelper.label.location")); //$NON-NLS-1$
      
      final Label separatorLabel = new Label(pane, SWT.HORIZONTAL | SWT.SEPARATOR);
      {
         FormData formData = new FormData();
         formData.left =
            new FormAttachment(textLabel, LABEL_HSPACE_OFFSET, SWT.RIGHT);
         formData.top = new FormAttachment(textLabel, 0, SWT.CENTER);
         formData.right = new FormAttachment(100, 0);
         separatorLabel.setLayoutData(formData);
      }
      
      initLocationControls(container, pane);
      return pane;
   }
   
   /**
    * Initializes location controls.
    */
   private void initLocationControls(Composite container, Control previousControl)
   {
      final Label prefixLabel = new Label(container, SWT.NONE);
      prefixLabel.setText(PREFIX_LABEL + ':');
      {
         final FormData formData = new FormData();
         formData.left = new FormAttachment(0, INDENT);
         formData.top = new FormAttachment(previousControl,
               LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, SWT.BOTTOM);
         prefixLabel.setLayoutData(formData);
      }
      
      m_prefixText = new Text(container, SWT.BORDER);
      m_prefixText.setTextLimit(PREFIX_SUFFIX_LIMIT);
      {
         final FormData formData = new FormData();
         formData.left = new FormAttachment(prefixLabel, LABEL_HSPACE_OFFSET, SWT.RIGHT);
         formData.top = new FormAttachment(previousControl, 0, SWT.BOTTOM);
         m_prefixText.setLayoutData(formData);
      }
      
      final Label suffixLabel = new Label(container, SWT.NONE);
      suffixLabel.setText(SUFFIX_LABEL + ':');
      {
         final FormData formData = new FormData();
         formData.left = new FormAttachment(50, INDENT);
         formData.top = new FormAttachment(m_prefixText, 0, SWT.TOP);
         suffixLabel.setLayoutData(formData);
      }
      
      m_suffixText = new Text(container, SWT.BORDER);
      m_suffixText.setTextLimit(PREFIX_SUFFIX_LIMIT);
      {
         final FormData formData = new FormData();
         formData.left = new FormAttachment(suffixLabel, LABEL_HSPACE_OFFSET, SWT.RIGHT);
         formData.top = new FormAttachment(previousControl, 0, SWT.BOTTOM);
         m_suffixText.setLayoutData(formData);
      }
   }
   
   /**
    * Loads data to UI controls from the provided template.
    */
   public void loadControlValues(final PSUiAssemblyTemplate template)
   {
      m_prefixText.setText(
            StringUtils.defaultString(template.getLocationPrefix()));
      m_suffixText.setText(
            StringUtils.defaultString(template.getLocationSuffix()));
   }

   /**
    * Reads data from UI controls to the template.
    */
   public void updateTemplate(PSUiAssemblyTemplate template)
   {
      template.setLocationPrefix(m_prefixText.getText());
      template.setLocationSuffix(m_suffixText.getText());
   }

   /**
    * Convenience method to get string resource.
    */
   private static String getMessage(final String key)
   {
      return PSMessages.getString(key);
   }

   /**
    * Prefix text control.
    */
   public Text getPrefixText()
   {
      return m_prefixText;
   }

   /**
    * Suffix text control.
    */
   public Text getSuffixText()
   {
      return m_suffixText;
   }
   
   /**
    * Offset for intended controls.
    */
   private static final int INDENT = LABEL_HSPACE_OFFSET + 16;
   
   /**
    * The limit of the prefix and suffix fields.
    */
   private static final int PREFIX_SUFFIX_LIMIT = 10;

   /**
    * Text for prefix label.
    */
   public static final String PREFIX_LABEL =
         getMessage("PSLocationHelper.label.prefix"); //$NON-NLS-1$
   
   /**
    * Text for suffix label.
    */
   public static final String SUFFIX_LABEL =
         getMessage("PSLocationHelper.label.suffix"); //$NON-NLS-1$

   /**
    * Text control containing location prefix.
    */
   private Text m_prefixText;

   /**
    * Text control containing location suffix.
    */
   private Text m_suffixText;
}
