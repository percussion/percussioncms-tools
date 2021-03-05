/*[ PSXslExtractor.java ]******************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader.extractor;


import com.icl.saxon.expr.XPathException;
import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.IPSFieldValue;
import com.percussion.cms.objectstore.PSItemField;
import com.percussion.cms.objectstore.ws.PSClientItem;
import com.percussion.loader.PSItemContext;
import com.percussion.loader.PSLoaderUtils;
import com.percussion.loader.objectstore.PSFieldProperty;
import com.percussion.loader.objectstore.PSProperty;
import com.percussion.loader.ui.PSConfigPanel;
import com.percussion.loader.ui.PSXSLConfigPanel;
import com.percussion.xml.PSXPathEvaluator;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Iterator;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;

/**
 * The XSL Extractor is expecting to extract items from source data in XML.
 * The source data may already conformed with the sys_StandardItem.dtd or it
 * will be transformed to the XML that conformed with sys_StandardItem.dtd.
 * The transformation is done by applying the XSL file that is specified in
 * the extractor definition. The extractor definition may contains a set of
 * default values for a list of fields, that is defined in the Field Properties.
 * The default values will be apply to the empty value fields for the
 * "transformed" or final version of the XML, which is conformed with
 * sys_StandardItem.dtd.
 */
public class PSXslExtractor extends PSItemExtractor
{
   // Implements the IPSUIPlugin interface
   public PSConfigPanel getConfigurationUI()
   {
      return new PSXSLConfigPanel();
   }

   // Implements IPSItemExtractor.extractItems(PSItemContext, InputStream)
   public PSItemContext[] extractItems(PSItemContext resource, InputStream in)
      throws java.io.IOException
   {

      try
      {
         // get the XML and apply stylesheet if needed
         Document doc = PSXmlDocumentBuilder.createXmlDocument(in, false);
         doc = transformDocument(doc);

         PSClientItem clientItem = resource.getItem();
         deleteChildEntries(clientItem); // set delete action to old child entries

         // overlay the updated item (in XML) to the original client item.
         clientItem.loadXmlData(doc.getDocumentElement());

         // set the default values for the specified fields if needed
         setDefaultValues(resource, doc);
      }
      catch (Exception e)
      {
         throw new RuntimeException(e.toString());
      }

      PSItemContext[] items = new PSItemContext[1];
      items[0] = resource;

      return items;
   }


   /**
    * For each Field Property (that is defined in the extractor definition),
    * if the value of the field in the current client item is empty, then
    * set it to the default value, which is defined in the Field Property.
    * The defined Field Properties are expected to be the item fields, not
    * the fields in child entries.
    *
    * @param itemCtx The context that contains the current processed client
    *    item object, assume not <code>null</code>.
    *
    * @param doc The final or transformed XML document, assume not
    *    <code>null</code>.
    *
    * @throws IOException if error occurs when evaluating $content variable.
    * @throws TransformerException if error occurs while creating XPath
    *    evaluator.
    * @throws XPathException if error occurs while evaluating XPath expression.
    * @throws PSCmsException if other error occurs.
    */
   private void setDefaultValues(PSItemContext itemCtx, Document doc)
      throws IOException,
             PSCmsException,
             TransformerException,
             XPathException
   {
      // prepare the evaluators for the operation below
      ByteArrayInputStream memStream = new ByteArrayInputStream(
         PSXmlDocumentBuilder.toString(doc).getBytes());

      PSVariableEvaluator varEval = new PSVariableEvaluator(itemCtx,
         false, memStream);

      PSXPathEvaluator xpathEval = new PSXPathEvaluator(doc);

      // Loop through each Field Property, set to default value if needed
      PSClientItem clientItem = itemCtx.getItem();
      Iterator fieldProperties = getExtractorDef().getFieldProperties();
      while (fieldProperties.hasNext())
      {
         PSFieldProperty prop = (PSFieldProperty) fieldProperties.next();
         PSItemField itemField = getField(clientItem, prop.getName());
         IPSFieldValue value = itemField.getValue();

         if (value == null ||
             value.getValue() == null ||
             value.getValueAsString().length() == 0)
         {
            setFieldValue(itemField, prop, varEval, xpathEval);
         }
      }
   }

   /**
    * Transform the specified document with the stylesheet that is
    * defined in the extractor definition. Do nothing and simply return
    * the pass in document if the stylesheet is not defined in the extractor
    * definition.
    *
    * @param doc The to be transformed document, assume not <code>null</code>.
    *
    * @return The transformed document. It may be the same one as
    *    <code>doc</code> if the <code>XSL_FILEPATH</code> property is not
    *    defined in the extractor definition.
    *
    * @throws MalformedURLException if fail to create URL object from the value
    *    of <code>XSL_FILEPATH</code> property.
    * @throws TransformerConfigurationException if an error occurs during
    *    transformation.
    * @throws TransformerException if an error occurs during transformation.
    */
   private Document transformDocument(Document doc)
      throws MalformedURLException,
             TransformerConfigurationException,
             TransformerException
   {
      PSProperty xslProp = PSLoaderUtils.getOptionalProperty(XSL_FILEPATH,
         getExtractorDef().getProperties());

      if (xslProp != null)
      {
         TransformerFactory factory = TransformerFactory.newInstance();
         Transformer transformer = factory.newTransformer(
            new StreamSource(new File(xslProp.getValue())));
         DOMResult result = new DOMResult();
         transformer.transform(new DOMSource(doc), result);
         doc = (Document) result.getNode();
      }

      return doc;
   }

   /**
    * The property name for XSL path in the extractor definition.
    */
   public final static String XSL_FILEPATH = "XSL FilePath";
}