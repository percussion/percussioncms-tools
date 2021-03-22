/******************************************************************************
 *
 * [ PSSelectionView.java ]
 * 
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.packageinstaller.ui;

import com.percussion.deployer.client.PSDeploymentServerConnection;
import com.percussion.packageinstaller.ui.managers.PSInstallerServerConnectionManager;
import com.percussion.packager.ui.PSResourceUtils;
import com.percussion.packager.ui.data.PSServerRegistration;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

/**
 * @author erikserating
 *
 */
public class PSSelectionView extends JPanel implements DocumentListener
{

   public PSSelectionView(PSPackageInstallerFrame frame)
   {      
      m_parent = frame;
      init();
   }
   
   /**
    * 
    * @return
    */
   public PSServerRegistration getServerRegistration()
   {
      Object host = m_hostField.getSelectedItem();
      String ports = m_portField.getText();
      String user = m_userField.getText();
      String rawPass = new String(m_passwordField.getPassword());
      String password = rawPass;
      int port = 0;
      try {
         port = Integer.parseInt(ports);
      }catch (NumberFormatException ne){
         System.out.println("Port should be a valid number :" + ports);
         return null;
      }

      boolean isSSL = m_useSSLBox.isSelected();
      
      if(host == null || port == 0 ||
         StringUtils.isBlank(user))
         return null;

      PSServerRegistration server = new PSServerRegistration(host.toString(),
         port,
         user,
         password,
         true,
         isSSL);
      return server;
      
   }
   
   public File getPackageFile()
   {
      return m_packageFile;
   }   
   
   private void init()
   {
      MigLayout layout = new MigLayout(
         "fill, wrap 1",
         "[center]",
         "[][][]");
      setLayout(layout);
      add(getPkgSelectionPanel(), "width 560px");
      add(getPkgInfoPanel(), "width 560px");
      add(getTargetSelectionPanel(), "width 560px");
      
      loadComboBox();
   }
   
   
   /*
    * Select Package File Panel
    */
   private JPanel getPkgSelectionPanel()
   {
      MigLayout layout = new MigLayout(
            "wrap 2",
            "[160][]",
            "[][][][]");
      
      JPanel mainPanel = new JPanel();
      mainPanel.setLayout(layout);
      
      JLabel headingOne = new JLabel(getResourceString("heading.selectPkg"));
      
      m_fc = new JFileChooser();
      m_fc.setApproveButtonText(getResourceString("button.fcSelect"));
      m_fc.setDialogTitle(getResourceString("title.fc"));
      m_fc.setFileFilter(new FileFilter()
         {
            @Override
            public boolean accept(File f)
            {
               return f.isDirectory() ||
                  f.getName().toLowerCase().endsWith(".ppkg");
            }

            @Override
            public String getDescription()
            {               
               return "Percussion Package (.ppkg)";
            }
         
         });
      disableNewFolderButton(m_fc);
      
      JLabel pathLabel = new JLabel(getResourceString("label.path"));
      m_pkgPathField = new JTextField(60);
      
      Dimension dim = new Dimension(25, 20);
      JButton packageFileButton = new JButton(getResourceString("button.addFile"));
      packageFileButton.setMaximumSize(dim);
      packageFileButton.setPreferredSize(dim);
      packageFileButton.setMinimumSize(dim);
      packageFileButton.addActionListener(new ActionListener()
      {
         public void actionPerformed(@SuppressWarnings("unused")
         ActionEvent e)
         {
            String path = loadDirPath();
            if(path != null)
               m_fc.setSelectedFile(new File(path));
            int returnVal = m_fc.showOpenDialog(PSSelectionView.this);
            if (returnVal == JFileChooser.APPROVE_OPTION) 
            {
                m_packageFile = m_fc.getSelectedFile();
                saveDirPath(m_packageFile.getAbsolutePath());
                m_pkgPathField.setText(m_packageFile.getAbsolutePath());                
                m_parent.onPackageFileSelected();
            }
         }
      });
      
      URL packageImageFile = getClass().getResource(
            getResourceString("image.package"));

      if (packageImageFile != null)
      {
         ImageIcon packageImage = new ImageIcon(packageImageFile);
         JLabel packageLabel = new JLabel(packageImage);
         mainPanel.add(packageLabel, "spany 4, growx");
      }  
      
      mainPanel.add(headingOne);
      mainPanel.add(pathLabel);
      mainPanel.add(m_pkgPathField, "split");
      mainPanel.add(packageFileButton);

      return mainPanel;
   }
   
   /**
    * Saves the specified directory path to the file system.
    * @param path the path, assumed not <code>null</code>.
    */
   private void saveDirPath(String path)
   {
      File file = new File(DIRECTORY_SAVE_FILE);
      FileOutputStream os = null;
      try
      {
         Properties props = new Properties();
         props.put("path", path);
         os = new FileOutputStream(file);
         props.store(os, "---No Comments---");
      }
      catch(IOException ignore){}
      finally
      {
         if(os != null)
         {
            try
            {
               os.close();
            }
            catch (IOException ignore){}
         }
      }
   }
   
   /**
    * Loads the persisted directory path if it exists.
    * @return the directory path or <code>null</code> if
    * not found.
    */
   private String loadDirPath()
   {
      String path = null;
      File file = new File(DIRECTORY_SAVE_FILE);
      if(!file.exists())
         return null;
      FileInputStream is = null;
      try
      {
         Properties props = new Properties();
         is = new FileInputStream(file);
         props.load(is);
         path = (String)props.get("path");
      }
      catch(IOException ignore){}
      finally
      {
         if(is != null)
         {
            try
            {
               is.close();
            }
            catch (IOException ignore){}
         }
      }
      return path;
   }
   
   /**
    * Returns the description text pane control.
    * @return the text pane, may be <code>null</code> if
    * called before init.
    */
   public JTextPane getDecriptionTextPane()
   {
      return m_descTextPane;
   }
   
   /*
    * Verify Package Information Panel
    */
   private JPanel getPkgInfoPanel()
   {
      MigLayout layout = new MigLayout(
            "wrap 2",
            "[100][grow]",
            "[][grow]");
      
      JPanel mainPanel = new JPanel();
      mainPanel.setLayout(layout);
      
      JLabel headingTwo = new JLabel(getResourceString("heading.pkgInfo"));
      
      JScrollPane descTextArea = new JScrollPane(m_descTextPane, 
      JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, 
      JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
      m_descTextPane.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
      m_descTextPane.setEditable(false);
      

      URL packageImageFile = getClass().getResource(
            getResourceString("image.tag"));
      if (packageImageFile != null)
      {
         ImageIcon packageImage = new ImageIcon(packageImageFile);
         JLabel packageLabel = new JLabel(packageImage);
         mainPanel.add(packageLabel, "spany 2");
      }
      
      mainPanel.add(headingTwo);
      mainPanel.add(descTextArea,"h 20:400,grow");

      return mainPanel;
   }
   
   /*
    * Select Target Server Panel
    */
   private JPanel getTargetSelectionPanel()
   {
      MigLayout layout = new MigLayout(
            "wrap 2",
            "[160][][]",
            "[][][][][]");
      
      JPanel mainPanel = new JPanel();
      mainPanel.setLayout(layout);
      
      JLabel headingThree = new JLabel(getResourceString("heading.selectTarget"));
      
      JLabel hostLabel = new JLabel(getResourceString("label.host"));
      m_hostField = new JComboBox();
      m_hostField.setRenderer(new ComboRenderer());
      
      m_hostField.setEditable(true);
      m_hostField.addActionListener(new ActionListener()
      {
         public void actionPerformed(@SuppressWarnings("unused")
         ActionEvent e)
         {
            JComboBox combo = (JComboBox)e.getSource();
            ActionListener[] listeners = combo.getActionListeners();
            for(ActionListener l : listeners)
               combo.removeActionListener(l);
            int index = combo.getSelectedIndex();
            if(index != -1)
            {              
               PSInstallerServerConnectionManager connMgr = 
                  PSInstallerServerConnectionManager.getInstance();
               String item = ((ComboItem)combo.getSelectedItem()).getValue();
               PSServerRegistration server = connMgr.getServerByHostPortString(item);
               if(server != null)
               {
                  m_portField.setText(String.valueOf(server.getPort()));
                  m_userField.setText(server.getUserName());
                  m_passwordField.setText(PSDeploymentServerConnection.decryptPwd(
                     server.getUserName(), server.getPassword()));
                  m_useSSLBox.setSelected(server.isUseSSL());
               }
               
            }            
            for(ActionListener l : listeners)
               combo.addActionListener(l);
            
         }
      });
      
      Dimension preferredSize = new Dimension(400,20);
      Dimension portSize = new Dimension(50,20);
      
      URL packageImageFile = getClass().getResource(
            getResourceString("image.server"));
      if (packageImageFile != null)
      {
         ImageIcon packageImage = new ImageIcon(packageImageFile);
         JLabel packageLabel = new JLabel(packageImage);
         mainPanel.add(packageLabel, "spany 11, growx");
      } 
      
      mainPanel.add(headingThree, "wrap 1");
      
      JLabel portLabel = new JLabel(getResourceString("label.port"));
      m_portField = new JTextField();
      m_portDocument = m_portField.getDocument();
      m_portDocument.addDocumentListener(this);
      m_portField.setPreferredSize(portSize);
      m_portField.setMinimumSize(portSize);
      m_portField.setMaximumSize(portSize);
      
      JLabel userLabel = new JLabel(getResourceString("label.user"));
      m_userField = new JTextField();
      m_userField.getDocument().addDocumentListener(this);
      m_userField.setPreferredSize(preferredSize);
      
      JLabel passwordLabel = new JLabel(getResourceString("label.password"));
      m_passwordField = new JPasswordField();
      m_passwordField.setPreferredSize(preferredSize);
      
      m_useSSLBox = new JCheckBox();
      JLabel sslLabel = new JLabel(getResourceString("label.useSSL"));
      
      mainPanel.add(hostLabel);
      mainPanel.add(m_hostField, "width 500px");
      mainPanel.add(portLabel);
      mainPanel.add(m_portField, "growx");
      mainPanel.add(userLabel);
      mainPanel.add(m_userField, "growx");
      mainPanel.add(passwordLabel);
      mainPanel.add(m_passwordField, "growx");
      mainPanel.add(m_useSSLBox,"cell 1 9, split");
      mainPanel.add(sslLabel);

      return mainPanel;

   }
   
   /**
    * Loads combo box from recent connections list.
    */
   void loadComboBox()
   {
      PSInstallerServerConnectionManager connMgr = 
         PSInstallerServerConnectionManager.getInstance();
      DefaultComboBoxModel model = (DefaultComboBoxModel)m_hostField.getModel();
      model.removeAllElements();
      for(String recent : connMgr.getRecentConnections())
      {
         model.addElement(new ComboItem(recent));
      }
      
   }
   
   /* (non-Javadoc)
    * @see javax.swing.event.DocumentListener#changedUpdate(
    * javax.swing.event.DocumentEvent)
    */
   public void changedUpdate(@SuppressWarnings("unused") DocumentEvent event)
   {
      m_parent.handleButtonState();         
   }

   /* (non-Javadoc)
    * @see javax.swing.event.DocumentListener#insertUpdate(
    * javax.swing.event.DocumentEvent)
    */
   public void insertUpdate(@SuppressWarnings("unused") DocumentEvent event)
   {      
      m_parent.handleButtonState();
   }

   /* (non-Javadoc)
    * @see javax.swing.event.DocumentListener#removeUpdate(
    * javax.swing.event.DocumentEvent)
    */
   public void removeUpdate(@SuppressWarnings("unused") DocumentEvent event)
   {      
      m_parent.handleButtonState();
   }
   
   
   /**
    * 
    * @param key
    * @return
    */
   private String getResourceString(String key)
   {
      return PSResourceUtils.getResourceString(this.getClass(), key);
   }
   
   /**
    * Removes the new folder button on JFileChooser
    */ 
   public void disableNewFolderButton( Container c ) {
      int len = c.getComponentCount();
      for (int i = 0; i < len; i++) {
        Component comp = c.getComponent(i);
        if (comp instanceof JButton) {
          JButton b = (JButton)comp;
          Icon icon = b.getIcon();
          if (icon != null
               && icon == UIManager.getIcon("FileChooser.newFolderIcon"))
             b.setVisible(false);
          }
        else if (comp instanceof Container) {
          disableNewFolderButton((Container)comp);
        }
      }
    }
   
   class ComboItem
   {
      public ComboItem(String hostport)
      {
         mi_hostport = hostport;
      }
      
      public String getValue()
      {
         return mi_hostport;
      }
      
      @Override
      public String toString()
      {
         return StringUtils.substringBeforeLast(mi_hostport, ":");
      }
      
      private String mi_hostport;
   }
   
   class ComboRenderer extends JLabel implements ListCellRenderer
   {

      /* (non-Javadoc)
       * @see javax.swing.ListCellRenderer#getListCellRendererComponent(
       * javax.swing.JList, java.lang.Object, int, boolean, boolean)
       */
      public Component getListCellRendererComponent(
            @SuppressWarnings("unused") JList list, 
            Object value,
            @SuppressWarnings("unused") int index, 
            @SuppressWarnings("unused") boolean isSelected, 
            @SuppressWarnings("unused") boolean hasFocus)
      {
         setText(((ComboItem)value).getValue());
         return this;
      }
   }
   
   
   /**
    * A reference to the parent frame. Initialized in the
    * ctor. Never <code>null</code> after that.
    */
   private PSPackageInstallerFrame m_parent;
   
   /*
    * Package Description
    */
   private JTextPane m_descTextPane = new JTextPane();

   /*
    * package path
    */
   private JTextField m_pkgPathField;
   
   /*
    * Target Server Host
    */
   private JComboBox m_hostField;
   
   /*
    * Target Server Port
    */
   private JTextField m_portField;
   
   /**
    * The Port field document.
    */
   private Document m_portDocument;
   
   /*
    * Target Server User
    */
   private JTextField m_userField;
   
   /*
    * Target Server Password
    */
   private JPasswordField m_passwordField;
   
   /*
    * Target Server Turn on SSL
    */
   private JCheckBox m_useSSLBox;
   
   /**
    * File chooser
    */
   private JFileChooser m_fc;
   
   /**
    * Import file
    */
   private File m_packageFile;
   
   /**
    * Name of file to save directory path.
    */
   private static final String DIRECTORY_SAVE_FILE = "saveDirectory";

   
}