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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * This class specifies and implements the interface for all the splitter
 * functionality used in the designer and XSpLit.
 * It will optimize the number of temporary files created and tidy processes
 * run for this instance. Also all conversions, etc. nessecary will be
 * performed through this class.
 */
public class Splitter
{
   /**
    * Construct a new splitter for the provided parameters. The provided
    * input stream is loaded and prepared for tidy into a temporary file.
    * Then tidy is run to create a well formed document, which is then used
    * to create all the user outputs on request.
    *
    * @param source a valid input stream containing the contents to split,
    *    not <code>null</code>.
    * @param root a valid root. Cannot be null or empty. This value will be
    *    used as the root element of any XML doc created by this class. As
    *    such, the name must conform to the element naming rules defined by
    *    the XML spec. The name is not verified here, thus errors will not be
    *    caught until the document created using this root is parsed.
    * @param config the splitter configuration to be used, not
    *    <code>null</code>.
    * @throws IllegalArgumentException for illegal arguments passed.
    * @throws SplitterException if running tidy could not be completed
    *    successfully.
    */
   public Splitter(InputStream source, String root,
      SplitterConfiguration config)
         throws SplitterException
   {
      if (source == null)
         throw new IllegalArgumentException("Invalid input stream!");

      if (root == null || root.equals(""))
         throw new IllegalArgumentException("Invalid root!");

      if (config == null)
         throw new IllegalArgumentException("config cannot be null");

      String enc = PSCharSets.getStdName(config.getProperty("inputEncoding"));
      if (enc == null || enc.equals(""))
         throw new IllegalArgumentException("Invalid or unknown encoding!");

      m_root = Util.makeXmlName(root);
      m_config = config;

      String content = null;
      try
      {
         content = loadSource(source, enc);
      }
      catch (IOException e)
      {
         String[] args =
         {
            "InputStream",
            e.getLocalizedMessage()
         };

         String msg = MainFrame.getRes().getString("loadingSourceFailed");
         throw new SplitterException(Util.dress(msg, args));
      }

      initializeSplitter(content, getOutputEncoding(
         m_config.getProperty("outputEncoding")),
         m_config.getProperty("showWarnings").equalsIgnoreCase("yes"), true);
   }

   /**
    * This constructor defaults to the standard root (file name)
    * and default input encoding (System).
    * @param source
    * @param config
    * @throws SplitterException
    *
    * @see #Splitter(File source, String root, SplitterConfiguration config)
    */
   public Splitter(File source, SplitterConfiguration config)
      throws SplitterException
   {
      this(source, null, config);
   }

   /**
    * Construct a new splitter for the provided parameters and run tidy. The
    * provided input file is loaded and prepared for tidy into a temporary
    * file. Then tidy is run to create a well formed document, which is then
    * used to create all the user outputs on request.
    *
    * @param source the source file to split. This must be a valid file.
    * @param root a valid root. If null or empty is provided the default
    *    root (file name) will be used. This value will be used as the root
    *    element of any XML doc created by this class. As such, the name must
    *    conform to the element naming rules defined by the XML spec. The
    *    name is not verified here, thus errors will not be caught until the
    *    document created using this root is parsed.
    * @param config the splitter configuration to use, not <code>null</code>.
    * @throws IllegalArgumentException for illegal arguments passed.
    * @throws SplitterException if running tidy could not be completed
    *    successfully.
    */
   public Splitter(File source, String root, SplitterConfiguration config)
      throws SplitterException
   {
      if (source == null || !source.isFile())
         throw new IllegalArgumentException("Invalid file!");

      if (config == null)
         throw new IllegalArgumentException("config cannot be null");
      m_config = config;

      String enc = PSCharSets.getStdName(m_config.getProperty("inputEncoding"));
      if (enc == null || enc.equals(""))
         throw new IllegalArgumentException("Invalid or unknown encoding!");

      m_root = (root == null || root.equals("")) ?
         Util.getRootName(source) : Util.makeXmlName(root);

      String content = null;
      try
      {
         content = loadSource(source, enc);
      }
      catch (IOException e)
      {
         String[] args =
         {
            source.getAbsolutePath(),
            e.getLocalizedMessage()
         };

         String msg = MainFrame.getRes().getString("loadingSourceFailed");
         throw new SplitterException(Util.dress(msg, args));
      }

      initializeSplitter(content, getOutputEncoding(
         m_config.getProperty("outputEncoding")),
         m_config.getProperty("showWarnings").equalsIgnoreCase("yes"), true);
   }

   /**
    * Convienience ctor that sets addGenerator to true
    * @param source a valid input stream containing the contents to split,
    *    not <code>null</code>.
    * @param root a valid root. Cannot be null or empty. This value will be
    *    used as the root element of any XML doc created by this class. As
    *    such, the name must conform to the element naming rules defined by
    *    the XML spec. The name is not verified here, thus errors will not be
    *    caught until the document created using this root is parsed.
    * @param config the splitter configuration to be used, not
    *    <code>null</code>.
    * @param changeRoot <code>true</code> if the root has to be changed in the
    * tidied document, <code>false</code> otherwise.
    * @throws SplitterException if running tidy could not be completed
    *    successfully.
    */
   public Splitter(InputStream source, String root,
      SplitterConfiguration config, boolean changeRoot)
         throws SplitterException
   {
      this(source, root, config, changeRoot, true);
   }

   /**
    * Construct a new splitter for the provided parameters. The provided
    * input stream is loaded and prepared for tidy into a temporary file.
    * Then tidy is run to create a well formed document, which is then used
    * to create all the user outputs on request.
    *
    * @param source a valid input stream containing the contents to split,
    *    not <code>null</code>.
    * @param root a valid root. Cannot be null or empty. This value will be
    *    used as the root element of any XML doc created by this class. As
    *    such, the name must conform to the element naming rules defined by
    *    the XML spec. The name is not verified here, thus errors will not be
    *    caught until the document created using this root is parsed.
    * @param config the splitter configuration to be used, not
    *    <code>null</code>.
    * @param changeRoot <code>true</code> if the root has to be changed in the
    * tidied document, <code>false</code> otherwise.
    * @param addGenerator flag indicating if generator meta tag should be
    * added.
    * @throws IllegalArgumentException for illegal arguments passed.
    * @throws SplitterException if running tidy could not be completed
    *    successfully.
    */
   public Splitter(InputStream source, String root,
      SplitterConfiguration config, boolean changeRoot, boolean addGenerator)
         throws SplitterException
   {
      if (source == null)
         throw new IllegalArgumentException("Invalid input stream!");

      if (root == null || root.equals(""))
         throw new IllegalArgumentException("Invalid root!");

      if (config == null)
         throw new IllegalArgumentException("config cannot be null");
      setShouldAddGenerator(addGenerator);
      String enc = PSCharSets.getStdName(config.getProperty("inputEncoding"));
      if (enc == null || enc.equals(""))
         throw new IllegalArgumentException("Invalid or unknown encoding!");

      m_root = Util.makeXmlName(root);
      m_config = config;

      String content = null;
      try
      {
         content = loadSource(source, enc);
      }
      catch (IOException e)
      {
         String[] args =
         {
            "InputStream",
            e.getLocalizedMessage()
         };

         String msg = MainFrame.getRes().getString("loadingSourceFailed");
         throw new SplitterException(Util.dress(msg, args));
      }

      initializeSplitter(content, getOutputEncoding(m_config
            .getProperty("outputEncoding")), m_config.getProperty(
            "showWarnings").equalsIgnoreCase("yes"), changeRoot);
   }

   /**
    * Initializes this splitter. No parameter checking is done.
    *
    * @param content the content string to split.
    * @param enc a valid input encoding (IANA name)
    * @param enableWarnings <code>true</code> to enable the tidy warnings,
    *    <code>false</code> otherwise. If this feature is enabled, warnings
    *    will be available through the method <code>getWarnings()</code>
    * @param changeRoot <code>true</code> if the root has to be changed in the
    * tidied document, <code>false</code> otherwise.
    * @throws IllegalArgumentException for illegal arguments passed
    * @throws SplitterException if running tiy could not be completed
    *    successfully.
    */
   private void initializeSplitter(String content, String enc,
      boolean enableWarnings, boolean changeRoot) throws SplitterException
   {
      m_tidiedInput = false;
      PrintWriter pw = null;

      try
      {
         m_wellFormedInput = content.startsWith(PI_XML_START);
         content = stripDocType(content);

         if (m_serverPageProcessor == null)
            m_serverPageProcessor = new ProcessServerPageTags(
               m_config.getServerPageTags());

         // preprocess server page tags
         content = m_serverPageProcessor.preProcess(content);

         // is tidy needed at all?
         if (m_wellFormedInput)
         {
            m_tidiedContent = content;
         }
         else
         {
            // create error file to which tidy will report errors
            m_tidyErrorFile = File.createTempFile("rxc", ".err");
            m_tidyErrorFile.deleteOnExit();
            pw = new PrintWriter(new FileOutputStream(m_tidyErrorFile));

            // configure tidy
            Properties tidyConfig = m_config.getTidyConfig();
            tidyConfig.setProperty("error-file",
               m_tidyErrorFile.getAbsolutePath());
            tidyConfig.setProperty("show-warnings",
               enableWarnings ? "yes" : "no");
            tidyConfig.setProperty("char-encoding",
               PSCharSets.getInternalEncoding());
            m_tidy.setConfigurationFromProps(tidyConfig);

            // redirect errors to our file
            m_tidy.setErrfile(m_tidyErrorFile.getAbsolutePath());
            m_tidy.setErrout(pw);

            ByteArrayInputStream is = new ByteArrayInputStream(
               content.getBytes(enc));
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            Document doc = m_tidy.parseDOM(is, os);
            m_tidiedContent = new String(os.toByteArray(), enc);
            m_tidiedInput = true;
         }

         // wrap script contents with CDATA
         m_tidiedContent = addCDATAForScript(m_tidiedContent);
      }
      catch (Throwable e)
      {
         throw new SplitterException(e.getLocalizedMessage());
      }
      finally
      {
         if (pw != null)
         {
            try
            {
               pw.flush();
               pw.close();
            }
            catch(Exception e)
            {
            }
         }
      }
      try
      {
         if(changeRoot)
            changeRoot(m_root);
         else
            changeRoot("");
      }
      catch (Exception e)
      {
         String[] args =
         {
            e.getLocalizedMessage()
         };

         String msg = null;
         if (didTidy())
         {
            if (m_tidiedContent.trim().length() == 0)
            {
               msg = MainFrame.getRes().getString("tidyFailed");
               String tidyErr = getTidyError();
               throw new SplitterException(Util.dress(msg,
                  new String[]{tidyErr}));
            }
            else
               msg = MainFrame.getRes().getString("postProcessFailedTidy");
         }
         else
            msg = MainFrame.getRes().getString("postProcessFailedNoTidy");

         throw new SplitterException(Util.dress(msg, args));
      }
   }

   /**
    * Strips out any existing doctype, recording the public and
    * system attributes for later use. Also removes the xml
    * declaration if the document is well formed.
    *
    * @param content the content to parse, may be <code>null</code>.
    * @return the content with the doctype and xml declaration
    * stripped out, may be <code>null</code>if supplied content is
    * <code>null</code>.
    */
   private String stripDocType(String content)
   {
      if(content == null)
         return null;
      String result = content;
      String doctype = null;

      int start = content.toLowerCase().indexOf("<!doctype");
      if(start == -1)
      {
         return result;
      }
      String ch = null;
      int offset = start;
      // We want to find the next opening tag that is not a
      // declaration (i.e. not <!)
      do
      {
         offset = content.indexOf("<", offset + 1);
         ch = content.substring(offset + 1, offset + 2);
      }
      while(ch.equals("!"));
      String limitedContent = 
         content.substring(0, offset);
      //Do NOT change the search order for end doctype marker!!!
      String endMarker = "]>";
      int end = limitedContent.indexOf(endMarker, start);
      if (end == -1)
      {
         endMarker = ">";
         end = limitedContent.indexOf(endMarker, start);
      }
      if (end != -1)
      {
         result = content.substring(0, start)
               + content.substring(end + endMarker.length());
         doctype = content.substring(start, end + endMarker.length());
      }
      // Remove the xml declaration. It will be put back later.
      if(m_wellFormedInput)
         result = result.substring(
            result.indexOf(PI_XML_END) + PI_XML_END.length() + 1);
      //Do not read entity definitions from doctype into doctype urls
      if(doctype != null && doctype.toLowerCase().indexOf("<!entity") == -1)
      {
         // parse doctype public
         start = doctype.indexOf('"');
         end = doctype.indexOf('"', start + 1);
         if(m_docTypePublic == null && start != -1 && end != -1)
            m_docTypePublic = doctype.substring(start + 1, end);
         // parse doctype system
         start = doctype.indexOf('"', end + 1);
         end = doctype.indexOf('"', start + 1);
         if(m_docTypeSystem == null && start != -1 && end != -1)
            m_docTypeSystem = doctype.substring(start + 1, end);
      }

      return result;

   }

   /**
    * Reads the error file to which Tidy has written its error and returns
    * the contents of the file in a String.
    * @return the contents of the error file to which Tidy has written its
    * errors as a string, never <code>null</code>, may be empty.
    */
   private String getTidyError() throws SplitterException
   {
      StringBuffer buf = new StringBuffer(1024);
      if ((m_tidyErrorFile != null) && (m_tidyErrorFile.isFile()))
      {
         BufferedReader br = null;
         try
         {
            br = new BufferedReader(new FileReader(m_tidyErrorFile));
            String line = null;
            while ((line = br.readLine()) != null)
            {
               buf.append(line);
               buf.append(" ");
            }
         }
         catch(IOException ioe)
         {
            String[] args = new String[]{ioe.getLocalizedMessage()};
            String msg = MainFrame.getRes().getString("postProcessFailedTidy");
            throw new SplitterException(Util.dress(msg, args));
         }
         finally
         {
            if (br != null)
            {
               try
               {
                  br.close();
               }
               catch(Exception e)
               {
               }
            }
         }
      }
      return buf.toString();
   }

   /**
    * This function encloses script blocks inside CDATA sections so that
    * the XML parser can parse the content even if the script has characters
    * like '<', '&' etc. Please note this function assumes the script block
    * is not already enclosed inside CDATA sections.
    *
    * For example,
    * <script language="javascript">function test()
    *    { if(a < b && a !=0) a = b } </script>
    * that is not parsable by XML parser would result
    *
    * <script language="javascript">
    *    <![CDATA[ function test() { if(a < b && a !=0) a = b } ]]> </script>.
    *
    * An XML parser can parse this without any problem.
    *
    * @param strHtml content as String
    * @return return modified HTML content as String
    */
   public static String addCDATAForScript(String strHtml)
   {
      StringBuffer sb = new StringBuffer(strHtml.length());
      String lowerCasedHtml = strHtml.toLowerCase();
      String openingTag = "<script";
      String closingTag = "</script>";
      String strCDATABegin = "\n<![CDATA[";
      String strCDATAEnd = "\n]]>";
      int pos = -1;
      int idx = 0;
      boolean isEmpty = false;
      String content = null;
      while((pos = lowerCasedHtml.indexOf(openingTag, idx)) > 0)
      {
         pos = lowerCasedHtml.indexOf('>', pos);
         isEmpty = lowerCasedHtml.substring(pos - 1, pos).equals("/");
         sb.append(strHtml.substring(idx, pos + 1));
         if(isEmpty)
            continue; // Skip empty script tag
         idx = pos + 1;
         pos = lowerCasedHtml.indexOf(closingTag, pos);
         content = strHtml.substring(idx, pos);
         if(content.trim().length() > 0)
         {
            sb.append(strCDATABegin);
            sb.append(content);
            sb.append(strCDATAEnd);
         }
         idx = pos + closingTag.length() + 1;
         sb.append(strHtml.substring(pos, idx - 1));


      }
      sb.append(strHtml.substring(idx));
      return sb.toString();
   }

   /**
    * Set standalone flag
    *
    * @param standalone true means we are operating as
    * standalone Xsplit, false means we are being called
    * from another application
    */
   public void isStandalone(boolean standalone)
   {
      m_isStandalone = standalone;
   }

   /**
    * Returns whether or not the source HTML was run trough tidy.
    *
    * @return <code>true</code> if the source was run through tidy,
    *    <code>false</code> otherwise
    */
   public boolean didTidy()
   {
      return !m_wellFormedInput && m_tidiedInput;
   }

   /**
    * Create a temporary file for the provided contents. The file will be
    * deleted on exit.
    *
    * @param content a string containing the file contents
    * @param encoding the encoding to use to create the file.
    * @return File the temporary file created.
    * @throws IOException for any I/O operation failed.
    */
   private File createTempFile(String content, String encoding) throws IOException
   {
      File temp = File.createTempFile("rxc", ".htm");
      temp.deleteOnExit();
      FileOutputStream fout = new FileOutputStream(temp);
      fout.write(content.getBytes(encoding));
      fout.flush();
      fout.close();

      return temp;
   }

   /**
    * This method returns the FORMS contents (the FORMS document as string).
    *
    * @param encoding the encoding to use to produce the output. If null or
    *    an unknown encoding is provided the standard encoding (UTF8) will
    *    be used.
    * @return the FROMS contents.
    * @throws SplitterException for all exceptions and errors generated by tidy.
    * @throws IOException for any I/O operation failed.
    * @throws SAXException for all SAX parser errors.
    * @see HTMLFORMS
    */
   public String getFORMSContents(String encoding) throws SplitterException,
      IOException, SAXException
   {
      HTMLFORMS forms = new HTMLFORMS(m_doc);
      forms.Process(false, getOutputEncoding(encoding));
      ByteArrayOutputStream os = forms.GetFormsDTDAsByteArrayOutputStream(
         getOutputEncoding(encoding));

      return new String(os.toByteArray(), getOutputEncoding(encoding));
   }

   /**
    * Returns the standard encoding name if known. If not known and a valid
    * Java name was provided, it will be used. If the provided encoding was
    * null or empty, the standard encoding (UTF8) will be returned.
    *
    * @param encoding a standard or java encoding. If null is provided the
    *    standard encoding will be returned.
    * @return a valid standard or java encoding.
    */
   private String getOutputEncoding(String encoding)
   {
      String enc = PSCharSets.getStdName(encoding);
      if (enc == null || enc.equals(""))
         return PSCharSets.getInternalEncoding();

      return enc;
   }

   /**
    * This method returns the FORMS document.
    *
    * @param encoding the encoding to use to produce the output. If null or
    *    an unknown encoding is provided the standard encoding (UTF8) will
    *    be used.
    * @return the FROMS document.
    * @throws SplitterException for all exceptions and errors generated by tidy.
    * @throws IOException for any I/O operation failed.
    * @throws SAXException for all SAX parser errors.
    * @see HTMLFORMS
    */
   public Document getFORMSDocument(String encoding) throws SplitterException,
      IOException, SAXException
   {
      HTMLFORMS forms = new HTMLFORMS(m_doc);
      forms.Process(false, getOutputEncoding(encoding));

      return forms.getXMLFormsDocument();
   }

   /**
    * Returns the XML sample created out of the DTD.
    *
    * @param encoding the encoding to use to produce the output. If null or
    *    an unknown encoding is provided the standard encoding (UTF8) will
    *    be used.
    * @return the XML smaple.
    * @throws SplitterException for all exceptions and errors generated by tidy.
    * @throws IOException for any I/O operation failed.
    * @throws SAXException for all SAX parser errors.
    */
   public String getXMLSample(String encoding) throws SplitterException,
      UnsupportedEncodingException, IOException, SAXException
   {
      HTML2DTD dtd = new HTML2DTD(m_doc);
      dtd.SetPsxTag(m_config.getProperty("dynamicTag"));
      dtd.Process(false, null);

      m_baseElementName = dtd.getBaseElementName();

      return dtd.getSampleXML(true, false);
   }

   /**
    * Returns an input stream for the DTD.
    *
    * @param encoding the encoding to use to produce the output. If null or
    *    an unknown encoding is provided the standard encoding (UTF8) will
    *    be used.
    * @return an input stream of the DTD.
    * @throws SplitterException for all exceptions and errors generated by tidy.
    * @throws IOException for any I/O operation failed.
    * @throws SAXException for all SAX parser errors.
    */
   public InputStream getDTDStream(String encoding) throws SplitterException,
      UnsupportedEncodingException, IOException, SAXException
   {
      return new ByteArrayInputStream(
         getDTDOutputStream(encoding).toByteArray());
   }

   /**
    * Returns the DTD contents.
    *
    * @param encoding the encoding to use to produce the output. If null or
    *    an unknown encoding is provided the standard encoding (UTF8) will
    *    be used.
    * @return the DTD content string.
    * @throws SplitterException for all exceptions and errors generated by tidy.
    * @throws IOException for any I/O operation failed.
    * @throws SAXException for all SAX parser errors.
    */
   public String getDTDContent(String encoding) throws SplitterException,
      UnsupportedEncodingException, IOException, SAXException
   {
      return new String(getDTDOutputStream(encoding).toByteArray(),
         getOutputEncoding(encoding));
   }

   /**
    * Returns the DTD contents.
    *
    * @param encoding the encoding to use to produce the output. If null or
    *    an unknown encoding is provided the standard encoding (UTF8) will
    *    be used.
    * @return the DTD content string.
    * @throws SplitterException for all exceptions and errors generated by tidy.
    * @throws IOException for any I/O operation failed.
    * @throws SAXException for all SAX parser errors.
    */
   private ByteArrayOutputStream getDTDOutputStream(String encoding)
      throws SplitterException, IOException, SAXException
   {
      HTML2DTD dtd = new HTML2DTD(m_doc);
      dtd.SetPsxTag(m_config.getProperty("dynamicTag"));
      dtd.Process(false, null);

      m_baseElementName = dtd.getBaseElementName();

      return dtd.GetDTDAsByteArrayOutputStream(getOutputEncoding(encoding));
   }

   /**
    * Get the DTD's base element name.
    *
    * @return the base element name. This might be <code>null</code>.
    */
   public String getBaseElementName()
   {
      return m_baseElementName;
   }

   /**
    * Returns an input stream for the XSL.
    *
    * @param encoding the encoding to use to produce the output. If null or
    *    an unknown encoding is provided the standard encoding (UTF8) will
    *    be used.
    * @return an input stream of the XSL.
    * @throws SplitterException for all exceptions and errors generated by tidy.
    * @throws SAXException for all SAX parser errors.
    * @throws IOException for any I/O operation failed.
    */
   public InputStream getXSLStream(String encoding) throws SplitterException,
      SAXException, IOException
   {
      byte[] bytes = getXSLContent(encoding).getBytes(
         getOutputEncoding(encoding));

      return new ByteArrayInputStream(bytes);
   }

   /**
    * Returns the XSL content as a string.
    *
    * @param encoding the encoding to use to produce the output. If null or
    *    an unknown encoding is provided the standard encoding (UTF8) will
    *    be used.
    * @return a string of the XSL contents.
    * @throws SplitterException for all exceptions and errors generated by tidy.
    * @throws SAXException for all SAX parser errors.
    * @throws IOException for any I/O operation failed.
    */
   public String getXSLContent(String encoding)
      throws SplitterException, SAXException, IOException
   {
      HTML2XSL xsl = new HTML2XSL(m_doc, getOutputEncoding(encoding));
      xsl.SetPsxTag(m_config.getProperty("dynamicTag"));
      //Should we put the body content into a seperate template?
      String bTemplate = m_config.getProperty("bodyContentTemplate");
      boolean bodyTemplateOn = false;
      if(bTemplate != null && bTemplate.trim().length()> 0)
      {
         bTemplate = bTemplate.trim();
         bodyTemplateOn = bTemplate.equalsIgnoreCase("yes") ||
            bTemplate.equalsIgnoreCase("true") ||
            bTemplate.equalsIgnoreCase("on");
      }
      xsl.setBodyTemplateOn(bodyTemplateOn);
      // Turn on pretty printing if we are standalone
      if(m_isStandalone)
         xsl.setPrettyPrinting(true);
      xsl.Process(false, m_wellFormedInput);
      Document doc = xsl.getXMLDocument();
      addOutputElement(doc);
      StringWriter wr = new StringWriter();
      HTML2XSL.printNode(doc, "\t", wr);

      return m_serverPageProcessor.postProcess(wr.toString());
   }

   /**
    * Returns the tiedied contents of the source file.
    *
    * @throws SplitterException for all exceptions and errors generated by tidy.
    * @throws IOException for any I/O operation failed.
    * @throws SAXException for all SAX parser errors.
    */
   public String getTidiedContents() throws SplitterException, IOException,
      SAXException
   {
      return m_tidiedContent;
   }

   /**
    * Returns the tiedied document of the source file.
    *
    * @throws SplitterException for all exceptions and errors generated by tidy.
    * @throws IOException for any I/O operation failed.
    * @throws SAXException for all SAX parser errors.
    */
   public Document getTidiedDocument() throws SplitterException,
      IOException, SAXException
   {
      return m_doc;
   }

   /**
    * Did the last run of tidy produce any warnings. This will always return
    * <code>false</code> if the user disabled warnings.
    *
    * @return <code>true</code> if there were warnings, <code>false</code>
    *    otherwise.
    */
   public boolean hasWarnings()
   {
      return m_tidy.getParseWarnings() > 0;
   }

   /**
    * Get the tidy warnings produced during the last run of tidy.
    *
    * @param the last warnings.
    */
   public String getWarnings()
   {
      try
      {
         return loadSource(new File(m_tidy.getErrfile()),
            m_config.getProperty("inputEncoding"));
      }
      catch (Throwable e)
      {
         // return the error message instead
         return "Could not load error file: " + e.getLocalizedMessage();
      }
   }

   /**
    * This method changes the current root to the provided root and recreates
    * the XSL document.
    * It After contruction this already has the root provided or the default
    * root. In some cases we need to switch the root without running tidy
    * again.
    *
    * @param root the new root to change to
    * @throws IOException for any I/O operation failed.
    * @throws SAXException for all SAX parser errors.
    */
   public void changeRoot(String root) throws IOException, SAXException
   {
      if (root == null)
         throw new IllegalArgumentException("illegal root!");

      m_root = root;
      String content = "";
      content += "<!DOCTYPE xsl:stylesheet [" +
      HTML2XSL.getDefaultEntities(null) + "]>" +
      HTML2XSL.NEWLINE + HTML2XSL.NEWLINE + m_tidiedContent;

      if (!m_root.equals(""))
      {
         content = HTML2XSL.AddBaseElement(content, m_root,
            m_config.getProperty("dynamicTag"));
      }

      m_doc = HTML2XSL.getXMLDocFromInputStream(new StringReader(content));
      m_doc = modifyCharsetMetaTags(m_doc);

      addGenerator(m_doc);
   }

   /**
    * This method loads the provided file into a string. The string is
    * converted to the character encoding specified while creating this
    * instance.
    *
    * @param source the source file to load.
    * @param encoing the encoding to use to load the file.
    * @return the content string of the current source file.
    * @throws IOException for all I/O errors
    */
   private String loadSource(File source, String encoding) throws IOException
   {
      FileInputStream in = null;

      try
      {
         in = new FileInputStream(source.getAbsolutePath());
         ByteArrayOutputStream out = new ByteArrayOutputStream();
         int read = 0;
         byte[] buf = new byte[1024];
         while ((read = in.read(buf)) >= 0)
         {
            out.write(buf, 0, read);
            if (read < buf.length)
               break;
         }
         out.flush();

         return out.toString(encoding);
      }
      finally
      {
         if (in != null) {
            try { in.close(); } catch (Exception e){}
         }
      }
   }

   /**
    * This method loads the provided input stream into a string. The string is
    * converted to the character encoding specified while creating this
    * instance.
    *
    * @param in the input stream to load.
    * @param encoing the encoding to use to load the file.
    * @return the content string of the current source file.
    * @throws IOException for all I/O errors
    */
   private String loadSource(InputStream in, String encoding) throws IOException
   {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      int read = 0;
      byte[] buf = new byte[1024];
      while ((read = in.read(buf)) >= 0)
      {
         out.write(buf, 0, read);
         if (read < buf.length)
            break;
      }
      out.flush();

      return out.toString(encoding);
   }

   /**
    * Modifies meta tags existing in the source HTML and specifying the output
    * encoding so that it will match the encoding in the xsl:output statement
    * The following formats are recognized:
    * <ol><li>
    * &lt;META http-equiv="content-Type" content="text/html; charset=ISO-8859-1"&gt;
    * </li>
    * <li>
    * &lt;META http-equiv="charset" content="ISO-8859-1"&gt;
    * </li>
    * <li>
    * &lt;META charset="ISO-8859-1"&gt;
    * </li></ol>
    *
    * @param doc the document from which meta tags defining the output encoding
    *    need to be modified, assumed not <code>null</code>.
    * @return the document with meta tags defining the output encoding modified,
    *    never <code>null</code>.
    */
   private Document modifyCharsetMetaTags(Document doc)
   {

      String encoding = m_config.getProperty("outputEncoding", "UTF-8");
      String outputEl = m_serverPageProcessor.getOutputElement();
      if(outputEl != null
         && outputEl.toLowerCase().indexOf("encoding=") != -1)
      {
         int sPos = outputEl.toLowerCase().indexOf("encoding=");
         int ePos = outputEl.indexOf('"', sPos + 10);
         encoding = outputEl.substring(sPos + 10, ePos);
      }

      NodeList nodes = doc.getElementsByTagName("meta");
      for (int i=0; i<nodes.getLength(); i++)
      {
         boolean found = false;
         Element elem = (Element) nodes.item(i);

         /*
         1st try format:
         <META http-equiv="content-Type" content="text/html; charset=ISO-8859-1">
         */
         String attr = elem.getAttribute("http-equiv");
         if (attr != null && attr.equalsIgnoreCase("content-type"))
         {
            String content = elem.getAttribute("content");
            if (content != null && !content.equals(""))
            {

               found = (content.toLowerCase().indexOf("charset=") != -1);
               if(found)
               {
                  StringBuffer sb = new StringBuffer();
                  StringTokenizer st =
                     new StringTokenizer(
                        elem.getAttribute("content"), "; ", true);
                  String token = null;
                  while(st.hasMoreTokens())
                  {
                     token = st.nextToken();
                     if(token.toLowerCase().startsWith("charset="))
                     {
                        sb.append("charset=");
                        sb.append(encoding);
                     }
                     else
                     {
                        sb.append(token);
                     }

                  }
                  elem.setAttribute("content", sb.toString());
               }

            }
         }

         /*
         2nd try format:
         <META http-equiv="charset" content="ISO-8859-1">
         */
         if (!found && (attr != null && attr.equalsIgnoreCase("charset")))
         {
            found = true;
            elem.setAttribute("content", encoding);
         }


         /*
         3rd try format:
         <META charset="ISO-8859-1">
         */
         if (!found)
         {
            attr = elem.getAttribute("charset");
            if (attr != null && !attr.equals(""))
               elem.setAttribute("charset", encoding);
         }


      }

      return doc;
   }

   /**
    * This will add an xsl:output statement to the provided stylesheet document
    * defining the output encoding. It will be added using the following rules:
    * <ol><li>
    * If there is already an xsl:output statement, nothing is added. This is
    * possible by defining an output statement in the source HTML.
    * </li>
    * <li>
    * If not spcified, an output statement is added with the encoding specified
    * in the splitter options.
    * </li></ol>
    *
    * @param doc the stylesheet in which to add the output declaration, assumed
    *    not <code>null</code>.
    */
   public void addOutputElement(Document doc)
   {
      if (m_serverPageProcessor.hasOutputElement())
         return;

      NodeList nl = doc.getElementsByTagName("xsl:output");
      Element output = null;
      boolean outputElemExists = false;
      if(nl.getLength()>0)
      {
         output = (Element)nl.item(0);
         outputElemExists = true;
      }
      else
      {
         output = doc.createElement("xsl:output");
         if (m_wellFormedInput)
            output.setAttribute("method", "xml");
         output.setAttribute("encoding",
            m_config.getProperty("outputEncoding"));
         output.setAttribute("omit-xml-declaration",
               m_wellFormedInput ? "no" : "yes");
      }
      if(m_docTypePublic != null && m_docTypeSystem != null)
      {
         if(m_docTypePublic.toLowerCase().indexOf("xhtml") != -1)
            output.setAttribute("method", "xml");
         output.setAttribute("doctype-public", m_docTypePublic);
         output.setAttribute("doctype-system", m_docTypeSystem);
      }

      if(!outputElemExists)
      {
         NodeList nodeList = doc.getElementsByTagName("xsl:stylesheet");
         Node stylesheet = nodeList.item(0);
         Node child = stylesheet.getFirstChild();
         while (child.getNodeType() != Node.ELEMENT_NODE
               || ((Element) child).getTagName().equalsIgnoreCase("xsl:import")
               || ((Element) child).getTagName().equalsIgnoreCase("xsl:include"))
         {
            child = child.getNextSibling();
         }
         stylesheet.insertBefore(output, child);
      }
   }

   /**
    * This will add the generator meta information to the provided document.
    *
    * @param doc the document to add the generator info to.
    */
   private void addGenerator(Document doc) throws SAXException
   {
      String shouldAddMeta = m_config.getProperty("rxGeneratorMetaTag") == null
         ? "yes"
         : m_config.getProperty("rxGeneratorMetaTag");

      if(!m_shouldAddGenerator ||
         !(shouldAddMeta.equalsIgnoreCase("yes")
            || shouldAddMeta.equalsIgnoreCase("true")))
         return;
      Element meta = doc.createElement("meta");
      meta.setAttribute("name", "generator");
      if (m_config.isStandalone())
         meta.setAttribute("content", "Percussion XSpLit");
      else
         meta.setAttribute("content", "Percussion Rhythmyx");

      NodeList nodes = doc.getElementsByTagName("head");
      if (nodes.getLength() > 0)
      {
         Node head = nodes.item(0);
         head.insertBefore(meta, head.getFirstChild());
      }
   }


   /**
    * Parse the HTTP Content-Type header.
    * <P>
    * The content-type header can have many parts. The syntax, which
    * is summarized here, is defined fully in the HTTP 1.1 spec
    * (RFC 2068), especially in section 3.7.
    * <P>
    * It is important to note that the HTTP 1.1 spec allows unlimited
    * whitespace between tokens. From section 2.1:
    * <P>
    * <BLOCKQUOTE>
    * The grammar described by this specification is word-based. Except
    * where noted otherwise, linear whitespace (LWS) can be included
    * between any two adjacent words (token or quoted-string), and
    * between adjacent tokens and delimiters (tspecials), without
    * changing the interpretation of a field. At least one delimiter
    * (tspecials) must exist between any two tokens, since they would
    * otherwise be interpreted as a single token.
    * </BLOCKQUOTE>
    * <PRE>
    * CTL            = <any US-ASCII control character (octets 0 - 31) and DEL (127)>
    * CHAR           = <any US-ASCII character (octets 0 - 127)>
    * token          = 1*<any CHAR except CTLs or tspecials>
    *
    * TEXT           = <any OCTET except CTLs, but including LWS>
    * quoted-string  = ( <"> *(qdtext) <"> )
    * qdtext         = <any TEXT except <">>
    *
    * media-type     = type "/" subtype *( ";" parameter )
    * type           = token
    * subtype        = token
    *
    * parameter      = attribute "=" value
    * attribute      = token
    * value          = token | quoted-string
    * </PRE>
    * The media-type is usually something like this:
    *                                                       <CODE>application/x-www-form-urlencoded</CODE>
    * conforming to MIME type syntax.
    * <P>
    * After the media-type, there may be zero or more parameters,
    * each of which starts with a semicolon. That would look something
    * like this:
    *                                                       <CODE>application/x-www-form-urlencoded ; charset=US-ASCII; foo="bar"</CODE>
    * <P>
    * Each parameter is of the form attribute=value, where value may
    * be either a token or a quoted string. A quoted string is a string
    * that starts and ends with a double quote ("). Since a token cannot
    * start with a double quote (or any other of a list of special
    * characters), our code makes the valid assumption that any value
    * starting with a double quote must also end in a double quote.
    * <P>
    * One of these parameters can be the charset parameter, which
    * specifies the character encoding of the form data. If this
    * parameter is left out, the spec says we must assume its value is
    * ISO-8859-1.
    *
    *
    * @param    contentType The value of the Content-Type HTTP header.
    *
    * @param    params A map in which we store the Content-Type parameters,
    * keyed by their lowercased names. The map can be null, in which case
    * no parameter values will be parsed or stored, except for the MIME type
    * which is the return value of this function.
    *
    * @return  String The media (MIME) type which makes up the first part
    * of the Content-Type header value. The media type will be all lowercase.
    */
   private String parseContentType(String contentType, Map params)
   {
      // strip off the media type which occurs before the optional
      // params string
      String mediaType = "";

      String cType = contentType.trim();
      int cTypeLen = cType.length();

      int semiPos = cType.indexOf(';');
      if (semiPos < 0)
         semiPos = cTypeLen;

      mediaType = cType.substring(0, semiPos).trim().toLowerCase();

      if (params != null && semiPos != cTypeLen)
      {
         String remainder = cType.substring(semiPos+1);
         parseHttpParamsString(remainder, params);
      }

      return mediaType;
   }

   /**
    * Parse an HTTP params string which consists of 0 or more
    * attribute=value pairs separated by semicolons. Unlimited whitespace
    * is allowed between tokens. Values can also be quoted, which means
    * that special characters (such as = and ;) should be ignored
    * between the quote delimiters.
    *
    * The params will be stored in the map as LCASE(name) -> value.
    *
    * @param paramStr the parameter string to parse.
    * @param params the map to which the parameters are stored.
    * @return the number of parameters found.
    */
   private static int parseHttpParamsString(String paramStr, Map params)
   {
      final String str = paramStr.trim();
      final int strLen = str.length();

      int semiPos = 0;
      int numParams = 0;

      while (semiPos >= 0 && semiPos < strLen)
      {
         if (semiPos == 0)
            semiPos = -1; // special case for first param

         int nextSemiPos = str.indexOf(';', semiPos + 1);
         if (nextSemiPos < 0)
            nextSemiPos = strLen;

         String param = str.substring(semiPos + 1, nextSemiPos).trim();

         // must have at least one char in the attribute, an equals sign,
         // and at least one char in the value, making the shortest
         // possible param ("a=b") length 3.
         if (param.length() < 3)
            throw new IllegalArgumentException("invalid attribute!");

         int eqPos = param.indexOf('=');
         if (eqPos < 2 || eqPos == (param.length() - 1))
            throw new IllegalArgumentException("invalid attribute!");

         String attribute = param.substring(0, eqPos).trim();
         String value = param.substring(eqPos + 1, param.length()).trim();

         // ignore delimiters within quoted strings
         char start = value.charAt(0);
         char end = value.charAt(value.length() - 1);
         while (start == '"' && end != '"')
         {
            int quotePos = str.indexOf('"', nextSemiPos + 1);
            if (quotePos < 0)
               throw new IllegalArgumentException("invalid attribute!");

            nextSemiPos = str.indexOf(';', quotePos + 1);
            param = str.substring(semiPos + 1, nextSemiPos).trim();
            value = param.substring(eqPos + 1, param.length()).trim();
            end = value.charAt(value.length() - 1);
         }

         params.put(attribute.toLowerCase(), value);
         numParams++;

         // advance to the next parameter
         semiPos = nextSemiPos;
      }

      return numParams;
   }

   /**
    * Get the root name for the current splitter instance.
    *
    * @return the root name, never <code>null</code>, might be empty.
    *    Guaranteed a valid XML name.
    */
   public static String getRoot()
   {
      return m_root;
   }

   /**
    * Get the splitter configuration.
    *
    * @return the active splitter configuration, never <code>null</code>.
    */
   public static SplitterConfiguration getConfig()
   {
      return m_config;
   }


   /**
    * Replace the server page tags in the supplied XML document that were
    * previously inserted by this object while construction.
    *
    * @param htmlDoc source XML document in which the tags have to be replaced,
    *           must not be <code>null</code>
    * @return XML document with all server page tags replaced back as string,
    *         never <code>null</code> or empty.
    * @throws IOException if the supplied XML document cannot be serialized to a
    *            string.
    * @throws SAXException if the supplied XML document cannot be serialized to
    *            a string.
    */
   public String replaceServerPageTags(Document htmlDoc) throws IOException,
         SAXException
   {
      if (htmlDoc == null)
      {
         throw new IllegalArgumentException("htmlDoc must not be null");
      }
      StringWriter wr = new StringWriter();
      HTML2XSL.printNode(htmlDoc, "", wr);

      return m_serverPageProcessor.replaceServerPageTags(stripDocType(wr
            .toString()));
   }

   /**
    * Set flag that indicates if the generator meta tag should
    * be added. Default is <code>true</code>
    * @param shouldAdd boolean indicating if generator should
    * be added
    */
   public void setShouldAddGenerator(boolean shouldAdd)
   {
      m_shouldAddGenerator = shouldAdd;
   }
   
   /**
    * Get the requested splitter configuration property.
    * 
    * @param name the name of the property, may be <code>null</code> or empty.
    * @return the requested property, may be <code>null</code> if not found
    *    and no default is definedd either.
    */
   public static String getProperty(String name)
   {
      return m_config.getProperty(name);
   }

   /**
    * The XML root name used for this instance, initialized during
    * construction, never <code>null</code> after that. Granteed a valid
    * XML name.
    */
   private static String m_root = null;

   /**
    * This flag is set to <code>false</code> before each run of tidy. If tidy
    * run successful, its set to <code>true</code>. The flag is used to
    * decide what error message to show to the user.
    */
   private boolean m_tidiedInput = false;

   /**
    * Status wheather the source was provided well-formed or not. Is the
    * source is provided well-formed, the tidy process is skipped entirely.
    */
   private boolean m_wellFormedInput = false;

   /**
    * The tidied content as string, reset for each run, never
    * <code>null</code> after that.
    */
   private String m_tidiedContent = null;

   /**
    * The opening tag of the XML processing instruction.
    */
   public static String PI_XML_START = "<?xml";

   /**
    * The closing tag of the XML processing instruction.
    */
   public static String PI_XML_END = "?>";

   /**
    * The well formed document created after tidy. Created during
    * construction, never <code>null</code> after that.
    */
   private Document m_doc = null;

   /**
    * The server page processor to be used for this context. Initialized
    * during construction, never <code>null</code> after that.
    */
   private ProcessServerPageTags m_serverPageProcessor = null;

   /** The DTD base element name */
   private String m_baseElementName = null;

   /**
    * The splitter configuration contains all configuration file access
    * functionality used by the splitter. Initialized during construction,
    * never <code>null</code> after that.
    */
   private static SplitterConfiguration m_config = null;

   /**
    * The tidy instance used for all splitter instances. Initialized during
    * construction, never <code>null</code> after that.
    */
   private static Tidy m_tidy = new Tidy();

   /**
    * Error File in which Tidy writes its errors, initialized in
    * <code>initializeSplitter</code> method.
    */
   private File m_tidyErrorFile = null;

   /**
    * Flag indicating that we are operating as a standalone splitter
    */
   private boolean m_isStandalone = false;

   /**
    * Represents the doctype's public attribute, may be <code>null</code>
    * or empty. The value is set in {@link #stripDocType(String)} only once.
    */
   private String m_docTypePublic = null;

   /**
    * Represents the doctype's system attribute, may be <code>null</code>
    * or empty.  The value is set in {@link #stripDocType(String)} only once.
    */
   private String m_docTypeSystem = null;

   /**
    * Flag indicating that generator meta tag should be added or not.
    */
   private boolean m_shouldAddGenerator = true;
}
