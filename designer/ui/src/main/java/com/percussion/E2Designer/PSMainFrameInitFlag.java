/******************************************************************************
 *
 * [ PSMainFrameInitFlag.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.E2Designer;

import com.percussion.workbench.ui.legacy.PSLegacyInitialzer;

/**
 * Holds indicator whether main frame is initialized or not.
 * Extracting this flag to a separate class helps to severe dependency of
 * {@link com.percussion.E2Designer.PSDialog} on Eclipse classes.
 *
 * @author Andriy Palamarchuk
 */
public class PSMainFrameInitFlag
{
   /**
    * Returns <code>true</code> if main frame is initialized.
    */
   public static boolean isMainFrameInitialized()
   {
      return PSLegacyInitialzer.ms_initializeRequired
         && E2Designer.getApp() != null
         && E2Designer.getApp().getMainFrame() != null;
   }
}
