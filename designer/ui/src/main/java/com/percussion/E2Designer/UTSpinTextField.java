/*[ UTSpinTextField.java ]******************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer;

import com.percussion.UTComponents.UTFixedButton;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * The UTSpinTextField provides an editor wich allows the user to edit the data
 * field using the up/down arrow buttons.
 */
public class UTSpinTextField extends JPanel 
   implements ValidationConstraint
{
   /**
    * Construct up/down editor panel, the minimal value allowed is
    * Integer.Max_VALUE and the maximal value allowed is Integer.MAX_VALUE.
    *
    * @param label      panel label
    * @param data      the initial data
    */
   public UTSpinTextField(String label, Integer data)
      throws IllegalArgumentException
   {
      if (label == null || data == null)
         throw new IllegalArgumentException("illegal null arguments");

      initSpinTextField(label, data, Integer.MIN_VALUE, Integer.MAX_VALUE);
   }

   /**
    * Construct up/down editor panel with the provided edit limits.
    *
    * @param label      panel label
    * @param data      the initial data
    * @param min         the lower edit limit
    * @param max         the upper edit limit
    */
   public UTSpinTextField(
      String label,
      Integer data,
      Integer min,
      Integer max)
      throws IllegalArgumentException
   {
      if (label == null || data == null || min == null || max == null)
         throw new IllegalArgumentException("illegal null arguments");

      initSpinTextField(label, data, min.intValue(), max.intValue());
   }

   /**
    * Returns the data field.
    *
    * @param      label      the controls label
    * @param      data      the initial data
    * @param      min         the minimal allowed value
    * @param      max         the maximal allowed value
    */
   private void initSpinTextField(
      String label,
      Integer data,
      int min,
      int max)
   {
      // get the resources
      try
      {
         m_res =
            ResourceBundle.getBundle(
               "com.percussion.E2Designer.UTSpinTextFieldResources",
               Locale.getDefault());
      }
      catch (MissingResourceException ex)
      {
         System.out.println(ex);
      }

      // save the min/max values
      m_min = min;
      m_max = max;
      m_default = data.intValue();

      // set layout
      setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

      if (label != null)
      {
         m_panelLabel = new JLabel(label);
         add(m_panelLabel);
      }
      m_dataField = new UTFixedTextField(data.toString(), DATAFIELD_SIZE);
      m_dataField.setHorizontalAlignment(SwingConstants.RIGHT);
      setDirectEditingAllowed(true);
      m_dataField.setEnabled(true);
      m_dataField.setNumericDataOnly(true);
      m_dataField.setBackground(Color.white);
      m_dataField.setForeground(Color.black);

      add(m_dataField);
      add(createSpinPanel());
   }

   /**
    * Create the spin button panel.
    *
    * @return   JPanel         a flow panel containing the dialogs type.
    */
   private JPanel createSpinPanel()
   {
      m_spinTimer = new Timer(SPIN_TIMER_INTERVAL_START, new ActionListener()
      {
         public void actionPerformed(ActionEvent event)
         {
            if (m_spinTimer.getDelay() > SPIN_TIMER_INTERVAL_MIN)
               m_spinTimer.setDelay(
                  m_spinTimer.getDelay() - SPIN_TIMER_INTERVAL_MIN);

            if (m_up)
               incrementData();
            else
               decrementData();
         }
      });

      m_upButton =
         new UTFixedButton(
            new ImageIcon(
               getClass().getResource(m_res.getString("gif_upButton"))),
            SPIN_BUTTON_SIZE);
      m_upButton.addMouseListener(new MouseAdapter()
      {
         public void mouseClicked(MouseEvent event)
         {
            if (m_upButton.isEnabled())
               incrementData();
         }
         public void mousePressed(MouseEvent event)
         {
            if (m_upButton.isEnabled())
            {
               m_up = true;
               m_spinTimer.setDelay(SPIN_TIMER_INTERVAL_START);
               m_spinTimer.start();
            }
         }
         public void mouseReleased(MouseEvent event)
         {
            if (m_upButton.isEnabled())
               m_spinTimer.stop();
         }
      });

      m_downButton =
         new UTFixedButton(
            new ImageIcon(
               getClass().getResource(m_res.getString("gif_downButton"))),
            SPIN_BUTTON_SIZE);
      m_downButton.addMouseListener(new MouseAdapter()
      {
         public void mouseClicked(MouseEvent event)
         {
            if (m_downButton.isEnabled())
               decrementData();
         }
         public void mousePressed(MouseEvent event)
         {
            if (m_downButton.isEnabled())
            {
               m_up = false;
               m_spinTimer.setDelay(SPIN_TIMER_INTERVAL_START);
               m_spinTimer.start();
            }
         }
         public void mouseReleased(MouseEvent event)
         {
            if (m_downButton.isEnabled())
               m_spinTimer.stop();
         }
      });

      JPanel panel = new JPanel();
      panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
      panel.add(m_upButton);
      panel.add(m_downButton);

      return panel;
   }

   /** Adding a listener to watch for incremental/decremental changes in the
    * m_dataField.
    */
   public void addChangeListener(ChangeListener c)
   {
      m_listener = c;
      if ((c != null) && (m_changeEvent == null))
      { // we only want to create this once
         m_changeEvent = new ChangeEvent(m_dataField);
      }
   }

   /** 
    * Removes the listener.
    */
   public void removeChangeListener(ChangeListener c)
   {
      if (m_listener == c)
         m_listener = null;
   }

   /** 
    * Clears the value in the text box.
    */
   public void clear()
   {
      m_dataField.setText("");
   }

   /** @return int returns the TextField default value.
      */
   public int getDefault()
   {
      return m_default;
   }

   /** 
    * @param min sets the TextField minimum to min.
    */
   public void setDefault(int defaultVal)
   {
      m_default = defaultVal;
   }

   /** 
    * @return int returns the TextField Minimum.
    */
   public int getMin()
   {
      return m_min;
   }

   /** 
    * @param min sets the TextField minimum to min.
    */
   //////////////////////////////////////////////////////////////////////////////
   public void setMin(int min)
   {
      m_min = min;
   }

   /** @return int returns the TextField Maximum.
   */
   //////////////////////////////////////////////////////////////////////////////
   public int getMax()
   {
      return m_max;
   }

   /** 
    * @param min sets the TextField maximum to max.
    */
   public void setMax(int max)
   {
      m_max = max;
   }

   /**
    * @returns JLabel Gets the label in front of the SpinTextField.
    */
   public JLabel getLabel()
   {
      return m_panelLabel;
   }

   /** 
    * @param label A JLabel to be set in front of the SpinTextField.
    */
   public void setLabel(JLabel label)
   {
      m_panelLabel = label;
   }

   /**
    * Returns the contents of data field.
    *
    * @return   int    the data
    */
   public Integer getData()
   {
      if (m_dataField.getText().equals(""))
         return null;
      else
         return new Integer(m_dataField.getText());
   }

   /**
    * Set the contents of data field
    *
    * @param   int    the data
    */
   public void setData(int data)
   {
      m_dataField.setText(String.valueOf(data));
   }

   /**
    * Returns true if the data was modified in the current edit session, false
    * otherwise
    *
    * @return   boolean    true if modified
    */
   public boolean isModified()
   {
      return m_bModified;
   }

   /**
   * Increment the data field by one
   *
   */
   private void incrementData()
   {
      int current;

      try
      {
         current = Integer.parseInt(m_dataField.getText());
      }
      catch (NumberFormatException e)
      {
         current = m_default;
      }

      if (current < m_max)
      {
         m_dataField.setText(String.valueOf(current + 1));

         if (null != m_listener)
            m_listener.stateChanged(m_changeEvent);
      }
   }

   /**
    * Decrement the data field by one
    *
    */
   private void decrementData()
   {
      int current;

      try
      {
         current = Integer.parseInt(m_dataField.getText());
      }
      catch (NumberFormatException e)
      {
         current = m_default;
      }

      if (current > m_min)
      {
         m_dataField.setText(String.valueOf(current - 1));

         if (null != m_listener)
            m_listener.stateChanged(m_changeEvent);
      }
   }

   /**
    * Enables/disables the whole control according to the passed flag.
    *
    * @param   bEnabled   the state into which th econtrol has to set
    */
   public void setEnabled(boolean bEnabled)
   {
      if (isDirectEditAllowed())
         m_dataField.setEnabled(bEnabled);

      if (bEnabled)
         m_dataField.setForeground(Color.black);
      else
         m_dataField.setForeground(Color.lightGray);

      m_upButton.setEnabled(bEnabled);
      m_downButton.setEnabled(bEnabled);
      m_panelLabel.setEnabled(bEnabled);
   }

   /**
   * Allow direct editing of data field.
   *
   * @param   bEnabled   the state into which th econtrol has to set
   */
   public void setDirectEditingAllowed(boolean bAllowed)
   {
      m_bAllowed = bAllowed;
      if (m_bAllowed)
         m_dataField.setBackground(Color.white);
      else
         m_dataField.setBackground(Color.lightGray);

      m_dataField.setEnabled(bAllowed);
   }

   /**
    * @returns boolean <CODE>true</CODE> = the datafield accepts numeric data
    * only.
    */
   public boolean isNumericDataOnly()
   {
      return m_dataField.isNumericDataOnly();
   }

   /**
    * @param b <CODE>true</CODE> = toggles the datafield to numeric data only.
    */
   public void setNumericDataOnly(boolean b)
   {
      m_dataField.setNumericDataOnly(b);
   }

   /**
    * Set the text field valus.
    *
    * @param   value   the value to set
    */
   public void setValue(int value)
   {
      m_dataField.setText(String.valueOf(value));
   }

   /**
    * Returns the information wether direct edit is allowed or not.
    *
    * @return boolean   true if allowed, false otherwise
    */
   public boolean isDirectEditAllowed()
   {
      return m_bAllowed;
   }

   // implementation of ValidationConstraint
   public String getErrorText()
   {
      return m_error;
   }

   public void checkComponent(Object suspect) throws ValidationException
   {
      try
      {
         Integer val = getData();
         if (null == val)
            throw new NumberFormatException();
         int iValue = val.intValue();
         if (iValue < m_min || iValue > m_max)
         {
            m_error = m_res.getString("outOfLimits");
            throw new ValidationException(
               "Minimal value: " + m_min + " Maximal value: " + m_max);
         }
      }
      catch (NumberFormatException e)
      {
         m_error = m_res.getString("notANumber");
         throw new ValidationException();
      }
   }

   /*
    * class resources
    */
   private static ResourceBundle m_res = null;
   
   /*
    * up/down button size
    */
   private static final Dimension SPIN_BUTTON_SIZE = new Dimension(14, 14);
   
   /*
    * data field size
    */
   private static final Dimension DATAFIELD_SIZE = new Dimension(40, 20);
   
   /*
    * up/down button size
    */
   private boolean m_bAllowed = false;
   
   /*
    * the spin panel label
    */
   private JLabel m_panelLabel = null;
   
   /*
    * the data field to be spinned
    */
   private UTFixedTextField m_dataField = null;
   
   /*
    * the spin button timer and its direction flag
    */
   private boolean m_up = true;
   private Timer m_spinTimer = null;
   
   /*
    * the up button
    */
   private UTFixedButton m_upButton = null;
   
   /*
    * the down button
    */
   private UTFixedButton m_downButton = null;
   
   /*
    * the modified flag
    */
   private boolean m_bModified = false;
   
   /*
    * the minimal value
    */
   private int m_min;
   
   /*
    * the maximal value
    */
   private int m_max;
   
   /*
    * the starting value; default value.
    */
   private int m_default;
   
   /*
    * the spin button interval (in ms)
    */
   private static final int SPIN_TIMER_INTERVAL_MIN = 20;
   private static final int SPIN_TIMER_INTERVAL_START = 200;
   
   /**
    * the error message
    */
   private String m_error = new String("Unknown error");
   
   /**
    * Used with incrementData and decrementData methods to listen for the data
    * change in the m_dataField.
    */
   private ChangeListener m_listener = null;

   /**
    * When a change listener is registered, we'll construct the change
    * event just once and reuse it. Since the change event references
    * the object directly (m_dataField) this should have no adverse side
    * effects and has a very big positive effect (eliminates many allocs).
    */
   private ChangeEvent m_changeEvent = null;
}
