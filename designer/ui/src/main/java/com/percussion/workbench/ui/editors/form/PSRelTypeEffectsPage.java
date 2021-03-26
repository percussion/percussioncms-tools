/******************************************************************************
 *
 * [ PSRelTypeEffectsPage.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form;

import com.percussion.E2Designer.E2Designer;
import com.percussion.E2Designer.OSExitCallSet;
import com.percussion.E2Designer.OSExtensionCall;
import com.percussion.E2Designer.PSDlgUtil;
import com.percussion.E2Designer.PSRuleEditorDialog;
import com.percussion.E2Designer.browser.PSEffectsTableModel;
import com.percussion.E2Designer.browser.PSRelationshipEditorDialog;
import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSModelException;
import com.percussion.client.PSMultiOperationException;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.models.IPSCmsModel;
import com.percussion.design.objectstore.PSConditionalEffect;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.design.objectstore.PSRule;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.controls.IPSNewRowObjectProvider;
import com.percussion.workbench.ui.controls.PSAbstractTableLabelProvider;
import com.percussion.workbench.ui.controls.PSButtonCellEditor;
import com.percussion.workbench.ui.controls.PSButtonedComboBoxCellEditor;
import com.percussion.workbench.ui.controls.PSButtonedTextBoxCellEditor;
import com.percussion.workbench.ui.controls.PSSortableTable;
import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import static com.percussion.E2Designer.browser.PSCloningFieldOverridesTableModel.getExtensionDescription;
import static com.percussion.E2Designer.browser.PSEffectsTableModel.translateEndpointName;
import static com.percussion.workbench.ui.IPSUiConstants.BUTTON_WIDTH;
import static com.percussion.workbench.ui.IPSUiConstants.COMMON_BORDER_OFFSET;
import static com.percussion.workbench.ui.IPSUiConstants.TEXT_VSPACE_OFFSET;
import static com.percussion.workbench.ui.IPSUiConstants.THREE_FORTH;
import static com.percussion.workbench.ui.editors.form.PSRelTypeEffectsExecutionContextDialog.executionContextToLabel;

/**
 * Provides management of relationship type effects. 
 *
 * @author Andriy Palamarchuk
 */
public class PSRelTypeEffectsPage extends Composite
{
   /**
    * Creates new cloning page.
    */
   public PSRelTypeEffectsPage(Composite parent, int style, PSEditorBase editor)
   {
      super(parent, style);
      setLayout(new FormLayout());
      
      initEffects();
      m_endPoints = createEndPoints();
      
      m_editor = editor;
      final Label effectsTableLabel = createEffectsTableLabel(this);
      m_effectsTable = createEffectsTable(this, effectsTableLabel);
      createDescriptionUI(this, m_effectsTable);
      editor.registerControl(
            effectsTableLabel.getText(), m_effectsTable, null);
   }

   /**
    * Lists all the endpoints labels in alphabetical order.
    */
   private List<String> createEndPoints()
   {
      final List<String> endPoints = new ArrayList<String>();
      final Iterator<String> i = PSRelationshipConfig.getActivationEndPoints();
      while (i.hasNext())
      {
         endPoints.add(
               translateEndpointName(i.next(), true));
      }
      Collections.sort(endPoints);
      return endPoints;
   }

   /**
    * Initializes {@link #m_effects}.
    *
    */
   private void initEffects()
   {
      try
      {
         final IPSCmsModel model =
            PSCoreFactory.getInstance().getModel(PSObjectTypes.EXTENSION);
         final Collection<IPSReference> refs = model.catalog();
         final IPSReference[] refsArray =
               new ArrayList<IPSReference>(refs).toArray(new IPSReference[0]);
         final Object[] objects = model.load(refsArray, false, false);
         for (final Object o : objects)
         {
            final IPSExtensionDef exit = (IPSExtensionDef) o;
            if (exit.implementsInterface(OSExitCallSet.EXT_TYPE_RS_EFFECT))
            {
               m_effects.add(exit);
            }
         }
         Collections.sort(m_effects, new Comparator<IPSExtensionDef>()
               {
                  public int compare(IPSExtensionDef o1, IPSExtensionDef o2)
                  {
                     return o1.getRef().getExtensionName().toLowerCase()
                           .compareTo(
                           o2.getRef().getExtensionName().toLowerCase());
                  }
               });
      }
      catch (PSModelException e)
      {
         throw new RuntimeException(e);
      }
      catch (PSMultiOperationException e)
      {
         throw new RuntimeException(e);
      }
   }

   /**
    * Creates a label above the effects table. 
    */
   private Label createEffectsTableLabel(Composite container)
   {
      final Label label = new Label(container, SWT.NONE);
      label.setText(getMessage("label.table") + ':');                           //$NON-NLS-1$
      final FormData formData = new FormData();
      formData.left = new FormAttachment(0, 0);
      formData.right = new FormAttachment(100, 0);
      formData.top = new FormAttachment(0, 0);
      label.setLayoutData(formData);
      return label;
   }

   /**
    * Creates the label description UI.
    * @param previousControl usually the properties table. 
    */
   private void createDescriptionUI(
         Composite container, Control previousControl)
   {
      final Label propDescLabel =
            createPropDescLabel(container, previousControl);
      m_effectDescText = createDescriptionText(container, propDescLabel);
   }

   /**
    * Expression editor title label.
    */
   private Label createPropDescLabel(
         Composite container, Control previousControl)
   {
      final Label label = new Label(container, SWT.NONE);
      label.setText(DESC_LABEL + ':');

      final FormData formData = new FormData();
      formData.top =
            new FormAttachment(previousControl, TEXT_VSPACE_OFFSET, SWT.BOTTOM);
      formData.left = new FormAttachment(0, 0);
      label.setLayoutData(formData);
      return label;
   }
   
   /**
    * Creates effect description text control. 
    */
   private Text createDescriptionText(
         Composite container, Label previousControl)
   {
      final Text text = new Text(container,
            SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.READ_ONLY | SWT.V_SCROLL);

      final FormData formData = new FormData();
      formData.left = new FormAttachment(0, 0);
      formData.right = new FormAttachment(100, -EFFECTS_TABLE_BUTTON_SPACE);
      formData.top = new FormAttachment(previousControl, 0, SWT.BOTTOM);
      formData.bottom = new FormAttachment(100, -COMMON_BORDER_OFFSET);
      text.setLayoutData(formData);
      
      return text;
   }
   
   /**
    * Creates effects table.
    */
   private PSSortableTable createEffectsTable(Composite container,
         Control previous)
   {
      IPSNewRowObjectProvider rowObjectProvider = new IPSNewRowObjectProvider()
      {
         public Object newInstance()
         {
            final List<Object> empty = new ArrayList<Object>();
            empty.add(new ArrayList<Integer>());                                //$NON-NLS-1$
            empty.add(translateEndpointName(
                  PSRelationshipConfig.ACTIVATION_ENDPOINT_OWNER, true));
            empty.add(null);
            empty.add(new ArrayList<PSRule>());
            return empty; 
         }

         @SuppressWarnings("unchecked") //$NON-NLS-1$
         public boolean isEmpty(Object obj)
         {
            if (!(obj instanceof List))
            {
               throw new IllegalArgumentException("Unrecognized data: " + obj); //$NON-NLS-1$
            }
            final List<Object> rowData = (List<Object>) obj;
            return getEffect(rowData) == null;
         }
      };
      final ITableLabelProvider labelProvider =
            new PSAbstractTableLabelProvider()
      {
         @SuppressWarnings("unchecked") //$NON-NLS-1$
         public String getColumnText(Object element, int columnIndex)
         {
            final List<Object> rowData = (List<Object>) element;
            switch (columnIndex)
            {
               case COL_CONTEXT:
                  return generateExecutionContextStr(rowData);
               case COL_ENDPOINT:
                  return getEndPoint(rowData);
               case COL_EFFECT:
                  final OSExtensionCall effect = getEffect(rowData);
                  return effect == null ? "" : effect.toString();                     //$NON-NLS-1$
               case COL_COND:
                  return "C";                                                   //$NON-NLS-1$
               default:
                  throw new AssertionError();
            }
         }
      };
      final PSSortableTable table =
            new PSSortableTable(container, labelProvider, rowObjectProvider,
                  SWT.FULL_SELECTION | SWT.BORDER | SWT.MULTI,
                  PSSortableTable.DELETE_ALLOWED
                  | PSSortableTable.INSERT_ALLOWED
                  | PSSortableTable.SHOW_ALL
                  | PSSortableTable.SHOW_INSERT
                  | PSSortableTable.SHOW_MOVE_DOWN
                  | PSSortableTable.SHOW_MOVE_UP);

      specifyTableLayoutData(table, previous);
      specifyTableColumns(table);
      
      // Add listener to fill the description field upon row selection
      table.getTable().addSelectionListener(new SelectionAdapter()
         {
            @Override
            public void widgetSelected(
                  @SuppressWarnings("unused") SelectionEvent e)
            {
               onEffectSelected();
            }
         });
      table.setCellModifier(new CellModifier(table));
      return table;
   }

   /**
    * Creates string presentation for the effect execution context from the
    * provied data.
    */
   private String generateExecutionContextStr(final List<Object> rowData)
   {
      if (getExecutionContexts(rowData) == null)
      {
         return ""; //$NON-NLS-1$
      }
      final SortedSet<String> labels = new TreeSet<String>();
      for (final int id : getExecutionContexts(rowData))
      {
         labels.add(executionContextToLabel(id));
      }
      return StringUtils.join(labels.iterator(), ',');
   }

   /**
    * Called when selection is changed in effect table.
    */
   private void onEffectSelected()
   {
      final Table table = m_effectsTable.getTable();
      if (table.getSelectionCount() == 0)
      {
         m_effectDescText.setText(""); //$NON-NLS-1$                  
      }
      else
      {
         final List<Object> row = getSelectedEffectData();
         m_effectDescText.setText(getExtensionDescription(getEffect(row)));
      }
   }

   /**
    * Defines columns in the provided effects table.
    */
   private void specifyTableColumns(PSSortableTable table)
   {
      {
         final PSButtonedTextBoxCellEditor cellEditor =
               new PSButtonedTextBoxCellEditor(table.getTable(), SWT.READ_ONLY);
         cellEditor.getButton().addSelectionListener(new SelectionAdapter()
               {
                  @Override
                  public void widgetSelected(
                        @SuppressWarnings("unused") SelectionEvent e)
                  {
                     final List<Object> rowData = getSelectedEffectData();
                     final PSRelTypeEffectsExecutionContextDialog dlg =
                           new PSRelTypeEffectsExecutionContextDialog(
                                 getShell(), getExecutionContexts(rowData));
                     final int result = dlg.open();
                     if (result == Window.OK)
                     {
                        setExecutionContexts(rowData, dlg.getSelections());
                        refreshFieldOverrideTable();
                     }
                  }
               });
         table.addColumn(getMessage("column.executionContext"),                 //$NON-NLS-1$
               PSSortableTable.IS_SORTABLE,
               new ColumnWeightData(4), cellEditor, SWT.LEFT);
      }
      {
         final CellEditor cellEditor = new ComboBoxCellEditor(
               table.getTable(), m_endPoints.toArray(new String[0]),
               SWT.READ_ONLY);
         table.addColumn(getMessage("column.endPoint"),                         //$NON-NLS-1$
               PSSortableTable.IS_SORTABLE,
               new ColumnWeightData(4), cellEditor, SWT.LEFT);
      }
      {
         final PSButtonedComboBoxCellEditor cellEditor =
               new PSButtonedComboBoxCellEditor(
                     table.getTable(), m_effects.toArray(), SWT.READ_ONLY);
         cellEditor.setLabelProvider(new LabelProvider()
               {
                  @Override
                  public String getText(Object element)
                  {
                     final IPSExtensionDef udf = (IPSExtensionDef) element;
                     return udf == null ? "" : udf.getRef().getExtensionName(); //$NON-NLS-1$
                  }
               });
         cellEditor.getCombo().addSelectionListener(new SelectionAdapter()
               {
                  @Override
                  public void widgetSelected(SelectionEvent e)
                  {
                     final CCombo combo = (CCombo) e.getSource();
                     final IPSExtensionDef udfDef =
                           m_effects.get(combo.getSelectionIndex());
                     final List<Object> rowData = getSelectedEffectData();
                     final OSExtensionCall oldUdf = getEffect(rowData);

                     if (oldUdf != null
                           && udfDef.equals(oldUdf.getExtensionDef()))
                     {
                        return;
                     }
                     asyncCreateEffect(rowData, udfDef);
                     cellEditor.deactivate();
                  }
               });
         cellEditor.getButton().addSelectionListener(new SelectionAdapter()
               {
                  @Override
                  public void widgetSelected(
                        @SuppressWarnings("unused") SelectionEvent e)
                  {
                     final List<Object> rowData = getSelectedEffectData();
                     final OSExtensionCall effect = getEffect(rowData);
                     if (effect == null)
                     {
                        // has not been created yet
                        // user should select extension from the dropdown first
                        return;
                     }
                     asyncEditEffect(rowData, effect);
                     cellEditor.deactivate();
                  }
               });
         table.addColumn(getMessage("column.effect"), PSSortableTable.NONE,      //$NON-NLS-1$
               new ColumnWeightData(8, false), cellEditor, SWT.CENTER);
      }

      {
         final PSButtonCellEditor cellEditor =
            new PSButtonCellEditor(table.getTable(), SWT.NONE);
         cellEditor.getButton().addSelectionListener(new SelectionAdapter()
               {
                  @Override
                  public void widgetSelected(
                        @SuppressWarnings("unused") SelectionEvent e)
                  {
                     final List<PSRule> rules =
                           getConditions(getSelectedEffectData());
                     SwingUtilities.invokeLater(new Runnable()
                           {
                              public void run()
                              {
                                 editRules(rules, false);
                                 refreshFieldOverrideTable();
                              }
                           });
                  }
               });
         table.addColumn(getMessage("column.condition"), PSSortableTable.NONE,       //$NON-NLS-1$
               new ColumnWeightData(1, false), cellEditor, SWT.LEFT);
      }
   }

   /**
    * Edits list of rules.
    * Copied from legacy code in
    * {@link com.percussion.E2Designer.browser.PSRelationshipEditorDialog}.
    * @param editCloningConditionals should be <code>true</code> if is called
    * for editing cloning conditionals, such as "shallow" or "deep" cloning
    * options and <code>false</code> if called for field overrides.
    */
   @SuppressWarnings("unchecked") //$NON-NLS-1$
   public void editRules(List rules, boolean editCloningConditionals)
   {
      try
      {
         /*
          * Pull out the first rule if this is the cloning view
          * as this is the rule that determines if this property
          * is checked or not and should not be editable other then
          * by checking or unchecking.
          */
         PSRule firstConditional = null;

         if (editCloningConditionals && !rules.isEmpty())
         {
            firstConditional = (PSRule) rules.get(0);
            rules.remove(0);
         }
         
         PSRuleEditorDialog ruleDlg = new PSRuleEditorDialog((JFrame) null);
         E2Designer.getApp().getMainFrame().registerDialog(ruleDlg);
         ruleDlg.center();
         ruleDlg.onEdit(rules.iterator());
         ruleDlg.setVisible(true);
         rules.clear();
         // Put the removed conditional back to the top of
         // the list if necessary
         if (firstConditional != null)
         {
            rules.add(firstConditional);
         }
         Iterator it = ruleDlg.getRulesIterator();
         while(it.hasNext())
         {
            final PSRule rule = (PSRule) it.next();
            rules.add(rule);
         }

      }
      catch (ClassNotFoundException e)
      {
         PSDlgUtil.showError(e);
      }
   }

   /**
    * Have to edit asynchroniously because use existing Swing UI. 
    */
   private void asyncEditEffect(final List<Object> rowData,
         final OSExtensionCall effect)
   {
      SwingUtilities.invokeLater(new Runnable()
      {
         public void run()
         {
            final OSExtensionCall editedUdf =
               PSRelationshipEditorDialog.editExitCall(effect);
            if (editedUdf != null)
            {
               setEffect(rowData, editedUdf);
            }
            refreshFieldOverrideTable();
         }
      });
   }

   /**
    * Have to create asynchroniously because use existing Swing UI. 
    */
   private void asyncCreateEffect(final List<Object> rowData,
         final IPSExtensionDef effect)
   {
      SwingUtilities.invokeLater(new Runnable()
            {
               public void run()
               {
                  final OSExtensionCall editedEffect =
                     PSRelationshipEditorDialog.createExitCall(effect);
                  if (editedEffect != null)
                  {
                     setEffect(rowData, editedEffect);
                  }
                  refreshFieldOverrideTable();
               }
            });
   }
   
   /**
    * Refreshes {@link #m_effectsTable} asyncroniously after update.
    * Also notifies the editor about the update.
    * Can be called from processing in other threads than SWT thread.
    */
   private void refreshFieldOverrideTable()
   {
      PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable()
      {
         public void run()
         {
            m_effectsTable.refreshTable();
            m_editor.onControlModified(m_effectsTable, false);
         }
      });
   }

   /**
    * Sets table layout.
    */
   private void specifyTableLayoutData(
         final PSSortableTable table, Control previous)
   {
      final FormData formData = new FormData();
      formData.left = new FormAttachment(0, 0);
      formData.right =
            new FormAttachment(100, -PSSortableTable.TABLE_BUTTON_HSPACE);
      formData.top = new FormAttachment(previous, 0, 0);
      formData.bottom = new FormAttachment(THREE_FORTH, 0);
      table.setLayoutData(formData);
   }

   /**
    * Load controls with the relationship type data.
    */
   public void loadControlValues(final PSRelationshipConfig relType)
   {
      final List<List<Object>> effectsData = loadEffectsData(relType);
      m_effectsTable.setValues(effectsData);
      final boolean hasEffects = !effectsData.isEmpty();
      if (hasEffects)
      {
         m_effectsTable.getTable().select(0);
         onEffectSelected();
      }
      else
      {
         m_effectDescText.setText(""); //$NON-NLS-1$
      }
      m_effectsTable.refreshTable();
   }
   
   /**
    * Loads data. Borrowed from legacy class PSEffectsTableModel, plus added
    * one more column for execution context.
    * List contains following elements: execution context
    * (list of integers), end point (end point label), effect (OSExtensionCall),
    * condition - list of conditions.
    */
   private List<List<Object>> loadEffectsData(PSRelationshipConfig relType)
   {
      final List<List<Object>> data = new ArrayList<List<Object>>();
      final Iterator extensions = relType.getEffects();
      while (extensions.hasNext())
      {
         Object obj = extensions.next();
         List<Object> element = new ArrayList<Object>();
         data.add(element);

         if(!(obj instanceof PSConditionalEffect))
         {
            throw new IllegalArgumentException(
               "all elements in effects must be instances of" + //$NON-NLS-1$
               " PSConditionalEffect"); //$NON-NLS-1$
         }

         final PSConditionalEffect effect = (PSConditionalEffect) obj;
         
         element.add(effect.getExecutionContexts());
         
         {
            String endpoint = effect.getActivationEndPoint();
            if (StringUtils.isNotBlank(endpoint))
            {
               element.add(translateEndpointName(endpoint, true));
            }
            else
            {
               //default
               element.add(translateEndpointName(
                     PSRelationshipConfig.ACTIVATION_ENDPOINT_OWNER, true));
            }
         }

         element.add(new OSExtensionCall(effect.getEffect()));
         element.add(IteratorUtils.toList(effect.getConditions()));
         assert element.size() == COL_COND + 1;
      }
      return data;
   }

   /**
    * Updates relationship type with the controls selection.
    */
   final void updateRelType(final PSRelationshipConfig relType)
   {
      relType.setEffects(getDataForUpdate());
   }
   
   /**
    * Generates effects to update relationship type.
    * Borrowed from {@link PSEffectsTableModel#getData()}.
    * Had to borrow, not reuse because the original version accesses cells. 
    */
   @SuppressWarnings("unchecked") //$NON-NLS-1$
   private Iterator getDataForUpdate()
   {
      List<PSConditionalEffect> extensions = new ArrayList<PSConditionalEffect>();
      for (final Object rowDataObj : m_effectsTable.getValues())
      {
         final List<Object> rowData = (List<Object>) rowDataObj;
         final PSConditionalEffect condEff =
               new PSConditionalEffect(getEffect(rowData));
         condEff.setExecutionContexts(getExecutionContexts(rowData));
         final String endpoint = getEndPoint(rowData);
         if (StringUtils.isNotBlank(endpoint))
         {
            condEff.setActivationEndPoint(
                  translateEndpointName(endpoint, false));
         }
         condEff.setConditions(getConditions(rowData).iterator());
         extensions.add(condEff);
      }
      return extensions.iterator();
   }

   /**
    * Extracts the effect data.
    */
   private OSExtensionCall getEffect(List<Object> rowData)
   {
      return (OSExtensionCall) rowData.get(COL_EFFECT);
   }

   /**
    * Saves the effect data.
    */
   private void setEffect(List<Object> rowData, OSExtensionCall udf)
   {
      rowData.set(COL_EFFECT, udf);
   }

   /**
    * Retrieves endpoint label.
    */
   private String getEndPoint(List<Object> rowData)
   {
      return (String) rowData.get(COL_ENDPOINT);
   }
   
   /**
    * Saves end point label.
    */
   private void setEndPoint(List<Object> rowData, final String endPoint)
   {
      rowData.set(COL_ENDPOINT, endPoint);
   }

   /**
    * Extracts the execution contexts data.
    */
   @SuppressWarnings("unchecked") //$NON-NLS-1$
   private List<Integer> getExecutionContexts(List<Object> rowData)
   {
      return (List<Integer>) rowData.get(COL_CONTEXT);
   }
   
   /**
    * Saves the execution contexts data.
    */
   private void setExecutionContexts(List<Object> rowData,
         final List<Integer> contexts)
   {
      rowData.set(COL_CONTEXT, contexts);
   }
   
   /**
    * Effect field data corresponding to currently selected item in the table.
    * Table must have a selection.
    */
   @SuppressWarnings("unchecked") //$NON-NLS-1$
   private List<Object> getSelectedEffectData()
   {
      return (List<Object>) getSelectedEffectTableItem().getData();
   }

   /**
    * Currently selected effects table item.
    * Table must have a selection.
    */
   private TableItem getSelectedEffectTableItem()
   {
      final Table table = m_effectsTable.getTable();
      return table.getItem(table.getSelectionIndices()[0]);
   }

   /**
    * Convenience method to get message.
    */
   private static String getMessage(final String key)
   {
      return PSMessages.getString("PSRelTypeEffectsPage." + key);  //$NON-NLS-1$
   }
   
   /**
    * Conditions.
    */
   @SuppressWarnings("unchecked") //$NON-NLS-1$
   private List<PSRule> getConditions(List<Object> rowData)
   {
      return (List<PSRule>) rowData.get(COL_COND);
   }
   
   /**
    * Column index for given column name.
    */
   private int getColumnIndex(String propertyName)
   {
      final int idx = m_effectsTable.getColumnIndex(propertyName);
      if (idx == -1)
      {
         throw new IllegalArgumentException(
               "Unrecognized column name: " + propertyName);   //$NON-NLS-1$
      }
      return idx;
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
      public boolean canModify(@SuppressWarnings("unused") Object element,
            String propertyName)
      {
         switch (getColumnIndex(propertyName))
         {
            case COL_CONTEXT:
               return true;
            case COL_ENDPOINT:
               return true;
            case COL_EFFECT:
               return true;
            case COL_COND:
               return true;
            default:
               throw new IllegalArgumentException(
                     "Unrecognized column " + getColumnIndex(propertyName)); //$NON-NLS-1$
         }
      }

      // see base
      @SuppressWarnings("unchecked") //$NON-NLS-1$
      public Object getValue(Object element, String propertyName)
      {
         final int column = getColumnIndex(propertyName);
         final List<Object> rowData = (List<Object>) element;
         switch (column)
         {
            case COL_CONTEXT:
               return generateExecutionContextStr(rowData);
            case COL_ENDPOINT:
               return m_endPoints.indexOf(getEndPoint(rowData)); 
            case COL_EFFECT:
               final OSExtensionCall effect = getEffect(rowData);
               return effect == null ? null : effect.getExtensionDef();
            case COL_COND:
               return getConditions(rowData);
            default:
               throw new IllegalArgumentException(
                     "Unrecognized column " + column); //$NON-NLS-1$
         }
      }

      // see base
      @SuppressWarnings("unchecked") //$NON-NLS-1$
      public void modify(Object element, String propertyName, Object value)
      {
         final int column = getColumnIndex(propertyName);
         final TableItem item = (TableItem) element;
         final List<Object> rowData = (List<Object>) item.getData();
         switch (column)
         {
            case COL_CONTEXT:
               // handled in the button
               break;
            case COL_ENDPOINT:
               final int idx = (Integer) value;
               if (idx == -1)
               {
                  break;
               }
               final String endPoint = m_endPoints.get(idx);
               setEndPoint(rowData, endPoint);
               break;
            case COL_EFFECT:
               // handled in the combo listener because the editing dialog
               // should be triggered on selection and this is too late here
               if (value == null)
               {
                  setEffect(rowData, null);
                  break;
               }
               break;
            case COL_COND:
               // handled in the combo listener because Swing legacy UI is used
               // for editing and it must run asynchroniously.
               break;
            default:
               throw new AssertionError("Unrecognized column " + column); //$NON-NLS-1$
         }
         mi_tableComp.refreshTable();
      }

      private PSSortableTable mi_tableComp;
   }

   /**
    * Label text for effect description
    */
   public static final String DESC_LABEL = getMessage("label.description");     //$NON-NLS-1$

   /**
    * Space taken by table buttons.
    */
   public final static int EFFECTS_TABLE_BUTTON_SPACE =
         PSSortableTable.TABLE_BUTTON_HSPACE * 2 + BUTTON_WIDTH;


   /**
    * Execution context column index.
    */
   private static final int COL_CONTEXT = 0;

   /**
    * Endpoint column index.
    */
   private static final int COL_ENDPOINT = 1;
   
   /**
    * Effect extension column index.
    */
   private static final int COL_EFFECT = 2;

   
   private static final int COL_COND = 3;

   /**
    * Effects table
    */
   private PSSortableTable m_effectsTable;

   /**
    * Text field to view effect description.
    */
   private Text m_effectDescText;

   /**
    * Editor - owner of the page.
    */
   private final PSEditorBase m_editor;
   
   /**
    * The list of effects (<code>IPSEffect</code>) available to
    * associate with a relationship, initialized in the constructor and never
    * <code>null</code> or modified after that. May be empty.
    */
   private List<IPSExtensionDef> m_effects = new ArrayList<IPSExtensionDef>();

   /**
    * All the end points labels.
    */
   private final List<String> m_endPoints;
}
