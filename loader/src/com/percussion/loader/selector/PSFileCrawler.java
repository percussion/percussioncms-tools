/*[ PSFileCrawler.java ]*******************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader.selector;

import com.percussion.loader.PSLoaderException;
import com.percussion.loader.PSLoaderUtils;
import com.percussion.loader.objectstore.PSFileSearchRoot;
import com.percussion.loader.objectstore.PSFilter;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternCompiler;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;

import websphinx.Link;


/**
 * This class is similar with PSCrawler, but it is implemented to crawl the file
 * system by recursively scan the directories.
 * @see {@link com.percussion.loader.IPSSelector} for general description.
 */
public class PSFileCrawler extends PSCommonCrawler
{
   /**
    * Constructs an instance of this class.
    *
    * @param calcChecksum Indicate whether the checksum will be calculated
    *    by the file crawler. <code>true</code> the checksum will be
    *    calculated; <code>false</code> the checksum will not be calculated.
    */
   public PSFileCrawler(boolean calcChecksum)
   {
      super(calcChecksum);
   }

   /**
    * Start crawling.  Returns either when the crawl is done, or
    * when stop() is called.  Because this method implements the
    * java.lang.Runnable interface, a crawler can be run in the
    * background thread.
    */
   public void run()
   {
      sendStatusEvent(PSCrawlEvent.STARTED);

      try
      {
         init();

         Iterator searchRoots = m_searchRoots.iterator();

         while (searchRoots.hasNext())
         {
            PSFileSearchRoot searchRoot = (PSFileSearchRoot)searchRoots.next();
            SearchParams sr = new SearchParams(searchRoot);
            File dir = new File(searchRoot.getSearchRoot());

            if (dir.isDirectory() && dir.exists())
            {
               scanDirectory(dir, sr);
            }
            else
            {
               Logger.getLogger(getClass().getName()).error(
                  "skip non-existed directory: " + searchRoot.getSearchRoot());
            }
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
         System.out.println(e.toString());
      }

      sendStatusEvent(PSCrawlEvent.STOPPED);
   }

   /**
    * Initialize the crawler for each fresh scanning.
    *
    * @throws PSLoaderException if an error occurs
    */
   private void init()
      throws PSLoaderException
   {
      initRun();
      m_totalCrawledDirectories = 0;
      initSearchRootLinks();
   }

   /**
    * Creates a list of links from the defined search root in the file
    * selector definition.
    *
    * @throws PSLoaderException if an error occurs.
    */
   private void initSearchRootLinks()
      throws PSLoaderException
   {
      m_searchRootLinks = new Link[m_searchRoots.size()];

      Iterator searchRoots = m_searchRoots.iterator();
      PSFileSearchRoot searchRoot = null;
      int i = 0;

      while (searchRoots.hasNext() && (!shouldStop()))
      {
         searchRoot = (PSFileSearchRoot)searchRoots.next();
         m_searchRootLinks[i++] =
            new Link(
               PSLoaderUtils.getURLFromFilePath(searchRoot.getSearchRoot()));
      }
   }

   /**
    * Scans the specified directory.
    *
    * @param dir the to be scanned directory, assume not <code>null</code>.
    *
    * @param params the search parameters, assume not <code>null</code>.
    */
   private void scanDirectory(File dir, SearchParams params)
   {
      m_totalCrawledDirectories++;

      File[] files = dir.listFiles();

      for (int i = 0; (i < files.length) && (!shouldStop()); i++)
      {
         if (files[i].isDirectory())
         {
            if (params.m_doRecurse)
            {
               scanDirectory(files[i], params);
            }
         }
         else
         {
            try
            {
               URL fileurl =
                  PSLoaderUtils.getURLFromFilePath(files[i].getAbsolutePath());

               if (!isValidFilePath(
                        fileurl.getPath(),
                        params))
               {
                  continue;
               }

               processLink(
                  new Link(fileurl),
                  params.m_rootLink.getURL(),
                  (Object)params);
            }
            catch (Exception e)
            {
               e.printStackTrace();
               Logger.getLogger(getClass().getName()).error(e.toString());
            }
         }
      }
   }

   /**
    * Determines whether the specified file path matches filters / patterns
    * that is defined in the selector definition.
    *
    * @param filePath the tested file path, assume not <code>null</code>.
    *
    * @param params The parameter that contains the patterns, assume not
    *    <code>null</code>.
    *
    * @return <code>true</code> if the file path matches one of the patterns.
    */
   private boolean isValidFilePath(String filePath, SearchParams params)
   {
      for (int i = 0; i < params.m_patterns.length; i++)
      {
         try
         {
            if (matchPathPattern(filePath, params.m_patterns[i]))
            {
               return true;
            }
         }
         catch (Exception e)
         {
            e.printStackTrace();

            break;
         }
      }

      return false;
   }

   /* Empty implementation of  IPSSelector method   */
   public boolean isValidLink(Link link, Object ctx)
   {
      return false;
   }

   /**
    * Configure the file crawler with additional search root.
    *
    * @param searchRoot to additional search root, it may not be
    *    <code>null</code>.
    */
   void addSearchRoot(PSFileSearchRoot searchRoot)
   {
      if (searchRoot == null)
      {
         throw new IllegalArgumentException("searchRoot may not be null");
      }

      m_searchRoots.add(searchRoot);
   }

   /**
    * @return the total number of crawled directories that has been done so far.
    */
   public int getTotalCrawledDirectories()
   {
      return m_totalCrawledDirectories;
   }

   /**
    * Matches a string against a regular expression created from thr Glob type
    * expression passed in.
    *
    * @param regex  The string to be compared regular expression, may not be
    *    <code>null</code>, but may be empty.
    *
    * @param patternString The to be compared pattern string, may not be
    *    <code>null</code>, but may be empty.
    *
    * @return <code>true</code> if matches; <code>false</code> otherwise.
    */
   public static boolean matchPathPattern(String regex, String patternString)
   {
      if (regex == null)
      {
         throw new IllegalArgumentException("regex may not be null");
      }

      if (patternString == null)
      {
         throw new IllegalArgumentException("patternString may not be null");
      }

      /*
       * Initialization of compiler, matcher
       */
      PatternCompiler compiler = new Perl5Compiler();
      PatternMatcher matcher = new Perl5Matcher();

      boolean doesMatch = false;

      try
      {
         Pattern pattern =
            compiler.compile(
               globToRegex(patternString),
               Perl5Compiler.CASE_INSENSITIVE_MASK);
         doesMatch = matcher.matches(regex, pattern);
      }
      catch (Exception e)
      {
         e.printStackTrace();
         doesMatch = false;
      }

      return doesMatch;
   }

   /**
    * Converts the Glob type wildcard characters into valid
    * Perl 5 regular expression syntax. This is an extended version of
    * Glob wildcards as it also contains the ** wildcard which comes from
    * Apache Ant.
    *
    * <b><font FACE="Courier New" SIZE="2" COLOR="#4040c2">
    *<p>Wildcards</font></b><font FACE="Courier New" SIZE="2" COLOR="#4040c2">:</p>
    *<b>
    *<p>?</b></font><font FACE="Courier New" SIZE="2"> </font><font FACE="Courier New" SIZE="2" COLOR="#4040c2">=</font><font FACE="Courier New" SIZE="2">
    *</font><font FACE="Courier New" SIZE="2" COLOR="#4040c2">match</font><font FACE="Courier New" SIZE="2">
    *</font><font FACE="Courier New" SIZE="2" COLOR="#4040c2">any</font><font FACE="Courier New" SIZE="2">
    *</font><font FACE="Courier New" SIZE="2" COLOR="#4040c2">one</font><font FACE="Courier New" SIZE="2">
    *</font><font FACE="Courier New" SIZE="2" COLOR="#4040c2">character</font><font FACE="Courier New" SIZE="2">
    *</font><font FACE="Courier New" SIZE="2" COLOR="#4040c2">(considers</font><font FACE="Courier New" SIZE="2">
    *</font><font FACE="Courier New" SIZE="2" COLOR="#4040c2">the</font><font FACE="Courier New" SIZE="2">
    *</font><font FACE="Courier New" SIZE="2" COLOR="#4040c2">path</font><font FACE="Courier New" SIZE="2">
    *</font><font FACE="Courier New" SIZE="2" COLOR="#4040c2">separator)</p>
    *<b>
    *<p>*</b></font><font FACE="Courier New" SIZE="2"> </font><font FACE="Courier New" SIZE="2" COLOR="#4040c2">=</font><font FACE="Courier New" SIZE="2">
    *</font><font FACE="Courier New" SIZE="2" COLOR="#4040c2">matches</font><font FACE="Courier New" SIZE="2">
    *</font><font FACE="Courier New" SIZE="2" COLOR="#4040c2">as</font><font FACE="Courier New" SIZE="2">
    *</font><font FACE="Courier New" SIZE="2" COLOR="#4040c2">many</font><font FACE="Courier New" SIZE="2">
    *</font><font FACE="Courier New" SIZE="2" COLOR="#4040c2">characters</font><font FACE="Courier New" SIZE="2">
    *</font><font FACE="Courier New" SIZE="2" COLOR="#4040c2">as</font><font FACE="Courier New" SIZE="2">
    *</font><font FACE="Courier New" SIZE="2" COLOR="#4040c2">possible,</font><font FACE="Courier New" SIZE="2">
    *</font><font FACE="Courier New" SIZE="2" COLOR="#4040c2">but</font><font FACE="Courier New" SIZE="2">
    *</font><font FACE="Courier New" SIZE="2" COLOR="#4040c2">can</font><font FACE="Courier New" SIZE="2">
    *</font><font FACE="Courier New" SIZE="2" COLOR="#4040c2">match</font><font FACE="Courier New" SIZE="2">
    *</font><font FACE="Courier New" SIZE="2" COLOR="#4040c2">0</font><font FACE="Courier New" SIZE="2"></p>
    *<p></font><font FACE="Courier New" SIZE="2" COLOR="#4040c2">(considers</font><font FACE="Courier New" SIZE="2">
    *</font><font FACE="Courier New" SIZE="2" COLOR="#4040c2">the</font><font FACE="Courier New" SIZE="2">
    *</font><font FACE="Courier New" SIZE="2" COLOR="#4040c2">path</font><font FACE="Courier New" SIZE="2">
    *</font><font FACE="Courier New" SIZE="2" COLOR="#4040c2">separator)</p>
    *<b>
    *<p>**</b></font><font FACE="Courier New" SIZE="2"> </font><font FACE="Courier New" SIZE="2" COLOR="#4040c2">=</font><font FACE="Courier New" SIZE="2">
    *</font><font FACE="Courier New" SIZE="2" COLOR="#4040c2">matches</font><font FACE="Courier New" SIZE="2">
    *</font><font FACE="Courier New" SIZE="2" COLOR="#4040c2">as</font><font FACE="Courier New" SIZE="2">
    *</font><font FACE="Courier New" SIZE="2" COLOR="#4040c2">many</font><font FACE="Courier New" SIZE="2">
    *</font><font FACE="Courier New" SIZE="2" COLOR="#4040c2">characters</font><font FACE="Courier New" SIZE="2">
    *</font><font FACE="Courier New" SIZE="2" COLOR="#4040c2">as</font><font FACE="Courier New" SIZE="2">
    *</font><font FACE="Courier New" SIZE="2" COLOR="#4040c2">possible,</font><font FACE="Courier New" SIZE="2">
    *</font><font FACE="Courier New" SIZE="2" COLOR="#4040c2">but</font><font FACE="Courier New" SIZE="2">
    *</font><font FACE="Courier New" SIZE="2" COLOR="#4040c2">can</font><font FACE="Courier New" SIZE="2">
    *</font><font FACE="Courier New" SIZE="2" COLOR="#4040c2">match</font><font FACE="Courier New" SIZE="2">
    *</font><font FACE="Courier New" SIZE="2" COLOR="#4040c2">0</font><font FACE="Courier New" SIZE="2"></p>
    *<p></font><font FACE="Courier New" SIZE="2" COLOR="#4040c2">(ignores</font><font FACE="Courier New" SIZE="2">
    *</font><font FACE="Courier New" SIZE="2" COLOR="#4040c2">the</font><font FACE="Courier New" SIZE="2">
    *</font><font FACE="Courier New" SIZE="2" COLOR="#4040c2">path</font><font FACE="Courier New" SIZE="2">
    *</font><font FACE="Courier New" SIZE="2" COLOR="#4040c2">separator)</font><font FACE="Courier New" SIZE="2">
    *</font><font FACE="Courier New" SIZE="2" COLOR="#4040c2">used</font><font FACE="Courier New" SIZE="2">
    *</font><font FACE="Courier New" SIZE="2" COLOR="#4040c2">for</font><font FACE="Courier New" SIZE="2">
    *</font><font FACE="Courier New" SIZE="2" COLOR="#4040c2">subfolder</font><font FACE="Courier New" SIZE="2">
    *</font><font FACE="Courier New" SIZE="2" COLOR="#4040c2">matching</p>
    *<p>The wildcard matches are case insensitive.</p>
    *</font><b><font FACE="Times New Roman">
    *<p>Examples:</p>
    *</font></b><font FACE="Times New Roman">
    *<p>Based on the following file structure:</p>
    *<b>
    *<p>Root:</b> c:\Inetpub\wwwroot</p>
    *</font><font FACE="Courier New" SIZE="2">
    *<p>\Certification</p>
    *<p>+---aircraft</p>
    *<p>+---AgingAircraft</p>
    *<p>| \---SSID</p>
    *<p>+---AIRProposed</p>
    *<p>| \---images</p>
    *<p>+---air_banner_files</p>
    *<p>+---air_nav_files</p>
    *<p>+---anminfo</p>
    *<p>| \---document</p>
    *<p>| \---final</p>
    *<p>| \---ARACESF</p>
    *<p>| \---ARACESF_Files</p>
    *<p>+---database</p>
    *<p>+---employment</p>
    *<p>+---engine_sat</p>
    *<p>+---images</p>
    *<p>+---SFAR88</p>
    *<p>| +---020320-Seminar</p>
    *<p>| +---020826-Seminar</p>
    *<p>| +---021119-Seminar</p>
    *<p>| +---BoeingOpMtg</p>
    *<p>| \---images</p>
    *<p>+---strategic_plan</p>
    *<p>\---TAD</p>
    *<p>\---Content</p>
    *<p>+---SEA-ACO</p>
    *<p>\---StreamSeatCert</p>
    *</font>
    *<table CELLSPACING="0" BORDER="1" CELLPADDING="2" WIDTH="617">
    *  <tr>
    *    <td WIDTH="52%"><b><font FACE="Times New Roman">
    *      <p>Filter Pattern</font></b></td>
    *    <td WIDTH="48%"><b><font FACE="Times New Roman">
    *      <p>Returns</font></b></td>
    *  </tr>
    *  <tr>
    *    <td WIDTH="52%"><font FACE="Times New Roman">
    *      <p>&#42;&#42;/s*.pdf</font></td>
    *    <td WIDTH="48%"><font FACE="Times New Roman">
    *      <p>Any &quot;pdf&quot; file that starts with &quot;s&quot; under <i>wwwroot</i>
    *      folder or any of it’s subfolders</font></td>
    *  </tr>
    *  <tr>
    *    <td WIDTH="52%"><font FACE="Times New Roman">
    *      <p>Certification/aircraft/&#42;&#42;/*.txt</font></td>
    *    <td WIDTH="48%"><font FACE="Times New Roman">
    *      <p>Any text file under any subfolder of the <i>aircraft</i> folder</font></td>
    *  </tr>
    *  <tr>
    *    <td WIDTH="52%"><font FACE="Times New Roman">
    *      <p>Certification/aircraft/&#42;&#42;/image?.*</font></td>
    *    <td WIDTH="48%"><font FACE="Times New Roman">
    *      <p>Any file that starts with &quot;image&quot; and then has exactly one of
    *      any character followed by . [any extension] and is located under any
    *       subfolder of aircraft.</p>
    *      <p>Examples:</p>
    *      <p>image1.gif</p>
    *      <p>imageA.gif</p>
    *      <p>&nbsp;</p>
    *      <p>image44.gif (will not be returned)</font></td>
    *  </tr>
    *  <tr>
    *    <td WIDTH="52%"><font FACE="Times New Roman">
    *      <p>Certification/aircraft/&#42;/*.html</font></td>
    *    <td WIDTH="48%"><font FACE="Times New Roman">
    *      <p>Any HTML file under any first level subfolder of the aircraft folder</font></td>
    *  </tr>
    *  <tr>
    *    <td WIDTH="52%"><font FACE="Times New Roman">
    *      <p>Certification/aircraft/database/*</font></td>
    *    <td WIDTH="48%"><font FACE="Times New Roman">
    *      <p>Any file under the database folder</font></td>
    *  </tr>
    *  <tr>
    *    <td WIDTH="52%"><font FACE="Times New Roman">
    *      <p>Certification/aircraft/*.gif</font></td>
    *    <td WIDTH="48%"><font FACE="Times New Roman">
    *      <p>Any &quot;gif&quot; file directly under the aircraft folder.</font></td>
    *  </tr>
    *</table>
    *
    *
    * @param pattern the pattern to convert, assumed not <code>null</code>.
    *
    * @return the converted pattern string.
    */
   private static String globToRegex(String pattern)
   {
      StringBuffer sb = new StringBuffer();
      String current = null;
      String following = null;
      String rawPattern = pattern;

      // Loop through the pattern string and replace the wildcard characters
      // with valid regular expressions.
      // To see more info about regular expression goto:
      // http://etext.lib.virginia.edu/helpsheets/regex.html
      for (int i = 0; i < pattern.length(); i++)
      {
         current = pattern.substring(i, i + 1);
         following =
            (i < (pattern.length() - 1))
            ? pattern.substring(i + 1, i + 2)
            : null;

         if (current.equals("?"))
         {
            sb.append("[^\\/]{1}");
         }
         else if (current.equals("*"))
         {
            if ((following != null) && following.equals("*"))
            {
               sb.append(".*");
               i++; // Skip to next char after second asterisk
            }
            else
            {
               sb.append("[^\\/]*");
            }
         }
         else if (current.equals("/"))
         {
            sb.append("\\/");
         }
         else if (current.equals("."))
         {
            sb.append("\\.");
         }
         else
         {
            sb.append(current);
         }
      }

      return sb.toString();
   }

   /**
    * See getTotalCrawlerDirectories() for description.
    */
   private int m_totalCrawledDirectories;

   /**
    * A list of search root, never <code>null</code>, but may be empty.
    */
   private Link[] m_searchRootLinks = new Link[0];

   /**
    * A list of file search root, <code>PSFileSearchRoot</code>, objects
    */
   private List m_searchRoots = new ArrayList();

   /**
    * Vector of {@ link com.percussion.loader.selector.PSRegExObjectFilter}.
    */
   private Vector m_vFilters = new Vector();

   /**
    * {@link #PSLinkExtractorFactory} that retrieves the proper
    * link extractor per Page based on its content type.
    */
   private PSLinkExtractorFactory m_lExFactory = new PSLinkExtractorFactory();

   /**
    * convenient innder class, contains data obtained from the file selector
    * definition.
    */
   private class SearchParams
   {
      /**
       * Creates the parameter from the specified file search root, assume not
       * <code>null</code>.
       */
      SearchParams(PSFileSearchRoot sr)
         throws PSLoaderException
      {
         m_doRecurse = sr.doRecurse();

         URL url = PSLoaderUtils.getURLFromFilePath(sr.getSearchRoot());
         m_rootLink = new Link(url);
         m_patterns = getPatterns(
               m_rootLink.getFile(),
               sr.getFilters());
      }

      /**
       * Get matching patterns from a base and a list of filters
       *
       * @param base The base used to create the matching patterns, it is
       *    the search root, assume not <code>null</code>.
       *
       * @param filters A list of <code>PSFilter</code> objects, assume not
       *    <code>null</code>, but may be empty.
       *
       * @return Created patterns array, never <code>null</code>, but may be
       *    empty.
       */
      private String[] getPatterns(String base, Iterator filters)
      {
         List fltList = new ArrayList();

         while (filters.hasNext())
            fltList.add(filters.next());

         String[] patterns = new String[fltList.size()];
         filters = fltList.iterator();

         for (int i = 0; i < patterns.length; i++)
         {
            PSFilter filter = (PSFilter)filters.next();
            patterns[i] = base + "/" + filter.getValue();
         }

         return patterns;
      }

      /**
       * Root link from the search root, never <code>null</code>.
       */
      Link m_rootLink;

      /**
       * <code>true</code> if recursive scan to sub-directory
       */
      boolean m_doRecurse;

      /**
       * patterns used to match file path
       */
      String[] m_patterns;
   }

   /**
    * Unit test
    */
   public static void main(String[] args)
   {
      String SRCH_ROOT = "C:\\YCHEN\\ECC\\WebSiteFiles";

      //String SRCH_ROOT = "C:\\YCHEN\\ECC\\FileSelectorTest";
      PSFileSearchRoot sroot = new PSFileSearchRoot("sroot1", SRCH_ROOT, true);
      sroot.addFilters("Default", "*");

      PSFileCrawler crawler = new PSFileCrawler(false);
      crawler.addSearchRoot(sroot);

      crawler.run();

      System.out.println("Total crawled files: " + crawler.getPagesVisited());
      System.out.println(
         "Total crawled directories: " + crawler.getTotalCrawledDirectories());

      PSContentTreeModel tree = crawler.getContentTree();
      tree.printNodeList();
   }
}
