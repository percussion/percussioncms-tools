/******************************************************************************
 *
 * [ PSRelTypeGeneralPage.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form;


import com.percussion.design.objectstore.PSRelationshipConfig;
import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class PSRelTypeGeneralPage extends PSNameDescPageBase
{
   public PSRelTypeGeneralPage(Composite parent, int style, PSEditorBase editor)
   {
      super(parent, style, editor);
      final Label categoryLabel =
            createTopLabel(m_leftPane, PSRelTypeCategoryHelper.LABEL);
      m_relTypeCategoryHelper.setCombo(
            createTopComboWithLabel(m_leftPane, categoryLabel));

      final Label propertiesLabel = createTopLabel(m_rightPane,
            getMessage("PSRelTypeGeneralPage.label.properties"));  //$NON-NLS-1$
      PSRelationshipConfig config = (PSRelationshipConfig)editor.m_data;
      m_propertiesHelper = new PSRelTypePropertiesTableHelper(
         true, config.isSystem());
      m_propertiesHelper.initUI(m_rightPane, propertiesLabel);
      editor.registerControl(propertiesLabel.getText(),
            m_propertiesHelper.getPropertiesTable(), null);
   }

   @Override
   protected String getNamePrefix()
   {
      return getMessage("PSRelTypeGeneralPage.label.name"); //$NON-NLS-1$
   }

   /**
    * Load controls with the relationship type values.
    */
   public void loadControlValues(final PSRelationshipConfig relType)
   {
      ((Label) m_nameLabelDesc.getNameText()).setText(relType.getName());
      m_nameLabelDesc.getLabelText().setText(relType.getLabel());
      m_nameLabelDesc.getDescriptionText().setText(
            StringUtils.defaultString(relType.getDescription()));
      m_relTypeCategoryHelper.loadControlValues(relType);
      m_propertiesHelper.loadControlValues(relType);
   }
   
   /**
    * Updates relationship type with the controls selection.
    */
   final void updateRelType(final PSRelationshipConfig relType)
   {
      relType.setLabel(m_nameLabelDesc.getLabelText().getText());
      relType.setDescription(m_nameLabelDesc.getDescriptionText().getText());
      m_relTypeCategoryHelper.updateRelType(relType);
      m_propertiesHelper.updateRelType(relType);
   }

   /**
    * Manages the category types dropdown.
    */
   final PSRelTypeCategoryHelper m_relTypeCategoryHelper =
         new PSRelTypeCategoryHelper();
   /**
    * The properties UI. Initialized in ctor, never <code>null</code> after
    * that.
    */
   private final PSRelTypePropertiesTableHelper m_propertiesHelper;
}
