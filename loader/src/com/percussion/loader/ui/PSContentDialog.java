/*[ PSContentDialog.java ]*****************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader.ui;

import com.percussion.guitools.PSDialog;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * Parent dialog to all the dialog in the loader ui.
 */
public class PSContentDialog extends PSDialog
{
   /**
    * Creates the dialog.
    *
    * @param frame owner the <code>Frame</code> from which the dialog is
    *    displayed.
    */
   public PSContentDialog(Frame frame)
   {
      super(frame);
   }

   /**
    * Creates the dialog with a title.
    *
    * @param frame owner the <code>Frame</code> from which the dialog is
    *    displayed.
    * @param title may be <code>null</code>
    */
   public PSContentDialog(Frame frame, String title)
   {
      super(frame, title);
   }

   /**
    * The action performed by the Apply button. This is to be overridden by
    * subclasses that implements the actual functionality.
    */
   public void onApply()
   {
   }

   /**
    * Creates the panel with the supplied title and description. Keeps the
    * description indented to the title. Uses <code>BoxLayout</code> for the
    * panel layout and sets white background color for the panel and the panel
    * is lowered.
    *
    * @param description the description string. Never <code>null</code> or
    * empty.
    * @param textArea the text area used to display the description, not
    *    <code>null</code>.
    * @return the panel, never <code>null</code>
    * @throws IllegalArgumentException if any parameter is invalid.
    */
   public static JPanel createDescriptionPanel(String description,
      JTextArea textArea)
   {
      if (description == null || description.length() == 0)
         throw new IllegalArgumentException(
            "description may not be null or empty.");

      if (textArea == null)
         throw new IllegalArgumentException("text area cannot be null");

      JPanel descPanel = new JPanel();
      descPanel.setLayout(new BorderLayout());
      textArea.setText(description);
      textArea.setLineWrap(true);
      textArea.setWrapStyleWord(true);
      textArea.setEditable(false);
      JScrollPane areaScrollPane = new JScrollPane(textArea);
      areaScrollPane.setPreferredSize(new Dimension(100, 100));
      descPanel.add(areaScrollPane, BorderLayout.NORTH);

      return descPanel;
   }

   /**
   * Centers the specified component.
   *
   * @param frame to be centered, may be <code>null</code>. If <code>null
   * </code> it silently exits doing nothing.
   */
  public static void center(Component frame)
  {
     if (frame == null)
        return;

     Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
     Dimension frameSize = frame.getSize();

     if (frameSize.height > screenSize.height)
        frameSize.height = screenSize.height;

     if (frameSize.width > screenSize.width)
        frameSize.width = screenSize.width;

     frame.setLocation((screenSize.width - frameSize.width) / 2,
        (screenSize.height - frameSize.height) / 2);
   }
}
