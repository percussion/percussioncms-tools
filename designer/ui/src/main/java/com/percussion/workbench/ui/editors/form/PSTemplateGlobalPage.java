/******************************************************************************
 *
 * [ PSTemplateGlobalPage.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form;

import com.percussion.client.objectstore.PSUiAssemblyTemplate;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * Properties page for global templates. 
 *
 * @author Andriy Palamarchuk
 */
public class PSTemplateGlobalPage extends PSTemplatePropertiesPageBase
// WB-1237
{
   /**
    * Creates new page.
    */
   public PSTemplateGlobalPage(Composite parent, int style, PSEditorBase editor)
   {
      super(parent, style, editor);
      
      final Label mimeTypeLabel = createTopLabel(m_leftPane, PSMimeTypeHelper.LABEL);
      m_mimeTypeHelper.setCombo(createTopComboWithLabel(m_leftPane, mimeTypeLabel));
      
      final Label characterSetLabel = createTopLabel(m_rightPane, PSCharSetHelper.LABEL);
      m_charSetHelper.setCombo(createTopComboWithLabel(m_rightPane, characterSetLabel));
   }

   @Override
   public void loadControlValues(final PSUiAssemblyTemplate template)
   {
      super.loadControlValues(template);
      m_mimeTypeHelper.loadControlValues(template);
      m_charSetHelper.loadControlValues(template);
   }

   @Override
   public void updateTemplate(PSUiAssemblyTemplate template)
   {
      super.updateTemplate(template);
      m_mimeTypeHelper.updateTemplate(template);
      m_charSetHelper.updateTemplate(template);
   }

   @Override
   protected String getNamePrefix()
   {
      return getMessage("PSTemplateGlobalPage.label.name"); //$NON-NLS-1$
   }

   /**
    * Manages the mime types dropdown.
    */
   final PSMimeTypeHelper m_mimeTypeHelper = new PSMimeTypeHelper();

   /**
    * Manages the char sets dropdown.
    */
   final PSCharSetHelper m_charSetHelper = new PSCharSetHelper();
}
