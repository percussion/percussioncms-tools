/*[ PSBinaryConfigPanel.java ]****************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader.ui;


/**
 * Implements configuration panel that is specific for the binary extractors.
 */
public class PSBinaryConfigPanel extends PSItemExtractorConfigPanel
{
   /**
    * Creates a field tab panel that is specific to the binary extractor.
    *
    * @return The created tab panel, never <code>null</code>.
    */
   protected PSAbstractExtractorConfigTabPanel createFieldTabPanel()
   {
      return new PSFieldConfigTabPanel(false);
   }

}