/******************************************************************************
 *
 * [ PSDeploymentJobDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.deployer.ui;

import com.percussion.deployer.client.IPSDeployJobControl;

import java.awt.*;

/**
 * The base class for deployment job dialogs to track the status of the job.
 */
public abstract class PSDeploymentJobDialog extends PSDeploymentWizardDialog 
   implements IPSJobManager
{
   /**
    * Constructor for any deployment job dialogs to track the status.
    * Calls {@link 
    * PSDeploymentWizardDialog#PSDeploymentWizardDialog(Frame, 
    * PSDeploymentServer, int, int) super(parent, server, step, sequence)}.
    * Additional parameter is described below.
    * 
    * @param title the title of the status dialog, may not be <code>null</code> 
    * or empty.
    */
   public PSDeploymentJobDialog(Frame parent, 
      PSDeploymentServer deploymentServer, int step, int sequence, String title)
   {
      super(parent, deploymentServer, step, sequence);
      
      if(title==null || title.trim().length() == 0)
         throw new IllegalArgumentException("title may not be null or empty.");
      
      m_statusDialog = new PSDeploymentStatusDialog(parent, this, title);
   }

   /**
    * Sets the specified job on the status dialog to track its status. Should be
    * called before making this dialog visible.
    * 
    * @param jobDescription the current job description, may not be <code>null
    * </code> or empty.
    * @param jobController the current job controller, may not be <code>null
    * </code>
    * 
    * @throws IllegalArgumentException if any parameter is invalid
    */   
   public void setJob(String jobDescription, IPSDeployJobControl jobController)
   {
      if(jobDescription == null || jobDescription.trim().length() == 0)
         throw new IllegalArgumentException(
            "jobDescription may not be null or empty.");
            
      if(jobController == null)
         throw new IllegalArgumentException("jobController may not be null.");
         
      m_statusDialog.setJob(jobDescription, jobController);
   }
   
   /**
    * Overridden to show or hide the status dialog.
    * 
    * @param flag if <code>true</code> it displays the dialog, otherwise hides
    * and disposes the dialog.
    */
   public void setVisible(boolean flag)
   {
      boolean isShowing = m_statusDialog.isShowing();
      
      m_statusDialog.setVisible(flag);
      
      if(!flag && isShowing)
         m_statusDialog.dispose();
   }   
   
   //default implementation to return false
   public boolean supportsLog()
   {
      return false;
   }   
   
   //default implementation is to throw UnsupportedOperationException  
   public void viewLog()
   {
      throw new UnsupportedOperationException("log view is not supported");
   }
   
   /**
    * The status dialog that will be shown for this job status, initialized
    * in the constructor and never <code>null</code> or modified after that.     
    */
   private PSDeploymentStatusDialog m_statusDialog;
}
