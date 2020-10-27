/******************************************************************************
 *
 * [ PSRelTypeCloningPage.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form;

import com.percussion.E2Designer.E2Designer;
import com.percussion.E2Designer.PSDlgUtil;
import com.percussion.E2Designer.browser.BrowserTree;
import com.percussion.E2Designer.browser.PSCloningConfigModel;
import com.percussion.design.objectstore.PSCloneHandlerConfig;
import com.percussion.design.objectstore.PSObjectStore;
import com.percussion.design.objectstore.PSProcessCheck;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.design.objectstore.PSRule;
import com.percussion.workbench.ui.PSMessages;
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
import org.eclipse.ui.PlatformUI;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import static com.percussion.E2Designer.browser.PSRelationshipEditorDialog.cleanDisabledProcessChecks;
import static com.percussion.workbench.ui.IPSUiConstants.BUTTON_VSPACE_OFFSET;
import static com.percussion.workbench.ui.IPSUiConstants.HALF;
import static com.percussion.workbench.ui.IPSUiConstants.LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET;
import static com.percussion.workbench.ui.IPSUiConstants.LABEL_HSPACE_OFFSET;

/**
 * <p>Provides management of relationship type cloning configuration. 
 *
 * <p>Implementation notes: because existing handling of cloning option is quite
 * convoluted had to reuse as much legacy logic as possible. 
 *
 * @author Andriy Palamarchuk
 */
public class PSRelTypeCloningPage extends Composite
{
   /**
    * Creates new cloning page.
    */
   public PSRelTypeCloningPage(Composite parent, int style, PSEditorBase editor)
   {
      super(parent, style);
      setLayout(new FormLayout());
      
      m_editor = editor;
      final Label cloningLabel = createCloningLabel(this);
      m_shallowCheckbox = createShallowCloningCheckbox(this, cloningLabel);
      m_shallowConditionButton =
            createConditionButton(this, m_shallowCheckbox);
      m_deepCheckbox = createDeepCloningCheckbox(this, m_shallowConditionButton);
      m_deepConditionButton = createConditionButton(this, m_deepCheckbox);
      
      m_fieldOverrideHelper.initUI(this, m_deepConditionButton, editor);
      editor.registerControl(
            m_shallowCheckbox.getText(), m_shallowCheckbox, null);
      editor.registerControl(m_deepCheckbox.getText(), m_deepCheckbox, null);
   }

   /**
    * Condition button for shallow cloning 
    */
   private Button createConditionButton(final Composite container,
         final Button checkbox)
   {
      final Button button = new Button(container, SWT.NONE);
      button.setText(
            PSMessages.getString("PSRelTypeCloningPage.label.condition")); //$NON-NLS-1$
      final FormData formData = new FormData();
      formData.left = new FormAttachment(HALF);
      formData.top = new FormAttachment(checkbox, 0, SWT.TOP);
      button.setLayoutData(formData);
      button.addSelectionListener(new SelectionAdapter()
            {
               @SuppressWarnings("unchecked") //$NON-NLS-1$
               @Override
               public void widgetSelected(final SelectionEvent event)
               {
                  final Button btn = (Button) event.getSource();
                  final Vector<Object> rowData = (Vector<Object>) btn.getData();
                  final List<PSRule> rules = getProcessCheckRules(rowData);
                  SwingUtilities.invokeLater(new Runnable()
                        {
                           public void run()
                           {
                              m_fieldOverrideHelper.editRules(rules, true);
                              notifyUpdate();
                           }
                        });
               }
            });
      return button;
   }

   private void notifyUpdate()
   {
      PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable()
            {
               public void run()
               {
                  m_editor.onControlModified(m_shallowCheckbox, false);
               }
            });
   }

   /**
    * Deep cloning checkbox.
    */
   private Button createDeepCloningCheckbox(final Composite container,
         final Control topControl)
   {
      final Button button = new Button(container, SWT.CHECK);
      button.setText(
            PSMessages.getString("PSRelTypeCloningPage.label.deepCloning")); //$NON-NLS-1$
      
      final FormData formData = new FormData();
      formData.left = new FormAttachment(0, INDENT);
      formData.top =
            new FormAttachment(topControl, BUTTON_VSPACE_OFFSET, SWT.BOTTOM);
      button.setLayoutData(formData);
      addConditionSelectionListener(button);
      return button;
   }

   /**
    * Creates shallow cloning checkbox.
    */
   private Button createShallowCloningCheckbox(final Composite container,
         final Control previousControl)
   {
      final Button button = new Button(container, SWT.CHECK);
      button.setText(
            PSMessages.getString("PSRelTypeCloningPage.label.shallowCloning")); //$NON-NLS-1$
      
      final FormData formData = new FormData();
      formData.left = new FormAttachment(0, INDENT);
      formData.top = new FormAttachment(previousControl,
            LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, SWT.BOTTOM);
      button.setLayoutData(formData);
      addConditionSelectionListener(button);
      return button;
   }

   /**
    * Adds a listener to update whether given checkbox is selected or not.
    */
   private void addConditionSelectionListener(Button button)
   {
      button.addSelectionListener(new SelectionAdapter()
            {
               @SuppressWarnings("unchecked") //$NON-NLS-1$
               @Override
               public void widgetSelected(SelectionEvent event)
               {
                  final Button checkbox = (Button) event.getSource();
                  final Vector<Object> rowData =
                        (Vector<Object>) checkbox.getData();
                  rowData.set(PSCloningConfigModel.COL_CLONE_ENABLE,
                        checkbox.getSelection());
               }
            });
   }

   /**
    * Label for cloning options grou.
    */
   private Label createCloningLabel(final Composite pane)
   {
      final Label textLabel = new Label(pane, SWT.NONE);
      {
         final FormData formData = new FormData();
         formData.left = new FormAttachment(0, 0);
         formData.top = new FormAttachment(0, 0);
         textLabel.setLayoutData(formData);
      }
      textLabel.setText(
            PSMessages.getString("PSRelTypeCloningPage.label.cloning")); //$NON-NLS-1$
      
      final Label separatorLabel = new Label(pane, SWT.HORIZONTAL | SWT.SEPARATOR);
      {
         FormData formData = new FormData();
         formData.left =
            new FormAttachment(textLabel, LABEL_HSPACE_OFFSET, SWT.RIGHT);
         formData.top = new FormAttachment(textLabel, 0, SWT.CENTER);
         formData.right = new FormAttachment(100,
               -PSRelTypeFieldOverrideTableHelper.FIELD_TABLE_BUTTON_SPACE);
         separatorLabel.setLayoutData(formData);
      }
      return textLabel;
   }
   
   /**
    * Load controls with the relationship type data.
    */
   public void loadControlValues(final PSRelationshipConfig relType)
   {
      final PSObjectStore objectStore =
            E2Designer.getApp().getMainFrame().getObjectStore();
      m_checkData = loadCheckData(relType, objectStore);
      loadConditionData(m_checkData, PSRelationshipConfig.PROC_CHECK_CLONE_SHALLOW,
            m_shallowCheckbox, m_shallowConditionButton);
      loadConditionData(m_checkData, PSRelationshipConfig.PROC_CHECK_CLONE_DEEP,
            m_deepCheckbox, m_deepConditionButton);

      m_fieldOverrideHelper.loadControlValues(relType);
   }

   /**
    * Loads shallow/deep cloning-related data. 
    */
   private Vector<Vector<Object>> loadCheckData(
         final PSRelationshipConfig relType, final PSObjectStore objectStore)
   {
      final Vector<Vector<Object>> checkData;
      try
      {
         final PSCloneHandlerConfig cloneHandlerConfigSet =
               BrowserTree.getCloneHandlerConfigSet(objectStore, false, false);
         checkData = PSCloningConfigModel.buildCheckData(
               relType.getProcessChecks(), cloneHandlerConfigSet);
      }
      catch (Exception e)
      {
         PSDlgUtil.showError(e);
         throw new RuntimeException(e);
      }
      return checkData;
   }

   private void loadConditionData(final Vector<Vector<Object>> checkData,
         final String processCheckName, final Button checkbox,
         final Button conditionButton)
   {
      final Vector<Object> rowData = findProcessCheck(checkData, processCheckName);
      checkbox.setSelection(isProcessCheckEnabled(rowData));
      checkbox.setData(rowData);
      conditionButton.setData(rowData);
   }

   /**
    * Indicates whether the process check described by the provided data is
    * enabled.
    */
   private boolean isProcessCheckEnabled(Vector<Object> rowData)
   {
      return (Boolean) rowData.get(PSCloningConfigModel.COL_CLONE_ENABLE);
   }
   
   /**
    * Extracts conditions from row data.
    */
   @SuppressWarnings("unchecked") //$NON-NLS-1$
   private List<PSRule> getProcessCheckRules(Vector<Object> rowData)
   {
      return (List<PSRule>) rowData.get(PSCloningConfigModel.COL_CLONE_COND);
   }
   
   /**
    * Extracts the process check object.
    */
   private PSProcessCheck getProcessCheck(Vector<Object> rowData)
   {
      return (PSProcessCheck) rowData.get(PSCloningConfigModel.COL_CLONE_NAME);
   }

   /**
    * Finds process check with the specified name.
    * @param checkData data as returned by
    * {@link PSCloningConfigModel#buildCheckData(Iterator, PSCloneHandlerConfig)}.
    * @param processCheckName check name to find.
    * @throws IllegalArgumentException if check with the specified name is not
    * found. 
    */
   private Vector<Object> findProcessCheck(
         final Vector<Vector<Object>> checkData, final String processCheckName)
   {
      for (final Vector<Object> row : checkData)
      {
         final PSProcessCheck check = (PSProcessCheck) row.get(
               PSCloningConfigModel.COL_CLONE_NAME);
         if (check.getName().equals(processCheckName))
         {
            return row;
         }
      }
      throw new IllegalArgumentException(
            "Could not find process check with name " + processCheckName); //$NON-NLS-1$
   }

   /**
    * Updates relationship type with the controls selection.
    */
   final void updateRelType(final PSRelationshipConfig relType)
   {
      relType.getSysProperty(PSRelationshipConfig.RS_ALLOWCLONING).setValue(
            m_deepCheckbox.getSelection() || m_shallowCheckbox.getSelection());
      relType.setProcessChecks(
            cleanDisabledProcessChecks(getDataForUpdate()));
      m_fieldOverrideHelper.updateRelType(relType);
   }

   /**
    * Borrowed from {@link PSCloningConfigModel#getData()} 
    * Had to borrow, not reuse because the original version accesses cells. 
    */
   private Iterator getDataForUpdate()
   {
      List<PSProcessCheck> processChecks = new ArrayList<PSProcessCheck>();
      for (int i = 0; i < m_checkData.size(); i++)
      {
         final Vector<Object> row = m_checkData.get(i);
         final PSProcessCheck check = getProcessCheck(row); 

         if (check != null)
         {
            processChecks.add(check);
            PSCloningConfigModel.updateProcessCheck(check,
                  isProcessCheckEnabled(row),
                  getProcessCheckRules(row));
         }
      }
      return processChecks.iterator();
   }


   /**
    * Offset for intended controls.
    */
   private static final int INDENT = LABEL_HSPACE_OFFSET + 16;

   /**
    * Shallow cloning checkbox.
    * Stores a row of data corresponding to the corresponding process check
    * as returned by
    * {@link PSCloningConfigModel#buildCheckData(Iterator, PSCloneHandlerConfig)}
    * in "data" property.
    */
   private Button m_shallowCheckbox;
   
   /**
    * Shallow cloning condition.
    * Stores a row of data corresponding to the corresponding process check
    * as returned by
    * {@link PSCloningConfigModel#buildCheckData(Iterator, PSCloneHandlerConfig)}
    * in "data" property.
    */
   private Button m_shallowConditionButton;

   /**
    * Deep cloning checkbox.
    * Stores a row of data corresponding to the corresponding process check
    * as returned by
    * {@link PSCloningConfigModel#buildCheckData(Iterator, PSCloneHandlerConfig)}
    * in "data" property.
    */
   private Button m_deepCheckbox;
   
   /**
    * Shallow cloning condition.
    * Stores a row of data corresponding to the corresponding process check
    * as returned by
    * {@link PSCloningConfigModel#buildCheckData(Iterator, PSCloneHandlerConfig)}
    * in "data" property.
    */
   private Button m_deepConditionButton;

   /**
    * The cloned fields override UI.
    */
   private final PSRelTypeFieldOverrideTableHelper m_fieldOverrideHelper =
         new PSRelTypeFieldOverrideTableHelper();
   
   /**
    * Deep/shallow cloning-related data.
    */
   Vector<Vector<Object>> m_checkData;
   
   /**
    * Editor - owner of the page.
    */
   private final PSEditorBase m_editor;
}
