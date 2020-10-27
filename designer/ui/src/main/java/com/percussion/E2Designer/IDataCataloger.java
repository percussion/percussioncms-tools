/******************************************************************************
 *
 * [ IDataCataloger.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.E2Designer;


/**
 * This interface allows the cataloging of data contained in the implementing
 * object. Because some objects can contain more of one type of data,
 * csObjecType indicates the type. Edit enumeration to extend functionality for
 * future catalog-able data types.
 */
public interface IDataCataloger
{
  /**
   * This follows a "shopping basket" model. A container is passes to whoever
   * uses this method and the implementor simply adds the data into the
   * objContainer.  Since objContainer is an extension of HashMap, all
   * duplications of data are instantly destroyed.
   *
   * @param iObjType this argument describes the type of data requested
   * from the object. For example, ObjectType.HTML_PARAM.
   * @param objContainer Our shopping basket to &quot;add&quot; the cataloged
   * objects.
   * @see CatalogReceiver
   */
  public void catalogData(ObjectType iObjType, CatalogReceiver objContainer);

  /**
   * Type of data requested from the object.
   */
  public enum ObjectType
  {
     /** The request type of HTML parameter. */
     HTML_PARAM,

     /** The request type for cataloging DTDs. */
     XML_DTD,
     
     /** The request type for cataloging UDFs */
     UDF
  };
} 
