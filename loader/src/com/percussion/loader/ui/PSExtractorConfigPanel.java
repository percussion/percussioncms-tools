/*[ PSExtractorConfigPanel.java ]**********************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader.ui;

import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.loader.PSLoaderException;
import com.percussion.loader.objectstore.PSExtractorDef;
import com.percussion.xml.PSXmlDocumentBuilder;

import javax.swing.JTabbedPane;

import org.w3c.dom.Element;

/**
 * This class can be used as the super class for the UI configuration panel for
 * all extractor definitions. The derived classes must contains a tab
 * panel and each sub-panel (of the tab panel) must implemente 
 * {@link IPSExtractorConfigTabPanel} interface.
 * 
 * @see PSItemExtractorConfigPanel
 */
public abstract class PSExtractorConfigPanel extends PSConfigPanel
{

   /**
    * Default constrcutor. Has to be called by the derived classes.
    */
   PSExtractorConfigPanel()
   {
      m_tabbedPane = initConfigPanel();
   }
   
   /**
    * Initializes the tab-panel, has to be implemented by the derived classes.
    */
   protected abstract JTabbedPane initConfigPanel();


   // implement IPSConfigPanel#load(Element)
   public void load(Element configXml)
      throws PSLoaderException
   {
      if (configXml == null)
         throw new IllegalArgumentException(
            "Config xml cannot tbe null");

      try
      {
         m_config = new PSExtractorConfigContext();
         m_config.setExtractorDef(new PSExtractorDef(configXml));
         loadTabPanels();
      }
      catch (PSUnknownNodeTypeException e)
      {
         throw new PSLoaderException(e);
      }
   }

   // implements IPSConfigPanel interface method
   public void reset()
      throws PSLoaderException
   {
      if (m_config == null)
         throw new IllegalStateException("m_config has not been set yet");
      
      boolean reset = false;
      reset = m_config.resetExtractor();
   }

   // implements IPSConfigPanel interface method
   public Element save()
      throws PSLoaderException
   {
      if (m_config == null)
         throw new IllegalStateException("m_config has not been set yet");
      
      saveTabPanels();

      PSExtractorDef def = m_config.getUpdatedExtractorDef();

      return def.toXml(PSXmlDocumentBuilder.createXmlDocument());

   }

   /**
    * Calls <code>validateContent</code> for each sub-panel.
    *
    * @return <code>true</code> if all <code>validateContent</code> methods
    *    return <code>true</code>; <code>false</code> if one of them returns
    *    <code>false</code>.
    */
   public boolean validateContent()
   {
      if (m_config == null)
         throw new IllegalStateException("m_config has not been set yet");
      
       int tabCount = m_tabbedPane.getTabCount();
       for (int i = 0; i < tabCount; i++)
       {
          Object obj = m_tabbedPane.getComponentAt(i);
          if(obj instanceof IPSExtractorConfigTabPanel)
          {
             if (! ((IPSExtractorConfigTabPanel)obj).validateContent())
               return false;
          }
       }
       return true;
   }

   // implements IPSConfigPanel interface method
   public String getName()
   {
      if (m_config == null)
         throw new IllegalStateException("m_config has not been set yet");
      
      Object obj = m_tabbedPane.getComponentAt(0);
      if(obj instanceof IPSExtractorConfigTabPanel)
         return ((IPSExtractorConfigTabPanel)obj).getName();
      return "";
   }


   /**
    * Loads each of the tab panels fields
    */
   protected void loadTabPanels()
      throws PSLoaderException
   {
       int tabCount = m_tabbedPane.getTabCount();
       for (int i = 0; i < tabCount; i++)
       {
          Object obj = m_tabbedPane.getComponentAt(i);
          if(obj instanceof IPSExtractorConfigTabPanel)
          {
             ((IPSExtractorConfigTabPanel)obj).load(m_config);
          }
       }
   }

   /**
    * Save each of the tab panels fields
    */
   protected void saveTabPanels()
      throws PSLoaderException   
   {
       int tabCount = m_tabbedPane.getTabCount();
       for (int i = 0; i < tabCount; i++)
       {
          Object obj = m_tabbedPane.getComponentAt(i);
          if(obj instanceof IPSExtractorConfigTabPanel)
          {
             ((IPSExtractorConfigTabPanel)obj).save(m_config);
          }
       }
   }



   /**
    * This extractor's configuration context. Initialized in #load(Element),
    * never <code>null</code> after that.
    */
   protected PSExtractorConfigContext m_config;

   /**
    * The main tab-panel, initialized by the ctor, never <code>null</code>
    * after that.
    */
   protected JTabbedPane m_tabbedPane;
}
