/*[ MonitorConsolePanel.java ]*************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer.admin;

import com.percussion.E2Designer.UTNodePrinter;
import com.percussion.xml.PSXmlTreeWalker;
import com.percussion.UTComponents.UTFixedButton;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.ResourceBundle;



/**
 * The applets main dialog is implemented as a tabbed dialog. One Tab of this
 * dialog is the "Logging" Tab which itself contains several Tabs. This class
 * implements the GUI elements and its functionality for the "Logging" "Settings" Tab.
 */
public class MonitorConsolePanel extends JPanel
{
   /**
    * Construct the GUI elements and initialize them with actual data.
    *
    * @param   serverConsole     the actual server configuration
    */
   public MonitorConsolePanel(ServerConsole serverConsole)
   {
      m_serverConsole = serverConsole;
      this.setLayout(new BorderLayout());
      this.setBorder(new EmptyBorder(5,5,5,5));

      // add panels
      this.add(createCommandPanel(), BorderLayout.NORTH);
      this.add(createCommandOutputPanel(), BorderLayout.CENTER);
   }

   /**
    * Create the command panel and implement their action listeners.
    *
    */
   private  JPanel createCommandPanel()
   {
      JPanel panel = new JPanel();

      JLabel labelCommand = new JLabel(m_res.getString("command"));
      m_tfCommand = new JTextField();
      m_tfCommand.setPreferredSize(new Dimension(340, 24));
      m_tfCommand.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            m_buttonExecute.doClick();
         }
      });
    
      m_buttonExecute = new UTFixedButton(m_res.getString("execute"));
      m_buttonExecute.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            onExecute();
         }
      });

      panel.add(labelCommand);
      panel.add(m_tfCommand);
      panel.add(m_buttonExecute);

      return panel;
   }

   /**
    * Create the command output panel.
    *
    */
   private JPanel createCommandOutputPanel()
   {
      JPanel panel = new JPanel(new BorderLayout());
      JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
      topPanel.add(new JLabel(m_res.getString("commandOutput")));

      m_taCommandOutput = new JTextArea();
      m_taCommandOutput.setLineWrap(true);
      m_taCommandOutput.setWrapStyleWord(true);
      m_taCommandOutput.setEditable(false);
      JScrollPane pane = new JScrollPane(m_taCommandOutput);

      panel.add(topPanel, BorderLayout.NORTH);
      panel.add(pane, BorderLayout.CENTER);

      return panel;
   }

   /**
    * Handler for Execute button clicked.
    *
    */
   private void onExecute()
   {
      String strCommand = m_tfCommand.getText().trim();
      if(strCommand.equals(""))
         return;

      m_strCommand = ConsoleCommandHelper.removeExtraWhitespace(strCommand);
//    System.out.println("Command after trimming and parsing ="+m_strCommand);
      m_strBaseCommand = ConsoleCommandHelper.getBaseCommand(m_strCommand);
//    System.out.println("Base command ="+m_strBaseCommand);
      Document xmlDoc = null;
      try
      {
         xmlDoc = m_serverConsole.execute(m_strCommand);
      }
      catch(Exception e)
      {
         m_taCommandOutput.setText("");
         m_taCommandOutput.setText(e.toString());
         m_taCommandOutput.repaint();
         return;
      }

      if(xmlDoc != null)
         processDocument(xmlDoc);
   }

   /**
    * process the xmlDocument and print output to the text area.
    *
    */
   private void processDocument(Document xmlDoc)
   {
      
      m_taCommandOutput.setText("");
      Element           root = xmlDoc.getDocumentElement();
      PSXmlTreeWalker   walker   = new PSXmlTreeWalker(xmlDoc);
      Node              saveCur;

      Node              rootNode = walker.getCurrent();

      if(walker.getNextElement("command", true, true) != null)
      {
         m_taCommandOutput.append("Command: ");
         m_taCommandOutput.append(walker.getElementData("command", false)+"\n");
      }

      if(m_strBaseCommand.equalsIgnoreCase(ConsoleCommandHelper.START_SERVER) ||
          m_strBaseCommand.equalsIgnoreCase(ConsoleCommandHelper.START_APPLICATION) ||
          m_strBaseCommand.equalsIgnoreCase(ConsoleCommandHelper.STOP_SERVER) ||
          m_strBaseCommand.equalsIgnoreCase(ConsoleCommandHelper.STOP_APPLICATION) ||
          m_strBaseCommand.equalsIgnoreCase(ConsoleCommandHelper.RESTART_SERVER) ||
          m_strBaseCommand.equalsIgnoreCase(ConsoleCommandHelper.RESTART_APPLICATION) ||
          m_strBaseCommand.equalsIgnoreCase(ConsoleCommandHelper.LOG_FLUSH))
      {
         if(walker.getNextElement("resultCode", true, true) != null)
         {
            m_taCommandOutput.append("Result Code: ");
            m_taCommandOutput.append(walker.getElementData("resultCode", false)+"\n");
         }
         if(walker.getNextElement("resultText", true, true) != null)
         {
            m_taCommandOutput.append("Result Text: ");
            m_taCommandOutput.append(walker.getElementData("resultText", false)+"\n");
         }
      }

      else if(m_strBaseCommand.equalsIgnoreCase(ConsoleCommandHelper.SHOW_APPLICATIONS))
      {
         m_taCommandOutput.append("List of Applications: \n");
         if (walker.getNextElement("Application", true, true) != null) 
         {
            saveCur = walker.getCurrent();
            while (walker.getNextElement("name", false, true) != null)
            {
               m_taCommandOutput.append("\t"+walker.getElementData("name", false));
               m_taCommandOutput.append("\n");
            }
            walker.setCurrent(saveCur);
         }
      }
      else if(m_strBaseCommand.equalsIgnoreCase(ConsoleCommandHelper.SHOW_STATUS_SERVER))
      {
//       if (walker.getNextElement("PSXServerStatus", true, true) != null) 
               m_taCommandOutput.append("Server status: \n");
            processStatistics(walker);
      }
      else if(m_strBaseCommand.equalsIgnoreCase(ConsoleCommandHelper.SHOW_STATUS_APPLICATION))
      {
//       if (walker.getNextElement("PSXApplicationStatus", true, true) != null)
//       {
            saveCur = walker.getCurrent();
            if (walker.getNextElement("name", true, true) != null)
            {
               m_taCommandOutput.append("Application status for ");
               m_taCommandOutput.append(walker.getElementData("name", false));
               m_taCommandOutput.append(":\n");
            }
            walker.setCurrent(saveCur);
            processStatistics(walker);
//       }
      }
      else
      {
         // print the xml representation of the result
         ByteArrayOutputStream stream = new ByteArrayOutputStream();
         PrintWriter pw = new PrintWriter(stream);
         UTNodePrinter.printNode(rootNode, " ", pw);
         pw.flush();
         m_taCommandOutput.append(stream.toString());
      }
   }

   /**
    * Processes the PSXStatistics node and it's children and outputs to the text area
    * on the panel.
    */
   private void processStatistics(PSXmlTreeWalker walker)
   {
      Node saveCur;
      if (walker.getNextElement("PSXStatistics", true, true) != null)
      {
         saveCur = walker.getCurrent();
         if (walker.getNextElement("ElapsedTime", true, true) != null) 
         {
            m_taCommandOutput.append("Time elapsed: ");
            if (walker.getNextElement("days", true, true) != null)
               m_taCommandOutput.append(walker.getElementData("days", false)+" days");
            if (walker.getNextElement("hours", true, true) != null)
               m_taCommandOutput.append(", "+walker.getElementData("hours", false)+" hours");
            if (walker.getNextElement("minutes", true, true) != null)
               m_taCommandOutput.append(", "+walker.getElementData("minutes", false)+" minutes");
            if (walker.getNextElement("seconds", true, true) != null)
               m_taCommandOutput.append(", "+walker.getElementData("seconds", false)+" seconds");
            if (walker.getNextElement("milliseconds", true, true) != null)
               m_taCommandOutput.append(", "+walker.getElementData("milliseconds", false)+" milliseconds");
            m_taCommandOutput.append(".\n");
            walker.setCurrent(saveCur);
         }
         if (walker.getNextElement("Counters", true, true) != null) 
         {
            m_taCommandOutput.append("Counters: ");
            if (walker.getNextElement("eventsProcessed", true, true) != null)
            {
               m_taCommandOutput.append("\n   Events processed= "
                  +walker.getElementData("eventsProcessed", false));
            }
            if (walker.getNextElement("eventsFailed", true, true) != null)
            {
               m_taCommandOutput.append("\n   Events failed= "
                  +walker.getElementData("eventsFailed", false));
            }
            if (walker.getNextElement("eventsPending", true, true) != null)
            {
               m_taCommandOutput.append("\n   Events pending= "
                  + walker.getElementData("eventsPending", false));
            }
            if (walker.getNextElement("cacheHits", true, true) != null)
            {
               m_taCommandOutput.append("\n   Cache Hits= "
                  + walker.getElementData("cacheHits", false));
            }
            if (walker.getNextElement("cacheMisses", true, true) != null)
            {
               m_taCommandOutput.append("\n   Cache Misses= "
                  + walker.getElementData("cacheMisses", false));
            }
            m_taCommandOutput.append("\n");
            walker.setCurrent(saveCur);
         }
         if (walker.getNextElement("Timers", true, true) != null) 
         {
            m_taCommandOutput.append(
                  "Timers (days:hours:minutes:seconds.millis): ");
            Element e = walker.getNextElement("EventAverage", true, true);
            if ( e != null)
               m_taCommandOutput.append( "\n   Event average= " + getTime(e));
            
            e = walker.getNextElement("EventMinimum", true, true);
            if ( e != null)
               m_taCommandOutput.append( "\n   Event minimum= " + getTime(e));
            
            e = walker.getNextElement("EventMaximum", true, true);   
            if ( e != null)
               m_taCommandOutput.append( "\n   Event maximum= " + getTime(e));
            m_taCommandOutput.append("\n");
            walker.setCurrent(saveCur);
         }
      }
   }

   /**
    * Expects the supplied element to contain the fillowing children:
    * <pre>
    * &lt;days&gt;0&lt;/days&gt;
    * &lt;hours&gt;0&lt;/hours&gt;
    * &lt;minutes&gt;0&lt;/minutes&gt;
    * &lt;seconds&gt;0&lt;/seconds&gt;
    * &lt;milliseconds&gt;0&lt;/milliseconds&gt;
    * </pre>
    * It reads the time from the nodes and creates a string in the following
    * format dd:hh:mm:ss.sss.
    * 
    * @param walker Doc walker positioned on the node containing the time
    *    elements. Assumed not <code>null</code>.
    *    
    * @return If any of the nodes aren't found, "" is returned, otherwise a time 
    *    duration as noted above is returned.
    */
   private String getTime( Element timer )
   {
      PSXmlTreeWalker walker = new PSXmlTreeWalker( timer );
      String days = walker.getElementData( "./days" );
      if ( null == days )
         return "";
      days.trim();
      String hours = walker.getElementData( "./hours" );
      if ( null == hours )
         return "";
      hours.trim();
      String minutes = walker.getElementData( "./minutes" );
      if ( null == minutes )
         return "";
      minutes.trim();
      String seconds = walker.getElementData( "./seconds" );
      if ( null == seconds )
         return "";
      seconds.trim();
      String milliseconds = walker.getElementData( "./milliseconds" );
      if ( null == milliseconds )
         return "";
      milliseconds.trim(); 
      if ( milliseconds.length() == 1 )
         milliseconds = "00" + milliseconds;
      else if ( milliseconds.length() == 2 )
         milliseconds = "0" + milliseconds;
            
      return days + ":" + hours + ":" + minutes + ":" + seconds + "." 
            + milliseconds;
   }

   /**
    * the server console we are working with
    */
   private ServerConsole m_serverConsole = null;

   /**
    * the command from the text field for entering command
    */
   private String m_strCommand = null;

   /**
    * the base command
    */
   private String m_strBaseCommand = null;

   /**
    * the text field to enter the command to be executed
    */
   private JTextField m_tfCommand = null;

   /**
    * the text area for command output
    */
   private JTextArea m_taCommandOutput = null;

   /**
    * the execute button
    */
   private UTFixedButton m_buttonExecute = null;

   /**
    * Resources
    */
   private static ResourceBundle m_res = PSServerAdminApplet.getResources();

}
