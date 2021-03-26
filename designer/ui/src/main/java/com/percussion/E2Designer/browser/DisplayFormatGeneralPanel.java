/*******************************************************************************
 *
 * [ DisplayFormatGeneralPanel.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.E2Designer.browser;

import com.percussion.E2Designer.PSDlgUtil;
import com.percussion.cms.objectstore.PSDisplayFormat;
import com.percussion.UTComponents.UTFixedHeightTextField;
import com.percussion.guitools.PSDialog;
import com.percussion.guitools.PSPropertyPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

/**
 * The panel shows all general components the user can define for a Display
 * Format.
 */
public class DisplayFormatGeneralPanel extends ParentPanel
{
   /**
    * Constructs the display format general panel.
    *
    * @param parent the container of this panel.  Must not be <code>null</code>.
    */
   public DisplayFormatGeneralPanel(DisplayFormatTabbedPanel parent)
   {
      if(parent == null)
         throw new IllegalArgumentException("parent must not be null");

      m_parentPanel = parent;
      init();
   }

   /**
    * Initializes the panel.
    */
   private void init()
   {
      if (null == ms_res)
         ms_res = ResourceBundle.getBundle(getClass().getName() + "Resources",
            Locale.getDefault());

      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
      JPanel mainPropertyPanel = new JPanel();
      mainPropertyPanel.setLayout(new BoxLayout(mainPropertyPanel,
         BoxLayout.Y_AXIS));

      PSPropertyPanel propertyPanel = new PSPropertyPanel();
      propertyPanel.setAlignmentX(PSPropertyPanel.RIGHT_ALIGNMENT);
      mainPropertyPanel.add(propertyPanel);

      m_labelField = new UTFixedHeightTextField();
      JComponent[] nameComp = {m_labelField};
      propertyPanel.addPropertyRow(ms_res.getString("text.name.label"),
         nameComp, m_labelField, 
         ms_res.getString("text.name.label.mn").charAt(0), 
         null);

      m_internalNameField = new UTFixedHeightTextField();
      JComponent[] labelComp = {m_internalNameField};
      propertyPanel.addPropertyRow(ms_res.getString("text.internalname.label"),
         labelComp, m_internalNameField, 
         ms_res.getString("text.internalname.label.mn").charAt(0), 
         null);


      //Menu Options Panel
      JPanel menuOptionsPanel = new JPanel();
      menuOptionsPanel.setLayout(new BoxLayout(menuOptionsPanel,
            BoxLayout.X_AXIS));

      //Description panel
      JPanel descPanel = new JPanel();
      descPanel.setLayout(new BorderLayout());

      descPanel.setBorder(PSDialog.createGroupBorder(ms_res.getString(
         "textarea.border.desc")));

      Font font = new Font("Arial", Font.PLAIN, 12);
      
      m_desc = new JTextArea();
      m_desc.setFont(font);
      m_desc.setLineWrap(true);
      m_desc.setWrapStyleWord(true);
      m_desc.setEditable(true);
      JScrollPane areaScrollPane = new JScrollPane(m_desc);
      areaScrollPane.setPreferredSize(new Dimension(100, 100));
      descPanel.add(areaScrollPane, BorderLayout.CENTER);

      mainPropertyPanel.setAlignmentX(JPanel.CENTER_ALIGNMENT);

      add(mainPropertyPanel);
      add(Box.createVerticalStrut(5));
      add(descPanel);
      add(Box.createVerticalStrut(5));
      add(menuOptionsPanel);
      EmptyBorder emptyBorder = new EmptyBorder(10, 10, 10, 10);
      setBorder(emptyBorder);
   }

   public void load(Object data)
   {
      if(data instanceof PSDisplayFormat)
      {
         m_dispFormat = (PSDisplayFormat)data;
         m_labelField.setText(m_dispFormat.getDisplayName());
         m_desc.setText(m_dispFormat.getDescription());
         m_internalNameField.setText(m_dispFormat.getInternalName());

         if(m_dispFormat.isPersisted())
            m_internalNameField.setEnabled(false);
         else
            m_internalNameField.setEnabled(true);

      }
   }

   public boolean save()
   {
      boolean valid = validateData();
      if (valid)
      {
         m_dispFormat.setDescription(m_desc.getText());
         m_dispFormat.setDisplayName(m_labelField.getText());
         m_dispFormat.setInternalName(m_internalNameField.getText().trim());
         m_internalNameField.setEnabled(false);
      }
      return valid;
   }

   // see interface for desc
   public boolean validateData()
   {
      // validate in the order the components appear on the screen
      
      //First, Display Name
      String s = m_labelField.getText();
      boolean valid = true;
      try
      {
         if (s == null || s.trim().length() == 0)
         {
            PSDlgUtil.showErrorDialog(ms_res.getString("error.msg.missinglabelname"),
                  ms_res.getString("error.title.missinglabelname"));
            valid = false;
         }

         // Validate display name data length
         if (s.length() > PSDisplayFormat.DISPLAYNAME_LENGTH)
         {
            PSDlgUtil.showErrorDialog(ms_res.getString("error.msg.dataexceedlength"),
                  ms_res.getString("error.title.dataexceedlength"));
            m_internalNameField.requestFocus();
            valid = false;
         }
      }
      finally
      {
         if(!valid)
         {
            m_labelField.setText(m_dispFormat.getDisplayName());
            m_labelField.requestFocus();
            return valid;
         }
      }

      //Second, Internal Name
      s = m_internalNameField.getText();
      valid = true;
      try
      {
         if (s == null || s.trim().length() == 0)
         {
            PSDlgUtil.showErrorDialog(
               ms_res.getString("error.msg.missinginternalname"),
               ms_res.getString("error.title.missinginternalname"));
            valid = false;
         }
   
         // Validate internal name data length
         if (s.length() > PSDisplayFormat.INTERNALNAME_LENGTH)
         {
            PSDlgUtil.showErrorDialog(
               ms_res.getString("error.msg.dataexceedlength"),
               ms_res.getString("error.title.dataexceedlength"));
            valid = false;
         }

         // Attempt to tokenize the internal name
         // to detect any spaces
         StringTokenizer st = new StringTokenizer(s);
   
         if (st.countTokens() > 1)
         {
            PSDlgUtil.showErrorDialog(
               ms_res.getString("error.msg.nospacesallowedinternalname"),
               ms_res.getString("error.title.invalidinternalname"));
            valid = false;
         }
   
         // check for internal names uniqueness
         if(!isUnique(s))
         {
            PSDlgUtil.showErrorDialog(
               ms_res.getString("err.internalnamenotnunique.msg"),
               ms_res.getString("error.title.invalidinternalname"));
            valid = false;
         }
      }
      finally
      {
         if(!valid)
         {
            m_internalNameField.setText(m_dispFormat.getInternalName());
            m_internalNameField.requestFocus();
            return valid;
         }
      }

      // Third, validate description data length
      if (m_desc.getText().length() > PSDisplayFormat.DESCRIPTION_LENGTH)
      {
         PSDlgUtil.showErrorDialog(
            ms_res.getString("error.msg.dataexceedlength"),
            ms_res.getString("error.title.dataexceedlength"));

         m_desc.setText(m_dispFormat.getDescription());
         m_desc.requestFocus();
         return false;
      }

      return true;
   }

   /**
    * Is the specified name unique in the component collection? This checks the
    * <code>PSDisplayFormatCollection</code> list for the existance of the
    * specified internalName.
    *
    * @param internalName the string for which to check in the
    * <code>PSDisplayFormatCollection</code>.  Assumed not <code>null</code> or
    * empty.
    *
    * @return <code>true</code> if it finds the name in the collection,  if it
    * does not find the name in the collection it returns <code>false</code>.
    */
   private boolean isUnique(String internalName)
   {
      Iterator itr = m_parentPanel.getDisplayFormatCollection().iterator();
      PSDisplayFormat displayFormat = null;
      boolean isUnique = true;

      while(itr.hasNext() && isUnique)
      {
         displayFormat = (PSDisplayFormat)itr.next();
         if(internalName.equalsIgnoreCase(displayFormat.getInternalName()) &&
            displayFormat != m_dispFormat)
         {
            isUnique = false;
         }
      }
      return isUnique;
   }


   /**
    * Sets the display label name for the display format node.
    *
    * @param labelName, display label name, assumed to be not <code>null</code>
    * or empty.
    */
   public void setLabelName(String labelName)
   {
      m_labelField.setText(labelName);
   }

   private PSDisplayFormat m_dispFormat;


   private JTextArea m_desc;

   /**
    * Displays Display Format name. Initialized in {@link#init()} never <code>
    * null</code> or modified after that.
    */
   private UTFixedHeightTextField m_labelField;

   /**
    * Displays Display Format internal name, must be unique across all defined
    * Display Format. Initialized in {@link#init()} never <code>null</code> or
    * modified after that.
    */
   private UTFixedHeightTextField m_internalNameField;

   /**
    * Resource bundle for this class. Initialized in {@link init()}.
    * It's not modified after that. Never <code>null</code>, equivalent to a
    * variable declared final.
    */
   private static ResourceBundle ms_res;

   /**
    * Initialized in the contstructor, never <code>null</code> after that and
    * reference is invariant.
    */
   private DisplayFormatTabbedPanel m_parentPanel = null;
}
