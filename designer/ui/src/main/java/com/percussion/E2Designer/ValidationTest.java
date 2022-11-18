/******************************************************************************
 *
 * [ ValidationTest.java ]
 * 
 * COPYRIGHT (c) 1999 - 2004 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/** A Testing class for the ValidationFramework and PSDialog.  This Object
  * creates a dialog that asks for a number within a range, a field that is not
  * empty, and a field without specified characters listed in the Invalid Character
  * field.  2 more fields are used to specify the max and the min for the number,
  * and 1 more field to specify the invalid characters described earlier.
  *
*/

public class ValidationTest extends PSDialog implements ActionListener
{
  public ValidationTest()
  {
    super();

    setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

    m_numbers = new JTextField(10);
    m_numbersLabel = new JLabel("Numbers");
    JPanel one = new JPanel(new GridLayout(1,2));
    one.add(m_numbersLabel);
    one.add(m_numbers);

    m_spaces = new JTextField(10);
    m_spacesLabel = new JLabel("Spaces");
    JPanel two = new JPanel(new GridLayout(1,2));
    two.add(m_spacesLabel);
    two.add(m_spaces);

    m_chars = new JTextField(10);
    m_charsLabel = new JLabel("Spec. Chars");
    JPanel three = new JPanel(new GridLayout(1,2));
    three.add(m_charsLabel);
    three.add(m_chars);

    m_range1 = new JTextField(10);
    m_range1Label = new JLabel("Set Max");
    JPanel four = new JPanel(new GridLayout(1,2));
    four.add(m_range1Label);
    four.add(m_range1);

    m_range2 = new JTextField(10);
    m_range2Label = new JLabel("Set Min");
    JPanel five = new JPanel(new GridLayout(1,2));
    five.add(m_range2Label);
    five.add(m_range2);

    m_invalid = new JTextField(10);
    m_invalidLabel = new JLabel("Set invalid char");
    JPanel six = new JPanel(new GridLayout(1,2));
    six.add(m_invalidLabel);
    six.add(m_invalid);

    m_ok = new JButton("Validate!");
    m_ok.setActionCommand("ok");
    m_ok.addActionListener(this);

    getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
    getContentPane().add(one);
    getContentPane().add(two);
    getContentPane().add(three);
    getContentPane().add(Box.createVerticalStrut(15));
    getContentPane().add(four);
    getContentPane().add(five);
    getContentPane().add(six);
    getContentPane().add(m_ok);
  }

/** Creates new validation information every time the user pushes the button.
  *
*/

  public void actionPerformed(ActionEvent e)
  {
      if (e.getActionCommand().equals("ok"));
      {
         m_invalidChars = new String(m_invalid.getText());

// initializing Constraints
    // /*
         try {
         iConst1 = new IntegerConstraint(new Integer(m_range1.getText()).intValue(),
                                         new Integer(m_range2.getText()).intValue());
         }

         catch(NumberFormatException exc)
         {}
    // */

  // iConst1 = new IntegerConstraint(new Integer(m_range1.getText()).intValue(), Integer.MAX_VALUE);

         iConst2 = new IntegerConstraint();
         iConst3 = new IntegerConstraint();

         sConst1 = new StringConstraint();
         sConst2 = new StringConstraint(m_invalidChars);


         vList[0] = iConst2;    cList[0] = m_range1;
         vList[1] = iConst3;    cList[1] = m_range2;

         vList[2] = iConst1;    cList[2] = m_numbers;
         vList[3] = sConst1;    cList[3] = m_spaces;
         vList[4] = sConst2;    cList[4] = m_chars;

         setValidationFramework(cList, vList);
         if (!activateValidation())
            return;
      }

  }

  public static void main(String[] args)
  {
    ValidationTest v = new ValidationTest();

    v.setSize(150, 200);

    v.setResizable(true);
    v.pack();
    v.setVisible(true);
  }

  private JButton    m_ok;
  private JTextField m_numbers;  private JLabel m_numbersLabel;
  private JTextField m_spaces;   private JLabel m_spacesLabel;
  private JTextField m_chars;    private JLabel m_charsLabel;
  private JTextField m_range1;   private JLabel m_range1Label;
  private JTextField m_range2;   private JLabel m_range2Label;

  private String m_invalidChars;
  private JTextField m_invalid;  private JLabel m_invalidLabel;

  private Component[] cList = new Component[5];
  private ValidationConstraint[] vList = new ValidationConstraint[5];
  private IntegerConstraint iConst1, iConst2, iConst3;
  private StringConstraint  sConst1, sConst2;

}

 
