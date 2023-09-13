/******************************************************************************
 *
 * [ PSWizardBase.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.wizards;

import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSCoreUtils;
import com.percussion.client.PSDuplicateNameException;
import com.percussion.client.PSModelException;
import com.percussion.client.PSObjectType;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.models.IPSCmsModel;
import com.percussion.client.models.IPSObjectDefaulter;
import com.percussion.services.security.IPSAcl;
import com.percussion.services.security.IPSAclEntry;
import com.percussion.services.security.PSAclUtils;
import com.percussion.services.security.PSPermissions;
import com.percussion.services.security.PSTypedPrincipal;
import com.percussion.security.IPSTypedPrincipal.PrincipalTypes;
import com.percussion.workbench.ui.IPSEditorFactory;
import com.percussion.workbench.ui.PSEditorRegistry;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.PSModelTracker;
import com.percussion.workbench.ui.PSUiReference;
import com.percussion.workbench.ui.PSWorkbenchPlugin;
import com.percussion.workbench.ui.controls.PSSlushBucket;
import com.percussion.workbench.ui.util.PSControlInfo;
import com.percussion.workbench.ui.util.PSErrorDialog;
import com.percussion.workbench.ui.util.PSUiUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.wizards.IWizardDescriptor;

import java.security.acl.NotOwnerException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

/**
 * This abstract class takes care of most of the functionality needed for
 * a wizard. The only thing that a subclass needs to do is to implement
 * <code>addPages()</code> and <code>getObjectType()</code>.
 * @author erikserating
 *
 */
public abstract class PSWizardBase extends Wizard implements INewWizard
{
   /**
    * Ctor.
    * 
    * @param type The object that the derived wizard is creating. Never
    * <code>null</code>.
    */
   protected PSWizardBase(PSObjectType type)
   {
      // Get the associated model for this wizard
      PSCoreFactory factory = PSCoreFactory.getInstance();
      m_objectType = type;
      try
      {
         m_model = 
            factory.getModel(m_objectType.getPrimaryType());
      }
      catch (PSModelException e)
      {
         PSWorkbenchPlugin.handleException(null, null, null, e);
         throw new RuntimeException(e);
      }
   }

   /**
    * Saves the supplied params for possible later use.
    * @inheritDoc
    */
   public void init(IWorkbench workbench, IStructuredSelection selection)
   {    
      // TODO use selection to pre-load wizard
      m_selection = selection;
      m_workbenchPage = workbench.getActiveWorkbenchWindow().getActivePage();
      if (m_workbenchPage == null)
         //should never happen
         throw new RuntimeException("Null page not supported."); //$NON-NLS-1$
   }

   /* 
    * @see org.eclipse.jface.wizard.Wizard#performFinish()
    */ 
   @Override
   public boolean performFinish()
   {
      final String name = findObjectName();
      if(name == null || name.trim().length() == 0)
      {
         String msg = 
            PSMessages.getString("PSWizardBase.error.no.name"); //$NON-NLS-1$
         new PSErrorDialog(getShell(), msg).open();
         return false;
      }      
      
      //Create and load the designer object
      try
      {
         PSUiReference parent = null;
         if (m_ref == null) // only create object if not already created
         {
            ObjectDefaulter defaulter = new ObjectDefaulter();
            // get selection and supply parent if appropriate
            IWorkbenchWindow window = 
               PSWorkbenchPlugin.getDefault().getWorkbench().
               getActiveWorkbenchWindow();
            if (window != null)
            {
               ISelectionService service = window.getSelectionService();
               if (service.getSelection() instanceof StructuredSelection)
               {
                  StructuredSelection selection = 
                     (StructuredSelection)service.getSelection();
                  if (selection != null)
                  {
                     if (selection.size() == 1
                           && selection.getFirstElement() instanceof PSUiReference)
                     {
                        parent = (PSUiReference) selection.getFirstElement();
                     }
                  }
               }
            }
            m_ref = PSModelTracker.getInstance().create(
                  getObjectTypeForRef(),
                  name, parent, defaulter);
            handleAcl(m_ref);
         }         
         
      }
      catch (PSDuplicateNameException e)
      {
         Object[] args = new Object[]{name};
         String msg = 
            PSMessages.getString("common.error.duplicatename", args); //$NON-NLS-1$
         new PSErrorDialog(getShell(), msg).open();
         return false;
      }
      catch (Exception e)
      {
         PSWorkbenchPlugin.handleException(null, null, null, e); 
         return false;
      }   
      
      try
      {
         // Open the editor
         PSEditorRegistry registry = PSEditorRegistry.getInstance();
         IPSEditorFactory factory = 
            registry.findEditorFactory(getObjectType());
         if(factory == null)
            throw new RuntimeException("Could not find a registered editor."); //$NON-NLS-1$
         factory.openEditor(m_workbenchPage, m_ref);
      }
      catch (PartInitException e)
      {
        PSWorkbenchPlugin.handleException(null, null, null, e);
      }
      return true;
   }
   
   /**
    * Allows operating on the design object just after creation but
    * before the objects editor is opened. This is a good place to
    * set defaults on the object. Any subclass that needs to operate
    * on the freshly created object should implement this method.
    * @param data never <code>null</code>.
    */
   protected void postObjectCreationOperations(
         @SuppressWarnings("unused") Object data)
   {
      // no-op
   }
   
   /**
    * Retrieve the associated <code>IPSCmsModel</code>.
    * 
    * @return Never <code>null</code>.
    */     
   public IPSCmsModel getModel()
   {
      return m_model;
   }   
   
   /**
    * Returns the <code>IPSReference</code> for the newly created object.
    * This should only be called after the object is created in the 
    * {@link #performFinish()} method. Will most likely be used
    * by a wizard page when the <code>updateDesignerObject</code> 
    * method is called.
    * @return the reference or <code>null</code> if not yet created.
    */
   public IPSReference getReference()
   {
      return m_ref;
   }
   
   /* 
    * @see org.eclipse.jface.wizard.Wizard#createPageControls(
    * org.eclipse.swt.widgets.Composite)
    */
   @Override
   public void createPageControls(Composite pageContainer)
   {
      super.createPageControls(pageContainer);
      getStartingPage(); // cause validation to run
      loadAclAndCommunityControl();
      // Call init help manager for each page
      for(IWizardPage page : getPages())
      {
         if(page instanceof PSWizardPageBase)
         {
            ((PSWizardPageBase)page).initHelpManager();
         }
      }
   }

   /* 
    * @see org.eclipse.jface.wizard.Wizard#getStartingPage()
    */
   @Override
   public IWizardPage getStartingPage()
   {
      IWizardPage page = super.getStartingPage();
      // run silent page validation on the starting page.
      if(page instanceof PSWizardPageBase)
         ((PSWizardPageBase)page).runPageValidation(true);
      return page;
   }
   
   /* 
    * @see org.eclipse.jface.wizard.Wizard#getNextPage(
    * org.eclipse.jface.wizard.IWizardPage)
    */
   @Override
   public IWizardPage getNextPage(IWizardPage page)
   {
      IWizardPage nextpage = super.getNextPage(page);
      if(nextpage instanceof PSWizardPageBase)
         ((PSWizardPageBase)nextpage).runPageValidation(true);
      return nextpage;
   }

   /* 
    * @see org.eclipse.jface.wizard.Wizard#performCancel()
    */
   @Override
   public boolean performCancel()
   {
      if (m_ref != null)
      {
         try
         {
            getModel().releaseLock(m_ref);
         }
         catch (Exception e)
         {
            PSWorkbenchPlugin.handleException(null, null, null, e);
         }
      }
      return true;
   }

  
   
   /**
    * Finds the designer object's name from the controls in the wizards
    * pages.
    * @return the object's name or <code>null</code> if it could
    * not be located.
    */
   protected String findObjectName()
   {
      Text text = findNameControl();
      return text == null ? null : text.getText();
   }
   
   /**
    * Finds the name control from the controls in the wizards
    * pages.
    * @return the object's name or <code>null</code> if it could
    * not be located.
    */
   private Text findNameControl()
   {
      IWizardPage[] pages = getPages();
      for(IWizardPage page : pages)
      {
         if(page instanceof PSWizardPageBase)
         {
            Iterator<PSControlInfo> controls = 
               ((PSWizardPageBase)page).getRegisteredControls();
            while(controls.hasNext())
            {
               PSControlInfo info = controls.next();
               if(info.isObjectName())
               {
                  // Must be a text field
                  Control control = info.getControl();
                  if(control instanceof Text)
                  {
                     return (Text)control;
                  }
                  String msg = 
                     PSMessages.getString(
                        "PSWizardBase.error.must.be.text.control"); //$NON-NLS-1$
                  
                  new PSErrorDialog(getShell(), msg).open();
                  PSUiUtils.log(msg);
                  throw new RuntimeException(msg);
               }
            }
         }
      }
      return null;
   }
   
   /**
    * Finds the control that represents the communities and returns
    * it if there is one.
    * @return the community control or <code>null</code> if none.
    */
   private PSControlInfo findCommunityControl()
   {
      IWizardPage[] pages = getPages();
      for(IWizardPage page : pages)
      {
         if(page instanceof PSWizardPageBase)
         {
            Iterator<PSControlInfo> controls = 
               ((PSWizardPageBase)page).getRegisteredControls();
            while(controls.hasNext())
            {
               PSControlInfo info = controls.next();
               if(info.isCommunitiesField())
               {
                  // Must be a PSSlushBucket control field
                  Control control = info.getControl();
                  if(control instanceof PSSlushBucket)
                  {
                     return info;
                  }
                  String msg = PSMessages.getString(
                     "PSWizardBase.error.mustBeSlushbucketControl"); //$NON-NLS-1$
                     
                  
                  new PSErrorDialog(getShell(), msg).open();
                  PSUiUtils.log(msg);
                  throw new RuntimeException(msg);
               }
            }
         }
      }
      return null;
      
   }
   
   /**
    * Gets the default ACL object then and modifies the slush bucket
    * (if it exists) to make sure it contains the same selections.
    */
   @SuppressWarnings("deprecation")
   protected void loadAclAndCommunityControl()
   {
      if(!((PSObjectTypes)getObjectType().getPrimaryType()).
         supportsAcls())
         return;
      PSWorkbenchPlugin plugin = PSWorkbenchPlugin.getDefault();
      m_defaultAcl = plugin.getDefaultAcl();
      PSControlInfo info = findCommunityControl();
      if(info == null)
         return;
      List<IPSReference> selectedComms = new ArrayList<IPSReference>();
      List<IPSReference> removeComms = new ArrayList<IPSReference>(); 
      boolean isSystemCommActive = false;
      Enumeration entries = m_defaultAcl.entries();
      while(entries.hasMoreElements())
      {
         IPSAclEntry entry = (IPSAclEntry)entries.nextElement();
         if(entry.isCommunity())
         {
            boolean hasVisibility = PSAclUtils.entryHasPermission(entry,
               PSPermissions.RUNTIME_VISIBLE);
           
            if(hasVisibility && !entry.isSystemCommunity())
            {
               IPSReference ref = PSUiUtils.getReferenceByName(
                  getCommunities(), entry.getName());
               if(ref != null && !selectedComms.contains(ref))
                  selectedComms.add(ref);
            }
            else if(hasVisibility && entry.isSystemCommunity())
            {
               // Add all communities
               for(IPSReference ref : getCommunities())
               {
                  if(!selectedComms.contains(ref))
                     selectedComms.add(ref);
               }
               isSystemCommActive = false;
            }
            else
            {
               // Add to list of communities that should not
               // be removed when the system community entry is active
               removeComms.add(PSUiUtils.getReferenceByName(
                  getCommunities(), entry.getName()));
            }
            
            
         }
         if(isSystemCommActive)
         {
            for(IPSReference ref : removeComms)
               selectedComms.remove(ref);
         }
         PSSlushBucket control = (PSSlushBucket)info.getControl();
         control.setValues(control.getAvailable(), selectedComms);
      }
      
   }
   
   /**
    * Handles saving the acl for the object and will set the proper
    * community entries if it finds a registered community control.
    * @param ref assumed not <code>null</code>
    * @throws PSModelException upon any error.
    */
   @SuppressWarnings("unchecked")
   private void handleAcl(IPSReference ref)
      throws PSModelException
   {
      if(!((PSObjectTypes)getObjectType().getPrimaryType()).
         supportsAcls())
         return;
      // Load the acl
      IPSAcl acl = (IPSAcl)m_model.loadAcl(ref, true);
     
      // Copy Acl entries from default
      PSAclUtils.copyAclEntries(m_defaultAcl, acl);
      
      PSControlInfo info = findCommunityControl();
      if(info == null)
         return;
      PSSlushBucket control = (PSSlushBucket)info.getControl();
      List<IPSReference> comms = getCommunities();
      List<IPSReference> selections = 
         (List<IPSReference>)control.getSelections();
      PSAclUtils.removeAllEntries(acl, PrincipalTypes.COMMUNITY);
      // Should we just set the system community entry
      if(comms.size() == selections.size())
      {         
         PSAclUtils.useSystemCommunityEntry(acl);
      }
      else
      {
         //Remove system comm visibility
         PSAclUtils.removeVisibilityOnSystemCommunityEntry(acl);
         // Add new entries for each selected community
         for(IPSReference selection : selections)
         {
            IPSAclEntry entry = acl.createEntry(new PSTypedPrincipal(selection
               .getName(), PrincipalTypes.COMMUNITY));
            entry.addPermission(PSPermissions.RUNTIME_VISIBLE);
            try
            {
               acl.addEntry(acl.getFirstOwner(), entry);
            }
            catch (SecurityException e)
            {
               // should not happen
               e.printStackTrace();
            }
            catch (NotOwnerException e)
            {
               // should not happen
               e.printStackTrace();
            }
         }
      }
      // Save the acl
      m_model.saveAcl(ref, true);
      
      
   }
   
 
   
   /**
    * Retrieves all available communities
    * @return list of communities, never <code>null</code> may
    * be empty.
    */
   private List<IPSReference> getCommunities()
   {
      try
      {         
         return PSCoreUtils.catalog(PSObjectTypes.COMMUNITY, false);
      }
      catch (PSModelException e)
      {
         PSWorkbenchPlugin.handleException(
            null,
            null,
            null,
            e);
         return null;
      }
   }
   
   /**
    * Returns the image the wizard was registered with.
    * If the image is not found used default behavior. 
    */
   @Override
   public Image getDefaultPageImage()
   {
      if (m_defaultPageImage == null && getWizardDesc() != null)
      {
         if (StringUtils.isNotBlank(getWizardIconPath()))
         {
            //convert the small icon path name to the large name
            // the small name format is [path]newXY16.gif
            // which needs to convert to [path]xY.gif
            String wizIconPath = getWizardIconPath()
                  .replace("16.", "."); //$NON-NLS-1$ //$NON-NLS-2$
            final String iconPrefix = "new";
            wizIconPath.replace('\\', '/');
            int lastSlashPos = wizIconPath.lastIndexOf('/');
            String path;
            String filename;
            if (lastSlashPos >= 0)
            {
               path = wizIconPath.substring(0, lastSlashPos+1);
               filename = wizIconPath.substring(lastSlashPos+1);
            }
            else
            {
               path = ""; //$NON-NLS-1$ 
               filename = wizIconPath;
            }

            if (filename.startsWith(iconPrefix))
            {
               filename = filename.substring(iconPrefix.length());
               char[] fn = filename.toCharArray();
               fn[0] = Character.toLowerCase(fn[0]);
               filename = String.valueOf(fn);
            }
            wizIconPath = path + filename;
            final ImageDescriptor desc = PSWorkbenchPlugin
                  .getImageDescriptor(wizIconPath);
            if (desc != null)
               m_defaultPageImage = desc.createImage();
         }
      }
      return m_defaultPageImage == null
            ? super.getDefaultPageImage()
            : m_defaultPageImage;
   }

   private String getWizardIconPath()
   {
      final IConfigurationElement config =
         (IConfigurationElement) getWizardDesc().getAdapter(IConfigurationElement.class);
      return config.getAttribute("icon"); //$NON-NLS-1$
   }
   
   /**
    * Cleans up reserved resources.
    */
   @Override
   public void dispose()
   {
      if (m_defaultPageImage != null)
      {
         m_defaultPageImage.dispose();
      }
      super.dispose();
   }

   /**
    * Finds New wizard specified by the current class name.
    */
   private IWizardDescriptor getWizardDesc()
   {
      return PSWorkbenchPlugin.getDefault().getWorkbench()
            .getNewWizardRegistry().findWizard(getClass().getName());
   }
   
   /**
    * The type supplied in the ctor. This should be the type of the object
    * being created by the derived wizard.
    * 
    * @return Never <code>null</code>.
    */
   public PSObjectType getObjectType()
   {
      return m_objectType;
   }
   
   /**
    * The type used to create a reference.
    * Only called during {@link #performFinish()}, so at this point user already
    * made all the selections.
    * This could be different from value returned by {@link #getObjectType()}
    * because secondary type can depend on the values specified by user.
    * @return default implementation returns {@link #getObjectType()}. 
    */
   protected PSObjectType getObjectTypeForRef()
   {
      return getObjectType();
   }
   
   /**
    * Class that adds the defaults to the object being created
    */
   class ObjectDefaulter implements IPSObjectDefaulter
   {      
      /* 
       * @see com.percussion.client.models.IPSObjectDefaulter#
       * modify(java.lang.Object)
       */
      public void modify(Object data)
      {
         // Allow operating on the freshly created object.
         postObjectCreationOperations(data);
         // loop through all wizard pages and call their
         // update designer object methods for each registered
         // control.
         for(IWizardPage page : getPages())
         {
            if(page instanceof PSWizardPageBase)
            {
               Iterator<PSControlInfo> controls = 
                  ((PSWizardPageBase)page).getRegisteredControls();
               while(controls.hasNext())
               {
                  PSControlInfo info = controls.next();
                  ((PSWizardPageBase)page).updateDesignerObject(
                     data, info.getControl());
               }
            }
         }
         
      }
      
   }

   /**
    * The associated <code>IPSCmsModel</code> for this wizard, never
    * <code>null</code>.
    */
   private final IPSCmsModel m_model;
   
   private IPSReference m_ref;

   /**
    * What was selected when this wizard was activated. Set in the
    * {@link #init(IWorkbench, IStructuredSelection) init} method. May be
    * <code>null</code> if nothing was selected.
    */
   @SuppressWarnings("unused") //$NON-NLS-1$
   private IStructuredSelection m_selection;

   /**
    * This is the page active when the wizard was called. Set in the 
    * {@link #init(IWorkbench, IStructuredSelection) init} method. This is
    * used to open the editor. Never <code>null</code> after set.
    */
   private IWorkbenchPage m_workbenchPage;

   /**
    * See {@link #getObjectType()}. Never <code>null</code>.
    */
   private final PSObjectType m_objectType;
   
   /**
    * Image to be returned by {@link #getDefaultPageImage()}.
    * If <code>null</code> the method has default behavior.
    */
   private Image m_defaultPageImage;
   
   /**
    * The default ACL as retrieved from the plugin's preference
    * store by {@link #loadAclAndCommunityControl()}. Never 
    * <code>null</code> after that.
    */
   private IPSAcl m_defaultAcl;
}
