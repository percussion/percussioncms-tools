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
import com.percussion.xml.PSXmlTreeWalker;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.EntityReference;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * The HTML2XSL class is used to transform a specially created HTML document.
 * There are several output possibilities:
 * <ol>
 * <li>XSL equivalent</li>
 * <li>XML DTD</li>
 * <li>Dump all the form field names to a file in XML format</li>
 * <li>A sample XML data document</li>
 * </ol>
 * All local functions assume that the input HTML file has been tidied before
 * and therfore assume that all node, eleement and attrubute names are in
 * lower case.
 *
 * @author     Rammohan Vangapalli
 * @version    1.0
 * @since      1.0
 */
public class HTML2XSL
{
   /**
    * Default Constructer
    */
   public HTML2XSL ()
   {
   }

   /**
    * Another Constructer that takes the HTML Source file. Supported properties
    * are: <table border="1">
    * <tr>
    * <th>Key</th>
    * <th>Value</th>
    * </tr>
    * <tr>
    * <td>Input HTML File Path</td>
    * <td>input HTML file for transformation</td>
    * </tr>
    * </table>
    *
    * @param strHTMLFile String
    *
    * @param enc The java name of the character encoding to use when writing the
    *           XSL file.
    * @throws SAXException
    * @throws DOMException
    * @throws IOException
    *
    */
   public HTML2XSL(String strHTMLFile, String enc) throws SAXException,
         DOMException, IOException
   {
      SetHTMLFile(strHTMLFile, enc);
   }

   /**
    * Yet another Constructer that takes the HTML Source file and the psx-tag.
    * Supported properties are:
    * <table border="1">
    * <tr><th>Key</th><th>Value</th></tr>
    * <tr><td>Input HTML File Path</td><td>input HTML file for transformation</td>
    * <tr><td>psx-tag</td><td>new psx-tag string</td>
    * </tr>
    * </table>
    *
    * @param   strHTMLFile String
    *
    * @param   strPsxTag String
    *
    * @param enc The java name of the character encoding to use when writing the
    * XSL file.
    * @throws SAXException
    * @throws DOMException
    * @throws IOException
    *
    */
   public HTML2XSL (String strHTMLFile, String strPsxTag, String enc )
         throws SAXException, DOMException, IOException
   {
      m_strPsxTag = strPsxTag;
      SetHTMLFile(strHTMLFile, enc);
   }

   /**
    * Constructer that takes the InputStream. Supported properties are: <table
    * border="1">
    * <tr>
    * <th>Key</th>
    * <th>Value</th>
    * </tr>
    * <tr>
    * <td>InputStream</td>
    * <td>InputStream possibly built out of another programs output</td>
    * <tr>
    * <td>psx-tag</td>
    * <td>new psx-tag string</td>
    * </tr>
    * </table>
    *
    * @param rdr A reader that can be used to get the HTML contents.
    *
    * @param enc The Java name of the character encoding that should be used
    *           when writing the output.
    * @throws SAXException
    * @throws IOException
    *
    */
   public HTML2XSL ( Reader rdr, String enc ) throws SAXException, IOException
   {
      m_xmlDoc = getXMLDocFromInputStream(rdr);
      initializeXmlDoc(enc);
   }

   /**
    * Create a new object from an already parsed HTML file.
    *
    * @param doc A Document containing the parsed HTML data.
    *
    * @param enc The Java name of the char encoding to use when writing the
    *           resutlts.
    * @throws IOException
    */
   public HTML2XSL( Document doc, String enc ) throws IOException
   {
      if ( null == doc )
         throw new IllegalArgumentException( "Document cannot be null" );
      m_xmlDoc = doc;
      initializeXmlDoc(enc);
   }

   /**
    * Set the global option String psx-tag.
    * Supported properties are:
    * <table border="1">
    * <tr><th>Key</th><th>Value</th></tr>
    * <tr><td>psx-tag</td><td>new psx-tag string</td>
    * </tr>
    * </table>
    *
    * @param   strPsxTag   The new dynamic tag indicator.
    *
    */
   public void SetPsxTag(String strPsxTag)
   {
      m_strPsxTag = strPsxTag;
   }

   /**
    * Get the global option String psx-tag
    *
    * @return     the psx-tag as String
    */
   public String GetPsxTag()
   {
      return m_strPsxTag;
   }

   /**
    * Set the pretty print on flag
    *
    * @param bool true if we should pretty print, else false
    */
   public static void setPrettyPrinting(boolean bool)
   {
      ms_prettyPrintOn = bool;
   }

   /**
    * Set body content template to be on. This means that the content
    * in the html body tag will be placed in it's own template
    * @param bool true if we should put the body content in a separate
    * template.
    */
   public static void setBodyTemplateOn(boolean bool)
   {
      ms_bodyTemplateOn = bool;
   }

   /**
    * Indicates if pretty printing is on
    * @return
    *
    * @returns <code>true</code> if pretty print on, else
    * <code>false</code>
    */
   public static boolean isPrettyPrintingOn()
   {
      return ms_prettyPrintOn;
   }

   /**
    * Set the HTML Source file processing.
    * Supported properties are:
    * <table border="1">
    * <tr><th>Key</th><th>Value</th></tr>
    * <tr><td>Input HTML File Path</td><td>input HTML file for transformation</td>
    * </tr>
    * </table>
    *
    * @param   strHTMLFile
    *
    * @param enc The Java name of the character encoding that should be used when
    * writing the output.
    * @throws SAXException
    * @throws DOMException
    * @throws IOException
    *
    */
   public void SetHTMLFile(String strHTMLFile, String enc) throws SAXException,
         DOMException, IOException
   {
      m_strHTMLFile = strHTMLFile;

      DocumentBuilder db = PSXmlDocumentBuilder.getDocumentBuilder(false);
      m_xmlDoc = db.parse(m_strHTMLFile);
      initializeXmlDoc( enc );
   }

   /**
    * Initialize the default stuff for all XML documents.
    *
    * @param enc The java name of the character encoding to use when specifying
    * it in the document.
    *
    * @throws IOException
    */
   private void initializeXmlDoc(String enc) throws IOException
   {
      if (null == enc || 0 == enc.trim().length())
         throw new IllegalArgumentException("Encoding cannot be null");

      // Pull out pre html root comments so we can put them in the
      // correct location under the xsl documents root before
      // the html root tag.
      Iterator preRootComments = pullPreRootComments().iterator();

      // Verify the encoding is a valid one. This will throw if it is not.
      InputStreamReader foo = new InputStreamReader(
         new ByteArrayInputStream(new byte[1]), enc);
      m_charEncoding = enc;

      // create the XML processing instuction
      ProcessingInstruction pi = m_xmlDoc.createProcessingInstruction("xml",
         "version='1.0' encoding='" + PSCharSets.getStdName(enc) + "'");
      m_xmlDoc.insertBefore(pi, m_xmlDoc.getChildNodes().item(0));

      // create the stylesheet element
      Element elemStyleSheet = m_xmlDoc.createElement("xsl:stylesheet");
      elemStyleSheet.setAttribute("version", "1.1");
      elemStyleSheet.setAttribute("xmlns:xsl", ms_strXSLNS);
      elemStyleSheet.setAttribute("xmlns:saxon", ms_XSLNS_SAXON);
      elemStyleSheet.setAttribute("extension-element-prefixes", "saxon");
      elemStyleSheet.setAttribute("xmlns:psxi18n", ms_RX_I18N);
      elemStyleSheet.setAttribute("exclude-result-prefixes", "psxi18n");
      Element elemChild = m_xmlDoc.createElement("xsl:template");
      elemChild.setAttribute("match","/");
      elemChild.setAttribute("name",XSPLIT_ROOT_NAME);
      elemStyleSheet.appendChild(elemChild);

      /*
      add the root of the main context. we might need it within templates
      dealing with external documents.
      */
      Element root = m_xmlDoc.createElement("xsl:variable");
      root.setAttribute("name", "this");
      root.setAttribute("select", "/");
      elemStyleSheet.insertBefore(root, elemStyleSheet.getFirstChild());

      /*
      Featurette: Rx-02-11-0008
      add the sys_command parameter value. This is used to build the
      condition that leaves in psxedit span tags only in active
      assembly mode
      */
      Element syscommand = m_xmlDoc.createElement("xsl:variable");
      syscommand.setAttribute("name", "syscommand");
      syscommand.setAttribute("select", "//@sys_command");
      elemStyleSheet.insertBefore(syscommand, elemStyleSheet.getFirstChild());

      // remove and save the document element
      Node rootNode = m_xmlDoc.getDocumentElement();
      if (null != rootNode)
         rootNode = m_xmlDoc.removeChild(rootNode);

      // now make our stylesheet element the document element
      m_xmlDoc.appendChild(elemStyleSheet);

      // append the pre root comments
      while(preRootComments.hasNext())
      {
         elemChild.appendChild((Node)preRootComments.next());
      }

      // and put the old document back into our root template if it existed
      if (null != rootNode)
         elemChild.appendChild(rootNode);

      /*
      the template created here is run for all nodes. this
      checks if the current node is empty or not. if not empty it
      will look for the no-escaping attribute and perfom the appropriate
      action. if the current node is an empty string we will add a non
      breaking space, so table borders show up correct. finally we add a
      new line (<br>) for all but the last element. this is used to create
      all list elements on a new line. it has no effect on single element
      nodes.
      */
      elemChild = appendChildElement(m_xmlDoc,
                                     elemStyleSheet,
                                     "xsl:template", "match", "*");

      Element elemChoose = appendChildElement(m_xmlDoc,
                                              elemChild, "xsl:choose",
                                              null, null);
      Element elemWhen = appendChildElement(m_xmlDoc, elemChoose,
                                            "xsl:when", "test", "text()");
      Element elemOtherwise = appendChildElement(m_xmlDoc, elemChoose,
                                                 "xsl:otherwise", null, null);

      EntityReference er = m_xmlDoc.createEntityReference("nbsp");
      elemOtherwise.appendChild(er);

      Element elemTmp1 = appendChildElement(m_xmlDoc, elemChild,
                                            "xsl:if", "test",
                                            "not(position()=last())");

      appendChildElement(m_xmlDoc, elemTmp1, "br", null, null);

      elemChoose = appendChildElement(m_xmlDoc, elemWhen,
                                      "xsl:choose", null, null);
      elemWhen = appendChildElement(m_xmlDoc, elemChoose,
                                    "xsl:when", "test", "@no-escaping");
      Element elemTmp = appendChildElement(m_xmlDoc, elemWhen,
                                           "xsl:value-of", "select", ".");
      elemTmp.setAttribute("disable-output-escaping", "yes");
      elemOtherwise = appendChildElement(m_xmlDoc, elemChoose,
                                         "xsl:otherwise", null, null);
      appendChildElement(m_xmlDoc, elemOtherwise,
                         "xsl:value-of", "select", ".");

      /*
      the template created here is run for all attributes. we add a
      new line (<br>) for all but the last element. this is used to create
      all attributes from one node on a new line. it has no effect on single
      attributes.
      */
      elemChild = appendChildElement(m_xmlDoc,
                                     elemStyleSheet,
                                     "xsl:template", "match", "attribute::*");

      appendChildElement(m_xmlDoc, elemChild,
                         "xsl:value-of", "select", ".");

      elemTmp1 = appendChildElement(m_xmlDoc, elemChild,
                                    "xsl:if", "test", "not(position()=last())");
      appendChildElement(m_xmlDoc, elemTmp1, "br", null, null);
      
      /*
       * Starting from 6.0, the slot names cannot have the blank spaces, throw
       * runtime exception.
       */
      NodeList nl = m_xmlDoc.getElementsByTagName("*");
      for (int i = 0; i < nl.getLength(); i++)
      {
         Element elem = (Element) nl.item(i);
         String attr = elem.getAttribute(AssemblerTransformation.SLOTNAME_ATTR);
         if (attr.contains(" "))
            throw new RuntimeException(
               "Slot name must not contain whitespace characters");
      }
   }

   /**
    * Pulls out pre root comments, those are comments
    * after the doc type node but before the first element.
    * These comments are typically server tag place holders
    * and special comment tags for server side processing.
    * We don't pull comments for server side tags that contain
    * XSL.
    * @return a List of comment nodes. Never <code>null</code>,
    * may be empty.
    */
   private List pullPreRootComments()
   {
      List commentNodes = new ArrayList();
      Node node = m_xmlDoc.getFirstChild();
      Node temp = null;
      // Loop to get all comment nodes before the
      // first element.
      while(null != node && node.getNodeType() != Node.ELEMENT_NODE)
      {
          // make a copy of the node reference
          temp = node;
          node = temp.getNextSibling();
          if(temp.getNodeType() == Node.COMMENT_NODE)
          {
            if(!temp.getNodeValue().trim().startsWith(
               ms_XslServerTagPlaceHolder))
            {
               // Remove the comment node from the document
               // and store it in the List
               commentNodes.add(
                  temp.getParentNode().removeChild(temp));
            }
          }
      }
      return commentNodes;
   }


   /**
    * Append a child element with given attributes.
    *
    * @param doc - the XML document as Document
    * @param parent - parent element as Element
    * @param child - child element name as string
    * @param attr - attribute name (String) to be added to the new child
    * @param attVal - attribute value (String) correspondingto each attribute
    *           name
    *
    * @return the appended child element
    */
   public Element appendChildElement(Document doc, Element parent,
                                     String child, String attr, String attVal)
   {
      Element elemChild = doc.createElement(child);

      if (null != attr && null != attVal)
         elemChild.setAttribute(attr, attVal);

      return (Element) (parent.appendChild(elemChild));
   }

   /**
    * Get the HTML Source file set for transformation
    * @param strHTMLFile
    *
    * @return     the HTML Source file as String
    */
   public String GetHTMLFile(String strHTMLFile)
   {
      return m_strHTMLFile;
   }

   /**
    * Call this function to do the actual work of creating an XSL file out of
    * an HTML source. The source (HTML) and target (XSL) files must be
    * initialized correct, which is done through all of the provided
    * constructors.<p>
    * To write the output into a file the HTML file needs to be set. This is
    * done through the 2 constuctors where you pass in the HTML filename or
    * by calling the SetHTMLFile function. The output filename is the same as
    * the HTML filename with the extension set to '.XSL'.<p>
    * This function call transforms the entire html document to a xsl document.
    * It basically creates the infrastructure for the XSL doc without modifying
    * the node contexts. However, The templates are created based on the
    * matching node context levels. The next (Herculian!) task is to modify
    * the node contexts. We start doing this by calling the function
    * loopForApplyTemplates() which is called recursively.
    *
    * @param bPrintToFile set this to <code>true</code> to write the result
    *    into a file.
    * @param isInputXml flag to indicate whether or not the input file is XML.
    * @throws IOException for any input/output function that fails.
    * @throws SplitterException
    */
   public void Process(boolean bPrintToFile, boolean isInputXml)
      throws IOException, SplitterException
   {
      m_bPrintToFile = bPrintToFile;

      // reset the unknow dorref counter for each run
      m_unknownDocRef = 0;
      // create a new context map for each run
      m_contexts.clear();
      // create a new map for each run
      m_roots.clear();

      /*
       * The global template suffix is used by the assembler transformator to to
       * create the correct attribute names for various elements. We pass an
       * empty string if the source doc is not meant for a global template
       * otherwise we pass the global template name. tneplate name.
       */
      String globalTemplateSuffix = getGlobalTemplateName(m_xmlDoc);
      m_casTransformator = new AssemblerTransformation(globalTemplateSuffix);

      // reset the assembler mark-up flags
      m_hasSlots = false;
      m_hasProperties = false;

      // first transform the HTML document into an XSL document
      transformNode(m_xmlDoc);

      // **** m_xmlDoc now is an XSL document

      // now transform all properties
      transformProperties(m_xmlDoc, m_casTransformator);

      // Get the root context
      Element elemRoot = m_xmlDoc.getDocumentElement();
      m_strBaseContext = elemRoot.getAttribute(ms_strContextNodeName);
      int nLoc = m_strBaseContext.indexOf('/');
      if (-1 != nLoc)
         m_strBaseContext = m_strBaseContext.substring(0, nLoc);

      /*
      To start with, locate the root template with match = "/" and call
      loopForApplyTemplates() where the rest is taken care.
      */
      String strAttr = "";
      Node rootTemplate = null;
      Node tmpNode = null;
      NodeList nodes = m_xmlDoc.getElementsByTagName("xsl:template");
      for (int i=0; i<nodes.getLength(); i++)
      {
         tmpNode = nodes.item(i);
         if (Node.ELEMENT_NODE != tmpNode.getNodeType())
            continue;

         strAttr = ((Element) tmpNode).getAttribute("match");
         if (strAttr.equals("/"))
         {
            rootTemplate = tmpNode;
            break;
         }
      }

      if (null != rootTemplate)
         loopForApplyTemplates(rootTemplate);
      else
      {
         // TODO: there is no base template node, log error
      }

      // replace every template match attribute from "." to "*"
      tuneTemplateMatchAttributes();

      // * m_xmlDoc now is an XSL Document with context node modified suitably

      tuneInputTemplates();
      tuneOptionTemplates();

      followUpExternalContexts(m_xmlDoc);
      createContextVariables(m_xmlDoc, (Element) rootTemplate, m_contexts);
      removeXmlRootDependency(m_xmlDoc);

      /*
      Call CleanupContexts() on the doc node. Otherwise we will see the
      attribute "zContext" for every node in the document.
      */
      cleanupContexts(m_xmlDoc);

      if (!isInputXml)
      {
         /*
         This is to add a dummy attribute if an element has no value and np
         attributes. Navigator does not understand <br/>, <hr/>, etc. In such
         cases, we add a dummy attribute such that we see something like:
         <br id="XSpLit"/>, that works with both IE and Navigator.
         This step is skipped if the source was already provided as well formed
         XML.
         */
         pleaseNavigator(m_xmlDoc, m_strDummAttrib);
      }

      // add the global template
      m_casTransformator.addGlobalTemplate(m_globalTemplateMarkup, m_xmlDoc);

      // add the inline link template used in content assemblers
      m_casTransformator.addInlineLinkTemplate(m_xmlDoc);
      // now add all assembler includes if necessary
      if (Splitter.getConfig().addAssemblerImports() ||
         m_hasSlots || m_hasProperties)
         m_casTransformator.addAssemblerImports(m_xmlDoc, m_globalTemplateMarkup.length()>0);
      // do not add non breaking spaces for slots
      m_casTransformator.excludeNBSPTemplate(m_xmlDoc,
         AssemblerTransformation.ASSEMBLER_INFO_ELEM);

      fixRepeatContexts();

      // add psxedit span choose conditions
      addSpanChooseCondition(m_xmlDoc);

      // Move body content to a seperate template
      handleBodyContent(m_xmlDoc);

      // **** m_xmlDoc now is an XSL Document
      if (true == m_bPrintToFile)
         createXSLFile();
   }


   /**
    * Evaluate if the source being split is using a global template. Looks for
    * the attribute named {@link #ATTR_PSXGLOBALTEMPLATE}of "html" element. If
    * it has non empty value then the source is assumed not using a global
    * template.
    *
    * @param doc the source XML document, assumed not <code>null</code>.
    * @return <code>true</code> if the source is marked to use a global
    *         template, <code>false</code> otherwise.
    */
   private boolean isUsingGlobalTemplate(Document doc)
   {
      String temp = "";
      NodeList nl = doc.getElementsByTagName("html");
      if (nl.getLength() > 0)
      {
         Element elem = (Element) nl.item(0);
         temp = elem.getAttribute(ATTR_PSXGLOBALTEMPLATE).trim();
      }
      return temp.length() > 0;
   }

   /**
    * Evaluate if the source is a global template and return its name. Looks for
    * the attribute named {@link #ATTR_PSXGLOBALTEMPLATE_NAME}of "html"
    * element. If it has non empty value then the source is treated as for
    * global template.
    *
    * @param doc the source XML document, assumed not <code>null</code>.
    * @return name of the global template. Assumed to be supplied as an
    *         attribute of &lt;html&gt; element with name
    *         {@link #ATTR_PSXGLOBALTEMPLATE_NAME }. Will be empty string if
    *         the source is not for a global temaplate.
    */
   private String getGlobalTemplateName(Document doc)
   {
         NodeList nl = doc.getElementsByTagName("html");
         String temp = "";
         if (nl.getLength() > 0)
         {
            Element elem = (Element) nl.item(0);
            temp = elem.getAttribute(ATTR_PSXGLOBALTEMPLATE_NAME).trim();
         }
         return temp;
   }

   /**
    * Puts the content of the body tag into its own separate template if
    * ms_bodyTemplateOn flag is <code>true</code>.
    * @param doc the xsl document, cannot be <code>null</code>.
    */
   private void handleBodyContent(Document doc)
   {
     if(doc == null)
        throw new IllegalArgumentException("Document cannot be null.");
     if(!ms_bodyTemplateOn)
        return;

     // First get the body element
     Element root = doc.getDocumentElement();
     Element body = null;
     Element stylesheet = null;
     NodeList nl = root.getElementsByTagName("body");
     if(nl.getLength() > 0)
     {
        body = (Element)nl.item(0);
        Element callTemplate =
           doc.createElement("xsl:call-template");
        callTemplate.setAttribute("name", XSPLIT_BODY_TEMPLATE);
        Element template =
           doc.createElement("xsl:template");
        template.setAttribute("name", XSPLIT_BODY_TEMPLATE);
        // Remove the body tags children and add them to the template
        if(body.hasChildNodes())
        {
           nl = body.getChildNodes();
           for(int i = 0; i < nl.getLength(); i++)
           {
              template.appendChild(body.removeChild(nl.item(i--)));
           }
        }
        body.appendChild(callTemplate);
        root.appendChild(template);


     }

   }


   /**
    * This will create the correct call to external documents for
    * apply-templates elements.
    * If the provided element refererences an external document, this will
    * create the correct document call. If the external document reference
    * is dynamic too, we create in addition a global variable to use as the
    * reference.
    *
    * @param elem the element to follow up on for external document calls
    */
   private void applyTemplatesToExternalContext(Element elem)
   {
      String select = elem.getAttribute("select");
      String strContext = select;
      int pos = select.indexOf('/');
      if (pos != -1)
         strContext = select.substring(0, pos);

      if (m_contexts.containsKey(strContext))
      {
         String docContext = "document($" + strContext + ")";
         elem.setAttribute("select", docContext);
      }
   }

   /**
    * This will create the correct call to external documents for all but
    * apply-templates elements.
    * If the provided element refererences an external document, this will
    * create the correct document call.
    *
    * @param elem the element to follow up on for external document calls
    */
   private void createExternalContext(Element elem)
   {
      String select = elem.getAttribute("select");
      if (select == null || select.equals(""))
         return;

      String strContext = select;
      String relContext = "";
      int pos = select.indexOf('/');
      if (pos != -1)
      {
         strContext = select.substring(0, pos);
         relContext = select.substring(pos+1, select.length());
      }

      if (m_contexts.containsKey(strContext))
      {
         String docContext = "document($" + strContext + ")";
         if (!relContext.equals(""))
            docContext += "/" + relContext;
         elem.setAttribute("select", docContext);
      }
   }

   /**
    * This will force the output as XML by adding the top-level element
    * <xsl:output method="xml"/>. Existing output elements are ignored.
    *
    * @param stylesheet the stylesheet document, not <code>null</code>
    * @param root the root template for this stylesheet, not <code>null</code>
    * @throws IllegalArgumentException for any invalid argument provided
    */
   private void forceXmlOutput(Document stylesheet, Element root)
   {
      if (stylesheet == null)
         throw new IllegalArgumentException("this needs a valid stylesheet document");
      if (root == null)
         throw new IllegalArgumentException("this needs a valid root element");

      Element element = stylesheet.createElement("xsl:output");
      element.setAttribute("method", "xml");

      // and insert it before the main template
      Node parent = root.getParentNode();
      if (parent != null)
         parent.insertBefore(element, root);
   }


   /**
    * This will create a global variable for each external context found in
    * the provided hashtable.
    *
    * @param stylesheet the stylesheet document, not <code>null</code>
    * @param root the root template for this stylesheet, not <code>null</code>
    * @param contexts a hashtable with all contexts to create the variables
    * @throws IllegalArgumentException for any invalid argument provided
    */
   private void createContextVariables(Document stylesheet, Element root,
                                       Hashtable contexts)
   {
      if (stylesheet == null)
         throw new IllegalArgumentException("this needs a valid stylesheet document");
      if (root == null)
         throw new IllegalArgumentException("this needs a valid root element");
      Node parent = root.getParentNode();
      if (parent == null)
         throw new IllegalArgumentException("this needs a valid stylesheet element");

      Enumeration keys = contexts.keys();
      while (keys.hasMoreElements())
      {
         String key = (String) keys.nextElement();
         String docURI = (String) contexts.get(key);

         String strSelect;
         if (docURI.startsWith(m_strPsxTag))
            strSelect = "/*/" + docURI.substring(m_strPsxTag.length());
         else
            strSelect = "'" + docURI + "'"; // wrap it with qoutes, so its interpreted as file
         Element var = stylesheet.createElement("xsl:variable");
         var.setAttribute("name", key);
         var.setAttribute("select", strSelect);

         parent.insertBefore(var, root);
      }
   }

   /**
    * This method goes through all external contexts found in the root
    * template. It replaces the context alias found with the correct document
    * call.
    *
    * @param source the document
    */
   private void followUpExternalContexts(Document source)
   {
      // loop through all templates and create the external document calls
      NodeList nodes = source.getElementsByTagName("*");
      for (int i=0; i<nodes.getLength(); i++)
      {
         Node node = nodes.item(i);
         if (Node.ELEMENT_NODE != node.getNodeType())
            continue;

         makeExternalContext((Element) node);
      }
   }

   /**
    * This recurses into all element children found in the provided element
    * and creates the external document context as nessecary.
    *
    * @param elem the element to follow up on for external document calls
    */
   private void makeExternalContext(Element elem)
   {
      if (elem.hasChildNodes())
      {
         NodeList children = elem.getChildNodes();
         for (int i=0; i<children.getLength(); i++)
         {
            Node child = children.item(i);
            if (Node.ELEMENT_NODE != child.getNodeType())
               continue;

            makeExternalContext((Element) child);
         }
      }

      if (elem.getTagName().equals("xsl:apply-templates"))
         applyTemplatesToExternalContext(elem);
      else
         createExternalContext(elem);
   }

   /**
    * This method walks through the entire document and removes all XML root
    * dependencies. This must be called after the external document
    * follow up!
    *
    * @param source the document to remove the XML root dependencies, not
    *    <code>null</code>
    * @throws IllegalArgumentException for any illegal arguments provided
    */
   private void removeXmlRootDependency(Document source)
   {
      if (source == null)
         throw new IllegalArgumentException("this need a valid document");

      NodeList nodes = source.getElementsByTagName("*");
      for (int i=0; i<nodes.getLength(); i++)
      {
         Node node = nodes.item(i);
         if (Node.ELEMENT_NODE != node.getNodeType())
            continue;

         fixXmlRoot((Element) node);
      }
   }

   /**
    * Replaces the XML root with the standard root (*) for the provided
    * element and all its children. This is done for select and match
    * attributes. It must be called after fixing the external document
    * follow up!
    *
    * @param elem the element to replace the XML roots for, never
    *    <code>null</code>
    * @throws IllegalArgumentException for any illegal parameters provided.
    */
   private void fixXmlRoot(Element elem)
   {
      if (elem == null)
         throw new IllegalArgumentException("this needs a valid element");

      if (elem.hasChildNodes())
      {
         NodeList children = elem.getChildNodes();
         for (int i=0; i<children.getLength(); i++)
         {
            Node child = children.item(i);
            if (Node.ELEMENT_NODE != child.getNodeType())
               continue;

            fixXmlRoot((Element) child);
         }
      }

      String select = elem.getAttribute("select");
      if (select != null && select.length() != 0)
      {
         String newSelect = "*";
         String test = select;
         int index = select.indexOf('/');
         if (index != -1)
         {
            test = select.substring(0, index);
            newSelect += select.substring(index);
         }

         if (m_roots.containsKey(test))
            elem.setAttribute("select", newSelect);
      }

      String match = elem.getAttribute("match");
      if (match != null && match.length() != 0)
      {
         String newMatch = "*";
         String test = match;
         int index = match.indexOf('/');
         if (index != -1)
         {
            test = match.substring(0, index);
            newMatch += match.substring(index);
         }

         if (m_roots.containsKey(test))
            elem.setAttribute("match", newMatch);
      }
   }

   /**
    * Remove all context information added during our process. This will
    * remove all context info starting from the provided node including all
    * children.
    *
    * @param htmlNode the node to start from.
    * @throws DOMException for any DOM function that failed.
    */
   private void cleanupContexts(Node htmlNode) throws DOMException
   {
      if (htmlNode.hasChildNodes())
      {
         NodeList nodeList;
         Node nodeTemp=null;
         nodeList = htmlNode.getChildNodes();
         for (int i=0; i<nodeList.getLength(); i++)
         {
            nodeTemp = nodeList.item(i);
            cleanupContexts(nodeTemp);
         }
      }

      if (null == htmlNode || Node.ELEMENT_NODE != htmlNode.getNodeType())
         return;

      Element elem = (Element) htmlNode;
      removeAttribute(elem, ms_strRootContext);
      removeAttribute(elem, ms_strContextNodeName);

      String strRepeat = elem.getAttribute(ms_strRepeatContext);
      if (strRepeat != null && !strRepeat.equals(""))
         followUpRepeat(elem, strRepeat);
      removeAttribute(elem, ms_strRepeatContext);
   }


   /**
    * This method adds a choose condition around psxedit span tags
    * so that they get stripped out when not in active assembly
    * mode.
    *
    * @param htmlNode the node to start from
    * @throws DOMException for any DOM function that fails
    */
    private void addSpanChooseCondition(Node htmlNode) throws DOMException
    {
       if(htmlNode.hasChildNodes())
       {
          NodeList children = htmlNode.getChildNodes();
          Node node = null;
          for(int i=0; i<children.getLength(); i++)
          {
             node = children.item(i);
             addSpanChooseCondition(node);
          }
       }

       // Lets see if this is a psxedit span tag
       if(htmlNode.getNodeType() == Node.ELEMENT_NODE &&
            htmlNode.getNodeName().equalsIgnoreCase("span"))
       {
          Element htmlElem = (Element)htmlNode;
          if(htmlElem.getAttributeNode("psxedit") != null)
          {
             // Make a copy of the span tag including children
             // so we start with a tag without parents
             Element spanCopy = (Element)htmlElem.cloneNode(true);
             // Get the children from within the span tag
             // so we can add them to the otherwise element
             NodeList spanChildren = htmlElem.getChildNodes();

             // Build the choose condition
             Element chooseElem = m_xmlDoc.createElement("xsl:choose");
             Element whenElem = m_xmlDoc.createElement("xsl:when");
             whenElem.setAttribute("test","$syscommand='editrc'");
             whenElem.appendChild(spanCopy);
             Element otherwiseElem = m_xmlDoc.createElement("xsl:otherwise");
             // Append all span children nodes, but clone first
             // to remove parent linkage
             for(int i=0; i<spanChildren.getLength(); i++)
             {
               otherwiseElem.appendChild(
                  spanChildren.item(i).cloneNode(true));
             }
             chooseElem.appendChild(whenElem);
             chooseElem.appendChild(otherwiseElem);
             // Now we replace the old span tag with the new condition
             // wrapped span
             htmlNode.getParentNode().replaceChild(chooseElem, htmlNode);
          }
       }

    }


   /**
    * This corrects the template if repetition was limited in the original
    * HTML code using the 'psx-repeat' tag.
    *
    * @param elem the node to followup for the repeat feature.
    * @param strRepeat the repeat context.
    */
   private void followUpRepeat(Element elem, String strRepeat)
   {
      if (elem == null)
         return;

      String strContext = strRepeat.substring(strRepeat.indexOf("/")+1,
                                              strRepeat.length());
      strContext = correctRepeatContext(strContext);

      NodeList nodes = elem.getElementsByTagName("*");
      for (int i=0; i<nodes.getLength(); i++)
      {
         Element temp = (Element) nodes.item(i);
         String strSelect = temp.getAttribute("select");
         if (strSelect.equals(strContext))
         {
            temp.setAttribute("select", ".");
         }
         else
         {
            if (strSelect.startsWith(strContext + "/"))
            {
               int start = strSelect.indexOf(strContext);
               if (start != -1)
               {
                  start += strContext.length() + 1;
                  if (start <strSelect.length())
                  {
                     strSelect = strSelect.substring(start, strSelect.length());
                     temp.setAttribute("select", strSelect);
                  }
               }
            }
         }
      }

      Element parent = (Element) elem.getParentNode();
      parent.setAttribute("select", strContext);
   }

   /**
    * This will correct the repeat context. All it does is remove the root
    * element if it belongs to an external document.
    *
    * @param strRepeat repeat context
    * @return the corrected repeat context
    */
   private String correctRepeatContext(String strRepeat)
   {
      int pos = strRepeat.indexOf('/');
      if (pos == -1)
         return strRepeat;

      String context = strRepeat.substring(0, pos);
      if (m_contexts.containsKey(context))
         return strRepeat.substring(pos+1, strRepeat.length());

      return strRepeat;
   }

   /**
    * This method will fix up the repeat contexts of the repeat templates
    * so that they actually work.
    * <pre>
    * <b>Example:</b>
    *
    * &lt;xsl:template match="*&#47;itemset" mode="mode0"&gt;
    *     &lt;xsl:for-each select="itemset/item"&gt;
    *      &lt;td&gt;
    *         &lt;xsl:apply-templates select="item/category_id"/&gt;
    *      &lt;/td&gt;
    *     &lt;/xsl:for-each&gt;
    *   &lt;/xsl:template&gt;
    *
    * <b>Becomes:</b>
    *
    * &lt;xsl:template match="*" mode="mode0"&gt;
    *     &lt;xsl:for-each select="item"&gt;
    *      &lt;td&gt;
    *         &lt;xsl:apply-templates select="category_id"/&gt;
    *      &lt;/td&gt;
    *     &lt;/xsl:for-each&gt;
    *   &lt;/xsl:template&gt;
    *
    *
    * </pre>
    */
   private void fixRepeatContexts()
   {
      NodeList nl = m_xmlDoc.getElementsByTagName("xsl:template");
      int len = nl.getLength();
      for(int i = 0; i < len; i++)
      {
         Element template = (Element)nl.item(i);
         // fix template if the repeat flag exists
         if(template.hasAttribute(ms_strRepeatFlag))
         {
            template.removeAttribute(ms_strRepeatFlag);
            String match = template.getAttribute("match");
            //Fix the context in the match attribute
            int matchIndex = match.indexOf("/");
            if(matchIndex != -1)
               template.setAttribute(
                  "match", match.substring(0, matchIndex));
            // Find xsl:for-each
            NodeList nlist = template.getElementsByTagName("xsl:for-each");
            if(nlist.getLength() == 0)
               return; // No for-each found. This should not happen
            Element foreach = (Element)nlist.item(0);
            String select = foreach.getAttribute("select");
            String temp1 = match.substring(match.indexOf("/") + 1) + "/";
            if(select.startsWith(temp1))
            {
               String cxt = select.substring(temp1.length());
               // fix the context in the select attribute of the for each
               foreach.setAttribute("select", cxt);
               fixRepeatApplyTemplatesContexts(foreach, cxt);
            }
         }
      }
   }

   /**
    * A recursive method that looks for all xsl:apply-templates elements
    * (under the repeat template) and fixes the context in the select
    * attribute, if needed.
    * @param node the node to be checked, assumed not <code>null</code>
    * @param context the context prefix string, this prefix gets chopped off
    * the select attribute string as the parent xsl:for-each is already at
    * this position in the tree
    */
   private void fixRepeatApplyTemplatesContexts(Node node, String context)
   {
      NodeList children = node.getChildNodes();
      int len = children.getLength();
      for(int i = 0; i < len; i++)
      {
         Node current = children.item(i);
         if(current.hasChildNodes())
            fixRepeatApplyTemplatesContexts(current, context);
         if(current instanceof Element
            && ((Element)current).getNodeName()
               .equalsIgnoreCase("xsl:apply-templates"))
         {
            String select = ((Element)current).getAttribute("select");
            if(select.startsWith(context + "/"))
               ((Element)current).setAttribute(
                  "select", select.substring(context.length() + 1));
         }
      }
   }

   /**
    * Tune all template match attributes for the entire document. This means
    * we replace all match attributes from '.' to '*' and remove all children
    * from 'xsl:template' nodes who's match attributes start with '@' or '/@'.
    * TODO: why are we doing this?
    */
   private void tuneTemplateMatchAttributes()
   {
      Element elem = null;
      String strAttr = "";
      NodeList nodes = m_xmlDoc.getElementsByTagName("*");
      for (int i=0; i<nodes.getLength(); i++)
      {
         elem = (Element) nodes.item(i);
         strAttr = elem.getAttribute("match");
         if (strAttr.equals("."))
            elem.setAttribute("match", "*");
      }

      nodes = m_xmlDoc.getElementsByTagName("xsl:template");
      for (int i=0; i<nodes.getLength(); i++)
      {
         elem = (Element) nodes.item(i);
         strAttr = elem.getAttribute("match");
         if (strAttr.startsWith("@") || strAttr.startsWith("/@"))
            elem.getParentNode().removeChild(elem);
      }
   }

   /**
    * Remove the attribute with the provided name if it exists from the passed
    * element.
    *
    * @param elem the element from which to remove the attribute
    * @param strAttr the attribute name to remove.
    * @throws DOMException for any DOM function that failed.
    */
   public static void removeAttribute(Element elem,
                                      String strAttr) throws DOMException
   {
      if (elem == null)
         throw new IllegalArgumentException("This method needs a valid element!");

      if (strAttr == null || strAttr.equals(""))
         throw new IllegalArgumentException("You provided an invalid attribute name!");

      if (elem.getAttribute(strAttr).equals(""))
         return;

      elem.removeAttribute(strAttr);
   }

   /**
    * Prints the current XML document to the system output queue.
    */
   private void debugOutput()
   {
      try
      {
         Document xslDoc = getXMLDocument();
         StringWriter wr = new StringWriter();
         HTML2XSL.printNode(xslDoc, TAB, wr);
         System.out.println(new String(wr.getBuffer()));
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
   }

   /**
    * Create the XSL file for the output file which must have been initialized
    * earlier. This will add the default file extension.
    */
   public void createXSLFile()
   {
      if (m_strHTMLFile.trim().equals(""))
         return;

      String strXSLFile;
      int nLoc = m_strHTMLFile.indexOf('.');
      strXSLFile = (nLoc != -1) ? m_strHTMLFile.substring(0, nLoc) : m_strHTMLFile;

      createXSLFile(strXSLFile + ".xsl");
   }

   /**
    * Creates the output file and prints teh contents of the current XML
    * document to it.
    *
    * @param strOutputFile file name to create and to write to.
    */
   public void createXSLFile(String strOutputFile)
   {
      try
      {
         FileOutputStream os = new FileOutputStream(strOutputFile);
         OutputStreamWriter out = new OutputStreamWriter(os, m_charEncoding);
         PrintWriter pw = new PrintWriter(out);

         writeXmlHeader(out, m_charEncoding);
         printNode(m_xmlDoc, TAB, pw);

         pw.flush();
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
   }

   /**
    * Loops recursivly through all child elements of the provided node and
    * adds the base (root) to all found dynamic element and attribute values.
    * While adding the base elements, this also converts assembler property
    * mark-up from psx-attr="$property/..." to rx-attr="property/... because
    * $property is not a valid XML name.
    *
    * @param node the node for which the base is added to all dynamic element
    *    values and attributes, assumed not <code>null</code>.
    * @param strBase the base to add, assumed not <code>null</code>.
    * @param strPsxTag the tag with which dynamic fields and attributes are
    *    marked-up.
    */
   private static void addBaseElement(Node node, String strBase,
      String strPsxTag)
   {
      NodeList nodeList;
      Node nodeTemp=null;

      if (node.hasChildNodes())
      {
         nodeList = node.getChildNodes();
         for (int i=0; i<nodeList.getLength(); i++)
         {
            nodeTemp = nodeList.item(i);
            addBaseElement(nodeTemp, strBase, strPsxTag);
         }
      }

      if (Node.TEXT_NODE == node.getNodeType())
      {
         String strTemp = null;
         String strValue = node.getNodeValue();
         if (null != strValue)
            strValue = strValue.trim ();

         if (null != strValue && strValue.trim().length() > 0)
         {
            strTemp = "";
            if (strValue.startsWith(strPsxTag))
            {
               strTemp = strPsxTag;
               strValue = strValue.substring(strPsxTag.length());
               if (strValue.startsWith("/"))
               {
                  strValue = strValue.substring (1);
                  strTemp += "/";
               }
               if (!contextStartsWith(strValue,strBase))
               {
                  strTemp += strBase + "/" + strValue;
                  node.setNodeValue(strTemp);
               }
            }
         }
      }
      else
      {
         NamedNodeMap attributes = node.getAttributes();
         if (null == attributes)
            return;

         AssemblerTransformation.addDynamicSlotId((Element) node);

         Attr attr = null;
         String strName = null;
         String strValue = null;
         String strTemp = null;
         for (int j=0; j<attributes.getLength(); j++)
         {
            attr = (Attr) attributes.item(j);
            if (AssemblerTransformation.convertToRxMarkup((Element) node, attr))
               continue;

            strName = attr.getName();
            strValue = attr.getValue();
            if (strName.startsWith(strPsxTag) ||
               strValue.startsWith(strPsxTag))
            {
               if (strName.startsWith(strPsxTag))
               {
                  strTemp = "";
                  if (strValue.startsWith(strPsxTag))
                     strValue = strValue.substring(strPsxTag.length());
               }
               else
               {
                  strTemp = strPsxTag;
                  strValue = strValue.substring(strPsxTag.length());
               }
               if (strValue.startsWith("/"))
               {
                  strValue = strValue.substring (1);
                  strTemp += "/";
               }
               if (false == contextStartsWith(strValue, strBase))
               {
                  strTemp += strBase + "/" + strValue;
                  attr.setValue(strTemp);
               }
            }
         }
      }
   }

   /**
    * @param src
    * @param strBase
    * @param strPsxTag
    * @return A string that contains an XML document.
    * @throws IOException
    * @throws SAXException
   **/
   public static String AddBaseElement(String src, String strBase,
         String strPsxTag) throws IOException, SAXException
   {
      Document xmlDoc = HTML2XSL
            .getXMLDocFromInputStream(new StringReader(src));
      addBaseElement(xmlDoc, strBase, strPsxTag);

      StringWriter sw = new StringWriter();
      writeXmlHeader(sw);
      HTML2XSL.printNode(xmlDoc, TAB, sw);
      return sw.toString();
   }


   /**
    * See the method by the same name w/ 3 params for a description. Writes
    * the header w/ standalone = false.
    * @param w
    * @param enc
    * @throws IOException
   **/
   public static void writeXmlHeader( Writer w, String enc )
      throws IOException
   {
      writeXmlHeader( w, false, enc );
   }

   /**
    * Writes an XML decl (if standalone is <code>true</code>), or an
    * XML/encoding decl.
    *
    * @param w The writer to which the header will be written. The encoding used
    *           by this writer should match the supplied encoding.
    *
    * @param standalone If <code>true</code>, adds 'standalone="yes"' to the
    *           XML decl.
    *
    * @param enc The java name of the character encoding to use when writing the
    *           XSL file.
    * @throws IOException
    */
   public static void writeXmlHeader( Writer w, boolean standalone, String enc)
      throws IOException
   {
      if (  null == enc || 0 == enc.trim().length())
         throw new IllegalArgumentException( "Encoding cannot be null" );
      // Verify the encoding is a valid one. This will throw if it is not.
      InputStreamReader foo = new InputStreamReader(new ByteArrayInputStream(
            new byte[1]), enc);

      w.write("<?xml version='1.0' encoding='");
      w.write( PSCharSets.getStdName( enc ));
      if ( standalone )
         w.write( "' standalone='yes' ?>" + NEWLINE );
      else
         w.write("'?>" + NEWLINE );
   }

   /**
    * Writes out the xml header with the XML version and the character
    * encoding reported by the Java VM.
    * @param w
    * @throws IOException
    */
   public static void writeXmlHeader(Writer w)
      throws IOException
   {
      String enc = System.getProperty("file.encoding");
      writeXmlHeader(w, enc);
   }

   /**
    * This function actually goes through all apply-template nodes and makes
    * the necessary node context corrections.
    *
    * @param node the node to process.
    */
   private void loopForApplyTemplates(Node node)
   {
      if (null == node)
         return;

      if (node.hasChildNodes())
      {
         NodeList nodeList = node.getChildNodes();
         Node nodeTemp = null;
         for (int i=0; i<nodeList.getLength(); i++)
         {
            nodeTemp = nodeList.item(i);
            loopForApplyTemplates(nodeTemp);
         }
      }

       // ignore all nodes other than xsl:apply-templates
      String strNodeName = node.getNodeName();
      if (!strNodeName.equals("xsl:apply-templates"))
         return;

      /*
      having matching template and attribute starting with '@' - remote
      possibility and bad tagging!!! skip it
      */
      String strAttr = ((Element) node).getAttribute("select");
      Node nodeMatch = getMatchTemplateElement((Element) node);
      if (null != nodeMatch && !strAttr.startsWith("@"))
      {
         String strBase = correctContextOnNode(nodeMatch);
         if (strBase.equals(""))
            return;

         ((Element) node).setAttribute("select", strBase);
         ((Element) node).setAttribute(ms_strContextNodeName ,strBase);

         return;
      }

      return;
   }

   /**
    * Get the first node from the provided context. The root is not considered
    * as a node.
    *
    * @param strContext the context we want the first node from.
    * @return the first node
    */
   private String getFirstInNodeContext(String strContext)
   {
      boolean bStartsWithRoot = false;
      if (contextStartsWith(strContext, m_strBaseContext))
         bStartsWithRoot = true;

      int nLoc = strContext.indexOf('/');
      if (-1 == nLoc)
         return strContext;

      if (bStartsWithRoot)
         nLoc = strContext.indexOf('/', nLoc+1);

      return ((-1 == nLoc) ? strContext : strContext.substring(0, nLoc));
   }

   /**
    * Get the context minus the base. Also create the root context if not
    * existing yet. If the paramters are invalid for this operation the
    * original context will be returned.
    *
    * @param elem the element to create the root context for.
    * @param strContext the context we want to subtract the base from.
    * @param strBase the base to subtract.
    * @return the subtracted context.
    */
   private String getContextMinusBase(Element elem,
                                      String strContext, String strBase)
   {
      String strRootContext = elem.getAttribute(ms_strRootContext);
      if (strRootContext == null || strRootContext.equals(""))
         elem.setAttribute(ms_strRootContext, strContext);

      strContext = correctContext(strContext);
      String strRet = strContext;
      if (contextStartsWith(strContext, strBase))
      {
         if (strBase.length() == strContext.length())
            return ".";

         if (strContext.charAt(strBase.length ()) != '/')
            return strContext;

         strRet = strContext.substring(strBase.length() + 1);
      }

      return strRet;
   }

   /**
    * Get the "match" template element from the provided element.
    *
    * @param elemApplyTemplates the element we are looking for the "match"
    *    template in.
    * @return the "match" template element if found or null.
    */
   private Element getMatchTemplateElement(Element elemApplyTemplates)
   {
      Element elem = null;
      String strSelect = elemApplyTemplates.getAttribute("select");
      if (strSelect.equals(""))
         return null;

      String strMode = elemApplyTemplates.getAttribute("mode");
      NodeList nodesTemplate = m_xmlDoc.getElementsByTagName("xsl:template");
      String strMatch = "", strModeMatch="";
      for (int i=0; i<nodesTemplate.getLength(); i++)
      {
         elem = (Element) (nodesTemplate.item(i));
         strModeMatch = elem.getAttribute("mode");
         if(!strMode.trim().equals("") && strModeMatch.equals(strMode))
            return elem;
      }

      return null;
   }

   /**
    * Update the context table provided with the context information found in
    * the given node. If the provided node is a comment we look for docalias
    * and docref tags. If we found both we add them to our tabls.
    *
    * @param node a node to look for external document definitions
    * @param contexts the table we should update.
    */
   public static void updateExternalDocRef(Node node, Hashtable contexts)
   {
      if (node.getNodeType() != Node.COMMENT_NODE)
         return;

      String aliasTag = m_strPsxTag + ms_docAlias;
      String refTag = m_strPsxTag + ms_docRef;

      String comment = ((Comment) node).getData().trim();
      int aliasPos = comment.indexOf(aliasTag);
      int refPos = comment.indexOf(refTag);
      if (aliasPos == -1 || refPos == -1)
         return;

      // get the alias
      int openQuote = comment.indexOf('"');
      if (openQuote == -1)
         return;
      int closeQuote = comment.indexOf('"', openQuote+1);
      if (closeQuote == -1)
         return;
      String alias = comment.substring(openQuote+1, closeQuote);

      // get the reference
      openQuote = comment.indexOf('"', closeQuote+1);
      if (openQuote == -1)
         return;
      closeQuote = comment.indexOf('"', openQuote+1);
      if (closeQuote == -1)
         return;
      String reference = comment.substring(openQuote+1, closeQuote);

      contexts.put(alias, reference);

      return;
   }

   /**
    * This function is the engine for HTML to XSL transformations and is
    * called recursively for all HTML nodes.<p>
    * All 'xsl:template' nodes will not be touched but the ones addressing the
    * root ('match="/"').<p>
    * Currently there are two main handlers, one for element nodes and one for
    * text nodes. The element node handler uses several sub-handlers to
    * transform the document. We support handlers for:
    * <ul>
    * <li>text rendering (span/id, psx-)</li>
    * <li>images (img)</li>
    * <li>links (a)</li>
    * <li>forms (form)</li>
    * <li>lists (tr, li, option)</li>
    * <li>inputs (input)</li>
    * <li>textarea</li>
    * <li>tables (img)</li>
    * </ul>
    *
    * @param htmlNode the HTML node to transform.
    * @throws DOMException
    * @throws SplitterException
    */
   private void transformNode(Node htmlNode) throws DOMException,
      SplitterException
   {
      String strNodeName = htmlNode.getNodeName();
      updateExternalDocRef(htmlNode, m_contexts);

      if (Node.ELEMENT_NODE == htmlNode.getNodeType())
      {
         if (strNodeName.equals ("xsl:template"))
         {
            String strAttr = ((Element) htmlNode).getAttribute("match");
            if (!strAttr.equals("/"))
               return;
         }
      }

      prepareSelectElement(htmlNode);

      NodeList nodeList = null;
      Node nodeTemp = null;
      if (htmlNode.hasChildNodes())
      {
         // transform all child HTML nodes first
         nodeList = htmlNode.getChildNodes();
         for (int i=0; i<nodeList.getLength(); i++)
         {
            nodeTemp = nodeList.item(i);
            transformNode(nodeTemp);
         }
      }

      // transform the HTML node
      switch (htmlNode.getNodeType())
      {
         case Node.COMMENT_NODE:
            if (m_casTransformator.transformSlots(m_xmlDoc, htmlNode))
               m_hasSlots = true;
            break;

         case Node.ELEMENT_NODE:
            if (m_casTransformator.transformSlots(m_xmlDoc, htmlNode))
               m_hasSlots = true;

            modifyNodeContext(htmlNode);

            if (strNodeName.equals("html"))
               transformHtmlElement(htmlNode);
            else if (strNodeName.equals("span"))
               transformSpanElement(htmlNode);
            else if (strNodeName.equals("div"))
               transformDivElement(htmlNode);
            else if (strNodeName.equals("img"))
               transformImageElement(htmlNode);
            else if (strNodeName.equals("a"))
               transformAnchorElement(htmlNode);
            else if (strNodeName.equals("form"))
               transformFormElement(htmlNode);
            else if (strNodeName.equals("td") ||
                     strNodeName.equals("tr") ||
                     strNodeName.equals("li") ||
                     strNodeName.equals("select") ||
                     strNodeName.equals("option"))
               transformRepeatedElement(htmlNode);
            else if(strNodeName.equals("input"))
               transformInputElement(htmlNode);
            else if(strNodeName.equals("textarea"))
               transformTextAreaElement(htmlNode);

            modifyAttributes(htmlNode);
            modifyNodeContext(htmlNode);
            break;

         case Node.TEXT_NODE:
            transformTextNode(htmlNode);
            break;

         default:
            break;
      }
   }

   /**
    * Transformer for the HTML element.
    *
    * @param node the HTML element, assumed not <code>null</code>.
    */
   private void transformHtmlElement(Node node)
   {
      Element elem = (Element) node;

      String globalTemplateMarkup = elem.getAttribute(ATTR_PSXGLOBALTEMPLATE)
            .trim();
      if (globalTemplateMarkup.length() != 0)
      {
         m_globalTemplateMarkup = globalTemplateMarkup;
         elem.removeAttribute(ATTR_PSXGLOBALTEMPLATE);
      }
      //Remove the global template name attribute if present.
      elem.removeAttribute(ATTR_PSXGLOBALTEMPLATE_NAME);
   }

   /**
    * Storage for the global template markup. Initialized in
    * {@link #transformHtmlElement(Node)}, never <code>null</code>, may be empty.
    */
   private String m_globalTemplateMarkup = "";

   /**
    * Transforms all property mark-ups found in the provided nodes children
    * and the node itself.
    *
    * @param node the node to transform all properties for, assumed not
    *   <code>null</code>.
    * @param transformator the properties transformator to use, assumed not
    *    <code>null</code>.
    */
   private void transformProperties(Node node,
      AssemblerTransformation transformator)
   {
      if (node.hasChildNodes())
      {
         NodeList nodeList = node.getChildNodes();
         for (int i=0; i<nodeList.getLength(); i++)
            transformProperties(nodeList.item(i), transformator);
      }

      if (node.getNodeType() == Node.ELEMENT_NODE)
      {
         if (transformator.transformProperties(m_xmlDoc,
            (Element) node, m_contexts))
            m_hasProperties = true;
      }
   }

   /**
    * Use this function to transform all prefixed or spaned elements found
    * in static text (outside of any other tag). The follwing inputs:
    *
    * @param htmlNode the node to transform.
    * @return the transformed element or null.
    * @throws DOMException for any DOM function failed.
    */
   private Element transformTextNode(Node htmlNode) throws DOMException
   {
      String strElemValue = htmlNode.getNodeValue();
      if (false == strElemValue.trim().startsWith(m_strPsxTag))
         return null;

      strElemValue = strElemValue.trim();
      String strContext = context(strElemValue);

      Element nodeElem = m_xmlDoc.createElement("xsl:apply-templates");
      nodeElem.setAttribute("select", strContext);
      nodeElem.setAttribute(ms_strContextNodeName, strContext);

      Node nodeParent = htmlNode.getParentNode();
      if (null != nodeParent)
         nodeParent.replaceChild(nodeElem, htmlNode);

      return nodeElem;
   }

   /**
    * This method prepares the context string. First the prefix (psx-) is
    * removed. Then it is checked if its a document context. If thats so we
    * remove the first '/' so we are able to use the normal translations and
    * post-process the document contexts later.
    *
    * @param strContext the string to prepare the context for
    * @return the context string.
    */
   private String context(String strContext)
   {
      // first remove the prefix (psx-)
      strContext = strContext.substring(m_strPsxTag.length());

      return repeat(strContext);
   }

   /**
    * This method prepares the repeat context string. It is checked if its a
    * document context. If thats so we remove the first '/' so we are able to
    * use the normal translations and post-process the document contexts later.
    *
    * @param strContext the string to prepare the repeat context for
    * @return the context string.
    */
   private String repeat(String strContext)
   {
      // then check if this is a document context and remove the leading '/'
      if (strContext.startsWith("/"))
      {
         int start = strContext.indexOf('/', 1);
         if (start != -1)
         {
            int end = strContext.indexOf('/', start+1);
            if (end == -1)
               end = strContext.length();

            String docContext = strContext.substring(start+1, end);
            if (!m_contexts.containsKey(docContext))
               m_contexts.put(docContext, "unknownRef" + m_unknownDocRef++);

            strContext = strContext.substring(1, strContext.length());
         }
      }

      return correctContext(strContext);
   }

   /**
    * Helper function to initialize the context string.
    *
    * @param htmlElement the element to initialize for
    * @return the context string if transformation is nessecary,
    *    null otherwise.
    */
   private String initContext(Element htmlElement)
   {
      return initContext(htmlElement, "id");
   }

   /**
    * Helper function to initialize the context string for the given attribute
    * name.
    *
    * @param htmlElement the element to initialize for, must not be
    * <code>null</code>
    * @param attrName of the attribute to get the context from, must not be
    * <code>null</code>
    * @return the context string if transformation is nessecary,
    *    null otherwise.
    */
   private String initContext(Element htmlElement, String attrName)
   {
      if (htmlElement == null || attrName==null)
         return null;

      String strContext = htmlElement.getAttribute(attrName);
      strContext = strContext.trim();
      if (false == strContext.startsWith(m_strPsxTag))
         return null;

      return context(strContext);
   }

   /**
    * Use this function to transform all prefixed or spaned elements found
    * in static text (outside of any other tag). The follwing inputs:
    * <ul>
    * <li><some text "psx-products/product/text" some more text></li>
    * <li><some text <span id="psx-products/product/text">dynamic sample</span></li>
    * </ul>
    * Are transformed to:
    * <ul>
    * <li><xsl:apply-templates select="root/products/product/text"/></li>
    * </ul>
    *
    * @param htmlNode the node to transform.
    * @return the transformed element or null.
    * @throws DOMException for any DOM function failed.
    */
   private Element transformSpanElement(Node htmlNode) throws DOMException
   {
      if (Node.ELEMENT_NODE != htmlNode.getNodeType())
         return null;

      Element htmlElem = (Element) htmlNode;
      /*
       * if the special attribute "psxshow" attribute is present, replace the
       * attribiute name with "id" as it is an alias for "id"
       */
      Attr attrPsxshow = htmlElem.getAttributeNode("psxshow");
      if(attrPsxshow!=null)
      {
         /*
          * Note that the "psxshow" attribute overrides the "id" attribute,
          * if present
          */
         htmlElem.setAttribute("id", attrPsxshow.getValue());
         htmlElem.removeAttributeNode(attrPsxshow);
      }

      Attr attrPsxedit = htmlElem.getAttributeNode("psxedit");
      /*
       * if the special attribute "psxedit" is present, treat it differently.
       * This field maps the assembly field to content editor field.
       */
      if(attrPsxedit != null)
      {
         //make sure to remove the psx-prefix from the attribute value
         String modifiedContext = initContext(htmlElem, attrPsxedit.getName());
         if(modifiedContext != null)
         {
            /*
             * make sure to remove any other context nodes e.g. root/ and keep
             * the last one only
             */
            int loc = modifiedContext.lastIndexOf('/');
            if(loc != -1)
               modifiedContext = modifiedContext.substring(loc+1);
            attrPsxedit.setValue(modifiedContext);
         }
      }

      String strContext = initContext(htmlElem);
      if (strContext == null)
         return null;

      Element nodeElem = m_xmlDoc.createElement("xsl:apply-templates");
      nodeElem.setAttribute("select", strContext);
      nodeElem.setAttribute(ms_strContextNodeName, strContext);

      if(attrPsxedit == null)
      {
         htmlNode.getParentNode().replaceChild(nodeElem, htmlNode);
      }
      else
      {

         /*
          * if "psxedit" attribute is present we need to preserve the SPAN
          * element, remove its children and append the newly created element
          * as child
          */
         htmlElem.removeAttribute("id");
         NodeList nl = htmlElem.getChildNodes();
         for(int i=0; nl!=null && i<nl.getLength(); i++)
         {
            htmlElem.removeChild(nl.item(i));
         }
         htmlNode.appendChild(nodeElem);
      }
      return nodeElem;
   }

   private Element transformDivElement(Node htmlNode) throws DOMException
   {
      return transformSpanElement(htmlNode);
   }

   /**
    * Transform the image element provided. The following types of inputs will
    * be transformed:
    * <ul>
    * <li><IMG SRC="psx-products/product/image", height="320", width="400">
    * </li>
    * <li><IMG SRC="sample.gif", height="320", width="400",
    * id="psx-products/product/image"></li>
    * </ul>
    * Into this output:
    * <ul>
    * <li><xsl:attribute name="src"> <xsl:value-of
    * select="root/products/product/image"/> </xsl:attribute></li>
    * </ul>
    *
    * @param htmlNode the node to transform.
    * @throws DOMException for any DOM function failed.
    */
   private void transformImageElement(Node htmlNode) throws DOMException
   {
      Element htmlElem = null;
      if (Node.ELEMENT_NODE != htmlNode.getNodeType())
         return;
      htmlElem = (Element) htmlNode;

      String strContext = initContext(htmlElem);
      if (strContext == null)
         return;

      removeAttribute(htmlElem, "id");
      removeAttribute(htmlElem, "src");

      htmlElem.setAttribute(ms_strContextNodeName, strContext);

      Element nodeElem = m_xmlDoc.createElement("xsl:attribute");
      nodeElem.setAttribute("name", "src");
      nodeElem.setAttribute(ms_strContextNodeName, strContext);

      htmlNode.appendChild(nodeElem);

      Element nodeElemChild = m_xmlDoc.createElement("xsl:value-of");
      nodeElemChild.setAttribute("select", strContext);
      nodeElem.setAttribute(ms_strContextNodeName, strContext);
      nodeElem.appendChild(nodeElemChild);
   }

   /**
    * Transform the anchor element provided. The following types of inputs
    * will be transformed:
    * <ul>
    * <li><a href="psx-products/product/link">HERE</a></li>
    * <li><a href="sample.htm" id="psx-products/product/link">HERE</a></li>
    * </ul>
    * Into this output:
    * <ul>
    * <li><a href="sample.htm" id="root/products/product/link">HERE</a></li>
    * <li><a><xsl:attribute name="href">
    *               <xsl:value-of select="root/products/product/link"/>
    *        </xsl:attribute>HERE</a></li>
    * </ul>
    *
    * @param htmlNode the node to transform.
    * @throws DOMException for any DOM function failed.
    */
  private void transformAnchorElement(Node htmlNode) throws DOMException
   {
      Element htmlElem = null;
      if (Node.ELEMENT_NODE != htmlNode.getNodeType())
         return;
      htmlElem = (Element) htmlNode;

      String strContext = initContext(htmlElem);
      if (strContext == null)
         return;

      boolean hasHrefAttr = (htmlElem.getAttribute("href").length() > 0);

      removeAttribute(htmlElem, "id");


      htmlElem.setAttribute(ms_strContextNodeName, strContext);

      // Only add the href attribute if it existed to begin with
      if(hasHrefAttr)
      {
         removeAttribute(htmlElem, "href");

         Element nodeElem = m_xmlDoc.createElement("xsl:attribute");
         nodeElem.setAttribute("name", "href");
         nodeElem.setAttribute(ms_strContextNodeName, strContext);

         htmlNode.insertBefore(nodeElem, htmlNode.getFirstChild());


         Element nodeElemChild = m_xmlDoc.createElement("xsl:value-of");
         nodeElemChild.setAttribute("select", strContext);
         nodeElemChild.setAttribute(ms_strContextNodeName, strContext);

         nodeElem.appendChild(nodeElemChild);
      }
   }
   /**
    * Transform the form element provided. The following types of inputs
    * will be transformed:
    * <ul>
    * <li><form action="psx-products/product/form" method="post">...</form></li>
    * <li><form action="sample.htm", id="psx-products/product/form" method="post">...</form></li>
    * </ul>
    * Into this output:
    * <ul>
    * <li><a href="sample.htm" id="root/products/product/link">HERE</a></li>
    * <li><a><<form method="post">
    *            <xsl:attribute name="action">
    *               <xsl:value-of select="root/products/product/form"/>
    *            </xsl:attribute>...</form></li>
    * </ul>
    *
    * @param htmlNode the node to transform.
    * @throws DOMException for any DOM function failed.
    */
   private void transformFormElement(Node htmlNode) throws DOMException
   {
      Element htmlElem = null;
      if (Node.ELEMENT_NODE != htmlNode.getNodeType())
         return;
      htmlElem = (Element) htmlNode;

      String strContext = initContext(htmlElem);
      if (strContext == null)
         return;

      removeAttribute(htmlElem, "id");
      removeAttribute(htmlElem, "action");

      htmlElem.setAttribute(ms_strContextNodeName, strContext);

      Element nodeElem = m_xmlDoc.createElement("xsl:attribute");
      nodeElem.setAttribute("name", "action");
      nodeElem.setAttribute(ms_strContextNodeName, strContext);

      htmlNode.appendChild(nodeElem);

      Element nodeElemChild = m_xmlDoc.createElement("xsl:value-of");
      nodeElemChild.setAttribute("select", strContext);
      nodeElemChild.setAttribute(ms_strContextNodeName, strContext);

      nodeElem.appendChild(nodeElemChild);
   }

   /**
    * Prepare repeat context for the provided element.
    *
    * @param htmlElem the element to prepare.
    * @param strContext
    * @return the current repeat string or null if none is defined.
    */
   private String prepareRepeatContext(Element htmlElem, String strContext)
   {
      String strRepeat = htmlElem.getAttribute("psx-repeat");
      if (strRepeat != null && !strRepeat.equals(""))
      {
         // remove the repeat tag and set the repeat context
         htmlElem.removeAttribute("psx-repeat");

         htmlElem.setAttribute(ms_strRepeatContext, repeat(strRepeat));
      }

      strRepeat = htmlElem.getAttribute(ms_strRepeatContext);
      if (strRepeat != null && !strRepeat.equals(""))
         return strRepeat;

      return null;
   }

   /**
    * Transform the provided element. This function repeats all elements
    * provided according to their context.
    *
    * @param htmlNode the node to transform.
    * @throws DOMException for any DOM function failed.
    */
   private void transformRepeatedElement(Node htmlNode) throws DOMException
   {
      String repeatAttr = ((Element) htmlNode).getAttribute(m_strPsxTag + "repeat");
      if (repeatAttr.equals("") || Node.ELEMENT_NODE != htmlNode.getNodeType())
         return;

      String strContext = ((Element) htmlNode).getAttribute(ms_strContextNodeName);
      if (strContext.equals(""))
         return;

      String strRepeat = prepareRepeatContext((Element) htmlNode, strContext);
      if (strRepeat != null)
      {
         if (strRepeat.lastIndexOf("/") != -1)
            strContext = strRepeat.substring(0, strRepeat.lastIndexOf("/"));
         else
            strContext = strRepeat;
      }

      String strMode = makeMode();
      Element nodeElem = m_xmlDoc.createElement("xsl:apply-templates");
      nodeElem.setAttribute("select", strContext);
      nodeElem.setAttribute(ms_strContextNodeName, strContext);
      nodeElem.setAttribute("mode", strMode);

      Node elemOld = htmlNode.getParentNode().replaceChild(nodeElem, htmlNode);

      NodeList nodeList = m_xmlDoc.getElementsByTagName("xsl:stylesheet");
      if (0 == nodeList.getLength())
         return;

      nodeElem = m_xmlDoc.createElement("xsl:template");
      nodeElem.setAttribute("match", strContext);
      nodeElem.setAttribute(ms_strContextNodeName, strContext);
      nodeElem.setAttribute("mode", strMode);
      nodeElem.setAttribute(ms_strRepeatFlag, "yes");

      Element elemStyleSheet = (Element) nodeList.item(0);
      Element elemTmp1 = (Element) elemStyleSheet.appendChild(nodeElem);

      Element elemTmp = (Element) m_xmlDoc.createElement("xsl:for-each");
      elemTmp.setAttribute("select", strContext);
      elemTmp.setAttribute(ms_strContextNodeName, strContext);

      elemTmp1 = (Element) elemTmp1.appendChild(elemTmp);

      elemTmp1.appendChild(elemOld);
   }

   /**
    * Transform the provided input element.
    *
    * @param htmlNode an input node to be transformed
    * @throws DOMException for every DOM function failed.
    */
   private void transformInputElement(Node htmlNode) throws DOMException
   {
      if (Node.ELEMENT_NODE != htmlNode.getNodeType())
         return;

      Element htmlElem = (Element) htmlNode;

      String strAttr = "";
      String strContext = "";
      Element sibElem = null;
      String strType = htmlElem.getAttribute("type");
      if (strType.equals("radio") || strType.equals("checkbox"))
      {
         Node sib = getNextValidSibling(htmlElem);
         strContext = htmlElem.getAttribute("id");
         if (!strContext.startsWith(m_strPsxTag) && null == sib)
            return;

         if (!strContext.startsWith(m_strPsxTag))
         {
            if (Node.TEXT_NODE == sib.getNodeType())
            {
               sibElem = transformTextNode(sib);
               if (null != sibElem)
               {
                  strContext = sibElem.getAttribute(ms_strContextNodeName);
                  if (!strContext.equals(""))
                     strContext = m_strPsxTag + strContext;
               }
            }
            else // got to be a SPAN element
            {
               sibElem = transformSpanElement(sib);
               if (null != sibElem)
               {
                  strContext = sibElem.getAttribute(ms_strContextNodeName);
                  if (!strContext.equals(""))
                     strContext = m_strPsxTag + strContext;
               }
            }
         }
         else
         {
            htmlElem.removeAttribute("id");
            sib = null;
         }

         if (null == sib && strContext.startsWith(m_strPsxTag))
         {
            sib = m_xmlDoc.createTextNode(strContext);
            try
            {
               sib = htmlNode.insertBefore(sib, htmlNode.getNextSibling());
            }
            catch(Exception e)
            {
               sib = htmlNode.getParentNode().appendChild(sib);
            }

            if (null != sib)
               sibElem = transformTextNode(sib);
         }
      }

      if (strType.equals ("image"))
      {
         strAttr = "src";

         /*
         check if the id attribute has a psx-tag. if so, set the context and
         remove the id attribute
         */
         strContext = htmlElem.getAttribute("id").trim ();
         if (strContext.startsWith (m_strPsxTag))
         {
            htmlElem.setAttribute("src", strContext);
            htmlElem.removeAttribute ("id");
         }
      }

      htmlNode = modifyAttributes(htmlElem);
      htmlNode = modifyNodeContext(htmlNode);

      if (!strContext.startsWith(m_strPsxTag))
      {
         strContext = ((Element) htmlNode).getAttribute(ms_strContextNodeName);
         if (!strContext.equals(""))
            strContext = m_strPsxTag + ((Element) htmlNode).getAttribute(ms_strContextNodeName);
      }

      // if nothing is dynamic just return
      if (!strContext.startsWith(m_strPsxTag))
         return;

      strContext = context(strContext);
      Element nodeElem = m_xmlDoc.createElement("xsl:apply-templates");
      String strMode = makeMode();
      nodeElem.setAttribute("select", strContext);
      nodeElem.setAttribute(ms_strContextNodeName, strContext);
      nodeElem.setAttribute("mode", strMode);

      Node nodeParent = htmlNode.getParentNode();
      if (null == nodeParent)
         return;

      htmlNode = nodeParent.replaceChild(nodeElem, htmlElem);

      NodeList nodeList = m_xmlDoc.getElementsByTagName("xsl:stylesheet");
      if (0 == nodeList.getLength())
         return;

      nodeElem = m_xmlDoc.createElement("xsl:template");
      nodeElem.setAttribute("match", strContext);
      nodeElem.setAttribute(ms_strContextNodeName, strContext);
      nodeElem.setAttribute("mode", strMode);

      Element elemStyleSheet = (Element) nodeList.item(0);
      Element elemTmp1 = (Element) elemStyleSheet.appendChild(nodeElem);

      Element elemTmp = (Element) m_xmlDoc.createElement("xsl:for-each");
      elemTmp.setAttribute("select", strContext);
      elemTmp.setAttribute(ms_strContextNodeName, strContext);

      elemTmp1 = (Element) elemTmp1.appendChild(elemTmp);

      String strPara = nodeParent.getNodeName();
      if (null == strPara)
         return;

      strPara = strPara.toLowerCase();
      String strTmp = "";
      Node nodeTmp = null;
      if (strPara.equals("span"))
      {
         nodeTmp = nodeParent.getLastChild();
         while (Node.ELEMENT_NODE != nodeTmp.getNodeType())
            nodeTmp = nodeTmp.getPreviousSibling();

         if (null != nodeTmp)
            strTmp = nodeTmp.getNodeName().toLowerCase();
      }
      elemTmp = (Element) elemTmp1.appendChild(htmlNode);
      elemTmp.setAttribute(ms_strContextNodeName, strContext);
      modifyAttributes(elemTmp);

      if (null != sibElem)
      {
         elemTmp = (Element) elemTmp1.appendChild(sibElem);
         if (strTmp.equals("br"))
            elemTmp1.appendChild(nodeTmp);
      }
   }

   /**
    * Prepares the select element before processing it. Actually we add the
    * select attribute from the select element to all child option and
    * optgroup elements if it exists
    *
    * @param htmlNode the select node to be prepared
    * @throws DOMException for every DOM function failed.
    */
   private void prepareSelectElement(Node htmlNode) throws DOMException
   {
      if (Node.ELEMENT_NODE != htmlNode.getNodeType())
         return;

      Element htmlElem = (Element) htmlNode;
      if (!htmlElem.getTagName().equals("select"))
         return;

      String strSelected = htmlElem.getAttribute("selected");
      if (strSelected != null && !strSelected.equals(""))
      {
         NodeList children = htmlNode.getChildNodes();
         for (int i=0; i<children.getLength(); i++)
         {
            Node option = children.item(i);
            if (option.getNodeName().equals("option"))
            {
               ((Element) option).setAttribute("selected", strSelected);
            }
            else if (option.getNodeName().equals("optgroup"))
            {
               prepareOptionGroupElement(option, strSelected);
            }
         }
      }
   }

   /**
    * Prepares the optgroup element before processing it. Actually we add the
    * select attribute provided to all child option elements.
    *
    * @param htmlNode the optgroup node to be prepared
    * @param strSelected the selected attribute string which will be added.
    * @throws DOMException for every DOM function failed.
    */
   private void prepareOptionGroupElement(Node htmlNode,
                                          String strSelected) throws DOMException
   {
      if (Node.ELEMENT_NODE != htmlNode.getNodeType())
         return;

      Element htmlElem = (Element) htmlNode;
      if (!htmlElem.getTagName().equals("optgroup"))
         return;

      if (strSelected != null && !strSelected.equals(""))
      {
         NodeList children = htmlNode.getChildNodes();
         for (int i=0; i<children.getLength(); i++)
         {
            Node option = children.item(i);
            if (option.getNodeName().equals("option"))
            {
               ((Element) option).setAttribute("selected", strSelected);
            }
            else if (option.getNodeName().equals("optgroup"))
            {
               prepareOptionGroupElement(option, strSelected);
            }
         }
      }
   }

   /**
    * Finish up all input elements. This will update all input tags to make
    * the checked attribute conditional if it exists.
    *
    * @throws DOMException for every DOM function failed.
    */
   private void tuneInputTemplates() throws DOMException
   {
      Element elem = null;
      String strAttr = "";
      NodeList nodes = m_xmlDoc.getElementsByTagName("input");
      for (int i=0; i<nodes.getLength(); i++)
      {
         Node input = nodes.item(i);

         String strContext = ((Element) input).getAttribute(ms_strContextNodeName);

         String strValue = ((Element) input).getAttribute("value");
         if (strValue != null && !strValue.equals(""))
         {
            NodeList children = input.getChildNodes();
            for (int j=0; j<children.getLength(); j++)
            {
               Node child = children.item(j);
               if (!child.getNodeName().equals("xsl:attribute"))
                  continue;

               String strName = ((Element) child).getAttribute("name");
               if (strName != null && strName.equals("checked"))
               {
                  Element ifElem = m_xmlDoc.createElement("xsl:if");
                  String strTest = strContext + "='" + strValue + "'";
                  ifElem.setAttribute("test", strTest);

                  input.replaceChild(ifElem, child);
                  child.removeChild(child.getFirstChild());
                  ifElem.appendChild(child);
               }
            }
         }
         else
         {
            NodeList children = input.getChildNodes();
            Element valueElement = getValueAttr(children);
            for (int j=0; j<children.getLength(); j++)
            {
               Node child = children.item(j);
               if (!child.getNodeName().equals("xsl:attribute"))
                  continue;

               String strName = ((Element) child).getAttribute("name");
               if (strName != null && strName.equals("checked"))
               {
                  String strRootContext = ((Element) child).getAttribute(ms_strRootContext);

                  Element ifElem = m_xmlDoc.createElement("xsl:if");
                  String strTest = "$this/" + strRootContext + "=$value";
                  ifElem.setAttribute("test", strTest);

                  input.replaceChild(ifElem, child);
                  child.removeChild(child.getFirstChild());
                  ifElem.appendChild(child);

                  Element variable = m_xmlDoc.createElement("xsl:variable");
                  variable.setAttribute("name", "value");
                  Element valueOf = m_xmlDoc.createElement("xsl:value-of");
                  if (valueElement == null)
                     valueOf.setAttribute("select", ".");
                  else
                     valueOf.setAttribute("select", valueElement.getAttribute("select"));
                  variable.appendChild(valueOf);

                  input.insertBefore(variable, input.getFirstChild());
               }
            }
         }
      }
   }

   /**
    * Get the first value attribute found in the provided node list. If none
    * is found we will return null.
    *
    * @param nodes the nodes to look for a value attribute
    * @return the first value attribute found or null
    */
   private Element getValueAttr(NodeList nodes)
   {
      for (int i=0; i<nodes.getLength(); i++)
      {
         Node node = nodes.item(i);
         if (!node.getNodeName().equals("xsl:attribute"))
            continue;

         String strName = ((Element) node).getAttribute("name");
         if (strName != null && strName.equals("value"))
            return (Element) node.getFirstChild();
      }

      return null;
   }

   /**
    * Finish up all select elements. This will update all child option and
    * optgroup tags and make the selected attributes conditional if they
    * exist.
    *
    * @throws DOMException for every DOM function failed.
    */
   private void tuneOptionTemplates() throws DOMException
   {
      Element elem = null;
      String strAttr = "";
      NodeList nodes = m_xmlDoc.getElementsByTagName("option");
      for (int i=0; i<nodes.getLength(); i++)
      {
         Node option = nodes.item(i);

         String strContext = ((Element) option).getAttribute(ms_strContextNodeName);

         String strValue = ((Element) option).getAttribute("value");
         if (strValue != null && !strValue.equals(""))
         {
            NodeList children = option.getChildNodes();
            for (int j=0; j<children.getLength(); j++)
            {
               Node child = children.item(j);
               if (!child.getNodeName().equals("xsl:attribute"))
                  continue;

               String strName = ((Element) child).getAttribute("name");
               if (strName != null && strName.equals("selected"))
               {
                  Element ifElem = m_xmlDoc.createElement("xsl:if");
                  String strTest = strContext + "='" + strValue + "'";
                  ifElem.setAttribute("test", strTest);

                  option.replaceChild(ifElem, child);
                  child.removeChild(child.getFirstChild());
                  ifElem.appendChild(child);
               }
            }
         }
         else
         {
            NodeList children = option.getChildNodes();
            Element valueElement = getValueAttr(children);
            for (int j=0; j<children.getLength(); j++)
            {
               Node child = children.item(j);
               if (!child.getNodeName().equals("xsl:attribute"))
                  continue;

               String strName = ((Element) child).getAttribute("name");
               if (strName != null && strName.equals("selected"))
               {
                  String strRootContext = ((Element) child).getAttribute(ms_strRootContext);

                  Element ifElem = m_xmlDoc.createElement("xsl:if");
                  String strTest = "$this/" + strRootContext + "=$value";
                  ifElem.setAttribute("test", strTest);

                  option.replaceChild(ifElem, child);
                  child.removeChild(child.getFirstChild());
                  ifElem.appendChild(child);

                  Element variable = m_xmlDoc.createElement("xsl:variable");
                  variable.setAttribute("name", "value");
                  Element valueOf = m_xmlDoc.createElement("xsl:value-of");
                  if (valueElement == null)
                     valueOf.setAttribute("select", ".");
                  else
                     valueOf.setAttribute("select", valueElement.getAttribute("select"));
                  variable.appendChild(valueOf);
                  option.insertBefore(variable, option.getFirstChild());
               }
            }
         }
      }
   }

   /**
    * Transform the provided text area element.
    *
    * @param htmlNode an input node to be transformed
    * @throws DOMException for every DOM function failed.
    */
   private void transformTextAreaElement(Node htmlNode) throws DOMException
   {
      if (Node.ELEMENT_NODE != htmlNode.getNodeType())
         return;

      Element htmlElem = (Element) htmlNode;

      String strContext = "";

      /*
      Find if id attribute that has a psx-tag. If positive, set the context
      and remove the id attribute.
      */
      Node nodeChild = htmlElem.getFirstChild();
      strContext = htmlElem.getAttribute("id").trim();
      Text text= null;
      if (strContext.startsWith(m_strPsxTag))
      {
         htmlElem.removeAttribute("id");
         text = m_xmlDoc.createTextNode(strContext);
         if (null == nodeChild)
            nodeChild = (Text) htmlElem.appendChild(text);
         else
            nodeChild = (Text) htmlElem.replaceChild(text, nodeChild);

         transformTextNode(text);
      }

      htmlNode = modifyAttributes(htmlElem);
      htmlNode = modifyNodeContext(htmlNode);

      if (!strContext.startsWith(m_strPsxTag))
      {
         strContext = ((Element) htmlNode).getAttribute(ms_strContextNodeName);
         if (!strContext.equals(""))
            strContext = m_strPsxTag + ((Element) htmlNode).getAttribute(ms_strContextNodeName);
      }

      // nothing dynamic so return
      if(!strContext.startsWith(m_strPsxTag))
         return;

      strContext = context(strContext);

      Element nodeElem = m_xmlDoc.createElement("xsl:apply-templates");
      String strMode = makeMode();
      nodeElem.setAttribute("select", strContext);
      nodeElem.setAttribute(ms_strContextNodeName, strContext);
      nodeElem.setAttribute("mode", strMode);

      Node nodeParent = htmlNode.getParentNode();

      if (null == nodeParent)
         return;

      htmlNode = nodeParent.replaceChild(nodeElem, htmlElem);

      NodeList nodeList = m_xmlDoc.getElementsByTagName("xsl:stylesheet");
      if (0 == nodeList.getLength())
         return;

      nodeElem = m_xmlDoc.createElement("xsl:template");
      nodeElem.setAttribute("match", strContext);
      nodeElem.setAttribute(ms_strContextNodeName, strContext);
      nodeElem.setAttribute("mode", strMode);

      Element elemStyleSheet = (Element) nodeList.item(0);
      Element elemTmp1 = (Element) elemStyleSheet.appendChild(nodeElem);

      Element elemTmp = (Element) m_xmlDoc.createElement("xsl:for-each");
      elemTmp.setAttribute("select", strContext);
      elemTmp.setAttribute(ms_strContextNodeName, strContext);

      elemTmp1 = (Element) elemTmp1.appendChild(elemTmp);

      String strPara = nodeParent.getNodeName();
      if (null == strPara)
         return;

      strPara = strPara.toLowerCase();
      String strTmp = "";
      Node nodeTmp = null;
      if (strPara.equals("span"))
      {
         nodeTmp = nodeParent.getLastChild();
         while (Node.ELEMENT_NODE != nodeTmp.getNodeType())
            nodeTmp = nodeTmp.getPreviousSibling ();

         if (null != nodeTmp)
            strTmp = nodeTmp.getNodeName().toLowerCase();
      }
      elemTmp = (Element) elemTmp1.appendChild(htmlNode);
      elemTmp.setAttribute(ms_strContextNodeName, strContext);

      modifyAttributes(elemTmp);

      if (strTmp.equals("br"))
         elemTmp1.appendChild (nodeTmp);

      return;
  }

   /**
    * Get the next valid sibling. If none is found, null is returned.
    *
    * @param node the node we are looking for the next sibling for.
    * @return the sibling or null if not found.
    */
   private Node getNextValidSibling(Node node)
   {
      Node sib = node.getNextSibling();
      String value = sib.getNodeValue();

      while (sib != null &&
             Node.TEXT_NODE == sib.getNodeType() &&
             value.trim().equals(""))
      {
         sib = sib.getNextSibling();
         if (sib != null)
            value = sib.getNodeValue();
      }

      if (sib != null)
      {
         if (Node.TEXT_NODE == sib.getNodeType())
            return sib;

         if (Node.ELEMENT_NODE == sib.getNodeType() &&
             sib.getNodeName().toLowerCase().equals("span"))
            return sib;
      }

      return null;
   }

   /**
    * Modify the attrubutes for the provided node.
    *
    * @param htmlNode an input node to be transformed
    * @return the modified or original node.
    * @throws DOMException for every DOM function failed.
    */
   private Node modifyAttributes(Node htmlNode) throws DOMException
   {
      if (Node.ELEMENT_NODE != htmlNode.getNodeType())
         return htmlNode;

      String strNodeName = htmlNode.getNodeName();

      Attr nodeAttr = null;
      Attr nodePsxAttr = null;
      Element nodeElem = null;
      String strAttrName = null;
      String strAttrValue = null;
      String strContext = null;
      NamedNodeMap xmlAttrib = htmlNode.getAttributes();
      for (int i=0; i<xmlAttrib.getLength(); i++)
      {
         nodeAttr = (Attr) xmlAttrib.item(i);
         if (null == nodeAttr)
            continue;

         strAttrName = nodeAttr.getName().trim();
         strAttrValue = nodeAttr.getValue().trim();
         if (strAttrName.equals(ms_strContextNodeName))
            continue;

         if (strAttrName.startsWith(m_strPsxTag))
         {
            strAttrName = context(strAttrName);
            if (strAttrValue.startsWith(m_strPsxTag))
               strAttrValue = context(strAttrValue);
            strContext = strAttrValue;

            // empty values to remove later
            nodeAttr.setValue("");
            Attr tmp = (Attr) xmlAttrib.getNamedItem(strAttrName);
            if (null != tmp)
               tmp.setValue("");

            nodeElem = m_xmlDoc.createElement("xsl:attribute");
            nodeElem.setAttribute("name", strAttrName);
            nodeElem.setAttribute(ms_strContextNodeName, strContext);

            htmlNode.insertBefore(nodeElem, htmlNode.getFirstChild());

            Element nodeElemChild = m_xmlDoc.createElement("xsl:value-of");
            nodeElemChild.setAttribute("select", strContext);
            nodeElemChild.setAttribute(ms_strContextNodeName, strContext);

            nodeElem.appendChild(nodeElemChild);
         }

         if (!strAttrValue.startsWith(m_strPsxTag))
            continue;

         strContext = context(strAttrValue);

         ((Attr) (xmlAttrib.getNamedItem(strAttrName))).setValue("");

         nodeElem = m_xmlDoc.createElement("xsl:attribute");
         nodeElem.setAttribute("name", nodeAttr.getName());
         nodeElem.setAttribute(ms_strContextNodeName, strContext);

         htmlNode.insertBefore(nodeElem, htmlNode.getFirstChild());

         Element nodeElemChild = m_xmlDoc.createElement("xsl:value-of");
         nodeElemChild.setAttribute("select", strContext);
         nodeElemChild.setAttribute(ms_strContextNodeName, strContext);

         nodeElem.appendChild(nodeElemChild);
      }

      // remove all empty attributes skipping the appropriate ones
      xmlAttrib = htmlNode.getAttributes();
      for (int i=0; i<xmlAttrib.getLength(); i++)
      {
         nodeAttr = (Attr) xmlAttrib.item(i);
         if (null == nodeAttr || nodeAttr.getName().trim().equals(""))
            continue;

         if (!ms_skipEmptyAttrRemoval.contains(nodeAttr.getName())
            && nodeAttr.getValue().trim().equals(""))
         {
            xmlAttrib.removeNamedItem(nodeAttr.getName().trim());

            // we removed an element, so update the counter!
            i--;
         }
      }

      return htmlNode;
   }

   /**
    * Get the value of the VALUE attribute for the provided node.
    *
    * @param xmlAttrib attribute map to look for the VALUE
    * @return the value of the VALUE attribute or null if not found.
    */
   private String getValueAttribute(NamedNodeMap xmlAttrib)
   {
      String id = null;
      Attr nodeAttr = null;
      for (int i=0; i<xmlAttrib.getLength(); i++)
      {
         nodeAttr = (Attr) xmlAttrib.item(i);
         if (null == nodeAttr)
            continue;

         id = nodeAttr.getName().trim();
         if (id.equals("value"))
            return nodeAttr.getValue().trim();
      }

      return null;
   }

   /**
    * Modify the node context for all element nodes which do have children.
    *
    * @param htmlNode the node to modify
    * @return the modified node
    * @throws DOMException for every DOM function failed.
    */
   private Node modifyNodeContext(Node htmlNode) throws DOMException
   {
      // just return the original node if this is not an element node
      if (Node.ELEMENT_NODE != htmlNode.getNodeType())
         return htmlNode;

      // nothing to do if there are no children
      if (!htmlNode.hasChildNodes())
         return htmlNode;

      // loop through all children and for each element node found do...
      String strInitialContext = "";
      String strContext = "";
      String strTemp = "";
      String strFirstExternalContext = null;
      boolean bContextInitialized = false;
      Element htmlElem = null;
      NodeList nodeList = htmlNode.getChildNodes();
      for (int i=0; i<nodeList.getLength(); i++)
      {
         if (Node.ELEMENT_NODE != nodeList.item(i).getNodeType())
            continue;

         htmlElem = (Element) nodeList.item(i);
         strTemp = htmlElem.getAttribute(ms_strContextNodeName);
         if (strTemp.equals("") || strTemp.startsWith("/"))
            continue;

         if (!bContextInitialized)
         {
            strContext = strTemp;
            strInitialContext = strTemp;
            if (!isMainContext(strContext))
               strFirstExternalContext = strContext;
            bContextInitialized = true;

            continue;
         }

         /*
         We have to find the common context for these leaves. If both are from
         the main context, the current algorithm works fine. If they are from
         different contexts, we have to decide which path we are going. By
         default we will go the path of the main context. If no main context
         exists, we will go the path of the first found external context.
         The main context refers to the psx-foo marked up fields, while
         external contexts refer to external documents marked up as psx-/bar.
         */
         if (isMainContext(strTemp))
         {
            if (isMainContext(strContext))
               strContext = getCommonContext(strTemp, strContext);
            else
               strContext = strTemp;
         }
         else if (isSameExternalContext(strTemp, strFirstExternalContext))
         {
            if (isSameExternalContext(strContext, strFirstExternalContext))
               strContext = getCommonContext(strTemp, strContext);
         }
      }

      Element elem = (Element) htmlNode;
      removeAttribute(elem, ms_strContextNodeName);
      if (!strContext.equals("") && !strContext.equals("."))
      {
         String root = strContext;
         int index = strContext.indexOf('/');
         if (index != -1)
            root = strContext.substring(0, index);

         m_roots.put(root, root);

         elem.setAttribute(ms_strContextNodeName, correctContext(strContext));
      }

      return htmlNode;
   }

   /**
    * Get the common context out of the two provided context strings.
    *
    * @param strContextA the first context string, may be empty not
    *           <code>null</code>
    * @param strContextB the second context string, may be empty not
    *           <code>null</code>
    * @return the common context found, may be an empty string if no common
    *         context was found. throws IllegalArgumentException for any invalid
    *         parameter passed.
    */
   private static String getCommonContext(String strContextA, String strContextB)
   {
      String baseElement = PSXmlTreeWalker.getBaseElement(strContextA,
            strContextB);
      return baseElement == null ? "" : baseElement;
   }

   /**
    * This tests whether or not the provided context string is from the main
    * context. The hashtable m_contexts must be initialized before calling this
    * method.
    *
    * @param strContext context string to test, may be empty, not
    *           <code>null</code>
    * @return <code>true</code> if the provided string is from the main
    *         context, <code>false</code> otherwise. throws
    *         IllegalArgumentException for any invalid parameter passed.
    */
   private boolean isMainContext(String strContext)
   {
      if (strContext == null)
         throw new IllegalArgumentException("this needs a valid context string");

      int loc = strContext.indexOf('/');
      if (loc != -1)
         strContext = strContext.substring(0, loc);

      if (m_contexts.containsKey(strContext))
         return false;

      return true;
   }

   /**
    * This tests whether or not the provided context strings have the same
    * external base.
    *
    * @param strContextA context string to test, may be empty, not
    *           <code>null</code>
    * @param strContextB context string to test against, may be empty or
    *           <code>null</code>
    * @return <code>true</code> if the provided string is from the main
    *         context, <code>false</code> otherwise. throws
    *         IllegalArgumentException for any invalid parameter passed.
    */
   private boolean isSameExternalContext(String strContextA,
                                         String strContextB)
   {
      if (strContextA == null)
         throw new IllegalArgumentException("this needs a valid context string");

      if (strContextB == null)
         return false;

      if (isMainContext(strContextA))
         return false;

      return getCommonContext(strContextA, strContextB).length() == 0;
   }

   /**
    * Correct the provided context and return it. This function retrieves the
    * second element in the provided context. If it is found in our context
    * map it is corrected (the first element removed).
    *
    * @param strContext the context to check for correction
    * @return the correct context.
    */
   private String correctContext(String strContext)
   {
      int first = strContext.indexOf('/');
      if (first == -1)
         return strContext;

      String context = "";
      int second = strContext.indexOf('/', first+1);
      if (second == -1)
         context = strContext.substring(first+1, strContext.length());
      else
         context = strContext.substring(first+1, second);

      if (m_contexts.containsKey(context))
         strContext = strContext.substring(first+1, strContext.length());

      return strContext;
   }

   /**
    * Correct the context for the provided node and return the first common
    * base.
    *
    * @param nodeInput the node to correct the context on.
    * @return the first common base.
    */
   private String correctContextOnNode(Node nodeInput)
   {
      if (Node.ELEMENT_NODE != nodeInput.getNodeType())
         return "";

      Element elemInput = (Element) nodeInput;
      String strAttr = "select";
      if (nodeInput.getNodeName().equals("xsl:template"))
         strAttr = "match";

      String strContext = elemInput.getAttribute(ms_strContextNodeName);
      String strBase = getFirstCommonBaseForChildren(elemInput);
      if (strBase.equals(""))
         return "";

      nodeInput = removeBaseContextFromAllChildren(nodeInput, strBase);
      elemInput.setAttribute(strAttr, strBase);
      elemInput.setAttribute(ms_strContextNodeName, strBase);

      NodeList nodeList =  nodeInput.getChildNodes();
      Node nodeTemp = null;
      for(int i=0; i<nodeList.getLength(); i++)
      {
         nodeTemp = nodeList.item (i);
         if (Node.ELEMENT_NODE != nodeTemp.getNodeType())
            continue;

         if (nodeTemp.getNodeName().equals("xsl:for-each"))
         {
            ((Element) nodeTemp).setAttribute("select", ".");
            ((Element) nodeTemp).setAttribute(ms_strContextNodeName , ".");
         }

         loopForApplyTemplates(nodeTemp);
      }

      return strBase;
   }

   /**
    * This method returns the common base context for the provided node and
    * all its children.
    *
    * @param node the node we want the common base for.
    * @return the common base or an empty string.
    */
   private String getFirstCommonBaseForChildren(Node node)
   {
      if (Node.ELEMENT_NODE != node.getNodeType())
         return "";

      NodeList nodeList;
      Element elem = null;
      String strContext = "";
      String strTemp = "";
      boolean bContextInitialized = false;

      if (!node.hasChildNodes())
         return "";

      nodeList = node.getChildNodes();
      for (int i=0; i<nodeList.getLength(); i++)
      {
         if (Node.ELEMENT_NODE != nodeList.item(i).getNodeType())
            continue;

         elem = (Element) nodeList.item(i);
         strTemp = elem.getAttribute(ms_strContextNodeName);
         if (strTemp.equals("") || strTemp.startsWith("/"))
            continue;

         if (!bContextInitialized)
         {
            strContext = strTemp;
            bContextInitialized = true;
            continue;
         }

         int nLoc = -1;
         while(!strTemp.startsWith(strContext) && !strContext.equals(""))
         {
            nLoc = strContext.lastIndexOf('/');
            if(-1 != nLoc)
               strContext = strContext.substring(0, nLoc);
            else
               strContext = "";
         }
      }

      if (strContext.equals(""))
      {
         if (bContextInitialized)
            return ".";
         else
            return strContext;
      }

      return getFirstInNodeContext(strContext);
   }

   /**
    * Remove the base context from all children of the provided node.
    *
    * @param node the node to remove the base context from its children.
    * @param strBase the base context to remove.
    * @return the node with the base context removed.
    */
   private Node removeBaseContextFromAllChildren(Node node, String strBase)
   {
      if (!node.hasChildNodes())
         return node;

      NodeList nodeList = node.getChildNodes();
      Node nodeTemp = null;
      Element elemTemp = null;
      String strAttr = "";
      for (int i=0; i<nodeList.getLength(); i++)
      {
         nodeTemp = nodeList.item(i);
         if (Node.ELEMENT_NODE != nodeTemp.getNodeType())
            continue;

         if (nodeTemp.getNodeName().equals("xsl:apply-templates"))
         {
            Element elemMatch = getMatchTemplateElement((Element) nodeTemp);
            if (null != elemMatch)
            {
               strAttr = elemMatch.getAttribute("match");
               if (!strAttr.equals(""))
                  elemMatch.setAttribute("match",
                     getContextMinusBase(elemMatch, strAttr, strBase));

               strAttr = elemMatch.getAttribute(ms_strContextNodeName);
               if (!strAttr.equals(""))
                  elemMatch.setAttribute(ms_strContextNodeName,
                     getContextMinusBase(elemMatch, strAttr, strBase));

               removeBaseContextFromAllChildren(elemMatch, strBase);
            }
         }

         nodeTemp = removeBaseContextFromAllChildren(nodeTemp, strBase);
         elemTemp = (Element) nodeTemp;

         strAttr = elemTemp.getAttribute("select");
         if (!strAttr.equals(""))
            elemTemp.setAttribute("select",
               getContextMinusBase(elemTemp, strAttr, strBase));

         strAttr = elemTemp.getAttribute(ms_strContextNodeName);
         if (!strAttr.equals(""))
            elemTemp.setAttribute(ms_strContextNodeName,
               getContextMinusBase(elemTemp, strAttr, strBase));
      }

      return node;
   }

   /**
    * This is to add a dummy attribute if an element has no value and no
    * attributes. Netscape Navigator does not understand &lt;br/&gt;, 
    * &lt;hr/&gt; , etc. In such cases, we add a unique dummy attribute such 
    * that we see something like &lt;br id="XSpLit2"/&gt;. This works with 
    * both IE and Navigator.
    * By default this is turned off. To turn it on you must add a property
    * named <code>pleaseNavigator</code> with a value of <code>yes</code> 
    * case insensitive to the <code>xsplit.properties</code> file.
    *
    * @param xmlDoc the document we want to add the dummy attributes.
    * @param strDummy the dummy string (attribute value). A counter value is
    *    incremented for each node processed and appended to this string for 
    *    use as the attribute value.
    */
   private static void pleaseNavigator(Document xmlDoc, String strDummy)
   {
      String pleaseNavigator = Splitter.getProperty("pleaseNavigator");
      if (pleaseNavigator != null && 
         pleaseNavigator.trim().equalsIgnoreCase("yes"))
      {
         NodeList nodeList = xmlDoc.getElementsByTagName("*");
         Element elem = null;
         NamedNodeMap xmlAttrib = null;
   
         for (int i=0; i<nodeList.getLength(); i++)
         {
            elem = (Element) nodeList.item(i);
            xmlAttrib = elem.getAttributes();
            if (0 == xmlAttrib.getLength() && !elem.hasChildNodes())
               elem.setAttribute("id", strDummy + i);
         }
      }
   }

   static Document createXMLDocument(String strBaseNodeName)
         throws IOException, SAXException
   {
      return createXMLDocument( strBaseNodeName, false );
   }


   static Document createXMLDocument(String strBaseNodeName, boolean standalone)
         throws IOException, SAXException
   {
      StringWriter basicXml = new StringWriter( 100 );
      writeXmlHeader( basicXml, standalone, PSCharSets.getInternalEncoding());
      basicXml.write( "<" + strBaseNodeName + "/>" );

      InputSource src = new InputSource( new StringReader( basicXml.toString()));
      DocumentBuilder db = PSXmlDocumentBuilder.getDocumentBuilder(false);
      return db.parse(src);
  }

   /*
    * These are the tiny helper functions that simplify our lives in parsing the dcoument
    */

   /**
    * Returns a string of all direct text node children of the supplied node.
    * The text is appended in the order the text nodes are found.
    *
    * @param node the node to get the element value from, migth be
    *    <code>null</code>.
    * @return the element value as String, never <code>null</code>, might be
    *    empty.
    */
   public static String getElementTextValue(Node node)
   {
      String sRes = "";
      if (node == null)
         return sRes;

      Node textNode;
      for (textNode=node.getFirstChild(); textNode!=null;
         textNode=textNode.getNextSibling())
      {
         if (Node.TEXT_NODE != textNode.getNodeType())
            continue;

         sRes += textNode.getNodeValue();
      }

      return sRes.trim();
   }

   /**
    * Get all contents contained in the provided node as String.
    *
    * @param node the node to get the content string from, might be
    *    <code>null</code>.
    * @return the contents of the provied node as String, never
    *    <code>null</code>, might be empty.
    * @throws IOException for any failed IO operation.
    */
   public static String getElementValue(Node node) throws IOException
   {
      if (node == null)
         return "";

      StringBuffer value = new StringBuffer();
      NodeList children = node.getChildNodes();
      for (int i=0; i<children.getLength(); i++)
      {
         Node child = children.item(i);
         if (child.getNodeType() == Node.TEXT_NODE)
            value.append(child.getNodeValue());
         else
         {
            StringWriter wr = new StringWriter();
            printNode(child, TAB, wr);
            wr.flush();
            wr.close();
            value.append(wr.getBuffer().toString());
         }
      }

      return value.toString();
   }

   /**
    * Prints the supplied node to the supplied writer. Empty elements are written
    * using the empty element tag format. See the method by the same name that
    * takes more parameters for a more detailed description.
    * @param node
    * @param indent
    * @param out
    * @throws IOException
   **/
   public static void printNode(Node node, String indent, Writer out)
                                throws IOException
   {
      printNode(node, indent, out, true);
   }

   /**
    * Writes the supplied node and all its children as a nicely formatted XML
    * document to the supplied writer.
    *
    * @param node The node to print. To print a whole document, pass in the
    * document. If null, nothing is done.
    *
    * @param indent A String containing white space. Comments and elements
    * are prepended with this string when they are being written. Children are
    * automatically indented from their parents. <p/>
    * When passing in a document, this should typically be the empty string.
    * This may be null to mean the empty string.
    *
    * @param out The target for the output. If null, nothing is done.
    *
    * @param useEmptyElementTag If <code>true</code>, when an element has no
    * data, the empty element tag form is used (example: <foo/>. If <code>false
    * </code>, a start and end tag is used, even when no content is present
    * (example: <foo></foo>). This flag has no effect if an element has content.
    *
    * @throws IOException If a failure occurs while navigating the node.
   **/
   public static void printNode(Node node, String indent, Writer out,
                                boolean useEmptyElementTag) throws IOException
   {
      if (node == null || null == out )
         return;

      if (null == indent)
         indent = "";

      String newline = "";
      String tab = "";

      if (ms_prettyPrintOn)
      {
         newline = NEWLINE;
         tab = TAB;
      }
      else
      {
         indent = "";
      }
      switch (node.getNodeType())
      {
         case Node.ATTRIBUTE_NODE:
            out.write(" " + ((Attr) node).getName() + "=\"" +
               ((Attr) node).getValue() + "\"");
            break;

         case Node.CDATA_SECTION_NODE:
            out.write(newline + indent + "<![CDATA[" +
                      ((CDATASection) node).getData() + "]]>");
            break;

         case Node.COMMENT_NODE:
            out.write(newline + indent + "<!--" + ((Comment)node).getData() + "-->" + newline);
            break;

         case Node.DOCUMENT_NODE:
            Document dNode = (Document) node;

            /*
            go through the doc's children, which should be the PI nodes,
            DTD nodes and then the root data node, do not indent this nodes
            */
            for (Node kid = dNode.getFirstChild(); kid != null; kid = kid.getNextSibling())
               printNode(kid, "", out, useEmptyElementTag);
            break;

         case Node.ELEMENT_NODE:
            Element eNode = (Element) node;
            String strName = eNode.getTagName();
            out.write(indent + "<" + eNode.getTagName());

            // print any attributes in the tag
            NamedNodeMap attrList = eNode.getAttributes();
            if (attrList != null)
            {
               Attr aNode;
               for (int i = 0; i < attrList.getLength(); i++)
               {
                  aNode = (Attr) attrList.item(i);
                  out.write(" " + aNode.getName() + "=\"" +
                     convertToXmlEntities(aNode.getValue()) + "\"");
               }
            }

            /*
            if there are children, close the tag, print the kids and print
            the end tag
            */
            if (eNode.hasChildNodes())
            {
               /* close the start tag */
               out.write(">");

               /* print the kids */
               Node kid = null;
               Text text = null;
               String str = "";
               boolean bElementPresent = false, bTextPresent = false;
               for (kid = eNode.getFirstChild(); kid != null; kid = kid.getNextSibling())
               {
                  if (kid instanceof Element)
                  {
                     bElementPresent = true;
                     out.write(newline);
                  }
                  if (kid instanceof Text)
                  {
                     text = (Text) kid;
                     str = text.getNodeValue();
                     if (!(str.trim().equals("")))
                        bTextPresent = true;
                  }
                  printNode(kid, indent + TAB, out, useEmptyElementTag);
               }

               // and the end tag
               if (bElementPresent)
                  out.write(newline + indent);

               out.write("</" + eNode.getTagName() + ">");
               if (!bTextPresent)
                  out.write(newline);
            }
            else
            {
               // close this tag with the end tag
               if (useEmptyElementTag)
                  out.write(" />");
               else
                  out.write("></" + eNode.getTagName() + ">");
            }
            break;

         case Node.PROCESSING_INSTRUCTION_NODE:
            ProcessingInstruction pi = (ProcessingInstruction)node;
            out.write("<?" + pi.getTarget() + " " + pi.getData() + "?>" + newline);
            break;

         case Node.TEXT_NODE:
            String str = node.getNodeValue();
            // Need to trim text if pretty print on.
            if (ms_prettyPrintOn)
               str = str.trim();
            if (!str.equals(""))
               out.write(convertToXmlEntities(str));
            break;

         case Node.ENTITY_REFERENCE_NODE:
            EntityReference er = (EntityReference) node;
            out.write("&" + er.getNodeName() + ";");
            break;

         case Node.DOCUMENT_TYPE_NODE:
            DocumentType dt = (DocumentType) node;
            /*
            Since we are not supporting embedded DOCTYPE's currently, we can
            just add the default entity defs that were added to the HTML doc
            before processing. This will have to be addressed later. The
            commented code below writes out all entities in the document.
            */
            out.write("<!DOCTYPE " + dt.getName() + " [" + NEWLINE +
                      getDefaultEntities(null) + NEWLINE + "]>" +
                      NEWLINE + NEWLINE);
            break;
      }

      return;
   }

   /**
    * Load XML document from stream reader.
    * @param rdr
    * @return
    * @throws IOException
    * @throws SAXException
   **/
   public static Document getXMLDocFromInputStream( Reader rdr )
         throws IOException, SAXException
   {
      InputSource src = new InputSource(rdr);
      DocumentBuilder db = PSXmlDocumentBuilder.getDocumentBuilder(false);
      return db.parse(src);
   }

   private static String convertToXmlEntities(String input)
   {
      /* This implementation should be fairly efficient in that
       * it minimizes the number of function calls. Hence, we
       * operate on an array rather than a String object. We
       * collect the output in a string buffer, and here too
       * we try to minimize the number of function calls.
       *
       * We do not add each normal character to the output
       * buffer one at a time. Instead, we build a "run" of
       * normal characters and, upon encountering a special
       * character, we write the previous normal run before
       * we write the special replacement.
       */
      char[] chars = input.toCharArray();
      int len = chars.length;

      StringBuffer buf = new StringBuffer((int)(chars.length * 1.5));

      char c;

      // the start of the latest run of normal characters
      int startNormal = 0;

      int i = 0;
      while (true)
      {
         if (i == len)
         {
            if (startNormal != i)
               buf.append(chars, startNormal, i - startNormal);
            break;
         }
         c = chars[i];
         switch (c)
         {
            case '&' :
               if (startNormal != i)
                  buf.append(chars, startNormal, i - startNormal);
               startNormal = i + 1;
               buf.append("&amp;");
               break;
            case '<' :
               if (startNormal != i)
                  buf.append(chars, startNormal, i - startNormal);
               startNormal = i + 1;
               buf.append("&lt;");
               break;
            case '>' :
               if (startNormal != i)
                  buf.append(chars, startNormal, i - startNormal);
               startNormal = i + 1;
               buf.append("&gt;");
               break;
            case '\'' :
               if (startNormal != i)
                  buf.append(chars, startNormal, i - startNormal);
               startNormal = i + 1;
               buf.append("&apos;");
               break;
            case '"' :
               if (startNormal != i)
                  buf.append(chars, startNormal, i - startNormal);
               startNormal = i + 1;
               buf.append("&quot;");
               break;
            default:
               int code = (int)c;
               if(code > 126)
               {
                  if (startNormal != i)
                     buf.append(chars, startNormal, i - startNormal);
                  startNormal = i + 1;
                  buf.append("&#" + Integer.toString(code) + ";");
                  break;
               }

         }
         i++;
      }
      return buf.toString();
   }

   /**
    * This is the access function to get the XML String of the XSL Document.
    * @return
    */
   public Document getXMLDocument()
   {
      return m_xmlDoc;
   }

   /**
    * Creates a unique mode string for the current session.
    *
    * @return a unique mode string, such as mode0, mode1, ...
    */
   protected synchronized String makeMode()
   {
      String name = getGlobalTemplateName(m_xmlDoc);
      // if name is empty then this is a local template
      String prefix = name.length() > 0 ? "G" : "L";
      return (prefix + "mode" + String.valueOf(m_nMode++));
   }

   /**
    * Get the default entities for the provided server root. An entry like
    * <!ENTITY % HTMLlat1 SYSTEM "<serverRoot>/DTD/HTMLlat1x.ent">%HTMLlat1;
    * will be created for all entities.
    * Include all of the entities defined for HTML, the originals are
    * available at:
    * "http://www.w3.org/TR/xhtml1/DTD/HTMLlat1x.ent"
    * "http://www.w3.org/TR/xhtml1/DTD/HTMLsymbolx.ent"
    * "http://www.w3.org/TR/xhtml1/DTD/HTMLspecialx.ent"
    * @param serverRoot
    * @return
    */
   public static String getDefaultEntities(String serverRoot)
   {
      String root = serverRoot;
      if (root == null || root.equals(""))
         root = ms_defaultServerRoot;

      return "\t<!ENTITY % HTMLlat1 SYSTEM \"/" + root + "/DTD/HTMLlat1x.ent\">" + NEWLINE + "\t\t%HTMLlat1;" + NEWLINE +
             "\t<!ENTITY % HTMLsymbol SYSTEM \"/" + root + "/DTD/HTMLsymbolx.ent\">" + NEWLINE + "\t\t%HTMLsymbol;" + NEWLINE +
             "\t<!ENTITY % HTMLspecial SYSTEM \"/" + root + "/DTD/HTMLspecialx.ent\">" + NEWLINE + "\t\t%HTMLspecial;";
   }

   /**
    * Returns true if one context starts with another context.
    * This method is similar to the String objects startWith
    * method, except that it considers the "/" delimiter.
    * We split each context into elements and check each
    * contextB element against the same element in contextA
    * for equality. If we find no unequal elements then we
    * return true.
    *
    * @param contextA the context string to parse.
    *  May be empty or <code>null</code>.
    * @param contextB the context string that we are looking for
    *  May be empty or <code>null</code>.
    * @return
    * @returns <code>true</code> if contextA starts with contextB
    */
   private static boolean contextStartsWith(String contextA, String contextB)
   {
      // If either context is null then the
      // result must be false
      if(contextA == null || contextB == null)
         return false;

      // Split the two contexts into elements
      List conA = Util.splitString(contextA,'/');
      List conB = Util.splitString(contextB,'/');

      // If contextA is smaller than contextB
      // the result must be false
      if(conA.size() < conB.size())
         return false;

      // Compare each element in contextB with its positional counterpart
      // in contextA for equality
      for(int i=0; i<conB.size(); i++)
      {
         if(!((String)conB.get(i)).equals((String)conA.get(i)))
            return false;

      }
      return true;
   }

   /**
    * The delimiter used to separate XML elements in an XPath expression.
    */
   public static final String XML_ELEMENT_DELIMITER = "/";

   /** The default server root */
   public static final String ms_defaultServerRoot = "Rhythmyx";

   /** New line string */
   public static final String NEWLINE = "\r\n";

   /** Tab string */
   public static final String TAB = "  ";

   /**
    * Name of the attribute to indicate an HTML source is using  a global template.
    */
   public static final String ATTR_PSXGLOBALTEMPLATE = "psxglobaltemplate";

   /**
    * Name of the attribute to indicate an HTML source is for a global template.
    */
   public static final String ATTR_PSXGLOBALTEMPLATE_NAME = "psxglobaltemplatename";


   /** counter used to enumerate unknown external document references */
   private int m_unknownDocRef = 0;

   protected int m_nMode = 0;
   protected boolean m_bPrintToFile = true;
   protected static final String ms_strContextNodeName = "zContext";
   protected static final String ms_strRootContext = "zRootContext";
   protected static final String ms_strRepeatContext = "zRepeat";
   protected static final String ms_strRepeatFlag = "RepeatFlag";
   protected static String m_strPsxTag =
      SplitterConfiguration.getDefaultProperty("dynamicTag");


   /** The xsplit root name */
   protected static final String XSPLIT_ROOT_NAME = "xsplit_root";

   /** The body content template name */
   protected static final String XSPLIT_BODY_TEMPLATE = "xsplit_body";

   /** The XSL marker indicating the server tag place holder cotains XSL */
   protected static final String ms_XslServerTagPlaceHolder = "XSL_XSpLit_";

   protected String m_strHTMLFile = "";
   protected String m_strDummAttrib = "XSpLit";
   protected String m_strBaseContext = "root";

   protected Document m_xmlDoc = null;
   protected static final String ms_strXSLNS = "http://www.w3.org/1999/XSL/Transform";
   protected static final String ms_XSLNS_SAXON = "http://icl.com/saxon";

   /**
    * The name space URN used for Rhythmyx I18N support.
    */
   protected static final String ms_RX_I18N = "urn:www.percussion.com/i18n";

   // The Java name of the character encoding used to write the document.
   private String m_charEncoding = null;

   /** The context definition alias tag */
   public static final String ms_docAlias = "docalias";
   /** The context definition references tag */
   public static final String ms_docRef = "docref";
   /**
    * A hashtable of all context information found, containing the alias
    * (key) and the reference (value).
    */
   private Hashtable m_contexts = new Hashtable();
   /**
    * A map of document roots found while processing the document. Key and
    * value are the same.
    */
   private HashMap m_roots = new HashMap();

   /**
    * Flag to indicate if the provided source document contained slot
    * mark-ups. This must be reset before each transformation. After the
    * transformation a value of <code>true</code> means the source had slots
    * marked-up.
    */
   private static boolean m_hasSlots = false;

   /**
    * Flag to indicate if the provided source document contained property
    * mark-ups. This must be reset before each transformation. After the
    * transformation a value of <code>true</code> means the source had
    * properties marked-up.
    */
   private static boolean m_hasProperties = false;

   /**
    * The assembler transformator. Recreated for each transformation.
    */
   private static AssemblerTransformation m_casTransformator = null;

   /**
    * Flag to indicate that pretty printing is turned on
    */
   private static boolean ms_prettyPrintOn = false;

   /**
    * Flag indicating that body content should have a separate
    * template.
    */
   private static boolean ms_bodyTemplateOn = false;

   /**
    * Set of names of all attribute that should NOT be removed as part of
    * cleanup when their value is empty. For example, empty "alt" attribute
    * of an &lt;image&gt; tag should NOT be removed for accessiblity support
    * reasons. Should there be any such attributes found in future, those need
    * to be added to this set in the static initializer. Never
    * <code>null</code> or empty.
    *
    * @see #modifyAttributes(Node) to see how this set is used.
    */
   private static Set ms_skipEmptyAttrRemoval = new HashSet();
   static
   {
      ms_skipEmptyAttrRemoval.add("alt");
   }

   /**
    *
    * Test method
    *
    * @param args none required.
    */
   public static void main(String[] args)
   {
      String res1 = HTML2XSL.getCommonContext("root/product/name",
            "root/product");
      System.out.println("res1=" + res1);
      String res2 = HTML2XSL.getCommonContext("root/product",
            "root/product/name");
      System.out.println("res2=" + res2);
      String res3 = HTML2XSL.getCommonContext("root/product/foo/",
            "root/product/bar");
      System.out.println("res3=" + res3);
      String res4 = HTML2XSL.getCommonContext("root1/product/foo/",
            "root/product/bar");
      System.out.println("res4=" + res4);
   }
}

