/*[ ITabDataHelper.java ]******************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer.admin;

/** All tabs and sub-tabs in AppletMainDialog of the admin client are required
  * to implement this interface to save the data within the tabs.
*/
public interface ITabDataHelper
{
/** Allows the Tab panel containers to save the data in each tab into the
  * ServerConfiguration object.
  *
  * @returns boolean This is returned to acknowledge that the panel implementing
  * ITabDataHelper has been updated. <CODE>false</CODE> means that nothing has
  * changed.
*/
  public boolean saveTabData();

/** Allows the Tab panel containers to validate its data. Have this method 
  * return <CODE>false</CODE> if the validation failed. If the panel has no
  * validation, simple implement by returning <CODE>true</CODE>.
*/
  public boolean validateTabData();
} 
