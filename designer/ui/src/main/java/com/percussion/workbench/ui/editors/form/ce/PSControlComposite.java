/******************************************************************************
 *
 * [ PSControlComposite.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form.ce;

import com.percussion.client.PSModelException;
import com.percussion.design.objectstore.PSControlMeta;
import com.percussion.workbench.ui.PSWorkbenchPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

public class PSControlComposite extends Composite
{

   public PSControlComposite(Composite parent, int style, PSFieldTableRowDataObject rowData, int editorType) 
   {
      super(parent, style);
      if(rowData == null)
      {
         throw new IllegalArgumentException("rowData must not be null");
      }
      if (!PSContentEditorDefinition.isValidEditorType(editorType))
      {
         throw new IllegalArgumentException("editorType is invalid");
      }
      m_rowData = rowData;
      PSControlMeta ctrl = null;
      try
      {
         ctrl = PSContentEditorDefinition.getControl(m_rowData
               .getControlName());
      }
      catch (PSModelException e)
      {
         String title = "Failed to get the control";
         String msg = "The following error occured while getting the control";
         PSWorkbenchPlugin.handleException("Control Choices Dialog",title, msg,e);
      }
      setLayout(new FormLayout());
      if(ctrl.getChoiceSet().equals(PSControlMeta.CHOICES_NONE))
      {
         
      }
      else
      {
         m_mainTabFolder = new TabFolder(this, SWT.TOP);
         final FormData formData = new FormData();
         formData.top = new FormAttachment(0, 0);
         formData.left = new FormAttachment(0, 0);
         formData.right = new FormAttachment(100, 0);
         formData.bottom = new FormAttachment(100, 0);
         m_mainTabFolder.setLayoutData(formData);

         m_controlMainTab = new TabItem(m_mainTabFolder, SWT.NONE);
         m_controlMainTab.setText("Control");
         //m_controlMainComp = new PSControlMainTabComposite(m_mainTabFolder,SWT.NONE,m_rowData,m_editorType,this);
         m_controlMainTab.setControl(m_controlMainComp);
      }
   }
   public void setData(PSFieldTableRowDataObject fieldData)
   {
      if (fieldData == null)
      {
         throw new IllegalArgumentException("fieldData must not be null");
      }
      PSControlMeta ctrl = null;
      try
      {
         ctrl = PSContentEditorDefinition.getControl(fieldData
               .getControlName());
         if(ctrl.getChoiceSet().equals(PSControlMeta.CHOICES_NONE))
            createChoices();
      }
      catch (PSModelException e)
      {
         String title = "Failed to get the control";
         String msg = "The following error occured while getting the control";
         PSWorkbenchPlugin.handleException("Control Choices Dialog",title, msg,e);
      }
   }
   public void updateData()
   {
      m_controlMainComp.updateData();
   }
   public PSFieldTableRowDataObject getData()
   {
      return null;
   }
   public void disposeChoices()
   {
      if(m_controlChoicesTab!= null && !m_controlChoicesTab.isDisposed())
         m_controlChoicesTab.dispose();
   }
   public void createChoices()
   {
      if(m_controlChoicesTab==null || m_controlChoicesTab.isDisposed())
      {
         m_controlChoicesTab = new TabItem(m_mainTabFolder, SWT.NONE);
         m_controlChoicesTab.setText("Choices");
         m_controlChoicesComp = new PSControlChoicesTabComposite(m_mainTabFolder,SWT.NONE,m_rowData);
         m_controlChoicesTab.setControl(m_controlChoicesComp);
      }
   }

   public void dispose()
   {
      super.dispose();
   }

   protected void checkSubclass()
   {
   }
   /**
    * Tab folder and items and corresponding composites
    */
   private TabFolder m_mainTabFolder;
   private TabItem m_controlMainTab;
   private PSControlMainTabComposite m_controlMainComp;
   private PSControlChoicesTabComposite m_controlChoicesComp;
   private TabItem m_controlChoicesTab;
   private PSFieldTableRowDataObject m_rowData;
}

