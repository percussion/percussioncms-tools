/******************************************************************************
 *
 * [ PSConnectionsDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.connection;

import com.percussion.client.PSConnectionInfo;
import com.percussion.workbench.config.PSUiConfigManager;
import com.percussion.workbench.connections.PSUserConnection;
import com.percussion.workbench.connections.PSUserConnectionSet;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.editors.dialog.PSTitleAreaDialog;
import com.percussion.workbench.ui.util.PSErrorDialog;
import com.percussion.workbench.ui.util.PSUiUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

/**
 * Dialog box for user to specify or choose the details to connect to a server.
 * It makes use of another composite to render the dialog area.
 * 
 * @version 6.0
 * @created 09-Sep-2005 2:49:09 PM
 */
public class PSConnectionsDialog extends PSTitleAreaDialog
{
   /**
    * @param parent The parent shell for the dialog.
    * @param closeButtonTextKey The close button may have a different label
    * depending upon the context in which is was called. This value is a key
    * that can be passed to the {@link PSMessages#getString(String)} method.
    * Never <code>null</code> or empty. 
    */
   public PSConnectionsDialog(Shell parent, String closeButtonTextKey)
   {
      super(parent);
      if (StringUtils.isBlank(closeButtonTextKey))
      {
         throw new IllegalArgumentException(
               "closeButtonTextKey cannot be null or empty");  
      }
      m_closeButtonTextKey = closeButtonTextKey;
      setShellStyle(getShellStyle() | SWT.RESIZE);
   }

   /**
    * Error message to display in the title area. Must be set before calling
    * open() method of this object. If this message was set prior to calling
    * open() method it will be displayed in the title area as an error,
    * otherwise the default text {@link #DEFAULT_MSG} will be displayed.
    * 
    * @param errMsg error message to display, must not be <code>null</code>.
    */
   public void setErrorMsg(String errMsg)
   {
      if (StringUtils.isEmpty(errMsg))
      {
         throw new IllegalArgumentException("errMsg must not be null or empty"); //$NON-NLS-1$
      }
      m_errorMessage = errMsg;
   }

   /**
    * Set the connection info object used to earlier. Thus is used to set the
    * password on the connection in the connection list being configured which
    * were read from the persistence storage and may not contain the password.
    * 
    * @param lastConn
    */
   public void setLastConnection(PSConnectionInfo lastConn)
   {
      m_lastConnection = lastConn;
   }

   @Override
   protected Control createDialogArea(Composite parent)
   {
      getShell().setText(PSMessages.getString("PSConnectionsDialog.title")); //$NON-NLS-1$
      getShell().setImage(PSUiUtils.getImage("icons/connect16.gif"));
      setTitleImage(PSUiUtils.getImage("icons/connect.gif"));
      PSUserConnectionSet conns = PSUiConfigManager.getInstance()
         .getUserConnections();
      PSUserConnection lastUsedConn = null;
      if (m_lastConnection != null)
      {
         lastUsedConn = conns.getConnectionByName(m_lastConnection.getName());
         if (lastUsedConn != null)
            lastUsedConn.setClearTextPassword(m_lastConnection
               .getClearTextPassword());
      }
      
      m_connectionsComposite = new PSConnectionsComposite(parent, SWT.NULL,
         conns, lastUsedConn, this);
      
      GridData data = new GridData(GridData.FILL_HORIZONTAL
         | GridData.FILL_VERTICAL | GridData.VERTICAL_ALIGN_FILL);
      m_connectionsComposite.setLayoutData(data);

      if (m_errorMessage != null)
      {
         setErrorMessage(""); //$NON-NLS-1$
         setErrorMessage(m_errorMessage);
      }
      else
      {
         setMessage(DEFAULT_MSG);
      }

      return m_connectionsComposite;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.jface.dialogs.Dialog#createButton(org.eclipse.swt.widgets.Composite,
    * int, java.lang.String, boolean)
    */
   @Override
   protected Button createButton(Composite parent, int id, String label,
      boolean defaultButton)
   {
      boolean connectButton = false;
      if (IDialogConstants.OK_LABEL.equals(label))
      {
         label = PSMessages
            .getString("PSConnectionsDialog.label.button.connect"); //$NON-NLS-1$
         connectButton = true;
      }
      else if (IDialogConstants.CANCEL_LABEL.equals(label))
      {
         label = PSMessages.getString(m_closeButtonTextKey);
      }
      Button b = super.createButton(parent, id, label, defaultButton);
      if (connectButton)
      {
         //set the proper enablement, then it is managed by a child
         m_connectionsComposite.configureEnablement();
      }
      return b;
   }

   /**
    * Should perform validation and transfer data from the dialog to the object.
    * 
    * @return <code>true</code> if validation succeeded and the object is
    * ready to save, <code>false</code> otherwise. In the latter case, the
    * user should be notified of the problem.
    */
   protected boolean accepted()
   {
      return false;
   }

   private boolean okToClose()
   {
      if (m_connectionsComposite.isDirty())
      {
         MessageBox messageBox = new MessageBox(getParentShell(),
            SWT.ICON_WARNING | SWT.YES | SWT.NO | SWT.CANCEL);
         String msgKey = m_connectionsComposite.isValid() 
            ? "PSConnectionsDialog.prompt.save_before_closing" //$NON-NLS-1$
            : "PSConnectionsDialog.prompt.saveWithInvalidConnection"; //$NON-NLS-1$
         messageBox.setMessage(PSMessages.getString(msgKey)); 
         messageBox.setText(PSMessages.getString(
               "PSConnectionsDialog.msgbox.save_before_closing.title")); //$NON-NLS-1$
         int response = messageBox.open();
         switch (response)
         {
            case SWT.YES:
               m_connectionsComposite.applyChanges();
               break;
            case SWT.CANCEL:
               return false;
         }
      }
      return true;
   }

   @Override
   public boolean close()
   {
      if (okToClose())
         super.close();
      return false;
   }

   @Override
   protected void okPressed()
   {
      m_connectionsComposite.applyChanges();
      try
      {
         super.okPressed();
      }
      catch (Exception e)
      {
         StringBuilder sb = new StringBuilder();
         sb
            .append(PSMessages
               .getString("PSConnectionsDialog.err_msg.failed_to_connect_to_server")); //$NON-NLS-1$
         sb.append(getSelectedConnection().getServer());
         sb.append(PSMessages.getString(":")); //$NON-NLS-1$
         sb.append(getSelectedConnection().getPort());
         sb.append(PSMessages.getString("PSConnectionsDialog.err_msg.as_user")); //$NON-NLS-1$
         sb.append(getSelectedConnection().getUserid());
         sb.append(PSMessages.getString(">.")); //$NON-NLS-1$
         new PSErrorDialog(getShell(), sb.toString()).open();
      }

   }

   /**
    * Enable or disable the OK button.
    * 
    * @param enable <code>true</code> to enable <code>false</code> to
    * disable.
    */
   public void enableOkButton(boolean enable)
   {
      Button okButton = getButton(Dialog.OK);
      if (okButton != null)
         okButton.setEnabled(enable);
   }

   /**
    * Get the selected connection to use to connect to the server.
    * 
    * @return may be <code>null</code>
    */
   public PSUserConnection getSelectedConnection()
   {
      return m_connectionsComposite.getCurrentConnection();
   }

   /**
    * Composite that fills the main dialog area, initialized in
    * {@link #createDialogArea(Composite)} and never <code>null</code> after
    * that.
    */
   public PSConnectionsComposite m_connectionsComposite;

   /**
    * Default information text to display in the title area of the dialog box.
    */
   static private final String DEFAULT_MSG = PSMessages
      .getString("PSConnectionsDialog.default_titlebar_msg"); //$NON-NLS-1$
   
   /**
    * See ctor for description. Never <code>null</code> or empty after ctor.
    */
   private final String m_closeButtonTextKey;

   /**
    * Error message to display. See {@link #setErrorMsg(String)} for more
    * details. Initialized to <code>null</code>.
    */
   private String m_errorMessage = null;

   private PSConnectionInfo m_lastConnection = null;
}
