/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.percussion.htmlConverter;

import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Attr;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

/**
 * The HTML2DTD class is used to transform a specially created HTML document to
 * XML DTD
 *
 * @author     Rammohan Vangapalli
 * @version    1.0
 * @since      1.0
 */
public class HTML2DTD
{
   /**
    * Default constructor.
    */
   public HTML2DTD()
   {
   }

   /**
    * Constructer that takes the HTML Source file.
    * Supported properties are:
    * <table border="1">
    * <tr><th>Key</th><th>Value</th></tr>
    * <tr><td>Input HTML File Path</td><td>input HTML file for transformation</td>
    * </tr>
    * </table>
    *
    * @param   strHTMLFile String
    */
   public HTML2DTD (String strHTMLFile )throws SAXException, DOMException, IOException
   {
      SetHTMLFile( strHTMLFile );
   }

   /**
    * Constructer that takes the HTML Source file and the psx-tag.
    * Supported properties are:
    * <table border="1">
    * <tr><th>Key</th><th>Value</th></tr>
    * <tr><td>Input HTML File Path</td><td>input HTML file for transformation</td>
    * <tr><td>psx-tag</td><td>new psx-tag string</td>
    * </tr>
    * </table>
    *
    * @param   strHTMLFile String
    * @param   strPsxTag String
    */
   public HTML2DTD (String strHTMLFile, String strPsxTag )
   throws SAXException, DOMException, IOException
   {
      m_strPsxTag = strPsxTag;
      SetHTMLFile(strHTMLFile);
   }

   /**
    * Constructer that takes the InputStream.
    * Supported properties are:
    * <table border="1">
    * <tr><th>Key</th><th>Value</th></tr>
    * <tr><td>InputStream</td><td>typically an output from some other program e.g. HTML Tidy</td>
    * </tr>
    * </table>
    *
    * @param   is InputStream
    */
   public HTML2DTD ( Reader rdr )throws SAXException, IOException
   {
      this(HTML2XSL.getXMLDocFromInputStream(rdr));
   }

   /**
    * Constructer that takes the input as XML Document.
    * Supported properties are:
    * <table border="1">
    * <tr><th>Key</th><th>Value</th></tr>
    * <tr><td>Document</td><td>typically a parsed XML Docuemnt probably called from HTML2XSL</td>
    * </tr>
    * </table>
    *
    * @param   is Document
    */
   public HTML2DTD (Document xmlDoc )
   {
      m_xmlDoc = xmlDoc;
      m_ht = new Hashtable();
      m_strPsxArray = new Vector();
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
    * @param   strPsxTag   String
    *
    */
   public void SetPsxTag(String strPsxTag)
   {
      m_strPsxTag = strPsxTag;
   }

   /**
    * Get the global option String psx-tag.
    *
    * @return     the psx-tag as String
    */
   public String GetPsxTag()
   {
      return m_strPsxTag;
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
    * @param   strPsxTag   String
    */
   public void SetHTMLFile(String strHTMLFile ) throws SAXException, DOMException, IOException
   {
      m_strHTMLFile = strHTMLFile;
      m_ht = new Hashtable();
      m_strPsxArray = new Vector();
      DocumentBuilder db = Util.getDocumentBuilder();
      m_xmlDoc = db.parse(m_strHTMLFile);
   }

   /**
    * Get the HTML Source file set for transformation.
    *
    * @return     the HTML Source file as String
    */
   public String GetHTMLFile(String strHTMLFile)
   {
      return m_strHTMLFile;
   }

   /**
    * A short form of its 2 parametered brethren, defaults to showing sample
    * data and using the start/end tag form for the XML elements.
    *
    * @throws SplitterException for all IO and SAX parser exceptions.
    */
   public String getSampleXML() throws SplitterException
   {
      return getSampleXML(true, true);
   }

   /**
    * Extracts a sample XML file from the document associated with this object.
    * Several different forms can be obtained based on the values of the supplied
    * flags. Whatever encoding type is passed in should be used when storing the
    * document, as the standard name for this Java type is used in the Encoding
    * decl within the XML decl for the created document. <p/>
    * The idea of showing the end tag when an element is empty is to make it easy
    * for a user to add a value. <p/>
    * When a psx- identifier is placed in an HTML file, it can be added using
    * exposed or hidden syntax. Exposed syntax requires less typing and allows
    * the identifier to be easily seen, but it doesn't allow for accurate previewing
    * of the document. Hidden syntax places the identifier as an attribute of an
    * element, thus hiding it in the typical editor. The content of the tag containing
    * the identifier can be anything and will be discarded when the document is
    * processed by the splitter. This allows expected values to be placed in the
    * document for accurate previewing. <p/>
    *
    * Example exposed syntax: <p/>
    * <title>psx-document/title/text</title> <p/><p/>
    *
    * Example hidden syntax: <p/>
    * <title id=psx-document/title/text>Typical Title</title> <p/><p/>
    *
    * The text "Typical Title" would be considered the sample content for the
    * "document/title/text" element.
    *
    * @param showSampleData If <code>true</code>, any sample data associated
    * with an identifief using hidden syntax will be placed as the value for that
    * elements content.
    *
    * @param useEmptyElementTag If <code>true</code>, the returned XML document is a
    * well formed document with empty elements being represented by the empty element
    * tag. Otherwise, empty elements will be represented by both a start and end
    * tag (with no content).
    *
    * @return A valid XML document as a String. When writing the results to a
    * file, the writer created on this string should use the encoding that was
    * passed in.
    *
    * @throws SplitterException for all IO and SAX parser exceptions.
    */
   public String getSampleXML(boolean showSampleData,
      boolean useEmptyElementTag) throws SplitterException
   {
      StringWriter out = new StringWriter();
      try
      {
         Document doc = getXMLDTDDocument(showSampleData);
         boolean ppState = HTML2XSL.isPrettyPrintingOn();
         HTML2XSL.setPrettyPrinting(true);
         HTML2XSL.printNode(doc, HTML2XSL.TAB, out, useEmptyElementTag);
         HTML2XSL.setPrettyPrinting(ppState);
      }
      catch (IOException e)
      {
         throw new SplitterException(e.getLocalizedMessage());
      }
      catch (SAXException e)
      {
         throw new SplitterException(e.getLocalizedMessage());
      }

      return out.toString();
   }


   /**
    * Looks at the type of element and finds the sample data, if there is any.
    * attrValue should be the "psx-..." variable that is the value of one of
    * the attributes in this node. The following rules are followed to determine
    * the sample data. If the result is empty or whitespace, null is returned.
    * <UL>
    * <LI>If the tag is not IMG, A, or FORM and the name of the attribute whose
    * value is supplied is ID, then the value of the node is returned.</LI>
    *
    * <LI>If the tag is not IMG, A or FORM and the name of the attribute whose
    * value is supplied is of the form psx-AttributeName, the value of the attribute
    * whose name is "AttributeName" is returned.</LI>
    *
    * <LI>If the tag is IMG, A or FORM, and the name of the attribute whose value
    * is supplied is ID, then the value of the SRC, HREF or ACTION attribute is
    * returned, respectively.</LI>
    * </UL>
    *
    * @param node An HTML document node that contains an attribute whose value
    * is supplied as the 2nd param
    *
    * @param attrName The name of one of the attributes in this node. The value
    * of this attribute is expected to have the form "psx-...".
    *
    * @param attrValue The value of the attribute whose name is attrName.
    *
    * @return A list, with the first element a Boolean specifying whether
    *    to enable or disable output escaping, the second  a String with
    *    the sample data. <code>null</code> if there is no sample data or it
    *    is whitespace.
    **/
   private List getSampleValue(Node node, String attrName, String attrValue)
      throws IOException
   {
      if (Node.ELEMENT_NODE != node.getNodeType())
         return null;

      Boolean noEscaping = false;
      List sample = new ArrayList(2);
      Element elem = (Element) node;
      NamedNodeMap attribs = node.getAttributes();

      String tagName = elem.getTagName().toLowerCase();

      String specialTags [][] =
      { /* lowercased tag name, lowercased special attribute */
         { "a", "href" },
         { "img", "src" },
         { "form", "action" }
      };
      // indexes into the above array
      final int SPECIAL_TAG = 0;
      final int SPECIAL_ATTR = 1;

      // is this a special node?
      for (int i=0; i<specialTags.length; ++i)
      {
         if (specialTags[i][SPECIAL_TAG].equals(tagName))
         {
            if (attrName.toLowerCase().equals("id"))
            {
               Attr attr = (Attr) attribs.getNamedItem(
                  specialTags[i][SPECIAL_ATTR]);
               String val = null != attr ? attr.getValue() : "";

               if (val == null || val.trim().length() == 0)
                  return null;

               sample.add(noEscaping);
               sample.add(val);
               return sample;
            }
         }
      }

      String val = null;
      if (attrName.toLowerCase().equals("id"))
      {
         if (node.getNodeName().equalsIgnoreCase("div"))
         {
            val = HTML2XSL.getElementValue(node);
            noEscaping = true;
         }
         else
            val = HTML2XSL.getElementTextValue(node);
      }
      else if (attrName.toLowerCase().startsWith(m_strPsxTag))
      {
         String realAttr = attrName.substring(m_strPsxTag.length());
         Attr attr = (Attr) attribs.getNamedItem(realAttr);
         val = null != attr ? attr.getValue() : null;
      }

      if (val == null || val.trim().length() == 0)
         return null;

      sample.add(noEscaping);
      sample.add(val);
      return sample;
   }

   /**
    * This is the function actually filters all the psx- tagged variables to write to
    * the DTD file.
    * <p>
    * Collect all psx-tagged variables from entire HTML document and make an array
    * This can be done by using node method selectNodeByTag()
    * for example you have a list of variables:
    * <p>
    * psx-customers/customer/name/first
    * psx-customers/customer/name/last
    * psx-customers/customer/phone
    * psx-customers/customer/address/street
    * psx-customers/customer/address/city
    * psx-customers/customer/address/state
    * psx-customers/customer/address/zip
    * <p>
    * The elements are stored in a hash table (ht), with the key being the
    * element name and the value being a vector that contains all the children
    * of that element. Leaf nodes contain no value within the hash table.
    * <p>
    * At the same time, we check for sample data and store it in m_sampleData map.
    * For every element that has sample data, the key is the fully qualified
    * name of the element, and the value is a string containing the sample data.
    *
    * @param bPrintToFile If <code>true</code>, then the results are written to
    * a file.
    *
    * @param enc The Java name of the char encoding to use when printing the results
    * to a file. If bPrintToFile is <code>false</code>, it is ignored.
    */
   public void Process(boolean bPrintToFile, String enc) throws IOException
   {
      m_bPrintToFile = bPrintToFile;
      m_strPsxDocAlias = new Vector();
      m_hasDocAliases = false;
      m_strBaseElement = Splitter.getRoot();

      // process all nodes for the provided document
      processNode(m_xmlDoc);

      // add the dynamic fields from the document aliases
      for (int i=0; i<m_strPsxDocAlias.size(); i++)
      {
         m_strPsxDocAlias.setElementAt(m_strBaseElement + "/" +
            m_strPsxDocAlias.elementAt(i), i);
      }
      m_strPsxArray.addAll(m_strPsxDocAlias);

      int nEffectiveCount = m_strPsxArray.size();
      int nLoc = -1;
      String strTemp = "";
      String strLeft = "";
      Vector strChildren = null;
      while (nEffectiveCount > 0)
      {
         nEffectiveCount = 0;
         for (int i=0; i<m_strPsxArray.size(); i++)
         {
            strTemp = (String) m_strPsxArray.elementAt(i);
            if (null == strTemp || strTemp.equals(""))
               continue;

            nEffectiveCount++;

            nLoc = strTemp.indexOf('/');
            if (nLoc != -1)
            {
               strLeft = strTemp.substring(0,nLoc);
               strTemp = strTemp.substring(nLoc+1);
            }
            else
            {
               strLeft = strTemp;
               strTemp = "";
            }

            m_strPsxArray.setElementAt(strTemp, i);

            if (strLeft.trim ().equals (""))
               continue;

            strChildren = (Vector) m_ht.get(strLeft);
            if (null == strChildren)
            {
               strChildren = new Vector();
               m_ht.put(strLeft, strChildren);
            }

            nLoc = strTemp.indexOf('/');
            if (-1 == nLoc)
               strLeft = strTemp;
            else
               strLeft = strTemp.substring(0, nLoc);

            if (!strLeft.trim().equals("") && -1 == strChildren.indexOf(strLeft))
               strChildren.addElement(strLeft);
         }
      }

      if (true == m_bPrintToFile)
         CreateDTDFile(enc);
   }

   /**
    * Process the provided node recursively. This updates the m_strPsxArray and
    * m_strPsxDocAlias which contain all dynamic tags or documant alias tags
    * found, make sure these members are cleared before the first call.
    *
    * @param node the node to process, assumed not <code>null</code>.
    * @throws IOException if any IO operation goes wrong.
    */
   private void processNode(Node node) throws IOException
   {
      NodeList   nodes = null;
      if (node.hasChildNodes())
      {
         // transform all child nodes first
         nodes = node.getChildNodes();
         for (int i=0; i<nodes.getLength(); i++)
         {
            processNode(nodes.item(i));
         }
      }

      int nIndex = -1;
      if (node.getNodeType() == Node.COMMENT_NODE)
      {
         String comment = ((Comment) node).getData();
         String docRef = m_strPsxTag + HTML2XSL.ms_docRef;
         nIndex = comment.indexOf(docRef);
         if (nIndex >= 0 &&
            comment.indexOf(m_strPsxTag + HTML2XSL.ms_docAlias) >= 0)
         {
            m_hasDocAliases = true;
            String test = comment.substring(nIndex + docRef.length());
            nIndex = test.indexOf(m_strPsxTag);
            if (nIndex >= 0)
            {
               String dynamicDoc = test.substring(nIndex + m_strPsxTag.length(),
                  test.indexOf('"', nIndex));
               m_strPsxDocAlias.addElement(dynamicDoc);
            }
         }

         return;
      }

      String strNodeValue = HTML2XSL.getElementTextValue(node);
      if (strNodeValue == null)
         strNodeValue = "";

      if (isMainContextPsxTag(strNodeValue))
      {
         strNodeValue = strNodeValue.substring(m_strPsxTag.length());
         nIndex = m_strPsxArray.indexOf(strNodeValue);
         if (-1 == nIndex)
            m_strPsxArray.addElement(strNodeValue);
      }

      // parsing for the element attributes
      String strAttrName;
      String strAttrValue;
      NamedNodeMap attribs = node.getAttributes();
      if (attribs == null)
         return;

      for (int i=0; i<attribs.getLength(); i++)
      {
         Attr attr = (Attr) attribs.item(i);
         strAttrName = attr.getName ();
         strAttrValue = attr.getValue ();
         // check to see if the attribute name is like "psx-src"
         if (isMainContextPsxTag(strAttrName))
         {
            if (isMainContextPsxTag(strAttrValue));
            nIndex = m_strPsxArray.indexOf(strAttrValue);
            if (-1 == nIndex)
            {
               m_strPsxArray.addElement(strAttrValue);
               // store sample data in another map
               List sample = getSampleValue(node, strAttrName,
                  strAttrValue);
               if (null != sample)
                  m_sampleData.put(strAttrValue, sample);
            }
         }

         /*
          * Check to see if the attribute value is like
          * "PSX-CUSTOMERS/CUSTOMER/SRC".
          */
         if (isMainContextPsxTag(strAttrValue))
         {
            strAttrValue = strAttrValue.substring(m_strPsxTag.length());
            nIndex = m_strPsxArray.indexOf(strAttrValue);
            if (-1 == nIndex)
            {
               m_strPsxArray.addElement(strAttrValue);
               // store sample data in another map
               List sample = getSampleValue(node, strAttrName,
                  strAttrValue);
               if (null != sample)
                  m_sampleData.put(strAttrValue, sample);
            }
         }
      }
   }

   /**
    * Create a table for all external context information found for the
    * provided node into the given table.
    *
    * @param node the node to create the table for.
    * @param table the table to create
    */
   private void createContextTable(Node node, Hashtable table)
   {
      // maintain external documents
      HTML2XSL.updateExternalDocRef(node, table);

      if (node.hasChildNodes())
      {
         NodeList nodes = node.getChildNodes();
         for (int i=0; i<nodes.getLength(); i++)
         {
            Node temp = nodes.item(i);
            createContextTable(temp, table);
         }
      }
   }

   /**
    * This returns <code>true</code> if the provided string is a valid psx-
    * taged element from the main context.
    *
    * @param source the string to test
    * @return the test result.
    */
   private boolean isMainContextPsxTag(String source)
   {
      if (source == null)
         return false;

      String test = source.toLowerCase().trim();
      return !test.equals("") &&
      test.startsWith(m_strPsxTag) &&
      !test.startsWith(m_strPsxTag + "/") &&
      !(test.equals(m_strPsxTag + "repeat"));
   }

   /**
    * This helper function creates the DTD file with default file name as the root
    * element name in the DTD.
    *
    * @param enc The Java name of the char encoding to use when writing the results
    * to a file.
    *
    * @throws IllegalArgumentException if enc is null
    * @throws UnsupportedEncodingException if enc does not name a supported encoding
    **/
   public void CreateDTDFile( String enc ) throws IOException
   {
      CreateDTDFile(m_strBaseElement + ".dtd", enc );
   }

   /**
    * This returns the DTD as ByteArrayOutputStream.
    * @param enc The Java name of the encoding to use when creating the byte array.
    * @return ByteArrayOutputStream
    *
    * @throws IllegalArgumentException if enc is null.
    * @throws UnsupportedEncodingException if enc does not name a supported encoding.
    */
   public ByteArrayOutputStream GetDTDAsByteArrayOutputStream(String enc) throws IOException
   {
      String strNewLine = "\n";
      String strElem = "", strChild = "";
      Vector strChildren;
      Enumeration e = m_ht.keys();
      ByteArrayOutputStream os = new ByteArrayOutputStream();
      OutputStreamWriter writer = new OutputStreamWriter(os, enc);

      HTML2XSL.writeXmlHeader(writer, enc);

      // create an empty root element
      if (!e.hasMoreElements() && m_hasDocAliases)
         writer.write("<!ELEMENT " + Splitter.getRoot() + " EMPTY>");

      ArrayList attribs = new ArrayList();
      while (e.hasMoreElements())
      {
         strElem = (String) e.nextElement();
         if (strElem.startsWith("@"))
            continue;   // attributes are special cased elsewhere

         attribs.clear();

         // here we need to do 2 passes. One to see if we have any attributes
         // and remove them from the element list. The second to take the
         // remaining elements and build the element structure
         strChildren = (Vector) m_ht.get(strElem);
         int nSize = strChildren.size();
         if (0 != nSize)
         {
            for (int i=0; i<nSize; i++)
            {
               strChild = (String) strChildren.get(i);
               if (strChild.length() == 0)
               {  // why keep empty elements
                  strChildren.remove(i);
                  i--;
                  nSize--;
               }
               else
               {
                  // bug id TGIS-4A9RNT - attributes must be dealt with separately
                  if (strChild.startsWith("@"))
                  {
                     // add the attribute
                     attribs.add(strChild.substring(1));
                     // remove it from the element list
                     strChildren.remove(i);
                     i--;
                     nSize--;
                  }
               }
            }
         }

         writer.write("<!ELEMENT ");
         if (0 != nSize)
         {
            String strElementDelimiter = null;
            writer.write(strElem);
            writer.write(" (");
            for (int i=0; i<nSize; i++)
            {  // we've stripped empty elements above
               strChild = (String)strChildren.elementAt(i);
               if (strElementDelimiter != null)
                  writer.write(strElementDelimiter);
               else
                  strElementDelimiter = ", ";
               writer.write(strChild + "*"); //for now we assume every element is repeating one
            }
            writer.write(")>");
         }
         else
         {
            writer.write(strElem);
            writer.write(" (#PCDATA)>");
         }
         writer.write(strNewLine);

         nSize = attribs.size();
         if (0 != nSize)
         {
            writer.write("<!ATTLIST ");
            writer.write(strElem);
            writer.write(strNewLine);

            for (int i = 0; i < nSize; i++)
            {
               writer.write((String) attribs.get(i));
               writer.write("   CDATA   #IMPLIED");
               if ((i + 1) == nSize)   // close the tag for the last attrib
                  writer.write(">");
               writer.write(strNewLine);
            }
         }
      }

      writer.flush();
      return os;
   }

   /**
    * This function creates the DTD to a supplied file and help overriding the default file name of the base elemnt name in the DTD.
    * @param output file name as String.
    *
    * @param enc The Java name of the char encoding to use when writing the results
    * to a file.
    *
    * @throws IllegalArgumentException if enc is null.
    * @throws UnsupportedEncodingException if enc does not name a supported encoding
    */
   public void CreateDTDFile(String strOutputFile, String enc) throws IOException
   {
      if (  null == enc || 0 == enc.trim().length())
         throw new IllegalArgumentException( "Encoding cannot be null" );
      ByteArrayOutputStream os = GetDTDAsByteArrayOutputStream( enc );

      FileWriter fr = new FileWriter(strOutputFile);

      fr.write (new String(os.toByteArray()));

      fr.flush ();
   }

   /**
    * This is the access function to get XML sample file. It returns the sample
    * which contains elements, and attributes based on the DTD, along with
    * sample data (if available) when requested by the supplied flag.
    *
    * @param   addSampleData If <code>true</code>, add the sample data to the elements
    * in the resulting document, <code>false</code> if only the non-data elements
    * are desired.
    *
    * @return A document that contains all the elements generated by processing
    * the HTML file that was set when creating this object. Before calling this
    * method, <code>process</code> must be called to perform the actual work.
    */
   public Document getXMLDTDDocument(boolean addSampleData)
      throws IOException, SAXException
   {
      if (m_strBaseElement.trim().equals(""))
         return null;

      Document doc = HTML2XSL.createXMLDocument(m_strBaseElement);
      Element rootElem = doc.getDocumentElement();

      // we need to keep track of the elements we've seen or else we
      // can get into serious recursion problems
      Map elementsSeen = new HashMap();

      AddChildElements(rootElem, doc, elementsSeen,
         addSampleData ? m_sampleData : null);

      if (!m_bPrintToFile)
         return doc;

      int nLoc = m_strHTMLFile.indexOf('.');
      String strXSLFile =
         (nLoc != -1) ? m_strHTMLFile.substring(0, nLoc) : m_strHTMLFile;
      strXSLFile += ".xml";

      PrintWriter pw = new PrintWriter(new FileWriter(strXSLFile));
      boolean ppState = HTML2XSL.isPrettyPrintingOn();
      HTML2XSL.setPrettyPrinting(true);
      HTML2XSL.printNode(doc, HTML2XSL.TAB, pw);
      HTML2XSL.setPrettyPrinting(ppState);
      pw.flush();
      pw.close();

      return doc;
   }


   /**
    * Determines the fully qaulified name for the supplied element and returns
    * it. The form will be parent0/parent1/parent2/.../elementName. If elem is
    * part of a document, "parent0" will be the root of the document.
    *
    * @param elem A valid element. If null, an exception is thrown.
    *
    * @return The fully qualified name of this element.
    *
    * @throws IllegalArgumentException if elem is null.
    **/
   private String getFullyQualifiedName( Element elem )
   {
      if ( null == elem )
         throw new IllegalArgumentException( "Parameter cannot be null" );

      String path = elem.getTagName();
      Element parent = elem;
      do
      {
         Node node = parent.getParentNode();
         if ( null == node || Node.ELEMENT_NODE != node.getNodeType())
            parent = null;
         else
         {
            parent = (Element) node;
            path = parent.getTagName() + "/" + path;
         }
      }
      while ( null != parent );
      return path;
   }

   private void AddChildElements(Element elemParent, Document doc,
      Map elementsSeen)
   {
      AddChildElements(elemParent, doc, elementsSeen, null);
   }

   private void AddChildElements(Element elemParent, Document doc,
      Map elementsSeen, Map sampleData)
   {
      String strElem = elemParent.getNodeName();
      boolean hasSampleData = null != sampleData && !sampleData.isEmpty();

      /* Note: this does not support nodes that contain children and data */
      Vector strChildren = (Vector) m_ht.get(strElem);
      if (strChildren == null)
         return;

      int nSize = strChildren.size();
      if(0 == nSize && hasSampleData )
      {
         String key = getFullyQualifiedName(elemParent);
         List sample = (List) m_sampleData.get(key);
         if (null != sample)
         {
            Text txt = doc.createTextNode((String) sample.get(1));
            elemParent.appendChild(txt);

            if (((Boolean) sample.get(0)).booleanValue())
               elemParent.setAttribute("no-escaping", "yes");
         }
         return;
      }

      String strChild;
      Element elemChild;
      for (int i=0; i<nSize; i++)
      {
         strChild = (String)strChildren.elementAt(i);
         // nodes may be stored within their children. this is a bad thing
         // as we can get stuck in an infinite loop. as such, we'll check
         // if we've seen this child already, and if so, avoid the recursion
         if (elementsSeen.get(strChild) == null)
         {
            // bug id TGIS-4A9RNT - attributes must be dealt with separately
            if (strChild.startsWith("@"))
            {
               // we can tack attributes on to others, so don't set
               // elementsSeen
               String value = "";
               if (hasSampleData)
               {
                  String key = getFullyQualifiedName(elemParent);
                  key += "/" + strChild;
                  List sample = (List) m_sampleData.get(key);
                  if (null != sample)
                     value = (String) sample.get(1);
               }
               elemParent.setAttribute(strChild.substring(1), value);
            }
            else
            {
               elementsSeen.put(strChild, Boolean.TRUE);
               elemChild = (Element) elemParent.appendChild(doc.createElement(strChild));
               AddChildElements(elemChild, doc, elementsSeen, sampleData);
            }
         }
      }
   }

   public String getBaseElementName()
   {
      return m_strBaseElement;
   }

   public Hashtable getDTDHashTable()
   {
      return m_ht;
   }

   protected String   m_strPsxTag =
      SplitterConfiguration.getDefaultProperty("dynamicTag");
   protected String   m_strHTMLFile = "";
   protected String   m_strBaseElement = "";
   protected boolean   m_bPrintToFile = true;

   protected Document   m_xmlDoc = null;
   protected Hashtable m_ht = null;
   protected Map m_sampleData = new HashMap();
   protected Vector  m_strPsxArray = null;
   /**
    * A vector used to store all document alias dynamic fields found while
    * processing a document. This must be initialized for each run of Process.
    */
   private Vector m_strPsxDocAlias = null;
   /**
    * A flag indication that we found document aliases while processing the
    * document. This must be initialised to <code>false</code> before each run
    * of Process.
    */
   private boolean m_hasDocAliases = false;
}
