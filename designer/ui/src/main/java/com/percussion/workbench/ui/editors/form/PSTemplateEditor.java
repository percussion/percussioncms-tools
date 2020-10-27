/******************************************************************************
 *
 * [ PSTemplateEditor.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form;

import com.percussion.E2Designer.PSDlgUtil;
import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSCoreUtils;
import com.percussion.client.PSModelChangedEvent;
import com.percussion.client.PSModelChangedEvent.ModelEvents;
import com.percussion.client.PSModelException;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.models.IPSCmsModel;
import com.percussion.client.models.IPSModelListener;
import com.percussion.client.objectstore.PSUiAssemblyTemplate;
import com.percussion.services.assembly.IPSAssemblyTemplate.OutputFormat;
import com.percussion.services.assembly.IPSTemplateBinding;
import com.percussion.services.assembly.data.PSAssemblyTemplate;
import com.percussion.services.assembly.data.PSTemplateBinding;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.string.PSStringUtils;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.PSWorkbenchPlugin;
import com.percussion.workbench.ui.controls.PSNameLabelDesc;
import com.percussion.workbench.ui.util.PSControlInfo;
import com.percussion.workbench.ui.util.PSHelpHintKeyHelper;
import com.percussion.workbench.ui.util.PSUiUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * {@link PSAssemblyTemplate} editor.
 * 
 * @author Andriy Palamarchuk
 */
public class PSTemplateEditor extends PSMultiPageEditorBase
{
   @Override
   protected void createPages()
   {
      addSlotsPage();
      addSitesPage();
      
      // Add a selection listener to determine when the
      // General tab is selected so we can set the correct
      // regen source button state.
      CTabFolder tabFolder = this.getTabFolder();
      tabFolder.addSelectionListener(new SelectionAdapter()
      {

         /* (non-Javadoc)
          * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(
          * org.eclipse.swt.events.SelectionEvent)
          */
         @Override
         public void widgetSelected(SelectionEvent e)
         {
            CTabFolder folder = (CTabFolder)e.getSource();
            CTabItem item = folder.getSelection();
            Control control = item.getControl();
            if(control instanceof ScrolledComposite)
            {
               ScrolledComposite scrollPanel = (ScrolledComposite)control;
               Control content = scrollPanel.getContent();
               if(content instanceof PSTemplateGeneralPage)
               {
                  ((PSTemplateGeneralPage)content).setRegenSourceButtonState();
               }
            }            
         }
         
      });
   }

   /**
    * Adds sites editor page.
    */
   private void addSitesPage()
   {
      m_sitesPage = new PSTemplateSitesPage(getContainer(), SWT.NONE, this);
      final int idx = addPage(m_sitesPage);
      setPageText(idx, SITES_LABEL_KEY);
      registerControl(SITES_LABEL_KEY, m_sitesPage, null);
   }

   /**
    * Adds slots editor page.
    */
   private void addSlotsPage()
   {
      m_slotsPage = new PSTemplateSlotsPage(getContainer(), SWT.NONE, this);
      final int idx = addPage(m_slotsPage);
      setPageText(idx, SLOTS_LABEL_KEY);
      registerControl(SLOTS_LABEL_KEY, m_slotsPage, null);
      addSlotObjectDeleteListener();
   }
   
   /**
    * Adds a listener to the model to listen for delete events on slot
    * objects. When a slot delete occurs the template object is "fixed"
    * so that it will only contain existing slots. The list of available
    * slots is also updated.
    */
   private void addSlotObjectDeleteListener()
   {
      try
      {
         PSCoreFactory factory = PSCoreFactory.getInstance();
         IPSCmsModel model = factory.getModel(PSObjectTypes.SLOT);
         m_modelListener = new IPSModelListener()
            {
            
            @SuppressWarnings("synthetic-access") //$NON-NLS-1$
            public void modelChanged(
               @SuppressWarnings("unused") PSModelChangedEvent event) //$NON-NLS-1$
            {               
               Job job = new Job(PSMessages.getString(
                  "PSTemplateEditor.updateEditor.message")) //$NON-NLS-1$
               {

                  @Override
                  protected IStatus run(IProgressMonitor monitor)
                  {
                     monitor.beginTask(PSMessages.getString(
                        "PSTemplateEditor.updateAvailSlots.message"), 10); //$NON-NLS-1$
                     validateSlotsExist(false, true);
                     monitor.worked(5);       
                     
                     Display.getDefault().asyncExec(new Runnable()
                        {
                        
                        public void run()
                        {
                           try
                           {
                              m_slotsPage.loadControlValues(
                                 (PSUiAssemblyTemplate)m_data);
                              m_slotsPage.getSlotsControl().createAvailableItems(
                                 m_data);
                           }
                           catch (PSModelException e)
                           {
                              PSWorkbenchPlugin.handleException(null, null, null, e);                                    
                           }                                 
                        }
                        
                        });
                     monitor.done();
                     return Status.OK_STATUS;                    
                  }
                  
               };
               job.schedule();
                           
            }
            };
         model.addListener(m_modelListener, ModelEvents.DELETED.getFlag());
      }
      catch (PSModelException e)
      {
         PSWorkbenchPlugin.handleException(null, null, null, e);         
      }
   }  

   @Override
   public boolean isValidReference(IPSReference ref)
   {
      return ref.getObjectType().getPrimaryType()
            .equals(PSObjectTypes.TEMPLATE);
   }

   /**
    * Updates template with current UI data.
    */
   private void updateTemplate()
   {
      m_propertiesPage.updateTemplate(getTemplate());
      if (m_bindingsPage != null)
      {
         m_bindingsPage.updateTemplate(getTemplate());
      }
      m_slotsPage.updateTemplate(getTemplate());
      m_sitesPage.updateTemplate(getTemplate());
      if (getTemplate().isVariant())
      {
         return;
      }
      if (m_sourcePageHelper != null)
      {
         m_sourcePageHelper.updateTemplate(getTemplate());
      }
      String url = getTemplate().getAssemblyUrl();
      if (url == null || url.trim().length() == 0)
      {
         getTemplate().setAssemblyUrl("../assembler/render"); //$NON-NLS-1$
      }
   }

   public void updateDesignerObject(@SuppressWarnings("unused") Object designObject, //$NON-NLS-1$
         @SuppressWarnings("unused") Object control) //$NON-NLS-1$
   {
      updateTemplate();
   }

   // see base
   public void loadControlValues(Object designObject)
   {
      final PSUiAssemblyTemplate template = (PSUiAssemblyTemplate) designObject;

      // finish UI initialization depending on data
      insertPropertiesPage(template);
      try
      {
         maybeInsertSourcePage(template);
         maybeInsertBindingsPage(template);
      }
      catch (PSModelException e)
      {
         throw new RuntimeException(e);
      }

      // actually load the data
      if (m_sourcePageHelper != null)
      {
         m_sourcePageHelper.loadControlValues(template);
      }
      m_propertiesPage.loadControlValues(template);
      if (m_bindingsPage != null)
      {
         m_bindingsPage.loadControlValues(template);
      }
      m_slotsPage.loadControlValues(template);
      m_sitesPage.loadControlValues(template);

      // activate the first page with useful information
      if (m_sourcePageHelper != null && !m_sourcePageHelper.hasSource())
      {
         setActivePage(PROPERTIES_PAGE_IDX);
      }
      else
      {
         setActivePage(0);
      }
   }

   // see base
   @Override
   public void doSave(IProgressMonitor pMonitor)
   {
      if (m_sourcePageHelper != null)
      {
         m_sourcePageHelper.updateTemplate(getTemplate());
      }
      validateSlotsExist(true, true);
      validateRequiredBindings();
      super.doSave(pMonitor);
      maybeNotifyNotSavedBindings();
   }

   // see base
   @Override
   public void doSaveAs()
   {
      if (m_sourcePageHelper != null)
      {
         m_sourcePageHelper.updateTemplate(getTemplate());
      }
      validateSlotsExist(true, true);
      validateRequiredBindings();
      super.doSaveAs();
      maybeNotifyNotSavedBindings();
   }

   /**
    * Notifies user if any of the bindings were not saved because they don't
    * have values. 
    */
   private void maybeNotifyNotSavedBindings()
   {
      if (m_bindingsPage != null
            && !m_bindingsPage.getNotSavedBindings().isEmpty())
      {
         final String bindings = StringUtils.join(
               m_bindingsPage.getNotSavedBindings().iterator(), ',');
         final String message = PSMessages.getString(
               "PSTemplateEditor.error.notSavedBindings",      //$NON-NLS-1$
               new Object[] {bindings});
         PSDlgUtil.showErrorDialog(message,
               PSMessages.getString("common.error.title"));    //$NON-NLS-1$
      }
   }
   
   /**
    * Validate that associated slots all exist and if not then
    * remove them from the object and warn user.
    * @param showWarning if <code>true</code> then a warning will be 
    * presented to the user.
    * @param force forces a clear cache on the model when cataloging
    */
   private void validateSlotsExist(boolean showWarning, boolean force)
   {
      PSUiAssemblyTemplate template = getTemplate();
      try
      {
         boolean hasDeletedSlot = false;
         Set<IPSGuid> slotGuids = new HashSet<IPSGuid>();
         Collection<IPSGuid> existing = 
            PSUiUtils.guidCollectionFromRefCollection(
               PSCoreUtils.catalog(PSObjectTypes.SLOT, force));
        
         for(IPSGuid guid : template.getSlotGuids())
         {
            if(existing.contains(guid))
            {
               slotGuids.add(guid);
            }
            else
            {
               hasDeletedSlot = true;
            }
         }
         if(hasDeletedSlot)
         {
            template.setSlotGuids(slotGuids);
            String msg = PSMessages.getString(
               "PSTemplateEditor.warn.nonExisitantSlots"); //$NON-NLS-1$
            if(showWarning)
               MessageDialog.openWarning(getSite().getShell(),
                  PSMessages.getString("PSTemplateEditor.warn.nonExistantSlots.title"), //$NON-NLS-1$
                  msg);
         }
      }
      catch (PSModelException e)
      {
         PSWorkbenchPlugin.handleException(null, null, null, e);         
      }
      
   }
   
   /**
    * The database binding for db template. It is fine to have only one of the
    * two(either DB_DATABASE or DB_ORIGIN} present in the bindings.
    */
   private static final String DB_DATABASE = "$db.database";
   
   /**
    * The origin binding for db template. It is fine to have only one of the
    * two(either DB_DATABASE or DB_ORIGIN} present in the bindings.
    */
   private static final String DB_ORIGIN   = "$db.origin";
   
   /**
    * For database assemblers only: certain dbs such as oracle needs only 
    * DB_ORIGIN and others need only DB_DATABASE. It is not possible to 
    * distinguish based on the database. Editor can validate if one of them 
    * exists
    */
   private static final List<String> ms_dbBindingList = new ArrayList<String>(2);
   
   static
   {
      ms_dbBindingList.add(DB_DATABASE);
      ms_dbBindingList.add(DB_ORIGIN);
   }
   
   /**
    * An array of bindings required for certain assemblers. The array is in
    * pairs, where the first element of each pair is the "name" of the 
    * assembler and the second is the binding that must exist. 
    */
   private static final String[] ms_requiredBindings = {
      "Java/global/percussion/assembly/dispatchAssembler", "$sys.template",
      "Java/global/percussion/assembly/binaryAssembler", "$sys.binary",
      "Java/global/percussion/assembly/binaryAssembler", "$sys.mimetype",
      "Java/global/percussion/assembly/databaseAssembler", "$db.parent",
      "Java/global/percussion/assembly/databaseAssembler", DB_DATABASE,
      "Java/global/percussion/assembly/databaseAssembler", "$db.resource",
      "Java/global/percussion/assembly/databaseAssembler", DB_ORIGIN,
      "Java/global/percussion/assembly/databaseAssembler", "$db.drivertype"
   };
   
   /**
    * Check that the template has the bindings it should have defined 
    * for certain assembly plugins.
    */
   @SuppressWarnings("unchecked")
   protected void validateRequiredBindings()
   {
      PSUiAssemblyTemplate template = getTemplate();
      List<PSTemplateBinding> bindings = template.getBindings();
      Set<String> boundVars = new HashSet<String>();
      for(IPSTemplateBinding b : bindings)
      {
         boundVars.add(b.getVariable());
      }
      
      // Determine the assembler, use the matching "rows" to figure out
      // what bindings are missing
      String asm = template.getAssembler();
      List<String> missingVars = new ArrayList<String>();
      for(int i = 0; i < ms_requiredBindings.length; i += 2)
      {
         if (asm.equals(ms_requiredBindings[i]))
         {
            String var = ms_requiredBindings[i + 1];
            if (!boundVars.contains(var))
            {
               missingVars.add(var);
            }
         }
      }

      if (!CollectionUtils.isSubCollection(ms_dbBindingList, missingVars))
         missingVars = (List<String>) CollectionUtils.subtract(missingVars,
               ms_dbBindingList);

      if (missingVars.size() > 0)
      {
         Object args[] = new Object[2];
         args[0] = asm;
         args[1] = PSStringUtils.listToString(missingVars, ", ");
         String msg = PSMessages.getString(
         "PSTemplateEditor.warn.missingBindings", args); //$NON-NLS-1$
         MessageDialog.openWarning(getSite().getShell(),
            PSMessages.getString("PSTemplateEditor.warn.missingBindings.title"), //$NON-NLS-1$
            msg);       
      }
      
   }

   /**
    * @return source editor control if source page is selected and source editor
    * is enabled. Othewise retuns <code>null</code>.
    */
   public IEditorPart getVisibleEditor()
   {
      if (m_sourcePageHelper == null)
      {
         return null;
      }
      final int sourcePageIdx = PSTemplateSourcePageHelper.PAGE_IDX;
      return getTabFolder().getSelectionIndex() == sourcePageIdx
            ? m_sourcePageHelper.getCurrentEditor() : null;  
   }

   /**
    * Adds bindings tab if required.
    * @param template the template to insert the page for.
    * Assumed not <code>null</code>.
    * @throws PSModelException on data loading form model failure.
    */
   private void maybeInsertBindingsPage(final PSUiAssemblyTemplate template)
         throws PSModelException
   {
      deleteTab(getMessage(BINDINGS_LABEL_KEY));
      if (template.isVariant())
      {
         return;
      }
      m_bindingsPage = new PSTemplateBindingsPage(
            getContainer(), SWT.NONE, this);

      final int idx = BINDINGS_PAGE_IDX;
      addPage(idx, m_bindingsPage);
      setPageText(idx, BINDINGS_LABEL_KEY);
      registerControl(BINDINGS_LABEL_KEY, m_bindingsPage, null);
   }

   /**
    * Adds first tab to show template source.
    * @throws PSModelException on data loading from model failure. 
    */
   @SuppressWarnings("unchecked") //$NON-NLS-1$
   private void maybeInsertSourcePage(final PSUiAssemblyTemplate template)
         throws PSModelException
   {
      deleteTab(PSTemplateSourcePageHelper.TAB_LABEL);
      m_sourcePageHelper = null;
      if (template.isVariant())
      {
         return;
      }
      m_sourcePageHelper = new PSTemplateSourcePageHelper(this);
   }

   /**
    * Inserts page now because it is chosen from a few alternatives based on
    * template properties and could not be defined ahead.
    */
   private void insertPropertiesPage(final PSUiAssemblyTemplate template)
   {
      deleteTab(getMessage(GENERAL_LABEL_KEY));
      createPropertiesPage(template);
      final int idx = 0;
      addPage(idx, m_propertiesPage);
      setPageText(idx, GENERAL_LABEL_KEY);
      m_helpManager.registerControls(getItem(idx).getControl());
   }

   /**
    * Initializes {@link #m_propertiesPage} with appropriate page depending on
    * the template properties.
    */
   private void createPropertiesPage(final PSUiAssemblyTemplate template)
   {
      if (template.isVariant())
      {
         m_propertiesPage = new PSTemplateVariantPage(getContainer(), SWT.NONE,
               this);
      }
      else if (template.getOutputFormat().equals(OutputFormat.Global))
      {
         m_propertiesPage = new PSTemplateGlobalPage(getContainer(), SWT.NONE,
               this);
      }
      else
      {
         final PSTemplateGeneralPage page =
            new PSTemplateGeneralPage(getContainer(), SWT.NONE, this);
         page.getOutputComposite().getAssemblerCombo().addSelectionListener(
               new SelectionAdapter()
               {
                  @Override
                  public void widgetSelected(
                        @SuppressWarnings("unused") SelectionEvent e) //$NON-NLS-1$
                  {
                     if (m_sourcePageHelper != null)
                     {
                        m_sourcePageHelper.reinitializePage();
                     }
                  }
               });
         m_propertiesPage = page;
      }
   }      

   /**
    * Deletes tab with the specified name if it exists.
    * Is used for recreating pages created depending on the data.
    *
    * @param name the name of the page to search for.
    * Assumed not blank.
    */
   private void deleteTab(String name)
   {
      assert StringUtils.isNotBlank(name);
      for (int i = 0; i < getPageCount(); i++)
      {
         if (name.equals(getPagename(i)))
         {
            if (getActivePage() == i)
            {
               // If currently selected page is removed select some other page.
               // This is a workaround for NullPointerException when importing
               // template twice.
               // See last code review comments for Rx-06-07-0304.
               setActivePage(i == 0 ? 1 : i - 1);
            }
            
            final Control control = getControl(i);
            removePage(i);
            if (control != null)
            {
               control.dispose();
            }
            break;
         }
      }
   }

   /**
    * Template which is being edited. Convenience method to access the editor
    * data as template.
    */
   private PSUiAssemblyTemplate getTemplate()
   {
      return (PSUiAssemblyTemplate) m_data;
   }

   /* 
    * @see com.percussion.workbench.ui.editors.form.PSMultiPageEditorBase#
    * getHelpKey(org.eclipse.swt.widgets.Control)
    */
   @Override
   public String getHelpKey(Control control)
   {         
      String[] rawkey = super.getHelpKey(control).split("_"); //$NON-NLS-1$
      String postfix = ms_helpMappings.get(rawkey[1]);
      if(((PSUiAssemblyTemplate) m_data).isVariant())
         postfix += "_variant";
      if(StringUtils.isBlank(postfix))
         postfix = "general"; //$NON-NLS-1$
      return rawkey[0] + "_" + postfix; //$NON-NLS-1$
      
   }
   
   /* 
    * @see com.percussion.workbench.ui.editors.form.PSEditorBase#
    * getHelpHintKey(com.percussion.workbench.ui.util.PSControlInfo)
    */
   @Override
   protected String getHelpHintKey(PSControlInfo controlInfo)
   {
      if(m_helpHintKeyHelper == null)
      {
         m_helpHintKeyHelper = new PSHelpHintKeyHelper(new String[]
         {
            PSNameLabelDesc.DESC_TEXT_KEY,
               "description", //$NON-NLS-1$
            PSNameLabelDesc.LABEL_TEXT_KEY,
               "label", //$NON-NLS-1$
            "PSCharSetHelper.label.characterSet", //$NON-NLS-1$
               "character_set", //$NON-NLS-1$
            "PSLocationHelper.label.prefix", //$NON-NLS-1$
               "location_prefix", //$NON-NLS-1$
            "PSLocationHelper.label.suffix", //$NON-NLS-1$
               "location_suffix", //$NON-NLS-1$
            "PSMimeTypeHelper.label.mimeType", //$NON-NLS-1$
               "mime_type", //$NON-NLS-1$
            "PSPublishWhenHelper.label.publish", //$NON-NLS-1$
               "publish", //$NON-NLS-1$
            "PSSitesControl.label.selectedSites", //$NON-NLS-1$
               "selected_sites", //$NON-NLS-1$
            "PSSlotsControl.label.containedSlots", //$NON-NLS-1$
               "contained_slots", //$NON-NLS-1$
            "PSTemplateBindingsPage.label.expressionEditor", //$NON-NLS-1$
               "expression_editor", //$NON-NLS-1$
            "PSTemplateBindingsPage.label.variables", //$NON-NLS-1$
               "variables", //$NON-NLS-1$
            "PSTemplateGeneralPage.label.activeAssemblyFormat", //$NON-NLS-1$
               "active_assembly_format", //$NON-NLS-1$
            "PSTemplateOutputComposite.label.assembler", //$NON-NLS-1$
               "assembler", //$NON-NLS-1$
            "PSTemplateOutputComposite.label.output", //$NON-NLS-1$
               "output", //$NON-NLS-1$
            "PSTemplateVariantPage.label.stylesheet", //$NON-NLS-1$
               "stylesheet", //$NON-NLS-1$
            "PSTemplateVariantPage.label.url", //$NON-NLS-1$
               "url"    //$NON-NLS-1$
         });
      }
      return m_helpHintKeyHelper.getKey(
         super.getHelpHintKey(controlInfo), controlInfo);
   }
   
   /**
    * @return The multipage editor tab folder control.
    */
   CTabFolder getTabFolder()
   {
      return getContainer() == null || getContainer() instanceof CTabFolder
            ? (CTabFolder) getContainer()
            : (CTabFolder) getContainer().getParent();
   }
   
   /* 
    * @see com.percussion.workbench.ui.editors.form.PSMultiPageEditorBase#dispose()
    */
   @Override
   public void dispose()
   {
      super.dispose();
      PSCoreFactory factory = PSCoreFactory.getInstance();
      try
      {
         IPSCmsModel model = factory.getModel(PSObjectTypes.SLOT);
         model.removeListener(m_modelListener);
      }
      catch (PSModelException ignore)
      {
      }
   }
   
   /**
    * Template variable bindings page.
    * Is initialized during call to {@link #loadControlValues(Object)}.
    * Initial value is <code>null</code>, always <code>null</code> for variants. 
    */
   PSTemplateBindingsPage getBindingsPage()
   {
      return m_bindingsPage;
   }
   
   /**
    * Source page helper
    * @return may be <code>null</code>.
    */
   PSTemplateSourcePageHelper getSourcePage()
   {
      return m_sourcePageHelper;
   }

   /**
    * Index of the properties page when the source page is present.
    * @see #m_sourcePageHelper
    */
   private static final int PROPERTIES_PAGE_IDX = 1;

   /**
    * Index where bindings page is placed when it is inserted.
    */
   private static final int BINDINGS_PAGE_IDX = 2;

   /**
    * Sites tab text.
    */
   private static final String SITES_LABEL_KEY = "PSTemplateEditor.label.tab.sites"; //$NON-NLS-1$

   /**
    * Slots tab text
    */
   private static final String SLOTS_LABEL_KEY = "PSTemplateEditor.label.tab.slots"; //$NON-NLS-1$

   /**
    * Bindings tab text
    */
   private static final String BINDINGS_LABEL_KEY = "PSTemplateEditor.label.tab.bindings"; //$NON-NLS-1$

   /**
    * General tab text
    */
   private static final String GENERAL_LABEL_KEY = "PSTemplateEditor.label.tab.general"; //$NON-NLS-1$

   /**
    * General page of the editor.
    */
   private PSTemplatePropertiesPageBase m_propertiesPage;

   /**
    * Manages source page.
    * <code>null</code> before created while loading data or if this editor
    * is used to edit template.
    */
   private PSTemplateSourcePageHelper m_sourcePageHelper;

   /**
    * @see #getBindingsPage()
    */
   private PSTemplateBindingsPage m_bindingsPage;

   /**
    * Page allows user to select slots.
    */
   private PSTemplateSlotsPage m_slotsPage;

   /**
    * Page allows user to select sites.
    */
   private PSTemplateSitesPage m_sitesPage;

   /**
    * Reference to the model listener on this editor. Used so we
    * can remove this listener from the model when this editor
    * is disposed.
    */
   private IPSModelListener m_modelListener;
   
   /**
    * Cache of the help hint helper object
    */
   private PSHelpHintKeyHelper m_helpHintKeyHelper;
      
   // Help key mappings 
   private static final Map<String, String> ms_helpMappings = 
      new HashMap<String, String>();
   static
   {
      ms_helpMappings.put(
         SITES_LABEL_KEY, "sites"); //$NON-NLS-1$
      ms_helpMappings.put(
         SLOTS_LABEL_KEY, "slots"); //$NON-NLS-1$
      ms_helpMappings.put(
         BINDINGS_LABEL_KEY, "bindings"); //$NON-NLS-1$
      ms_helpMappings.put(
         GENERAL_LABEL_KEY, "general"); //$NON-NLS-1$
   }
   
    

}
