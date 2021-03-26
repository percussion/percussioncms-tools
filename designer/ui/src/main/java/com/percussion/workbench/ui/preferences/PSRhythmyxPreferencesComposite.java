/******************************************************************************
 *
 * [ PSRhythmyxPreferencesComposite.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.workbench.ui.preferences;

import com.percussion.client.preferences.PSRhythmyxPreferences;
import com.percussion.workbench.ui.PSMessages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

/**
 * This composite encapsulates and renders the data object of
 * {@link com.percussion.client.preferences.PSRhythmyxPreferences} and is
 * intended to be part of the field editor
 * {@link com.percussion.workbench.ui.preferences.PSRhythmyxPreferenceFieldEditor}.
 */
public class PSRhythmyxPreferencesComposite extends Composite
{
   /**
    * Ctor that take sthe parent composite and the style. Renders the data
    * returned by {@link #getPreferences()} which will be the default data for
    * the object.
    * 
    * @see Composite#Composite(org.eclipse.swt.widgets.Composite, int)
    */
   public PSRhythmyxPreferencesComposite(Composite parent, int style)
   {
      super(parent, style);
      setLayout(new FormLayout());

      m_showWarningOnButton = new Button(this, SWT.CHECK);
      final FormData formData_2 = new FormData();
      formData_2.top = new FormAttachment(0, 5);
      formData_2.left = new FormAttachment(0, 5);
      m_showWarningOnButton.setLayoutData(formData_2);
      m_showWarningOnButton
         .setText(PSMessages
            .getString("PSRhythmyxPreferencesComposite.Show_warning_on_opening_of_a_read-only_object")); //$NON-NLS-1$

      m_showDeprecatedFunctionalityButton = new Button(this, SWT.CHECK);
      final FormData formData_3 = new FormData();
      formData_3.top = new FormAttachment(m_showWarningOnButton, 0);
      formData_3.left = new FormAttachment(0, 5);
      m_showDeprecatedFunctionalityButton.setLayoutData(formData_3);
      m_showDeprecatedFunctionalityButton
         .setText(PSMessages
            .getString("PSRhythmyxPreferencesComposite.Show_deprecated_functionality")); //$NON-NLS-1$

      m_automaticallyConnectOnButton = new Button(this, SWT.CHECK);
      final FormData formData_4 = new FormData();
      formData_4.top = new FormAttachment(m_showDeprecatedFunctionalityButton,
         0);
      formData_4.left = new FormAttachment(0, 5);
      m_automaticallyConnectOnButton.setLayoutData(formData_4);
      m_automaticallyConnectOnButton
         .setText(PSMessages
            .getString(
               "PSRhythmyxPreferencesComposite.Automatically_connect_when_workbench_opens")); //$NON-NLS-1$

      m_automaticallyOpenProblemsButton = new Button(this, SWT.CHECK);
      final FormData formData_7 = new FormData();
      formData_7.top = new FormAttachment(m_automaticallyConnectOnButton,
         0);
      formData_7.left = new FormAttachment(0, 5);
      m_automaticallyOpenProblemsButton.setLayoutData(formData_7);
      m_automaticallyOpenProblemsButton
         .setText(PSMessages.getString(
            "PSRhythmyxPreferencesComposite.autoOpenProblemsView"));  //$NON-NLS-1$
      
      m_showLegacyInterfacesForExtns = new Button(this, SWT.CHECK);
      final FormData formData_8 = new FormData();
      formData_8.top = new FormAttachment(m_automaticallyOpenProblemsButton, 0);
      formData_8.left = new FormAttachment(0, 5);
      m_showLegacyInterfacesForExtns.setLayoutData(formData_8);
      m_showLegacyInterfacesForExtns.setText(PSMessages
            .getString("PSRhythmyxPreferencesComposite."
                  + "ShowAllExtensionsInFieldProperties"));
      
      setData(m_prefs);
   }

   /**
    * Set the data object to render in the UI. The data from the object is
    * rendered and UI is refreshed to reflect the changes.
    * 
    * @param prefs must not be <code>null</code>.
    */
   public void setPreferences(PSRhythmyxPreferences prefs)
   {
      if (prefs == null)
      {
         throw new IllegalArgumentException("prefs must not be null"); //$NON-NLS-1$
      }
      m_prefs = prefs;
      // Refersh the UI
      objectToUi();
   }

   /**
    * Get the data object rendered by this UI. The data object is built off the
    * current state if the UI.
    * 
    * @return preference data m never <code>null</code>.
    */
   public PSRhythmyxPreferences getPreferences()
   {
      uiToObject();
      return m_prefs;
   }

   /**
    * Helper method to read the settings to the data object from UI.
    */
   private void uiToObject()
   {
      m_prefs.setAutoConnectOnOpen(m_automaticallyConnectOnButton
         .getSelection());
      m_prefs
         .setShowDeprecatedFunctionality(m_showDeprecatedFunctionalityButton
            .getSelection());
      m_prefs.setShowWarningForReadOnlyObjects(m_showWarningOnButton
         .getSelection());
      m_prefs.setAutoOpenProblemsView(m_automaticallyOpenProblemsButton
         .getSelection());
      m_prefs.setShowLegacyInterfacesForExtns(
            m_showLegacyInterfacesForExtns.getSelection());
   }

   /**
    * Helper method to render the settings from the data object to the UI.
    */
   private void objectToUi()
   {
      m_showWarningOnButton.setSelection(m_prefs
         .isShowWarningForReadOnlyObjects());
      m_showDeprecatedFunctionalityButton.setSelection(m_prefs
         .isShowDeprecatedFunctionality());
      m_automaticallyConnectOnButton
         .setSelection(m_prefs.isAutoConnectOnOpen());
      m_automaticallyOpenProblemsButton.setSelection(
         m_prefs.isAutoOpenProblemsView());
      m_showLegacyInterfacesForExtns.setSelection(m_prefs
            .isShowLegacyInterfacesForExtns());
   }

   @Override
   public void dispose()
   {
      super.dispose();
      m_prefs = null;
   }

   @Override
   protected void checkSubclass()
   {
   }

   /**
    * The preference data object, initialized with the default ctor and is
    * changeable using {@link #setData(PSRhythmyxPreferences)}. Never
    * <code>null</code>.
    * 
    * @see #getPreferences()
    * @see #setPreferences(PSRhythmyxPreferences)
    */
   private PSRhythmyxPreferences m_prefs = new PSRhythmyxPreferences();

   // All controls used to render the data object.
   private Button m_showWarningOnButton;

   private Button m_showDeprecatedFunctionalityButton;

   private Button m_automaticallyConnectOnButton;
   
   private Button m_automaticallyOpenProblemsButton;
   
   private Button m_showLegacyInterfacesForExtns;
}
