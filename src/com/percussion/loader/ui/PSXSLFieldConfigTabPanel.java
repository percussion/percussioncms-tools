/*[ PSXSLFieldConfigTabPanel.java ]******************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader.ui;


import com.percussion.guitools.ErrorDialogs;
import com.percussion.loader.PSLoaderException;
import com.percussion.loader.extractor.PSXslExtractor;
import com.percussion.loader.objectstore.PSExtractorDef;
import com.percussion.loader.objectstore.PSProperty;

import java.io.File;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.border.Border;

/**
 * This is the "Fields" tab panel for the XSL configuration panel. It defines
 * the XSL file path.
 */
public class PSXSLFieldConfigTabPanel extends PSAbstractExtractorConfigTabPanel
{
   /**
    * Constructs a new <code>PSXSLFieldConfigTabPanel</code> object
    *
    */
   public PSXSLFieldConfigTabPanel()
   {
      init();
   }


   /**
    * Initializes the gui components for this panel
    */
   private void init()
   {

      if (null == ms_res)
         ms_res = ResourceBundle.getBundle(
               getClass().getName() + "Resources", Locale.getDefault() );

      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

      // Create borders
      Border b = BorderFactory.createEmptyBorder(10, 10, 10, 10 );

      // XSL file path field
      m_browsePanel = new PSBrowsePanel(
         this,
         ms_res.getString("field.label.xslFile"),
         JFileChooser.FILES_ONLY,
         "xsl",
         "XSL Files");
      m_browsePanel.setBorder(b);
      add(m_browsePanel);


   }

   // implements IPSExtractorConfigTabPanel interface method
   public void load(PSExtractorConfigContext config)
      throws PSLoaderException
   {
      if(null == config)
         throw new IllegalArgumentException(
            "Extractor Configuration context cannot be null");

      m_config = config;
      PSExtractorDef def = m_config.getExtractorDef();
      String xslFilePath = 
         def.getProperty(PSXslExtractor.XSL_FILEPATH).getValue();
      m_browsePanel.setPath(xslFilePath);
   }

   // implements IPSExtractorConfigTabPanel interface method
   public void save(PSExtractorConfigContext config)
      throws PSLoaderException
   {
      if(null == config)
         throw new IllegalArgumentException(
            "Extractor Configuration context cannot be null");

      PSExtractorDef def = m_config.getExtractorDef();
      PSProperty prop = def.getProperty(PSXslExtractor.XSL_FILEPATH);
      prop.setValue(m_browsePanel.getPath());
   }

   // implements IPSExtractorConfigTabPanel interface
   public boolean validateContent()
   {
      if(null == m_config)
         throw new IllegalStateException("m_config cannot be null");

      String xslFilePath = m_browsePanel.getPath();
      if (xslFilePath == null || xslFilePath.trim().length() == 0)
      {
         ErrorDialogs.showErrorDialog(this,
         getResourceString("error.message.xslfilepath.empty"),
         getResourceString("error.title.xslfilepath.empty"),
         JOptionPane.ERROR_MESSAGE);
         return false;
      }
      File file = new File(xslFilePath);
      if (! file.exists())
      {
         ErrorDialogs.showErrorDialog(this,
         getResourceString("error.message.xslfilepath.notexist"),
         getResourceString("error.title.xslfilepath.notexist"),
         JOptionPane.ERROR_MESSAGE);
         return false;
      }

      return true;
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
    * XSL file selection field. Initialized in {@link #init()}, never
    * <code>null</code> after that.
    */
   private PSBrowsePanel m_browsePanel;

   /**
    * This extractor's configuration context. Initialized in #loader(),
    * never <code>null</code> after that.
    */
   private PSExtractorConfigContext m_config;

   /**
    * Resource bundle for this class. Initialized once in {@link #init()},
    * never <code>null</code> after that.
    */
   protected static ResourceBundle ms_res;


}