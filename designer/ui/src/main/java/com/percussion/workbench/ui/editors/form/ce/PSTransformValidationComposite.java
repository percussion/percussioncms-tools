/******************************************************************************
*
* [ PSTransformValidationComposite.java ]
*
* COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
* All rights reserved. This material contains unpublished, copyrighted
* work including confidential and proprietary information of Percussion.
*
*****************************************************************************/
package com.percussion.workbench.ui.editors.form.ce;

import com.percussion.workbench.ui.IPSUiConstants;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater;
import com.percussion.workbench.ui.editors.form.PSEditorBase;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;

public class PSTransformValidationComposite extends Composite
      implements
         IPSUiConstants,
         IPSDesignerObjectUpdater
{

   public PSTransformValidationComposite(Composite parent, int style,
         PSEditorBase editor)
   {
      super(parent, style);
      setLayout(new FormLayout());

      //Create the tab folder and tab otems and add them to it.
      m_tabFolder = new CTabFolder(this,SWT.BORDER);
      m_itTabItem = new CTabItem(m_tabFolder, SWT.BORDER);
      m_otTabItem = new CTabItem(m_tabFolder, SWT.BORDER);
      m_valTabItem =  new CTabItem(m_tabFolder, SWT.BORDER);
      m_preTabItem = new CTabItem(m_tabFolder, SWT.BORDER);
      m_postTabItem =  new CTabItem(m_tabFolder, SWT.BORDER);
      m_itTabItem.setText(PSMessages.getString(
         "PSTransformValidationComposite.extensions.tab.inputTransforms.title"));
      m_otTabItem.setText(PSMessages.getString(
         "PSTransformValidationComposite.extensions.tab.outputTransforms.title"));
      m_valTabItem.setText(PSMessages.getString(
         "PSTransformValidationComposite.extensions.tab.validations.title"));
      m_preTabItem.setText(PSMessages.getString(
         "PSTransformValidationComposite.extensions.tab.preExits.title"));
      m_postTabItem.setText(PSMessages.getString(
         "PSTransformValidationComposite.extensions.tab.postExits.title"));
      
      final FormData formData = new FormData();
      formData.left = new FormAttachment(0, 0);
      formData.right = new FormAttachment(100, 0);
      formData.top = new FormAttachment(0, 0);
      m_tabFolder.setLayoutData(formData);
      
      m_itExtControl = new PSExtensionsControl(m_tabFolder, style, editor,
            PSExtensionsControl.INPUT_TRANSFORMS);
      m_itTabItem.setControl(m_itExtControl);
      
      m_otExtControl = new PSExtensionsControl(m_tabFolder, style, editor,
            PSExtensionsControl.OUTPUT_TRANSFORMS);
      m_otTabItem.setControl(m_otExtControl);

      m_valExtControl = new PSExtensionsControl(m_tabFolder, style, editor,
            PSExtensionsControl.VALIDATIONS);
      m_valTabItem.setControl(m_valExtControl);

      m_preExtControl = new PSExtensionsControl(m_tabFolder, style, editor,
            PSExtensionsControl.PRE_EXITS);
      m_preTabItem.setControl(m_preExtControl);

      m_postExtControl = new PSExtensionsControl(m_tabFolder, style, editor,
            PSExtensionsControl.POST_EXITS);
      m_postTabItem.setControl(m_postExtControl);

      m_tabFolder.setSelection(m_itTabItem);
   }

   public void dispose()
   {
      super.dispose();
   }

   protected void checkSubclass()
   {
   }

   public void updateDesignerObject(Object designObject, Object control)
   {
      m_itExtControl.updateDesignerObject(designObject,control);
      m_otExtControl.updateDesignerObject(designObject,control);
      m_valExtControl.updateDesignerObject(designObject,control);
      m_preExtControl.updateDesignerObject(designObject,control);
      m_postExtControl.updateDesignerObject(designObject,control);
   }

   public void loadControlValues(Object designObject)
   {
      m_itExtControl.loadControlValues(designObject);
      m_otExtControl.loadControlValues(designObject);
      m_valExtControl.loadControlValues(designObject);
      m_preExtControl.loadControlValues(designObject);
      m_postExtControl.loadControlValues(designObject);
   }
   //Controls
   private CTabFolder m_tabFolder;
   private CTabItem m_itTabItem;
   private CTabItem m_otTabItem;
   private CTabItem m_valTabItem;
   private CTabItem m_preTabItem;
   private CTabItem m_postTabItem;
   private PSExtensionsControl m_itExtControl;
   private PSExtensionsControl m_otExtControl;
   private PSExtensionsControl m_valExtControl;
   private PSExtensionsControl m_preExtControl;
   private PSExtensionsControl m_postExtControl;
   //Constants for tabs
   public static final String INPUT_TRANSFORMS_LABEL = "Input Transforms";
   public static final String OUTPUT_TRANSFORMS_LABEL = "Output Transforms";
   public static final String VALIDATIONS_LABEL = "Validations";
   public static final String PRE_EXITS_LABEL = "Output Transforms";
   public static final String POST_EXITS_LABEL = "Validations";

}
