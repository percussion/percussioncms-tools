/*[ PSItemTransformer.java ]***************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader;

import com.percussion.loader.ui.IPSUIPlugin;
import com.percussion.loader.ui.PSConfigPanel;
import com.percussion.loader.ui.PSTransformationEditorPanel;

import org.w3c.dom.Element;

/**
 * Sample class that implements the <code>IPSItemTransformer</code>
 */
public class PSItemTransformerTest implements IPSUIPlugin, IPSItemTransformer
{
   // Implements IPSPlugin.configure(Element)
   public void configure(Element config) throws PSConfigurationException
   {
   }

   //implements the IPSUIPlugin interface
   public PSConfigPanel getConfigurationUI()
   {
      return new PSTransformationEditorPanel(true);
   }

   /**
    * implements the IPSUIPlugin interface
    */
   public PSItemContext transform(Object[] params, PSItemContext item)
       throws PSParameterValidationException, PSTransformationException,
         PSExcludeException
   {
      return item;
   }


}