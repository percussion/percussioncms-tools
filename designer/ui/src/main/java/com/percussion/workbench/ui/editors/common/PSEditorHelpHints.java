/******************************************************************************
*
* [ PSEditorHelpHints.java ]
*
* COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
* All rights reserved. This material contains unpublished, copyrighted
* work including confidential and proprietary information of Percussion.
*
*****************************************************************************/
package com.percussion.workbench.ui.editors.common;

import com.percussion.workbench.ui.PSWorkbenchPlugin;
import com.percussion.workbench.ui.help.PSHelpManager;
import com.percussion.workbench.ui.util.PSUiUtils;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.service.datalocation.Location;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * This class represents the PSXEditorHelpHints.xml file that contains all the editor
 * and wizard help hints that will be displayed in the field help view 
 * when a field get focus.
 * It loads the xml file when the first method call is made and 
 * then caches the informationfor any future calls. If the file does not exist then
 * nothing will be loaded but the methods will still work and just 
 * won't find the desired hint.
 */
public class PSEditorHelpHints
{

   private PSEditorHelpHints()
   {
      
   }   
      
   /**
    * Returns the help hint message or <code>null</code>
    * if it does not exist.
    * @param key may be <code>null</code>.
    * @return the help hint message or <code>null</code>.
    */
   public static String getMessage(String key)
   {
      if(key == null)
         return null;
      if(ms_data == null)
         load();
           
      String data = ms_data.get(key);
      if(data == null)
         data = "No field help available for the selected field.";
      StringBuilder sb = new StringBuilder();
      sb.append(addResourceBase(HTML_START));
      sb.append(data);
      sb.append(HTML_END);
      return sb.toString();
   }
   
   /**
    * Helper method to add resource base to image tags
    * @param data
    * @return modified string, never <code>null</code>.
    */
   private static String addResourceBase(String data)
   {
     return
         StringUtils.replace(data, "@@RESOURCEBASE@@", ms_resourceBase);         
   }
   
   /**
    * Loads the data from the xml file if it can be found
    * else just creates an empty map.
    */
   private static void load()
   {
      PSWorkbenchPlugin plugin = PSWorkbenchPlugin.getDefault();
      ms_data = new HashMap<String, String>();
      try
      {
        
         File defaultConfigDir = plugin.findDirectory(DEFAULT_CONFIG);
         String locale = Locale.getDefault().toString();
         //Try to find current xml file for this locale first
         File xmlFile = new File(defaultConfigDir, FILE_PATH + "_" + locale + ".xml");
         ms_resourceBase = getHelpBase();
         if(!(xmlFile.exists() && xmlFile.isFile()))
         {
            // Default to english version
            xmlFile = new File(defaultConfigDir, FILE_PATH + ".xml");
         }
         if(xmlFile.exists() && xmlFile.isFile())
         {
            loadXml(xmlFile);
         }
         else
         {
            PSUiUtils.log("Could not find editor help hints file: "
               + xmlFile.getAbsolutePath());
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
         PSUiUtils.log("Error trying load editor help hints file.");         
      }
   }
   
   /**
    * Locates the the help plugin'd directory for this workbench installation.
    * @return the directory path, never <code>null</code> or empty.
    */
   private static String getHelpBase()
   {      
      Location installLocation = Platform.getInstallLocation();
      URL url = installLocation.getURL();
      File installBase = new File(url.getFile());
      File helpBase = new File(
         installBase, "plugins/" + PSHelpManager.HELP_PLUGIN_DIR);
      return helpBase.getAbsolutePath();
   }
   
   /**
    * Loads the help hint data from the xml file which should be in the 
    * following format:
    * <p>
    * <pre>
    * <code>
    * &lt;!ELEMENT PSXEditorHelpHints (helphint*)&gt;
    * &lt;!ELEMENT helphint (#PCDATA)&gt;
    * &lt;!ATTLIST helphint
    *     key CDATA #REQUIRED
    * &gt;
    * </code>
    * </pre>
    * </p>
    * @param xmlfile the xml file, assumed not to be
    * <code>null</code>.
    * @throws SAXException 
    */
   private static void loadXml(File xmlfile)
      throws IOException, SAXException
   {
      InputStream is = null;      
      try
      {
         is = new FileInputStream(xmlfile);
         Document doc = 
            PSXmlDocumentBuilder.createXmlDocument(is, false);
         NodeList nl = 
            doc.getDocumentElement().getElementsByTagName(ELEM_HINT);
         int len = nl.getLength();
         for(int i = 0; i < len; i++)
         {
            Element hint = (Element)nl.item(i);
            String key = hint.getAttribute(ATTR_KEY);
            StringBuilder message = new StringBuilder();
            // Get Text
            NodeList children = hint.getChildNodes();
            int childLen = children.getLength();
            for(int c = 0; c < childLen; c++)
            {
               Node node = children.item(c);
               if(node instanceof Text)
               {
                  message.append(((Text)node).getData());
               }
            }            
            if(StringUtils.isNotBlank(message.toString()))
            {               
               ms_data.put(key, addResourceBase(message.toString()));
            }
         }
      }
      finally
      {
         try
         {
            if(is != null)
               is.close();
         }
         catch(IOException ignore){}
      }
   }
   
   
   /**
    * The map that holds all the data parsed from the xml file
    */
   private static Map<String, String> ms_data;
   
   /**
    * This image base path for this client.
    */
   private static String ms_resourceBase;
   
   // Constants  
   private static final String DEFAULT_CONFIG = "default-config";
   private static final String FILE_PATH = 
      "/rxconfig/Workbench/PSXEditorHelpHints";
   private static final String ELEM_HINT = "helphint";
   private static final String ATTR_KEY = "key";
   
   private static final String HTML_START = "<html><head><link rel=\"stylesheet\" " +
      "type=\"text/css\" href=\"@@RESOURCEBASE@@\\stylesheet.css\"></head><body>";
   private static final String HTML_END = "</body></html>";

}
