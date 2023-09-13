/******************************************************************************
 *
 * [ PSArchiveViewHandler.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.deployer.ui;

import com.percussion.deployer.objectstore.PSArchiveInfo;
import com.percussion.deployer.objectstore.PSArchiveManifest;
import com.percussion.deployer.objectstore.PSArchiveSummary;
import com.percussion.deployer.objectstore.PSExportDescriptor;
import com.percussion.design.objectstore.PSFeature;
import com.percussion.design.objectstore.PSFeatureSet;
import com.percussion.error.PSDeployException;
import com.percussion.guitools.ErrorDialogs;
import com.percussion.guitools.PSResources;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Iterator;

/**
 * The view handler that handles the 'Archives' view.
 */
public class PSArchiveViewHandler extends PSTableViewHandler
{

   /**
    * Overridden to update the table model to display the archive logs in the
    * table. See <code>super.setData(Object)</code> for more description of
    * the method and its parameters.
    */
   public void setData(Object object) throws PSDeployException
   {
      super.setData(object);
      m_server = (PSDeploymentServer)m_object;
      setTableModel( m_server.getArchives(false) );
      
      // see if we can allow creates
      m_allowCreate = false;
      PSFeatureSet featureSet = m_server.getFeatureSet();
      Iterator i = featureSet.getFeatureSet();
      while (i.hasNext() && !m_allowCreate)
      {
         PSFeature feature = (PSFeature)i.next();
         if (feature.getName().equalsIgnoreCase("CreateFromArchive"))
            m_allowCreate = true;
      }
   }


   /**
    * Creates the pop-up menu items and corresponding actions that need to be
    * shown in this view.
    */
   protected void createPopupMenu()
   {
      PSResources res = PSDeploymentClient.getResources();

      Action viewAction = new AbstractAction(res.getString("viewLog"))
      {
         public void actionPerformed(ActionEvent e)
         {
            showDetailView(m_curSelectedRow);
         }
      };
      m_createMenuItem = m_popupMenu.add(viewAction);
      
      Action createAction = new AbstractAction(res.getString("createArchive"))
      {
         public void actionPerformed(ActionEvent e)
         {
            onCreate();
         }
      };
      m_createMenuItem = m_popupMenu.add(createAction);
      
      m_popupMenu.add(new AbstractAction(res.getString("delete"))
      {
         public void actionPerformed(ActionEvent e)
         {
            onDelete();
         }
      });

      m_popupMenu.add(new AbstractAction(res.getString("exportManifest"))
      {
         public void actionPerformed(ActionEvent e)
         {
            onExportManifest();
         }
      });

      m_popupMenu.add(new AbstractAction(res.getString("redeploy"))
      {
         public void actionPerformed(ActionEvent e)
         {
            onRedeploy();
         }
      });
   }

   /**
    * Disables the "Create Archive" menu item if server does not support it.
    */
   protected void updatePopupMenu()
   {
      if(m_curSelectedRow == -1)
         throw new IllegalStateException(
            "the current model row is not set");

      if (m_createMenuItem == null) 
         throw new IllegalStateException("createPopupMenu has not been called");
      
      // enable/disable allow create
      m_createMenuItem.setEnabled(m_allowCreate);
   }

   /**
    * Sends a request to the server to delete the archive referred to by the
    * archive log for which the action is taking place and informs the data
    * object of this view to recatalog the archive logs (many archive logs may
    * refer to single archive) if the request is successful. Displays error
    * dialog with appropriate message if exceptions happen in the process.
    *
    * @throws IllegalStateException if there is no row set for which this menu
    * action need to be shown.
    */
   private void onDelete()
   {
      if(m_curSelectedRow == -1)
         throw new IllegalStateException(
            "the current model row is not set");

      try {
      
         int option = JOptionPane.showConfirmDialog(
            PSDeploymentClient.getFrame(), 
            ErrorDialogs.cropErrorMessage( MessageFormat.format(
            PSDeploymentClient.getResources().getString("deleteArchiveMsg"), 
            new String[] {m_tableModel.getDisplayName(m_curSelectedRow)} ) ),
            PSDeploymentClient.getResources().getString("deleteConfirmTitle"), 
            JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
         
         if(option == JOptionPane.YES_OPTION)
         {
            int id = Integer.parseInt(m_tableModel.getID(m_curSelectedRow));      
            m_server.getDeploymentManager().deleteArchive(id);
            m_server.updateArchives();
            m_server.updatePackages();
         }
      }
      catch(NumberFormatException e)
      {
         PSResources res = PSDeploymentClient.getResources();
         PSDeploymentClient.getErrorDialog().showErrorMessage(
            res.getString("invalidLogID"), res.getString("errorTitle"));
      }
      catch(PSDeployException e)
      {
         PSDeploymentClient.getErrorDialog().showError(e, false,
            PSDeploymentClient.getResources().getString("errorTitle"));
      }
   }

   /**
    * Displays a file browser dialog to save the manifest of an archive referred
    * by the selected archive log and saves the manifest to the selected file
    * in the dialog. Displays error dialog with appropriate message if
    * exceptions happen in the process.
    *
    * @throws IllegalStateException if there is no row set for which this menu
    * action is shown.
    */
   private void onExportManifest()
   {
      if(m_curSelectedRow == -1)
         throw new IllegalStateException(
            "the current model row is not set");

      FileOutputStream out = null;
      try {
         int id = Integer.parseInt(m_tableModel.getID(m_curSelectedRow));
         PSArchiveSummary summary =
            m_server.getDeploymentManager().getArchiveSummary(id);
         PSArchiveManifest manifest = summary.getArchiveManifest();
         if(manifest == null)
         {
            PSDeploymentClient.getErrorDialog().showErrorMessage(
               PSDeploymentClient.getResources().getString("noArchiveManifest"),
               PSDeploymentClient.getResources().getString("errorTitle"));
            return;
         }
         
         File selectedFile = PSDeploymentClient.showFileDialog(
            PSDeploymentClient.getFrame(), 
            new File(summary.getArchiveInfo().getArchiveRef() + ".xml"),
            "xml", "XML Files (*.xml)", JFileChooser.SAVE_DIALOG);
            
         if(selectedFile != null)
         {            
            out = new FileOutputStream(selectedFile);
            Document doc = PSXmlDocumentBuilder.createXmlDocument();
            PSXmlDocumentBuilder.write(manifest.toXml(doc), out);
         }
      }
      catch(NumberFormatException e)
      {
         PSResources res = PSDeploymentClient.getResources();
         PSDeploymentClient.getErrorDialog().showErrorMessage(
            res.getString("invalidLogID"), res.getString("errorTitle"));
      }
      catch(IOException e)
      {
         PSDeploymentClient.getErrorDialog().showError(e, false,
            PSDeploymentClient.getResources().getString("errorTitle"));
      }
      catch(PSDeployException e)
      {
         PSDeploymentClient.getErrorDialog().showError(e, false,
            PSDeploymentClient.getResources().getString("errorTitle"));
      }
      finally
      {
         try {
            if(out != null)
               out.close();
         } catch(IOException ie){}
      }
   }

   /**
    * Gets the archive info from the deployment manager for the archive log id
    * for which this ('redeploy') action should take place. Constructs the
    * import wizard manager with the archive and this view object (this
    * deployment server) and runs the wizard to show the wizard steps starting
    * from the mode selection as archive and target server are already known.
    * Displays error dialog with appropriate message if exceptions happen in
    * the process.
    *
    * @throws IllegalStateException if there is no row set for which this menu
    * action is shown.
    */
   private void onRedeploy()
   {
      if(m_curSelectedRow == -1)
         throw new IllegalStateException(
            "the current model row is not set");
      try 
      {
         int option = JOptionPane.showConfirmDialog(
            PSDeploymentClient.getFrame(), 
            ErrorDialogs.cropErrorMessage(
            PSDeploymentClient.getResources().getString("installWarning")),
            PSDeploymentClient.getResources().getString("warningTitle"),
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            
         if(option == JOptionPane.NO_OPTION)
            return;
            
         int id = Integer.parseInt(m_tableModel.getID(m_curSelectedRow));
         PSArchiveInfo info =
            m_server.getDeploymentManager().getArchiveInfo(id);
         
         /* 
         // validate the archive
         String msg = m_server.getDeploymentManager().validateArchive(
            info, false);
         if (msg != null)
         {
            PSDeploymentClient.getErrorDialog().showErrorMessage(msg, 
               PSDeploymentClient.getResources().getString("validationTitle"));
         }
         */
         PSDeploymentImportExecutionPlan impPlan = 
            new PSDeploymentImportExecutionPlan(PSDeploymentClient.getFrame(), 
               info, m_server);
         PSDeploymentImportWizardManager mgr = 
            new PSDeploymentImportWizardManager(
            PSDeploymentClient.getFrame(), impPlan);
         mgr.runWizard();
      }
      catch(NumberFormatException e)
      {
         PSResources res = PSDeploymentClient.getResources();
         PSDeploymentClient.getErrorDialog().showErrorMessage(
            res.getString("invalidLogID"), res.getString("errorTitle"));
      }
      catch(PSDeployException e)
      {
         PSDeploymentClient.getErrorDialog().showError(e, false, 
            PSDeploymentClient.getResources().getString("errorTitle"));
      }

   }
   /**
    * Gets the export descriptor from the archive specified by the archive log 
    * id for which this ('create') action should take place. Constructs the
    * export wizard manager with the descriptor and this view object (this
    * deployment server) and starts the  manager to display the wizard dialogs 
    * with the selected descriptor. Displays error dialog with appropriate 
    * message if exceptions happen in the process.
    *
    * @throws IllegalStateException if there is no row set for which this menu
    * action is shown.
    */
   private void onCreate()
   {
      if(m_curSelectedRow == -1)
         throw new IllegalStateException(
            "the current model row is not set");   
            
      try 
      {
         int id = Integer.parseInt(m_tableModel.getID(m_curSelectedRow));
         PSDescriptorLocator locator = new PSDescriptorLocator(id);
         PSExportDescriptor descriptor = locator.load(m_server);
         
         PSDeploymentExportExecutionPlan expPlan = 
            new PSDeploymentExportExecutionPlan(PSDeploymentClient.getFrame(), 
            descriptor, locator, m_server);
         PSDeploymentExportWizardManager mgr = 
            new PSDeploymentExportWizardManager(
            PSDeploymentClient.getFrame(), expPlan);
         mgr.runWizard();
      }
      catch(NumberFormatException e)
      {
         PSResources res = PSDeploymentClient.getResources();
         PSDeploymentClient.getErrorDialog().showErrorMessage(
            res.getString("invalidLogID"), res.getString("errorTitle"));
      }
      catch(PSDeployException e)
      {
         PSDeploymentClient.getErrorDialog().showError(e, false, 
            PSDeploymentClient.getResources().getString("errorTitle"));
      }
   }

   //see IPSViewHandler interface
   public boolean supportsDetailView()
   {
      return true;
   }

   //see IPSViewHandler interface
   public void showDetailView(int index)
   {
      try
      {
         int rc = m_tableModel.getRowCount();
         if ( index < 0 || index >= rc )
            throw new IllegalArgumentException("Index supplied is not vaild");
            
         int id = Integer.parseInt(m_tableModel.getID(index));
         PSArchiveSummary arcSum =
            m_server.getDeploymentManager().getArchiveSummary(id);
         PSArchiveSummaryDialog dlg = new PSArchiveSummaryDialog(
            PSDeploymentClient.getFrame(), m_server, arcSum);
         dlg.setVisible(true);
      }
      catch(NumberFormatException e)
      {
         PSResources res = PSDeploymentClient.getResources();
         PSDeploymentClient.getErrorDialog().showErrorMessage(
            res.getString("invalidLogID"), res.getString("errorTitle"));
      }
      catch(PSDeployException e)
      {
         PSDeploymentClient.getErrorDialog().showError(e, false,
            PSDeploymentClient.getResources().getString("errorTitle"));
      }
   }

   /**
    * The data object that supports this view and its pop-up menu actions, same
    * as super class <code>m_object</code>, but properly casted to be  easy to
    * use. <code>null</code> until first call to <code>setData(Object)</code>.
    */
   private PSDeploymentServer m_server = null;
   
   /**
    * Determines if the "Create Archive" option should be allowed in the popup
    * menu.  Evaluated during calls to <code>setData()</code>, used by
    * <code>updatePopupMenu()</code>.
    */
   boolean m_allowCreate = false;
   
   /**
    * The menu item for the "Create Archive" option.  Initialized by a call to 
    * <code>createPopupMenu()</code>.  Enabled/disabled by calls to 
    * <code>updatePopupMenu()</code>.
    */
   private JMenuItem m_createMenuItem;
}
