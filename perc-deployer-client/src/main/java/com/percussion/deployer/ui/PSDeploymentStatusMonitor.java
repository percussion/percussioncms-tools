/******************************************************************************
 *
 * [ PSDeploymentStatusMonitor.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.deployer.ui;

import com.percussion.deployer.client.IPSDeployJobControl;
import com.percussion.error.PSDeployException;
import com.percussion.guitools.ErrorDialogs;

/**
 * Uses a thread to poll a job and set status on the passed
 * in Status Dialog.  The dialog will handle the UI update so this class
 * will not need to handle any UI threading issues. When job is completed or
 * aborted, calls appropriate method on the Status Dialog and then the thread
 * will terminate.
 */
public class PSDeploymentStatusMonitor
{

   /**
    * Constructor for this class.
    * 
    * @param jobControl The controller used to check on the status of
    * the running job, may not be <code>null</code>
    * @param statusDlg The status dialog that should be updated, may not be 
    * <code>null</code>
    * 
    * @throws IllegalArgumentException if any parameter is invalid.
    */
   public PSDeploymentStatusMonitor(IPSDeployJobControl jobControl,
      PSDeploymentStatusDialog statusDlg)
   {
      if(jobControl == null)
         throw new IllegalArgumentException("jobControl may not be null.");
         
      if(statusDlg == null)
         throw new IllegalArgumentException("statusDlg may not be null.");
      
      m_controller = jobControl;
      m_dialog = statusDlg;
   }

   /**
    * Starts the worker thread. Polls controller for status of job, and updates
    * status on the statusdialog's progress bar.  If controller reports job is
    * completed or aborted, calls appropriate method on status dialog and
    * terminates. If an exception happens getting the status from the controller
    * it tries one more time to get the status and if still the exception 
    * happens, it terminates the thread by informing the dialog as job aborted 
    * with the exception message as reason for job aborting. May not be called 
    * more than once.
    * 
    * @throws IllegalStateException if called second time.
    */
   public void startMonitor()
   {
      if(m_monitorThread != null)
         throw new IllegalStateException(
            "The monitor thread is already running/stopped running, " +
            "you can not start it again.");
            
      m_monitorThread = new Thread()
      {
         public void run()
         {
            try {
               int status = 1;
               int excepCounter = 0;
               String statusMessage = "";
              
               do {
                  try {
                     synchronized(m_controller)
                     {
                        status = m_controller.getStatus();
                        statusMessage = m_controller.getStatusMessage();
                     }
                     if(status != -1)
                        m_dialog.updateStatus(statusMessage, status);
                  }
                  catch(PSDeployException e)
                  {
                     excepCounter++;                        
                     if(excepCounter > 1)
                     {
                        status = -1;
                        statusMessage = e.getLocalizedMessage();
                        break;
                     }
                  }
                                         
                  //sleep for 1 sec   
                  sleep(100);
               }
               while(status != -1 && status != 100);                                             

               if(status == -1)
               {
                  m_dialog.jobAborted(statusMessage);
               }
               else
                  m_dialog.jobCompleted();
            }
            catch(InterruptedException e)
            {

            }
         }
         
      };
      m_monitorThread.setDaemon(true);
      m_monitorThread.start();
   }

   /**
    * Cancels the currently running job and informs the dialog that the job is 
    * cancelled with the cancel status and interrupts the status thread if the 
    * cancelling was done successfully. If the thread is not currenly running, 
    * simply returns. Displays error message if an exception happens cancelling 
    * the job and simply returns. Should be called when the status dialog's 
    * cancel button has been clicked.
    * <p>
    * See {@link 
    * IPSDeployJobControl#cancelDeployJob()}
    * for more description about the cancel status. 
    */
   public void stopMonitor()
   {
      if(m_monitorThread != null && m_monitorThread.isAlive())
      {         
         int status = -1;
         try {
            synchronized(m_controller)
            {
               status = m_controller.cancelDeployJob();
            }         
            m_monitorThread.interrupt();
            m_dialog.jobCancelled(status, m_controller.getStatusMessage());
         }
         catch(PSDeployException e)
         {
            ErrorDialogs.showErrorMessage(m_dialog, e.getLocalizedMessage(), 
               PSDeploymentClient.getResources().getString("errorTitle"));
            return;
         }
      }
   }
   
   /**
    * The thread that monitors the status of the current job set, initialized
    * and started in <code>startMonitor()</code> and never <code>null</code> or 
    * modified after that. The thread will be stopped by calling <code>
    * stopMonitor()</code>.
    */
   private Thread m_monitorThread = null;

   /**
    * The job controller to use for determining job status.  Initialized in
    * constructor, never <code>null</code> or modified after that.
    */
   private IPSDeployJobControl m_controller = null;

   /**
    * The status dialog that is displaying the status of the running job. 
    * Initialized in constructor, never <code>null</code> or modified after 
    * that.
    */
   private PSDeploymentStatusDialog m_dialog = null;
}
