/******************************************************************************
 *
 * [ PSDescriptorViewHandler.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.deployer.ui;

import com.percussion.deployer.client.IPSDeployConstants;
import com.percussion.deployer.objectstore.PSExportDescriptor;
import com.percussion.error.PSDeployException;
import com.percussion.guitools.ErrorDialogs;
import com.percussion.guitools.PSResources;
import com.percussion.util.PSProperties;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The view handler that handles the 'Descriptors' view.
 */
public class PSDescriptorViewHandler extends PSTableViewHandler
{

   /**
    * Overridden to update the table model to display the descriptors in the
    * table. See <code>super.setData(Object)</code> for more description of
    * the method and its parameters.
    */
   public void setData(Object object) throws PSDeployException
   {
      super.setData(object);
      m_server = (PSDeploymentServer)m_object;
      setTableModel( m_server.getDescriptors(false) );
   }

   /**
    * Creates the pop-up menu and actions need to be shown in this view.
    */
   protected void createPopupMenu()
   {
      Iterator actions = getActions();
      while(actions.hasNext())
      {
         m_popupMenu.add((Action)actions.next());
      }
   }

   //implements super's method, Does not enable or disabe any of menu items
   protected void updatePopupMenu()
   {
      if(m_curSelectedRow == -1)
         throw new IllegalStateException(
            "the current model row is not set");
   }

   /**
    * Creates all the actions for the popup menu items that is shown in this
    * view.
    *
    * @return iterator of actions list, never <code>null</code> or empty.
    */
   private Iterator getActions()
   {
      List m_actions = new ArrayList();

      PSResources res = PSDeploymentClient.getResources();

      m_actions.add(new AbstractAction(res.getString("createArchive"))
      {
         public void actionPerformed(ActionEvent e)
         {
            onCreateArchive();
         }
      });

      m_actions.add(new AbstractAction(res.getString("delete"))
      {
         public void actionPerformed(ActionEvent e)
         {
            onDelete();
         }
      });

      m_actions.add( new AbstractAction(res.getString("deploy"))
      {
         public void actionPerformed(ActionEvent e)
         {
            onDeploy();
         }
      });

      return m_actions.iterator();
   }

   /**
    * Sends a request to server to delete the selected descriptor from there and
    * updates data object of this view if the request is successful.
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
            PSDeploymentClient.getResources().getString("deleteDescMsg"),
            new String[] {m_tableModel.getDisplayName(m_curSelectedRow)} ) ),
            PSDeploymentClient.getResources().getString("deleteConfirmTitle"),
            JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);

         if(option == JOptionPane.YES_OPTION)
         {
            String id = m_tableModel.getID(m_curSelectedRow);
            m_server.getDeploymentManager().deleteExportDescriptor(id);
            m_server.updateDescriptors();
         }
      }
      catch(PSDeployException e)
      {
         PSDeploymentClient.getErrorDialog().showError(e, false,
            PSDeploymentClient.getResources().getString("errorTitle"));
      }
   }

   /**
    * Gets the selected descriptor from the deployment manager based on the
    * selected row's id and starts the export wizard manager to display the
    * wizard dialogs with the selected descriptor.
    *
    * @throws IllegalStateException if there is no row set for which this menu
    * action is shown.
    */
   private void onCreateArchive()
   {
      if(m_curSelectedRow == -1)
         throw new IllegalStateException(
            "the current model row is not set");

      if (!m_server.isServerLicensed())
      {
         // server is not licensed for Enterprise Manager
         // check if the client is running in support mode
         if (!PSDeploymentClient.isSupportMode())
         {
            // not running in support mode, display error message
            PSDeploymentClient.getErrorDialog().showErrorMessage(
               PSDeploymentClient.getResources().getString("notLicensedServer"),
               PSDeploymentClient.getResources().getString("errorTitle") );
            return;
         }
      }


      String id = m_tableModel.getID(m_curSelectedRow);

      try
      {
         PSDeploymentClient.getFrame().setCursor(
               Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
         PSDescriptorLocator locator = new PSDescriptorLocator(id);
         PSExportDescriptor descriptor = locator.load(m_server);

         PSDeploymentExportExecutionPlan expPlan = 
            new PSDeploymentExportExecutionPlan(
               PSDeploymentClient.getFrame(), descriptor, locator, m_server);
         PSDeploymentExportWizardManager mgr = 
            new PSDeploymentExportWizardManager(
               PSDeploymentClient.getFrame(), expPlan);
         PSDeploymentClient.getFrame().setCursor(
               new Cursor(Cursor.DEFAULT_CURSOR));
         mgr.runWizard();
      }
      catch (PSDeployException e)
      {
         PSDeploymentClient.getErrorDialog().showError(e, false,
               PSDeploymentClient.getResources().getString("errorTitle"));
      }
      finally
      {
         // Set Cursor back to default in case of error
         PSDeploymentClient.getFrame().setCursor(
               new Cursor(Cursor.DEFAULT_CURSOR));
      }

   }

   /**
    * Checks whether an archive corresponding to the selected descriptor exists
    * on this client system or not, if it exists displays the 'Install Archive
    * Wizard' with the archive selected, otherwise not.
    *
    * @throws IllegalStateException if there is no row set for which this menu
    * action is shown.
    */
   private void onDeploy()
   {
      if(m_curSelectedRow == -1)
         throw new IllegalStateException(
            "the current model row is not set");

      String id = m_tableModel.getID(m_curSelectedRow);

      try {
         int option = JOptionPane.showConfirmDialog(
            PSDeploymentClient.getFrame(),
            ErrorDialogs.cropErrorMessage(
            PSDeploymentClient.getResources().getString("installWarning")),
            PSDeploymentClient.getResources().getString("warningTitle"),
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

         if(option == JOptionPane.NO_OPTION)
            return;

         PSExportDescriptor descriptor = m_server.getDeploymentManager().
            getExportDescriptor(id);

         File archiveFile = null;
         try {
            File descArcFile = new File(IPSDeployConstants.CLIENT_DIR,
                  PSDeploymentClient.DESCRIPTOR_ARCHIVE_FILE);
            if(descArcFile.exists())
            {
               PSProperties properties = new PSProperties(
                  descArcFile.getAbsolutePath());
               String arcFileName = properties.getProperty(m_server.getServerName()
                   + PSDeploymentClient.SERVER_DESCRIPTOR_SEPARATOR +
                  descriptor.getName());
               if(arcFileName != null)
               {
                  archiveFile = new File(arcFileName);
                  if(!archiveFile.exists())
                     archiveFile = null;
               }
            }
         }
         catch(IOException e) {}

         if(archiveFile == null)
         {
            PSDeploymentClient.getErrorDialog().showErrorMessage(
               PSDeploymentClient.getResources().getString("archiveNotFound"),
               PSDeploymentClient.getResources().getString("errorTitle"));
         }

         PSDeploymentImportExecutionPlan impPlan =
            new PSDeploymentImportExecutionPlan(
            PSDeploymentClient.getFrame(),
            PSDeploymentClient.getFrame().getRegisteredServers(),
            archiveFile);
         PSDeploymentImportWizardManager mgr =
            new PSDeploymentImportWizardManager(
            PSDeploymentClient.getFrame(), impPlan);
         mgr.runWizard();
      }
      catch(PSDeployException e)
      {
         PSDeploymentClient.getErrorDialog().showError(e, false,
            PSDeploymentClient.getResources().getString("errorTitle"));
      }
   }

   /**
    * The data object that supports this view and its pop-up menu actions, same
    * as super class <code>m_object</code>, but properly casted to be easy to
    * use. <code>null</code> until first call to <code>setData(Object)</code>.
    */
   private PSDeploymentServer m_server = null;
}
