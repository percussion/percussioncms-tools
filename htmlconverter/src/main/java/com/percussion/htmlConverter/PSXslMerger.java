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

import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * This class provides functionality to create an HTML document from an XSL and
 * XML document. All methods are static, so no instances of this object need to
 * be instantiated.
 */
public class PSXslMerger
{
   /**
    * Do not create instances of this class, use the static methods instead.
    */
   private PSXslMerger()
   {
   }
   
   /**
    * Takes a stylesheet and xml data and merges them into an HTML file.
    *
    * @param stylesheet an input source containing the stylesheet to merge 
    *    w/ the xml data, not <code>null</code>.
    * @param xml a reader containing the data to merge w/ the supplied 
    *    stylesheet, not <code>null</code>.
    * @param html A stream to which the output HTML file will be written, 
    *    not <code>null</code>.
    * @return the error message if ther were errors or <code>null</code>
    *    otherwise.
    */
   public static String merge(InputSource stylesheet, Reader xml, 
      OutputStream html) throws IOException, SAXException, 
         TransformerConfigurationException, TransformerException
   {
      String errorMessage = null;
      StringWriter errorWriter = new StringWriter();
      try
      {
         if (stylesheet == null || xml == null || html == null || 
            errorWriter == null)
            throw new IllegalArgumentException("parameters cannot be null");

         PSTransformErrorListener errorListener = 
            new PSTransformErrorListener(errorWriter);
         TransformerFactory factory = TransformerFactory.newInstance();
         factory.setErrorListener(errorListener);
         
         Transformer transformer = transformer = factory.newTransformer(
            new SAXSource(stylesheet));
         transformer.setErrorListener(errorListener);
         transformer.transform(new StreamSource(xml), new StreamResult(html));
      }
      catch (TransformerException e)
      {
         errorMessage = errorWriter.toString();
      }
      
      return errorMessage;
   }
}

