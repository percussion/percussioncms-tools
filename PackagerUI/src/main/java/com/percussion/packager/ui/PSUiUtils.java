/******************************************************************************
 *
 * [ PSUiUtils.java ]
 * 
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.packager.ui;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.applet.Applet;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Enumeration;

/**
 * @author erikserating
 *
 */
public class PSUiUtils
{
   /**
    * Private ctor to inhibit initialization
    */
   private PSUiUtils()
   {
      
      
   }
   
   /**
    * Centers a window on the screen, based on its current size.
    * @window the dialog to be centered, cannot be <code>null</code>.
    */   
   public static void center(Window window)
   {
      if(window == null)
         throw new IllegalArgumentException("dialog cannot be null.");
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      Dimension size = window.getSize();
      window.setLocation(( screenSize.width - size.width ) / 2,
            ( screenSize.height - size.height ) / 2 );
   }
   
   /**
    * Creates the menu items with the specified parameters.
    *
    * @param label the label to be used for menu item, may not be <code>
    * null</code> or empty.
    * @param mnemonic the character that is set to access menu items by using
    * ALT key combos, ignored if it is '0'.
    * @param accelKey keystroke describing the accelerator key for this menu,
    * ignored if it is <code>null</code>
    * @param toolTip the tool tip to be set on menu item, ignored if it is
    * <code>null</code> or empty.
    * @param icon The icon to set, ignored if it is <code>null</code>
    * @param name the name of the component to recognize it, ignored if it is
    * <code>null</code> or empty.
    * @param actionCommand the action command to set, ignored if it is <code>
    * null</code> or empty.
    * @param listener the listener to this menu action, ignored if it is <code>
    * null</code>
    *
    * @return the menu item, never <code>null</code>
    *
    * @throws IllegalArgumentException if label is <code>null</code> or empty.
    */
   public static JMenuItem createMenuItem(String label, char mnemonic,
      KeyStroke accelKey, String toolTip, ImageIcon icon, String name,
      String actionCommand, ActionListener listener)
   {
      if(label == null || label.trim().length() == 0)
         throw new IllegalArgumentException("label may not be null or empty.");

      JMenuItem menuItem = new JMenuItem( label );
      if(0 != mnemonic)
         menuItem.setMnemonic(mnemonic);

      if(null != accelKey)
         menuItem.setAccelerator( accelKey );

      if (null != toolTip && toolTip.length() > 0)
         menuItem.setToolTipText( toolTip );

      if(null != icon)
         menuItem.setIcon(icon);

      if (null != name && name.length() > 0)
         menuItem.setName(name);

      if (null != actionCommand && actionCommand.length() > 0)
         menuItem.setActionCommand(actionCommand);

      if(null != listener)
         menuItem.addActionListener(listener);

      return menuItem;
   }
   
   /**
    * Get the expansion state of a tree.
    *
    * @param tree
    * @return expanded tree path as Enumeration
    */
   public static Enumeration getExpansionState(JTree tree) {

       return tree.getExpandedDescendants(
          new TreePath(tree.getModel().getRoot()));

   }


   /**
    * Restore the expansion state of a JTree.
    * @param tree
    * @param enumeration an Enumeration of expansion state. You can get it using
    *  {@link #saveExpansionState(javax.swing.JTree)}.
    */
   public static void loadExpansionState(JTree tree, Enumeration enumeration) 
   {
       if (enumeration != null)
       {
           while (enumeration.hasMoreElements())
           {
              TreePath treePath = (TreePath) enumeration.nextElement();              
              tree.expandPath(treePath);
           }
       }
   }  
   
   /**
    * Modifies the JFileChooser to be a directory only chooser.
    * Changing the file name label to directory name and removing the
    * file type field. Also sets file selection to DIRECTORIES_ONLY.
    */
   public static void makeDirChooser(Container c)
   {
      if(c instanceof JFileChooser)
         ((JFileChooser)c).setFileSelectionMode(
            JFileChooser.DIRECTORIES_ONLY);
      String fileName = 
         UIManager.getString("FileChooser.fileNameLabelText");
      String fileType = 
         UIManager.getString("FileChooser.filesOfTypeLabelText");
      int len = c.getComponentCount();
      for (int i = 0; i < len; i++)
      {
         Component comp = c.getComponent(i);
         if (comp instanceof JLabel)
         {
            JLabel lab = (JLabel) comp;
            if(fileName.equals(lab.getText()))
            {
               lab.setText(
                  PSResourceUtils.getCommonResourceString("label.dir.name"));               
            }
            if(fileType.equals(lab.getText()))
            {
               lab.setVisible(false);
               c.getComponent(i + 1).setVisible(false);
            }
            
         }
         else if (comp instanceof Container)
         {
            makeDirChooser((Container) comp);
         }
      }
   }

   public static void showStackTraceDialog(Throwable throwable,
                                           String title) {/*from  w  w w .  j av a2s  .  co  m*/
      String message = throwable.getMessage() == null ? throwable
              .toString() : throwable.getMessage();
      showStackTraceDialog(throwable, title, message);
   }

   public static void showStackTraceDialog(Throwable throwable,
                                           String title, String message) {
      Window window = DefaultKeyboardFocusManager
              .getCurrentKeyboardFocusManager().getActiveWindow();
      showStackTraceDialog(throwable, window, title, message);
   }

   /**
    * show stack trace dialog when exception throws
    * @param throwable
    * @param parentComponent
    * @param title
    * @param message
    */
   public static void showStackTraceDialog(Throwable throwable,
                                           Component parentComponent, String title, String message) {
      final String more = "More";
      // create stack strace panel
      JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
      JLabel label = new JLabel(more + ">>");
      labelPanel.add(label);

      JTextArea straceTa = new JTextArea();
      final JScrollPane taPane = new JScrollPane(straceTa);
      taPane.setPreferredSize(new Dimension(360, 240));
      taPane.setVisible(false);
      // print stack trace into textarea
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      throwable.printStackTrace(new PrintStream(out));
      straceTa.setForeground(Color.RED);
      straceTa.setText(new String(out.toByteArray()));

      final JPanel stracePanel = new JPanel(new BorderLayout());
      stracePanel.add(labelPanel, BorderLayout.NORTH);
      stracePanel.add(taPane, BorderLayout.CENTER);

      label.setForeground(Color.BLUE);
      label.setCursor(new Cursor(Cursor.HAND_CURSOR));
      label.addMouseListener(new MouseAdapter() {
         public void mouseClicked(MouseEvent e) {
            JLabel tmpLab = (JLabel) e.getSource();
            if (tmpLab.getText().equals(more + ">>")) {
               tmpLab.setText("<<" + more);
               taPane.setVisible(true);
            } else {
               tmpLab.setText(more + ">>");
               taPane.setVisible(false);
            }
            SwingUtilities.getWindowAncestor(taPane).pack();
         };
      });

      JPanel panel = new JPanel(new BorderLayout());
      panel.add(new JLabel(message), BorderLayout.NORTH);
      panel.add(stracePanel, BorderLayout.CENTER);

      JOptionPane pane = new JOptionPane(panel, JOptionPane.ERROR_MESSAGE);
      JDialog dialog = pane.createDialog(parentComponent, title);
      int maxWidth = Toolkit.getDefaultToolkit().getScreenSize().width * 2 / 3;
      if (dialog.getWidth() > maxWidth) {
         dialog.setSize(new Dimension(maxWidth, dialog.getHeight()));
         setLocationRelativeTo(dialog, parentComponent);
      }
      dialog.setResizable(true);
      dialog.setVisible(true);
      dialog.dispose();
   }

   /**
    * set c1 location relative to c2
    * @param c1
    * @param c2
    */
   public static void setLocationRelativeTo(Component c1, Component c2) {
      Container root = null;

      if (c2 != null) {
         if (c2 instanceof Window || c2 instanceof Applet) {
            root = (Container) c2;
         } else {
            Container parent;
            for (parent = c2.getParent(); parent != null; parent = parent
                    .getParent()) {
               if (parent instanceof Window
                       || parent instanceof Applet) {
                  root = parent;
                  break;
               }
            }
         }
      }

      if ((c2 != null && !c2.isShowing()) || root == null
              || !root.isShowing()) {
         Dimension paneSize = c1.getSize();

         Point centerPoint = GraphicsEnvironment
                 .getLocalGraphicsEnvironment().getCenterPoint();
         c1.setLocation(centerPoint.x - paneSize.width / 2,
                 centerPoint.y - paneSize.height / 2);
      } else {
         Dimension invokerSize = c2.getSize();
         Point invokerScreenLocation = c2.getLocation(); // by longrm:
         // c2.getLocationOnScreen();

         Rectangle windowBounds = c1.getBounds();
         int dx = invokerScreenLocation.x
                 + ((invokerSize.width - windowBounds.width) >> 1);
         int dy = invokerScreenLocation.y
                 + ((invokerSize.height - windowBounds.height) >> 1);
         Rectangle ss = root.getGraphicsConfiguration().getBounds();

         // Adjust for bottom edge being offscreen
         if (dy + windowBounds.height > ss.y + ss.height) {
            dy = ss.y + ss.height - windowBounds.height;
            if (invokerScreenLocation.x - ss.x + invokerSize.width / 2 < ss.width / 2) {
               dx = invokerScreenLocation.x + invokerSize.width;
            } else {
               dx = invokerScreenLocation.x - windowBounds.width;
            }
         }

         // Avoid being placed off the edge of the screen
         if (dx + windowBounds.width > ss.x + ss.width) {
            dx = ss.x + ss.width - windowBounds.width;
         }
         if (dx < ss.x)
            dx = ss.x;
         if (dy < ss.y)
            dy = ss.y;

         c1.setLocation(dx, dy);
      }
   }
  
}
