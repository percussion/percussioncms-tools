/******************************************************************************
 *
 * [ PSGeneralPage.java ]
 * 
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.packager.ui;

import com.percussion.desktop.deployer.client.PSDeploymentServerConnection;
import com.percussion.desktop.deployer.objectstore.PSDescriptor;
import com.percussion.packager.ui.data.PSPackageDependency;
import com.percussion.packager.ui.data.PSPackageDescriptorMetaInfo;
import com.percussion.packager.ui.managers.PSServerConnectionManager;
import com.percussion.packager.ui.model.PSPackagerClientModel;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author erikserating
 *
 */
public class PSGeneralPage extends JPanel implements IPSPage, DocumentListener
{

   public PSGeneralPage()
   {
      init();
   }
   
   private void init()
   {
      MigLayout layout = new MigLayout("fill", "[]", "[TOP]");
      setLayout(layout);
      this.add(getMainPanel(), "grow");
      turnOnListener(true);
   }
   
   /*
    * Sets Main Panel
    */
   public JPanel getMainPanel()
   {
      MigLayout layout = new MigLayout(
            "fill",
            "[50%][50%]",
            "[top]20[top][top]");
      
      JPanel mainPanel = new JPanel();
      mainPanel.setLayout(layout);
      
      JLabel title = new JLabel(getResourceString("title"));
      
      
      MigLayout leftLayout = new MigLayout("fill", "[]", "[top]0[top]");
      JPanel leftPanel = new JPanel();
      leftPanel.setLayout(leftLayout);
      leftPanel.add(getTopLeftPanel(), "growx, wrap 1");
      leftPanel.add(getBottomLeftPanel(), "grow");
      
      MigLayout rightLayout = new MigLayout("fill", "[]", "[top]0[top]");
      JPanel rightPanel = new JPanel();
      rightPanel.setLayout(rightLayout);
      rightPanel.add(getTopRightPanel(), "grow");
      
      
      mainPanel.add(title, "wrap 1, span 2, growprio 0, gapy 0px 20px");
      mainPanel.add(leftPanel,"growx 50, growy");
      mainPanel.add(rightPanel, "growx 50, growy");
      return mainPanel;
   }
   
   /*
    * Sets Top Left Panel
    */
   private JPanel getTopLeftPanel()
   {
      MigLayout layout = new MigLayout(
            "fillx, wrap 1",
            "[]",
            "[TOP][TOP][TOP][TOP]");
      
      JPanel mainPanel = new JPanel();
      mainPanel.setLayout(layout);
      
      JLabel pkgNameLabel = new JLabel(getResourceString("label.pkgName"));
      m_pkgNameTextField = new JTextField();
      
      JLabel descLabel = new JLabel(getResourceString("label.desc"));
      m_descTextArea.setLineWrap(true);
      m_descTextArea.setWrapStyleWord(true);
      JScrollPane descTextArea = new JScrollPane(m_descTextArea, 
            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, 
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
      m_descTextArea.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
      
      mainPanel.add(pkgNameLabel);
      mainPanel.add(m_pkgNameTextField, "grow");
      mainPanel.add(descLabel);
      mainPanel.add(descTextArea, "h 50:150, grow");

      return mainPanel;
   }
   
   /*
    * Sets Bottom Left Panel
    */
   private JPanel getBottomLeftPanel()
   {
      MigLayout layout = new MigLayout(
            "wrap 1",
            "[]",
            "[][][][][][]");
      
      JPanel mainPanel = new JPanel();
      mainPanel.setLayout(layout);
      
      JLabel versionLabel = new JLabel(getResourceString("label.version"));
      m_versionTextField = new JTextField();
      
      JLabel cmsMinLabel = new JLabel(getResourceString("label.cmsMin"));
      m_cmsMinTextField = new JTextField();
      
      JLabel cmsMaxLabel = new JLabel(getResourceString("label.cmsMax"));
      m_cmsMaxTextField = new JTextField();
      m_cmsMaxTextField.setEnabled(false);
      
      mainPanel.add(versionLabel);
      mainPanel.add(m_versionTextField, "grow");
      mainPanel.add(cmsMinLabel);
      mainPanel.add(m_cmsMinTextField, "grow");
      mainPanel.add(cmsMaxLabel);
      mainPanel.add(m_cmsMaxTextField, "grow");
      return mainPanel;
   }
   
   /*
    * Sets Top Right Panel
    */
   private JPanel getTopRightPanel()
   {
      MigLayout layout = new MigLayout(
            "fillx, wrap 1",
            "[]",
            "[Top][Top][Top][Top][Top]");
      
      JPanel mainPanel = new JPanel();
      mainPanel.setLayout(layout);
      
      JLabel pubNameLabel = new JLabel(getResourceString("label.pubName"));
      m_pubNameTextField = new JTextField();
      
      JLabel pubUrlLabel = new JLabel(getResourceString("label.pubUrl"));
      m_pubUrlTextField = new JTextField();
      
      mainPanel.add(pubNameLabel);
      mainPanel.add(m_pubNameTextField, "grow");
      mainPanel.add(pubUrlLabel);
      mainPanel.add(m_pubUrlTextField, "grow");
      return mainPanel;

   }
   
   public void focusAndSelectPackageName()
   {
      m_pkgNameTextField.selectAll();
      m_pkgNameTextField.requestFocus();
      
   }
      
   /* (non-Javadoc)
    * @see com.percussion.packager.ui.IPSPage#load(java.lang.Object)
    */
   public void load(PSPackagerClientModel model)
   {
      
      PSPackageDescriptorMetaInfo metaInfo = model.getDescriptorMetaInfo();
      if (metaInfo != null)
      {
         turnOnListener(false);
         m_pkgNameTextField.setText(metaInfo.getName());
         m_pkgNameTextField.setEnabled(model.isDescriptorNew());
         m_descTextArea.setText(metaInfo.getDescription());
         m_versionTextField.setText(metaInfo.getVersion());
         if(StringUtils.isEmpty(m_versionTextField.getText()))
            m_versionTextField.setText(getResourceString("default.version"));
         m_cmsMinTextField.setText(metaInfo.getCmsMinVersion());
         if(StringUtils.isEmpty(m_cmsMinTextField.getText()))
            m_cmsMinTextField.setText(getServerVersion());
         m_cmsMaxTextField.setText(getServerVersion());
         m_pubNameTextField.setText(metaInfo.getPublisherName());
         m_pubUrlTextField.setText(metaInfo.getPublisherUrl());
         turnOnListener(true);
      }
      if (model.isDescriptorNew())
      {
         m_pkgNameTextField.setEditable(true);
         focusAndSelectPackageName();
      }
      else
      {
         m_pkgNameTextField.setEditable(false);
      }
   }

   /* (non-Javadoc)
    * @see com.percussion.packager.ui.IPSPage#update(java.lang.Object)
    */
   public void update(PSPackagerClientModel model)
   {
      PSPackageDescriptorMetaInfo metaInfo = model.getDescriptorMetaInfo();
      if (metaInfo != null)
      {
         metaInfo.setName(m_pkgNameTextField.getText());
         metaInfo.setDescription(m_descTextArea.getText());
         metaInfo.setVersion(m_versionTextField.getText());
         metaInfo.setCmsMinVersion(PSDescriptor.formatVersion(
               m_cmsMinTextField.getText(), true, true));
         metaInfo.setCmsMaxVersion(getServerVersion());
         metaInfo.setPublisherName(m_pubNameTextField.getText());
         metaInfo.setPublisherUrl(m_pubUrlTextField.getText());
      }
   }

   /* (non-Javadoc)
    * @see com.percussion.packager.ui.IPSPage#validateData()
    */
   public List<String> validateData()
   {
      List<String> errorList = new ArrayList<String>();

      // Check Package Name
      if (StringUtils.isBlank(m_pkgNameTextField.getText()))
      {
         errorList.add(getResourceString("error.mustEnterPkgName"));
      }
      else
      {
         //New Descriptor Checks
         PSPackagerClientModel model = PSPackagerClient.getFrame().getModel();
         if (model.isDescriptorNew())
         {
            String namePrefix = StringUtils.substringBefore(
                  m_pkgNameTextField.getText(), ".");
            String solutionName = StringUtils.substringAfter(
                  m_pkgNameTextField.getText(), ".");
            // Check for pkgName format
            if(StringUtils.isBlank(namePrefix) || StringUtils.isBlank(solutionName))
            {
               errorList.add(getResourceString("error.pkgNameBadFormat"));
            }
            // Check Package Name Unique
            Iterator<PSPackageDependency> packages = model
                  .getExistingPackages();
            while (packages.hasNext())
            {
               String name = packages.next().getPackageName();
               if (name.equalsIgnoreCase(m_pkgNameTextField.getText()))
               {
                  errorList.add(getResourceString("error.duplicatePkgName"));
                  break;
               }
            }
         }

      }
     
      // Check Package Publisher name
      if(StringUtils.isBlank(m_pubNameTextField.getText()))
      {
         errorList.add(getResourceString("error.mustEnterVendor"));
      }
      
      //Check Package Version
      if(StringUtils.isBlank(m_versionTextField.getText()))
      {
         errorList.add(getResourceString("error.mustEnterVersion"));
      }
      String fieldName = null;
      try
      {
         fieldName = 
            StringUtils.chomp(getResourceString("label.version"), ":");
         PSDescriptor.formatVersion(m_versionTextField.getText(), true, true);
      }
      catch(RuntimeException e)
      {
         String rawMsg = getResourceString("error.invalidVersionFormat");
         errorList.add(MessageFormat.format(rawMsg, fieldName));        
      }
      
      //Check CMS Min Version
      fieldName = null;
      if(StringUtils.isBlank(m_cmsMinTextField.getText()))
      {
         errorList.add(getResourceString("error.mustEnterCmsMinVersion"));
      }
      else
      {
         String formatedVersion = null;
         try
         {
            fieldName = 
               StringUtils.chomp(getResourceString("label.cmsMin"), ":");
            formatedVersion = 
               PSDescriptor.formatVersion(m_cmsMinTextField.getText(), true, true);
            
            //Check Package CMS Min Version
            if(isHigherVersion(formatedVersion))
            {
               errorList.add(getResourceString("error.CmsMinVersionTooLarge"));
            }
         }
         catch(RuntimeException e)
         {
            String rawMsg = getResourceString("error.invalidCmsVersionFormat");
            errorList.add(MessageFormat.format(rawMsg, fieldName));        
         }
      }
      
      return errorList;
   }
   
   public void changedUpdate(DocumentEvent e)
   {
      dirtyModel();
   }

   public void insertUpdate(DocumentEvent e)
   {
      dirtyModel();
   }

   public void removeUpdate(DocumentEvent e)
   {
      dirtyModel();
   }
   
   /**
    * Set model to a dirty state.
    */
   private void dirtyModel()
   {
      PSPackagerClientModel model = 
         PSPackagerClient.getFrame().getModel();
      model.setAsDirty();
   }
   
   /**
    * Get Server Version
    */
   private String getServerVersion()
   {
    PSServerConnectionManager cm = PSServerConnectionManager.getInstance();
    PSDeploymentServerConnection con = cm.getConnection();
    if( con != null && con.isConnected())
    {
       String serverString = con.getServerVersion();
       String[] serverVersion = serverString.split(" ");
       String version = 
          PSDescriptor.formatVersion(serverVersion[1], true, true);
       
       return version;
    }
      return "Not Connected";
   }
   
   /**
    * Turn on Listener
    */
   private void turnOnListener(boolean on)
   {
      
      if(on)
      {
         m_pkgNameTextField.getDocument().addDocumentListener(this);
         m_descTextArea.getDocument().addDocumentListener(this);
         m_versionTextField.getDocument().addDocumentListener(this);
         m_cmsMinTextField.getDocument().addDocumentListener(this);
         m_cmsMaxTextField.getDocument().addDocumentListener(this);
         m_pubNameTextField.getDocument().addDocumentListener(this);
         m_pubUrlTextField.getDocument().addDocumentListener(this);
      }
      else
      {
         m_pkgNameTextField.getDocument().removeDocumentListener(this);
         m_descTextArea.getDocument().removeDocumentListener(this);
         m_versionTextField.getDocument().removeDocumentListener(this);
         m_cmsMinTextField.getDocument().removeDocumentListener(this);
         m_cmsMaxTextField.getDocument().removeDocumentListener(this);
         m_pubNameTextField.getDocument().removeDocumentListener(this);
         m_pubUrlTextField.getDocument().removeDocumentListener(this);
      }

   }
   
   /**
    * Checks version 1 is a higher version current server version.  
    * Allowed formats: n.n.n
    * 
    * @param s1 version 1 to compare, assumed not <code>null</code>.
    * 
    * @return <code>true</code> if version 1 is higher than server version,
    * <code>false</code> otherwise.
    */
   private boolean isHigherVersion(String s1)
   {
      String s2 = getServerVersion();
      String delims = "[.]";
      String[] sv1 = s1.split(delims);
      String[] sv2 = s2.split(delims);
      int sv1int0 = Integer.parseInt(sv1[0]);
      int sv2int0 = Integer.parseInt(sv2[0]);
      int sv1int1 = Integer.parseInt(sv1[1]);
      int sv2int1 = Integer.parseInt(sv2[1]);
            
      if(sv1int0 > sv2int0)
      {
         return true;
      }
      else if (sv1int0 == sv2int0 && sv1int1 > sv2int1)
      {
         return true;
      }
      else if (sv1int0 == sv2int0 && sv1int1 == sv2int1 &&
            Integer.parseInt(sv1[2]) > Integer.parseInt(sv2[2]))
      {
         return true;
      }

      return false;
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
   
   /*
    * Package Name Field
    */
   private JTextField m_pkgNameTextField;
   
   /*
    * Package Description
    */
   private JTextArea m_descTextArea= new JTextArea();

   /*
    * Version Field
    */
   private JTextField m_versionTextField;

   /*
    * CMS Minimum Version Field
    */
   private JTextField m_cmsMinTextField;

   /*
    * CMS Maximum Version Field
    */
   private JTextField m_cmsMaxTextField;

   /*
    * Publisher Name
    */
   private JTextField m_pubNameTextField;

   /*
    * Publisher URL
    */
   private JTextField m_pubUrlTextField;
 
}
