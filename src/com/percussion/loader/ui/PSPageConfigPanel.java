/*[ PSPageConfigPanel.java ]****************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader.ui;


/**
 * Implements configuration panel that is specific for the page extractors.
 */
public class PSPageConfigPanel extends PSItemExtractorConfigPanel
{
   /**
    * Creates a field tab panel that is specific to the page extractor.
    *
    * @return The created tab panel, never <code>null</code>.
    */
   protected PSAbstractExtractorConfigTabPanel createFieldTabPanel()
   {
      return new PSPageFieldConfigTabPanel();
   }


}