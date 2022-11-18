/*[ RequestLinkPropertyDialog.java ]*******************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer;

import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSRequestLink;
import com.percussion.design.objectstore.PSUpdatePipe;
import com.percussion.util.PSCollection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.Enumeration;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Vector;

/**
 * The dataset input connector property dialog.
 */
////////////////////////////////////////////////////////////////////////////////
public class RequestLinkPropertyDialog extends PSEditorDialog //implements ActionListener
{
   /**
   * Construct the default request link property dialog.
   *
    */
  //////////////////////////////////////////////////////////////////////////////
   public RequestLinkPropertyDialog()
   {
     super();
     initDialog();
   }

   /**
   * Construct the default dataset input connector property dialog.
   *
    */
  //////////////////////////////////////////////////////////////////////////////
   public RequestLinkPropertyDialog(JFrame parent, OSRequestLinkSet requestLinkSet)
   {
     super(parent);
    this.setLocationRelativeTo(parent);

    m_requestLinkSet = requestLinkSet;
     initDialog();
   }

   /**
   * Create target panel
   *
   * @return   JPanel   the panel
   */
  //////////////////////////////////////////////////////////////////////////////
  private JPanel createTargetPanel()
  {
    // read only
     m_target.setEnabled(false);
    m_target.setBackground(Color.lightGray);

     JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    panel.add(new UTMnemonicLabel(getResources(), "targetDataset", m_target));
    panel.add(m_target);

    return panel;
  }
   /**
   * Create XML field panel
   *
   * @return   JPanel   the panel
   */
  //////////////////////////////////////////////////////////////////////////////
  private JPanel createXmlFieldPanel()
  {
     JPanel fieldPanel = new JPanel();
    fieldPanel.setBorder(new TitledBorder(getResources().getString("urlGeneration")));
      fieldPanel.setLayout(new BoxLayout(fieldPanel, BoxLayout.Y_AXIS));
      if(m_bUpdateEnabled)
      {
         JPanel panelUpdate = new JPanel(new FlowLayout(FlowLayout.RIGHT));
         m_comboUpdate.setEditable(true);
         panelUpdate.add(new UTMnemonicLabel(getResources(), "updateField", m_comboUpdate));
         panelUpdate.add(m_comboUpdate);

         JPanel panelInsert = new JPanel(new FlowLayout(FlowLayout.RIGHT));
         m_comboInsert.setEditable(true);
         panelInsert.add(new UTMnemonicLabel(getResources(), "insertField", m_comboInsert));
         panelInsert.add(m_comboInsert);

         JPanel panelDelete = new JPanel(new FlowLayout(FlowLayout.RIGHT));
         m_comboDelete.setEditable(true);
         panelDelete.add(new UTMnemonicLabel(getResources(), "deleteField", m_comboDelete));
         panelDelete.add(m_comboDelete);
         
         fieldPanel.add(panelUpdate);
         fieldPanel.add(panelInsert);
         fieldPanel.add(panelDelete);
      }
      else
      {
         JPanel panelQuery = new JPanel(new FlowLayout(FlowLayout.RIGHT));
         m_comboQuery.setEditable(true);
         panelQuery.add(new UTMnemonicLabel(getResources(), "queryField", m_comboQuery));
         panelQuery.add(m_comboQuery);
         fieldPanel.add(panelQuery);
      }
    return fieldPanel;
  }
   /**
   * Create the view panel
   *
   * @return   JPanel      the view panel
   */
  //////////////////////////////////////////////////////////////////////////////
  private JPanel createViewPanel()
  {
     JPanel p1 = new JPanel(new BorderLayout());
    p1.setBorder(new EmptyBorder(5, 5, 5, 5));
    p1.add(createTargetPanel(), "North");
      // NOTE: the createXmlFieldPanel() method is called in onEdit()
      p1.setPreferredSize(new Dimension(400, 170));
      return p1;
  }

   /**
   * Create the command panel.
   *
   * @return   JPanel      the command panel
   */
  //////////////////////////////////////////////////////////////////////////////
  private JPanel createCommandPanel()
  {
    m_commandPanel = new UTStandardCommandPanel(this, "", SwingConstants.VERTICAL)
    {
       // implement onOk action
       public void onOk()
      {
         RequestLinkPropertyDialog.this.onOk();
      }
    };

     JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    panel.add(m_commandPanel);
    return panel;
  }

   /**
   * Initialize the dialogs GUI elements with its data.
   *
   */
  //////////////////////////////////////////////////////////////////////////////
  private void initDialog()
  {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(new EmptyBorder(5, 5, 5, 5));
      m_viewPanel = createViewPanel();
    panel.add(m_viewPanel, "Center");
    panel.add(createCommandPanel(), "East");

     // set the default button
    getRootPane().setDefaultButton(m_commandPanel.getOkButton());

     getContentPane().setLayout(new BorderLayout());
    getContentPane().add(panel);
    pack();
  }

   /**
   * Added for testing reasons only.
   *
   */
  //////////////////////////////////////////////////////////////////////////////
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

   //////////////////////////////////////////////////////////////////////////////
   // implementation of IEditor
  public boolean onEdit(UIFigure figure, final Object data)
  {
    PSApplication app = null;
    if (data instanceof PSApplication)
      app = (PSApplication) data;
    else
      throw new IllegalArgumentException("PSApplication expected");

     if (figure.getData() instanceof OSRequestLinkSet)
    {
       m_requestLinkSet = (OSRequestLinkSet) figure.getData();

        // assume there is no target
        m_target.setText("");
      UIConnectableFigure resultPage = null;
      if (figure instanceof UIConnector)
      {
           UIConnector connector = (UIConnector) figure;
        Enumeration e = connector.getDynamicConnections();
        while (e.hasMoreElements())
        {
          UIConnectionPoint cp = (UIConnectionPoint) e.nextElement();
          UIConnectableFigure fig = cp.getOwner();
          if (fig != null)
          {
            if (fig.getId() == AppFigureFactory.DATASET_ID ||
                AppFigureFactory.BINARY_RESOURCE_ID == fig.getId())
            {
              // enable radio buttons according to the target pipe type
                     OSDataset dataset = (OSDataset) fig.getData();
                     m_bUpdateEnabled = dataset.getPipe() instanceof PSUpdatePipe;
                     m_viewPanel.add(createXmlFieldPanel(), "Center");

                     // set target dataset name
                     m_target.setText(dataset.getName());
                  }
            else if (fig.getId() == AppFigureFactory.RESULT_PAGE_ID)
            {
              // save the result page for later. we need it to get the list of
              // fields for the combo box, where to store the request URLs
              resultPage = fig;
            }
          }
        }

        // if we found the result page, we need all source datasets for the
        // combobox entries
        UTAppNavigator nav = new UTAppNavigator();
        Vector datasets = nav.getSourceDatasets(resultPage);
        for (int i=0; i<datasets.size(); i++)
        {
          OSDataset dataset = (OSDataset) ((UIConnectableFigure) datasets.get(i)).getData();
          OSPageDatatank tank = (OSPageDatatank) dataset.getPageDataTank();
          if (tank != null)
          {
                  Vector columns = tank.getColumns();
            for (int j=0; j<columns.size(); j++)
                  {
              if(m_bUpdateEnabled)
                     {
                        m_comboUpdate.addItem(new String((String)columns.get(j)));
                        m_comboInsert.addItem(new String((String)columns.get(j)));
                        m_comboDelete.addItem(new String((String)columns.get(j)));
                     }
                     else
                     {
                        m_comboQuery.addItem(new String((String)columns.get(j)));
                     }
                  }
          }
        }
      }
      else
            m_target.setText(m_requestLinkSet.getTargetDataset());

         if(m_bUpdateEnabled)
         {
            m_comboUpdate.setSelectedItem("");
            m_comboInsert.setSelectedItem("");
            m_comboDelete.setSelectedItem("");
         }
         else
            m_comboQuery.setSelectedItem("");

         PSCollection cLinks = m_requestLinkSet.getRequestLinks();
         for(int i=0; i<cLinks.size(); i++)
         {
            PSRequestLink requestLink = (PSRequestLink)cLinks.get(i);
            if(m_bUpdateEnabled)
            {
               if(requestLink.isLinkTypeUpdate())
                  m_comboUpdate.setSelectedItem(requestLink.getXmlField());
               else if(requestLink.isLinkTypeInsert())
                  m_comboInsert.setSelectedItem(requestLink.getXmlField());
               else if(requestLink.isLinkTypeDelete())
                  m_comboDelete.setSelectedItem(requestLink.getXmlField());
            }
            else
            {
               if(requestLink.isLinkTypeQuery())
                  m_comboQuery.setSelectedItem(requestLink.getXmlField());
            }
         }
         this.center();
         this.setVisible(true);
    }
    else
       throw new IllegalArgumentException("OSRequestLinkSet expected!");

    return m_modified;
  }


   /** Handles the ok button action. Overrides PSDialog onOk() method
    * implementation.
   **/
   public void onOk()
   {
      try
      {
         m_modified = true;
         String xmlFieldUpdate = "";
         String xmlFieldInsert = "";
         String xmlFieldDelete = "";
         String xmlFieldQuery = "";
         PSCollection cLinks = m_requestLinkSet.getRequestLinks();
         String strTargetDataset = m_target.getText();
         cLinks.clear();

         if(m_bUpdateEnabled) // update
         {
            xmlFieldUpdate = ((String) m_comboUpdate.getSelectedItem()).trim();
            xmlFieldInsert = ((String) m_comboInsert.getSelectedItem()).trim();
            xmlFieldDelete = ((String) m_comboDelete.getSelectedItem()).trim();
            if( 0 == xmlFieldUpdate.length() && 0 == xmlFieldInsert.length() && 0 == xmlFieldDelete.length())
            {
               PSRequestLink link = new PSRequestLink(strTargetDataset);
               link.setLinkTypeNone();
               cLinks.add(link);
            }
            else
            {
               PSRequestLink link = null;
               if(0 != xmlFieldUpdate.length())
               {
                  link = new PSRequestLink(strTargetDataset);
                  link.setLinkTypeUpdate();
                  link.setXmlField(xmlFieldUpdate);
                  cLinks.add(link);
               }
               if(0 != xmlFieldInsert.length())
               {
                  link = new PSRequestLink(strTargetDataset);
                  link.setLinkTypeInsert();
                  link.setXmlField(xmlFieldInsert);
                  cLinks.add(link);
               }
               if(0 != xmlFieldDelete.length())
               {
                  link = new PSRequestLink(strTargetDataset);
                  link.setLinkTypeDelete();
                  link.setXmlField(xmlFieldDelete);
                  cLinks.add(link);
               }
            }
         }
         else //query
         {
            xmlFieldQuery = ((String) m_comboQuery.getSelectedItem()).trim();
            if( 0 == xmlFieldQuery.length())
            {
               PSRequestLink link = new PSRequestLink(strTargetDataset);
               link.setLinkTypeNone();
               cLinks.add(link);
            }
            else
            {
               PSRequestLink link = null;
               if(xmlFieldQuery.length() > 0)
               {
                  link = new PSRequestLink(strTargetDataset);
                  link.setLinkTypeQuery();
                  link.setXmlField(xmlFieldQuery);
                  cLinks.add(link);
               }
            }
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }

      dispose();
  }

   //////////////////////////////////////////////////////////////////////////////
   /**
   * the result page output connector request link
   */
  OSRequestLinkSet m_requestLinkSet = null;
   /**
   * the target dataset
   */
  private UTFixedTextField m_target = new UTFixedTextField("",new Dimension(60,20));
   /**
   * the XML field for the Update URL
   */
  private UTFixedComboBox m_comboUpdate = new UTFixedComboBox();
   /**
   * the XML field for the Insert URL
   */
  private UTFixedComboBox m_comboInsert = new UTFixedComboBox();
   /**
   * tthe XML field for the Delete URL
   */
  private UTFixedComboBox m_comboDelete = new UTFixedComboBox();
   /**
   * tthe XML field for the Query URL
   */
  private UTFixedComboBox m_comboQuery = new UTFixedComboBox();
   /**
   * the standard command panel
   */
  private UTStandardCommandPanel m_commandPanel = null;
  /**
   * this flag will be set if any data within this dialog was modified
   */
  private boolean m_modified = false;

  /**
   * This flag is <code>true</code> if the target dataset is an update dataset.
   * It is set in the onEdit method.
  **/
  private boolean m_bUpdateEnabled = false;

   private JPanel m_viewPanel = null;
}

