/******************************************************************************
 *
 * [ PSTemplatePropertiesPageBase.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form;

import com.percussion.client.objectstore.PSUiAssemblyTemplate;
import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * Base class for the template editor property pages -
 * the first page of the editor. Different page is used based on the type of
 * template.
 *
 * @author Andriy Palamarchuk
 */
public abstract class PSTemplatePropertiesPageBase extends PSNameDescPageBase
{
   // see base
   public PSTemplatePropertiesPageBase(Composite parent, int style, PSEditorBase editor)
   {
      super(parent, style, editor);
   }

   /**
    * Load controls with the template values.
    */
   public void loadControlValues(final PSUiAssemblyTemplate template)
   {
      ((Label) m_nameLabelDesc.getNameText()).setText(template.getName());
      m_nameLabelDesc.getLabelText().setText(template.getLabel());
      m_nameLabelDesc.getDescriptionText().setText(
            StringUtils.defaultString(template.getDescription()));
   }

   /**
    * Updates template with the data provided by UI. 
    */
   public void updateTemplate(final PSUiAssemblyTemplate template)
   {
      if (StringUtils.isBlank(m_nameLabelDesc.getLabelText().getText()))
      {
         template.setLabel(template.getName());
      }
      else
      {
         template.setLabel(m_nameLabelDesc.getLabelText().getText());
      }
      template.setDescription(m_nameLabelDesc.getDescriptionText().getText());
   }
}
