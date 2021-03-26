/******************************************************************************
 *
 * [ ContentTypeDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.E2Designer;

import com.percussion.design.objectstore.PSContentType;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.ResourceBundle;

/**
 * A dialog for creating a new content type
 */

public class ContentTypeDialog extends PSDialog
{
   /**
    * Constructs a dialog for creating a new content type.
    * A new content type must be an unique entry among existing content types
    *
    * @param contentTypes a list of existing content types, can not be
    * <code>null</code>, but it might be empty.
    *
    * @throws IllegalArgumentException is thrown if list of content types is
    * <code>null</code>
    */
   public ContentTypeDialog(List contentTypes)
   {
      super();
      if(contentTypes == null)
         throw new IllegalArgumentException
            ("List of content types can not be null");

      m_contentTypes = contentTypes;
      initDialog();

   }

   /**
    * Worker method for the OK button action handler. Before saving validates
    * the content name (no longer then 50 chars) and the description (no longer
    * then 255 chars) if there is any description.  If any violation happens
    * an error message will pop-up with the descriptin of the error.
    */
   public void onOk()
   {
      if(!validateContentName())
         return;

      if(!validateContentDescription())
         return;

      m_newContentType = new PSContentType(m_contentName.getText());
      String description = m_contentDescription.getText();
      if(description != null )
         m_newContentType.setDescription(description); 

      setVisible(false);
   }

   /**Overrides PSDialog onCancel() method implementation.
    */
   public void onCancel()
   {
      dispose();
   }

   /**
    * Gets a new content type if any created.
    *
    * @return a content type, can be <code>null</code>
    */
   public PSContentType getNewContent()
   {
      return m_newContentType;
   }


   /**
    * Validates the content editor name.  The name can not be longer then
    * 50 chars and it must be an unique name among all existing content editor
    * types.
    * @return <code>true</code> if the content name is a valid name, otherwise
    * an error message pops-up.
    */
   private boolean validateContentName()
   {
      String contentName = m_contentName.getText();

      if(contentName == null || contentName.trim().length() == 0)
      {
         JOptionPane.showMessageDialog(this, ms_res.getString("emptyName"),
            ms_res.getString("error"), JOptionPane.ERROR_MESSAGE);
         return false;
      }
      else
      {
         if(contentName.length() > 50)
         {
            JOptionPane.showMessageDialog(this, ms_res.getString("longName"),
               ms_res.getString("error"), JOptionPane.ERROR_MESSAGE);
            return false;
         }

      }

      if(m_contentTypes != null)
      {
         boolean isMatch = false;
         for(int i = 0; i < m_contentTypes.size(); i++)
         {
            if(m_contentTypes.get(i).toString().equals(contentName))
            {
               isMatch = true;
               break;
            }
         }
         if(isMatch)
         {
            JOptionPane.showMessageDialog(this,
               Util.cropErrorMessage(ms_res.getString("existingName")),
               ms_res.getString("error"), JOptionPane.ERROR_MESSAGE);
            return false;
         }
      }
      return true;
   }

   /**
    * Validates the description of the content editor type. Can not be longer
    * then 255 characters if there is the description provided.  The description
    * of the new content type can be empty.
    *
    * @return <code>true</code> if the description is a valid text
    */
   private boolean validateContentDescription()
   {
      String description = m_contentDescription.getText();
      if(description != null && description.length() > 255)
      {
         JOptionPane.showMessageDialog(this,
               Util.cropErrorMessage(ms_res.getString("longDescription")),
               ms_res.getString("error"), JOptionPane.ERROR_MESSAGE);
         return false;

      }
      return true;
   }

   /**
    * Creates all controls for this dialog
    */
   private void initDialog()
   {
      ms_res = getResources();

      if(ms_res == null)
         throw new IllegalArgumentException("Can not find " + getResourceName());

      JPanel panel = new JPanel(new BorderLayout());
      getContentPane().add(panel);

      String labelStr = ms_res.getString("contentName");
      JLabel nameLabel = new JLabel(labelStr, JLabel.RIGHT);
      char mn = getResources().getString("contentName.mn").charAt(0);
      nameLabel.setDisplayedMnemonicIndex(labelStr.indexOf(mn));
      nameLabel.setDisplayedMnemonic(mn);
      
      labelStr = ms_res.getString("description");
      JLabel desLabel = new JLabel(labelStr, JLabel.RIGHT);
      mn = getResources().getString("description.mn").charAt(0);
      desLabel.setDisplayedMnemonicIndex(labelStr.indexOf(mn));
      desLabel.setDisplayedMnemonic(mn);
      
      m_contentName = new JTextField(20);
      m_contentName.setMinimumSize(new Dimension(120,20));
      m_contentName.setPreferredSize(new Dimension(120,20));
      m_contentName.setMaximumSize(new Dimension (Short.MAX_VALUE,20));
      nameLabel.setLabelFor(m_contentName);
      
      m_contentDescription = new JTextArea(5,15);
      m_contentDescription.setLineWrap(true);
      m_contentDescription.setWrapStyleWord(true);
      m_contentDescription.setEditable(true);
      desLabel.setLabelFor(m_contentDescription);
      
      //nameLabel.setMnemonic(ms_res.getString("contentName").charAt(0));
      JScrollPane descPanel = new JScrollPane (m_contentDescription);

      Box labelBox = Box.createVerticalBox();

      Box left = Box.createHorizontalBox();
      left.add(Box.createHorizontalGlue());
      left.add(nameLabel);

      labelBox.add(left);
      labelBox.add( Box.createVerticalStrut(5));
      labelBox.add( Box.createVerticalGlue());

      left = Box.createHorizontalBox();
      left.add(Box.createHorizontalGlue());
      left.add(desLabel);

      labelBox.add(left);
      labelBox.add(Box.createVerticalGlue());
      labelBox.add(Box.createVerticalStrut(70));

      JPanel controlPanel = new JPanel();
      controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
      controlPanel.add(m_contentName);
      controlPanel.add(Box.createVerticalStrut(5));
      controlPanel.add(descPanel);

      JPanel commandPanel = new JPanel();
      commandPanel.add(createCommandPanel(), BorderLayout.EAST);

      JPanel panelNew = new JPanel(new BorderLayout());
      panelNew.setBorder(new EmptyBorder(10,5,5,5));
      panelNew.add(labelBox, BorderLayout.WEST);
      panelNew.add(controlPanel, BorderLayout.CENTER);
      panelNew.add(Box.createHorizontalStrut(10), BorderLayout.EAST);
      panel.add(panelNew, BorderLayout.NORTH);
      panel.add(createCommandPanel(), BorderLayout.SOUTH);
      
      setTitle(ms_res.getString("title"));
      setResizable(true);
      pack();
      center(); 
   }

   /**
    *   Creates the command panel with OK, Cancel and Help buttons.
    * @return The command panel, never <code>null</code>.
    */
   private JPanel createCommandPanel()
   {
      UTStandardCommandPanel commandPanel =
         new UTStandardCommandPanel(this, "", SwingConstants.HORIZONTAL)
      {
        public void onOk()
        {
            ContentTypeDialog.this.onOk();
        }

      };
      getRootPane().setDefaultButton(commandPanel.getOkButton());

      JPanel cmdPanel = new JPanel(new BorderLayout());
      cmdPanel.setBorder(new EmptyBorder(5,5,5,8));
      cmdPanel.add(commandPanel, BorderLayout.EAST);
      return cmdPanel;
   }

   /** A field that will hold content type name, gets initialized in
    *  <code>initDialog</code>
    */
   private JTextField m_contentName = null;

   /**
    * A text area that will hold content type description, gets initialized in
    * <code>initDialog</code>
    */
   private JTextArea m_contentDescription = null;

   /**
    * A list of the current content types, gets initialized in the constructor.
    */
   private List m_contentTypes = null;

   /**A content type, gets initialized in {@link #onOk()}*/
   private PSContentType m_newContentType = null;

   /**Dialog resource strings, initialized in the {@link #initDialog()}*/
   private static ResourceBundle ms_res = null;


} 
