/*[ EncryptorPropertyDialog.java ]*********************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * The EncryptorPropertyDialog provides the user interface to view and edit
 * the data security settings.
 */
////////////////////////////////////////////////////////////////////////////////
public class EncryptorPropertyDialog extends PSEditorDialog
{
   /**
   * Construct the data security property dialog with default information.
   *
    */
  //////////////////////////////////////////////////////////////////////////////
  public EncryptorPropertyDialog()
  {
      super();
      initDialog(false, false, DEFAULT_KEY_INT);
  }

   /**
   * Construct the data security property dialog with provided information.
   *
   * @param parent         the parent frame
   * @param bOverride      override default (not used)
   * @param bRequired      SSL required     (not used)
   * @param keyLength      minimal key length
    */
  //////////////////////////////////////////////////////////////////////////////
  public EncryptorPropertyDialog(Frame parent, boolean bOverride,
                                                boolean bRequired, int keyLength)
  {
      super(parent);
      initDialog(bOverride, bRequired, keyLength);
  }

   /**
   * Returns wether or not the default security is overridden.
   *
   * @return   m_defaultsOverridden   true if defaults are overridden, false otherwise
   */
  //////////////////////////////////////////////////////////////////////////////
  /*
  public boolean isDefaultOverridden()
  {
     return m_defaultsOverridden.isSelected();
  }
  */

   /**
   * Returns wether or not the SSL is required.
   *
   * @return   m_SSLRequired    true if SSL is required, false otherwise
   */
  //////////////////////////////////////////////////////////////////////////////
  /*
  public boolean isSSLRequired()
  {
     return m_SSLRequired.isSelected();
  }
  */

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
   * Create the dialogs view/edit panel.
   *
   * @param      keyLength      the minimum key length
   * @return   JPanel         a grid panel containing the dialogs view/edit panel.
   */
  //////////////////////////////////////////////////////////////////////////////
  private JPanel createViewPanel(int keyLength)
  {
    //m_defaultsOverridden.setMnemonic(getResources().getString("mn_overrideDefault").charAt(0));

    //m_SSLRequired.setMnemonic(getResources().getString("mn_requireSSL").charAt(0));

    m_keyEditor.setDirectEditingAllowed(true);

     //Box box = new Box(BoxLayout.Y_AXIS);
    //box.add(m_defaultsOverridden);
    //box.add(m_SSLRequired);

    JPanel panel = new JPanel(new BorderLayout());
    //panel.add(box, BorderLayout.NORTH);
    panel.add(m_keyEditor, BorderLayout.CENTER);

    return panel;
  }

   /**
   * Initialize the dialogs GUI elements with its data.
   *
   * @param   bOverrideDefault   override the default (not used)
   * @param bSSLRequired         SSL is required
   * @param keyLength               minimal key length
   */
  //////////////////////////////////////////////////////////////////////////////
  private void initDialog(boolean bOverrideDefault, boolean bSSLRequired,
                                      int keyLength)
  {
    m_commandPanel = new UTStandardCommandPanel(this, "", SwingConstants.HORIZONTAL)
    {
       // implement onOk action
       public void onOk()
       {
            EncryptorPropertyDialog.this.onOk();
      }
    };
    getRootPane().setDefaultButton(m_commandPanel.getOkButton());

    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(new EmptyBorder(5, 5, 5, 5));
    panel.add(createViewPanel(keyLength), BorderLayout.NORTH);
    panel.add(m_commandPanel, BorderLayout.SOUTH);

     getContentPane().setLayout(new BorderLayout());
    getContentPane().add(panel);

    this.setSize(DIALOG_SIZE);

    // initialize validation constraints
    m_validatedComponents[0] = m_keyEditor;
      m_validationConstraints[0] = m_keyEditor;
      setValidationFramework(m_validatedComponents, m_validationConstraints);
  }

   /**
   * Test the dialog.
   *
   */
  //////////////////////////////////////////////////////////////////////////////
  public static void main(String[] args)
  {
     try
    {
         final JFrame frame = new JFrame("Test Dialog");
         UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
         frame.addWindowListener(new BasicWindowMonitor());

         JButton startButton = new JButton("Open Dialog");
         frame.getContentPane().add(startButton);
         startButton.addActionListener(new ActionListener()
         {
            public void actionPerformed(ActionEvent e)
            {
               JDialog dialog = new EncryptorPropertyDialog(frame, true, true,
                                                                             DEFAULT_KEY_INT);
            dialog.setLocationRelativeTo(frame);
               dialog.setVisible(true);
            }
         });

         frame.setSize(500, 220);
         frame.setVisible(true);
    }
    catch (Exception ex)
    {
       ex.printStackTrace();
    }
   }

   //////////////////////////////////////////////////////////////////////////////
   // implementation of IEditor
  public boolean onEdit(UIFigure figure, final Object data)
  {
     if (figure.getData() instanceof OSDataEncryptor)
    {
       m_encryptor = (OSDataEncryptor) figure.getData();

      //m_defaultsOverridden.setSelected(m_encryptor.isDefaultOverridden());
      //m_SSLRequired.setSelected(m_encryptor.isSSLRequired());
      m_keyEditor.setData(m_encryptor.getKeyLength());

      this.center();
       this.setVisible(true);
    }
    else
       throw new IllegalArgumentException("OSDataEncryptor expected!");

    return m_modified;
  }

/** Overriding PSDialog.onOk() method implementation.
*/
  public void onOk()
  {
    if (activateValidation())
    {
      try
      {
        m_modified |= m_keyEditor.isModified();

        m_encryptor.save(true, //isDefaultOverridden(),
                               true, //isSSLRequired(),
                         m_keyEditor.getData().intValue());

        dispose();
      }
      catch (Exception e)
      {
        JOptionPane.showMessageDialog(this, Util.cropErrorMessage(e.getMessage()),
                                      E2Designer.getResources().getString("OpErrorTitle"),
                                      JOptionPane.ERROR_MESSAGE);

        e.printStackTrace();
      }
    }
  }

   //////////////////////////////////////////////////////////////////////////////
   /**
   * the server name
   */
  private OSDataEncryptor m_encryptor = null;
   /**
   * the check box to override the default security
   */
  //private JCheckBox m_defaultsOverridden = new JCheckBox(getResources().getString("overrideDefault"));
   /**
   * the check box to define wetehr or not SSL is required
   */
  //private JCheckBox m_SSLRequired = new JCheckBox(getResources().getString("requireSSL"));
   /**
   * the up/down editor
   */
  private UTSpinTextField m_keyEditor = new UTSpinTextField(getResources().getString("minimumKeyLength"),
                                                                                         DEFAULT_KEY,
                                                                                         MINIMAL_KEY, MAXIMAL_KEY);;
   /**
   * the standard command panel
   */
  private UTStandardCommandPanel m_commandPanel = null;
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
  private final static Dimension DIALOG_SIZE = new Dimension(275, 110);//(380, 140);

   /**
   * the validation framework variables
   */
   //////////////////////////////////////////////////////////////////////////////
  private static final int NUM_COMPONENTS_VALIDATED = 1;
  private final Component m_validatedComponents[] = new Component[NUM_COMPONENTS_VALIDATED];
  private final ValidationConstraint m_validationConstraints[] = new ValidationConstraint[NUM_COMPONENTS_VALIDATED];
}




