/******************************************************************************
 *
 * [ PSSlotEditor.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form;

import com.percussion.E2Designer.DTTextLiteral;
import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSCoreUtils;
import com.percussion.client.PSModelChangedEvent;
import com.percussion.client.PSModelChangedEvent.ModelEvents;
import com.percussion.client.PSModelException;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.models.IPSCmsModel;
import com.percussion.client.models.IPSContentTypeModel;
import com.percussion.client.models.IPSModelListener;
import com.percussion.client.models.PSLockException;
import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.design.objectstore.IPSReplacementValue;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.PSExtensionDef;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.IPSTemplateSlot.SlotType;
import com.percussion.services.assembly.data.PSAssemblyTemplate;
import com.percussion.services.assembly.data.PSTemplateSlot;
import com.percussion.services.assembly.data.PSTemplateTypeSlotAssociation;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSDesignGuid;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.types.PSPair;
import com.percussion.workbench.ui.IPSUiConstants;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.PSWorkbenchPlugin;
import com.percussion.workbench.ui.controls.IPSNewRowObjectProvider;
import com.percussion.workbench.ui.controls.PSAbstractLabelProvider;
import com.percussion.workbench.ui.controls.PSAbstractTableLabelProvider;
import com.percussion.workbench.ui.controls.PSComboBoxCellEditor;
import com.percussion.workbench.ui.controls.PSDefaultContentProvider;
import com.percussion.workbench.ui.controls.PSElipseButtonComboComposite;
import com.percussion.workbench.ui.controls.PSNameLabelDesc;
import com.percussion.workbench.ui.controls.PSRadioAndCheckBoxes;
import com.percussion.workbench.ui.controls.PSReferenceLabelProvider;
import com.percussion.workbench.ui.controls.PSSortableTable;
import com.percussion.workbench.ui.editors.dialog.PSExtensionParamsDialog;
import com.percussion.workbench.ui.util.PSControlInfo;
import com.percussion.workbench.ui.util.PSHelpHintKeyHelper;
import com.percussion.workbench.ui.util.PSReferenceComparator;
import com.percussion.workbench.ui.util.PSUiUtils;
import com.percussion.workbench.ui.validators.IPSControlValueValidator;
import com.percussion.workbench.ui.validators.PSControlValidatorFactory;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Label;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Provides a single paned UI for modifying a slot design object. 
 * 
 * @version 6.0
 * @author Paul Howard
 */
public class PSSlotEditor extends PSEditorBase implements IPSUiConstants
{
      
   // see base class
   @Override
   @SuppressWarnings({"unchecked", "synthetic-access"}) //$NON-NLS-1$ //$NON-NLS-2$
   public void createControl(Composite parent)
   {
      final Composite comp = new Composite(parent, SWT.NONE);
      comp.setLayout(new FormLayout());
      comp.setSize(720,420); 
      PSControlValidatorFactory vFactory = 
         PSControlValidatorFactory.getInstance();
      IPSControlValueValidator required = vFactory.getRequiredValidator();
      
      // Add the comon composite that contains name, label, description
      // fields. This control can register itself with the editor.
      m_commonComp = new PSNameLabelDesc(comp, SWT.NONE, 
         PSMessages.getString("PSSlotEditor.slotName.label"), //$NON-NLS-1$
         EDITOR_LABEL_NUMERATOR, 
         PSNameLabelDesc.SHOW_ALL 
            | PSNameLabelDesc.NAME_READ_ONLY
            |PSNameLabelDesc.LAYOUT_SIDE, this);      
            
      // Add the types combo control
      m_typeComp = new PSRadioAndCheckBoxes(comp, 
         PSMessages.getString("PSSlotEditor.type.label"), //$NON-NLS-1$
         SWT.HORIZONTAL | SWT.SEPARATOR);
      m_typeComp.addEntry(PSMessages.getString("PSSlotEditor.type.regular.choice"), "Regular"); //$NON-NLS-1$ //$NON-NLS-2$
      m_typeComp.addEntry(PSMessages.getString("PSSlotEditor.type.inline.choice"), "Inline"); //$NON-NLS-1$ //$NON-NLS-2$
      m_typeComp.layoutControls();        
      Map typeHint = new HashMap(1);
      typeHint.put(CHANGE_HINT_TYPE, ""); //$NON-NLS-1$
      registerControl("PSSlotEditor.type.label", m_typeComp,  //$NON-NLS-1$
         new IPSControlValueValidator[]{required},
         typeHint);
      
      // Add the allowed relationship types label
      final Label allowedRelTypesLabel = new Label(comp, SWT.WRAP);
      allowedRelTypesLabel.setAlignment(SWT.RIGHT);
      allowedRelTypesLabel.setText(
         PSMessages.getString("PSSlotEditor.allowedRelTypes.label")); //$NON-NLS-1$
      
      //  Add the allowed relationship types control
      m_relTypeComboViewer = new ComboViewer(comp, SWT.READ_ONLY);
      m_relTypeCombo = m_relTypeComboViewer.getCombo();           
      m_relTypeComboViewer.setContentProvider(new PSDefaultContentProvider());
      m_relTypeComboViewer.setLabelProvider(new PSReferenceLabelProvider());     
      
      // Add the content finder label
      final Label contentFinderLabel = new Label(comp, SWT.WRAP);
      contentFinderLabel.setAlignment(SWT.RIGHT);      
      contentFinderLabel.setText(PSMessages.getString(
         "PSSlotEditor.contentFinder.label")); //$NON-NLS-1$
      
      // Add the content finder combo and button control
      final PSElipseButtonComboComposite elipseButtonComboComposite = 
         new PSElipseButtonComboComposite(comp, SWT.READ_ONLY);            
      
      m_contentFinderComboViewer = elipseButtonComboComposite.getComboViewer();
      m_contentFinderCombo = m_contentFinderComboViewer.getCombo();
      m_contentFinderCombo.addSelectionListener(new SelectionAdapter()
         {
            /* 
             * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(
             * org.eclipse.swt.events.SelectionEvent)
             */
            @Override
            public void widgetSelected(@SuppressWarnings("unused") SelectionEvent e) //$NON-NLS-1$
            {
               handleBrowseButtonEnable();               
            }
         
         });
      m_browseButton = elipseButtonComboComposite.getButton();
      m_contentFinderComboViewer.setContentProvider(new PSDefaultContentProvider());
      m_contentFinderComboViewer.setLabelProvider(new PSAbstractLabelProvider()
         {

            public String getText(Object element)
            {
               if(element instanceof PSExtensionDef)
               {
               PSExtensionDef def = (PSExtensionDef)element;
               return StringUtils.defaultString(def.getRef().getExtensionName());
               }
               else if(element instanceof String)
               {
                  return (String)element;
               }
               return ""; //$NON-NLS-1$
            }
         
         });     
        
      /**
       * A selectionlistener is added to the content finder combo
       * so the finder arguments are cleared when a new selection is
       * made.
       */
      m_contentFinderCombo.addSelectionListener(new SelectionAdapter()
         {

            @Override
            public void widgetSelected(@SuppressWarnings("unused") SelectionEvent e) //$NON-NLS-1$
            {
               m_finderArgs.clear();               
            }         
         });
      /*
       * A selection listener is added to launch the extension params
       * dialog if a selection exists in the content finder combo
       */
      m_browseButton.addSelectionListener(new SelectionAdapter()
         {

            @Override
            public void widgetSelected(@SuppressWarnings("unused") SelectionEvent e) //$NON-NLS-1$
            {
               if(m_contentFinderCombo.getSelectionIndex() < 1)
                  return;               
               
               IStructuredSelection selection = 
                  (IStructuredSelection)m_contentFinderComboViewer.getSelection();
               IPSExtensionDef def = (IPSExtensionDef)selection.getFirstElement();
               
               Map<String, IPSReplacementValue> params = 
                  new HashMap<String, IPSReplacementValue>();
               Set<String> keys = m_finderArgs.keySet();
               DTTextLiteral dt = new DTTextLiteral();
               for(String key : keys)
               {
                  params.put(key, (IPSReplacementValue)dt.create(
                     m_finderArgs.get(key)));
               }
               
               PSExtensionParamsDialog dialog = 
                  new PSExtensionParamsDialog(comp.getShell(), def, params,
                     PSExtensionParamsDialog.MODE_TEXT_LITERAL_ONLY);
                              
               if( dialog.open() == Dialog.OK)
               {
                 m_finderArgs.clear();
                 for(PSPair<String, IPSReplacementValue> pair : dialog.getParamValues())
                 {
                   if(pair.getSecond() != null &&
                      StringUtils.isNotBlank(pair.getSecond().getValueText()))
                       m_finderArgs.put(StringUtils.defaultString(pair.getFirst()),
                          StringUtils.defaultString(pair.getSecond().getValueText()));
                 }
                 updateDesignerObject(m_data, m_contentFinderCombo);
                 setDirty();
                  
               }
            } 
         
         });     
         
      // Add the allowed content type label
      final Label allowedContentLabel = new Label(comp, SWT.WRAP);      
      allowedContentLabel.setText(
         PSMessages.getString("PSSlotEditor.allowedContent.label")); //$NON-NLS-1$
      
      // Add the allowed content type table
      createTable(comp);
      Map assocHint = new HashMap(1);
      assocHint.put(CHANGE_HINT_ASSOCIATION, "templates"); //$NON-NLS-1$
      registerControl(
         "PSSlotEditor.allowedContent.label", m_ctTable, null, //$NON-NLS-1$
         assocHint);
      
      // Layout all the controls
      final FormData formData_14 = new FormData();
      formData_14.left = new FormAttachment(0, 0);
      formData_14.right = 
         new FormAttachment(100, 0);
      formData_14.top = new FormAttachment(0, 0);
      m_commonComp.setLayoutData(formData_14);
      
      // Layout the items in the right comp bottom to top
      
      // Type
      final FormData formData_5 = new FormData();
      formData_5.right = new FormAttachment(40, 0);
      formData_5.top = 
         new FormAttachment(m_commonComp, 40, SWT.BOTTOM);
      formData_5.left = 
         new FormAttachment(0, 0);
      m_typeComp.setLayoutData(formData_5);
      
      // Allowed rel type label
      final FormData formData_6 = new FormData();
      formData_6.left = new FormAttachment(0, 0);      
      formData_6.top = 
         new FormAttachment(allowedContentLabel, 0, SWT.TOP);      
      allowedRelTypesLabel.setLayoutData(formData_6);
      
      // Allowed rel type combo
      final FormData formData_7 = new FormData();      
      formData_7.right = new FormAttachment(m_typeComp, 0, SWT.RIGHT);
      formData_7.top = new FormAttachment(allowedRelTypesLabel, 0, SWT.BOTTOM);
      formData_7.left = new FormAttachment(0, 0);
      m_relTypeCombo.setLayoutData(formData_7);      
          
      // Content finder label
      final FormData formData_8 = new FormData();
      formData_8.left = new FormAttachment(m_typeComp, 15, SWT.RIGHT);     
      formData_8.top = new FormAttachment(m_typeComp, 0, SWT.TOP);      
      contentFinderLabel.setLayoutData(formData_8);
      
      // Content finder combo
      final FormData formData = new FormData();
      formData.right = new FormAttachment(100, 0);
      formData.top = new FormAttachment(contentFinderLabel, 0, SWT.BOTTOM);
      formData.left = new FormAttachment(contentFinderLabel, 0, SWT.LEFT);
      elipseButtonComboComposite.setLayoutData(formData);
      
      // Allowed content label
      final FormData formData_11 = new FormData();
      formData_11.top = 
         new FormAttachment(elipseButtonComboComposite, 10, SWT.BOTTOM);
      formData_11.left = new FormAttachment(elipseButtonComboComposite, 0, SWT.LEFT);
      allowedContentLabel.setLayoutData(formData_11);
      
      // Allowed content table
      final FormData formData_12 = new FormData();
      formData_12.height = 120;
      formData_12.right = 
         new FormAttachment(100, 0);
      formData_12.top = new FormAttachment(allowedContentLabel, 0, SWT.BOTTOM);
      formData_12.left = new FormAttachment(allowedContentLabel, 0, SWT.LEFT);
      m_ctTable.setLayoutData(formData_12);     
      
      // load all the initial values into the combo controls      
      loadRelTypesControl();
      loadContentFinderControl(); 
      
      // Set the tab ordering
      Control[] tabList = new Control[]{
         m_commonComp,
         m_typeComp,
         elipseButtonComboComposite,
         m_relTypeCombo,
         m_ctTable
      };
      comp.setTabList(tabList);
      addObjectDeleteListener();
      registerControl(
            "PSSlotEditor.allowedRelTypes.label", m_relTypeComboViewer,  //$NON-NLS-1$
            new IPSControlValueValidator[]{required});      
      registerControl(
            "PSSlotEditor.contentFinder.label", m_contentFinderComboViewer, null);       //$NON-NLS-1$
   }
   
   /* 
    * @see com.percussion.workbench.ui.editors.form.PSEditorBase
    * #isValidReference(com.percussion.client.IPSReference)
    */
   @Override
   public boolean isValidReference(IPSReference ref)
   {
      if(ref == null)
         return false; // Should never happen
      if(ref.getObjectType().getPrimaryType() == PSObjectTypes.SLOT)
         return true;
      return false;
   }
  
   /* 
    * @see com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater#
    * updateDesignerObject(java.lang.Object, java.lang.Object)
    */
   @SuppressWarnings("unchecked") //$NON-NLS-1$
   public void updateDesignerObject(Object designObject, Object control)
   {
      // Cast the design object
      PSTemplateSlot slot = (PSTemplateSlot)designObject;
            
      if(control == m_commonComp.getLabelText())
      {
         slot.setLabel(m_commonComp.getLabelText().getText().trim());
      }
      else if(control == m_commonComp.getDescriptionText())
      {
         slot.setDescription( 
            m_commonComp.getDescriptionText().getText().trim());
      }
      else if(control == m_typeComp)
      {
        String val = m_typeComp.getButtonValue(m_typeComp.getSelectedIndex());
        slot.setSlottype(SlotType.valueOf(val.toUpperCase()));
      }
      else if(
         control == m_relTypeCombo)
      {
         int selection = m_relTypeCombo.getSelectionIndex();
         if(selection == -1)
            return;
         List<IPSReference> data = 
            (List<IPSReference>)m_relTypeComboViewer.getInput();
         slot.setRelationshipName(data.get(selection).getName());
      }
      else if(
         control == m_contentFinderCombo)
      {
         String finderName = null;
         int selection = m_contentFinderCombo.getSelectionIndex();
         if(selection > 0)
         {
            List data = 
               (List)m_contentFinderComboViewer.getInput();
            finderName = ((PSExtensionDef)data.get(selection)).getRef().toString();
         }
         else
         {
            m_finderArgs.clear();
         }
         slot.setFinderName(finderName);        
         slot.setFinderArguments(m_finderArgs);
      }
      else if(
         control == m_ctTable)
      {
         List<PSPair<IPSGuid, IPSGuid>> values = 
            (List<PSPair<IPSGuid, IPSGuid>>)m_ctTable.getValues();         
         slot.setSlotAssociations(values);
      }
      
   }

  
   
   /**
    * Create the allowed content types table and load it
    * @param comp the parent component,assumed not <code>null</code>.
    */
   private void createTable(final Composite comp)
   {
      // Create the label provider for this table
      ITableLabelProvider labelProvider = new PSAbstractTableLabelProvider()
         {
            
            @SuppressWarnings({"unchecked","synthetic-access"}) //$NON-NLS-1$ //$NON-NLS-2$
            public String getColumnText(Object element, int columnIndex)
            {
               PSPair<IPSGuid, IPSGuid> pair = 
                  (PSPair<IPSGuid, IPSGuid>)element;
               IPSGuid guid = null;
               switch(columnIndex)
               {
                  case 0:
                     guid = pair.getFirst();
                     if(guid == null)
                        return ""; //$NON-NLS-1$
                     IPSReference ref = 
                        PSUiUtils.getReferenceByGuid(
                           getContentTypes(), guid);
                     String refVal = ""; //$NON-NLS-1$
                     if(ref == null)
                     {
                        return ""; //$NON-NLS-1$
                     }
                     else
                        refVal = ref.getName();
                     return refVal;
                  case 1:
                     guid = pair.getSecond();
                     if(guid == null)
                        return ""; //$NON-NLS-1$
                     IPSReference tRef = 
                        PSUiUtils.getReferenceByGuid(getAllowedTemplates(null), guid);
                     String val = ""; //$NON-NLS-1$
                     if(tRef == null)
                     {
                        return ""; //$NON-NLS-1$
                     }
                     else
                        val = tRef.getName();
                     return val;
                  
               }
               return ""; // should never get here //$NON-NLS-1$
            }
         
         };
         
      // Create the new row object provider for this table
      IPSNewRowObjectProvider newRowProvider = new IPSNewRowObjectProvider()
         {

            public Object newInstance()
            {
               return new PSPair<IPSGuid, IPSGuid>();
            }

            @SuppressWarnings("unchecked") //$NON-NLS-1$
            public boolean isEmpty(Object obj)
            {
               if(!(obj instanceof PSPair))
                  throw new IllegalArgumentException(
                     "The passed in object must be an instance of PSPair."); //$NON-NLS-1$
               PSPair pair = (PSPair)obj;
               return (pair.getFirst() == null 
                  || pair.getSecond() == null);
               
            }
         
         };         
         
      m_ctTable = new PSSortableTable(
         comp, labelProvider, newRowProvider, SWT.NONE, 
         PSSortableTable.INSERT_ALLOWED
          | PSSortableTable.DELETE_ALLOWED);
      m_ctTable.setCellModifier(new ContentTypeTemplateCellModifier(m_ctTable));
      // Create cell editors
      
       m_cTypeCellEditor = 
         new PSComboBoxCellEditor(
            m_ctTable.getTable(), new String[]{}, SWT.READ_ONLY);            
     
      m_cTypeCellEditor.setLabelProvider(new CellEditorLabelProvider(true));
            
      PSComboBoxCellEditor templateCellEditor = 
         new PSComboBoxCellEditor(
            m_ctTable.getTable(), new String[]{}, SWT.READ_ONLY);
      templateCellEditor.setLabelProvider(new CellEditorLabelProvider(false));
      
      
      // Add columns
      m_ctTable.addColumn("PSSlotEditor.contentType.columnname", //$NON-NLS-1$
         PSSortableTable.IS_SORTABLE,
         new ColumnWeightData(10,100, true), m_cTypeCellEditor, SWT.LEFT);
      
      m_ctTable.addColumn("PSSlotEditor.template.columnname", //$NON-NLS-1$
         PSSortableTable.IS_SORTABLE,
         new ColumnWeightData(10,100, true), templateCellEditor, SWT.LEFT);     
      
      
   }
      
   /* 
    * @see com.percussion.workbench.ui.editors.form.PSEditorBase
    * #loadControlValues()
    */
   @SuppressWarnings("unchecked") //$NON-NLS-1$
   public void loadControlValues(Object designObject)
   {
      PSTemplateSlot slot = (PSTemplateSlot)designObject;
      loadAllowedTemplatesMap(false);
      
      // Set name
      ((Label)m_commonComp.getNameText()).setText(slot.getName());      
      // Set label text
      m_commonComp.getLabelText().setText(
         StringUtils.defaultString(slot.getLabel()));
      // Set description
      m_commonComp.getDescriptionText().setText(
         StringUtils.defaultString(slot.getDescription())); 
                 
      // Set the slot type value
      int idx = m_typeComp.getIndexByValue(slot.getSlottypeEnum().toString());
      if(idx == -1)
      {
         // Set default
         m_typeComp.setSelection(0);
      }
      else
      {
         m_typeComp.setSelection(idx);
      }
      // Set Allowed Relationship value
      String relName = slot.getRelationshipName();
      if(!StringUtils.isBlank(relName))
      {
         m_relTypeCombo.select(
            PSUiUtils.getReferenceIndexByName(
               (List<IPSReference>)m_relTypeComboViewer.getInput(),
               relName));
      }
      
      // Set content finder value and arguments
      String finderName = slot.getFinderName();
      if(finderName != null && finderName.trim().length() > 0)
      {
         boolean found = false;
         List finders = (List)m_contentFinderComboViewer.getInput();
         int len = finders.size();
         int i = 0;
         for(i = 1; i < len; i++ )
         {
            PSExtensionDef def = (PSExtensionDef)finders.get(i);
            if(finderName.equals(def.getRef().toString()))
            {
               found = true;
               break;
            }
         }
         m_contentFinderCombo.select(found ? i : 0);
         // set finder args
         m_finderArgs = slot.getFinderArguments();
        
      }    
      
      loadCTypeTable();
      m_ctTable.sortColumn(0, true);
      handleBrowseButtonEnable();
      
   }
   
   /**
    * Loads data in the CTypeTable
    */
   @SuppressWarnings("unchecked") //$NON-NLS-1$
   private void loadCTypeTable()
   {
      PSTemplateSlot slot = (PSTemplateSlot)m_data;
      // Set content type choices in cell editor
      List<IPSGuid> types = new ArrayList<IPSGuid>();
      for(IPSReference ref : getAllowedContentTypes())
      {
         types.add(ref.getId());
      }
      m_cTypeCellEditor.setItems(types.toArray());
      
      // set content type / template table values
      List associations = (List)slot.getSlotAssociations();
      m_ctTable.setValues(associations);      
   }
   
   /**
    * Adds a listener to the model to listen for delete events on content
    * type and template objects.
    * When a delete occurs the template object is "fixed"
    * so that it will only contain existing content type/template pairs.
    * The list of available content types and templates is also updated.
    */
   private void addObjectDeleteListener()
   {
      try
      {
         m_modelListeners = new IPSModelListener[2];
         PSCoreFactory factory = PSCoreFactory.getInstance();
         IPSCmsModel ctypeModel = factory.getModel(PSObjectTypes.CONTENT_TYPE);
         IPSCmsModel templateModel = factory.getModel(PSObjectTypes.TEMPLATE);
         m_modelListeners[0] = new IPSModelListener()
         {
            
            @SuppressWarnings("synthetic-access") //$NON-NLS-1$
            public void modelChanged(
               @SuppressWarnings("unused") PSModelChangedEvent event) //$NON-NLS-1$
            {               
               Job job = new Job(PSMessages.getString("PSSlotEditor.updatingEditor.message")) //$NON-NLS-1$
               {

                  @Override
                  protected IStatus run(IProgressMonitor monitor)
                  {
                     monitor.beginTask(PSMessages.getString("PSSlotEditor.updateCTypeTable.message"), 10); //$NON-NLS-1$
                     validateData(false, true);
                     monitor.worked(5);
                     Display.getDefault().asyncExec(new Runnable()
                        {

                           public void run()
                           {
                              loadCTypeTable();                              
                           }
                        
                        });
                     monitor.done();
                     return Status.OK_STATUS;
                  }
                  
               };
               job.schedule();
                            
            }
         };
         ctypeModel.addListener(m_modelListeners[0],
            ModelEvents.DELETED.getFlag());
         
         m_modelListeners[1] = new IPSModelListener()
         {
            
            @SuppressWarnings("synthetic-access") //$NON-NLS-1$
            public void modelChanged(
               @SuppressWarnings("unused") PSModelChangedEvent event) //$NON-NLS-1$
            {         
               
               Job job = new Job(PSMessages.getString(
                  "PSSlotEditor.updatingEditor.message")) //$NON-NLS-1$
               {

                  @Override
                  protected IStatus run(IProgressMonitor monitor)
                  {
                     monitor.beginTask(PSMessages.getString(
                        "PSSlotEditor.updateCTypeTable.message"), 9); //$NON-NLS-1$
                     validateData(false, true);
                     monitor.worked(3);
                     loadAllowedTemplatesMap(true);
                     monitor.worked(6);
                     Display.getDefault().asyncExec(new Runnable()
                        {

                           public void run()
                           {
                              loadCTypeTable();                              
                           }
                        
                        });
                     monitor.done();
                     return Status.OK_STATUS;
                  }
                  
               };
               job.schedule();
            }
         };
         templateModel.addListener(m_modelListeners[1],
            ModelEvents.DELETED.getFlag());
      }
      catch (PSModelException e)
      {
         PSWorkbenchPlugin.handleException(null, null, null, e);         
      }
   }  

   
   
   /**
    * Load values in the allowed relationship types combo control
    */
   private void loadRelTypesControl()
   {
      PSCoreFactory factory = PSCoreFactory.getInstance();
      try
      {
         IPSCmsModel model = 
            factory.getModel(PSObjectTypes.RELATIONSHIP_TYPE);
         Collection<IPSReference> relTypes = model.catalog(false);
         m_relTypeComboViewer.setInput(relTypes);
      }
      catch (PSModelException e)
      {
         PSWorkbenchPlugin.handleException(
            PSMessages.getString("PSSlotEditor.error.catalogingRelTypes"),  //$NON-NLS-1$
            PSMessages.getString("common.error.title"), //$NON-NLS-1$
            e.getLocalizedMessage(),
            e);
      }
   }
   
   /**
    * load values into the content finder combo control
    */
   @SuppressWarnings("unchecked") //$NON-NLS-1$
   private void loadContentFinderControl()
   {    
       List defs = new ArrayList();
       defs.add(PSMessages.getString("PSSlotEditor.contentFinder.none.choice")); //$NON-NLS-1$
       defs.addAll(getSlotFinderExtensions());
       m_contentFinderComboViewer.setInput(defs);
       m_contentFinderCombo.select(0); // set default to none
   }      
   
   /**
    * Convenience method to return a <code>Collection</code> of
    * all known content types.
    * @return a collection of <code>IPSReference</code> objects that
    * represent the content types
    */
   @SuppressWarnings("unchecked") //$NON-NLS-1$
   private Collection<IPSReference> getContentTypes()
   {
      Collection<IPSReference> results = null;      
      try
      {         
         results = 
            PSCoreUtils.catalog(PSObjectTypes.CONTENT_TYPE, false);
      }
      catch (PSModelException e)
      {
         PSWorkbenchPlugin.handleException(
            PSMessages.getString("PSSlotEditor.error.gettingContentTypes"),  //$NON-NLS-1$
            PSMessages.getString("common.error.title"), //$NON-NLS-1$
            e.getLocalizedMessage(),
            e);
      }
      return results;
   }
   
   /**
    * Returns a list of content types that have one or more templates
    * associated to it.
    * @return never <code>null</code>, may be empty.
    */
   @SuppressWarnings("unchecked") //$NON-NLS-1$
   private List<IPSReference> getAllowedContentTypes()
   {
      List<IPSReference> results = new ArrayList<IPSReference>();
      Set<IPSReference> keys = m_allowedTemplates.keySet();
      results.addAll(keys);
      Collections.sort(results, new PSReferenceComparator());
      return results;
   }
   
   /**
    * Returns a list of allowed templates for the content type
    * indicated by the passed in reference. All Shared templates will
    * also be returned.
    * @param ref the reference of the content type that we will find
    * the allowed templates on.
    * @return list of template references, never <code>null</code>
    * but may be empty.
    */
   @SuppressWarnings("unchecked") //$NON-NLS-1$
   private List<IPSReference> getAllowedTemplates(IPSReference ref)
   {
      
      List<IPSReference> results = new ArrayList<IPSReference>();
      if(ref == null)
      {
         for(IPSReference key : m_allowedTemplates.keySet()) 
         {
            results.addAll(m_allowedTemplates.get(key));
         }
         Collections.sort(results, new PSReferenceComparator());
      }
      else if(m_allowedTemplates.containsKey(ref))
      {
         results.addAll(m_allowedTemplates.get(ref));
         Collections.sort(results, new PSReferenceComparator());
      }    
      
      return results;
   }
   
   /**
    * Utility method to load the template celleditor with the allowed and unused
    * template choices.
    * @param guid the guid for the content type that the template will be
    * associated with. Assumed not <code>null</code>.
    */
   @SuppressWarnings("unchecked") //$NON-NLS-1$
   private void loadTemplateColumn(IPSGuid guid, IPSGuid currentVal)
   {
      PSComboBoxCellEditor editor = 
         (PSComboBoxCellEditor)m_ctTable.getCellEditor(1);      
      List<IPSReference> temps = 
         getAllowedTemplates(PSUiUtils.getReferenceByGuid(getContentTypes(), guid));
      List<IPSGuid> used = new ArrayList<IPSGuid>();
      for(PSPair<IPSGuid, IPSGuid> pair : 
         (List<PSPair<IPSGuid, IPSGuid>>)m_ctTable.getValues())
      {
         if(guid.equals(pair.getFirst()))
            used.add(pair.getSecond());         
      }
         
      IPSGuid[] guids = new IPSGuid[temps.size()];
      int idx = 0;
      for(IPSReference ref : temps)
      {
         if(!used.contains(ref.getId()))
            guids[idx++] = ref.getId();
      }
      if(currentVal != null)
         guids[idx++] = currentVal;
      editor.setItems(guids);
   }
   
   /**
    * Loads the item definition specified by the passed in content type
    * reference. This object will be cached and retrived from the cache upon
    * further calls to this method.
    * @param ref the content type reference,assumed not<code>null</code>.
    * @return The <code>PSItemDefinition</code> for the specified
    * content type.
    */
   @SuppressWarnings("unused") //$NON-NLS-1$
   private PSItemDefinition getItemDef(IPSReference ref)
   {
      if(m_itemDefs.containsKey(ref))
         return m_itemDefs.get(ref);
      try
      {
         IPSCmsModel model = 
            PSCoreFactory.getInstance().getModel(PSObjectTypes.CONTENT_TYPE);
         PSItemDefinition def = (PSItemDefinition)model.load(ref, false, false);
         m_itemDefs.put(ref, def);
         return def;
      }
      catch (Exception e)
      {
         PSWorkbenchPlugin.handleException(
            PSMessages.getString("PSSlotEditor.error.gettingItemDef"),  //$NON-NLS-1$
            PSMessages.getString("common.error.title"), //$NON-NLS-1$
            e.getLocalizedMessage(),
            e);
      }
      return null; // should never get here.      
   }
   
   /**
    * Loads the assembly template specified by the passed in template
    * reference. This object will be cached and retrived from the cache upon
    * further calls to this method.
    * @param ref the template reference,assumed not<code>null</code>.
    * @return The <code>PSAssemblyTemplate</code> for the specified
    * content type.
    */
   @SuppressWarnings("unused") //$NON-NLS-1$
   private PSAssemblyTemplate getAssemblyTemplate(IPSReference ref)
   {
      if(m_loadedTemplates.containsKey(ref))
         return m_loadedTemplates.get(ref);
      try
      {
         IPSCmsModel model = 
            PSCoreFactory.getInstance().getModel(PSObjectTypes.TEMPLATE);
         if (m_loadedTemplates.isEmpty())
         {
            Collection<IPSReference> refs = model.catalog();
            /*
             * force model to load all templates at once rather than 1 at a
             * time, assuming we are going to need most of them.
             */ 
            model.load(refs.toArray(new IPSReference[refs.size()]), false,
                  false);
         }
         
         PSAssemblyTemplate template = 
            (PSAssemblyTemplate) model.load(ref, false, false);
         m_loadedTemplates.put(ref, template);
         return template;
      }
      catch (Exception e)
      {
         PSWorkbenchPlugin.handleException(
            PSMessages.getString("PSSlotEditor.error.gettingAssemblyTemplates"),  //$NON-NLS-1$
            PSMessages.getString("common.error.title"), //$NON-NLS-1$
            e.getLocalizedMessage(),
            e);
      }
      return null; // should never get here.      
   }
   
   /**
    * Loads the allowed templates mapping filtering out template that are a page
    * output format for all slots except the inline link slot which only allows
    * page and binary output format. 
    */
   private void loadAllowedTemplatesMap(boolean force)
   {
      
      m_allowedTemplates.clear();
      boolean isInlineLinkSlot =  (m_reference.getId().getUUID() == 103);     
      try
      {
         PSCoreFactory factory = PSCoreFactory.getInstance();
         IPSContentTypeModel ctypeModel = 
            (IPSContentTypeModel)factory.getModel(PSObjectTypes.CONTENT_TYPE);
         Collection<IPSReference> types = getContentTypes();
         Map<IPSReference, IPSAssemblyTemplate.OutputFormat> templateFormats = 
            new HashMap<IPSReference, IPSAssemblyTemplate.OutputFormat>();
         // Put associated templates in temporary map
         Map<IPSReference, Collection<IPSReference>> tempMap = 
            ctypeModel.getTemplateAssociations(types, force, false);         
         for(IPSReference key : tempMap.keySet())
         {
            Collection<IPSReference> temps = new ArrayList<IPSReference>();
            for(IPSReference template : tempMap.get(key))
            {
               if(template == null)
                  continue;
               if(!templateFormats.containsKey(template))
               {
                  PSAssemblyTemplate aTemp = getAssemblyTemplate(template);
                  templateFormats.put(template, aTemp.getOutputFormat());
               }
               if(isInlineLinkSlot)
               {
                  if(templateFormats.get(template).equals(
                     IPSAssemblyTemplate.OutputFormat.Page) || 
                     templateFormats.get(template).equals(
                        IPSAssemblyTemplate.OutputFormat.Binary))
                  {
                     temps.add(template);
                  } 
               }
               else
               {
                  if(!templateFormats.get(template).equals(
                     IPSAssemblyTemplate.OutputFormat.Page))
                  {
                     temps.add(template);
                  }
               }
            }
            if(!temps.isEmpty())
               m_allowedTemplates.put(key, temps);
         }
            
         
      }
      catch (PSModelException e)
      {
         PSWorkbenchPlugin.handleException(PSMessages
            .getString("PSSlotEditor.error.gettingAllowedTemplates"), //$NON-NLS-1$
            PSMessages.getString("common.error.title"), //$NON-NLS-1$
            e.getLocalizedMessage(), e);         
      }
      catch (PSLockException e)
      {
         //will never happen because we aren't locking
         throw new RuntimeException("Should never happen."); //$NON-NLS-1$
      }
   }
   
   /**
    * Helper method to handle the setting of the browse
    * button to enabled or diabled
    */
   private void handleBrowseButtonEnable()
   {
      boolean enable = false;
      if(m_contentFinderCombo.getSelectionIndex() > 0)
         enable = true;
      m_browseButton.setEnabled(enable);
   }   
   
   /**
    * Custom cell modifier for the allowed content type
    * table.  
    */
   class ContentTypeTemplateCellModifier implements ICellModifier
   {

      ContentTypeTemplateCellModifier(PSSortableTable comp)
      {
         mi_tableComp = comp;
      }
      
      /* 
       * @see org.eclipse.jface.viewers.ICellModifier#canModify(
       * java.lang.Object, java.lang.String)
       */
      @SuppressWarnings({"synthetic-access","unchecked"}) //$NON-NLS-1$ //$NON-NLS-2$
      public boolean canModify(Object element, String property)
      {
         if(COL_CONTENTTYPE.equals(property))
            return true;
         PSPair<IPSGuid, IPSGuid> rowVal = 
            (PSPair<IPSGuid, IPSGuid>)element;
         if(rowVal.getFirst() != null)
         {
            loadTemplateColumn(rowVal.getFirst(), rowVal.getSecond()); // load the allowed template values
                                                   // in the cell editor
            return true;
         }
         return false;
      }

      /* 
       * @see org.eclipse.jface.viewers.ICellModifier#getValue(
       * java.lang.Object, java.lang.String)
       */
      @SuppressWarnings({"unchecked", "synthetic-access"}) //$NON-NLS-1$ //$NON-NLS-2$
      public Object getValue(Object element, String property)
      {
         PSPair<IPSGuid, IPSGuid> rowVal = 
            (PSPair<IPSGuid, IPSGuid>)element;
         if(COL_CONTENTTYPE.equals(property))
            return rowVal.getFirst();
         else if(COL_TEMPLATE.equals(property))
            return rowVal.getSecond();
         else
         return null;
      }

      /* 
       * @see org.eclipse.jface.viewers.ICellModifier#modify(
       * java.lang.Object, java.lang.String, java.lang.Object)
       */
      @SuppressWarnings({"unchecked", "synthetic-access"}) //$NON-NLS-1$ //$NON-NLS-2$
      public void modify(Object element, String property, Object value)
      {
         if(value == null)
            return;
         if(element instanceof Item)
            element = ((Item)element).getData();
         
         IPSGuid guid = (IPSGuid)value;
         PSPair<IPSGuid, IPSGuid> rowVal = 
            (PSPair<IPSGuid, IPSGuid>)element;
         if(COL_CONTENTTYPE.equals(property))
         {
            if(!guid.equals(rowVal.getFirst()))
            {
               rowVal.setFirst(guid);
               rowVal.setSecond(null);               
            }
         }
         else if(COL_TEMPLATE.equals(property))
            rowVal.setSecond(guid);
         // must refresh the view to see the modified cell
         mi_tableComp.refreshTable();
         
      }
      
      private PSSortableTable mi_tableComp;
      
   }
   
   /**
    * The label provider for the cell editors in the content type /
    * template table.
    */
   class CellEditorLabelProvider extends PSAbstractLabelProvider
   {

      CellEditorLabelProvider(boolean isContentType)
      {         
         mi_isContentType = isContentType;   
      }
      
      /* 
       * @see org.eclipse.jface.viewers.ILabelProvider#getText(
       * java.lang.Object)
       */
      @SuppressWarnings("synthetic-access") //$NON-NLS-1$
      public String getText(Object element)
      {
         IPSGuid guid = (IPSGuid)element;
         if(mi_isContentType)
         {
            if(guid == null)
               return ""; //$NON-NLS-1$
            IPSReference ref = 
               PSUiUtils.getReferenceByGuid(getContentTypes(), guid);
            return ref == null ? "" : ref.getName(); //$NON-NLS-1$
         }
         if(guid == null)
            return ""; //$NON-NLS-1$
         
         IPSReference ref = 
            PSUiUtils.getReferenceByGuid(getAllowedTemplates(null), guid);
         return ref == null ? "" : ref.getName(); //$NON-NLS-1$         
      }
      
      boolean mi_isContentType;
      
   }
   
   /**
    * Retrieves all of the finder extensions.
    * 
    * @return Never <code>null</code>. May be empty if none are found or any
    * errors communicating w/ the server. If any errors, a message will be
    * displayed to the user.
    */
   public static List<PSExtensionDef> getSlotFinderExtensions()
   {
      List<PSExtensionDef> exts = new ArrayList<PSExtensionDef>();
      try
      {
         IPSCmsModel model = PSCoreFactory.getInstance().getModel(
            PSObjectTypes.EXTENSION);
         List<IPSReference> all =
            PSCoreUtils.catalog(PSObjectTypes.EXTENSION, false);
         Object[] defs = model.load(all
               .toArray(new IPSReference[all.size()]), false, false);
         for(Object o : defs)
         {
            PSExtensionDef def = (PSExtensionDef) o;
            if(def.implementsInterface(
               "com.percussion.services.assembly.IPSSlotContentFinder")) //$NON-NLS-1$
               exts.add(def);
         }
      }
      catch (Exception e)
      {         
         PSWorkbenchPlugin.handleException(
            PSMessages.getString("PSSlotEditor.error.catalogingSlotFinderExts"), //$NON-NLS-1$
            PSMessages.getString("common.error.title"), //$NON-NLS-1$
            e.getLocalizedMessage(),
            e);
      }
      return exts;
   }
   
   
   
   /* 
    * @see com.percussion.workbench.ui.editors.form.PSEditorBase#doSave(
    * org.eclipse.core.runtime.IProgressMonitor)
    */
   @Override
   public void doSave(IProgressMonitor pMonitor)
   {
      validateData(true, true);
      super.doSave(pMonitor);
   }

   /* 
    * @see com.percussion.workbench.ui.editors.form.PSEditorBase#doSaveAs()
    */
   @Override
   public void doSaveAs()
   {
      validateData(true, true);
      super.doSaveAs();
   }

   /**
    * Validate that associated contentType/template pairs all exist and 
    * if not then remove them from the object and warn user.
    * @param showWarning if <code>true</code> then a warning will be 
    * presented to the user.
    * @param force forces a clear cache on the model when cataloging
    */
   private void validateData(boolean showWarning, boolean force)
   {
      try
      {
         PSTemplateSlot slot = (PSTemplateSlot)m_data;
         boolean hasDeletedItems = false;
         Collection<PSPair<IPSGuid, IPSGuid>> results = 
            new ArrayList<PSPair<IPSGuid, IPSGuid>>();
         Collection<IPSGuid> existingCtypes = 
            PSUiUtils.guidCollectionFromRefCollection(
               PSCoreUtils.catalog(PSObjectTypes.CONTENT_TYPE, force));
         Collection<IPSGuid> existingTemplates = 
            PSUiUtils.guidCollectionFromRefCollection(
               PSCoreUtils.catalog(PSObjectTypes.TEMPLATE, force));
         for(PSTemplateTypeSlotAssociation assoc : slot.getSlotTypeAssociations())
         {
            IPSGuid ctypeid = new PSDesignGuid(PSTypeEnum.NODEDEF, assoc.getContentTypeId());
            IPSGuid templateid = new PSDesignGuid(PSTypeEnum.TEMPLATE, assoc.getTemplateId());
            if(!existingCtypes.contains(ctypeid) || 
               !existingTemplates.contains(templateid))
            {
               hasDeletedItems = true;
            }
            else
            {
               results.add(new PSPair<IPSGuid, IPSGuid>(ctypeid, templateid));
            }
         }
         if(hasDeletedItems)
         {
            slot.setSlotAssociations(results);
            String msg = PSMessages.getString("PSSlotEditor.error.deletedCTypeAssociation"); //$NON-NLS-1$
            if(showWarning)
               MessageDialog.openWarning(getSite().getShell(), PSMessages.getString("PSSlotEditor.error.deletedCTypeAssociation.title"), //$NON-NLS-1$
                  msg);
         }
         
      }
      catch (PSModelException e)
      {
         PSWorkbenchPlugin.handleException(null, null, null, e);         
      }
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
            "PSSlotEditor.type.label", //$NON-NLS-1$
               "type", //$NON-NLS-1$
            "PSSlotEditor.allowedRelTypes.label", //$NON-NLS-1$
               "allowed_relationship_types", //$NON-NLS-1$
            "PSSlotEditor.contentFinder.label", //$NON-NLS-1$
               "content_finder", //$NON-NLS-1$
            "PSSlotEditor.allowedContent.label", //$NON-NLS-1$
               "allowed_content", //$NON-NLS-1$
            "PSSlotEditor.contentType.columnname", //$NON-NLS-1$
               "content_type",    //$NON-NLS-1$
            "PSSlotEditor.template.columnname", //$NON-NLS-1$
               "template"    //$NON-NLS-1$
         });
      }
      return m_helpHintKeyHelper.getKey(
         super.getHelpHintKey(controlInfo), controlInfo);
   }
   
   /* 
    * @see com.percussion.workbench.ui.editors.form.PSEditorBase#dispose()
    */
   @Override
   public void dispose()
   {
      super.dispose();
      PSCoreFactory factory = PSCoreFactory.getInstance();
      try
      {
         IPSCmsModel ctypeModel = factory.getModel(PSObjectTypes.CONTENT_TYPE);
         IPSCmsModel templateModel = factory.getModel(PSObjectTypes.TEMPLATE);
         ctypeModel.removeListener(m_modelListeners[0]);
         templateModel.removeListener(m_modelListeners[1]);
      }
      catch (PSModelException ignore)
      {  
      }
      
   }
      
   // Controls
   private Combo m_contentFinderCombo;
   private Combo m_relTypeCombo;
   private PSRadioAndCheckBoxes m_typeComp;
   private ComboViewer m_contentFinderComboViewer;
   private ComboViewer m_relTypeComboViewer; 
   private PSSortableTable m_ctTable; 
   private PSNameLabelDesc m_commonComp;
   private Button m_browseButton;
   private PSComboBoxCellEditor m_cTypeCellEditor;
   
   /**
    * Cache of the help hint helper object
    */
   private PSHelpHintKeyHelper m_helpHintKeyHelper;
   
   /**
    * Finder argument map, never <code>null</code>, may be empty.
    */
   private Map<String, String> m_finderArgs = new HashMap<String, String>();   
 
         
   /**
    * Map of cached item defition objects. Never <code>null</code>, may
    * be empty.
    */
   private Map<IPSReference, PSItemDefinition> m_itemDefs = 
      new HashMap<IPSReference, PSItemDefinition>();

   /**
    * Map of cached template objects. Never <code>null</code>, may
    * be empty.
    */
   private Map<IPSReference, PSAssemblyTemplate> m_loadedTemplates = 
      new HashMap<IPSReference, PSAssemblyTemplate>(); 
   
   /**
    * Map of cached allowed content type to template mappings
    */
   private Map<IPSReference, Collection<IPSReference>> m_allowedTemplates =
      new HashMap<IPSReference, Collection<IPSReference>>();
   
   /**
    * Reference to the model listeners placed on this editor. 
    * Needed so we can remove them on dispose. Initialized
    * in {@link #addObjectDeleteListener()}, never <code>null</code>
    * after that.
    */
   IPSModelListener[] m_modelListeners;
   
   /**
    * Content type column name
    */
   private static final String COL_CONTENTTYPE = 
      PSMessages.getString("PSSlotEditor.contentType.columnname"); //$NON-NLS-1$
   
   /**
    * Template column name
    */
   private static final String COL_TEMPLATE = 
      PSMessages.getString("PSSlotEditor.template.columnname"); //$NON-NLS-1$

   
   
  
  

}
