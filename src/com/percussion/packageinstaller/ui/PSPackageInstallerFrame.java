/******************************************************************************
 *
 * [ PSPackageInstallerFrame.java ]
 * 
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.packageinstaller.ui;

import com.percussion.deployer.client.IPSDeployJobControl;
import com.percussion.deployer.client.PSDeploymentManager;
import com.percussion.deployer.error.IPSDeploymentErrors;
import com.percussion.deployer.error.PSDeployException;
import com.percussion.deployer.objectstore.PSArchive;
import com.percussion.deployer.objectstore.PSArchiveDetail;
import com.percussion.deployer.objectstore.PSArchiveInfo;
import com.percussion.deployer.objectstore.PSDeployableElement;
import com.percussion.deployer.objectstore.PSExportDescriptor;
import com.percussion.deployer.objectstore.PSImportDescriptor;
import com.percussion.deployer.objectstore.PSImportPackage;
import com.percussion.deployer.objectstore.PSValidationResult;
import com.percussion.deployer.objectstore.PSValidationResults;
import com.percussion.packageinstaller.ui.managers.PSInstallerServerConnectionManager;
import com.percussion.packager.ui.PSResourceUtils;
import com.percussion.packager.ui.PSUiUtils;
import com.percussion.packager.ui.data.PSServerRegistration;
import com.percussion.packagerhelp.PSEclHelpManager;
import com.percussion.utils.collections.PSMultiValueHashMap;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author erikserating
 *
 */
public class PSPackageInstallerFrame extends JFrame implements ActionListener
{
   public PSPackageInstallerFrame()
   { 
         init();
   }
   
   private void init()
   {
      setTitle("Percussion Package Installer");
      setResizable(false);
      URL imageFile = getClass().getResource(getResourceString("image.icon16"));
      if (imageFile != null)
      {
         ImageIcon icon = new ImageIcon(imageFile);
         setIconImage(icon.getImage());
      }    
      
      JPanel mainPanel = createMainPanel();
      
      getContentPane().add(mainPanel);
      displayView(Views.SELECTION);      
      setSize(600, 680);
      PSUiUtils.center(this);
   }
   
   private JPanel createMainPanel()
   {
      MigLayout layout = new MigLayout
         ("wrap 1, fillx, ins 0 0 5 0",
          "[]",
          "[grow, 100:100:100][grow, fill][grow, 30:30:30]");
      JPanel panel = new JPanel();
      
      panel.setLayout(layout);
      
      panel.add(createOverviewPanel(), "grow");
      m_viewPanel = new JPanel();
      
      m_cardLayout = new CardLayout();
      m_viewPanel.setLayout(m_cardLayout);
      panel.add(m_viewPanel, "grow");
      
      // Add views
      m_selectionView = new PSSelectionView(this);
      m_viewPanel.add(m_selectionView, Views.SELECTION.toString());
      
      m_errorWarningView = new PSInfoView(this);
      m_viewPanel.add(m_errorWarningView, Views.ERROR_WARNING.toString());
      
      m_progressView = new PSProgressView(this);
      m_viewPanel.add(m_progressView, Views.PROGRESS.toString());
      
      m_completeView = new PSInfoView(this);
      m_viewPanel.add(m_completeView, Views.COMPLETE.toString());
      
//      panel.add(new JSeparator(), "growx");
      
      m_cmdPanel = createCommandPanel();
      
      panel.add(m_cmdPanel, "grow");      
      
      return panel;
   }
   
   private JPanel createOverviewPanel()
   {
      MigLayout layout = new MigLayout(
         "fill",
         "[grow][]",
         "[top][]");
      JPanel panel = new JPanel();
      panel.setLayout(layout);      
      panel.setOpaque(true);
      
      URL logoImage = getClass().getResource(getResourceString("image.title"));
      if (logoImage != null)
      {
         ImageIcon logo = new ImageIcon(logoImage);
         JLabel logoLabel = new JLabel(logo);
         logoLabel.setHorizontalAlignment(SwingConstants.LEFT);
         panel.add(logoLabel, "growx, left, split");
      }
      
      URL imageFile = getClass().getResource(getResourceString("image.icon"));
      if (imageFile != null)
      {
         ImageIcon icon = new ImageIcon(imageFile);
         m_overviewIconLabel = new JLabel(icon);
         panel.add(m_overviewIconLabel, "wrap 1");
      }
      
      m_overviewLabel = new JLabel("");
      panel.add(m_overviewLabel,"split, growx");
      
      return panel;
   }
   
   private JPanel createCommandPanel()
   {
      MigLayout layout = new MigLayout(
         "hidemode 3, right",
         "[][][][]",
         "[]");
      JPanel panel = new JPanel();
      panel.setLayout(layout);
      panel.setBackground(Color.GRAY);
      
      m_backButton = new JButton(getResourceString("button.back"));
      changeButtonText(m_backButton, "button.back");
      m_backButton.addActionListener(this);
      panel.add(m_backButton, "sg 1");
      
      m_okButton = new JButton(getResourceString("button.install"));
      changeButtonText(m_okButton, "button.install");
      m_okButton.addActionListener(this);
      panel.add(m_okButton, "sg 1");
      
      m_cancelButton = new JButton(getResourceString("button.cancel"));
      changeButtonText(m_cancelButton, "button.cancel");
      m_cancelButton.addActionListener(this);
      panel.add(m_cancelButton, "sg 1");
      
      m_helpButton = new JButton(getResourceString("button.help"));
      changeButtonText(m_helpButton, "button.help");
      m_helpButton.addActionListener(this);
      panel.add(m_helpButton, "sg 1");
      
      return panel;
   }
   
   private void displayView(Views view)
   {
      
      if(view == Views.SELECTION)
      {
         m_okButton.setVisible(true);
         changeButtonText(m_okButton, "button.install");         
         m_backButton.setVisible(false);
         changeButtonText(m_cancelButton, "button.exit");
         m_overviewLabel.setText("<html><b>" + 
               getResourceString("disView.selection") + "</b></html>");
      }
      else if(view == Views.ERROR_WARNING)
      {
         m_okButton.setVisible(true);         
         changeButtonText(m_okButton, "button.continue");
         m_backButton.setVisible(true);
         changeButtonText(m_backButton, "button.back");
         changeButtonText(m_cancelButton, "button.cancel");
         m_overviewLabel.setText("<html><b>" + 
               getResourceString("disView.error") + "</b></html>");
      }
      else if(view == Views.PROGRESS)
      {
         m_okButton.setVisible(false);
         m_backButton.setVisible(false);
         changeButtonText(m_cancelButton, "button.cancel");
         m_overviewLabel.setText("<html><b>"+ 
               getResourceString("disView.progress") + "</b></html>");
      }
      else if(view == Views.COMPLETE)
      {
         m_okButton.setVisible(false);
         m_backButton.setVisible(true);
         changeButtonText(m_backButton, "button.install.another");
         changeButtonText(m_cancelButton, "button.exit");
         m_overviewLabel.setText("<html><b>" + 
               getResourceString("disView.complete") + "</b></html>");
      }
      m_cardLayout.show(m_viewPanel, view.toString());
      m_currentView = view;
      handleButtonState();
   }
   
   /**
    * Invoked by the selection view when a new package file
    * path has been selected.
    */
   void onPackageFileSelected()
   {
      // Attempt to load the specified package to get info and
      // determine if it is valid archive.
      m_packageFile = m_selectionView.getPackageFile();
      try
      {
         if(m_packageFile.exists())
         {
            JTextPane textPane = m_selectionView.getDecriptionTextPane();
            StyledDocument doc = (StyledDocument)textPane.getDocument();
            doc.remove(0, doc.getLength());
            
            Style bold = doc.addStyle("BOLD", null);
            Style normal = doc.addStyle("NORMAL", null);
            Style error = doc.addStyle("ERROR", null);
            StyleConstants.setBold(bold, true);            
            StyleConstants.setBold(normal, false);
            StyleConstants.setBold(error, true);
            StyleConstants.setForeground(error, Color.red);
            
            try
            {
               PSArchive archive = new PSArchive(m_packageFile);
               m_archiveInfo = archive.getArchiveInfo(true);
               m_archiveDetail = m_archiveInfo.getArchiveDetail();
               m_exportDescriptor = m_archiveDetail.getExportDescriptor();            
               
               archive.close();
               
               doc.insertString(doc.getLength(),
                  "Package Name:  ", bold);
               doc.insertString(doc.getLength(),
                  m_exportDescriptor.getName() + "\n", normal);
               
               doc.insertString(doc.getLength(),
                  "Version:  ", bold);
               doc.insertString(doc.getLength(),
                  m_exportDescriptor.getVersion() + "\n", normal);
                     
               doc.insertString(doc.getLength(),
                  "Publisher:  ", bold);
               doc.insertString(doc.getLength(),
                  m_exportDescriptor.getPublisherName() + "\n", normal);      
               
               doc.insertString(doc.getLength(),
                  "Description:\n", bold);
               doc.insertString(doc.getLength(),
                  m_exportDescriptor.getDescription() , normal);
                          
               
               m_currentPkgIsValid = true;
            }
            catch (PSDeployException e)
            {
               doc.insertString(doc.getLength(), 
                     getResourceString("error.invalidpkg"),
                  error);
               m_currentPkgIsValid = false;
            }
            handleButtonState();
         }
      }
      catch (BadLocationException ignore)
      {
         
      }
   }
   
   void handleButtonState()
   {
      if(m_currentView == Views.SELECTION)
      {
         boolean enable = m_currentPkgIsValid && 
            m_selectionView.getServerRegistration() != null;
         
         m_okButton.setEnabled(enable);
         
      }
      else if(m_currentView == Views.ERROR_WARNING)
      {
         m_okButton.setVisible(!m_hasErrors);
         if(m_hasErrors)
         {
            changeButtonText(m_cancelButton, "button.exit");
         }
      }
   }
   
   /* (non-Javadoc)
    * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
    */
   public void actionPerformed(ActionEvent event)
   {
      Object source = event.getSource();
      if(source == m_backButton)
      {
         if(m_currentView == Views.ERROR_WARNING)
         {
            m_cancelRequested = true;
         }
         displayView(Views.SELECTION);
      }
      else if(source == m_cancelButton)
      {
         if(m_currentView == Views.PROGRESS)
         {
            m_cancelRequested = true;
            if(m_job != null)
            {
               try
               {
                  m_job.cancelDeployJob();
                  m_completeView.setText(getResourceString("error.canInstall"));
                  m_overviewLabel.setText("<html><b>" +
                     getResourceString("disView.complete.cancelled") + "</b></html>");
                  displayView(Views.COMPLETE);
               }
               catch (PSDeployException e)
               {
                  handleCompleted(false, e);
               } 
            }
               
         }
         else if(m_currentView == Views.ERROR_WARNING && !m_hasErrors)
         {
            m_cancelRequested = true;
            m_completeView.setText(getResourceString("error.canInstall"));
            m_overviewLabel.setText("<html><b>" +
               getResourceString("disView.complete.cancelled") + "</b></html>");
            displayView(Views.COMPLETE);
         }
         else
         {
            exitAction();
         }
      }
      else if(source == m_helpButton)
      {
         onHelp();
      }
      else if(source == m_okButton)
      {
         if(m_currentView == Views.SELECTION)
         {
            doInstall();
         }
         if(m_currentView == Views.ERROR_WARNING)
         {
            m_suspend = false;
         }
      }
      
   }
   
   private void doInstall()
   {
      final PSInstallerServerConnectionManager connMgr = 
         PSInstallerServerConnectionManager.getInstance();
      
      PSServerRegistration server = m_selectionView.getServerRegistration();
      Runnable runIt = null;
      m_cancelRequested = false;
      m_suspend = false;
      
      // Attempt to get connection
      try
      {
         setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
         

         try
         {
            connMgr.initConnection(server);
         }
         catch (Exception e)
         {
            if(e.getMessage().startsWith("An unexpected error has occurred:"))
            {
               throw new PSDeployException(
                  IPSDeploymentErrors.UNABLE_TO_CONNECT_TO_SERVER);
            }
            else
            {
               throw e;
            }
         }         
         
         connMgr.saveServerRegistration(server);
         connMgr.deleteAllNonRecentConnections();
         
         m_importDescriptor = createImportDescriptor();
         setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        
            
            
            runIt = new Runnable()
            {
               public void run()
               {
                  try
                  {
                     setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                     final PSDeploymentManager dm = 
                        PSInstallerServerConnectionManager.getInstance().
                           getDeploymentManager();
                     // Step 1 -- Validation
                     SwingUtilities.invokeAndWait(new Runnable()
                     {
                        public void run()
                        {
                           m_progressView.setMessage(getResourceString("progView.message"));
                           m_progressView.setNote(getResourceString("progView.note.valid"));
                           displayView(Views.PROGRESS);
                        }                        
                     });
                     
                     //Do package level validation first
                     m_hasErrors = false;
                     boolean hasWarnings = false;
                     PSMultiValueHashMap<String, String> pkgValResults = 
                        dm.validateArchive(m_archiveInfo, false);
                     final List<String> pkgErrors = pkgValResults.get("Error");
                     final List<String> pkgWarns = pkgValResults.get("Warning");
                     m_hasErrors = !pkgErrors.isEmpty();
                     hasWarnings = !pkgWarns.isEmpty();
                     
                     // If errors or warnings show them and wait
                     if(m_hasErrors || hasWarnings)
                     {                        
                        SwingUtilities.invokeAndWait(new Runnable()
                        {
                           public void run()
                           {
                              showErrorAndWarnings(pkgErrors, pkgWarns);                              
                           }                        
                        });
                        m_suspend = true;
                        while(m_suspend && !m_cancelRequested)
                        {
                           Thread.sleep(250);
                        }
                        if(m_cancelRequested)
                           return;
                     }
                     m_job = dm.runValidationJob(m_importDescriptor);
                     while(m_job.getStatus() < 100 && m_job.getStatus() != -1
                        && !m_cancelRequested)
                     {
                        Thread.sleep(250);
                        SwingUtilities.invokeAndWait(new Runnable()
                        {
                           public void run()
                           {                              
                              try
                              {
                                 m_progressView.setNote(m_job.getStatusMessage());
                              }
                              catch (PSDeployException ignore){}
                              
                           }                        
                        });
                     }
                     if(m_cancelRequested)
                        return;
                     
                     if(m_job.getStatus() == -1)
                     {
                        SwingUtilities.invokeAndWait(new Runnable()
                        {
                           public void run()
                           {                              
                              handleCompleted(false, null);                              
                           }                        
                        });
                        return;
                     }
                     
                     // Step 2 -- Process Validation Results
                     SwingUtilities.invokeAndWait(new Runnable()
                     {
                        public void run()
                        {
                           m_progressView.setMessage(getResourceString("progView.message"));
                           m_progressView.setNote(getResourceString("progView.note.process"));
                           displayView(Views.PROGRESS);
                        }                        
                     });
                     dm.loadValidationResults(m_importDescriptor);
                     m_hasErrors = false;
                     hasWarnings = false;
                     final PSMultiValueHashMap<String, String> errors = 
                        new PSMultiValueHashMap<String, String>();
                     final PSMultiValueHashMap<String, String> warns = 
                        new PSMultiValueHashMap<String, String>();
                     for(PSImportPackage pkg : m_importDescriptor.getImportPackageList())
                     {
                        PSValidationResults results = pkg.getValidationResults();
                        if(results != null)
                        {
                           Iterator<PSValidationResult> entries = results.getResults();
                           while(entries.hasNext())
                           {
                              PSValidationResult entry = entries.next();
                              String name = entry.getDependency().getDisplayName();
                              String type = entry.getDependency().getObjectType();
                              String oType = type.equals("sys_UserDependency") ? 
                                 getResourceString("type.file.resource") :
                                 entry.getDependency().getObjectTypeName();
                              String key = oType + " -- " + name;
                              if(entry.isError())
                              {
                                 m_hasErrors = true;
                                 if(!errors.containsValue(key, entry.getMessage()))
                                    errors.put(key, entry.getMessage());
                              }
                              else
                              {
                                 hasWarnings = true;
                                 if(!warns.containsValue(key, entry.getMessage()))
                                    warns.put(key, entry.getMessage());
                              }                              
                           }
                        }
                     }
                     // If errors or warnings show them and wait
                     if(m_hasErrors || hasWarnings)
                     {                        
                        SwingUtilities.invokeAndWait(new Runnable()
                        {
                           public void run()
                           {
                              showErrorAndWarnings(errors, warns);                              
                           }                        
                        });
                        m_suspend = true;
                        while(m_suspend && !m_cancelRequested)
                        {
                           Thread.sleep(250);
                        }
                        if(m_cancelRequested)
                           return;
                     }
                     // Step 3 -- Copy Package to Server
                     SwingUtilities.invokeAndWait(new Runnable()
                     {
                        public void run()
                        {
                           m_progressView.setMessage(getResourceString("progView.message"));
                           m_progressView.setNote(getResourceString("progView.note.copy"));
                           displayView(Views.PROGRESS);
                        }                        
                     });
                     m_job = dm.copyArchiveToServer(m_exportDescriptor.getName(), 
                        m_packageFile);
                     while(m_job.getStatus() < 100 && m_job.getStatus() != -1 
                        && !m_cancelRequested)
                     {
                        Thread.sleep(250);
                     }
                     if(m_cancelRequested)
                        return;
                     
                     if(m_job.getStatus() == -1)
                     {
                        SwingUtilities.invokeAndWait(new Runnable()
                        {
                           public void run()
                           {                              
                              handleCompleted(false, null);                              
                           }                        
                        });
                        return;
                     }
                     
                     // Step 4 -- Install Package on Server
                     SwingUtilities.invokeAndWait(new Runnable()
                     {
                        public void run()
                        {
                           m_progressView.setMessage(getResourceString("progView.message"));
                           m_progressView.setNote(getResourceString("progView.note.install"));
                           displayView(Views.PROGRESS);
                           
                        }                        
                     });
                     m_job = dm.runImportJob(m_importDescriptor);
                     while(m_job.getStatus() < 100 && m_job.getStatus() != -1 
                        && !m_cancelRequested)
                     {
                        Thread.sleep(250);
                        SwingUtilities.invokeAndWait(new Runnable()
                        {
                           public void run()
                           {                              
                              try
                              {
                                 m_progressView.setNote(m_job.getStatusMessage());
                              }
                              catch (PSDeployException ignore){}
                              
                           }                        
                        });
                     }
                     if(m_cancelRequested)
                        return;
                     
                     if(m_job.getStatus() == -1)
                     {
                        SwingUtilities.invokeAndWait(new Runnable()
                        {
                           public void run()
                           {                              
                              handleCompleted(false, null);                              
                           }                        
                        });
                        return;
                     }
                     SwingUtilities.invokeAndWait(new Runnable()
                     {
                        public void run()
                        {
                           handleCompleted(true, null);
                        }                        
                     });
                  }
                  catch (final Exception e)
                  {
                     try
                     {
                        SwingUtilities.invokeAndWait(new Runnable()
                        {
                           public void run()
                           {                              
                              handleCompleted(false, e);                              
                           }                        
                        });
                     }
                     catch (Exception ignore){}
                  }
                  finally
                  {
                     setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                  }
                  
                  
               }               
            };
            m_installThread = new Thread(runIt);
            m_installThread.start();         
         
      }      
      catch (Exception e)
      {
        PSPackageInstallerClient.getErrorDialog().showError(
           e, true, PSResourceUtils.getCommonResourceString("errorTitle"));
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      }
      
   }
   
   /**
    * Invoked when help button is clicked.
    */
   private void onHelp()
   {
      PSEclHelpManager.launchHelp(this.getClass().getName());
   }
   
   /**
    * Helper method to change the text for a button's
    * label, tooltip and mnemonic.
    * @param button assumed not <code>null</code>.
    * @param key assumed not <code>null</code> or empty.
    */
   private void changeButtonText(JButton button, String key)
   {
      String name = getResourceString(key);
      String mnemonic = getResourceString(key + ".m");
      String tooltip = getResourceString(key + ".tt"); 
      button.setText(name);
      button.setToolTipText(tooltip);
      if(mnemonic.length() == 1)
         button.setMnemonic(mnemonic.charAt(0));        
   }
   
   /**
    * Handle completion of install by displaying the appropriate message
    * or error.
    * 
    * @param completedNormally
    * @param e
    */
   private void handleCompleted(boolean completedNormally, Exception e)
   {
      StringBuilder buff = new StringBuilder();
      PSInstallerServerConnectionManager connMgr = 
         PSInstallerServerConnectionManager.getInstance();
      if(completedNormally)
      {
         buff.append("<span style=\"color: green; font-family: Arial;");
         buff.append("font-size: 9px;\"><b>");
         buff.append(getResourceString("message.success.m1"));
         buff.append(" &quot;");
         buff.append(m_exportDescriptor.getName());
         buff.append("(");
         buff.append(m_exportDescriptor.getVersion());
         buff.append(")");
         buff.append("&quot; ");
         buff.append(getResourceString("message.success.m1"));
         buff.append(" ");
         buff.append(connMgr.getCurrentServerInfo().toString());
         buff.append("</b></span>");
      }
      else
      {
         buff.append("<span style=\"color: red; font-family: Arial;");
         buff.append("font-size: 9px;\"><b>");
         buff.append(getResourceString("message.error"));
         buff.append(" &quot;");
         buff.append(m_exportDescriptor.getName());
         buff.append("(");
         buff.append(m_exportDescriptor.getVersion());
         buff.append(")");
         buff.append("&quot;</b></span><hr>");
         buff.append("<span style=\"color: blue; font-family: Arial;");
         buff.append("font-size: 9px;\">");
         if (e != null)
         {
            buff.append(e.getLocalizedMessage());
         }
         else
         {
            try
            {
               buff.append(m_job.getStatusMessage());
            }
            catch (PSDeployException ignore)
            {
            }
         }
         buff.append("</span>");
      }
      m_completeView.setText(buff.toString());
      displayView(Views.COMPLETE);
   }
   
   /**
    * Helper method to decorate and show error and warning messages for each
    * type-name.
    * @param errors assumed not <code>null</code>.
    * @param warns assumed not <code>null</code>.
    */
   private void showErrorAndWarnings(List<String> errors,
      List<String> warns)
   {
      StringBuilder buff = new StringBuilder();
      if(!errors.isEmpty())
      {
         String pre = StringUtils.replace(HTMLHEADER, "@TYPE@", "Errors");
         pre = StringUtils.replace(pre, "@COLOR@", "red");
         buff.append(pre);
         buff.append("<br>");
         int errorCount = errors.size();
         if(errorCount > 1)
            buff.append("<ul>\n");         
         for(String msg : errors)
         {
            msg = StringUtils.replace(msg, "\n", "<br>");
            if(errorCount > 1)
               buff.append("<li>");
            buff.append(msg);
            if(errorCount > 1)
               buff.append("</li>");
            buff.append("\n");
         }
         if(errorCount > 1)
            buff.append("</ul>\n");
      }
      if(!warns.isEmpty())
      {
         String pre = StringUtils.replace(HTMLHEADER, "@TYPE@", "Warnings");
         pre = StringUtils.replace(pre, "@COLOR@", "blue");
         buff.append(pre);
         buff.append("<br>");
         int warnCount = warns.size();
         if(warnCount > 1)
            buff.append("<ul>\n");
         for(String msg : warns)
         {
            msg = StringUtils.replace(msg, "\n", "<br>");
            if(warnCount > 1) 
               buff.append("<li>");
            buff.append(msg);
            if(warnCount > 1)
               buff.append("</li>");
            buff.append("\n");
         }
         if(warnCount > 1)
            buff.append("</ul>\n");
      }
      m_errorWarningView.setText(buff.toString());
      displayView(Views.ERROR_WARNING);
      setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      handleButtonState();
   }
   
   /**
    * Helper method to decorate and show error and warning messages for each
    * type-name.
    * @param errors assumed not <code>null</code>.
    * @param warns assumed not <code>null</code>.
    */
   private void showErrorAndWarnings(PSMultiValueHashMap<String, String> errors,
      PSMultiValueHashMap<String, String> warns)
   {
      Set<String> eTypes = new TreeSet<String>(errors.keySet());
      Set<String> wTypes = new TreeSet<String>(warns.keySet());
      StringBuilder buff = new StringBuilder();
      if(!eTypes.isEmpty())
      {
         String pre = StringUtils.replace(HTMLTABLESTART, "@TYPE@", "Errors");
         pre = StringUtils.replace(pre, "@COLOR@", "red");
         buff.append(pre);
         for(String type : eTypes)
         {
            buff.append("<tr>\n");
            buff.append(HTMLCOLUMNSTART);
            buff.append(type);
            buff.append("</td>\n");
            buff.append(HTMLCOLUMNSTART);
            List<String> errs = errors.get(type);
            int errorCount = errs.size();
            if(errorCount > 1)
               buff.append("<ul>\n");
            for(String msg : errs)
            {
               msg = StringUtils.replace(msg, "\n", "<br>");
               if(errorCount > 1)
                  buff.append("<li>");
               buff.append(msg);
               if(errorCount > 1)
                  buff.append("</li>");
               buff.append("\n");
            }
            if(errorCount > 1)
               buff.append("</ul>");
            buff.append("</td></tr>\n");
         }
         buff.append("</table><br>");
      }
      if(!wTypes.isEmpty())
      {
         String pre = StringUtils.replace(HTMLTABLESTART, "@TYPE@", "Warnings");
         pre = StringUtils.replace(pre, "@COLOR@", "blue");
         buff.append(pre);
         for(String type : wTypes)
         {
            buff.append("<tr>\n");
            buff.append(HTMLCOLUMNSTART);
            buff.append(type);
            buff.append("</td>\n");
            buff.append(HTMLCOLUMNSTART);
            List<String> wrs = warns.get(type);
            int warnCount = wrs.size();
            if(warnCount > 1)
               buff.append("<ul>\n");
            for(String msg : warns.get(type))
            {
               msg = StringUtils.replace(msg, "\n", "<br>");
               if(warnCount > 1)
                  buff.append("<li>");
               buff.append(msg);
               if(warnCount > 1)
                  buff.append("</li>");
               buff.append("\n");
            }
            if(warnCount > 1)
               buff.append("</ul>");
            buff.append("</td></tr>\n");
         }
         buff.append("</table>");
      }
      m_errorWarningView.setText(buff.toString());
      displayView(Views.ERROR_WARNING);
      setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      handleButtonState();
   }
   
   /**
    * Create the import descriptor from the archive.
    * @return import descriptor, never <code>null</code>.
    */
   @SuppressWarnings("unchecked")
   private PSImportDescriptor createImportDescriptor()
   {
      
      PSImportDescriptor desc = new PSImportDescriptor(m_archiveInfo);
      m_archiveInfo.setArchiveRef(m_exportDescriptor.getName());
      List lst = desc.getImportPackageList();
      lst.clear();
      Iterator itr = m_archiveDetail.getPackages();
      while(itr.hasNext())
      {
         Object obj = itr.next();
         if (obj instanceof PSDeployableElement)
         {
            lst.add(new PSImportPackage((PSDeployableElement)obj));
         }
      }
      return desc;
   
   }   
   
   /**
    * Processes the window close event as exit action.
    * 
    * @param e the window event, assumed not to be <code>null</code> as this 
    * method will be called by <code>Swing</code> model when user clicks on the
    * close button of this frame.
    */
   @Override
   protected void processWindowEvent( WindowEvent e )
   {      
      if (e.getID( ) == WindowEvent.WINDOW_CLOSING)
      {
         exitAction();       
      }
      super.processWindowEvent( e );
   }
   
   private void exitAction()
   {
      PSInstallerServerConnectionManager connMgr = 
         PSInstallerServerConnectionManager.getInstance();
      try
      {
         connMgr.disconnect();
      }
      catch (PSDeployException ignore){}
      System.exit(0);
   }
   
   /**
    * 
    * @param key
    * @return
    */
   private static String getResourceString(String key)
   {
      return PSResourceUtils.getResourceString(PSPackageInstallerFrame.class, key);
   }
   
   private enum Views
   {
      SELECTION,
      ERROR_WARNING,
      COMPLETE,
      PROGRESS
   }
   
   PSArchiveInfo m_archiveInfo;
   PSArchiveDetail m_archiveDetail;
   PSExportDescriptor m_exportDescriptor;
   PSImportDescriptor m_importDescriptor;
   private File m_packageFile; 
   
   private IPSDeployJobControl m_job;
   private Thread m_installThread;
   private boolean m_hasErrors;
   private boolean m_suspend;
   private boolean m_cancelRequested;
   
   private boolean m_currentPkgIsValid;
   
   private PSSelectionView m_selectionView;
   private PSInfoView m_errorWarningView;
   private PSInfoView m_completeView;
   private PSProgressView m_progressView;
   private Views m_currentView;
   
   private JPanel m_cmdPanel;
   private JPanel m_viewPanel;
   private JLabel m_overviewLabel;
   private JLabel m_overviewIconLabel;
   
   private JButton m_backButton;
   private JButton m_okButton;
   private JButton m_cancelButton;
   private JButton m_helpButton;
   private CardLayout m_cardLayout;
   
   private static final String HTMLCOLUMNSTART = 
      "<td valign=\"top\" style=\"border-style: solid; border-width: 1px;\">";
   
   private static final String HTMLHEADER =
      "<span style=\"font-family: Arial;font-size: 10px;font-weight: bold;color: @COLOR@;\">@TYPE@</span>\r\n";
   
   private static final String HTMLTABLESTART = 
      HTMLHEADER + 
   	"<table style=\"font-size: 9px; font-family: Arial; \" cellspacing=\"0\" cellpadding=\"3\" width=\"100%\">\r\n" + 
   	"<tr><td width=\"40%\" style=\"border-style: solid; border-width: 1px;\">" +
   	"<b>" + getResourceString("colHead.name") + "</b></td><td width=\"60%\" "+
   	"style=\"border-style: solid; border-width: 1px;\"><b>" + 
   	getResourceString("colHead.message") + "</b></td></tr>\r\n"; 
   		
   
   
}
