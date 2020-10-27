/*[ GenericValidator.java ]****************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer;

/**
 * This class will serve as utility class to  do the generic dataentry
 * validation.
 */
public class GenericValidator 
{

   /**
    * this funciton only allows visible char string in a row, not allow space been
    * contained.
    *
    * @return true if empty char inside, otherwise false
    */
   public static boolean isEmptyCharContained( String str )
   {
      System.out.println( "The content is:"+str );
      if(str == null)
         return true;
      else if(str.indexOf( " " )!=-1)
      {
         System.out.println( "Empty Char inside" );
         return true;
      }
      else
      {
         System.out.println( "here is the sucess case" );
         return false;
      }  
   }
}


