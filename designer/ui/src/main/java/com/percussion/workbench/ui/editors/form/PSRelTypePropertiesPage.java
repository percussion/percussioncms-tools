/******************************************************************************
 *
 * [ PSRelTypePropertiesPage.java ]
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
import com.percussion.workbench.ui.util.PSControlInfo;
import com.percussion.workbench.ui.validators.IPSControlValueValidator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.percussion.workbench.ui.IPSUiConstants.LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET;

/**
 * Allows user to edit relationship types properties.
 *
 * @author Andriy Palamarchuk
 */
public class PSRelTypePropertiesPage extends Composite
{
   /**
    * Default constructor. 
    */
   public PSRelTypePropertiesPage(Composite parent, int style,
         final PSEditorBase editor)
   {
      super(parent, style);
      setLayout(new FormLayout());
      m_editor = editor;

      final Label propertiesLabel = createTopLabel(this,
            PSMessages.getString("PSRelTypePropertiesPage.label.properties"));   //$NON-NLS-1$
      PSRelationshipConfig config = (PSRelationshipConfig)editor.m_data;
      m_propertiesHelper = new PSRelTypePropertiesTableHelper(
         false, config.isSystem());
      m_propertiesHelper.initUI(this, propertiesLabel);
      editor.registerControl(propertiesLabel.getText(),
            m_propertiesHelper.getPropertiesTable(),
            new IPSControlValueValidator[] {m_propertyTableValidator});
      editor.registerControl(PSRelTypePropertiesTableHelper.DESC_LABEL,
            m_propertiesHelper.getPropertyDescriptionText(), null);
   }

   /**
    * Creates label in the top left corner of the container.
    */
   private Label createTopLabel(final Composite container, String labelText)
   {
      final Label label = new Label(container, SWT.NONE);
      label.setText(labelText + ':');
      final FormData formData = new FormData();
      formData.left = new FormAttachment(0, 0);
      formData.top =
            new FormAttachment(0, LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET);
      label.setLayoutData(formData);
      return label;
   }

   /**
    * Load controls with the relationship type values.
    */
   public void loadControlValues(final PSRelationshipConfig relType)
   {
      m_propertiesHelper.loadControlValues(relType);
   }
   
   /**
    * Makes sure that relationship type of "Active Assembly" category 
    * have mandatory properties.
    */
   private class PSMandatoryAssemblyPropsValidator
         implements IPSControlValueValidator
   {
      // see interface
      public String validate(
            @SuppressWarnings("unused") PSControlInfo controlInfo) //$NON-NLS-1$
      {
         final PSRelationshipConfig relType =
               (PSRelationshipConfig) m_editor.getDesignerObject();
         
         if (relType.getCategory().equals(
               PSRelationshipConfig.CATEGORY_ACTIVE_ASSEMBLY))
         {
            final List<String> mandatoryProps = new ArrayList<String>(
                  PSRelationshipConfig.getPreDefinedUserPropertyNames());
            for (Object o : m_propertiesHelper.getPropertiesTable().getValues())
            {
               final PSProperty property = (PSProperty) o;
               mandatoryProps.remove(property.getName());
            }
            if (!mandatoryProps.isEmpty())
            {
               Collections.sort(mandatoryProps);
               final String propsStr = listToString(mandatoryProps);
               if (mandatoryProps.size() == 1)
               {
                  return PSMessages.getString(
                        "PSRelTypePropertiesPage.error.required1Property", //$NON-NLS-1$
                        propsStr);
               }
               return PSMessages.getString(
                     "PSRelTypePropertiesPage.error.requiredNProperties", //$NON-NLS-1$
                     propsStr);
            }
            return null;
         }

         return null;
      }
   }
   
   /**
    * Pretty-prints the strings.
    */
   String listToString(List<String> strings)
   {
      if (strings.isEmpty())
      {
         return "";                 //$NON-NLS-1$
      }
      final StringBuffer buf = new StringBuffer();
      final String separator = ", "; //$NON-NLS-1$
      for (final String s : strings)
      {
         buf.append(s);
         buf.append(separator);
      }
      buf.delete(buf.length() - separator.length(), buf.length());
      return buf.toString();
   }
   
   /**
    * Updates relationship type with the controls selection.
    */
   void updateRelType(final PSRelationshipConfig relType)
   {
      m_propertiesHelper.updateRelType(relType);
   }
   
   /**
    * Validator on the properties page.
    */
   public IPSControlValueValidator getPropertyTableValidator()
   {
      return m_propertyTableValidator;
   }
   
   /**
    * The properties UI. Initialized in ctor, never <code>null</code> after
    * that.
    */
   private final PSRelTypePropertiesTableHelper m_propertiesHelper ;
   
   /**
    * The property table validator.
    */
   private IPSControlValueValidator m_propertyTableValidator =
         new PSMandatoryAssemblyPropsValidator();
   
   /**
    * The editor containing the page.
    */
   final PSEditorBase m_editor;
}
