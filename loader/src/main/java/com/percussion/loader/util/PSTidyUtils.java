/*[ PSTidyUtils.java ]*********************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/


package com.percussion.loader.util;

import com.percussion.loader.IPSContentSelector;
import com.percussion.loader.IPSContentTreeNode;
import com.percussion.loader.IPSLoaderErrors;
import com.percussion.loader.IPSLogCodes;
import com.percussion.loader.PSLoaderException;
import com.percussion.loader.PSLoaderUtils;
import com.percussion.loader.PSLogMessage;
import com.percussion.loader.objectstore.PSConnectionDef;
import com.percussion.loader.selector.PSContentTreeModel;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Iterator;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;


/**
 * Utility routines to processing XML document through tidy
 **/
public class PSTidyUtils
{

   /**
    * this class should never be instantiated.
    **/
   private PSTidyUtils()
   {
      // don't construct this
   }

   /**
    * Process the specified html text through Tidy.
    *
    * @param htmlText The to be processed html text, it may not be
    *    <code>null</code>, but may be empty.
    *
    * @return The processed text, never <code>null</code>.
    *
    * @throws PSLoaderException if any error occurs.
    */
   public static String getTidyHtml(String htmlText)
      throws PSLoaderException
   {
      if (htmlText == null)
         throw new IllegalArgumentException("htmlText may not be null");

      initXmlDomContext();
      String tidiedHTML = null;

      try
      {
         tidiedHTML = tidyInput(ms_domContext, htmlText);
      }
      catch (PSLoaderException e)
      {
         throw e;
      }
      catch (Exception e)
      {
         throw new PSLoaderException(
            IPSLoaderErrors.UNEXPECTED_ERROR, e.toString());
      }

      return tidiedHTML;
   }

   /**
    * Validating whether a specified node contains well-formed document. The
    * validation process is:  to process the original text through Tidy, then
    * parse the processed text by XML parser.
    *
    * @param selector The current selector, used to retrieve original text,
    *    it may not be <code>null</code>.
    *
    * @param connDef The connection definition to the server. It may not be
    *    <code>null</code>.
    *    
    * @param node The specified node that contains the to be validated text,
    *    it may not be <code>null</code>.
    *
    * @return <code>true</code> if it has been successfully run through above
    *    process; otherwise return <code>false</code>.
    */
   public static boolean isWellFormedHtml(IPSContentSelector selector,
      PSConnectionDef connDef, IPSContentTreeNode node)
   {
      if (selector == null)
         throw new IllegalArgumentException("selector may not be null");
      if (connDef == null)
         throw new IllegalArgumentException("connDef may not be null");
      if (node == null)
         throw new IllegalArgumentException("node may not be null");

      initXmlDomContext();

      Document doc = null;
      InputStream in = null;

      try
      {
         in = selector.retrieve(node);
         String text = new String(PSLoaderUtils.getRawData(in));
         doc = loadXmlDocument(ms_domContext, connDef, text);
      }
      catch (Exception e)
      {
         PSLogMessage msg = new PSLogMessage(
            IPSLogCodes.ERROR_WELL_FORMED_XML,
            new String [] {node.getItemContext().getResourceId(), e.toString()},
            node.getItemContext()
            );

         Logger.getLogger(PSContentTreeModel.class.getName()).error(
            msg.getMessage());

         //Logger.getInstance(getClass().getName()).info(e);
      }
      finally
      {
         if (in != null)
         {
            try { in.close(); } catch (Exception ex) {}
         }
      }

      return (doc != null);
   }

   /**
    * Initialize the internal XML Dom Context variable if needed
    */
   private static void initXmlDomContext()
   {
      if (ms_domContext == null)
      {
         Properties tidyProperties = new Properties();
         FileInputStream propsInput = null;
         try
         {
            propsInput = new FileInputStream("rxW2Ktidy.properties");
            tidyProperties.load(propsInput);
         }
         catch (Exception e)
         {
            throw new RuntimeException(
               "Failed to read file, rxW2Ktidy.properties, caught exception: "
               + e.toString());
         }
         ms_domContext = new PSXmlDomContext("validateWellFormedXml");
         ms_domContext.setTidyProperties(tidyProperties);
         ms_domContext.setValidate(false);
      }
   }

   /**
    * Parses an XML Document from the given String. Depending on the settings
    * in the supplied context, the String will first be run through Tidy and
    * server page tags processing.  The context also determines if a
    * validating or non-validating parser will be used.
    *
    * @param cx the PSXmlDomContext for this request; cannot be <code>null
    * </code>
    * @param HTMLString the source HTML or XML document as a String; may be
    * <code>null</code> or empty.
    *
    * @return the parsed org.w3c.dom.Document.  Will be <code>null</code> if
    * the source string is empty.
    *
    * @throws FileNotFoundException
    * @throws IOException
    * @throws UnsupportedEncodingException
    * @throws Exception
    */
   public static Document loadXmlDocument(PSXmlDomContext cx, 
      PSConnectionDef connDef, String HTMLString)
      throws FileNotFoundException, IOException,
      UnsupportedEncodingException, Exception
   {
      Document resultDoc = null;
      if (null == HTMLString || HTMLString.trim().length() == 0)
      {
         return null;
      }
      if (null == cx)
         throw new IllegalArgumentException("PSXmlDomContext cannot be null");

      //String tidiedHTML = tidyInput(cx, HTMLString);
      String tidiedHTML = HTMLString;
      String tidiedOutput;

      /*
       * the doctypeHeader contains the entity definitions; these are required
       * if the incoming file uses common entities such as
       * <code>&amp;nbsp;</code>
       */
      String doctypeHeader = "<?xml version='1.0'?>" + NEWLINE;
      doctypeHeader += "<!DOCTYPE html [" +
            getDefaultEntities(connDef) + "]>" + NEWLINE + NEWLINE;

/*  Don't do the ServerPageTag for now
      if (cx.isServerPageTags())
      {
         cx.printTraceMessage("using Server Page Tags file: "
                              + cx.getServerPageTags());
         ProcessServerPageTags pspt =
               new ProcessServerPageTags(cx.getServerPageTags());
         String ParsedHTML = pspt.preProcess(tidiedHTML);
         if (cx.isLogging())
         {
            cx.printTraceMessage("writing trace file xmldocServerPageTags.doc");
            FileOutputStream beforeParse =
                  new FileOutputStream("xmldocServerPageTags.doc");
            beforeParse.write(ParsedHTML.getBytes(DEBUG_ENCODING));
            beforeParse.close();
         }
         if (hasXMLHeaders(ParsedHTML))
         {
             tidiedOutput = ParsedHTML;
         }
         else
         {
             tidiedOutput = doctypeHeader + ParsedHTML;
         }
      }
      else
*/
      {
         if (hasXMLHeaders(tidiedHTML))
         {
            tidiedOutput = tidiedHTML;
         }
         else
         {
            tidiedOutput = doctypeHeader + tidiedHTML;
         }
      }
      if (cx.isLogging())
      {
         cx.printTraceMessage("writing trace file xmldocbeforeparse.doc");
         FileOutputStream beforeParse =
               new FileOutputStream("xmldocbeforeparse.doc");
         beforeParse.write(tidiedOutput.getBytes(DEBUG_ENCODING));
         beforeParse.close();
      }

      InputSource is = new InputSource((Reader) new StringReader(tidiedOutput));
      StringWriter errString = new StringWriter();
      PrintWriter pWriter = new PrintWriter(errString, true);
      try
      {
         resultDoc = createXmlDocument(is, cx.isValidate(), pWriter);
      }
      catch (Exception e)
      {
         cx.printTraceMessage("XML Parser Errors occurred \n" +
                              errString.toString());
         throw e;
      }

      if (cx.isLogging())
      {
         if (errString.toString().length() > 0)
         {
            cx.printTraceMessage("XML Parser Errors/Warnings: \n" +
                                 errString.toString());
         }
         cx.printTraceMessage("writing trace file xmldocparsedout.doc");
         FileOutputStream parsedOutput =
               new FileOutputStream("xmldocparsedout.doc");

         PSXmlTreeWalker walk = new PSXmlTreeWalker(resultDoc);
         walk.write(new BufferedWriter(
               new OutputStreamWriter(parsedOutput, ENCODING)), true);

         parsedOutput.close();
      }

      return resultDoc;
   }


   /**
    * Tidy the incoming document, based on the settings in the
    * operation ccontext.
    *
    * @param cx  the PSXmlDomContext for this request
    *
    * @param htmlInput a String containing the input to be tidied
    *
    * @returns the tidied output in a String
    *
    **/
   private static String tidyInput(PSXmlDomContext cx, String htmlInput)
         throws FileNotFoundException, IOException,
         UnsupportedEncodingException, PSLoaderException
   {
      if (!cx.isTidyEnabled())
      {
         cx.printTraceMessage("Tidy Not Enabled");
         return htmlInput;
      }

      Tidy tidy = new Tidy();
      tidy.setConfigurationFromProps(cx.getTidyProperties());
      tidy.setInputEncoding("UTF-8");

      if (cx.isLogging())
      {
         cx.printTraceMessage("writing trace file xmldompretidy.doc");
         FileOutputStream preTidy = new FileOutputStream("xmldompretidy.doc");
         preTidy.write(htmlInput.getBytes(DEBUG_ENCODING));
         preTidy.close();
      }

      ByteArrayInputStream bystream = null;
      ByteArrayInputStream xmlStream = null;
      ByteArrayOutputStream tidiedStream = null;
      try
      {
         bystream = new ByteArrayInputStream(htmlInput.getBytes(ENCODING));
         StringWriter tidyErrors = new StringWriter();
         tidy.setErrout(new PrintWriter((Writer) tidyErrors));
         Document TidyXML = tidy.parseDOM(bystream, System.out);

         if (tidy.getParseErrors() > 0)
         {
            cx.printTraceMessage("Tidy Errors: " + tidyErrors.toString());
            throw new PSLoaderException(IPSLoaderErrors.TIDY_ERROR,
               tidyErrors.toString());
         }

         // Write out the document element using PSNodePrinter. This removes
         // the Xml and Doctype declaration.
         StringWriter swriter = new StringWriter();
         PSNodePrinter np = new PSNodePrinter(swriter);
         np.printNode(TidyXML.getDocumentElement());
         String result = swriter.toString();

         if(cx.getUsePrettyPrint())
         {
            xmlStream = new ByteArrayInputStream(result.getBytes(ENCODING));
            TidyXML = tidy.parseDOM(xmlStream, System.out);
            tidiedStream = new ByteArrayOutputStream();
            tidy.pprint(TidyXML, (OutputStream) tidiedStream);
            result = tidiedStream.toString(ENCODING);
         }
         if (cx.isLogging())
         {
            cx.printTraceMessage("writing trace file xmldomtidied.doc");
            PrintWriter pw = new PrintWriter(
                     new FileOutputStream("xmldomtidied.doc"));
            pw.println(result);
            pw.flush();
            pw.close();
         }
         return result;
      }
      finally
      {
         if (bystream != null)
         {
            try
            {
               bystream.close();
            }
            catch (Exception e)
            {
            }
         }
         if (xmlStream != null)
         {
            try
            {
               xmlStream.close();
            }
            catch (Exception e)
            {
            }
         }
         if (tidiedStream != null)
         {
            try
            {
               tidiedStream.close();
            }
            catch (Exception e)
            {
            }
         }
      }
   }

   /**
    * Put a string into the result document, at a particular node location,
    * which can be a compound name: category/nodelevel1/nodelevel2.
    *
    * @param  cx  @see(PSXmlDomContext) for the current request
    *
    * @param  resultDoc the DOM document where the node is to be added
    *
    * @param nodeName the name of the node to add under the root document. This
    * name may be a compound name (e.g. category/nodevalue)
    *
    * @param nodeValue the string value to add
    **/
   protected static void addResultNode(PSXmlDomContext cx, Document resultDoc,
                                       String nodeName, String nodeValue)
         throws Exception
   {

      Element docElement = resultDoc.getDocumentElement();
      PSXmlTreeWalker resWalker = new PSXmlTreeWalker(docElement);
      Element outputNode =
            resWalker.getNextElement(nodeName,
                                     PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN +
                                     PSXmlTreeWalker.GET_NEXT_RESET_CURRENT);
      if (outputNode == null)
      {
         //walker didn't find node, we must create it
         int lastslash = nodeName.lastIndexOf('/');
         if (lastslash == -1)
         {
            //no directory name, just add our element under the document root
            PSXmlDocumentBuilder.addElement(resultDoc, docElement,
                                            nodeName, nodeValue);
         }
         else
         {
            String pathPart = nodeName.substring(0, lastslash);
            String nodePart = nodeName.substring(lastslash + 1);
            //first see if we can find the path to insert under.
            Element newParent =
                  resWalker.getNextElement(
                  pathPart,
                  PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN +
                  PSXmlTreeWalker.GET_NEXT_RESET_CURRENT);
            if (null != newParent)
            {
               // we found an actual element with that name
               PSXmlDocumentBuilder.addElement(
               resultDoc,
               newParent,
               nodePart,
               nodeValue);
            }
            else
            {
               // the parent node was not found.  This probably should throw
               // an exception, but we'll just put the node under the root.
               cx.printTraceMessage(
                     "Warning: Node <" +
                     pathPart +
                     "> not found, adding to <ROOT> ");
               PSXmlDocumentBuilder.addElement(resultDoc, docElement,
                                               nodePart, nodeValue);
            }
         }
      }
      else
      {
         // The target node already exists in the document,
         // just replace the value.
         replaceText(resultDoc, outputNode, nodeValue);
      }
   }

   /**
    * Replace all of the TEXT nodes underneath a given element
    * with a single new text node.  If the node has markup, it may not
    * be preserved. Comments, CDATA sections, and other children of the
    * node are not changed.
    *
    * @param parentDoc the document that the element belongs to
    *
    * @param elementNode the element to be replaced
    *
    * @param newValue the string which contains the new value
    *
    * @return the original Element.
    **/
   public static Element replaceText(Document parentDoc,
                                     Element elementNode,
                                     String newValue)
   {
      int i;
      if (elementNode.hasChildNodes())
      {
         elementNode.normalize();
         NodeList children = elementNode.getChildNodes();
         if (children.getLength() == 1 &&
               children.item(0).getNodeType() == Node.TEXT_NODE)
         {
            // only 1 child, and it's a TEXT.  Just replace the value
            children.item(0).setNodeValue(newValue);
            return elementNode;
         }
         i = 0;
         while (i <= children.getLength())
         {
            Node tempNode = children.item(i);
            if (tempNode.getNodeType() == Node.TEXT_NODE)
            {
               // found a TEXT node, must delete it.
               elementNode.removeChild(tempNode);
            }
            else
            {
               //skip over any non TEXT nodes
               i++;
            }
         } //if
      } //while
      Node newText = parentDoc.createTextNode(newValue);
      elementNode.appendChild(newText);

      return elementNode;
   };


   /**
    * Checks if an XML document string already has a
    * <code>&lt;?xml ...&gt;</code> or <code>&lt!DOCTYPE...&gt;</code> header.
    *
    * @param xmlString the XML document to check.
    * @returns <code>true</code> if an XML header is found,
    * <code>false</code> otherwise.
    */
   private static boolean hasXMLHeaders(String xmlString)
   {
       String subString = xmlString.substring(0,11).toLowerCase();
       if (subString.startsWith("<?xml"))
       {
          return true;
       }
       if (subString.startsWith("<!doctype"))
       {
          return true;
       }
       return false;
   }

   /**
    * Add the entity references required by the parser.  
    * 
    * @param connDef Connection definition used to point to the entity files
    *    on the server.
    */
   private static String getDefaultEntities(PSConnectionDef connDef)
      throws PSLoaderException
   {
      String serverRoot = connDef.getServerName() + ":" + 
         connDef.getPort() + "/" + connDef.getServerRoot();

      return "\t<!ENTITY % HTMLlat1 SYSTEM \"http://" +
            serverRoot + "/DTD/HTMLlat1x.ent\">" + NEWLINE +
            "\t\t%HTMLlat1;" + NEWLINE +
            "\t<!ENTITY % HTMLsymbol SYSTEM \"http://" +
            serverRoot + "/DTD/HTMLsymbolx.ent\">" + NEWLINE +
            "\t\t%HTMLsymbol;" + NEWLINE +
            "\t<!ENTITY % HTMLspecial SYSTEM \"http://" +
            serverRoot + "/DTD/HTMLspecialx.ent\">" + NEWLINE +
            "\t\t%HTMLspecial;";
   }

   /**
    * NEWLINEs in XML are always <code>&lt;CR&gt;&lt;LF&gt;</code>,
    * even on platforms where &lt;LF&gt; is normally used
    **/
   private static final String NEWLINE = "\r\n";
   private static final int BUFFERSIZE = 20000;

   /**
    *ENCODING is always ISO 8859-1 for all Word HTML files.
    **/
   public static final String ENCODING = "UTF8";

   /**
    *Default name for all Private Objects is "XMLDOM"
    **/
   public static final String DEFAULT_PRIVATE_OBJECT = "XMLDOM";

   /**
    *The encoding for all "debugging" files
    **/
   public static final String DEBUG_ENCODING = "UTF8";

   /**
    * This section is copied directly from PSXmlDocumentBuilder.  We need to
    * print out the Parser errors (and warnings), but the default routine
    * does not do this.  We cannot overload this because of Obfuscation;
    * we must copy it here and change it.
    **/
   private static Document createXmlDocument(
         InputSource in,
         boolean validate,
         PrintWriter errorLog)
         throws java.io.IOException, org.xml.sax.SAXException
   {
      Document doc = null;
      DocumentBuilder db = PSXmlDocumentBuilder.getDocumentBuilder(validate);
      PSSaxErrorHandler errHandler = new PSSaxErrorHandler(errorLog);
      errHandler.throwOnFatalErrors(false);
      db.setErrorHandler(errHandler);
      doc = db.parse(in);
      errorLog.flush();

      /* We want to handle XML files without the <?xml ...> preamble. These
       * files cause IBM's parser to report "Invalid document structure."
       * (at least on a US english version).
       *
       * Therefore, if more than one exception is thrown (which there
       * will be for real errors) we'll throw the first exception
       * encountered.
       */
      if (errHandler.numFatalErrors() > 1)
      {
         Iterator errors = errHandler.fatalErrors();
         SAXParseException ex = (SAXParseException) errors.next();
         throw ex;
      }
      return doc;
   }

   /**
    * Used to cache and processing document through tidy. It contains
    * properties of tidy after initialized, never <code>null</code> or change
    * after that.
    */
   private static PSXmlDomContext ms_domContext = null;

}
