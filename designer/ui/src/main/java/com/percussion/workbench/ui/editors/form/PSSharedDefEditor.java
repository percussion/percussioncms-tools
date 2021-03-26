/******************************************************************************
 *
 * [ PSSharedDefEditor.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form;

import com.percussion.client.IPSReference;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.models.PSLockException;
import com.percussion.design.objectstore.PSContainerLocator;
import com.percussion.design.objectstore.PSContentEditorSharedDef;
import com.percussion.design.objectstore.PSDisplayMapper;
import com.percussion.design.objectstore.PSFieldSet;
import com.percussion.design.objectstore.PSSharedFieldGroup;
import com.percussion.design.objectstore.PSTableLocator;
import com.percussion.design.objectstore.PSTableRef;
import com.percussion.design.objectstore.PSTableSet;
import com.percussion.design.objectstore.PSUIDefinition;
import com.percussion.util.PSCollection;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.PSModelTracker;
import com.percussion.workbench.ui.PSWorkbenchPlugin;
import com.percussion.workbench.ui.controls.PSSortableTable;
import com.percussion.workbench.ui.editors.form.ce.PSContentEditorDefinition;
import com.percussion.workbench.ui.editors.form.ce.PSSharedDefFieldSetTab;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class PSSharedDefEditor extends PSMultiPageEditorBase
{

   @Override
   protected void createPages()
   {
      PSContentEditorSharedDef shardDef = (PSContentEditorSharedDef) m_data;
      Iterator iter = shardDef.getFieldGroups();
      while (iter.hasNext())
      {
         PSSharedFieldGroup group = (PSSharedFieldGroup) iter.next();
         PSSharedDefFieldSetTab shTab = new PSSharedDefFieldSetTab(
               getContainer(), SWT.NONE, this, group.getName());
         int index = addPage(shTab);
         setPageText(index, group.getName());
         setPageTitle(
               index,
               PSMessages.getString("PSSharedDefEditor.title.sharedgroup") + group.getName()); //$NON-NLS-1$
         m_fieldSetPages.put(group.getName(), shTab);
      }
   }

   @Override
   public boolean isValidReference(IPSReference ref)
   {
      if (ref == null)
         return false; // Should never happen
      if (ref.getObjectType().getPrimaryType() == PSObjectTypes.SHARED_FIELDS)
         return true;
      return false;
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater#updateDesignerObject(java.lang.Object,
    *      java.lang.Object)
    */
   public void updateDesignerObject(Object designObject, Object control)
   {
      Iterator iter = m_fieldSetPages.values().iterator();
      while (iter.hasNext())
      {
         ((PSSharedDefFieldSetTab) iter.next()).updateDesignerObject(
               designObject, control);
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater#loadControlValues(java.lang.Object)
    */
   public void loadControlValues(Object designObject)
   {
      Iterator iter = m_fieldSetPages.values().iterator();
      while (iter.hasNext())
      {
         ((PSSharedDefFieldSetTab) iter.next()).loadControlValues(designObject);
      }
   }

   /**
    * Adds a new Field Set with the supplied name. Validates the name before
    * creating
    * 
    * @param name
    */
   public void addNewFieldSetPage(String name)
   {
      String validationError = PSContentEditorDefinition
            .validateFieldName(name);
      if (validationError != null)
      {
         MessageDialog.openError(getSite().getShell(), PSMessages
               .getString("PSSharedDefEditor.error.title.invalidname"), //$NON-NLS-1$
               validationError); //$NON-NLS-1$
         return;
      }

      // Check whether any field set exists with this field name.
      Set fs = m_fieldSetPages.keySet();
      if (fs.contains(name))
      {
         MessageDialog
               .openError(
                     getSite().getShell(),
                     PSMessages
                           .getString("PSSharedDefEditor.error.title.duplicatefieldsetname"), //$NON-NLS-1$
                     PSMessages
                           .getString("PSSharedDefEditor.error.message.duplicatefieldsetname")); //$NON-NLS-1$
         return;
      }

      PSContentEditorSharedDef shardDef = (PSContentEditorSharedDef) m_data;
      Iterator iter = shardDef.getFieldGroups();
      if (iter.hasNext())
      {
         PSSharedFieldGroup group = (PSSharedFieldGroup) iter.next();
         PSSharedFieldGroup newGroup = new PSSharedFieldGroup(name, group
               .getFilename());
         PSFieldSet set = new PSFieldSet(name);
         set.setType(PSFieldSet.TYPE_MULTI_PROPERTY_SIMPLE_CHILD);
         set.setRepeatability(PSFieldSet.REPEATABILITY_ONE_OR_MORE);
         set.setSequencingSupported(false);
         newGroup.setFieldSet(set);
         PSDisplayMapper mapper = new PSDisplayMapper(name);
         PSUIDefinition uiDef = new PSUIDefinition(mapper);
         newGroup.setUIDefinition(uiDef);

         try
         {
            PSTableLocator tloc = PSContentEditorDefinition
                  .getSystemTableLocator();
            PSTableSet tset = new PSTableSet(tloc, new PSTableRef("CT_SH_" //$NON-NLS-1$
                  + StringUtils.upperCase(name)));
            PSCollection tableSets = new PSCollection(PSTableSet.class);
            tableSets.add(tset);
            PSContainerLocator loc = new PSContainerLocator(tableSets);
            newGroup.setLocator(loc);
         }
         catch (Exception e)
         {
            PSWorkbenchPlugin
                  .handleException(
                        "SharedDef Editor", //$NON-NLS-1$
                        PSMessages
                              .getString("PSSharedDefEditor.error.title.sharedgroupcreation"), //$NON-NLS-1$
                        PSMessages
                              .getString("PSSharedDefEditor.error.message.sharedgroupcreation"), //$NON-NLS-1$
                        e);
         }

         shardDef.addFieldGroup(newGroup);

         PSSharedDefFieldSetTab shTab = new PSSharedDefFieldSetTab(
               getContainer(), SWT.NONE, this, name);
         int index = addPage(shTab);
         setPageText(index, name);
         setPageTitle(index, PSMessages
               .getString("PSSharedDefEditor.title.sharedgrouppage") + name); //$NON-NLS-1$
         m_fieldSetPages.put(name, shTab);
         setActivePage(index);
         PSModelTracker tracker = PSModelTracker.getInstance();
         try
         {
            tracker.propertyChanged(m_reference, null);
         }
         catch (PSLockException e)
         {
            PSWorkbenchPlugin.handleException(
               "Shared definition editor", null, null, e);                   //$NON-NLS-1$
         }
      }

   }

   @Override
   public void doSave(IProgressMonitor pMonitor)
   {
      String infoTitle = PSMessages
            .getString("PSSharedDefEditor.info.title.sharedfieldchanges"); //$NON-NLS-1$
      String infoMessage = PSMessages
            .getString("PSSharedDefEditor.info.message.sharedfieldchanges"); //$NON-NLS-1$
      MessageDialog.openInformation(getSite().getShell(), infoTitle,
            infoMessage);
      super.doSave(pMonitor);
   }
   
   /* 
    * @see com.percussion.workbench.ui.editors.form.PSMultiPageEditorBase#addPage(
    * org.eclipse.swt.widgets.Control)
    */
   @Override
   public int addPage(Control control)
   {
      int pageIndex = super.addPage(control);
      PSTabItem item = getItem(pageIndex);
      addContextMenu(item.getControl());
      return pageIndex;
   }   
   
   /* 
    * Overriden to just return the classname
    */
   @Override
   public String getHelpKey(@SuppressWarnings("unused") Control control)
   {
      return getClass().getName();
   }

   /**
    * Adds a context menu to all the passed in control if it is an instance
    * of either Composite or Label. Will recursively add the menu to 
    * children of the composite controls.
    * @param control assumed not <code>null</code>.
    */
   private void addContextMenu(Control control)
   {
      if((!(control instanceof PSSortableTable)) &&
         (control instanceof Composite || control instanceof Label))
      {
         if(control instanceof Composite)
         {
            for(Control child : ((Composite)control).getChildren())
               addContextMenu(child);
         }
         control.setMenu(createContextMenu(control));
         
      }
   }
   
   /**
    * Creates the context menu for the passed in control.
    * @param control assumed not <code>null</code>.
    * @return the newly created context menu, never <code>null</code>.
    */
   private Menu createContextMenu(Control control)
   {
      Menu menu = new Menu(control);
      MenuItem deleteFieldSetItem = new MenuItem(menu, SWT.NONE);
      deleteFieldSetItem.setText(PSMessages.getString("PSSharedDefEditor.menuItem.deleteFieldSet")); //$NON-NLS-1$
      deleteFieldSetItem.addSelectionListener(new SelectionAdapter()
         {

            /* 
             * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(
             * org.eclipse.swt.events.SelectionEvent)
             */
            @Override
            public void widgetSelected(@SuppressWarnings("unused") SelectionEvent e) //$NON-NLS-1$
            {
               doDeleteFieldSet();
            }
            
         });
      return menu;
   }
   
   /**
    * Deletes the field set for the currently selected page tab
    */
   private void doDeleteFieldSet()
   {
      if(getPageCount() == 1)
      {
         
         MessageDialog.openError(getSite().getShell(),
            PSMessages.getString("PSSharedDefEditor.error.cannotDeleteFieldSet.title"), //$NON-NLS-1$
            PSMessages.getString("PSSharedDefEditor.error.cannotDeleteFieldSet")); //$NON-NLS-1$
         return;
      }
      Control control = getControl(getActivePage());
      if(control instanceof ScrolledComposite)
         control = ((ScrolledComposite)control).getContent();
      if(control instanceof PSSharedDefFieldSetTab)
      {
         PSSharedDefFieldSetTab tab = (PSSharedDefFieldSetTab)control;
         String name = tab.getFieldSetName();
         Object[] args = new Object[]{name};
         boolean bOK = MessageDialog.openQuestion(getSite().getShell(), 
            PSMessages.getString("PSSharedDefEditor.error.deleteFieldSet.title"), //$NON-NLS-1$
            PSMessages.getString("PSSharedDefEditor.error.deleteFieldSet", args));  //$NON-NLS-1$
         if(!bOK)
            return;
         PSContentEditorSharedDef def = (PSContentEditorSharedDef) m_data;
         Iterator iter = def.getFieldGroups();
         while (iter.hasNext())
         {
            PSSharedFieldGroup group = (PSSharedFieldGroup) iter.next();
            if(group.getName().equals(name))
            {
               def.removeFieldGroup(group);
               removePage(getActivePage());
               PSModelTracker tracker = PSModelTracker.getInstance();
               try
               {
                  tracker.propertyChanged(m_reference, null);
               }
               catch (PSLockException e)
               {
                  PSWorkbenchPlugin.handleException(
                     "Shared definition editor", null, null, e);                   //$NON-NLS-1$
               }
               setDirty();
               return;
            }
         }
        
      }
   }
   
   

   /**
    * A map to hold the all the fieldset pages.
    */
   private Map<String, PSSharedDefFieldSetTab> m_fieldSetPages = new HashMap<String, PSSharedDefFieldSetTab>();
   

   

}
