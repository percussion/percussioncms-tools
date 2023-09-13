/******************************************************************************
 *
 * [ PSDeploymentServerSelectionDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.deployer.ui;

import com.percussion.deployer.catalog.PSCatalogResult;
import com.percussion.deployer.objectstore.PSExportDescriptor;
import com.percussion.error.PSDeployException;
import com.percussion.guitools.ErrorDialogs;
import com.percussion.guitools.PSPropertyPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Iterator;
import java.util.List;

/**
 * The dialog to choose the server and descriptor to create an archive or
 * descriptor or update a descriptor.
 */
public class PSDeploymentServerSelectionDialog  extends PSDeploymentWizardDialog
   implements ItemListener
{
   /**
    * Constructs this dialog calling {@link 
    * PSDeploymentWizardDialog#PSDeploymentWizardDialog(Frame, int, int) 
    * super(parent, step, sequence)}.
    * Additional parameter is described below.
    *
    * @param servers the list of <code>PSDeploymentServer</code>s, may not be
    * <code>null</code> or empty. The elements in the list may not be <code>
    * null</code>. Used to set in the 'Server' combo-box.
    */
   public PSDeploymentServerSelectionDialog(Frame parent, int step, 
      int sequence, List servers)
   {
      super(parent, step, sequence);

      if(servers == null || servers.isEmpty())
         throw new IllegalArgumentException(
            "servers may not be null or empty.");
      try
      {
         servers.toArray(new PSDeploymentServer[0]);
      }
      catch(ArrayStoreException e)
      {
         throw new IllegalArgumentException(
            "the elements in the servers list must be " +
            "instances of PSDeploymentServer");
      }
      m_servers = servers;

      initDialog();
   }

   /**
    * Creates the dialog framework with the description, controls and command
    * button panels and adds listeners to the controls.
    */
   protected void initDialog()
   {
      JPanel panel = new JPanel();
      getContentPane().add(panel);
      panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
      EmptyBorder emptyBorder = new EmptyBorder(10, 10, 10, 10);
      panel.setBorder(emptyBorder);

      setTitle(getResourceString("title"));
      int steps = 2;
      try
      {
         steps = Integer.parseInt(getResourceString("descStepCount"));
      }
      catch (NumberFormatException ex)
      {
         //uses the default
      }
      String[] description = new String[steps];
      for (int i = 1; i <= steps; i++)
      {
         description[i-1] = getResourceString("descStep" + i);
      }

      JPanel descPanel = createDescriptionPanel(
         getResourceString("descTitle"), description);

      PSPropertyPanel controlPanel = new PSPropertyPanel();
      controlPanel.setAlignmentX(LEFT_ALIGNMENT);
      m_serverCombo = new JComboBox();
      m_serverCombo.setEditable(false);
      Iterator iter = m_servers.iterator();
      while(iter.hasNext())
         m_serverCombo.addItem(iter.next());
      m_serverCombo.setSelectedIndex(-1);
      m_serverCombo.addItemListener(this);
      m_serverCombo.setRenderer(new serverComboCellRenderer());
      controlPanel.addPropertyRow(getResourceString("serverName"),
                                  new JComponent[] {m_serverCombo},
                                  m_serverCombo,
                                  getResourceString("serverName.mn").charAt(0),
                                  null);

      m_existDescCheck = new JCheckBox(getResourceString("existDesc"));
      m_existDescCheck.setMnemonic(getResourceString("existDesc.mn").charAt(0));
      m_existDescCheck.setEnabled(false);
      m_existDescCheck.setHorizontalAlignment(SwingConstants.RIGHT);
      m_existDescCheck.addItemListener(this);
      controlPanel.addControlsRow(m_existDescCheck, null);

      m_descCombo = new JComboBox();
      m_descCombo.setEditable(false);
      m_descCombo.setEnabled(false);
      controlPanel.addPropertyRow(getResourceString("descName"),
                                  new JComponent[] {m_descCombo},
                                  m_descCombo,
                                  getResourceString("descName.mn").charAt(0),
                                  null);

      panel.add(descPanel);
      panel.add(Box.createVerticalStrut(20));
      panel.add(controlPanel);
      panel.add(Box.createVerticalStrut(20));
      panel.add(createCommandPanel(true));

      pack();
      center();
      setResizable(true);
   }

   /**
    * The last selected server in this dialog will be selected as the source
    * server by default if it exists and connected. Please see <code>
    * super.init()</code> for  more descritpion of this method.
    */
   protected void init()
   {
      Object[] data = (Object[]) getData();
      int selectedServer = -1;
      if (data != null)
      {
         selectedServer = ((Integer)data[0]).intValue();
         if (selectedServer > -1 && selectedServer < 
            m_serverCombo.getModel().getSize())
         {
            m_serverCombo.setSelectedIndex(selectedServer);
            PSDeploymentServer origServer = 
               (PSDeploymentServer)m_serverCombo.getSelectedItem();
            if (origServer != null)
               m_origServer = origServer.getServerName();
         }

         boolean useExist = ((Boolean)data[2]).booleanValue();
         m_existDescCheck.setSelected(useExist);
         
         int selectedDesc = ((Integer)data[1]).intValue();
         if (selectedDesc > -1 && selectedDesc < 
            m_descCombo.getModel().getSize())
         {
            m_descCombo.setSelectedIndex(selectedDesc);
            if (useExist)
            {
               PSCatalogResult desc = 
                  (PSCatalogResult)m_descCombo.getSelectedItem();
               if (desc != null)
                  m_origDesc = desc.getID();
            }
         }
         
         m_loadedDesc = ((Boolean)data[3]).booleanValue();
      }
      
      if (selectedServer == -1)
      {
         PSDeploymentServer server = (PSDeploymentServer)PSMainFrame.
         getDeployProperty(PSMainFrame.LAST_SELECTED_SERVER);
         if(server != null)
            m_serverCombo.setSelectedItem(server);         
      }

   }

   /**
    * Handles the item state change event for 'Server' combo-box and 'Use
    * Existing Descriptor' check-box. Does the following for these controls.
    * <table border=1>
    * <tr><th>Control</th><th>Actions</th></tr>
    * <tr><td>'Server' combo-box</td><td>Validates that the selected server is
    * connected and displays error message if it is not.</td></tr>
    * <tr><td>'Use Existing Descriptor' check-box</td><td>If it is selected,
    * enables the 'Descriptors' combo-box, gets the descriptors list from the
    * server if it is connected and fills the combo, otherwise disables the
    * combo-box</td></tr>
    * </table>
    *
    * @param event the event generated when an item is selected/deselected from
    * combo-box or when a check-box is checked or unchecked, assumed not to
    * be <code>null</code> as Swing model calls this method with an event when
    * such an action occurs.
    */
   public void itemStateChanged(ItemEvent event)
   {
      Object source = event.getSource();
      if(source == m_serverCombo)
      {
         if(event.getStateChange() == ItemEvent.SELECTED)
         {
            m_existDescCheck.setEnabled( validateServer(false) );
            m_existDescCheck.setSelected( false );
         }
      }
      else if(source == m_existDescCheck)
      {
         if(event.getStateChange() == ItemEvent.SELECTED)
         {
            m_descCombo.setEnabled(true);
            m_descCombo.removeAllItems();
            PSDeploymentServer server = (PSDeploymentServer)
               m_serverCombo.getSelectedItem();
            if(server.isConnected())
            {
               try {
                  Iterator descriptors = server.getDescriptors(false).
                     getResults();
                  while(descriptors.hasNext())
                  {
                     m_descCombo.addItem(descriptors.next());
                  }
               }
               catch(PSDeployException e)
               {
                  ErrorDialogs.showErrorMessage(this,
                     e.getLocalizedMessage(),
                     getResourceString("errorTitle") );
               }
            }
         }
         else
         {
            m_descCombo.setEnabled(false);
            m_descCombo.setSelectedIndex(-1);
         }
      }
   }

   // see base class
   public Object getDataToSave()
   {
      Object[] data = new Object[4];
      data[0] = new Integer(m_serverCombo.getSelectedIndex());
      data[1] = new Integer(m_descCombo.getSelectedIndex());
      data[2] = Boolean.valueOf(m_existDescCheck.isSelected());
      data[3] = Boolean.valueOf(m_loadedDesc);
      
      return data;
   }

   // see base class
   public void onBack()
   {
      setShouldUpdateUserSettings(false);
      super.onBack();
   }
   
   /**
    * Validates that the selected deployment server is connected and updates the
    * server and descriptor. Hides the dialog if the validation is succeeded,
    * otherwise displays the error message. Displays error message if it is not
    * able to get the selected descriptor from the server. In case of errors,
    * this does not hide the dialog.
    */
   public void onNext()
   {
      if(validateData())
      {
         m_deploymentServer =
            (PSDeploymentServer)m_serverCombo.getSelectedItem();
         boolean isOrigServer = m_deploymentServer.getServerName().equals(
            m_origServer);
         
         if(m_existDescCheck.isSelected() &&
            m_descCombo.getSelectedItem() != null)
         {
            PSCatalogResult descriptor = (PSCatalogResult)
               m_descCombo.getSelectedItem();
            
            // load if we haven't yet, or if the user changed anything
            String descriptorName = descriptor.getID();
            if (!(m_loadedDesc && isOrigServer && descriptorName.equals(
               m_origDesc)))
            {
               m_loadedDesc = false;
               m_descriptorLocator = new PSDescriptorLocator(descriptorName);
               try 
               {
                  m_descriptor = m_descriptorLocator.load(m_deploymentServer);
                  m_loadedDesc = true;
               }
               catch(PSDeployException e)
               {
                  ErrorDialogs.showErrorMessage(this,
                     e.getLocalizedMessage(),
                     getResourceString("errorTitle") );
                  return;
               }
            }
         }
         else
         {
            // if server has changed or if we had an original descriptor, need
            // to change to a "clean" descriptor
            if (!isOrigServer || (m_origDesc != null && 
               m_origDesc.trim().length() > 0))
            {
               m_descriptor = new PSExportDescriptor("temp");
               m_loadedDesc = false;
            }
         }

         //store the selected server
         PSMainFrame.putDeployProperty(
            PSMainFrame.LAST_EXPORT_SERVER, m_deploymentServer);

         super.onNext();
      }
   }

   //see super class method description
   protected boolean validateData()
   {
      return validateServer(true);
   }

   /**
    * Validates that the user has chosen a server and the selected server is
    * connected.
    *
    * @param showError if <code>true</code> error message will be shown,
    * otherwise not.
    *
    * @return <code>true</code> if there is selection and selected server is
    * connected, otherwise <code>false</code>
    */
   private boolean validateServer(boolean showError)
   {
      if(m_serverCombo.getSelectedItem() != null)
      {
         PSDeploymentServer server = 
            (PSDeploymentServer)m_serverCombo.getSelectedItem();
            
         // try to connect if not connected    
         if (!PSDeploymentClient.getConnectionHandler().connectToServer(server))
         {
            //display a message to connect to the server before
            //creating an archive.
            if(showError)
            {
               ErrorDialogs.showErrorMessage(this,
                  getResourceString("notConnServer"),
                  getResourceString("errorTitle") );
            }
            return false;
         }
         if (!server.isServerLicensed())
         {
            // do not allow sample archive creation if server is not licensed
            if (PSDeploymentClient.isSampleMode() || !PSDeploymentClient.isSupportMode())
            {
               if (showError)
               {
                  ErrorDialogs.showErrorMessage(this,
                     getResourceString("notLicensedServer"),
                     getResourceString("errorTitle") );
               }
               return false;
            }
         }
         return true;
      }
      else
      {
         if(showError)
         {
            ErrorDialogs.showErrorMessage(this,
               getResourceString("noServer"),
               getResourceString("errorTitle") );
         }
         return false;
      }
   }

   /**
    * Gets the deployment server that is selected in this dialog. Should be
    * called only after this dialog returns control to the caller from <code>
    * onNext()</code> method.
    *
    * @return the server, will not be <code>null</code> if {@link #isNext()}
    * returns <code>true</code>
    */
   public PSDeploymentServer getDeploymentServer()
   {
      return m_deploymentServer;
   }

   /**
    * Gets the user selection for the descriptor. Should be called only after 
    * this dialog returns control to the caller from <code>onNext()</code> 
    * method. The caller can check this by calling {@link #isNext()}.
    *
    * @return The locator if the 'Use Exisitng Descriptor' check-box is
    * selected and user selected a descriptor from the 'Descriptor' combo-box,
    * otherwise <code>null</code>.
    */
   public PSDescriptorLocator getDescriptorLocator()
   {
      return m_descriptorLocator;
   }

   /**
    * The cell renderer that is used with 'Server' combo box to show the not
    * connected servers as disabled.
    */
   private class serverComboCellRenderer extends DefaultListCellRenderer
   {
      public Component getListCellRendererComponent(JList list, Object value,
         int index, boolean isSelected, boolean cellHasFocus)
      {
         super.getListCellRendererComponent(list, value, index, isSelected,
            cellHasFocus);
         if(value != null)
         {
            PSDeploymentServer server = (PSDeploymentServer)value;
            if(!server.isConnected())
            {
               setEnabled(false);
            }
         }
         return this;
      }
   }

   /**
    * The list of <code>PSDeploymentServer</code>s that need to be added to
    * server combo box, initialized in the constructor and never <code>null
    * </code> or modified after that.
    */
   private List m_servers = null;

   /**
    * If the user has chosen an existing descriptor to use to create the 
    * archive, this will be a valid reference to that archive, otherwise it is
    * <code>null</code>.  Initialized to <code>null</code> and initialized in 
    * <code>onNext()</code> if the 'Use Existing Descriptor' check box is 
    * selected and the descriptor combo box has a selected descriptor.
    */
   private PSDescriptorLocator m_descriptorLocator = null;

   /**
    * The combo box to display the list of registered servers, initialized in
    * <code>initDialog()</code> and never <code>null</code> after that. The
    * items gets filled in <code>init()</code> and never modified after that.
    */
   private JComboBox m_serverCombo;

   /**
    * The check box to allow user to choose from one of the existing descriptors
    * of the server, initialized in <code>initDialog()</code> and never <code>
    * null</code> after that.
    */
   private JCheckBox m_existDescCheck;

   /**
    * The combo box to display the export descriptors of a selected server. This
    * is initially disabled and is enably only if user selects 'Use Existing
    * Descriptor' check box. The items gets filled when it is enabled.
    * Initialized in <code>initDialog()</code> and never <code>null</code> after
    * that.
    */
   private JComboBox m_descCombo;
   
   /**
    * ID of the descriptor value, if any, that was previously saved and restored
    * by {@link #getDataToSave()} and {@link #init()}.  May be <code>null</code> 
    * or empty.
    */
   private String m_origDesc = null;
   
   /**
    * Name of the server, if any, that was previously saved and restored
    * by {@link #getDataToSave()} and {@link #init()}.  May be <code>null</code> 
    * or empty.
    */
   private String m_origServer = null;

   /**
    * Flag to indicate if the specified descriptor has already been loaded. 
    * Modified by {@link #onNext()} and saved and restored by 
    * {@link #getDataToSave()} and {@link #init()}  
    */
   private boolean m_loadedDesc = false;
}
