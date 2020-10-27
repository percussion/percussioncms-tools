/*******************************************************************************
 *
 * [ PSRhythmyxPreferenceFieldEditor.java ]
 * 
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.workbench.ui.preferences;

import com.percussion.client.preferences.PSRhythmyxPreferences;
import com.percussion.workbench.ui.PSWorkbenchPlugin;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

/**
 * This field editor control manages all general preferences for Rhythmyx
 * workbench. We could have used the available individual field editor controls
 * from eclipse. However, it is much easier and better controllable if we
 * encapsulated all the settings into one data object and corresponding control
 * and hence this control and the data class
 * {@link com.percussion.client.preferences.PSRhythmyxPreferences}.
 */
public class PSRhythmyxPreferenceFieldEditor extends FieldEditor
{
   /**
    * Default ctor, just invokes the base class version.
    */
   public PSRhythmyxPreferenceFieldEditor()
   {
      super();
   }

   /**
    * Another ctor, just invokes the base class version.
    * 
    * @see FieldEditor#FieldEditor(java.lang.String, java.lang.String,
    * org.eclipse.swt.widgets.Composite)
    */
   public PSRhythmyxPreferenceFieldEditor(String name, String labelText,
      Composite parent)
   {
      super(name, labelText, parent);
   }

   @Override
   protected void adjustForNumColumns(int numColumns)
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
   protected void doFillIntoGrid(Composite parent, int numColumns)
   {
      m_prefsComposite = new PSRhythmyxPreferencesComposite(parent, SWT.NONE);
      final FormData formData = new FormData();
      formData.left = new FormAttachment(0, -5);
      formData.right = new FormAttachment(100, 0);
      formData.top = new FormAttachment(0, -5);
      formData.bottom = new FormAttachment(100, 0);
      m_prefsComposite.setLayoutData(formData);
   }

   @Override
   protected void doLoad()
   {
      PSRhythmyxPreferences prefs = PSWorkbenchPlugin.getDefault()
         .getPreferences();
      m_prefsComposite.setPreferences(prefs);
   }

   @Override
   protected void doLoadDefault()
   {
      PSRhythmyxPreferences prefs = new PSRhythmyxPreferences();
      // We may accept the defaults set by the default ctor.
      // Or, we may set here.
      m_prefsComposite.setPreferences(prefs);
      //Done setting default, now set the flag to store on apply or ok button
      setPresentsDefaultValue(false);
   }

   @Override
   protected void doStore()
   {
      PSWorkbenchPlugin.getDefault().savePreferences(
         m_prefsComposite.getPreferences());
   }

   @Override
   public int getNumberOfControls()
   {
      // Always 1
      return 1;
   }

   /**
    * The composite that actually renders the data. Initialized in
    * {@link #doFillIntoGrid(Composite, int)}. Never <code>null</code> after
    * that.
    */
   private PSRhythmyxPreferencesComposite m_prefsComposite;
}
