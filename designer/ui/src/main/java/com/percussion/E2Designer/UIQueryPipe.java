/*[ UIQueryPipe.java ]*********************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer;


/**
 * Same as its super class, but it has knowledge about dependencies between
 * different objects that attach to it. When certain objects attach, property
 * listeners and vetoable property listeners will be added as needed. It also
 * has knowledge of its image.
 */
public class UIQueryPipe extends UIPipe
{
   public UIQueryPipe( )
   {
   }


   protected int getPipeImageExtent( )
   {
      return 0;
   }

}

