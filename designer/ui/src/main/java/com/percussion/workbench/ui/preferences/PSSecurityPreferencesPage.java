/******************************************************************************
 *
 * [ PSSecurityPreferencesPage.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.preferences;

import com.percussion.workbench.ui.PSWorkbenchPlugin;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * This class represents a preference page that is contributed to the
 * Preferences dialog. By subclassing <samp>FieldEditorPreferencePage</samp>,
 * we can use the field support built into JFace that allows us to create a page
 * that is small and knows how to save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They are stored in the
 * preference store that belongs to the main plug-in class. That way,
 * preferences can be accessed directly via the preference store.
 */

public class PSSecurityPreferencesPage extends FieldEditorPreferencePage
   implements IWorkbenchPreferencePage
{
   /**
    * Default ctor. Invokes base class ctor
    * {@link FieldEditorPreferencePage#FieldEditorPreferencePage(int)} with
    * {@link FieldEditorPreferencePage#FLAT} as value for the parameter. This
    * means we are not using the grid layout and the editor field is handled as
    * single component.
    * 
    */
   public PSSecurityPreferencesPage()
   {
      super(FLAT);
      setPreferenceStore(PSWorkbenchPlugin.getDefault().getPreferenceStore());
   }

   /**
    * Creates the field editors. Field editors are abstractions of the common
    * GUI blocks needed to manipulate various types of preferences. Each field
    * editor knows how to save and restore itself.
    */
   @Override
   public void createFieldEditors()
   {
      FieldEditor fe = new PSAclFieldEditor(getPreferenceName(), "", //$NON-NLS-1$
         getFieldEditorParent());
      addField(fe);
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
    */
   public void init(@SuppressWarnings("unused") IWorkbench workbench)
   {
   }

   /**
    * Access method for the preference name
    * 
    * @return name of the preference this preference page that is persisted to
    * preference store. Never <code>null</code> or empty.
    */
   public static String getPreferenceName()
   {
      return ms_prefName;
   }

   /**
    * See {@link #getPreferenceName()}.
    */
   private static String ms_prefName = PSSecurityPreferencesPage.class
      .getName()
      + ".defaultAcl"; //$NON-NLS-1$

}
