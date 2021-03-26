/*[ IFigureLabelHelper.java ]**************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer;

import java.io.Serializable;

/** This abstract class is used between the UIFigure and the
  * AppFigureFactory/PipeFigureFactory for filtering data in each specified
  * OS? class to be displayed as a String label.
  *
  * @see AppFigureFactory
  * @see PipeFigureFactory
  * @see UIFigure
*/

public abstract class IFigureLabelHelper implements Serializable
{

/** Filters the data object passed in and outputs specific information in string
  * format.
  *
  * @param data The GUI data object (eg: OSDataset) to be filtered.
  * @returns String The filtered data for display in the figure label. 
*/
  public abstract String getLabelText(Object data);


/** Filters the data object passed in and outputs specific information in string
  * format.
  *
  * @param data The GUI data object (eg: OSDataset) to be filtered.
  * @returns String The filtered data for display in the figure tool tip. 
*/
  public abstract String getToolTipText(Object data);
} 
