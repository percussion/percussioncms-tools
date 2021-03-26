/*[ IPSProgressListener.java ]**************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader;



/**
 * The listener interface for receiving progress event. 
 */
public interface IPSProgressListener extends IPSStatusListener
{
   /**
    * Invoked after a progress event is fired.
    * 
    * @param e The fired progress event, which contains the current progress of 
    *    the source
    */
   void progressChanged(PSProgressEvent e);
}
