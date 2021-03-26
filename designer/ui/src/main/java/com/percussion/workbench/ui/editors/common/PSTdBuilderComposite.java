/******************************************************************************
 *
 * [ PSTdBuilderComposite.java ]
 * 
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.workbench.ui.editors.common;

import com.percussion.client.PSTdSchemaXmlRequester;
import com.percussion.client.catalogers.PSCatalogDatabaseTables;
import com.percussion.client.catalogers.PSCatalogDatasources;
import com.percussion.workbench.ui.IPSUiConstants;
import com.percussion.workbench.ui.PSMessages;
import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author erikserating
 *
 */
public class PSTdBuilderComposite extends Composite implements 
   IPSUiConstants, SelectionListener
{

   /**
    * @param parent
    */
   public PSTdBuilderComposite(Composite parent)
   {
      super(parent, SWT.NONE);
      setLayout(new FormLayout());
           
      /* JNDI Resource label and input field */
      Label resourceLabel = new Label(this, SWT.WRAP);
      resourceLabel.setAlignment(SWT.LEFT);
      resourceLabel.setText(
         PSMessages.getString("PSTdBuilderComposite.dbResource.label")); //$NON-NLS-1$
      FormData formData2 = new FormData();
      formData2.top = new FormAttachment(0, 
         (COMMON_BORDER_OFFSET * 2) + LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET);
      formData2.left = 
         new FormAttachment(0, LABEL_HSPACE_OFFSET);
      resourceLabel.setLayoutData(formData2);
      
      m_resourceCombo = new Combo(this, SWT.DROP_DOWN | SWT.READ_ONLY);
      FormData formData3 = new FormData();
      formData3.top = 
         new FormAttachment(resourceLabel, 
            -LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, SWT.TOP);
      formData3.left = 
         new FormAttachment(resourceLabel, 0, SWT.RIGHT);
      formData3.right = new FormAttachment(100, -LABEL_HSPACE_OFFSET);
      m_resourceCombo.setLayoutData(formData3);      
      
      
      /* Schema Selection Separator Label */
      Label separatorTextLabel = new Label(this, SWT.LEFT);
      separatorTextLabel.setText(
         PSMessages.getString("PSTdBuilderComposite.schemaSelectionSeparator.label")); //$NON-NLS-1$
      FormData formData12 = new FormData();
      formData12.top = new FormAttachment(m_resourceCombo, 30, SWT.BOTTOM);
      formData12.left = new FormAttachment(
               resourceLabel, 0, SWT.LEFT);
      separatorTextLabel.setLayoutData(formData12);
      
      Label separator = new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL);
      FormData formData13 = new FormData();
      formData13.top = new FormAttachment(separatorTextLabel, 8, SWT.TOP);
      formData13.left = 
         new FormAttachment(separatorTextLabel, LABEL_HSPACE_OFFSET, SWT.RIGHT);
      formData13.right = new FormAttachment(100, -LABEL_HSPACE_OFFSET);
      separator.setLayoutData(formData13);
      
      Label tablesLabel = new Label(this, SWT.WRAP);
      tablesLabel.setAlignment(SWT.LEFT);
      tablesLabel.setText(PSMessages.getString("PSTdBuilderComposite.tables.label")); //$NON-NLS-1$
      FormData formData14 = new FormData();
      formData14.top = new FormAttachment(separatorTextLabel, 30, SWT.BOTTOM);
      formData14.left = 
         new FormAttachment(resourceLabel, LABEL_HSPACE_OFFSET, SWT.LEFT);
      tablesLabel.setLayoutData(formData14);
      
      Composite tablePanel = createTablePanel();
      FormData tableFormData = new FormData();
      tableFormData.top = new FormAttachment(tablesLabel, 0, SWT.BOTTOM);
      tableFormData.right = new FormAttachment(80, 0);
      tableFormData.left = 
         new FormAttachment(tablesLabel, 0, SWT.LEFT);
      tablePanel.setLayoutData(tableFormData);
      
      
      Composite buttonPanel = createButtonPanel();
      FormData buttonFormData = new FormData();
      buttonFormData.top = new FormAttachment(tablePanel, 0, SWT.TOP);
      buttonFormData.left = 
         new FormAttachment(tablePanel, LABEL_HSPACE_OFFSET, SWT.RIGHT);
      buttonPanel.setLayoutData(buttonFormData);
      
      m_allowSchemaChangeCheckbox = new Button(this, SWT.CHECK);
      m_allowSchemaChangeCheckbox.setText(
         PSMessages.getString("PSTdBuilderComposite.allowChanges.checkbox.label")); //$NON-NLS-1$
      FormData formData15 = new FormData();
      formData15.top = new FormAttachment(tablePanel, 10, SWT.BOTTOM);
      formData15.left = 
         new FormAttachment(tablePanel, 0, SWT.LEFT);
      m_allowSchemaChangeCheckbox.setLayoutData(formData15);
      
      loadResourceCombo();
      addListeners();
   }
   
   private Composite createTablePanel()
   {
      Composite comp = new Composite(this, SWT.NONE);
      comp.setLayout(new FormLayout());
      
      m_table = new PSTdTablesTable(comp);
      FormData formData1 = new FormData();
      formData1.top = new FormAttachment(0, 0);
      formData1.left = new FormAttachment(0, 0);
      formData1.right = new FormAttachment(100, 0);
      formData1.height = 200;
      m_table.setLayoutData(formData1);      
      
      return comp;
   }
   
   private Composite createButtonPanel()
   {
      Composite comp = new Composite(this, SWT.NONE);
      comp.setLayout(new FormLayout());
      
      m_catalogButton = new Button(comp, SWT.PUSH);
      m_catalogButton.setText(
         PSMessages.getString("PSTdBuilderComposite.catalogButton.label")); //$NON-NLS-1$
      FormData formData1 = new FormData();
      formData1.top = new FormAttachment(0, 0);
      formData1.left = new FormAttachment(0, 0);
      formData1.width = BUTTON_WIDTH;
      formData1.height = BUTTON_HEIGHT;
      m_catalogButton.setLayoutData(formData1);
      
      m_selectAllButton = new Button(comp, SWT.PUSH);
      m_selectAllButton.setText(
         PSMessages.getString("PSTdBuilderComposite.selectAllButton.label")); //$NON-NLS-1$
      FormData formData2 = new FormData();
      formData2.top = new FormAttachment(
         m_catalogButton, BUTTON_VSPACE_OFFSET, SWT.BOTTOM);
      formData2.left = new FormAttachment(m_catalogButton, 0, SWT.LEFT);
      formData2.width = BUTTON_WIDTH;
      formData2.height = BUTTON_HEIGHT;
      m_selectAllButton.setLayoutData(formData2);
      
      
      m_clearAllButton = new Button(comp, SWT.PUSH);
      m_clearAllButton.setText(
         PSMessages.getString("PSTdBuilderComposite.clearAllButton.label")); //$NON-NLS-1$
      FormData formData3 = new FormData();
      formData3.top = new FormAttachment(
         m_selectAllButton, BUTTON_VSPACE_OFFSET, SWT.BOTTOM);
      formData3.left = new FormAttachment(m_selectAllButton, 0, SWT.LEFT);
      formData3.width = BUTTON_WIDTH;
      formData3.height = BUTTON_HEIGHT;
      m_clearAllButton.setLayoutData(formData3);
      
      return comp;
   }
   
   /**
    * Set the values of the selected tables, resource, and allows changes checkbox.
    * @param datasource cannot be <code>null</code> or empty.
    * @param tableNames cannot be <code>null</code>.
    * @param allowsChanges flag indicating that allow changes checkbox should
    * be selected.
    */
   public void setValues(String datasource, List<String> tableNames, boolean allowsChanges)
   {
      if(StringUtils.isBlank(datasource))
         throw new IllegalArgumentException("datasource cannot be null or empty."); //$NON-NLS-1$
      if(tableNames == null)
         throw new IllegalArgumentException("tablenames cannot be null."); //$NON-NLS-1$
      int idx = -1;
      for(String item : m_resourceCombo.getItems())
      {
         ++idx;
         if(datasource.equals(item))
         {
            m_resourceCombo.select(idx);
            doCatalog();
            m_table.selectTablesByName(tableNames);
            m_allowSchemaChangeCheckbox.setSelection(allowsChanges);
            break;
         }
      }
   }
   
   /**
    * Generates the table def schema xml based on the current control selections.
    * @return the schema or <code>null</code> if the schema was
    * not created.
    * @throws IOException
    * @throws SAXException
    */
   public String generateSchemaXml() throws IOException, SAXException
   {
      int idx = m_resourceCombo.getSelectionIndex();
      String datasource = null;
      if(idx != -1)
      {
         datasource = m_resourceCombo.getItem(idx);
      }
      else
      {
         return null;
      }
      List<String> selectedRows = new ArrayList<String>();
      List<PSTdTablesTable.TdTableRow> rows =  m_table.getRows();
      for(PSTdTablesTable.TdTableRow row : rows)
      {
         if(row.isSelected())
            selectedRows.add(row.getName());
      }
      if(selectedRows.isEmpty())
         return null;
      return PSTdSchemaXmlRequester.getSchema(
         datasource, selectedRows, m_allowSchemaChangeCheckbox.getSelection());
      
   }
   
   /* (non-Javadoc)
    * @see org.eclipse.swt.events.SelectionListener#widgetSelected(
    * org.eclipse.swt.events.SelectionEvent)
    */
   public void widgetSelected(SelectionEvent e)
   {
      Object source = e.getSource();      
      if(source == m_catalogButton)
      {
         doCatalog();
      }
      else if(source == m_selectAllButton)
      {
         m_table.selectAll();
      }
      else if(source == m_clearAllButton)
      {
         m_table.clearAll();  
      }      
   }

   /* (non-Javadoc)
    * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(
    * org.eclipse.swt.events.SelectionEvent)
    */
   public void widgetDefaultSelected(@SuppressWarnings("unused") //$NON-NLS-1$
   SelectionEvent e)
   {
      // no - op      
   }
   
   /**
    * Catalogs all tables for the datasource and sets the table rows.
    */
   private void doCatalog()
   {
      int idx = m_resourceCombo.getSelectionIndex();
      if(idx != -1)
      {
         String ds = m_resourceCombo.getItem(idx);
         List<String> tables = 
            PSCatalogDatabaseTables.getCatalog(ds, "TABLE", false);          //$NON-NLS-1$
         List<PSTdTablesTable.TdTableRow> rows = 
            new ArrayList<PSTdTablesTable.TdTableRow>();
         for(String table : tables)
         {
            rows.add(m_table.new TdTableRow(table, false));
         }
         m_table.setRows(rows);
      }
   }
   
   /**
    * Add button listeners.
    */
   private void addListeners()
   {
      m_catalogButton.addSelectionListener(this);
      m_selectAllButton.addSelectionListener(this);
      m_clearAllButton.addSelectionListener(this);  
   }
   
   /**
    * Loads the resource combo with a list of available
    * datasources.
    */
   private void loadResourceCombo()
   {      
      
      List<String> sources = 
         PSCatalogDatasources.getCatalog(null, false, true);
      String[] items = new String[sources.size()];
      int i = 0;
      for(String ds : sources)
      {
         items[i++] = ds;
      }
      m_resourceCombo.setItems(items);
      if(m_resourceCombo.getItemCount() > 0)
         m_resourceCombo.select(0);
   } 
   
   /**
    * Gets the combo control. Really only needed so that
    * the wizard page can get a hold of a control to register.
    * @return the combo, never <code>null</code>. 
    */
   public Combo getResourceCombo()
   {
      return m_resourceCombo;
   }   
   
   /**
    * The resource combo, initialized in the ctor, never <code>null</code> after that.
    */
   private Combo m_resourceCombo;
   
   /**
    * The tables table, initialized in {@link #createTablePanel()}
    * Never <code>null</code> after that.
    */
   private PSTdTablesTable m_table;
   
   /**
    * The catalog button, initialized in {@link #createButtonPanel()}
    * Never <code>null</code> after that.
    */
   private Button m_catalogButton;
   
   /**
    * The select all button, initialized in {@link #createButtonPanel()}
    * Never <code>null</code> after that.
    */
   private Button m_selectAllButton;
   
   /**
    * The clear all button, initialized in {@link #createButtonPanel()}
    * Never <code>null</code> after that.
    */
   private Button m_clearAllButton;
   
   /**
    * The allow changes checkbox button, initialized in {@link #createButtonPanel()}
    * Never <code>null</code> after that.
    */
   private Button m_allowSchemaChangeCheckbox;
 
   
   

}
