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


import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * The HTMLFORMS class is used to create an XML file that has a list of forms, form fields with names
 *
 * @author     Rammohan Vangapalli
 * @version    1.0
 * @since      1.0
 */
public class HTMLFORMS
{
   /**
    * Construct a HTMLFORMS
    */
   public HTMLFORMS()
   {
   }

   /**
    * Another Constructer that takes the HTML Source
    * Supported properties are:
    * <table border="1">
    * <tr><th>Key</th><th>Value</th></tr>
    * <tr><td>Input HTML File Path</td><td>input HTML file for form field filtering</td>
    * </tr>
    * </table>
    *
    * @param   strHTMLFile String
    */
   public HTMLFORMS (String strHTMLFile) throws SAXException, DOMException, IOException
   {
      SetHTMLFile(strHTMLFile);
   }

   /**
    * Constructer that takes the InputStream
    * Supported properties are:
    * <table border="1">
    * <tr><th>Key</th><th>Value</th></tr>
    * <tr><td>InputStream</td><td>InputStream possibly built out of another programs output</td>
    * <tr><td>psx-tag</td><td>new psx-tag string</td>
    * </tr>
    * </table>
    *
    * @param rdr A reader that can be used to get the HTML document.
    */
   public HTMLFORMS (Reader rdr) throws SAXException, IOException
   {
         m_xmlDoc = HTML2XSL.getXMLDocFromInputStream(rdr);
   }

   public HTMLFORMS( Document doc )
   {
      m_xmlDoc = doc;
   }

   /**
    * Set the HTML Source file processing.
    * Supported properties are:
    * <table border="1">
    * <tr><th>Key</th><th>Value</th></tr>
    * <tr><td>Input HTML File Path</td><td>input HTML file for form field filtering</td>
    * </tr>
    * </table>
    *
    * @param   strHTMLFile   String
    *
    */
   public void SetHTMLFile(String strHTMLFile) throws SAXException, DOMException, IOException
   {
      m_strHTMLFile = strHTMLFile;
      DocumentBuilder db = Util.getDocumentBuilder();
      m_xmlDoc = db.parse(m_strHTMLFile);
   }

   /**
    * Get the HTML Source file set for form field filtering
    *
    * @return      the HTML Source file as String
    */
   public String GetHTMLFile(String strHTMLFile)
   {
      return m_strHTMLFile;
   }

   /**
    * Processes the HTML data supplied during construction or via the setHTMLFile
    * method. Processing involves reading the file and extracting all html form
    * names. These can be retrieved in a DTD document via the getFormsDTDAsByteArray
    * method.
    *
    * @param bPrintToFile If <code>true</code>, the results are written to a
    * file after the document has been processed.
    *
    * @param enc The Java name of the char encoding. This parameter is ignored
    * unless bPrintToFile is <code>true</code>.
    *
    * @throws IllegalArgumentException if enc is null and bPrintToFile is <code>
    * true</code>.
    * @throws UnsupportedEncodingException if enc is not supported by the JDK.
   **/
   public void Process( boolean bPrintToFile, String enc ) throws IOException, SAXException
   {
      m_strFieldNameArray = new Vector();

      NodeList nodesHTML = m_xmlDoc.getElementsByTagName("form");
      if(1>nodesHTML.getLength())
         nodesHTML = m_xmlDoc.getElementsByTagName("FORM");

         Element elemForm;
      for(int i=0; i<nodesHTML.getLength(); i++)
      {
         elemForm = (Element) nodesHTML.item(i);
         ProcessElement(elemForm, "input");
         ProcessElement(elemForm, "select");
         ProcessElement(elemForm, "textarea");
      }

      //build the document from the collected form names
      m_xmlFormsDoc = HTML2XSL.createXMLDocument(m_strBaseElement, true);
      Element rootElem = m_xmlFormsDoc.getDocumentElement();

      String strTemp = "";
      Element elem;
      for(int i=0; i<m_strFieldNameArray.size (); i++)
      {
         strTemp = (String)m_strFieldNameArray.elementAt (i);
         elem = m_xmlFormsDoc.createElement(strTemp);
         setElementValue(m_xmlFormsDoc, elem, "Dummy");
         rootElem.appendChild(elem);
      }

      if(true == bPrintToFile)
         CreateFORMSFile( enc );
   }

   private void ProcessElement(Element elemHTML, String strFormElemTag)
   {
      NodeList nodesHTML = elemHTML.getElementsByTagName(strFormElemTag.toUpperCase());
      if(1 > nodesHTML.getLength())
         nodesHTML = elemHTML.getElementsByTagName(strFormElemTag.toLowerCase());

      String strAttrValue = "";
      Element elemTemp;
      for(int i=0; i<nodesHTML.getLength(); i++)
      {
         elemTemp = (Element)nodesHTML.item(i);

         strAttrValue = elemTemp.getAttribute("name");
         if(null == strAttrValue || strAttrValue.equals("")) //no NAME attribute, try ID attribute
            strAttrValue = elemTemp.getAttribute("id");
         if(null == strAttrValue || strAttrValue.equals("")) //no ID attribute too
            continue;

      if(-1 == m_strFieldNameArray.indexOf(strAttrValue))
        m_strFieldNameArray.addElement(strAttrValue);
    }
  }

   public static void setElementValue(Document xmlDoc, Node node, String strValue)
   {
      Node nodeChild = xmlDoc.createTextNode("#text");
      if(null == nodeChild)
         return;
      nodeChild.setNodeValue(strValue);
      node.appendChild(nodeChild);
   }

   /**
    * This function creates the xml file with all forms and the field & names in
    * the forms. The name of the file is derived from the original HTML file name.
    *
    * @param enc The Java name of the char encoding to use when converting the
    * chars to the byte array.
    *
    * @throws IllegalArgumentException if enc is null.
    * @throws UnsupportedEncodingException if enc is not supported by the JDK.
    */
   public void CreateFORMSFile( String enc ) throws IOException
   {
      String strFORMSFile;

      int nLoc = m_strHTMLFile.indexOf('.');
      if(-1 != nLoc)
         strFORMSFile = m_strHTMLFile.substring(0, nLoc);
      else
         strFORMSFile = m_strHTMLFile;

      CreateFORMSFile(strFORMSFile + "_f.xml", enc );
   }

   /**
    * This function creates the xml file with all forms and the field & names in the forms.
    * The characters are written to the stream using the local encoding.
    *
    * @param enc The Java name of the char encoding to use when converting the
    * chars to the byte array.
    *
    * @throws IllegalArgumentException if enc is null.
    * @throws UnsupportedEncodingException if enc is not supported by the JDK.
   **/
   public ByteArrayOutputStream GetFormsDTDAsByteArrayOutputStream( String enc )
      throws IOException
   {
      if ( null == enc )
         throw new IllegalArgumentException( "Encoding cannot be null" );
      ByteArrayOutputStream str = new ByteArrayOutputStream();
      OutputStreamWriter os = new OutputStreamWriter( str, enc );

      HTML2XSL.writeXmlHeader(os, enc);

      String strNewLine = "\n";
      String strChild = "";
      String strDTDElem = "<!ELEMENT ";
      strDTDElem += m_strBaseElement + " (";
      int nSize = m_strFieldNameArray.size();
      for(int i=0; i<nSize; i++)
      {
         strChild = (String)m_strFieldNameArray.elementAt(i);
         if (StringUtils.isNotEmpty(strChild))
         {
            strDTDElem += strChild;
            if(i != nSize-1)
               strDTDElem += ", ";
         }
      }
      strDTDElem += ")>";

      os.write (strDTDElem, 0, strDTDElem.length());
      os.write (strNewLine, 0, strNewLine.length());

      for(int i=0; i<nSize; i++)
      {
         strDTDElem = "<!ELEMENT ";
         strChild = (String)m_strFieldNameArray.elementAt(i);
         strDTDElem += strChild + " (#PCDATA)>";
         os.write (strDTDElem, 0, strDTDElem.length());
         os.write (strNewLine, 0, strNewLine.length());
      }
      os.flush();

      return str;
   }

   /**
    * This function creates the xml file with all forms and the field & names in the forms
    *
    * @param strOutputFile The name of the file to write the results.
    *
    * @param enc The Java name of the char encoding to use when converting the
    * chars to the byte array.
    *
    * @throws IllegalArgumentException if enc is null.
    * @throws UnsupportedEncodingException if enc is not supported by the JDK.
    */
   public void CreateFORMSFile(String strOutputFile, String enc ) throws IOException
   {
      FileWriter fr = new FileWriter(strOutputFile);

      ByteArrayOutputStream os = GetFormsDTDAsByteArrayOutputStream( enc );

    fr.write (new String(os.toByteArray()));

    fr.flush ();
   }

   /**
    * This is the access function to get the XML String of the XML form fields Documnet.
    */
   public Document getXMLFormsDocument() throws IOException
   {
      if(m_strBaseElement.trim().equals(""))
         return null;

      return m_xmlFormsDoc;
   }

   public String getBaseElementName()
   {
    return m_strBaseElement;
  }

   protected String   m_strHTMLFile = "";
   protected Vector   m_strFieldNameArray = null;
   protected String   m_strBaseElement = "PSXParam";

   protected Document   m_xmlDoc = null;
   protected Document  m_xmlFormsDoc = null;
}
