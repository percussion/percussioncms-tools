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
    * @return the error message if there were errors or <code>null</code>
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
         if (stylesheet == null || xml == null || html == null)
            throw new IllegalArgumentException("parameters cannot be null");

         PSTransformErrorListener errorListener = 
            new PSTransformErrorListener(errorWriter);
         TransformerFactory factory = TransformerFactory.newInstance();
         factory.setErrorListener(errorListener);
         
         Transformer transformer = factory.newTransformer(
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

