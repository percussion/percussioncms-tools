/******************************************************************************
 *
 * [ PSDeploymentWizardManager.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.deployer.ui;

import com.percussion.error.PSDeployException;
import com.percussion.guitools.ErrorDialogs;
import com.percussion.guitools.PSResources;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
* The abstract base class for wizard manager. Provides basic implementation and
* the abstract methods to be overridden by derived classes.
*/
public abstract class PSDeploymentWizardManager  implements IPSJobManager
{   
  /**
   * Constructs this wizard manager with supplied execution plan.
   * 
   * @param parent the parent frame for the status dialog of this wizard task, 
   * may be <code>null</code>
   * @param plan the plan to execute, may not be <code>null</code>
   * 
   * @throws IllegalArgumentException if any plan is <code>null</code>
   */
  public PSDeploymentWizardManager(Frame parent, 
     IPSDeploymentExecutionPlan plan)
  {
     if(plan == null)
        throw new IllegalArgumentException("plan may not be null.");
        
     m_parent = parent;
     m_plan = plan;
  }   

  /**
   * Displays the dialogs in the wizard in sequence according to its plan and 
   * updates the plan with user settings in the dialogs. Once last step of 
   * wizard completes, calls {@link #runJob()} to initiate the jobs to be done.
   * If user cancels out of any step this wizard displays or exception happens
   * in displaying the dialog, then it calls {@link #onFinish()} to clean up 
   * any resources and leaves the wizard task.
   */
  public void runWizard()
  {
     PSDeploymentWizardDialog dlg = null;
     try {
        m_parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        boolean isNext = true;
        boolean isCancelled = false;
        while(!isCancelled && (dlg = m_plan.getDialog(isNext)) != null)
        {
           m_parent.setCursor(
                Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));                        
           showDialog(dlg);
           m_parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));            
           if(dlg.isNext())
           {
              isNext = true;
              m_plan.updateUserSettings(dlg);
           }
           else if (dlg.isBack())
           {
              isNext = false;
              m_plan.updateUserSettings(dlg);
           }
           else 
           {
              // cancelled
              isCancelled = true;
           }
           dlg.dispose();
        }
        
        if (!isCancelled)
           runJob();
        else
           onFinish();
        
        m_parent.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));                                                            
     }
     catch(PSDeployException e)
     {
        m_parent.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));         
        ErrorDialogs.showErrorMessage(m_parent, e.getLocalizedMessage(), 
           ms_res.getString("errorTitle") );
        onFinish();
     }
  }
  
  /**
   * Initializes the list of jobs to be done and initializes status dialog with
   * the first job. Once the status dialog returns, calls {@link #onFinish()} 
   * to clean up any resources. All the remaining jobs will be done in <code>
   * jobCompleted()</code> as the previous job is completed. Removes the job 
   * from the list once the job is initiated.
   */
  protected abstract void runJob();
  
  /**
   * Disposes the status dialog if it is shown and frees any other resources 
   * used.
   */
  protected void onFinish()
  {
     if(m_statusDialog != null)
        m_statusDialog.dispose();
  }

  /**
   * Displays the supplied dialog. 
   * 
   * @param dialog the dialog to show, may not be <code>null</code>
   * 
   * @throws IllegalArgumentException if dialog is <code>null</code>
   * @throws PSDeployException if an error happens showing the dialog.
   */
  protected void showDialog(PSDeploymentWizardDialog dialog) 
     throws PSDeployException
  {
     if(dialog == null)
        throw new IllegalArgumentException("dialog may not be null.");
     
     dialog.onShow(m_plan.getDescriptor());
  }

  /**
   * Always returns <code>false</code>. Derived classes should override this if
   * it supports log viewing. This is useful for status dialog of the job 
   * progress to determine whether to show the 'View Log' button or not.
   * 
   * @return <code>false</code>
   */
  public boolean supportsLog()
  {
     return false;
  }

  /**
   * Derived classes should override this, if it supports the log view. See 
   * {@link #supportsLog()} for more information. 
   * 
   * @throws UnsupportedOperationException if it does not support the log view.
   */
  public void viewLog()
  {
     throw new UnsupportedOperationException(
        "This manager does not support logging.");
  }

  /**
   * The parent frame of the status dialog that is shown while executing the 
   * job, initialized in the constructor, may be <code>null</code> if it is not 
   * supplied. Never modified after construction.
   */
  protected Frame m_parent;

  /**
   * Dialog for displaying job progress. Gets constructed in <code>runJob()
   * </code> and disposed in {@link #onFinish()}. Never <code>null</code> once
   * constructed. 
   */
  protected PSDeploymentStatusDialog m_statusDialog = null;
  
  /**
   * The list of ids of the jobs to execute by this wizard manager in the  
   * order of execution, initialized to empty list and gets filled in 
   * <code>runJob()</code>. The job will be removed once the job is started.
   */
  protected List m_todoJobs = new ArrayList();
  
  /**
   * The plan that should be used by this wizard manager to display the wizard
   * dialogs, initialized in the constructor and never <code>null</code> or 
   * modified after that.
   */
  protected IPSDeploymentExecutionPlan m_plan;
  
  /**
   * The resource bundle containing the generic descriptions for the wizard
   * tasks used by all wizard dialogs.
   */
  protected static final PSResources ms_res = PSDeploymentClient.getResources();

}
