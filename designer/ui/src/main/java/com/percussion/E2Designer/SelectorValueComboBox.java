/*[ SelectorValueComboBox.java ]***********************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer;

import javax.swing.*;

/**
 * Create a combo box providing all possible value choices.
 */
////////////////////////////////////////////////////////////////////////////////
public class SelectorValueComboBox extends JComboBox
{
   public SelectorValueComboBox()
   {
    this.setEditable(true);
    for (int i=0; i<20; i++)
         this.addItem("param" + i + ".partno");
   }
}
 
