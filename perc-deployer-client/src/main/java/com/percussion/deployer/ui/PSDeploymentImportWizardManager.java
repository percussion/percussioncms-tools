/******************************************************************************
 *
 * [ PSDeploymentImportWizardManager.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.deployer.ui;

import com.percussion.deployer.client.IPSDeployJobControl;
import com.percussion.deployer.objectstore.PSArchiveSummary;
import com.percussion.deployer.objectstore.PSImportDescriptor;
import com.percussion.error.PSDeployException;
import com.percussion.guitools.ErrorDialogs;
import com.percussion.server.PSServerLockException;

import javax.swing.*;
import java.awt.*;

/**
 * The wizard manager for installing an archive. Controls the import process and
 * manages all dialogs in the wizard. After instance of this class is created, a
 * call to {@link #runWizard()} will manage entire process. Once that method 
 * call returns, a reference to this class is no longer needed or useful.
 */
public class PSDeploymentImportWizardManager  extends PSDeploymentWizardManager
{
   /**
    * Constructs the manager with the supplied plan.
    * 
    * @param parent the parent frame for the status dialog of this wizard task, 
    * may be <code>null</code>
    * @param plan the plan to use by this manager, may not be <code>null</code>
    * 
    * @throws IllegalArgumentException if plan is <code>null</code>
    */
   public PSDeploymentImportWizardManager(Frame parent, 
      PSDeploymentImportExecutionPlan plan)
   {
      super(parent, plan);
      
      m_importPlan = plan;
   }

   /**
    * Prepares the list of jobs (<code>COPY_JOB</code> and <code>IMPORT_JOB
    * </code>) and displays the status dialog. The <code>COPY_JOB</code> is 
    * initiated only if the archive does not exist on server. At the end cleans 
    * up the resources by calling {@link #onFinish()}. See base class 
    * description for this method for more description.
    */
   protected void runJob()
   {
      m_statusDialog = new PSDeploymentStatusDialog(m_parent, this, 
         ms_res.getString("importStatusTitle"));
      PSDeploymentServer deploymentServer = 
         m_importPlan.getDeploymentServer();
      
      int job;         
      m_todoJobs.clear();        
      try
      {
         if(m_importPlan.isExistingArchive()) //only import job
            job = IMPORT_JOB;
         else //both copy and import
         {
            job = COPY_JOB;
            m_todoJobs.add(new Integer(IMPORT_JOB));                  
            
            // check for overwrite and delete
            if (m_importPlan.isOverWrite())
            {
               deploymentServer.getDeploymentManager().deleteArchive(
                  m_importPlan.getArchiveRef());
            }
         }

         m_parent.setCursor(
            Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));                  
         if(initiateAndSetJob(job))
         {
            m_parent.setCursor(
               Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));                  
            m_statusDialog.setVisible(true);             
            //when we return from status dialog, update data         
            deploymentServer.updateArchives();
            deploymentServer.updatePackages();
         }         
      }
      catch(PSDeployException e)
      {
         ErrorDialogs.showErrorMessage(m_statusDialog, 
            e.getLocalizedMessage(), ms_res.getString("errorTitle") );
      }  
      
      m_parent.setCursor(
         Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));            

      onFinish();
   }
   
   /**
    * Initiates or starts the job corresponding to specified job id and sets the 
    * job on status dialog for refreshing its ui and monitor the status. 
    * Displays error message if an exception happens starting the job.
    * 
    * @param jobID the job identifier, assumed to be one of the xxx_JOB values.
    * 
    * @return <code>true</code> if successfully started the job, otherwise 
    * <code>false</code>
    */
   private boolean initiateAndSetJob(int jobID)
   {
      boolean result = true;
      
      PSDeploymentServer deploymentServer = 
         m_importPlan.getDeploymentServer();         
      PSImportDescriptor descriptor = 
         (PSImportDescriptor)m_importPlan.getDescriptor();
      try 
      {
         IPSDeployJobControl jobControl = null;
         switch(jobID)
         {
            case COPY_JOB:
               jobControl = deploymentServer.getDeploymentManager().
                  copyArchiveToServer(m_importPlan.getArchiveRef(), 
                  m_importPlan.getArchiveFile());
               m_statusDialog.setJob(ms_res.getString("copyArchiveToServer"), 
                  jobControl); 
               break;
               
            case IMPORT_JOB:
               Component parent;
               if (m_statusDialog.isShowing())
                  parent = m_statusDialog;
               else
                  parent = m_parent;
               boolean tryImport = true;
               while (tryImport)
               {
                  try
                  {
                     jobControl = deploymentServer.getDeploymentManager().
                        runImportJob(descriptor);  
                     tryImport = false;
                  }
                  catch (PSServerLockException e)
                  {
                     tryImport = waitForPublisher(parent);
                  }
               }
               
               if (jobControl == null)
               {
                  result = false;
                  
                  // try to delete the archive if we've copied it
                  if(!m_importPlan.isExistingArchive())
                     deploymentServer.getDeploymentManager().deleteArchive(
                        m_importPlan.getArchiveRef());
                  
                  if (m_statusDialog.isShowing())
                     m_statusDialog.setVisible(false);
               }
               else
                  m_statusDialog.setJob(ms_res.getString("importJob"), 
                     jobControl);                  
               break;            
         }
      }
      catch(PSDeployException e)
      {
         //inform the status dialog about the error if it is visible.
         if(m_statusDialog.isShowing())
            m_statusDialog.errorSettingJob(e.getLocalizedMessage());
         else
         {
            ErrorDialogs.showErrorMessage(m_parent, e.getLocalizedMessage(), 
               ms_res.getString("errorTitle") );
         }
         result = false;
      }
      
      return result;
   }
   
   /**
    * Notification event for the job initiated is completed. Initiates the next
    * job in queue. When all jobs are completed enables the log button in the
    * status dialog and renames the cancel button to close. Informs the server
    * to update its cache regarding installed archives and packages.
    */
   public void jobCompleted()
   {
      m_statusDialog.setCursor(
         Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));    
      if(!m_todoJobs.isEmpty())
      {
         int job = ((Integer)m_todoJobs.remove(0)).intValue();
         initiateAndSetJob(job);
      }
      else
      {
         m_statusDialog.changeCancelToClose();
         m_statusDialog.enableLog();
      }
      m_statusDialog.setCursor(
         Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));       
   }
   
   /**
    * Checks whether this manager supports log view or not
    * 
    * @return <code>true</code> to indicate it supports.
    */
   public boolean supportsLog()
   {
      return true;
   }

   /**
    * Shows the log view of the installed archive. This should be called after 
    * the completion of import jobs when view log button is clicked in status 
    * dialog.
    */
   public void viewLog()
   {
      try {
         PSDeploymentServer deploymentServer = m_importPlan.getDeploymentServer();             
         PSArchiveSummary summary = deploymentServer.getDeploymentManager().
            getArchiveSummary(m_importPlan.getArchiveRef());
         PSArchiveSummaryDialog dlg = 
            new PSArchiveSummaryDialog(m_parent, deploymentServer, summary);
         dlg.setVisible(true);
      }
      catch(PSDeployException e)
      {
         ErrorDialogs.showErrorMessage(m_statusDialog, e.getLocalizedMessage(), 
            ms_res.getString("errorTitle") );
      }
   }
   
   /**
    * Displays message to user explaining they must wait for the publisher to
    * finish, offering them a chance to either abort or try again.
    * 
    * @param parent The parent window, assumed not <code>null</code>.
    * 
    * @return <code>true</code> to try again, <code>false</code> to abort.
    */
   private boolean waitForPublisher(Component parent)
   {
      boolean tryAgain = true;
      int option = JOptionPane.showConfirmDialog(parent, 
         ErrorDialogs.cropErrorMessage(
            ms_res.getString("publisherLockedMsg")), 
         ms_res.getString("publisherLockedTitle"), 
         JOptionPane.OK_CANCEL_OPTION, 
         JOptionPane.INFORMATION_MESSAGE);
      
      if (option == JOptionPane.CANCEL_OPTION)
      {
         // lets be really sure they want to abort
         int abortOption = JOptionPane.showConfirmDialog(parent, 
            ErrorDialogs.cropErrorMessage(
               ms_res.getString(
                  "publisherLockedAbortConfirmMsg")), 
            ms_res.getString("publisherLockedAbortConfirmTitle"), 
            JOptionPane.YES_NO_OPTION, 
            JOptionPane.INFORMATION_MESSAGE);
         
         if (abortOption == JOptionPane.YES_OPTION)
         {
            // were done
            tryAgain = false;
         }
         else
         {
            tryAgain = waitForPublisher(parent);
         }
      }
      return tryAgain;
   }
   
   /**
    * The plan that is used by this manager to execute, intialized in the 
    * constructor and never <code>null</code> or modified after that.
    */
   private PSDeploymentImportExecutionPlan m_importPlan;
   
   /**
    * The constant to identify copying the archive to the server from the 
    * client.
    */
   private static final int COPY_JOB = 1;
     
   /**
    * The constant to identify the import job.
    */
   private static final int IMPORT_JOB = 2;
}
