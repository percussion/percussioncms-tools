/* *****************************************************************************
 *
 * [ ResourceSelectorDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *******************************************************************************/

package com.percussion.E2Designer;

import com.percussion.guitools.ErrorDialogs;

import javax.swing.*;
import java.awt.*;

/**
 * Simple dialog to contain an instance of a {@link ResourceSelectionPanel}, to
 * allow selection of an application and resource.
 */
public class ResourceSelectorDialog extends PSDialog
{
   /**
    * Construct this dialog, providing parent dialog and title.
    * 
    * @param parent The parent dialog, may not be <code>null</code>.
    * @param title The title of the dialog, may be <code>null</code> or empty.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    */
   public ResourceSelectorDialog(Dialog parent, String title)
   {
      super(parent, title);
      init();
   }
   
   /**
    * Construct this dialog, providing parent frame and title.
    * 
    * @param parent The parent frame, may not be <code>null</code>.
    * @param title The title of the dialog, may be <code>null</code> or empty.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    */
   public ResourceSelectorDialog(Frame parent, String title)
   {
      super(parent, title);
      init();
   }
   
   /**
    * Get the name of the appliation selected.
    * 
    * @return The app name, may be <code>null</code> if none selected, never
    * empty.
    */
   public String getAppName()
   {
      return m_appName;
   }
   
   /**
    * Get the page name of the resource selected.
    * 
    * @return The page name, may be <code>null</code> if none selected, never
    * empty. 
    */
   public String getRequestPageName()
   {
      return m_resourceName;
   }
   
   /**
    * Inialize all ui components for this dialog.
    */
   private void init()
   {
      // create main panel
      JPanel mainPane = new JPanel();
      mainPane.setLayout(new BorderLayout());
      mainPane.setBorder(BorderFactory.createEmptyBorder( 5, 5, 5, 5 ));

      // add section panel
      m_selectionPanel = new ResourceSelectionPanel();
      mainPane.add(m_selectionPanel, BorderLayout.CENTER);
      
      // add cmd panel
      UTStandardCommandPanel cmdPanel = new UTStandardCommandPanel(this,
         SwingConstants.HORIZONTAL, false)
      {
         public void onOk()
         {
            ResourceSelectorDialog.this.onOk();
         }
      };
      cmdPanel.setBorder(BorderFactory.createEmptyBorder( 5, 5, 5, 5 ));
      
      JPanel commandPanel = new JPanel(new BorderLayout());
      commandPanel.add(cmdPanel, BorderLayout.EAST);
      mainPane.add(commandPanel, BorderLayout.SOUTH);
      
      getContentPane().add(mainPane);
      
      pack();
      center();
      setResizable(true);
   }
   
   /**
    * Handle user click of OK button.  Saves the currently selected app and
    * resource names.  If either are not selected, displays and error to the 
    * user and the dialog remains open, otherwise disposes this dialog.
    */
   public void onOk()
   {
      m_appName = m_selectionPanel.getApplicationName();
      m_resourceName = m_selectionPanel.getRequestPage();
      
      if (m_appName == null || m_resourceName == null)
      {
         ErrorDialogs.showErrorMessage(getOwner(), 
            getResources().getString("errMsg"), getResources().getString(
            "errMsgTitle"));
      }
      else
         dispose();
   }
   
   /**
    * The selection panel that contains the app and resource selection 
    * components, initialized in the <code>init()</code> method, never 
    * <code>null</code> after that.
    */
   private ResourceSelectionPanel m_selectionPanel;
   
   /**
    * The name of the application specified by the user.  Initially 
    * <code>null</code>, set by <code>onOk()</code>.  May be <code>null</code>
    * after that if no selection was made.
    */
   private String m_appName = null;
   
   /**
    * The name of the request page specified by the user.  Initially 
    * <code>null</code>, set by <code>onOk()</code>.  May be <code>null</code>
    * after that if no selection was made.
    */
   private String m_resourceName = null;
}
