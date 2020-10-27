/*[ FigureCreationException.java ]*********************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer;


/**
 * This exception is thrown if a figure factory fails for any reason while
 * trying to create the requested valid figure.
 */
public class FigureCreationException extends Exception
{
   public FigureCreationException()
   {
      super();
   }

   public FigureCreationException( String strDetail )
   {
      super( strDetail );
   }
}
