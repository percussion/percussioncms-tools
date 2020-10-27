/*[ PSItemExtractorConfigPanel.java ]********************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader.ui;

import com.percussion.guitools.ErrorDialogs;
import com.percussion.loader.PSLoaderException;

import java.awt.BorderLayout;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;


/**
 * A Panel that repesents an Item Extractor and may contain zero or more tab 
 * panels that represent different sections of the item extractor to be edited.
 * 
 * This class implemented four tab panels, all of them are fully functional
 * except one empty "Fields" tab panel, which is created by 
 * {@link #createFieldTabPanel()}. A typical usage would be to subclass this 
 * class and then override the {@link #createFieldTabPanel()} method.
 */
public class PSItemExtractorConfigPanel extends PSExtractorConfigPanel
   implements IPSExtractorConfigChangeListener
{


   /**
    * Constructs a new <code>PSItemExtractorConfigPanel</code> object
    */
   public PSItemExtractorConfigPanel()
   {
      super(); // this will call initConfigPanel()
   }

   // Implements PSExtractorConfigPanel#initConfigPanel()
   protected JTabbedPane initConfigPanel()
   {
      if (null == ms_res)
         ms_res = ResourceBundle.getBundle(
               getClass().getName() + "Resources", Locale.getDefault() );

      setLayout(new BorderLayout());

      JTabbedPane pane = new JTabbedPane();
      PSAbstractExtractorConfigTabPanel contentTypePanel =
         createContentTypeTabPanel();
      contentTypePanel.addChangeListener(this);
      pane.addTab(getResourceString("tab.name.contentType"),
         contentTypePanel);
      pane.addTab(getResourceString("tab.name.fields"),
         createFieldTabPanel());
      pane.addTab(getResourceString("tab.name.filters"),
         createFilterTabPanel());
      pane.addTab(getResourceString("tab.name.workflow"),
         createWorkflowTabPanel());

      add(pane, BorderLayout.CENTER);

      return pane;
   }

   /**
    * Attempts to get string from the calling class's resource file.
    * If not found then it tries to find it in
    * <code>PSItemExtractorConfigPanel</code>'s resource file.
    * If all else fails the key string is returned.
    *
    * @param key the key string. May not be <code>null</code>.
    * @return localized string never <code>null</code>.
    */
    protected String getResourceString(String key)
    {
        String result = "";
        //First check this classes resource bundle
        try
        {
            result = ms_res.getString(key);
        }
        catch(MissingResourceException e1)
        {
           // See if the super classes resource has it
           try
           {
               result = ms_superRes.getString(key);
           }
           catch(MissingResourceException e2)
           {
              // Just return the key string
              result = key;
              System.err.println("Missing resource string for key: " + key);
           }

        }
        return result;

    }

    // implements IPSExtractorConfigChangeListener interface method
    public void configChanged(PSExtractorConfigChangeEvent event)
    {
        try
        {
           if(event.getType() ==
              PSExtractorConfigChangeEvent.VALUE_TYPE_COMMUNITY)
           {
              // Reset workflow tab panel
              Object obj = m_tabbedPane.getComponentAt(3);
              if(obj instanceof IPSExtractorConfigTabPanel)
              {
                ((IPSExtractorConfigTabPanel)obj).reset(m_config);
              }
           }
           if(event.getType() ==
              PSExtractorConfigChangeEvent.VALUE_TYPE_CONTENTTYPE)
           {
              // Reset fields tab panel
              Object obj = m_tabbedPane.getComponentAt(1);
              if(obj instanceof IPSExtractorConfigTabPanel)
              {
                ((IPSExtractorConfigTabPanel)obj).reset(m_config);
              }
           }
        }
        catch(PSLoaderException e)
        {
           ErrorDialogs.showErrorDialog(PSItemExtractorConfigPanel.this,
              e.getMessage(),
              ms_res.getString("err.title.loaderexception"),
              JOptionPane.ERROR_MESSAGE);
        }
    }

   /**
    * Creates a fully functioned content type tab panel, which is suitable for
    * most of the extractors.
    *
    * @return The created tab panel, never <code>null</code>.
    */
   protected PSAbstractExtractorConfigTabPanel createContentTypeTabPanel()
   {
      return new PSContentTypeConfigTabPanel();
   }

   /**
    * Creates an empty tab panel. The derived class may override this for its
    * own implementation of this panel.
    *
    * @return The created tab panel, never <code>null</code>.
    */
   protected PSAbstractExtractorConfigTabPanel createFieldTabPanel()
   {
      return new PSEmptyConfigTabPanel();
   }

   /**
    * Creates a fully functioned filter tab panel, which is suitable for
    * most of the extractors.
    *
    * @return The created tab panel, never <code>null</code>.
    */
   protected PSAbstractExtractorConfigTabPanel createFilterTabPanel()
   {
      return new PSFilterConfigTabPanel();
   }

   /**
    * Creates a fully functioned workflow tab panel, which is suitable for
    * most of the extractors.
    *
    * @return The created tab panel, never <code>null</code>.
    */
   protected PSAbstractExtractorConfigTabPanel createWorkflowTabPanel()
   {
      return new PSWorkflowConfigTabPanel();
   }

   /**
    * Resource bundle for this class. Initialized once in
    * {@link #initConfigPanel()}, never <code>null</code> after that.
    */
   protected static ResourceBundle ms_res;

   /**
    * Resource bundle for the <code>PSItemExtractorConfigPanel</code> class.
    * Initialized once in static block,
    * never <code>null</code> after that.
    */
   private static ResourceBundle ms_superRes;

   static
   {
      if (null == ms_superRes)
         ms_superRes = ResourceBundle.getBundle(
               "com.percussion.loader.ui.PSItemExtractorConfigPanelResources",
               Locale.getDefault() );

   }

}