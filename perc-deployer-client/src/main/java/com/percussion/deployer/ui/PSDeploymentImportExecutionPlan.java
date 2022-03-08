/******************************************************************************
 *
 * [ PSDeploymentImportExecutionPlan.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.deployer.ui;

import com.percussion.deployer.objectstore.PSArchiveInfo;
import com.percussion.deployer.objectstore.PSDeployableElement;
import com.percussion.deployer.objectstore.PSImportDescriptor;
import com.percussion.deployer.objectstore.PSImportPackage;
import com.percussion.error.PSDeployException;

import java.awt.*;
import java.io.File;
import java.util.List;

/**
 * The plan for the import wizard manager. Defines the plan for installing the
 * packages in the archive as defined in import descriptor.
 */
public class PSDeploymentImportExecutionPlan extends PSDeploymentExecutionPlan
{
  /**
     * Constructs this plan with supplied archive and the deployment server. This
    * should be called when the archive to use and the deployment server on 
    * which the archive exists or on which it needs to be re-deployed. Makes the
    * starting step to display the 'Mode Selection Wizard' step.
    * 
    * @param parent the parent frame for the dialogs in this wizard plan, may be 
    * <code>null</code>
    * @param archiveInfo the archive to install, may not be <code>null</code> and 
    * must have archive detail.
    * @param deploymentServer the deployment server on which the archive should
    * be redeployed, may not be <code>null</code> and must be connected.
    * 
    * @throws IllegalArgumentException if any parameter is invalid.
    * @throws PSDeployException if an error happens getting archive info from 
    * supplied archive.
    */
   public PSDeploymentImportExecutionPlan(Frame parent, 
      PSArchiveInfo archiveInfo, PSDeploymentServer deploymentServer) 
      throws PSDeployException
   {
      super(parent, ms_totalSteps, ID_MODE_SELECTION_WIZARD);
      
      if(archiveInfo == null)
         throw new IllegalArgumentException("archiveInfo may not be null.");
      
      if(archiveInfo.getArchiveDetail() == null)
         throw new IllegalArgumentException("archiveInfo must have detail.");
      
      if(deploymentServer == null)
         throw new IllegalArgumentException(
            "deploymentServer may not be null.");
            
      if(!deploymentServer.isConnected())
         throw new IllegalArgumentException(
            "deploymentServer must be connected.");
            
      m_archiveInfo = archiveInfo;
      setDescriptor(new PSImportDescriptor(m_archiveInfo));
      setDeploymentServer(deploymentServer);
      m_isExistingArchive = true;
   }
   
   /**
     * Constructs this plan with supplied list of deployment servers. The plan
    * should be constructed using this, if the plan should execute from the
    * beginning.
    * 
    * @param parent the parent frame for the dialogs in this wizard, may be 
    * <code>null</code> 
    * @param servers the list of registered <code>PSDeploymentServer</code>s 
    * that should be shown in the target server selection wizard dialog, may not
    * be <code>null</code> or empty.
    * @param archiveFile the archive file to use for install, may be <code>null
    * </code>, if not <code>null</code> the file should exist.
    * 
    * @throws IllegalArgumentException <code>servers</code> is invalid.
    */
   public PSDeploymentImportExecutionPlan(Frame parent, List servers, 
      File archiveFile)
   {
      super(parent, ms_totalSteps, ID_WELCOME_WIZARD);
      
      if(servers == null || servers.isEmpty())
         throw new IllegalArgumentException(
            "servers may not be null or empty.");
      try 
      {
         servers.toArray(new PSDeploymentServer[0]);
      }
      catch(ArrayStoreException e)
      {
         throw new IllegalArgumentException(
            "the elements in the servers list must be " + 
            "instances of PSDeploymentServer");
      }
      
      if(archiveFile != null && !archiveFile.isFile())
         throw new IllegalArgumentException(
            "archiveFile must be a file and must exist.");
      
      m_servers = servers;
      m_archiveFile = archiveFile;
   }
   
   /**
    * Gets the current dialog to show. Updates the step to show with the next 
    * step in its plan. If the current dialog step is <code>ID_TRANSFORMS_WIZARD
    * </code> or <code>ID_VALIDATIONS_WIZARD</code>, checks whether this need to
    * be shown or not based on plan mode and validation results. The following 
    * describes the conditions when these dialog steps are shown. 
    * <ol>
    * <li>ID_TRANSFORMS_WIZARD - If the plan mode is 'Custom' and some/all 
    * packages included in the descriptor have some dependency objects that 
    * support id mapping or contain external dbms references that need to be 
    * transformed.</li>
    * <li>ID_TRANSFORMS_WIZARD - If the plan mode is 'Typical' and some/all 
    * packages included in the descriptor have some dependency objects that 
    * support id mapping or contain external dbms references that need to be 
    * mapped for transforming</li>
    * <li>ID_VALIDATIONS_WIZARD - If any package in the descriptor has warnings
    * or errors.</li>
    * </ol>
    * 
    * @param isNext <code>true</code> to get the next dialog, <code>false</code>
    * to go back and get the previous dialog.
    * 
    * @return the dialog, may be <code>null</code> if there are no more dialogs
    * to show in the plan.
    * 
    * @throws PSDeployException if there are any errors.
    */
   public PSDeploymentWizardDialog getDialog(boolean isNext) 
      throws PSDeployException
   {
      if (isNext)
         m_currentStep = getNextStep();
      else
         m_currentStep = getPreviousStep();

      /* Displays transforms step only if it has some id/dbms maps that 
       * needs to be edited. If not displaying this step, get the next step from 
       * its plan and get its corresponding dialog.
       * Displays validation step only if the validated import packages has some
       * validation results with either warnings or errors.
       * If going back, skip transforms and validation job steps
       */
      PSTransformsHandler transformsHandler = null;

      if (isNext)
      {
         if(m_currentStep == ID_TRANSFORMS_WIZARD)
         {
            transformsHandler = new PSTransformsHandler(m_deploymentServer, 
               m_archiveInfo.getServerName(), 
               m_archiveInfo.getRepositoryInfo());
            if(!hasTransformMaps(transformsHandler))
               m_currentStep = getNextStep();
         }         
         
         if(m_currentStep  == ID_VALIDATIONS_WIZARD)
         {
            //show the dialog only if it has validation results.
            if(!hasValidationResults())
               m_currentStep = getNextStep();
         }
      }
      else
      {
         if(m_currentStep  == ID_VALIDATIONS_WIZARD && !hasValidationResults())
            m_currentStep = getPreviousStep();
         
         if (m_currentStep == ID_VALIDATION_JOB_WIZARD)
            m_currentStep = getPreviousStep();
         
         if(m_currentStep == ID_TRANSFORMS_WIZARD)
         {
            transformsHandler = new PSTransformsHandler(m_deploymentServer, 
               m_archiveInfo.getServerName(), 
               m_archiveInfo.getRepositoryInfo());
            if(!hasTransformMaps(transformsHandler))            
               m_currentStep = getPreviousStep();            
         }         
      }

      PSDeploymentWizardDialog dlg = null;
      int sequence = getStepSequence(m_currentStep);
      switch(m_currentStep)
      {
         case ID_WELCOME_WIZARD:
            dlg = new PSDeploymentStepsDialog(m_parent, m_currentStep, sequence,
               PSDeploymentWizardDialog.TYPE_IMPORT);
            break;
            
         case ID_ARCHIVE_SELECTION_WIZARD:
            if(m_archiveFile != null)
            {
               dlg = new PSDeploymentArchiveSelectionDialog(
                  m_parent, m_currentStep, sequence, m_servers, m_archiveFile);
            }
            else
            {
               dlg = new PSDeploymentArchiveSelectionDialog(
                  m_parent, m_currentStep, sequence, m_servers);            
            }               
            break;
            
         case ID_MODE_SELECTION_WIZARD:
            dlg = new PSDeploymentModeSelectionDialog(m_parent, 
               m_currentStep, sequence, PSDeploymentWizardDialog.TYPE_IMPORT, m_archiveInfo.getArchiveDetail());
            break;
            
         case ID_TRANSFORMS_WIZARD:
            dlg = new PSDeploymentTransformsDialog(m_parent, m_currentStep, 
               sequence, isTypical(), transformsHandler, 
               m_archiveInfo.getArchiveDetail());
            break;
            
         case ID_VALIDATION_JOB_WIZARD:
            dlg = new PSDeploymentValidationJobDialog(
               m_parent, m_deploymentServer, m_currentStep, sequence);
            break;
            
         case ID_VALIDATIONS_WIZARD:
            dlg = new PSDeploymentDependencyValidationsDialog(
               m_parent, m_deploymentServer, m_currentStep, sequence);
            break;
            
         case ID_SUMMARY_WIZARD:
            dlg = new PSDeploymentInstallSummaryDialog(
               m_parent, m_deploymentServer, m_currentStep, sequence);
            break;               
      }

      if (dlg != null)
         dlg.setData(getData(m_currentStep));
      
      return dlg;
   }
   
   /**
    * Checks whether any package of the import descriptor has maps that needs 
    * to be shown to the user. In case of typical mode it checks for mappings
    * that are unmapped only, where as in custom mode it checks for all 
    * mappings.
    * 
    * @param handler the transforms handler to use to check the mappings, 
    * assumed not to be <code>null</code>
    * 
    * @return <code>true</code> if any one of the packages has maps, otherwise
    * <code>false</code>
    */
   private boolean hasTransformMaps(PSTransformsHandler handler)
   {
      PSImportDescriptor descriptor = (PSImportDescriptor)m_descriptor;
      List impPackages = descriptor.getImportPackageList();

      for (int i = 0; i < impPackages.size(); i++) 
      {
         PSDeployableElement pkg = 
            ((PSImportPackage)impPackages.get(i)).getPackage();   
         if( handler.getDBMSMappings(isTypical(), pkg, 
            m_archiveInfo.getArchiveDetail()).hasNext() ||
            (handler.needToMapIds() && 
            handler.getIDMappings(isTypical(), pkg).hasNext()) )
         {
            return true;
         }
      }
      
      return false;      
   }
   
   /**
    * Checks whether any package of the import descriptor has validation results
    * that need to be shown to the user.
    * 
    * @return <code>true</code> if it has any, otherwise <code>false</code>
    */
   private boolean hasValidationResults()
   {
      PSImportDescriptor descriptor = (PSImportDescriptor)m_descriptor;
      List impPackages = descriptor.getImportPackageList();

      for (int i = 0; i < impPackages.size(); i++) 
      {
         PSImportPackage pkg = 
            (PSImportPackage)impPackages.get(i);   
         if(pkg.getValidationResults() != null && 
            pkg.getValidationResults().getResults().hasNext())
         {
            return true;
         }            
      }      
      return false;      
   }
   
   /**
    * Updates the user settings from the supplied dialog. 
    * The following table describes the settings updated for each step.
    * 
    * <table border=1>
    * <tr><th>Step</th><th>Settings updated</th></tr>
    * <tr><td>ID_WELCOME_WIZARD</td><td>None</td></tr>
    * <tr><td>ID_ARCHIVE_SELECTION_WIZARD</td><td>the deployment server (<code>
    * m_deploymentServer</code>), the archive info from the selected archive and
    * the import descriptor (<code>m_descriptor</code>)</td></tr>
    * <tr><td>ID_MODE_SELECTION_WIZARD</td><td>plan mode</td></tr>
    * <tr><td>ID_PACKAGE_SELECTION_WIZARD</td><td>None(As the dialog updates the
    * given descriptor)</td></tr>
    * <tr><td>ID_TRANSFORMS_WIZARD</td><td>None(As the dialog updates the server
    * with transforms)</td></tr>
    * <tr><td>ID_VALIDATION_JOB_WIZARD</td><td>None(As the dialog updates the 
    * given descriptor)</td></tr>
    * <tr><td>ID_VALIDATIONS_WIZARD</td><td>None(As the dialog updates the given
    * descriptor)</td></tr>
    * <tr><td>ID_SUMMARY_WIZARD</td><td>None</td></tr>
    * </table>
    * 
    * @param dialog the dialog that is currently shown, may not be
    * <code>null</code>
    * 
    * @throws IllegalArgumentException if dialog is <code>null</code>
    * @throws PSDeployException if there are any errors.
    */
   public void updateUserSettings(PSDeploymentWizardDialog dialog) 
      throws PSDeployException
   {
      int step = dialog.getStep();
      
      // store any data
      setData(step, dialog.getDataToSave());
      
      if (!dialog.shouldUpdateUserSettings())
         return;
      
      switch(step)
      {
         case ID_ARCHIVE_SELECTION_WIZARD:
            PSDeploymentArchiveSelectionDialog dlg = 
               (PSDeploymentArchiveSelectionDialog)dialog;
            m_archiveFile = dlg.getArchiveFile();
            m_archiveInfo = dlg.getArchiveInfo();
            setDeploymentServer(dlg.getDeploymentServer());
            PSImportDescriptor importDesc = 
               (PSImportDescriptor)dlg.getDescriptor();
            // instance of archive info in the descriptor may be a copy, so
            // update the archive ref in case it was changed.
            importDesc.getArchiveInfo().setArchiveRef(
               m_archiveInfo.getArchiveRef());
            setDescriptor(importDesc);
            m_isOverWrite = dlg.isOverWrite();
            break;
            
         case ID_MODE_SELECTION_WIZARD:
            setIsTypical(
               ((PSDeploymentModeSelectionDialog)dialog).isTypicalMode() );
            break;
            
         default:
            break;
      }
   }
   
   /**
    * Finds whether the archive to install exists on the server or not. Useful 
    * to determine whether archive need to be copied to server or not at the end
    * of import process.
    * 
    * @return <code>true</code> if the archive exists on the server, otherwise
    * <code>false</code>
    */
   public boolean isExistingArchive()
   {
      return m_isExistingArchive;
   }
   
   /**
    * Determines if the installation should overwrite the existing archive file
    * and logs on the server.
    * 
    * @return <code>true</code> to overwrite, <code>false</code> otherwise.
    */
   public boolean isOverWrite()
   {
      return m_isOverWrite;
   }
   
   /**
    * Gets the archive file on the client to copy to the server. Should be 
    * called only if the {@link #isExistingArchive()} returns <code>false</code>.
    * 
    * @return the archive file, may be <code>null</code> if the user installing
    * from an archive that exists on server, if not <code>null</code> this file
    * exists.
    */
   public File getArchiveFile()
   {
      return m_archiveFile;
   }
   
   /**
    * Gets the archive reference name for the archive file that needs to be 
    * installed. 
    * 
    * @return the archive ref, may be <code>null</code> until the archive is 
    * selected by the user in <code>ID_ARCHIVE_SELECTION_WIZARD</code>, never
    * empty.
    */
   public String getArchiveRef()
   {
      if(m_archiveInfo != null)
         return m_archiveInfo.getArchiveRef();
      
      return null;
   }
   
   /**
    * The list of registered servers to show in <code>
    * ID_ARCHIVE_SELECTION_WIZARD</code> dialog, initialized in {@link 
    * #PSDeploymentImportExecutionPlan(Frame, List, File)}, may be 
    * <code>null</code> if this object is constructed from {@link 
    * #PSDeploymentImportExecutionPlan(Frame, PSArchiveInfo, PSDeploymentServer)
    * }. Never modified after initialization.
    */
   protected List m_servers = null;
   
   /**
    * The flag to identify whether the archive used to install exists on server
    * or not. Initialized to <code>false</code> and set to <code>true</code> if
    * this object is constructed from {@link 
    * #PSDeploymentImportExecutionPlan(Frame, PSArchiveInfo, PSDeploymentServer)}. 
    */
   private boolean m_isExistingArchive = false;
   
   /**
    * The archive file on the client that needs to be copied to the server to
    * install, initialized to <code>null</code> and modified either in the 
    * {@link #PSDeploymentImportExecutionPlan(Frame, List, File) constructor} or
    * based on the user selection in <code>ID_ARCHIVE_SELECTION_WIZARD</code> 
    * dialog by a call to <code>updateUserSettings(PSDeploymentWizardDialog)
    * </code>. May be <code>null</code> if archive already exists on server.
    */
   private File m_archiveFile = null;
   
   /**
    * The archive info with the detail that is used to install, initialized to
    * <code>null</code> and modified either in the {@link 
    * #PSDeploymentImportExecutionPlan(Frame, PSArchiveInfo, PSDeploymentServer)
    * } or based on the user selection in <code>ID_ARCHIVE_SELECTION_WIZARD
    * </code> dialog by a call to <code>
    * updateUserSettings(PSDeploymentWizardDialog)</code>.
    */
   private PSArchiveInfo m_archiveInfo = null;
   
   /**
    * Determines if the installation will overwrite an existing archive on the
    * server.  Initially <code>false</code>, modified by 
    * {@link #updateUserSettings(PSDeploymentWizardDialog)}.
    */
   private boolean m_isOverWrite = false;   
   
   /**
    * The constant to identify the dialog which describes the steps to follow
    * in this wizard plan.
    */
   public static final int ID_WELCOME_WIZARD = 1;
   
   /**
    * The constant to identify the archive and server selection dialog step.
    */
   public static final int ID_ARCHIVE_SELECTION_WIZARD = 2;
   
   /**
    * The constant to identify the mode selection dialog step.
    */
   public static final int ID_MODE_SELECTION_WIZARD = 3;
   
   /**
    * The constant to identify the transforms dialog step.
    */
   public static final int ID_TRANSFORMS_WIZARD = 4;
   
   /**
    * The constant to identify the validations dialog step.
    */
   public static final int ID_VALIDATIONS_WIZARD = 5;
   
   /**
    * The constant to identify summary dialog step.
    */
   public static final int ID_SUMMARY_WIZARD = 6;
   
   /**
    * The constant to indicate validation job status dialog.
    */
   public static final int ID_VALIDATION_JOB_WIZARD = 7;
   
   /**
    * The array of the import wizard step identifiers in the order they need to 
    * be displayed. The steps may be skipped depending on the user selections in
    * the wizard dialogs. 
    */
   private static final int[] ms_totalSteps = {ID_WELCOME_WIZARD, 
      ID_ARCHIVE_SELECTION_WIZARD, ID_MODE_SELECTION_WIZARD, 
      ID_TRANSFORMS_WIZARD, 
      ID_VALIDATION_JOB_WIZARD, ID_VALIDATIONS_WIZARD, ID_SUMMARY_WIZARD}; 
}
