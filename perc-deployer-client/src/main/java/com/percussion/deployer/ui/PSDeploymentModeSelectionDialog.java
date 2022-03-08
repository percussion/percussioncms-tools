/******************************************************************************
 *
 * [ PSDeploymentModeSelectionDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.deployer.ui;

import com.percussion.deployer.objectstore.PSArchiveDetail;
import com.percussion.deployer.objectstore.PSDeployableElement;
import com.percussion.deployer.objectstore.PSImportDescriptor;
import com.percussion.deployer.objectstore.PSImportPackage;
import com.percussion.guitools.ErrorDialogs;
import com.percussion.guitools.PSPropertyPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;

/**
* The dialog to choose the mode of export or import process.
*/
public class PSDeploymentModeSelectionDialog  extends PSDeploymentWizardDialog
{
   /**
    * Constructs this dialog calling {@link 
    * PSDeploymentWizardDialog#PSDeploymentWizardDialog(Frame, int, int) 
    * super(parent, step, sequence)}.  Additional parameter is described
    * below.
    *
    * @param type the type of the wizard in which this dialog is used, must be
    * one of the TYPE_xxx values.
    */
   public PSDeploymentModeSelectionDialog(Frame parent, int step, int sequence, 
      int type)
   {
      super(parent, step, sequence);
      if(type != TYPE_IMPORT && type != TYPE_EXPORT)
         throw new IllegalArgumentException("type is invalid.");      
         
      initDialog(type);
      
      m_type = type;      
   }

   /**
    * Constructs this dialog calling {@link 
    * PSDeploymentModeSelectionDialog#PSDeploymentModeSelectionDialog(Frame,
    * int, int, int)}. Additional parameter is described below.
    * 
    * @param type the type of the wizard in which this dialog is used, must be
    *            one of the TYPE_xxx values.
    */
   public PSDeploymentModeSelectionDialog(Frame parent, int step, int sequence, 
         int type, PSArchiveDetail arcDetail)
   {
      this(parent, step, sequence, type);
      m_archiveDetail = arcDetail;
      m_isImport = true;
   }
   
   /**
    * Creates the dialog framework with the description, radio button and
    * command button panels. The 'Typical' radio button is selected by default.
    * Sets the description panel to display the tile and description of this
    * dialog based on the supplied wizard type. Gets the description from its 
    * properties file based on the type of the wizard in which it is shown. Sets
    * the dialog title also based on the wizard type.
    * 
    * @param the type of the wizard in which this dialog is used, assumed to be
    * one of the TYPE_xxx values.
    */
   protected void initDialog(int type)
   {
      JPanel panel = new JPanel();
      getContentPane().add(panel);
      panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
      EmptyBorder emptyBorder = new EmptyBorder(10, 10, 10, 10);
      panel.setBorder(emptyBorder);
            
      String dlgTitleRes;
      String descTitleRes;
      String descriptionRes;
      String stepCount;
      int steps;
      if(type == TYPE_EXPORT)
      {
         dlgTitleRes = "exportTitle";
         descTitleRes = "exportDescTitle";
         descriptionRes = "exportDescription";
         stepCount = "exportDescriptionStepCount";
         steps = 2;
      }
      else
      {
         dlgTitleRes = "importTitle";
         descTitleRes = "importDescTitle";
         descriptionRes = "importDescription";
         stepCount = "importDescriptionStepCount";            
         steps = 2;
      }
      setTitle(getResourceString(dlgTitleRes));
      
      try 
      {
         steps = Integer.parseInt(getResourceString(stepCount));
      }
      catch (NumberFormatException ex) 
      {
         //uses the default  
      }
      String[] description = new String[steps];
      for (int i = 1; i <= steps; i++) 
      {
         description[i-1] = getResourceString(descriptionRes + i);
      }
   
      JPanel descPanel = createDescriptionPanel(
         getResourceString(descTitleRes), description);
      
      PSPropertyPanel radioPanel = new PSPropertyPanel();
      radioPanel.setAlignmentX(LEFT_ALIGNMENT);
      radioPanel.setBorder(createGroupBorder(
         getResourceString("modeGroupTitle")));

      m_typicalRadio = new JRadioButton(getResourceString("typicalRadio"));
      m_typicalRadio.setMnemonic(getResourceString("typicalRadio.mn").charAt(0));
      m_customRadio = new JRadioButton(getResourceString("customRadio"));
      m_customRadio.setMnemonic(getResourceString("customRadio.mn").charAt(0));
      ButtonGroup group = new ButtonGroup();
      group.add(m_typicalRadio);
      group.add(m_customRadio);      
      
      radioPanel.addControlsRow(m_typicalRadio, null);
      radioPanel.addControlsRow(m_customRadio, null);      
      
      panel.add(descPanel);
      panel.add(Box.createVerticalStrut(10));
      panel.add(radioPanel);
      panel.add(Box.createVerticalStrut(10));      
      panel.add(createCommandPanel(true));
      
      pack();      
      center();    
      setResizable(true);
   }
   
   /**
    * Called by <code>onShow(PSDescriptor)</code> to display this dialog. 
    * Restores any saved data to the dialog state
    */
   protected void init()
   {
      Boolean isTypical = (Boolean)getData();

      if (m_isImport)
      {
         m_importDescriptor = (PSImportDescriptor)m_descriptor;
      }
      
      if (isTypical != null)
      {
         m_isTypical = isTypical.booleanValue();
      }
      
      if (m_isTypical)
         m_typicalRadio.doClick();
      else
         m_customRadio.doClick();
         
      m_typicalRadio.addActionListener(new ActionListener() 
      {
         public void actionPerformed(ActionEvent e) 
         {
            m_isTypical = true;
         }
      });
      
      m_customRadio.addActionListener(new ActionListener() 
      {
         public void actionPerformed(ActionEvent e) 
         {
            m_isTypical = false;
         }
      });
      

   }
   
   //overridden to show different help depending on the context of dialog
   protected String subclassHelpId(String helpId)
   {
      if(helpId == null || helpId.trim().length() == 0)
         throw new IllegalArgumentException("helpId may not be null or empty.");
         
      if(m_type == TYPE_EXPORT)
         return helpId + "_export";
      else
         return helpId + "_import";
   }

   // see base class
   public Object getDataToSave()
   {
      return Boolean.valueOf(m_isTypical);
   }

   // see base class
   public void onBack()
   {      
      setShouldUpdateUserSettings(false);
      super.onBack();
   }
   
   @Override
   public void onNext()
   {
      if (m_isImport)
      {
         if (!validateData())
         {
            String title = getResources().getString("errorTitle");
            String desc = getResources().getString("errorDesc");
            ErrorDialogs.showErrorMessage(this, desc, title);
            return;
         }

         List lst = m_importDescriptor.getImportPackageList();
         lst.clear();
         Iterator itr = m_archiveDetail.getPackages();
         while(itr.hasNext())
         {
            Object obj = itr.next();
            if (obj instanceof PSDeployableElement)
            {
               lst.add(new PSImportPackage((PSDeployableElement)obj));
            }
         }
      }
      super.onNext();
   }

   /**
    * Gets the user selected mode in this dialog. 
    * 
    * @return <code>true</code> if the 'Typical' radio button is selected, 
    * otherwise <code>false</code>
    */
   public boolean isTypicalMode()
   {
      return m_isTypical;
   }
   
   /**
    * The mode of the wizard process, initialized to <code>true</code> to 
    * indicate 'Typical' mode, modified by action listener methods based on user
    * actions. <code>false</code> indicates 'Custom' mode. 
    */
   private boolean m_isTypical = true;   
   
   /**
    * The type of the wizard task for which this dialog is shown. Initialized in
    * the constructor and never modified after that.
    */
   private int m_type;
   
   /**
    * Radio button to indicate typical mode, initialized during construction,
    * never <code>null</code> after that.
    */
   private JRadioButton m_typicalRadio;
   
   /**
    * Radio button to indicate custom mode, initialized during construction,
    * never <code>null</code> after that.
    */
   private JRadioButton m_customRadio;

   /**
    * The descriptor passed into onShow(), casted to an Import descriptor for
    * convenience.
    */
   private PSImportDescriptor m_importDescriptor = null;
   
   /**
    * The archive detail consisting external dbms info, initialized in the 
    * constructor and never <code>null</code> or modified after that.
    */
   private PSArchiveDetail m_archiveDetail;

   /**
    * The mode of the wizard process, initialized to <code>false</code> to 
    * indicate is code is executing for export or import.
    */
   private boolean m_isImport = false;  
}
