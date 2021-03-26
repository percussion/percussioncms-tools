/******************************************************************************
 *
 * [ PSViewStandardComposite.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form;

import com.percussion.client.PSCoreFactory;
import com.percussion.cms.objectstore.PSSearch;
import com.percussion.workbench.ui.IPSUiConstants;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.controls.PSNameLabelDesc;
import com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater;
import com.percussion.workbench.ui.editors.common.PSEditorUtil;
import com.percussion.workbench.ui.validators.PSControlValidatorFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import java.util.ArrayList;
import java.util.List;

/**
 * Composite containing the controls for editing a standard view
 */
public class PSViewStandardComposite extends Composite
   implements IPSDesignerObjectUpdater, IPSUiConstants
{
   /**
    * Create the composite
    * 
    * @param parent
    * @param editor
    * @param useExternalSearch <code>true</code> if the search being edited
    * uses the external (FTS) search engine, <code>false</code> if not
    */
   public PSViewStandardComposite(
      Composite parent, PSEditorBase editor, boolean useExternalSearch)
   {
      super(parent, SWT.EMBEDDED);
      setLayout(new FormLayout());
      if(editor == null)
         throw new IllegalArgumentException("Editor cannot be null."); //$NON-NLS-1$
      m_editor = editor;
      createControl(useExternalSearch);      
   }
   
   /**
    * Creates and lays out all controls for this composite
    * 
    * @param useExternalSearch <code>true</code> if the external search engine
    * is being used, false otherwise
    */
   private void createControl(boolean useExternalSearch)
   {      
      PSControlValidatorFactory vFactory = 
         PSControlValidatorFactory.getInstance();
      vFactory.getRequiredValidator();
      // This control can register itself with the editor
      m_commonComp = 
         new PSNameLabelDesc(this, SWT.NONE, PSMessages.getString(
            "PSViewStandardComposite.standardView.label"), 0,  //$NON-NLS-1$
            PSNameLabelDesc.LAYOUT_SIDE |
            PSNameLabelDesc.SHOW_ALL |
            PSNameLabelDesc.NAME_READ_ONLY |
            PSNameLabelDesc.LABEL_USES_NAME_PREFIX, m_editor);
      final FormData formData = new FormData();
      formData.top = new FormAttachment(0, 0);
      formData.right = new FormAttachment(100, 0);
      formData.left = new FormAttachment(0,0);
      m_commonComp.setLayoutData(formData);
                  
      if (useExternalSearch)
      {
         m_queryComp = new PSSearchQueryComposite(this, SWT.NONE, m_editor);
         final FormData formData_0 = new FormData();      
         formData_0.top = new FormAttachment(m_commonComp, 10, SWT.BOTTOM);
         formData_0.left = new FormAttachment(0, 0);
         formData_0.right = new FormAttachment(50, -20);
         m_queryComp.setLayoutData(formData_0);
      }
      
      m_resultsComp = new PSSearchResultsComposite(this, SWT.NONE, m_editor);
      final FormData formData_1 = new FormData();
      if (useExternalSearch)
         formData_1.top = new FormAttachment(m_queryComp, 5, SWT.BOTTOM);
      else
         formData_1.top = new FormAttachment(m_commonComp, 35, SWT.BOTTOM);
      
      formData_1.left = new FormAttachment(0, 0);
      formData_1.right = new FormAttachment(50, -20);
      m_resultsComp.setLayoutData(formData_1);
      
      m_parentCatComp = new PSViewParentCategoryComposite(this, m_editor, false);
      final FormData formData_7 = new FormData();
      formData_7.top = new FormAttachment(m_resultsComp, 5, SWT.BOTTOM);
      formData_7.left = new FormAttachment(m_resultsComp, 0, SWT.LEFT);
      formData_7.right = new FormAttachment(m_resultsComp, 0, SWT.RIGHT);
      m_parentCatComp.setLayoutData(formData_7);
      
      m_searchCriteriaLabel = new Label(this, SWT.NONE);
      final FormData formData_4 = new FormData();
      formData_4.top = new FormAttachment(m_commonComp, 10, SWT.BOTTOM);
      formData_4.left = new FormAttachment(50, 0);
      m_searchCriteriaLabel.setLayoutData(formData_4);
      m_searchCriteriaLabel.setText(
         PSMessages.getString("common.searchCriteria.label")); //$NON-NLS-1$
      
      //Search criteria panel
      PSCoreFactory factory = PSCoreFactory.getInstance();
      PSSearch search = (PSSearch)m_editor.m_data;
      m_queryEditor = new PSSearchFieldEditorComposite(
         this, search, factory.getRemoteRequester(),
         PSEditorUtil.getCEFieldCatalog(false), m_editor);      
      final FormData formData_5 = new FormData();
      formData_5.top = new FormAttachment(m_searchCriteriaLabel, 0, SWT.BOTTOM); 
      formData_5.left = new FormAttachment(m_searchCriteriaLabel, 0, SWT.LEFT);
      formData_5.right = new FormAttachment(100, 0);
      formData_5.bottom = new FormAttachment(100, -5);
      m_queryEditor.setLayoutData(formData_5);      
      
      final Button customButton = new Button(this, SWT.PUSH);
      customButton.setText(PSMessages.getString(
         "common.customizeButton.label")); //$NON-NLS-1$
      final FormData formData_6 = new FormData();
      formData_6.right = new FormAttachment(m_queryEditor, -5, SWT.LEFT);
      formData_6.bottom = new FormAttachment(m_queryEditor, 0, SWT.BOTTOM);
      customButton.setLayoutData(formData_6);
      customButton.addSelectionListener(new SelectionAdapter()
         {

            /* 
             * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(
             * org.eclipse.swt.events.SelectionEvent)
             */
            @SuppressWarnings("synthetic-access") //$NON-NLS-1$
            @Override
            public void widgetSelected(@SuppressWarnings("unused") SelectionEvent e)
            {               
               m_queryEditor.onCustomize();
            }
            
         });
      List<Control> tabCtrls = new ArrayList<Control>();
      tabCtrls.add(m_commonComp);
      if (m_queryComp != null)
         tabCtrls.add(m_queryComp);
      tabCtrls.add(m_resultsComp);
      tabCtrls.add(m_parentCatComp);
      tabCtrls.add(customButton);
      setTabList(tabCtrls.toArray(new Control[tabCtrls.size()]));
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
   public void updateDesignerObject(Object designObject, Object control)
   {
      PSSearch def = (PSSearch)designObject;
      
      if(control == m_commonComp.getLabelText())
      {
         def.setDisplayName(m_commonComp.getLabelText().getText());
      }
      else if(control == m_commonComp.getDescriptionText())
      {
         def.setDescription(m_commonComp.getDescriptionText().getText());
      }
      else if(control == m_queryEditor)
      {
         m_queryEditor.updateDesignerObject(designObject, control);
      }
      else
      {
         if (m_queryComp != null)
            m_queryComp.updateDesignerObject(designObject, control);

         m_resultsComp.updateDesignerObject(designObject, control);
         
         m_parentCatComp.updateDesignerObject(designObject, control);
      }
   }
   
   /* 
    * @see com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater#
    * loadControlValues(java.lang.Object)
    */
   public void loadControlValues(Object designObject)
   {
      PSSearch def = (PSSearch)designObject;
      
      // Set name/label/description
      ((Label)m_commonComp.getNameText()).setText(def.getInternalName());
      m_commonComp.getLabelText().setText(def.getDisplayName());
      m_commonComp.getDescriptionText().setText(def.getDescription());
      
      m_resultsComp.loadControlValues(designObject);
      
      // Set search query panel
      m_queryEditor.loadControlValues(def);
      
      if (m_queryComp != null)
         m_queryComp.loadControlValues(def);
      
      m_parentCatComp.loadControlValues(def);
   }
   
     
   
   
   private PSEditorBase m_editor;  
   
   //Controls
   private PSNameLabelDesc m_commonComp;
   private Label m_searchCriteriaLabel;
   private PSSearchFieldEditorComposite m_queryEditor;

   /**
    * Composite containing controls for editing the external (FTS) search
    * values, intialized during construction, will be <code>null</code> if the
    * view will not support FTS features.  See 
    * {@link #PSViewStandardComposite(Composite, PSEditorBase, boolean)} for
    * details on the <code>useExternalSearch</code> param.
    */
   private PSSearchQueryComposite m_queryComp = null;

   /**
    * Composite containing controls for editing the search result settings,
    * intialized during construction, never <code>null</code> or modified
    * after that.
    */
   private PSSearchResultsComposite m_resultsComp;
   
   /**
    * Composite that contains the parent categories drop down
    * and functionality. Initialized during construction, never
    * <code>null</code> or modified after that.
    */
   private PSViewParentCategoryComposite m_parentCatComp;
}
