/*[ SelectorColumnComboBox.java ]**********************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer;

import javax.swing.*;
import java.util.Vector;

/**
 * Create a combo box providing all possible selector column choices.
 */
////////////////////////////////////////////////////////////////////////////////
public class SelectorColumnComboBox extends JComboBox
{
   /**
   * Construct an empty combo box.
   *
    */
  //////////////////////////////////////////////////////////////////////////////
   public SelectorColumnComboBox()
   {
      // allow direct editing
    this.setEditable(true);
   }

   /**
   * Set the combobox entries.
   *
    */
  //////////////////////////////////////////////////////////////////////////////
   public void setContents(Vector columns)
   {
     this.removeAll();
    for (int col=0; col<columns.size(); col++)
       this.addItem(columns.elementAt(col));
   }
}
