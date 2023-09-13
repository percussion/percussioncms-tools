/******************************************************************************
 *
 * [ IPSJobManager.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.deployer.ui;

/**
 * The interface to be implemented by the wizard managers to use the 
 * <code>PSDeploymentStatusDialog</code> for job progress.
 */
public interface IPSJobManager 
{
   /**
    * Notification event for the job initiated by manager is completed.
    */
   public void jobCompleted();
   
   /**
    * Checks whether the manager supports any log view for the job. 
    * 
    * @return <code>true</code> if it supports, otherwise <code>false</code>
    */
   public boolean supportsLog();
   
   /**
    * Displays the log view dialog for the currently managing job.
    * 
    * @throws UnsupportedOperationException if the manager does not support the
    * log view for the job.
    */
   public void viewLog();
}
