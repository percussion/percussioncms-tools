/******************************************************************************
 *
 * [ PSActionMenuUsageComposite.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form;

import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreUtils;
import com.percussion.client.PSModelException;
import com.percussion.client.PSObjectTypes;
import com.percussion.cms.objectstore.PSAction;
import com.percussion.cms.objectstore.PSDbComponentCollection;
import com.percussion.cms.objectstore.PSMenuModeContextMapping;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.PSWorkbenchPlugin;
import com.percussion.workbench.ui.controls.PSDefaultContentProvider;
import com.percussion.workbench.ui.controls.PSReferenceLabelProvider;
import com.percussion.workbench.ui.controls.PSSlushBucket;
import com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater;
import com.percussion.workbench.ui.util.PSUiUtils;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;

import java.util.ArrayList;
import java.util.Iterator;

public class PSActionMenuUsageComposite extends Composite
   implements IPSDesignerObjectUpdater
{   
   
   /**
    * Create the composite
    * @param parent
    * @param style
    */
   public PSActionMenuUsageComposite(
      Composite parent, int style, PSEditorBase editor, int page)
   {
      super(parent, style);
      setLayout(new FormLayout());
      m_editor = editor;
      
      m_usageLabel = new Label(this, SWT.NONE);
      final FormData formData = new FormData();
      formData.top = new FormAttachment(0, 0);
      formData.left = new FormAttachment(0, 50);
      m_usageLabel.setLayoutData(formData);
      m_usageLabel.setText(PSMessages.getString(
         "PSActionMenuUsageComposite.usageArea.label")); //$NON-NLS-1$
      
      m_usagelistViewer = new ListViewer(this, SWT.BORDER);
      m_usagelistViewer.setContentProvider(new PSDefaultContentProvider());
      m_usagelistViewer.setLabelProvider(new PSReferenceLabelProvider(true));
      m_usagelistViewer.setSorter(new ViewerSorter());
      m_usageList = m_usagelistViewer.getList();
      final FormData formData_1 = new FormData();
      formData_1.right = new FormAttachment(100, -50);
      formData_1.top = new FormAttachment(m_usageLabel, 0, SWT.BOTTOM);
      formData_1.left = new FormAttachment(m_usageLabel, 0, SWT.LEFT);
      formData_1.height = 185;
      m_usageList.setLayoutData(formData_1);
      m_usagelistViewer.setInput(getMenuModes());
      m_usageList.select(0);
      m_usageList.addSelectionListener(new SelectionAdapter()
         {

            /* 
             * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(
             * org.eclipse.swt.events.SelectionEvent)
             */
            @SuppressWarnings("synthetic-access")
            @Override
            public void widgetSelected(@SuppressWarnings("unused") SelectionEvent e)
            {
               loadContextsControl();
            }
            
         });
      
      m_contextSlushBucket = new PSSlushBucket(
         this, SWT.NONE, PSMessages.getString(
            "PSActionMenuUsageComposite.availUiContexts.label"), //$NON-NLS-1$
            PSMessages.getString(
               "PSActionMenuUsageComposite.usedUiContexts.label"), //$NON-NLS-1$ 
         new PSReferenceLabelProvider(true));
      final FormData formData_2 = new FormData();
      formData_2.height = 175;
      formData_2.left = new FormAttachment(m_usageList, 0, SWT.LEFT);
      formData_2.right = new FormAttachment(m_usageList, 0, SWT.RIGHT);
      formData_2.top = new FormAttachment(m_usageList, 15, SWT.BOTTOM);
      m_contextSlushBucket.setLayoutData(formData_2);
      editor.registerControl(
         "PSActionMenuUsageComposite.usedUiContexts.label", //$NON-NLS-1$
         m_contextSlushBucket,
         null,
         page);
      
      
      //
   }
   
   /** 
    * Loads the ui contexts slush bucket control with the appropriate values
    * based on the current mode selected.
    */
   private void loadContextsControl()
   {
      PSAction action = (PSAction)m_editor.m_data;
      // Get the selected mode
      IStructuredSelection selection = 
         (IStructuredSelection)m_usagelistViewer.getSelection();
      IPSReference modeRef = (IPSReference)selection.getFirstElement();
      //Get all the selected contexts for this mode
      PSDbComponentCollection coll = action.getModeUIContexts();
      Iterator it = coll.iterator();
      java.util.List<IPSReference> selected = new ArrayList<IPSReference>();
      while(it.hasNext())
      {
         PSMenuModeContextMapping mapping = 
            (PSMenuModeContextMapping)it.next();
         if(modeRef.getId().getUUID() == 
            Integer.parseInt(mapping.getModeId()))
         {
            IPSReference context = 
               PSUiUtils.getReferenceById(getMenuContexts(),
                  Long.parseLong(mapping.getContextId()));
            if(context != null)
               selected.add(context);
         }
      }
      // Set the slush bucket's values
      m_contextSlushBucket.setValues(getMenuContexts(), selected);
   }
   
   /**
    * Sets the mode context values on the passed in action for the currently
    * selected mode.
    * @param action assumed not <code>null</code>.
    */
   @SuppressWarnings("unchecked")
   private void setModeContexts(PSAction action)
   {
      // Get the selected mode
      IStructuredSelection selection = 
         (IStructuredSelection)m_usagelistViewer.getSelection();
      IPSReference modeRef = (IPSReference)selection.getFirstElement();      
      // Get selected contexts from the control
      java.util.List<IPSReference> selected = 
         m_contextSlushBucket.getSelections();
      // Get all the selected contexts for this mode
      PSDbComponentCollection coll = action.getModeUIContexts();
      
      //Remove all contexts for this mode
      java.util.List<PSMenuModeContextMapping> removeList = 
         new ArrayList<PSMenuModeContextMapping>();
      Iterator it = coll.iterator();
      while(it.hasNext())
      {
         PSMenuModeContextMapping mapping = 
            (PSMenuModeContextMapping)it.next();
         if(modeRef.getId().getUUID() == 
            Integer.parseInt(mapping.getModeId()))
         {
            removeList.add(mapping);
         }
      }
      for(PSMenuModeContextMapping mapping : removeList)
         coll.remove(mapping);
      
      //Add selected contexts for this mode
      for(IPSReference ref : selected)
      {
         PSMenuModeContextMapping mapping = 
            new PSMenuModeContextMapping(
               String.valueOf(modeRef.getId().getUUID()),
               String.valueOf(ref.getId().getUUID()),
               String.valueOf(action.getId()));
         coll.add(mapping);
      }
   }

   @Override
   public void dispose()
   {
      super.dispose();
   }

   @Override
   protected void checkSubclass()
   {
      // Disable the check that prevents subclassing of SWT components
   }
   
   /* 
    * @see com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater#
    * updateDesignerObject(java.lang.Object, java.lang.Object)
    */
   public void updateDesignerObject(Object designObject,
      @SuppressWarnings("unused") Object control)
   {
      PSAction action = (PSAction)designObject;
      setModeContexts(action);
   }
   
   /**
    * sets intial control values
    */
   public void loadControlValues(@SuppressWarnings("unused") Object designObject)
   {
      loadContextsControl();
   }
   
   /**
    * Helper method to retrieve all of the menu modes 
    * @return never <code>null</code>, may be empty.
    */
   private java.util.List<IPSReference> getMenuModes()
   {      
      java.util.List<IPSReference> results = new ArrayList<IPSReference>();
      try
      {
         java.util.List<IPSReference> refs = 
            PSCoreUtils.catalog(PSObjectTypes.UI_ACTION_MENU_MISC, false);
         for(IPSReference ref : refs)
         {
            if(ref.getObjectType().getSecondaryType() ==
               PSObjectTypes.UiActionMenuMiscSubTypes.MODES)
            {
               results.add(ref);
            }
         }
      }
      catch (PSModelException e)
      {
         PSWorkbenchPlugin.handleException(
            PSMessages.getString(
            "PSActionMenuUsageComposite.error.catalogingMenuModes"), //$NON-NLS-1$
            PSMessages.getString("common.error.title"), //$NON-NLS-1$
            e.getLocalizedMessage(),
            e);
      }
      
      return results;
   }
   
   /**
    * Helper method to retrieve all of the menu contexts 
    * @return never <code>null</code>, may be empty.
    */
   private java.util.List<IPSReference> getMenuContexts()
   {
      
      java.util.List<IPSReference> results = new ArrayList<IPSReference>();
      try
      {
         java.util.List<IPSReference> refs = 
            PSCoreUtils.catalog(PSObjectTypes.UI_ACTION_MENU_MISC, false);
         for(IPSReference ref : refs)
         {
            if(ref.getObjectType().getSecondaryType() ==
               PSObjectTypes.UiActionMenuMiscSubTypes.CONTEXTS)
            {
               results.add(ref);
            }
         }
      }
      catch (PSModelException e)
      {
         PSWorkbenchPlugin.handleException(
            PSMessages.getString(
            "PSActionMenuUsageComposite.error.catalogingMenuContexts"), //$NON-NLS-1$
            PSMessages.getString("common.error.title"), //$NON-NLS-1$
            e.getLocalizedMessage(),
            e);
      }
      
      return results;
   }
   
   //Controls
   private Label m_usageLabel;
   private PSSlushBucket m_contextSlushBucket;
   private List m_usageList;
   private ListViewer m_usagelistViewer;
   
   private PSEditorBase m_editor;   
   

}
