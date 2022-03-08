/******************************************************************************
 *
 * [ PSDeploymentCreateArchiveDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.deployer.ui;

import com.percussion.UTComponents.IUTConstants;
import com.percussion.UTComponents.UTBrowseButton;
import com.percussion.UTComponents.UTFixedHeightTextField;
import com.percussion.deployer.catalog.PSCatalogResult;
import com.percussion.deployer.client.IPSDeployConstants;
import com.percussion.deployer.client.PSDeploymentManager;
import com.percussion.deployer.objectstore.PSDescriptor;
import com.percussion.deployer.objectstore.PSExportDescriptor;
import com.percussion.error.PSDeployException;
import com.percussion.guitools.ErrorDialogs;
import com.percussion.guitools.PSPropertyPanel;
import com.percussion.util.PSLineBreaker;
import com.percussion.util.PSProperties;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * The dialog to choose the server and descriptor to create an archive or 
 * descriptor or update a descriptor.
 */
public class PSDeploymentCreateArchiveDialog  extends PSDeploymentWizardDialog
   implements ItemListener
{
   /**
    * Constructs this dialog calling {@link 
    * PSDeploymentWizardDialog#PSDeploymentWizardDialog(Frame, 
    * PSDeploymentServer, int, int) super(parent, server, step, sequence)}. 
    * Additional parameter is described below.
    * 
    * @param isExistingDesc supply <code>true</code> to indicate the existing 
    * descriptor in which case the descriptor name can not be modifiable, 
    * otherwise <code>false</code>.
    */
   public PSDeploymentCreateArchiveDialog(Frame parent, 
      PSDeploymentServer server, int step, int sequence, boolean isExistingDesc)
   {
      super(parent, server, step, sequence);
      
      m_isExistingDesc = isExistingDesc;
      
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
      panel.setLayout(new BorderLayout(20, 20));
      EmptyBorder emptyBorder = new EmptyBorder(10, 10, 10, 10);
      panel.setBorder(emptyBorder);
      
      setTitle(getResourceString("title"));
      int steps = 1;
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
      panel.add(descPanel, BorderLayout.NORTH);      
         
      PSPropertyPanel controlPanel = new PSPropertyPanel();
      controlPanel.setBorder(
            BorderFactory.createTitledBorder(
                 BorderFactory.createEtchedBorder(EtchedBorder.LOWERED)));
      controlPanel.setAlignmentX(LEFT_ALIGNMENT);
      
      m_descriptorField = new JTextField();
      controlPanel.addPropertyRow(getResources().getString("descriptor"), 
               new JComponent[] {m_descriptorField},
               m_descriptorField,
               getResourceString("descriptor.mn").charAt(0),
               null);
      
      m_descriptionField = new JTextArea();
      m_descriptionField.setLineWrap(true);
      m_descriptionField.setWrapStyleWord(true);
      controlPanel.addPropertyRow(getResourceString("description"), 
                                  new JComponent[]{m_descriptionField},
                                  m_descriptionField,
                                  getResourceString("description.mn").charAt(0),
                                  null);
      m_vendorNameField = new JTextField();
      controlPanel.addPropertyRow(getResourceString("vendorName"), 
               new JComponent[]{m_vendorNameField},
               m_vendorNameField,
               getResourceString("vendorName.mn").charAt(0),
               null);      
      m_vendorUrlField = new JTextField();
      controlPanel.addPropertyRow(getResourceString("vendorUrl"), 
               new JComponent[]{m_vendorUrlField},
               m_vendorUrlField,
               getResourceString("vendorUrl.mn").charAt(0),
               null);      
      m_versionField = new JTextField();
      m_versionField.setText(PSDescriptor.DEFAULT_VERSION);
      controlPanel.addPropertyRow(getResourceString("version"), 
               new JComponent[]{m_versionField},
               m_versionField,
               getResourceString("version.mn").charAt(0),
               null);
      m_cmsMinVersionField = new JTextField();
      m_cmsMinVersionField.setText(PSDescriptor.DEFAULT_CMS_MIN_VERSION);
      controlPanel.addPropertyRow(getResourceString("cmsMinVersion"), 
               new JComponent[]{m_cmsMinVersionField},
               m_cmsMinVersionField,
               getResourceString("cmsMinVersion.mn").charAt(0),
               null);
      m_cmsMaxVersionField = new JTextField();
      controlPanel.addPropertyRow(getResourceString("cmsMaxVersion"), 
               new JComponent[]{m_cmsMaxVersionField},
               m_cmsMaxVersionField,
               getResourceString("cmsMaxVersion.mn").charAt(0),
               null);      
      
      m_implConfigFileField = new UTFixedHeightTextField();
      UTBrowseButton cfBrowseButton = new UTBrowseButton();
      cfBrowseButton.addActionListener(new ActionListener() 
      {
         public void actionPerformed(ActionEvent e) 
         {
            File selectedFile = PSDeploymentClient.showFileDialog(
                  PSDeploymentCreateArchiveDialog.this, 
                  new File(m_implConfigFileField.getText()),
                  "xml", "XML Files (*.xml)", JFileChooser.SAVE_DIALOG);
               
               if(selectedFile != null)
                  m_implConfigFileField.setText(selectedFile.toString());
            
         }
      });
      
      JPanel iConfigFilePanel = new JPanel();
      iConfigFilePanel.setLayout(new BoxLayout(iConfigFilePanel, BoxLayout.X_AXIS));
      iConfigFilePanel.add(m_implConfigFileField);
      iConfigFilePanel.add(cfBrowseButton);
      controlPanel.addPropertyRow(getResourceString("implConfigFile"), 
               new JComponent[] {iConfigFilePanel},
               iConfigFilePanel,
               getResourceString("implConfigFile.mn").charAt(0),
               null);
      
      m_localConfigFileField = new UTFixedHeightTextField();
      UTBrowseButton lcfBrowseButton = new UTBrowseButton();
      lcfBrowseButton.addActionListener(new ActionListener() 
      {
         public void actionPerformed(ActionEvent e) 
         {
            File selectedFile = PSDeploymentClient.showFileDialog(
                  PSDeploymentCreateArchiveDialog.this, 
                  new File(m_localConfigFileField.getText()),
                  "xml", "XML Files (*.xml)", JFileChooser.SAVE_DIALOG);
               
               if(selectedFile != null)
                  m_localConfigFileField.setText(selectedFile.toString());
            
         }
      });
      
      JPanel lConfigFilePanel = new JPanel();
      lConfigFilePanel.setLayout(new BoxLayout(lConfigFilePanel, BoxLayout.X_AXIS));
      lConfigFilePanel.add(m_localConfigFileField);
      lConfigFilePanel.add(lcfBrowseButton);
      controlPanel.addPropertyRow(getResourceString("localConfigFile"), 
               new JComponent[] {lConfigFilePanel},
               lConfigFilePanel,
               getResourceString("localConfigFile.mn").charAt(0),
               null);
      
      m_archiveField = new UTFixedHeightTextField();         
      m_createArchiveCheck = new JCheckBox(getResourceString("createArchive"));
      m_createArchiveCheck.setMnemonic(
                              getResourceString("createArchive.mn").charAt(0));
      controlPanel.addControlsRow(m_createArchiveCheck, null); 
      m_createArchiveCheck.setPreferredSize(new Dimension(150, 20));
      m_createArchiveCheck.setHorizontalAlignment(SwingConstants.RIGHT);        
      m_createArchiveCheck.setSelected(true);

      UTBrowseButton browseButton = new UTBrowseButton();
      browseButton.addActionListener(new ActionListener() 
      {
         public void actionPerformed(ActionEvent e) 
         {
            if(m_createArchiveCheck.isSelected())
            {
               File selectedFile = PSDeploymentClient.showPackageFileDialog(
                  PSDeploymentCreateArchiveDialog.this, 
                  new File(m_archiveField.getText()),
                  JFileChooser.SAVE_DIALOG);
               
               if(selectedFile != null)
                  m_archiveField.setText(selectedFile.toString());
            }
         }
      });
      JPanel archivePanel = new JPanel();
      archivePanel.setLayout(new BoxLayout(archivePanel, BoxLayout.X_AXIS));
      archivePanel.add(m_archiveField);
      archivePanel.add(browseButton);
      controlPanel.addPropertyRow(getResourceString("saveToArchive"), 
                                  new JComponent[] {archivePanel},
                                  archivePanel,
                                  getResourceString("saveToArchive.mn").charAt(0),
                                  null);
           
      //Avoid preferred sizes set by PSPropertyPanel by calculating our own 
      //based on sizes taken by components.
      archivePanel.setPreferredSize(
         new Dimension(IUTConstants.PREF_WIDTH, IUTConstants.FIXED_HEIGHT));
      archivePanel.setMinimumSize(
         new Dimension(Integer.MIN_VALUE, IUTConstants.FIXED_HEIGHT));
      archivePanel.setMaximumSize(
         new Dimension(Integer.MAX_VALUE, IUTConstants.FIXED_HEIGHT));
      
         
      panel.add(descPanel, BorderLayout.NORTH);
      panel.add(controlPanel, BorderLayout.CENTER);
      panel.add(createCommandPanel(true), BorderLayout.SOUTH);
      
      pack();      
      center();    
      setResizable(true);
   }

   /**
    * Updates the various values of the descriptor if the descriptor is 
    * an existing descriptor. See <code>super.init()</code> for more description.
    */
   @Override
   protected void init()
   {
      Object[] data = (Object[]) getData();
      if (data != null)
      {
         if (data[0] != null)
            m_descriptionField.setText(data[0].toString());
         if (data[1] != null)
            m_createArchiveCheck.setSelected(((Boolean)data[1]).booleanValue());
         if (data[2] != null)
            m_archiveField.setText(data[2].toString());         
         if (data[3] != null)
            m_descriptorField.setText(data[3].toString());
         if (data[4] != null)
            m_vendorNameField.setText(data[4].toString());
         if (data[5] != null)
            m_vendorUrlField.setText(data[5].toString());
         if (data[6] != null)
            m_versionField.setText(data[6].toString());
         if (data[7] != null)
            m_cmsMinVersionField.setText(data[7].toString());
         if (data[8] != null)
            m_cmsMaxVersionField.setText(data[8].toString());
         if (data[9] != null)
            m_implConfigFileField.setText(data[9].toString());
         if (data[9] != null)
            m_localConfigFileField.setText(data[10].toString());
            
      }
      else
      {
         if(m_isExistingDesc)
         {
            m_descriptionField.setText(m_descriptor.getDescription());      
            m_descriptorField.setText(m_descriptor.getName());
            m_vendorNameField.setText(m_descriptor.getPublisherName());
            m_vendorUrlField.setText(m_descriptor.getPublisherUrl());
            m_versionField.setText(m_descriptor.getVersion());
            m_cmsMinVersionField.setText(m_descriptor.getCmsMinVersion());
            m_cmsMaxVersionField.setText(m_descriptor.getCmsMaxVersion());
            m_implConfigFileField.setText(m_descriptor.getConfigDefFile());
            m_localConfigFileField.setText(m_descriptor.getLocalConfigFile());
         }
      }

      m_createArchiveCheck.addItemListener(this);      
   }
   
   /**
    * Action method state change events for the check-boxes. If 'Create Archive'
    * check-box is selected, makes the field to enter archive name as enabled, 
    * otherwise not. If 'Save Descriptor' check-box is selected, makes the field
    * to enter descriptor name as enabled only if the descriptor is not an 
    * existing descriptor.
    * 
    * @param event the event generated when a check-box is checked or unchecked,
    * assumed not to be <code>null</code> as Swing model calls this method with 
    * an event when such an action occurs.
    */
   public void itemStateChanged(ItemEvent event)
   {
      Object source = event.getSource();
      if(source == m_createArchiveCheck)
      {
         if(event.getStateChange() == ItemEvent.SELECTED)
            m_archiveField.setEnabled(true);
         else
            m_archiveField.setEnabled(false);
      }
   }
   
   /**
    * Validates and saves the data. Confirms with the user to overwrite the 
    * archive file if the user has chosen to create archive and the selected 
    * archive file exists. Saves the descriptor if user has chosen to 
    * save the descriptor. Displays error message if it is not 
    * able to save the descriptor. Hides the dialog if the validation is 
    * succeeded, otherwise displays the error messages.  In case of errors, 
    * this does not hide the dialog. See <code>validateData()</code> for 
    * validation details.
    */
   public void onNext()
   {
      if(validateData())
      {   
         if(StringUtils.isNotBlank(m_descriptorField.getText()))
            m_descriptor.setName(m_descriptorField.getText());
         
         m_descriptor.setDescription(m_descriptionField.getText());
         m_descriptor.setPublisherName(m_vendorNameField.getText());
         m_descriptor.setPublisherUrl(m_vendorUrlField.getText());
         m_descriptor.setVersion(m_versionField.getText());
         m_descriptor.setCmsMinVersion(m_cmsMinVersionField.getText());
         m_descriptor.setCmsMaxVersion(m_cmsMaxVersionField.getText());
         m_descriptor.setConfigDefFile(m_implConfigFileField.getText());
         m_descriptor.setLocalConfigFile(m_localConfigFileField.getText());
         
         
         if(m_createArchiveCheck.isSelected())
         {
            m_isCreateArchive = m_createArchiveCheck.isSelected();
                  
            String arcExtLower = 
               IPSDeployConstants.ARCHIVE_EXTENSION.toLowerCase();
            String fileName = m_archiveField.getText().trim();
            if (!fileName.toLowerCase().endsWith(arcExtLower))
               fileName += arcExtLower;
            
            m_archiveFile = new File(fileName);
            if(m_archiveFile.exists())
            {
               int option = JOptionPane.showConfirmDialog(this, 
                  ErrorDialogs.cropErrorMessage(
                  MessageFormat.format(getResourceString("replaceFile"), 
                  new String[]{m_archiveFile.getName()}) ), 
                  getResourceString("replaceFileTitle"), 
                  JOptionPane.YES_NO_OPTION);
               if(option == JOptionPane.NO_OPTION)
                  return;
               if(!m_archiveFile.canWrite())
               {
                  ErrorDialogs.showErrorMessage(this, 
                     MessageFormat.format(getResourceString("noWriteAccess"), 
                     new String[]{m_archiveFile.getName()}),                   
                     getResourceString("error"));        
                  return;
               }
               if (m_archiveFile.isDirectory())
               {
                  ErrorDialogs.showErrorMessage(this, 
                     MessageFormat.format(getResourceString(
                     "selectedDirectory"), 
                     new String[]{m_archiveFile.getName()}),                   
                     getResourceString("error"));        
                  return;                  
               }
            }  
            else
            {
               // try to create the file
               try 
               {
                  m_archiveFile.createNewFile();
               }
               catch (IOException e) 
               {
                  // show error and return
                  ErrorDialogs.showErrorMessage(this, 
                     MessageFormat.format(getResourceString("cannotCreateFile"), 
                     new String[]{e.getLocalizedMessage()}),                   
                     getResourceString("error"));        
                  return;
               }
               finally
               {
                  // delete the file if it got created
                  if (m_archiveFile.exists())
                     m_archiveFile.delete();
               }
            }       
         }
                     
         try
         {            
            PSDeploymentManager deploymentMgr = 
               m_deploymentServer.getDeploymentManager();
            if(StringUtils.isBlank(m_descriptor.getId()))
            {
               m_descriptor.setId(deploymentMgr.createDescriptorGuid());
            }            
            deploymentMgr.saveExportDescriptor(
                     (PSExportDescriptor) m_descriptor);
            // Tell server to re-catalog the descriptors as we may have
            // modified the view.
            m_deploymentServer.updateDescriptors();
         }
         catch (PSDeployException e)
         {
            ErrorDialogs.showErrorMessage(this, e.getLocalizedMessage(),
                     getResourceString("error"));
            return;
         }
        
         //save the archive file location with descriptor to facilititate the
         //later creation of archive from the descriptor.
         if(m_createArchiveCheck.isSelected())
         {
            File descArcFile = new File(IPSDeployConstants.CLIENT_DIR, 
               PSDeploymentClient.DESCRIPTOR_ARCHIVE_FILE);            
            descArcFile.getParentFile().mkdirs();
            FileOutputStream out = null;            
            try {            
               descArcFile.createNewFile();
               PSProperties properties = new PSProperties(
                  descArcFile.getAbsolutePath());
               
               //remove the previous matching entry for this archive
               Iterator iter = properties.entrySet().iterator();
               while(iter.hasNext())
               {
                  Map.Entry entry = (Map.Entry)iter.next();
                  if(entry.getValue().equals(m_archiveFile.getAbsolutePath()))
                  {
                     iter.remove();
                     break;
                  }
               }
               //add the new property for the relationship
               properties.setProperty(m_deploymentServer.getServerName() + 
                  PSDeploymentClient.SERVER_DESCRIPTOR_SEPARATOR + 
                  m_descriptor.getName(), m_archiveFile.getAbsolutePath());
               out = new FileOutputStream(descArcFile);               
               properties.store(out, "");
            }
            catch(IOException e)
            {               
               ErrorDialogs.showErrorMessage(this, 
                  MessageFormat.format(
                  getResourceString("unableToSaveDescArchive"), 
                  new String[] {e.getLocalizedMessage()}), 
                  getResourceString("error"));   
            }
            finally
            {
               try {
                  if(out != null)
                     out.close();
               } catch(IOException ie){}
            }
         }
         
         super.onNext();
      }
   }
   
   // see base class
   public Object getDataToSave()
   {
      // save description, archive and descriptor settings
      Object[] data = new Object[11];
      data[0] = m_descriptionField.getText();
      data[1] = Boolean.valueOf(m_createArchiveCheck.isSelected());
      data[2] = m_archiveField.getText();
      data[3] = m_descriptorField.getText();
      data[4] = m_vendorNameField.getText();
      data[5] = m_vendorUrlField.getText();
      data[6] = m_versionField.getText();
      data[7] = m_cmsMinVersionField.getText();
      data[8] = m_cmsMaxVersionField.getText();
      data[9] = m_implConfigFileField.getText();
      data[10] = m_localConfigFileField.getText();
      
      return data;
   }

   // see base class
   public void onBack()
   {
      setShouldUpdateUserSettings(false);
      super.onBack();
   }
   
   /**
    * Validates data entered in the dialog. The validations are
    * <ol>
    * <li>Either one of the options 'Create Archive', 'Save Descriptor' must be
    * selected</li>
    * <li>Archive File name must not be empty if the 'Create Archive' option is
    * chosen</li>   
    * <li>Descriptor name must be entered if the 'Save Descriptor' option is 
    * chosen.</li>
    * <li>Descriptor with that name must not exist on server in case of creating
    * a new descriptor.</li>
    * </ol>
    * 
    * @return <code>true</code> if the validation succeeds, otherwise <code>
    * false</code>
    */
   protected boolean validateData()
   {
      if(m_createArchiveCheck.isSelected())
      {
         if(m_archiveField.getText().trim().length() == 0)
         {
            ErrorDialogs.showErrorMessage(this, 
               getResourceString("mustEnterFileName"), 
               getResourceString("error"));
            return false;
         }
      }      
      if(StringUtils.isBlank(m_versionField.getText()))
      {
         ErrorDialogs.showErrorMessage(this,
                  MessageFormat.format(getResourceString("requiredField"),
                     StringUtils.chomp(getResourceString("version"), ":")) ,
                  getResourceString("error"));
         return false;   
      }
      String fieldName = null;
      try
      {
         fieldName = 
            StringUtils.chomp(getResourceString("version"), ":");
         PSDescriptor.formatVersion(m_versionField.getText(), true, true);
         fieldName = 
            StringUtils.chomp(getResourceString("cmsMinVersion"), ":");
         PSDescriptor.formatVersion(m_cmsMinVersionField.getText(), true, true);
         fieldName = 
            StringUtils.chomp(getResourceString("cmsMaxVersion"), ":");
         PSDescriptor.formatVersion(m_cmsMaxVersionField.getText(), true, true);
      }
      catch(RuntimeException e)
      {
         String rawMsg = fieldName.equals(
            StringUtils.chomp(getResourceString("version"), ":")) 
               ? getResourceString("invalidVersionFormat") 
               : getResourceString("invalidCmsVersionFormat");
         ErrorDialogs.showErrorMessage(this,
                  MessageFormat.format(rawMsg, fieldName),
                  getResourceString("error"));
         return false;         
      }
      String descName = m_descriptorField.getText().trim();
      if (descName.length() == 0)
      {
         ErrorDialogs.showErrorMessage(this,
                  getResourceString("mustEnterName"),
                  getResourceString("error"));
         return false;
      }

      if (!(m_isExistingDesc && m_descriptor.getName().equalsIgnoreCase(
               descName)))
      {
         if (!PSDeploymentClient.isValidServerObjectName(descName))
         {
            ErrorDialogs.showErrorMessage(this,
                     getResourceString("invalidDescName"),
                     getResourceString("error"));
            return false;
         }

         try
         {
            boolean exists = false;
            Iterator descriptors = m_deploymentServer.getDescriptors(false)
                     .getResults();
            while (descriptors.hasNext() && !exists)
            {
               if (((PSCatalogResult) descriptors.next()).getID()
                        .equalsIgnoreCase(descName))
               {
                  exists = true;
               }
            }

            if (exists)
            {
               String confMsg = getResourceString("duplicateName");
               confMsg = MessageFormat.format(confMsg, new Object[]
               {descName});
               confMsg = PSLineBreaker.wrapString(confMsg, 80, 25, "\n");
               int response = JOptionPane.showConfirmDialog(this, confMsg,
                        getResourceString("warning"),
                        JOptionPane.OK_CANCEL_OPTION);
               // if no, stop and select the field
               if (response == JOptionPane.CANCEL_OPTION)
               {
                  return false;
               }
            }
         }
         catch (PSDeployException e)
         {
            ErrorDialogs.showErrorMessage(this, e.getLocalizedMessage(),
                     getResourceString("error"));
            return false;
         }
      }      
      
      try
      {
         if(!validateConfigs())
            return false;
      }
      catch (PSDeployException e)
      {
         ErrorDialogs.showErrorMessage(this, e.getLocalizedMessage(),
                  getResourceString("error"));
         return false;
      }
      
      return true;
   }
   
   /**
    * Validate the configuration files specified.
    * We validate that either both configs or no configs
    * have been specified. We validate the local config against
    * the localConfig.xsd.
    * @return <code>true</code> if validation passes.
    * @throws PSDeployException on any error.
    */
   private boolean validateConfigs() throws PSDeployException
   {
      //Both configs must be specified or none
      boolean configDefSpecified = 
         StringUtils.isNotBlank(m_implConfigFileField.getText()); 
      boolean localDefSpecified = 
         StringUtils.isNotBlank(m_localConfigFileField.getText());
      if(configDefSpecified ^ localDefSpecified)
      {
         Object[] args =
         {
            StringUtils.chomp(getResourceString("localConfigFile"), ":"),
            StringUtils.chomp(getResourceString("implConfigFile"), ":")
         };
         if(configDefSpecified)
         {
            ArrayUtils.reverse(args);
         }         
         
         ErrorDialogs.showErrorMessage(this,
            MessageFormat.format(
               getResourceString("bothConfigsMustBeSpecified"), args),
            getResourceString("error"));
         return false;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     
      }
      
      //Validate local config against schema
      if(StringUtils.isNotBlank(m_localConfigFileField.getText()))
      {
         File localConfigFile = new File(m_localConfigFileField.getText());
         PSDeploymentManager deploymentMgr = 
            m_deploymentServer.getDeploymentManager();
         List<String> errors = 
            deploymentMgr.validateLocalConfigFile(localConfigFile);
         if(errors != null && !errors.isEmpty())
         {
            StringBuilder sb = new StringBuilder();
            for(String error : errors)
            {
               sb.append("\n");
               sb.append(error);               
            }
            
            Object[] args = 
            {
               StringUtils.chomp(getResourceString("localConfigFile"), ":"), 
               sb.toString()
            };
            ErrorDialogs.showErrorMessage(this,
               MessageFormat.format(getResourceString("invalidConfigFile"), args),
               getResourceString("error"));
            return false; 
         }
      }
      return true;
   }
   
   /**
    * Gets user chosen option to create archive. Should be called after the
    * dialog is hidden by clicking Next button in the dialog.
    * 
    * @return <code>true</code> if user has chosen to create archive,
    *         otherwise not.
    */
   public boolean isCreateArchive()
   {
      return m_isCreateArchive;
   }

   /**
    * Gets the archive file that need to be created. Should be called only if 
    * the call to {@link #isCreateArchive()} returned <code>true</code> after 
    * the dialog is hidden by clicking Next button in the dialog. 
    * 
    * @return the archive file, may be <code>null</code> if the call to {@link 
    * #isCreateArchive()} returned <code>false</code>.
    */   
   public File getArchiveFile()
   {
      return m_archiveFile;
   }

   /**
    * The flag to indicate whether the supplied descriptor is an existing 
    * descriptor or not, initialized in the constructor and never modified after
    * that.
    */
   private boolean m_isExistingDesc;
   
   /**
    * The flag to indicate whether user has chosen to create an archive or not,
    * initialized to <code>false</code> and set to <code>true</code> in <code>
    * onNext()</code> if the 'Create Archive' check box is checked.
    */
   private boolean m_isCreateArchive = false;
   
   /**
    * The archive file that need to be created,  <code>null</code> until <code>
    * onNext()</code> is called and the validation succeeds.
    */
   private File m_archiveFile = null;
   
   /**
    * The check box to choose to create an archive or not. Initialized in <code>
    * initDialog()</code> and never <code>null</code> or modified after that. 
    */
   private JCheckBox m_createArchiveCheck;
   
   /**
    * The text field to enter the archive file name, enabled only if the 'Create
    * Archive' check box is checked. Initialized in <code>initDialog()</code> 
    * and never <code>null</code> or modified after that. 
    */
   private UTFixedHeightTextField m_archiveField;
   
   /**
    * The text field to enter the description for the descriptor. Initialized in
    * <code>initDialog()</code> and never <code>null</code> or modified after 
    * that. 
    */
   private JTextArea m_descriptionField;
      
   /**
    * The text field to edit/view the descriptor name, enabled only if the 
    * 'Save Descriptor' check box is checked and the descriptor is not an 
    * existing descriptor. Initialized in <code>initDialog()</code> 
    * and never <code>null</code> or modified after that. 
    */
   private JTextField m_descriptorField;
   
   /**
    * The text field to enter the version for the descriptor. Initialized in
    * <code>initDialog()</code> and never <code>null</code> or modified after 
    * that. 
    */
   private JTextField m_versionField;
   
   /**
    * The text field to enter the vendor name for the descriptor. Initialized in
    * <code>initDialog()</code> and never <code>null</code> or modified after 
    * that. 
    */
   private JTextField m_vendorNameField;
   
   /**
    * The text field to enter the vendor url for the descriptor. Initialized in
    * <code>initDialog()</code> and never <code>null</code> or modified after 
    * that. 
    */
   private JTextField m_vendorUrlField;
   
   /**
    * The text field to enter the CMS max version for the descriptor. Initialized in
    * <code>initDialog()</code> and never <code>null</code> or modified after 
    * that. 
    */
   private JTextField m_cmsMaxVersionField;
   
   /**
    * The text field to enter the CMS min version for the descriptor. Initialized in
    * <code>initDialog()</code> and never <code>null</code> or modified after 
    * that. 
    */
   private JTextField m_cmsMinVersionField;
   
   /**
    * The text field to enter the implementor config file for the descriptor. Initialized in
    * <code>initDialog()</code> and never <code>null</code> or modified after 
    * that. 
    */
   private UTFixedHeightTextField m_implConfigFileField;
   
   /**
    * The text field to enter the local config file for the descriptor. Initialized in
    * <code>initDialog()</code> and never <code>null</code> or modified after 
    * that. 
    */
   private UTFixedHeightTextField m_localConfigFileField;
}
