/******************************************************************************
 *
 * [ PSServerManager.java ]
 * 
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.packager.ui;

import com.percussion.deployer.client.PSDeploymentServerConnection;
import com.percussion.guitools.PSDialog;
import com.percussion.packager.ui.data.PSServerRegistration;
import com.percussion.packager.ui.managers.PSServerConnectionManager;
import com.percussion.packagerhelp.PSEclHelpManager;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Server connection manger dialog.
 * @author erikserating
 *
 */
public class PSServerManagerDialog extends PSDialog implements ActionListener,
   ListSelectionListener
{

   /**
    * Ctor
    * @param f
    */
   public PSServerManagerDialog(Frame f)
   {
      super(f);
      setTitle(getResourceString("title"));
      initDialog();
   }
   
   /**
    * Initializes the dialog by creating all ui components and laying
    * them out. Also loads server list values.
    */
   private void initDialog()
   {
      setModal(true);
      Container contentpane = getContentPane();
      contentpane.setLayout(new GridLayout(1,1));
      
      JPanel mainpanel = new JPanel();
      MigLayout layout = new MigLayout(
         "wrap 1",
         "[grow]",
         "[grow, top][grow, top][grow, top]");
      mainpanel.setLayout(layout);
      mainpanel.add(createServerListPanel(), "grow");
      mainpanel.add(createServerInfoPanel(), "grow");
      mainpanel.add(createCredsPanel(), "grow");
      mainpanel.add(createCommandPanel(), "dock south, align right");
      contentpane.add(mainpanel);
      
      loadServerList(null);
      handleButtonState();
      
      setSize(400, 420);
      setResizable(true);
      center();
      
      if(m_serverList.getModel().getSize() == 0)
         onNew();
   }
   
   /**
    * Helper method to create the server list panel.
    * @return the server list panel, never <code>null</code>.
    */
   private JPanel createServerListPanel()
   {
      JPanel panel = new JPanel();
      MigLayout layout = new MigLayout(
         "fill",
         "[fill] []",
         "[grow,:100:]");
      panel.setLayout(layout);
      
      
      m_serverList = new JList();
      m_serverList.addListSelectionListener(this);
      JScrollPane scrollPane = new JScrollPane(m_serverList);
      m_newButton = createButton("new");
      m_deleteButton = createButton("delete");
      
      panel.add(scrollPane,"grow");
      panel.add(m_newButton, "split 2, flowy, sg 1, top");
      panel.add(m_deleteButton, "sg 1");
      
      return panel;
   }
   
   /**
    * Helper method to create the server info panel.
    * @return the server info panel, never <code>null</code>.
    */
   private JPanel createServerInfoPanel()
   {
      JPanel panel = new JPanel();
      MigLayout layout = new MigLayout(
         "fill, wrap 2, hidemode 3",
         "[][]",
         "[][][][]");
      panel.setLayout(layout);
      JLabel hostLabel = new JLabel(getResourceString("label.host"));
      m_hostTextField = new JTextField();
      panel.add(hostLabel);
      panel.add(m_hostTextField, "growx");
      
      JLabel portLabel = new JLabel(getResourceString("label.port"));
      m_portTextField = new JTextField(6);
      panel.add(portLabel);
      panel.add(m_portTextField, "");
      
      m_defaultCheckBox = 
         new JCheckBox(getResourceString("label.default")); 
      m_defaultCheckBox.setVisible(false); // hiding this field in case
      // we determine it is needed. SO we easily put it back.
      panel.add(m_defaultCheckBox, "span 2, wrap");
      
      m_useSslCheckBox = 
         new JCheckBox(getResourceString("label.use.ssl"));
      panel.add(m_useSslCheckBox, "span 2");
      
      
      return panel;
   }
   
   /**
    * Helper method to create the credentials panel.
    * @return the creds panel, never <code>null</code>.
    */
   private JPanel createCredsPanel()
   {
      JPanel panel = new JPanel();
      MigLayout layout = new MigLayout(
         "fill, wrap 2",
         "[][]",
         "[][][]");
      panel.setLayout(layout);
      panel.setBorder(BorderFactory.createTitledBorder(
         BorderFactory.createEtchedBorder(), 
            getResourceString("label.creds")));
      JLabel userLabel = 
         new JLabel(getResourceString("label.user"));
      m_userTextField = new JTextField();
            
      panel.add(userLabel);
      panel.add(m_userTextField, "growx");
      
      JLabel passwordLabel = 
         new JLabel(getResourceString("label.password"));
      m_passwordTextField = new JPasswordField();
      panel.add(passwordLabel);
      panel.add(m_passwordTextField, "growx");
      
      m_savePassCheckBox = 
         new JCheckBox(getResourceString("label.save.password"));
      panel.add(m_savePassCheckBox, "span 2");
      
      return panel;
   }
   
   /**
    * Helper method to create the command panel.
    * @return the command panel, never <code>null</code>.
    */
   private JPanel createCommandPanel()
   {
      JPanel panel = new JPanel();      
      MigLayout layout = new MigLayout(
         "right",
         "[][][][]",
         "[]");
      panel.setLayout(layout);
           
      m_saveButton = createButton("save");
      panel.add(m_saveButton);
      
      m_saveConnectButton = createButton("saveandconnect");
      panel.add(m_saveConnectButton);
      
      m_helpButton = createButton("help");
      panel.add(m_helpButton);
      
      m_cancelButton = createButton("cancel");
      panel.add(m_cancelButton);
      
      return panel;
   }
   
   /* (non-Javadoc)
    * @see java.awt.event.ActionListener#actionPerformed(
    * java.awt.event.ActionEvent)
    */
   public void actionPerformed(ActionEvent e)
   {
      Object source = e.getSource();
      if(source == m_newButton)
      {
         onNew();
      }
      if(source == m_deleteButton)
      {
         onDelete();
      }
      if(source == m_saveButton)
      {
         onSave();
      }
      if(source == m_saveConnectButton)
      {
         if(onSave())
            onConnect();
      }
      if(source == m_helpButton)
      {
         onHelp();
      }
      if(source == m_cancelButton)
      {
         onCancel();
      }
   }
   
   /* (non-Javadoc)
    * @see javax.swing.event.ListSelectionListener#valueChanged(
    * javax.swing.event.ListSelectionEvent)
    */
   public void valueChanged(@SuppressWarnings("unused")
      ListSelectionEvent e)
   {      
      PSServerRegistration server = 
         (PSServerRegistration)m_serverList.getSelectedValue();
      loadFields(server);
      setFieldsEnabled(!isSelectedServerConnected());
      handleButtonState();
   }
   
   /* (non-Javadoc)
    * @see com.percussion.guitools.PSDialog#onCancel()
    */
   @Override
   public void onCancel()
   {
      super.onCancel();
   }
   
   /* (non-Javadoc)
    * @see com.percussion.guitools.PSDialog#onHelp()
    */
   @Override
   public void onHelp()
   {
      PSEclHelpManager.launchHelp(null,"/com.percussion.doc.help.packagebuilder/toc.xml");
   }
   
   
   
   /* (non-Javadoc)
    * @see com.percussion.guitools.PSDialog#setVisible(boolean)
    */
   @Override
   public void setVisible(boolean visible)
   {
      m_useHelpViewer = false;
      super.setVisible(visible);
   }

   /**
    * Creates server connection for specified server
    * registration.
    */
   private void onConnect()
   {
      PSServerRegistration server = 
         (PSServerRegistration)m_serverList.getSelectedValue();
      if(server == null)
         return;
     
      try
      {
         setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
         PSPackagerClient.getFrame().getModel().connect(server);                 
         super.onCancel();
      }
      catch (Exception e)
      {
         PSPackagerClient.getErrorDialog().showError(
            e, false, PSResourceUtils.getCommonResourceString("errorTitle"));
      }
      finally
      {
         setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      }      
   }   
   
   /**
    * Persist the server registration to the file system.
    * @return <code>false</code> if validation failed causing
    * save to not occur.
    */
   private boolean onSave()
   {
      if(!validateData())
         return false;
      PSServerConnectionManager connMgr = 
         PSServerConnectionManager.getInstance();
      PSServerRegistration server = new PSServerRegistration(
        m_hostTextField.getText(),
        Integer.parseInt(m_portTextField.getText()),
        m_userTextField.getText(),
           new String(m_passwordTextField.getPassword()),
        m_savePassCheckBox.isSelected(),
        m_useSslCheckBox.isSelected()        
        );
      server.setIsDefault(m_defaultCheckBox.isSelected());
      connMgr.saveServerRegistration(server);
      loadServerList(null);
      PSPackagerClient.getFrame().refreshTitle();
      selectServer(server);
      return true;
   }
   
   /**
    * Delete action.
    */
   private void onDelete()
   {
      PSServerRegistration server = 
         (PSServerRegistration)m_serverList.getSelectedValue();
      if(server != null)
      {
         Object[] args = new Object[]{server.toString()}; 
         String msg = MessageFormat.format(
            getResourceString("warning.delete.connection"), args);
         int result = JOptionPane.showConfirmDialog(this, msg,
            PSResourceUtils.getCommonResourceString("warningTitle"),
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
         if(result == JOptionPane.YES_OPTION)
         {
            m_serverList.clearSelection();
            clearFields();
            PSServerConnectionManager.getInstance().
               deleteServerRegistration(server);
            loadServerList(null);
            PSPackagerClient.getFrame().refreshTitle();
         }         
      }
   }
   
   /**
    * New action.
    */
   private void onNew()
   {
      m_serverList.clearSelection();
      clearFields();
      m_hostTextField.requestFocus();
   }
   
   /**
    * Helper method to handle enable state of buttons.
    */
   private void handleButtonState()
   {
      boolean hasSelection = m_serverList.getSelectedIndex() != -1;
      
      boolean isSelectedConnected = isSelectedServerConnected();
      m_deleteButton.setEnabled(
         hasSelection && !isSelectedConnected);
      m_saveButton.setEnabled(!isSelectedConnected);
      m_saveConnectButton.setEnabled(!isSelectedConnected);
     
   }
   
   /**
    * Determines if the selected server registration is connected.
    * @return <code>true</code> if connected.
    */
   private boolean isSelectedServerConnected()
   {
      boolean hasSelection = m_serverList.getSelectedIndex() != -1;
      if(!hasSelection)
         return false;
      PSServerRegistration reg = 
         (PSServerRegistration)m_serverList.getModel().getElementAt(
            m_serverList.getSelectedIndex());
      
      return reg.equals(
         PSServerConnectionManager.getInstance().getCurrentServerInfo());
      
   }
   
   /**
    * Helper method to select the server from the server list by the server
    * registration object specified.
    * @param server assumed not <code>null</code>
    */
   private void selectServer(PSServerRegistration server)
   {
      int len = m_serverList.getModel().getSize();
      int pos = -1;
      for(int i = 0; i < len; i++)
      {
         PSServerRegistration current = 
            (PSServerRegistration)m_serverList.getModel().getElementAt(i);
         if(server.equals(current))
         {
            pos = i;
            break;
         }
      }
      if(pos != -1)
         m_serverList.setSelectedIndex(pos);
   }
   
   /** 
    * Validate the data in the edit fields.
    * @return <code>true</code> if valid.
    */
   private boolean validateData()
   {
        if(StringUtils.isBlank(m_hostTextField.getText()))
        {
           Object[] args = new Object[]{
              StringUtils.chomp(getResourceString("label.host"), ":")};
           PSPackagerClient.getErrorDialog().showErrorMessage(
              MessageFormat.format(
                 getResourceString("error.required.field"), args), 
              PSResourceUtils.getCommonResourceString("errorTitle"));
           return false;
        }
        if(StringUtils.isBlank(m_portTextField.getText()))
        {
           Object[] args = new Object[]{
              StringUtils.chomp(getResourceString("label.port"), ":")};
           PSPackagerClient.getErrorDialog().showErrorMessage(
              MessageFormat.format(
                 getResourceString("error.required.field"), args), 
              PSResourceUtils.getCommonResourceString("errorTitle"));
           return false;
        }
        if(!StringUtils.isNumeric(m_portTextField.getText()) ||
           m_portTextField.getText().indexOf('.') != -1)        
        {
           PSPackagerClient.getErrorDialog().showErrorMessage(
              getResourceString("error.port.not.int"), 
              PSResourceUtils.getCommonResourceString("errorTitle"));
           return false;
        }
        
      return true;
   }
   
   /**
    * Load edit fields from server registration object.
    * @param server
    */
   private void loadFields(PSServerRegistration server)
   {
      if(server == null)
         return;
      m_hostTextField.setText(
         StringUtils.defaultString(server.getServer()));
      m_portTextField.setText(String.valueOf(server.getPort()));
      m_defaultCheckBox.setSelected(server.isDefault());
      m_useSslCheckBox.setSelected(server.isUseSSL());
      m_userTextField.setText(
         StringUtils.defaultString(server.getUserName()));
      m_passwordTextField.setText(StringUtils.isBlank(m_userTextField.getText()) ? "" :
         PSDeploymentServerConnection.decryptPwd(server.getUserName(), server.getPassword()));
      m_savePassCheckBox.setSelected(server.isSaveCredentials());
   }
   
   /**
    * Clear all edit fields.
    */
   private void clearFields()
   {
      m_hostTextField.setText("");
      m_portTextField.setText("");
      m_defaultCheckBox.setSelected(false);
      m_useSslCheckBox.setSelected(false);
      m_userTextField.setText("");
      m_passwordTextField.setText("");
      m_savePassCheckBox.setSelected(false);
   }
   
   /**
    * Set all field enable state.
    * @param enabled
    */
   private void setFieldsEnabled(boolean enabled)
   {
      m_hostTextField.setEnabled(enabled);
      m_portTextField.setEnabled(enabled);
      m_defaultCheckBox.setEnabled(enabled);
      m_useSslCheckBox.setEnabled(enabled);
      m_userTextField.setEnabled(enabled);
      m_passwordTextField.setEnabled(enabled);
      m_savePassCheckBox.setEnabled(enabled);
   }

   /**
    * Helper method to create a new button and pull mnemonic and
    * tooltip info from the resource bundle.
    * @param resourcename resource name key. Assumed not
    * <code>null</code>.
    * @return the newly created button, never <code>null</code>.
    */
   private JButton createButton(String resourcename)
   {
      String name = getResourceString("button." + resourcename);
      String mnemonic = getResourceString("button." + resourcename + ".m");
      String tooltip = getResourceString("button." + resourcename + ".tt"); 
      JButton button = new JButton(name);
      button.setToolTipText(tooltip);
      if(mnemonic.length() == 1)
         button.setMnemonic(mnemonic.charAt(0));
      button.addActionListener(this);
      return button;      
   }
   
   /**
    * Load the server list control.
    * @server a server registration to add to bottom of list, but
    * will not be persisted.
    */
   private void loadServerList(PSServerRegistration server)
   {
      PSServerConnectionManager connMgr = 
         PSServerConnectionManager.getInstance();
      Iterator<PSServerRegistration> it = connMgr.getServers().getServers();
      List<PSServerRegistration> values = 
         new ArrayList<PSServerRegistration>();
      while(it.hasNext())
      {
         values.add(it.next());
      }
      if(server != null)
         values.add(server);
      m_serverList.setListData(values.toArray());
      
   }
   
   /**
    * Server list component. Initialized in {@link #initDialog()}
    * never <code>null</code> after that.
    */
   private JList m_serverList;
   
   /**
    * Text field for host value. Initialized in {@link #initDialog()}
    * never <code>null</code> after that.
    */
   private JTextField m_hostTextField;
   
   /**
    * Text field for port value. Initialized in {@link #initDialog()}
    * never <code>null</code> after that.
    */
   private JTextField m_portTextField;
   
   /**
    * Check box field for flagging server as default.
    * Initialized in {@link #initDialog()} never <code>null</code>
    * after that.
    */
   private JCheckBox m_defaultCheckBox;
   
   /**
    * Text field for user value. Initialized in {@link #initDialog()}
    * never <code>null</code> after that.
    */
   private JTextField m_userTextField;
   
   /**
    * Password field for password value. Initialized in {@link #initDialog()}
    * never <code>null</code> after that.
    */
   private JPasswordField m_passwordTextField;
   
   /**
    * Check box field for flagging password as being save-able.
    * Initialized in {@link #initDialog()} never <code>null</code>
    * after that.
    */
   private JCheckBox m_savePassCheckBox;
   
   /**
    * Check box field for flagging use ssl.
    * Initialized in {@link #initDialog()} never <code>null</code>
    * after that.
    */
   private JCheckBox m_useSslCheckBox;
   
   /**
    * Button for adding a new server entry.
    * Initialized in {@link #initDialog()} never <code>null</code>
    * after that.
    */
   private JButton m_newButton;
   
   /**
    * Button for deleting of server entries.
    * Initialized in {@link #initDialog()} never <code>null</code>
    * after that.
    */
   private JButton m_deleteButton;   
    
   /**
    * Button for saving of server entry.
    * Initialized in {@link #initDialog()} never <code>null</code>
    * after that.
    */
   private JButton m_saveButton;
   
   /**
    * Button for saving a server entry and then connecting to it.
    * Initialized in {@link #initDialog()} never <code>null</code>
    * after that.
    */
   private JButton m_saveConnectButton;
   
   /**
    * Button for launching help.
    * Initialized in {@link #initDialog()} never <code>null</code>
    * after that.
    */
   private JButton m_helpButton;
   
   /**
    * Button to cancel and close dialog.
    * Initialized in {@link #initDialog()} never <code>null</code>
    * after that.
    */
   private JButton m_cancelButton;

   

  
   
}
