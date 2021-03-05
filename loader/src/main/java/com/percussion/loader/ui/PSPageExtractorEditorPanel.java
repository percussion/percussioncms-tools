/*[ PSPageExtractorEditorPanel.java ]******************************************
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
import com.percussion.loader.objectstore.PSPageExtractorDef;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.awt.BorderLayout;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.Border;

import org.w3c.dom.Element;

/**
 * Represents page extractor editor panel containing a checkbox and description 
 * panel. Page extractors extract the whole page into Rhythmyx field.
 */
public class PSPageExtractorEditorPanel  extends PSConfigPanel
{
   /**
    * Creates a page extractor editor panel.
    */
   public PSPageExtractorEditorPanel ()
   {
      init();
   }

   /**
    * Initializes this panel with a checkbox and a description panel.
    */
   private void init()
   {
      if (null == ms_res)
         ms_res = ResourceBundle.getBundle(
               getClass().getName() + "Resources", Locale.getDefault() );

      setLayout(new BorderLayout());
      Border b1 = BorderFactory.createEmptyBorder(10, 10, 10, 10 );
      Border b3 = BorderFactory.createEtchedBorder();
      Border b2 = BorderFactory.createCompoundBorder(b3, b1);
      setBorder(b2);
      String description = PSContentLoaderResources.getResourceString(
         ms_res, "description");
      JTextArea txtArea = new JTextArea();
      JPanel descPanel =
         PSContentDescriptorDialog.createDescriptionPanel(description,
         txtArea);
      JPanel dPanel = new JPanel();
      dPanel.setBorder(b2);
      dPanel.setLayout(new BorderLayout());
      dPanel.add(descPanel, BorderLayout.CENTER);
      add(dPanel, BorderLayout.SOUTH);
   }

   /**
    * Implements the {@link IPSConfigPanel} interface.
    */
   public void load(Element configXml)
   {
      try
      {
         m_extPageDef = new PSPageExtractorDef(configXml);
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
   public Element save()
   {
      if(!validateContent())
         return null;

      return m_extPageDef.toXml(PSXmlDocumentBuilder.createXmlDocument());
   }

   /**
    * Implements the {@link IPSConfigPanel} interface.
    */
   public void reset()
   {
   }

   /**
    * Implements the {@link IPSConfigPanel} interface.
    */
   public boolean validateContent()
   {
      return true;
   }

   /**
    * {@link com.percussion.objectstore.PSPageExtractorDef}, Initialized in
    * {@link #load(Element)}, never <code>null</code> or modified after that.
    */
   private PSPageExtractorDef m_extPageDef;


   /**
    * Resource bundle for this class. Initialised in the constructor.
    * It's not modified after that. Never <code>null</code>
    */
   private static ResourceBundle ms_res;
}
