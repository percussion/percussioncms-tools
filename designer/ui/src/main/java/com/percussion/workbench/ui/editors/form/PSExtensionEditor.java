/******************************************************************************
 *
 * [ PSExtensionEditor.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form;

import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSModelException;
import com.percussion.client.PSMultiOperationException;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.models.IPSCmsModel;
import com.percussion.client.models.IPSExtensionModel;
import com.percussion.extension.PSExtensionDef;
import com.percussion.util.PSIgnoreCaseStringComparator;
import com.percussion.workbench.ui.controls.IPSNewRowObjectProvider;
import com.percussion.workbench.ui.controls.PSAbstractTableLabelProvider;
import com.percussion.workbench.ui.controls.PSComboBoxCellEditor;
import com.percussion.workbench.ui.controls.PSSortableTable;
import com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater;
import com.percussion.workbench.ui.editors.common.PSJexlExtensionContextFilter;
import com.percussion.workbench.ui.util.PSControlInfo;
import com.percussion.workbench.ui.util.PSHelpHintKeyHelper;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PSExtensionEditor extends PSEditorBase
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
      if(ref.getObjectType().getPrimaryType() == 
         PSObjectTypes.EXTENSION)
         return true;
      return false;
   }

   /* 
    * @see com.percussion.workbench.ui.editors.form.PSEditorBase#
    * createControl(org.eclipse.swt.widgets.Composite)
    */
   @Override
   public void createControl(Composite comp)
   {
      if(m_isJavaExt)
      {
         m_comp = new PSExtensionJavaComposite(comp, this);
      }
      else
      {
         m_comp = new PSExtensionJavascriptComposite(comp, this);
      }

   }

   /* 
    * @see com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater#
    * updateDesignerObject(java.lang.Object, java.lang.Object)
    */
   public void updateDesignerObject(Object designObject, Object control)
   {
      m_comp.updateDesignerObject(designObject, control);
   }

   /* 
    * @see com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater#
    * loadControlValues(java.lang.Object)
    */
   public void loadControlValues(Object designObject)
   {
      m_comp.loadControlValues(designObject);
   }
   
   /* 
    * @see com.percussion.workbench.ui.editors.form.PSEditorBase#init(
    * org.eclipse.ui.IEditorSite, org.eclipse.ui.IEditorInput)
    */
   @Override
   public void init(IEditorSite site, IEditorInput input) throws PartInitException
   {      
      super.init(site, input);
      PSExtensionDef def = (PSExtensionDef)m_data;
      String handler = def.getRef().getHandlerName();
      m_isJavaExt = handler.equalsIgnoreCase(
         IPSExtensionModel.Handlers.JAVA.toString());
   }
   
   /* 
    * @see org.eclipse.ui.part.EditorPart#setPartName(java.lang.String)
    */
   @Override
   protected void setPartName(String partName)
   {
      
      super.setPartName(
         partName.substring(partName.lastIndexOf("/") + 1));
   }
   
   /**
    * All the contexts.
    */
   public static List<String> loadContexts() throws PSModelException,
      PSMultiOperationException
   {
      final IPSCmsModel model = PSCoreFactory.getInstance().getModel(
         PSObjectTypes.EXTENSION);
      final List<IPSReference> extensionRefs =
            new ArrayList<IPSReference>(model.catalog());
      final Object[] extensions =
         model.load(extensionRefs.toArray(new IPSReference[0]),
               false, false);
      final Set<String> contextSet = new HashSet<String>();
      for (final Object o : extensions)
      {
         final PSExtensionDef extension = (PSExtensionDef) o;
         contextSet.add(extension.getRef().getContext());
      }
      //Make sure the two Jexl contexts are in the list
      contextSet.add("global/percussion/system/");
      contextSet.add("global/percussion/user/");

      final List<String> contexts = new ArrayList<String>(contextSet);
      Collections.sort(contexts, new PSIgnoreCaseStringComparator());
      return contexts;
   }
   
   /**
    * Handles the selection of a supported interface. Will turn
    * on context filtering if a Jexl expression interface is one
    * of the selected interfaces.
    * @param selections list of selected interfaces, cannot be <code>null</code>,
    * may be empty.
    * @param viewer the context combo viewer for the editor or wizard page,
    * cannot be <code>null</code>.
    * @param filter the jexl context filter, cannot be <code>null</code>.
    */
   public static void handleInterfaceSelection(List<String> selections,
      ComboViewer viewer, PSJexlExtensionContextFilter filter)
   {
      if(selections == null)
         throw new IllegalArgumentException("selections cannot be null.");
      if(viewer == null)
         throw new IllegalArgumentException("viewer cannot be null.");
      if(filter == null)
         throw new IllegalArgumentException("filter cannot be null.");
      for(String clazz : selections)
      {
         if(clazz.equals("com.percussion.extension.IPSJexlExpression"))
         {
            filter.setApplyFiltering(true);
            viewer.refresh();            
            if(viewer.getCombo().getSelectionIndex() == -1)
            {
               // Set default to user
               int len = viewer.getCombo().getItemCount();
               for(int i = 0; i < len; i++)
               {
                  if(viewer.getCombo().getItem(i).equals(
                     "global/percussion/user/"))
                  {
                     viewer.getCombo().select(i);
                     return;
                  }
               }
            }
            return;
         }
      }
      if(filter.isFilteringOn())
      {
         filter.setApplyFiltering(false);
         viewer.refresh();
      }
   }
   
   /**
    * Helper method to create the params table
    * @param parent
    * @param isJava flag indicating the table is in the Java ext. composite
    * @return the table, never <code>null</code>.
    */
   PSSortableTable createParamsTable(final Composite parent, final boolean isJava)
   {
      
      IPSNewRowObjectProvider newRowProvider = new IPSNewRowObjectProvider()
         {

            public Object newInstance()
            {
               String defaultType = isJava ? ms_java_types[0] : 
                  ms_javascript_types[0];
               return new String[]{"", defaultType, ""};
            }

            public boolean isEmpty(Object obj)
            {
               String[] row = (String[])obj;
               return (StringUtils.isBlank(row[0]) || 
                  StringUtils.isBlank(row[1]));
            }
         
         };
         
      ITableLabelProvider labelProvider = new PSAbstractTableLabelProvider()
         {

            public String getColumnText(Object element, int columnIndex)
            {
               if(columnIndex > 2)
                  return "";
               return ((String[])element)[columnIndex];
            }
         
         };
         
         
         
         final PSSortableTable table = new PSSortableTable(parent, labelProvider,
            newRowProvider, SWT.NONE, 
            PSSortableTable.DELETE_ALLOWED |
            PSSortableTable.INSERT_ALLOWED |
            PSSortableTable.SURPRESS_MANUAL_SORT);
         
         table.setCellModifier(new CellModifier(table));
         
         CellEditor cEditor = new TextCellEditor(table.getTable());
         table.addColumn("Name", PSSortableTable.NONE, 
            new ColumnWeightData(8, 80, true), cEditor, SWT.LEFT);
         
         String[] types = isJava ? ms_java_types : ms_javascript_types;
         cEditor = new PSComboBoxCellEditor(table.getTable(), types);
         table.addColumn("Type", PSSortableTable.NONE, 
            new ColumnWeightData(8, 80, true), cEditor, SWT.LEFT);
         
         cEditor = new TextCellEditor(table.getTable());
         table.addColumn("Description", PSSortableTable.NONE, 
            new ColumnWeightData(10, 100, true), cEditor, SWT.LEFT);
         
         return table;
      
   }
   
   /* 
    * @see com.percussion.workbench.ui.editors.form.PSEditorBase#
    * getHelpKey(org.eclipse.swt.widgets.Control)
    */
   @Override
   public String getHelpKey(Control control)
   {
      String postfix = m_isJavaExt ? "java" : "javascript";
      return super.getHelpKey(control) + "_" + postfix;
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
            "PSExtensionJavaComposite.classNames.label",
               "class_name",
            "PSExtensionJavaComposite.context.label",
               "context",
            "PSExtensionJavaComposite.params.label",
               "",
            "PSExtensionJavaComposite.requiredApps.label",
               "required_applications",
            "PSExtensionJavaComposite.requiredFiles.label",
               "required_files",
            "PSExtensionJavaComposite.supportedInterfaces.label",
               "",
            "PSExtensionJavascriptComposite.body.label",
               "body",
            "PSExtensionJavascriptComposite.context.label",
               "context",
            "PSExtensionJavascriptComposite.params.label",
               "",
            "PSExtensionJavascriptComposite.version.label",
               "version"
         });
      }
      return m_helpHintKeyHelper.getKey(
         super.getHelpHintKey(controlInfo), controlInfo);
   }
   
   /**
    * Cell modifier for the params table
    */ 
   class  CellModifier implements ICellModifier
   {            
      
      public CellModifier(PSSortableTable table)
      {
         mi_table = table;
      }
      
      /* 
       * @see org.eclipse.jface.viewers.ICellModifier#canModify(
       * java.lang.Object, java.lang.String)
       */
      public boolean canModify(
         @SuppressWarnings("unused") Object element,
         @SuppressWarnings("unused") String property)
      {         
         return true;
      }

      /* 
       * @see org.eclipse.jface.viewers.ICellModifier#getValue(
       * java.lang.Object, java.lang.String)
       */
      public Object getValue(Object element, String property)
      {
         int col = mi_table.getColumnIndex(property);
         String[] row = (String[])element;
         return StringUtils.defaultString(row[col]);
      }

      /* 
       * @see org.eclipse.jface.viewers.ICellModifier#modify(
       * java.lang.Object, java.lang.String, java.lang.Object)
       */
      public void modify(Object element, String property, Object value)
      {
         int col = mi_table.getColumnIndex(property);
         if(value == null)
            return;
         if(element instanceof Item)
            element = ((Item)element).getData();
         String[] row = (String[])element;
         row[col] = StringUtils.defaultString((String)value);
         mi_table.refreshTable();
         
      }      
      
      private PSSortableTable mi_table;
      
   }
      
   
   /**
    * Flag indicating the extension being edited uses a Java handler
    */
   private boolean m_isJavaExt;
   
   /**
    * Cache of the help hint helper object
    */
   private PSHelpHintKeyHelper m_helpHintKeyHelper;
   
   private IPSDesignerObjectUpdater m_comp;
   
   /**
    * Java types that can appear in extension params table
    */
   static final String[] ms_java_types = new String[]
      {
         "java.lang.String",
         "java.lang.Number",
         "java.lang.Integer",
         "java.lang.Object"         
      };
   
   /**
    * Javascript types that can appear in extension params table
    */
    static final String[] ms_javascript_types = new String[]
      {
         "String",
         "Boolean",
         "Number",
         "Date"
      };

   

   



   

}
