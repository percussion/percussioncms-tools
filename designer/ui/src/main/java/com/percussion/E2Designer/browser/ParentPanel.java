/*[ ParentPanel.java ]**************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer.browser;

import javax.swing.*;

//Note: may be modified to suit the need

/**
 * A generic panel which can be subclassed by classes wishing to save, load and
 * validate data.
 */
public class ParentPanel extends JPanel implements IPSPersistableInput
{
   /**
    * To be overridden by the subclass. Validates the data in the panel.
    */
   public boolean validateData()
   {
      return true;
   }

   /**
    * To be overridden by the subclass. Saves the data in the panel.
    */
   public boolean save()
   {
      return true;
   }

   /**
    * To be overridden by the subclass. Loads the data in the panel.
    * @param data, data to be loaded by the panel.
    */
   public void load(Object data)
   {
   }
}