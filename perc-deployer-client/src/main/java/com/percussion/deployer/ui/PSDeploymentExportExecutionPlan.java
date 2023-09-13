/******************************************************************************
 *
 * [ PSDeploymentExportExecutionPlan.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.deployer.ui;

import com.percussion.deployer.objectstore.PSDeployableElement;
import com.percussion.deployer.objectstore.PSExportDescriptor;
import com.percussion.error.PSDeployException;
import com.percussion.util.PSEntrySet;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * The plan for the export wizard manager. Defines the plan for creating archive
 * or descriptor.
 */
public class PSDeploymentExportExecutionPlan extends PSDeploymentExecutionPlan
{
   /**
    * Constructs this plan with supplied export descriptor and the deployment
    * server. This should be called when the descriptor to use and the 
    * deployment server on which the archive/descriptor needs to be 
    * created/updated is already known. Makes the starting step to display 
    * the 'Mode Selection Wizard' step.
    * 
    * @param parent the parent frame for the dialogs in this wizard plan, may be 
    * <code>null</code>
    * @param descriptor the export descriptor to be updated or used to create
    * archive, may not be <code>null</code>
    * @param locator the locator used to load the descriptor if it was retrieved
    * from the server.  May be <code>null</code> if the descriptor was not 
    * loaded from the server. 
    * @param deploymentServer the deployment server on which the archive should
    * be created, may not be <code>null</code> and must be connected.
    * 
    * @throws IllegalArgumentException if any parameter is invalid.
    */
   public PSDeploymentExportExecutionPlan(Frame parent, 
      PSExportDescriptor descriptor, PSDescriptorLocator locator, 
      PSDeploymentServer deploymentServer)
   {
      super(parent, ms_totalSteps, ID_MODE_SELECTION_WIZARD);
      
      if(descriptor == null)
         throw new IllegalArgumentException("descriptor may not be null.");
      
      if(deploymentServer == null)
         throw new IllegalArgumentException(
            "deploymentServer may not be null.");
            
      if(!deploymentServer.isConnected())
         throw new IllegalArgumentException(
            "deploymentServer must be connected.");
            
      setDescriptor(descriptor);
      setDeploymentServer(deploymentServer);
      m_descriptorLocator = locator;      
   }


   /**
    * Constructs this plan with supplied list of deployment servers. The plan
    * should be constructed using this, if the plan should execute from the
    * beginning.
    * 
    * @param parent the parent frame for the dialogs in this wizard, may be 
    * <code>null</code> 
    * @param servers the list of registered <code>PSDeploymentServer</code>s 
    * that should be shown in the server selection wizard dialog, may not
    * be <code>null</code> or empty.
    * 
    * @throws IllegalArgumentException <code>servers</code> is invalid.
    */
   public PSDeploymentExportExecutionPlan(Frame parent, List servers)
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
      
      int archiveType = PSExportDescriptor.ARCHIVE_TYPE_NORMAL;
      if (PSDeploymentClient.isSupportMode())
         archiveType = PSExportDescriptor.ARCHIVE_TYPE_SUPPORT;
      else if (PSDeploymentClient.isSampleMode())
         archiveType = PSExportDescriptor.ARCHIVE_TYPE_SAMPLE;

      setDescriptor(new PSExportDescriptor("temp", archiveType));
      m_servers = servers;
   }
   
   /**
    * Gets the current dialog to show. Updates the step to show with the next 
    * step in its plan. If the current dialog step is <code>
    * ID_TYPES_SELECTION_WIZARD</code>, checks whether this need to be shown or
    * not based on plan mode and user selections. The following describes the
    * conditions when this dialog step is shown. 
    * <ol>
    * <li>If the plan mode is 'Custom' and some/all elements included in the 
    * descriptor have some dependency applications which have literal ids</li>
    * <li>If the plan mode is 'Typical' and some/all elements included in the 
    * descriptor have some dependency applications which have literal ids and 
    * need to be identified with their types</li>
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
      
      /* Displays id types selection step only if it has some id types that need
       * to be edited. If not displaying this step, get the next step from its 
       * plan and get its corresponding dialog.
       */
      List elementIDTypes = null;
      if(m_currentStep == ID_TYPES_SELECTION_WIZARD)
      {
         // if going back, skip over this, else if going forward skip if there
         // are no ID types to display.
         if (!isNext)
            m_currentStep = getPreviousStep();
         else
         {
            elementIDTypes = getElementIDTypes();
            if(elementIDTypes.isEmpty())
               m_currentStep = getNextStep();
         }
      }
            
      PSDeploymentWizardDialog dlg = null;
      int sequence = getStepSequence(m_currentStep);
      switch(m_currentStep)
      {
         case ID_WELCOME_WIZARD:
            dlg = new PSDeploymentStepsDialog(m_parent, m_currentStep, sequence,
               PSDeploymentWizardDialog.TYPE_EXPORT);
            break;
            
         case ID_SERVER_SELECTION_WIZARD:
            dlg = new PSDeploymentServerSelectionDialog(
               m_parent, m_currentStep, sequence, m_servers);
            break;
            
         case ID_MODE_SELECTION_WIZARD:
            dlg = new PSDeploymentModeSelectionDialog(m_parent, 
               m_currentStep, sequence, PSDeploymentWizardDialog.TYPE_EXPORT);
            break;
            
         case ID_ELEMENT_SELECTION_WIZARD:
            dlg = new PSDeploymentElementSelectionDialog(m_parent, 
               m_deploymentServer, m_currentStep, sequence);
            break;
            
         case ID_TYPES_SELECTION_WIZARD:
            dlg = new PSDeploymentIDTypesDialog(m_parent, 
               m_deploymentServer, m_currentStep, sequence, isTypical(), 
               elementIDTypes, m_descriptorLocator);         
            break;
            
         case ID_DEPENDENCY_SELECTION_WIZARD:
            dlg = new PSDeploymentElementDependencyDialog(m_parent, 
               m_deploymentServer, m_currentStep, sequence, isTypical());
            break;
            
         case ID_ARCHIVE_SELECTION_WIZARD:
            dlg = new PSDeploymentCreateArchiveDialog(m_parent, 
               m_deploymentServer, m_currentStep, sequence, 
               isExistingDescriptor());
            break;               
      }
      
      if (dlg != null)
         dlg.setData(getData(m_currentStep));
      
      return dlg;
   }
   
   /**
    * Updates the user settings from the supplied dialog. 
    * The following table describes the settings updated for each step.
    * 
    * <table border=1>
    * <tr><th>Step</th><th>Settings updated</th></tr>
    * <tr><td>ID_WELCOME_WIZARD</td><td>None</td></tr>
    * <tr><td>ID_SERVER_SELECTION_WIZARD</td><td>the deployment server (<code>
    * m_deploymentServer</code>), descriptor locator (<code>
    * m_descriptorLocator</code>) and the export descriptor (<code>
    * m_descriptor</code>) if user chose to use existing descriptor</td></tr>
    * <tr><td>ID_MODE_SELECTION_WIZARD</td><td>plan mode</td></tr>
    * <tr><td>ID_ELEMENT_SELECTION_WIZARD</td><td>None(As the dialog updates the
    * given descriptor)</td></tr>
    * <tr><td>ID_TYPES_SELECTION_WIZARD</td><td>None(As the dialog updates the
    * given descriptor)</td></tr>
    * <tr><td>ID_DEPENDENCY_SELECTION_WIZARD</td><td>None(As the dialog updates 
    * the given descriptor)</td></tr>
    * <tr><td>ID_ARCHIVE_SELECTION_WIZARD</td><td>isCreateArchive flag (<code>
    * m_createArchive</code>) and Archive File (m_archiveFile)</td></tr>
    * <tr><td>ID_TYPES_SELECTION_WIZARD</td><td>the export descriptor (<code>
    * m_descriptor</code>) if the export descriptor was reloaded to reflect
    * id type changes</td></tr>
    * </table>
    * 
    * @param dialog the dialog that is currently shown, may not be
    * <code>null</code>
    * 
    * @throws IllegalArgumentException if dialog is <code>null</code>
    */
   public void updateUserSettings(PSDeploymentWizardDialog dialog)
   {
      int step = dialog.getStep();

      // store any data
      setData(step, dialog.getDataToSave());
      
      if (!dialog.shouldUpdateUserSettings())
         return;
      
      switch(step)
      {
         case ID_SERVER_SELECTION_WIZARD:
            PSDeploymentServerSelectionDialog dlg = 
               (PSDeploymentServerSelectionDialog)dialog;
            m_descriptorLocator = dlg.getDescriptorLocator();
            setDeploymentServer(dlg.getDeploymentServer());
            setDescriptor(dlg.getDescriptor());
            break;
            
         case ID_MODE_SELECTION_WIZARD:
            setIsTypical(
               ((PSDeploymentModeSelectionDialog)dialog).isTypicalMode() );
            break;

         case ID_ARCHIVE_SELECTION_WIZARD:
            PSDeploymentCreateArchiveDialog createDialog = 
               (PSDeploymentCreateArchiveDialog)dialog;
            m_isCreateArchive = createDialog.isCreateArchive();
            if(m_isCreateArchive)
               m_archiveFile = createDialog.getArchiveFile();
            break;
            
         case ID_TYPES_SELECTION_WIZARD:
            if (m_descriptorLocator != null)
               setDescriptor(dialog.getDescriptor());
            break;
               
         default:
            break;
      }
   }
   
   /**
    * Gets the user selection for creating archive in this plan execution. 
    * Should be called to determine whether create archive job need to be 
    * initiated or not.
    * 
    * @return <code>true</code> if user chosen to create archive, otherwise 
    * <code>false</code>
    */
   public boolean isCreateArchive()
   {
      return m_isCreateArchive;
   }
   
   /**
    * Gets the archive file to use to save the archive created on the server.
    * Should be called only if the {@link #isCreateArchive()} returns <code>true
    * </code>
    * 
    * @return the archive file, may be <code>null</code> if the user did not 
    * choose to create archive in the plan execution.
    */
   public File getArchiveFile()
   {
      return m_archiveFile;
   }

   /**
    * Gets the id types for local dependencies of each deployable element in the
    * export descriptor based on the plan mode. Loads the dependencies for the 
    * deployable element if it is not loaded with them yet.
    * 
    * @return A list of <code>Map.Entry</code> with 
    * <code>PSDeployableElement</code> as key and <code>Iterator</code> of 
    * <code>PSApplicationIDTypes</code> of local dependencies
    * of the element as value, never <code>null</code>. The following table 
    * describes the return value based on different conditions.
    * <table border=1>
    * <tr><th>Plan Mode</th><th>Condition</th><th>Returned <code>Map</code></th>
    * </tr>
    * <tr><td>Typical</td><td>None of the element's local dependencies supports
    * id types</td><td>empty</td></tr>
    * <tr><td>Typical</td><td>Some/All of the element's local dependencies 
    * support id types but all of them are identified</td><td>empty</td></tr>
    * <tr><td>Typical</td><td>Some/All of the element's local dependencies 
    * support id types but only some of them are identified</td><td>Contain 
    * entries for the deployable elements that have local dependencies with 
    * unidentified literal ids with value <code>Iterator</code> containing only
    * incomplete <code>PSApplicationIDTypes</code> entries</td></tr>
    * <tr><td>Custom</td><td>None of the elements (its local dependencies)
    * supports id types</td><td>empty</td></tr>
    * <tr><td>Custom</td><td>Some/All of the element's local dependencies 
    * support id types but some/all of them are identified</td><td>Contain  
    * entries for each deployable element. The value <code>Iterator</code> might 
    * be empty if none of the local dependencies of the element support id types
    * </td></tr>
    * </table>
    * 
    * @throws PSDeployException if error happens loading the dependencies or
    * getting the id types from the server.
    */
   private List getElementIDTypes() throws PSDeployException
   {
      List elementIDTypes = new ArrayList();
      boolean hasIdTypes = false;

      //Find all dependencies that supports id types.
      PSExportDescriptor descriptor = (PSExportDescriptor)m_descriptor;
      Iterator elements = descriptor.getPackages();
      while(elements.hasNext())
      {
         PSDeployableElement element = (PSDeployableElement)elements.next();         
         Iterator idTypes = PSDependencyHelper.getIDTypes(element, 
            m_deploymentServer, isTypical());
            
         // remember if we put a non-emtpy iterator into the list.  Put empty
         // iterators in only if not in typical mode
         Map.Entry entry = new PSEntrySet(element, idTypes);
         if(idTypes.hasNext())
         {
            hasIdTypes = true;
            elementIDTypes.add(entry);
         }
         else if (!isTypical())
         {
            elementIDTypes.add(entry);
         }
      }
      
      // now only return the list if we actually had any idtypes
      if (!hasIdTypes)
         elementIDTypes = new ArrayList();
         
      return elementIDTypes;
   }
   
   
   /**
    * Determines if the archive was created from a saved export descriptor.
    * 
    * @return <code>true</code> if it is, <code>false</code> otherwise.
    */
   private boolean isExistingDescriptor()
   {
      return (m_descriptorLocator != null && 
         m_descriptorLocator.isSavedExportDescriptor());
   }
   
   /**
    * The list of registered servers to show in <code>ID_SERVER_SELECTION_WIZARD
    * </code> dialog, initialized in {@link 
    * #PSDeploymentExportExecutionPlan(Frame, List)}, may be 
    * <code>null</code> if this object is constructed from {@link 
    * #PSDeploymentExportExecutionPlan(Frame, PSExportDescriptor, 
    * PSDescriptorLocator, PSDeploymentServer)}. Never modified after 
    * initialization.
    */
   protected List m_servers = null;
   
   /**
    * The flag to identify whether user chose to create archive or not. 
    * Initialized to <code>false</code> and modified based on user setting in 
    * the <code>ID_ARCHIVE_SELECTION_WIZARD</code> dialog by a call to <code>
    * updateUserSettings</code>. 
    */
   private boolean m_isCreateArchive = false;
   
   /**
    * The archive file to which the created archive needs to be saved, 
    * initialized to <code>null</code> and modified based on user selection in 
    * the <code>ID_ARCHIVE_SELECTION_WIZARD</code> dialog by a call to <code>
    * updateUserSettings</code>. If the user does not choose to create archive
    * this may be <code>null</code>. 
    */
   private File m_archiveFile = null;
   
   /**
    * The descriptor locator used to load an existing descriptor from a saved
    * source descriptor or from an installed archive.  Initialized during
    * construction, may be <code>null</code> if creating a new descriptor, never
    * modified after that.
    */
   private PSDescriptorLocator m_descriptorLocator;
   
   /**
    * The constant to identify the dialog which describes the steps to follow
    * in this wizard plan.
    */
   public static final int ID_WELCOME_WIZARD = 1;
   
   /**
    * The constant to identify the server and descriptor selection dialog step.
    */
   public static final int ID_SERVER_SELECTION_WIZARD = 2;
   
   /**
    * The constant to identify the mode selection dialog step.
    */
   public static final int ID_MODE_SELECTION_WIZARD = 3;
   
   /**
    * The constant to identify the element selection dialog step.
    */
   public static final int ID_ELEMENT_SELECTION_WIZARD = 4;
   
   /**
    * The constant to identify the literal id type identification dialog step.
    */
   public static final int ID_TYPES_SELECTION_WIZARD = 5;
   
   /**
    * The constant to identify the dependency selection dialog step.
    */
   public static final int ID_DEPENDENCY_SELECTION_WIZARD = 6;
   
   /**
    * The constant to identify archive selection dialog step.
    */
   public static final int ID_ARCHIVE_SELECTION_WIZARD = 7;
   
   /**
    * The array of the export wizard steps identifiers in the order they need to 
    * be displayed. The steps may be skipped depending on the user selections in
    * the wizard dialogs. 
    */
   private static final int[] ms_totalSteps = {ID_WELCOME_WIZARD, 
      ID_SERVER_SELECTION_WIZARD, ID_MODE_SELECTION_WIZARD, 
      ID_ELEMENT_SELECTION_WIZARD, ID_TYPES_SELECTION_WIZARD, 
      ID_DEPENDENCY_SELECTION_WIZARD, ID_ARCHIVE_SELECTION_WIZARD}; 
}
