/*[ PSComboBox.java ]**********************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;

/** A little JComboBox wrapper class to intialize the combobox border with
  * a LOWERED BevelBorder.
*/

public class PSComboBox extends JComboBox
{

  public PSComboBox()
  {
     super();
     setBorder(new BevelBorder(BevelBorder.LOWERED,
                               Color.white,
                               Color.lightGray,
                               Color.black,
                               Color.darkGray));
  }

  public PSComboBox(javax.swing.ComboBoxModel model)
  {
     super(model);
     setBorder(new BevelBorder(BevelBorder.LOWERED,
                               Color.white,
                               Color.lightGray,
                               Color.black,
                               Color.darkGray));
  }

  public PSComboBox(Object[] array)
  {
     super(array);
     setBorder(new BevelBorder(BevelBorder.LOWERED,
                               Color.white,
                               Color.lightGray,
                               Color.black,
                               Color.darkGray));
  }

  public PSComboBox(java.util.Vector vector)
  {
     super(vector);
     setBorder(new BevelBorder(BevelBorder.LOWERED,
                               Color.white,
                               Color.lightGray,
                               Color.black,
                               Color.darkGray));
  }

  public PSComboBox(Dimension size)
  {
    this();
    setPreferredSize(size);
    setMaximumSize(this.getPreferredSize());
    setMinimumSize(this.getPreferredSize());
  }

   /** 
    * The general contract of this is not to work properly if a model has
    * been set. It appears to throw an IndexOutOfBoundsException.
    */
   public void removeAllItems()
   {
      // if there are no items, nothing to remove
        if (getItemCount() == 0)
         return;
      super.removeAllItems();
   }
}

