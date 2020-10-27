/******************************************************************************
 *
 * [ PSControlMainTabComposite.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form.ce;

import com.percussion.E2Designer.JavaExitsPropertyDialog;
import com.percussion.E2Designer.OSExitCallSet;
import com.percussion.E2Designer.OSExtensionCall;
import com.percussion.client.PSModelException;
import com.percussion.design.objectstore.IPSDependentObject;
import com.percussion.design.objectstore.PSControlMeta;
import com.percussion.design.objectstore.PSControlRef;
import com.percussion.design.objectstore.PSDependency;
import com.percussion.design.objectstore.PSExtensionCall;
import com.percussion.design.objectstore.PSField;
import com.percussion.design.objectstore.PSProperty;
import com.percussion.design.objectstore.PSPropertySet;
import com.percussion.util.PSCollection;
import com.percussion.workbench.ui.IPSUiConstants;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.PSWorkbenchPlugin;
import org.apache.commons.lang.StringUtils;
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
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Text;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Control main tab composite handles the control properties. Displays the
 * control type lets the user to edit the control paramets and other properties.
 * The container dialog should call {@link #updateData()} to update the supplied
 * data object.
 */
public class PSControlMainTabComposite extends Composite
      implements
         IPSUiConstants
{

  /**
   * Ctor.
   * @param parent The parent composite.
   * @param style The SWT style to be applied to this composite
   * @param rowData The data object that needs to be handled in this dialog.
   * Must not be <code>null</code>.
   * @param editorType The type of the editor. Must be a valid type as defined
   * in {@link PSContentEditorDefinition#isValidEditorType(int)}.
   */
   public PSControlMainTabComposite(Composite parent, int style,
         PSFieldTableRowDataObject rowData, int editorType) {
      super(parent, style);
      if (!PSContentEditorDefinition.isValidEditorType(editorType))
      {
         throw new IllegalArgumentException("editorType is invalid"); //$NON-NLS-1$
      }
      if (rowData == null || rowData.isEmpty())
      {
         throw new IllegalArgumentException("rowData must not be null or empty"); //$NON-NLS-1$
      }
      m_rowData = rowData;
      try
      {
         m_ctrlMeta = PSContentEditorDefinition.getControl(m_rowData
               .getControlName());
      }
      catch (PSModelException e)
      {
         // This should not happen as we fix all the controls while
         // opening the editor.
         PSWorkbenchPlugin
               .handleException(
                     "Control Editor",
                     "Missing control definition",
                     "The following error occured while getting the definition for the control.",
                     e);
         return;
      }

      setLayout(new FormLayout());
      createControls();
      loadControlValues();
      //
   }

   /**
    * Creates teh controls for this composite.
    *
    */
   public void createControls()
   {
      final Label dataTypeLabel = new Label(this, SWT.WRAP);
      dataTypeLabel.setAlignment(SWT.RIGHT);
      final FormData formData_2 = new FormData();
      formData_2.right = new FormAttachment(WIZARD_LABEL_NUMERATOR, 0);
      formData_2.top = new FormAttachment(0, 20);
      formData_2.left = new FormAttachment(0, 10);
      dataTypeLabel.setLayoutData(formData_2);
      dataTypeLabel.setText(PSMessages.getString(
         "PSControlMainTabComposite.dataType.label")); //$NON-NLS-1$

      m_Text = new Text(this, SWT.BORDER);
      m_Text.setEditable(false);
      final FormData formData_3 = new FormData();
      formData_3.right = new FormAttachment(100, -10);
      formData_3.top = new FormAttachment(dataTypeLabel,
            LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, SWT.TOP);
      formData_3.left = new FormAttachment(dataTypeLabel, LABEL_HSPACE_OFFSET,
            SWT.RIGHT);
      m_Text.setLayoutData(formData_3);
      m_Text.setEditable(false);
      
      m_controlParamsComp = new PSControlParametersComposite(this, SWT.NONE,
            m_ctrlMeta);
      final FormData formData_4 = new FormData();
      formData_4.right = new FormAttachment(100, -10);
      formData_4.top = new FormAttachment(m_Text,LABEL_VSPACE_OFFSET,SWT.BOTTOM);
      formData_4.left = new FormAttachment(0, 10);
      m_controlParamsComp.setLayoutData(formData_4);
      
      if (!m_rowData.isFieldSet())
      {
         if (!m_ctrlMeta.getDependencies().isEmpty())
         {
            m_ctrlDependenciesButton = new Button(this, SWT.NONE);
            final FormData depFD = new FormData();
            depFD.top = new FormAttachment(m_controlParamsComp, LABEL_VSPACE_OFFSET,
                  SWT.BOTTOM);
            depFD.left = new FormAttachment(0, 10);
            m_ctrlDependenciesButton.setLayoutData(depFD);
            m_ctrlDependenciesButton.setText("Dependencies");
            m_ctrlDependenciesButton
                  .addSelectionListener(new SelectionAdapter()
                  {
                     @Override
                     public void widgetSelected(SelectionEvent e)
                     {
                        Menu menu = new Menu(getShell(), SWT.POP_UP);
                        List<PSDependency> deps = m_rowData
                              .getControlDependencies();
                        if (deps == null || deps.isEmpty())
                           deps = m_ctrlMeta.getDependencies();
                        for (final PSDependency dep : deps)
                        {
                           MenuItem link = new MenuItem(menu, SWT.NONE);
                           link.setText(dep.toString());
                           link.addSelectionListener(new SelectionAdapter()
                           {
                              public void widgetSelected(SelectionEvent e)
                              {
                                 SwingUtilities.invokeLater(new Runnable()
                                 {
                                    @SuppressWarnings("synthetic-access")
                                    public void run()
                                    {
                                       IPSDependentObject dependent = dep
                                             .getDependent();
                                       if (dependent instanceof PSExtensionCall)
                                       {
                                          OSExtensionCall call;
                                          if (dependent instanceof OSExtensionCall)
                                             call = (OSExtensionCall) dependent;
                                          else
                                          {
                                             call = new OSExtensionCall(
                                                   (PSExtensionCall) dependent);
                                          }

                                          /*
                                           * Make a call set and call the dialog
                                           * with disabled action for adding or
                                           * deleting the exits. To add it to
                                           * the call set it needs one of the
                                           * supported interface type, so give
                                           * the first interface found.
                                           */
                                          OSExitCallSet callSet = new OSExitCallSet();
                                          Iterator iter = call
                                                .getExtensionDef()
                                                .getInterfaces();
                                          callSet.add(call, (String) iter
                                                .next());

                                          JavaExitsPropertyDialog dlg = new JavaExitsPropertyDialog(
                                                null, callSet, false);
                                          dlg.setVisible(true);

                                          dep
                                                .setDependent((OSExtensionCall) callSet
                                                      .get(0));
                                       }
                                    }
                                 });

                              };
                           });
                        }
                        menu.setVisible(true);
                     }
                  });
         }
         final Label fieldDataPropertiesLabel = new Label(this, SWT.WRAP);
         final FormData formData_6 = new FormData();
         Control prevCtrl = m_ctrlDependenciesButton == null
               ? m_controlParamsComp
               : m_ctrlDependenciesButton;
         formData_6.top = new FormAttachment(prevCtrl, LABEL_VSPACE_OFFSET,
               SWT.BOTTOM);
         formData_6.left = new FormAttachment(0, 10);
         fieldDataPropertiesLabel.setLayoutData(formData_6);
         fieldDataPropertiesLabel.setText(PSMessages
               .getString("PSControlMainTabComposite.fieldDataProps.label")); //$NON-NLS-1$

         final Label fieldDataPropertiesSep = new Label(this, SWT.SEPARATOR
               | SWT.HORIZONTAL);
         final FormData formData_6a = new FormData();
         formData_6a.right = new FormAttachment(100, -10);
         formData_6a.top = new FormAttachment(fieldDataPropertiesLabel, 0,
               SWT.CENTER);
         formData_6a.left = new FormAttachment(fieldDataPropertiesLabel,
               LABEL_HSPACE_OFFSET, SWT.RIGHT);
         fieldDataPropertiesSep.setLayoutData(formData_6a);

         m_allowInlineLinksButton = new Button(this, SWT.CHECK);
         final FormData formData_7 = new FormData();
         formData_7.right = new FormAttachment(100, -10);
         formData_7.top = new FormAttachment(fieldDataPropertiesLabel,
               LABEL_VSPACE_OFFSET, SWT.BOTTOM);
         formData_7.left = new FormAttachment(fieldDataPropertiesLabel,
               LABEL_HSPACE_OFFSET, SWT.LEFT);
         m_allowInlineLinksButton.setLayoutData(formData_7);
         m_allowInlineLinksButton.setText(PSMessages
               .getString("PSControlMainTabComposite.allowInlineLinks.label")); //$NON-NLS-1$

         m_cleanUpBrokenButton = new Button(this, SWT.CHECK);
         final FormData formData_8 = new FormData();
         formData_8.right = new FormAttachment(100, -10);
         formData_8.top = new FormAttachment(m_allowInlineLinksButton,
               LABEL_VSPACE_OFFSET, SWT.BOTTOM);
         formData_8.left = new FormAttachment(fieldDataPropertiesLabel,
               LABEL_HSPACE_OFFSET, SWT.LEFT);
         m_cleanUpBrokenButton.setLayoutData(formData_8);
         m_cleanUpBrokenButton.setText(PSMessages
               .getString("PSControlMainTabComposite.cleanBrokenLinks.label")); //$NON-NLS-1$

         m_mayContainIdentifiersButton = new Button(this, SWT.CHECK);
         final FormData formData_9 = new FormData();
         formData_9.right = new FormAttachment(100, -10);
         formData_9.top = new FormAttachment(m_cleanUpBrokenButton,
               LABEL_VSPACE_OFFSET, SWT.BOTTOM);
         formData_9.left = new FormAttachment(fieldDataPropertiesLabel,
               LABEL_HSPACE_OFFSET, SWT.LEFT);
         m_mayContainIdentifiersButton.setLayoutData(formData_9);
         m_mayContainIdentifiersButton.setText(PSMessages
               .getString("PSControlMainTabComposite.identifiers.label")); //$NON-NLS-1$

         // Namespace Controls

         final Label namespaceLabel = new Label(this, SWT.WRAP);
         final FormData formData_10 = new FormData();
         formData_10.top = new FormAttachment(m_mayContainIdentifiersButton,
               LABEL_VSPACE_OFFSET, SWT.BOTTOM);
         formData_10.left = new FormAttachment(0, 10);
         namespaceLabel.setLayoutData(formData_10);
         namespaceLabel.setText(PSMessages
               .getString("PSControlMainTabComposite.namespaces.label")); //$NON-NLS-1$

         final Label namespaceSep = new Label(this, SWT.SEPARATOR
               | SWT.HORIZONTAL);
         final FormData formData_11 = new FormData();
         formData_11.right = new FormAttachment(100, -10);
         formData_11.top = new FormAttachment(namespaceLabel, 0, SWT.CENTER);
         formData_11.left = new FormAttachment(namespaceLabel,
               LABEL_HSPACE_OFFSET, SWT.RIGHT);
         namespaceSep.setLayoutData(formData_11);

         m_cleanupNamespacesButton = new Button(this, SWT.CHECK);
         m_cleanupNamespacesButton.addSelectionListener(new SelectionAdapter()
         {

            /*
             * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(
             *      org.eclipse.swt.events.SelectionEvent)
             */
            @SuppressWarnings("synthetic-access")
            @Override
            public void widgetSelected(SelectionEvent e)
            {
               handleNamespacePrefixEnable();
            }

         });
         final FormData formData_12 = new FormData();
         formData_12.right = new FormAttachment(100, -10);
         formData_12.top = new FormAttachment(namespaceLabel,
               LABEL_VSPACE_OFFSET, SWT.BOTTOM);
         formData_12.left = new FormAttachment(namespaceLabel,
               LABEL_HSPACE_OFFSET, SWT.LEFT);
         m_cleanupNamespacesButton.setLayoutData(formData_12);
         m_cleanupNamespacesButton.setText(PSMessages
               .getString("PSControlMainTabComposite.cleanNamespaces.label")); //$NON-NLS-1$

         m_allowActiveTagsButton = new Button(this, SWT.CHECK);
         final FormData formData_13 = new FormData();
         formData_13.right = new FormAttachment(100, -10);
         formData_13.top = new FormAttachment(m_cleanupNamespacesButton,
               LABEL_VSPACE_OFFSET, SWT.BOTTOM);
         formData_13.left = new FormAttachment(namespaceLabel,
               LABEL_HSPACE_OFFSET, SWT.LEFT);
         m_allowActiveTagsButton.setLayoutData(formData_13);
         m_allowActiveTagsButton.setText(PSMessages
               .getString("PSControlMainTabComposite.allowActiveTags.label")); //$NON-NLS-1$

         final Label namespacePrefixesLabel = new Label(this, SWT.WRAP);
         final FormData formData_14 = new FormData();
         formData_14.top = new FormAttachment(m_allowActiveTagsButton,
               LABEL_VSPACE_OFFSET, SWT.BOTTOM);
         formData_14.left = new FormAttachment(0, 10);
         namespacePrefixesLabel.setLayoutData(formData_14);
         namespacePrefixesLabel.setText(PSMessages
               .getString("PSControlMainTabComposite.namespacePrefixes.label")); //$NON-NLS-1$

         m_namespacePrefixesText = new Text(this, SWT.BORDER | SWT.V_SCROLL
               | SWT.WRAP);
         final FormData formData_15 = new FormData();
         formData_15.top = new FormAttachment(namespacePrefixesLabel, 0,
               SWT.BOTTOM);
         formData_15.left = new FormAttachment(0, 10);
         formData_15.right = new FormAttachment(100, -10);
         formData_15.height = 60;
         m_namespacePrefixesText.setLayoutData(formData_15);

      }

   }

   /**
    * Loads teh control values.
    *
    */
   private void loadControlValues()
   {
      // Set the control name and Data Type
      String dataType = m_ctrlMeta.getDimension();
      m_Text.setText(dataType);
      // Initialize the params in the table
      // Set the params
      m_controlParamsComp.setParamsData(m_rowData.getControlRef()
            .getParameters());

      if (!m_rowData.isFieldSet())
      {
         // Set the FieldData properties
         PSField field = m_rowData.getField();
         m_allowInlineLinksButton.setSelection(field.mayHaveInlineLinks());
         m_cleanUpBrokenButton.setSelection(field.cleanupBrokenInlineLinks());
         m_mayContainIdentifiersButton.setSelection(field
               .getBooleanProperty(PSField.MAY_CONTAIN_IDS_PROPERTY));
         m_cleanupNamespacesButton.setSelection(field.isCleanupNamespaces());
         m_allowActiveTagsButton.setSelection(field.isAllowActiveTags());
         PSProperty allowed = field.getProperty(PSField.DECLARED_NAMESPACES);
         Object allowedvalue = allowed != null ? allowed.getValue() : null;
         String allowedstr = allowedvalue != null
               ? allowedvalue.toString()
               : null;
         m_namespacePrefixesText.setText(StringUtils.defaultString(allowedstr));
         handleNamespacePrefixEnable();
      }
   }

   /**
    * Updates the data. Gets the values from the controls and updates the
    * supplied design object.
    * 
    */
   public void updateData()
   {
      // Set the parameters
      PSControlRef ctrl = m_rowData.getControlRef();
      PSCollection parameters = m_controlParamsComp.getParamsData();
      ctrl.setParameters(parameters);
      m_rowData.setControlRef(ctrl);
      m_rowData.getDisplayMapping().getUISet().setControl(ctrl);
      if (!m_rowData.isFieldSet())
      {
         // Set the FieldData properties
         PSField field = m_rowData.getField();
         
         String rawNamespaces = m_namespacePrefixesText.getText();
         List<String> prefixes = new ArrayList<String>();
         StringTokenizer st = new StringTokenizer(rawNamespaces, ",; "); //$NON-NLS-1$
         while(st.hasMoreTokens())
            prefixes.add(st.nextToken().trim());
         String[] temp = prefixes.toArray(new String[]{});
         field.setDeclaredNamespaces(temp);         
         
         PSPropertySet props = field.getProperties();
         // If propertyset is null then create a new propertyset.
         if (props == null)
            props = new PSPropertySet();
         
         setBooleanFieldProperty(props, PSField.MAY_HAVE_INLINE_LINKS_PROPERTY,
            m_allowInlineLinksButton.getSelection());
         setBooleanFieldProperty(props, PSField.CLEANUP_BROKEN_INLINE_LINKS_PROPERTY,
            m_cleanUpBrokenButton.getSelection());
         setBooleanFieldProperty(props, PSField.MAY_CONTAIN_IDS_PROPERTY,
            m_mayContainIdentifiersButton.getSelection());
         setBooleanFieldProperty(props, PSField.CLEANUP_NAMESPACES_PROPERTY,
            m_cleanupNamespacesButton.getSelection());
         setBooleanFieldProperty(props, PSField.ALLOW_ACTIVE_TAGS_PROPERTY,
            m_allowActiveTagsButton.getSelection());
         
         
         field.setProperties(props);
      }
   }
   
   /**
    * Helper to handle enabling and disabling the 
    * namespace prefixes field depending on the state of 
    * the cleanup namespaces button.
    *
    */
   private void handleNamespacePrefixEnable()
   {
      m_namespacePrefixesText.setEnabled(
         m_cleanupNamespacesButton.getSelection());
   }
   
   /**
    * Helper method to set a fields boolean property
    * @param props assumed not <code>null</code>.
    * @param name assumed not <code>null</code> or empty.
    * @param value boolean value to assign to property
    */
   private void setBooleanFieldProperty(
      PSPropertySet props, String name, boolean value)
   {
      PSProperty prop = props.getProperty(name);
      // If property is null then create a new property
      if (prop == null)
      {
         prop = new PSProperty(name,
               PSProperty.TYPE_BOOLEAN, false, false, null);
         props.add(prop);
      }
      prop.setValue(Boolean.valueOf(value));
   }
   
   /*
    *  (non-Javadoc)
    * @see org.eclipse.swt.widgets.Widget#dispose()
    */
   public void dispose()
   {
      super.dispose();
   }

   /*
    *  (non-Javadoc)
    * @see org.eclipse.swt.widgets.Widget#checkSubclass()
    */
   protected void checkSubclass()
   {
   }


   /*
    * Controls
    */
   private PSFieldTableRowDataObject m_rowData;
   private Text m_Text;
   private Button m_allowInlineLinksButton;
   private Button m_cleanUpBrokenButton;
   private Button m_mayContainIdentifiersButton;
   private Button m_cleanupNamespacesButton;
   private Button m_allowActiveTagsButton;
   private Text m_namespacePrefixesText;
   private Button m_ctrlDependenciesButton;
   private PSControlParametersComposite m_controlParamsComp;
   private PSControlMeta m_ctrlMeta;
}
