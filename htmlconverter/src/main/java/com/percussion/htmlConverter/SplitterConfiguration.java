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

import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * This class wraps all functionality to access and maintain the splitter
 * configuration files. These are currently:
 */
public class SplitterConfiguration
{
   /**
    * Convenience ctor that calls 
    * {@link #SplitterConfiguration(boolean, File) this(standalone, null)}
    */
   public SplitterConfiguration(boolean standalone) throws SplitterException
   {
      this(standalone, null);
   }
   
   /**
    * Loads and initializes all splitter configuration files.
    *
    * @param standalone a flag indication whether or not this splitter
    *    instance is used for the standalone splitter (<code>true</code>) or
    *    not (<code>false</code>).
    * @param rootDir The root directory to use for file path resolution, may be
    *    <code>null</code> to use the current directory.
    * @throws SplitterException for all exceptions raised while loading and
    *    initializing the splitter configuration files.
    */
   public SplitterConfiguration(boolean standalone, File rootDir) 
      throws SplitterException
   {
      if (rootDir != null)
         ms_rootDir = rootDir;
      
      init(standalone);

      /*
       * Make sure all tags have the separator added and save the
       * configuration if this was not the case.
       */
      if (checkTagsForSeparator())
         saveProperties();
   }

   /**
    * Creates a new splitter configuration overwriting the provided
    * parameters.
    *
    * @param dynamicTag the dynamic HTML mark-up tag to use, might be
    *    <code>null</code> or empty. This will overwrite the current setting
    *    only if valid. A tag separator is added if not provided.
    * @param propertyTag the property HTML mark-up tag to use, might be
    *    <code>null</code> or empty. This will overwrite the current setting
    *    only if valid. A tag separator is added if not provided.
    * @param xslEncoding the encoding to specify in the prodeced XSL output
    *    element, might be <code>null</code>.
    * @param showWarnings overwrite the show warning setting with the
    *    provided setting, might be <code>null</code>. Will only overwrite
    *    the current setting if valid.
    */
   public SplitterConfiguration(String dynamicTag, String propertyTag,
      String xslEncoding, Boolean showWarnings) throws SplitterException
   {
      init(false);

      // overwrite the splitter configuration with provided values
      if (dynamicTag != null && dynamicTag.trim().length() > 0)
         ms_xsplitProperties.setProperty("dynamicTag", dynamicTag);

      if (propertyTag != null && propertyTag.trim().length() > 0)
         ms_xsplitProperties.setProperty("propertyTag", propertyTag);

      if (xslEncoding != null && xslEncoding.trim().length() > 0)
         ms_xsplitProperties.setProperty("xslOutputEncoding", xslEncoding);

      if (showWarnings != null)
         ms_xsplitProperties.setProperty("showWarnings",
            showWarnings.booleanValue() ? "yes" : "no");

      /*
       * Make sure all tags have the separator added and save the
       * configuration if this was not the case.
       */
      if (checkTagsForSeparator())
         saveProperties();
   }

   /**
    * Initializes the splitter configuration.
    *
    * @param standalone a flag indication whether or not this splitter
    *    instance is used for the standalone splitter (<code>true</code>) or
    *    not (<code>false</code>).
    * @throws SplitterException for all exceptions raised while loading and
    *    initializing the splitter configuration files.
    */
   private void init(boolean standalone) throws SplitterException
   {
      ms_standalone = standalone;

      final String xsplitDir = new File(ms_rootDir, XSPLIT_DIR).getPath();
      
      ms_serverPageTags = loadConfigDocument(
         getConfigFile(ENTRY_NAME, SERVER_PAGE_TAGS, xsplitDir));

      ms_assemblerConfig = loadConfigDocument(
         getConfigFile(ENTRY_NAME, ASSEMBLER_CONFIG, xsplitDir));

      ms_propertyFile = getConfigFile(ENTRY_NAME, XSPLIT_PROPS, xsplitDir);
      try
      {
         // load the splitter configuration
         ms_xsplitProperties.load(new FileInputStream(ms_propertyFile));

         // load the tidy configuration
         ms_tidyConfig.load(new FileInputStream(
            getConfigFile(ENTRY_NAME, TIDY_PROPS, xsplitDir)));
      }
      catch (FileNotFoundException e)
      {
         ms_xsplitProperties = DEFAULT_PROPERTIES;
         saveProperties();
      }
      catch (Throwable e)
      {
         throw new SplitterException(e.getLocalizedMessage());
      }
   }

   /**
    * Returns the status whether this instance is used in the standalone
    * xsplit product or not.
    *
    * @return <code>true</code> if used as standalone xsplit,
    *    <code>false</code> otherwise.
    */
   public static boolean isStandalone()
   {
      return ms_standalone;
   }

   /**
    * Checks all tags in this configuration for the separators. If not
    * provided, they will be added.
    *
    * @return <code>true</code> if any tag had a missing tag separator,
    *    <code>false</code> otherwise.
    */
   public boolean checkTagsForSeparator()
   {
      boolean hadMissingSeparator = false;

      String dynamicTag = ms_xsplitProperties.getProperty("dynamicTag");
      if (!dynamicTag.endsWith(TAG_SEPARATOR))
      {
         dynamicTag += TAG_SEPARATOR;
         ms_xsplitProperties.setProperty("dynamicTag", dynamicTag);
         hadMissingSeparator = true;
      }

      String propertyTag = ms_xsplitProperties.getProperty("propertyTag");
      if (!propertyTag.endsWith(TAG_SEPARATOR))
      {
         propertyTag += TAG_SEPARATOR;
         ms_xsplitProperties.setProperty("propertyTag", propertyTag);
         hadMissingSeparator = true;
      }

      return hadMissingSeparator;
   }

   /**
    * Save the XSpLit properties file.
    *
    * @throws SplitterException if the file could not be saved.
    */
   public void saveProperties() throws SplitterException
   {
      try
      {
         ms_xsplitProperties.store(new FileOutputStream(ms_propertyFile),
            "XSpLit configuration");
      }
      catch (Throwable t)
      {
         throw new SplitterException(t.getLocalizedMessage());
      }
   }

   /**
    * Returns the splitter property asked for.
    *
    * @param property the name of the property we want, might be
    *    <code>null</code> or empty.
    * @return the property value if found, <code>null</code> otherwise.
    */
   public String getProperty(String property)
   {
      String value = null;
      if (property != null)
      {
         value = ms_xsplitProperties.getProperty(property);
         if (value == null)
            value = DEFAULT_PROPERTIES.getProperty(property);
      }

      return value;
   }

   /**
    * Returns the default property asked for.
    *
    * @param property the name of the property we want, might be
    *    <code>null</code> or empty.
    * @return the property value if found, <code>null</code> otherwise.
    */
   public static String getDefaultProperty(String property)
   {
      String value = null;
      if (property != null)
         value = DEFAULT_PROPERTIES.getProperty(property);

      return value;
   }

   /**
    * Returns the splitter property asked for or the provided default if not
    * found.
    *
    * @param property the name of the property we want, might be
    *    <code>null</code> or empty.
    * @param defaultValue the default value returned if the property was not
    *    found, might be <code>null</code> or empty.
    * @return the property value if found, <code>null</code> otherwise.
    */
   public String getProperty(String property, String defaultValue)
   {
      if (property == null)
         return defaultValue;

      return ms_xsplitProperties.getProperty(property, defaultValue);
   }

   /**
    * Set the provided splitter property.
    *
    * @param key the property key, might be <code>null</code> or empty.
    * @param value the property value, might be <code>null</code> or empty.
    */
   public void setProperty(String key, String value)
   {
      ms_xsplitProperties.setProperty(key, value);
   }

   /**
    * Accessor to the server page tags configuration document.
    *
    * @return the serverPageTags document, never <code>null</code>.
    */
   public Document getServerPageTags()
   {
      return ms_serverPageTags;
   }

   /**
    * Accessor to the assembler configuration document.
    *
    * @return the assemblerConfig document, never <code>null</code>.
    */
   public Document getAssemblerConfig()
   {
      return ms_assemblerConfig;
   }

   /**
    * Get the tidy configuration.
    *
    * @return the tidy configuration, never <code>null</code>.
    */
   public Properties getTidyConfig()
   {
      return ms_tidyConfig;
   }

   /**
    * Get the stylesheet location(s) for the provided element name.
    *
    * @param tagName the tag name we need the stylesheet locations from,
    *    might be <code>null</code> or empty.
    * @return a list of all stylesheet locations (String) found for the
    *    provided element name, never <code>null</code>, might be empty. The
    *    list is returned in the order they need to be included in the target
    *    stylesheet.
    */
   public List getStylesheetLocations(String tagName)
   {
      List<String> list = new ArrayList<String>();
      if (tagName != null && tagName.trim().length() > 0)
      {
         NodeList nodes = ms_assemblerConfig.getElementsByTagName(tagName);
         if (nodes != null && nodes.getLength() > 0)
         {
            // there should only be one node
            Node node = nodes.item(0);
            if (node != null && node instanceof Element)
            {
               NodeList locations = node.getChildNodes();
               for (int i=0; i<locations.getLength(); i++)
               {
                  Node location = locations.item(i);
                  if (location.getFirstChild() instanceof Text)
                     list.add(0, ((Text) location.getFirstChild()).getData());
               }
            }
         }
      }

      return list;
   }

   /**
    * Returns whether or not to add the assembler specific imports in any case
    * or not.
    *
    * @return <code>true</code> to add the imports in all cases,
    *    <code>false</code> otherwise.
    */
   public boolean addAssemblerImports()
   {
      return getProperty("addAssemblerImports").equalsIgnoreCase("yes");
   }

   /**
    * Load and parse the configuration file asked for into a Document and
    * report any proplem occurred to the console.
    *
    * @param file the configuration file to load, not <code>null</code>,
    *    must be a valid file
    * @return the document we have read and parsed, never <code>null</code>
    *    if no exception is thrown.
    * @throws IllegalArgumentException if the rpovided file is
    *    <code>null</code> or not a valid file.
    * @throws SplitterException if anything loading or parsing the file
    *    fails.
    */
   private static Document loadConfigDocument(File file)
      throws SplitterException
   {
      if (file == null || !file.isFile())
         throw new IllegalArgumentException(
            "file cannot be null and must be a valid file");

      try
      {
         InputSource src = new InputSource(new FileReader(file));
         DocumentBuilder db = PSXmlDocumentBuilder.getDocumentBuilder(false);
         return db.parse(src);
      }
      catch (FileNotFoundException e)
      {
         throw new SplitterException(
            "Could not find splitter configuration file: " +
            file.getAbsolutePath() + "\nError message:\n" +
            e.getLocalizedMessage());
      }
      catch (IOException e)
      {
         throw new SplitterException(
            "Could not load splitter configuration file: " +
            file.getAbsolutePath() + "\nError message:\n" +
            e.getLocalizedMessage());
      }
      catch (SAXException e)
      {
         throw new SplitterException(
            "Could not parse splitter configuration file: " +
            file.getAbsolutePath() + "\nError message:\n" +
            e.getLocalizedMessage());
      }
   }

   /**
    * We are not using the server packages because we want to have a stanalone
    * splitter installation without including any server JAR's.
    *
    * see com.percussion.util.PSProperties
    * @throws SplitterException if anything goes wrong loading the config file.
    */
   private static File getConfigFile(String entry, String property, String dir)
      throws SplitterException
   {
      File file = null;
      try
      {
         File propFile = new File(INIT_PROPS);

         if (propFile.exists() && propFile.isFile())
         {
            Properties prop = new Properties();
            prop.load(new FileInputStream(INIT_PROPS));

            String str = prop.getProperty(entry);
            file = new File(str, property);
         }
         else
         {
            // use the default
            file = new File(dir, property);
         }

      }
      catch(FileNotFoundException e)
      {
         throw new SplitterException(e.getLocalizedMessage());
      }
      catch(IOException e)
      {
         throw new SplitterException(e.getLocalizedMessage());
      }

      return file;
   }
   
   /**
    * Sets value for the root directory to use for file path resolution, may be
    * <code>null</code> to use the current directory.
    */
   public static void setRootDir(final File rootDir)
   {
      if (rootDir != null)
      {
         ms_rootDir = rootDir;
      }
   }

   /**
    * Constant for the name of the config file containing name/value pairs of
    * all cofiguration files installed for all modules. Never
    * <code>null</code>.
    */
   private static final String INIT_PROPS = "init.properties";

   /**
    * The splitter properties file where all options editable through
    * the options dialog are persisted. Never <code>null</code>.
    */
   private static final String XSPLIT_PROPS = "xsplit.properties";

   /**
    * The tidy properties file specifies the default configuration used
    * to run tidy. Never <code>null</code>.
    */
   private static final String TIDY_PROPS = "tidy.properties";

   /**
    * Constant for the name of the entry that represents the splitters
    * name/value pairs.
    */
   private static final String ENTRY_NAME = "htmlconverter_config_base_dir";

   /**
    * Constant for the directory containing the splitter configuration files.
    * Never <code>null</code>.
    */
   public static final String XSPLIT_DIR = "rxconfig/XSpLit";

   /**
    * Constant for the serverPageTags.xml configuration file. Never
    * <code>null</code>.
    */
   public static final String SERVER_PAGE_TAGS = "serverPageTags.xml";

   /**
    * Constant for the assemblerConfig.xml configuration file. Never
    * <code>null</code>.
    */
   public static final String ASSEMBLER_CONFIG = "assemblerConfig.xml";

   /**
    * The tag separator used. This will be added to all tags if not provided
    * anyway.
    */
   public static final String TAG_SEPARATOR = "-";

   /**
    * This vector defines all supported XSL output encodings.
    */
   public static final Vector<String> XSL_OUTPUT_ENCODINGS = new Vector<String>();
   static
   {
      XSL_OUTPUT_ENCODINGS.add("ASCII");
      XSL_OUTPUT_ENCODINGS.add("ISO-8859-1");
      XSL_OUTPUT_ENCODINGS.add("ISO-10646-UCS-2");
      XSL_OUTPUT_ENCODINGS.add("UTF-8");
      XSL_OUTPUT_ENCODINGS.add("UTF-16");
      XSL_OUTPUT_ENCODINGS.add("UTF-16BE");
      XSL_OUTPUT_ENCODINGS.add("UTF-16LE");
   }

   /**
    * The document holding the server page tags configuration. The splitter
    * does not initialize if this file cannot be loaded.
    */
   private static Document ms_serverPageTags = null;

   /**
    * The document holding the assembler configuration. The splitter
    * does not initialize if this file cannot be loaded.
    */
   private static Document ms_assemblerConfig = null;

   /**
    * The xsplit properties containing the currently selected options.
    * Initilazed during construction and never <code>null</code> after that.
    * Saved to disk each time a change is made.
    */
   private static Properties ms_xsplitProperties = new Properties();

   /**
    * The xsplit properties file, initialized during construction, never
    * <code>null</code> after that.
    */
   private static File ms_propertyFile;

   /**
    * The tidy properties containing the configuration used to run tidy. Its
    * initialized during construction, never <code>null</code> after that.
    */
   private static Properties ms_tidyConfig = new Properties();
   
   /**
    * Root directory for locating files, initially <code>null</code> to indicate 
    * the current directory, may be overriden during construction.
    */
   private static File ms_rootDir = new File(".");

   /**
    * A flag indicating whether this instance is used from the standalone
    * xsplit product (<code>true</code>) or not (<code>false</code>).
    */
   private static boolean ms_standalone = false;

   /**
    * The default values for all splitter configuration settings, never
    * <code>null</code>.
    */
   private static Properties DEFAULT_PROPERTIES = new Properties();
   static
   {
      DEFAULT_PROPERTIES.setProperty("baseLocation",
         System.getProperty("user.dir"));
      DEFAULT_PROPERTIES.setProperty("outputEncoding",
         PSCharSets.getInternalEncoding());
      DEFAULT_PROPERTIES.setProperty("inputEncoding",
         PSCharSets.DEFAULT_INPUT_ENCODING);
      DEFAULT_PROPERTIES.setProperty("xslOutputEncoding", "UTF-8");
      DEFAULT_PROPERTIES.setProperty("dynamicTag", "psx" + TAG_SEPARATOR);
      DEFAULT_PROPERTIES.setProperty("propertyTag", "rx" + TAG_SEPARATOR);
      DEFAULT_PROPERTIES.setProperty("propertyPrefix", "$");
      DEFAULT_PROPERTIES.setProperty("showWarnings", "yes");
      DEFAULT_PROPERTIES.setProperty("addAssemblerImports", "yes");
   }
}
