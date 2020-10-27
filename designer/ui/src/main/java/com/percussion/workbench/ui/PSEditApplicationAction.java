/******************************************************************************
 *
 * [ PSEditApplicationAction.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui;

import com.percussion.E2Designer.E2Designer;
import com.percussion.client.PSObjectType;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.impl.PSReference;
import com.percussion.workbench.ui.legacy.PSLegacyInitialzer;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;

public class PSEditApplicationAction extends Action implements IWorkbenchAction
{
   public PSEditApplicationAction(final IWorkbenchWindow workbenchWindow)
   {
      if (workbenchWindow == null)
         throw new IllegalArgumentException();
      m_workbenchWindow = workbenchWindow;
   }

   @Override
   public void run()
   {
      try
      {
         PSLegacyInitialzer.initializeLegacySystems();
         if (E2Designer.getDesignerConnection() == null
               || !E2Designer.getDesignerConnection().isConnected())
         {
            MessageDialog.openError(m_workbenchWindow.getShell(), "Error",
                  "Connect to the server first!");
            return;
         }
         
         final PSReference ref = new PSReference();
         ref.setPersisted();
         ref.setObjectType(new PSObjectType(PSObjectTypes.XML_APPLICATION,
               PSObjectTypes.XmlApplicationSubTypes.SYSTEM));

         final MessageDialog dlg = new MessageDialog(m_workbenchWindow
               .getShell(), "Request", null,
               "Please enter the application name:",
               MessageDialog.INFORMATION,
               new String[] { IDialogConstants.OK_LABEL,
                     IDialogConstants.CANCEL_LABEL }, 0)
         {
            @Override
            protected Control createCustomArea(Composite parent)
            {
               final Composite container = new Composite(parent, SWT.NULL);
               container.setLayout(new FormLayout());

               m_appNameField = new Text(container, SWT.SINGLE | SWT.BORDER);
               final FormData formData = new FormData();
               formData.left = new FormAttachment(0, 5);
               formData.width = 400;
               m_appNameField.setLayoutData(formData);

               m_appNameField.addModifyListener(new ModifyListener()
               {
                  @SuppressWarnings("unused")
                  public void modifyText(ModifyEvent e)
                  {
                     ref.setName(m_appNameField.getText());
                  }
               });
               return container;
            }
         };
         if (dlg.open() == 0)
         {
            
            // default "sys_pubSupport"
            // application with stylesheet sources - rxs_AutoIndex_cas
            IWorkbenchPage page = m_workbenchWindow.getActivePage();
            PSEditorRegistry.getInstance().findEditorFactory(
                  ref.getObjectType()).openEditor(page, ref);
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
         PSWorkbenchPlugin.handleException(null, null, null, e);
      }
   }

   // does nothing
   public void dispose() {}
   
   private final IWorkbenchWindow m_workbenchWindow;
   
   private Text m_appNameField;
}
