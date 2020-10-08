/*[ PSBrowsePanel.java ]*******************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader.ui;

import com.percussion.UTComponents.UTBrowseButton;
import com.percussion.UTComponents.UTFixedHeightTextField;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;

/**
 * Panel with a text field and a browse button for browsing file system. It
 * allows to browse directory and select the directory path in the text field.
 */
public class PSBrowsePanel extends JPanel
{
   /**
    * Ctor for browse panel.
    *
    * @param parent The parent component, it may not be <code>null</code>.
    * 
    * @param browseType, specifies if directories and or files have to be
    * browsed. Values are JFileChooser.DIRECTORIES_ONLY, JFileChooser.FILES_ONLY
    * JFileChooser.FILES_AND_DIRECTORIES
    */
   public PSBrowsePanel(Component parent, int browseType)
   {
      this(parent, null, browseType, null, null, true);
   }

   /**
    * Ctor for browse panel
    *
    * @param parent The parent component, it may not be <code>null</code>.
    * 
    * @param labelName label name for the text field. May be 
    *    <code>null</code> or empty.
    * @param browseType, specifies if directories and or files have to be
    * browsed. Values are JFileChooser.DIRECTORIES_ONLY, JFileChooser.FILES_ONLY
    * JFileChooser.FILES_AND_DIRECTORIES
    * @throws IllegalArgumentException if any parameter is invalid.
    */
   public PSBrowsePanel(Component parent, String labelName, int browseType)
   {
      this(parent, labelName, browseType, null, null, true);
   }

   /**
    * Convenient ctor, calls {@link #PSBrowsePanel(Component,String,int,
    * String,String,boolean) PSBrowsePanel(Component,String,int,String,String,
    * true)}
    */
   public PSBrowsePanel(Component parent, String labelName, int browseType, 
      String extension, String extDesc)
   {
      this(parent, labelName, browseType, extension, extDesc, true);
   }
   
   /**
    * Ctor for browse panel, the same as {@link PSBrowsePanel(String, int)},
    * except this method can filter on a specified file extension
    *
    * @param parent The parent component, it may not be <code>null</code>.
    * 
    * @param extension The file extension it may filter on. It may be 
    *    <code>null</code> if choose not to apply filter.
    *    
    * @param extDesc The description of the file extension. It may be
    *    <code>null</code>.
    *    
    * @param editable <code>true</code> if the path can be edited; otherwise
    *    it is display only, the path cannot be edited.
    *
    * @throws IllegalArgumentException if any parameter is invalid.
    */
   public PSBrowsePanel(Component parent, String labelName, int browseType, 
      String extension, String extDesc, boolean editable)
   {
      if (parent == null)
         throw new IllegalArgumentException("parent may not be null");
         
      if (browseType != JFileChooser.DIRECTORIES_ONLY &&
         browseType != JFileChooser.FILES_ONLY &&
         browseType != JFileChooser.FILES_AND_DIRECTORIES)
         throw new IllegalArgumentException("Invalid browse type.");

      if (labelName != null && labelName.length() > 0)
         m_label = new JLabel(labelName);
      
      m_parent = parent;   
      m_browseType = browseType;
      init(extension, extDesc, editable);
   }

   /**
    * Initializes the panel.
    * 
    * @param extension The file extension it may filter on. It may be 
    *    <code>null</code> if choose not to apply filter.
    *    
    * @param extDesc The description of the file extension. It may be
    *    <code>null</code>.
    *    
    * @param editable <code>true</code> if the path can be edited; otherwise
    *    it is display only, the path cannot be edited.
    */
   private void init(String extension, String extDesc, boolean editable)
   {
      setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
      setBorder(BorderFactory.createEmptyBorder( 10, 0, 0, 0));
      m_extension = extension;
      m_extDesc = extDesc == null ? "" : extDesc;
      m_path = new UTFixedHeightTextField();
      UTBrowseButton browse = new UTBrowseButton();
      browse.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            JFileChooser chooser = new JFileChooser(
               System.getProperty("user.dir"));
            chooser.setMultiSelectionEnabled(false);
            chooser.setFileSelectionMode(m_browseType);

            if (m_extension != null)
            {            
               chooser.setFileFilter( new FileFilter()
               {
                  // accept any directory or files ending with specified 
                  // extension.
                  public boolean accept(File path)
                  {
                     if ((path.isFile() && 
                          path.getAbsolutePath().endsWith("." + m_extension))
                          || path.isDirectory())
                     {
                        return true;
                     }
                     return false;
                  }
         
                  public String getDescription()
                  {
                     return m_extDesc;
                  }
               });
            }
            
            
            
            int returnVal = chooser.showOpenDialog(m_parent);
            if (returnVal == JFileChooser.OPEN_DIALOG)
            {
               File file = chooser.getSelectedFile();
               m_path.setText(file.getAbsolutePath());
            }
         }
      });

      if (m_label != null)
      {
         add(m_label);
         add(Box.createRigidArea(new Dimension(20, 0)));
      }
      
      add(m_path);
      if (editable)
      {
         add(browse);
      }
      else
      {
         m_path.setEditable(false);
      }
   }

   /**
    * Gets the path of the selected directory.
    *
    * @return full directory path, may be <code>null</code> or empty.
    */
   public String getPath()
   {
      return m_path.getText();
   }

   /**
    * Sets the path.
    *
    * @param path, may be <code>null</code> or empty.
    */
   public void setPath(String path)
   {
      if (path == null || path.length() == 0)
         return;
         
      m_path.setText(path);
   }

   /**
    * Label for the text field. It's optional, initialized in non default ctor.
    * May be <code>null</code> if initialized by default ctor otherwise not,
    * never modified after that.
    */
   private JLabel m_label;

   /**
    * @see the ctor. Initialized in the ctor.
    */
   private int m_browseType;

   /**
    * Text field for secifying the fully qualified path to dump the content
    * for preview. Initialized in {@link init()}, never <code>null</code>
    * after that.
    */
   private UTFixedHeightTextField m_path;
   
   /**
    * File extension that may be filtered with. Initialized by init(), 
    * never modified after that. It may be <code>null</code> if not to apply
    * filter
    */
   private String m_extension;
   
   /**
    * Description of the above file extension. Initiailized by init(), never 
    * modified after that. 
    */
   private String m_extDesc;
   
   /**
    * The parent component. Initialized by ctor, never <code>null</code> after
    * that.
    */
   private Component m_parent;
}
