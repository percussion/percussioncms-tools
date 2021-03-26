/*[ PSBinaryExtractor.java ]***************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader.extractor;

import com.percussion.cms.objectstore.PSItemField;
import com.percussion.cms.objectstore.ws.PSClientItem;
import com.percussion.loader.PSItemContext;
import com.percussion.loader.objectstore.PSFieldProperty;
import com.percussion.loader.ui.PSBinaryConfigPanel;
import com.percussion.loader.ui.PSConfigPanel;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

/**
 * Binary Extractor which will be able to extract binary content from the
 * resource data, along with other specified fields.
 */
public class PSBinaryExtractor extends PSItemExtractor
{
   // Implements the IPSUIPlugin interface
   public PSConfigPanel getConfigurationUI()
   {
      return new PSBinaryConfigPanel();
   }

   // Implements IPSItemExtractor.extractItems(PSItemContext, InputStream)
   public PSItemContext[] extractItems(PSItemContext resource, InputStream in)
      throws IOException
   {
      // setup evaluator
      PSVariableEvaluator varEval =
         new PSVariableEvaluator(resource, true, in);

      // process field one at a time
      PSClientItem clientItem = resource.getItem();
      Iterator fieldProperties = getExtractorDef().getFieldProperties();
      while (fieldProperties.hasNext())
      {
         PSFieldProperty prop = (PSFieldProperty) fieldProperties.next();
         PSItemField itemField = getField(clientItem, prop.getName());
         setFieldValue(itemField, prop, varEval);
      }

      PSItemContext[] items = new PSItemContext[1];
      items[0] = resource;

      return items;
   }

}