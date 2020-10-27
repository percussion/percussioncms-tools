/******************************************************************************
 *
 * [ PSContentTypeEditor.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form;

import com.percussion.client.IPSReference;
import com.percussion.client.PSModelChangedEvent;
import com.percussion.client.PSModelChangedEvent.ModelEvents;
import com.percussion.client.PSObjectTypes;
import com.percussion.cms.objectstore.PSFieldDefinition;
import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.design.objectstore.PSContentEditor;
import com.percussion.design.objectstore.PSContentEditorMapper;
import com.percussion.design.objectstore.PSContentEditorPipe;
import com.percussion.design.objectstore.PSControlDependencyMap;
import com.percussion.design.objectstore.PSControlRef;
import com.percussion.design.objectstore.PSDependency;
import com.percussion.design.objectstore.PSDisplayMapper;
import com.percussion.design.objectstore.PSDisplayMapping;
import com.percussion.design.objectstore.PSDisplayText;
import com.percussion.design.objectstore.PSField;
import com.percussion.design.objectstore.PSFieldSet;
import com.percussion.design.objectstore.PSSearchProperties;
import com.percussion.design.objectstore.PSUISet;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.workbench.ui.IPSUiConstants;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.PSWorkbenchPlugin;
import com.percussion.workbench.ui.controls.PSSortableTable;
import com.percussion.workbench.ui.editors.form.ce.PSContentEditorDefinition;
import com.percussion.workbench.ui.editors.form.ce.PSContentTypeChildTab;
import com.percussion.workbench.ui.editors.form.ce.PSContentTypeMainTab;
import com.percussion.workbench.ui.editors.form.ce.PSContentTypePropertiesTab;
import com.percussion.workbench.ui.editors.form.ce.PSFieldTableRowDataObject;
import com.percussion.workbench.ui.util.PSUiUtils;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Control;
import org.w3c.dom.Document;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.percussion.workbench.ui.editors.form.ce.PSContentEditorDefinition.demergeFields;
import static com.percussion.workbench.ui.editors.form.ce.PSContentEditorDefinition.mergeFields;

/**
 * Provides a multi-tabbed UI for modifying a content type design object. The
 * first tab contains the parent fields, the last tab the general properties,
 * and all intermediate tabs are children.
 * 
 * @version 6.0
 * @author Paul Howard
 */
public class PSContentTypeEditor extends PSMultiPageEditorBase
      implements
         IPSUiConstants
{
   public PSContentTypeEditor() 
   {
      super();
   }

   /*
    * @see com.percussion.workbench.ui.editors.form.PSEditorBase
    *      #isValidReference(com.percussion.client.IPSReference)
    */
   @Override
   public boolean isValidReference(IPSReference ref)
   {
      if (ref == null)
         return false; // Should never happen
      if (ref.getObjectType().getPrimaryType() == PSObjectTypes.CONTENT_TYPE)
         return true;
      return false;
   }
   
   @Override
   public void reloadControlValues()
   {
      try
      {
         demergeFields((PSItemDefinition) m_data);
         super.reloadControlValues();
      }
      catch (Exception e)
      {
         PSUiUtils.handleExceptionSync("Demerging system and shared defs", 
               null, null, e);
      }
   }

   /*
    * @see com.percussion.workbench.ui.editors.form.PSEditorBase#
    *      loadControlValues()
    */
   public void loadControlValues(Object designObject)
   {
      // Get the systemdef and get the shareddef and then get the merged mapper.
      try
      {
         if (!m_disableMerge)
         {
            mergeFields((PSItemDefinition) m_data);
         }
      }
      catch (Exception e)
      {
         PSUiUtils.handleExceptionSync("Merging system and shared defs",  
            null, null, e);
      }

      loadSavedItemDef();
      
      m_ctypePage.loadControlValues(designObject);
      m_propsPage.loadControlValues(designObject);
      //Handle the child tables here.
      //If we are importing xml then remove the old child table tabs if any
      if(m_childPages != null && !m_childPages.isEmpty())
      {
         Set<String> frefs = new HashSet<String>(m_childPages.keySet());
         for(String fref:frefs)
            removeChildPage(fref);
      }
      //Create the child tabs using the current item definition.
      createChildPages();
      //Load the control values for the child pages.
      Iterator iter = m_childPages.values().iterator();
      while (iter.hasNext())
      {
         ((PSContentTypeChildTab) iter.next()).loadControlValues(designObject);
      }
   }
   
   @Override
   protected void handleModelChanged(PSModelChangedEvent event)
   {
      if (event.getEventType().equals(ModelEvents.RENAMED))
      {
         m_disableMerge = true;
      }
      try
      {
         super.handleModelChanged(event);
      }
      finally
      {
         m_disableMerge = false;
      }
   }

   @Override
   public void doSave(IProgressMonitor pMonitor)
   {
      //Warn if re-index is required
      if (isReindexRequired())
      {
         final String reindexWarnTitle = PSMessages
               .getString("PSContentTypeEditor.warning.reindexRequired.title");
         final String reindexWarn = PSMessages
               .getString(
                     "PSContentTypeEditor.warning.reindexRequired.message");
         MessageDialog.openInformation(getSite().getShell(), reindexWarnTitle,
               reindexWarn);
                 
         m_isReindexRequired = false;
      }
      
      //Demerge the mapper before saving the 
      if (prepareContentTypeForSave())
      {
         try
         {
            super.doSave(pMonitor);
         }
         finally
         {
            try
            {
               mergeFields((PSItemDefinition) m_data);
               loadSavedItemDef();
            }
            catch (Exception e)
            {
               PSUiUtils.handleExceptionSync("Merging system and shared defs",  
                  null, null, e);
            }
         }
      }
      resetTableNames();
      //set icon control values
      PSItemDefinition itemDef = (PSItemDefinition) m_data;
      PSContentEditor editor = itemDef
            .getContentEditor();
      if(editor.getIconSource().equals(PSContentEditor.ICON_SOURCE_SPECIFIED))
      {
         m_propsPage.setIconControlValues();
      }
      //Setting the icon control values may trigger the editor dirty.
      //Set it to not dirty as we just saved the content type
      this.setClean();
   }

   /**
    * Performs activities required for save - does some validation,
    * updates content type. This method calls
    * {@link PSContentEditorDefinition#demergeFields(PSItemDefinition)}, which 
    * affects the data object. The caller of this method must call 
    * {@link PSContentEditorDefinition#mergeFields(PSItemDefinition)}
    * when they have finished their operation.
    * 
    * @return <code>true</code> if the save should proceed, <code>false</code> 
    * if validation or other errors should prohibit the save (in which case
    * error messages will have already been displayed).
    */
   private boolean prepareContentTypeForSave()
   {
      final PSItemDefinition itemDef = (PSItemDefinition) m_data;
      assert itemDef != null;
      PSContentEditorPipe pipe = (PSContentEditorPipe) itemDef
            .getContentEditor().getPipe();
      
      PSContentEditorMapper mapper = pipe.getMapper();
      // Content editor needs at least one local field in parent field set and
      // each child field set to function properly.
      // If none add a place holder and inform user about it.
      Collection<String> fsetNames = addPlaceholderField(mapper);
      if(!fsetNames.isEmpty())
      {
         Object[] obj = {fsetNames.toString()};
         final String missingLocalFieldErrTitle = PSMessages
               .getString("PSContentTypeEditor.error.missingLocalField.title");
         final String missingLocalFieldErr = MessageFormat.format(PSMessages
               .getString("PSContentTypeEditor.error.missingLocalField.message"),obj);
         MessageDialog.openInformation(getSite().getShell(),
               missingLocalFieldErrTitle, missingLocalFieldErr);
      }

      updateControlDependencyMap(pipe);
      try
      {
         PSContentEditorDefinition.updateExcludes(mapper);
      }
      catch (Exception e)
      {
         PSWorkbenchPlugin.handleException("Saving", null, null, e); 
         return false;
      }

      // Get the systemdef and get the shareddef and then get the merged mapper.
      try
      {
         PSContentEditorDefinition.demergeFields((PSItemDefinition) m_data);
      }
      catch (Exception e)
      {
         PSWorkbenchPlugin.handleException("Saving", null, null, e); 
         return false;
      }
      return true;
   }
   
   /**
    * Checks the parent and child field sets for local fields if not found then
    * calls
    * {@link #addPlaceholderFieldToFieldSet(String, PSSortableTable, PSContentEditorMapper)}
    * to add a place holder field. Returns the list of the field sets for which
    * the placeholder field has been added.
    * 
    * @param mapper The Object of PSContentEditorMapper for which the
    *           placeholder fields needs to be added. Assumed not
    *           <code>null</code>.
    * @return Collection of field set names for that are missing local fields.
    *         May be empty but never <code>null</code>.
    */
   private Collection<String> addPlaceholderField(PSContentEditorMapper mapper)
   {
      List<String> fsetNames = new ArrayList<String>(); 
      if (!hasLocalFields(mapper.getFieldSet()))
      {
         addPlaceholderFieldToFieldSet(mapper.getFieldSet().getName(),
               m_ctypePage.getSortableTable(), mapper);
         fsetNames.add("Parent Editor");
      }
      for (String chName : m_childPages.keySet())
      {
         PSContentTypeChildTab chPage = m_childPages.get(chName);
         PSSortableTable table = chPage.getSortableTable();
         if (table.getValues().isEmpty())
         {
            addPlaceholderFieldToFieldSet(chName, table, mapper);
            fsetNames.add(chName);
         }
      }
      return fsetNames;
   }

   
   /**
    * Adds a place holder field to the mapper and adds a row to the table
    * identified by the supplied fieldset name.
    * 
    * @param fsetName The name of the field set for which the placeholder field
    *           needs to be added. Assumed not <code>null</code>.
    * @param table The PSSortableTable object that shows the fields of the Field
    *           Set represented by the fsetName.
    * @param mapper The PSContentEditorMapper of the content type. Assumed not
    *           <code>null</code>.
    */
   @SuppressWarnings("unchecked")
   private void addPlaceholderFieldToFieldSet(String fsetName,
         PSSortableTable table, PSContentEditorMapper mapper)
   {
      PSFieldDefinition fieldDef = createPlaceholderField();
      PSFieldTableRowDataObject rowData = new PSFieldTableRowDataObject(
            fieldDef);
      List<PSFieldTableRowDataObject> rows = table.getValues();
      rows.add(rowData);
      table.setValues(rows);
      table.refreshTable();

      PSFieldSet parentSet = mapper.getFieldSet();
      PSDisplayMapper dmapper = mapper.getUIDefinition().getDisplayMapper();
      if (fsetName.equalsIgnoreCase(parentSet.getName()))
      {
         parentSet.add(fieldDef.getField());
         dmapper.add(fieldDef.getMapping());
      }
      else
      {
         PSFieldSet chSet = (PSFieldSet) parentSet.get(fsetName);
         chSet.add(fieldDef.getField());
         Iterator iter = dmapper.iterator();
         while (iter.hasNext())
         {
            PSDisplayMapping dm = (PSDisplayMapping)iter.next();
            if (dm.getDisplayMapper() != null
                  && dm.getDisplayMapper().getFieldSetRef().equalsIgnoreCase(
                        fsetName))
            {
               dm.getDisplayMapper().add(fieldDef.getMapping());
               break;
            }
         }
      }
   }

   /**
    * Creates a placeholder field. Uses sys_HiddenInput as the control. If a
    * field already exists with the name placeholder, then appends it with X,
    * where X is a number that makes the placeholderX name unique.
    * 
    * @return PSFieldDefinition Object that represents the placeholder field.
    * Never <code>null</code>.
    */
   private PSFieldDefinition createPlaceholderField()
   {
      String placeholder = "placeholder";
      int counter = 1;
      String temp = placeholder;
      while (doesFieldExist(temp))
      {
         temp = placeholder + Integer.toString(counter++);
      }
      placeholder = temp;

      PSUISet uiSet = new PSUISet();
      PSDisplayText label = new PSDisplayText(PSContentEditorDefinition
            .convertToProper(placeholder));
      uiSet.setLabel(label);
      uiSet.setErrorLabel(label);
      PSControlRef ref = new PSControlRef("sys_HiddenInput");
      ref.setId(PSContentEditorDefinition.getUniqueId());
      uiSet.setControl(ref);

      PSField fld = PSContentEditorDefinition.getDefaultField(placeholder,
            PSContentEditorDefinition.LOCALDEF_PARENT_EDITOR);
      PSDisplayMapping dm = new PSDisplayMapping(placeholder, uiSet);

      PSFieldDefinition fieldDef = new PSFieldDefinition(fld, dm);
      return fieldDef;
   }

   /**
    * Updates the control dependencies, Clears the dependencies first
    * and then walks through all the rows in parent and child tables and 
    * sets the dependencies.
    * @param pipe Content editor pipe for which the dependencies needs to
    * be set. Assumed not <code>null</code>.
    */
   @SuppressWarnings("unchecked")
   private void updateControlDependencyMap(PSContentEditorPipe pipe)
   {
      //Update the dependency map
      //Walk through all the rows in parent and child mapper
      PSSortableTable parentTable = m_ctypePage.getSortableTable();
      List<PSFieldTableRowDataObject> rows = parentTable.getValues();
      for(String chName:m_childPages.keySet())
      {
         PSSortableTable chTable = m_childPages.get(chName).getSortableTable();
         rows.addAll(chTable.getValues());
      }
      PSControlDependencyMap depMap = pipe.getControlDependencyMap();
      depMap.clearControlDependencies();
      for(PSFieldTableRowDataObject row : rows)
      {
         //Get the dependencies from the data row.
         List<PSDependency> deps = row.getControlDependencies();
         if(deps != null)
         {
            depMap.setControlDependencies(row.getDisplayMapping(),deps);
         }
      }
   }

   // see base class
   @Override
   protected boolean tuneForSaveAs() throws Exception
   {
      return prepareContentTypeForSave();
   }

   // see base class
   @Override
   protected void saveForSaveAs() throws Exception
   {
      demergeFields((PSItemDefinition) m_data);
      try
      {
         super.saveForSaveAs();
      }
      finally
      {
         mergeFields((PSItemDefinition) m_data);
      }
      resetTableNames();
   }
   
   /**
    * The fields table composite holds a memeber variable of the database table
    * name. This name is set while loading the control values, but while saving
    * the content types the table names may change to make them unique. We need
    * to reset the table name after saving the content type.
    * This sets the table name on the parent table and walks through all the
    * child tables and sets the table name for child tables also.
    */
   private void resetTableNames()
   {
      PSItemDefinition itemDef = (PSItemDefinition) m_data;
      PSContentEditorPipe pipe = (PSContentEditorPipe) itemDef
            .getContentEditor().getPipe();
      PSFieldSet fieldSet = pipe.getMapper().getFieldSet();
      m_ctypePage.setDBTableName(fieldSet);
      Iterator iter = fieldSet.getAll();
      while (iter.hasNext())
      {
         Object obj = iter.next();
         if (obj instanceof PSFieldSet)
         {
            PSFieldSet set = (PSFieldSet) obj;
            PSContentTypeChildTab chtab = m_childPages.get(set.getName());
            if (chtab != null)
               chtab.setDBTableName(set);
         }
      }

   }
   
   /*
    * @see com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater#
    *      updateDesignerObject(java.lang.Object, java.lang.Object)
    */
   public void updateDesignerObject(Object designObject, Object control)
   {
      m_ctypePage.updateDesignerObject(designObject, control);
      m_propsPage.updateDesignerObject(designObject, control);
      Iterator iter = m_childPages.values().iterator();
      while (iter.hasNext())
      {
         ((PSContentTypeChildTab) iter.next()).updateDesignerObject(
               designObject, control);
      }
   }
   
   /**
    * @return Returns the properties tab of this content type editor. May be
    * <code>null</code>. If no properties tab is defined for the content editor. 
    */
   public PSContentTypePropertiesTab getPropsTab()
   {
      return m_propsPage;
   }

   @Override
   protected void createPages()
   {
      createContentTypePage();
      createPropertiesPage();
   }

   private void createContentTypePage()
   {
      m_ctypePage = new PSContentTypeMainTab(getContainer(), SWT.NONE, this);
      m_ctypePage.setLayout(new FormLayout());
      int index = addPage(m_ctypePage);
      setPageText(index, "Content Type"); 
      setPageTitle(index, "Editing Content Type"); 
   }

   private void createChildPages()
   {
      m_childPages = new HashMap<String, PSContentTypeChildTab>();
      PSItemDefinition itemDef = (PSItemDefinition) m_data;
      PSContentEditorPipe pipe = (PSContentEditorPipe) itemDef
            .getContentEditor().getPipe();
      PSFieldSet fieldSet = pipe.getMapper().getFieldSet();
      PSDisplayMapper displayMapper = 
         pipe.getMapper().getUIDefinition().getDisplayMapper();
      Iterator mappings = displayMapper.iterator();
      while (mappings.hasNext())
      {
         PSDisplayMapping mapping = (PSDisplayMapping) mappings.next();

         PSDisplayMapper mapper = mapping.getDisplayMapper();
         if (mapper != null)
         {
            String fieldSetRef = mapper.getFieldSetRef();
            PSFieldSet set = (PSFieldSet) fieldSet.get(fieldSetRef);
            if (set != null && set.getType() == PSFieldSet.TYPE_COMPLEX_CHILD)
               addChildPage(fieldSetRef);
         }
      }
   }

   /**
    * Checks whether a field or fieldset exists in this editor with the supplied
    * name.
    * 
    * @param fieldName name of the field to be checked. If <code>null</code>
    *           or empty returns false.
    * @return <code>true</code> if field exists other wise <code>false</code>.
    */
   public boolean doesFieldExist(String fieldName)
   {
      if (StringUtils.isBlank(fieldName))
         return false;
      PSItemDefinition itemDef = (PSItemDefinition) m_data;
      PSContentEditorPipe pipe = (PSContentEditorPipe) itemDef
            .getContentEditor().getPipe();
      PSContentEditorMapper mapper = pipe.getMapper();
      PSFieldSet fset = mapper.getFieldSet();
      Object obj = PSContentEditorDefinition.getFieldOrSetByName(fset,
            fieldName);
      return obj == null ? false : true;
   }
   
   /**
    * Gets a field with the supplied name from this editor.
    * 
    * @param fieldName name of the field to be checked.  Assumed not
    * <code>null</code> or empty.
    * @return {@link PSField} object representation of the field if it exists,
    * <code>null</code> if the field is not found.
    */
   private PSField getField(String fieldName)
   {
      PSItemDefinition itemDef = (PSItemDefinition) m_data;
      PSContentEditorPipe pipe = (PSContentEditorPipe) itemDef
            .getContentEditor().getPipe();
      PSContentEditorMapper mapper = pipe.getMapper();
      
      PSFieldSet fset = mapper.getFieldSet();
      Object obj = PSContentEditorDefinition.getFieldOrSetByName(fset,
            fieldName);
      
      if (obj == null || obj instanceof PSFieldSet)
         return null;
      else
         return (PSField) obj;
   }
   
   /**
    * Gets a field set with the supplied name from this editor.
    * 
    * @param fieldName name of the field set to be checked.  Assumed not
    * <code>null</code>.
    * @return {@link PSFieldSet} object representation of the field set if it
    * exists, <code>null</code> if the field set is not found.
    */
   private PSFieldSet getFieldSet(String fieldName)
   {
      PSItemDefinition itemDef = (PSItemDefinition) m_data;
      PSContentEditorPipe pipe = (PSContentEditorPipe) itemDef
            .getContentEditor().getPipe();
      PSContentEditorMapper mapper = pipe.getMapper();
      
      PSFieldSet fset = mapper.getFieldSet();
      Object obj = PSContentEditorDefinition.getFieldOrSetByName(fset,
            fieldName);
      
      if (obj == null || obj instanceof PSField)
         return null;
      else
         return (PSFieldSet) obj;
   }
   
   
   /**
    * Get all field names from parent and child editors if any.
    * 
    * @return String[] of field names may be empty, but never <code>null</code>.
    */
   public String[] getAllFieldNames()
   {
      final List<String> fnames = new ArrayList<String>();
      PSItemDefinition itemDef = (PSItemDefinition) m_data;
      PSContentEditorPipe pipe = (PSContentEditorPipe) itemDef
            .getContentEditor().getPipe();
      PSContentEditorMapper mapper = pipe.getMapper();
      PSFieldSet fset = mapper.getFieldSet();
      Iterator iter = fset.getAll();
      while(iter.hasNext())
      {
         Object obj = iter.next();
         if(obj == null)
            continue;
         else if(obj instanceof PSField)
            fnames.add(((PSField)obj).getSubmitName());
         else if(obj instanceof PSFieldSet)
            fnames.add(((PSFieldSet)obj).getName());
      }
      return fnames.toArray(new String[fnames.size()]);
   }
   
   /**
    * 
    */
   public void addChildPage(String fieldSetRef)
   {
      PSContentTypeChildTab chTab = new PSContentTypeChildTab(getContainer(),
            SWT.NONE, this, fieldSetRef);
      chTab.setLayout(new FormLayout());
      int index = getPageCount() - 1;
      addPage(index, chTab);
      setPageText(index, fieldSetRef);
      setPageTitle(index, "Editing Child " + fieldSetRef); 
      m_childPages.put(fieldSetRef, chTab);
      m_helpManager.registerControls(chTab);
   }

   public void removeChildPage(String fieldSetRef)
   {
      int index = getPageIndexByName(fieldSetRef, false);
      if (index < 1 || index >= getPageCount())
      {
         throw new IndexOutOfBoundsException("Illegal page index: " + index);
      }
      m_childPages.remove(fieldSetRef);
      removePage(index);
   }

   public void renameChildPage(String oldFieldSetRef, String newFieldSetRef)
   {
      int index = getPageIndexByName(oldFieldSetRef, false);
      if (index < 1 || index >= getPageCount())
      {
         // This should not happen as we add these pages.
         return;
      }
      setPageText(index,newFieldSetRef);
      PSContentTypeChildTab chTab = m_childPages.get(oldFieldSetRef);
      chTab.setFieldSetName(newFieldSetRef);
      m_childPages.remove(oldFieldSetRef);
      m_childPages.put(newFieldSetRef,chTab);
   }
   
   private void createPropertiesPage()
   {
      m_propsPage = new PSContentTypePropertiesTab(getContainer(), SWT.NONE,
            this);
      m_propsPage.setLayout(new FormLayout());
      int index = addPage(m_propsPage);
      setPageText(index, "Properties"); 
      setPageTitle(index, "Editing Properties"); 
   }
   
   /**
    * Checks whether the supplied fieldset has at least one local field
    * 
    * @param set Object of PSFeildSet to be checked for local fields
    * @return <code>true</code> if the supplied set has at least one local
    *         field otherwise <code>false</code>.
    */
   private boolean hasLocalFields(PSFieldSet set)
   {
      boolean hasLocal = false;
      Iterator iter = set.getAll();
      while(iter.hasNext())
      {
         Object obj = iter.next();
         if (obj instanceof PSField && ((PSField)obj).isLocalField())
         {
            hasLocal = true;
            break;
         }
      }
      return hasLocal;
   }

   /* 
    * @see com.percussion.workbench.ui.editors.form.PSMultiPageEditorBase#
    * getHelpKey(org.eclipse.swt.widgets.Control)
    */
   @Override
   public String getHelpKey(@SuppressWarnings("unused") Control control) 
   {
      int pageIndex = getActivePage();
      Control pageControl = 
         ((ScrolledComposite)getControl(pageIndex)).getContent();
      String postfix = ""; 
      if(pageControl instanceof PSContentTypePropertiesTab)
      {
         postfix = "properties";
      }
      else if(pageIndex == 0)
      {
         postfix = "contenttype";             
      }
      else
      {
         postfix = "fieldset";
      }
      return getClass().getName() + '_' + postfix;
   }
   
   /**
    * Determines if the content type needs to be re-indexed for search.  A
    * content type requires re-indexing if at least one of the following
    * conditions is met:
    * <br>
    * <br>
    * 1. A field which is saved and searchable is deleted.
    * <br>
    * 2. A child table which is saved with at least one searchable field is
    * deleted.
    * <br>
    * 3. The mime type of a field which is saved and searchable is modified.
    * <br>
    * 4. The search properties of a field which is saved are modified.
    * <br>
    * <br>
    * Sets the re-index flag, {@link #m_isReindexRequired}, to <code>true</code>
    * if the content type needs to be re-indexed for search.  The check will
    * only occur when the re-index flag is <code>false</code>.
    * 
    * @return <code>true</code> if the content types requires re-indexing,
    * <code>false</code> otherwise.
    */
   private boolean isReindexRequired()
   {
      if (!m_isReindexRequired)
      {
         try
         {
            PSContentEditorPipe savedPipe = (PSContentEditorPipe) m_savedItemDef
               .getContentEditor().getPipe();
            PSContentEditorMapper savedMapper = savedPipe.getMapper();
            PSFieldSet savedFset = savedMapper.getFieldSet();
            if (!savedFset.isUserSearchable())
            {
               // content type is not searchable, so warning is not required
               return false;
            }
                        
            Iterator savedIter = savedFset.getAll();
            while (savedIter.hasNext())
            {
               Object obj = savedIter.next();
               if (obj instanceof PSField)
               {
                  PSField savedField = (PSField) obj;
                  PSField curField = getField(savedField.getSubmitName());
                  
                  if (isReindexRequired(curField, savedField))
                  {
                     m_isReindexRequired = true;
                     break;
                  }
               }
               else if (obj instanceof PSFieldSet)
               {
                  PSFieldSet savedChildTable = (PSFieldSet) obj;
                  PSFieldSet curChildTable = 
                     getFieldSet(savedChildTable.getName());
                  if (curChildTable == null)
                  {
                     //child table has been deleted, check for searchable fields
                     boolean isFieldSearchable = false;
                     for (PSField childField : savedChildTable.getAllFields(
                           false))
                     {
                        if (childField.getSearchProperties().
                              isUserSearchable())
                        {
                           isFieldSearchable = true;
                           break;
                        }
                     }
                     
                     if (isFieldSearchable)
                     {
                        m_isReindexRequired = true;
                        break;
                     }
                  }
                  else if (isReindexRequired(curChildTable, savedChildTable))
                  {
                     m_isReindexRequired = true;
                     break;
                  }
               }
            }                 
         }
         catch (Exception e)
         {
            PSWorkbenchPlugin.handleException("Saving", null, null, e); 
         }
      }
      
      return m_isReindexRequired;
   }
   
   /**
    * Determines if a field has been modified in a way which requires the
    * content type to be re-indexed for search.  See
    * {@link #isReindexRequired()} for details.
    * 
    * @param curField The current version of the field.
    * @param savedField The saved version of the field.
    * 
    * @return <code>true</code> if the content types requires re-indexing based
    * on the changes to the field, <code>false</code> otherwise.
    */
   private boolean isReindexRequired(PSField curField, PSField savedField)
   {
      boolean isSearchable = savedField.getSearchProperties().
      isUserSearchable();
      
      if (curField == null)
      {
         //field has been deleted
         if (isSearchable)
            return true;
      }
      else
      {
         if (isSearchable)
         {
            //check for modifications to mime type
            String savedMimeType = savedField.getMimeType();
            PSField.PSMimeTypeModeEnum savedMimeTypeMode = savedField
                  .getMimeTypeMode();
            String savedMimeTypeValue = savedField.getMimeTypeValue();

            String curMimeType = curField.getMimeType();
            PSField.PSMimeTypeModeEnum curMimeTypeMode = curField
                  .getMimeTypeMode();
            String curMimeTypeValue = curField.getMimeTypeValue();
            
            if ((curMimeType != null && 
                  !curMimeType.equals(savedMimeType)) ||
                  (savedMimeType != null &&
                        !savedMimeType.equals(curMimeType)))
            {
               return true;
            }
            else if ((curMimeTypeMode != null && 
                  !curMimeTypeMode.equals(savedMimeTypeMode)) ||
                  (savedMimeTypeMode != null &&
                        !savedMimeTypeMode.equals(curMimeTypeMode)))
            {
               return true;
            }
            else if ((curMimeTypeValue != null && 
                  !curMimeTypeValue.equals(savedMimeTypeValue)) ||
                  (savedMimeTypeValue != null &&
                        !savedMimeTypeValue.equals(curMimeTypeValue)))
            {
               return true;
            }
         }
         
         //check for modifications to search properties
         PSSearchProperties savedSearchProps = 
            savedField.getSearchProperties();
         PSSearchProperties curSearchProps =
            curField.getSearchProperties();
         
         if (!curSearchProps.equals(savedSearchProps))
            return true;
      }
                  
      return false;
   }
   
   /**
    * Determines if a field set has been modified in a way which requires the
    * content type to be re-indexed for search.  See
    * {@link #isReindexRequired()} for details.
    * 
    * @param curFieldSet The current version of the field set.
    * @param savedFieldSet The saved version of the field set.
    * 
    * @return <code>true</code> if the content types requires re-indexing based
    * on the changes to the field set, <code>false</code> otherwise.
    */
   private boolean isReindexRequired(PSFieldSet curFieldSet,
         PSFieldSet savedFieldSet)
   {
      Iterator savedIter = savedFieldSet.getAll(false);
      while (savedIter.hasNext())
      {
         Object obj = savedIter.next();
         if (obj instanceof PSFieldSet)
            continue;
         
         PSField savedField = (PSField) obj;
         PSField curField = (PSField) curFieldSet.get(
               savedField.getSubmitName());
         
         if (isReindexRequired(curField, savedField))
            return true;
      }
      
      return false;
   }
   
   /**
    * Loads the current version of the item definition as the saved version of
    * the item definition.  This is performed during the initial load of the
    * editor in {@link #loadControlValues(Object)}, and also during save in
    * {@link #doSave(IProgressMonitor)}.
    */
   private void loadSavedItemDef()
   {
      PSItemDefinition origData = (PSItemDefinition) m_data;
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      try
      {
         m_savedItemDef = new PSItemDefinition(origData.toXml(doc));
      }
      catch (PSUnknownNodeTypeException e)
      {
         PSUiUtils.handleExceptionSync("Loading item definition", null, null,
               e);
      }
   }
   
   // page composites
   private PSContentTypeMainTab m_ctypePage;

   private PSContentTypePropertiesTab m_propsPage;

   private Map<String, PSContentTypeChildTab> m_childPages;

   /**
    * If <code>true</code> the editor does not merge system fields.
    * Is used to disable field merge when it is known that fields are already
    * merged.
    * <p>Usually its value is <code>false</code>. It is changed to
    * <code>true</code> only during calls to {@link #loadControlValues(Object)}
    * which need field merging disabled. The code disabling merging needs
    * to make sure merging is enabled after loading is done, so the flag is
    * not left with <code>true</code>.
    */
   private boolean m_disableMerge;
   
   /**
    * Flag which indicates whether the content type should be re-indexed for
    * search following a save.
    */
   private boolean m_isReindexRequired = false;
   
   /**
    * Used to store the saved version of the current item definition.  Loaded
    * in {@link #loadSavedItemDef()}.
    */
   private PSItemDefinition m_savedItemDef;
}
