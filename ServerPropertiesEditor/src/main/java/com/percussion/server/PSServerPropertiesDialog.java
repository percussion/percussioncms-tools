/******************************************************************************
 *
 * [ PSServerPropertiesDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2011 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.server;

import com.percussion.guitools.PSPropertyPanel;
import com.percussion.tools.help.PSJavaHelp;
import com.percussion.util.PSProperties;
import com.percussion.utils.container.IPSConnector;
import com.percussion.utils.container.IPSContainerUtils;
import com.percussion.utils.container.PSAbstractConnector;
import com.percussion.utils.container.PSCiphers;
import com.percussion.utils.container.PSContainerUtilsFactory;
import com.percussion.utils.container.PSPortConfiguration;
import com.percussion.utils.container.PSPortInfo;
import com.percussion.utils.io.PathUtils;
import com.percussion.utils.xml.PSInvalidXmlException;
import org.apache.commons.lang.StringUtils;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static com.percussion.utils.container.PSAbstractConnector.SCHEME_HTTPS;

/**
 * The dialog to edit the PercussionCMS Server Properties.
 */
@SuppressWarnings(value =
{"unchecked"})
public class PSServerPropertiesDialog extends JFrame
{
    /**
    * 
    */
   private static final long serialVersionUID = 1L;
   public static final Path BASE = PathUtils.getRxPath().resolve(Paths.get("jetty", "base")).toAbsolutePath();

    /**
    * Creates a new dialog object with the supplied set of properties.
    *
    * @param properties the server properties, may not be <code>null</code>
    * @param connectors The list of tomcat connectors, may not be
    *           <code>null</code> or empty.
    * @param portConfig The port configuration to use, may be <code>null</code>.
    * @param isAdvanced <code>false</code> to display only known "recognized"
    *           properties, <code>true</code> to also display "unrecognized"
    *           properties.
    */
   public PSServerPropertiesDialog(PSProperties properties, List<IPSConnector> connectors,
         PSPortConfiguration portConfig, boolean isAdvanced)
   {
      if (properties == null)
         throw new IllegalArgumentException("properties may not be null.");

      if (connectors == null || connectors.isEmpty())
         throw new IllegalArgumentException("connectors may not be null or empty");
      
      m_serverProps = properties;
      m_isAdvanced = isAdvanced;

      for (IPSConnector connector : connectors)
      {
         if (connector.getScheme().equals(PSAbstractConnector.SCHEME_HTTPS))
         {
            if (m_httpsConnector == null)
               m_httpsConnector = connector;
            else
               m_legacyConnectors.add(connector);

         }
         else
         {
            if (m_httpConnector == null)
               m_httpConnector = connector;
            else
               m_legacyConnectors.add(connector);
         }
      }

      if (m_httpConnector == null)
         m_httpConnector = PSAbstractConnector.getBuilder().setPort(9992).build();

       m_portConfig = portConfig;

      initDialog();
      initData();
      setTitle(PSServerPropertiesStatic.getString("title"));
      setDefaultCloseOperation(EXIT_ON_CLOSE);
      URL imageFile = this.getClass().getResource(PSServerPropertiesStatic.getString("gif_main"));
      if (imageFile != null)
      {
         ImageIcon icon = new ImageIcon(imageFile);
         setIconImage(icon.getImage());
      }
   }

   /**
    * Initializes the dialog with all controls and makes the dialog centered to
    * the screen. The dialog is resizable.
    */
   private void initDialog()
   {
      JPanel panel = new JPanel();
      panel.setLayout(new BorderLayout(20, 10));
      panel.setBorder((new EmptyBorder(5, 5, 5, 5)));
      getContentPane().add(panel);

      panel.add(createPropertiesPanel(), BorderLayout.CENTER);
      panel.add(createCommandPanel(), BorderLayout.SOUTH);

      pack();
      center();
      setResizable(true);

      // Set the help set
      String helpSetURL = PSJavaHelp.getHelpSetURL(PSServerPropertiesStatic.HELPSET_FILE);
      m_help = PSJavaHelp.getInstance();
      m_help.setHelpSet(helpSetURL, "com.percussion.server.helptopicmapping");
   }

   /**
    * Creates the panel for editing server properties. Each group of properties
    * are put in different panels and all panels are laid out vertically.
    *
    * @return the panel, never <code>null</code>
    */
   private JComponent createPropertiesPanel()
   {
      JPanel propertiesPane = new JPanel();
      propertiesPane.setLayout(new BoxLayout(propertiesPane, BoxLayout.Y_AXIS));

      propertiesPane.add(createGeneralPropertiesPanel());
      propertiesPane.add(Box.createVerticalStrut(10));
      propertiesPane.add(Box.createVerticalGlue());
      propertiesPane.add(createLoggingPropertiesPanel());
      propertiesPane.add(Box.createVerticalStrut(10));
      propertiesPane.add(Box.createVerticalGlue());
      propertiesPane.add(createApplicationPropertiesPanel());
      propertiesPane.add(Box.createVerticalStrut(10));
      propertiesPane.add(Box.createVerticalGlue());
      propertiesPane.add(createSSLPropertiesPanel());
      propertiesPane.add(Box.createVerticalStrut(10));
      propertiesPane.add(Box.createVerticalGlue());
      if (m_jBossEnabled)
         propertiesPane.add(createAppServerPropertiesPanel());

      JScrollPane pane = new JScrollPane(propertiesPane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      pane.setPreferredSize(new Dimension(500, 500));
      return pane;
   }

   /**
    * These maps are used for the group rendering type combo box to convert
    * between labels and values.
    */
   private Map<String, String> m_groupTypeLabelToValue = new HashMap<String, String>();

   private Map<String, String> m_groupTypeValueToLabel = new HashMap<String, String>();
   {
      m_groupTypeLabelToValue.put(PSServerPropertiesStatic.getString("groupRenderCollapsibleLabel"),
            PSServerPropertiesStatic.GROUP_RENDERING_TYPE_VALUE_COLLAPSIBLE);
      m_groupTypeLabelToValue.put(PSServerPropertiesStatic.getString("groupRenderTabsLeftLabel"),
            PSServerPropertiesStatic.GROUP_RENDERING_TYPE_VALUE_TABS_LEFT);

      for (String s : m_groupTypeLabelToValue.keySet())
         m_groupTypeValueToLabel.put(m_groupTypeLabelToValue.get(s), s);
   }

   /**
    * Creates a panel to edit general properties of server. All labels are
    * aligned to their right and all controls are aligned to their left. All
    * properties that are not recognized by this dialog are added at the end of
    * this panel and uses property name as label and <code>JTextField</code> as
    * control for editing its value.
    *
    * @return the panel, never <code>null</code>
    */
   private JPanel createGeneralPropertiesPanel()
   {
      PSPropertyPanel panel = new PSPropertyPanel();

      m_bindPort = new JTextField();
      panel.addPropertyRow(PSServerPropertiesStatic.getString("bindPort"), new JComponent[]
      {m_bindPort});

      m_responseCloseDelay = new JTextField();
      panel.addPropertyRow(PSServerPropertiesStatic.getString("responseCloseDelay"), new JComponent[]
      {m_responseCloseDelay});

      m_allowXslEncodingMods = new JCheckBox();
      panel.addPropertyRow(PSServerPropertiesStatic.getString("allowXslEncodingMods"), new JComponent[]
      {m_allowXslEncodingMods});

      List<String> labels = new ArrayList<String>();
      labels.addAll(m_groupTypeLabelToValue.keySet());
      Collections.sort(labels);
      m_contentEditorGroupRenderingType = new JComboBox(labels.toArray());
      m_contentEditorGroupRenderingType.setEditable(false);
      panel.addPropertyRow(PSServerPropertiesStatic.getString("groupRenderType"), new JComponent[]
      {m_contentEditorGroupRenderingType});

      // Add any properties which we don't know if processing advanced
      if (m_isAdvanced)
      {
         Iterator props = m_serverProps.keySet().iterator();
         while (props.hasNext())
         {
            String property = (String) props.next();
            if (!PSServerPropertiesStatic.ms_recProperties.contains(property))
            {
               JTextField field = new JTextField();
               m_unrecPropertyMap.put(property, field);
               panel.addPropertyRow(property, new JComponent[]
               {field});
            }
         }
      }

      panel.setBorder(createGroupBorder(PSServerPropertiesStatic.getString("generalProperties")));

      return panel;
   }

   /**
    * Creates a panel to edit logging properties of server. All labels are
    * aligned to their right and all controls are aligned to their left.
    *
    * @return the panel, never <code>null</code>
    */
   private JPanel createLoggingPropertiesPanel()
   {
      PSPropertyPanel panel = new PSPropertyPanel();

      m_logType = new JComboBox(new Object[]
      {PSServerPropertiesStatic.LOG_DBMS, PSServerPropertiesStatic.LOG_FILE});
      m_logType.setEditable(false);
      panel.addPropertyRow(PSServerPropertiesStatic.getString("logType"), new JComponent[]
      {m_logType});

      m_logUrl = new JTextField();
      panel.addPropertyRow(PSServerPropertiesStatic.getString("logUrl"), new JComponent[]
      {m_logUrl});

      panel.setBorder(createGroupBorder(PSServerPropertiesStatic.getString("loggingProperties")));

      return panel;
   }

   /**
    * Creates a panel to edit SSL properties of server. All labels are aligned
    * to their right and all controls are aligned to their left.
    *
    * @return the panel, never <code>null</code>
    */
   private JPanel createSSLPropertiesPanel()
   {
      PSPropertyPanel panel = new PSPropertyPanel();

      m_sslKeyFile = new JTextField();
      panel.addPropertyRow(PSServerPropertiesStatic.getString("sslKeyFile"), new JComponent[]
      {m_sslKeyFile});

      m_sslKeyPasswd = new JPasswordField();
      panel.addPropertyRow(PSServerPropertiesStatic.getString("sslKeyPasswd"), new JComponent[]
      {m_sslKeyPasswd});

      m_sslKeyPasswdConfirm = new JPasswordField();
      panel.addPropertyRow(PSServerPropertiesStatic.getString("sslKeyPasswdConf"), new JComponent[]
      {m_sslKeyPasswdConfirm});

      m_sslCipherList = new JList(PSCiphers.getIntermediateCiphers().toArray());
      panel.addPropertyRow(PSServerPropertiesStatic.getString("sslCipherList"), new JComponent[]
      {m_sslCipherList});

      m_sslPort = new JTextField();
      panel.addPropertyRow(PSServerPropertiesStatic.getString("sslPort"), new JComponent[]
      {m_sslPort});

      panel.setBorder(createGroupBorder(PSServerPropertiesStatic.getString("sslProperties")));

      return panel;
   }

   /**
    * Creates a panel to edit application properties of server.
    *
    * @return the panel, never <code>null</code>
    */
   private JPanel createApplicationPropertiesPanel()
   {
      PSPropertyPanel panel = new PSPropertyPanel();

      m_objStorePropFile = new JTextField();
      panel.addPropertyRow(PSServerPropertiesStatic.getString("objStorePropFile"), new JComponent[]
      {m_objStorePropFile});

      m_csUrl = new JCheckBox();

      panel.addPropertyRow(PSServerPropertiesStatic.getString("csUrl"), new JComponent[]
      {m_csUrl});

      panel.setBorder(createGroupBorder(PSServerPropertiesStatic.getString("applicationProperties")));

      return panel;
   }

   /**
    * Create a panel to edit app server properties
    * 
    * @return The panel, never <code>null</code>.
    */
   private JPanel createAppServerPropertiesPanel()
   {
      PSPropertyPanel panel = new PSPropertyPanel();
      
      for (PSPortInfo portInfo : m_portConfig.getPortConfigurations())
      {
         JTextField field = new JTextField();
         m_portPropertyMap.put(portInfo.getName(), field);
         panel.addPropertyRow(portInfo.getLabel(), field);
      }

      panel.setBorder(createGroupBorder(PSServerPropertiesStatic.getString("appServerProperties")));
      
      return panel;
   }

   /**
    * Centers the dialog on the screen, based on its current size.
    */
   private void center()
   {
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      Dimension size = getSize();
      setLocation((screenSize.width - size.width) / 2, (screenSize.height - size.height) / 2);
   }

   /**
    * Creates a titled border which can be used to group a set of properties.
    *
    * @param title the title to use for the border, if <code>null</code> border
    *           without title will be created.
    *
    * @return the border, never <code>null</code>
    */
   private Border createGroupBorder(String title)
   {
      if (null == title)
         title = "";
      Border tb = BorderFactory.createTitledBorder(new EtchedBorder(EtchedBorder.LOWERED), title);
      // leave more space between the titled border and the components
      Border eb = BorderFactory.createEmptyBorder(0, 5, 5, 5);
      return BorderFactory.createCompoundBorder(tb, eb);
   }

   /**
    * Creates a panel with 'OK', 'Cancel' buttons in vertical direction.
    *
    * @return the panel, never <code>null</code>
    */
   private JPanel createCommandPanel()
   {
      JPanel panel = new JPanel();
      panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

      JButton okButton = new JButton(PSServerPropertiesStatic.getString("OK"));
      okButton.setMnemonic(PSServerPropertiesStatic.getString("OK.mn").charAt(0));
      okButton.setPreferredSize(PSServerPropertiesStatic.STANDARD_BUTTON_SIZE);
      okButton.setMaximumSize(PSServerPropertiesStatic.STANDARD_BUTTON_SIZE);
      okButton.setMinimumSize(PSServerPropertiesStatic.STANDARD_BUTTON_SIZE);

      okButton.addActionListener(new ActionListener()
      {
         public void actionPerformed(@SuppressWarnings("unused") ActionEvent e)
         {
            onOk();
         }
      });

      JButton cancelButton = new JButton(PSServerPropertiesStatic.getString("Cancel"));
      cancelButton.setMnemonic(PSServerPropertiesStatic.getString("Cancel.mn").charAt(0));
      cancelButton.setPreferredSize(PSServerPropertiesStatic.STANDARD_BUTTON_SIZE);
      cancelButton.setMaximumSize(PSServerPropertiesStatic.STANDARD_BUTTON_SIZE);
      cancelButton.setMinimumSize(PSServerPropertiesStatic.STANDARD_BUTTON_SIZE);

      cancelButton.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            onCancel();
         }
      });

      JButton helpButton = new JButton(PSServerPropertiesStatic.getString("Help"));
      helpButton.setMnemonic(PSServerPropertiesStatic.getString("Help.mn").charAt(0));
      helpButton.setPreferredSize(PSServerPropertiesStatic.STANDARD_BUTTON_SIZE);
      helpButton.setMaximumSize(PSServerPropertiesStatic.STANDARD_BUTTON_SIZE);
      helpButton.setMinimumSize(PSServerPropertiesStatic.STANDARD_BUTTON_SIZE);

      helpButton.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            onHelp();
         }
      });

      getRootPane().setDefaultButton(okButton);

      panel.add(Box.createHorizontalGlue());
      panel.add(okButton);
      panel.add(Box.createHorizontalStrut(10));
      panel.add(cancelButton);
      panel.add(Box.createHorizontalStrut(10));
      panel.add(helpButton);

      return panel;
   }

   /**
    * The action performed by the Help button. If the child dialog does not have
    * help, override <CODE>onHelp</CODE> and do nothing. Launches JavaHelp
    * viewer to display the help.
    */
   private void onHelp()
   {
      String fullname = getClass().getName();
      String helpId = fullname.substring(fullname.lastIndexOf(".") + 1);
      PSJavaHelp.launchHelp(helpId, false, this);
   }

   /**
    * Initializes all controls with their respective values from server
    * properties. The passwords are decrypted and set if they are provided.
    */
   private void initData()
   {
      m_bindPort.setText(String.valueOf(m_httpConnector.getPort()));
      m_responseCloseDelay.setText(m_serverProps.getProperty(PSServerPropertiesStatic.RESPONSE_CLOSE_DELAY, "10"));

      String allowXslEncodingMods = m_serverProps.getProperty(PSServer.PROP_ALLOW_XSL_ENCODING_MODS);

      m_allowXslEncodingMods.setSelected("true".equalsIgnoreCase(allowXslEncodingMods)
            || "yes".equalsIgnoreCase(allowXslEncodingMods));

      String renderingType = m_serverProps.getProperty(
            PSServerPropertiesStatic.CONTENT_EDITOR_GROUP_RENDERING_TYPE_PROP_NAME,
            PSServerPropertiesStatic.GROUP_RENDERING_TYPE_VALUE_COLLAPSIBLE);
      m_contentEditorGroupRenderingType.setSelectedItem(m_groupTypeValueToLabel.get(renderingType));

      m_objStorePropFile.setText(m_serverProps.getProperty(PSServerPropertiesStatic.OBJSTORE_FILE));
      String caseSensitivieURL = m_serverProps.getProperty(PSServerPropertiesStatic.CASE_SENSITIVE_URL);
      if (caseSensitivieURL != null && caseSensitivieURL.equalsIgnoreCase("true"))
      {
         m_csUrl.setSelected(true);
      }

      m_logType.setSelectedItem(m_serverProps.getProperty(PSServerPropertiesStatic.LOG_TYPE,
            PSServerPropertiesStatic.LOG_DBMS));
      m_logUrl.setText(m_serverProps.getProperty(PSServerPropertiesStatic.LOG_URL, ""));

      if (m_httpsConnector != null)
      {
         String keyFile = getRelPath(m_httpsConnector.getKeystoreFile()).toString();

         m_sslKeyFile.setText(keyFile);
         m_sslKeyPasswd.setText(m_httpsConnector.getKeystorePass());
         m_sslKeyPasswdConfirm.setText(m_httpsConnector.getKeystorePass());

         Set<String> ciphers = m_httpsConnector.getCiphers();
         List<Integer> indexList = new ArrayList<Integer>();
         List<Integer> defaultIndexList = new ArrayList<Integer>();
         ciphers.retainAll(PSCiphers.getIntermediateCiphers());
         List<String> defaultCiphers = PSCiphers.getModernCiphers();
         
         for (int i = 0; i < PSCiphers.getIntermediateCiphers().size(); i++)
         {
            String test = PSCiphers.getIntermediateCiphers().get(i);
            if (ciphers.contains(test))
               indexList.add(i);
            if (defaultCiphers.contains(test))
               defaultIndexList.add(i);
         }
         
         if (indexList.size()==0)
         {
            indexList = defaultIndexList;
         }
         
         int[] indeces = new int[indexList.size()];
         for (int i = 0; i < indeces.length; i++)
         {
            indeces[i] = indexList.get(i);
         }
         m_sslCipherList.setSelectedIndices(indeces);

         m_sslPort.setText(String.valueOf(m_httpsConnector.getPort()));
      }
      else
         m_sslPort.setText("9443");

      // set unrec properties data
      Iterator unrecProps = m_unrecPropertyMap.entrySet().iterator();
      while (unrecProps.hasNext())
      {
         Map.Entry entry = (Map.Entry) unrecProps.next();
         JTextField field = (JTextField) entry.getValue();
         field.setText(m_serverProps.getProperty((String) entry.getKey(), ""));
      }

      // set port data
      if (m_jBossEnabled)
      {
         for (PSPortInfo portInfo : m_portConfig.getPortConfigurations())
         {
            m_portPropertyMap.get(portInfo.getName()).setText(String.valueOf(portInfo.getValue()));
         }
      }
   }

   /**
    * Validates the properties, displays error message to the user if there is
    * invalid value and switches the focus to the component with invalid value.
    * The tests done for validation are:
    * <ol>
    * <li>Makes sure all number fields have either a number or empty.</li>
    * <li>If the logging type is specified as 'FILE', validates the log url is a
    * well-formed url.</li>
    * <li>If the logging type is specified as 'DBMS', validates the driver name
    * and server name to exist.</li>
    * <li>Validates that ObjectStore Properties File is specifed.</li>
    * <li>Validates that both SSL Key file and password are specifed to support
    * SSL enabling.</li>
    * </ol>
    * Displays a confirmation message to the user if any of other ssl properties
    * are specified without specifying Key file to set that for enabling ssl
    * support.
    *
    * @return <code>true</code> if the validation is successful, otherwise
    *         <code>false</code>
    */
   private boolean validateProperties()
   {
      JTextField[] numberFields = new JTextField[3];
      numberFields[0] = m_bindPort;
      numberFields[1] = m_sslPort;
      numberFields[2] = m_responseCloseDelay;

      if (!validateNumbers(numberFields, true))
         return false;
      
      if (m_jBossEnabled)
      {
         numberFields = new JTextField[m_portPropertyMap.size()];
         int index = 0;
         for (JTextField field : m_portPropertyMap.values())
         {
            numberFields[index] = field;
            index++;
         }
     

         if (!validateNumbers(numberFields, false))
            return false;
      }
      // validate logging properties
      String logType = (String) m_logType.getSelectedItem();
      String msg = PSServerPropertiesStatic.getString("missingLogField");
      if (logType.equals(PSServerPropertiesStatic.LOG_FILE))
      {
         String logUrl = m_logUrl.getText();
         if (logUrl == null || logUrl.trim().length() == 0)
         {
            msg = MessageFormat.format(msg, new Object[]
            {PSServerPropertiesStatic.getString("logUrl"), PSServerPropertiesStatic.LOG_FILE});
            return invalidComponentValue(msg, m_logUrl);
         }

         try
         {
            new URL(logUrl);
         }
         catch (MalformedURLException e)
         {
            return invalidComponentValue(PSServerPropertiesStatic.getString("invalidLogUrl"), m_logUrl);
         }
      }

      String objStoreFile = m_objStorePropFile.getText();
      if (objStoreFile == null || objStoreFile.trim().length() == 0)
      {
         msg = MessageFormat.format(PSServerPropertiesStatic.getString("missingReqField"), new Object[]
         {PSServerPropertiesStatic.getString("objStorePropFile")});

         return invalidComponentValue(msg, m_objStorePropFile);
      }

      String sslKeyFile = m_sslKeyFile.getText();
      char[] sslKeypw = m_sslKeyPasswd.getPassword();
      char[] sslKeypwConfirm = m_sslKeyPasswdConfirm.getPassword();
      Object[] sslCiphers = m_sslCipherList.getSelectedValues();
      String sslPort = m_sslPort.getText();
      if (sslKeyFile == null || sslKeyFile.trim().length() == 0)
      {
         if ((sslKeypw != null && sslKeypw.length != 0) || (sslCiphers != null && sslCiphers.length != 0)
               || (sslPort != null && sslPort.trim().length() != 0))
         {
            msg = MessageFormat.format(PSServerPropertiesStatic.getString("ignoreSSLProps"), new Object[]
            {PSServerPropertiesStatic.getString("sslKeyFile")});

            int opt = JOptionPane.showConfirmDialog(null, msg, PSServerPropertiesStatic.getString("warning"),
                  JOptionPane.YES_NO_OPTION);
            if (opt == JOptionPane.YES_OPTION)
            {
               m_sslKeyFile.requestFocus();
               return false;
            }
         }
      }
      else
      {
         if (sslKeypw == null || sslKeypw.length == 0)
         {
            msg = MessageFormat.format(PSServerPropertiesStatic.getString("missingSSLField"), new Object[]
            {PSServerPropertiesStatic.getString("sslKeyPasswd")});
            return invalidComponentValue(msg, m_sslKeyPasswd);
         }
         else if (!Arrays.equals(sslKeypw, sslKeypwConfirm))
         {
            msg = PSServerPropertiesStatic.getString("sslKeyPasswdConfError");
            return invalidComponentValue(msg, m_sslKeyPasswd);
         }
      }

      return true;
   }

   /**
    * Validates and saves the properties to server properties file. Closes the
    * dialog if successful.
    */
   private void onOk()
   {
      if (save())
         exit();
   }

   /**
    * Saves the port configurations. Displays an error message for any
    * exceptions encountered.
    * 
    * @return <code>true</code> if save is successful, otherwise
    *         <code>false</code>
    */
   private boolean savePortConfig(IPSContainerUtils jettyUtils, IPSContainerUtils jbossUtils)
   {
      // No more portConfig for Jetty
      return true;
   }

   /**
    * Saves the properties to the server properties file(<code>
    * rxconfig/Server/server.properties</code>). Displays an error message if it
    * is unable to write to the file or an exception while writing.
    *
    * @return <code>true</code> if save is successful, otherwise
    *         <code>false</code>
    */
   private boolean saveProperties()
   {
      try
      {
         if (!persistPropFile(m_serverProps))
         {
            JOptionPane.showMessageDialog(null, PSServerPropertiesStatic.getString("unableToWrite"),
                  PSServerPropertiesStatic.getString("error"), JOptionPane.ERROR_MESSAGE);
            return false;
         }
      }
      catch (IOException e)
      {
         JOptionPane.showMessageDialog(null, e.getLocalizedMessage(), PSServerPropertiesStatic.getString("error"),
               JOptionPane.ERROR_MESSAGE);
         return false;
      }
      return true;
   }

   /**
    * Saves the supplied properties to the server properties file(<code>
    * rxconfig/Server/server.properties</code>)
    * 
    * @param props The properties to save, assumed not <code>null</code>.
    * 
    * @return <code>true</code> if the file could be located and written to,
    *         <code>false</code> if not.
    * 
    * @throws IOException If there is an error while writing the file.
    */
   private boolean persistPropFile(PSProperties props) throws IOException
   {
      boolean success = true;
      File file = null;

      file = PSProperties.getConfig(PSServer.ENTRY_NAME, PSServer.PROPS_SERVER, PSServer.getRxConfigDir());
      if (file != null && file.canWrite())
      {
         props.store(new FileOutputStream(file.getAbsolutePath()), null);
      }
      else
      {
         success = false;
      }

      return success;
   }

   /**
    * Saves the properties.
    * 
    * @return <code>true</code> if sucessful, <code>false</code> otherwise.
    */
   private boolean save()
   {
      if (!validateProperties())
         return false;

      setData();

      List<IPSConnector> connectors = new ArrayList<IPSConnector>();

      connectors.add(m_httpConnector);

      if (m_httpsConnector != null)
         connectors.add(m_httpsConnector);

      //connectors.addAll(m_legacyConnectors);
      // jboss
      // PSContainerUtilsFactory.getInstance().saveConnectors(connectors);

      IPSContainerUtils jettyUtils = PSContainerUtilsFactory.getInstance();
      jettyUtils.getConnectorInfo().setConnectors(connectors);
      boolean success=false;
      try {
          PSContainerUtilsFactory.getConfigurationContextInstance().save();
          success=true;
      } catch (Exception e)
      {
         e.printStackTrace();
      }

      return success;
   }

   /**
    * Gets data from controls and sets the server and connector properties.
    */
   private void setData()
   {
      String port = m_bindPort.getText();
      if (!StringUtils.isBlank(port))
      {
         m_httpConnector.setPort(Integer.parseInt(port));
      }
      m_serverProps.setProperty(PSServerPropertiesStatic.RESPONSE_CLOSE_DELAY, m_responseCloseDelay.getText());
      m_serverProps.setProperty(PSServer.PROP_ALLOW_XSL_ENCODING_MODS, m_allowXslEncodingMods.isSelected()
            ? "true"
            : "false");
      m_serverProps.setProperty(PSServerPropertiesStatic.CONTENT_EDITOR_GROUP_RENDERING_TYPE_PROP_NAME,
            m_groupTypeLabelToValue.get(m_contentEditorGroupRenderingType.getSelectedItem()));
      m_serverProps.setProperty(PSServerPropertiesStatic.LOG_TYPE, (String) m_logType.getSelectedItem());
      m_serverProps.setProperty(PSServerPropertiesStatic.LOG_URL, m_logUrl.getText());
      m_serverProps.setProperty(PSServerPropertiesStatic.OBJSTORE_FILE, m_objStorePropFile.getText());
      if (m_csUrl.isSelected())
         m_serverProps.setProperty(PSServerPropertiesStatic.CASE_SENSITIVE_URL, "true");
      else
         m_serverProps.setProperty(PSServerPropertiesStatic.CASE_SENSITIVE_URL, "false");

      String sslKeyFile = m_sslKeyFile.getText();
      if (!StringUtils.isBlank(sslKeyFile))
      {
         String strSSLPort = m_sslPort.getText();
         int sslPort = StringUtils.isBlank(strSSLPort) ? 9443 : Integer.parseInt(strSSLPort);
         if (m_httpsConnector == null)
         {
            m_httpsConnector = PSAbstractConnector.getBuilder().setPort(sslPort).setHttps().setKeystoreFile(Paths.get(sslKeyFile)).setKeystorePass(String.valueOf(m_sslKeyPasswd.getPassword())).build();

            m_httpsConnector.setScheme(PSAbstractConnector.SCHEME_HTTPS);
         }
         else
         {

            m_httpsConnector.setPort(sslPort);

            m_httpsConnector.setKeystoreFile(getAbsPath(sslKeyFile));
            m_httpsConnector.setKeystorePass(String.valueOf(m_sslKeyPasswd.getPassword()));

         }

          Object[] cipherList = m_sslCipherList.getSelectedValues();
          Set<String> ciphers = new HashSet<String>(cipherList.length);
          for (int i = 0; i < cipherList.length; i++)
              ciphers.add((String) cipherList[i]);

          m_httpsConnector.setCiphers(ciphers);
      }
      else
      {
         m_httpsConnector = null;
      }

      Iterator unrecProps = m_unrecPropertyMap.entrySet().iterator();
      while (unrecProps.hasNext())
      {
         Map.Entry entry = (Map.Entry) unrecProps.next();
         m_serverProps.setProperty((String) entry.getKey(), ((JTextField) entry.getValue()).getText());
      }
   }

    private Path getAbsPath(String sslKeyFile) {
        return PathUtils.getRxPath().resolve(Paths.get("jetty", "base")).resolve(sslKeyFile);
    }

    private String getRelPath(Path sslKeyFile) {
        return BASE.relativize(BASE.resolve(sslKeyFile).toAbsolutePath()).toString();
    }

    /**
    * Utility method to display an error message to the user and switch focus to
    * the component with invalid value.
    *
    * @param msg the message to display, assumed not to be <code>null</code> and
    *           empty.
    * @param component the component that has invalid value, assumed not to be
    *           <code>null</code>
    *
    * @return always <code>false</code>
    */
   private boolean invalidComponentValue(String msg, JComponent component)
   {
      JOptionPane.showMessageDialog(null, msg, PSServerPropertiesStatic.getString("error"), JOptionPane.ERROR_MESSAGE);
      component.requestFocus();
      return false;
   }

   /**
    * Validates the value in the supplied array of components as
    * number(Integer).
    * 
    *
    * @param components the array of components to test, assumed not to be
    *           <code>null</code> or empty.
    * @param allowEmpty <code>true</code> to ignore empty values,
    *           <code>false</code> to consider it an error.
    *
    * @return <code>false</code> if any component in the array has invalid
    *         value, otherwise <code>true</code>
    */
   private boolean validateNumbers(JTextField[] components, boolean allowEmpty)
   {
      for (int i = 0; i < components.length; i++)
      {
         JTextField component = components[i];
         try
         {
            String text = component.getText();
            if (!(StringUtils.isEmpty(text) && allowEmpty))
               Integer.parseInt(component.getText());
         }
         catch (NumberFormatException e)
         {
            return invalidComponentValue(PSServerPropertiesStatic.getString("invalidNumber"), component);
         }
      }
      return true;
   }

   /**
    * Disposes the dialog without saving.
    */
   private void onCancel()
   {
      exit();
   }

   /**
    * Hides the window, disposes the frame, and exits the applicaiton
    */
   private void exit()
   {
      setVisible(false);
      dispose();
      System.exit(0);
   }

   /**
    * Loads the properties from the server properties file
    * {@link PSServer#PROPS_SERVER } in {@link PSServer#SERVER_DIR } directory and
    * displays the dialog to edit the properties. Logs the error messages to the
    * console if it is unable to find the file or read properties from the file.
    *
    * @param args the arguments, these are ignored.
    */
   public static void main(String[] args)
   {
      // first try to load server.properties
      File propFile = PSProperties.getConfig(PSServer.ENTRY_NAME, PSServer.PROPS_SERVER, PSServer.getRxConfigDir());
      if (propFile == null)
      {
         System.out.println("Unable to load server properties file");
         return;
      }
      
      // now check args
      boolean showHelp = false;
      boolean badArg = false;
      boolean advanced = false;
      Properties argProps = PSServerPropertiesStatic.processArgs(args);

      Iterator entries = argProps.entrySet().iterator();
      while (entries.hasNext())
      {
         Map.Entry entry = (Map.Entry) entries.next();
         String key = (String) entry.getKey();
         String value = (String) entry.getValue();

         // we're not expecting any values
         if (value.trim().length() > 0)
            badArg = true;
         else if (key.equalsIgnoreCase("help") || key.equalsIgnoreCase("h"))
            showHelp = true;
         else if (key.equalsIgnoreCase("advanced"))
            advanced = true;
         else
         {
            // unknown arg
            badArg = true;
         }
      }
      
      // if help or bad argument, print usage and exit with appropriate code
      if (showHelp || badArg)
      {
         PSServerPropertiesStatic.showCmdLineUsage();
         System.exit(badArg ? 1 : 0);
      }
      try
      {

         // first try to load server.properties
         propFile = PSProperties.getConfig(PSServer.ENTRY_NAME, PSServer.PROPS_SERVER, PSServer.getRxConfigDir());

         PSProperties properties = new PSProperties(propFile.getPath());

          populateJettyDialog(properties, advanced);
          return;

      }catch (Throwable t)
      {
         System.out.println("Unexpected exception in main:" + t.getLocalizedMessage());
         t.printStackTrace();
         System.exit(1);
      }

   }


   private static void populateJettyDialog(PSProperties properties, boolean advanced) throws PSInvalidXmlException,
         IOException, SAXException, InstantiationException, IllegalAccessException, ClassNotFoundException,
         UnsupportedLookAndFeelException
   {
      IPSContainerUtils utils = PSContainerUtilsFactory.getInstance();
      List<IPSConnector> connectors = utils.getConnectorInfo().getConnectors();

      // PSPortConfiguration portConfig = utils.getPortConfig();

      // Set our default look and feel
      String strLnFClass = UIManager.getSystemLookAndFeelClassName();
      LookAndFeel lnf = (LookAndFeel) Class.forName(strLnFClass).newInstance();
      UIManager.setLookAndFeel(lnf);

      PSServerPropertiesDialog serverPropsDlg = new PSServerPropertiesDialog(properties, connectors, null,
            advanced);

      serverPropsDlg.setVisible(true);
   }

   /**
    * The field to enter the port number to bind, initialized in <code>
    * createGeneralPropertiesPanel()</code> and never <code>null</code> after
    * that.
    */
   private JTextField m_bindPort;

   /**
    * The field to enter the value of the responseCloseDelay property,
    * initialized in <code>createGeneralPropertiesPanel()</code> and never
    * <code>null</code> after that.
    */
   private JTextField m_responseCloseDelay;

   /**
    * The field to enter the value of the allowXslEncodingMods property,
    * initialized in <code>createGeneralPropertiesPanel()</code> and never
    * <code>null</code> after that.
    */
   private JCheckBox m_allowXslEncodingMods;

   /**
    * The drop list to choose how groups are rendered in a content editor UI.
    * Initialized in {@link #createGeneralPropertiesPanel()} and never
    * <code>null</code> after that.
    */
   private JComboBox m_contentEditorGroupRenderingType;

   /**
    * The drop-list to show where the server has to log to, initialized in
    * <code>createLoggingPropertiesPanel()</code> and never <code>null</code>
    * after that.
    */
   private JComboBox m_logType;

   /**
    * The field to enter the url for FILE logging, initialized in <code>
    * createLoggingPropertiesPanel()</code> and never <code>null</code> after
    * that.
    */
   private JTextField m_logUrl;

   /**
    * The field to enter the objectstore properties file location, initialized
    * in <code>createApplicationPropertiesPanel()</code> and never <code>null
    * </code> after that.
    */
   private JTextField m_objStorePropFile;

   /**
    * The field to set the request url as case sensitive/insensitive,
    * initialized in <code>createApplicationPropertiesPanel()</code> and never
    * <code>null</code> after that.
    */
   private JCheckBox m_csUrl;

   /**
    * The field to enter the SSL key store file location, initialized in
    * <code>createSSLPropertiesPanel()</code> and never <code>null</code> after
    * that.
    */
   private JTextField m_sslKeyFile;

   /**
    * The field to enter the SSL key password, initialized in
    * <code>createSSLPropertiesPanel()</code> and never <code>null</code> after
    * that.
    */
   private JPasswordField m_sslKeyPasswd;

   /**
    * The field to confirm the SSL key password, initialized in
    * <code>createSSLPropertiesPanel()</code> and never <code>null</code> after
    * that.
    */
   private JPasswordField m_sslKeyPasswdConfirm;

   /**
    * The list to select the SSL ciphers, initialized in
    * <code>createSSLPropertiesPanel()</code> and never <code>null</code> after
    * that.
    */
   private JList m_sslCipherList;

   /**
    * The field to enter the SSL port, initialized in
    * <code>createSSLPropertiesPanel()</code> and never <code>null</code> after
    * that.
    */
   private JTextField m_sslPort;

   /**
    * The map of server property name as key and the component to edit the
    * property as value. Uses <code>JTextField</code> to edit the value.
    * Initilized in <code>createGeneralPropertiesPanel()</code> and never
    * modified after that.
    */
   private Map m_unrecPropertyMap = new HashMap();

   /**
    * The map of port from the {@link #m_portConfig}, with the name as the key
    * and the editing component as the value.
    */
   private Map<String, JTextField> m_portPropertyMap = new HashMap();

   /**
    * The server properties, initialized in {@link #main} and set with modified
    * values in <code>setProperties()</code>
    */
   private PSProperties m_serverProps;

   /**
    * Indicates if advanced or "unrecognized" properties are to be displayed.
    * Set in the ctor (see that method's javadoc), never modified after that.
    */
   private boolean m_isAdvanced;

   /**
    * We keep a reference to the singleton help object, so we don't try to
    * create another instance. Initialized in {@link #initDialog()}
    */
   private PSJavaHelp m_help;

   /**
    * The tomcat http connector, inialized during construction, never
    * <code>null</code> after that.
    */
   private IPSConnector m_httpConnector;

   /**
    * The tomcat https (SSL) connector, inialized during construction, may be
    * <code>null</code> if one is not defined.
    */
   private IPSConnector m_httpsConnector = null;

   /**
    * Legcay http and https connectors supplied during ctor, never
    * <code>null</code>, may be empty.
    */
   private List<IPSConnector> m_legacyConnectors = new ArrayList<IPSConnector>();

   /**
    * PortCofiguration supplied during construction, never <code>null</code>.
    */
   private PSPortConfiguration m_portConfig = null;

   private static boolean m_jBossEnabled = false;;
   
   // static block
   static
   {
      PSServerPropertiesStatic.staticInit();
   }

}
