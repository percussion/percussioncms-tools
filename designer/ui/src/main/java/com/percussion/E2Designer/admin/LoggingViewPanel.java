/******************************************************************************
 *
 * [ LoggingViewPanel.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/


package com.percussion.E2Designer.admin;

import com.percussion.E2Designer.UTMultiLineCellRenderer;
import com.percussion.E2Designer.UTMultiLineTable;
import com.percussion.E2Designer.UTNodePrinter;
import com.percussion.E2Designer.UTReadOnlyTableCellEditor;
import com.percussion.conn.PSDesignerConnection;
import com.percussion.conn.PSServerException;
import com.percussion.error.PSException;
import com.percussion.xml.PSXmlTreeWalker;
import com.percussion.UTComponents.UTFixedButton;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ResourceBundle;
import java.util.Vector;

/**
 * The applets main dialog is implemented as a tabbed dialog. One Tab of this
 * dialog is the "Logging" Tab which itself contains several Tabs. This class
 * implements the GUI elements and its functionality for the "Logging" "View" Tab.
 */
public class LoggingViewPanel extends JPanel implements ITabDataHelper
{
   /**
    * Construct the GUI elements and initialize them with actual data.
    *
    * @param   serverConsole      the server remote console
    */
   public LoggingViewPanel(ServerConsole serverConsole)
   {
      try
      {
       m_serverConsole = serverConsole;
         this.setLayout(new BorderLayout());
      this.setBorder(new EmptyBorder(5, 5, 5, 5));

      // add panels
       this.add(createQueryConditionsPanel(), BorderLayout.NORTH);
         this.add(createCommandPanel(), BorderLayout.SOUTH);
      }
      catch (Exception ex)
      {
         ex.printStackTrace();
      }
      m_vTableData = new Vector();
   }

   /**
    * Create the query conditions panel.
    *
    */
   private   JPanel createQueryConditionsPanel()
   {
      JPanel panel = new JPanel(new BorderLayout());
      panel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED),
         m_res.getString("queryConditions")));
      
      
      JPanel cbPanel = new JPanel(new GridLayout(1,2));
      cbPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED),
         m_res.getString("queryType")));
      
      JPanel leftPanel = new JPanel();
      leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
      leftPanel.setPreferredSize(new Dimension(120, 145));
      leftPanel.add(m_cbErrors);
      leftPanel.add(m_cbServerStart);
      leftPanel.add(m_cbServerStop);
      leftPanel.add(m_cbApplicationStart);
      leftPanel.add(m_cbApplicationStop);
      leftPanel.add(m_cbApplicationStats);
      JPanel rightPanel = new JPanel();
      rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
      rightPanel.setPreferredSize(new Dimension(120, 145));
      rightPanel.add(m_cbBasicUserActivity);
      rightPanel.add(m_cbDetailedUserActivity);
      rightPanel.add(m_cbFullUserActivity);
      rightPanel.add(m_cbWarnings);
      rightPanel.add(m_cbMultipleAppHandlers);
      cbPanel.add(leftPanel);
      cbPanel.add(rightPanel);
      panel.add(cbPanel, BorderLayout.CENTER);
      
      JPanel datePanel = new JPanel();
      datePanel.setLayout(new BoxLayout(datePanel, BoxLayout.Y_AXIS));
      datePanel.setBorder(new TitledBorder(new EtchedBorder(
         EtchedBorder.LOWERED), m_res.getString("queryDate")));
      datePanel.setPreferredSize(new Dimension(180, 120));
      datePanel.add(new JLabel(m_res.getString("dateFormat")));
      datePanel.add(Box.createVerticalStrut(14));
      
      
      JLabel qStartLabel = new JLabel(m_res.getString("queryStart"));
      datePanel.add(qStartLabel);
      
      m_tfStartDate.setPreferredSize(new Dimension(80, 20));
      datePanel.add(m_tfStartDate);
      datePanel.add(Box.createVerticalStrut(6));
      JLabel qEndLabel = new JLabel(m_res.getString("queryEnd"));
      datePanel.add(qEndLabel);
      qEndLabel.setLabelFor(m_tfEndDate);
      m_tfEndDate.setPreferredSize(new Dimension(80, 20));
      datePanel.add(m_tfEndDate);
      datePanel.add(Box.createVerticalStrut(45));
      panel.add(datePanel, BorderLayout.EAST);
      
      return panel;
   }


   /**
    * Create and initialize the Command Panel and implement its action
   * listener.
    *
    */
   private   JPanel createCommandPanel()
  {
     m_buttonGetIt.addActionListener(new ActionListener()
    {
        public void actionPerformed(ActionEvent event)
        {
             onGetIt();
       }
    });
    m_buttonGetIt.setPreferredSize(new Dimension(80, 22));
    
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

    panel.add(m_buttonGetIt);

    return panel;
   }

   /**
    * Create the description table. This is created only after the 
    * log data is retrieved from the server.
    * @param vData the table data which is a vector of vectors (row data)
    *
    */
   private   void createDescriptionTable(Vector vData)
  {
    Vector vTableHeaders = new Vector();

      vTableHeaders.add(m_res.getString("queryDateFormat"));
      vTableHeaders.add(m_res.getString("queryType"));
      vTableHeaders.add(m_res.getString("description"));

      m_table = new UTMultiLineTable(vData, vTableHeaders);
      m_table.getTableHeader().setReorderingAllowed(false);

      m_table.getColumnModel().getColumn(0).setPreferredWidth(120);
      m_table.getColumnModel().getColumn(1).setPreferredWidth(80);
      m_table.getColumnModel().getColumn(2).setPreferredWidth(500);

      UTMultiLineCellRenderer renderer = new UTMultiLineCellRenderer();
      m_table.getColumnModel().getColumn(2).setCellRenderer(renderer);

      UTReadOnlyTableCellEditor editor = new UTReadOnlyTableCellEditor();
      for(int i=0; i<3; i++)
         m_table.getColumnModel().getColumn(i).setCellEditor(editor);

   }

   /**
    * Handler for GetIt button clicked. Walks through the selected items 
    * and executes the appropriate log dump command. Gets the data from
    * the server and makes the call to construct the table and the dialog
    * to display the data.
    *
    */
   private void onGetIt()
   {
      String since ="";
      String until ="";
      
      if(!isSomethingSelected())
      {
         JOptionPane.showMessageDialog(AppletMainDialog.getMainframe(), m_res.getString("selectAQuery"));
         return;
      }

      String strStartDate = getStartDateTime();
      if(strStartDate == null)
         return;
      if(!strStartDate.equals(""))
         since=" since "+strStartDate;

      String strEndDate = getEndDateTime();
      if(strEndDate == null)
         return;

      if(!strEndDate.equals(""))
         until=" until "+strEndDate;

      m_vTableData.clear();

      StringBuffer commandBuf = new StringBuffer("log dump");
      if(m_cbErrors.isSelected())
      {
         /*
          * If errors are selected, get all error types: internal errors (can
          * not be turned off by the user) and all other errors (if logging
          * for these is turned on by the user.
          */ 
         commandBuf.append(" type 0");
         commandBuf.append(" type 1");
      }
      if(m_cbServerStart.isSelected())
      {
         commandBuf.append(" type 2");
      }
      if(m_cbServerStop.isSelected())
      {
         commandBuf.append(" type 3");
      }
      if(m_cbApplicationStart.isSelected())
      {
         commandBuf.append(" type 4");
      }
      if(m_cbApplicationStop.isSelected())
      {
         commandBuf.append(" type 5");
      }
      if(m_cbApplicationStats.isSelected())
      {
         commandBuf.append(" type 6");
      }
      if(m_cbBasicUserActivity.isSelected())
      {
         commandBuf.append(" type 7");
      }
      if(m_cbDetailedUserActivity.isSelected())
      {
         commandBuf.append(" type 8");
      }
      if(m_cbFullUserActivity.isSelected())
      {
         commandBuf.append(" type 9");
      }
      if(m_cbWarnings.isSelected())
      {
         commandBuf.append(" type 10");
      }
      if(m_cbMultipleAppHandlers.isSelected())
      {
         commandBuf.append(" type 11");
      }

      commandBuf.append(since);
      commandBuf.append(until);

      processCommand(commandBuf.toString());

      if(m_vTableData.size() <=0 )
      {
         JOptionPane.showMessageDialog(AppletMainDialog.getMainframe(), m_res.getString("noLogResultsReturned"));
         return;
      }
      else
      {
         createDescriptionTable(m_vTableData);
         (new LoggingDescriptionDialog(m_res.getString("titleLogging"), m_table)).setVisible(true);
      }
   }

   /**
    * Gets the start date from the text field. Also makes call to validate the date. 
    *
    */
   private String getStartDateTime()
   {
      String strDate = m_tfStartDate.getText().trim();
      if(strDate == null || strDate.equals(""))
         return "";
      else
      { 
         if(isDateAcceptable(strDate, true))
         {
            return "'"+strDate+"'";
         }
      }
      return null;
   }

   /**
    * Gets the end date from the text field. Also makes call to validate the date. 
    *
    */
   private String getEndDateTime()
   {
      String strDate = m_tfEndDate.getText().trim();
      if(strDate == null || strDate.equals(""))
         return "";
      else
      { 
         if(isDateAcceptable(strDate, false))
         {
            return "'"+strDate+"'";
         }
      }
      return null;
   }

   /** Performs a check on the passed in date string.
    * @param strDate - the date and time string to be validated.
    * returns true if the passed in strDate is a valid date and time string. 
    * @param bIsStartDate - true if it is validating start date, false for end date.
    */
   private boolean isDateAcceptable(String strDate, boolean bIsStartDate)
   {
      boolean bAcceptable = true;
      try
      {
         ms_dateFormatter.parse(strDate);
      }
      catch(ParseException e)
      {
         bAcceptable = false;
         displayDateErrorMessage(e, bIsStartDate);
      }
      return bAcceptable;
   }

   /** Displays the error message if an exception occurred during parsing the
    * date string.
    * @param e - the exception.
    * @param bIsStartDate - true if it is validating start date, false for end date.
    */
   private void displayDateErrorMessage(ParseException e, boolean bIsStartDate)
   {
      String message = m_res.getString("errorReadingDate");
      message = message+"\n"+e.toString()+"\n";
      JOptionPane.showMessageDialog(AppletMainDialog.getMainframe(),
                                                 message,
                                     m_res.getString("error"),
                                     JOptionPane.ERROR_MESSAGE);
      if(bIsStartDate)
         m_tfStartDate.requestFocus();
      else
         m_tfEndDate.requestFocus();
   }

   /** 
    * @returns true if at least one of the check boxes is selected.
    */
   private boolean isSomethingSelected()
   {
      boolean bSomethingSelected = false;
      if(m_cbErrors.isSelected())
         bSomethingSelected = true;
      else if(m_cbServerStart.isSelected())
         bSomethingSelected = true;
      else if(m_cbServerStop.isSelected())
         bSomethingSelected = true;
      else if(m_cbApplicationStart.isSelected())
         bSomethingSelected = true;
      else if(m_cbApplicationStop.isSelected())
         bSomethingSelected = true;
      else if(m_cbApplicationStats.isSelected())
         bSomethingSelected = true;
      else if(m_cbBasicUserActivity.isSelected())
         bSomethingSelected = true;
      else if(m_cbDetailedUserActivity.isSelected())
         bSomethingSelected = true;
      else if(m_cbFullUserActivity.isSelected())
         bSomethingSelected = true;
      else if(m_cbWarnings.isSelected())
         bSomethingSelected = true;
      else if(m_cbMultipleAppHandlers.isSelected())
         bSomethingSelected = true;
      return bSomethingSelected;
   }


   /** 
    * returns a string that describes the log type
    *@param - iLogType is the integer associated with the type.
    *@see com.percussion.server.PSRemoteConsole
    */
   private String getLogType(int iLogType)
   {
      switch(iLogType)
      {
         case 1:
            return (m_res.getString("viewErrors")); 
         case 2:
            return (m_res.getString("serverStart"));
         case 3:
            return (m_res.getString("serverStop"));
         case 4:
            return (m_res.getString("applicationStart"));
         case 5:
            return (m_res.getString("applicationStop"));
         case 6:
            return (m_res.getString("applicationStats"));
         case 7:
            return (m_res.getString("basicUserActivity"));
         case 8:
            return (m_res.getString("detailedUserActivity"));
         case 9:
            return (m_res.getString("fullUserActivity"));
         case 10:
            return (m_res.getString("warnings"));
         case 11:
            return (m_res.getString("multipleApplicationHandlers"));
         default:
            return "";
      }

   }

   /** executes the command.
    * @param command - the command to be executed.
    *@see com.percussion.server.PSRemoteConsole
    */
   private void processCommand(String command)
   {
      try
      {
         Document xmlDoc = m_serverConsole.execute(command);
         PSException ex = processDocument(xmlDoc);
         if (ex != null)
         {
            // get the internal error if there is one
            if (ex instanceof PSServerException)
            {
               PSException inEx = ((PSServerException)ex).getOriginatingException();
               if (inEx != null)
                  ex = inEx;
            }
            throw ex;
         }
      }
      catch(Exception e)
      {
         processError(e.toString());
      }
   }

   /** Processes the error by adding it to the Description table.
    * @param strError - the error generated by the server.
    *@see com.percussion.server.PSRemoteConsole
    */
   private void processError(String strError)
   {
         Vector vRow = new Vector();
         vRow.add(m_res.getString("error"));
         // vRow.add(getLogType(iLogType));
         vRow.add("???");
         vRow.add(strError);
         m_vTableData.add(vRow);
   }

   /**
    * Processes the document generated by the command. Adds the log
    * results to the table.
    *
    * @param xmlDoc - the document associated with the log.
    *
    * @return A PSException object if there was an error, or <CODE>null</CODE>
    * if everything was OK.
    *
    * @see com.percussion.server.PSRemoteConsole
    */
   private PSException processDocument(Document xmlDoc)
   {
      Element            root = xmlDoc.getDocumentElement();

      // see if there is an error in here
      PSException ex = PSDesignerConnection.createExceptionFromXml(root);
      if (ex != null)
         return ex;

      PSXmlTreeWalker   walker   = new PSXmlTreeWalker(xmlDoc);
      Node               saveCur;

      Node               rootNode = walker.getCurrent();

      Element msgEl = null;
      while(null != (msgEl = walker.getNextElement("PSXLogMessage", true, true)))
      {
         int iLogType = 0;
         String type = msgEl.getAttribute("type");
         if (type != null && type.length() > 0)
         {
            iLogType = Integer.parseInt(type);
         }

         saveCur = walker.getCurrent();
         String dateAndTime = "";

         String strTime = null;
         if(walker.getNextElement("time", true, true) != null)
         {
            strTime = walker.getElementData("time", false);
            dateAndTime = parseTime(strTime);
         }
         walker.setCurrent(saveCur);

         // print the xml representation of the result
         ByteArrayOutputStream stream = new ByteArrayOutputStream();
         PrintWriter pw = new PrintWriter(stream);
         UTNodePrinter.printNode(saveCur, " ", pw);
         pw.flush();
         Vector vRow = new Vector();
         vRow.add(dateAndTime);
         vRow.add(getLogType(iLogType));
         vRow.add(stream.toString());
         m_vTableData.add(vRow);
      }

      return null;
   }

   /** 
    * parses the text associated with the time element of the xmlDocument
    * returned by the server
    * the time element string has the format dateTtime where date is in format yyyymmdd
    * and time is in format hr:min:sec:millisec
    */
   private String parseTime(String strTime)
   {
      int i = strTime.indexOf("T");
      String date = strTime.substring(0, i);
      String time = strTime.substring(i+1);
      date = date+" "+time.substring(0,2)+":"+time.substring(2,4)+":"+time.substring(4,6)+":"+time.substring(6);
      return date;
   }

/** 
   * nothing to save for this panel.
  *
  * @returns boolean <CODE>true</CODE> always.
   */
   public boolean saveTabData()
   {
    return true;
   }

/** Does not need validation. Thus does nothing.
*/
  public boolean validateTabData()
  {
    return true;
  }             


   /**
    * the server console we are working with
    */
  private ServerConsole m_serverConsole = null;

   JCheckBox m_cbErrors = new JCheckBox(m_res.getString("viewErrors")); 
   JCheckBox m_cbServerStart = new JCheckBox(m_res.getString("serverStart"));
   JCheckBox m_cbServerStop = new JCheckBox(m_res.getString("serverStop"));
   JCheckBox m_cbApplicationStart = new JCheckBox(m_res.getString("applicationStart"));
   JCheckBox m_cbApplicationStop = new JCheckBox(m_res.getString("applicationStop"));
   JCheckBox m_cbApplicationStats = new JCheckBox(m_res.getString("applicationStats"));
   JCheckBox m_cbBasicUserActivity = new JCheckBox(m_res.getString("basicUserActivity"));
   JCheckBox m_cbDetailedUserActivity = new JCheckBox(m_res.getString("detailedUserActivity"));
   JCheckBox m_cbFullUserActivity = new JCheckBox(m_res.getString("fullUserActivity"));
   JCheckBox m_cbWarnings = new JCheckBox(m_res.getString("warnings"));
   JCheckBox m_cbMultipleAppHandlers = new JCheckBox(m_res.getString("multipleApplicationHandlers"));

   JTextField m_tfStartDate = new JTextField();
   JTextField m_tfEndDate = new JTextField();

   /**
    * the table capable of supporting multiple lines in a cell
    */
  UTMultiLineTable  m_table;

   /**
    * the table data. Vector of vectors (row data).
    */
   private Vector m_vTableData = null;

   private static SimpleDateFormat ms_dateFormatter = new SimpleDateFormat("yyyyMMdd hh:mm:ss");


   /**
    * the get it button
    */
  private JButton m_buttonGetIt = new UTFixedButton(m_res.getString("getIt"));
   /**
   * Resources
   */
  private static ResourceBundle m_res = PSServerAdminApplet.getResources();
}
