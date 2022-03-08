/******************************************************************************
 *
 * [ IPSDeploymentExecutionPlan.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.deployer.ui;

import com.percussion.deployer.objectstore.PSDescriptor;
import com.percussion.error.PSDeployException;

/**
 * Defines the deployment execution plan interface to set the plan mode, get the
 * current dialog to display in the plan and update the plan with user settings
 * from the dialog.
 */
public interface IPSDeploymentExecutionPlan
{
   /**
    * Sets the mode of this plan.
    * 
    * @param mode <code>true</code> to signify 'Typical' mode, <code>false
    * </code> to signify 'Custom' mode.
    */
   public void setIsTypical(boolean mode);
   
   /**
    * Checks whether the plan mode is 'Typical'.
    * 
    * @return <code>true</code> if it is typical, otherwise <code>false</code>
    */
   public boolean isTypical();

   /**
    * Gets the dialog to show from its plan. The owner of the plan should 
    * call this method in loop to show the dialogs in the plan according to its
    * order.
    * 
    * @param isNext <code>true</code> to get the next dialog, <code>false</code>
    * to go back and get the previous dialog.
    * 
    * @return the dialog to show, may be <code>null</code> if it has no more
    * dialogs to show.
    * @throws PSDeployException if an error happens.
    */
   public PSDeploymentWizardDialog getDialog(boolean isNext) 
      throws PSDeployException;
   
   /**
    * Updates the user settings from the supplied dialog. 
    * 
    * @param dialog the dialog corresponding to a step in this plan that is 
    * shown to user, may not be <code>null</code>.
    * 
    * @throws IllegalArgumentException if dialog is <code>null</code> or if the
    * dialog step is not a recoginised step of this plan.
    * @throws PSDeployException if an error happens updating the settings.
    */
   public void updateUserSettings(PSDeploymentWizardDialog dialog) 
      throws PSDeployException;
   
   /**
    * Gets the descriptor that is used in the plan.
    * 
    * @return the descriptor, may be <code>null</code> if it is not yet set in
    * the plan.
    */
   public PSDescriptor getDescriptor();
   
}
