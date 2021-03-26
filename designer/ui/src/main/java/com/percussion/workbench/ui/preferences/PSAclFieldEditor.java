/*******************************************************************************
 *
 * [ PSAclFieldEditor.java ]
 * 
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.workbench.ui.preferences;

import com.percussion.client.PSSecurityUtils;
import com.percussion.services.security.IPSAcl;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.PSWorkbenchPlugin;
import com.percussion.workbench.ui.security.PSAclComposite;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

/**
 * This field editor control facilitates configuration of default Access Control
 * List. We could have used the available individual field editor controls from
 * eclipse. However, it is much easier and better controllable if we
 * encapsulated all the settings into one data object and corresponding control
 * and hence this control and the data class
 * {@link com.percussion.services.security.data.PSAclImpl}.
 */
public class PSAclFieldEditor extends FieldEditor
{
   /**
    * Default ctor, just invokes the base class version.
    */
   public PSAclFieldEditor()
   {
      super();
   }

   /**
    * Another ctor, just invokes the base class version.
    * 
    * @see FieldEditor#FieldEditor(java.lang.String, java.lang.String,
    * org.eclipse.swt.widgets.Composite)
    */
   public PSAclFieldEditor(String name, String labelText, Composite parent)
   {
      super(name, labelText, parent);
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.jface.preference.FieldEditor#adjustForNumColumns(int)
    */
   @Override
   protected void adjustForNumColumns(@SuppressWarnings("unused") int numColumns)
   {
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.jface.preference.FieldEditor#createControl(org.eclipse.swt.widgets.Composite)
    */
   @Override
   protected void createControl(Composite parent)
   {
      GridData data = (GridData) parent.getLayoutData();
      data.grabExcessVerticalSpace = true;
      data.verticalAlignment = GridData.FILL;
      FormLayout layout = new FormLayout();
      parent.setLayout(layout);
      doFillIntoGrid(parent, 1);
   }

   @Override
   protected void doFillIntoGrid(Composite parent,
         @SuppressWarnings("unused") int numColumns)
   {
      m_aclComposite = new PSAclComposite(parent, SWT.NONE,
            PSMessages.getString(
                  "PSAclFieldEditor.description.defaultObjectAcl"));   //$NON-NLS-1$

      final FormData formData = new FormData();
      formData.left = new FormAttachment(0, -5);
      formData.right = new FormAttachment(100, 0);
      formData.top = new FormAttachment(0, -5);
      formData.bottom = new FormAttachment(100, 0);
      m_aclComposite.setLayoutData(formData);
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.jface.preference.FieldEditor#doLoad()
    */
   @Override
   protected void doLoad()
   {
      IPSAcl acl = PSWorkbenchPlugin.getDefault().getDefaultAcl();
      m_aclComposite.setAcl(acl, true);
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.jface.preference.FieldEditor#doLoadDefault()
    */
   @Override
   protected void doLoadDefault()
   {
      m_aclComposite.setAcl(PSSecurityUtils.createNewAcl(), true);
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.jface.preference.FieldEditor#doStore()
    */
   @Override
   protected void doStore()
   {
      PSWorkbenchPlugin.getDefault().saveDefaultAcl(m_aclComposite.getAcl());
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.jface.preference.FieldEditor#getNumberOfControls()
    */
   @Override
   public int getNumberOfControls()
   {
      // Always 1
      return 1;
   }

   /**
    * Getter for the composite this uses.
    * 
    * @return never <code>null</code>.
    */
   public PSAclComposite getAclComposite()
   {
      return m_aclComposite;
   }

   /**
    * The composite that actually renders the data. Initialized in
    * {@link #doFillIntoGrid(Composite, int)}. Never <code>null</code> after
    * that.
    */
   protected PSAclComposite m_aclComposite;
}
