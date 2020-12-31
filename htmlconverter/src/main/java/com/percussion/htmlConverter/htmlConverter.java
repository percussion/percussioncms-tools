/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.htmlConverter;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.io.IOException;

import javax.swing.UIManager;

public class htmlConverter
{
   boolean packFrame = false;
   MainFrame frame = null;
   
   //Construct the application
   public htmlConverter(String[] args) throws SplitterException
   {
      boolean belongsToAnOtherApplication = false;
      if (args.length>=1)
      {
         String strTest = (String) args[1];
         belongsToAnOtherApplication = strTest.toLowerCase().equals("yes");
      }
      frame = new MainFrame(belongsToAnOtherApplication, true);
      
      //Validate frames that have preset sizes
      //Pack frames that have useful preferred size info, e.g. from their layout
      if (packFrame)
         frame.pack();
      else
         frame.validate();
      
      if (args.length>0)
      {
         initApplication(args[0]);
      }
      
      //Center the window
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      Dimension frameSize = frame.getSize();
      if (frameSize.height > screenSize.height)
         frameSize.height = screenSize.height;
      if (frameSize.width > screenSize.width)
         frameSize.width = screenSize.width;
      frame.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
      frame.setVisible(true);
   }
   
   public void setVisible(String filePath)
   {
      initApplication(filePath);
      
      frame.setVisible (true);
      frame.setState (Frame.NORMAL);
      frame.toFront ();
   }
   
   private void initApplication(String filePath)
   {
      try
      {
         frame.loadSourceFile(filePath);
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
   }
   
   //Main method
   public static void main(String[] args)
   {
      try
      {
         UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
         new htmlConverter(args);
      }
      catch (Exception e)
      {
         System.out.println(
            "Could not initialize splitter for the following reason:\n" + 
            e.getLocalizedMessage());
      }
   }
}

