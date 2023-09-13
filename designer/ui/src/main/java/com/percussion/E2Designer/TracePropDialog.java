/******************************************************************************
 *
 * [ TracePropDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.E2Designer;

import com.percussion.conn.PSDesignerConnection;
import com.percussion.debug.PSTraceFlag;
import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSTraceInfo;
import com.percussion.design.objectstore.PSTraceOption;
import com.percussion.server.PSRemoteConsole;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Dialog for editing Tracing properties.  Can enable tracing with a set of
 * temporary options, or save tracing properties to the application which will
 * persist and take effect when the application is restarted.
 */
public class TracePropDialog extends PSEditorDialog
{

   /**
    * Parameterless constructor for this class.  Editor must then be invoked
    * thru onEdit().
    *
    * @param app The application object for which trace properties are being
    * modified.
    * @see #onEdit(UIFigure, Object) onEdit
    */
   public TracePropDialog()
   {
   }

   /**
    * Basic constructor for this class.  Delegates editor invocation to
    * init().
    *
    * @param app The application object for which trace properties are being
    * modified.
    * @see #init(PSApplication)
    */
   public TracePropDialog(PSApplication app)
   {
      init(app);

   }

   /**
    * Store reference to app and trace options, and make call to initialize UI.
    *
    * @param app The application object for which trace properties are being
    * modified.  May not be <code>null</code>.
    * @throws IllegalArgumentException if app is <code>null</code>.
    */
   private void init(PSApplication app)
   {
      // be sure the app is not null
      if (app == null)
         throw new IllegalArgumentException("app cannot be null");

      // store the app
      m_app = app;

      /* get the app's trace options - this is getting the actual reference
       * contained in the app, so any changes to this object are reflected
       * in the app
       */
      m_traceInfo = app.getTraceInfo();

      // get the resource bundle
      m_res = getResources();

      initUI();
       setVisible(true);
   }

   /**
    * Initializes all panels and puts everything together.
    */
   private void initUI()
   {
      // create main panel to hold everything else
      JPanel contentPane = new JPanel();
        getContentPane().add(contentPane);

       setResizable(true);

      final int BORDER_WIDTH = 3;
      contentPane.setBorder(new EmptyBorder(2*BORDER_WIDTH, BORDER_WIDTH,
         BORDER_WIDTH, BORDER_WIDTH ));

      contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

      // create top panel to hold options panel and command buttons
      JPanel top = new JPanel();
      top.setLayout(new BorderLayout());
      top.setBorder(new EmptyBorder(5,5,5,5));
      top.add(createOptionsPanel(), BorderLayout.CENTER);
      top.add(createDebugCommandPanel(), BorderLayout.EAST);
      contentPane.add(Box.createVerticalGlue());
      contentPane.add(top);
      contentPane.add(createBottomPanel());
      contentPane.add(Box.createVerticalGlue());
      contentPane.add(createCommandPanel());

      setSize(DIALOG_SIZE);
      pack();
      center();


      // set the title
      Object[] args = {m_app.getName()};
      setTitle(MessageFormat.format(getResources().getString("title"), args));
   }

   /**
    * Handles click of the Start/Stop button.
    * - Sends remote console command to the
    * server to either start or stop tracing, with the composite trace flag
    * created
    * from the dialog settings.
    * - Toggles the button text to and from Start and Stop.
    * - Disables the Apply button.
    * Does not cause any information to be saved with the app.
    *
    */
   public void onStartStop()
   {
      // see if starting or stopping
      boolean starting = !isAppTracing();

      if (starting)
      {
         // get the command.  this will also stop editing in the table
         PSTraceFlag flag = new PSTraceFlag();
         String cmd = getTraceStartConsoleCmd(m_optionListTable, flag);

         // be sure at least one is checked
         initValidationFramework(false);
         if ( activateValidation())
         {
            // send the command
            sendConsoleCmd(cmd);

            // change the button text
            m_startButton.setText(m_res.getString("stop"));

            // disable the apply button
            m_applyButton.setEnabled(false);

            // set the tracing indicator
            m_isAppTracing = true;
         }
      }
      else  // stopping
      {
         // send appropriate command
         sendConsoleCmd(getTraceStopConsoleCmd());

         // change the button text
         m_startButton.setText(m_res.getString("start"));

         // disable the apply button
         m_applyButton.setEnabled(false);

         // set the tracing indicator
         m_isAppTracing = false;
      }

   }

   /**
    * Handle click of Ok button.  Validate, then set properties on OSTraceInfo
    * object, which will cause them to be set in the PSApplication object.
    * Finally close the dialog.
    */
   public void onOk()
   {
      // get the flags, which will update the tablemodel
      PSTraceFlag flag = new PSTraceFlag();
      setTraceFlagsFromTable(m_optionListTable,
         flag);


      initValidationFramework(true);
      if ( !activateValidation())
         return;

      try
      {
         // set basic options
         m_traceInfo.setTraceEnabled(m_enabledCB.isSelected());
         m_traceInfo.setTimeStampOnlyTrace(m_timeStampOnlyCB.isSelected());
         m_traceInfo.setColumnWidth(Integer.parseInt(m_colWidthText.getText()));

         // set the flags, don't let it update the trace enabled setting
            m_traceInfo.setTraceOptionsFlag(flag, false);

         /* no need to set the trace options back into the app as we have a
          * reference to the app's traceInfo object
          */

      }
      catch(NumberFormatException e)
      {
         // this should never happen as we've already validated the col width
      }

      dispose();
      
   }

   /**
    * Handles click of Apply button.  Sends a remote console Trace command to
    * the server to restart tracing with a composite trace flag created from
    * the dialog settings.  If no trace options are selected, this will stop
    * tracing and the user is warned to that effect and allowed to choose if
    * they want to proceed.
    * Does not save any information in the app.
    */
   public void onApply()
   {
      // get the command to send, this will also complete changes to the table
      // model
      PSTraceFlag flag = new PSTraceFlag();
      String cmd = getTraceStartConsoleCmd(m_optionListTable, flag);

      // see if this would stop tracing
      if (!flag.isTraceEnabled())
      {
         // double check with the user that they want to stop tracing
         int option = JOptionPane.showConfirmDialog(this,
               E2Designer.getResources( ).getString("TraceApplyNoOptions" ),
               E2Designer.getResources().getString("ConfirmOperation"),
               JOptionPane.OK_CANCEL_OPTION,
               JOptionPane.QUESTION_MESSAGE);


         if (option == JOptionPane.OK_OPTION)
         {
            // the user said ok, call onstartstop to stop tracing
            onStartStop();
         }

      }
      else
      {
         // send it
         sendConsoleCmd(cmd);

         // Diable apply button
         m_applyButton.setEnabled(false);

         // switch the start/stop button
         String btnText = isAppTracing() ? "stop" :
            "start";
         m_startButton.setText(m_res.getString(btnText));
      }

   }

   /**
    * Handles event when checking or unchecking any of the trace options in the
    * table.  Any change to the option list enables the Apply button if
    * app has been started and tracing is active.
    */
   public void onChecked()
   {
      if (m_app.isEnabled() && isAppTracing())
         m_applyButton.setEnabled(true);
   }

   /**
    * Creates the validation framework and sets it in the parent dialog.
    * If not saving the settings, ensures that at least one option in the list is
    * checked.
    * If saving the settings, ensures the Column Width specified is an integer
    * and if trace is enabled, that at least one option in the list is checked.
    *
    * @param saveSettings indicates whether or not settings are being saved.
    * @throws java.lang.IllegalArgumentException if the component and constraint
    * arrays don't match. This can only happen if they are both not updated
    * equally.
    * (i.e. an implementation flaw).
    */
   public void initValidationFramework(boolean saveSettings)
   {
      // set up the validation framework
      final List<Component> comps = new ArrayList<Component>();
      final List<ValidationConstraint> constraints =
            new ArrayList<ValidationConstraint>();

      // only check column width if saving settings
      if (saveSettings)
      {
         IntegerConstraint isValidInt =
            new IntegerConstraint(COL_WIDTH_MAX, COL_WIDTH_MIN);
         comps.add(m_colWidthText);
         constraints.add(isValidInt);
      }

      // if saving, only add if enabled is checked
      if ((saveSettings && m_enabledCB.isSelected()) || !saveSettings)
      {
         CheckboxSelectorConstraint isCBSelected =
            new CheckboxSelectorConstraint();
         comps.add(m_optionListTable);
         constraints.add(isCBSelected);
      }

      Component [] c = new Component[comps.size()];
      comps.toArray( c );
      ValidationConstraint [] v = new ValidationConstraint[constraints.size()];
      constraints.toArray( v );
      setValidationFramework( c, v );

   }


   /**
    * Handles updating the option description text when the user
    * selects a different trace option in the list.
    */
   public void onSelectionChanged()
   {
      // selection has changed, need to update the description
      String descText = "";
      int index = m_optionListTable.getSelectionModel().getMinSelectionIndex();
      if (index > -1)
      {
         descText = 
            ((PSTraceOption)m_optionList.get(index)).getDescription();
      }     
      m_descText.setText(descText);

      // scroll it to the top - leaves scrollpane at end of text field otherwise
      m_descText.setCaretPosition(0);
   }

   /**
    * Implementation has been provided here, but is not currently being used.
    * The figure passed in is being ignored, but may be used for future
    * implementations.
    * @see PSEditorDialog#onEdit(UIFigure, Object)
    */
   public boolean onEdit(UIFigure figure, final Object data)
   {
      init((PSApplication)data);
      return true;
   }

   /**
    * Creates the panel containing the enabled checkbox and the list box of
    * trace options.
    * @return the panel
    */
   private JPanel createOptionsPanel()
   {
      JPanel optionsPanel = new JPanel();
      optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS ));

      // add the enabled checkbox
      m_enabledCB = new JCheckBox(m_res.getString("traceEnabled"));
      m_enabledCB.setMnemonic(m_res.getString("traceEnabled.mn").charAt(0));
      m_enabledCB.setSelected(m_traceInfo.isTraceEnabled());

      // this is only way I can seem get it to left align in the box
      JPanel tsPanel = new JPanel(new BorderLayout());
      tsPanel.add(m_enabledCB, BorderLayout.WEST);
      optionsPanel.add(tsPanel);


      // setup a table to hold checkboxes for each trace option
      m_optionListTable = new JTable(0,2 );
      m_optionListTable.setShowVerticalLines(false);
      m_optionListTable.setShowHorizontalLines(false);
      m_optionListTable.setSelectionModel(new DefaultListSelectionModel());
      m_optionListTable.getSelectionModel().setSelectionMode(
         ListSelectionModel.SINGLE_SELECTION);

      DefaultTableModel dtm = new CheckBoxTableModel(0,2);
      m_optionListTable.setModel(dtm);

      m_optionListTable.setTableHeader(new JTableHeader());

      // add column for the checkbox
      TableColumn column1 = m_optionListTable.getColumnModel().getColumn(0);
      column1.setPreferredWidth(15);
      column1.setMaxWidth(15);
      column1.setResizable(true);
      UTCheckBoxCellEditor column1Editor = new UTCheckBoxCellEditor();
      column1.setCellEditor(column1Editor);

      // add listener to be informed when checkboxes are checked/unchecked
      column1Editor.addCheckBoxListener(
         new ActionListener()
         {
            public void actionPerformed(ActionEvent e)
            {
               if (e.getSource() instanceof JCheckBox)
                  TracePropDialog.this.onChecked();
            }
         }
      );

      UTCheckBoxCellRenderer column1Renderer = new UTCheckBoxCellRenderer();
      column1.setCellRenderer(column1Renderer);

      // add column for the label of the checkbox
      TableColumn column2 = m_optionListTable.getColumnModel().getColumn(1);
      column2.setPreferredWidth(300);

      // put it in a scroll pane
      JScrollPane optionListScrollPane = new JScrollPane(m_optionListTable);


      // now add a row to the table for each trace option
      Iterator i = m_traceInfo.getTraceOptions();
      m_optionList = new ArrayList<PSTraceOption>();
      while (i.hasNext())
      {
         PSTraceOption option = (PSTraceOption)i.next();
         Object [] data = {
            Boolean.valueOf(m_traceInfo.isTraceEnabled(option.getFlag())),
            option.getDisplayName()};
         dtm.addRow(data);

         // save it for later
         m_optionList.add(option);
      }

      // add listener for table cell selection changing
      m_optionListTable.getSelectionModel().addListSelectionListener(
         new ListSelectionListener()
         {
            public void valueChanged(ListSelectionEvent e)
            {
               TracePropDialog.this.onSelectionChanged();
            }
         }
      );

      // add it to the panel
      JPanel listPanel = new JPanel();
      listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.X_AXIS ));
      listPanel.add(Box.createRigidArea(new Dimension(20, 0)));
      listPanel.add(optionListScrollPane);
      optionsPanel.add(listPanel);

      return optionsPanel;
   }

   
   /**
    * Create command panel. This contains the ok, cancel and help
    * buttons.
    *
    * @return JPanel the command panel. Never <code>null</code>.
    */
   private JPanel createCommandPanel()
   {
      UTStandardCommandPanel cmd = new UTStandardCommandPanel(this, "",
                                                      SwingConstants.HORIZONTAL)
      {
         public void onOk()
         {
            TracePropDialog.this.onOk();
         }
         public void onCancel()
         {
            TracePropDialog.this.onCancel();
         }
      };

      JPanel panel = new JPanel(new BorderLayout());
      panel.setBorder(new EmptyBorder(5, 5, 5, 5));
      panel.add(cmd, BorderLayout.EAST);
      // set the default button
      getRootPane().setDefaultButton(cmd.getOkButton());

      return panel;
  }
   /**
    * Creates the panel containing the command buttons for the dialog and for
    * starting and stopping tracing.
    * @return the panel
    */
   private JPanel createDebugCommandPanel()
   {
      // set up the options panel
      JPanel cmdPanel = new JPanel();
      cmdPanel.setLayout(new BoxLayout(cmdPanel, BoxLayout.Y_AXIS ));
      cmdPanel.setBorder(new EmptyBorder(5,5,5,5));

      // add the trace buttons
      JPanel panel = createTraceButtonPanel();
      cmdPanel.add(panel);
      cmdPanel.setAlignmentX(CENTER_ALIGNMENT);
      cmdPanel.setAlignmentY(CENTER_ALIGNMENT);
      return cmdPanel;
   }

   /**
    * Creates the panel containing the controls contained in the bottom panel
    * of the main layout. This includes the Column Width text box, the Enable
    * Timestamp Only checkbox, and the trace option description text area.
    * @return the panel
    */
   private JPanel createBottomPanel()
   {
      // create the panel
      JPanel bottom = new JPanel();
      bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS ));
      bottom.setBorder(new EmptyBorder(5,5,5,5));

      // add the output column panel
      JPanel colWidthPanel = new JPanel();
      colWidthPanel.setLayout(new BoxLayout(colWidthPanel, BoxLayout.X_AXIS ));
      
      String labelStr    = m_res.getString("outputColumn");
      char mn            = m_res.getString("outputColumn.mn").charAt(0); 
      UTFixedLabel label = new UTFixedLabel(labelStr, SwingConstants.RIGHT);     
      label.setDisplayedMnemonic(mn);
      label.setDisplayedMnemonicIndex(
                        labelStr.lastIndexOf((""+mn).toLowerCase().charAt(0)));
      
      colWidthPanel.add(label);
      colWidthPanel.add(Box.createRigidArea(new Dimension(5, 0)));
      m_colWidthText = new UTFixedTextField();
      m_colWidthText.setText(Integer.toString(m_traceInfo.getColumnWidth()));
      m_colWidthText.setColumns(3);
      colWidthPanel.add(m_colWidthText);
      colWidthPanel.add(Box.createHorizontalGlue());
      label.setLabelFor(m_colWidthText);
      
      bottom.add(colWidthPanel);


      // add the timestamp checkbox
      m_timeStampOnlyCB = new JCheckBox(m_res.getString("timeStampOnly"));
      m_timeStampOnlyCB.setMnemonic(
                                 m_res.getString("timeStampOnly.mn").charAt(0));
      m_timeStampOnlyCB.setSelected(m_traceInfo.IsTimeStampOnlyTrace());
      // this is only way I can seem get it to left align in the box
      JPanel tsPanel = new JPanel(new BorderLayout());
      tsPanel.add(m_timeStampOnlyCB, BorderLayout.WEST);
      bottom.add(tsPanel);

      // add the description label
      UTFixedLabel desc = new UTFixedLabel(m_res.getString("description"),
         SwingConstants.LEFT);
      desc.setAlignmentX(LEFT_ALIGNMENT);
      // this is only way I can seem get it to left align in the box
      JPanel descPanel = new JPanel(new BorderLayout());
      descPanel.add(desc, BorderLayout.WEST);
      bottom.add(descPanel);

      // add the text area for the description
      m_descText = new JTextArea();
      m_descText.setRows(10);
      m_descText.setLineWrap(true);
      m_descText.setWrapStyleWord(true);
      m_descText.setBackground(getBackground());
      m_descText.setEditable(false);
      m_descText.setAlignmentX(LEFT_ALIGNMENT);
      m_descScrollPane = new JScrollPane(m_descText,
                              ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                              ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
      m_descScrollPane.setBorder(BorderFactory.createCompoundBorder(
                           BorderFactory.createEtchedBorder(),
                           new EmptyBorder(5, 5, 5, 5)));

      bottom.add(m_descScrollPane);

      // Select the first row of the option list table to update the contents
      if (m_optionList.size() > 0)
      {
         m_optionListTable.getSelectionModel().setSelectionInterval(0, 0);
      }

      return bottom;
   }

   /**
    * Creates the panel containing the buttons that control tracing.  This
    * includes the Start/Stop button and the Apply button.
    * @return the panel
    */
   private JPanel createTraceButtonPanel()
   {
      JPanel tracePanel = new JPanel();
      Box box = new Box(BoxLayout.Y_AXIS);
      box.add(Box.createVerticalGlue());

      //see if we should start or stop
      String startText;
      if (m_app.isEnabled() && isAppTracing())
         startText = "stop";
      else
         startText = "start";

        m_startButton = new JButton(m_res.getString(startText));
      m_startButton.setMnemonic(m_res.getString(startText+".mn").charAt(0));
      if (!m_app.isEnabled())
         m_startButton.setEnabled(false);
        m_startButton.addActionListener(
         new ActionListener()
         {
            public void actionPerformed(ActionEvent e)
            {
               TracePropDialog.this.onStartStop();
            }
         }
      );
      m_startButton.setAlignmentX(CENTER_ALIGNMENT);
      box.add(m_startButton);

      box.add(Box.createVerticalStrut(5));
        m_applyButton = new UTFixedButton(m_res.getString("apply"));
      m_applyButton.setMnemonic(m_res.getString("apply.mn").charAt(0));
      m_applyButton.setEnabled(false);
        m_applyButton.addActionListener(
         new ActionListener()
         {
            public void actionPerformed(ActionEvent e)
            {
               TracePropDialog.this.onApply();
            }
         }
      );
      m_applyButton.setAlignmentX(CENTER_ALIGNMENT);
      box.add(m_applyButton);

      tracePanel.setLayout(new BorderLayout());
      tracePanel.add(box, BorderLayout.SOUTH);
      return tracePanel;
   }


   /**
    * Sends the given command string to the server using PSRemoteConsole.
    *
    * @param cmd The console command to send. May not be <code>null</code>.
    * @return The result xml document from the server.  May be
    * <code>null</code>.
    */
   private Document sendConsoleCmd(String cmd)
   {
      if (cmd == null || cmd.length() == 0)
         throw new IllegalArgumentException("cmd may not be null");

      Document   xmlDoc   = null;

      // Switch cursor to an hourglass
      this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

      // Send command to the server
      try {
         PSDesignerConnection conn = E2Designer.getDesignerConnection();
         PSRemoteConsole console = new PSRemoteConsole(conn);
         xmlDoc = console.execute(cmd);
      }
      catch (Exception e)
      {
         PSDlgUtil.showError(e, true, 
               E2Designer.getResources().getString("ServerConnErr"));
      }
      finally
      {
         // Switch cursor back
         this.setCursor(Cursor.getDefaultCursor());
      }

      return xmlDoc;
   }

   /**
    * Constructs a trace console command to start tracing given the
    * current dialog settings.
    *
    * @param table the JTable containg the options to use to construct the
    * console command.  May not be <code>null</code>.
    * @param flag a PSTraceFlag that has its options set using the table.  This
    * flag's state will match the options sent to the server after this
    * method call. May not be <code>null</code>.
    * @return a string containing the console command to be passed to the
    * server.
    */
   private String getTraceStartConsoleCmd(JTable table, PSTraceFlag flag)
   {
      if (table == null)
         throw new IllegalArgumentException("table may not be null");

      if (flag == null)
         throw new IllegalArgumentException("flag may not be null");

      String cmd = TRACE_CMD;

      // get flag set from options selected in the table
      setTraceFlagsFromTable(table, flag);

      // get the composite flag for each group and construct the call
      for (int i = 0; i < FLAG_GROUP_COUNT; i++)
        cmd += FLAG_PREFIX + Integer.toHexString(flag.getFlag(i));

      // now tack on the app name
      cmd += " " + m_app.getName();

      return cmd;
   }

   /**
    * Constructs a trace console command to stop tracing.
    *
    * @return a string containing the console command to be passed to the
    * server.
    */
   private String getTraceStopConsoleCmd()
   {
      String cmd = TRACE_CMD + TRACE_NONE + " " + m_app.getName();

      return cmd;
   }

   /**
    * Constructs a trace console command to get app tracing status.
    *
    * @return a string containing the console command to be passed to the
    * server.
    */
   private String getTraceStatusConsoleCmd()
   {
      String cmd = TRACE_CMD + " " + m_app.getName();

      return cmd;
   }

   /**
    * Walks the table and sets the selected options on the trace flag
    * object passed in.  Sets bit if selected in table, clears bit if not.
    *
    * @param table the JTable containing the trace options selected.  May not
    * be <code>null</code>.
    * @param flag the PSTraceFlag object to set the flags on.  This flag's state
    * will match that of the options selected in the table after this method
    * call completes. May be <code>null</code>.
    */
   private void setTraceFlagsFromTable(JTable table, PSTraceFlag flag)
   {
      if (table == null)
         throw new IllegalArgumentException("table may not be null");

      if (flag == null)
         flag = new PSTraceFlag();


      /* Need to force finish editing the current cell of the table if last
       * thing use did was to click a check box - in this case the checkbox
       * cell is still in edit mode and the action is
       * not yet recorded in the table model
       */
      int edRow = table.getEditingRow();
      int edCol = table.getEditingColumn();
      if ((edRow != -1) && (edCol != -1))
      table.getCellEditor(edRow, edCol).stopCellEditing();


      // trace is enabled, create an empty flag and set bits on it
      DefaultTableModel dtm = (DefaultTableModel)table.getModel();

      // walk table entries - option list should be in the same order
      for (int i = 0; i < dtm.getRowCount(); i++)
      {
         // get matching trace option for this row
         PSTraceOption option = (PSTraceOption)m_optionList.get(i);

         /* check row's value for first column and set flag for current option
          * if true
          */
         if (((Boolean)dtm.getValueAt(i, 0)).booleanValue())
            flag.setBit(option.getFlag());
         else
            flag.clearBit(option.getFlag());
      }

   }


   /**
    * Determines if the application currently has tracing active.  If first
    * time called, sends a console command to see if the app is
    * currently tracing.  Stores result in a member variable.  After that,
    * it checks member variable.
    */
   private boolean isAppTracing()
   {
      try
      {
         if (!m_appTracingChecked)
         {
            // send a console command
            Document doc = sendConsoleCmd(getTraceStatusConsoleCmd());

            // check response
            if (doc != null)
            {
               int result = -1;
               PSXmlTreeWalker walker = new PSXmlTreeWalker(doc);
               if (walker.getNextElement("resultCode", true, true) != null)
                  result = Integer.parseInt(
                     walker.getElementData("resultCode", false));

               if ( result == 0 )
               {
                  // locate the PSXTraceFlag node
                  Element el = walker.getNextElement("PSXTraceFlag", true, true);
                  if (el != null)
                  {
                     int flagTot = 0;

                     // get the value of each flag attribute from the node
                     for (int i = 1; i < FLAG_GROUP_COUNT; i++)
                     {
                        String flag = el.getAttribute(FLAG_ATTR + i);

                        // strip off hex prefix
                        if (flag.toLowerCase().startsWith("0x"))
                           flag = flag.substring("0x".length());

                        flagTot += Integer.parseInt(flag, 16);
                     }

                     // if we got any non-zero flags, tracing is active
                     m_isAppTracing = (flagTot > 0);

                  }
               }
            }

            // set the appTraceChecked flag so we don't ask the server again
            m_appTracingChecked = true;
         }
      }
      catch(NumberFormatException e)
      {
         // server passed us a bad flag?
         e.printStackTrace();
         throw new IllegalArgumentException(
            "Invalid trace flag format from server: " + e.getLocalizedMessage());
      }

      return m_isAppTracing;
   }

   /**
    * The objectstore object containing the app's trace options.
    * This a reference the actual object contained in the app, so any
    * changes to this object reference are automatically reflected in the app.
    * Initialized in the init method, never <code>null</code> after that.
    * @see #init(PSApplication)
    */
   private PSTraceInfo m_traceInfo;

   /**
    * The objectstore object containing the app. Passed into the init method,
    * never <code>null</code> after that.
    * @see #init(PSApplication)
    */
   private PSApplication m_app;

   /**
    * The Start/Stop button. Text will change from Start to Stop depending on
    * whether tracing has been started.  Will be greyed out if the application
    * is not running.  Starts or stops tracing by sending a remote console
    * command to the server.  Initialized during
    * construction, never <code>null</code> after that.
    * @see #initUI()
    */
   private JButton m_startButton;

   /**
    * The Apply button. Causes the current trace settings to be applied by
    * sending a remote console command to the server.  Will be greyed out if
    * tracing is not already started.  Initialized during
    * construction, never <code>null</code> after that.
    * @see #initUI()
    */
   private JButton m_applyButton;

   /**
    * The text box for the column width.  Initialized during
    * construction, never <code>null</code> after that.
    */
   private UTFixedTextField m_colWidthText;

   /**
    * The checkbox to indicate if TimeStampOnly tracing is enabled.
    * Initialized during construction, never <code>null</code> after that.
    */
   private JCheckBox m_timeStampOnlyCB;

   /**
    * The checkbox to indicate if tracing is enabled.
    * Initialized during construction, never <code>null</code> after that.
    */
   private JCheckBox m_enabledCB;

   /**
    * Displays the description of the currently selected trace option.
    * Initialized during construction, never <code>null</code> after that.
    */
   private JTextArea m_descText;

   /**
    * Contains the checkboxes for each trace option that can be selected
    * Initialized during construction, never <code>null</code> after that.
    */
   private JTable m_optionListTable;

   /**
    * Contains the traceOptions for easy access later.
    * Initialized during construction, never <code>null</code> after that.
    */
   private List<PSTraceOption> m_optionList;

   /**
    * The scrollpane containing the description text.  Always set when
    * dialog is constructed, never <code>null</code> after that.
    */
   private JScrollPane m_descScrollPane;

   /**
    * The resource bundle for this dialog.  Always set by init method.
    */
   private ResourceBundle m_res;

   /**
    * Determines if the app has tracing activated.  May be set to true by first
    * call to isAppTracing.  Subsequently, clicking the apply button or stop
    * button may alter this value.
    * @see #isAppTracing()
    */
   private boolean m_isAppTracing;

   /**
    * Determines if call to server has been made to determine is tracing is
    * active.  Checked in call to isAppTracing.
    * @see #isAppTracing()
    */
   private boolean m_appTracingChecked;


   /**
    * This dialogs preferred size.
    */
   private final static Dimension DIALOG_SIZE = new Dimension(380, 480);

   /**
    * Root of trace console command.  Used for constructing commands to send
    * to the server.
    */
   private final static String TRACE_CMD = "trace";

   /**
    * The trace flags to send when disabling tracing.
    */
   private final static String TRACE_NONE = " 0 0 0 0";


   /**
    * Fhe prefix for flags when sending console cmd.
    */
   private final static String FLAG_PREFIX = " 0x";

   /**
    * The prefix for the flag attribute in the response from the server console
    * command "trace <appname>. Used to retrieve each flag value returned as
    * an attribute.
    */
   private final static String FLAG_ATTR = "flag";

   /**
    * The number of groups supported by the PFTraceFlag.  Used when getting
    * or setting the value of each group.
    */
   private final static int FLAG_GROUP_COUNT = 4;

   /**
    * The minimum output column width that can be specified.
    */
   private final static int COL_WIDTH_MIN = 40;

   /**
    * The maximum output column width that can be specified.
    */
   private final static int COL_WIDTH_MAX = 200;

   /**
    * A Table model that forces the second column to be read only.  Used
    * for simulating the label on a check box contained in the first column.
    */
   public class CheckBoxTableModel extends DefaultTableModel
   {
      /**
       * Constructor for this class
       */
      public CheckBoxTableModel(int numRows, int numCols)
      {
         super(numRows, numCols);
      }

      /**
       * Overrides method in parent to return false whenever checking on
       * a cell in the second column.
       * @see javax.swing.table.DefaultTableModel#isCellEditable(int, int)
       */
      public boolean isCellEditable(int rowIndex,
                           int columnIndex)
      {
         if (columnIndex == 1)
            return false;
         else
            return super.isCellEditable(rowIndex, columnIndex);
      }
   }
}

