/* ****************************************************************************
 *
 * [ PSUserPreferenceDialog.java ]
 * 
 * COPYRIGHT (c) 1999 - 2004 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.loader.ui;

import com.percussion.guitools.UTStandardCommandPanel;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

/**
 * Dialog for defining user preferences. Specifies three selection criteria -
 * open a new descriptor, last descriptor or none through three radio buttons.
 */
public class PSUserPreferenceDialog extends PSContentDialog
{
   /**
    * Creates user preference dialog. Initializes <code>m_preference</code> by
    * calling {@link #initPreference()}. Initializes the dialog.
    *
    * @param frame owner the <code>Frame</code> from which the dialog is
    *    displayed.
    */
   public PSUserPreferenceDialog(Frame frame)
   {
      super(frame);
      m_preference = PSUserPreferences.deserialize();
      initDialog();
   }

   /**
    * Creates user preference dialog with a specified title/
    *
    * @param frame owner the <code>Frame</code> from which the dialog is
    *    displayed.
    * @param title, may not be <code>null</code>.
    */
   public PSUserPreferenceDialog(Frame frame, String title)
   {
      super(frame, title);
      initDialog();
   }

   /**
    * Initializes the dialog.
    */
   private void initDialog()
   {
      if (null == ms_res)
         ms_res = ResourceBundle.getBundle(
         getClass().getName() + "Resources", Locale.getDefault() );

      setTitle(
         PSContentLoaderResources.getResourceString(ms_res, "dialog.title"));
      JPanel mainPane = new JPanel();
      mainPane.setLayout(new BorderLayout());
      UTStandardCommandPanel cmdPanel = new UTStandardCommandPanel(this,
         SwingConstants.HORIZONTAL, true);
      mainPane.add(cmdPanel, BorderLayout.SOUTH);
      m_newDesc = new JRadioButton(
         PSContentLoaderResources.getResourceString(ms_res, "radiobutton.new"));
      m_newDesc.setSelected(m_preference.isNewDescriptor());
      m_lastDesc = new JRadioButton(
         PSContentLoaderResources.getResourceString(ms_res, "radiobutton.last"));
      m_lastDesc.setSelected(m_preference.isLastDescriptor());
      m_noDesc = new JRadioButton(
         PSContentLoaderResources.getResourceString(ms_res, "radiobutton.none"));
      m_noDesc.setSelected(m_preference.isNoDescriptor());
      ButtonGroup btnGrp = new ButtonGroup();
      btnGrp.add(m_newDesc);
      btnGrp.add(m_lastDesc);
      btnGrp.add(m_noDesc);
      Border b1 = BorderFactory.createEmptyBorder(10, 10, 10, 10 );
      Border b3 = BorderFactory.createEtchedBorder();
      Border b2 = BorderFactory.createCompoundBorder(b3, b1);
      Border b = BorderFactory.createTitledBorder(b2,
         PSContentLoaderResources.getResourceString(ms_res, "border.title"));
      cmdPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0 ));
      JPanel radioPane = new JPanel();
      radioPane.setLayout(new BoxLayout(radioPane, BoxLayout.X_AXIS));
      JPanel topLeft = new JPanel();
      topLeft.setLayout(new BoxLayout(topLeft, BoxLayout.Y_AXIS));
      topLeft.add(m_newDesc);
      topLeft.add(m_lastDesc);
      topLeft.add(m_noDesc);
      radioPane.add(topLeft);
      radioPane.add(Box.createHorizontalGlue());
      radioPane.setBorder(b);
      mainPane.setBorder(b1);
      mainPane.add(radioPane, BorderLayout.CENTER);
      getContentPane().add(mainPane);
      pack();
      center();
      setResizable(true);
   }

   /**
    * Applies the user preference based on the user selection.
    */
   public void onOk()
   {
      m_preference.setIsLastDescriptor(m_lastDesc.isSelected());
      m_preference.setIsNewDescriptor(m_newDesc.isSelected());
      m_preference.setIsNoneDescriptor(m_noDesc.isSelected());
      PSUserPreferences.serialize(m_preference);
      super.onOk();
   }

   /**
    * Cancels the currently selected user preference.
    */
   public void onCancel()
   {
      super.onCancel();
   }
  
   /**
    * Resource bundle for this class. Initialized in the constructor.
    * It's not modified after that. Never <code>null</code>.
    */
   private static ResourceBundle ms_res;

   /**
    * Encapsulates user preference settings. Intialized in {@link
    * #initPreference()}, never <code>null</code> or modified after that.
    */
   private PSUserPreferences m_preference;

   /**
    * 'Open New Descriptor' radio button in this dialog. If selected the tool
    * will start a new loader descriptor everytime. Intialized in {@link
    * #init()}, never <code>null</code> or modified after that.
    */
   private JRadioButton m_newDesc;

   /**
    * 'Open Last Descriptor' radio button in this dialog. If selected, the tool
    * opens the last opened Loader Descriptor. If none was open before,
    * this will start a new descriptor. Intialized in {@link #init()}, never
    * <code>null</code> or modified after that.
    */
   private JRadioButton m_lastDesc;

   /**
    * 'Open None' radio button in this dialog. If selected, the tool will start
    * with a Loader Descriptor. To use the tool the user must either open an
    * existing descriptor or create a new one manually. Intialized in {@link
    * #init()}, never <code>null</code> or modified after that.
    */
   private JRadioButton m_noDesc;
}