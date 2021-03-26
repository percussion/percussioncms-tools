/* *****************************************************************************
 *
 * [ DatasetPropertyDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *******************************************************************************/

package com.percussion.E2Designer;

import com.percussion.design.objectstore.PSPipe;
import com.percussion.design.objectstore.PSResourceCacheSettings;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * The dataset property dialog.
 */
public class DatasetPropertyDialog extends PSEditorDialog
{
   /**
   * Construct the default dataset property dialog.
   *
    */
   public DatasetPropertyDialog()
   {
   super();
   initDialog();
   }

   /**
   * Create the name panel
   *
   * @returns   JPanel      the name panel
   */
  private JPanel createNamePanel()
  {
   JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
      
      m_tfName = new JTextField();
      m_tfName.setPreferredSize(new Dimension(300, 22));
      m_tfName.setMaximumSize(new Dimension(400, 22));

    panel.add(new UTMnemonicLabel(getResources(), "name", m_tfName));
      panel.add(m_tfName);
    return panel;
  }

   /**
   * Create type panel
   *
   * @returns the type panel
   */
  private JPanel createTypePanel()
  {
   JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
      
      JLabel type = new JLabel(getResources().getString("type"));
      m_tfType = new JTextField();
      m_tfType.setPreferredSize(new Dimension(300, 22));
      m_tfType.setMaximumSize(new Dimension(400, 22));
      m_tfType.setBackground(Color.lightGray);
      m_tfType.setEditable(false);

      panel.add(type);
      panel.add(m_tfType);
    return panel;
  }


   /** 
    *
    * Create the command panel
    */
   private UTStandardCommandPanel createCommandPanel()
   {
      UTStandardCommandPanel commandPanel = 
            new UTStandardCommandPanel(this, "", SwingConstants.HORIZONTAL)
      {
        public void onOk()
        {
            DatasetPropertyDialog.this.onOk();
        }
      
      };
      return commandPanel;
   }

   /** 
    *
    * Create the command panel
    */
   private JPanel createDescriptionPanel()
   {
      JPanel panel = new JPanel(new BorderLayout());
      panel.setBorder(new EmptyBorder(5,10,10,10));
      JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

      m_taDescription = new JTextArea();
      m_taDescription.setLineWrap(true);
      m_taDescription.setWrapStyleWord(true);
      JScrollPane pane = new JScrollPane(m_taDescription);

    topPanel.add(new UTMnemonicLabel(getResources(), "description", m_taDescription));

      panel.add(topPanel, BorderLayout.NORTH);
      panel.add(pane, BorderLayout.CENTER);

      return panel;
   }

   /**
    * Create panel with button to launch cache settings
    * 
    * @return The panel, never <code>null</code>.
    */
   private JPanel createCacheSetingsPanel()
   {
      JPanel panel = new JPanel(new BorderLayout());
      panel.setBorder(new EmptyBorder(5,10,10,10));
      m_btnCacheSetting = new JButton(getResources().getString(
         "cacheSettings"));
      m_btnCacheSetting.setMnemonic(getResources().getString(
                                                "mn_cacheSettings").charAt(0));
      panel.add(m_btnCacheSetting, BorderLayout.EAST);
      m_btnCacheSetting.addActionListener(new ActionListener() 
      {
         public void actionPerformed(ActionEvent e) 
         {
            ResourceCacheSettingsDialog dialog = 
               new ResourceCacheSettingsDialog(DatasetPropertyDialog.this, 
               m_cacheSettings);
            dialog.setVisible(true);
         }
      });
   
      return panel;
   }


   /**
    * Initialize the dialogs GUI elements with its data.
    *
    */
   private void initDialog()
   {
      setSize(350, 400);
      JPanel panel = new JPanel();
      panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
      panel.setAlignmentX((float)0.0);
      panel.setBorder(new EmptyBorder(5, 5, 5, 5));
      panel.add(createNamePanel());
      panel.add(createTypePanel());
      
      
      JPanel topPanel = new JPanel();
      topPanel.add(panel, BorderLayout.WEST);
      topPanel.add(Box.createHorizontalGlue(), BorderLayout.EAST);
      UTStandardCommandPanel commandPanel = createCommandPanel();
      JPanel cmdPanel = new JPanel(new BorderLayout());
      cmdPanel.add(commandPanel, BorderLayout.EAST);
      
      getContentPane().setLayout(new BorderLayout());
      getContentPane().add(topPanel, BorderLayout.NORTH);
      getRootPane().setDefaultButton(commandPanel.getOkButton());
      getContentPane().add(createDescriptionPanel(), BorderLayout.CENTER);
      JPanel bottomPanel = new JPanel(new BorderLayout());
      
      if (FeatureSet.getFeatureSet().isFeatureSupported("ResourceCaching"))
         bottomPanel.add(createCacheSetingsPanel(), BorderLayout.NORTH);
      bottomPanel.add(cmdPanel, BorderLayout.SOUTH);
      
      getContentPane().add(bottomPanel, BorderLayout.SOUTH);
      // initialize validation cons;traints
      m_validatedComponents[0] = m_tfName;
      m_validationConstraints[0] = new StringConstraint(
         "~!@#$%^&*()+`-=[]{}|;':,.<>/?");
      setValidationFramework(m_validatedComponents, m_validationConstraints);
   }


   // implementation of IEditor
  public boolean onEdit(UIFigure figure, final Object data)
  {
      if(figure.getData() instanceof OSQueryPipe)
      {
         m_pipe = (PSPipe)figure.getData();
         m_tfType.setText(E2Designer.getResources().getString("QueryPipe"));
         OSQueryPipe query = (OSQueryPipe)m_pipe;
         m_tfName.setText(query.getDatasetName());
         m_taDescription.setText(query.getDatasetDescription());
         m_cacheSettings = new PSResourceCacheSettings(
            query.getCacheSettings());
         if (m_btnCacheSetting != null)
            m_btnCacheSetting.setEnabled(true);
      this.center();
      this.setVisible(true);
      }
    else if(figure.getData() instanceof OSUpdatePipe)
      {
      m_pipe = (PSPipe)figure.getData();
         m_tfType.setText(E2Designer.getResources().getString("UpdatePipe"));
         OSUpdatePipe update = (OSUpdatePipe)m_pipe;
         m_tfName.setText(update.getDatasetName());
         m_taDescription.setText(update.getDatasetDescription());
         if (m_btnCacheSetting != null)
            m_btnCacheSetting.setEnabled(false);
      this.center();
      this.setVisible(true);
      }

    return m_bModified;
  }


/** Overrides the PSDialog.onOk() method implementation.
*/
  public void onOk()
  {
    if (activateValidation())
    {
      try
      {
        m_bModified = true;
        if(m_pipe instanceof OSQueryPipe)
        {
          OSQueryPipe query = (OSQueryPipe)m_pipe;
          query.setDatasetName(m_tfName.getText());
          query.setDatasetDescription(m_taDescription.getText());
          query.setCacheSettings(m_cacheSettings);
        }
        else
        {
          OSUpdatePipe update = (OSUpdatePipe)m_pipe;
          update.setDatasetName(m_tfName.getText());
          update.setDatasetDescription(m_taDescription.getText());
        }
      }
      catch (Exception e)
      {
         JOptionPane.showMessageDialog(this, Util.cropErrorMessage(
            e.getMessage()), E2Designer.getResources().getString(
               "OpErrorTitle"), JOptionPane.ERROR_MESSAGE);

        e.printStackTrace();;
      }
      dispose();
    }
  }


   /**
   * Test the dialog.
   *
   */
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
          JDialog dialog = new DatasetPropertyDialog();
               dialog.setVisible(true);
            }
         });

         frame.setSize(640, 480);
         frame.setVisible(true);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

   /**
   * Added for testing reasons only.
   *
   */
  private ResourceBundle m_res = null;
  protected ResourceBundle getResources()
  {
      try
    {
      if (m_res == null)
         m_res = ResourceBundle.getBundle(getClass().getName() + "Resources",
                                                          Locale.getDefault() );
    }
    catch (MissingResourceException e)
      {
         System.out.println(e);
      }

      return m_res;
   }

  /**
   * The PSPipe object that brings up this editor dialog. The pipe and the dataset have a 1 to 1
    * correlation.
   */
   private PSPipe m_pipe = null;

   private boolean m_bModified = false;

  /**
   * The textfield for name
   */
  JTextField m_tfName = null;

  /**
   * The textfield for type
   */
  JTextField m_tfType = null;

  /**
   * The textarea for the description
   */
   JTextArea m_taDescription = null;

   /**
    * The Cache Settings button. Initialized by 
    * <code>createCacheSetingsPanel()</code>, <code>null</code>after that only
    * if the server's featureset does not support resource caching.
    * May be disabled or enabled dependending on the type of pipe referenced by
    * {@link #m_pipe}.
    */
   JButton m_btnCacheSetting = null;
   
   /**
    * The a copy of the cache settings contained in {@link #m_pipe}, initialized
    * by <code>onEdit()</code> and supplied to the cache settings dialog when 
    * required.  May be <code>null</code> if the supplied pipe does not support
    * cache settings.
    */
   private PSResourceCacheSettings m_cacheSettings = null;
   
   /**
   * the validation framework variables
   */
  private static final int NUM_COMPONENTS_VALIDATED = 1;
  private final Component m_validatedComponents[] = new Component[NUM_COMPONENTS_VALIDATED];
  private final ValidationConstraint m_validationConstraints[] = new ValidationConstraint[NUM_COMPONENTS_VALIDATED];
}


