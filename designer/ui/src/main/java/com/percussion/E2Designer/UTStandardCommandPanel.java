/**[ UTStandardCommandPanel ]*************************************************
 *
 * COPYRIGHT (c) 2000 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 * $Id: UTStandardCommandPanel.java 1.10 2002/10/02 15:59:47Z SyamalaKommuru Exp $
 *
 * Version Labels  : $Name: $
 *
 * Locked By       : $Locker: $
 *
 * Revision History:
 * $Log: UTStandardCommandPanel.java $
 * Revision 1.10  2002/10/02 15:59:47Z  SyamalaKommuru
 * modified to use javahelp instead of browser help
 * Revision 1.9  2000/06/14 16:05:28Z  paulhoward
 * Made the Help button optional in the ctor.
 * Cleaned up some formatting.
 *
 * Revision 1.8  1999/09/19 04:52:49Z  AlexTeng
 * Removed all uses of "import....*" to use each individual object file.
 *
 * Revision 1.7  1999/08/25 20:41:01  AlexTeng
 * Updated files to use
 * com.percussion.UTComponents.UTFixedButton instead of
 * com.percussion.E2Designer.UTFixedButton.
 *
 * Revision 1.6  1999/07/26 22:06:36  AlexTeng
 * Changed onHelp to call PSDialog.onHelp() instead of
 * PSDialog.getHelp().
 *
 * Revision 1.5  1999/07/22 15:08:36  martingenhart
 * call correct help function
 * Revision 1.4  1999/07/21 19:07:10  AlexTeng
 * Changed onHelp funtionality.
 *
 * Revision 1.3  1999/07/14 21:08:59  AlexTeng
 * Added focus handling capabilities to swap back to the ok button
 * when the focus leaves either the cancel button or the help button.
 *
 * Revision 1.2  1999/04/09 21:47:19  martingenhart
 *
 * updated to changes in PSDialog
 * Revision 1.1  1999/04/02 22:55:20  martingenhart
 * Initial revision
 *
 *****************************************************************************/
package com.percussion.E2Designer;

import com.percussion.UTComponents.UTFixedButton;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * The UTStandardCommandPanel provides the standard command panel containing
 * the buttons OK, Cancel and Help.
 */
////////////////////////////////////////////////////////////////////////////////
public abstract class UTStandardCommandPanel extends JPanel implements ActionListener
{
   /**
    * Create the standard command panel containing an OK, Cancel and Help button.
    *
    * @param   dialog   the dialog containing this instance
    * @param helpURL   the help page URL
    * @param axis      the layout axis
    */
   //////////////////////////////////////////////////////////////////////////////
   public UTStandardCommandPanel(JDialog dialog,
         @SuppressWarnings("unused") String helpURL, int axis)
   {
      this( dialog, axis, true );
   }


   public UTStandardCommandPanel(JDialog dialog, int axis, boolean showHelpButton )
   {
        m_dialog = dialog;

      try
        {
         m_res = ResourceBundle.getBundle("com.percussion.E2Designer.UTStandardCommandPanelResources",
                                                       Locale.getDefault());
        }
        catch(MissingResourceException ex)
        {
           System.out.println(ex);
        }

        if (axis == SwingConstants.VERTICAL)
          initVerticalPanel( showHelpButton &&  shouldShowHelpButtons());
      else
         initHorizontalPanel( showHelpButton &&  shouldShowHelpButtons() );
   }
   
   /**
    * We use reflection to attempt to load the eclipse workbench's 
    * help manager class to see if it exists. If so we know we should not show the 
    * help buttons as f1 contextual help will work.
    * 
    * @return <code>true</code> help buttons should be shown.
    */
   private boolean shouldShowHelpButtons()
   {
      String classname = "com.percussion.workbench.ui.help.PSHelpManager";
      try
      {
         Class.forName(classname);
         return false;
      }
      catch (Throwable t)
      {
         return true;         
      }     
   }

   /**
    * Initialize this as vertical command panel.
    *
    * @param showHelpButton If <code>true</code>, the help button is created
    * and the local m_helpButton member is initialized with the new button.
    */
   //////////////////////////////////////////////////////////////////////////////
   private void initButtons( boolean showHelpButton )
   {
        // create ok button and handle all its actions
        m_okButton = new UTFixedButton(m_res.getString("ok"));
      m_okButton.setDefaultCapable(true);
        m_okButton.setActionCommand(m_res.getString("ok"));
        m_okButton.setMnemonic((m_res.getString( "ok.mn" )).charAt(0));
        m_okButton.addActionListener(this);

        // create cancel button and handle all its actions
        m_cancelButton = new UTFixedButton(m_res.getString("cancel"));
        m_cancelButton.setActionCommand(m_res.getString("cancel"));
           m_cancelButton.setMnemonic((m_res.getString( "cancel.mn" )).charAt(0));
        m_cancelButton.addActionListener(this);
      // creating behavior that gives defaultButton focus back to the OK button
      // when focus leaves the CANCEL button.
      m_cancelButton.addFocusListener(new FocusAdapter()
      {
         @Override
         public void focusLost(@SuppressWarnings("unused") FocusEvent e)
         {
            getRootPane().setDefaultButton(m_okButton);
         }
      });

      if ( showHelpButton)
      {
           // create help button and handle all its actions
           m_helpButton = new UTFixedButton(m_res.getString("help"));
           m_helpButton.setActionCommand(m_res.getString("help"));
           m_helpButton.setMnemonic((m_res.getString( "help.mn" )).charAt(0));
           m_helpButton.addActionListener(this);
         // creating behavior that gives defaultButton focus back to the OK button
         // when focus leaves the HELP button.
         m_helpButton.addFocusListener(new FocusAdapter()
         {
            @Override
            public void focusLost(@SuppressWarnings("unused") FocusEvent e)
            {
               getRootPane().setDefaultButton(m_okButton);
            }
         });
      }
   }

   /**
    * Initialize this as vertical command panel.
    *
    * @param showHelpButton If <code>true</code>, the help button is added to
    * the panel in the standard location.
    */
   private void initVerticalPanel( boolean showHelpButton )
   {
        initButtons( showHelpButton );

      Box box = new Box(BoxLayout.Y_AXIS);
      box.add(Box.createVerticalGlue());
      box.add(m_okButton);
      box.add(Box.createVerticalStrut(5));
      box.add(m_cancelButton);
      if ( showHelpButton )
      {
         box.add(Box.createVerticalStrut(5));
         box.add(m_helpButton);
      }

      this.setLayout(new BorderLayout());
      this.add(box, BorderLayout.NORTH);
   }

   /**
    * Initialize this as horizontal command panel.
    *
    * @param showHelpButton If <code>true</code>, the help button is added to
    * the panel in the standard location.
    */
   private void initHorizontalPanel( boolean showHelpButton )
   {
        initButtons( showHelpButton );

      Box box = new Box(BoxLayout.X_AXIS);
      box.add(Box.createHorizontalGlue());
      box.add(m_okButton);
      box.add(Box.createHorizontalStrut(5));
      box.add(m_cancelButton);
      if ( showHelpButton )
      {
         box.add(Box.createHorizontalStrut(5));
         box.add(m_helpButton);
      }

      this.setLayout(new FlowLayout(FlowLayout.LEFT));
      this.add(box);
   }


   /**
    * Create the dialogs command panel (OK, Cancel and Help).
    */
   //////////////////////////////////////////////////////////////////////////////
   public void actionPerformed(ActionEvent e)
   {
      if (e.getActionCommand().equals(m_res.getString("ok")))
        {
          onOk();
      }
      if (e.getActionCommand().equals(m_res.getString("cancel")))
        {
          onCancel();
      }
      if (e.getActionCommand().equals(m_res.getString("help")))
        {
          onHelp();
      }
   }

   /**
    * Get the OK button.
    *
    * @return      JButton      the OK button
    */
   //////////////////////////////////////////////////////////////////////////////
   public JButton getOkButton()
   {
        return m_okButton;
   }

   /**
    * Get the Cancel button.
    *
    * @return      JButton      the Cancel button
    */
   //////////////////////////////////////////////////////////////////////////////
   public JButton getCancelButton()
   {
      return m_cancelButton;
   }

   /**
    * Get the Help button.
    *
    * @return      JButton      the Help button, which may be null
    */
   //////////////////////////////////////////////////////////////////////////////
   public JButton getHelpButton()
   {
        return m_helpButton;
   }

   /**
    * This is special in any case.
    */
   //////////////////////////////////////////////////////////////////////////////
   abstract public void onOk();

   /**
    * The default action closes and disposes the dialog. Override this if special
    * functionality is nessecary.
    */
   public void onCancel()
   {
      ((PSDialog)m_dialog).onCancel();
   }

   /**
    * The default action opens the help URL. Override this only if no help
    * functionality is nessecary.
    *
    */
   public void onHelp()
   {
        ((PSDialog)m_dialog).onHelp();
   }

   //////////////////////////////////////////////////////////////////////////////
   /*
    * class resources
    */
   private static ResourceBundle m_res = null;
   /**
    * the OK button
    */
   private JButton m_okButton = null;
   /**
    * the Cancel button
    */
   private JButton m_cancelButton = null;
   /**
    * the help button. May be null if panel is created with the help button
    * flag set to <code>false</code>.
    */
   private JButton m_helpButton = null;
   /**
    * the dialog which contains this
    */
   private JDialog m_dialog = null;

}
