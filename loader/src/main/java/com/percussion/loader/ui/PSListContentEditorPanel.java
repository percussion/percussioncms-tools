/*[ PSListContentEditorPanel.java ]********************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader.ui;

import com.percussion.UTComponents.UTFixedHeightTextField;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.guitools.ErrorDialogs;
import com.percussion.loader.PSLoaderException;
import com.percussion.loader.objectstore.PSListSelectorDef;
import com.percussion.loader.objectstore.PSLoaderPreviewDef;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.w3c.dom.Element;

/**
 * Panel specifying preview editor, it allows the user to specify all the
 * properties for the current loading process to the file system. It consists of
 * a browse button to to specify the file location and a text field holding the
 * specified location.
 */
public class PSListContentEditorPanel extends PSConfigPanel
{
   /**
    * Creates the list editor panel.
    */
   public PSListContentEditorPanel()
   {
      this(false);
   }

   /**
    * Creates preview editor panel or list editor panel based on the argument.
    *
    * @param isPreview, if <code>true</code> creates a preview editor panel else
    * a list editor panel.
    */
   public PSListContentEditorPanel(boolean isPreview)
   {
      m_isPreview = isPreview;
      init();
   }

   /**
    * Initializes the panel.
    */
   private void init()
   {
      if (null == ms_res)
         ms_res = ResourceBundle.getBundle(
            getClass().getName() + "Resources", Locale.getDefault());

      setLayout(new BorderLayout());
      setPreferredSize(new Dimension(200, 200));
      JPanel jp = new JPanel();
      jp.setLayout(new BoxLayout(jp, BoxLayout.X_AXIS));
      if (m_isPreview)
      {
         m_browsePanel = new PSBrowsePanel(this,
         PSContentLoaderResources.getResourceString(ms_res,
         "label.textfield.path"), JFileChooser.DIRECTORIES_ONLY);
      }
      else
      {
         m_browsePanel = new PSBrowsePanel(this,
            PSContentLoaderResources.getResourceString(ms_res,
            "label.textfield.contentlist"), 
            JFileChooser.FILES_ONLY, 
            XML_EXTENSION,
            PSContentLoaderResources.getResourceString(ms_res, 
            "label.xml.extension"));
      }
      jp.add(Box.createRigidArea(new Dimension(10, 0)));
      jp.add(m_browsePanel);
      jp.add(Box.createRigidArea(new Dimension(20, 0)));
      add(jp, BorderLayout.NORTH);
      jp.setBorder(BorderFactory.createEmptyBorder(30,10,10,20));
   }

   /**
    * Resets the the path textfield in this panel to an empty string.
    */
   public void reset()
   {
      try
      {
         if (m_isPreview)
            m_browsePanel.setPath(m_previewDef.getPreviewPath());
         else
            m_browsePanel.setPath(m_listSelDef.getContentList());
      }
      catch(PSLoaderException e)
      {
         ErrorDialogs.showErrorDialog(this,
            PSContentLoaderResources.getResourceString(
            ms_res, e.getMessage()),
            PSContentLoaderResources.getResourceString(
            ms_res, "err.title.loaderexception"),
            JOptionPane.ERROR_MESSAGE);
         return;
      }
   }

   /**
    * Loads the data for this panel. Error dialog is shown if there is a problem
    * in loading the data.
    *
    * @param configXml, data for this panel, never <code>null</code>.
    *
    * @throws IllegalArgumentException if the supplied argument is not valid.
    */
   public void load(Element configXml)
   {
      if (configXml == null)
         throw new IllegalArgumentException(
            "Config xml cannot tbe null");
      try
      {
         if (m_isPreview)
         {
            m_previewDef = new PSLoaderPreviewDef(configXml);
            m_browsePanel.setPath(m_previewDef.getPreviewPath());
         }
         else
         {
            m_listSelDef = new PSListSelectorDef(configXml);
            m_browsePanel.setPath(m_listSelDef.getContentList());
         }
      }
      catch(PSLoaderException e)
      {
         ErrorDialogs.showErrorDialog(this,
            PSContentLoaderResources.getResourceString(
            ms_res, e.getMessage()),
            PSContentLoaderResources.getResourceString(
            ms_res, "err.title.loaderexception"),
            JOptionPane.ERROR_MESSAGE);
         return;
      }
      catch(PSUnknownNodeTypeException f)
      {
         ErrorDialogs.showErrorDialog(this,
            PSContentLoaderResources.getResourceString(
            ms_res, f.getMessage()),
            PSContentLoaderResources.getResourceString(
            ms_res, "error.title.unknownnode"),
            JOptionPane.ERROR_MESSAGE);
         return;
      }
   }

   /**
    * Saves the data in this panel to the data object loaded by {@link #load(
    * object)}. Popsup an error dialog
    */
   public Element save()
   {
      if (!validateContent())
         return null;
      try
      {
          if (m_isPreview)
             m_previewDef.setPreviewPath(m_browsePanel.getPath());
          else
             m_listSelDef.setContentList( m_browsePanel.getPath());
      }
      catch(PSLoaderException e)
      {
         ErrorDialogs.showErrorDialog(this,
         getResourceString(e.getLocalizedMessage()),
         getResourceString("error.title.uabletoset"),
         JOptionPane.ERROR_MESSAGE);
      }
      if (m_isPreview)
         return m_previewDef.toXml(PSXmlDocumentBuilder.createXmlDocument());
      else
         return m_listSelDef.toXml(PSXmlDocumentBuilder.createXmlDocument());
   }

   /**
    * Gets the resource mapping for the supplied key.
    *
    * @param key, may not be <code>null</code>
    * @return mapping corresponding to the key, never <code>null</code>.
    *
    * @throws IllegalArgumentException if the argument is invalid.
    */
   private String getResourceString(String key)
   {
      return PSContentLoaderResources.getResourceString(ms_res, key);
   }

   /**
    * Validates all the fields in the panel. Pops up a dilaog at every eror
    * occurrence.
    *
    * @return <code>true</code> if vaildation is successful else <code>false
    * </code>
    */
   public boolean validateContent()
   {
      String s = m_browsePanel.getPath();
      File f = new File(s);
      if (m_isPreview)
      {
         if (!f.isDirectory())
         {
            ErrorDialogs.showErrorDialog(this,
            getResourceString("error.msg.pathnotfound"),
            getResourceString("error.title.pathnotfound"),
            JOptionPane.ERROR_MESSAGE);
            return false;
         }
      }
      else if (!f.isFile())
      {
         ErrorDialogs.showErrorDialog(this,
         getResourceString("error.msg.filenotfound"),
         getResourceString("error.title.filenotfound"),
         JOptionPane.ERROR_MESSAGE);
         return false;
      }

      return true;
   }

   /**
    * Initialized in the ctor. If <code>true</code> then it's a preview editor
    * panel else list editor panel.
    */
   private boolean m_isPreview;

   /**
    * The preview definition. Intialized in {@link #load(Element),never 
    * <code>null</code> after that.
    */
   private PSLoaderPreviewDef m_previewDef;

   /**
    * Encapsulates the definition of the list content selector. Intialized in
    * {@link #load(Element)  load(configXml)}, never <code>null
    * </code>, equivalent to a variable declared final.
    */
   private PSListSelectorDef m_listSelDef;

   /**
    * Text field for secifying the fully qualified path to dump the content
    * for preview. Initialized in {@link init()}, never <code>null</code>
    * after that. Equivalent to a variable declared final.
    */
   private UTFixedHeightTextField m_path;

   /**
    * Panel containing a textfield, a browse button and or a label for the text
    * field. Initialized in the {@link #init()}, never <code>null</code>,
    * equivalent to a variable declared final.
    */
   PSBrowsePanel m_browsePanel;

   /**
    * Resource bundle for this class. Initialized in {@link init()}.
    * It's not modified after that. Never <code>null</code>, equivalent to a
    * variable declared final.
    */
   private static ResourceBundle ms_res;
   
   /**
    * The XML file extension
    */
   private final static String XML_EXTENSION = "xml";
}
