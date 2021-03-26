/*[ UTOperatorComboBox.java ]**************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer;

import com.percussion.design.objectstore.PSConditional;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Create a combo box providing the generic table operators.
 */
////////////////////////////////////////////////////////////////////////////////
public class UTOperatorComboBox extends PSComboBox
{
   public UTOperatorComboBox()
   {
    this.setEditable(false);

    this.addItem("");
    this.addItem(getResources().getString("equals"));
    this.addItem(getResources().getString("notEquals"));
    this.addItem(getResources().getString("lessThan"));
    this.addItem(getResources().getString("lessThanOrEquals"));
    this.addItem(getResources().getString("greaterThan"));
    this.addItem(getResources().getString("greaterThanOrEquals"));
    this.addItem(getResources().getString("isNull"));
    this.addItem(getResources().getString("isNotNull"));
    this.addItem(getResources().getString("between"));
    this.addItem(getResources().getString("notBetween"));
    this.addItem(getResources().getString("in"));
    this.addItem(getResources().getString("notIn"));
    this.addItem(getResources().getString("like"));
    this.addItem(getResources().getString("notLike"));
   }

   /**
   * Overwritten to translate from display text to internal text.
   *
   * @param index the item index
   * @return the indexed item
   */
  //////////////////////////////////////////////////////////////////////////////
  public Object getItemAt(int index)
  {
    String original = (String) super.getItemAt(index);

    return translateToInternal(original);
  }

   /**
   * Overwritten to translate from display text to internal text.
   *
   * @return the selected item
   */
  //////////////////////////////////////////////////////////////////////////////
  public Object getSelectedItem()
  {
    String original = (String) super.getSelectedItem();

    return translateToInternal(original);
  }

   /**
   * Overwritten to translate from display text to internal text.
   *
   * @return the selected items
   */
  //////////////////////////////////////////////////////////////////////////////
  public Object[] getSelectedObjects()
  {
    String[] originals = (String[]) super.getSelectedObjects();

    for (int i=0, n=originals.length; i<n; i++)
      translateToInternal(originals[i]);

    return originals;
  }

   /**
   * Overwritten to translate from internal to display text.
   *
   * @param object the object to select
   */
  //////////////////////////////////////////////////////////////////////////////
  public void setSelectedItem(Object object)
  {
    super.setSelectedItem(translateToDisplay((String) object));
  }

   /**
   * This translates the provided internal string to its display representation.
   *
   * @param original the string as displayed
   * @return String the internal representation of the original string
   */
  //////////////////////////////////////////////////////////////////////////////
  public String translateToDisplay(String original)
  {
    if (original.equals(PSConditional.OPTYPE_EQUALS))
      return getResources().getString("equals");
    if (original.equals(PSConditional.OPTYPE_NOTEQUALS))
      return getResources().getString("notEquals");
    if (original.equals(PSConditional.OPTYPE_LESSTHAN))
      return getResources().getString("lessThan");
    if (original.equals(PSConditional.OPTYPE_LESSTHANOREQUALS))
      return getResources().getString("lessThanOrEquals");
    if (original.equals(PSConditional.OPTYPE_GREATERTHAN))
      return getResources().getString("greaterThan");
    if (original.equals(PSConditional.OPTYPE_GREATERTHANOREQUALS))
      return getResources().getString("greaterThanOrEquals");
    if (original.equals(PSConditional.OPTYPE_ISNULL))
      return getResources().getString("isNull");
    if (original.equals(PSConditional.OPTYPE_ISNOTNULL))
      return getResources().getString("isNotNull");
    if (original.equals(PSConditional.OPTYPE_BETWEEN))
      return getResources().getString("between");
    if (original.equals(PSConditional.OPTYPE_NOTBETWEEN))
      return getResources().getString("notBetween");
    if (original.equals(PSConditional.OPTYPE_IN))
      return getResources().getString("in");
    if (original.equals(PSConditional.OPTYPE_NOTIN))
      return getResources().getString("notIn");
    if (original.equals(PSConditional.OPTYPE_LIKE))
      return getResources().getString("like");
    if (original.equals(PSConditional.OPTYPE_NOTLIKE))
      return getResources().getString("notLike");

    return original;
  }

   /**
   * This translates the provided display string to the internal representation.
   *
   * @param original the string as displayed
   * @return String the internal representation of the original string
   */
  //////////////////////////////////////////////////////////////////////////////
  private String translateToInternal(String original)
  {
    if (original.equals(getResources().getString("equals")))
      return PSConditional.OPTYPE_EQUALS;
    if (original.equals(getResources().getString("notEquals")))
      return PSConditional.OPTYPE_NOTEQUALS;
    if (original.equals(getResources().getString("lessThan")))
      return PSConditional.OPTYPE_LESSTHAN;
    if (original.equals(getResources().getString("lessThanOrEquals")))
      return PSConditional.OPTYPE_LESSTHANOREQUALS;
    if (original.equals(getResources().getString("greaterThan")))
      return PSConditional.OPTYPE_GREATERTHAN;
    if (original.equals(getResources().getString("greaterThanOrEquals")))
      return PSConditional.OPTYPE_GREATERTHANOREQUALS;
    if (original.equals(getResources().getString("isNull")))
      return PSConditional.OPTYPE_ISNULL;
    if (original.equals(getResources().getString("isNotNull")))
      return PSConditional.OPTYPE_ISNOTNULL;
    if (original.equals(getResources().getString("between")))
      return PSConditional.OPTYPE_BETWEEN;
    if (original.equals(getResources().getString("notBetween")))
      return PSConditional.OPTYPE_NOTBETWEEN;
    if (original.equals(getResources().getString("in")))
      return PSConditional.OPTYPE_IN;
    if (original.equals(getResources().getString("notIn")))
      return PSConditional.OPTYPE_NOTIN;
    if (original.equals(getResources().getString("like")))
      return PSConditional.OPTYPE_LIKE;
    if (original.equals(getResources().getString("notLike")))
      return PSConditional.OPTYPE_NOTLIKE;

    return original;
  }

   /**
   * Accessor to the resources of this class.
   */
  //////////////////////////////////////////////////////////////////////////////
  private ResourceBundle m_res = null;
  protected ResourceBundle getResources()
  {
      try
    {
      if (m_res == null)
          m_res = ResourceBundle.getBundle(getClass().getName() + "Resources",
                                                           Locale.getDefault());
    }
    catch (MissingResourceException e)
      {
         System.out.println(e);
      }

       return m_res;
    }
}

