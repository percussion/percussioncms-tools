/******************************************************************************
 *
 * [ PSItemFilterEditor.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form;

import com.percussion.E2Designer.DTTextLiteral;
import com.percussion.client.IPSReference;
import com.percussion.client.PSCatalogUtils;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.models.IPSCmsModel;
import com.percussion.design.objectstore.IPSReplacementValue;
import com.percussion.extension.PSExtensionDef;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.content.data.PSKeyword;
import com.percussion.services.content.data.PSKeywordChoice;
import com.percussion.services.filter.IPSItemFilter;
import com.percussion.services.filter.IPSItemFilterRule;
import com.percussion.services.filter.IPSItemFilterRuleDef;
import com.percussion.services.filter.PSFilterException;
import com.percussion.services.filter.data.PSItemFilter;
import com.percussion.services.filter.data.PSItemFilterRuleDef;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.utils.types.PSPair;
import com.percussion.workbench.ui.IPSUiConstants;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.PSWorkbenchPlugin;
import com.percussion.workbench.ui.controls.IPSNewRowObjectProvider;
import com.percussion.workbench.ui.controls.PSAbstractLabelProvider;
import com.percussion.workbench.ui.controls.PSAbstractTableLabelProvider;
import com.percussion.workbench.ui.controls.PSButtonedComboBoxCellEditor;
import com.percussion.workbench.ui.controls.PSDefaultContentProvider;
import com.percussion.workbench.ui.controls.PSNameLabelDesc;
import com.percussion.workbench.ui.controls.PSSortableTable;
import com.percussion.workbench.ui.editors.dialog.PSExtensionParamsDialog;
import com.percussion.workbench.ui.util.PSControlInfo;
import com.percussion.workbench.ui.util.PSHelpHintKeyHelper;
import com.percussion.workbench.ui.validators.IPSControlValueValidator;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Label;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Item filter editor class.
 *
 */
public class PSItemFilterEditor extends PSEditorBase
      implements
         IPSUiConstants
{

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.workbench.ui.editors.form.PSEditorBase#isValidReference(com.percussion.client.IPSReference)
    */
   @Override
   public boolean isValidReference(IPSReference ref)
   {
      if (ref == null)
         return false; // Should never happen
      if (ref.getObjectType().getPrimaryType() == PSObjectTypes.ITEM_FILTER)
         return true;
      return false;
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.workbench.ui.editors.form.PSEditorBase#createControl(org.eclipse.swt.widgets.Composite)
    */
   @Override
   public void createControl(Composite comp)
   {
      m_itemFilter = (PSItemFilter) m_data;
      final Composite mainComp = new Composite(comp, SWT.NONE);
      mainComp.setLayout(new FormLayout());
      mainComp.setSize(720, 420);
      // Add the common composite that contains label, description
      // fields
      m_commonComp = new PSNameLabelDesc(mainComp, SWT.NONE, PSMessages.getString(
         "PSItemFilterEditor.label.item.filter"), //$NON-NLS-1$
            EDITOR_LABEL_NUMERATOR, PSNameLabelDesc.SHOW_DESC
                  | PSNameLabelDesc.SHOW_NAME | PSNameLabelDesc.NAME_READ_ONLY
                  | PSNameLabelDesc.LAYOUT_SIDE, this);
      final FormData fd1 = new FormData();
      fd1.left = new FormAttachment(0, 0);
      fd1.right = new FormAttachment(100, 0);
      fd1.top = new FormAttachment(0, 0);
      m_commonComp.setLayoutData(fd1);

      final Label inheritLabel = new Label(mainComp, SWT.NONE);
      final FormData fd2 = new FormData();
      fd2.left = new FormAttachment(10, 0);
      fd2.right = new FormAttachment(90, 0);
      fd2.top = new FormAttachment(m_commonComp, 15, SWT.BOTTOM);
      inheritLabel.setLayoutData(fd2);
      inheritLabel.setText(PSMessages.getString(
         "PSItemFilterEditor.label.inherit.from")); //$NON-NLS-1$

      m_inheritFrom = new ComboViewer(mainComp, SWT.READ_ONLY);
      m_inheritFrom.setLabelProvider(new LabelProvider()
      {
         @Override
         public String getText(Object element)
         {
            if (!(element instanceof IPSItemFilter))
               throw new IllegalArgumentException(
                     "The passed in object must be an instance of IPSItemFilter."); //$NON-NLS-1$
            return StringUtils.defaultString(((IPSItemFilter) element)
                  .getName());
         }
      });
      m_inheritFrom.setContentProvider(new PSDefaultContentProvider());
      registerControl("PSItemFilterEditor.label.inherit.from",
         m_inheritFrom, null);

      try
      {
         m_parentFiltersList.add(new PSItemFilter());
         m_parentFiltersList.addAll(PSCatalogUtils
               .catalogItemFilterParents(m_itemFilter));
         m_inheritFrom.setInput(m_parentFiltersList);
      }
      catch (Exception e)
      {
         PSWorkbenchPlugin.handleException(PSMessages.getString("PSItemFilterEditor.error.parentfiltercatalogging.context"), //$NON-NLS-1$
               PSMessages.getString("PSItemFilterEditor.error.parentfiltercatalogging.title"), //$NON-NLS-1$
               PSMessages.getString("PSItemFilterEditor.error.parentfiltercatalogging.message"), e); //$NON-NLS-1$
      }

      final FormData fd2a = new FormData();
      fd2a.left = new FormAttachment(inheritLabel, 0, SWT.LEFT);
      fd2a.right = new FormAttachment(inheritLabel, 0, SWT.RIGHT);
      fd2a.top = new FormAttachment(inheritLabel, COMBO_VSPACE_OFFSET,
            SWT.BOTTOM);
      m_inheritFrom.getCombo().setLayoutData(fd2a);

      final Label authTypeLabel = new Label(mainComp, SWT.NONE);
      final FormData fd21 = new FormData();
      fd21.left = new FormAttachment(10, 0);
      fd21.right = new FormAttachment(90, 0);
      fd21.top = new FormAttachment(m_inheritFrom.getCombo(), LABEL_VSPACE_OFFSET, SWT.BOTTOM);
      authTypeLabel.setLayoutData(fd21);
      authTypeLabel.setText(PSMessages.getString("PSItemFilterEditor.label.authtype")); //$NON-NLS-1$

      m_authType = new ComboViewer(mainComp, SWT.READ_ONLY);
      m_authType.setLabelProvider(new LabelProvider()
      {
         @Override
         public String getText(Object element)
         {
            if (!(element instanceof AType))
               throw new IllegalArgumentException(
                     "The passed in object must be an instance of AType."); //$NON-NLS-1$
            return StringUtils.defaultString(((AType) element)
                  .label);
         }
      });
      m_authType.setContentProvider(new PSDefaultContentProvider());
      registerControl(
            "PSItemFilterEditor.label.authtype",
            m_authType, null);

      final FormData fd21a = new FormData();
      fd21a.left = new FormAttachment(authTypeLabel, 0, SWT.LEFT);
      fd21a.right = new FormAttachment(authTypeLabel, 0, SWT.RIGHT);
      fd21a.top = new FormAttachment(authTypeLabel, COMBO_VSPACE_OFFSET,
            SWT.BOTTOM);
      m_authType.getCombo().setLayoutData(fd21a);

      try
      {
         IPSCmsModel model = PSCoreFactory.getInstance().getModel(
               PSObjectTypes.KEYWORD);
         IPSReference ref = model.getReference(new PSGuid(
               PSTypeEnum.KEYWORD_DEF, AUTHTYPE_KEYWORD_ID));
         if (ref != null)
         {
            PSKeyword kw = (PSKeyword) model.load(ref, false, false);
            List<PSKeywordChoice> atList = kw.getChoices();
            List<AType> authTypes = new ArrayList<AType>();
            authTypes.add(new AType());
            for(PSKeywordChoice ch:atList)
            {
               authTypes.add(new AType(ch.getLabel(),ch.getValue()));
            }
            m_authType.setInput(authTypes);
         }
      }
      catch (Exception e)
      {
         PSWorkbenchPlugin.handleException(PSMessages.getString("PSItemFilterEditor.error.authtypecatalogging.context"), //$NON-NLS-1$
         PSMessages.getString("PSItemFilterEditor.error.authtypecatalogging.title"), //$NON-NLS-1$
               PSMessages.getString("PSItemFilterEditor.error.authtypecatalogging.message"), e); //$NON-NLS-1$
      }
      
      final Label rulesLabel = new Label(mainComp, SWT.NONE);
      final FormData fd3 = new FormData();
      fd3.left = new FormAttachment(m_authType.getCombo(), 0, SWT.LEFT);
      fd3.right = new FormAttachment(m_authType.getCombo(), 0, SWT.RIGHT);
      fd3.top = new FormAttachment(m_authType.getCombo(),
            LABEL_VSPACE_OFFSET, SWT.BOTTOM);
      rulesLabel.setLayoutData(fd3);
      rulesLabel.setText(PSMessages.getString(
         "PSItemFilterEditor.label.rules")); //$NON-NLS-1$

      // Create the label provider for this table
      ITableLabelProvider labelProvider = new PSAbstractTableLabelProvider()
      {
         public String getColumnText(Object element,
            @SuppressWarnings("unused") int columnIndex)
         {
            Row rule = (Row) element;
            String coltext = rule != null ? StringUtils.defaultString(rule
                  .getDisplayName()) : StringUtils.EMPTY;
            return coltext; // should never get here
         }
      };
      // Create the new row object provider for this table
      IPSNewRowObjectProvider newRowProvider = new IPSNewRowObjectProvider()
      {
         @SuppressWarnings("synthetic-access") //$NON-NLS-1$
         public Object newInstance()
         {
            Row newRow = new Row();
            return newRow;
         }

         public boolean isEmpty(Object obj)
         {
            if (!(obj instanceof Row))
               throw new IllegalArgumentException(
                     "The passed in object must be an instance of Row."); //$NON-NLS-1$
            Row rule = (Row) obj;
            return rule.isEmpty();
         }
      };

      m_rulesTable = new PSSortableTable(mainComp, labelProvider,
            newRowProvider, SWT.NONE, PSSortableTable.SHOW_DELETE
                  | PSSortableTable.DELETE_ALLOWED
                  | PSSortableTable.INSERT_ALLOWED);
      m_rulesTable.setCellModifier(new CellModifier(m_rulesTable));
      final FormData fd3a = new FormData();
      fd3a.left = new FormAttachment(m_authType.getCombo(), 0, SWT.LEFT);
      fd3a.right = new FormAttachment(m_authType.getCombo(), 0, SWT.RIGHT);
      fd3a.top = new FormAttachment(rulesLabel, COMBO_VSPACE_OFFSET, SWT.BOTTOM);
      fd3a.height = 2 * DESCRIPTION_FIELD_HEIGHT;
      m_rulesTable.setLayoutData(fd3a);
      m_ruleCellEditor = new PSButtonedComboBoxCellEditor(m_rulesTable
            .getTable(), new String[0], SWT.READ_ONLY);
      registerControl("PSItemFilterEditor.label.rules", m_rulesTable, 
         new IPSControlValueValidator[]{new DupeRuleTableValidator()});

      final CCombo ctrlCombo = m_ruleCellEditor.getCombo();
      ctrlCombo.addSelectionListener(new SelectionAdapter()
         {

            /* 
             * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(
             * org.eclipse.swt.events.SelectionEvent)
             */
            @SuppressWarnings("synthetic-access") //$NON-NLS-1$
            @Override
            public void widgetSelected(@SuppressWarnings("unused") SelectionEvent e)
            {
               String rulename  = (String) m_ruleCellEditor.getValue();
               PSExtensionDef def = getExtension(rulename);
               Iterator iter = def.getRuntimeParameterNames();
               if (iter.hasNext())
               {
                  m_ruleCellEditor.getButton().setVisible(true);
                  StructuredSelection selection = 
                     (StructuredSelection)m_rulesTable.getTableViewer().getSelection();
                  Row rule = (Row)selection.getFirstElement();
                 
                  rule.setParams(null);
                  runValidation(true);
                  if(doesDupeRuleExist(m_rulesTable))
                  {                     
                     return;
                  }
                  openExtensionParamDialog(true);
               }
               else
               {
                  m_ruleCellEditor.getButton().setVisible(false);
               }
            }
            
         });
      // Create the label provider for this table
      ILabelProvider ruleLabelProvider = new PSAbstractLabelProvider()
      {
         public String getText(Object element)
         {
            
            if ((element instanceof Row)) //$NON-NLS-1$
               return StringUtils.defaultString(((Row) element).getDisplayName());
            return element == null ? "" : element.toString(); //$NON-NLS-1$
         }
      };

      m_ruleCellEditor.setLabelProvider(ruleLabelProvider);
      m_ruleCellEditor.getButton().addSelectionListener(new SelectionAdapter()
         {

            /* 
             * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(
             * org.eclipse.swt.events.SelectionEvent)
             */
            @SuppressWarnings("synthetic-access") //$NON-NLS-1$
            @Override
            public void widgetSelected(@SuppressWarnings("unused") SelectionEvent e)
            {
               openExtensionParamDialog(false);
            }
            
         });
      try
      {
         m_extDefs.addAll(PSCatalogUtils
               .catalogExtensions(IPSItemFilterRule.class.getName()));
      }
      catch (Exception e)
      {
         PSWorkbenchPlugin.handleException(
               PSMessages.getString("PSItemFilterEditor.error.extensionscatalogging.context"), //$NON-NLS-1$
               PSMessages.getString("PSItemFilterEditor.error.extensionscatalogging.title"), //$NON-NLS-1$
               PSMessages.getString("PSItemFilterEditor.error.extensionscatalogging.message"), //$NON-NLS-1$
               e);
      }
      m_rulesTable.addColumn(PSMessages.getString("PSItemFilterEditor.label.rule.columntitle"), PSSortableTable.NONE, //$NON-NLS-1$
            new ColumnWeightData(20, 100), m_ruleCellEditor, SWT.LEFT);
      loadRuleChoices();
   }  
   
   /**
    * Loads all rule choices into the rule cell editor
    */
   @SuppressWarnings("unchecked") //$NON-NLS-1$
   private void loadRuleChoices()
   {
      
      try
      {        
         List<String> choices = new ArrayList<String>();
         for(PSExtensionDef def : m_extDefs)
         {
           
            String name = def.getRef().getExtensionName();
            String[] temp = name.split("/"); //$NON-NLS-1$
            if (temp.length > 0)
            {
               name = temp[temp.length - 1];
            }
            choices.add(name);
            
         } 
         m_ruleCellEditor.setItems(choices.toArray());
      }
      catch (Exception e)
      {
         PSWorkbenchPlugin.handleException(
               PSMessages.getString(
                  "PSItemFilterEditor.error.extensionscatalogging.context"), //$NON-NLS-1$
               PSMessages.getString(
                  "PSItemFilterEditor.error.extensionscatalogging.title"), //$NON-NLS-1$
               PSMessages.getString(
                  "PSItemFilterEditor.error.extensionscatalogging.message"), //$NON-NLS-1$
               e);
      }
   }

   /**
    * Cell modifier for the rules table
    */
   class CellModifier implements ICellModifier
   {

      CellModifier(PSSortableTable comp) {
         mi_tableComp = comp;
      }

      /*
       * @see org.eclipse.jface.viewers.ICellModifier#canModify(
       *      java.lang.Object, java.lang.String)
       */
      @SuppressWarnings("synthetic-access") //$NON-NLS-1$
      public boolean canModify(@SuppressWarnings("unused") Object element,
         @SuppressWarnings("unused") String property)
      {
         return true;
      }

      /*
       * @see org.eclipse.jface.viewers.ICellModifier#getValue(
       *      java.lang.Object, java.lang.String)
       */
      @SuppressWarnings("synthetic-access") //$NON-NLS-1$
      public Object getValue(Object element,
         @SuppressWarnings("unused") String property)
      {
         Row rule = (Row) element;
         return rule.getDisplayName();
      }

      /*
       * @see org.eclipse.jface.viewers.ICellModifier#modify( java.lang.Object,
       *      java.lang.String, java.lang.Object)
       */
      @SuppressWarnings("synthetic-access") //$NON-NLS-1$
      public void modify(Object element,
         @SuppressWarnings("unused") String property, Object value)
      {
         if(value == null)
            return;
         if(element instanceof Item)
            element = ((Item)element).getData();
         Row rule = (Row) element;
         String val = (String)value;
        
         if(rule.getDisplayName().equals(val))
            return;
         
         rule.setName(getFullExtensionName(val));
         mi_tableComp.refreshTable();
         m_ruleCellEditor.setValue(""); //$NON-NLS-1$
         runValidation(true);
         
      }

      @SuppressWarnings("unused") //$NON-NLS-1$
      private PSSortableTable mi_tableComp;

   }
   
   private String getFullExtensionName(String displayname)
   {
      PSExtensionDef def = getExtension(displayname);
      return def == null ? displayname : def.getRef().getFQN();
   }

   /**
    * Extension identified by provided display name.
    */
   private PSExtensionDef getExtension(String displayname)
   {
      for (final PSExtensionDef def : m_extDefs)
      {
         if (def.getRef().getExtensionName().equals(displayname))
         {
            return def;
         }
      }
      return null;
   }
   
   /**
    * Convenience object to represent a table row
    */
   private class Row
   {
      Row()
      {
         
      }
      
      Row(String name, Map<String, String> params)
      {
         mi_name = name;
         mi_params.clear();
         if(params != null)
            mi_params.putAll(params);
      }
      
      Row(String name)
      {
         this(name, null);
      }
      
      Row(IPSItemFilterRuleDef def) throws PSFilterException
      {
         this(def.getRuleName(), def.getParams());
      }
      
      String getName()
      {
         return mi_name;
      }
      
      void setName(String name)
      {
         mi_name = name;
      }
      
      IPSItemFilterRuleDef getRuleDef()
      {
         PSItemFilterRuleDef def = new PSItemFilterRuleDef();
         def.setRule(mi_name);
         for(String pname : mi_params.keySet())
         {
            String value = mi_params.get(pname);
            if (StringUtils.isNotBlank(value))
            {
               def.addParam(pname, value);
            }
         }
         return def;
      }
      
      public boolean isEmpty()
      {
         return StringUtils.isBlank(mi_name);
      }
      
      public String getDisplayName()
      {
         if(StringUtils.isBlank(mi_name))
            return ""; //$NON-NLS-1$
         String name = ""; //$NON-NLS-1$
         String[] temp = mi_name.split("/"); //$NON-NLS-1$
         if (temp.length > 0)
         {
            name = temp[temp.length - 1];
         }
         return name;
      }
      
      public Map<String, String> getParams()
      {
         return mi_params;
      }
      
      public void setParams(Map<String, String> params)
      {
         mi_params.clear();
         if(params != null)
            mi_params.putAll(params);
      }
      
      
      
      private String mi_name;
      private Map<String, String> mi_params = new HashMap<String, String>();
   }

  

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater#updateDesignerObject(java.lang.Object,
    *      java.lang.Object)
    */
   public void updateDesignerObject(Object designObject, Object control)
   {
      PSItemFilter filter = (PSItemFilter) designObject;
      if (control == m_commonComp.getDescriptionText())
      {
         filter.setDescription(m_commonComp.getDescriptionText().getText()
               .trim());
      }
      else if (control == m_inheritFrom.getCombo())
      {
         int index = m_inheritFrom.getCombo().getSelectionIndex();
         if (index < 1)
            filter.setParentFilter(null);
         else
            filter.setParentFilter(m_parentFiltersList.get(index));
      }
      else if (control == m_authType.getCombo())
      {
         int index = m_authType.getCombo().getSelectionIndex();
         if (index < 1)
            filter.setLegacyAuthtypeId(null);
         else
         {
            StructuredSelection sel = (StructuredSelection) m_authType.getSelection();
            filter.setLegacyAuthtypeId(new Integer(((AType)sel.getFirstElement()).value));
         }
      }
      else if (control == m_rulesTable)
      {
         List rows = m_rulesTable.getValues();
         Set<IPSItemFilterRuleDef> rDefs = new HashSet<IPSItemFilterRuleDef>(
               rows.size());
         for (int i = 0; i < rows.size(); i++)
         {
            Row row = (Row) rows.get(i);
            rDefs.add(row.getRuleDef());
         }
         filter.setRuleDefs(rDefs);
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater#loadControlValues(java.lang.Object)
    */
   @SuppressWarnings("unchecked")
   public void loadControlValues(Object designObject)
   {
      PSItemFilter filter = (PSItemFilter) designObject;
      // Set name
      ((Label) m_commonComp.getNameText()).setText(filter.getName());
      // Set description
      m_commonComp.getDescriptionText().setText(
            StringUtils.defaultString(filter.getDescription()));
      // Set parent Filter
      if (filter.getParentFilter() != null)
      {
         int index = findMatchingParentFilter(filter.getParentFilter());
         m_inheritFrom.getCombo().select(index);
      }
      //Set auth type
      if(filter.getLegacyAuthtypeId() != null)
      {
         List<AType> ats = (List<AType>) m_authType.getInput();
         //should only be null if the Auth type lookup keyword value changes
         if (ats != null)
         {
            for(int i=0;i<ats.size();i++)
            {
               if(ats.get(i).value.equals(filter.getLegacyAuthtypeId().toString()))
               {
                  m_authType.getCombo().select(i);
               }
            }
         }
         else
         {
            System.out.println(
               "The LookupId of the Authorization_Types keyword does not match"
               + " the expected value of " + AUTHTYPE_KEYWORD_ID);
         }
      }
      // Set rules
      Set ruleDefs = filter.getRuleDefs();
      Iterator iter = ruleDefs.iterator();
      List<Row> rules = new ArrayList<Row>();
      while (iter.hasNext())
      {
         IPSItemFilterRuleDef ruleDef = (IPSItemFilterRuleDef) iter.next();
         try
         {
            rules.add(new Row(ruleDef));
         }
         catch (PSFilterException e)
         {
            PSWorkbenchPlugin.handleException(
               PSMessages.getString("PSItemFilterEditor.error.extensionscatalogging.context"), //$NON-NLS-1$
               PSMessages.getString("PSItemFilterEditor.error.extensionscatalogging.title"), //$NON-NLS-1$
               PSMessages.getString("PSItemFilterEditor.error.extensionscatalogging.message"), //$NON-NLS-1$
               e);
         }
      }      
      m_rulesTable.setValues(rules);
   }

   /**
    * Return the index of the supplied filter in the parent filter list by long
    * value of guid.
    * 
    * @param filter filter to find by guid, assumed not <code>null</code>.
    * @return zero based index of the matching filter, -1 if not found.
    */
   private int findMatchingParentFilter(IPSItemFilter filter)
   {
      for (int i = 0; i < m_parentFiltersList.size(); i++)
      {
         IPSItemFilter f = m_parentFiltersList.get(i);
         if (f.getGUID().longValue() == filter.getGUID().longValue())
         {
            return i;
         }
      }
      return -1;
   }
   

   /**
    * Convenient method to open extension param dialog.
    */
   private void openExtensionParamDialog(boolean fromSelectionEvent)
   {
      String rulename = (String) m_ruleCellEditor.getValue();
      if(StringUtils.isBlank(rulename))
         return;
      StructuredSelection selection = 
         (StructuredSelection)m_rulesTable.getTableViewer().getSelection();
      Row rule = (Row)selection.getFirstElement();
      if(rule == null)
         return;
      
      Map<String, String> params = rule.getParams();
      
      Map<String, IPSReplacementValue> values = 
         new HashMap<String, IPSReplacementValue>();
      if(!fromSelectionEvent)
      {
         Set<String> keys = params.keySet();
         DTTextLiteral dt = new DTTextLiteral();
         for(String key : keys)
         {
            values.put(key, (IPSReplacementValue)dt.create(
               params.get(key)));
         }
      }
      PSExtensionDef extDef = getExtension(rulename);
      PSExtensionParamsDialog dialog = new PSExtensionParamsDialog(getSite()
            .getShell(), extDef, values, 
            PSExtensionParamsDialog.MODE_TEXT_LITERAL_ONLY);

      if (dialog.open() == Dialog.OK)
      {
         Map<String, String> newParams = new HashMap<String, String>();
         for (PSPair<String, IPSReplacementValue> pair : dialog.getParamValues())
         {
            String newVal = pair.getSecond() == null 
            ? "" : pair.getSecond().getValueText();             //$NON-NLS-1$
            newParams.put(StringUtils.defaultString(pair.getFirst()), newVal);
         }
         rule.setParams(newParams);
         //Force update
         updateDesignerObject(m_data, m_rulesTable);
         setDirty();
      }
   }
   
   /**
    * Helper method to check if duplicate rule exists in the
    * table or current cell.
    * @param table cannot be <code>null</code>.
    * @return <code>true</code> if a duplicate rule exists
    */
   @SuppressWarnings("unchecked") //$NON-NLS-1$
   protected boolean doesDupeRuleExist(PSSortableTable table)
   {
      if(table == null)
         throw new IllegalArgumentException("table cannot be null.");
      List<Row> rules = (List<Row>)table.getValues();
      List<String> used = new ArrayList<String>();
      int selection = table.getTable().getSelectionIndex();
      int count = 0;
      for(Row rule : rules)
      {
         if(m_ruleCellEditor.isActivated() && count == selection)
            continue;
         if(used.contains(rule.getName()))
         {
            return true;
         }
         used.add(rule.getName());
         count++;
      }
      String temp = (String)m_ruleCellEditor.getValue();
      if(temp != null && m_ruleCellEditor.isActivated())
      {
         temp = getFullExtensionName(temp);
         if(used.contains(temp))
            return true;
            
      }
      return false;
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
            "PSNameLabelDesc.label.description",
               "description",
            "PSItemFilterEditor.label.inherit.from",
               "inherit_from",
            "PSItemFilterEditor.label.rules",
               "rules"
         });
      }
      return m_helpHintKeyHelper.getKey(
         super.getHelpHintKey(controlInfo), controlInfo);
   }
   
   /**
    * Validator to check for duplicate rule entries
    */
   class DupeRuleTableValidator implements IPSControlValueValidator
   {

      @SuppressWarnings({"unchecked","synthetic-access"}) //$NON-NLS-1$ //$NON-NLS-2$
      public String validate(PSControlInfo controlInfo)
      {
         PSSortableTable table = (PSSortableTable)controlInfo.getControl();
         if(doesDupeRuleExist(table))
            return PSMessages.getString("PSItemFilterEditor.error.duplicateRules"); //$NON-NLS-1$
         return null;
      }
      
   }

  /**
    * Convenient inner class to hold the Authorization types and their values.
    */
  private class AType
  {
     AType()
     {
        
     }
     AType(String key, String val)
     {
        label = key;
        value = val;
     }
     public String label = ""; //$NON-NLS-1$
     public String value = ""; //$NON-NLS-1$
  }
   /**
    * Cache of the help hint helper object
    */
   private PSHelpHintKeyHelper m_helpHintKeyHelper;

   // Controls for this editor
   private PSNameLabelDesc m_commonComp;
   private ComboViewer m_inheritFrom;
   private ComboViewer m_authType;
   private PSSortableTable m_rulesTable;
   private PSButtonedComboBoxCellEditor m_ruleCellEditor;
   private PSItemFilter m_itemFilter;
   private List<IPSItemFilter> m_parentFiltersList = new ArrayList<IPSItemFilter>();
   private List<PSExtensionDef> m_extDefs = new ArrayList<PSExtensionDef>();
   private static final int AUTHTYPE_KEYWORD_ID = 107;
}
