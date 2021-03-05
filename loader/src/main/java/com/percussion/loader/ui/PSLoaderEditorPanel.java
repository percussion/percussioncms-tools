/*[ PSLoaderEditorPanel.java ]*************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader.ui;

import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.guitools.ErrorDialogs;
import com.percussion.loader.PSLoaderException;
import com.percussion.loader.objectstore.PSLoaderDef;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.JOptionPane;

import org.w3c.dom.Element;


/**
 * Loader editor panel for specifying all the properties for the content loading
 * process into Rhythmyx. It contains text fields for entering server name, port
 * , user name and password.
 */
public class PSLoaderEditorPanel extends PSConfigPanel
{
   /**
    * Creates the loader editor panel.
    */
   public PSLoaderEditorPanel()
   {
      init();
   }

   /**
    * Initializes the panel.
    */
   protected void init()
   {
      if (null == ms_res)
         ms_res = ResourceBundle.getBundle(
            getClass().getName() + "Resources", Locale.getDefault());
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
            "xml element cannot tbe null");
      try
      {
         m_loaderDef = new PSLoaderDef(configXml);
      }
      catch(PSLoaderException e)
      {
         ErrorDialogs.showErrorDialog(this,
            PSContentLoaderResources.getResourceString(ms_res, e.getMessage()),
            PSContentLoaderResources.getResourceString(ms_res,
            "err.title.loaderexception"), JOptionPane.ERROR_MESSAGE);
         return;
      }
      catch(PSUnknownNodeTypeException f)
      {
         ErrorDialogs.showErrorDialog(this,
            PSContentLoaderResources.getResourceString(ms_res, f.getMessage()),
            PSContentLoaderResources.getResourceString(ms_res,
            "error.title.unknownnode"), JOptionPane.ERROR_MESSAGE);
         return;
      }
   }

   /**
    * Implements the {@link IPSConfigPanel} interface.
    */
   public boolean validateContent()
   {
      return true;
   }

   /**
    * Implements the {@link IPSConfigPanel} interface.
    */
   public Element save()
   {
      if (!validateContent())
         return null;
      return m_loaderDef.toXml(PSXmlDocumentBuilder.createXmlDocument());
   }

   /**
    * Gets the resource mapping for the supplied key.
    *
    * @param key, may not be <code>null</code>
    * @return mapping corresponding to the key, never <code>null</code>.
    *
    * @throws IllegalArgumentException if the argument is invalid.
    */
   public String getResourceString(String key)
   {
      return PSContentLoaderResources.getResourceString(ms_res, key);
   }

   /**
    * Represents a PSLoaderDef xml object. Initialized in {@link #load(Element)}
    * , never <code>null</code> or modified after that.
    */
   private PSLoaderDef m_loaderDef;

   /**
    * Resource bundle for this class. Initialised in the constructor.
    * It's not modified after that. Never <code>null</code>.
    */
   private static ResourceBundle ms_res;
}