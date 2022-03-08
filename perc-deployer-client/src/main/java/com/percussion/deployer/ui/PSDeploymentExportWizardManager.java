/******************************************************************************
 *
 * [ PSDeploymentExportWizardManager.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.deployer.ui;

import com.percussion.deployer.client.IPSDeployJobControl;
import com.percussion.deployer.objectstore.PSExportDescriptor;
import com.percussion.error.PSDeployException;
import com.percussion.guitools.ErrorDialogs;

import java.awt.*;
import java.io.File;

/**
 * The wizard manager for creating archive and creating/updating export 
 * descriptor. Controls the export process and manages all dialogs in the 
 * wizard. After instance of this class is created, a call to {@link 
 * #runWizard()} will manage entire process. Once that method call returns,
 * a reference to this class is no longer needed or useful.
 */
public class PSDeploymentExportWizardManager  extends PSDeploymentWizardManager
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
   public PSDeploymentExportWizardManager(Frame parent, 
      PSDeploymentExportExecutionPlan plan)
   {
      super(parent, plan);
      
      m_exportPlan = plan;
   }

   /**
    * Prepares the list of jobs (<code>EXPORT_JOB</code> and <code>COPY_JOB
    * </code>) and displays the status dialog set with the first job after 
    * starting it only if the user chosen to create archive in the export plan
    * execution dialogs. At the end cleans up the resources by calling {@link 
    * #onFinish()}. See base class description for this method for more 
    * description.
    */
   protected void runJob()
   {
      if(m_exportPlan.isCreateArchive())
      {
         //the first job 'EXPORT_JOB' is not added to the list as we are
         //immediately starting the job.
         m_todoJobs.clear();
         m_todoJobs.add(new Integer(COPY_JOB));         
         m_parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));            
         m_statusDialog = new PSDeploymentStatusDialog(m_parent, this, 
            ms_res.getString("exportStatusTitle"));
         if(initiateAndSetJob(EXPORT_JOB))
         {
            m_parent.setCursor(
               Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));                                       
            m_statusDialog.setVisible(true);       
         }
         else
         {
            m_parent.setCursor(
               Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));                              
         }
      }
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
      PSDeploymentServer deploymentServer = 
         m_exportPlan.getDeploymentServer();         
      PSExportDescriptor descriptor = 
         (PSExportDescriptor)m_exportPlan.getDescriptor();
      try 
      {
         IPSDeployJobControl jobControl = null;
         switch(jobID)
         {
            case EXPORT_JOB:
               jobControl = deploymentServer.getDeploymentManager().
                  runExportJob(descriptor);  
               m_statusDialog.setJob(ms_res.getString("exportJob"), 
                  jobControl);                   
               break;
            case COPY_JOB:
               File archiveFile = m_exportPlan.getArchiveFile();
               jobControl = deploymentServer.getDeploymentManager().
                  copyArchiveFromServer(descriptor.getName(), archiveFile);
               m_statusDialog.setJob(ms_res.getString("copyArchiveFromServer"), 
                  jobControl); 
               break;
         }
         return true;
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
         return false;
      }
   }
   
   /**
    * Notification event for the job initiated is completed.
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
      }
      m_statusDialog.setCursor(
         Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));                                             
   }
   
   /**
    * The plan that is used by this manager to execute, intialized in the 
    * constructor and never <code>null</code> or modified after that.
    */
   private PSDeploymentExportExecutionPlan m_exportPlan;
     
   /**
    * The constant to identify the export job/creating archive
    */
   private static final int EXPORT_JOB = 1;
   
   /**
    * The constant to identify copying the archive from the server to the 
    * client.
    */
   private static final int COPY_JOB = 2;
}
