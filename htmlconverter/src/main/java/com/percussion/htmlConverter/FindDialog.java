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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

/** 
 * The find dialog provides functionality to search for strings in a given 
 * file.
 */
public class FindDialog extends JDialog
{
   /**
    * Construct the search dialog with the provided owner and title. We will
    * always create a modal dialog.
    *
    * @param owner the owner frame or null.
    * @param title the dialog title
    */   
   public FindDialog(Frame owner, String title)
   {
      super(owner, title, false);
      
      m_owner = owner;
      initDialog();
   }
   
   /**
    * This constructor is for classes extending this dialog but need an other
    * GUI initialization.
    *
    * @param owner the owner frame or null.
    * @param title the dialog title
    * @parma modal whether to create the dialog modal or not.
    */   
   protected FindDialog(Frame owner, String title, boolean modal)
   {
      super(owner, title, modal);
   }
   
   /**
    * Use this member to specify a new text document where this dialog will 
    * search in. The provided document must be of type JScrollPane which must
    * conatin a JTExtArea as the viewed component.
    *
    * @param doc a scroller pane wich views a JTextArea.
    */
   public void newSearchDocument(JScrollPane doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("We need a valid scroll pane!");
      
      if (doc.getViewport() == null)
         throw new IllegalArgumentException("The viewport must be set in the provided scroll pane!");
      
      Component comp = doc.getViewport().getView();
      if (!(comp instanceof JTextArea))
         throw new IllegalArgumentException("The scroll panes viewport must contain a valid JTextArea as its view!");
      
      // save the search document and its text area
      m_doc = doc;
      m_textDoc = (JTextArea) comp;
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

      addKeyListener(new KeyAdapter()
      {
         public void keyReleased(KeyEvent event)
         {
            if (KeyEvent.VK_ESCAPE == event.getKeyCode())
               hide();
         }
      });
   }
   
   /**
    * Create the select panel which contains all elements to specify the
    * parameters needed for the search process.
    */   
   protected JPanel createSelectPanel()
   {
      JLabel findWhatLabel = new JLabel(
         MainFrame.getRes().getString("findWhatLabel"));
      m_findWhat = new FixedComboBox();
      m_findWhat.setEditable(true);
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

      JPanel p1 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
      p1.add(findWhatLabel);
      p1.add(m_findWhat);
      
      m_matchCase = new JCheckBox(
         MainFrame.getRes().getString("matchCaseLabel"));
      JPanel p2 = new JPanel(new FlowLayout(FlowLayout.LEADING));
      p2.add(m_matchCase);
      
      JPanel p3 = new JPanel(new BorderLayout());
      p3.add(p1, "North");
      p3.add(p2, "South");
      
      return p3;
   }
   
   /**
    * This function maintains the provided combo box for this session. If the
    * new string passed in is valid (not null and not empty) the current
    * entries are checked and if not found the new string will be added.
    *
    * @param comboBox the combo box to maintain.
    * @param newString the new string to add to the combo box.
    */   
   protected void maintainFindWhatList(FixedComboBox comboBox,
                                       String newString)
   {
      // we do not add empty strings
      if (newString == null || newString.equals(""))
         return;
      
      for (int i=0; i<comboBox.getItemCount(); i++)
      {
         // ist already there, so just return
         if (newString.equals((String) comboBox.getItemAt(i)))
            return;
      }
      
      // this is a new search string, so add it
      comboBox.addItem(newString);
   }
   
   /**
    * Search the currrent document for the first occurence of the provided
    * string after the given start position. If we reach the end of file 
    * before we find the string, we move to the top of file and continue
    * searching until we reached the start position again.
    * 
    * @param searchString the string to search for.
    * @param startPos the position to start the search from.
    * @param showNotFound if <CODE>true</CODE> a message dialog is shown if we 
    *    could not find the search string
    * @return the start position of the string found. -1 will be returned if
    *    the string was not found at all.
    */   
   protected int find(String searchString, int startPos, boolean showNotFound)
   {
      String text = m_textDoc.getText();
      String search = searchString;

      if (!m_matchCase.isSelected())
      {
         // do not match case
         text = text.toLowerCase();
         search = search.toLowerCase();
      }
      
      int pos = text.indexOf(search, startPos);
      if (pos == -1)
      {
         // not found, try the whole file
         pos = text.indexOf(search);
      }
      
      if (pos == -1)
      {
         if (showNotFound)
         {
            JOptionPane messageDialog = new JOptionPane(null,
                                                        JOptionPane.INFORMATION_MESSAGE);
            String[] args =
            {
               searchString
            };
            String msg = MainFrame.getRes().getString("searchStringNotFound");
            messageDialog.showMessageDialog(this, Util.dress(msg, args));
         }
      }
      else
         m_textDoc.select(pos, pos+search.length());
      
      return pos;
   }
   
   /**
    * Find and highlight the next occurance for the current search settings.
    * This function throws an illegal state exception if the search string
    * has not been defined yet.
    */   
   public void findNext()
   {
      if (m_findWhat == null)
         throw new IllegalStateException(
            "No search string has been defined yet!");
      
      String searchString = (String) m_findWhat.getSelectedItem();
      m_lastFoundPos = find(searchString, m_textDoc.getCaretPosition(), true);
   }
   
   /**
    * This function shows the find dialog and sets the currrent text to find
    * to the provieded text.
    *
    * @param what the text to find.
    */   
   public void setVisible(String what)
   {
      maintainFindWhatList(m_findWhat, what);
      m_findWhat.setSelectedItem(what);
      
      setVisible(true);
   }
   
   /**
    * This function is overwritten to select the find combo box text each 
    * time this dialog is shown.
    *
    * @param visible the visibility
    */
   public void setVisible(boolean visible)
   {
      m_lastSelectionIndex = m_findWhat.getSelectedIndex();
      m_findWhat.getEditor().selectAll();
      
      super.setVisible(visible);
   }
   
   /**
    * Returns the status whether or not all search setting have been specified
    * yet.
    *
    * @return <CODE>true</CODE> if this is in a legal state, 
    *    <CODE>false</CODE> otherwise.
    */   
   public boolean isInLegalState()
   {
      boolean isLegal = m_findWhat != null &&
                        m_doc != null && 
                        m_textDoc != null;
      
      if (isLegal)
      {
         if (m_findWhat.getItemCount() == 0)
         {
            isLegal = false;
         }
         else
         {
            String selected = (String) m_findWhat.getSelectedItem();
            isLegal = !selected.equals("");
         }
      }
      
      return isLegal;
   }
   
   /**
    * Handles the find next action.
    * 
    * @param showNotFound if <CODE>true</CODE> a message dialog is shown if we 
    *    could not find the search string
    */   
   public void findNext(boolean showNotFound)
   {
      String searchString = (String) m_findWhat.getSelectedItem();
      maintainFindWhatList(m_findWhat, searchString);
      m_lastFoundPos = find(searchString, 
                            m_textDoc.getCaretPosition(), showNotFound);
   }
   
   /**
    * Create the command panel which contains two buttons: Find Next and 
    * Cancel.
    */   
   protected JPanel createCommandPanel()
   {
      FixedButton findNext = new FixedButton(
         MainFrame.getRes().getString("findNextButton"));
      findNext.setDefaultCapable(true);
      findNext.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent event)
         {
            findNext(true);
         }
      });
      getRootPane().setDefaultButton(findNext);
      
      FixedButton cancel = new FixedButton(
         MainFrame.getRes().getString("findCancelButton"));
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
      b1.add(cancel);

      JPanel p1 = new JPanel(new BorderLayout());
      p1.add(b1, "North");
      
      return p1;
   }

   /**
    * Centers the dialog to its owner, based on the current size. This is done
    * only the first time this instance is shown.
    */
   public void center()
   {
      if (m_notShownYet)
      {
         Dimension ownerSize = m_owner.getSize();
         Dimension size = getSize();
         Point ownerLocation = m_owner.getLocation();
         setLocation(ownerLocation.x + (ownerSize.width - size.width) / 2, 
                     ownerLocation.y + (ownerSize.height - size.height) / 2);
         
         m_notShownYet = false;
      }
   }
   
   /**
    * This class provide a button with a fixed size.
    */
   protected class FixedButton extends JButton
   {
      public FixedButton(String name)
      { 
         super(name);
         setPreferredSize(BUTTON_SIZE);
      }
      public Dimension getMinimumSize() { return getPreferredSize(); }
      public Dimension getMaximumSize() { return getPreferredSize(); }

      private Dimension BUTTON_SIZE = new Dimension(80, 24);
   }
   
   /**
    * This class provide a combo box with a fixed size.
    */
   protected class FixedComboBox extends JComboBox
   {
      public FixedComboBox()
      { 
         super();
         setPreferredSize(COMBOBOX_SIZE);
      }
      public Dimension getMinimumSize() { return getPreferredSize(); }
      public Dimension getMaximumSize() { return getPreferredSize(); }

      private Dimension COMBOBOX_SIZE = new Dimension(200, 20);
   }

   /**
    * The search document must be of type JScrollPane which views an object
    * of type JTextArea.
    */   
   protected JScrollPane m_doc = null;
   /**
    * The text area retrieved from the JScrollPane's viewport. This is stored
    * for easier access only.
    */
   protected JTextArea m_textDoc = null;
   /**
    * The owner of this dialog.
    */   
   protected Frame m_owner = null;
   /**
    * The find what combo box keeps a list of all searches done during the
    * current session.
    */   
   protected FixedComboBox m_findWhat = null;
   /**
    * The position of the last search result.
    */   
   protected int m_lastFoundPos = 0;
   /**
    * The check box defining whether or not to search case sensitive.
    */
   protected JCheckBox m_matchCase = null;
   /**
    * The index of the last selected item.
    */
   protected int m_lastSelectionIndex = -1;
   /**
    * Status whether or not we have shown this dialog yet.
    */
   protected boolean m_notShownYet = true;
}
