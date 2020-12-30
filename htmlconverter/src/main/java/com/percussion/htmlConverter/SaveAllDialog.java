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

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.JFileChooser;
import javax.swing.UIManager;


/**
 * This is an extension of the standard file saver. It allows saving to an
 * arbitrary number of files that is determined by the user.
**/
public class SaveAllDialog extends JFileChooser
{
   /**
    * The name of the class that implements the look for this component.
   **/
   private static String m_uiClassID = "FileSaveAllUI";
   static   {
      /* Register the UI handler for this class. Normally, this would be done by
         the L&F, but I don't want to create a new L&F, just add this dialog. */
      UIManager.put( m_uiClassID, "com.percussion.htmlConverter.FileSaveAllUI" );
   }
   /**
    * Creates a new dialog that can save all the files specified in the supplied
    * array. The dialog is built based on this info. <p>
    * The top of the dialog looks similar to the standard file chooser, while the
    * bottom of the dlg lists each file in the supplied array with a checkbox and
    * text field.
    *
    * @param directory The default directory to use. All relative file names are relative
    * to this directory.
    *
    * @param filesToSave An array of objects containing the information about each
    * file to save.
   **/
   public SaveAllDialog( String directory, SaveInfo [] filesToSave )
   {
      super(directory);
      if ( null == filesToSave || 0 == filesToSave.length )
         throw new IllegalArgumentException( "Must have at least 1 file info." );
      m_saveLabels = new String[filesToSave.length];
      for ( int i = 0; i < filesToSave.length; ++i )
         m_saveLabels[i] = filesToSave[i].m_label;
      m_filesToSave = filesToSave;
      ((FileSaveAllUI) getUI()).initUI( this, m_saveLabels );
      // set all the defaults
      FileSaveAllUI ui = (FileSaveAllUI) getUI();
      Hashtable extMap = new Hashtable();
      for ( int i = 0; i < filesToSave.length; ++i )
      {
         ui.setSelected( filesToSave[i].m_label, filesToSave[i].m_defaultSave );
         ui.setFileName( filesToSave[i].m_label, filesToSave[i].m_defaultFilename );
         // we know all extensions have a description, this could break in the future
         if ( null != filesToSave[i].m_fileTypeDesc )
            extMap.put( filesToSave[i].m_fileTypeDesc, filesToSave[i].m_extensions );
      }

      Enumeration keys = extMap.keys();
      while ( keys.hasMoreElements())
      {
         String desc = (String) keys.nextElement();
         String [] extensions = (String []) extMap.get( desc );
         super.addChoosableFileFilter( new HTMLFileFilter( extensions, desc ));
      }
   }

   /**
    * Contains details about a file that needs to be saved.
   **/
   public static class SaveInfo
   {
      /**
       * The text that will be displayed in the dialog next to the filename edit
       * box. Cannot be null.
      **/
      public String m_label = null;

      /**
       * If <code>true</code>, the checkbox will be checked when the dialog is
       * first displayed, otherwise, it won't be checked. By default it is
       * <code>false</code>.
      **/
      public boolean m_defaultSave = false;

      /**
       * This name will appear in the filename text field if not null. If null,
       * the text field will start out empty. Typically this will be the filename
       * w/o the path.
      **/
      public String m_defaultFilename = null;

      /**
       * The extensions associated with this file. They will be added to the list
       * of extensions. May be null.
      **/
      public String [] m_extensions = null;

      /**
       * The name to use for the extensions provided, null if there is none.
      **/
      public String m_fileTypeDesc = null;
   }

   /**
    * Should be called after the dialog has been dismissed by the "Save" button. Used to
    * determine if the file w/ the supplied identifier should be saved or not. If
    * <code>true</code> is returned, then <a href="#getFile">getFile</a> can be
    * called to obtain the File object.
    *
    * @param label A label that was supplied in one of the SaveInfo objects passed to
    * the ctor.
    *
    * @return <code>true</code> if the user checked the checkbox identified by
    * the supplied label, <code>false</code> otherwise
   **/
   public boolean isSaveChecked( String label )
   {
      return ((FileSaveAllUI) getUI()).isSelected( label );
   }

   public File getFile(String label)
      throws FileNotFoundException
   {
      String filename = (( FileSaveAllUI)getUI()).getFileName(label).trim();
      if( null == filename )
         throw new IllegalArgumentException( "Invalid label supplied" );
      if ( 0 == filename.trim().length() )
         throw new FileNotFoundException( "No file specified" );
      String path;
      if( filename.charAt(0) == File.separatorChar || filename.charAt(1) == ':' )
         path = filename;
      else
         path = getCurrentDirectory().getAbsolutePath() + File.separatorChar + filename;
      return new File( path );
   }

   /**
    * Returns a string that specifies the name of the class that renders this
    * component. Normally, this would be a Look and Feel class, but in this case,
    * it has only been implemented once, not for each look and feel.
    *
    * @return "ButtonUI"
    * @see JComponent#getUIClassID
    * @see UIDefaults#getUI
    * @beaninfo
    *        expert: true
    *   description: A string that specifies the name of the L&F class.
   **/
   public String getUIClassID() {
       return m_uiClassID;
   }

   private SaveInfo [] m_filesToSave;
   private String [] m_saveLabels;
}
