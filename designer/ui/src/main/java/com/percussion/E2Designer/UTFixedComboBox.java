/*[ UTFixedComboBox.java ]*****************************************************
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
import java.util.Vector;

/**
 * A fixed sized JComboBox.
 */
////////////////////////////////////////////////////////////////////////////////
public class UTFixedComboBox extends JComboBox
{
   /**
   * Construct a new fixed combo box with the standard size.
   */
   public UTFixedComboBox()
   {
      super();
      setPreferredSize(STANDARD_COMBOBOX_SIZE);
      setBorder(new BevelBorder(BevelBorder.LOWERED, Color.gray, Color.darkGray));
   }

   /**
   * Construct a new combo box of passed size.
   *
   * @param size         the text field size
   */
   public UTFixedComboBox(Dimension size)
   {
      super();
      setPreferredSize(size);
      setBorder(new BevelBorder(BevelBorder.LOWERED, Color.gray, Color.darkGray));
   }

   /**
   * Construct a new fixed combo box of passed width/height.
   *
   * @param width         the text field width
   * @param height      the text field height
   */
   public UTFixedComboBox(int width, int height)
   {
      super();
      setPreferredSize(new Dimension(width, height));
      setBorder(new BevelBorder(BevelBorder.LOWERED, Color.gray, Color.darkGray));
   }

   /**
   * Construct a new fixed combo box with the standard size.
   *
   * @param vItemList   the list of items in this combo box.
   */
   public UTFixedComboBox(Vector vItemList)
   {
      this(vItemList.toArray());
   }

   /**
   * Construct a new fixed combo box with the standard size.
   *
   * @param vItemList   the list of items in this combo box.
   */
   public UTFixedComboBox(Object[] itemList)
   {
      super(itemList);
      setPreferredSize(STANDARD_COMBOBOX_SIZE);
      setBorder(new BevelBorder(BevelBorder.LOWERED, Color.gray, Color.darkGray));
   }

   /**
   * Construct a new combo box of passed dimension and passed list of items.
   *
   * @param itemList   the list of items in this combo box.
   * @param size         the text field size.
   */
   public UTFixedComboBox(Object[] itemList, Dimension size)
   {
      super(itemList);
      setPreferredSize(size);
      setBorder(new BevelBorder(BevelBorder.LOWERED, Color.gray, Color.darkGray));
   }

   /**
   * Construct a new combo box of passed width/height and passed list of items.
   *
   * @param itemList   the list of items in this combo box.
   * @param width         the text field width.
   * @param height      the text field HEIGHT.
   */
   public UTFixedComboBox(Object[] itemList, int width, int height)
   {
      super(itemList);
      setPreferredSize(new Dimension(width, height));
      setBorder(new BevelBorder(BevelBorder.LOWERED, Color.gray, Color.darkGray));
   }

   /**
   * Construct a new combo box of passed dimension and passed list of items.
   *
   * @param vItemList   the list of items in this combo box.
   * @param size         the text field size.
   */
   public UTFixedComboBox(Vector vItemList, Dimension size)
   {
      this(vItemList.toArray(), size);
   }

   /**
   * Construct a new combo box of passed width/height and passed list of items.
   *
   * @param vItemList   the list of items in this combo box.
   * @param width         the text field width.
   * @param height      the text field HEIGHT.
   */
   public UTFixedComboBox(Vector vItemList, int width, int height)
   {
      this(vItemList.toArray(), width, height);
   }

   /**
   * Make size fix.
   *
    */
  //////////////////////////////////////////////////////////////////////////////
   public Dimension getMinimumSize()
   {
      return getPreferredSize();
   }

   /**
   * Make size fix.
   *
    */
  //////////////////////////////////////////////////////////////////////////////
   public Dimension getMaximumSize()
   {
      return getPreferredSize();
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

  //////////////////////////////////////////////////////////////////////////////
   /**
   * the standard text field size
    */
  private static final Dimension STANDARD_COMBOBOX_SIZE = new Dimension(200, 20);
}
