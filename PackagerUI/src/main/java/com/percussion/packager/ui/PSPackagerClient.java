/******************************************************************************
 *
 * [ PSPackagerClient.java ]
 * 
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.packager.ui;

import com.percussion.deployer.client.IPSDeployConstants;
import com.percussion.guitools.ErrorDialogs;
import com.percussion.tools.help.PSJavaHelp;
import org.jvnet.substance.skin.SubstanceModerateLookAndFeel;

import javax.swing.*;




/**
 * @author erikserating
 *
 */
public class PSPackagerClient
{

   /**
    * @param args
    */
   public static void main(String[] args)
   {
      try
      {
           
         if(ms_frame != null)
            return; // Can only have one local instance of the client.


         //Make sure we run in the Swing Ui Thread
         SwingUtilities.invokeLater(new Runnable()
         {

            public void run()
            {
                try {
                    UIManager.setLookAndFeel(new SubstanceModerateLookAndFeel());
                    ms_frame = new PSPackagerMainFrame();
                    ms_errDlg = new ErrorDialogs(ms_frame);
                    ms_help = PSJavaHelp.getInstance();

                    ms_help.setHelpSet(
                            PSJavaHelp.getHelpSetURL(IPSDeployConstants.HELPSET_FILE),
                            "com.percussion.deployer.ui.helptopicmapping");
                    ms_frame.setVisible(true);
                    ms_frame.initialServerConnect();
                }catch(Exception e){
                    e.printStackTrace();
                    System.out.println( "Unexpected exception in main" );
                    ErrorDialogs.FatalError(e.getLocalizedMessage());
                }
            }
         });
         
      }      
      catch (Throwable e)
      {
         e.printStackTrace();
         // we're on our way out, so notify user
         System.out.println( "Unexpected exception in main" );
         ErrorDialogs.FatalError(e.getLocalizedMessage());
      }

   }
   
   /**
    * Gets the object that can be used to display different kind of error
    * messages to the user with the main frame of this application as parent of
    * the error dialog.
    *
    * @return the error dialogs object, may be <code>null</code> if it is called
    * before it was constructed in <code>main(String[])</code> method.
    */
   public static ErrorDialogs getErrorDialog()
   {
      return ms_errDlg;
   }

   /**
    * Gets the main frame of this application.
    *
    * @return the frame may be <code>null</code> if this method is called before
    * the main frame is constructed.
    */
   public static PSPackagerMainFrame getFrame()
   {
      return ms_frame;
   }
   
   /**
    * The singleton instance of java help used to launch help, initialized in
    * <code>main()</code> method and never <code>null</code> or modified after
    * that.
    */
   private static PSJavaHelp ms_help;
   
   /**
    * The frame for this client.
    */
   private static PSPackagerMainFrame ms_frame;
   
   /**
    * The object that can be used to display the error dialogs with in this
    * application. Initialized in <code>main()</code> method after constructing
    * the main frame and never <code>null</code> or modified after that.
    */
   private static ErrorDialogs ms_errDlg;
   
   /**
    * 
    */

}
