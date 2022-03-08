/******************************************************************************
 *
 * [ PSDeploymentExecutionPlan.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.deployer.ui;

import com.percussion.deployer.objectstore.PSDescriptor;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * The base class for deployment wizard execution plans. Provides the basic 
 * functionality for any plan.
 */
public abstract class PSDeploymentExecutionPlan implements 
   IPSDeploymentExecutionPlan
{
   /**
     * Constructs the execution plan. By default the plan mode is typical. Call 
    * {@link #setIsTypical(boolean) setIsTypical(false)} for changing it to 
    * custom mode.
    * 
    * @param parent the parent frame for the dialogs in this wizard plan, may be 
    * <code>null</code>.
    * @param steps The list of step IDs for this plan.        
    * @param startStep the start step of the plan.
    * 
    * @throws IllegalArgumentException if any parameter is invalid.
    */
   public PSDeploymentExecutionPlan(Frame parent,  int[] steps, int startStep)
   {
      if(steps == null || steps.length == 0)
         throw new IllegalArgumentException("steps may not be null or empty.");

      m_parent = parent;                     
      m_steps = steps;
      m_firstStep = startStep;
      m_data = new HashMap(m_steps.length);
   }
   
   /**
    * Sets the descriptor to work with in the plan.
    * 
    * @param descriptor the descriptor to work with, either import or export 
    * descriptor, derived classes should check for correct instance, may not be
    * <code>null</code>
    * 
    * @throws IllegalArgumentException if descriptor is <code>null</code>
    */
   public void setDescriptor(PSDescriptor descriptor)
   {
      if(descriptor == null)
         throw new IllegalArgumentException("descriptor may not be null.");
         
      m_descriptor = descriptor;
   }
   
   //implements interface method
   public PSDescriptor getDescriptor()
   {
      return m_descriptor;
   }
   
   /**
    * Sets the deployment server on which this plan should work on.
    * 
    * @param deploymentServer the deployment server on which this plan should
    * work on, may not be <code>null</code> and must be connected.
    */
   public void setDeploymentServer(PSDeploymentServer deploymentServer)
   {
      if(deploymentServer == null)
         throw new IllegalArgumentException(
            "deploymentServer may not be null.");
            
      if(!deploymentServer.isConnected())
         throw new IllegalArgumentException(
            "deploymentServer must be connected.");
         
      m_deploymentServer = deploymentServer;
   }
   
   /**
    * Gets the deployment server set with this plan.
    * 
    * @return the deployment server, may be <code>null</code> if it is not set.
    */
   public PSDeploymentServer getDeploymentServer()
   {
      return m_deploymentServer;
   }
   
   //implements interface method
   public void setIsTypical(boolean mode)
   {
      m_isTypical = mode;
   }
   
   //implements interface method
   public boolean isTypical()
   {
      return m_isTypical;
   }
   
   /**
    * Gets the next step id based on the current step id.
    * 
    * @return the next step id, may be <code>-1</code> if the current step is 
    * the last step.
    */
   protected int getNextStep()
   {
      if (m_currentStep == -1)
         return m_firstStep;
      
      for(int i = 0; i < m_steps.length; i++) 
      {
         if(m_steps[i] == m_currentStep)
         {
            int nextIndex = ++i;
            if(nextIndex == m_steps.length)
               return -1;
            else
               return m_steps[nextIndex];
         }                  
      }
      
      return -1; //this will not arise at any time
   }

   /**
    * Gets the previous step id based on the current step id.
    * 
    * @return the previous step id, may be <code>-1</code> if the current step 
    * is the first step.
    */
   protected int getPreviousStep()
   {
      if (m_currentStep == m_firstStep)
         return -1;
      
      for(int i = 0; i < m_steps.length; i++) 
      {
         if(m_steps[i] == m_currentStep)
         {
            int prevIndex = --i;
            if(prevIndex < 0)
               return -1;
            else
               return m_steps[prevIndex];
         }                  
      }
      
      return -1; //this will not arise at any time
      
   }
   
   /**
    * Determine if the supplied step is the last step in this plan.
    * 
    * @param step the step id to check
    * 
    * @return <code>false</code> if it is not the last step or a step in this 
    * plan, otherwise <code>false</code>. 
    */
   protected boolean isLastStep(int step)
   {
      return step == m_steps[m_steps.length - 1];
   }

   /**
    * Determine if the supplied step is the first step in this plan or not.
    * 
    * @param step the step id to check
    * 
    * @return <code>false</code> if it is not the first step or a step in this 
    * plan, otherwise <code>false</code>. 
    */
   protected boolean isFirstStep(int step)
   {
      return step == m_firstStep;
   }

   /**
    * Determine the sequence of the supplied step
    * 
    * @param step The step to check.
    * 
    * @return One of the <code>PSDeploymentWizardDialog.SEQUENCE_XXX</code>
    * values.
    */
   protected int getStepSequence(int step)
   {
      int seq = PSDeploymentWizardDialog.SEQUENCE_MID;
      if (isFirstStep(step))
         seq = PSDeploymentWizardDialog.SEQUENCE_FIRST;
      else if (isLastStep(step))
         seq = PSDeploymentWizardDialog.SEQUENCE_LAST;
      
      return seq;
   }
   
   /**
    * Store temp data for the specified step.
    * 
    * @param step The step to store data for.
    * @param data The data, may be <code>null</code>.
    */
   protected void setData(int step, Object data)
   {
      m_data.put(new Integer(step), data);
   }
   
   /**
    * Get any temp data stored for the supplied step.
    * 
    * @param step The step to retrieve data for.
    * 
    * @return The data, may be <code>null</code>.
    */
   protected Object getData(int step)
   {
      return m_data.get(new Integer(step));
   }
   
   /**
    * The parent frame of the dialogs that are shown as part of this plan, 
    * initialized in the constructor, may be <code>null</code> if it is not 
    * supplied. Never modified after construction.
    */
   protected Frame m_parent;
   
   /**
    * The array of step ids that needs to be executed in its order, initialized
    * in the constructor and never <code>null</code> or modified after that.
    */
   private int[] m_steps;   
   
   /**
    * The descriptor that defines the user selections in the dialogs of this 
    * plan, <code>null</code> until first call to <code>
    * setDescriptor(PSDescriptor)</code> and never <code>null</code> after that.
    * May be modified in the <code>updateUserSettings(PSDeploymentWizardDialog)
    * </code> as the dialogs modify the descriptors.
    */
   protected PSDescriptor m_descriptor = null;
  
   /**
    * The deployment server that needs to be connected to make requests. 
    * <code>null</code> until first call to <code> 
    * setDeploymentServer(PSDeploymentServer)</code> and never <code>null</code>
    * after that.
    */
   protected PSDeploymentServer m_deploymentServer = null;
  
   /**
    * The current step the plan has to show, initialized to -1 
    * and gets modified as the plan gets executed.
    */
   protected int m_currentStep = -1;

   /**
    * The mode setting for this plan, initialized to <code>true</code> 
    * and may be modified through <code>setIsTypical(boolean)</code>. If
    * <code>true</code> the plan mode is typical, otherwise custom. Useful to
    * figure out the next steps for the derived classes if it wants to behave
    * differently in each mode.
    */
   private boolean m_isTypical = true;
  
   /**
    * The first step in the plan, initialized in the constructor, never
    * modified after that.
    */
   private int m_firstStep;
   
   /**
    * Temporary data store for each step, key is the step ID as an 
    * <code>Integer</code>, value is the data as an <code>Object</code>.  
    * Initalized in the contructor, never <code>null</code> after that, 
    * populated in <code>updateUserSettings</code>, may contain 
    * <code>null</code> values. 
    */
   private Map m_data;
}
