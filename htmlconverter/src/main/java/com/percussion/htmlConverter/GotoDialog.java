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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;

/** 
 * This class provides the GUI for the goto dialog.
 */
public class GotoDialog extends JDialog
{
   /**
    * Construct the dialog.
    *
    * @param owner the owners frame.
    * @param text the text area we are using.
    */
   public GotoDialog(Frame owner, JTextArea text)
   {
      super(owner, MainFrame.getRes().getString("gotoTitle"));
      m_owner = owner;
      
      if (text == null)
         throw new IllegalArgumentException("You must provide a valid text area component!");
      m_text = text;
      
      initDialog();
   }

   /**
    * Packs and centers the dialog to its owner, based on the current size.
    */
   public void packCenter()
   {
      pack();
      center();
   }

   /**
    * Centers the dialog to its owner, based on the current size.
    */
   public void center()
   {
      if (m_owner != null)
      {
         Dimension ownerSize = m_owner.getSize();
         Dimension size = getSize();
         Point ownerLocation = m_owner.getLocation();
         setLocation(ownerLocation.x + (ownerSize.width - size.width) / 2, 
                     ownerLocation.y + (ownerSize.height - size.height) / 2);
      }
   }
   
   /**
    * Initialize the dialog GUI.
    */
   private void initDialog()
   {
      JPanel p1 = new JPanel(new BorderLayout(5, 5));
      p1.add(createSelectPanel(), "Center");
      p1.add(createCommandPanel(), "East");

      getContentPane().add(p1);

      this.addKeyListener(new KeyAdapter()
      {
         public void keyReleased(KeyEvent e)
         {
            if (KeyEvent.VK_ESCAPE == e.getKeyCode())
               dispose();
         }
      });
   }
   
   /**
    * Create the select panel which contains all elements to specify the
    * parameters needed for the search process.
    */   
   protected JPanel createSelectPanel()
   {
      JLabel gotoLineLabel = new JLabel(MainFrame.getRes().getString("gotoLineLabel"));
      m_lineNumber = new FixedTextField();
      m_lineNumber.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent event)
         {
            onOk();
         }
      });
      
      JPanel p1 = new JPanel(new GridLayout(2, 1));
      p1.setBorder(new EmptyBorder(10, 20, 10, 10));
      p1.add(gotoLineLabel);
      p1.add(m_lineNumber);
      
      return p1;
   }
   
   /**
    * Create the command panel which contains two buttons: OK and Cancel.
    */   
   protected JPanel createCommandPanel()
   {
      final UTFixedButton ok = new UTFixedButton(MainFrame.getRes().getString("gotoOkButton"));
      ok.setDefaultCapable(true);
      ok.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent event)
         {
            onOk();
         }
      });
      getRootPane().setDefaultButton(ok);
      
      UTFixedButton cancel = new UTFixedButton(MainFrame.getRes().getString("gotoCancelButton"));
      cancel.setDefaultCapable(true);
      cancel.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent event)
         {
            dispose();
         }
      });
      cancel.addFocusListener(new FocusAdapter()
      {
         public void focusLost(FocusEvent event)
         {
            getRootPane().setDefaultButton(ok);
         }
      });
      
      Box b1 = new Box(BoxLayout.Y_AXIS);
      b1.add(Box.createVerticalGlue());
      b1.add(ok);
      b1.add(Box.createVerticalStrut(5));
      b1.add(cancel);

      JPanel p1 = new JPanel(new BorderLayout());
      p1.setBorder(new EmptyBorder(10, 10, 10, 10));
      p1.add(b1, "North");
      
      return p1;
   }
   
   /**
    * Perform the action for the ok button.
    */
   private void onOk()
   {
      try
      {
         String lineNumber = m_lineNumber.getText();
         if (lineNumber != null)
         {
            Integer line = new Integer(lineNumber);
            if (line.intValue() < m_text.getLineCount())
               m_text.setCaretPosition(m_text.getLineStartOffset(line.intValue()-1));
            else
               m_text.setCaretPosition(m_text.getLineStartOffset(m_text.getLineCount()-1));
         }
      }
      catch (BadLocationException e)
      {
         // just do nothing
      }
      dispose();
   }
   
   /**
    * This class provides a text field with a fixed size.
    */
   protected class FixedTextField extends JTextField
   {
      public FixedTextField()
      { 
         super();
         setPreferredSize(TEXTFIELD_SIZE);
      }
      public Dimension getMinimumSize() { return getPreferredSize(); }
      public Dimension getMaximumSize() { return getPreferredSize(); }

      private Dimension TEXTFIELD_SIZE = new Dimension(80, 20);
   }
   
   /**
    * The owner frame of this dialog.
    */
   Frame m_owner = null;
   /**
    * The text field for the line number.
    */
   FixedTextField m_lineNumber = null;
   /**
    * The text area component we will use to perform the goto action.
    */
   JTextArea m_text = null;
}
