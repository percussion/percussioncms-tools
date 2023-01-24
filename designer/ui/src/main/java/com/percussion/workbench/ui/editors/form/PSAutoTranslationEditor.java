/*
 * Copyright 1999-2022 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.percussion.workbench.ui.editors.form;

import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSCoreUtils;
import com.percussion.client.PSModelException;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.PSSecurityUtils;
import com.percussion.client.models.IPSContentTypeModel;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.content.data.PSAutoTranslation;
import com.percussion.utils.types.PSPair;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.PSWorkbenchPlugin;
import com.percussion.workbench.ui.controls.IPSNewRowObjectProvider;
import com.percussion.workbench.ui.controls.PSAbstractTableLabelProvider;
import com.percussion.workbench.ui.controls.PSComboBoxCellEditor;
import com.percussion.workbench.ui.controls.PSReferenceLabelProvider;
import com.percussion.workbench.ui.controls.PSSortableTable;
import com.percussion.workbench.ui.util.PSControlInfo;
import com.percussion.workbench.ui.util.PSHelpHintKeyHelper;
import com.percussion.workbench.ui.util.PSReferenceComparator;
import com.percussion.workbench.ui.util.PSUiUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class PSAutoTranslationEditor extends PSEditorBase
{
   
   /* 
    * @see com.percussion.workbench.ui.editors.form.PSEditorBase#
    * isValidReference(com.percussion.client.IPSReference)
    */
   @Override
   public boolean isValidReference(IPSReference ref)
   {
      if(ref == null)
         return false; // Should never happen
      return ref.getObjectType().getPrimaryType() ==
              PSObjectTypes.AUTO_TRANSLATION_SET;
   }


   /* 
    * @see com.percussion.workbench.ui.editors.form.PSEditorBase#
    * createControl(org.eclipse.swt.widgets.Composite)
    */
   @Override
   public void createControl(Composite parent)
   {
      initCaches();

      Composite comp = new Composite(parent, SWT.NONE);
      comp.setLayout(new FormLayout());

      Label translationSettingsLabel = new Label(comp, SWT.NONE);
      final FormData formData = new FormData();
      formData.top = new FormAttachment(0, 15);
      formData.left = new FormAttachment(0, 20);
      translationSettingsLabel.setLayoutData(formData);
      translationSettingsLabel.setText(PSMessages.getString(
         "PSAutoTranslationEditor.translationSetting.label")); //$NON-NLS-1$
      
      m_table = new PSSortableTable(comp, new TranslationTableLabelProvider(),
         new NewRowObjectProvider(), SWT.NONE, 
         PSSortableTable.SHOW_DELETE |
         PSSortableTable.SHOW_INSERT |
         PSSortableTable.INSERT_ALLOWED |
         PSSortableTable.DELETE_ALLOWED |
         PSSortableTable.SURPRESS_MANUAL_SORT);
      m_table.setCellModifier(new TranslationTableCellModifier());
      registerControl(
         translationSettingsLabel.getText(),
         m_table,
         null);
      final FormData formData1 = new FormData();
      formData1.height = 300;
      formData1.left = new FormAttachment(translationSettingsLabel, 0, SWT.LEFT);
      formData1.right = new FormAttachment(100, 0);
      formData1.top = new FormAttachment(translationSettingsLabel, 0, SWT.BOTTOM);
      m_table.setLayoutData(formData1);
      
      m_cTypeCellEditor = new PSComboBoxCellEditor(m_table.getTable(), new String[]{}, SWT.READ_ONLY);
      m_cTypeCellEditor.setLabelProvider(new PSReferenceLabelProvider(true));
      m_table.addColumn(
         "PSAutoTranslationEditor.col.sourceContentType.label", //$NON-NLS-1$
         PSSortableTable.NONE, 
         new ColumnWeightData(10,100, true), m_cTypeCellEditor, SWT.LEFT);
      
      m_localeCellEditor = new PSComboBoxCellEditor(m_table.getTable(), new String[]{}, SWT.READ_ONLY);
      m_localeCellEditor.setLabelProvider(new PSReferenceLabelProvider(true));
      m_table.addColumn(
         "PSAutoTranslationEditor.col.targetLocale.label", PSSortableTable.NONE, //$NON-NLS-1$
         new ColumnWeightData(10,100, true), m_localeCellEditor, SWT.LEFT);
      
      m_commCellEditor = new PSComboBoxCellEditor(m_table.getTable(), new String[]{}, SWT.READ_ONLY);
      m_commCellEditor.setLabelProvider(new PSReferenceLabelProvider(true));
      m_table.addColumn(
         "PSAutoTranslationEditor.col.targetCommunity.label", PSSortableTable.NONE, //$NON-NLS-1$
         new ColumnWeightData(10,100, true), m_commCellEditor, SWT.LEFT);     
      
      m_workflowCellEditor = new PSComboBoxCellEditor(m_table.getTable(), new String[]{}, SWT.READ_ONLY);
      m_workflowCellEditor.setLabelProvider(new PSReferenceLabelProvider(true));
      m_table.addColumn(
         "PSAutoTranslationEditor.col.targetWorkflow.label", PSSortableTable.NONE, //$NON-NLS-1$
         new ColumnWeightData(10,100, true), m_workflowCellEditor, SWT.LEFT);

   }

   /* 
    * @see com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater#
    * updateDesignerObject(java.lang.Object, java.lang.Object)
    */
   @SuppressWarnings("unchecked") //$NON-NLS-1$
   public void updateDesignerObject(Object designObject, Object control)
   {
      Set<PSAutoTranslation> set = (Set<PSAutoTranslation>)designObject;
      if(control == m_table)
      {
         set.clear();
         List<TranslationTableRow> list =
                 m_table.getValues();
         for(TranslationTableRow row : list)
         {
            set.add(row.getAutoTranslation());
         }
      }

   }

   /* 
    * @see com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater#
    * loadControlValues(java.lang.Object)
    */
   @SuppressWarnings("unchecked") //$NON-NLS-1$
   public void loadControlValues(Object designObject)
   {
      Set<PSAutoTranslation> set = (Set<PSAutoTranslation>)designObject;
      List<TranslationTableRow> list = new ArrayList<>();
      for(PSAutoTranslation trans : set)
      {
         list.add(new TranslationTableRow(trans));
      }
      m_table.setValues(list);

   }
   
   /**
    * Loads the specified column's cell editor with its appropriate list
    * of choices.
    * @param col the column
    */
   protected void loadSelectedCellEditor(int col)
   {
      int row = m_table.getTable().getSelectionIndex();
      if(row == -1)
         return;
      TranslationTableRow tRow = 
         (TranslationTableRow)m_table.getTable().getItem(row).getData();
      switch(col)
      {
         case 0:
         {
            List<PSPair<IPSReference, IPSReference>> avail = getAvailableKeyPairs(row);
            IPSReference locale = tRow.getLocale();
            List<IPSReference> choices = new ArrayList<>();
            for(PSPair<IPSReference, IPSReference> pair : avail)
            {
               if((locale == null || locale.equals(pair.getSecond())) &&
                  !choices.contains(pair.getFirst()))
                  choices.add(pair.getFirst());
            }
            m_cTypeCellEditor.setItems(collectionToSortedArray(choices));
            break;
            
         }
         case 1:
         {
            List<PSPair<IPSReference, IPSReference>> avail = getAvailableKeyPairs(row);
            IPSReference cType = tRow.getContenttype();
            List<IPSReference> choices = new ArrayList<>();
            for(PSPair<IPSReference, IPSReference> pair : avail)
            {
               if((cType == null || cType.equals(pair.getFirst())) &&
                  !choices.contains(pair.getSecond()))
                  choices.add(pair.getSecond());
            }
            m_localeCellEditor.setItems(collectionToSortedArray(choices));
            break;
         }
         case 2:
         {
            m_commCellEditor.setItems(
               collectionToSortedArray(getCommunities(false)));
            break;
         }
         case 3:
         {            
            m_workflowCellEditor.setItems(collectionToSortedArray(
               getAllowedWorkflows(tRow.getCommunity())));
            break;
         }
         default:
         {
            break;
         }
      }
   }
   
   /**
    * Helper method to sort a collection of references and return
    * them as an array.
    * @param coll assumed not <code>null</code>.
    * @return a sorted array of references.
    */
   private Object[] collectionToSortedArray(Collection<IPSReference> coll)
   {
      List<IPSReference> list = new ArrayList<>(coll);
      list.sort(new PSReferenceComparator());
      return list.toArray();
   }
   
      
   
   /**
    * Returns a list of all unused contenttype/locale key pairs
    * @param ignoreRow a row to not be considered or -1 to consider
    * all rows.
    * @return never <code>null</code>, may be empty.
    */
   private List<PSPair<IPSReference, IPSReference>> getUsedKeyPairs(int ignoreRow)
   {
      TableItem[] items = m_table.getTable().getItems();
      TranslationTableRow tRow;
      List<PSPair<IPSReference, IPSReference>> pairs = 
         new ArrayList<>();
      for(int i = 0; i < items.length; i++)
      {
         if(ignoreRow > -1 && i == ignoreRow)
            continue;
         tRow = (TranslationTableRow)items[i].getData();
         IPSReference contenttype = tRow.getContenttype();
         IPSReference locale = tRow.getLocale();
         if(contenttype != null && locale != null)
            pairs.add(new PSPair<>(
               contenttype, locale));
      }
      return pairs;
   }
   
   /**
    * Calculates a list of all available contenttype/locale key pairs
    * for the table.
    * @param ignoreRow a row to not be considered or -1 to consider
    * all rows.
    * @return never <code>null</code>, may be empty.
    */
   private List<PSPair<IPSReference, IPSReference>> getAvailableKeyPairs(int ignoreRow)
   {
      List<PSPair<IPSReference, IPSReference>> pairs = 
         new ArrayList<>();
      Collection<IPSReference> cTypes = getContentTypes(false);
      Collection<IPSReference> locales = getLocales(false);
      List<PSPair<IPSReference, IPSReference>> used = getUsedKeyPairs(ignoreRow);
      
      for(IPSReference cType : cTypes)
      {
         for(IPSReference locale : locales)
         {
            PSPair<IPSReference, IPSReference> pair = new PSPair<>(
               cType, locale);
            if(!used.contains(pair))
               pairs.add(pair);
         }
      }
      return pairs;
      
   }

   private void initCaches(){
      cachedLocales = getLocales(true);
      cachedWorkflows = getWorkflows(true);
      cachedContentTypes = getContentTypes(true);
      cachedCommunities = getCommunities(true);
   }
   /**
    * Convenience method to return a <code>Collection</code> of
    * all known content types.
    * @return a collection of <code>IPSReference</code> objects that
    * represent the content types, never <code>null</code>
    */
   protected Collection<IPSReference> getContentTypes(boolean refresh)
   {
      try
      {
         if(cachedContentTypes == null || cachedContentTypes.isEmpty() || refresh) {
            PSCoreFactory factory = PSCoreFactory.getInstance();
            IPSContentTypeModel model =
                    (IPSContentTypeModel) factory.getModel(PSObjectTypes.CONTENT_TYPE);

            cachedContentTypes = model.getUseableContentTypes(false);
         }
         
      }
      catch (PSModelException e)
      {
         PSWorkbenchPlugin.handleException(
            PSMessages.getString("PSSlotEditor.error.gettingContentTypes"),  //$NON-NLS-1$
            PSMessages.getString(COMMON_ERROR), //$NON-NLS-1$
            e.getLocalizedMessage(),
            e);
      }
      return cachedContentTypes;
   }

   private Collection<IPSReference> cachedCommunities;
   private Collection<IPSReference> cachedContentTypes;
   private Collection<IPSReference> cachedWorkflows;
   private Collection<IPSReference> cachedLocales;

   /**
    * Convenience method to return a <code>Collection</code> of
    * all known communities.
    * @return a collection of <code>IPSReference</code> objects that
    * represent the communities
    */
   protected Collection<IPSReference> getCommunities(boolean refresh)
   {
      try
      {         
            if(cachedCommunities == null || cachedCommunities.isEmpty() || refresh) {
               cachedCommunities = PSCoreUtils.catalog(PSObjectTypes.COMMUNITY, false);
            }
      }
      catch (PSModelException e)
      {
         PSWorkbenchPlugin.handleException(
            PSMessages.getString("PSAutoTranslationEditor.error.catalogingComms"), //$NON-NLS-1$
            PSMessages.getString(COMMON_ERROR), //$NON-NLS-1$
            e.getLocalizedMessage(),
            e);
      }
      return cachedCommunities;
   }
   
   /**
    * Convenience method to return a <code>Collection</code> of
    * all allowed workflows for the passed in community.
    * @param ref the reference for the community to finf workflows
    * on. May be <code>null</code>.
    * @return a collection of <code>IPSReference</code> objects that
    * represent the workflows. Never <code>null</code>, may be empty.
    */
   protected Collection<IPSReference> getAllowedWorkflows(IPSReference ref)
   {
      Collection<IPSReference> results = new ArrayList<>();
      if(ref == null)
         return results;      
      try
      {         
         results = 
            PSSecurityUtils.getVisibilityByCommunity(ref, PSTypeEnum.WORKFLOW);
      }
      catch (PSModelException e)
      {
         PSWorkbenchPlugin.handleException(
            PSMessages.getString("PSAutoTranslationEditor.error.catalogingAllowedWorkflows"), //$NON-NLS-1$
            PSMessages.getString(COMMON_ERROR), //$NON-NLS-1$
            e.getLocalizedMessage(),
            e);
      }
      return results;
   }
   
   /**
    * Convenience method to return a <code>Collection</code> of
    * all known workflows.
    * @return a collection of <code>IPSReference</code> objects that
    * represent the workflows
    */
   protected Collection<IPSReference> getWorkflows(boolean refresh)
   {
      try
      {
         if(cachedWorkflows == null || cachedWorkflows.isEmpty() || refresh) {
            cachedWorkflows =
                    PSCoreUtils.catalog(PSObjectTypes.WORKFLOW, false);
         }
      }
      catch (PSModelException e)
      {
         PSWorkbenchPlugin.handleException(
            PSMessages.getString("PSAutoTranslationEditor.error.catalogingWorkflows"), //$NON-NLS-1$
            PSMessages.getString(COMMON_ERROR), //$NON-NLS-1$
            e.getLocalizedMessage(),
            e);
      }
      return cachedWorkflows;
   }
   
   /**
    * Convenience method to return a <code>Collection</code> of
    * all known locales.
    * @return a collection of <code>IPSReference</code> objects that
    * represent the locales
    */
   protected Collection<IPSReference> getLocales(boolean refresh)
   {

      try
      {
         if(cachedLocales == null || cachedLocales.isEmpty() || refresh) {
            cachedLocales =
                    PSCoreUtils.catalog(PSObjectTypes.LOCALE, false);
         }
      }
      catch (PSModelException e)
      {
         PSWorkbenchPlugin.handleException(
            PSMessages.getString("PSAutoTranslationEditor.error.catalogingLocales"), //$NON-NLS-1$
            PSMessages.getString(COMMON_ERROR), //$NON-NLS-1$
            e.getLocalizedMessage(),
            e);
      }
      return cachedLocales;
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
            "PSAutoTranslationEditor.col.sourceContentType.label",
               "source_content_type",
            "PSAutoTranslationEditor.col.targetLocale.label", "target_locale",
            "PSAutoTranslationEditor.col.targetCommunity.label", 
               "target_community",
            "PSAutoTranslationEditor.col.targetWorkflow.label", 
               "target_workflow"
         });
      }
      return m_helpHintKeyHelper.getKey(
         super.getHelpHintKey(controlInfo), controlInfo);
   }
       
   /**
    * New row object provider for the translation table
    */
   class NewRowObjectProvider implements IPSNewRowObjectProvider
   {

      /* 
       * @see com.percussion.workbench.ui.controls.IPSNewRowObjectProvider#
       * newInstance()
       */
      public Object newInstance()
      {
         return new TranslationTableRow();
      }

      /* 
       * @see com.percussion.workbench.ui.controls.IPSNewRowObjectProvider#
       * isEmpty(java.lang.Object)
       */
      @SuppressWarnings("synthetic-access") //$NON-NLS-1$
      public boolean isEmpty(Object obj)
      {
         TranslationTableRow row = (TranslationTableRow)obj;
         if(getAvailableKeyPairs(-1).isEmpty())
            return true;
         return row.getCommunity() == null || row.getContenttype() == null ||
                 row.getLocale() == null || row.getWorkflow() == null;
      }
      
   }
   
   /**
    * Cell modifier  fot the translation table
    */
   class TranslationTableCellModifier implements ICellModifier
   {

      /* 
       * @see org.eclipse.jface.viewers.ICellModifier#canModify(
       * java.lang.Object, java.lang.String)
       */
      @SuppressWarnings("synthetic-access") //$NON-NLS-1$
      public boolean canModify(@SuppressWarnings("unused") Object element, 
            String property)
      {         
         loadSelectedCellEditor(m_table.getColumnIndex(property));
         return true;
      }

      /* 
       * @see org.eclipse.jface.viewers.ICellModifier#getValue(
       * java.lang.Object, java.lang.String)
       */
      @SuppressWarnings("synthetic-access") //$NON-NLS-1$
      public Object getValue(Object element, String property)
      {
         int col = m_table.getColumnIndex(property);
         TranslationTableRow row = (TranslationTableRow)element;
         switch(col)
         {
            case 0:
               return row.getContenttype();
            case 1:
               return row.getLocale();
            case 2:
               return row.getCommunity();
            case 3:
               return row.getWorkflow();
            default:
               return null;
         }
      }

      /* 
       * @see org.eclipse.jface.viewers.ICellModifier#modify(
       * java.lang.Object, java.lang.String, java.lang.Object)
       */
      @SuppressWarnings("synthetic-access") //$NON-NLS-1$
      public void modify(Object element, String property, Object value)
      {
         int col = m_table.getColumnIndex(property);
         IPSReference val = (IPSReference)value;
         TranslationTableRow row = 
            (TranslationTableRow)((TableItem)element).getData();
         switch(col)
         {
            case 0: {
               row.setContenttype(val);
               break;
            }
            case 1:
               row.setLocale(val);
               break;
            case 2: {
               row.setCommunity(val);
               break;
            }
            case 3: {
               row.setWorkflow(val);
               break;
            }
            default:{
               break;
            }
         }
         m_table.refreshTable();
         
      }
      
   }
   
   /**
    * Label provider for the translation table
    */
   class TranslationTableLabelProvider extends PSAbstractTableLabelProvider
   {
      /* 
       * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(
       * java.lang.Object, int)
       */
      public String getColumnText(Object element, int columnIndex)
      {
         TranslationTableRow row = (TranslationTableRow)element;
         String label;
         switch(columnIndex)
         {
            case 0:
            {
               label = row.getContenttype() == null 
               ? null : row.getContenttype().getLocalLabelKey();
               return StringUtils.defaultString(label);
            }
            case 1:
            {
               label = row.getLocale() == null 
               ? null : row.getLocale().getLocalLabelKey();
               return StringUtils.defaultString(label);
            }
            case 2:
            {
               label = row.getCommunity() == null 
               ? null : row.getCommunity().getLocalLabelKey();
               return StringUtils.defaultString(label);
            }
            case 3:
            {
               label = row.getWorkflow() == null 
               ? null : row.getWorkflow().getLocalLabelKey();
               return StringUtils.defaultString(label);
            }
            default:
               return ""; //$NON-NLS-1$
         }
         
      }
   }
   
   /**
    * Convenience object to handle the rows in the auto
    * translation table.
    */
   class TranslationTableRow
   {
      TranslationTableRow()
      {
         
      }

      TranslationTableRow(IPSReference contenttype, IPSReference community,
         IPSReference locale, IPSReference workflow)
      {
         mi_contenttype = contenttype;
         mi_community = community;
         mi_locale = locale;
         mi_workflow = workflow;
      }
      
      TranslationTableRow(PSAutoTranslation trans)
      {         
         mi_contenttype = PSUiUtils.getReferenceById(
            getContentTypes(false), trans.getContentTypeId());
         mi_community = PSUiUtils.getReferenceById(
            getCommunities(false), trans.getCommunityId());
         mi_locale = PSUiUtils.getReferenceByName(
            getLocales(false), trans.getLocale());
         mi_workflow = PSUiUtils.getReferenceById(
            getWorkflows(false), trans.getWorkflowId());
      }
      
      public PSAutoTranslation getAutoTranslation()
      {
         PSAutoTranslation trans = new PSAutoTranslation();
         trans.setCommunityId(mi_community.getId().longValue());
         trans.setContentTypeId(mi_contenttype.getId().longValue());
         trans.setLocale(mi_locale.getName());
         trans.setWorkflowId(mi_workflow.getId().longValue());
         return trans;
      }
      
      /**
       * @return Returns the community.
       */
      public IPSReference getCommunity()
      {
         return mi_community;
      }
      /**
       * @param community The community to set.
       */
      public void setCommunity(IPSReference community)
      {
         mi_community = community;
      }
      /**
       * @return Returns the contenttype.
       */
      public IPSReference getContenttype()
      {
         return mi_contenttype;
      }
      /**
       * @param contenttype The contenttype to set.
       */
      public void setContenttype(IPSReference contenttype)
      {
         mi_contenttype = contenttype;
      }
      /**
       * @return Returns the locale.
       */
      public IPSReference getLocale()
      {
         return mi_locale;
      }
      /**
       * @param locale The locale to set.
       */
      public void setLocale(IPSReference locale)
      {
         mi_locale = locale;
      }
      /**
       * @return Returns the workflow.
       */
      public IPSReference getWorkflow()
      {
         return mi_workflow;
      }
      /**
       * @param workflow The workflow to set.
       */
      public void setWorkflow(IPSReference workflow)
      {
         mi_workflow = workflow;
      }
      
      private IPSReference mi_contenttype;
      private IPSReference mi_locale;
      private IPSReference mi_community;
      private IPSReference mi_workflow;
   }
   
   /**
    * Cache of the help hint helper object
    */
   private PSHelpHintKeyHelper m_helpHintKeyHelper;

   private PSSortableTable m_table;
   private PSComboBoxCellEditor m_cTypeCellEditor;
   private PSComboBoxCellEditor m_commCellEditor;
   private PSComboBoxCellEditor m_localeCellEditor;
   private PSComboBoxCellEditor m_workflowCellEditor;

   private static final String COMMON_ERROR="common.error.title";

}
