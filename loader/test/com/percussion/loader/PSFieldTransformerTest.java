/******************************************************************************
 *
 * [ PSFieldTransformerTest.java ]
 *
 * COPYRIGHT (c) 1999 - 2004 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader;

import com.percussion.cms.objectstore.PSItemField;
import com.percussion.cms.objectstore.PSTextValue;
import com.percussion.loader.ui.IPSUIPlugin;
import com.percussion.loader.ui.PSConfigPanel;
import com.percussion.loader.ui.PSTransformationEditorPanel;

import org.w3c.dom.Element;

/**
 * This class is designed to wrap a specified element to the value of the
 * specified field. It is expecting the field value is in <code>String</code>.
 * <p>
 * An example to specify this class as a local field transformer (within an
 * extractor):
 * <pre>
 *    <PSXExtractorDef ...>
 *       ...
 *       <Filters>
 *          ...
 *       </Filters>
 *       <PSXFieldTransformationsDef>
 *          <PSXFieldTransformationDef name="Content transformer"
 *                class="com.percussion.loader.PSFieldTransformerTest"
 *                targetField="bodycontent">
 *             <ParamDefs>
 *                <PSXParamDef name="tagname" type="java.lang.String">
 *                   <Description>tag name to wrap the field value</Description>
 *                   <Value>div</Value>
 *                </PSXParamDef>
 *             </ParamDefs>
 *          </PSXFieldTransformationDef>
 *       </PSXFieldTransformationsDef>
 *       <PSXWorkflowDef ...>
 *          ...
 *       </<PSXWorkflowDef>
 *    </PSXExtractorDef>
 * </pre>
 */
public class PSFieldTransformerTest implements IPSFieldTransformer,
      IPSUIPlugin
{
   // Implements IPSPlugin.configure(Element)
   public void configure(Element config) throws PSConfigurationException
   {
   }


   //implements the IPSUIPlugin interface
   public PSConfigPanel getConfigurationUI()
   {
      return new PSTransformationEditorPanel(false);
   }

   ////implements the IPSUIPlugin interface
   public void transform(Object[] params, IPSTransformContext info)
      throws PSParameterValidationException, PSTransformationException
   {
      PSItemField field = info.getItemField();

      if (params.length < 1)
         throw new PSParameterValidationException(
            IPSLoaderErrors.UNEXPECTED_ERROR,
            "Expecting a parameter for the tag name");

      if (! (params[0] instanceof String))
         throw new PSParameterValidationException(
            IPSLoaderErrors.UNEXPECTED_ERROR,
            "Expecting a String type for the tag name as the first parameter");

      if (! (field.getValue() instanceof PSTextValue))
         throw new PSParameterValidationException(
            IPSLoaderErrors.UNEXPECTED_ERROR,
            "Expecting PSTextValue instance, but not " +
            field.getValue().getClass().getName());

      String tagname = (String) params[0];
      PSTextValue fieldValue = (PSTextValue) field.getValue();
      String value = "<"  + tagname + ">" + fieldValue.getValueAsString() +
                     "</" + tagname + ">";
      fieldValue = new PSTextValue(value);
      field.addValue(fieldValue);
   }
}