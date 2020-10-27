/******************************************************************************
*
* [ PSViewParentCategoryComposite.java ]
*
* COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
* All rights reserved. This material contains unpublished, copyrighted
* work including confidential and proprietary information of Percussion.
*
*****************************************************************************/
package com.percussion.workbench.ui.editors.form;

import com.percussion.cms.objectstore.PSSearch;
import com.percussion.workbench.ui.IPSUiConstants;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.controls.PSDefaultContentProvider;
import com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater;
import com.percussion.workbench.ui.editors.wizards.PSWizardPageBase;
import com.percussion.workbench.ui.validators.IPSControlValueValidator;
import com.percussion.workbench.ui.validators.PSControlValidatorFactory;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import java.util.ArrayList;
import java.util.List;

/**
 * Composite that contains the parent category control and functionality
 * around that control.
 */
public class PSViewParentCategoryComposite extends Composite
implements
   IPSDesignerObjectUpdater, IPSUiConstants
{
   
   
   /**
    * Ctor
    * @param parent the parent composite, may be <code>null</code>.
    * @param updater the editor or wizard page. Cannot
    * be <code>null</code>.
    * @param isLabelOnTop if <code>true</code>, then label will be
    * above the combo, else it will be on the left side of the combo.
    */
   public PSViewParentCategoryComposite(Composite parent,
      IPSDesignerObjectUpdater updater, boolean isLabelOnTop)
   {
      super(parent, SWT.NONE);
      if(updater == null)
         throw new IllegalArgumentException("editor cannot be null"); //$NON-NLS-1$
      setLayout(new FormLayout());
      
      PSControlValidatorFactory vFactory = 
         PSControlValidatorFactory.getInstance();
      IPSControlValueValidator required = vFactory.getRequiredValidator();
      
      final Label m_parentCategoryLabel = new Label(this, SWT.NONE);
      final FormData formData_7 = new FormData();      
      if(isLabelOnTop)
         formData_7.top = new FormAttachment(0, 0);
      else
         formData_7.top = new FormAttachment(0, 
            LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET);
      
      formData_7.left = new FormAttachment(0, 0);
      m_parentCategoryLabel.setLayoutData(formData_7);
      m_parentCategoryLabel.setText(
         PSMessages.getString("PSViewParentCategoryComposite.parentCategory.label"));  //$NON-NLS-1$

      m_parentCategoryComboViewer = new ComboViewer(this, SWT.READ_ONLY);
      m_parentCategoryComboViewer.setContentProvider(
         new PSDefaultContentProvider());
      m_parentCategoryComboViewer.setLabelProvider(
         new LabelProvider());
      final Combo combo = m_parentCategoryComboViewer.getCombo();
      final FormData formData_8 = new FormData();     
      formData_8.right = new FormAttachment(100, 0);
      if(isLabelOnTop)
      {
         formData_8.top = new FormAttachment(m_parentCategoryLabel,
            0, SWT.BOTTOM);
         formData_8.left = new FormAttachment(m_parentCategoryLabel,
            0, SWT.LEFT);
      }
      else
      {
         formData_8.top = new FormAttachment(m_parentCategoryLabel,
            -LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, SWT.TOP);
         formData_8.left = new FormAttachment(m_parentCategoryLabel,
            LABEL_VSPACE_OFFSET, SWT.RIGHT);
      }
      combo.setLayoutData(formData_8);
      
      if(updater instanceof PSEditorBase)
      {
         ((PSEditorBase)updater).registerControl(
            "PSViewParentCategoryComposite.parentCategory.label", //$NON-NLS-1$
            combo,
            new IPSControlValueValidator[]{required});         
      }
      else if (updater instanceof PSWizardPageBase)
      {
         ((PSWizardPageBase)updater).registerControl(
            "PSViewParentCategoryComposite.parentCategory.label", //$NON-NLS-1$
            combo,
            new IPSControlValueValidator[]{required});
         loadCategoryChoices();
      }
      
   }
   
   /**
    * Returns the category combo viewer for this composite.
    * @return never <code>null</code>.
    */
   public ComboViewer getComboViewer()
   {
      return m_parentCategoryComboViewer;
   }

   /* 
    * @see com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater#
    * updateDesignerObject(java.lang.Object, java.lang.Object)
    */
   public void updateDesignerObject(Object designObject, Object control)
   {
      PSSearch view = (PSSearch)designObject;
      if(control == m_parentCategoryComboViewer.getCombo())
      {
         int selection = 
            m_parentCategoryComboViewer.getCombo().getSelectionIndex();
         if(selection == -1)
            selection = 0; // default to my content if no selection
         view.setParentCategory(selection + 1);
      }
      
   }

   /* 
    * @see com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater#
    * loadControlValues(java.lang.Object)
    */
   public void loadControlValues(Object designObject)
   {
      PSSearch view = (PSSearch)designObject;
      loadCategoryChoices();
      m_parentCategoryComboViewer.getCombo().select(
         view.getParentCategory() - 1);
   }
   
   /**
    * Loads the category choices and sets the default category
    * of my content.
    */
   private void loadCategoryChoices()
   {
      m_parentCategoryComboViewer.setInput(ms_categories);
      m_parentCategoryComboViewer.getCombo().select(0);
   }
      
   /**
    * The combo viewer, initalized in the ctor, never <code>null</code> 
    * after that.
    */
   private ComboViewer m_parentCategoryComboViewer;
   
   /*
    * Initialize up categories list
    */    
   private static final List<String> ms_categories = new ArrayList<String>(4);
   static
   {
      ms_categories.add(
         PSMessages.getString(
         "PSViewParentCategoryComposite.category.myContent.choice")); //$NON-NLS-1$
      ms_categories.add(
         PSMessages.getString(
         "PSViewParentCategoryComposite.category.commContent.choice")); //$NON-NLS-1$
      ms_categories.add(
         PSMessages.getString(
         "PSViewParentCategoryComposite.category.allContent.choice")); //$NON-NLS-1$
      ms_categories.add(
         PSMessages.getString(
         "PSViewParentCategoryComposite.category.otherContent.choice")); //$NON-NLS-1$
   }
}
