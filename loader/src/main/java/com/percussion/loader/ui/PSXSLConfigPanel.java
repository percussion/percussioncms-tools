/*[ PSXSLConfigPanel.java ]****************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader.ui;


/**
 * Implements configuration panel that is specific for the XSL extractors.
 */
public class PSXSLConfigPanel extends PSItemExtractorConfigPanel
{

   /**
    * Creates a fully functioned content type tab panel, that allows XPath
    * expressions in the value map table
    *
    * @return The created tab panel, never <code>null</code>.
    */
   protected PSAbstractExtractorConfigTabPanel createContentTypeTabPanel()
   {
      return new PSContentTypeConfigTabPanel(true);
   }
   
   /**
    * Creates a field tab panel that is specific to the XML extractor.
    *
    * @return The created tab panel, never <code>null</code>.
    */
   protected PSAbstractExtractorConfigTabPanel createFieldTabPanel()
   {
      return new PSXSLFieldConfigTabPanel();
   }

}