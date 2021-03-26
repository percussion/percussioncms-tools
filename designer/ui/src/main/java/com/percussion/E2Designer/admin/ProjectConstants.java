/*[ ProjectConstants.java ]****************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer.admin;

import java.awt.*;

/**
 * The ProjectConstants provides serveral constants used within this project
 * only.
 */
////////////////////////////////////////////////////////////////////////////////
public class ProjectConstants
{
   /**
    * Constuctor.
    *
    */
  //////////////////////////////////////////////////////////////////////////////
   public ProjectConstants()
   {
   }

  //////////////////////////////////////////////////////////////////////////////
   /**
    * Applets width.
    */
  public static final int APPLET_WIDTH = 960;
   /**
    * Applets height.
    */
  public static final int APPLET_HEIGHT = 700;
   /**
    * Applets size (dimension).
    */
  public static final Dimension APPLET_SIZE = new Dimension(APPLET_WIDTH, APPLET_HEIGHT);

  //////////////////////////////////////////////////////////////////////////////
   /**
    * Applications width.
    */
  public static final int APPLICATION_WIDTH = APPLET_WIDTH;
   /**
    * Applications height.
    */
  public static final int APPLICATION_HEIGHT = APPLET_HEIGHT+20;
   /**
    * Applications size (dimension).
    */
  public static final Dimension APPLICATION_SIZE = new Dimension(APPLICATION_WIDTH, APPLICATION_HEIGHT);

  //////////////////////////////////////////////////////////////////////////////
   /**
    * Dialog width for SetPasswordDialog
    */
  public static final int PW_DIALOG_WIDTH = 340;
   /**
    * Dialog height for SetPasswordDialog
    */
  public static final int PW_DIALOG_HEIGHT = 140;
   /**
    * Dialog size (dimension) for SetPasswordDialog
    */
  public static final Dimension PW_DIALOG_SIZE = new Dimension(PW_DIALOG_WIDTH, PW_DIALOG_HEIGHT);

  //////////////////////////////////////////////////////////////////////////////
   /**
    * Preferred normal text box size.
    */
  public static final Dimension TEXT_BOX_SIZE = new Dimension(200, 20);
   /**
    * Preferred small text box size.
    */
  public static final Dimension TEXT_BOX_SIZE_SMALL = new Dimension(60, 20);

  //////////////////////////////////////////////////////////////////////////////
   /**
    * Default update interval in milliseconds.
    */
  public static final int DETAILS_UPDATE_INTERVAL = 5000;

  //////////////////////////////////////////////////////////////////////////////
   /**
    * Time after a server connection attempt times out.
    */
  public static final int CONNECTION_TIMEOUT = 20000;

  //////////////////////////////////////////////////////////////////////////////
   /**
    * Column index for status tables name.
    */
   public static final int STATUS_TABLE_NAME_INDEX = 0;
   /**
    * Column index for status tables type.
    */
  public static final int STATUS_TABLE_TYPE_INDEX = 1;
   /**
    * Column index for status tables status.
    */
  public static final int STATUS_TABLE_STATUS_INDEX = 2;
}
