/******************************************************************************
 *
 * [ PSRepositoryAliasDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.deployer.ui;

import com.percussion.guitools.ErrorDialogs;
import com.percussion.guitools.PSDialog;
import com.percussion.guitools.PSPropertyPanel;
import com.percussion.validation.ListMemberConstraint;
import com.percussion.validation.StringConstraint;
import com.percussion.validation.ValidationConstraint;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.MessageFormat;
import java.util.List;
import java.util.StringTokenizer;

/**
 * The dialog to present to user to enter the alias for the repository.
 */
public class PSRepositoryAliasDialog extends PSDialog
{
   /**
    * Constructs this object. 
    * 
    * @param parent the parent frame of this dialog, may be <code>
    * null</code>
    * @param dataSource the datasource to be set may not be <code>null</code> 
    * @param alias the default alias to set, may not be <code>null</code> or
    * empty.
    * @param existingAliases the list of existing aliases, may not be <code>
    * null</code>, can be empty.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    */
   public PSRepositoryAliasDialog(Frame parent, String dataSource, String alias, 
      List existingAliases)
   {
      super(parent);
      
      init(dataSource, alias, existingAliases);           
   }
   
   /**
    * Constructs this object. 
    * 
    * @param dataSource the datasource to be set may not be <code>null</code> 
    * @param parent the parent dialog of this dialog, may be <code>
    * null</code>
    * @param alias the default alias to set, may not be <code>null</code> or
    * empty.
    * @param existingAliases the list of existing aliases, may not be <code>
    * null</code>, can be empty.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    */
   public PSRepositoryAliasDialog(Dialog parent, String dataSource, String alias, 
      List existingAliases)
   {
      super(parent);
      
      init(dataSource, alias, existingAliases);       
   }
   
   /**
    * Initializes this object's members and creates the dialog framework.
    * 
    * @param dataSource the datasource to be set may not be <code>null</code>
    * @param alias the default alias to set, may not be <code>null</code> or
    * empty.
    * @param existingAliases the list of existing aliases, may not be <code>
    * null</code>, can be empty.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    */
   private void init(String dataSource, String alias, List existingAliases)
   {
      if(dataSource == null || dataSource.trim().length() == 0)
         throw new IllegalArgumentException(
            "dataSource may not be null or empty.");
      
      if(alias == null || alias.trim().length() == 0)
         throw new IllegalArgumentException(
            "alias may not be null or empty.");
            
      if(existingAliases == null)
         throw new IllegalArgumentException(
            "existingAliases may not be null.");
         
      m_alias = alias;
      m_dataSource = dataSource;
      m_existingAliases = existingAliases;       
      
      initDialog();    
   }
   
   /**
    * Creates the validation framework and sets it in the parent dialog. Sets 
    * the following validations.
    * <ol>
    * <li>alias field is not empty and not duplicate of existing aliases</li>
    * </ol>
    */
   private void initValidationFramework()
   {
      Component [] c = new Component[]
      {
         m_aliasField,
         m_aliasField,
      };

      ValidationConstraint nonEmpty = new StringConstraint();
      ValidationConstraint [] v = new ValidationConstraint[]
      {
         nonEmpty,
         new ListMemberConstraint(m_existingAliases),
      };

      setValidationFramework( c, v );
   }
   
   /**
    * Initializes the dialog framework and sets data in controls.
    */
   private void initDialog()
   {
      setTitle(getResourceString("title"));
      JPanel mainPanel = new JPanel(new BorderLayout());
      
      JPanel panel = new JPanel();
      
      panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
      panel.setBorder(new EmptyBorder(10,10,10,10));
      
      JPanel descriptionPanel = new JPanel();
      descriptionPanel.setBorder(new EmptyBorder(10,10,10,10));
      descriptionPanel.setLayout(
         new BoxLayout(descriptionPanel, BoxLayout.Y_AXIS));
      descriptionPanel.setAlignmentX(LEFT_ALIGNMENT);
      
      String description = ErrorDialogs.cropErrorMessage(MessageFormat.format(
         getResourceString("description"), new String[]{m_dataSource, m_alias}));
         
      StringTokenizer st = new StringTokenizer(description, "\n");      
      while(st.hasMoreTokens())
      {
         JLabel label = new JLabel(st.nextToken(), SwingConstants.LEFT);
         descriptionPanel.add(label);
      }      
      panel.add(descriptionPanel);
      panel.add(Box.createVerticalStrut(20));         
      panel.add(Box.createVerticalGlue());  
         
      PSPropertyPanel aliasPanel = new PSPropertyPanel();
      aliasPanel.addPropertyRow(getResourceString("aliasName"), 
         new JComponent[]{m_aliasField});
      m_aliasField.setText(m_alias);
      aliasPanel.setBorder(new EmptyBorder(5,10,5,10));
      aliasPanel.setAlignmentX(LEFT_ALIGNMENT);
      panel.add(aliasPanel);     
      panel.add(Box.createVerticalStrut(20));         
      panel.add(Box.createVerticalGlue());  
   
      JPanel commandPanel = new JPanel();
      commandPanel.setLayout(new BorderLayout());
      commandPanel.setBorder(new EmptyBorder(5,5,8,5));
      commandPanel.add( createCommandPanel(SwingConstants.HORIZONTAL,true), 
                        BorderLayout.EAST);
   
      mainPanel.add(panel, BorderLayout.NORTH);
      mainPanel.add(commandPanel, BorderLayout.SOUTH);
        
      getContentPane().add(mainPanel);
      pack();
      setResizable(true);
      center();
   }
   
   /**
    * Validates that user enters an alias name and updates the user entered
    * alias name.
    */
   public void onOk()
   {
      initValidationFramework();
      
      if(!activateValidation())
         return;
      
      m_alias = m_aliasField.getText();
      
      super.onOk();
   }
   
   /**
    * Gets the user entered alias.
    * 
    * @return the alias name, never <code>null</code> or empty.
    */
   public String getAlias()
   {
      return m_alias;
   }
   
   /**
    * The text field to enter the alias name, never <code>null</code> or 
    * modified after construction.
    */
   private JTextField m_aliasField = new JTextField();
   
   /**
    * The list of exisiting aliases,  never <code>null</code> or modified 
    * after construction.
    */
   private List m_existingAliases;
   
   /**
    * The datasource name set by the user in this dialog, never <code>null</code> or 
    * empty, initialized in the constructor and modified and modified during 
    * call to <code>onOk</code>
    */
   private String m_dataSource;

   /**
    * The alias set by the user in this dialog, never <code>null</code> or 
    * empty, initialized in the constructor and modified and modified during 
    * call to <code>onOk</code>
    */
   private String m_alias;
}
