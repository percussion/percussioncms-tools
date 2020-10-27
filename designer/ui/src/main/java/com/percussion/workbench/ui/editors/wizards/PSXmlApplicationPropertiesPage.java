/******************************************************************************
 *
 * [ PSXmlApplicationPropertiesPage.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.wizards;

import com.percussion.E2Designer.UIMainFrame;
import com.percussion.design.objectstore.PSApplication;
import com.percussion.workbench.ui.IPSUiConstants;
import com.percussion.workbench.ui.controls.PSNameLabelDesc;
import com.percussion.workbench.ui.util.PSControlInfo;
import com.percussion.workbench.ui.util.PSHelpHintKeyHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

/**
 * First page of application wizard - wizard properties.
 *
 * @author Andriy Palamarchuk
 */
public class PSXmlApplicationPropertiesPage extends PSWizardPageBase
   implements IPSUiConstants
{

   public PSXmlApplicationPropertiesPage()
   {
      super(getMessage("PSXmlApplicationPropertiesPage.name"));  //$NON-NLS-1$
      setTitle(getMessage("PSXmlApplicationPropertiesPage.title"));  //$NON-NLS-1$
   }

   public void createControl(Composite parent)
   {
      final Composite container = new Composite(parent, SWT.NULL);
      container.setLayout(new FormLayout());
      setControl(container);
      m_nameDescComposite = createNameDescComposite(container);
   }

   /**
    * Creates UI control to handle name and description. 
    */
   private PSNameLabelDesc createNameDescComposite(final Composite container)
   {
      final PSNameLabelDesc composite = new PSNameLabelDesc(container, SWT.NONE,
               getMessage("PSXmlApplicationPropertiesPage.label.name"),
               WIZARD_LABEL_NUMERATOR, 
               PSNameLabelDesc.SHOW_NAME | PSNameLabelDesc.SHOW_DESC, this);
      {
         final FormData formData = new FormData();
         formData.left = new FormAttachment(0, 0);
         formData.right = 
            new FormAttachment(WIZARD_VALUE_NUMERATOR, -COMMON_BORDER_OFFSET);
         formData.top = new FormAttachment(0, WIZARD_TOP_OFFSET);
         composite.setLayoutData(formData);
      }
      return composite;
   }

   /* 
    * @see com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater#
    * updateDesignerObject(java.lang.Object, java.lang.Object)
    */
   public void updateDesignerObject(Object designObject, Object control)
   {
      final PSApplication application = (PSApplication) designObject;
      if(control == m_nameDescComposite.getNameText())
      {
         UIMainFrame.initApplication(application,
               ((Text)m_nameDescComposite.getNameText()).getText());
         
      }
      else if(control == m_nameDescComposite.getDescriptionText())
      {
         application.setDescription(m_nameDescComposite.getDescriptionText().getText());
      }
      else
      {
         throw new IllegalArgumentException("Unexpected control: " + control);
      }
   }
   
   /* 
    * @see com.percussion.workbench.ui.editors.form.PSEditorBase#
    * getHelpHintKey(com.percussion.workbench.ui.util.PSControlInfo)
    */
   @Override
   protected String getHelpHintKey(PSControlInfo controlInfo)
   {
      if(m_helpHintKeyHelper == null)
      {
         m_helpHintKeyHelper = new PSHelpHintKeyHelper(new String[]
         {
            PSNameLabelDesc.DESC_TEXT_KEY,
               "description",
            PSNameLabelDesc.NAME_TEXT_KEY,
               "application_name"
         });
      }
      return m_helpHintKeyHelper.getKey(
         super.getHelpHintKey(controlInfo), controlInfo);
   }
   
   /**
    * Cache of the help hint helper object
    */
   private PSHelpHintKeyHelper m_helpHintKeyHelper;
   
   /**
    * Composite managing name and description data.
    */
   PSNameLabelDesc m_nameDescComposite;
}
