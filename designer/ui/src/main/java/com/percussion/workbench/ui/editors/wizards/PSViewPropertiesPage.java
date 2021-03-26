/******************************************************************************
 *
 * [ PSViewPropertiesPage.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.wizards;

import com.percussion.client.IPSReference;
import com.percussion.cms.objectstore.PSSearch;
import com.percussion.cms.objectstore.PSSearchField;
import com.percussion.workbench.ui.FeatureSet;
import com.percussion.workbench.ui.IPSUiConstants;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.controls.PSDefaultContentProvider;
import com.percussion.workbench.ui.controls.PSNameLabelDesc;
import com.percussion.workbench.ui.controls.PSSelectCommunitiesComposite;
import com.percussion.workbench.ui.editors.form.PSSearchEditor;
import com.percussion.workbench.ui.editors.form.PSViewParentCategoryComposite;
import com.percussion.workbench.ui.util.PSControlInfo;
import com.percussion.workbench.ui.util.PSHelpHintKeyHelper;
import com.percussion.workbench.ui.util.PSUiUtils;
import com.percussion.workbench.ui.validators.IPSControlValueValidator;
import com.percussion.workbench.ui.validators.PSControlValidatorFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * @version 1.0
 * @created 03-Sep-2005 4:44:05 PM
 */
public class PSViewPropertiesPage extends PSWizardPageBase
   implements IPSUiConstants
{

  
   /**
    * Ctor
    * @param isView flag indicating that this wizard page is for a view
    * @param pagename page name that will appear in wizards message box
    * @param title the title that will appear in the wizards 
    * message box.
    * @param titleImage the image that will appear in the wizards
    * message box. May be <code>null</code>
    */
   public PSViewPropertiesPage(
      boolean isView, String pagename, String title, ImageDescriptor titleImage)
   {
      super(pagename, title, titleImage);
      m_isView = isView;
   }

   /**
    * 
    * @param parent
    */
   public void createControl(Composite parent)
   {
      Composite comp = new Composite(parent, SWT.NONE);
      comp.setLayout(new FormLayout());
      
      
      PSControlValidatorFactory vFactory = 
         PSControlValidatorFactory.getInstance();
      IPSControlValueValidator requiredValidator = 
         vFactory.getRequiredValidator();
      String namePrefix = m_isView
         ? PSMessages.getString("PSViewPropertiesPage.view.title") //$NON-NLS-1$
         : PSMessages.getString("PSViewPropertiesPage.search.title"); //$NON-NLS-1$
         // This composite takes care of registering its own controls
         m_commonComp = 
         new PSNameLabelDesc(
            comp, SWT.NONE, namePrefix, WIZARD_LABEL_NUMERATOR, 
            PSNameLabelDesc.SHOW_ALL, this);
      {
         final FormData formData = new FormData();
         formData.right = new FormAttachment(100, 0);
         formData.top = new FormAttachment(0, 0);
         formData.left = new FormAttachment(0, 0);
         m_commonComp.setLayoutData(formData);
      }
      
      // Override text limit for name and label
      ((Text)m_commonComp.getNameText()).setTextLimit(255);
      m_commonComp.getLabelText().setTextLimit(255);      
      
      
      final Label typeLabel = new Label(comp, SWT.NONE);
      typeLabel.setAlignment(SWT.RIGHT);
      final FormData formData_1 = new FormData();      
      formData_1.right = new FormAttachment(WIZARD_LABEL_NUMERATOR, 0);
      formData_1.top = new FormAttachment(m_commonComp, 
         LABEL_HSPACE_OFFSET 
         + LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, SWT.BOTTOM);
      formData_1.left = new FormAttachment(0, 0);
      typeLabel.setLayoutData(formData_1);
      typeLabel.setText(
         PSMessages.getString(
            "PSViewPropertiesPage.type.label")); //$NON-NLS-1$

      m_typeComboViewer = new ComboViewer(comp, SWT.READ_ONLY);
      m_typeComboViewer.setContentProvider(new PSDefaultContentProvider());
      m_typeCombo = m_typeComboViewer.getCombo();
      final FormData formData_2 = new FormData();
      formData_2.right = new FormAttachment(50, 0);
      formData_2.top = 
         new FormAttachment(typeLabel, 
            -LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, SWT.TOP);
      formData_2.left = 
         new FormAttachment(typeLabel, LABEL_HSPACE_OFFSET, SWT.RIGHT);
      m_typeCombo.setLayoutData(formData_2);
      List<String> types = new ArrayList<String>();
      types.add(PSMessages.getString(
         "PSViewPropertiesPage.types.standard.choice"));  //$NON-NLS-1$      
      types.add(PSMessages.getString(
         "PSViewPropertiesPage.types.url.choice")); //$NON-NLS-1$
      m_typeComboViewer.setInput(types);
      // Set default value of "Standard"
      m_typeCombo.select(0);
      registerControl("PSViewPropertiesPage.type.label",
         m_typeCombo, new IPSControlValueValidator[]{requiredValidator});

      if(m_isView)
      {
         m_parentCatComp = new PSViewParentCategoryComposite(comp, this, false);
         final FormData formData_3 = new FormData();         
         formData_3.top = new FormAttachment(typeLabel, 0, SWT.TOP);
         formData_3.left = new FormAttachment(m_typeCombo, 40, SWT.RIGHT);
         formData_3.right = new FormAttachment(100, 0);
         m_parentCatComp.setLayoutData(formData_3);
      }
      
      m_communitiesControl = new PSSelectCommunitiesComposite(comp, SWT.NONE);
      {
         final FormData formData = new FormData();
         formData.left = new FormAttachment(0);
         formData.right = new FormAttachment(100);
         formData.top = new FormAttachment(m_typeCombo, COMBO_VSPACE_OFFSET, SWT.BOTTOM);
         formData.bottom = new FormAttachment(100);
         m_communitiesControl.setLayoutData(formData);
      }
      registerControl(
         "PSSelectCommunitiesComposite.label.visible.communities", //$NON-NLS-1$
         m_communitiesControl.getSlushControl(),
         null,
         PSControlInfo.TYPE_COMMUNITY);
      registerControlHelpOnly("common.label.filter",
         m_communitiesControl.getFilterTextControl());
      
      setControl(comp);
   }

   /**
    * Checks the data in the 'Type' control.
    * 
    * @return <code>true</code> if the value is 'Standard' (or equivalent),
    * <code>false</code> otherwise.
    */
   public boolean isStandard()
   {
      return m_typeCombo.getSelectionIndex() == 0;      
   }
   
   /* 
    * @see com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater#
    * updateDesignerObject(java.lang.Object, java.lang.Object)
    */
   public void updateDesignerObject(Object designObject, Object control)
   {
      PSSearch def = (PSSearch)designObject;
      if(control == m_commonComp.getNameText())
      {
         def.setInternalName(((Text)m_commonComp.getNameText()).getText());
      }
      else if(control == m_commonComp.getLabelText())
      {
         def.setDisplayName(m_commonComp.getLabelText().getText());
      }
      else if (control == m_commonComp.getDescriptionText())
      {
         def.setDescription(m_commonComp.getDescriptionText().getText());
      }      
      else if (control == m_typeCombo)
      {
         boolean isCustom = !isStandard();
         if(m_isView)
         {
            def.setType(PSSearch.TYPE_VIEW);
         }
         else
         {
            def.setType(
               isCustom 
                  ? PSSearch.TYPE_CUSTOMSEARCH 
                  : PSSearch.TYPE_STANDARDSEARCH);
            
         }
         def.setCustom(isCustom);
         setDefaults(def, isCustom);
      }
      else
      {
         if(m_isView)
            m_parentCatComp.updateDesignerObject(def, control);
      }
      
      
   }
   
   /**
    * Sets the various default settings needed for searches and
    * views.
    * @param def assumed not <code>null</code>.
    * @param isCustom flag indicating that this is a custom view/search
    */
   private void setDefaults(PSSearch def, boolean isCustom)
   {
      if(!isCustom || !m_isView)
      {
         def.setMaximumNumber(100);
      }
      boolean isFTS = FeatureSet.isFTSearchEnabled();
      boolean useFTS = false;
      if(!isCustom)
      {
         if (isFTS)
         {
            useFTS = true;
            def.setProperty(PSSearch.PROP_EXPANSIONLEVEL, "4"); //$NON-NLS-1$
            def.setProperty(PSSearch.PROP_SEARCH_MODE, 
               PSSearch.SEARCH_MODE_SIMPLE);
            def.setProperty(PSSearch.PROP_SEARCH_ENGINE_TYPE, 
               PSSearch.SEARCH_ENGINE_TYPE_EXTERNAL);
         }

         if(!m_isView)
         {
            def.setProperty(PSSearch.PROP_USER_CUSTOMIZABLE, "true"); //$NON-NLS-1$           
         }
      } 
      
      //    Set default search criteria of sys_title
      PSSearchField field = 
         new PSSearchField("sys_title", "Title", "T", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            "Text", "Content item title"); //$NON-NLS-1$ //$NON-NLS-2$
      if (useFTS)
         field.setExternalOperator("CONCEPT");
      
      def.addField(field);
      
      // Set default display format as "Default"
      IPSReference formatRef = PSUiUtils.getReferenceByName(
         PSSearchEditor.getDisplayFormats(),
         "Default");
      def.setDisplayFormatId(formatRef.getId().getUUID() + "");
   }
   
   /* 
    * @see com.percussion.workbench.ui.editors.wizards.PSWizardPageBase#
    * getHelpKey(org.eclipse.swt.widgets.Control)
    */
   @Override
   public String getHelpKey(Control control)
   {
      String base = super.getHelpKey(control);
      if(m_isView)
         return super.getHelpKey(control);
      return base.substring(0, base.lastIndexOf('.')) +
      "PSSearchPropertiesPage";
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
            PSNameLabelDesc.DESC_TEXT_KEY,
               "description",
            PSNameLabelDesc.LABEL_TEXT_KEY,
               "label",
            "PSViewPropertiesPage.type.label",
               "type",
            "PSViewParentCategoryComposite.parentCategory.label",
               "parent_category",
            "PSSelectCommunitiesComposite.label.visible.communities",
               "visible_in_these_communities",
            "common.label.filter",
               "filter"
         });
         if(m_isView)
            m_helpHintKeyHelper.addMapping(PSNameLabelDesc.NAME_TEXT_KEY,
               "view_name");
         else
            m_helpHintKeyHelper.addMapping(PSNameLabelDesc.NAME_TEXT_KEY,
               "search_name");
      }
      
      return m_helpHintKeyHelper.getKey(
         super.getHelpHintKey(controlInfo), controlInfo);
   }
   
   /**
    * Cache of the help hint helper object
    */
   private PSHelpHintKeyHelper m_helpHintKeyHelper;
   
   
   // Controls
   private PSNameLabelDesc m_commonComp;
   private Combo m_typeCombo;
   private ComboViewer m_typeComboViewer;
   
   /**
    * Communities selection control.
    */
   private PSSelectCommunitiesComposite m_communitiesControl;
   
   /**
    * flag indicating that this wizard page is for a view, set
    * in ctor.
    */
   private boolean m_isView;
   
   /**
    * Composite that contains the parent categories drop down
    * and functionality. Initialized during construction, never
    * <code>null</code> or modified after that.
    */
   private PSViewParentCategoryComposite m_parentCatComp;
   
   
}
