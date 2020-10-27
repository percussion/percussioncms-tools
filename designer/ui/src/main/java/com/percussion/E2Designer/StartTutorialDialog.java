/*[ StartTutorialDialog.java ]*************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;


// This dialog is used to change the types of files that show up in the file tab

public class StartTutorialDialog extends PSDialog
{
  /** Constructor which takes the JFrame object.
   *
   */
   public StartTutorialDialog( JFrame frame)
   {
       super( frame );
      setTitle(getResources().getString("title"));
      
      m_commandPanel = new UTStandardCommandPanel(this, "", SwingConstants.VERTICAL)
      {
         public void onOk()
         {
            m_bOk = true;
        StartTutorialDialog.this.onHelp();
            UserConfig.getConfig().setValue(START_INITIALS, m_txtInitials.getText());
            dispose();
         }
      };
    m_commandPanel.getOkButton().setPreferredSize(BUTTON_SIZE);
    m_commandPanel.getCancelButton().setPreferredSize(BUTTON_SIZE);
    m_commandPanel.getHelpButton().setPreferredSize(BUTTON_SIZE);
    m_commandPanel.getHelpButton().setText(getResources().getString("lessonguide"));
      getRootPane().setDefaultButton(m_commandPanel.getOkButton());

      JPanel contentPane = new JPanel(new BorderLayout());

      m_rbLessonOne = new JRadioButton(getResources().getString("lessonone"));      
      m_rbLessonOne.setSelected(true);
      m_rbLessonTwo = new JRadioButton(getResources().getString("lessontwo"));
      m_rbLessonThree = new JRadioButton(getResources().getString("lessonthree"));      
      m_rbLessonFour = new JRadioButton(getResources().getString("lessonfour"));
   
       m_bgGroup = new ButtonGroup();
       m_bgGroup.add(m_rbLessonOne);
       m_bgGroup.add(m_rbLessonTwo);
       m_bgGroup.add(m_rbLessonThree);
       m_bgGroup.add(m_rbLessonFour);

      JPanel typePane = new JPanel();
      typePane.setLayout(new GridLayout(2,2));
      typePane.add(m_rbLessonOne);
      typePane.add(m_rbLessonTwo);
      typePane.add(m_rbLessonThree);
      typePane.add(m_rbLessonFour);
      typePane.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED),
                                     getResources().getString("typetitle")));


      m_txtInitials = new UTFixedTextField(new String(""));
      
      String strInitials = UserConfig.getConfig().getValue(START_INITIALS);
      if(strInitials != null)
         m_txtInitials.setText(strInitials);
      
      m_txtName = new UTFixedTextField(new String(""));
      m_txtName.setEnabled(false);
      JPanel paneInitials = new JPanel();
      JPanel paneName = new JPanel();
            
      m_lblInitials = new JLabel(getResources().getString("initials"));
      m_lblName = new JLabel(getResources().getString("name"));
      paneInitials.add(m_lblInitials);
      paneName.add(m_lblName);

      JPanel namePane = new JPanel();
      namePane.setLayout(new BoxLayout(namePane, BoxLayout.Y_AXIS));
      namePane.add(paneInitials);
      namePane.add(m_txtInitials);
      namePane.add(paneName);
      namePane.add(m_txtName);
      
      contentPane.add(m_commandPanel, "East");
      JPanel centerPanel = new JPanel(new BorderLayout());
      centerPanel.add(typePane, "North");
      centerPanel.add(namePane, "Center");
      centerPanel.setBorder(new EmptyBorder(5,5,5,5));
      contentPane.add(centerPanel, "Center");
      
        getContentPane().setLayout(new BorderLayout());
      contentPane.setBorder(new EmptyBorder(5,5,5,5));
      getContentPane().add(contentPane);
      this.setSize(new Dimension(1000,1000));
      pack();
        center();
      
      m_txtInitials.addKeyListener(new KeyAdapter()
      {
         public void keyReleased(KeyEvent e)
         {
            updateName();            
         }      
      });
      
      m_rbLessonOne.addChangeListener(new ChangeListener()
      {
         public void stateChanged(ChangeEvent e)
         {
               updateName();
         }
      });

      m_rbLessonTwo.addChangeListener(new ChangeListener()
      {
         public void stateChanged(ChangeEvent e)
         {
               updateName();
         }
      });

      m_rbLessonThree.addChangeListener(new ChangeListener()
      {
         public void stateChanged(ChangeEvent e)
         {
               updateName();
         }
      });

      m_rbLessonFour.addChangeListener(new ChangeListener()
      {
         public void stateChanged(ChangeEvent e)
         {
               updateName();
         }
      });

      updateName();
      setVisible(true);
   }
   
   public boolean wasOKPressed()
   {
      return(m_bOk);
   }
   
   private void updateName()
   {
      String strName = m_txtInitials.getText() + "TutorialLesson";
      if(m_rbLessonOne.isSelected())
         strName += getResources().getString("one");
      else if(m_rbLessonTwo.isSelected())
         strName += getResources().getString("two");
      else if(m_rbLessonThree.isSelected())
         strName += getResources().getString("three");
      else if(m_rbLessonFour.isSelected())
         strName += getResources().getString("four");

      m_txtName.setText(strName);      
   }
   
  public String getTutorialName()
  {
     return(m_txtName.getText());
  }
  

/**
 * @returns int The lesson number selected.
*/
  public int getLessonSelected()
  {
    if (isOneSelected())
         return 1;
    else if (isTwoSelected())
      return 2;
    else if (isThreeSelected())
      return 3;
    else
      return 4;  
  }

  public boolean isOneSelected()
  {
     return(m_rbLessonOne.isSelected());
  }
  
  public boolean isTwoSelected()
  {
     return(m_rbLessonTwo.isSelected());
  }

  public boolean isThreeSelected()
  {
     return(m_rbLessonThree.isSelected());
  }

  public boolean isFourSelected()
  {
     return(m_rbLessonFour.isSelected());
  }


   /**
    * Appends a number that corresponds to the active lesson, 1 based.
   **/
   protected String subclassHelpId( String helpId )
   {
      return helpId + getLessonSelected();
   }


  private UTStandardCommandPanel m_commandPanel = null;
  private boolean m_bOk = false;
  private JRadioButton m_rbLessonOne, m_rbLessonTwo, m_rbLessonThree, m_rbLessonFour;
  private ButtonGroup m_bgGroup;
  private UTFixedTextField m_txtInitials, m_txtName;
  private JLabel m_lblInitials, m_lblName;
  public static final String START_INITIALS = "start_tut_initials";
  private static final Dimension BUTTON_SIZE = new Dimension(120, 24); 
}
