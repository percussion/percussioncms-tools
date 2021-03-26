/*[ Cataloger.java ]***********************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer.browser;

import java.io.Serializable;
import java.util.Iterator;

/**
 * The base class for all cataloging classes.
 *
 * <p>
 * The derived class should set the Type value in its constructor.
 */
public abstract class Cataloger implements Serializable
{   
   /**
    *
    * @param Type a non-negative value
    *
    * @throws IllegalArgumentException if Type is not > 0
    */
   public Cataloger( int Type )
   {
      if (Type > 0)
         m_Type = Type;
      else
         throw new IllegalArgumentException( );
   }

   /**
    * Returns an iterator object for this particular cataloger.
    */
   public abstract Iterator iterator( );

   // variables
   protected int m_Type = -1;
}


