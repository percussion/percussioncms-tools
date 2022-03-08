/******************************************************************************
 *
 * [ PSDeploymentValidationJobDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.deployer.ui;

import com.percussion.deployer.client.IPSDeployJobControl;
import com.percussion.deployer.objectstore.PSImportDescriptor;
import com.percussion.error.PSDeployException;
import com.percussion.guitools.ErrorDialogs;

import java.awt.*;

/**
 * The dialog to create validation job and track its status.
 */
public class PSDeploymentValidationJobDialog extends PSDeploymentJobDialog
{
   /**
   * Constructs this dialog calling {@link 
    * PSDeploymentJobDialog#PSDeploymentJobDialog(Frame, PSDeploymentServer, 
    * int, int, String)} with the appropriate title.
    */
   public PSDeploymentValidationJobDialog(Frame parent, 
      PSDeploymentServer deploymentServer, int step, int sequence)
   {
      super(parent, deploymentServer, step, sequence,
         ms_res.getString("validtionJobTitle"));
   }

   /**
    * Initializes the validation job and tracks the status of the job.
    * 
    * @throws PSDeployException if an error happens initializing the job.
    */   
   protected void init() throws PSDeployException
   {
      m_impDescriptor = (PSImportDescriptor)m_descriptor;
      IPSDeployJobControl validationJob = m_deploymentServer.
         getDeploymentManager().runValidationJob(m_impDescriptor);
      setJob(ms_res.getString("validationJob"), validationJob);      
   }

   /**
    * Recieves the notification for validation job completion. Gets the 
    * validation results of the import descriptor from the server and hides the
    * dialog by calling <code>super.onNext()</code>. If an exception happens 
    * getting the results it simply hides the dialog, so that the caller knows 
    * that job is not successful, since it does not set the super's <code>
    * m_isNext</code> flag to <code>true</code>. Displays the error message too.
    */   
   public void jobCompleted()
   {
      try {
         m_deploymentServer.getDeploymentManager().loadValidationResults(
            m_impDescriptor);
         //hides the dialog and sets the flag as that next button is clicked.            
         super.onNext(); 
      }
      catch(PSDeployException e)
      {
         ErrorDialogs.showErrorMessage(this, e.getLocalizedMessage(), 
            ms_res.getString("errorTitle") );
         //simply hides the dialog does not set that as the Next button is clicked            
         setVisible(false); 
      }         
   }

   // see base class
   public Object getDataToSave()
   {
      return null;
   }

   /**
    * The import descriptor that to be validated, initialized in the <code>
    * init()</code> method and updated with validation results after validating 
    * the descriptor. Never <code>null</code> after it is initialized.
    */
   private PSImportDescriptor m_impDescriptor;
}
