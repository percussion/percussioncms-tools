/******************************************************************************
 *
 * [ PSRhythmyxPreferences.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.client.preferences;

import org.apache.commons.lang.StringUtils;

/**
 * A simple data bean that is usable in the UI implementation of the Rhythmyx
 * general preferences.
 * It is made sure to serializable ad deserializable using
 * {@link com.percussion.xml.serialization.PSObjectSerializer}.
 */
public class PSRhythmyxPreferences
{
   /**
    * Default ctor. Does nothing.
    */
   public PSRhythmyxPreferences()
   {
   }

   /**
    * @return <code>true</code> if the option to connect to server
    * automatically on opening the workbench is on, <code>false</code>
    * otherwise.
    */
   public boolean isAutoConnectOnOpen()
   {
      return m_autoConnectOnOpen;
   }
   
   /**
    * @return <code>true</code> if the field problems view should
    * automatically be opened upon field error.
    */
   public boolean isAutoOpenProblemsView()
   {
      return m_autoOpenProblemsView;
   }

   /**
    * Set the the option to connect to server automatically on opening the
    * workbench
    * 
    * @param autoConnectOnOpen <code>true</code> to auto connect,
    * <code>false</code> otherwise.
    */
   public void setAutoConnectOnOpen(boolean autoConnectOnOpen)
   {
      m_autoConnectOnOpen = autoConnectOnOpen;
   }
   
   /**
    * Set the the option to open field problems view automatically
    * 
    * @param autoOpenProblems <code>true</code> to auto connect,
    * <code>false</code> otherwise.
    */
   public void setAutoOpenProblemsView(boolean autoOpenProblems)
   {
      m_autoOpenProblemsView = autoOpenProblems;
   }

   /**
    * @return the default date format, naver <code>null</code> or empty.
    */
   public String getDefaultDateFormat()
   {
      return m_defaultDateFormat;
   }

   /**
    * Set the default date format.
    * 
    * @param defaultDateFormat must not be <code>null</code> or empty. No
    * validation is done on the supplied value.
    */
   public void setDefaultDateFormat(String defaultDateFormat)
   {
      if (StringUtils.isEmpty(defaultDateFormat))
         m_defaultDateFormat = DEFAULT_DATE_FORMAT;
      else
         m_defaultDateFormat = defaultDateFormat;
   }

   /**
    * Is the option to show the deprecated functionality on?
    * 
    * @return <code>true</code> if the deprecated funationality to be hidden,
    * <code>false</code> otherwise.
    */
   public boolean isShowDeprecatedFunctionality()
   {
      return m_showDeprecatedFunctionality;
   }

   /**
    * Set the option to hide or show deprecated functionality in the workbench.
    * 
    * @param showDeprecatedFunctionality <code>true</code> to show,
    * <code>false</code> otherwise.
    */
   public void setShowDeprecatedFunctionality(
      boolean showDeprecatedFunctionality)
   {
      m_showDeprecatedFunctionality = showDeprecatedFunctionality;
   }

   /**
    * Is the option to show advanced functionality in the workbench on?
    * 
    * @return <code>true</code> if the advanced funationality to be shown,
    * <code>false</code> otherwise.
    */
   public boolean isShowAdvancedFunctionality()
   {
      return m_showAdvancedFunctionality;
   }

   /**
    * Set the option to show or hide advanced functionality in the workbench.
    * 
    * @param showAdvancedFunctionality <code>true</code> to show,
    * <code>false</code> otherwise.
    */
   public void setShowAdvancedFunctionality(boolean showAdvancedFunctionality)
   {
      m_showAdvancedFunctionality = showAdvancedFunctionality;
   }

   /**
    * Is the option to show warning when opening read only objects in the
    * workbench on?
    * 
    * @return <code>true</code> if the warning to be displayed when opening
    * read only objects in the workbench, <code>false</code> otherwise.
    */
   public boolean isShowWarningForReadOnlyObjects()
   {
      return m_showWarningForReadOnlyObjects;
   }

   /**
    * Set the option to show warning when opening read only objects in the
    * workbench.
    * 
    * @param showWarningForReadOnlyObjects <code>true</code> if the warning to
    * be displayed when opening read only objects in the workbench,
    * <code>false</code> otherwise.
    */
   public void setShowWarningForReadOnlyObjects(
      boolean showWarningForReadOnlyObjects)
   {
      m_showWarningForReadOnlyObjects = showWarningForReadOnlyObjects;
   }

   
   /**
    * Is the option to show legacy interfaces for extensions in a content type
    * on? By default on a fresh install, this is checked off, meaning show the
    * extensions only with the specified interfaces
    * 
    * @return <code>true</code> if checked on, <code>false</code> otherwise.
    */
   public boolean isShowLegacyInterfacesForExtns()
   {
      return m_showLegacyInterfacesForExtns;
   }

   /**
    * Set the option to show legacy interfaces for extensions in a 
    * content type
    * 
    * @param showAll <code>true</code> to show all the legacy interfaces for 
    * extensions in a content type <code>false</code> otherwise.
    */
   public void setShowLegacyInterfacesForExtns(boolean showAll)
   {
      m_showLegacyInterfacesForExtns = showAll;
   }

   /**
    * @see #isShowAdvancedFunctionality()
    * @see #setShowAdvancedFunctionality(boolean)
    */
   private boolean m_showAdvancedFunctionality = false;

   /**
    * @see #isShowWarningForReadOnlyObjects()
    * @see #setShowWarningForReadOnlyObjects(boolean)
    */
   private boolean m_showWarningForReadOnlyObjects = true;

   /**
    * @see #isShowDeprecatedFunctionality()
    * @see #setShowDeprecatedFunctionality(boolean)
    */
   private boolean m_showDeprecatedFunctionality = false;

   /**
    * @see #isAutoConnectOnOpen()
    * @see #setAutoConnectOnOpen(boolean)
    */
   private boolean m_autoConnectOnOpen = true;
   
   /**
    * @see #isAutoOpenProblemsView()
    * @see #setAutoOpenProblemsView(boolean)
    */
   private boolean m_autoOpenProblemsView = true;

   /**
    * @see #getDefaultDateFormat()
    * @see #setDefaultDateFormat(String)
    */
   private String m_defaultDateFormat = DEFAULT_DATE_FORMAT;

   /**
    * @see #isShowLegacyInterfacesForExtns()
    * @see #setShowLegacyInterfacesForExtns(boolean)
    */
   private boolean m_showLegacyInterfacesForExtns = false;

   /**
    * Default date format. Used as a fallback. 
    */
   static private final String DEFAULT_DATE_FORMAT = "mm/dd/yyyy"; //$NON-NLS-1$
}
