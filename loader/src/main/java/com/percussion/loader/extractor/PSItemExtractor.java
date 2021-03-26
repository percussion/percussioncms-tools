/******************************************************************************
 *
 * [ PSItemExtractor.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.loader.extractor;

import com.icl.saxon.expr.XPathException;
import com.icl.saxon.om.AbstractNode;
import com.icl.saxon.om.NodeInfo;
import com.percussion.cms.objectstore.PSDateValue;
import com.percussion.cms.objectstore.PSItemChild;
import com.percussion.cms.objectstore.PSItemChildEntry;
import com.percussion.cms.objectstore.PSItemField;
import com.percussion.cms.objectstore.PSItemFieldMeta;
import com.percussion.cms.objectstore.PSTextValue;
import com.percussion.cms.objectstore.ws.PSClientItem;
import com.percussion.guitools.PSCalendarField;
import com.percussion.loader.IPSLoaderErrors;
import com.percussion.loader.PSLoaderException;
import com.percussion.loader.objectstore.PSFieldProperty;
import com.percussion.util.PSDataTypeConverter;
import com.percussion.xml.PSXPathEvaluator;
import com.percussion.xml.PSXmlTreeWalker;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

/**
 * This class contains convenient methods for most item extractors. The derived
 * class must implement the <code>extractItems</code> method, which is defined
 * in <code>IPSItemExtractor</code>. The GUI configuration panel is empty,
 * which can be customized by overriding the {@link #getConfigurationUI()}
 * method.
 */
public abstract class PSItemExtractor extends PSExtractor
{
   /**
    * Set delete action to all child entries for the specified client item.
    *
    * @param clientItem The client item, it may not be <code>null</code>.
    */
   protected void deleteChildEntries(PSClientItem clientItem)
   {
     if (clientItem == null)
        throw new IllegalArgumentException("clientItem may not be null");

      Iterator children = clientItem.getAllChildren();
      while (children.hasNext())
      {
         PSItemChild child = (PSItemChild) children.next();
         Iterator entries = child.getAllEntries();
         while (entries.hasNext())
         {
            PSItemChildEntry entry = (PSItemChildEntry) entries.next();
            entry.setAction(PSItemChildEntry.CHILD_ACTION_DELETE);
         }
      }
   }

   /**
    * Evaluates the specified XPath expression.
    *
    * @param xpathEval The XPath evaluator, it may not be <code>null</code>.
    *
    * @param XPath The XPath expression, it may not be <code>null</code> or
    *    empty.
    *
    * @return The values of the evaluated nodes as array of String. It will
    *    never be <code>null</code>. The array will contain at least one
    *    element, which may be an empty string, but not <code>null</code>.
    *
    * @throws XPathException if an error occurs while evaluating the XPath.
    */
   protected String[] evaluateXPath(PSXPathEvaluator xpathEval,
      String XPath)
      throws XPathException
   {
      if (xpathEval == null)
         throw new IllegalArgumentException("xpathEval may not be null");
      if (XPath == null || XPath.trim().length() == 0)
         throw new IllegalArgumentException("XPath may not be null");

      Iterator it = xpathEval.enumerate(XPath, true);
      List valueList = new ArrayList();
      while (it.hasNext())
      {
         String value = "";
         NodeInfo nodeInfo = (NodeInfo) it.next();
         if (nodeInfo.getNodeType() == Node.ELEMENT_NODE)
         {
            // serialize the Element with PSXmlTreeWalker
            AbstractNode node = (AbstractNode) nodeInfo;
            PSXmlTreeWalker treeWalker = new PSXmlTreeWalker(node);
            StringWriter writer = new StringWriter();
            try {
               treeWalker.write(writer);
            }
            catch (IOException ignore) { // this is not possible
               throw new RuntimeException(ignore.toString());
            }
            value = writer.toString();
            boolean unexpectedFormat = false;

            // extracts the value of the Element
            String nodeBegin = "<" + node.getNodeName();
            if (value.indexOf(nodeBegin) >= 0)
            {
               int begin = value.indexOf(nodeBegin) + nodeBegin.length();
               int lessThan = value.indexOf(">", begin);

               // if it is:  <NodeName attr=v1 ... />
               if ((lessThan + 1) == value.length())
               {
                  value = "";
               }
               else // then it must be  <NodeName>     ... </NodeName>
               {    //              or  <NodeName ...> ... </NodeName>

                  String nodeEnd = "</" + node.getNodeName() + ">";
                  int end = value.lastIndexOf(nodeEnd);
                  if (end != -1 && (lessThan+1) <= end)
                     value = value.substring(lessThan+1, end);
                  else
                     unexpectedFormat = true;
               }
            }
            else
            {
               unexpectedFormat = true;
            }
            if (unexpectedFormat)
            {
               Logger.getLogger(PSXmlExtractor.class).warn(
                  "Unexpected XML text: " + value);
            }
         }
         else
         {
            value = nodeInfo.getStringValue();
         }
         valueList.add(value);
      }

      // convert list to array of String
      String[] values = null;
      if (valueList.size() > 0)
      {
         values = new String[valueList.size()];
         valueList.toArray(values);
      }
      else
      {
         values = new String[1];
         values[0] = new String("");
      }

      return values;
   }

   /**
    * Convenient method calls {@link #setFieldValue(PSItemField,
    * PSFieldProperty, PSVariableEvaluator, PSXPathEvaluator)
    * setFieldValue(PSItemField, PSFieldProperty, PSVariableEvaluator,
    * null)}
    *
    * @throws IOException if an error occurs while evaluating the
    *    variable-expression.
    * @throws IllegalStateException if <code>xpathEval</code> is
    *    <code>null</code> but the value type of <code>fieldProperty</code> is
    *    <code>PSFieldProperty.VALUE_TYPE_XPATH</code>.
    */
   protected void setFieldValue(PSItemField itemField,
      PSFieldProperty fieldProperty,
      PSVariableEvaluator varEval)
      throws IOException
   {
      try
      {
         setFieldValue(itemField, fieldProperty, varEval, null);
      }
      catch (XPathException e)
      {
         e.printStackTrace(); // this is not possible
      }
   }

   /**
    * Evaluates the specified value-expression and set the evaluated value for
    * the specified item field, which is a top level field, not a child field.
    *
    * @param itemField   The item field (non-child field), its value will be 
    *    set to the evaluated result. It may not be <code>null</code>.
    *
    * @param fieldProperty The field property that contains the to be evaluated
    *    value-expression. It may not be <code>null</code>.
    *
    * @param varEval The evaluator for extractor-variable, it may not be
    *    <code>null</code>.
    *
    * @param xpathEval The Evaluator for XPath expression, it may be
    *    <code>null</code>.
    *
    * @throws IOException if an error occurs while evaluating the
    *    variable-expression.
    * @throws IllegalStateException if <code>xpathEval</code> is
    *    <code>null</code> but the value type of <code>fieldProperty</code> is
    *    <code>PSFieldProperty.VALUE_TYPE_XPATH</code>.
    * @throws XPathException if an error occurs while evaluating the XPath
    *    expression.
    */
   protected void setFieldValue(PSItemField itemField,
      PSFieldProperty fieldProperty,
      PSVariableEvaluator varEval,
      PSXPathEvaluator xpathEval)
      throws XPathException,
             IOException
   {
      if (itemField == null)
         throw new IllegalArgumentException("itemField may not be null");
      if (fieldProperty == null)
         throw new IllegalArgumentException("fieldProperty may not be null");
      if (varEval == null)
         throw new IllegalArgumentException("varEval may not be null");

      String valueExpression = fieldProperty.getValue();
      String type = fieldProperty.getValueType();

      if (type.equalsIgnoreCase(PSFieldProperty.VALUE_TYPE_VARIABLE))
      {
         itemField.addValue(varEval.evaluate(valueExpression));
      }
      else if (type.equalsIgnoreCase(PSFieldProperty.VALUE_TYPE_LITERAL) ||
               type.equalsIgnoreCase(PSFieldProperty.VALUE_TYPE_NUMBER))
      {
         itemField.addValue(new PSTextValue(valueExpression));
      }
      else if (type.equalsIgnoreCase(PSFieldProperty.VALUE_TYPE_DATE))
      {
         Date date = PSDataTypeConverter.parseStringToDate(valueExpression);
         if (date == null)
            date = new Date(System.currentTimeMillis());
         itemField.addValue(new PSDateValue(date));
      }
      else  // must be PSFieldProperty.VALUE_TYPE_XPATH
      {
         if (xpathEval == null)
            throw new IllegalStateException("xpathEval must not be null");

         String[] values = evaluateXPath(xpathEval, valueExpression);
         
         // Handle date values returned from XPATH evaluation
         PSItemFieldMeta meta = itemField.getItemFieldMeta();
         if (meta.getBackendDataType() == PSItemFieldMeta.DATATYPE_DATE)
         {
            Date date = PSDataTypeConverter.parseStringToDate(values[0]);
            itemField.addValue(new PSDateValue(date));
         }
         else
         {
            PSTextValue textValue = new PSTextValue(values[0]);
            itemField.addValue(textValue);
         }
      }
   }

   /**
    * Get a item field for the specified field, not a field in a
    * child item within the specified client item.
    *
    * @param clientItem The client item that contains the specified field. 
    *    It may not be <code>null</code>.
    *
    * @param fieldName The name of the specified field. It may not be
    *    <code>null</code> or empty.
    *
    * @return The specified item field. It never be <code>null</code>.
    *
    * @throws RuntimeException if the specified field does not exist in
    *    <code>clientItem</code>.
    */
   protected PSItemField getField(PSClientItem clientItem, String fieldName)
   {
      if (clientItem == null)
         throw new IllegalArgumentException("clientItem may not be null");
      if (fieldName == null || fieldName.trim().length() == 0)
         throw new IllegalArgumentException(
            "fieldName may not be null or empty");

      PSItemField itemField = clientItem.getFieldByName(fieldName);

      if (itemField == null)
      {
         String[] args = {fieldName, getExtractorDef().getName()};
         PSLoaderException exp = new PSLoaderException(
            IPSLoaderErrors.CANNOT_FIND_FIELD, args);

         throw new RuntimeException(exp.getLocalizedMessage());
      }

      return itemField;
   }

}
