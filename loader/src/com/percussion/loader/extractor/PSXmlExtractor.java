/*[ PSXmlExtractor.java ]******************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader.extractor;

import com.icl.saxon.expr.XPathException;
import com.percussion.cms.objectstore.PSItemChild;
import com.percussion.cms.objectstore.PSItemChildEntry;
import com.percussion.cms.objectstore.PSItemField;
import com.percussion.cms.objectstore.PSTextValue;
import com.percussion.cms.objectstore.ws.PSClientItem;
import com.percussion.loader.IPSLoaderErrors;
import com.percussion.loader.PSItemContext;
import com.percussion.loader.PSLoaderException;
import com.percussion.loader.objectstore.PSFieldProperty;
import com.percussion.loader.ui.PSConfigPanel;
import com.percussion.loader.ui.PSXMLConfigPanel;
import com.percussion.xml.PSXPathEvaluator;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.w3c.dom.Document;

/**
 * The XML Extractor extracts item data from XML source data. The extraction
 * is based on a set of Field Properties that is defined in the extractor
 * definition. It evaluates the "value-expression" for each "Field Property"
 * and set the result to the related field.
 */
public class PSXmlExtractor extends PSItemExtractor
{

   // Implements the IPSUIPlugin interface
   public PSConfigPanel getConfigurationUI()
   {
      return new PSXMLConfigPanel();
   }

   // Implements IPSItemExtractor.extractItems(PSItemContext, InputStream)
   public PSItemContext[] extractItems(PSItemContext itemCtx, InputStream in)
      throws java.io.IOException
   {

      try
      {
         deleteChildEntries(itemCtx.getItem());

         // prepare evaluators for both XPath and Extractor-Variable
         Document doc = PSXmlDocumentBuilder.createXmlDocument(in, false);
         PSXPathEvaluator xpEval = new PSXPathEvaluator(doc);

         ByteArrayInputStream memStream = new ByteArrayInputStream(
            PSXmlDocumentBuilder.toString(doc).getBytes());
         PSVariableEvaluator varEval = new PSVariableEvaluator(itemCtx,
            false, memStream);

         // Loop through each Field Property, set the evaluated value
         PSClientItem clientItem = itemCtx.getItem();
         Iterator fieldProperties = getExtractorDef().getFieldProperties();
         while (fieldProperties.hasNext())
         {
            PSFieldProperty prop = (PSFieldProperty) fieldProperties.next();
            PSItemField itemField = clientItem.getFieldByName(prop.getName());

            if (itemField != null)
            {
               setFieldValue(itemField, prop, varEval, xpEval);
            }
            else // it may be a field in one of the child item
            {
               if (isChildField(clientItem, prop.getName()))
               {
                  setChildFieldValue(clientItem, prop, varEval, xpEval);
               }
               else // log field not found
               {
                  String[] args = {prop.getName(), getExtractorDef().getName()};
                  PSLoaderException exp = new PSLoaderException(
                     IPSLoaderErrors.CANNOT_FIND_FIELD, args);

                  throw new RuntimeException(exp.getLocalizedMessage());
               }
            }
         } // while ()
      }
      catch (Exception e)
      {
         throw new RuntimeException(e.toString());
      }

      //System.out.println(PSXmlDocumentBuilder.toString(
      //   itemCtx.getStandardItemDoc()));

      PSItemContext[] items = new PSItemContext[1];
      items[0] = itemCtx;

      return items;
   }

   /**
    * Evaluates the specified XPath expression and set the result to the
    * specified child field.
    *
    * @param clientItem The client item that contains the specified field,
    *    assume not <code>null</code>.
    *
    * @param prop The field property, its name is the name of the child field,
    *    its value is the to be evaluated value-expression. Assume not
    *    <code>null</code>.
    *
    * @param varEval The variable evaluator, assume not <code>null</code>.
    *
    * @param xpathEval The XPath evaluator, asssume not <code>null</code>.
    *
    * @throws IOException if an error occurs while evaluating an
    *    extractor-variable.
    * @throws XPathException if an error occurs while evaluating the XPath
    *    expression.
    */
   private void setChildFieldValue(PSClientItem clientItem,
      PSFieldProperty prop,
      PSVariableEvaluator varEval,
      PSXPathEvaluator xpathEval)
      throws XPathException,
             IOException
   {
      Iterator children = clientItem.getAllChildren();
      while (children.hasNext())
      {
         PSItemChild child = (PSItemChild) children.next();
         if (isChildEntryField(child, prop.getName()))
         {

            String type = prop.getValueType();
            if (type.equalsIgnoreCase(PSFieldProperty.VALUE_TYPE_XPATH))
            {
               String[] values = evaluateXPath(xpathEval, prop.getValue());
               for (int i=0; i < values.length; i++)
               {
                  PSItemChildEntry entry = child.createChildEntry();
                  entry.setAction(PSItemChildEntry.CHILD_ACTION_INSERT);
                  PSItemField itemField = entry.getFieldByName(prop.getName());
                  PSTextValue tvalue = new PSTextValue(values[0]);
                  itemField.addValue(tvalue);

                  child.addEntry(entry);
               }
            }
            else
            {
               PSItemChildEntry entry = child.createChildEntry();
               entry.setAction(PSItemChildEntry.CHILD_ACTION_INSERT);
               PSItemField itemField = entry.getFieldByName(prop.getName());
               itemField.addValue(varEval.evaluate(prop.getValue()));

               child.addEntry(entry);
            }
         }
      }
   }

   /**
    * Determines whether the specified field name exists in one of the item
    * child within the given client item.
    *
    * @param clientItem The client item to be searched from, assume not
    *    <code>null</code>.
    *
    * @param fieldName The searched field name, assume not <code>null</code>.
    *
    * @return <code>true</code> if found the field name exist in one of the
    *    item child within the client item; otherwise, return <code>false
    *    </code>.
    */
   private boolean isChildField(PSClientItem clientItem, String fieldName)
   {
      Iterator children = clientItem.getAllChildren();
      while (children.hasNext())
      {
         PSItemChild child = (PSItemChild) children.next();
         if (isChildEntryField(child, fieldName))
            return true;
      }
      return false;
   }

   /**
    * Determines whether the specified field name exists in the given item
    * child.
    *
    * @param child The item child, assume not <code>null</code>.
    *
    * @param fieldName The searched field name, assume not <code>null</code>.
    *
    * @return <code>true</code> if the field name exists in the item child;
    *    otherwise, return <code>false</code>.
    */
   private boolean isChildEntryField(PSItemChild child, String fieldName)
   {
      PSItemChildEntry entry = null;
      PSItemField itemField = null;
      if (child.childEntryCount() > 0)
      {
         Iterator entries = child.getAllEntries();
         entry = (PSItemChildEntry) entries.next();
      }
      else
      {
         entry = child.createChildEntry();
      }

      return (entry.getFieldByName(fieldName) != null);
   }

   /**
    * Unit test
    */
   public static void main(String[] args)
   {
      try
      {
         String simpleXml =
            "C:\\e2.crimson\\UnitTestResources\\com\\percussion\\xml\\simple.xml";
         FileInputStream in = new FileInputStream(simpleXml);
         Document doc = PSXmlDocumentBuilder.createXmlDocument(in, false);
         PSXPathEvaluator xpEval = new PSXPathEvaluator(doc);

         PSXmlExtractor xmlExtractor = new PSXmlExtractor();

         String[] body = xmlExtractor.evaluateXPath(xpEval, "//body");
         //System.out.println("[//body] = " + body[0]); 
         if (body.length != 1 || body[0].length() == 0) // should be only 1
            System.out.println("ERROR: body.length != 1 or is EMPTY");
         
         
         String[] value = xmlExtractor.evaluateXPath(xpEval,
            "//ControlNameSet");
         //System.out.println("[//value] = " + value[0]); 

         String[] controlName1 = xmlExtractor.evaluateXPath(xpEval,
            "//ControlName");
         //System.out.println("[//ControlName] = " + controlName1[0]); 

         String[] controlNameSys = xmlExtractor.evaluateXPath(xpEval,
            "//ControlName[@name='sys']");
         //System.out.println("[//ControlName[@name='sys']] = " + controlNameSys[0]); 

         if (! controlName1[0].equals(controlNameSys[0]))
            System.out.println("ERROR: controlName1 != controlNameSys");

         String[] childKey = xmlExtractor.evaluateXPath(xpEval,
            "//Row/@childkey");
         if (! childKey[0].equals("3"))
            System.out.println("ERROR: childKey != 3");
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

}