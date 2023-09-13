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
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/** 
 * The replace dialog provides functionality to search for strings and replace
 * them for a given document.
 */
public class ReplaceDialog extends FindDialog
{
   /**
    * Construct the replace dialog with the provided owner and title.
    */   
   public ReplaceDialog(Frame owner, String title)
   {
      super(owner, title, false);
      
      m_owner = owner;
      initDialog();
   }
   
   /**
    * Initializes all GUI components.
    */
   protected void initDialog()
   {
      JPanel p1 = new JPanel(new BorderLayout(5, 5));
      p1.setBorder(new EmptyBorder(20, 10, 10, 10));
      p1.add(createSelectPanel(), "Center");
      p1.add(createCommandPanel(), "East");

      getContentPane().add(p1);

      this.addKeyListener(new KeyAdapter()
      {
         public void keyReleased(KeyEvent e)
         {
            if (KeyEvent.VK_ESCAPE == e.getKeyCode())
               hide();
         }
      });
   }
   
   /**
    * Create the select panel which contains all elements to specify the
    * parameters needed for the search/replace process.
    */   
   protected JPanel createSelectPanel()
   {
      JLabel findWhatLabel = new JLabel(
         MainFrame.getRes().getString("findWhatLabel"));
      m_findWhat = new FixedComboBox();
      m_findWhat.setEditable(true);
      m_findWhat.requestFocus();
      /*
       * The combo box 'eats' the 'Enter' key, which is required for the default
       * button to work. This is a workaround for that.
       */
      m_findWhat.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent event)
         {
            findNext(true);
         }
      });
      
      JLabel replaceWhatLabel = new JLabel(
         MainFrame.getRes().getString("replaceWhatLabel"));
      m_replaceWhat = new FixedComboBox();
      m_replaceWhat.setEditable(true);
      /*
       * The combo box 'eats' the 'Enter' key, which is required for the default
       * button to work. This is a workaround for that.
       */
      m_replaceWhat.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent event)
         {
            findNext(true);
         }
      });
      
      JPanel p11 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
      p11.add(findWhatLabel);
      p11.add(m_findWhat);
      
      JPanel p12 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
      p12.add(replaceWhatLabel);
      p12.add(m_replaceWhat);
      
      Box b1 = new Box(BoxLayout.Y_AXIS);
      b1.add(Box.createVerticalGlue());
      b1.add(p11);
      b1.add(Box.createVerticalStrut(5));
      b1.add(p12);
      
      m_matchCase = new JCheckBox(MainFrame.getRes().getString("matchCaseLabel"));
      JPanel p2 = new JPanel(new FlowLayout(FlowLayout.LEADING));
      p2.add(m_matchCase);
      
      JPanel p3 = new JPanel(new BorderLayout());
      p3.add(b1, "North");
      p3.add(p2, "South");
      
      return p3;
   }
   
   /**
    * Create the command panel which contains four buttons: Find Next, 
    * Replace, Replace All and Cancel.
    */   
   protected JPanel createCommandPanel()
   {
      FixedButton findNext = new FixedButton(
         MainFrame.getRes().getString("replaceNextButton"));
      findNext.setDefaultCapable(true);
      findNext.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent event)
         {
            findNext(true);
         }
      });
      getRootPane().setDefaultButton(findNext);

      FixedButton replace = new FixedButton(
         MainFrame.getRes().getString("replaceButton"));
      replace.setDefaultCapable(true);
      replace.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent event)
         {
            replace(event);
            findNext(true);
         }
      });

      FixedButton replaceAll = new FixedButton(MainFrame.getRes().getString("replaceAllButton"));
      replaceAll.setDefaultCapable(true);
      replaceAll.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent event)
         {
            replaceAll(event);
         }
      });
      
      FixedButton cancel = new FixedButton(MainFrame.getRes().getString("replaceCancelButton"));
      cancel.setDefaultCapable(true);
      cancel.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent event)
         {
            if (m_lastSelectionIndex != -1)
               m_findWhat.setSelectedIndex(m_lastSelectionIndex);
            else
               m_findWhat.setSelectedItem("");

            hide();
         }
      });
      
      Box b1 = new Box(BoxLayout.Y_AXIS);
      b1.add(Box.createVerticalGlue());
      b1.add(findNext);
      b1.add(Box.createVerticalStrut(5));
      b1.add(replace);
      b1.add(Box.createVerticalStrut(5));
      b1.add(replaceAll);
      b1.add(Box.createVerticalStrut(5));
      b1.add(cancel);

      JPanel p1 = new JPanel(new BorderLayout());
      p1.add(b1, "North");
      
      return p1;
   }
   
   /**
    * Handles the replace action.
    * 
    * @param event the action event to be handeld.
    */   
   public void replace(ActionEvent event)
   {
      String replaceString = (String) m_replaceWhat.getSelectedItem();
      maintainFindWhatList(m_replaceWhat, replaceString);
      
      if (m_textDoc.getSelectedText() != null)
         m_textDoc.replaceSelection(replaceString);
   }
   
   /**
    * Handles the replace all action.
    * 
    * @param event the action event to be handeld.
    */   
   public void replaceAll(ActionEvent event)
   {
      findNext(true);
      int startPos = m_lastFoundPos;
      do
      {
         replace(event);
         findNext(false);
      }
      while (m_lastFoundPos != startPos &&
             m_textDoc.getSelectedText() != null);
   }

   /**
    * The replace what combo box keeps a list of all replaces done during the
    * current session.
    */   
   protected FixedComboBox m_replaceWhat = null;
}
