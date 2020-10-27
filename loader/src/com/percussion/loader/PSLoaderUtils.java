/*[ PSLoaderUtils.java ]*******************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader;

import com.percussion.loader.objectstore.PSComponentList;
import com.percussion.loader.objectstore.PSContentSelectorDef;
import com.percussion.loader.objectstore.PSExtractorDef;
import com.percussion.loader.objectstore.PSFileSearchRoot;
import com.percussion.loader.objectstore.PSListSelectorDef;
import com.percussion.loader.objectstore.PSLoaderDescriptor;
import com.percussion.loader.objectstore.PSProperty;
import com.percussion.loader.objectstore.PSSearchRoot;
import com.percussion.loader.selector.PSListImporter;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.zip.CRC32;

import org.apache.oro.text.GlobCompiler;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternCompiler;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.Perl5Matcher;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import websphinx.Link;


/**
 * This class contains various convenient utility methods
 */
public class PSLoaderUtils
{

   /**
    * Retreives all extractor names in the specified descriptor
    *
    * @param descrip the descriptor that contains extractors, it may not be
    *    <code>null</code>.
    *
    * @return An iterator over zero or more <code>String</code>. Never
    *    <code>null</code>, but may be empty.
    */
   public static Iterator getAvailableExtractorNames(PSLoaderDescriptor descrip)
   {
      if (descrip == null)
         throw new IllegalArgumentException("descrip must not be null");

      ArrayList names = new ArrayList();
      PSComponentList comps = descrip.getAllExtractorDefs();
      Iterator extractors = comps.getComponents();

      while (extractors.hasNext())
      {
         PSExtractorDef aDef = (PSExtractorDef) extractors.next();
         names.add(aDef.getName());
      }

      return names.iterator();
   }

   /**
    * Get an extractor def that matches the specified name from a given
    * descriptor.
    *
    * @param descrip The descriptor, it may not be <code>null</code>.
    *
    * @param name the searched extractor name. It may not be <code>null</code>
    *    or empty.
    *
    * @return the maching extractor, never <code>null</code>.
    *
    */
   public static PSExtractorDef getExtractorDefFromName(
      PSLoaderDescriptor descrip, String name)
   {
      if (descrip == null)
         throw new IllegalStateException("descrip must not be null");
      if (name == null || name.trim().length() == 0)
         throw new IllegalStateException("name may not be null or empty");

      PSComponentList comps = descrip.getAllExtractorDefs();
      Iterator extractors = comps.getComponents();

      while (extractors.hasNext())
      {
         PSExtractorDef aDef = (PSExtractorDef) extractors.next();
         if (aDef.getName().equals(name))
            return aDef;
      }

      // this should never happen
      throw new RuntimeException(
         "There is no PSExtractorDef with the name of: " + name);
   }
   /**
    * Read the raw data from an input stream object. It will not close the
    * input stream object, it is caller's responsibility to do so.
    *
    * @param in The input stream object, it may not be <code>null</code>
    *
    * @return The retrieved raw data, never <code>null</code>.
    *
    * @throws PSLoaderException if error occurs.
    */
   public static byte[] getRawData(InputStream in) throws java.io.IOException
   {
      if (in == null)
         throw new IllegalArgumentException("in must not be null");

      byte[] buf = null;

      int nExpandingLength = TYPICAL_LENGTH;

      buf = new byte[nExpandingLength];
      int n;
      int nTotal = 0;

      while ((n = in.read(buf, nTotal, buf.length - nTotal)) != -1)
      {
         nTotal += n;

         if (nTotal == buf.length)
         {
            // try to read one more character
            int c = in.read ();
            if (c == -1)
               break; // EOF, we're done
            else
            {
               // need more space in array.  Double the array.
               byte[] newbuf = new byte[buf.length + nExpandingLength];
               System.arraycopy (buf, 0, newbuf, 0, buf.length);
               buf = newbuf;
               buf[nTotal++] = (byte) c;
            }
         }
      }

      if (nTotal != buf.length)
      {
         // resize the array to be precisely nTotal bytes long
         byte[] newbuf = new byte[nTotal];
         System.arraycopy (buf, 0, newbuf, 0, nTotal);
         buf = newbuf;
      }

      return buf;
   }


   /**
    * Calculates the checksum of a data, which is from a input stream.
    *
    * @param in The input steam that contains the to be calculated data,
    *    it may not be <code>null</code>.
    *
    * @return The checksum of the data.
    *
    * @throws PSLoaderException if error occurs.
    */
   public static long calcChecksum(InputStream in) throws java.io.IOException
   {
      if (in == null)
         throw new IllegalArgumentException("in must not be null");

        byte[] data = getRawData(in);
        return calcChecksum(data);
   }

   /**
    * Calculates the checksum from the specified data.
    *
    * @param data The data that is used for the checksum calculation. It may
    *    not be <code>null</code>.
    *
    * @return The calculated checksum
    */
   public static long calcChecksum(byte[] data)
   {
      if (data == null)
         throw new IllegalArgumentException("data must not be null");

      CRC32 cs = new CRC32();
      cs.update(data);
      return cs.getValue();
   }

   /**
    * A convenient method, call {@link #getProperty(String, Iterator, true)}.
    */
   public static PSProperty getProperty(String name, Iterator properties)
      throws PSLoaderException
   {
      return getProperty(name, properties, true);
   }

   /**
    * A convenient method, call {@link #getProperty(String, Iterator, false)}.
    */
   public static PSProperty getOptionalProperty(String name, 
      Iterator properties)
   {
      PSProperty property = null;
      try
      {
         property = getProperty(name, properties, false);
      }
      catch (PSLoaderException ignore)
      {
         ignore.printStackTrace(); // should never happen
         throw new RuntimeException("caught unexpected exception: " + 
            ignore.toString());
      }
      
      return property;
   }
   
   /**
    * Get the value of an optional property.
    * 
    * @param name The name of the property, it may not be <code>null</code> or
    *    empty.
    * 
    * @param properties An iterator over zero or more <code>PSProperty</code>
    *    objects. It may not be <code>null</code>.
    *    
    * @return The value of the property. It may be <code>null</code> if the
    *    property not exist.
    */
   public static String getOptionalPropertyValue(String name, 
      Iterator properties)
   {
      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name may be not null or empty");
      if (properties == null)
         throw new IllegalArgumentException("properties may not be null");
         
      PSProperty prop = getOptionalProperty(name, properties);
      return (prop == null) ? null : prop.getValue();
   }
   
  /**
   * Get a specified property from a property list.
   *
   * @param name The name of the to be searched property, may not
   *    <code>null</code> or empty.
   * 
   * @param properties The property list, iterator over zero or more
   *    <code>PSProperty</code> objects. It may not be <code>null</code>.
   *
   * @param required <code>true</code> if the searched property is required.
   * 
   * @return The specified property, never <code>null</code> if the searched
   *    property is required.
   *
   * @throws PSLoaderException if cannot find a required property in the 
   *    property list.
   */
   public static PSProperty getProperty(String name, Iterator properties, 
      boolean required)
      throws PSLoaderException
   {
      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name may not be null or empty");

      if (properties == null)
         throw new IllegalArgumentException("perperties may not be null");

      while (properties.hasNext())
      {
         PSProperty prop = (PSProperty)properties.next();
         if (prop.getName().equals(name))
            return prop;
      }
      
      if (required)
         throw new PSLoaderException(IPSLoaderErrors.MISSING_PROPERTY, name);
      else
         return null;
   }      

   /**
    * Creates a URL from the specified file path.
    *
    * @param filePath The file path that is used to create the URL. It may not
    *    be <code>null</code> or empty.
    *
    * @return The created URL, never <code>null</code>.
    *
    * @throws PSLoaderException if an error occurs.
    */
   public static URL getURLFromFilePath(String filePath)
      throws PSLoaderException
   {
      if (filePath == null || filePath.trim().length() == 0)
         throw new IllegalArgumentException(
            "filePath may not be null or empty");

      try
      {
         if (!filePath.startsWith("file://"))
         {
            filePath = filePath.replace('\\', '/');
            return new URL("file", "localhost", "/" + filePath);
         }
         else
            return new URL(filePath);
         }
      catch (MalformedURLException e)
      {
         throw new PSLoaderException(IPSLoaderErrors.UNEXPECTED_ERROR,
            e.toString());
      }
   }

   /**
    * Determines whether the specified link is relative to root.
    * The format of the (inline) link that is relative to root would looks
    * like: "/web_resource/image/foo.gif". However, the inline link that is
    * is relative to current page would looks like: "image/foo.gif".
    *
    * @param link The to be tested link, it may not be <code>null</code>.
    *
    * @return <code>true</code> if the link is relative to the root; otherwise
    *    return <code>false</code> if it is relative to the current page.
    */
   public static boolean isRelativeToRoot(Link link)
   {
      if (link == null)
         throw new IllegalArgumentException("link may not be null");

      String hrefAttr = link.getHTMLAttribute("href");
      if (hrefAttr != null)
      {
         hrefAttr = hrefAttr.trim();
         return (hrefAttr.length() > 0) && (hrefAttr.charAt(0) == '/');
      }
      else
      {
         String imgAttr = link.getHTMLAttribute("src");
         if (imgAttr != null)
         {
            imgAttr = imgAttr.trim();
            return (imgAttr.length() > 0) && (imgAttr.charAt(0) == '/');
         }
         else
         {
            return false;
         }
      }
   }

   /**
    * Determines whether the specified URL and root URL are the same kind in
    * terms of protocol, port and host.
    *
    * @param url the tested URL, may not be <code>null</code>.
    * @param rootUrl the root URL, may not be <code>null</code>.
    *
    * @return <code>true</code> if both URl have the same protocol, port and
    *    host; otherwise return <code>false</code>.
    */
   public static boolean isSameKindURL(URL url, URL rootUrl)
   {
      if (url == null)
         throw new IllegalArgumentException("url may not be null");
      if (rootUrl == null)
         throw new IllegalArgumentException("rootUrl may not be null");

      return url.getHost().equals(rootUrl.getHost()) &&
             url.getProtocol().equals(rootUrl.getProtocol()) &&
             url.getPort() == rootUrl.getPort();
   }
   /**
    * Creates an absolute URL from the specified base URL and the relative URL,
    * which is relative to the base URL.
    * <p>
    * For example, if the base URL is: "file://localhost/C:/baseDir",
    * and the relative URL is: "/images/foo.gif", then the absolute URL is:
    * "file://localhost/C:/baseDir/images/foo.gif".
    * </p>
    * @param baseURL The base URL, it may not be <code>null</code>.
    *
    * @param currURL The relative URL, it may not be <code>null</code>.
    *
    * @return The created URL, never <code>null</code>.
    *
    * @throws PSLoaderException if an error occurs.
    */
   public static URL getAbsoluteURL(URL baseURL, URL currURL)
      throws PSLoaderException
   {
      if (baseURL == null)
         throw new IllegalArgumentException("baseURL may not be null");
      if (currURL == null)
         throw new IllegalArgumentException("currURL may not be null");

      try
      {
         String root = baseURL.getFile();
         int index = root.lastIndexOf("/");
         String rootName = null;
         if (index > 0)
            rootName = root.substring(index +1, root.length());

         String cfile = currURL.getFile();
         String relativeURL = rootName != null ? rootName + cfile : cfile;
         return new URL(baseURL, relativeURL);
      }
      catch (MalformedURLException e)
      {
         throw new PSLoaderException(IPSLoaderErrors.UNEXPECTED_ERROR,
            e.toString());
      }
   }

   /**
    * Determines whether a regular expression matches a pattern.
    *
    * @param regex  The to be compared regular expression, may not be
    *    <code>null</code>, but may be empty.
    *
    * @param patternString The to be compared pattern string, may not be
    *    <code>null</code>, but may be empty.
    *
    * @return <code>true</code> if matches; <code>false</code> otherwise.
    */
   public static boolean matchRegExpression(String regex, String patternString)
   {
      if (regex == null)
         throw new IllegalArgumentException("regex may not be null");
      if (patternString == null)
         throw new IllegalArgumentException("patternString may not be null");

      /**
       * Initialization of compiler, matcher
       */
      PatternCompiler compiler = new GlobCompiler();
      PatternMatcher matcher  = new Perl5Matcher();

      boolean doesMatch = false;

      try
      {
         Pattern pattern = compiler.compile(patternString);
         doesMatch = matcher.matches(regex, pattern);
      }
      catch (Exception e)
      {
         doesMatch = false;
      }

      return doesMatch;
   }


   /**
    * Given a PSLoaderComponent configuration object it retreives
    * the root url based on a possible child url.
    *
    * @param def The selector definition to retrieve the root url
    *    from. Never <code>null</code>.
    *
    * @param strResId String resource id to check if it's a child.
    *    Never <code>null</code> or empty.
    *
    * @return String the root url to use. May be <code>null</code> if no
    *    root url exists.
    *
    * @throws IllegalArgumentException if parameters are invalid.
    */
   public static String getRootUrlFromDesc(PSContentSelectorDef def,
      String strResId)
   {
      if (def == null || strResId == null)
         throw new IllegalArgumentException(
            "strResId and def must not be null");

      if (strResId.trim().length() < 1)
         throw new IllegalArgumentException(
            "strResId must not be empty");

      ArrayList l = new ArrayList();
      Iterator  iter = def.getSearchRoots();
      String strRtn = null;

      while (iter.hasNext())
      {
         PSSearchRoot psRoot = (PSSearchRoot) iter.next();
         String strVal = psRoot.getProperty(
            PSFileSearchRoot.XML_SEARCHROOT_NAME).getValue();

         if (strResId.toLowerCase().indexOf(strVal.toLowerCase()) >= 0)
         {
            strRtn = strVal;
            break;
         }
      }

      // If there are no search roots at this point,
      // then it may be the tree derived from a list import.
      if (strRtn == null)
      {
         try
         {
            // Attempt to parse configuration for a list importing
            // which has no search roots but the path to the xml file.
            PSProperty psProp = PSLoaderUtils.getProperty(
               PSListSelectorDef.CONTENT_LIST, def.getProperties());

            Iterator roots = PSLoaderUtils.getListRoots(psProp.getValue());

            while (roots.hasNext())
            {
               String strVal = (String) roots.next();
               if (strResId.toLowerCase().indexOf(strVal.toLowerCase()) >= 0)
                  strRtn = strVal;
            }
         }
         catch (PSLoaderException e)
         {
            return null;
         }
      }

      if (strRtn == null)
         return null;

      // Generate a valid url from the specified root
      try
      {
         java.net.URL url = null;

         if (!strRtn.startsWith("file://")
            && !strRtn.startsWith("http://")
            && !strRtn.startsWith("https://")
            && !strRtn.startsWith("ftp://"))
         {
            url = new java.net.URL(
               "file", "localhost", "/" + strRtn);
         }
         else
         {
            url = new java.net.URL(strRtn);
         }

         strRtn = url.toString();
      }
      catch (Exception e)
      {
         return null;
      }

      return strRtn;
   }

   /**
    * Given a ListContentSelector xml file this method will
    * extract the root url for each root defined and return an
    * Iterator over those url Strings.
    *
    * @param strPath a full path to the xml file to load.
    *    Never <code>null</code> or empty.
    *
    * @throws IllegalArgumentException if <code>strPath</code> is
    *    invalid.
    *
    * @throws PSLoaderException if an error occurs when retrieving the xml file.
    */
   public static Iterator getListRoots(String strPath)
      throws PSLoaderException
   {
      if (strPath == null || strPath.trim().length() < 1)
         throw new IllegalArgumentException(
            "strPath must not be null or empty");

      ArrayList l = new ArrayList();
      Document doc = null;
      FileInputStream in = null;

      try
      {
         in = new FileInputStream(strPath);
         doc = PSXmlDocumentBuilder.createXmlDocument(in, false);
      }
      catch (Exception e)
      {
         throw new PSLoaderException(IPSLoaderErrors.UNEXPECTED_ERROR,
            e.toString());
      }
      finally
      {
         try {
            if (in != null)
               in.close();
         }
         catch (Exception ioe) {}
      }

      NodeList servers = doc.getElementsByTagName(
         PSListImporter.XML_NODE_SERVER);

      if (servers.getLength() < 1)
         throw new IllegalStateException(
            "Invalid xml missing: " + PSListImporter.XML_NODE_SERVER);

      for (int i=0; i<servers.getLength(); i++)
      {
         try
         {
            Element server = (Element) servers.item(i);
            NodeList roots = server.getElementsByTagName(
               PSListImporter.XML_NODE_ROOT);

            if (roots.getLength() < 1)
               throw new PSLoaderException(
                  IPSLoaderErrors.MISSING_PROPERTY,
                  PSListImporter.XML_NODE_ROOT);

            for (int j=0; j<roots.getLength(); j++)
            {
               Element root = (Element) roots.item(j);
               String url = root.getAttribute(PSListImporter.XML_ATTR_ROOT);
               if (url != null && url.length() != 0)
                  l.add(url);
               else
                  throw new PSLoaderException(
                  IPSLoaderErrors.ROOT_ATTRIB_MISSING);
            }
         }
         catch (PSLoaderException e)
         {
            throw new IllegalStateException(e.getMessage());
         }
      }

      return l.iterator();
   }

   /**
    * Method returns the host name from a given url.
    *
    * @param strUrl a string url. Never <code>null</code> or
    *    empty.
    *
    * @return String host name. Never <code>null</code> or empty.
    *
    * @throws IllegalArgumentException if <code>strUrl</code> is
    *    invalid.
    *
    * @throws MalformedURLException if <code>strUrl</code> is not a
    *    valid url.
    */
   public static String getHostFromURL(String strUrl)
      throws MalformedURLException
   {
      if (strUrl == null || strUrl.trim().length() == 0)
         throw new IllegalArgumentException(
            "strUrl must not be null or empty");

      URL url = new URL(strUrl);
      return url.getHost();
   }

   /**
    * Method returns the file path of the url. For example,
    * given 'http://host.com/services/path/test.htm' this method
    * will return 'services/path'.
    *
    * @param strUrl a string url. Never <code>null</code> or
    *    empty.
    *
    * @return String the path. Never <code>null</code> or empty.
    *
    * @throws IllegalArgumentException if <code>strUrl</code> is
    *    invalid.
    *
    * @throws MalformedURLException if <code>strUrl</code> is not a
    *    valid url.
    */
   public static String getFilePathFromURL(String strUrl)
      throws MalformedURLException
   {
      if (strUrl == null || strUrl.trim().length() == 0)
         throw new IllegalArgumentException(
            "strUrl must not be null or empty");

      URL url = new URL(strUrl);
      return url.getPath();
   }

   /**
    * Method returns the file name of the url. For example,
    * given 'http://host.com/services/path/test.htm' this method
    * will return 'test.htm'.
    *
    * @param strUrl a string url. Never <code>null</code> or
    *    empty.
    *
    * @return String the file name. Never <code>null</code> or empty.
    *
    * @throws IllegalArgumentException if <code>strUrl</code> is
    *    invalid.
    *
    * @throws MalformedURLException if <code>strUrl</code> is not a
    *    valid url.
    */
   public static String getFile(String strUrl)
      throws MalformedURLException
   {
      if (strUrl == null || strUrl.trim().length() == 0)
         throw new IllegalArgumentException(
            "strUrl must not be null or empty");

      URL url = new URL(strUrl);
      String strFile = url.getFile();

      if (strFile.endsWith("/"))
      {
         strFile = strFile.substring(0, strFile.length() -1);
      }
      else if (strFile.endsWith("\\"))
      {
         strFile = strFile.substring(0, strFile.length() -1);
      }

      int nIndex = strFile.lastIndexOf("/");

      if (nIndex > 0)
      {
         return strFile.substring(nIndex+1, strFile.length());
      }

      nIndex = strFile.lastIndexOf("\\");

      if (nIndex > 0)
      {
         return strFile.substring(nIndex+1, strFile.length());
      }

      throw new IllegalArgumentException("strUrl is not well formed");
   }

   /**
    * Method returns the mimetype of the specified url.
    *
    * @param strUrl a string url. Never <code>null</code> or
    *    empty.
    *
    * @return String the file name. Never <code>null</code> or empty.
    *
    * @throws IllegalArgumentException if <code>strUrl</code> is
    *    invalid.
    *
    * @throws MalformedURLException if <code>strUrl</code> is not a
    *    valid url.
    */
   public static String getMimeType(String strUrl)
      throws MalformedURLException, IOException
   {
      if (strUrl == null || strUrl.trim().length() == 0)
         throw new IllegalArgumentException(
            "strUrl must not be null or empty");

      URL url = new URL(strUrl);
      URLConnection conn = url.openConnection();
      conn.connect();
      return conn.getContentType();
   }

   /**
    * Retrieves the root urls from a config object and returns an iterator
    * over string roots.
    *
    * @return Iterator over string representation of the root urls.
    */
   public static Iterator getRoots(PSContentSelectorDef def)
      throws PSConfigurationException
   {
      if (def == null)
         throw new IllegalArgumentException(
            "def must not be null");

      ArrayList l = new ArrayList();
      Iterator  iter = def.getSearchRoots();

      while (iter.hasNext())
      {
         PSSearchRoot psRoot = (PSSearchRoot) iter.next();
         String strVal = psRoot.getProperty(
            PSFileSearchRoot.XML_SEARCHROOT_NAME).getValue();

         l.add(strVal);
      }

      // If there are no search roots at this point,
      // then it may be the tree derived from a list import.
      if (l.size() < 1)
      {
         try
         {
            // Attempt to parse configuration for a list importing
            // which has no search roots but the path to the xml file.
            PSProperty psProp = PSLoaderUtils.getProperty(
               PSListSelectorDef.CONTENT_LIST, def.getProperties());

            Iterator roots = PSLoaderUtils.getListRoots(psProp.getValue());
            return roots;
         }
         catch (PSLoaderException e)
         {
            throw new PSConfigurationException
               (IPSLoaderErrors.UNEXPECTED_ERROR,
               new Object []
               {
                  e.getMessage()
               });
         }
      }
      else
      {
         return l.iterator();
      }
   }

   // private static constants
   // May be placed somewhere else in the sense that
   // calcChecksum itself could be static anywhere and
   // that is the only use so far.
   private static final int TYPICAL_LENGTH = 20240;

}
