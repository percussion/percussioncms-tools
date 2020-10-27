/******************************************************************************
 *
 * [ PSTemplateTdSettingsPage.java ]
 * 
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.workbench.ui.editors.wizards;

import com.percussion.client.objectstore.PSUiAssemblyTemplate;
import com.percussion.workbench.ui.IPSUiConstants;
import com.percussion.workbench.ui.editors.common.PSEditorUtil;
import com.percussion.workbench.ui.editors.common.PSTdBuilderComposite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.xml.sax.SAXException;

import java.io.IOException;

/**
 * @author erikserating
 *
 */
public class PSTemplateTdSettingsPage extends PSWizardPageBase 
   implements IPSUiConstants 
{
   
   public PSTemplateTdSettingsPage()
   {
      super(getMessage("PSTemplateWizard.title"));     //$NON-NLS-1$
      setTitle(getMessage("PSTemplateWizard.screenTitle")); //$NON-NLS-1$
   }

   /* (non-Javadoc)
    * @see com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater#updateDesignerObject(java.lang.Object, java.lang.Object)
    */
   public void updateDesignerObject(Object designObject, Object control)
   {
      if(control == m_comp.getResourceCombo())
      {
         final PSUiAssemblyTemplate template = 
            (PSUiAssemblyTemplate) designObject;
         try
         {
            String xml = m_comp.generateSchemaXml();
            if(xml != null)
            {
               String datasource = m_comp.getResourceCombo().getItem(
                  m_comp.getResourceCombo().getSelectionIndex());
               template.setTemplate(xml);
               PSEditorUtil.updateTemplateDbBindings(datasource, template);
            }
         }
         catch (IOException e)
         {
            // FIXME Auto-generated catch block
            e.printStackTrace();
         }
         catch (SAXException e)
         {
            // FIXME Auto-generated catch block
            e.printStackTrace();
         }   
      }

   }
  
   /* (non-Javadoc)
    * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
    */
   public void createControl(Composite parent)
   {
      final Composite container = new Composite(parent, SWT.NULL);
      container.setLayout(new FormLayout());
      setControl(container);
      m_comp = new PSTdBuilderComposite(container);
      final FormData formData = new FormData();
      formData.left = new FormAttachment(0, COMMON_BORDER_OFFSET);
      formData.right = new FormAttachment(100, -COMMON_BORDER_OFFSET);
      formData.top = new FormAttachment(0, WIZARD_TOP_OFFSET);
      m_comp.setLayoutData(formData);
      
      Combo combo = m_comp.getResourceCombo();
      registerControl("Data Resource", combo, null);
      
   }   
   
   private PSTdBuilderComposite m_comp;
 
}
