/******************************************************************************
 *
 * [ PreferenceInitializer.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.preferences;

import com.percussion.workbench.ui.PSWorkbenchPlugin;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer
{

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
    */
   public void initializeDefaultPreferences()
   {
      IPreferenceStore store = PSWorkbenchPlugin.getDefault()
         .getPreferenceStore();
      store.setDefault(PreferenceConstants.P_BOOLEAN, true);
      store.setDefault(PreferenceConstants.P_CHOICE, "choice2");
      store.setDefault(PreferenceConstants.P_STRING, "Default value");
   }

}
