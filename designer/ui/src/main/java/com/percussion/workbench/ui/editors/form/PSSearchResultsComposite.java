/******************************************************************************
 *
 * [ PSSearchResultsComposite.java ]
 * 
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.workbench.ui.editors.form;

import com.percussion.client.IPSReference;
import com.percussion.cms.objectstore.PSSearch;
import com.percussion.workbench.ui.IPSUiConstants;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.controls.PSDefaultContentProvider;
import com.percussion.workbench.ui.controls.PSReferenceLabelProvider;
import com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater;
import com.percussion.workbench.ui.editors.common.PSMaxRowsSpinnerComposite;
import com.percussion.workbench.ui.util.PSUiUtils;
import com.percussion.workbench.ui.validators.IPSControlValueValidator;
import com.percussion.workbench.ui.validators.PSControlValidatorFactory;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * Contains the controls used to edit the search result proeprties.
 */
public class PSSearchResultsComposite extends Composite
   implements
      IPSDesignerObjectUpdater,
      IPSUiConstants
{
   /**
    * Construct the composite, creates the layout and controls.
    * 
    * @param parent The parent composite, may not be <code>null</code>.
    * @param style The style to construct, see 
    * {@link Composite#Composite(Composite, int)} for details.
    * @param editor The parent editor, may not be <code>null</code>.
    */
   public PSSearchResultsComposite(Composite parent, int style, 
      PSEditorBase editor)
   {
      super(parent, style);

      if(editor == null)
         throw new IllegalArgumentException("editor cannot be null."); //$NON-NLS-1$
      
      setLayout(new FormLayout());
      
      PSControlValidatorFactory vFactory = 
         PSControlValidatorFactory.getInstance();
      IPSControlValueValidator required = vFactory.getRequiredValidator();

      m_maxRowsSpinner = new PSMaxRowsSpinnerComposite(this);
      final FormData formData_3 = new FormData();
      formData_3.top = new FormAttachment(0, 0);
      formData_3.left = new FormAttachment(0, 0);
      m_maxRowsSpinner.setLayoutData(formData_3);
      editor.registerControl(
         "PSMaxRowsSpinnerComposite.label.max.rows",
         m_maxRowsSpinner,
         null);
      
      Label displayFormatLabel = new Label(this, SWT.WRAP);
      final FormData formData_8 = new FormData();
      formData_8.top = new FormAttachment(m_maxRowsSpinner,
         COMBO_VSPACE_OFFSET + LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET,
         SWT.BOTTOM);
      formData_8.left = new FormAttachment(0, 0);
      displayFormatLabel.setLayoutData(formData_8);
      displayFormatLabel.setText(
         PSMessages.getString("common.displayFormat.label")); //$NON-NLS-1$

      m_displayFormatComboViewer = new ComboViewer(this, SWT.READ_ONLY);
      m_displayFormatComboViewer.setContentProvider(
         new PSDefaultContentProvider());
      m_displayFormatComboViewer.setLabelProvider(
         new PSReferenceLabelProvider());
      m_displayFormatCombo = m_displayFormatComboViewer.getCombo();
      final FormData formData_9 = new FormData();
      formData_9.right = new FormAttachment(100, 0);
      formData_9.top = new FormAttachment(displayFormatLabel,
         -LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, SWT.TOP);
      formData_9.left = new FormAttachment(
         displayFormatLabel, LABEL_HSPACE_OFFSET, SWT.RIGHT);
      m_displayFormatCombo.setLayoutData(formData_9);
      m_displayFormatComboViewer.setInput(PSSearchEditor.getDisplayFormats());
      editor.registerControl(
         "common.displayFormat.label",
         m_displayFormatCombo,
         new IPSControlValueValidator[]{required});
      
      setTabList(new Control[]{
         m_maxRowsSpinner,
         m_displayFormatCombo});  
   }

   /* (non-Javadoc)
    * @see IPSDesignerObjectUpdater#updateDesignerObject(Object, Object)
    */
   public void updateDesignerObject(Object designObject, Object control)
   {
      PSSearch def = (PSSearch)designObject;
      if(control == m_displayFormatCombo)
      {
         int selection = m_displayFormatCombo.getSelectionIndex();
         IPSReference ref = 
            (IPSReference)m_displayFormatComboViewer.getElementAt(selection);
         int id = (int)ref.getId().longValue();
         def.setDisplayFormatId(String.valueOf(id));         
      }
      else if(control == m_maxRowsSpinner)
      {
         def.setMaximumNumber(m_maxRowsSpinner.getValue());
      }

   }

   /* (non-Javadoc)
    * @see IPSDesignerObjectUpdater#loadControlValues(Object)
    */
   public void loadControlValues(Object designObject)
   {
      PSSearch def = (PSSearch)designObject;

      // set display format
      if(!StringUtils.isBlank(def.getDisplayFormatId()))
      {
         int id = Integer.parseInt(def.getDisplayFormatId());         
         int selection =PSUiUtils.getReferenceIndexById(
            PSSearchEditor.getDisplayFormats(), id);
         m_displayFormatCombo.select(selection);
      }
      else
      {
         m_displayFormatCombo.select(0);
      }
      //set max rows 
      m_maxRowsSpinner.setValue(def.getMaximumResultSize());
   }
   
   /**
    * Control to allow selection of the display format, never <code>null</code> 
    * or modified after ctor.
    */
   private Combo m_displayFormatCombo;
   
   /**
    * Viewer for the {@link #m_displayFormatCombo} to allow the model to be
    * backed with reference objects, never <code>null</code> or modified after
    * ctor.
    */
   private ComboViewer m_displayFormatComboViewer;
   
   /**
    * Controls to allow the user to specify the max rows settings, never
    * <code>null</code> or modified after ctor.
    */
   private PSMaxRowsSpinnerComposite m_maxRowsSpinner;
}

