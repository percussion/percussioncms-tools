/*[ UTBooleanComboBox.java ]***************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer;

import com.percussion.design.objectstore.PSConditional;

/**
 * Create a combo box providing the generic boolean table operators.
 */
////////////////////////////////////////////////////////////////////////////////
public class UTBooleanComboBox extends PSComboBox
{
   public UTBooleanComboBox()
   {
    this.setEditable(false);

    this.addItem("");
      this.addItem(PSConditional.OPBOOL_AND);
      this.addItem(PSConditional.OPBOOL_OR);
   }
}
