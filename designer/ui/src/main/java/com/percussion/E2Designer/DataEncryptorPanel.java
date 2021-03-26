/*[ DataEncryptorPanel.java ]**************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer;

import com.percussion.design.objectstore.PSDataEncryptor;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;


/**
 * The DataEncryptorPanel provides the user interface to view and edit
 * the data security settings.
 */
////////////////////////////////////////////////////////////////////////////////
public class DataEncryptorPanel extends JPanel
{
   /**
   * Construct the data security property dialog with default information.
   *
    */
  //////////////////////////////////////////////////////////////////////////////
  public DataEncryptorPanel(PSDataEncryptor data)
  {
      super();

    if (null == data)
    {
      m_encryptor = new PSDataEncryptor(DEFAULT_KEY_INT);
      m_encryptor.setSSLRequired(false);
        initPanel(false, false, DEFAULT_KEY_INT);
    }
    else
    {
      m_encryptor = data;
      initPanel(true, data.isSSLRequired(), data.getKeyStrength());
    }
  }

   
   /**
   * Returns wether or not the default security is overridden.
   *
   * @return   m_defaultsOverridden   true if defaults are overridden, false otherwise
   */
  //////////////////////////////////////////////////////////////////////////////
  public boolean isDefaultOverridden()
  {
     return m_defaultsOverridden.isSelected();
  }

   /**
   * Returns wether or not the SSL is required.
   *
   * @return   m_SSLRequired    true if SSL is required, false otherwise
   */
  //////////////////////////////////////////////////////////////////////////////
  public boolean isSSLRequired()
  {
     return m_SSLRequired.isSelected();
  }

   /**
   * Returns the minimal key length.
   *
   * @return   int    th eminimal key length
   */
  //////////////////////////////////////////////////////////////////////////////
  public int getKey()
  {
     return m_keyEditor.getData().intValue();
  }

  /**
   * Returns the encryptor created in this class.
   *
   * @returns PSDataEncryptor The encryptor. If validation fails, a null is
   * returned.
   */
  public PSDataEncryptor getEncryptor()
  {
    if (m_framework.checkValidity())
    {
      m_encryptor.setSSLRequired(isSSLRequired());
      m_encryptor.setKeyStrength(getKey());
      return m_encryptor;
    }
    else
      return null;
  }

   /**
   * Create the dialogs view/edit panel.
   *
   * @param      keyLength      the minimum key length
   * @return   JPanel         a grid panel containing the dialogs view/edit panel.
   */
  //////////////////////////////////////////////////////////////////////////////
  private JPanel createViewPanel(int keyLength)
  {
    m_defaultsOverridden.setMnemonic(sm_res.getString("mn_overrideDefault").charAt(0));
      //add listners
      m_defaultsOverridden.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            enableControls();
         }
      });

    m_SSLRequired.setMnemonic(sm_res.getString("mn_requireSSL").charAt(0));

    m_keyEditor.setData(keyLength);
    m_keyEditor.setDirectEditingAllowed(true);

    Box overrideBox = new Box(BoxLayout.X_AXIS);
    overrideBox.add(m_defaultsOverridden);
    overrideBox.add(Box.createHorizontalGlue());

    Box reqBox = new Box(BoxLayout.X_AXIS);
      reqBox.add(Box.createHorizontalStrut(50));
    reqBox.add(m_SSLRequired);
    reqBox.add(Box.createHorizontalGlue());

     Box box = new Box(BoxLayout.Y_AXIS);
    box.add(overrideBox);
      box.add(Box.createVerticalStrut(10));
    box.add(reqBox);
    box.add(m_keyEditor);

    JPanel panel = new JPanel(new BorderLayout());
    panel.add(box, BorderLayout.NORTH);

    return panel;
  }

   /**
   * Initialize the dialogs GUI elements with its data.
   *
   * @param   bOverrideDefault   override the default
   * @param bSSLRequired         SSL is required
   * @param keyLength               minimal key length
   */
  //////////////////////////////////////////////////////////////////////////////
  private void initPanel(boolean bOverrideDefault, boolean bSSLRequired,
                                      int keyLength)
  {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(new EmptyBorder(5, 5, 5, 5));
    panel.add(createViewPanel(keyLength), "West");
    //panel.add(m_commandPanel, "East");

    m_defaultsOverridden.setSelected(bOverrideDefault);
    m_SSLRequired.setSelected(bSSLRequired);
      enableControls();
    
     setLayout(new BorderLayout());
    setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED),
                               sm_res.getString("title")));
    add(panel);

    this.setSize(DIALOG_SIZE);

    // initialize validation constraints
    m_validatedComponents[0] = m_keyEditor;
      m_validationConstraints[0] = m_keyEditor;
      m_framework.setFramework(m_validatedComponents, m_validationConstraints);
  }

   public void enableControls()
   {
      if(m_defaultsOverridden.isSelected())
      {
         m_SSLRequired.setEnabled(true);
         m_keyEditor.setEnabled(true);

      }
      else
      {
         m_SSLRequired.setEnabled(false);
         m_keyEditor.setEnabled(false);
      }

   }

   /**
   * the server name
   */
  private PSDataEncryptor m_encryptor = null;
   /**
   * the check box to override the default security
   */
  private JCheckBox m_defaultsOverridden = new JCheckBox(sm_res.getString("overrideDefault"));
   /**
   * the check box to define wetehr or not SSL is required
   */
  private JCheckBox m_SSLRequired = new JCheckBox(sm_res.getString("requireSSL"));
   /**
   * the up/down editor
   */
  private UTSpinTextField m_keyEditor = new UTSpinTextField(sm_res.getString("minimumKeyLength"),
                                                                                         DEFAULT_KEY,
                                                                                         MINIMAL_KEY, MAXIMAL_KEY);;
   /**
   * flag which indicates any changes in current edit session
   */
  private boolean m_modified = false;
    /**
   * the default key
   */
  private final static int DEFAULT_KEY_INT = 56;
  private final static Integer DEFAULT_KEY = new Integer(DEFAULT_KEY_INT);
    /**
   * the minimal key limit
   */
  private final static Integer MINIMAL_KEY = new Integer(40);
    /**
   * the maximal key limit
   */
  private final static Integer MAXIMAL_KEY = new Integer(128);
   /**
   * the dialog size
   */
  private final static Dimension DIALOG_SIZE = new Dimension(380, 140);

  static ResourceBundle sm_res = null;
  static
  {
    try
    {
      sm_res = ResourceBundle.getBundle("com.percussion.E2Designer.DataEncryptorPanelResources", Locale.getDefault( ) );
    }catch(MissingResourceException mre)
    {
      mre.printStackTrace();
    }
  }

   /**
   * the validation framework variables
   */
   //////////////////////////////////////////////////////////////////////////////
  private static final int NUM_COMPONENTS_VALIDATED = 1;
  private final Component m_validatedComponents[] = new Component[NUM_COMPONENTS_VALIDATED];
  private final ValidationConstraint m_validationConstraints[] = new ValidationConstraint[NUM_COMPONENTS_VALIDATED];
  private final ValidationFramework m_framework = new ValidationFramework();
}




