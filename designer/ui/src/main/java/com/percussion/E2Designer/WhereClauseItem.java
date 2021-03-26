/******************************************************************************
 *
 * [ WhereClauseItem.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.E2Designer;



/** A container object for WHERE clause data that is used to pass the data
  * to/from SelectorPropertyDialog and FlexibleQueryBuilderDialog.  Each item
  * will hold the WHERE clause&apos;s column name, operator, value, and the
  * conditional boolean (AND/OR).  These items will most likely be passed in
  * an array of WhereClauseItems.
*/

public class WhereClauseItem
{

//
// CONSTRUCTORS
//
  
  public WhereClauseItem()
  {}

  public WhereClauseItem(String columnName, String op, String value, boolean isOr)
  {
    this.columnName = columnName;
    this.m_op = op;
    this.value = value;
    this.m_isOr = isOr;
  }

// PUBLIC METHODS
  
  /**
   * Checks object equality. Overrides {@link Object#equals(java.lang.Object)}.
   */
  @Override
  public boolean equals(final Object obj)
  {
     if (obj != null && obj instanceof WhereClauseItem)
     {
        return equals((WhereClauseItem) obj);
     }
     else
     {
        return false;
     }
  }

  public boolean equals(WhereClauseItem item)
  {
    if (columnName.equals(item.getColumnName()))
      if (m_op.equals(item.getOperator()))
        if (value.equals(item.getValue()))
          if (m_isOr == item.isOr())
            return true;

    return false;
  }
  
  /**
   * Generates object hash code. Overrides {@link Object#hashCode().
   */
  @Override
  public int hashCode()
  {
     throw new UnsupportedOperationException("Not Implemented");
  }

  public String getColumnName()
  {
    return columnName;
  }

  public void setColumnName(String name)
  {
    columnName = name;
  }

  public String getOperator()
  {
    return m_op;
  }

  public void setOperator(String op)
  {
    this.m_op = op;
  }

  public String getValue()
  {
    return value;
  }

  public void setValue(String value)
  {
    this.value = value;
  }

  public boolean isOr()
  {
    return m_isOr;
  }

  public void setOr(boolean isOr)
  {
    this.m_isOr = isOr;
  }

//
// MEMBER VARIABLES
//

  private String columnName;
  private String m_op;
  private String value;
  private boolean m_isOr;
}

