/*[ ITabPaneRetriever.java ]***************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer.admin;

import javax.swing.*;

/** A convience interface to helper retrieve the sub-tabbedPane from the outer
  * tabbedPane.
*/

public interface ITabPaneRetriever
{
/** @returns JTabbedPane The tab pane within the implementing class.
*/
  public JTabbedPane getTabbedPane();

} 
