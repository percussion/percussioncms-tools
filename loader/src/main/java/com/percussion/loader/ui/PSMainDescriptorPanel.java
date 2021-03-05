/*[ PSMainDescriptorPanel.java ]**********************************************
 * COPYRIGHT (c) 2002 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *****************************************************************************/
package com.percussion.loader.ui;

import com.percussion.UTComponents.UTFixedHeightTextField;
import com.percussion.guitools.ErrorDialogs;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.Border;

import org.w3c.dom.Element;

/**
 * Setup panel to specify the name of the descriptor and its location. It
 * consists of atext field to specify the name and a filechooser to specify the
 * location.
 */
public class PSMainDescriptorPanel extends PSConfigPanel
{
   /**
    * Creates the setup panel
    * 
    * @param isNewDescriptor <code>true</code> if editing a new descriptor, 
    *    then the name of the descriptor can be modified; otherwise, the
    *    name of the descriptor cannot be modified.
    */
   public PSMainDescriptorPanel(boolean isNewDescriptor)
   {
      init(isNewDescriptor);
   }

   /**
    * Initializes the panel.
    * 
    * @param isNewDescriptor <code>true</code> if editing a new descriptor, 
    *    then the name of the descriptor can be modified; otherwise, the
    *    name of the descriptor cannot be modified.
    */
   private void init(boolean isNewDescriptor)
   {
      if (null == ms_res)
         ms_res = ResourceBundle.getBundle(
               getClass().getName() + "Resources", Locale.getDefault() );

      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
      setPreferredSize(new Dimension(200, 450));

      JPanel namePane = new JPanel();
      namePane.setLayout(new BoxLayout(namePane, BoxLayout.X_AXIS));

      JLabel lb = new JLabel();
      lb.setText(PSContentLoaderResources.getResourceString(ms_res,
         "textField.label.name"));
      m_nameText = new UTFixedHeightTextField();
      namePane.add(Box.createRigidArea(new Dimension(100,0)));
      namePane.add(lb);
      namePane.add(Box.createRigidArea(new Dimension(10,0)));
      namePane.add(m_nameText);
      namePane.add(Box.createRigidArea(new Dimension(5,0)));

      m_nameText.setEditable(isNewDescriptor);
      
      String desc = PSContentLoaderResources.getResourceString(ms_res,
         "description");
      JTextArea txtArea = new JTextArea();
      JPanel descPanel =
         PSContentDescriptorDialog.createDescriptionPanel(desc, txtArea);

      JPanel dPanel = new JPanel();
      Border b1 = BorderFactory.createEmptyBorder( 5, 5, 5, 5 );
      Border b3 = BorderFactory.createEtchedBorder();
      Border b2 = BorderFactory.createCompoundBorder(b3, b1);
      Border b = BorderFactory.createTitledBorder(b2,
         PSContentLoaderResources.getResourceString(ms_res, "border.path"));
      dPanel.setBorder(b);
      dPanel.setLayout(new BorderLayout());
      dPanel.add(descPanel, BorderLayout.NORTH);
      if (isNewDescriptor)
         m_brPane = new PSBrowsePanel(this, JFileChooser.DIRECTORIES_ONLY);
      else
         m_brPane = new PSBrowsePanel(this, null, JFileChooser.DIRECTORIES_ONLY,
            null, null, false);
      dPanel.add(m_brPane, BorderLayout.CENTER);
      add(Box.createRigidArea(new Dimension(0, 10)));
      add(namePane);
      add(dPanel);
      JPanel filPane = new JPanel();
      filPane.setPreferredSize(new Dimension(100, 100));
      m_connectionPanel = new PSConnectionEditorPanel();
      add(Box.createRigidArea(new Dimension(0, 10)));
      add(m_connectionPanel);
      add(filPane);
      add(Box.createVerticalGlue());
      setBorder(BorderFactory.createEmptyBorder( 5, 5, 5, 5 ));
      //set default data
      File curDir = new File("");
      String temp = "descriptor" + ms_counter;
      m_brPane.setPath(curDir.getAbsolutePath() + File.separatorChar + DESC_DIR);
      m_nameText.setText(temp);
      ms_counter++;
   }

   /**
    * Get the connetion panel that is created within this object.
    * 
    * @return The connection panel, never <code>null</code>.
    */
   public PSConnectionEditorPanel getConnectionPanel()
   {
      return m_connectionPanel;
   }
   
   /**
    * Loads the data for this panel. Error dialog is shown if there is a problem
    * in loading the data.
    *
    * @param configXml, data for this panel, never <code>null</code>.
    *
    * @throws IllegalArgumentException if the supplied argument is not valid.
    */
   public void load(Element configXml)
   {
      m_connectionPanel.load(configXml);
   }

   /**
    * Implements the {@link IPSConfigPanel} interface.
    */
   public void reset()
   {
      m_connectionPanel.reset();
   }

   /**
    * Implements the {@link IPSConfigPanel} interface.
    */
   public Element save()
   {
      return m_connectionPanel.save();
   }

   /**
    * Checks if the descriptor path and name have been supplied or lese
    * appropriate error dialog is shown.
    *
    * @return <code>true</code>, if the validation succeeds.
    */
   public boolean validateContent()
   {
      //always refresh the connection info here
      String path = getPath();
      if (path == null || path.length() == 0)
      {
         ErrorDialogs.showErrorDialog(this,
         PSContentLoaderResources.getResourceString(
            ms_res, "error.msg.missingpath"),
         PSContentLoaderResources.getResourceString(ms_res,
         "error.title.missingpath"),
         JOptionPane.ERROR_MESSAGE);
         return false;
      }
      String name = getDescriptorName();
      if (name == null || name.length() == 0)
      {
         ErrorDialogs.showErrorDialog(this,
         PSContentLoaderResources.getResourceString(
            ms_res, "error.msg.missingdescname"),
         PSContentLoaderResources.getResourceString(ms_res,
         "error.title.missingdescname"),
         JOptionPane.ERROR_MESSAGE);
         return false;
      }
      int index = name.lastIndexOf(".");
      if (index != -1)
      {
         ErrorDialogs.showErrorDialog(this,
            PSContentLoaderResources.getResourceString(
            ms_res, "error.msg.invaliddescname"),
            PSContentLoaderResources.getResourceString(ms_res,
            "error.title.invaliddescname"),
            JOptionPane.ERROR_MESSAGE);
         return false;
      }
      return m_connectionPanel.validateContent();
   }

   // override PSConfigPanel#getName()
   public String getName()
   {
      return getPath();
   }
   
   /**
    * Returns the absolute path of the descriptor.
    *
    * @return absolute path of the descriptor, never <code>null</code> or empty.
    */
   String getRootName()
   {
      return getPath() + File.separator + getDescriptorName();
   }

   /**
    * Returns the location where the descriptor will be stored.
    *
    * @return location of the descriptor, never <code>null</code> or empty.
    */
   String getPath()
   {
      return m_brPane.getPath();
   }

   /**
    * Returns the name of the descriptor.
    *
    * @return name of the descriptor, never <code>null</code> or empty.
    */
   String getDescriptorName()
   {
      return m_nameText.getText();
   }

   /**
    *  Sets the path where the descriptor will be stored.
    *
    * @param path, assumed to not <code>null</code> or empty.
    */
   public void setPath(String path)
   {
      m_brPane.setPath(path);
   }

   /**
    * Sets the descriptor name. Name should be without extension. Descriptor has
    * the name as the directory in which it's stored.
    *
    * @param name, assumed to not <code>null</code> or empty.
    */
   public void setDescriptorName(String name)
   {
      m_nameText.setText(name);
   }

   /**
    * Resource bundle for this class. Initialized in {@link init()}.
    * It's not modified after that. Never <code>null</code>.
    */
   private static ResourceBundle ms_res;

   /**
    * Counter for assigning default descriptor name. Modified in {@link #init()}
    */
   private static int ms_counter = 1;

   /**
    * trst field storing the name of the descriptor. Initialized in the {@link
    * #init()}, never <code>null</code> or modified after that.
    */
   private UTFixedHeightTextField m_nameText;

   /**
    * @see {@link PSBrowsePanel}. Initialized in the {@link #init()}, never
    * <code>null</code> or modified after that.
    */
   private PSBrowsePanel m_brPane;

   /**
    * Panel specifying the server credentials, {@link PSConnectionEditorPanel}.
    * Initialized in {@init #init()}, never <code>null</code> or modified after
    * that.
    */
   private PSConnectionEditorPanel m_connectionPanel;

   /**
    * The sub directory within the current directory, it's the default location
    * where the descriptors are stored.
    */
   private static final String DESC_DIR = "contentconnector";


}