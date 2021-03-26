/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.htmlConverter;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

/**
 * The option dialog for XSpLit. This lets us define the HTML markup tag,
 * input encoding, output encoding and the base location to resolve relative
 * references.
 */
public class OptionsDialog extends JDialog
{
   /**
    * Construct a new modal Options dialog for the provided frame, with title.
    *
    * @param frame the parent frame, may be <code>null</code>
    * @param title the dialog title, may be empty
    */
   public OptionsDialog(JFrame frame, String title)
   {
      super(frame, title, true);

      initDialog();
      setLocationRelativeTo(frame);
   }

   /**
    * Construct a new modal Options dialog for the provided frame, with no
    * title.
    *
    * @param frame the parent frame, may be <code>null</code>
    * @param title the dialog title, may be empty
    */
   public OptionsDialog(JFrame frame)
   {
      this(frame, "");
   }
   
   /**
    * Initialize the dialog with the provided configuration.
    *
    * @param config the cofiguration to initialize the dialog with, not
    *    <code>null</code>.
    * @throws IllegalArgumentException if the provided configuration is 
    *    <code>null</code>.
    */
   public void initialize(SplitterConfiguration config)
   {
      if (config == null)
         throw new IllegalArgumentException("config cannot be null");
      
      m_config = config;
      
      m_psxTag.setText(config.getProperty("dynamicTag", 
         config.getDefaultProperty("dynamicTag")));
      
      m_rxTag.setText(config.getProperty("propertyTag", 
         config.getDefaultProperty("propertyTag")));
      
      m_inputEncoding.setSelectedItem(config.getProperty("inputEncoding", 
         config.getDefaultProperty("inputEncoding")));
      
      m_outputEncoding.setSelectedItem(config.getProperty("outputEncoding", 
         config.getDefaultProperty("outputEncoding")));
      
      m_xslOutputEncoding.setSelectedItem(config.getProperty("xslOutputEncoding", 
         config.getDefaultProperty("xslOutputEncoding")));
      
      m_baseLocation.setText(config.getProperty("baseLocation", 
         config.getDefaultProperty("baseLocation")));
         
      String showWarnings = config.getProperty("showWarnings", 
         config.getDefaultProperty("showWarnings"));
      m_showWarnings.setSelected(showWarnings.equalsIgnoreCase("yes"));
   }
   
   /** 
    * Test if the user returned with the OK button.
    * 
    * @return <code>true</code> if the user exited with OK, <code>false</code>
    *    otherwise
    */
   public boolean ok()
   {
      return m_exit == OK;
   }
   
   /**
    * Accessor to the current psxTag.
    *
    * @return the current psxTag string, never <code>null</code> or empty
    */
   public String getPsxTag()
   {
      return m_psxTag.getText();
   }
   
   /**
    * Accessor to the current rxTag.
    *
    * @return the current rxTag string, never <code>null</code> or empty
    */
   public String getRxTag()
   {
      return m_rxTag.getText();
   }
   
   /**
    * Accessor to the current input encoding selection.
    *
    * @return the current input encoding string, never <code>null</code> or 
    *    empty
    */
   public String getInputEncoding()
   {
      return (String) m_inputEncoding.getSelectedItem();
   }
   
   /**
    * Accessor to the current output encoding selection.
    *
    * @return the current output encoding string, never <code>null</code> or 
    *    empty
    */
   public String getOutputEncoding()
   {
      return (String) m_outputEncoding.getSelectedItem();
   }
   
   /**
    * Accessor to the current XSL output encoding selection.
    *
    * @return the current XSL output encoding string, never <code>null</code> or 
    *    empty
    */
   public String getXslOutputEncoding()
   {
      return (String) m_xslOutputEncoding.getSelectedItem();
   }
   
   /**
    * Accessor to the current base location string.
    *
    * @return the current base location string, never <code>null</code> or 
    *    empty
    */
   public String getBaseLocation()
   {
      return m_baseLocation.getText();
   }
   
   /**
    * Get the show warnings status.
    *
    * @return <code>true</code> to show warnings and errors, 
    *    <code>false</code> to show errors only.
    */
   public boolean showWarnings()
   {
      return m_showWarnings.isSelected();
   }

   /**
    * Create and initialize all GUI elements for this dialog.
    */
   private void initDialog()
   {
      JPanel p1 = new JPanel(new BorderLayout());
      p1.add(new JLabel(MainFrame.getRes().getString("psxTag")), "North");
      p1.add(m_psxTag, "Center");
      
      JPanel p2 = new JPanel(new BorderLayout());
      p2.add(new JLabel(MainFrame.getRes().getString("rxTag")), "North");
      p2.add(m_rxTag, "Center");
      
      JPanel p3 = new JPanel(new BorderLayout());
      p3.add(new JLabel(MainFrame.getRes().getString("inputEncoding")), 
        "North");
      p3.add(m_inputEncoding, "Center");
      
      JPanel p4 = new JPanel(new BorderLayout());
      p4.add(new JLabel(MainFrame.getRes().getString("outputEncoding")), 
        "North");
      p4.add(m_outputEncoding, "Center");
      
      JPanel p8 = new JPanel(new BorderLayout());
      p8.add(new JLabel(MainFrame.getRes().getString("xslOutputEncoding")), 
        "North");
      p8.add(m_xslOutputEncoding, "Center");
      
      UTFixedButton browse = new UTFixedButton("...", 20, 20);
      browse.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent event)
         {
            onBrowse();
         }
      });
      
      JPanel p5 = new JPanel(new BorderLayout());
      p5.add(m_baseLocation, "Center");
      p5.add(browse, "East");
      
      JPanel p6 = new JPanel(new BorderLayout());
      p6.add(new JLabel(MainFrame.getRes().getString("baseLocation")), "North");
      p6.add(p5, "Center");
      
      JPanel p7 = new JPanel(new BorderLayout());
      p7.add(m_showWarnings);
      
      Box b1 = Box.createVerticalBox();
      b1.add(p1);
      // this is not configurable by the user anymore
      //b1.add(Box.createVerticalStrut(5));
      //b1.add(p2);
      b1.add(Box.createVerticalStrut(5));
      b1.add(p3);
      b1.add(Box.createVerticalStrut(5));
      b1.add(p4);
      b1.add(Box.createVerticalStrut(5));
      b1.add(p8);
      b1.add(Box.createVerticalStrut(5));
      b1.add(p7);
      b1.add(Box.createVerticalStrut(5));
      
      Box b2 = Box.createHorizontalBox();
      b2.add(b1);
      b2.add(Box.createHorizontalStrut(20));
      b2.add(createCommandPanel());
      
      JPanel p = new JPanel(new BorderLayout());
      p.setBorder(new EmptyBorder(10, 10, 10, 10));
      p.add(b2, "Center");
      p.add(p6, "South");
      
      getContentPane().add(p);
      pack();
   }
   
   /**
    * Create a vertical command panel containing an Ok and a Cancel button.
    *
    * @return the created command panel, never <code>null</code>
    */
   private JPanel createCommandPanel()
   {
      UTFixedButton ok = new UTFixedButton(MainFrame.getRes().getString("ok"));
      ok.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent event)
         {
            onOk();
         }
      });
      ok.setDefaultCapable(true);
      getRootPane().setDefaultButton(ok);
      
      UTFixedButton cancel = new UTFixedButton(MainFrame.getRes().getString("cancel"));
      cancel.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent event)
         {
            onCancel();
         }
      });
      
      UTFixedButton def = new UTFixedButton(MainFrame.getRes().getString("default"));
      def.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent event)
         {
            onDefault();
         }
      });
      
      Box box = Box.createVerticalBox();
      box.add(ok);
      box.add(box.createVerticalStrut(5));
      box.add(cancel);
      box.add(box.createVerticalStrut(5));
      box.add(def);
      
      JPanel p = new JPanel(new BorderLayout());
      p.add(box);
      
      return p;
   }
   
   /**
    * Perform OK action.
    */
   private void onOk()
   {
      m_exit = OK;
      maintainDash();
      
      if (m_config != null)
      {
         m_config.setProperty("dynamicTag", getPsxTag());
         m_config.setProperty("propertyTag", getRxTag());
         m_config.setProperty("inputEncoding", getInputEncoding());
         m_config.setProperty("outputEncoding", getOutputEncoding());
         m_config.setProperty("xslOutputEncoding", getXslOutputEncoding());
         m_config.setProperty("baseLocation", getBaseLocation());
         m_config.setProperty("showWarnings", showWarnings() ? "yes" : "no");
            
         m_config = null;
      }
      
      File test = new File(m_baseLocation.getText());
      if (!test.isDirectory())
      {
         if (JOptionPane.showConfirmDialog(this, 
            MainFrame.getRes().getString("invalidDir"), 
            MainFrame.getRes().getString("errorTitle"),
            JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION)
               return;
         else
            m_baseLocation.setText(System.getProperty("user.dir"));
      }
      
      dispose();
   }
   
   /**
    * Perform Cancel action.
    */
   private void onCancel()
   {
      m_exit = CANCEL;
      dispose();
   }
   
   /**
    * Reset the current properties back to the default settings.
    */
   private void onDefault()
   {
      m_psxTag.setText(SplitterConfiguration.getDefaultProperty("dynamicTag"));
      m_rxTag.setText(SplitterConfiguration.getDefaultProperty("propertyTag"));
      m_inputEncoding.setSelectedItem(
         SplitterConfiguration.getDefaultProperty("inputEncoding"));
      m_outputEncoding.setSelectedItem(
         SplitterConfiguration.getDefaultProperty("outputEncoding"));
      m_xslOutputEncoding.setSelectedItem(
         SplitterConfiguration.getDefaultProperty("xslOutputEncoding"));
      m_baseLocation.setText(
         SplitterConfiguration.getDefaultProperty("baseLocation"));
      String showWarnings = 
         SplitterConfiguration.getDefaultProperty("showWarnings");
      m_showWarnings.setSelected(showWarnings.equalsIgnoreCase("yes"));
   }
   
   /**
    * The browse button opens the file chooser dialog in directory selection
    * mode. If the user exits by selecting the approve button the new selected
    * directory is set to the base location text field.
    */
   private void onBrowse()
   {
      File current = new File(m_baseLocation.getText());
      JFileChooser chooser = new JFileChooser(current);
      chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      if (chooser.showDialog(this, MainFrame.getRes().getString("ok")) == 
          JFileChooser.APPROVE_OPTION)
         m_baseLocation.setText(chooser.getSelectedFile().toString());
   }

   /**
    * Create the 'dash' at the tag end if the user didn't provide one.
    */
   private void maintainDash()
   {
      String tag = m_psxTag.getText();
      if (tag.charAt(tag.length()-1) != '-')
         m_psxTag.setText(tag + "-");

      tag = m_rxTag.getText();
      if (tag.charAt(tag.length()-1) != '-')
         m_rxTag.setText(tag + "-");
   }
   
   /** 
    * The edit field for editing the tag used to markup dynamic HTML,
    * never <code>null</code>.
    */
   private JTextField m_psxTag = new JTextField("");
   
   /** 
    * The edit field for editing the tag used to markup assembler properties,
    * never <code>null</code>.
    */
   private JTextField m_rxTag = new JTextField("");

   /** 
    * The combo box to select the input file character encoding,
    * never <code>null</code>.
    */
   static private JComboBox m_inputEncoding = new JComboBox();
   static
   {
      m_inputEncoding.addItem(PSCharSets.DEFAULT_INPUT_ENCODING);
      m_inputEncoding.addItem(PSCharSets.getInternalEncoding());
   }

   /** 
    * The combo box to select the output file character encoding, 
    * never <code>null</code>.
    */
   static private JComboBox m_outputEncoding = new JComboBox();
   static
   {
      m_outputEncoding.addItem(PSCharSets.DEFAULT_OUTPUT_ENCODING);
      m_outputEncoding.addItem(PSCharSets.getInternalEncoding());
   }

   /** 
    * The combo box to select the output encoding instruction to produce in the
    * stylesheets generated.
    */
   static private JComboBox m_xslOutputEncoding = new JComboBox(
      SplitterConfiguration.XSL_OUTPUT_ENCODINGS);

   /** 
    * The combo box to edit or select the base location for releative URL's,
    * never <code>null</code>.
    */
   private JTextField m_baseLocation = new JTextField("");
   
   /**
    * The checkbox indicating if the splitter should show warnings and errors
    * (checked) or errors only (unchecked), never <code>null</code>.
    */
   private JCheckBox m_showWarnings = new JCheckBox(
      MainFrame.getRes().getString("showWarnings"));

   /**
    * The exit action chossen by the user to close this dialog.
    */
   private int m_exit = CANCEL;

   /** The exit action if the Cancel button was hit */
   public final static int CANCEL = 1;

   /** The exit action if the OK button was hit */
   public final static int OK = 2;
   
   /**
    * The splitter configuration to use, set in initialize call, reset onOk.
    */
   private SplitterConfiguration m_config = null;
}
