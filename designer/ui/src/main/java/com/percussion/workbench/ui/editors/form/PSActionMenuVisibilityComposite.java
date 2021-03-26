/******************************************************************************
 *
 * [ PSActionMenuVisibilityComposite.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form;

import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSCoreUtils;
import com.percussion.client.PSModelException;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.models.IPSCmsModel;
import com.percussion.cms.objectstore.PSAction;
import com.percussion.cms.objectstore.PSActionVisibilityContext;
import com.percussion.cms.objectstore.PSActionVisibilityContexts;
import com.percussion.utils.types.PSPair;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.PSWorkbenchPlugin;
import com.percussion.workbench.ui.controls.PSAbstractLabelProvider;
import com.percussion.workbench.ui.controls.PSDefaultContentProvider;
import com.percussion.workbench.ui.controls.PSReferenceLabelProvider;
import com.percussion.workbench.ui.controls.PSSlushBucket;
import com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater;
import org.eclipse.jface.viewers.ILabelProvider;
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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class PSActionMenuVisibilityComposite extends Composite
   implements IPSDesignerObjectUpdater
{   
   
   /**
    * Create the composite
    * @param parent
    * @param style
    */
   public PSActionMenuVisibilityComposite(
      Composite parent, int style, PSEditorBase editor, int page)
   {
      super(parent, style);
      setLayout(new FormLayout());
      m_editor = editor;
      
      m_visibilityContextsLabel = new Label(this, SWT.NONE);
      final FormData formData = new FormData();
      formData.top = new FormAttachment(0, 0);
      formData.left = new FormAttachment(0, 50);
      m_visibilityContextsLabel.setLayoutData(formData);
      m_visibilityContextsLabel.setText(PSMessages.getString(
         "PSActionMenuVisibilityComposite.visibilityContexts.label"));  //$NON-NLS-1$
      
      m_visibilityContextsViewer = new ListViewer(this, SWT.BORDER);
      m_visibilityContextsViewer.setContentProvider(new PSDefaultContentProvider());
      m_visibilityContextsViewer.setLabelProvider(new PSReferenceLabelProvider(true));
      m_visibilityContextsViewer.setSorter(new ViewerSorter());
      m_visibilityContextsList = m_visibilityContextsViewer.getList();
      final FormData formData_1 = new FormData();
      formData_1.right = new FormAttachment(100, -50);
      formData_1.top = new FormAttachment(m_visibilityContextsLabel, 0, SWT.BOTTOM);
      formData_1.left = new FormAttachment(m_visibilityContextsLabel, 0, SWT.LEFT);
      formData_1.height = 185;
      m_visibilityContextsList.setLayoutData(formData_1);
      m_visibilityContextsViewer.setInput(getVisibilityContexts());
      m_visibilityContextsList.select(0);
      m_visibilityContextsList.addSelectionListener(new SelectionAdapter()
         {

            /* 
             * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(
             * org.eclipse.swt.events.SelectionEvent)
             */
            @SuppressWarnings("synthetic-access") //$NON-NLS-1$
            @Override
            public void widgetSelected(@SuppressWarnings("unused") SelectionEvent e)
            {
               loadMappingsControl();
            }
            
         });
      
      ILabelProvider labelProvider = new PSAbstractLabelProvider()
      {

         @SuppressWarnings("unchecked") //$NON-NLS-1$
         public String getText(Object element)
         {
            if(element == null)
               return ""; //$NON-NLS-1$
            PSPair<String, String> pair = (PSPair<String, String>)element;
            return pair.getSecond();
         }
         
      };
      m_mappingsSlushBucket = new PSSlushBucket(
         this, SWT.NONE, PSMessages.getString(
            "PSActionMenuVisibilityComposite.hide.label"),  //$NON-NLS-1$
            PSMessages.getString(
               "PSActionMenuVisibilityComposite.show.label"),  //$NON-NLS-1$
         labelProvider);
      final FormData formData_2 = new FormData();
      formData_2.height = 175;
      formData_2.left = new FormAttachment(m_visibilityContextsList, 0, SWT.LEFT);
      formData_2.right = new FormAttachment(m_visibilityContextsList, 0, SWT.RIGHT);
      formData_2.top = new FormAttachment(m_visibilityContextsList, 15, SWT.BOTTOM);
      m_mappingsSlushBucket.setLayoutData(formData_2);
      editor.registerControl(
         PSMessages.getString("PSActionMenuVisibilityComposite.show.label"),  //$NON-NLS-1$
         m_mappingsSlushBucket,
         null,
         page);
      
      
      //
   }
   
   /** 
    * Loads the visibility context mappings slush bucket control with the 
    * appropriate values based on the current context selected.
    */
   private void loadMappingsControl()
   {
      PSAction action = (PSAction)m_editor.m_data;
      // Get the selected mode
      IStructuredSelection selection = 
         (IStructuredSelection)m_visibilityContextsViewer.getSelection();
      IPSReference ctxRef = (IPSReference)selection.getFirstElement();
      PSActionVisibilityContexts contexts = action.getVisibilityContexts();
      // Get all available options for this context
      java.util.List<PSPair<String, String>> avail = 
         new ArrayList<PSPair<String, String>>();
      Map<String, PSPair<String, String>> availMap = 
         new HashMap<String, PSPair<String, String>>();
      for(PSPair<String, String> pair : getVCMap().get(ctxRef))
      {
         avail.add(pair);
         availMap.put(pair.getFirst(), pair);
      }
      // Get the selected options for this context
      java.util.List<PSPair<String, String>> selected = 
         new ArrayList<PSPair<String, String>>(avail);      
      Iterator it = contexts.iterator();
      while(it.hasNext())
      {
         PSActionVisibilityContext context = 
            (PSActionVisibilityContext)it.next();
         if(context.getName().equals(String.valueOf(ctxRef.getId().getUUID())))
         {
            Iterator cIt = context.iterator();
            while(cIt.hasNext())
            {               
               selected.remove(availMap.get(cIt.next()));
            }
         }         
      }
      
      m_mappingsSlushBucket.setValues(avail, selected);
   }
   
   /**
    * Sets the mode context values on the passed in action for the currently
    * selected mode.
    * @param action assumed not <code>null</code>.
    */
   @SuppressWarnings("unchecked") //$NON-NLS-1$
   private void setContextOptions(PSAction action)
   {
      // Get the selected mode
      IStructuredSelection selection = 
         (IStructuredSelection)m_visibilityContextsViewer.getSelection();
      IPSReference ctxRef = (IPSReference)selection.getFirstElement();      
      // Get selected contexts from the control
      java.util.List<PSPair<String, String>> available = 
         m_mappingsSlushBucket.getAvailable();
      // Get all the selected contexts for this mode
      PSActionVisibilityContexts coll = action.getVisibilityContexts();
      
      //Remove all contexts for this mode
      java.util.List<PSActionVisibilityContext> removeList = 
         new ArrayList<PSActionVisibilityContext>();
      Iterator it = coll.iterator();
      while(it.hasNext())
      {
         PSActionVisibilityContext mapping = 
            (PSActionVisibilityContext)it.next();
         if(String.valueOf(ctxRef.getId().getUUID()).equals(mapping.getName()))
         {
            removeList.add(mapping);
         }
      }
      for(PSActionVisibilityContext mapping : removeList)
         coll.remove(mapping);
      
      //Add selected contexts for this mode
      for(PSPair<String, String> pair : available)
      {         
         coll.addContext(String.valueOf(ctxRef.getId().getUUID()),
            pair.getFirst());
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
      setContextOptions(action);
   }
   
   /**
    * sets intial control values
    */
   public void loadControlValues(@SuppressWarnings("unused") Object designObject)
   {
      loadMappingsControl();
   }
   
   /**
    * Helper method to retrieve all of the visibility contexts 
    * @return never <code>null</code>, may be empty.
    */
   private java.util.List<IPSReference> getVisibilityContexts()
   {      
      java.util.List<IPSReference> results = new ArrayList<IPSReference>();
      try
      {
         java.util.List<IPSReference> refs = 
            PSCoreUtils.catalog(PSObjectTypes.UI_ACTION_MENU_MISC, false);
         for(IPSReference ref : refs)
         {
            if(ref.getObjectType().getSecondaryType() ==
               PSObjectTypes.UiActionMenuMiscSubTypes.VISIBILITY_CONTEXTS)
            {
               results.add(ref);
            }
         }
      }
      catch (PSModelException e)
      {
         PSWorkbenchPlugin.handleException(
            PSMessages.getString("PSActionMenuVisibilityComposite.catVisibilityContexts.msg"), //$NON-NLS-1$
            PSMessages.getString("common.error.title"), //$NON-NLS-1$
            e.getLocalizedMessage(),
            e);
      }
      
      return results;
   }
   
   /**
    * Helper method to retrieve all of the visibility context options 
    * @return never <code>null</code>, may be empty.
    */
   @SuppressWarnings("unchecked") //$NON-NLS-1$
   private Map<IPSReference, Collection<PSPair<String, String>>> getVCMap()
   {
      if(m_vcMap == null)
      {
         m_vcMap = 
            new HashMap<IPSReference, Collection<PSPair<String, String>>>();
         try
         {
            IPSCmsModel model = 
               PSCoreFactory.getInstance().getModel(
                  PSObjectTypes.UI_ACTION_MENU_MISC);
            java.util.List<IPSReference> contexts = getVisibilityContexts();
            for(IPSReference ref : contexts)
            {
               Object obj = model.load(ref, false, false);
               m_vcMap.put(ref, (Collection<PSPair<String, String>>)obj);
            }
         }
         catch (Exception e)
         {
           PSWorkbenchPlugin.handleException(
               PSMessages.getString("PSActionMenuVisibilityComposite.catVisibilityContextsOpts.msg"), //$NON-NLS-1$
               PSMessages.getString("common.error.title"), //$NON-NLS-1$
               e.getLocalizedMessage(),
               e);
         }
      }
      return m_vcMap;
   }  
   
   //Controls
   private Label m_visibilityContextsLabel;
   private PSSlushBucket m_mappingsSlushBucket;   
   private List m_visibilityContextsList;
   private ListViewer m_visibilityContextsViewer;
   
   //Locally cache the vc options map
   Map<IPSReference, Collection<PSPair<String, String>>> m_vcMap;
   
   private PSEditorBase m_editor;
   
  
   

}
