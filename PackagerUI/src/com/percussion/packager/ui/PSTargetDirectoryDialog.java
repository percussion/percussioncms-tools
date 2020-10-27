/******************************************************************************
 *
 * [ PSTargetDirectoryDialog.java ]
 * 
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.packager.ui;

import com.percussion.packager.ui.data.PSPackageDescriptorMetaInfo;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * A small dialog that queries the user for a target directory.
 * Also will ask for export operation if in export mode.
 * @author erikserating
 *
 */
public class PSTargetDirectoryDialog extends JDialog implements ActionListener
{

   /**
    * Ctor
    * @param frame parent frame, may be <code>null</code>.
    * @param info package meta info used for Default config, may be <code>null</code>.
    * @param exportMode flag indicating dialog should be shown in
    * export mode which will add export operation radio buttons.
    */
   public PSTargetDirectoryDialog(Frame frame,PSPackageDescriptorMetaInfo info,
         boolean exportMode)
   {
      super(frame);
      m_info = info;
      init(exportMode);
   }
   
   /**
    * Layout the dialog and initialize components.
    * @param exportMode
    */
   private void init(boolean exportMode)
   {
      String titleKey = exportMode ?
         "title.export.mode" :
         "title";
      setTitle(getResourceString(titleKey));
      setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);      
      setModal(true);
      JPanel mainPanel = new JPanel();
      getContentPane().add(mainPanel);
      
      MigLayout mainlayout = new MigLayout(
         "wrap 1, fill, hidemode 3",
         "[grow, shrink]",
         "[grow][grow][grow][center]");
      mainPanel.setLayout(mainlayout);
      String msgKey = exportMode ?
         "msg.export.mode" :
         "msg";
      m_messageLabel = 
         new JLabel("<html>" + getResourceString(msgKey) + "</html>");
      mainPanel.add(m_messageLabel, "grow");
      
      JPanel radioPanel = new JPanel();
      radioPanel.setBorder(BorderFactory.createTitledBorder(
         BorderFactory.createEtchedBorder(), "Operations"));
      MigLayout radioLayout = new MigLayout(
         "",
         "[][][]",
         "[]");
      radioPanel.setLayout(radioLayout);
      m_configDefRadio = 
         new JRadioButton(getResourceString("button.config.def"));
      m_configDefRadio.setSelected(true);
      radioPanel.add(m_configDefRadio);
      m_defaultConfigRadio = 
         new JRadioButton(getResourceString("button.default.config"));
      radioPanel.add(m_defaultConfigRadio);
      m_summaryRadio = 
         new JRadioButton(getResourceString("button.summary"));
      radioPanel.add(m_summaryRadio);
      ButtonGroup bGroup = new ButtonGroup();
      bGroup.add(m_configDefRadio);
      bGroup.add(m_defaultConfigRadio);
      bGroup.add(m_summaryRadio);
      
      mainPanel.add(radioPanel, "growx");
      radioPanel.setVisible(exportMode);
      
      JPanel pathPanel = new JPanel();      
      MigLayout pathLayout = new MigLayout(
         "fillx",
         "[grow, fill, 360::][]",
         "[][grow]");
      pathPanel.setLayout(pathLayout);
      JLabel pathLabel = new JLabel(getResourceString("label.directory"));
      pathPanel.add(pathLabel, "wrap");
      String path = ms_path != null ?
         ms_path.getAbsolutePath() : "";
      m_pathTextField = new JTextField(path);      
      m_pathTextField.setToolTipText(path);
      pathPanel.add(m_pathTextField, "growx");
      m_browseButton = new JButton("...");
      Dimension dim = new Dimension(25, 20);
      m_browseButton.setMaximumSize(dim);
      m_browseButton.setPreferredSize(dim);
      m_browseButton.setMinimumSize(dim);
      
      m_browseButton.addActionListener(this);
      pathPanel.add(m_browseButton);
      
      mainPanel.add(pathPanel, "growx");
      
      JPanel cmdPanel = new JPanel();
      MigLayout cmdLayout = new MigLayout(
         "center",
         "[][]",
         "[]");
      cmdPanel.setLayout(cmdLayout);
      m_okButton = 
         new JButton(PSResourceUtils.getCommonResourceString("label.ok"));
      m_okButton.addActionListener(this);
      cmdPanel.add(m_okButton, "sg 1");
      
      m_cancelButton = 
         new JButton(PSResourceUtils.getCommonResourceString("label.cancel"));
      m_cancelButton.addActionListener(this);
      cmdPanel.add(m_cancelButton, "sg 1");
      
      mainPanel.add(cmdPanel, "growx");
      
      pack();
      PSUiUtils.center(this);
      
   }
   
   /* (non-Javadoc)
    * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
    */
   public void actionPerformed(ActionEvent event)
   {
      Object source = event.getSource();
      if(source == m_browseButton)
      {
         JFileChooser chooser = new JFileChooser();
         PSUiUtils.makeDirChooser(chooser);
         m_temp_path = new File(m_pathTextField.getText());
         chooser.setSelectedFile(m_temp_path);
         int result = chooser.showDialog(this,
            getResourceString("title.select"));
         if(result == JFileChooser.APPROVE_OPTION)
         {
            chooser.setMultiSelectionEnabled(false);
            File dir = chooser.getSelectedFile();
            m_temp_path = dir;
            m_pathTextField.setText(m_temp_path.getAbsolutePath());
            m_pathTextField.setToolTipText(m_temp_path.getAbsolutePath());
         }
      }
      if(source == m_cancelButton)
      {
         isOk = false;
         setVisible(false);
      }
      if(source == m_okButton)
      {
         boolean noPathSpecified = 
            StringUtils.isBlank(m_pathTextField.getText());
         File temp = null;
         if(!noPathSpecified)
            temp = new File(m_pathTextField.getText());
         if(noPathSpecified || (!temp.exists() || !temp.isDirectory()))
         {
            String msg  = getResourceString("msg.error.invalid.dir");
            JOptionPane.showMessageDialog(this, msg,
                     PSResourceUtils.getCommonResourceString("errorTitle"),
                     JOptionPane.ERROR_MESSAGE);
            return;
         }
         if(m_configDefRadio.isSelected())
         {
            m_operation = TYPE_CONFIGDEF;
         }
         else if(m_defaultConfigRadio.isSelected())
         {
            if(m_info == null)
            {
               String msg = getResourceString("msg.error.missing.package.info");
               JOptionPane.showMessageDialog(this, msg,
                        PSResourceUtils.getCommonResourceString("errorTitle"),
                        JOptionPane.ERROR_MESSAGE);
               return;
            }
            File configDef = new File(temp.getAbsolutePath(), m_info.getName() + "_configDef.xml");
            if(!configDef.exists() || !configDef.canRead())
            {
               String msg = getResourceString("msg.error.missing.configdef");
               JOptionPane.showMessageDialog(this, msg,
                        PSResourceUtils.getCommonResourceString("errorTitle"),
                        JOptionPane.ERROR_MESSAGE);
               return;
            }
            else
            {
               m_operation = TYPE_DEFAULTCONFIG;
            }
         }
         else
         {
            m_operation = TYPE_SUMMARY;
         }
         m_temp_path = new File(m_pathTextField.getText());
         isOk = true;
         ms_path = m_temp_path;
         setVisible(false);
      }
      
   }
   
   /**
    * Displays the dialog and blocks.
    * @return a flag that if <code>true</code> indicates that
    * the ok button was pressed.
    */
   public boolean showTargetDirectoryDialog()
   {
      setVisible(true);
      return isOk;
   }
   
   /**
    * Get the directory path from the dialog as entered or
    * chosen by the user.
    * @return may be <code>null</code> if cancel was pressed.
    */
   public File getPath()
   {
      return new File(ms_path.getAbsolutePath());
   }
   
   /**
    * Returns the export operation type of
    * either TYPE_CONFIGDEF or TYPE_SUMMARY.
    * @return the type.
    */
   public int getOperation()
   {
      return m_operation;
   }
   
   /**
    * Gets a resource string from this classes resource bundle.
    * @param key cannot be <code>null</code>.
    * @return the resource string or the key if not found.
    */
   private String getResourceString(String key)
   {
      return PSResourceUtils.getResourceString(
         getClass(), key);
   }
   
   /**
    * @param args
    */
   public static void main(String[] args)
   {
      PSTargetDirectoryDialog dialog = new PSTargetDirectoryDialog(null, null, false);
      boolean result = dialog.showTargetDirectoryDialog();
      if(result)
         System.out.println("True");
      else
         System.out.println("False");
   }
   
   /**
    * 
    */
   private JLabel m_messageLabel;
   
   /**
    * 
    */
   private JButton m_okButton;
   
   /**
    * 
    */
   private JButton m_cancelButton;
   
   /**
    * 
    */
   private JButton m_browseButton;
   
   /**
    * 
    */
   private JTextField m_pathTextField;
   
   /**
    * 
    */
   private JRadioButton m_configDefRadio;
   
   /**
    * 
    */
   private JRadioButton m_defaultConfigRadio;
   
   /**
    * 
    */
   private JRadioButton m_summaryRadio;
   
   /**
    * 
    */
   private int m_operation;
   
   /**
    * 
    */
   private boolean isOk;
   
   /**
    * 
    */
   private File m_temp_path;
   
   /**
    * The file path, held as a static so that it persists through
    * the life of the application.
    */
   private static File ms_path;
   
   /**
    * 
    */
   private PSPackageDescriptorMetaInfo m_info;
   
   /**
    * Export operation type for default config generation.
    */
   public static final int TYPE_DEFAULTCONFIG = 2;
   
   /**
    * Export operation type for config def generation.
    */
   public static final int TYPE_CONFIGDEF = 1;
   
   /**
    * Export operation type for summary generation.
    */
   public static final int TYPE_SUMMARY = 0;
   
   
   
   
   

  
   

}
