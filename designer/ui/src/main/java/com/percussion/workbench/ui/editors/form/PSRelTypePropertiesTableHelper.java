/******************************************************************************
 *
 * [ PSRelTypePropertiesTableHelper.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form;

import com.percussion.design.objectstore.PSProperty;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.PSWorkbenchPlugin;
import com.percussion.workbench.ui.controls.IPSNewRowObjectProvider;
import com.percussion.workbench.ui.controls.PSAbstractTableLabelProvider;
import com.percussion.workbench.ui.controls.PSSortableTable;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.percussion.workbench.ui.IPSUiConstants.BUTTON_WIDTH;
import static com.percussion.workbench.ui.IPSUiConstants.ONE_FIFTH;
import static com.percussion.workbench.ui.IPSUiConstants.ONE_THIRD;
import static com.percussion.workbench.ui.IPSUiConstants.TEXT_VSPACE_OFFSET;
import static com.percussion.workbench.ui.IPSUiConstants.THREE_FORTH;

/**
 * Helps to manage properties UI for relationship types.
 *
 * @author Andriy Palamarchuk
 */
public class PSRelTypePropertiesTableHelper
{
   /**
    * Creates new helper
    * @param system if <code>true</code> the helper displays and manages
    * system properties, if <code>false</code> - user propreties.
    */
   public PSRelTypePropertiesTableHelper(boolean system, boolean isSystemRelType)
   {
      m_system = system;
      m_isSystemRelType = isSystemRelType;
   }
   
   /**
    * Initializes the property UI.
    */
   public void initUI(Composite container, Control previous)
   {
      m_propertiesTable = createPropertiesTable(container, previous);
      createDescriptionUI(container, m_propertiesTable);
   }
   
   /**
    * Creates the label description UI.
    * @param previousControl usually the properties table. 
    */
   private void createDescriptionUI(Composite container, Control previousControl)
   {
      final Label propDescLabel =
            createPropDescLabel(container, previousControl);
      m_propDescText = createExpressionText(container, propDescLabel);
   }
   
   /**
    * Expression editor title label.
    */
   private Label createPropDescLabel(Composite container, Control previousControl)
   {
      final Label label = new Label(container, SWT.NONE);
      label.setText(DESC_LABEL + ':');

      final FormData formData = new FormData();
      formData.top = new FormAttachment(previousControl, TEXT_VSPACE_OFFSET, SWT.BOTTOM);
      formData.left = new FormAttachment(0, 0);
      label.setLayoutData(formData);
      return label;
   }
   
   /**
    * Creates expression editor text control. 
    */
   private Text createExpressionText(Composite container, Label previousControl)
   {
      int style = SWT.BORDER | SWT.MULTI | SWT.WRAP;
      if (m_system)
      {
         style |= SWT.READ_ONLY;
      }
      final Text text = new Text(container, style)
            {
               @Override
               public void addModifyListener(ModifyListener listener)
               {
                  if (m_interceptDescListener)
                  {
                     if (!m_descModifyListeners.contains(listener))
                     {
                        m_descModifyListeners.add(listener);
                     }
                  }
                  super.addModifyListener(listener);
               }

               @Override
               public void removeModifyListener(ModifyListener listener)
               {
                  if (m_interceptDescListener)
                  {
                     m_descModifyListeners.remove(listener);
                  }
                  super.removeModifyListener(listener);
               }

               /**
                * Disables the check. We subclass the widget only to intercept
                * modify listeners.
                * @see org.eclipse.swt.widgets.Widget#checkSubclass()
                */
               @Override
               protected void checkSubclass() {}
            };
      final FormData formData = new FormData();
      formData.left = new FormAttachment(0, 0);
      formData.right = m_system
            ? new FormAttachment(100) 
            : new FormAttachment(100,
                  -(PSSortableTable.TABLE_BUTTON_HSPACE * 2) - BUTTON_WIDTH);
      formData.top = new FormAttachment(previousControl, 0, SWT.BOTTOM);
      formData.bottom = m_system
            ? new FormAttachment(100 - ONE_THIRD, 0)
            : new FormAttachment(100); 
      text.setLayoutData(formData);
      
      // add a modify listener to update description
      text.addModifyListener(new ModifyListener()
         {
            public void modifyText(@SuppressWarnings("unused") ModifyEvent e)
            {               
               if (m_propertiesTable.getTable().getSelectionCount() == 0)
               {
                  return;
               }
               getSelectedProperty().setDescription(m_propDescText.getText());
            }         
         });

      return text;
   }
   
   /**
    * Initializes properties table.
    */
   private PSSortableTable createPropertiesTable(Composite container, Control previous)
   {
      IPSNewRowObjectProvider rowObjectProvider = new IPSNewRowObjectProvider()
      {
         public Object newInstance()
         {
            if (m_system)
            {
               throw new UnsupportedOperationException();
            }
            return new PSProperty(BLANK_NAME_VAL);
         }

         public boolean isEmpty(Object o)
         {
            if (!(o instanceof PSProperty))
            {
               throw new IllegalArgumentException(
                     "The passed in object must be an instance of PSProperty."); //$NON-NLS-1$
            }
            final PSProperty property = (PSProperty) o;
            return property.getName().equals(BLANK_NAME_VAL);
         }
      };
      final ITableLabelProvider labelProvider = new PSAbstractTableLabelProvider()
      {
         @Override
         public Image getColumnImage(Object element, int columnIndex)
         {
            if (columnIndex == VALUE_COL)
            {
               final PSProperty property = (PSProperty) element;
               if (property.getType() == PSProperty.TYPE_BOOLEAN)
               {
                  final boolean isSelected = property.getValue() == null
                        ? false
                        : (Boolean) property.getValue();
                  return getImage(isSelected);
               }
               
            }
            return null;
         }

         public String getColumnText(Object element, int columnIndex)
         {
            final PSProperty property = (PSProperty) element;
            final Object value = property.getValue();
            switch (columnIndex)
            {
               case DUMMY_COL:
                  return "";                                //$NON-NLS-1$
               case NAME_COL:
                  return getPropertyName(property);
               case VALUE_COL:
                  if (property.getType() == PSProperty.TYPE_BOOLEAN)
                  {
                     return "";                            //$NON-NLS-1$
                  }
                  return value == null ? "" : value.toString();  //$NON-NLS-1$
                  
               default:
                  throw new AssertionError();
            }
         }
      };
      final PSSortableTable table =
            new PSSortableTable(container, labelProvider, rowObjectProvider,
                  SWT.FULL_SELECTION | SWT.BORDER | SWT.MULTI,
                  getTableOptions())
                  {
                     @Override
                     public void setValues(List values)
                     {
                        super.setValues(values);
                        // Andriy: a hack to fix tab focus jumping through table
                        // Rx-06-06-0320
                        if (m_cursor != null && !values.isEmpty())
                        {
                           m_cursor.setSelection(0, 1);
                           m_cursor.setVisible(false);
                        }
                     }
                  };
      configureTableLayoutData(table, previous);
      specifyPropertiesTableColumns(table);
      
      // Add listener to fill the description field upon row selection
      table.addSelectionListener(new SelectionAdapter()
         {
            @Override
            @SuppressWarnings("unused")
            public void widgetSelected(SelectionEvent e)
            {
               onPropertySelected();
            }
         });
      table.setCellModifier(new CellModifier(table));
      return table;
   }

   /**
    * Sets table layout.
    */
   private void configureTableLayoutData(final PSSortableTable table, Control previous)
   {
      final FormData formData = new FormData();
      formData.left = new FormAttachment(0, 0);
      formData.right = m_system
            ? new FormAttachment(THREE_FORTH) 
            : new FormAttachment(100, -PSSortableTable.TABLE_BUTTON_HSPACE);
      formData.top = new FormAttachment(previous, 0, 0);
      formData.bottom = m_system
            ? new FormAttachment(ONE_FIFTH * 2)
            : new FormAttachment(THREE_FORTH);
      table.setLayoutData(formData);
   }
   
   /**
    * Called when property selection is changed in property table.
    */
   private void onPropertySelected()
   {
      final Table table = m_propertiesTable.getTable();
      enableDescText(table.getSelectionCount() != 0);
      if (table.getSelectionCount() == 0)
      {
         setDescription(""); //$NON-NLS-1$                  
      }
      else
      {
         final PSProperty property = getSelectedProperty();
         final String text = StringUtils.defaultString(property.getDescription());
         setDescription(text);
      }
   }

   /**
    * Changes content of {@link #m_propDescText} without firing modification
    * change.
    */
   private void setDescription(final String text)
   {
      // modification notification is disabled by temporary removing
      // modification listeners
      assert m_interceptDescListener;
      m_interceptDescListener = false;
      try
      {
         removeDescModifyListeners();
         m_propDescText.setText(text);
      }
      finally
      {
         restoreDescModifyListeners();
         m_interceptDescListener = true;
      }
   }

   /**
    * Restores stored modification listeners.  
    */
   private void restoreDescModifyListeners()
   {
      assert !m_interceptDescListener;
      for (final ModifyListener listener : m_descModifyListeners)
      {
         m_propDescText.addModifyListener(listener);
      }
   }

   /**
    * Removes stored modification listeners.  
    */
   private void removeDescModifyListeners()
   {
      assert !m_interceptDescListener;

      for (final ModifyListener listener : m_descModifyListeners)
      {
         m_propDescText.removeModifyListener(listener);
      }
   }

   /**
    * Enables property description text field.
    * Makes sure the text field is always disabled in system configuration.
    */
   private void enableDescText(final boolean condition)
   {
      if (!m_system)
      {
         m_propDescText.setEnabled(condition);
      }
   }

   /**
    * Defines columns in the provided properties table.
    */
   private void specifyPropertiesTableColumns(PSSortableTable table)
   {
      {
         table.addColumn("", PSSortableTable.NONE,                      //$NON-NLS-1$
               new ColumnPixelData(0, false), null, SWT.LEFT);
         final TableColumn column = table.getTable().getColumn(DUMMY_COL);
         column.setResizable(false);
         column.setMoveable(false);
         column.setWidth(0);
      }
      {
         final CellEditor cellEditor = m_system ? null
               : new TextCellEditor(table.getTable(), SWT.NONE);
         final int sortable =
            m_system ? PSSortableTable.NONE : PSSortableTable.IS_SORTABLE;
         table.addColumn(getMessage("column.name"), sortable,          //$NON-NLS-1$
               new ColumnWeightData(8), cellEditor, SWT.LEFT);
      }
      {
         // real cell editor will be set for each cell separately
         // depending on property type
         final CellEditor placeholderCellEditor =
            new TextCellEditor(table.getTable(), SWT.NONE);
         table.addColumn(getMessage("column.value"), PSSortableTable.NONE,      //$NON-NLS-1$
               new ColumnWeightData(2, false),
               placeholderCellEditor, SWT.CENTER);
      }
   }
   
   /**
    * Load controls with the relationship type values.
    */
   public void loadControlValues(final PSRelationshipConfig relType)
   {
      m_properties = new ArrayList<PSProperty>();
      final Iterator propertyIterator = m_system
            ? getSystemProperties(relType)
            : relType.getUserDefProperties();
      while (propertyIterator.hasNext())
      {
         final PSProperty property = (PSProperty) propertyIterator.next();
         m_properties.add((PSProperty) property.clone());
      }      

      m_propertiesTable.setValues(m_properties);
      m_propertiesTable.sortColumn(0, true);
      final boolean hasProperties = !m_properties.isEmpty();
      enableDescText(hasProperties);
      if (hasProperties)
      {
         m_propertiesTable.getTable().select(0);
         onPropertySelected();
      }
      else
      {
         setDescription(""); //$NON-NLS-1$
      }
      m_propertiesTable.refreshTable();
   }

   /**
    * Updates relationship type with the controls selection.
    */
   public void updateRelType(PSRelationshipConfig relType)
   {
      if (m_system)
      {
         int idx = 0;
         for (Iterator i = getSystemProperties(relType); i.hasNext();)
         {
            final PSProperty property = (PSProperty) i.next();
            property.setValue(m_properties.get(idx).getValue());
            idx++;
         }
      }
      else
      {
         relType.setUserDefProperties(m_propertiesTable.getValues().iterator());
      }
   }

   /**
    * Relationship type system properties.
    */
   private Iterator getSystemProperties(PSRelationshipConfig relType)
   {
      return relType.getSysPropertiesFiltered();
   }

   /**
    * Property currently selected in the table.
    * Table must have a selection.
    */
   private PSProperty getSelectedProperty()
   {
      return (PSProperty) getSelectedPropertiesTableItem().getData();
   }

   /**
    * Currently selected properties table item.
    * Table must have a selection.
    */
   private TableItem getSelectedPropertiesTableItem()
   {
      final Table table = m_propertiesTable.getTable();
      return table.getItem(table.getSelectionIndices()[0]);
   }
   
   /**
    * The properties table control.
    */
   public PSSortableTable getPropertiesTable()
   {
      return m_propertiesTable;
   }
   
   /**
    * The property description control; 
    */
   public Text getPropertyDescriptionText()
   {
      return m_propDescText;
   }

   /**
    * Sets value column cell editor depending on property type.
    */
   private void resetValueColumnCellEditor(final PSProperty property)
   {
      getPropTableCellEditors()[VALUE_COL] =
            getTypeCellEditor(property.getType());
   }

   /**
    * Creates a cell editor corresponding to property type.
    */
   private CellEditor getTypeCellEditor(final int type)
   {
      final Table table = m_propertiesTable.getTable();
      switch (type)
      {
         case PSProperty.TYPE_STRING:
            return new TextCellEditor(table, SWT.NONE);
         case PSProperty.TYPE_BOOLEAN:
            return new CheckboxCellEditor(table, SWT.NONE);
         default:
            throw new IllegalArgumentException(
                  "Unrecognized property type " + type);          //$NON-NLS-1$
      }
   }

   /**
    * Creates property names to readable names map. 
    */
   private static Map<String, String> createPropertyLabels()
   {
      final Map<String, String> labels = new HashMap<String, String>();
      labels.put("rs_useownerrevision",                                    //$NON-NLS-1$
            getMessage("propertyLabel.rs_useownerrevision"));              //$NON-NLS-1$
      labels.put("rs_usedependentrevision",                                //$NON-NLS-1$
            getMessage("propertyLabel.rs_usedependentrevision"));          //$NON-NLS-1$
      labels.put("rs_useserverid",                                         //$NON-NLS-1$
            getMessage("propertyLabel.rs_useserverid"));                   //$NON-NLS-1$
      labels.put("rs_islocaldependency",                                   //$NON-NLS-1$
            getMessage("propertyLabel.rs_islocaldependency"));             //$NON-NLS-1$
      labels.put("rs_skippromotion",                                       //$NON-NLS-1$
            getMessage("propertyLabel.rs_skippromotion"));                 //$NON-NLS-1$
      return Collections.unmodifiableMap(labels);
   }
  

   /**
    * Returns the image with the given key, or <code>null</code> if not found.
    */
   private Image getImage(boolean isSelected) {
      String key = isSelected ? CHECKED_IMAGE : UNCHECKED_IMAGE;
      return  m_imageRegistry.get(key);
   }
   
   /**
    * Column index for given column name.
    */
   private int getColumnIndex(String propertyName)
   {
      final int idx = m_propertiesTable.getColumnIndex(propertyName);
      if (idx == -1)
      {
         throw new IllegalArgumentException(
               "Unrecognized column name: " + propertyName);   //$NON-NLS-1$
      }
      return idx;
   }

   /**
    * The {@link #m_propertiesTable} cell editors.
    */
   private CellEditor[] getPropTableCellEditors()
   {
      return m_propertiesTable.getTableViewer().getCellEditors();
   }

   /**
    * Options passed to {@link PSSortableTable} during creation.
    */
   int getTableOptions()
   {
      return m_system ? PSSortableTable.SURPRESS_MANUAL_SORT
            : PSSortableTable.SURPRESS_MANUAL_SORT
                  | PSSortableTable.DELETE_ALLOWED
                  | PSSortableTable.INSERT_ALLOWED
                  | PSSortableTable.SHOW_DELETE;
   }

   /**
    * Property name. Correctly handles {@link #BLANK_NAME_VAL}. 
    */
   String getPropertyName(final PSProperty property)
   {
      if (m_system)
      {
         if (SYS_PROPERTY_LABELS.containsKey(property.getName()))
         {
            return SYS_PROPERTY_LABELS.get(property.getName());
         }
      }
      return property.getName().equals(BLANK_NAME_VAL) ? ""             //$NON-NLS-1$
            : property.getName();
   }

   /**
    * Sets property name. Correctly handles {@link #BLANK_NAME_VAL}.
    * @param name new property name value.
    */
   void setPropertyName(final PSProperty property, final String name)
   {
      property.setName(StringUtils.isBlank(name) ? BLANK_NAME_VAL : name);
   }

   /**
    * Convenience method to get message.
    */
   private static String getMessage(final String key)
   {
      return PSMessages.getString("PSRelTypePropertiesTableHelper." + key);     //$NON-NLS-1$
   }

   /**
    * Cell modifier for the properties table
    */
   private class CellModifier implements ICellModifier
   {
      CellModifier(PSSortableTable tableComp)
      {
         mi_tableComp = tableComp;
      }

      /**
       * Returns <code>true</code> only for value field.
       * @see org.eclipse.jface.viewers.ICellModifier#canModify(java.lang.Object, java.lang.String)
       */
      public boolean canModify(Object element, String propertyName)
      {
         final int column = getColumnIndex(propertyName);
         final PSProperty property = (PSProperty) element;
         switch (column)
         {
            case DUMMY_COL:
               return false;
            case NAME_COL:
               return !m_system;
            case VALUE_COL:
               resetValueColumnCellEditor(property);
               return !(m_system && m_isSystemRelType);
            default:
               throw new IllegalArgumentException(
                     "Unrecognized column " + column); //$NON-NLS-1$
         }
      }

      // see base
      public Object getValue(Object element, String propertyName)
      {
         final int column = getColumnIndex(propertyName);
         final PSProperty property = (PSProperty) element;
         switch (column)
         {
            case DUMMY_COL:
               return "";                             //$NON-NLS-1$
            case NAME_COL:
               return getPropertyName(property);
            case VALUE_COL:
               return getPropertyValue(property);
            default:
               throw new IllegalArgumentException(
                     "Unrecognized column " + column); //$NON-NLS-1$
         }
      }

      /**
       * Property value for editor.
       */
      private Object getPropertyValue(final PSProperty property)
      {
         if (property.getValue() == null)
         {
            switch (property.getType())
            {
               case PSProperty.TYPE_BOOLEAN:
                  return Boolean.FALSE;
               case PSProperty.TYPE_STRING:
                  return "";                             //$NON-NLS-1$
               case PSProperty.TYPE_FILE:
                  return "";                             //$NON-NLS-1$
               default:
                  // continue
            }
         }
         return property.getValue();
      }

      // see base
      public void modify(Object element, String propertyName, Object value)
      {
         final int column = getColumnIndex(propertyName);
         final TableItem item = (TableItem) element;
         final PSProperty property = (PSProperty) item.getData();
         switch (column)
         {
            case DUMMY_COL:
               break;
            case NAME_COL:
               if (m_system)
               {
                  throw new IllegalArgumentException(
                        "Column " + propertyName + " can't be modified!");      //$NON-NLS-1$ //$NON-NLS-2$
               }             
               setPropertyName(property, (String) value);               
               break;
            case VALUE_COL:
               property.setValue(value);
               break;
            default:
               throw new AssertionError("Unrecognized column " + column); //$NON-NLS-1$
         }
         mi_tableComp.refreshTable();
      }
      
      private PSSortableTable mi_tableComp;
   }

   /**
    * Image for checked checkbox.
    */
   private static final String CHECKED_IMAGE  = "icons/checked.gif";            //$NON-NLS-1$

   /**
    * Image for unchecked checkbox.
    */
   private static final String UNCHECKED_IMAGE  = "icons/unchecked.gif";        //$NON-NLS-1$

   /**
    * For the checkbox images.
    */ 
   private static ImageRegistry m_imageRegistry = new ImageRegistry();

   /**
    * Note: An image registry owns all of the image objects registered with it,
    * and automatically disposes of them the SWT Display is disposed.
    */ 
   static {
      try
      {
         m_imageRegistry.put(CHECKED_IMAGE,
               PSWorkbenchPlugin.getImageDescriptor(CHECKED_IMAGE));
         m_imageRegistry.put(UNCHECKED_IMAGE,
               PSWorkbenchPlugin.getImageDescriptor(UNCHECKED_IMAGE));
      }
      catch (Exception ignore)
      {
         // can happen e.g. during unit tests
      }
   }
   
   /**
    * Human-readable labels for some properties.
    */
   private static final Map<String, String> SYS_PROPERTY_LABELS =
         createPropertyLabels(); 

   /**
    * A dummy column to work around Eclipse intendation bug (43910).
    */
   private final static int DUMMY_COL = 0;

   /**
    * Index of the name column.
    */
   private final static int NAME_COL = 1;
   
   /**
    * Value column index
    */
   private final static int VALUE_COL = 2;
   
   /**
    * Label text for property description
    */
   public static final String DESC_LABEL = getMessage("label.description");     //$NON-NLS-1$
   
   /**
    * Reserved value for blank name indicator.
    * Is used as a workaround restriction that properties can't have blank names.
    */
   static final String BLANK_NAME_VAL =
         "com.percussion.workbench.ui.editors.form.PSRelTypePropertiesTableHelper.blankPropertyName";//$NON-NLS-1$
   
   /**
    * Editor copy of the properties.
    */
   List<PSProperty> m_properties;

   /**
    * Table to manipulate properties.
    */
   PSSortableTable m_propertiesTable;
   
   /**
    * Text field to view property description.
    */
   Text m_propDescText;
   
   /**
    * Whether the helper manages system or user properties UI.
    */
   private final boolean m_system;
   
   /**
    * Whether to intercept description modify listener operation.
    * When <code>false</code> the add and remove operations work as in
    * standard text control.
    */
   private boolean m_interceptDescListener = true;
   
   /**
    * Listeners disabled during programmatic text field update.
    */
   private List<ModifyListener> m_descModifyListeners =
         new ArrayList<ModifyListener>();
   
   /**
    * Flag indicating the this is a system rel type. Initialized
    * in ctor.
    */
   private boolean m_isSystemRelType;
}
