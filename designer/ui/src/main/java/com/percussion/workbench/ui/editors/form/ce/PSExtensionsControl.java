/******************************************************************************
 *
 * [ PSExtensionsControl.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form.ce;

import com.percussion.E2Designer.OSExitCallSet;
import com.percussion.E2Designer.OSExtensionCall;
import com.percussion.E2Designer.PSRuleEditorDialog;
import com.percussion.client.PSCatalogUtils;
import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.design.objectstore.IPSReplacementValue;
import com.percussion.design.objectstore.PSApplyWhen;
import com.percussion.design.objectstore.PSConditionalExit;
import com.percussion.design.objectstore.PSContentEditor;
import com.percussion.design.objectstore.PSContentEditorPipe;
import com.percussion.design.objectstore.PSExtensionCall;
import com.percussion.design.objectstore.PSExtensionCallSet;
import com.percussion.design.objectstore.PSExtensionParamValue;
import com.percussion.design.objectstore.PSInputTranslations;
import com.percussion.design.objectstore.PSOutputTranslations;
import com.percussion.design.objectstore.PSTextLiteral;
import com.percussion.design.objectstore.PSValidationRules;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSItemInputTransformer;
import com.percussion.extension.IPSItemOutputTransformer;
import com.percussion.extension.IPSItemValidator;
import com.percussion.extension.IPSRequestPreProcessor;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSExtensionDef;
import com.percussion.extension.PSExtensionRef;
import com.percussion.swtdesigner.SWTResourceManager;
import com.percussion.util.PSCollection;
import com.percussion.utils.collections.PSIteratorUtils;
import com.percussion.utils.types.PSPair;
import com.percussion.workbench.ui.IPSUiConstants;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.PSWorkbenchPlugin;
import com.percussion.workbench.ui.controls.IPSNewRowObjectProvider;
import com.percussion.workbench.ui.controls.PSAbstractTableLabelProvider;
import com.percussion.workbench.ui.controls.PSButtonCellEditor;
import com.percussion.workbench.ui.controls.PSButtonedComboBoxCellEditor;
import com.percussion.workbench.ui.controls.PSSortableTable;
import com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater;
import com.percussion.workbench.ui.editors.dialog.PSExtensionParamsDialog;
import com.percussion.workbench.ui.editors.form.PSEditorBase;
import com.percussion.workbench.ui.legacy.AwtSwtModalDialogBridge;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A common class for handling extensions for input translations, out put
 * translations, validations, pre exits and post exits
 */
public class PSExtensionsControl extends Composite
      implements
         IPSUiConstants,
         IPSDesignerObjectUpdater,
         SelectionListener
{
   /**
    * Construuctor, creates the composite with all required comtrols in it.
    * @param parent parent composite assumed not <code>null</code>.
    * @param style SWT style options for this composite.
    * @param editor The design object PSEditorBase object
    * @param type The type of the extensions this composite needs to handle.
    */
   public PSExtensionsControl(Composite parent, int style, PSEditorBase editor,
         int type) {
      super(parent, style);
      if (!(type == INPUT_TRANSFORMS || type == OUTPUT_TRANSFORMS
            || type == VALIDATIONS || type == PRE_EXITS || type == POST_EXITS))
         throw new IllegalArgumentException(
               "type must be either input transforms or output transforms or validations"); //$NON-NLS-1$
      m_type = type;
      m_editor = editor;
      setLayout(new FormLayout());

      // Create the label provider for this table
      ITableLabelProvider labelProvider = new PSAbstractTableLabelProvider()
      {

         @SuppressWarnings("synthetic-access") //$NON-NLS-1$
         public String getColumnText(Object element, int columnIndex)
         {
            DataContainer data = (DataContainer) element;
            switch (columnIndex)
            {
               case 0 :
                  if (!data.isEmpty())
                     return StringUtils.defaultString(data.toString());
                  break;
               case 1 :
                  if (!data.isEmpty())
                  {
                     return "C"; //$NON-NLS-1$
                  }
            }
            return ""; // should never get here //$NON-NLS-1$
         }

      };

      // Create the new row object provider for this table
      IPSNewRowObjectProvider newRowProvider = new IPSNewRowObjectProvider()
      {

         public Object newInstance()
         {
            return new DataContainer();
         }

         public boolean isEmpty(Object obj)
         {
            if (!(obj instanceof DataContainer))
               throw new IllegalArgumentException(
                     "The passed in object must be an instance of DataContainer."); //$NON-NLS-1$
            DataContainer data = (DataContainer) obj;
            return data.isEmpty();

         }

      };
      m_extsTable = new PSSortableTable(this, labelProvider, newRowProvider,
            SWT.NONE, PSSortableTable.SHOW_ALL
                  | PSSortableTable.INSERT_ALLOWED
                  | PSSortableTable.DELETE_ALLOWED);
      m_extsTable.setCellModifier(new CellModifier(m_extsTable));
      final FormData formData_14 = new FormData();
      formData_14.left = new FormAttachment(0, 0);
      formData_14.top = new FormAttachment(0, LABEL_VSPACE_OFFSET);
      formData_14.right = new FormAttachment(75, 0);
      formData_14.height = 2 * DESCRIPTION_FIELD_HEIGHT;
      m_extsTable.setLayoutData(formData_14);
      m_extsTable.getTable().addSelectionListener(this);
      m_extCellEditor = new PSButtonedComboBoxCellEditor(
            m_extsTable.getTable(), new String[0], SWT.READ_ONLY);
      final CCombo ctrlCombo = m_extCellEditor.getCombo();
      ctrlCombo.addSelectionListener(this);
      m_extCellEditor.getButton().addSelectionListener(this);

      m_extsTable.addColumn(PSMessages.getString("PSExtensionsControl.label.tablecolumn.extension"), PSSortableTable.NONE, //$NON-NLS-1$
            new ColumnWeightData(20, 100), m_extCellEditor, SWT.LEFT);

      m_btnCellEditor = new PSButtonCellEditor(m_extsTable.getTable(), SWT.NONE);
      m_btnCellEditor.getButton().addSelectionListener(this);
      if (type == INPUT_TRANSFORMS || type == OUTPUT_TRANSFORMS
            || type == VALIDATIONS)
      {
         m_extsTable.addColumn(PSMessages.getString("PSExtensionsControl.label.tablecolumn.conditional"), 0, new ColumnWeightData(1, 10), //$NON-NLS-1$
               m_btnCellEditor, SWT.LEFT);
      }

      final Label description = new Label(this, SWT.NONE);
      description.setText(PSMessages.getString("PSExtensionsControl.label.description")); //$NON-NLS-1$
      final FormData formData_0 = new FormData();
      formData_0.top = new FormAttachment(m_extsTable, LABEL_VSPACE_OFFSET,
            SWT.BOTTOM);
      formData_0.left = new FormAttachment(m_extsTable, 0, SWT.LEFT);
      description.setLayoutData(formData_0);

      m_description = new Text(this, SWT.V_SCROLL | SWT.BORDER | SWT.WRAP
            | SWT.READ_ONLY);
      final FormData formData_1 = new FormData();
      formData_1.right = new FormAttachment(m_extsTable, -85, SWT.RIGHT);
      formData_1.top = new FormAttachment(description, LABEL_VSPACE_OFFSET,
            SWT.BOTTOM);
      formData_1.left = new FormAttachment(m_extsTable, 0, SWT.LEFT);
      formData_1.height = DESCRIPTION_FIELD_HEIGHT;
      m_description.setLayoutData(formData_1);

      m_maxErrorsLabel = new Label(this, SWT.WRAP | SWT.LEFT);
      m_maxErrorsLabel.setText(PSMessages.getString("PSExtensionsControl.label.maxerrors")); //$NON-NLS-1$
      final FormData formData_2 = new FormData();
      formData_2.top = new FormAttachment(m_description,
            LABEL_HSPACE_OFFSET, SWT.TOP);
      formData_2.left = new FormAttachment(m_description, LABEL_HSPACE_OFFSET,
            SWT.RIGHT);
      m_maxErrorsLabel.setLayoutData(formData_2);

      m_maxErrorsText = new Text(this, SWT.BORDER);
      final FormData formData_3 = new FormData();
      formData_3.top = new FormAttachment(m_maxErrorsLabel,
            -LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, SWT.TOP);
      formData_3.left = new FormAttachment(m_maxErrorsLabel,
            2 * LABEL_HSPACE_OFFSET, SWT.RIGHT);
      formData_3.width = 20;
      m_maxErrorsText.setLayoutData(formData_3);
      m_maxErrorsText.addModifyListener(new ModifyListener()
      {

         @SuppressWarnings("synthetic-access") //$NON-NLS-1$
         public void modifyText(@SuppressWarnings("unused") ModifyEvent e)
         {
            try
            {
               m_maxErrorsToStop = Integer.parseInt(m_maxErrorsText.getText());
            }
            catch (NumberFormatException ex)
            {
               MessageBox mbox = new MessageBox(getShell());
               mbox.setText(PSMessages.getString("PSExtensionsControl.error.maxerrors.title")); //$NON-NLS-1$
               mbox.setMessage(PSMessages.getString("PSExtensionsControl.error.maxerrors.message")); //$NON-NLS-1$
               mbox.open();
               m_maxErrorsText.setText(Integer.toString(m_maxErrorsToStop));
            }

         }
      });
      String name = ""; //$NON-NLS-1$
      if(type == INPUT_TRANSFORMS)
         name=PSTransformValidationComposite.INPUT_TRANSFORMS_LABEL;
      else if(type == OUTPUT_TRANSFORMS)
         name = PSTransformValidationComposite.OUTPUT_TRANSFORMS_LABEL;
      else if(type == VALIDATIONS)
         name = PSTransformValidationComposite.VALIDATIONS_LABEL;
      else if(type == PRE_EXITS)
         name = PSTransformValidationComposite.PRE_EXITS_LABEL;
      else if(type == POST_EXITS)
         name = PSTransformValidationComposite.POST_EXITS_LABEL;
      
      editor.registerControl(name,m_extsTable,null);
      editor.registerControl(m_maxErrorsLabel.getText(),m_maxErrorsText,null);
   }

   /**
    * Cell modifier for the choice list table
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
      @SuppressWarnings("unused") 
      public boolean canModify(Object element, String property)
      {
         return true;
      }

      /*
       * @see org.eclipse.jface.viewers.ICellModifier#getValue(
       *      java.lang.Object, java.lang.String)
       */
      @SuppressWarnings("synthetic-access")//$NON-NLS-1$
      public Object getValue(Object element, String property)
      {
         int col = mi_tableComp.getColumnIndex(property);
         DataContainer data = (DataContainer) element;
         switch (col)
         {
            case 0 :
               if (!data.isEmpty())
                  return StringUtils.defaultString(data.getExtensionDef()
                        .toString());
            case 1 :
               return PSMessages.getString("PSExtensionsControl.label.conditonalcell"); //$NON-NLS-1$
         }
         return ""; //$NON-NLS-1$
      }

      /*
       * @see org.eclipse.jface.viewers.ICellModifier#modify( java.lang.Object,
       *      java.lang.String, java.lang.Object)
       */
      @SuppressWarnings("unused")//$NON-NLS-1$
      public void modify(Object element, String property, Object value)
      {
         // We are doing everything as part of extension combo selection only.
         return;
      }

      private PSSortableTable mi_tableComp;

   }

   /*
    *  (non-Javadoc)
    * @see org.eclipse.swt.widgets.Widget#dispose()
    */
   @Override
   public void dispose()
   {
      super.dispose();
   }

   /*
    *  (non-Javadoc)
    * @see org.eclipse.swt.widgets.Widget#checkSubclass()
    */
   @Override
   protected void checkSubclass()
   {
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater#updateDesignerObject(java.lang.Object, java.lang.Object)
    */
   public void updateDesignerObject(Object designObject, Object control)
   {
      PSItemDefinition itemDef = (PSItemDefinition) designObject;
      PSContentEditor editor = itemDef.getContentEditor();
      if (control == m_extsTable)
      {
         if (m_type == INPUT_TRANSFORMS)
         {
            PSInputTranslations inputTranslations = new PSInputTranslations();
            inputTranslations.addAll(getConditionalExits());
            editor.setInputTranslation(inputTranslations);
         }
         else if (m_type == OUTPUT_TRANSFORMS)
         {
            PSOutputTranslations outputTranslations = new PSOutputTranslations();
            outputTranslations.addAll(getConditionalExits());
            editor.setOutputTranslation(outputTranslations);
         }
         else if (m_type == VALIDATIONS)
         {
            PSValidationRules validationRules = new PSValidationRules();
            validationRules.addAll(getConditionalExits());
            validationRules.setMaxErrorsToStop(Integer.parseInt(m_maxErrorsText
                  .getText()));
            editor.setValidationRules(validationRules);
         }
         else if (m_type == PRE_EXITS)
         {
            PSExtensionCallSet preExits = new PSExtensionCallSet();
            Iterator iter = getConditionalExits().iterator();
            while(iter.hasNext())
            {
               PSConditionalExit exit = (PSConditionalExit) iter.next();
               PSExtensionCallSet cset = exit.getRules();
               PSExtensionCall call = (PSExtensionCall) cset.get(0);
               preExits.add(call);
            }
            ((PSContentEditorPipe) editor.getPipe())
                  .setContentEditorInputDataExtensions(preExits);
         }
         else if (m_type == POST_EXITS)
         {
            PSExtensionCallSet postExits = new PSExtensionCallSet();
            Iterator iter = getConditionalExits().iterator();
            while(iter.hasNext())
            {
               PSConditionalExit exit = (PSConditionalExit) iter.next();
               PSExtensionCallSet cset = exit.getRules();
               PSExtensionCall call = (PSExtensionCall) cset.get(0);
               postExits.add(call);
            }
            editor.getPipe().setResultDataExtensions(postExits);
         }
      }
      if (control == m_maxErrorsText)
      {
         if (m_type == VALIDATIONS)
         {
            PSValidationRules validationRules = new PSValidationRules();
            validationRules.addAll(PSIteratorUtils.cloneList(editor
                  .getValidationRules()));
            validationRules.setMaxErrorsToStop(Integer.parseInt(m_maxErrorsText
                  .getText()));
            editor.setValidationRules(validationRules);
         }
      }
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater#loadControlValues(java.lang.Object)
    */
   public void loadControlValues(Object designObject)
   {
      PSItemDefinition itemDef = (PSItemDefinition) designObject;
      PSContentEditor editor = itemDef.getContentEditor();

      PSCollection exits = null;
      List avexits = null;
      Set<String> ifnames = new HashSet<String>();
      Set<String> legacyIfaceNames = new HashSet<String>();
      boolean showAllExtns = PSWorkbenchPlugin.getDefault().getPreferences()
            .isShowLegacyInterfacesForExtns();
      if (m_type == INPUT_TRANSFORMS)
      {
         PSInputTranslations inputTranslations = new PSInputTranslations();
         inputTranslations.addAll(PSIteratorUtils.cloneList(editor
               .getInputTranslations()));
         exits = inputTranslations;
         ifnames.add(IPSItemInputTransformer.class.getName());
         legacyIfaceNames.add(IPSRequestPreProcessor.class.getName());
         avexits = getExtensions(ifnames, legacyIfaceNames, showAllExtns, exits);
         m_maxErrorsLabel.setVisible(false);
         m_maxErrorsText.setVisible(false);
      }
      else if (m_type == OUTPUT_TRANSFORMS)
      {
         PSOutputTranslations outputTranslations = new PSOutputTranslations();
         outputTranslations.addAll(PSIteratorUtils.cloneList(editor
               .getOutputTranslations()));
         exits = outputTranslations;
         ifnames.add(IPSItemOutputTransformer.class.getName());
         legacyIfaceNames.add(IPSResultDocumentProcessor.class.getName());
         avexits = getExtensions(ifnames, legacyIfaceNames, showAllExtns, exits);
         m_maxErrorsLabel.setVisible(false);
         m_maxErrorsText.setVisible(false);
      }
      else if (m_type == VALIDATIONS)
      {
         PSValidationRules validationRules = new PSValidationRules();
         validationRules.addAll(PSIteratorUtils.cloneList(editor
               .getValidationRules()));
         validationRules.setMaxErrorsToStop(editor
               .getMaxErrorsToStopValidation());
         exits = validationRules;
         ifnames.add(IPSItemValidator.class.getName());
         legacyIfaceNames.add(IPSResultDocumentProcessor.class.getName());
         avexits = getExtensions(ifnames, legacyIfaceNames, showAllExtns, exits);
         m_maxErrorsToStop = validationRules.getMaxErrorsToStop();
         m_maxErrorsLabel.setVisible(true);
         m_maxErrorsText.setVisible(true);
         m_maxErrorsText.setText(Integer.toString(m_maxErrorsToStop));
      }
      else if (m_type == PRE_EXITS)
      {
         PSCollection preExits = new PSCollection(PSConditionalExit.class);
         PSExtensionCallSet orgSet = ((PSContentEditorPipe) editor.getPipe())
               .getContentEditorInputDataExtensions();
         if (orgSet != null)
         {
            Iterator iter = orgSet.iterator();
            while (iter.hasNext())
            {
               PSExtensionCall ecall = (PSExtensionCall) iter.next();
               PSExtensionCallSet eset = new PSExtensionCallSet();
               eset.add(ecall);
               preExits.add(new PSConditionalExit(eset));
            }
         }
         exits = preExits;
         avexits = getExtensions(IPSRequestPreProcessor.class.getName(), exits);
         m_maxErrorsLabel.setVisible(false);
         m_maxErrorsText.setVisible(false);
      }
      else if (m_type == POST_EXITS)
      {
         PSCollection postExits = new PSCollection(PSConditionalExit.class);
         PSExtensionCallSet orgSet = editor.getPipe().getResultDataExtensions();
         if (orgSet != null)
         {
            Iterator iter = orgSet.iterator();
            while (iter.hasNext())
            {
               PSExtensionCall ecall = (PSExtensionCall) iter.next();
               PSExtensionCallSet eset = new PSExtensionCallSet();
               eset.add(ecall);
               postExits.add(new PSConditionalExit(eset));
            }
         }
         exits = postExits;
         avexits = getExtensions(IPSResultDocumentProcessor.class.getName(),
               exits);
         m_maxErrorsLabel.setVisible(false);
         m_maxErrorsText.setVisible(false);
      }
      initExitData(avexits, exits);
      TableItem[] items = m_extsTable.getTable().getItems();
      for(TableItem item : items)
         setConditionalCellFont(item);
   }

   /**
    * Initialize the composite with the supplied data.
    * 
    * @param exitDefinitions all extension definitions allowed for this panel,
    *           may be <code>null</code>.
    * @param conditionalExits all conditional exits with which to initialize the
    *           panel table, may be <code>null</code>.
    */
   @SuppressWarnings("unchecked")
   protected void initExitData(Collection exitDefinitions,
         Collection conditionalExits)
   {
      m_exitsList = new ArrayList();
      m_exitNames = new ArrayList();
      Map<String, IPSExtensionDef> callDeffMap = 
            new HashMap<String, IPSExtensionDef>();
      if (exitDefinitions != null)
      {
         Iterator exitWalker = exitDefinitions.iterator();
         while (exitWalker.hasNext())
         {
            IPSExtensionDef extensionDef = (IPSExtensionDef) exitWalker.next();
            m_exitsList.add(new DataContainer(extensionDef, null, null));
            m_exitNames.add(extensionDef.toString());
            callDeffMap.put(extensionDef.toString(), extensionDef);
         }
      }
      m_extCellEditor.setItems(m_exitNames.toArray(new String[m_exitNames
            .size()]));
      m_exitData = new ArrayList();
      if (conditionalExits != null)
      {
         Iterator entries = conditionalExits.iterator();
         while (entries.hasNext())
         {
            PSConditionalExit entry = (PSConditionalExit) entries.next();
            PSExtensionCall pscall = (PSExtensionCall) entry.getRules().get(0);
            OSExtensionCall call = new OSExtensionCall(pscall, callDeffMap
                  .get(pscall.getExtensionRef().getExtensionName()));
            OSExitCallSet callSet = new OSExitCallSet();
            callSet.add(call, (String) call.getExtensionDef().getInterfaces()
                  .next());

            DataContainer container = new DataContainer(call.getExtensionDef(),
                  callSet, entry.getCondition());
            m_exitData.add(container);
         }
      }
      m_extsTable.setValues(m_exitData);
   }

   /**
    * A container to store all required data for the extensions column.
    */
   protected class DataContainer
   {
      public DataContainer()
      {}
      
      /**
       * Constructor to initialize all data elements.
       * 
       * @param extensionDef the extension definition to initialize with, not
       *           <code>null</code>.
       * @param extensionCalls the extension calls to initialized with, may be
       *           <code>null</code>.
       * @param conditions the list of conditions to initialized with, may be
       *           <code>null</code>.
       */
      public DataContainer(IPSExtensionDef extensionDef,
            OSExitCallSet extensionCalls, PSApplyWhen conditions) {
         setExtensionDef(extensionDef);
         setExtensionCalls(extensionCalls);
         setConditionals(conditions);
      }

      public boolean isEmpty()
      {
         return getExtensionDef() == null ? true : false;
      }

      /**
       * Get the extension definition.
       * 
       * @return the extension definition, may be <code>null</code>.
       */
      public IPSExtensionDef getExtensionDef()
      {
         return mi_extensionDef;
      }

      /**
       * Set a new extension definition.
       * 
       * @param extensionDef the new extension definition, not <code>null</code>.
       */
      public void setExtensionDef(IPSExtensionDef extensionDef)
      {
         if (extensionDef == null)
            throw new IllegalArgumentException("extensionDef cannot be null"); //$NON-NLS-1$

         mi_extensionDef = extensionDef;
      }

      /**
       * Get the extension calls.
       * 
       * @return the extension calls, may be <code>null</code>.
       */
      public OSExitCallSet getExtensionCalls()
      {
         return mi_extensionCalls;
      }

      /**
       * Set new extension calls.
       * 
       * @param extensionCalls the new extension calls, may be <code>null</code>.
       */
      public void setExtensionCalls(OSExitCallSet extensionCalls)
      {
         mi_extensionCalls = extensionCalls;
      }

      /**
       * Get the extension calls.
       * 
       * @return the extension calls, may be <code>null</code>.
       */
      public PSApplyWhen getConditionals()
      {
         return mi_conditionals;
      }

      /**
       * Set new extension calls.
       * 
       * @param conditionals The new set, may be <code>null</code>.
       */
      public void setConditionals(PSApplyWhen conditionals)
      {
         mi_conditionals = conditionals;
      }

      /**
       * Overridden to show the extension name until one has been selected and
       * the extension name with runtime parameters otherwise.
       * 
       * @see java.lang.Object#toString()
       */
      @Override
      public String toString()
      {
         if (getExtensionCalls() != null)
         {
            OSExtensionCall call = (OSExtensionCall) getExtensionCalls().get(0);
            if (call != null)
               return call.toString();
         }

         return getExtensionDef().toString();
      }

      /**
       * Storage for the extension definition. Initialized to <code>null</code>,
       * may be changed through {@link #setExtensionDef(IPSExtensionDef)}.
       */
      private IPSExtensionDef mi_extensionDef = null;

      /**
       * Storage for all extension calls. Initialized to <code>null</code>,
       * may be changed through {@link #setExtensionCalls(OSExitCallSet)}.
       */
      private OSExitCallSet mi_extensionCalls = null;

      /**
       * Storage for all conditionals. Initialized to <code>null</code>, may
       * be changed through {@link #setConditionals(PSApplyWhen)}.
       */
      private PSApplyWhen mi_conditionals = null;

   }

   /**
    * Gets all the extensions that implement the supplied interface.  Deprecated
    * extensions will not be included unless they are being processed.
    * 
    * @param ifname The interface, assumed not <code>null</code>.
    * @param exits The exits that are being processed, assumed not
    * <code>null</code>.
    * 
    * @return List of PSExtensionDef objects, may be empty but never
    *         <code>null</code>
    */
   @SuppressWarnings("unchecked")
   private static List<PSExtensionDef> getExtensions(String ifname,
      PSCollection exits)
   {
      List<PSExtensionDef> catExts = new ArrayList<PSExtensionDef>();
      List<PSExtensionDef> exts = new ArrayList<PSExtensionDef>();
      try
      {
         Set<PSExtensionRef> exitRefs = new HashSet<PSExtensionRef>();
         Iterator exitIter = exits.iterator();
         while (exitIter.hasNext())
         {
            PSConditionalExit exit = (PSConditionalExit) exitIter.next();
            PSExtensionCallSet cs = exit.getRules();
            Iterator callIter = cs.iterator();
            while (callIter.hasNext())
            {
               PSExtensionCall call = (PSExtensionCall) callIter.next();
               exitRefs.add(call.getExtensionRef());
            }
         }
         
         catExts.addAll(PSCatalogUtils.catalogExtensions(ifname));
         for (PSExtensionDef ext : catExts)
         {
            if (!ext.isDeprecated() || exitRefs.contains(ext.getRef()))
            {
               exts.add(ext);
            }
         }
      }
      catch (Exception e)
      {
         System.out.println(e.getMessage());
         e.printStackTrace();
         PSWorkbenchPlugin.handleException("Cataloging extensions.",
               PSMessages.getString(
                     "PSExtensionsControl.error.catalogextensions"), e //$NON-NLS-1$ //$NON-NLS-2$
               .getLocalizedMessage(), e);
      }
      return exts;
   }
   
   /**
    * Gets all the extensions that implement the supplied interfaces.
    * 
    * @param baseIfaceNames The set of interface names, assumed not
    * <code>null</code> or empty.
    * @param legacyIfaceNames Additional interface names, the use of which
    * depends on the last 2 params. May be <code>null</code>.
    * @param showLegacy If <code>true</code>, then the legacy interfaces will
    * be added to the set of base interface names for cataloging.
    * @param exits The exits that are being processed. If any of these exits do
    * not have a corresponding entry in the cataloged defs, then the catalog
    * will be redone with the legacy interfaces added in.
    * @return List of PSExtensionDef objects, may be empty but never
    * <code>null</code>
    */
   @SuppressWarnings("unchecked")
   private static List<PSExtensionDef> getExtensions(
         Set<String> baseIfaceNames, Set<String> legacyIfaceNames,
         boolean showLegacy, PSCollection exits)
   {
      Set<String> ifaces = new HashSet<String>();
      ifaces.addAll(baseIfaceNames);
      if (showLegacy && legacyIfaceNames != null)
         ifaces.add(IPSRequestPreProcessor.class.getName());
      Set<PSExtensionDef> exts = new HashSet<PSExtensionDef>();
      for (String ifname : ifaces)
      {
         exts.addAll(getExtensions(ifname, exits));
      }
      if (showLegacy || exits.isEmpty() || legacyIfaceNames == null
            || legacyIfaceNames.isEmpty())
      {
         return new ArrayList<PSExtensionDef>(exts);
      }

      boolean fail = false;
      Iterator exitIter = exits.iterator();
      while (exitIter.hasNext() && !fail)
      {
         PSConditionalExit exit = (PSConditionalExit) exitIter.next();
         PSExtensionCallSet cs = exit.getRules();
         Iterator callIter = cs.iterator();
         while (callIter.hasNext() && !fail)
         {
            boolean found = false;
            PSExtensionCall call = (PSExtensionCall) callIter.next();
            for (PSExtensionDef def : exts)
            {
               if (def.getRef().equals(call.getExtensionRef()))
               {
                  found = true;
                  break;
               }
            }
            if (!found)
            {
               fail = true;
            }
         }
      }
      
      if (fail && !legacyIfaceNames.isEmpty())
      {
         ifaces.addAll(legacyIfaceNames);
         exts.addAll(getExtensions(legacyIfaceNames, null, false, exits));
      }
      
      return new ArrayList<PSExtensionDef>(exts);
   }   

   /**
    * Method to retrieve all conditional exits currently defined in this
    * control.
    * 
    * @return a collection of <code>PSConditionalExit</code> objects, never
    *         <code>null</code>, may be empty.
    */
   public PSCollection getConditionalExits()
   {
      PSCollection exits = new PSCollection(PSConditionalExit.class);
      @SuppressWarnings("unchecked")
      List<DataContainer> rows = m_extsTable.getValues();
      for (DataContainer data : rows)
      {
         if(data.isEmpty())
            continue;
         PSConditionalExit exit = null;
         if (data.getExtensionCalls() == null)
            continue;
         exit = new PSConditionalExit(data.getExtensionCalls());
         if (data.getConditionals() != null)
            exit.setCondition(data.getConditionals());
         exits.add(exit);
      }
      return exits;
   }

   /*
    *  (non-Javadoc)
    * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
    */
   public void widgetSelected(SelectionEvent e)
   {
      if (e.getSource() == m_extCellEditor.getCombo())
      {
         String val = (String) m_extCellEditor.getValue();
         // If the value is empty user just clicked on the combo but did not
         // select anything just return
         if (StringUtils.isBlank(val))
            return;

         TableItem[] item = m_extsTable.getTable().getSelection();
         DataContainer data = (DataContainer) item[0].getData();
         boolean emptyData = data.isEmpty();
         // If user selects the same exit return
         if (!emptyData && data.getExtensionDef().toString().equals(val))
            return;

         int index = m_exitNames.indexOf(val);
         IPSExtensionDef extDef = m_exitsList.get(index).getExtensionDef();

         OSExitCallSet callSet = new OSExitCallSet();
         callSet.setExtension(extDef, (String) extDef.getInterfaces().next());
         data.setExtensionDef(extDef);
         data.setExtensionCalls(callSet);
         data.setConditionals(null);

         if (emptyData)
         {
            m_exitData.add(data);
            m_extsTable.refreshTable();
         }
         Iterator iter = extDef.getRuntimeParameterNames();
         if (iter.hasNext())
         {
            m_extCellEditor.getButton().setVisible(true);
            openExtensionParamDialog();
         }
         else
         {
            m_extCellEditor.getButton().setVisible(false);
         }
         // Set the Description
         String desc = data.getExtensionDef().getInitParameter(
               IPSExtensionDef.INIT_PARAM_DESCRIPTION);
         m_description.setText(desc);

      }
      else if (e.getSource() == m_extCellEditor.getButton())
      {
         openExtensionParamDialog();
      }
      else if (e.getSource() == m_extsTable.getTable())
      {
         TableItem[] item = m_extsTable.getTable().getSelection();
         DataContainer data = (DataContainer) item[0].getData();
         if (!data.isEmpty())
         {
            Iterator iter = data.getExtensionDef().getRuntimeParameterNames();
            if (iter.hasNext())
               m_extCellEditor.getButton().setVisible(true);
            else
               m_extCellEditor.getButton().setVisible(false);
            if (data.getConditionals() == null || data.getConditionals().isEmpty())
               m_btnCellEditor.setButtonFont(false);
            else
               m_btnCellEditor.setButtonFont(true);
            // Set the Description
            String desc = data.getExtensionDef().getInitParameter(
                  IPSExtensionDef.INIT_PARAM_DESCRIPTION);
            m_description.setText(desc);
         }
      }
      else if (e.getSource() == m_btnCellEditor.getButton())
      {
         m_btnCellEditor.getButton().setEnabled(false);
         final TableItem[] item = m_extsTable.getTable().getSelection();
         final Display display = getShell().getDisplay();
         final DataContainer data = (DataContainer) item[0].getData();
         if (data.isEmpty())
            return;
         PSApplyWhen conds = data.getConditionals();
         if(conds == null)
            conds = new PSApplyWhen();
         final PSApplyWhen conditionals = conds;
         final AwtSwtModalDialogBridge bridge = 
            new AwtSwtModalDialogBridge(
               getShell());
            SwingUtilities.invokeLater( new Runnable()
            {
            @SuppressWarnings("synthetic-access") //$NON-NLS-1$
            public void run()
            {            
               try
               {
                  PSRuleEditorDialog rulesDlg = new PSRuleEditorDialog((Frame)null);
                  bridge.registerModalSwingDialog(rulesDlg);
                  rulesDlg.center();
                  if(conditionals != null)
                  {
                     rulesDlg.onEdit(conditionals.iterator());
                  }
                  rulesDlg.setVisible(true);
                  if (rulesDlg.isOk())
                  {
                     conditionals.clear();
                     Iterator rulesWalker = rulesDlg.getRulesIterator();
                     while (rulesWalker.hasNext())
                     {
                        conditionals.add(rulesWalker.next());
                     }
                     data.setConditionals(conditionals);
                     display.asyncExec(new Runnable()
                     {
                        public void run()
                        {
                           setConditionalCellFont(item[0]);
                        }
                     });
                  }
               }
               catch (ClassNotFoundException ex)
               {
                  //This should not happen as we are creating an existing PSRule collection.
                  ex.printStackTrace();
               }
               finally
               {
                  display.asyncExec(new Runnable()
                  {
                     public void run()
                     {
                        m_btnCellEditor.getButton().setEnabled(true);
                     }
                  });

               }
            }
            });
      }
      m_editor.updateDesignerObject(m_editor.getDesignerObject(),m_extsTable);
   }
   
   /**
    * Sets the font of the conditional cell either normal or bold, based on the
    * supplied boolean bold parameter.
    * 
    * @param item object of TableItem for which the font needs to be set.
    */
   private void setConditionalCellFont(TableItem item)
   {
      //Create a dummy button
      DataContainer data = (DataContainer) item.getData();
      Button btn = new Button(this, SWT.NONE);
      if(data.getConditionals() == null || data.getConditionals().isEmpty())
      {
         item.setFont(1,btn.getFont());
         m_btnCellEditor.setButtonFont(false);
      }
      else
      {
         item.setFont(1,SWTResourceManager.getBoldFont(btn.getFont()));
         m_btnCellEditor.setButtonFont(true);
      }
      //dispose the dummy button
      btn.dispose();
   }

   /*
    *  (non-Javadoc)
    * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
    */
   public void widgetDefaultSelected(@SuppressWarnings("unused") SelectionEvent e)
   {
   }

   /**
    * Opens extension parameter dialog.
    */
   private void openExtensionParamDialog()
   {
      String val = (String) m_extCellEditor.getValue();
      if (StringUtils.isBlank(val))
         return;
      TableItem[] item = m_extsTable.getTable().getSelection();
      DataContainer data = (DataContainer) item[0].getData();
      OSExitCallSet extCallSet = data.getExtensionCalls();
      OSExtensionCall extCall = (OSExtensionCall) extCallSet.get(0);
      PSExtensionParamValue[] values = extCall.getParamValues();
      Iterator iter = data.getExtensionDef().getRuntimeParameterNames();
      Map<String, IPSReplacementValue> params = 
         new HashMap<String, IPSReplacementValue>();
      int i = 0;
      while (iter.hasNext())
      {
         String name = (String) iter.next();
         IPSReplacementValue value = values[i].getValue();
         params.put(name, value);
         i++;
      }
      PSExtensionParamsDialog dialog = new PSExtensionParamsDialog(getShell(),
            data.getExtensionDef(), params);

      if (dialog.open() == Dialog.OK)
      {
         List<PSPair> retparams = dialog.getParamValues();
         PSExtensionParamValue[] newvalues = new PSExtensionParamValue[retparams
               .size()];

         for (int j = 0; j < retparams.size(); j++)
         {
            IPSReplacementValue repVal = (IPSReplacementValue) retparams.get(j).getSecond();
            if (repVal == null)
               repVal = new PSTextLiteral(StringUtils.EMPTY);
            newvalues[j] = new PSExtensionParamValue(repVal);
         }
         extCall.setParamValues(newvalues);
         extCallSet.clear();
         extCallSet.add(extCall);
         data.setExtensionCalls(extCallSet);
         m_exitData.add(m_extsTable.getTable().getSelectionIndex(),data);
         m_extsTable.refreshTable();
      }
   }
   
   /*
    * Controls for this class
    */
   private PSSortableTable m_extsTable;
   private List<DataContainer> m_exitData;
   private PSButtonedComboBoxCellEditor m_extCellEditor;
   private List<DataContainer> m_exitsList;
   private PSButtonCellEditor m_btnCellEditor;
   private Text m_description;
   private Text m_maxErrorsText;
   private Label m_maxErrorsLabel;
   private List<String> m_exitNames;
   private int m_maxErrorsToStop;
   private int m_type;
   private PSEditorBase m_editor;
   /*
    * Constants for type of extensions this composite holding
    */
   public static final int INPUT_TRANSFORMS = 1;
   public static final int OUTPUT_TRANSFORMS = 2;
   public static final int VALIDATIONS = 3;
   public static final int PRE_EXITS = 4;
   public static final int POST_EXITS = 5;
}
