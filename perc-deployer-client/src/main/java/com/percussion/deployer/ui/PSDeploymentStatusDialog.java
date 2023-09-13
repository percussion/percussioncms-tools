/******************************************************************************
 *
 * [ PSDeploymentStatusDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.deployer.ui;

import com.percussion.UTComponents.UTFixedButton;
import com.percussion.deployer.client.IPSDeployJobControl;
import com.percussion.guitools.ErrorDialogs;
import com.percussion.guitools.PSDialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;

/**
 * The status dialog to show the progress bar for the status of the current job
 * set with this dialog. Creates a new thread to poll on the job controller and 
 * receive status and updates the progress bar and status message for each job 
 * set.
 */
public class PSDeploymentStatusDialog extends PSDialog
{
   /**
    * Constructs this dialog with specified job manager.
    * 
    * @param parent the parent frame of this dialog, may be <code>null</code>
    * @param jobManager the job manager, may not be <code>null</code>
    * @param title the title of the dialog, may not be <code>null</code> or 
    * empty.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    */
   public PSDeploymentStatusDialog(Frame parent, IPSJobManager jobManager, 
      String title)
   {
      super(parent);
      
      if(jobManager == null)
         throw new IllegalArgumentException("jobManager may not be null");
         
      if(title == null || title.trim().length() == 0)
         throw new IllegalArgumentException("title may not be null or empty.");
         
      m_jobManager = jobManager;
      
      initDialog(title);
   }
   
   /**
    * Initializes the dialog framework with progress bar and labels for job 
    * description. Sets the dialog title also.
    * 
    * @param title the title of the dialog, assumed not <code>null</code> or 
    * empty. 
    */
   private void initDialog(String title)
   {
      setTitle(title);
      
      JPanel panel = new JPanel();
      getContentPane().add(panel);
      panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
      EmptyBorder emptyBorder = new EmptyBorder(10, 10, 10, 10);
      panel.setBorder(emptyBorder);
      
      panel.add(m_msgLabel);
      panel.add(Box.createVerticalStrut(10));
            
      panel.add(new JLabel(
         getResourceString("status"), SwingConstants.LEFT));         
      JPanel progressPanel = new JPanel();
      progressPanel.setLayout(new BoxLayout(progressPanel, BoxLayout.X_AXIS));
      m_progressBarLabel.setPreferredSize(new Dimension(180, 20));
      progressPanel.add(m_progressBarLabel);
      progressPanel.add(Box.createHorizontalStrut(10));
      m_progressBar.setStringPainted(true);
      progressPanel.add(m_progressBar);    
      progressPanel.add(Box.createHorizontalGlue());
      progressPanel.setAlignmentX(LEFT_ALIGNMENT);
      panel.add(progressPanel);
      
      panel.add(Box.createVerticalStrut(15));
      JPanel commandPanel = new JPanel();
      commandPanel.setLayout(new BoxLayout(commandPanel, BoxLayout.X_AXIS));
      commandPanel.setAlignmentX(LEFT_ALIGNMENT);
      commandPanel.add(Box.createHorizontalGlue());
      if(m_jobManager.supportsLog())
      {
         m_showLogButton = new UTFixedButton(
            getResourceString("showLog"), new Dimension(100, 24));
         m_showLogButton.setEnabled(false);
         m_showLogButton.addActionListener(new ActionListener() 
         {
            public void actionPerformed(ActionEvent e) 
            {
               m_jobManager.viewLog();
            }
         });
         commandPanel.add(m_showLogButton);
         commandPanel.add(Box.createHorizontalStrut(10));
      }
      m_cancelButton = new UTFixedButton("");  
      m_cancelButton.setAction(
         new AbstractAction(getResourceString("cancel"))
         {
            public void actionPerformed(ActionEvent e)
            {
               onCancel();
            }
         }
      );
      commandPanel.add(m_cancelButton);
      commandPanel.add(Box.createHorizontalGlue());
      panel.add(commandPanel);
      panel.add(Box.createVerticalStrut(15));      
      panel.add(Box.createVerticalGlue());

      pack();
      center();
      setResizable(true);
   }
   
   /**
    * Action method for 'Cancel' button. Confirms with the user to cancel the 
    * job and cancels the job if the button represents 'Cancel' action, 
    * otherwise simply closes the dialog by hiding it. See {@link 
    * PSDeploymentStatusMonitor#stopMonitor()} for more description on 
    * cancelling a job.
    */
   public void onCancel()
   {
      if(m_isCancel)
      {
         int option = JOptionPane.showConfirmDialog(this, 
            getResourceString("cancelConfirmMsg"), 
            getResourceString("cancelTitle"), 
            JOptionPane.YES_NO_OPTION, 
            JOptionPane.INFORMATION_MESSAGE);
            
         if(option == JOptionPane.YES_OPTION)
         {
            m_jobStatusMonitor.stopMonitor();      
         }
      }
      else
         setVisible(false);
   }
   
   /**
    * Called by monitor when the job this dialog is monitoring has completed 
    * successfully. Informs the manager about the current job completion.
    */
   public void jobCompleted()
   {
      m_jobManager.jobCompleted();
   }

   /**
    * Called by monitor when the job this dialog is monitoring terminates 
    * abnormally. Displays error message to the user with the reason for job
    * abnormal termination and hides this dialog.
    */
   public void jobAborted(String message)
   {
      String msg = getResourceString("abortErrorMsg");
      msg = MessageFormat.format(msg, 
         new String[] {message});
      ErrorDialogs.showErrorMessage(this, msg,
            getResourceString("abortErrorTitle") );
      setVisible(false);
   }
   
   /**
    * Called by monitor when the job cancel request is completed sucessfully.
    * 
    * @param cancelStatus the status returned by the job controller, must be one
    * of the <code>IPSDeployJobControl.JOB_xxx</code> values.
    * @param message the message returned from the controller, may be <code>
    * null</code> or empty.
    * 
    * @throws IllegalArgumentException if cancelStatus is not valid.
    */
   public void jobCancelled(int cancelStatus, String message)
   {      
      if(cancelStatus == IPSDeployJobControl.JOB_ABORTED ||
         cancelStatus == IPSDeployJobControl.JOB_COMPLETED ||
         cancelStatus == IPSDeployJobControl.JOB_CANCELLED)
      {
         //display a message that the job is aborted before completion.
         if(message != null && message.trim().length() == 0)
         {
            ErrorDialogs.showErrorMessage(this, message,
               getResourceString("cancelTitle") );
         }
      }
      else
         throw new IllegalArgumentException("invalid cancel status");
      
      setVisible(false);
   }

   /**
    * Updates the ui to set the job description and creates a new status monitor
    * with job controller to poll on the status and update the ui.
    * 
    * @param jobDescription the current job description, may not be <code>null
    * </code> or empty.
    * @param jobController the current job controller, may not be <code>null
    * </code>
    */
   public void setJob(String jobDescription, IPSDeployJobControl jobController)
   {
      if(jobDescription == null || jobDescription.trim().length() == 0)
         throw new IllegalArgumentException(
            "jobDescription may not be null or empty.");
            
      if(jobController == null)
         throw new IllegalArgumentException("jobController may not be null.");
         
      m_progressBarLabel.setText(jobDescription);
      m_jobStatusMonitor = new PSDeploymentStatusMonitor(jobController, this);
      m_jobStatusMonitor.startMonitor();
   }
   
   /**
    * Displays the error message and hides this dialog if the dialog is showing.
    * The method to be called by the job manager of this dialog, when an error
    * happens to set the job on this dialog.
    * 
    * @param errorMsg the error message to be displayed, may not be <code>null
    * </code> or empty.
    * 
    * @throws IllegalArgumentException if error message is invalid.
    */
   public void errorSettingJob(String errorMsg)
   {
      if(errorMsg == null || errorMsg.trim().length() == 0)
         throw new IllegalArgumentException(
            "errorMsg may not be null or empty.");
         
      if(isShowing())
      {
         ErrorDialogs.showErrorMessage(this, 
            MessageFormat.format(getResourceString("abortErrorMsg"), 
            new String[]{errorMsg}),
            getResourceString("abortErrorTitle") );
         setVisible(false);
      }
   }

   /**
    * Requests the Swing event dispatch thread to update the progress bar and 
    * status message and returns immediately.
    * 
    * @param statusMessage the status message to show, may be <code>null</code> 
    * or empty.
    * @param percentDone The percent complete to set on the progress bar. Must be 
    * between 1 and 100.
    * 
    * @throws IllegalArgumentException if percentDone is not valid.
    */
   public void updateStatus(final String statusMessage, final int percentDone)
   {
      if(percentDone < 1 || percentDone > 100)
         throw new IllegalArgumentException(
            "percentDone must be between 1 and 100.");
            
      SwingUtilities.invokeLater(new Runnable() 
      {
         public void run()
         {
            m_msgLabel.setText(statusMessage);
            m_progressBar.setValue(percentDone);      
         }
      });
   }
   
   /**
    * Requests the Swing event dispatch thread to enable the log button if it 
    * exists and returns immediately. May be called by the job manager
    * when all the jobs initiated by the manager are completed and if it 
    * supports log view.
    */
   public void enableLog()
   {
      SwingUtilities.invokeLater(new Runnable()
      {
         public void run()
         {
            if(m_showLogButton != null)
               m_showLogButton.setEnabled(true);
         }
      });
   }
   
   /**
    * Requests the Swing event dispatch thread to change 'Cancel' button label 
    * to 'Close' and to change the action of the button correspondingly and 
    * returns immediately. Should be called by the manager when all the jobs
    * completed by it are done. See {@link #onCancel()} for more description of
    * 'Cancel' button action.
    */
   public void changeCancelToClose()
   {         
      SwingUtilities.invokeLater(new Runnable()
      {
         public void run()
         {
            m_cancelButton.setText(getResourceString("close"));
            m_isCancel = false;
         }
      });
   }

   /**
    * The flag to indicate 'Cancel' button current action. If <code>true</code>
    * the button represents 'Cancel' action (confirms with user to cancel and 
    * cancels current job), otherwise 'Close' action (simply closes as job is 
    * done). Initialized to <code>true</code> and set to <code>false</code> in
    * <code>changeCancelToClose()</code>. 
    */
   private boolean m_isCancel = true;
   
   /**
    * The progress bar for the currently monitored job. Never <code>null</code> 
    * after it is initialized.
    */
   private JProgressBar m_progressBar = new JProgressBar();
   
   /**
    * The label that represents the current job status message, initialized to
    * an empty label, and gets updated by status monitor of the current job in
    * <code>updateStatus(String, int)</code). Never <code>null</code> after it
    * is initialized.
    */
   private JLabel m_msgLabel = new JLabel();
   
   /**
    * The label that represents the current job description, initialized to an
    * empty label and set with actual label in <code>
    * setJob(String, IPSDeployJobControl)</code> for each job this dialog 
    * monitors. Never <code>null</code> after it is initialized.
    */
   private JLabel m_progressBarLabel = new JLabel();
   
   /**
    * The button to show log view of the current job, shown only if the job 
    * manager supports the log view. This will be disabled until the job 
    * manager informs to enable it. Initialized in <code>initDialog()</code> 
    * only if the log view is supported and never <code>null</code> or modified 
    * after that.
    */
   private UTFixedButton m_showLogButton = null;
   
   /**
    * The button to cancel the current running job or close the dialog, the 
    * action changes from cancel to close by a call to <code>
    * changeCancelToClose()</code>, initialized in <code>initDialog()</code> and
    * never <code>null</code> after that.
    */
   private UTFixedButton m_cancelButton;
   
   /**
    * The monitor which polls on the job controller for the current job, 
    * initialized to a new monitor object in <code>
    * setJob(String, IPSDeployJobControl)</code>. Never <code>null</code> after
    * it is initialized.
    */
   private PSDeploymentStatusMonitor m_jobStatusMonitor;
   
   /**
    * The manager that need to be informed of the job completed status when the
    * currently set job is done. This dialog will have 'Show Log' button only if
    * the job manager supports the log view and will be disabled until the job 
    * manager informs to enable it. Initialized in the constructor and never 
    * <code>null</code> or modified after that.
    */
   private IPSJobManager m_jobManager;
}
