/*[ PageableAndPrintable.java ]************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer;

import java.awt.*;
import java.awt.print.Printable;

public interface PageableAndPrintable extends Printable
{
   public void setPrintLocation(Point pt);
   public Point getPrintLocation();
}   
