/*[ ExtendedTypes.java ]*******************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer;
import java.util.Vector;

/**
*this class is a container of exteded types, (such as  ExtendedTypesUniqueKeys)
*
*/

public class ExtendedTypes
{
  /**
  * default constructor of ExtendedTypes
  *
  */
  public ExtendedTypes()
  {

  }
  /**
  * removes all the stored extended types
  *
  *
  */
  public  void removeAllElements()
  {
    if(m_UniqueKeys != null )
    {
       m_UniqueKeys.removeAllElements();
    }

    if( m_PrimaryKeys !=null )
    {
       m_PrimaryKeys.removeAllElements();
    }
    if( m_foreignKeys !=null )
    {
       m_foreignKeys.removeAllElements();
    }
  }

  /**
  * return the extended catalog types
  *
  * @param keyType indicates the type of cataloger to construct, if not one of this ID's
  * <table BORDER COLS=1 WIDTH="50%" >
  *  <tr>
  *  <td>FOREIGN_KEY</td>
  *  <td>PRIMARY_KEY</td>
  *  <td>UNIQUE_KEY</td>
  *  </tr>
  *  </table>
  *  an IllegalArgumentException is thrown.
  *
  * @throws IllegalArgumentException if keyType is invalid
  */
  public  Vector  getExtendedTypes(int keyType)
  {
    switch(keyType)
    {
    case FOREIGN_KEY: return(m_foreignKeys);
    case PRIMARY_KEY: return(m_PrimaryKeys);
    case UNIQUE_KEY:  return(m_UniqueKeys);
    }
    // if we reached here throw
    throw new IllegalArgumentException();
  }
  /**
  *adds the ExtendedTypePrimaryKey into the vector
  *
  * @param primary key to be added into the vector
  *
  */
  public void addType(ExtendedTypesPrimaryKey primary)
  {
       if(m_PrimaryKeys == null )
       {
            m_PrimaryKeys=new Vector(2);
       }
       ExtendedTypesPrimaryKey obj=new ExtendedTypesPrimaryKey();
       obj.copyFrom(primary);
       m_PrimaryKeys.add(obj);
  }

 /**
 * adds a ExtendedTypesForeignKey into the list
 *
 * @param foreignKey to be added to the list
 *
 *
 */
  public void addType(ExtendedTypesForeignKey foreignKey)
  {
     if( m_foreignKeys == null )
     {
        m_foreignKeys= new Vector(2);
     }
     ExtendedTypesForeignKey obj= new ExtendedTypesForeignKey();
     obj.copyFrom(foreignKey);
     m_foreignKeys.add(obj);
  }
 /**
 * adds a ExtendedTypesUniqueKeys  into the list
 *
 * @param uniqueKeys to be added to the list
 *
 *
 */

  public void addType(ExtendedTypesUniqueKeys uniqueKeys)
  {
    if( m_UniqueKeys == null )
    {
       m_UniqueKeys= new Vector(2);
    }
    ExtendedTypesUniqueKeys obj= new ExtendedTypesUniqueKeys();
    obj.copyFrom(uniqueKeys);
    m_UniqueKeys.add(obj);
  }
  /**
  * store of unique keys
  */
  private  Vector m_UniqueKeys=null;
  /**
  * store of primary keys
  */
  private  Vector m_PrimaryKeys=null;
  /**
  * store of foreign keys
  */
  private  Vector m_foreignKeys=null;
  /**
  *catalog foreign keys
  */
  private static final int FOREIGN_KEY=1;
   /**
  *catalog primary keys
  */
  private static final int PRIMARY_KEY=2;
  /**
  *catalog unique keys
  */
  private static final int UNIQUE_KEY=3;


}
