/* *****************************************************************************
 *
 * [ PageTankPropertyDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *******************************************************************************/
package com.percussion.E2Designer;

import com.percussion.design.objectstore.PSPipe;
import com.percussion.design.objectstore.PSUpdatePipe;
import com.percussion.UTComponents.UTFixedButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;

/**
 * The DatatankPropertyDialog displays the datatanks properties and provides
 * their editors if applicable.
 */
////////////////////////////////////////////////////////////////////////////////
public class PageTankPropertyDialog extends PSEditorDialog
{
   /**
   * Construct the default datatank property dialog.
   *
    */
  //////////////////////////////////////////////////////////////////////////////
  public PageTankPropertyDialog()
  {
      super();
      initDialog("", "");
  }

   /**
   * Get the schema source.
   *
   * @return   m_schemaSource    the schema source
   */
  //////////////////////////////////////////////////////////////////////////////
  public String getSchemaSource()
  {
     return m_schemaSource.getText();
  }

   /**
   * Get the XML field.
   *
   * @return   m_XMLField    the XML field
   */
  //////////////////////////////////////////////////////////////////////////////
  public String getXMLField()
  {
     return m_XMLField.getText();
  }

   /**
   * Create the dialogs schema source panel.
   *
   * @param      schemaSource      initial pipes name
   * @return   JPanel         a flow panel containing the dialogs schema source.
   */
  //////////////////////////////////////////////////////////////////////////////
  private JPanel createSchemaSourcePanel(String schemaSource)
  {
    m_schemaSource.setText(schemaSource);

     JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    panel.add(new UTMnemonicLabel(getResources(), "schemaSource", m_schemaSource));
    panel.add(m_schemaSource);

    return panel;
  }

   /**
   * Create the browse button panel.
   */
  //////////////////////////////////////////////////////////////////////////////
  private JPanel createBrowsePanel()
  {
    m_chooser.setMultiSelectionEnabled(false);
    m_chooser.setFileFilter(new PageTankFileFilter());
    m_browse.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent event)
      {
         int option = m_chooser.showOpenDialog(E2Designer.getApp().getMainFrame());
         if (option == JFileChooser.APPROVE_OPTION)
         {
          String fileName = m_chooser.getSelectedFile().getName();
          m_schemaSource.setText(fileName);
           }
      }
    });

     JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(new EmptyBorder(5, 0, 0, 5));
    panel.add(m_browse, "North");

    return panel;
  }

   /**
   * Create the dialogs XML panel.
   *
   * @param      XMLField      the XML field for request type
   * @return   JPanel         a flow panel containing the dialogs XML field.
   */
  //////////////////////////////////////////////////////////////////////////////
  private JPanel createXMLFieldPanel(String XMLField)
  {
    m_XMLField.setText(XMLField);

     JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

    panel.add(m_XMLLabel);
    panel.add(m_XMLField);

    return panel;
  }

   /**
   * Create the dialogs view/edit panel.
   *
   * @param      schemaSource   initial pipes name
   * @param      XMLField         pipes type
   * @return   JPanel            a grid panel containing the dialogs view/edit panel.
   */
  //////////////////////////////////////////////////////////////////////////////
  private JPanel createViewPanel(String schemaSource, String XMLField)
  {
     Box box = new Box(BoxLayout.Y_AXIS);
    box.add(Box.createVerticalGlue());
    box.add(createSchemaSourcePanel(schemaSource));
    box.add(createXMLFieldPanel(XMLField));

    //m_createDTD.setMnemonic(getResources().getString("mn_createDTD").charAt(0));

    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(new EmptyBorder(5, 5, 5, 5));
    panel.add(box, "Center");
    panel.add(createBrowsePanel(), "East");
    panel.add(m_browser, "South");
    //panel.add(m_createDTD);

    return panel;
  }

   /**
   * Initialize the dialogs GUI elements with its data.
   *
   * @param   schemaSource   the schema source
   * @param XMLField         the XML field
   */
  //////////////////////////////////////////////////////////////////////////////
  private void initDialog(String schemaSource, String XMLField)
  {
    m_commandPanel = new UTStandardCommandPanel(this, "", SwingConstants.HORIZONTAL)
    {
       // implement onOk action
       public void onOk()
      {
            PageTankPropertyDialog.this.onOk();
      }
      public void onCancel()
      {
           PageTankPropertyDialog.this.onCancel();
      }

    };
    getRootPane().setDefaultButton(m_commandPanel.getOkButton());
    JPanel cmdPanel = new JPanel(new BorderLayout());
    cmdPanel.add(m_commandPanel, BorderLayout.EAST);
    
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(new EmptyBorder(5, 5, 5, 5));
    panel.add(createViewPanel(schemaSource, XMLField), "North");
    panel.add(cmdPanel, "South");

      getContentPane().setLayout(new BorderLayout());
    getContentPane().add(panel);
    pack();
    
    // initialize validation constraints
    m_validatedComponents[0] = m_schemaSource;
    m_validationConstraints[0] = new StringConstraint();

    setValidationFramework(m_validatedComponents, m_validationConstraints);
  }

   //////////////////////////////////////////////////////////////////////////////
   // implementation of IEditor
  public boolean onEdit(UIFigure figure, final Object data)
  {
     if (figure.getData() instanceof OSPageDatatank)
    {
      UTPipeNavigator navigator = new UTPipeNavigator();
      UIFigure pipeFigure = navigator.getPipe(figure);
      UIFigure page = navigator.getPageTank(figure);

      if (pipeFigure != null)
      {
        PSPipe pipe = (PSPipe) pipeFigure.getData();
        if (pipe instanceof PSUpdatePipe && page != null)
        {
          m_XMLField.setEnabled(true);
          m_XMLField.setBackground(Color.white);
          m_XMLLabel.setEnabled(true);
        }
        else
        {
          m_XMLField.setEnabled(false);
          m_XMLField.setBackground(Color.lightGray);
          m_XMLLabel.setEnabled(false);
        }
      }

      m_tank = (OSPageDatatank) figure.getData();

       m_schemaSource.setText(m_tank.getSchemaSource().toString());
      m_schemaSource.setEnabled(!m_tank.isSchemaSourceReadOnly());
      m_schemaSource.setBackground(m_tank.isSchemaSourceReadOnly() ? Color.lightGray : Color.white);
      m_browse.setEnabled(!m_tank.isSchemaSourceReadOnly());

          m_XMLField.setText(m_tank.getActionTypeXmlField());
      //m_createDTD.setSelected(m_tank.isCreateDTD());

      m_browser.initTree(m_tank);

      this.center();
       this.setVisible(true);
    }
    else
       throw new IllegalArgumentException(getResources().getString("datatypeError"));

    return m_modified;
  }

  public void onCancel()
  {
    if(m_browser!= null )
    {
        m_browser.restore(); //restore set the dirty flag to false
    }
    dispose();

  }

  /**
   * Handles the ok button action. Overrides PSDialog onOk() method
   * implementation.
   */
  public void onOk()
  {
    if(m_browser!= null )
    {
        m_browser.updateDirtyFlag();// update the flag
    }

    if (activateValidation())
    {
      String urlString = m_schemaSource.getText();
      URL url = null;
      try
      {
        url = new URL(urlString);
      }
      catch (MalformedURLException e)
      {
        try
        {
          // try again adding the protocol automatically
          urlString = Util.createURLString(urlString, Util.XML_FILE_EXTENSION);
          url = new URL(urlString);
        }
        catch (MalformedURLException e2)
        {
          Object[] params =
          {
            urlString,
          };

          // its still not possible to create the URL, inform the user and return
          JOptionPane.showMessageDialog(this,
                                                        MessageFormat.format(E2Designer.getResources().getString("InvalidURL"), params),
                                        E2Designer.getResources().getString("OpErrorTitle"),
                                        JOptionPane.WARNING_MESSAGE);
          return;
        }
      }

      try
      {
        m_modified = true;

        m_tank.setSchemaSource(url);
        m_tank.setActionTypeXmlField(m_XMLField.getText());
        //m_tank.setCreateDTD(m_createDTD.isSelected());
      }
      catch (IllegalArgumentException e)
      {
        e.printStackTrace();
      }

      dispose();
    }
  }


   //////////////////////////////////////////////////////////////////////////////
   /**
   * the datatank we are editing
   */
   private OSPageDatatank m_tank = null;
   /**
   * the edit field of the schema source
   */
  private UTFixedTextField m_schemaSource = new UTFixedTextField("");
  /**
   * browse schema source
   */
  private UTFixedButton m_browse = new UTFixedButton((new ImageIcon(getClass().getResource(E2Designer.getResources().getString("gif_Browser")))), 20, 20);
  /**
   * the file browser
   */
  private JFileChooser m_chooser = new JFileChooser(System.getProperty("user.dir"));
   /**
   * the edit field of the XML field for request type
   */
  private XMLDropTargetTextField m_XMLField = new XMLDropTargetTextField("");
  private UTMnemonicLabel m_XMLLabel = new UTMnemonicLabel(getResources(), "XMLField", m_XMLField);
  /*
   * the XML browser storage
   */
  private MapBrowser m_browser = new MapBrowser();

  /**
   * the standard command panel
   */
  private UTStandardCommandPanel m_commandPanel = null;
  /**
   * the modified status flag
   */
  private boolean m_modified = false;
   /**
   * the validation framework variables
   */
   //////////////////////////////////////////////////////////////////////////////
  private static final int NUM_COMPONENTS_VALIDATED = 1;
  private final Component m_validatedComponents[] = new Component[NUM_COMPONENTS_VALIDATED];
  private final ValidationConstraint m_validationConstraints[] = new ValidationConstraint[NUM_COMPONENTS_VALIDATED];
}

