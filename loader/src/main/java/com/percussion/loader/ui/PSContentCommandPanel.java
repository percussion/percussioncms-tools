/******************************************************************************
 *
 * [ PSContentCommandPanel.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.loader.ui;

import com.percussion.UTComponents.UTFixedButton;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * The PSCommandPanel provides the standard command panel containing
 * the buttons OK, Apply, Cancel and Help.
 */
public class PSContentCommandPanel extends JPanel implements ActionListener
{
   /**
    * Create the standard command panel containing an OK, Apply, Cancel button.
    *
    * @param dialog the dialog containing this instance, not <code>null</code>.
    * @param axis   the layout axis.
    * @throws IllegalArgumentException if the supplied dialog is 
    *    <code>null</code>.
    */
   public PSContentCommandPanel(PSContentDialog dialog, int axis)
   {
      this(dialog, axis, true);
   }

   /**
    * Create the standard command panel containing an OK, Apply, Cancel button.
    * A Help button is added if requested only.
    *
    * @param dialog the dialog containing this instance, not <code>null</code>.
    * @param axis   the layout axis.
    * @boolean showHelpButton <code>true</code> to show the help button, 
    *    <code>false</code> otherwise.
    * @throws IllegalArgumentException if the supplied dialog is 
    *    <code>null</code>.
    */
   public PSContentCommandPanel(PSContentDialog dialog, int axis,
      boolean showHelpButton)
   {
      if (dialog == null)
         throw new IllegalArgumentException("dialog cannot be null");
         
      m_dialog = dialog;
      if (null == m_res)
         m_res = ResourceBundle.getBundle(
               getClass().getName() + "Resources", Locale.getDefault() );

      if (axis == SwingConstants.VERTICAL)
         initVerticalPanel(showHelpButton);
      else
         initHorizontalPanel(showHelpButton);
   }

   /**
    * Initialize this as vertical command panel.
    *
    * @param showHelpButton If <code>true</code>, the help button is created
    * and the local m_helpButton member is initialized with the new button.
    */
   private void initButtons( boolean showHelpButton )
   {
      // create ok button and handle all its actions
      m_okButton = new UTFixedButton(m_res.getString("ok"));
      m_okButton.setDefaultCapable(true);
      m_okButton.setActionCommand(m_res.getString("ok"));
      m_okButton.addActionListener(this);

      // create apply button and handle all its actions
      m_applyButton = new UTFixedButton(m_res.getString("apply"));
      m_applyButton.setDefaultCapable(true);
      m_applyButton.setActionCommand(m_res.getString("apply"));
      m_applyButton.addActionListener(this);

      // create cancel button and handle all its actions
      m_cancelButton = new UTFixedButton(m_res.getString("cancel"));
      m_cancelButton.setActionCommand(m_res.getString("cancel"));
      m_cancelButton.addActionListener(this);
      // creating behavior that gives defaultButton focus back to the OK button
      // when focus leaves the CANCEL button.
      m_cancelButton.addFocusListener(new FocusAdapter()
      {
         public void focusLost(FocusEvent e)
         {
            getRootPane().setDefaultButton(m_okButton);
         }
      });

      if ( showHelpButton )
      {
         // create help button and handle all its actions
         m_helpButton = new UTFixedButton(m_res.getString("help"));
         m_helpButton.setActionCommand(m_res.getString("help"));
         m_helpButton.addActionListener(this);
         // creating behavior that gives defaultButton focus back to the OK button
         // when focus leaves the HELP button.
         m_helpButton.addFocusListener(new FocusAdapter()
         {
            public void focusLost(FocusEvent e)
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
      box.add(m_applyButton);
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
      box.add(m_applyButton);
      box.add(Box.createHorizontalStrut(5));
      box.add(m_cancelButton);
      if ( showHelpButton )
      {
         box.add(Box.createHorizontalStrut(5));
         box.add(m_helpButton);
      }

      this.setLayout(new FlowLayout(FlowLayout.RIGHT));
      this.add(box);
   }

   /**
    * Is called by the event handler and performs the requested action.
    * 
    * @param e the action event, never <code>null</code>.
    */
   public void actionPerformed(ActionEvent e)
   {
      if (e.getActionCommand().equals(m_res.getString("ok")))
        {
          onOk();
      }

      if (e.getActionCommand().equals(m_res.getString("apply")))
        {
          onApply();
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
    * @return JButton the OK button, never <code>null</code>.
    */
   public JButton getOkButton()
   {
        return m_okButton;
   }

   /**
    * Get the Cancel button.
    *
    * @return JButton the Cancel button, never <code>null</code>.
    */
   public JButton getCancelButton()
   {
      return m_cancelButton;
   }

   /**
    * Get the Help button.
    *
    * @return JButton the Help button, may be <code>null</code>.
    */
   public JButton getHelpButton()
   {
      return m_helpButton;
   }

   /**
    * The default action closes and disposes the dialog. Override this if 
    * special functionality is nessecary.
    */
   public void onCancel()
   {
      m_dialog.onCancel();
   }

   /**
    * The default implementation for 'OK' in the dialog.
    */
   public void onOk()
   {
      m_dialog.onOk();
   }

   /**
    * The default implementation for 'Apply' in the dialog.
    */
   public void onApply()
   {
      m_dialog.onApply();
   }

   /**
    * The default action opens the help URL. Override this only if no help
    * functionality is nessecary.
    *
    */
   public void onHelp()
   {
      m_dialog.onHelp();
   }

   /**
    * The dialog resources, initialized in ctor, never <code>null</code> or
    * changed after that.
    */
   private static ResourceBundle m_res = null;
   
   /**
    * The OK button, initialized in ctor, never <code>null</code> or
    * changed after that.
    */
   private JButton m_okButton = null;
   
   /**
    * The Apply button, initialized in ctor, never <code>null</code> or
    * changed after that.
    */
   private JButton m_applyButton = null;
   
   /**
    * The Cancel button, initialized in ctor, never <code>null</code> or
    * changed after that.
    */
   private JButton m_cancelButton = null;
   
   /**
    * The help button. May be null if panel is created with the help button
    * flag set to <code>false</code>.
    */
   private JButton m_helpButton = null;
   
   /**
    * The dialog which contains this, initialized in ctor, never 
    * <code>null</code> or changed after that.
    */
   private PSContentDialog m_dialog = null;
}
