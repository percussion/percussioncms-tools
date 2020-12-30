/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.htmlConverter;

import java.awt.Color;

import javax.swing.JTextArea;

/**
 * Provides an editor component with addtional functionality used to describe
 * a tab in the splitter.
 */
public abstract class TabComponent extends JTextArea
{
   /**
    * Construct a text editor component and initialize it accoring to the
    * provided parameters.
    *
    * @param title the tab title displayed for the user.
    * @param id this string is used as an identifier among multiple tab's. 
    *    The owner of this tab component is responsible to provid a unique id.
    * @param toggleAction the toggle action command string for this tab.
    * @param hideable is this tab hidable.
    * @param saveable is this tabs file saveable.
    * @param loadable is this tabs file loadable.
    */
   public TabComponent(String title, String id, String toggleAction,
                       boolean hideable, boolean saveable, boolean loadable)
   {
      if (title == null || title.equals(""))
         throw new IllegalArgumentException("A valid title for this tab must be provided!");

      m_title = title;
      m_id = id;
      m_toggleAction = toggleAction;
      m_hideable = hideable;
      m_saveable = saveable;
      m_loadable = loadable;
      
      this.setTabSize(2);
   }
   
   /**
    * Clears the content and other properties associated with the content.
    */
   public void clear()
   {
      setText("");
      m_fileName = null;
      m_dirty = false;
   }

   /**
    * Set the tabs tooltip text.
    *
    * @param tooltip the new tooltip for this.
    */
   public void setTooltip(String tooltip)
   {
      m_tooltip = tooltip;
   }

   /**
    * Get the tabs tooltip text.
    *
    * @return the tooltip text.
    */
   public String getTooltip()
   {
      return m_tooltip;
   }

   /**
    * Get toggle action command string.
    *
    * @return the toggle action command.
    */
   public String getToggleAction()
   {
      return m_toggleAction;
   }

   /**
    * Get the tabs id.
    *
    * @return the tab id.
    */
   public String getId()
   {
      return m_id;
   }

   /**
    * Set the name of the currently loaded file.
    *
    * @param the file name
    */
   public void setFileName(String fileName)
   {
      m_fileName = fileName;
   }

   /**
    * Get the name of the currently loaded file.
    *
    * @return the file name
    */
   public String getFileName()
   {
      return m_fileName;
   }

   /**
    * Get the file suffix used in this tab.
    *
    * @return the file suffix.
    */
   public String getFileSuffix()
   {
      return m_fileSuffix;
   }

   /**
    * Get the file extensions used in this tab.
    *
    * @return the file extensions.
    */
   public String[] getFileExtensions()
   {
      return m_extensions;
   }

   /**
    * Get the file descroptor used in this tab.
    *
    * @return the file descriptor.
    */
   public String getFileDescriptor()
   {
      return m_fileDescriptor;
   }

   /**
    * Get the status if this is hideable or not.
    *
    * @return the hideable status.
    */
   public boolean isHideable()
   {
      return m_hideable;
   }

   /**
    * Get the status if this is saveable or not.
    *
    * @return the saveable status.
    */
   public boolean isSaveable()
   {
      return m_saveable;
   }

   /**
    * Get the status if this is loadable or not.
    *
    * @return the loadable status.
    */
   public boolean isLoadable()
   {
      return m_loadable;
   }

   /**
    * Set the tabs color.
    *
    * @param color the new tab color.
    */
   public void setColor(Color color)
   {
      m_color = color;
   }

   /**
    * Get the tabs color.
    *
    * @return the tab color.
    */
   public Color getColor()
   {
      return m_color;
   }

   /**
    * Get the tabs title.
    *
    * @return the tab title.
    */
   public String getTitle()
   {
      return m_title;
   }

   /**
    * Set file extensions to be used for the file chooser dialog within this
    * tab.
    *
    * @param extensions all valid file extensions for this tab.
    * @param fileDescriptor the description to be used for the file chooser 
    *    dialog within this tab.
    * @param fileSuffix the suffix to be added while saving this file.
    */
   public void setFileAttributes(String[] extensions,
                                 String fileDescriptor,
                                 String fileSuffix)
   {
      m_extensions = extensions;
      m_fileDescriptor = fileDescriptor;
      m_fileSuffix = fileSuffix;
   }

   /**
    * The user visible name of the tab. Should be internationalized. Should
    * not be null.
    */
   private String m_title = null;

   /**
    * The id of the tab used by the program. It must be unique among all tab 
    * components within the container of this tab component.
    */
   private String m_id = null;

   /**
    * Contains a list of strings containing default extensions for files loaded
    * into/ saved from this tab. If more than 1 extension is present, the first
    * one is used as the default file extension. If null, no default extension
    * is used. Extensions should not include the '.'
    */
   private String[] m_extensions = null;
   
   /**
    * The file descriptor to be used for the file chooser dialog within this
    * tab.
    */
   private String m_fileDescriptor = null;

   /**
    * The file suffix to be added while saving this file.
    */
   private String m_fileSuffix = null;
   
   /**
    * The color of the tab. If null, the default color is used.
    */
   private Color m_color = null;

   /**
    * The tooltip to display for this tab. If null, no tip will be shown.
    */
   private String m_tooltip = null;

   /**
    * The toggle action command string.
    */
   private String m_toggleAction = null;

   /**
    * The filename of the currently loaded file for this tab.
    */
   private String m_fileName = null;

   /**
    * Can the contents of this tab be saved? If <code>false</code>, the "save as"
    * menu item will be grayed when this tab is active and this tab's name
    * will not appear on the Save all dialog.
    */
   private boolean m_saveable = true;

   /**
    * Can the contents of this tab be loaded?
    */
   private boolean m_loadable = true;

   /**
    * The status whether this tab is hideable (<CODE>true</CODE>) or not.
    */
   private boolean m_hideable = false;

   /**
    * If <code>true</code>, the content has changed since the last save or 
    * clear.
    */
   private boolean m_dirty = false;
}
