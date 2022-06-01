/******************************************************************************
 *
 * [ PSControlPropertiesDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form.ce;

import com.percussion.client.PSModelException;
import com.percussion.design.objectstore.PSControlMeta;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.editors.dialog.PSDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

public class PSControlPropertiesDialog extends PSDialog
{

   public PSControlPropertiesDialog(Shell parentShell,
         PSFieldTableRowDataObject rowData, int editorType)
   {
      super(parentShell);
      if (rowData == null)
      {
         throw new IllegalArgumentException("fieldData must not be null");
      }
      if (!PSContentEditorDefinition.isValidEditorType(editorType))
      {
         throw new IllegalArgumentException("editorType is invalid");
      }
      try
      {
         m_controlMeta = PSContentEditorDefinition.getControl(rowData
               .getControlName());
         if(m_controlMeta == null)
         {
            throw new IllegalArgumentException("invalid control");
         }
      }
      catch (PSModelException e)
      {
         throw new IllegalArgumentException("unable get the control details");
      }

      m_rowData = rowData;
      m_editorType = editorType;
   }

   @Override
   protected Control createDialogArea(Composite parent)
   {
      
      Composite m_controlComp = new Composite(parent, SWT.NONE);
      m_controlComp.setLayout(new FormLayout());
      if(m_controlMeta.getChoiceSet().equals(PSControlMeta.CHOICES_NONE))
      {
         m_controlMainComp = new PSControlMainTabComposite(
               m_controlComp,SWT.NONE,m_rowData,m_editorType);
         final FormData formData = new FormData();
         formData.top = new FormAttachment(0, 0);
         formData.left = new FormAttachment(0, 0);
         formData.right = new FormAttachment(100, 0);
         formData.bottom = new FormAttachment(100, 0);
         m_controlMainComp.setLayoutData(formData);
      }
      else
      {
         m_mainTabFolder = new TabFolder(m_controlComp, SWT.TOP);
         final FormData formData = new FormData();
         formData.top = new FormAttachment(0, 0);
         formData.left = new FormAttachment(0, 0);
         formData.right = new FormAttachment(100, 0);
         formData.bottom = new FormAttachment(100, 0);
         m_mainTabFolder.setLayoutData(formData);

         m_controlMainTab = new TabItem(m_mainTabFolder, SWT.NONE);
         m_controlMainTab.setText(PSMessages
               .getString("PSControlPropertiesDialog.tab.control.title"));
         m_controlMainComp = new PSControlMainTabComposite(
               m_mainTabFolder,SWT.NONE,m_rowData,m_editorType);
         m_controlMainTab.setControl(m_controlMainComp);

         m_controlChoicesTab = new TabItem(m_mainTabFolder, SWT.NONE);
         m_controlChoicesTab.setText(PSMessages
               .getString("PSControlPropertiesDialog.tab.choices.title"));
         m_controlChoicesComp = new PSControlChoicesTabComposite(
               m_mainTabFolder, SWT.NONE, m_rowData);
         m_controlChoicesTab.setControl(m_controlChoicesComp);
         
      }
      GridData data = new GridData(GridData.FILL_HORIZONTAL
         | GridData.FILL_VERTICAL | GridData.VERTICAL_ALIGN_FILL);
      m_controlComp.setLayoutData(data);
      m_controlComp.pack();
      return m_controlComp;
   }   
   
   @Override
   protected void okPressed()
   {
      m_controlMainComp.updateData();
      if(!m_controlMeta.getChoiceSet().equals(PSControlMeta.CHOICES_NONE))
         m_controlChoicesComp.updateData();
      super.okPressed();
   }
   
   @Override
   protected void configureShell(Shell newShell)
   {
     super.configureShell(newShell);
     String dialogTitle = PSMessages.getString(
            "PSControlPropertiesDialog.title", new Object[] { m_rowData
                  .getControlName() });
     newShell.setText(dialogTitle);
   }
   
   @Override
   protected Point getInitialSize()
   {
      return new Point(500,800);
   }
   private PSFieldTableRowDataObject m_rowData;
   private int m_editorType;
   private TabFolder m_mainTabFolder;
   private TabItem m_controlMainTab;
   private PSControlMainTabComposite m_controlMainComp;
   private PSControlChoicesTabComposite m_controlChoicesComp;
   private TabItem m_controlChoicesTab;
   private PSControlMeta m_controlMeta;
   
}
