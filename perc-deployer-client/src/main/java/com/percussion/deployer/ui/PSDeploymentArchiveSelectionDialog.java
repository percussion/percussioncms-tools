/******************************************************************************
 *
 * [ PSDeploymentArchiveSelectionDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.deployer.ui;

import com.percussion.UTComponents.UTBrowseButton;
import com.percussion.UTComponents.UTFixedHeightTextField;
import com.percussion.deployer.objectstore.PSArchive;
import com.percussion.deployer.objectstore.PSArchiveInfo;
import com.percussion.deployer.objectstore.PSImportDescriptor;
import com.percussion.error.PSDeployException;
import com.percussion.error.PSDeployNonUniqueException;
import com.percussion.guitools.ErrorDialogs;
import com.percussion.guitools.PSPropertyPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Iterator;
import java.util.List;

/**
 * The dialog to choose an archive to install and the target server on which
 * the archive need to be installed.
 */
public class PSDeploymentArchiveSelectionDialog  extends
   PSDeploymentWizardDialog
{
   /**
    * Constructs this dialog calling {@link 
    * PSDeploymentWizardDialog#PSDeploymentWizardDialog(Frame, int, int) 
    * super(parent, step, sequence)}. Additional parameters are described below.
    *
    * @param servers the list of <code>PSDeploymentServer</code>s, may not be
    * <code>null</code> or empty. The elements in the list may not be <code>
    * null</code>. Used to set in the 'Target Server' list box.
    * @param archive the archive file of the packages to install, may not be
    * <code>null</code> and must exist.
    */
   public PSDeploymentArchiveSelectionDialog(Frame parent, int step, 
      int sequence, List<PSDeploymentServer> servers, File archive)
   {
      super(parent, step, sequence);

      if (servers == null)
         throw  new IllegalArgumentException(
        "List of target servers may not be null.");
      if (servers.isEmpty())
         throw  new IllegalArgumentException(
           "List of target servers may not be empty.");
      if (archive == null)
         throw  new IllegalArgumentException(
          "Archive file may not be null.");
      if (!archive.isFile())
         throw  new IllegalArgumentException(
          "The file denoted by this abstract pathname does not" +
           "exist and is not a normal file.");

      m_srvList = servers;
      m_archiveFile = archive;
      initDialog();
   }

   /**
    * Constructs this dialog calling {@link 
    * PSDeploymentWizardDialog#PSDeploymentWizardDialog(Frame, int, int) 
    * super(parent, step, sequence)}. Additional parameters are described below.
    *
    * @param servers the list of <code>PSDeploymentServer</code>s, may not be
    * <code>null</code> or empty. The elements in the list may not be <code>
    * null</code>. Used to set in the 'Target Server' list box.
    */
   public PSDeploymentArchiveSelectionDialog(Frame parent, int step, 
      int sequence, List<PSDeploymentServer> servers)
   {
      super(parent, step, sequence);

      if (servers == null)
         throw  new IllegalArgumentException(
          "List of target servers may not be null.");
      if (servers.isEmpty())
         throw  new IllegalArgumentException(
          "List of target servers may not be empty.");

      m_srvList = servers;
      initDialog();
   }

   /**
    * Initializes the dialog framework.
    */
   protected void initDialog()
   {
      JPanel panel = new JPanel();
      getContentPane().add(panel);
      panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
      panel.setBorder(new EmptyBorder(5,5,15,5));
      panel.setPreferredSize(new Dimension(370,430));
      panel.setMaximumSize(new Dimension(370,430));
      panel.setMinimumSize(new Dimension(370,430));
      PSPropertyPanel bodyPanel = new PSPropertyPanel();
      bodyPanel.setAlignmentX(LEFT_ALIGNMENT);
      m_srcArcField = new UTFixedHeightTextField();
      m_targetArcField = new UTFixedHeightTextField();
      // if the archive file is specified in the constructor then set the fields
      // accordingly
      if (m_archiveFile != null)
      {
         m_srcArcField.setText(m_archiveFile.getAbsolutePath());
         setTargetArchiveField(m_archiveFile.getName());
      }

      JPanel panelA = new JPanel();
      UTBrowseButton browse = new UTBrowseButton();
      browse.addActionListener(new ActionListener()
      {
         public void actionPerformed(@SuppressWarnings("unused")
               ActionEvent e)
         {
            File selectedFile = PSDeploymentClient.showPackageFileDialog(
               PSDeploymentArchiveSelectionDialog.this,
               new File(m_srcArcField.getText()),
               JFileChooser.OPEN_DIALOG);

            if(selectedFile != null)
            {
               m_srcArcField.setText(selectedFile.getAbsolutePath());
               setTargetArchiveField(selectedFile.getName());
            }
         }
      });
      panelA.setLayout(new BoxLayout(panelA, BoxLayout.X_AXIS));
      panelA.add(m_srcArcField);
      panelA.add(browse);
      JComponent[] j1 = {panelA};
      bodyPanel.addPropertyRow(getResourceString("srcArchiveLabel"), 
                               new JComponent[]{panelA}, panelA,
                               getResourceString("srcArchiveLabel.mn").charAt(0),
                               null);
      setMnemonicOnTextField(bodyPanel, "srcArchiveLabel");

      JComponent[] j2 = {createServerListBox()};
      bodyPanel.addPropertyRow(getResourceString("serverLable"), j2);
      setMnemonicOnTextField( bodyPanel, "serverLable");
      JComponent[] j3 = {m_targetArcField};
      bodyPanel.addPropertyRow(
                           getResourceString("targetArchiveLabel"), 
                           new JComponent[]{m_targetArcField}, m_targetArcField,
                           getResourceString("targetArchiveLabel.mn").charAt(0),
                           null);
      
      m_descriptionPanel = new JPanel();
      m_descriptionPanel.setAlignmentX(LEFT_ALIGNMENT);
      panel.add(m_descriptionPanel);
      panel.add(Box.createVerticalStrut(20));
      panel.add(bodyPanel);
      panel.add(Box.createVerticalStrut(20));
      JPanel cmdPanel = new JPanel(new BorderLayout());
      cmdPanel.setBorder(new EmptyBorder(5,5,5,5));
      cmdPanel.add(createCommandPanel(true), BorderLayout.EAST);
      panel.add(createCommandPanel(true));
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

      m_descriptionPanel.setLayout(new BoxLayout(
            m_descriptionPanel, BoxLayout.Y_AXIS));
      m_descriptionPanel.add(createDescriptionPanel(
       getResourceString("descTitle"), description));
      pack();
      center();
      setResizable(true);
   }

   /**
    * @param bodyPanel the row panel, that contains a text field
    * @param labelName, the rowpanel that contains a JLabel field name
    */
   private void setMnemonicOnTextField(PSPropertyPanel bodyPanel, 
                                       String labelName) 
   {
      char mn = getResourceString(labelName + ".mn").charAt(0);
      List comp = bodyPanel.getMatchingRowByLabel(getResourceString(labelName));
      Component jlbl = (Component) comp.iterator().next();
      if ( jlbl instanceof JLabel )
      {   
         Iterator iterator = comp.iterator();  
         while (iterator.hasNext())
         {
            Component item = (Component) iterator.next();
            if ( item instanceof JPanel )
            {
               Component[] childCmps = ((Container) item).getComponents();
               for ( int i=0; i<childCmps.length; i++)
               {
                  Component cmp = childCmps[i];
                  if ( cmp instanceof JTextComponent || 
                       cmp instanceof JScrollPane)
                     ((JLabel)jlbl).setLabelFor(cmp);                  
               }
            }
         }
      }
   }

   /**
    * Creates the panel with 'Target Server' label and a list box enclosed
    * in scroll pane.
    *
    * @return the panel, never <code>null</code>
    */
   private JPanel createServerListBox()
   {
      PSDeploymentServer server = (PSDeploymentServer)PSMainFrame.
      getDeployProperty(PSMainFrame.LAST_SELECTED_SERVER);
      
      JPanel panel = new JPanel();
      panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
      m_targetSrvList = new JList(m_srvList.toArray(
        new PSDeploymentServer[m_srvList.size()]));
      m_targetSrvList.setSelectionMode(
            ListSelectionModel.SINGLE_SELECTION);
      m_targetSrvList.setCellRenderer(new CellRenderer());
      if(server != null)
         m_targetSrvList.setSelectedValue(server, true);  

      JScrollPane listPane = new JScrollPane(m_targetSrvList);
      listPane.setAlignmentX(LEFT_ALIGNMENT);
      panel.add(listPane);
      panel.setAlignmentX(LEFT_ALIGNMENT);
      return panel;
   }

   /**
    * Updates and checks whether a deployment server has been selected or not
    * and if selected checks whether it is connected or not. Displays an
    * error message if the server is not selected or not connected.
    *
    * @return <code>true</code> if the selected server is connected, <code>false
    * </code> if there is no selection or selected server is not connected.
    */
   private boolean serverCheck()
   {
      int selIndex = m_targetSrvList.getSelectedIndex();
      if (selIndex == -1)
      {
         ErrorDialogs.showErrorMessage(this,getResourceString("noServer"),
            getResourceString("noServerTitle"));
         return false;
      }
      else
      {
         ListModel model = m_targetSrvList.getModel();
         m_deploymentServer = (PSDeploymentServer)model.getElementAt(
            selIndex);
         
         if (!PSDeploymentClient.getConnectionHandler().connectToServer(
            m_deploymentServer))
         {
            JOptionPane.showMessageDialog(this,
               getResourceString("serverNotConnected"),
               getResourceString("serverErrTitle"),
               JOptionPane.INFORMATION_MESSAGE);
            return false;
         }
      }
      return true;
   }

   /**
    * Checks if the archive to install is a valid MSM archive by creating
    * a <code>PSArchive</code> object from the specified file. Shows an
    * error message if archive file is invalid. If archive file is valid, then
    * checks if the server is licensed for MSM. If server is not licensed then
    * checks if the archive to install is a Rhythmyx sample applications archive.
    * If it is a sample archive then allows installation, else shows
    * licensing error message. Then checks to see if Package is already installed
    * and that it is greater then the version already installed.
    *
    * @return <code>true</code> if the archive to install is a valid
    * MSM archive file and the server is licensed for MSM, <code>false</code>
    * otherwise.
    */
   private boolean archiveCheck()
   {
      File archiveFile = new File(m_srcArcField.getText().trim());
      PSArchive archive = null;
      try
      {
         archive = new PSArchive(archiveFile);
      }
      catch (PSDeployException ex)
      {
         String errMsg = getResourceString("invalidArchiveFile");
         errMsg += " ";
         errMsg += ex.getLocalizedMessage();

         ErrorDialogs.showErrorMessage(this,
            errMsg,
            getResourceString("fileErrTitle"));
         return false;
      }

      // check if server is licensed for MSM
      if (!m_deploymentServer.isServerLicensed())
      {
         // server is not licensed for Multi-Server Manager
         // check if the archive to install is a sample archive
         if (!archive.isSampleArchive())
         {
            // not installing a sample archive, display error message
            ErrorDialogs.showErrorMessage(this,
               getResourceString("notLicensedServer"),
               getResourceString("error") );

            return false;
         }
      }
      
      return true;
   }

   /**
    * The last selected server in this dialog will be selected as the target
    * server by default if it exists and connected. Please see <code>
    * super.init()</code>
    * for more description of this method.
    */
   @Override
   protected void init()
   {
      PSDeploymentServer lastServer = (PSDeploymentServer)PSMainFrame.
         getDeployProperty(PSMainFrame.LAST_IMPORT_SERVER);
      if(lastServer != null && lastServer.isConnected())
         m_targetSrvList.setSelectedValue(lastServer, true);      

      Object[] data = (Object[])getData();
      if (data != null)
      {
         if (data[0] != null)
            m_srcArcField.setText(data[0].toString());
         
         if (data[1] != null)
            m_targetArcField.setText(data[1].toString());
         
         if (data[2] != null)
         {
            ListModel model = m_targetSrvList.getModel();
            for (int i = 0; i < model.getSize(); i++)
            {
               PSDeploymentServer server = 
                  (PSDeploymentServer)model.getElementAt(i);
               if (server.getServerName().equals(data[2].toString()))
                  m_targetSrvList.setSelectedIndex(i);
            }
         }
         
         if (data[3] != null)
            m_archiveInfo = (PSArchiveInfo) data[3];
         
         m_origArchive = (String)data[4];
      }
   }

   /**
    * Validates the following.
    * <ol>
    * <li>Archive File Name is not empty.</li>
    * <li>Server Archive Name is valid.</li>
    * <li>Target server is selected and connected. </li>
    * </ol>
    *
    * @return <code>true</code> if the validation succeeds, otherwise <code>
    * false</code>
    */
   @Override
   public boolean validateData()
   {
      boolean isValid = true;
      if(m_srcArcField.getText().trim().length() == 0)
      {
         ErrorDialogs.showErrorMessage(this, getResourceString("noArchive"),
            getResourceString("error"));
         isValid = false;
         m_srcArcField.requestFocus();
      }
      else if (!serverCheck())
         isValid = false;
      else if (!archiveCheck())
         isValid = false;
      else
      {
         String archiveName = m_targetArcField.getText().trim();
         if(archiveName.length() == 0)
         {
            ErrorDialogs.showErrorMessage(this, getResourceString(
               "noArchiveName"), getResourceString("error"));
            isValid = false;
         }
         else if (!PSDeploymentClient.isValidServerObjectName(archiveName))
         {
            ErrorDialogs.showErrorMessage(this, getResourceString(
               "invalidArchiveName"), getResourceString("error"));
            isValid = false;
         }
         if (!isValid)
            m_targetArcField.requestFocus();
      }

      return isValid;
   }

   /**
    * Validate data, updates user settings like the archive file, archive
    * name and the server.  Checks whether the server is connected or not.
    * Calls super's <code>onNext()</code> to hide the dialog.
    */
   @Override
   public void onNext()
   {
      try
      {
         if(!validateData())
            return;

         String source = m_srcArcField.getText();
         m_archiveFile = new File(source);
         if (!m_archiveFile.isFile())
         {
            ErrorDialogs.showErrorMessage(this,getResourceString("fileErrMsg"),
                  getResourceString("fileErrTitle"));
            return;
         }
         
         PSArchive archive = new PSArchive(m_archiveFile);
         if (!m_archiveFile.getAbsolutePath().equals(m_origArchive) || 
            m_archiveInfo == null)
         {
            m_archiveInfo = archive.getArchiveInfo(true);
            m_descriptor = new PSImportDescriptor(m_archiveInfo);
         }
         archive.close();
         
         String target = m_targetArcField.getText();
         m_archiveInfo.setArchiveRef(target);

         String msg = null;
         m_isOverWrite = false;
         /*
         try
         {
            msg = m_deploymentServer.getDeploymentManager().validateArchive(
               m_archiveInfo, true);
         }
         catch (PSDeployNonUniqueException e)
         {
            // prompt for overwrite
            String confMsg = getResourceString("overwriteArchiveMsg");
            confMsg = MessageFormat.format(confMsg, new Object[] {target});
            confMsg = PSLineBreaker.wrapString(confMsg, 80, 25, "\n");
            int response = JOptionPane.showConfirmDialog(this, confMsg, 
               getResourceString("warning"), JOptionPane.OK_CANCEL_OPTION);
            // if no, stop and select the field
            if (response == JOptionPane.CANCEL_OPTION)
            {
               m_targetArcField.requestFocus();
               return;
            }
            else
            {
               // revalidate w/out overwrite check
               msg = m_deploymentServer.getDeploymentManager().validateArchive(
                  m_archiveInfo, false);
               m_isOverWrite = true;
            }
         }
         
         if (msg != null)
         {
            ErrorDialogs.showErrorMessage(this, msg,
               PSDeploymentClient.getResources().getString("validationTitle"));
         }
         */
         //store the selected server
         PSMainFrame.putDeployProperty(
            PSMainFrame.LAST_IMPORT_SERVER, m_deploymentServer);

         m_origArchive = m_archiveFile.getAbsolutePath();
         
         super.onNext();
      }
      catch(PSDeployNonUniqueException nonEx)
      {

         ErrorDialogs.showErrorDialog(this, nonEx.getMessage(),
          getResourceString("error"), JOptionPane.ERROR_MESSAGE);
      }
      catch(PSDeployException psdEx)
      {
         ErrorDialogs.showErrorDialog(this, psdEx.getMessage(),
          getResourceString("error"), JOptionPane.ERROR_MESSAGE);
      }
   }

   /**
    * Gets the archive info with the detail that is to be deployed to the target
    * server. Should be called after the dialog is invisible by clicking Next.
    *
    * @return the archive info, never <code>null</code>.
    */
   public PSArchiveInfo getArchiveInfo()
   {
      return m_archiveInfo;
   }

   /**
    * Gets the selected archive file that is to be deployed on the selected
    * target server. Should be called after the dialog is invisible by clicking
    * Next.
    *
    * @return the archive file, never <code>null</code>.
    */
   public File getArchiveFile()
   {
      return m_archiveFile;
   }

   /**
    * Gets the selected deployment server. Should be called after
    * the dialog is invisible by clicking Next.
    * @return the target server, never <code>null</code>.
    */
   public PSDeploymentServer getDeploymentServer()
   {
      return m_deploymentServer;
   }
   
   /**
    * Determines if the installation should overwrite the existing archive file
    * and logs on the server.
    * 
    * @return <code>true</code> to overwrite, <code>false</code> otherwise.
    */
   public boolean isOverWrite()
   {
      return m_isOverWrite;
   }

   // see base class
   @Override
   public Object getDataToSave()
   {
      // save selected server, archive file, and server archive name
      Object[] data = new Object[5];
      data[0] = m_srcArcField.getText();
      data[1] = m_targetArcField.getText();
      
      int selIndex = m_targetSrvList.getSelectedIndex();
      if (selIndex != -1)
      {
         ListModel model = m_targetSrvList.getModel();
         PSDeploymentServer server = (PSDeploymentServer)model.getElementAt(
            selIndex);
         data[2] = server.getServerName();
      }
      
      data[3] = m_archiveInfo;
      data[4] = m_origArchive;
      
      return data;
   }

   // see base class
   @Override
   public void onBack()
   {
      setShouldUpdateUserSettings(false);
      super.onBack();
   }
   
   /**
    * Sets the target archive field value with the supplied name, first removing
    * any extension and then substituting any invalid characters with a hyphen.
    * See {@link PSDeploymentClient#fixServerObjectName(String)} for more info
    * on invalid characters.
    *
    * @param name The name to set, assumed not <code>null</code> or empty.
    */
   private void setTargetArchiveField(String name)
   {
      int ind = name.lastIndexOf('.');

      // string extension
      if(ind != -1)
         name = name.substring(0, ind);
      // subsitute "_" for invalid chars and set the field
      m_targetArcField.setText(PSDeploymentClient.fixServerObjectName(name));
   }

   /**
    * The cell renderer to be used with target server list box to display not
    * connected servers as disabled.
    *
    * Does cell rendering based on whether a target server is connected
    * or not. If the server is not connected it's greyed out.
    * List to be rendered contains <code>PSDeploymentServer</code> objects.
    */
   private class CellRenderer extends DefaultListCellRenderer
   {
      @Override
      public Component getListCellRendererComponent(JList list, Object value,
       int index, boolean isSelected, boolean cellHasFocus)
      {
         super.getListCellRendererComponent(list, value, index, isSelected,
          cellHasFocus);
         PSDeploymentServer server = (PSDeploymentServer)value;
         if (!server.isConnected())
            setEnabled(false);
         return this;
      }
   }

   /**
    * Indicates the field which will hold source archive file name.
    * It's being set in <code>initDialog() </code>. It's never <code>null</code>
    * or modified after that.
    */
   UTFixedHeightTextField m_srcArcField;

   /**
    * Lists the list of target servers on which to deploy the selected
    * archive file. It's being set in <code>initDialog()</code>.
    * It's never <code>null</code> or modified after that.
    */
   JList m_targetSrvList;

   /**
    * Indicates text field that will hold archive name on target server.
    * It's being set in <code>initDialog()</code>.
    * It may also get set in <code>validateData()</code> depending on the user's
    * action. It's never <code>null</code> or modified after that.
    */
   UTFixedHeightTextField m_targetArcField;

   /**
    * List of target servers.It's being set in the constructors.
    * It's never <code>null</code> or modified after that.
    */
   List m_srvList;

   /**
    * The archive info of the archive that needs to be deployed. Initialized to
    * <code>null</code> and gets updated in <code>onNext()</code>.
    */
   private PSArchiveInfo m_archiveInfo = null;

   /**
    * The selected archive file on the source server.It's being set in
    * <code>validateData()</code>. It's never <code>null</code> or modifed
    * after that.
    */
   private File m_archiveFile;
   
   /**
    * The panel to display the title and description steps for this dialog,
    * initialized in <code>initDialog()</code>, but actually creates the panel
    * framework in <code>init()</code> method based on its type. Never <code>
    * null</code> after initialization.
    */
   private JPanel m_descriptionPanel;
   
   /**
    * Archive file path previously saved and restore by {@link #getDataToSave()} 
    * and {@link #init()}.  May be <code>null</code>.
    */
   private String m_origArchive = null;
   
   /**
    * Determines if the installation will overwrite an existing archive on the
    * server.  Initially <code>false</code>, modified by {@link #onNext()}.
    */
   private boolean m_isOverWrite = false;
}
