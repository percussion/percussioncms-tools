/*[ SelectorOperatorComboBox.java ]********************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer;

import javax.swing.*;

/**
 * Create a combo box providing all possible operator choices.
 */
////////////////////////////////////////////////////////////////////////////////
public class SelectorOperatorComboBox extends JComboBox
{
   public SelectorOperatorComboBox()
   {
      this.addItem("=");
      this.addItem(">");
      this.addItem(">=");
      this.addItem("<");
      this.addItem("<=");
      this.addItem("!=");
   }
}
 
