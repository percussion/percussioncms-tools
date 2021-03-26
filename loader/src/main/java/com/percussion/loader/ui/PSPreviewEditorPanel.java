/*[ PSPreviewEditorPanel.java ]************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader.ui;

import com.percussion.UTComponents.UTFixedHeightTextField;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JPanel;

/**
 * Panel specifying preview editor, it allows the user to specify all the
 * properties for the current loading process to the file system. It consists of
 * a browse button to to specify the file location and a text field holding the
 * specified location.
 *
 */
public class PSPreviewEditorPanel extends PSConfigPanel
{
   /**
    * Creates the preview editor panel.
    */
   public PSPreviewEditorPanel()
   {
      init();
   }

   /**
    * Initializes the panel.
    */
   private void init()
   {
      if (null == ms_res)
         ms_res = ResourceBundle.getBundle(
               getClass().getName() + "Resources", Locale.getDefault() );

      setLayout(new BorderLayout());
      setPreferredSize(new Dimension(200, 200));
      JPanel jp = new JPanel();
      jp.setLayout(new BoxLayout(jp, BoxLayout.X_AXIS));
      PSBrowsePanel pp = new PSBrowsePanel(this, 
         PSContentLoaderResources.getResourceString(ms_res,
         "label.textfield.path"), JFileChooser.DIRECTORIES_ONLY);
      jp.add(Box.createRigidArea(new Dimension(10, 0)));
      jp.add(pp);
      jp.add(Box.createRigidArea(new Dimension(20, 0)));
      add(jp, BorderLayout.NORTH);
      jp.setBorder(BorderFactory.createEmptyBorder(30,10,10,20));
   }

   /**
    * Text field for secifying the fully qualified path to dump the content
    * for preview. Initialized in {@link init()}, never <code>null</code>
    * after that.
    */
   private UTFixedHeightTextField m_path;

   /**
    * Resource bundle for this class. Initialized in {@link init()}.
    * It's not modified after that. Never <code>null</code>
    */
   private static ResourceBundle ms_res;
}
