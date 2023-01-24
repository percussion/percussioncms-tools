/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

