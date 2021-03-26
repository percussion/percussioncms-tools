/*[ OSUdfConstants.java ]***************************************
 * COPYRIGHT (c) 2000 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted 
 * work including confidential and proprietary information of Percussion.
 *
 * $ID$
 *
 * Version Labels  : $Name: Pre_CEEditorUI RX_40_REL 20010618_3_5 $
 *
 * Locked By      : $Locker: $
 *
 * Revision History:
 *
 *      $Log: OSUdfConstants.java $
 *      Revision 1.3  2001/05/03 21:02:32Z  JaySeletz
 *      Removed the functionality to enable preinstalled udfs.
 *      Revision 1.2  2000/06/28 07:58:35Z  paulhoward
 *      Added constant for unknown.
 *      
 *      Revision 1.1  2000/06/28 00:25:05Z  AlexTeng
 *      Initial revision
 *
 ***************************************************************************/
package com.percussion.E2Designer;

/**
 * A class that provides a central place for all the UDF constants used
 * throughout the designer. This class cannot be instantiated.
 */
public class OSUdfConstants
{
   /** Hide the constuctor since we do not want to instantiate this class. */
   private OSUdfConstants() { }

   /** Flag that indicates the UDF type has not been or can't be determined. */
   public static final int UDF_UNKNOWN = -1;

   /** Flag for specifying the Application UDF collection. */
   public static final int UDF_APP = 0;

   /** Flag for specifying the Global UDF collection. */
   public static final int UDF_GLOBAL = 1;
}