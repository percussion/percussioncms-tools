/******************************************************************************
*
* [ PSContentTypeChildTab.java ]
*
* COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
* All rights reserved. This material contains unpublished, copyrighted
* work including confidential and proprietary information of Percussion.
*
*****************************************************************************/
package com.percussion.workbench.ui.editors.form.ce;

import com.percussion.design.objectstore.PSFieldSet;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.controls.PSSortableTable;
import com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater;
import com.percussion.workbench.ui.editors.form.PSEditorBase;
import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * Composite for the child field set that holds the mappings for the fields in
 * it.
 */
public class PSContentTypeChildTab extends Composite
      implements
         IPSDesignerObjectUpdater
{
   
   /**
    * Content type editor creates object of this class for each field set in it.
    * 
    * @param parent The parent composite, that holds this compoiste.
    * @param style The style for the composite.
    * @param editor Object of PSEditorBase used for showing and updating the
    *           data.
    * @param fieldSetName name of the child tab.
    */
   public PSContentTypeChildTab(Composite parent, int style,
         PSEditorBase editor, String fieldSetName) {
      super(parent, style);
      setLayout(new FormLayout());
      String tabletitle = PSMessages.getString("PSContentTypeChildTab.label.title") + " " + fieldSetName; //$NON-NLS-1$ //$NON-NLS-2$
      m_fieldsComp = new PSCEFieldsCommonComposite(this, SWT.NONE, editor,
            tabletitle, PSContentEditorDefinition.LOCALDEF_CHILD_EDITOR,
            fieldSetName);
      final FormData formData_10 = new FormData();
      formData_10.top = new FormAttachment(0, 0);
      formData_10.left = new FormAttachment(15, 0);
      formData_10.bottom = new FormAttachment(100, 0);
      formData_10.right = new FormAttachment(90, 0);
      m_fieldsComp.setLayoutData(formData_10);
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater#updateDesignerObject(java.lang.Object, java.lang.Object)
    */
   public void updateDesignerObject(Object designObject, Object control)
   {
      m_fieldsComp.updateDesignerObject(designObject,control);
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.workbench.ui.editors.common.IPSDesignerObjectUpdater#loadControlValues(java.lang.Object)
    */
   public void loadControlValues(Object designObject)
   {
      m_fieldsComp.loadControlValues(designObject);
   }

   /**
    * Sets the field set name.
    */
   public void setFieldSetName(String fieldSetName)
   {
      if(StringUtils.isBlank(fieldSetName))
         throw new IllegalArgumentException("fieldSetName can not be null"); //$NON-NLS-1$
      m_fieldsComp.setFieldSetName(fieldSetName);
   }
   
   /**
    * Gets the sortable table representing the rows of child table.
    * @return PSSortableTable sortable table representing the rows of child table.
    */
   public PSSortableTable getSortableTable()
   {
      return m_fieldsComp.getTableComp();
   }
   
   /**
    * Sets the database table name represented by the field set on to fields
    * composite. {@see PSCEFieldsCommonComposite#setDBTableName(PSFieldSet)}. 
    * @param set The fieldset from which the table name is extracted and set.
    */
   public void setDBTableName(PSFieldSet set)
   {
      m_fieldsComp.setDBTableName(set);
   }
   
   /**
    * Fields common composite control
    */
   private PSCEFieldsCommonComposite m_fieldsComp;

}
