/*[ PipePropertyDialog.java ]**************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer;

import com.percussion.design.objectstore.PSPipe;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * The PipePropertyDialog displays the pipe properties "Name" and "Type". The
 * name property is editable while the type property is read only.
 */
////////////////////////////////////////////////////////////////////////////////
public class PipePropertyDialog extends PSEditorDialog
{
   /**
   * Construct the default pipe property dialog.
   *
    */
  //////////////////////////////////////////////////////////////////////////////
  public PipePropertyDialog()
  {
      super();
      initDialog("", "");
  }

   /**
   * Construct the pipe property dialog with provided information. This is for
   * TESTING purpose only!
   *
   * @param parent      the parent frame
   * @param pipeName    the name of the pipe
   * @param pipeType   the type of the pipe
    */
  //////////////////////////////////////////////////////////////////////////////
  protected PipePropertyDialog(Frame parent, String pipeName, String pipeType)
  {
      super(parent);
      initDialog(pipeName, pipeType);
  }

   /**
   * Get the pipes name.
   *
   * @return   m_pipeName    the name of the pipe
   */
  //////////////////////////////////////////////////////////////////////////////
  public String getPipeName()
  {
     return m_pipeName.getText();
  }

   /**
   * Get the pipes type.
   *
   * @return   m_pipeType    the type of the pipe
   */
  //////////////////////////////////////////////////////////////////////////////
  public String getPipeType()
  {
     return m_pipeType.getText();
  }

   /**
   * Create the dialogs name panel.
   *
   * @param      pipeName      initial pipes name
   * @return   JPanel         a flow panel containing the dialogs name.
   */
  //////////////////////////////////////////////////////////////////////////////
  private JPanel createNamePanel(String pipeName)
  {
    m_pipeName.setText(pipeName);

     JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    panel.add(new UTMnemonicLabel(getResources(), "name", m_pipeName));
    panel.add(m_pipeName);

    return panel;
  }

   /**
   * Create the dialogs type panel.
   *
   * @param      pipeType      the pipes type
   * @return   JPanel         a flow panel containing the dialogs type.
   */
  //////////////////////////////////////////////////////////////////////////////
  private JPanel createTypePanel(String pipeType)
  {
    m_pipeType.setText(pipeType);
    m_pipeType.setEnabled(false);
    m_pipeType.setBackground(Color.lightGray);

     JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    panel.add(new JLabel(getResources().getString("type")));
    panel.add(m_pipeType);

    return panel;
  }

   /**
   * Create the dialogs view/edit panel(Name, Type).
   *
   * @param      pipeName      initial pipes name
   * @param      pipeType      pipes type
   * @return   JPanel         a grid panel containing the dialogs view/edit panel.
   */
  //////////////////////////////////////////////////////////////////////////////
  private JPanel createViewPanel(String pipeName, String pipeType)
  {
     Box box = new Box(BoxLayout.Y_AXIS);
    box.add(Box.createVerticalGlue());
    box.add(createNamePanel(pipeName));
    box.add(createTypePanel(pipeType));

    JPanel panel = new JPanel(new BorderLayout());
    panel.add(box, BorderLayout.NORTH);

    return panel;
  }

   /**
   * Initialize the dialogs GUI elements with its data.
   *
   * @param   pipeName      the pipes name
   * @param pipeType      the pipes type
   */
  //////////////////////////////////////////////////////////////////////////////
  private void initDialog(String pipeName, String pipeType)
  {
     // initialize GUI
    m_commandPanel = new UTStandardCommandPanel(this, "", SwingConstants.VERTICAL)
    {
       // implement onOk action
       public void onOk()
      {
            PipePropertyDialog.this.onOk();
      }
    };
    getRootPane().setDefaultButton(m_commandPanel.getOkButton());

    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(new EmptyBorder(5, 5, 5, 5));
    panel.add(createViewPanel(pipeName, pipeType), "West");
    panel.add(m_commandPanel, "East");

     getContentPane().setLayout(new BorderLayout());
    getContentPane().add(panel);
    setSize(DIALOG_SIZE);

    // initialize validation constraints
    m_validatedComponents[0] = m_pipeName;
      m_validationConstraints[0] = new StringConstraint();
      setValidationFramework(m_validatedComponents, m_validationConstraints);
  }

   /**
   * Test the Pipe Property dialog.
   *
   */
  //////////////////////////////////////////////////////////////////////////////
  public static void main(String[] args)
  {
     try
    {
         final JFrame frame = new JFrame("Test Pipe Property Dialog");
         UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
         frame.addWindowListener(new BasicWindowMonitor());

         JButton startButton = new JButton("Open Dialog");
         frame.getContentPane().add(startButton);
         startButton.addActionListener(new ActionListener()
         {
            public void actionPerformed(ActionEvent e)
            {
               PipePropertyDialog dialog = new PipePropertyDialog(frame,
                                                                                      "Products Overview",
                                                             "Query");
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
     if (figure.getData() instanceof OSQueryPipe)
          m_pipeType.setText(E2Designer.getResources().getString("QueryPipe"));
    else
          m_pipeType.setText(E2Designer.getResources().getString("UpdatePipe"));

     if (figure.getData() instanceof PSPipe)
    {
       m_pipe = (PSPipe) figure.getData();
       m_pipeName.setText(m_pipe.getName());

      this.center();
       this.setVisible(true);
    }
    else
       throw new IllegalArgumentException(getResources().getString("datatypeError"));

    return m_modified;
  }

/** Handles the ok button action. Overrides PSDialog onOk() method
  * implementation.
*/  
  public void onOk()
  {
    if (activateValidation())
    {
      try
      {
        m_modified = true;
        m_pipe.setName(m_pipeName.getText());
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
      dispose();
    }
  }

   //////////////////////////////////////////////////////////////////////////////
   /**
   * the pipe we are editing
   */
   private PSPipe m_pipe = null;
   /**
   * the edit field of the pipes name
   */
  private UTFixedTextField m_pipeName = new UTFixedTextField("",new Dimension(60,20));
   /**
   * the dieplay field of the pipes type
   */
  private UTFixedTextField m_pipeType = new UTFixedTextField("",new Dimension(60,20));
   /**
   * the standard command panel
   */
  private UTStandardCommandPanel m_commandPanel = null;
  /**
   * this flag will be set if any data within this dialog was modified
   */
  private boolean m_modified = false;
   /**
   * the dialog size
   */
  private final static Dimension DIALOG_SIZE = new Dimension(380, 140);

   /**
   * the validation framework variables
   */
   //////////////////////////////////////////////////////////////////////////////
  private static final int NUM_COMPONENTS_VALIDATED = 1;
  private final Component m_validatedComponents[] = new Component[NUM_COMPONENTS_VALIDATED];
  private final ValidationConstraint m_validationConstraints[] = new ValidationConstraint[NUM_COMPONENTS_VALIDATED];
}




