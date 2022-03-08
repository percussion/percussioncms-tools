/******************************************************************************
 *
 * [ PSDeploymentStepsDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.deployer.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
* The dialog which displays the steps to follow in the wizard.
*/
public class PSDeploymentStepsDialog  extends PSDeploymentWizardDialog
{
   /**
    * Constructs this dialog calling {@link 
    * PSDeploymentWizardDialog#PSDeploymentWizardDialog(Frame, int, int) 
    * super(parent, step, sequence)}.
    * Additional parameters are described below.
    * Displays the title and description steps according to the type.  
    * 
    * @param type the type of the wizard in which this dialog is used, must be
    * one of the TYPE_xxx values.
    */
   public PSDeploymentStepsDialog(Frame parent, int step, int sequence,
      int type)
   {
      super(parent, step, sequence);
      
      if(type != TYPE_IMPORT && type != TYPE_EXPORT)
         throw new IllegalArgumentException("type is invalid.");      
      
      initDialog(type);
   }
   
   /**
    * Initializes the dialog to display the description of wizard steps. Gets 
    * the description from its properties file based on the type of the wizard  
    * in which it is shown.
    * 
    * @param type the type of the wizard in which this dialog is used, assumed 
    * to be one of the TYPE_xxx values.
    */
   private void initDialog(int type)
   {
      JPanel panel = new JPanel();
      getContentPane().add(panel);
      panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
      EmptyBorder emptyBorder = new EmptyBorder(10, 10, 10, 10);
      panel.setBorder(emptyBorder);
      
      String dlgTitleRes;
      String stepsTitleRes;
      String descriptionRes;
      String stepCount;
      int steps;
      if(type == TYPE_EXPORT)
      {
         dlgTitleRes = "exportTitle";
         stepsTitleRes = "exportStepsTitle";
         descriptionRes = "exportDescription";
         stepCount = "exportDescriptionStepCount";
         steps = 5;
      }
      else
      {
         dlgTitleRes = "importTitle";
         stepsTitleRes = "importStepsTitle";
         descriptionRes = "importDescription";
         stepCount = "importDescriptionStepCount";            
         steps = 3;
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
         getResourceString(stepsTitleRes), description);
      descPanel.setBackground(panel.getBackground());
      for(int i=0; i<descPanel.getComponentCount(); i++)
      {
         if(descPanel.getComponent(i) instanceof JPanel)
         {
            ((JPanel)descPanel.getComponent(i)).setBackground(
               panel.getBackground());
         }
      }
      descPanel.setBorder(new EmptyBorder(5,5,5,5));
      panel.add(descPanel);

      panel.add(createCommandPanel(false));
   
      pack();      
      center();    
      setResizable(true);
   }
   
   //nothing to implement   
   protected void init()
   {
   }   
   
   // see base class
   public Object getDataToSave()
   {
      return null;
   }
}
