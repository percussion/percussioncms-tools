/******************************************************************************
 *
 * [ SettingsPerformancePanel.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

/*******************************************************************************
 * $Id: SettingsPerformancePanel.java 1.9 2001/12/10 19:46:38Z martingenhart Exp $
 *
 * Version Labels  : $Name: $
 *
 * Locked By       : $Locker: $
 *
 * Revision History:
 *
 * $Log: SettingsPerformancePanel.java $
 * Revision 1.9  2001/12/10 19:46:38Z  martingenhart
 * fixed Rx-01-12-0019
 * 
 * Revision 1.8  2000/12/22 15:34:42Z  snezanahasonovic
 * Fix bug Rx-00-11-0007
 * Revision 1.7  2000/12/08 22:22:21Z  martingenhart
 * added user interface to edit user session timeout
 * 
 * Revision 1.6  1999/10/20 14:35:01Z  AlexTeng
 * Fixed bug (Rx-99-10-0031)
 * Made all the SpinTextFields take only numeric data.
 *
 * Revision 1.5  1999/08/11 16:01:41Z  vimalagrawal
 * fixed save related bugs and initialization related  bugs
 *
 *
 * Revision 1.4  1999/07/08 22:41:05Z  AlexTeng
 * Added ITabDataHelper implementation & ITabPaneRetriever
 * interfaces at the different panel classes.
 * Implemented saveTabData() and validateTabData() for ITabDataHelper
 * and getTabbedPane() for ITabPaneRetriever.
 * So these classes now saves to the server.
 * Changed update button to new button group: Ok, close, apply, and
 * help.
 *
 * Revision 1.3  1999/05/13 18:35:26  AlexTeng
 * Fixed Panel clipping problem.
 *
 * Revision 1.2  1999/05/12 17:42:23  AlexTeng
 * Fixed the CheckBox layout problem.
 *
 * Revision 1.1  1999/05/11 13:09:16  alexteng
 * Initial revision
 *
 ******************************************************************************/

package com.percussion.E2Designer.admin;

import com.percussion.E2Designer.UTFixedTextField;
import com.percussion.design.objectstore.PSServerConfiguration;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ResourceBundle;

/** 
 * This panel displays/controls the performance settings in the admin client.
 * Admin settings for server level performance settings are updated by pressing
 * the Update button.
 */
public class SettingsPerformancePanel extends JPanel implements ITabDataHelper
{
   private static final long serialVersionUID = 1L;

   /**
    * Construct this dialog.
    * 
    * @param config The config to use, may not be <code>null</code>.
    */
   public SettingsPerformancePanel(ServerConfiguration config)
   {
      super();

      if (config == null)
         throw new IllegalArgumentException("config may not be null");
      
      m_config = config;

      init();
   }

   //
   // PUBLIC METHODS
   //

   /** Implementation of ISaveTabData.
    */
   public boolean saveTabData()
   {
      if (!m_bModified)
         return false;

      System.out.println("Saving SettingsPerformancePanel data...");
      boolean result=false;

      try
      {
         m_config.setUserSessionEnabled(m_userSession.isSelected());
         m_config.setUserSessionTimeout(
            new Integer(m_userSessionTimeout.getText()).intValue() * SECONDS_PER_MINUTE);
         m_config.setUserSessionWarning(
                 new Integer(m_userSessionWarning.getText()).intValue() * SECONDS_PER_MINUTE);
         m_config.setMaxOpenUserSessions(
            new Integer(m_maxOpenUserSessions.getText()).intValue());

         result=true;
      }
      catch (IllegalArgumentException exc)
      {
         JOptionPane.showMessageDialog(null,
         sm_res.getString("missingnumber"),
         sm_res.getString("error"),
         JOptionPane.ERROR_MESSAGE);

         exc.printStackTrace();
      }
      catch (Exception exc)
      {
         JOptionPane.showMessageDialog(null,
         sm_res.getString("invalidsettings"),
         sm_res.getString("error"),
         JOptionPane.ERROR_MESSAGE);

         exc.printStackTrace();
      }

      ///// todo: set m_bModified to false once Sun fixes the bug with
      //   JTextField events.
      //    m_bModified = false;  // resetting modification flag.
      return result;
   }

   /** Validates data of the fields before data is to be saved. By the
    *  construction of the fields user is allowed to enter only numeric data.
    *  Fields can not be empty and can not contain too large numbers.
    *  If no data was entered an error message box is dispayed,
    *  a cursor is set in a first field where no data was found,
    *  and <CODE>false</CODE> is returned, otherwise <CODE>true</CODE>
    *  is returned meaning data is valid and could be saved.
    *  If the string from the fields are parsable by the Integer constructor
    *  validation succeeds. Else, catches the exception thrown by Integer
    *  constructor, dispays an error message box, sets the cursor in the first
    *  field where such data was encountered, and return <CODE>false</CODE> 
    *  for failing validation.
    *
    */

   public boolean validateTabData()
   {
        JTextField emptyField=null;

      /* Create an array for storing JTextFields.  Everytime a new field is added to
       *  a panel that needs validation for an empty string or unparsable number
       *  it must be added to this array.
       *  Otherwise such a field's data will not be validated.
       */

      JTextField [] fields = { m_userSessionTimeout, m_userSessionWarning, m_maxOpenUserSessions };

      boolean test = true;
      try
      {
         Integer testInt = null;

         for (int j=0; j<fields.length && test; j++)
         {
            String text = null;
            emptyField = fields[j];
            text = fields[j].getText();
            if(0 == text.length())
            {
               String error = "missingnumber";
               showError(error, fields[j]);
               test = false;
            }
            else
            {
               testInt = new Integer(fields[j].getText());
               
               if (fields[j] == m_maxOpenUserSessions && testInt.intValue() < 
                  PSServerConfiguration.MINIMAL_REQUIRED_OPEN_SESSIONS)
               {
                  String error = "greaterThan";
                  showError(error, fields[j]);
                  test = false;
               }
            }
         }
      }
      catch (NumberFormatException e)
      {
         String error="invalidnumber";
         showError(error, emptyField);
         test = false;
      }

       return test;
   }

   //
   // PRIVATE METHODS
   //

   private void onValueChanged()
   {
      m_bModified = true;
   }


   /** Used m_updateButton&apos;s actionPerformed() method for each item with an
    * &quot;Unlimited&quot; checkbox.  Returns -1 if the checkbox is checked.
    * otherwise, return the value within the TextField.
    *
    * @param field The TextField with a limit value.
    * @param box The CheckBox associated with field and the return value is based
    * on if this box is checked.
    * @return The limit value the TextField is supposed to represent.
    */
   private int sortFieldAndBox(JTextField field, JCheckBox box)
   {
      if (box.isSelected())
         return -1;   // -1 means unlimited resource
      else
         return new Integer(field.getText()).intValue();
   }

   /** Used by initializeFields() method to check for unlimited settings, then
    * enable/disable the field and selects/deselects the checkbox.
    *
    * @param field The TextField to enable/disable.
    * @param box The CheckBox related to field to select/deselect.
    * @param value The value retrieved from ServerConfiguration.
    */
   private void coordinateFieldAndBox(JTextField field, JCheckBox box, int value)
   {
      if (value == -1)
      {
         box.setSelected(true);
         field.setText("1");
         field.setEditable(false);
         field.setEnabled(false);
      }
      else
      {
         box.setSelected(false);
         field.setText(new Integer(value).toString());
         field.setEditable(true);
         field.setEnabled(true);
      }
   }

   /** Create the TextFields and their &quot;Unlimited&quot; CheckBoxes.
    */
   private void initializeFields()
   {
      Dimension dField = new Dimension(80, 22);

      // user session management settings
      m_userSessionTimeout = new UTFixedTextField("", dField);
      m_userSessionTimeout.setNumericDataOnly(true);
      m_userSessionTimeout.setEnabled(m_config.isUserSessionEnabled());
      m_userSessionTimeout.setText(new Integer(m_config.getUserSessionTimeout() / SECONDS_PER_MINUTE).toString());
      m_userSessionTimeout.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            onValueChanged();
         }
      });
      
      m_userSessionWarning = new UTFixedTextField("", dField);
      m_userSessionWarning.setNumericDataOnly(true);
      m_userSessionWarning.setEnabled(m_config.isUserSessionEnabled());
      m_userSessionWarning.setText(new Integer(m_config.getUserSessionWarning() / SECONDS_PER_MINUTE).toString());
      m_userSessionWarning.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            onValueChanged();
         }
      });


      // user session management settings
      m_maxOpenUserSessions = new UTFixedTextField("", dField);
      m_maxOpenUserSessions.setNumericDataOnly(true);
      m_maxOpenUserSessions.setEnabled(m_config.isUserSessionEnabled());
      m_maxOpenUserSessions.setText(new Integer(
         m_config.getMaxOpenUserSessions()).toString());
      m_maxOpenUserSessions.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            onValueChanged();
         }
      });

      m_userSession = new JCheckBox(sm_res.getString("enableuser"),
                                    m_config.isUserSessionEnabled());
      m_userSession.setPreferredSize(new Dimension(300, 18));
      m_userSession.setVerticalAlignment(SwingConstants.TOP);
      m_userSession.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            onValueChanged();
         }
      });

      m_userSession.addItemListener(new ItemListener()
      {
         public void itemStateChanged(ItemEvent e)
         {
            if (e.getStateChange() == ItemEvent.SELECTED)
            {
               m_userSessionTimeout.setEnabled(true);
               m_userSessionTimeout.setEditable(true);

               m_userSessionWarning.setEnabled(true);
               m_userSessionWarning.setEditable(true);
               
               m_maxOpenUserSessions.setEnabled(true);
               m_maxOpenUserSessions.setEditable(true);
            }
            else
            {
               m_userSessionTimeout.setEnabled(false);
               m_userSessionTimeout.setEditable(false);

               m_userSessionWarning.setEnabled(false);
               m_userSessionWarning.setEditable(false);
               
               m_maxOpenUserSessions.setEnabled(false);
               m_maxOpenUserSessions.setEditable(false);
            }
         }
      });
   }

   /** Performs the initialization for the constructor.
    */
   private void init()
   {
      initializeFields();

      JPanel leftPanel = new JPanel();
      leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
      leftPanel.add(createSessionMgmtPanel());

      add(leftPanel); 

   }

   /** 
    * Creates the panel encompassing the User session management settings.
    * 
    * @return The panel, never <code>null</code>.
    */
   private JPanel createSessionMgmtPanel()
   {
      Dimension d = new Dimension(150, 18);

      JLabel sessionTimeoutLabel = new JLabel(
         sm_res.getString("sessionTimeout"), SwingConstants.RIGHT);
      sessionTimeoutLabel.setPreferredSize(d);
      
      JLabel sessionWarningLabel = new JLabel(
              sm_res.getString("sessionWarning"), SwingConstants.RIGHT);
           sessionTimeoutLabel.setPreferredSize(d);
      
      JPanel p1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
      p1.add(sessionTimeoutLabel);
    
      p1.add(m_userSessionTimeout);
      p1.add(sessionWarningLabel);
      p1.add(m_userSessionWarning);

      JLabel maxOpenUserSessionsLabel = new JLabel(
         sm_res.getString("maxOpenUserSessions"), SwingConstants.RIGHT);
      maxOpenUserSessionsLabel.setPreferredSize(d);
      
      JPanel p3 = new JPanel(new FlowLayout(FlowLayout.LEFT));
      p3.add(maxOpenUserSessionsLabel);
      p3.add(m_maxOpenUserSessions);
      
      JPanel p2 = new JPanel(new BorderLayout());
      p2.add(m_userSession, "North");
      p2.add(p3, "Center");
      p2.add(p1, "South");

      JPanel sessionPanel = new JPanel();
      sessionPanel.setLayout(new FlowLayout());
      sessionPanel.add(p2);
      sessionPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED),
         sm_res.getString("session")));

      return sessionPanel;
   }

   /** Displays an error message and sets a  focus in the component where an error
    *  was encountered.
    * @param strerror an error message.  If strerror is null or empty an error 
    * message displayed is a general error message.
    * @param comp a component where an error occurred, a comp must not be
    * <CODE>null</CODE>
   */

   private void showError(String strerror, Component comp)
   {
       if(null == comp)
         throw new IllegalArgumentException("comp must not be null");


      if (StringUtils.isEmpty(strerror))
      {
         strerror="generalerror";
      }

      JOptionPane.showMessageDialog( PSServerAdminApplet.getFrame(),
                                     sm_res.getString(strerror),
                                     sm_res.getString("error"),
                                     JOptionPane.ERROR_MESSAGE );

      comp.requestFocus();

   }

   //
   // MEMBER VARIABLES
   //

   private ServerConfiguration m_config = null;

   private JCheckBox m_userSession;
   
   
   /** The text field to edit the user session timeout */
   private UTFixedTextField m_userSessionTimeout = null;
   
   /** The text field to edit the user session timeout */
   private UTFixedTextField m_userSessionWarning = null;
   
   /**
    * The text field to edit the maximum number of open user sessions cached 
    * by the server. 
    */
   private UTFixedTextField m_maxOpenUserSessions = null;
   ///// m_bModified should be set to "false" but since the text fields do not generate
   ///// KeyTyped events, or even FocusGained events m_bModified never gets set to "true"
   ///// and data is not saved. WORKAROUND: always save this tab. Set m_bModified to true.
   ///// todo: set m_bModified to false once Sun fixes this bug.
   private boolean m_bModified = true;
   private static ResourceBundle sm_res = PSServerAdminApplet.getResources();
   
   /** The multiplicator used to convert seconds to minutes */
   private static final int SECONDS_PER_MINUTE = 60;
}

