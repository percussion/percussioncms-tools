/******************************************************************************
 *
 * [ CodeGenerator.java ]
 * 
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.CodeGenerator;


import com.percussion.install.Code;
import com.percussion.install.CodeException;
import com.percussion.install.IPSBrandCodeMap;
import com.percussion.install.PSBrandCodeData;
import com.percussion.install.PSBrandCodeMap;
import com.percussion.install.PSBrandCodeMapVersion;
import com.percussion.install.PSBrandCodeUtil;
import com.percussion.util.IPSBrandCodeConstants.EvalTypes;
import com.percussion.util.IPSBrandCodeConstants.ServerTypes;
import com.percussion.util.PSLineBreaker;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.FieldPosition;
import java.text.MessageFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.Vector;

/**
 * This class creates the dialog box for generating the brand code.
 */
@SuppressWarnings("unchecked")
public class CodeGenerator extends JDialog
{
   /**
    * Constructor
    * @param frame the frame in which this dialog box is displayed
    * @throws CodeException if any error occurs parsing the component map
    * Xml document
    */
   public CodeGenerator(JFrame frame) throws CodeException
   {
      super(frame);
      setModal(true);

      loadResourceBundle();
      setTitle(m_resBundle.getString("title"));
      
      m_brandCodeMap = PSBrandCodeMap.newInstance();

      layoutPanels();
      initData();
      registerListeners();
      setDefaultValues();
   }

   /**
    * Sets default selections and values for any controls that should have them.
    */
   private void setDefaultValues()
   {
      if (m_rxVersion.getItemCount() > 0)
         m_rxVersion.setSelectedIndex(m_rxVersion.getItemCount() - 1);
      setDefaultBuildNumber();
   }

   /**
    * If only one build is available, sets it as the selected item
    */
   private void setDefaultBuildNumber()
   {
      if (m_buildNumber.getItemCount() > 0)
         m_buildNumber.setSelectedIndex(m_buildNumber.getItemCount() - 1);
   }

   /**
    * Lays out the panels of this dialog box.
    */
   private void layoutPanels()
   {
      //set the sizes of text fields.
      m_codeExpire.setPreferredSize(new Dimension(100, 25));
      m_results.setPreferredSize(new Dimension(250, 25));
      m_verResults.setPreferredSize(new Dimension(250, 25));
      m_buildNumber.setPreferredSize(new Dimension(150, 25));

      // Brand Code Version (brandVersionPanel)
      ButtonGroup verGroup = new ButtonGroup();
      verGroup.add(m_rbUseRxVerBuild);
      verGroup.add(m_rbUseBrandCodeMapVer);
      m_rbUseRxVerBuild.setSelected(true);

      JLabel rxVersionLabel = new JLabel(m_resBundle.getString("rhythmyxVersion"));
      JLabel buildNumberLabel = new JLabel(m_resBundle.getString("buildNumber"));
      JLabel brandCodeVersionLabel =
             new JLabel(m_resBundle.getString("brandCodeMapVersion"));

      JPanel rxVersionPanel = new JPanel();
      rxVersionPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
      rxVersionPanel.add(m_rbUseRxVerBuild);
      rxVersionPanel.add(rxVersionLabel);
      rxVersionPanel.add(m_rxVersion);
      rxVersionPanel.add(buildNumberLabel);
      rxVersionPanel.add(m_buildNumber);

      JPanel brandCodeMapVerPanel = new JPanel();
      brandCodeMapVerPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
      brandCodeMapVerPanel.add(m_rbUseBrandCodeMapVer);
      brandCodeMapVerPanel.add(brandCodeVersionLabel);
      brandCodeMapVerPanel.add(m_bcmVersion);

      JPanel brandVersionPanel = new JPanel();
      brandVersionPanel.setLayout(new BorderLayout());
      brandVersionPanel.add(rxVersionPanel, "North");
      brandVersionPanel.add(brandCodeMapVerPanel, "South");

      brandVersionPanel.setPreferredSize(new Dimension(450, 90));
      brandVersionPanel.setBorder(new TitledBorder(
         new EtchedBorder(EtchedBorder.LOWERED),
         m_resBundle.getString("version")));

      // Server Type Panel (serverTypePanel)
      JLabel serverTypeLabel = new JLabel(m_resBundle.getString("servertype"));
      JPanel serverTypePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
      m_serverType = new JComboBox();
      
      serverTypePanel.add(serverTypeLabel);
      serverTypePanel.add(m_serverType);

      serverTypePanel.setPreferredSize(new Dimension(450, 40));

      // Install Type Panel (installTypePanel)
      JPanel installTypePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
      JLabel evalTypeLabel = new JLabel(m_resBundle.getString("evalType"));
      
      installTypePanel.add(evalTypeLabel);
      installTypePanel.add(m_evalType);

      installTypePanel.setPreferredSize(new Dimension(450, 40));

      // Expire Panel (expirePanel)
      JPanel expirePanel = new JPanel();
      expirePanel.setLayout(new FlowLayout(FlowLayout.CENTER));

      m_format = new SimpleDateFormat("M/dd/yyyy");

      JLabel formatl = new JLabel("(" + m_format.toPattern() + ")");
      JLabel lblCodeExpire = new JLabel(m_resBundle.getString("codelabel"));

      expirePanel.add(lblCodeExpire);
      expirePanel.add(m_codeExpire);
      expirePanel.add(formatl);

      expirePanel.setPreferredSize(new Dimension(400, 40));

      // central panel (centerPanel)
      JPanel centerPanel = new JPanel();
      centerPanel.setLayout(new BorderLayout());
      centerPanel.add(serverTypePanel, BorderLayout.NORTH);
      centerPanel.add(installTypePanel, BorderLayout.CENTER);
      centerPanel.add(expirePanel, BorderLayout.SOUTH);

      centerPanel.setBorder(new TitledBorder(
         new EtchedBorder(EtchedBorder.LOWERED), ""));

      // License & Propties Table Panel (licPropsPanel)
      JPanel licPanel = new JPanel();
      licPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
      JLabel licenseLbl = new JLabel(m_resBundle.getString("license"));
      licPanel.add(licenseLbl);
      licPanel.add(m_licenseCombo);

      // Properties Table
      PropertiesTableModel propsTableModel = new PropertiesTableModel();
      int tblColCount = propsTableModel.getColumnCount();
      if (tblColCount != PROPS_TBL_COLUMN_COUNT)
      {
         Object []args = new Object[] {String.valueOf(tblColCount)};
         String err = createMessage("errMsgInvalidNumberOfColumns", args);
         showErrorMessage(err);
         return;
      }
      m_propsTable = new JTable(propsTableModel);
      m_propsTable.setPreferredScrollableViewportSize(new Dimension(400, 40));

      TableColumn tblColumn = null;
      tblColumn = m_propsTable.getColumnModel().getColumn(0);
      tblColumn.setPreferredWidth(200);
      tblColumn = m_propsTable.getColumnModel().getColumn(1);
      tblColumn.setPreferredWidth(50);

      //Create the scroll pane and add the table to it.
      JScrollPane propsScrollPane = new JScrollPane(m_propsTable);

      JPanel propsPanel = new JPanel();
      propsPanel.add(propsScrollPane);

      JPanel licPropsPanel = new JPanel();
      licPropsPanel.setLayout(new BorderLayout());
      licPropsPanel.add(licPanel, "North");
      licPropsPanel.add(propsPanel, "South");
      licPropsPanel.setBorder(new TitledBorder(
         new EtchedBorder(EtchedBorder.LOWERED),
         m_resBundle.getString("license")));

      // Result and Verify Panel (southPanel)
      m_generate = new JButton(m_resBundle.getString("generate"));
      m_verify = new JButton(m_resBundle.getString("verify"));

      JPanel textFieldsPanel = new JPanel();
      textFieldsPanel.setLayout(new BoxLayout(textFieldsPanel,
         BoxLayout.Y_AXIS));
      textFieldsPanel.add(Box.createVerticalStrut(10));
      textFieldsPanel.add(m_results);
      textFieldsPanel.add(Box.createVerticalStrut(10));
      textFieldsPanel.add(m_verResults);
      textFieldsPanel.add(Box.createVerticalStrut(10));

      JPanel buttonsPanel = new JPanel();
      buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));
      buttonsPanel.add(Box.createVerticalStrut(10));
      buttonsPanel.add(m_generate);
      buttonsPanel.add(Box.createVerticalStrut(10));
      buttonsPanel.add(m_verify);
      buttonsPanel.add(Box.createVerticalStrut(10));

      JPanel southPanel = new JPanel();
      southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.X_AXIS));
      southPanel.add(Box.createHorizontalStrut(20));
      southPanel.add(textFieldsPanel);
      southPanel.add(Box.createHorizontalStrut(20));
      southPanel.add(buttonsPanel);
      southPanel.add(Box.createHorizontalStrut(20));

      //Part Panel (partPanel)
      PartsTableModel dataModel = new PartsTableModel();
      int colCount = dataModel.getColumnCount();
      if (colCount != PARTS_TBL_COLUMN_COUNT)
      {
         Object []args = new Object[] {String.valueOf(colCount)};
         String err = createMessage("errMsgInvalidNumberOfColumns", args);
         showErrorMessage(err);
         return;
      }
      m_partsTable = new JTable(dataModel);
      m_partsTable.setPreferredScrollableViewportSize(new Dimension(400, 210));

      TableColumn column = null;
      column = m_partsTable.getColumnModel().getColumn(0);
      column.setPreferredWidth(75);
      column = m_partsTable.getColumnModel().getColumn(1);
      column.setPreferredWidth(320);

      //Create the scroll pane and add the table to it.
      JScrollPane scrollPane = new JScrollPane(m_partsTable);

      JPanel partPanel = new JPanel();
      partPanel.setBorder(new TitledBorder(new EtchedBorder(
         EtchedBorder.LOWERED), m_resBundle.getString("optional")));

      partPanel.setLayout(new BorderLayout());
      partPanel.add(scrollPane, BorderLayout.CENTER);

      // Main Panel
      JPanel northPanel = new JPanel();
      northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
      northPanel.add(brandVersionPanel);
      northPanel.add(centerPanel);
      northPanel.add(licPropsPanel);

      m_mainPanel = new JPanel(new BorderLayout());
      m_mainPanel.add(northPanel, BorderLayout.NORTH);
      m_mainPanel.add(partPanel, BorderLayout.CENTER);
      m_mainPanel.add(southPanel, BorderLayout.SOUTH);

      setContentPane(m_mainPanel);
      setSize(1000,1000);
      pack();

      //now set location centered
      Dimension screenSize = new Dimension(
         Toolkit.getDefaultToolkit().getScreenSize());
      int x = (screenSize.width / 2) - (getSize().width / 2);
      int y = (screenSize.height / 2) - (getSize().height / 2);
      setLocation(new Point(x, y));
   }

   /**
    * Initializes the data displayed in this dialog box when this dialog box
    * starts.
    * @throws CodeException if any error occurs obtaining data from the brand
    * code map object <code>m_brandCodeMap</code>.
    */
   private void initData()
      throws CodeException
   {
      // set the date brand code should expire
      GregorianCalendar today = new GregorianCalendar();
      today.add(Calendar.MONTH, 1);
      StringBuffer buffer = new StringBuffer();
      buffer = m_format.format(today.getTime(), buffer, new FieldPosition(0));
      m_codeExpire.setText(buffer.toString());

      // get the Rhythmyx Versions
      List rxVersions = m_brandCodeMap.getRhythmyxVersions();
      Collections.sort(rxVersions);
      Iterator it = rxVersions.iterator();
      while (it.hasNext())
         m_rxVersion.addItem(it.next());
      m_rxVersion.setSelectedIndex(-1);

      // get the brand code map versions
      Iterator itbcmv = m_brandCodeMap.getBrandCodeMapVersions();
      while (itbcmv.hasNext())
      {
         PSBrandCodeMapVersion bcmv = (PSBrandCodeMapVersion)itbcmv.next();
         m_bcmVersion.addItem(new Integer(bcmv.getVersion()));
      }
      m_bcmVersion.setSelectedIndex(-1);
      m_bcmVersion.setEnabled(false);

      // populate the properties table
      Iterator itProps = m_brandCodeMap.getProperties().entrySet().iterator();
      m_propertiesIdValueMap = new HashMap();
      while (itProps.hasNext())
      {
         Map.Entry item = (Map.Entry)itProps.next();
         String propId = (String)item.getKey();
         m_propertiesIdValueMap.put(propId, "1");
      }
      setProperties(m_propertiesIdValueMap);
   }

   /**
    * Sets the values in the properties table based on the values in the
    * specified map.
    *
    * @param propertiesMap map containing the id of property
    * (<code>String</code>) as key and quantity (<code>String</code>) as value,
    * assumed not <code>null</code>
    *
    * @throws CodeException if any error occurs
    */
   private void setProperties(Map propertiesMap)
      throws CodeException
   {
      TableModel propsTblModel = m_propsTable.getModel();
      ((PropertiesTableModel)propsTblModel).deleteAllRows();
      Iterator itProps = propertiesMap.entrySet().iterator();
      int counter = 0;
      m_propertiesList.clear();
      while (itProps.hasNext())
      {
         Map.Entry item = (Map.Entry)itProps.next();

         String propId = (String)item.getKey();
         String propValue = (String)item.getValue();
         String propName = (String)m_brandCodeMap.getProperties().get(propId);

         m_propertiesList.add(propId);
         propsTblModel.setValueAt(propName, counter, PROPS_TBL_NAME_COL);
         propsTblModel.setValueAt(propValue, counter, PROPS_TBL_QUANTITY_COL);
         counter++;
      }
   }

   /**
    * Registers the event listeners for the controls of this dialog box
    */
   private void registerListeners()
   {
      m_rbUseRxVerBuild.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            clearCode();
            onClickUseRxVerBuild(e);
         }
      });

      m_rbUseBrandCodeMapVer.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            clearCode();
            onClickUseBrandCodeMapVer(e);
         }
      });

      m_rxVersion.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            clearCode();
            onSelectRhythmyxVersion(e);
         }
      });

      m_buildNumber.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            clearCode();
            onSelectBuildNumber(e);
         }
      });

      m_bcmVersion.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            clearCode();
            onSelectBrandCodeMapVersion(e);
         }
      });

      m_licenseCombo.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            clearCode();
            onSelectLicense(e);
         }
      });

      m_generate.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            onClickGenerateCode(e);
         }
      });

      m_verify.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            clearCode();
            onClickVerifyCode(e);
         }
      });

      m_serverType.addItemListener(new ItemListener()
      {
         public void itemStateChanged(@SuppressWarnings("unused")
         ItemEvent e)
         {
            clearCode();
            onSelectServerType();
         }
      });

      m_evalType.addActionListener(new ActionListener()
      {
         public void actionPerformed(@SuppressWarnings("unused")
         ActionEvent e)
         {
            clearCode();
            onSelectEvalType();
         }
      });

      m_codeExpire.addActionListener(new ActionListener()
      {
         public void actionPerformed(@SuppressWarnings("unused")
         ActionEvent e)
         {
            clearCode();
         }
      });

   }

   /**
    * Handle the event generated by clicking the radio button corresponding to
    * use Rhythmyx Version and build number.
    *
    * @param e the event triggered by selecting the radio button, may be
    * <code>null</code>.
    */
   private void onClickUseRxVerBuild(ActionEvent e)
   {
      try
      {
         m_bcmVersion.setEnabled(false);
         m_selectedBcmv = -1;
         m_rxVersion.setEnabled(true);
         m_buildNumber.setEnabled(true);

         m_bListParts = false;
         setLicenses(-1);
         int selIndex = m_rxVersion.getSelectedIndex();
         if (selIndex > -1)
            onSelectRhythmyxVersion(null);
      }
      catch (Exception ex)
      {
         handleException(ex);
      }
      finally
      {
         m_bListParts = true;
      }
   }

   /**
    * Handle the event generated by clicking the radiobuton corresponding to
    * use brand code map version.
    *
    * @param e the event triggered by selecting the radiobutton, may be
    * <code>null</code>.
    */
   private void onClickUseBrandCodeMapVer(ActionEvent e)
   {
      try
      {
         m_bcmVersion.setEnabled(true);
         m_rxVersion.setEnabled(false);
         m_buildNumber.setEnabled(false);

         m_bListParts = false;
         setLicenses(-1);
         int selIndex = m_bcmVersion.getSelectedIndex();
         if (selIndex > -1)
            onSelectBrandCodeMapVersion(null);
      }
      catch (Exception ex)
      {
         handleException(ex);
      }
      finally
      {
         m_bListParts = true;
      }
   }

   /**
    * Handle the event generated when the user selects a Rhythmyx version.
    *
    * @param e the event triggered by selecting a Rhythmyx version in the
    * combo box, may be <code>null</code>.
    */
   private void onSelectRhythmyxVersion(ActionEvent e)
   {
      try
      {
         setBuildNumbers();
         setLicenses(-1);
         setDefaultBuildNumber();
      }
      catch (Exception ex)
      {
         handleException(ex);
      }
   }

   /**
    * Handle the event generated when the user selects the Rhythmyx build
    * number.
    *
    * @param e the event triggered by selecting the Rhythmyx build number,
    * may be <code>null</code>.
    */
   private void onSelectBuildNumber(ActionEvent e)
   {
      try
      {
         m_bListParts = false;
         int selIndex = m_buildNumber.getSelectedIndex();
         if (selIndex > -1)
         {
            int bcmv = getBrandCodeMapVersion();
            setMapVersionData(bcmv);
         }
      }
      catch (Exception ex)
      {
         handleException(ex);
      }
      finally
      {
         m_bListParts = true;
      }
   }

   /**
    * Handle the event generated when the user selects a Rhythmyx version.
    *
    * @param e the event triggered by selecting a Rhythmyx version in the
    * combo box, may be <code>null</code>.
    */
   private void onSelectBrandCodeMapVersion(ActionEvent e)
   {
      try
      {
         m_bListParts = false;
         int bcmv = getBrandCodeMapVersion();
         int option = JOptionPane.NO_OPTION;
         if (m_selectedBcmv != -1 && bcmv != -1 && m_selectedBcmv != bcmv && 
            m_licenseCombo.getSelectedIndex() != -1)
         {
            String title = m_resBundle.getString("changeMapVersionTitle");
            String msg = m_resBundle.getString("changeMapVersionPrompt");
            msg = PSLineBreaker.wrapString(msg, 80, 25, "\n");
            option = JOptionPane.showConfirmDialog(m_mainPanel, msg, title, 
               JOptionPane.YES_NO_OPTION);
         }
         
         if (option == JOptionPane.OK_OPTION)
         {
            PSBrandCodeData data = getBrandCodeData();
            data.setBrandCodeMapVersion(m_selectedBcmv);
            setMapVersionData(bcmv);
            m_bListParts = true;
            setDataFromCode(convertData(data, bcmv));
         }
         else
         {
            setMapVersionData(bcmv);
         }
         
         m_selectedBcmv = bcmv;
      }
      catch (Exception ex)
      {
         m_selectedBcmv = -1;
         handleException(ex);
      }
      finally
      {
         m_bListParts = true;
      }
   }

   /**
    * Handle the event generated when the user selects a license.
    *
    * @param e the event triggered by selecting a license in the
    * combo box, may be <code>null</code>.
    */
   private void onSelectLicense(ActionEvent e)
   {
      try
      {
         // populate the parts table
         TableModel partsTblModel = m_partsTable.getModel();
         ((PartsTableModel)partsTblModel).deleteAllRows();
         ((PartsTableModel)partsTblModel).disableRows(-1);
         if (!m_bListParts)
            return;

         int licenseId = getSelectedLicenseId();
         if (licenseId == -1)
            return;
         
         int bcmv = getBrandCodeMapVersion();

         m_partsId.clear();
         int rowIndex = 0;

         // required parts - don't add to the table
         m_reqParts = m_brandCodeMap.getParts(bcmv, licenseId,
            IPSBrandCodeMap.PARTS_TYPE_REQUIRED);

         // optional parts
         m_optParts = new HashMap();

         // selected optional parts
         Map selOptPartsMap = m_brandCodeMap.getParts(bcmv, licenseId,
            IPSBrandCodeMap.PARTS_TYPE_OPTIONAL_SELECTED);
         m_optParts.putAll(selOptPartsMap);
         rowIndex = addParts(selOptPartsMap, rowIndex, true, true);

         // unselected optional parts
         Map unselOptPartsMap = m_brandCodeMap.getParts(bcmv, licenseId,
            IPSBrandCodeMap.PARTS_TYPE_OPTIONAL_UNSELECTED);
         m_optParts.putAll(unselOptPartsMap);
         rowIndex = addParts(unselOptPartsMap, rowIndex, false, true);

         // populate the properties table
         List props = m_brandCodeMap.getLicenseProperties(bcmv, licenseId);
         Map propsMap = new HashMap();
         Iterator it = props.iterator();
         while (it.hasNext())
         {
            String propId = (String)it.next();
            String propValue = (String)m_propertiesIdValueMap.get(propId);
            propsMap.put(propId, propValue);
         }
         setProperties(propsMap);
      }
      catch (Exception ex)
      {
         handleException(ex);
      }
   }
   
   /**
    * Filters License list based on the selected server type
    */
   private void onSelectServerType()
   {
      try
      {
         setLicenses(getBrandCodeMapVersion());
      }
      catch (CodeException e)
      {
         handleException(e);
      }
   }
   
   /**
    * Sets the expiration date based on the eval type selected
    */
   private void onSelectEvalType()
   {
      Calendar cal = new GregorianCalendar();
      EvalTypes evalType = (EvalTypes) m_evalType.getSelectedItem();
      if (evalType == null)
         return;
      
      int days = 0;
      if (evalType.equals(EvalTypes.NOT_EVAL) || 
         evalType.equals(EvalTypes.M30_DAY))
      {
         days = 30;
      }
      else if (evalType.equals(EvalTypes.M60_DAY))
         days = 60;
      else if (evalType.equals(EvalTypes.M90_DAY))
         days = 90;
      
      cal.add(Calendar.DAY_OF_MONTH, days);
      setCodeExpString(cal);
   }

   /**
    * Adds the specified parts to the parts table model.
    *
    * @param partsMap map containing the id of the part (<code>String</code>)
    * as the key and the part name (<code>String</code>) as the value, assumed
    * not <code>null</code>
    * @param startRowIndex index of the row in the parts table model at which
    * the rows insertion should start
    * @param selected should the parts be selected or not, if <code>true</code>
    * the parts are selected, otherwise not
    * @param enabled should the parts be enabled, if <code>true</code> the user
    * can select/unselect the part, otherwise not
    *
    * @return the index of the row at which new rows can be inserted in the
    * parts table
    */
   private int addParts(Map partsMap, int startRowIndex,
      boolean selected, boolean enabled)
   {
      int rowIndex = startRowIndex;
      TableModel partsTblModel = m_partsTable.getModel();
      Set sortedSet = getMapValues(partsMap, true, null);
      Map tempMap = reverseMap(partsMap);
      Iterator it = sortedSet.iterator();
      while (it.hasNext())
      {
         String partName = (String)it.next();
         String partId = (String)tempMap.get(partName);
         m_partsId.add(partId);

         boolean isSel = selected;
         if (!isSel)
            isSel = m_selectedPartsList.contains(partId);
         partsTblModel.setValueAt(new Boolean(isSel), rowIndex,
            PARTS_TBL_CHECKBOXES_COL);

         partsTblModel.setValueAt(partName, rowIndex, PARTS_TBL_PARTS_COL);

         if (!enabled)
            ((PartsTableModel)partsTblModel).disableRows(rowIndex);

         rowIndex++;
      }
      return rowIndex;
   }

   /**
    * Handle the event generated when the user clicks on generate brand code button.
    *
    * @param e the event triggered by clicking on generate brand code button,
    * may be <code>null</code>.
    */
   private void onClickGenerateCode(ActionEvent e)
   {
      try
      {
         // stop the cell editing for the properties table
         stopCellEditing(m_propsTable);

         PSBrandCodeData data = getBrandCodeData();

         Code code = new Code(data);
         m_results.setText(code.toString());
      }
      catch (Exception ex)
      {
         handleException(ex);
      }
   }

   /**
    * Creates the brand code data from the current UI values.
    * 
    * @return The data, never {@link NullPointerException}.
    * 
    * @throws CodeException if any selection or value is invalid for code
    * generation.
    */
   private PSBrandCodeData getBrandCodeData() throws CodeException
   {
      // Verify the brand code expiry date
      Date codeExpire = getBrandCodeExpiryDate();

      EvalTypes productExpire = (EvalTypes) m_evalType.getSelectedItem();
      
      ServerTypes serverType = (ServerTypes) m_serverType.getSelectedItem();

      int bcmv = getBrandCodeMapVersion();
      int licenseId = getSelectedLicenseId();
      if (licenseId == -1)
      {
         throw new CodeException(
            m_resBundle.getString("selectLicense"));
      }

      TableModel partsTblModel = m_partsTable.getModel();
      List partsIdList = ((PartsTableModel)partsTblModel).getSelectedPartsId();

      // add the selected parts to the list which stores all the parts
      // selected by the user when generating the brand code
      Iterator itSelParts = partsIdList.iterator();
      while (itSelParts.hasNext())
      {
         String selPart = (String)itSelParts.next();
         if (!m_selectedPartsList.contains(selPart))
            m_selectedPartsList.add(selPart);
      }
      
      // add required parts
      partsIdList.addAll(m_reqParts.keySet());

      System.out.println("Generating code with required parts: " + 
         partsIdList);

      TableModel propsTblModel = m_propsTable.getModel();
      Map propsMap = ((PropertiesTableModel)propsTblModel).getProperties();
      for (Object val : propsMap.values())
      {
         try
         {
            int intVal = Integer.parseInt((String) val); 
            if (intVal < 0 || intVal >= 64)
            {
               throw new CodeException(
                  m_resBundle.getString("invalidPropertyValue"));
            }               
         }
         catch (NumberFormatException e1)
         {
            throw new CodeException(
               m_resBundle.getString("nonNumericPropertyValue"));
         }
         

      }
      // save property values for reuse
      m_propertiesIdValueMap.putAll(propsMap);

      PSBrandCodeData data = new PSBrandCodeData();
      data.setBrandCodeMapVersion(bcmv);
      data.setServerType(serverType);
      data.setProductExpires(productExpire);
      data.setCodeExpires(codeExpire);
      data.setLicenseId(licenseId);
      data.setPartsId(partsIdList);
      data.setProperties(propsMap);
      return data;
   }


   /**
    * Handle the event generated when the user clicks on verify brand code button.
    *
    * @param e the event triggered by clicking on verify brand code button,
    * may be <code>null</code>.
    */
   private void onClickVerifyCode(ActionEvent e)
   {
      try
      {
         // stop the cell editing for the properties table
         stopCellEditing(m_propsTable);

         String strCode = m_verResults.getText();
         if ((strCode == null) || (strCode.trim().length() == 0))
         {
            throw new CodeException(
               m_resBundle.getString("enterBrandCodeToVerify"));
         }
         Code deCode = new Code(strCode);
         setDataFromCode(deCode.getBrandCodeData());
      }
      catch (Exception ex)
      {
         handleException(ex);
      }
   }

   /**
    * Sets Data in the dialog box from the values obtained from the brand code.
    * @param codeData The brand code data, assumed not <code>null</code>
    */
   private void setDataFromCode(PSBrandCodeData codeData)
   {
      // set the brand code map version
      m_bcmVersion.setEnabled(true);
      m_rxVersion.setEnabled(false);
      m_buildNumber.setEnabled(false);

      m_rbUseBrandCodeMapVer.setSelected(true);
      int bcmv = codeData.getBrandCodeMapVersion();
      m_bcmVersion.setSelectedItem(new Integer(bcmv));

      // server type
      m_serverType.setSelectedItem(codeData.getServerType());

      // install type
      m_evalType.setSelectedItem(codeData.getProductExpires());

      //expiration date
      GregorianCalendar cal = new GregorianCalendar();
      cal.setTime(codeData.getCodeExpires());
      setCodeExpString(cal);

      // license
      int licenseId = codeData.getLicenseId();
      String strLicenseId = (String)m_licenses.get("" + licenseId);
      m_licenseCombo.setSelectedItem(strLicenseId);

      // properties
      Map propsMap = codeData.getProperties();
      TableModel propsTblModel = m_propsTable.getModel();
      Iterator itProps = m_propertiesList.iterator();
      int counter = 0;
      while (itProps.hasNext())
      {
         String propId = (String)itProps.next();
         String propValue = (String)propsMap.get(propId);
         if (propValue != null)
         {
            propsTblModel.setValueAt(propValue, counter,
                  PROPS_TBL_QUANTITY_COL);
         }
         
         counter++;
      }

      // parts
      List partsIdList = codeData.getPartsId();
      TableModel partsTblModel = m_partsTable.getModel();
      Iterator partIt = m_partsId.iterator();
      counter = 0;
      while (partIt.hasNext())
      {
         String partId = (String)partIt.next();
         if (partsIdList.contains(partId))
         {
            partsTblModel.setValueAt(new Boolean(true), counter, 
               PARTS_TBL_CHECKBOXES_COL);
         }
         else
         {
            partsTblModel.setValueAt(new Boolean(false), counter, 
               PARTS_TBL_CHECKBOXES_COL);
         }
         counter++;
      }
   }
   
   /**
    * Sets the text for the code expiration field from the supplied calendar
    * object.
    * 
    * @param cal The calendar object, assumed not <code>null</code>.
    */
   private void setCodeExpString(Calendar cal)
   {
      String yr = Integer.toString(cal.get(Calendar.YEAR));
      String mon = Integer.toString(cal.get(Calendar.MONTH)+1);
      String day = Integer.toString(cal.get(Calendar.DAY_OF_MONTH));
      m_codeExpire.setText(mon + "/" + day + "/" + yr);
   }

   /**
    * Converts the supplied data to the specified brand code map version. Will
    * display a warning message to the user if any parts or properties specified
    * in the data are unsupported in the new version.  A noop if the data 
    * already specifies the new brand code map version.
    * 
    * @param data The data to convert, assumed not <code>null</code>.
    * @param bcmv The new brandcode map version, assumed to be valid.
    * 
    * @return The converted data, never <code>null</code>.
    * 
    * @throws CodeException If the server type or license specified in the 
    * supplied data is not valid for the new brand code map version.
    */
   private PSBrandCodeData convertData(PSBrandCodeData data, int bcmv) 
      throws CodeException
   {
      int oldBcmv = data.getBrandCodeMapVersion();
      if (oldBcmv == bcmv)
         return data;
      
      PSBrandCodeData result = new PSBrandCodeData();
      result.setBrandCodeMapVersion(bcmv);
      result.setCodeExpires(data.getCodeExpires());
      
      EvalTypes evalType = data.getProductExpires();
      if (evalType.isExtendedEvalType() && 
         !m_brandCodeMap.supportsExtendedProductInfo(bcmv))
      {
         throw new CodeException(MessageFormat.format(m_resBundle.getString(
            "cannotConvertEvalType"), new Object[] {bcmv, evalType}));
      }
      result.setProductExpires(data.getProductExpires());
      
      ServerTypes serverType = data.getServerType();
      if (serverType.isExtendedServerType() && 
         !m_brandCodeMap.supportsExtendedProductInfo(bcmv))
      {
         throw new CodeException(MessageFormat.format(m_resBundle.getString(
            "cannotConvertServerType"), new Object[] {bcmv, serverType}));
      }
      result.setServerType(data.getServerType());

      int licenseId = data.getLicenseId();
      Object licenseName = m_brandCodeMap.getLicenses(oldBcmv).get(
         String.valueOf(licenseId));
      if (!m_brandCodeMap.getLicenses(bcmv).containsKey(
         String.valueOf(licenseId)))
      {
         throw new CodeException(MessageFormat.format(m_resBundle.getString(
            "cannotConvertLicense"), new Object[] {bcmv, licenseName}));
      }
      List<ServerTypes> licenseTypes = m_brandCodeMap.getLimitedServerTypes(
         bcmv, licenseId);
      if (!(licenseTypes.isEmpty() || licenseTypes.contains(serverType)))
      {
         throw new CodeException(MessageFormat.format(m_resBundle.getString(
            "cannotConvertLicenseForServerType"), new Object[] {bcmv, 
            serverType, licenseName}));
      }
      result.setLicenseId(licenseId);
      
      Set<String> parts = new HashSet<String>(data.getPartsId());
      Map<String, String> allParts = m_brandCodeMap.getParts(bcmv, licenseId, 
         IPSBrandCodeMap.PARTS_TYPE_ALL);
      

      // warn on parts not supported
      Set<String> warnPartIds = new HashSet<String>(parts);
      warnPartIds.removeAll(allParts.keySet());
      if (!warnPartIds.isEmpty())
      {
         List<String> warnPartNames = new ArrayList<String>();
         for (String partId : warnPartIds)
         {
            warnPartNames.add(m_brandCodeMap.getPartName(oldBcmv, 
               Integer.parseInt(partId)));
         }
         
         String msg = MessageFormat.format(m_resBundle.getString(
            "warnUnsupportedParts"), new Object[] {bcmv, licenseName, 
               warnPartNames});
         showWarningMessage(msg);
      }
      
      // keep the parts that are supported
      parts.retainAll(allParts.keySet());
      // add required parts for the license
      parts.addAll(m_brandCodeMap.getParts(bcmv, licenseId, 
         IPSBrandCodeMap.PARTS_TYPE_REQUIRED).keySet());
      List<String> newParts = new ArrayList<String>(parts);
      result.setPartsId(newParts);
      
      // filter properties for allowed choices 
      List licPropIds = m_brandCodeMap.getLicenseProperties(bcmv, licenseId);
      Map<String, String> props = data.getProperties();
      Set<String> warnPropIds = new HashSet<String>(props.keySet());
      warnPropIds.removeAll(licPropIds);
      if (!warnPropIds.isEmpty())
      {
         Map<String, String> allProps = m_brandCodeMap.getProperties();
         List<String> warnPropNames = new ArrayList<String>();
         for (String propId : warnPropIds)
         {
            warnPropNames.add(allProps.get(propId));
            props.remove(propId);
         }
         String msg = MessageFormat.format(m_resBundle.getString(
         "warnUnsupportedProperties"), new Object[] {bcmv, licenseName, 
            warnPropNames});
         showWarningMessage(msg);
      }
      
      result.setProperties(props);
      
      return result;
   }

   /**
    * Tell the table cell editor for the specified table to stop editing and
    * accept any partially edited value as the value of the editor.
    *
    * @param table the table for which to stop cell editing, may not be
    * <code>null</code>
    */
   private void stopCellEditing(JTable table)
   {
      if (table == null)
         throw new IllegalArgumentException("table may not be null");

      if (table.isEditing())
      {
         int editRow = table.getEditingRow();
         int editCol = table.getEditingColumn();
         TableCellEditor cellEditor = table.getCellEditor(editRow, editCol);
         if (cellEditor != null)
            cellEditor.stopCellEditing();
      }
   }

   /**
    * Prints the error message and stacktrace to the console. Also displays
    * a message box with the error description.
    * @param ex the exception, assumed not <code>null</code>
    */
   private void handleException(Exception ex)
   {
      System.out.println("Error : " + ex.getLocalizedMessage());
      ex.printStackTrace();
      showErrorMessage(ex.getLocalizedMessage());
   }

   /**
    * Returns the brand code map version to use for generating the brand code.
    * @return the brand code map version to use for generating the brand code,
    * always greater than 0.
    * @throws CodeException if any error occurs
    */
   private int getBrandCodeMapVersion()
      throws CodeException
   {
      int selIndex = -1;
      if (m_rbUseRxVerBuild.isSelected())
      {
         // get the brand code map version based on the Rhythmyx version
         // and build number
         selIndex = m_rxVersion.getSelectedIndex();
         if (selIndex < 0)
         {
            throw new CodeException(
               m_resBundle.getString("selectRhythmyxVersion"));
         }
         String version = (String)m_rxVersion.getSelectedItem();

         selIndex = m_buildNumber.getSelectedIndex();
         if (selIndex < 0)
         {
            throw new CodeException(
               m_resBundle.getString("selectRhythmyxBuildNumber"));
         }
         BuildNumber bldNumber = (BuildNumber)m_buildNumber.getSelectedItem();

         return m_brandCodeMap.getBrandCodeMapVersion(version,
            bldNumber.getBuildFrom(), bldNumber.getBuildTo());
      }
      else
      {
         // return the brand code map version selected in the brand code map
         // version combo box
         selIndex = m_bcmVersion.getSelectedIndex();
         if (selIndex < 0)
         {
            throw new CodeException(
               m_resBundle.getString("selectBrandCodeMapVersion"));
         }
         Integer iVersion = (Integer)m_bcmVersion.getSelectedItem();
         return iVersion.intValue();
      }
   }

   /**
    * Returns the date on which the brand code should expire. This method
    * verifies that the date entered by the user is correct. The date entered
    * should be greater that the current date and the year in the date should
    * always be greater than BRAND_CODE_EXPIRE_BASE_YR.
    * @return the date on which the brand code should expire, never
    * <code>null</code>
    * @throws CodeException if invalid date is entered
    */
   private Date getBrandCodeExpiryDate()
      throws CodeException
   {
      String strDate = m_codeExpire.getText();
      String invalidDate = m_resBundle.getString("invalidDate");
      if ((strDate == null) || (strDate.trim().length() < 1))
         throw new CodeException(invalidDate);
      Date codeExpire = m_format.parse(strDate, new ParsePosition(0));
      if (codeExpire == null)
         throw new CodeException(invalidDate);

      GregorianCalendar now = new GregorianCalendar();
      GregorianCalendar expire = new GregorianCalendar();
      expire.setTime(codeExpire);

      now.clear(Calendar.HOUR);
      now.clear(Calendar.MINUTE);
      now.clear(Calendar.SECOND);
      now.clear(Calendar.MILLISECOND);
      expire.clear(Calendar.HOUR);
      expire.clear(Calendar.MINUTE);
      expire.clear(Calendar.SECOND);
      expire.clear(Calendar.MILLISECOND);

      if (expire.before(now))
         throw new CodeException(invalidDate);

      now.set(Calendar.YEAR, Code.BRAND_CODE_EXPIRE_BASE_YR);
      if (expire.before(now))
         throw new CodeException(invalidDate);

      return codeExpire;
   }

   /**
    * Returns the id of the license selected by the user.
    * @return the id of the license selected by the user, always non-negative
    * for a valid license, -1 if no license is selected.
    * 
    */
   private int getSelectedLicenseId()
   {
      int selIndex = m_licenseCombo.getSelectedIndex();
      if (selIndex < 0)
      {
         return -1;
      }
      String strLicenseId = (String)m_licensesId.get(selIndex);
      return Integer.parseInt(strLicenseId);
   }

   /**
    * Clears the list of current licenses and parts. Then adds licenses to the
    * license combobox. The licenses added depend upon the specified brand code
    * map version and selected server type. If brandCodeMapVersion is less than
    * 1, or if there is no server type selected, then it only clears the list of
    * current licenses and parts.
    * 
    * @param brandCodeMapVersion the brand code map version to use to get the
    * list of licenses, if less than 1 then only the list of current licenses
    * and parts is cleared. If 1 or greater, then adds the list of licenses to
    * the license combobox.
    * 
    * @throws CodeException if any error occurs obtaining the list of licenses
    * for the specified brand code map version
    */
   private void setLicenses(int brandCodeMapVersion)
      throws CodeException
   {
      m_licenseCombo.removeAllItems();
      TableModel partsTblModel = m_partsTable.getModel();
      ((PartsTableModel)partsTblModel).deleteAllRows();
      m_licensesId.clear();

      if (brandCodeMapVersion < 1)
         return;
      
      ServerTypes curType = (ServerTypes) m_serverType.getSelectedItem();
      if (curType == null)
         return;

      m_licenses = m_brandCodeMap.getLicenses(brandCodeMapVersion);
      Map tempMap = reverseMap(m_licenses);

      Set sortedSet = getMapValues(m_licenses, true,
         new LicenseComparator(tempMap));

      Iterator itList = sortedSet.iterator();
      while (itList.hasNext())
      {
         String licenseName = (String)itList.next();
         String licenseId = (String)tempMap.get(licenseName);
         
         List<ServerTypes> types = m_brandCodeMap.getLimitedServerTypes(
            brandCodeMapVersion, Integer.parseInt(licenseId));
         
         if (!(types.isEmpty() || types.contains(curType)))
            continue;
         
         m_licensesId.add(licenseId);
         m_licenseCombo.addItem(licenseName);
      }

      m_licenseCombo.setSelectedIndex(-1);
      
      
   }
   
   /**
    * Set the eval and server types based on the map version, then sets the 
    * licenses.
    * 
    * @param brandCodeMapVersion The map version
    * 
    * @throws CodeException if the specified version is invalid.
    */
   private void setMapVersionData(int brandCodeMapVersion)
      throws CodeException
   {
      boolean extSupported = m_brandCodeMap.supportsExtendedProductInfo(
         brandCodeMapVersion);
      
      // set valid eval types
      m_evalType.removeAllItems();
      for (EvalTypes type : EvalTypes.values())
      {
         if (!extSupported && type.isExtendedEvalType())
            continue;
         
         m_evalType.addItem(type);
      }
      m_evalType.setSelectedItem(EvalTypes.NOT_EVAL);
      
      // set valid server types
      m_serverType.removeAllItems();
      for (ServerTypes type : ServerTypes.getSortedValues())
      {
         // do not add extended types unless supported
         if (!extSupported && type.isExtendedServerType())
            continue;
         
         m_serverType.addItem(type);
      }
      m_serverType.setSelectedItem(ServerTypes.PRODUCTION);
      setLicenses(brandCodeMapVersion);
   }

   /**
    * Sets the build numbers in the combox box depending upon the currently
    * selected Rhythmyx version.
    *
    * @throws CodeException if any error occurs obtaining the build numbers
    * for the currently selected Rhythmyx version
    */
   private void setBuildNumbers()
      throws CodeException
   {
      m_buildNumber.removeAllItems();
      int selIndex = m_rxVersion.getSelectedIndex();
      if (selIndex > -1)
      {
         String rxVersion = (String)m_rxVersion.getSelectedItem();
         Iterator itbcmv = m_brandCodeMap.getBrandCodeMapVersions();
         while (itbcmv.hasNext())
         {
            PSBrandCodeMapVersion bcmv = (PSBrandCodeMapVersion)itbcmv.next();
            int[] buildNumbers = bcmv.getRhythmyxBuildNumbers(rxVersion);
            if (buildNumbers != null)
            {
               BuildNumber bldNumber = new BuildNumber(
                  buildNumbers[0], buildNumbers[1]);
               m_buildNumber.addItem(bldNumber);
            }
         }
      }
      m_buildNumber.setSelectedIndex(-1);
   }

   /**
    * Clears the current brand code text field.
    */
   void clearCode()
   {
      m_results.setText("");
   }

   /**
    * Returns a map containing the key of the map <code>inputMap</code> as value
    * and the value of the map <code>inputMap</code> as key.
    *
    * @param inputMap the map from which the returned map should be constructed,
    * the key of this map is used as value in the returned map, and the values
    * in this map is used as key in the returned map.
    *
    * @return the map containing the value of the map <code>inputMap</code> as
    * key and the key of the map <code>inputMap</code> as value,
    * never <code>null</code>
    */
   private Map reverseMap(Map inputMap)
   {
      Iterator itMap = inputMap.entrySet().iterator();
      Map retMap = new HashMap();
      while (itMap.hasNext())
      {
         Map.Entry item = (Map.Entry)itMap.next();
         retMap.put(item.getValue(), item.getKey());
      }
      return retMap;
   }

   /**
    * Returns the set of values in the specified map. If <code>bSorted</code>
    * is <code>true</code> then the values in the returned set are sorted.
    *
    * @param inputMap the map whose values should be returned, assumed not
    * <code>null</code>
    *
    * @param bSorted if <code>true</code> then the values in the returned set
    * are sorted based on their natural order if <code>comp</code> is
    * <code>null</code> otherwise <code>comp</code> is used for sorting the
    * elements
    *
    * @param comp the comparator that will be used to sort this set. A
    * <code>null</code> value indicates that the elements' natural ordering
    * should be used
    *
    * @return the set of values obtained from the specified map, never
    * <code>null</code>
    */
   private Set getMapValues(Map inputMap, boolean bSorted, Comparator comp)
   {
      Set set = null;
      if (bSorted)
      {
         if (comp == null)
            set = new TreeSet();
         else
            set = new TreeSet(comp);
      }
      else
      {
         set = new HashSet();
      }

      Iterator itMap = inputMap.entrySet().iterator();
      while (itMap.hasNext())
      {
         Map.Entry item = (Map.Entry)itMap.next();
         set.add(item.getValue());
      }
      return set;

   }

   /**
    * Create a formatted message for messages taking an array of
    * arguments. Be sure to store the arguments in the correct order in
    * the array, where {0} in the string is array element 0, etc.
    *
    * @param key The error string to load, never<code>null</code> or empty
    *
    * @param args  The array of arguments to use as the arguments
    *    in the error message, may be <code>null</code> or empty.
    *
    * @return The formatted message, never <code>null</code>. If the appropriate
    *    message cannot be created, a message is constructed from the msgCode
    *    and args and is returned.
    *
    * @throws IllegalArgumentException if key is <code>null</code> or empty
    */
   private String createMessage(String key, Object[] args)
   {
      if ((key == null) || (key.trim().length() == 0))
         throw new IllegalArgumentException("key may not be null or empty");

      if (args == null)
         args = new Object[0];

      String msg = m_resBundle.getString(key);

      if (msg != null)
      {
         try
         {
            msg = MessageFormat.format(msg, args);
         }
         catch (IllegalArgumentException e)
         {
            // some problem with formatting
            msg = null;
         }
      }

      if (msg == null)
      {
         String sArgs = "";
         String sep = "";

         for (int i = 0; i < args.length; i++) {
            sArgs += sep + args[i].toString();
            sep = "; ";
         }

         msg = key + ": " + sArgs;
      }

      return msg;
   }

   /**
    * Displays error message in a message box.
    *
    * @param msg the error message to display, assumed not <code>null</code> or 
    * empty.
    */

   private void showErrorMessage(String msg)
   {
      msg = PSLineBreaker.wrapString(msg, 80, 25, "\n");
      String msgTitle = m_resBundle.getString("errMsgTitle");
      int msgType = JOptionPane.ERROR_MESSAGE;
      JOptionPane.showMessageDialog(m_mainPanel, msg, msgTitle, msgType);
   }

   
   /**
    * Displays warning message in a message box.
    * 
    * @param msg the warning message to display, assumed not <code>null</code>
    * or empty.
    */

   private void showWarningMessage(String msg)
   {
      msg = PSLineBreaker.wrapString(msg, 80, 25, "\n");
      String msgTitle = m_resBundle.getString("warnMsgTitle");
      int msgType = JOptionPane.WARNING_MESSAGE;
      JOptionPane.showMessageDialog(m_mainPanel, msg, msgTitle, msgType);
   }
   
   /**
    * parses the string str into an array of strings separated by comma
    * and inserts them into the vector which is returned. The reason for
    * returning a <code>Vector</code> instead of the <code>List</code>
    * interface is that the <code>JComboBox</code> has a contructor which
    * takes a <code>Vector</code> and populates the ComboBox with the list
    * of <code>String</code> in this <code>Vector</code>
    *
    * @param str string containing list of strings separated by comma, may not
    * be <code>null</code> or empty
    *
    * @return vector in which the array of strings will be inserted, never
    * <code>null</code>, may be empty
    *
    * @throws IllegalArgumentException if str is <code>null</code> or empty
    */
   private static Vector parseString(String str)
   {
      if ((str == null) || (str.trim().length() == 0))
         throw new IllegalArgumentException("str may not be null or empty");

      Vector vec = new Vector();
      StringTokenizer st = new StringTokenizer(str, ",");
      while (st.hasMoreTokens())
      {
         vec.addElement(st.nextToken());
      }
      return vec;
   }

   /**
    * Gets resource file name for this class.
    *
    * @return resource file name, never <code>null</code> or empty.
    **/
   private String getResourceName()
   {
      return getClass().getName() + "Resources";
   }

   /**
    * Utility method to load the ResourceBundle associated with this class
    */
   private void loadResourceBundle()
   {
      try
      {
         m_resBundle = ResourceBundle.getBundle(
               getResourceName(), Locale.getDefault());
      }
      catch(MissingResourceException mre)
      {
         /* no-op */
      }
   }

   /**
    * Runs the code generator.
    *
    * @param args not used, may be <code>null</code> or empty array. If
    * <code>null</code> then launches the code generator.
    * If "/p" (or "/P" or "-p" or "-P") argument is specified then a brand code
    * must be supplied. The details of the brand code such as license name,
    * part and components licensed are then printed out to the console in an
    * Xml format.
    * If invalid arguments are specified then it prints the usage.
    *
    * @throws CodeException if any error occurs parsing the component map
    * xml
    */
   public static void main(String[] args)
      throws CodeException
   {
      if ((args == null) || (args.length == 0))
      {
         try
         {
            String strLnFClass = UIManager.getSystemLookAndFeelClassName();
            LookAndFeel lnf = (LookAndFeel) Class.forName(strLnFClass).newInstance();
            UIManager.setLookAndFeel(lnf);
         }
         catch(Exception e)
         {
            System.out.println("Error : " + e.getLocalizedMessage());
            e.printStackTrace();
         }

         JFrame frame = new JFrame("");
         frame.setLocation(10000, 10000);

         CodeGenerator dlg = new CodeGenerator(frame);
         frame.setVisible(true);
         dlg.setVisible(true);
      }
      else
      {
         if ((args[0].equalsIgnoreCase("-p")) ||
            (args[0].equalsIgnoreCase("/p")))
         {
            if (args.length == 2)
            {
               String brandCode = args[1];
               try
               {
                  Code code = new Code(brandCode);
                  Document doc = PSXmlDocumentBuilder.createXmlDocument();
                  Element root = code.toXml(doc);
                  doc.appendChild(root);
                  System.out.println(PSXmlDocumentBuilder.toString(doc));
               }
               catch (Exception e)
               {
                  System.out.println("Exception : " + e.getLocalizedMessage());
               }
            }
            else
            {
               showUsage();
            }
         }
         else
         {
            showUsage();
         }
      }

      System.exit(0);
   }

   /**
    * Prints the usage of the RxCodeGenerator.exe
    */
   private static void showUsage()
   {
      System.out.println("Usage:");
      System.out.println("RxCodeGenerator.exe");
      System.out.println("RxCodeGenerator.exe /p [BRAND CODE] " +
         "- prints the license name, parts and components licensed by the " +
         "specified brand code.");
   }

   /**
    * constant for number of columns in the license properties table
    */
   private static final int PROPS_TBL_COLUMN_COUNT = 2;

   /**
    * constant for checkboxes column in the parts table
    */
   private static final int PROPS_TBL_NAME_COL = 0;

   /**
    * constant for quantity column in the parts table
    */
   private static final int PROPS_TBL_QUANTITY_COL = 1;

   /**
    * constant for number of columns in the parts table
    */
   private static final int PARTS_TBL_COLUMN_COUNT = 2;

   /**
    * constant for checkboxes column in the parts table
    */
   private static final int PARTS_TBL_CHECKBOXES_COL = 0;

   /**
    * constant for parts column in the parts table
    */
   private static final int PARTS_TBL_PARTS_COL = 1;


   /**
    * In memory representation of the Component Map Xml, initialized in the
    * constructor, never <code>null</code> after initialization.
    */
   private IPSBrandCodeMap m_brandCodeMap = null;

   /**
    * The resource bundle containing GUI resources and messages displayed
    * to the user, may be <code>null</code> if resource file is not found,
    * initially <code>null</code>, initialized in the
    * <code>loadResourceBundle</code> method.
    */
   private ResourceBundle m_resBundle;

   /**
    * Map containing the license id and license names. It is set when the
    * user selects a Rhythmyx Version or enters a build number.
    */
   private Map m_licenses = null;

   /**
    * List for storing the id of licenses displayed in the license combobox,
    * never <code>null</code>
    */
   private List m_licensesId = new ArrayList();

   /**
    * Map for storing the property id (<code>String</code>) as key and its
    * quantity (<code>String</code>) as value, initialized in the
    * <code>initData()</code> method, modified in the
    * <code>onClickGenerateCode()</code> method, never <code>null</code> after
    * initialization
    */
   private Map m_propertiesIdValueMap = null;

   /**
    * Map for storing the the id and name of required parts, initialized in the
    * constructor, never <code>null</code> after initialization
    */
   private Map m_reqParts = null;

   /**
    * Map for storing the the id and name of optional parts, initialized in the
    * constructor, never <code>null</code> after initialization
    */
   private Map m_optParts = null;

   /**
    * if <code>true</code> then gets the list of parts for the selected
    * license, otherwise does not populate the parts list.
    */
   private boolean m_bListParts = true;

   /**
    * List for storing the id of parts displayed in the parts table,
    * never <code>null</code>
    */
   private List m_partsId = new ArrayList();

   /**
    * list for storing the id of properties for the currently selected license
    */
   private List m_propertiesList = new ArrayList();

   /**
    * Stores the id of parts that has been selected by the user while generating
    * the brand codes, never <code>null</code>, may be empty.
    * It contains the id of parts as <code>String</code> objects. Modified in
    * the <code>onSelectLicense()</code> method.
    */
   private List m_selectedPartsList = new ArrayList();
   
   /**
    * The selected brand code map version, updated whenever a version is 
    * selected, <code>-1</code> when no version is selected.
    */
   private int m_selectedBcmv = -1;

   /**
    * table for parts
    */
   private JTable m_partsTable = null;

   /**
    * table for license properties
    */
   private JTable m_propsTable = null;

   /**
    * date format to use in the text field for entering the date on which the
    * brand code expires
    */
   private SimpleDateFormat m_format = null;

   /**
    * Text field for entering the date on which the brand code expires,
    * never <code>null</code>
    */
   private JTextField m_codeExpire = new JTextField();

   /**
    * Text field for displaying the generated brand code,
    * never <code>null</code>
    */
   private JTextField m_results = new JTextField();

   /**
    * Text field for entering a brand code for verification,
    * never <code>null</code>
    */
   private JTextField m_verResults = new JTextField();

   /**
    * Combox box for selecting Rhythmyx build number,
    * never <code>null</code>
    */
   private JComboBox m_buildNumber = new JComboBox();

   /**
    * Combobox for selecting Rhythmyx version,
    * never <code>null</code>
    */
   private JComboBox m_rxVersion = new JComboBox();

   /**
    * Combobox for selecting brand code map version,
    * never <code>null</code>
    */
   private JComboBox m_bcmVersion = new JComboBox();

   /**
    * Combobox for selecting license,
    * never <code>null</code>
    */
   private JComboBox m_licenseCombo = new JComboBox();

  
   /**
    * Combo box for the server types
    */
   private JComboBox m_serverType = null;


   /**
    * Button for generating the brand code,
    * initialized in the constructor, never <code>null</code> after initialization
    */
   private JButton m_generate = null;

   /**
    * Button for verifying the brand code,
    * initialized in the constructor, never <code>null</code> after initialization
    */
   private JButton m_verify = null;
   
   /**
    * ComboBox for selecting the eval type, never <code>null</code>.
    */
   private JComboBox m_evalType = new JComboBox();

   /**
    * RadioButton corresponding to use Rhythmyx version and build number
    * for determing the brand code map to use, never <code>null</code>
    */
   private JRadioButton m_rbUseRxVerBuild = new JRadioButton();

   /**
    * RadioButton corresponding to use the specified brand code map version for
    * generating the brand code, never <code>null</code>
    */
   private JRadioButton m_rbUseBrandCodeMapVer = new JRadioButton();
   
   /**
    * Main panel of this app, initialized in {@link #layoutPanels()}, never
    * <code>null</code> or modified after that.
    */
   private JPanel m_mainPanel;

   /**
    * This class is used to populate the build number combo box.
    */
   public class BuildNumber
   {
      /**
       * Construct this object from the specified minimum and maximum supported
       * build numbers
       *
       * @param buildFrom the minimum supported build number, should either
       * be <code>-1</code> or a 8 digit number
       * @param buildTo the maximum supported build number, should either be
       * <code>-1</code> or a 8 digit number. If not <code>-1</code>, then
       * should be greater than <code>buildFrom</code>
       *
       * @throws IllegalArgumentException if <code>buildFrom</code> or
       * <code>buildTo</code> is invalid
       */
      public BuildNumber(int buildFrom, int buildTo)
      {
         // do the validations
         if ((buildFrom != -1) && (("" + buildFrom).length() != 8))
            throwException(buildFrom, buildTo);

         if ((buildTo != -1) && (("" + buildTo).length() != 8))
            throwException(buildFrom, buildTo);

         if ((buildFrom > buildTo) && (buildTo != -1))
            throwException(buildFrom, buildTo);

         m_buildFrom = buildFrom;
         m_buildTo = buildTo;
      }

      /**
       * Returns the minimum supported build number.
       *
       * @return the minimum supported build number, either <code>-1</code>
       * or a 8 digit number
       */
      public int getBuildFrom()
      {
         return m_buildFrom;
      }

      /**
       * Returns the maximum supported build number.
       *
       * @return the maximum supported build number, either <code>-1</code>
       * or a 8 digit number
       */
      public int getBuildTo()
      {
         return m_buildTo;
      }

      /**
       * The <code>String</code> representation of the object follows
       * these rules.
       * <p>
       * If "buildFrom" is <code>-1</code> then the string is of the form
       * "buildTo or lesser" (eg "20030601 or lesser").
       * If "buildTo" is <code>-1</code> then the string is of the form
       * "buildTo or greater" (eg "20030701 or greater").
       * If none of "buildFrom" and "buildTo" equals <code>-1</code> then the
       * string is of the form "buildFrom - buildTo" (eg "20030301 - 20030401")
       * If both "buildFrom" and "buildTo" equal <code>-1</code> then the
       * string is of the form "All Builds".
       *
       * @return the string representation of this object,
       * never <code>null</code> or empty
       */
      @Override
      public String toString()
      {
         String str = "";
         if ((m_buildFrom == -1) && (m_buildTo == -1))
            str = "All Builds";
         else if ((m_buildFrom == -1) && (m_buildTo != -1))
            str = m_buildTo + " or lesser";
         else if ((m_buildFrom != -1) && (m_buildTo == -1))
            str = m_buildFrom + " or greater";
         else
            str = m_buildFrom + " - " + m_buildTo;

         return str;
      }

      /**
       * Throws <code>IllegalArgumentException</code> using the specified
       * build numbers.
       *
       * @param buildFrom the minimum supported build number
       * @param buildTo the maximum supported build number
       *
       * @throws IllegalArgumentException always throws this exception
       */
      private void throwException(int buildFrom, int buildTo)
      {
         throw new IllegalArgumentException(
            "Invalid buildFrom (" + buildFrom + ") or buildTo (" + buildTo +
            ") specified.");
      }

      /**
       * the minimum supported build number, should either be <code>-1</code>
       * or a 8 digit number, initialized in the ctor, never modified after
       * that.
       */
      private int m_buildFrom;

      /**
       * the maximum supported build number, should either be <code>-1</code>
       * or a 8 digit number, initialized in the ctor, never modified after
       * that.
       */
      private int m_buildTo;

   }

   /**
    * Used for sorting licenses based on their id.
    */
   class LicenseComparator implements Comparator
   {
      /**
       * Construct the comparator from the license map for a particular
       * brand code map version.
       *
       * @param licenseNameToIdMap Map of licenses, the license name
       * (<code>String</code>) is the key and the id of the license
       * (<code>String</code>) is the value, may not be <code>null</code>
       *
       * @throws IllegalArgumentException if <code>licenseNameToIdMap</code> is
       * <code>null</code>
       */
      LicenseComparator(Map licenseNameToIdMap)
      {
         if (licenseNameToIdMap == null)
            throw new IllegalArgumentException(
               "licenseNameToIdMap may not be null");
         m_licenseNameToIdMap = licenseNameToIdMap;
      }

      /**
       * See Comparator interface.
       * @see java.util.Comparator
       *
       * @throws IllegalArgumentException if <code>obj1</code> or
       * <code>obj2</code> is not contained in the map specified as argument in
       * the constructor, or if they are not <code>String</code> objects.
       */
      public int compare(Object obj1, Object obj2)
      {
         if (!(obj1 instanceof java.lang.String))
            throw new IllegalArgumentException("obj1 is not a String object");
         if (!(obj2 instanceof java.lang.String))
            throw new IllegalArgumentException("obj2 is not a String object");

         String strId1 = (String)m_licenseNameToIdMap.get(obj1);
         if (strId1 == null)
            throw new IllegalArgumentException(
               "Invalid license name : " + obj1);

         String strId2 = (String)m_licenseNameToIdMap.get(obj2);
         if (strId2 == null)
            throw new IllegalArgumentException(
               "Invalid license name : " + obj2);

         try
         {
            int id1 = PSBrandCodeUtil.toInt(strId1,
               "Invalid id : " + strId1 + " for license : " + obj1);

            int id2 = PSBrandCodeUtil.toInt(strId2,
               "Invalid id : " + strId2 + " for license : " + obj2);

            int retVal = 0;
            if (id1 > id2)
               retVal = 1;
            else if (id1 < id2)
               retVal = -1;

            return retVal;
         }
         catch (CodeException ex)
         {
            throw new RuntimeException(ex.getLocalizedMessage());
         }
      }

      /**
       * Map of licenses, the license name (<code>String</code>) is the key and
       * the id of the license (<code>String</code>) is the value,
       * initialized in the constructor, never <code>null</code> or modified
       * after that.
       */
      private Map m_licenseNameToIdMap = null;
   }

   /**
    * Implements the "TableModel" interface by inheriting from
    * "AbstractTableModel". Implemenets the  methods the JTable
    * will use to interrogate a tabular data model.
    */
   class PartsTableModel extends AbstractTableModel
   {
      /**
       * Returns the number of columns in the model.
       *
       * @return the number of columns in the model
       */
      public int getColumnCount()
      {
         return m_columnNames.size();
      }

      /**
       * Returns the number of rows in the model.
       *
       * @return the number of rows in the model
       */
      public int getRowCount()
      {
         int size = m_checkBoxes.size();
         if (m_parts.size() == size)
            return size;
         return 0;
      }

      /**
       * Returns the name of the specified column.
       *
       * @param col the index of the column
       *
       * @return the name of the column, may be <code>null</code> if col
       * is greater than or equal to the number of columns in the table
       */
      @Override
      public String getColumnName(int col)
      {
         if (col >= m_columnNames.size())
            return null;
         return (String)m_columnNames.elementAt(col);
      }

      /**
       * Returns the value for the cell at the specified row and column.
       *
       * @param row the row whose value is to be queried
       * @param col the column whose value is to be queried
       *
       * @return the value Object at the specified cell, may be
       * <code>null</code> if this row and column does not exist in the table
       */
      public Object getValueAt(int row, int col)
      {
         switch(col)
         {
            case PARTS_TBL_CHECKBOXES_COL:
               if (row < m_checkBoxes.size())
                  return m_checkBoxes.get(row);
               else
                  return null;

            case PARTS_TBL_PARTS_COL:
               if (row < m_parts.size())
                  return m_parts.get(row);
               else
                  return null;
         }
         return null;
      }

      /**
       * Returns the most specific superclass for all the cell values in
       * the column.
       *
       * @param c the index of the column
       *
       * @return the common ancestor class of the object values in the model,
       * returns <code>null</code> if this column does not exist
       */
      @Override
      public Class getColumnClass(int c)
      {
         switch(c)
         {
            case PARTS_TBL_CHECKBOXES_COL:
               return (new Boolean(true)).getClass();

            case PARTS_TBL_PARTS_COL:
               return (new String("")).getClass();
         }
         return null;
      }

      /**
       * Returns true if the cell at the specified row and column is editable.
       *
       * @param row the row whose value to be queried
       * @param col the column whose value to be queried
       *
       * @return true if the cell is editable
       */
      @Override
      public boolean isCellEditable(int row, int col)
      {
         switch(col)
         {
            case PARTS_TBL_CHECKBOXES_COL:
               if (row > m_disabledRowIndex)
                  return true;
               return false;

            case PARTS_TBL_PARTS_COL:
               return false;

            default:
               return false;
         }
      }

      /**
       * Sets the value in the cell at columnIndex and rowIndex to value.
       *
       * @param value the new value, may not be <code>null</code>
       * @param row the row whose value is to be changed
       * @param col the column whose value is to be changed
       *
       * @throws IllegalArgumentException if value is <code>null</code>
       */
      @Override
      public void setValueAt(Object value, int row, int col)
      {
         if (value == null)
            throw new IllegalArgumentException("value may not be null");

         clearCode();
         switch(col)
         {
            case PARTS_TBL_CHECKBOXES_COL:
               if (row >= m_checkBoxes.size())
               {
                  m_checkBoxes.add(value);
                  fireTableRowsInserted(0,1);
               }
               else
               {
                  m_checkBoxes.set(row, value);
                  fireTableCellUpdated(row, col);
               }
               break;

            case PARTS_TBL_PARTS_COL:
               if (row >= m_parts.size())
               {
                  m_parts.add(value);
                  fireTableRowsInserted(0,1);
               }
               else
               {
                  m_parts.set(row, value);
                  fireTableCellUpdated(row, col);
               }
               break;
         }
      }

      /**
       * Deletes all the rows in the table.
       */
      public void deleteAllRows()
      {
         int lastRow = m_parts.size();
         if(lastRow > 0)
         {
            m_checkBoxes.clear();
            m_parts.clear();
            fireTableRowsDeleted(0, lastRow-1);
         }
      }

      /**
       * Makes the first <code>rowIndex+1</code> rows non-editable.
       * @param rowIndex the index of the last row to make non-editable
       */
      public void disableRows(int rowIndex)
      {
         m_disabledRowIndex = rowIndex;
      }

      /**
       * Returns the id of parts which has been selected by the user.
       *
       * @return the id of parts which has been selected by the user,
       * never <code>null</code>,
       * may be empty if no part has been selected
       */
      public List getSelectedPartsId()
      {
         List parts = new ArrayList();
         int size = getRowCount();
         for (int i = 0; i < size; i++)
         {
            Boolean b = (Boolean)m_checkBoxes.get(i);
            if(b.equals(Boolean.TRUE))
            {
               parts.add(m_partsId.get(i));
            }
         }
         return parts;
      }

      /**
       * List of column names, never <code>null</code> or empty.
       */
      Vector m_columnNames = parseString(
         m_resBundle.getString("partsTblcolumnHeaders"));

      /**
       * <code>List</code> of <code>boolean</code> values. The value of an
       * element in this <code>List</code> is <code>true</code> if the user
       * has selected the corresponding part.
       */
      List m_checkBoxes = new ArrayList();

      /**
       * parts obtained from the "ComponentMap.xml" for the selected license,
       * never <code>null</code> or empty, because each license will have
       * atleast one part.
       */
      List m_parts = new ArrayList();

      /**
       * the index of the last row to make non-editable
       */
      int m_disabledRowIndex = -1;

   }

   /**
    * Implements the "TableModel" interface by inheriting from
    * "AbstractTableModel". Implemenets the  methods the JTable
    * will use to interrogate a tabular data model.
    */
   class PropertiesTableModel extends AbstractTableModel
   {
      /**
       * Returns the number of columns in the model.
       *
       * @return the number of columns in the model
       */
      public int getColumnCount()
      {
         return m_columnNames.size();
      }

      /**
       * Returns the number of rows in the model.
       *
       * @return the number of rows in the model
       */
      public int getRowCount()
      {
         return m_properties.size();
      }

      /**
       * Returns the name of the specified column.
       *
       * @param col the index of the column
       *
       * @return the name of the column, may be <code>null</code> if col
       * is greater than or equal to the number of columns in the table
       */
      @Override
      public String getColumnName(int col)
      {
         if (col >= m_columnNames.size())
            return null;
         return (String)m_columnNames.elementAt(col);
      }

      /**
       * Returns the value for the cell at the specified row and column.
       *
       * @param row the row whose value is to be queried
       * @param col the column whose value is to be queried
       *
       * @return the value Object at the specified cell, may be
       * <code>null</code> if this row and column does not exist in the table
       */
      public Object getValueAt(int row, int col)
      {
         switch(col)
         {
            case PROPS_TBL_NAME_COL:
               if (row < m_properties.size())
                  return m_properties.get(row);
               else
                  return null;

            case PROPS_TBL_QUANTITY_COL:
               if (row < m_quantities.size())
                  return m_quantities.get(row);
               else
                  return null;
         }
         return null;
      }

      /**
       * Returns the most specific superclass for all the cell values in
       * the column.
       *
       * @param c the index of the column
       *
       * @return the common ancestor class of the object values in the model,
       * returns <code>null</code> if this column does not exist
       */
      @Override
      public Class getColumnClass(int c)
      {
         switch(c)
         {
            case PROPS_TBL_NAME_COL:
               return (new String("")).getClass();

            case PROPS_TBL_QUANTITY_COL:
               return (new String("")).getClass();
         }
         return null;
      }

      /**
       * Returns true if the cell at the specified row and column is editable.
       *
       * @param row the row whose value to be queried
       * @param col the column whose value to be queried
       *
       * @return true if the cell is editable
       */
      @Override
      public boolean isCellEditable(int row, int col)
      {
         if (col == PROPS_TBL_QUANTITY_COL)
            return true;
         return false;
      }

      /**
       * Returns map containing the id of property as key and quantity as
       * value.
       *
       * @return map containing the id of property as key and quantity as
       * value, never <code>null</code>.
       */
      public Map getProperties()
      {
         Map props = new HashMap();
         int size = getRowCount();
         for (int i = 0; i < size; i++)
         {
            String propId = (String)m_propertiesList.get(i);
            String propValue = (String)m_quantities.get(i);
            props.put(propId, propValue);
         }
         return props;
      }

      /**
       * Sets the value in the cell at columnIndex and rowIndex to value.
       *
       * @param value the new value, may not be <code>null</code>
       * @param row the row whose value is to be changed
       * @param col the column whose value is to be changed
       *
       * @throws IllegalArgumentException if value is <code>null</code>
       */
      @Override
      public void setValueAt(Object value, int row, int col)
      {
         if (value == null)
            throw new IllegalArgumentException("value may not be null");

         clearCode();
         switch(col)
         {
            case PROPS_TBL_NAME_COL:
               if (row >= m_properties.size())
               {
                  m_properties.add(value);
                  fireTableRowsInserted(0,1);
               }
               else
               {
                  m_properties.set(row, value);
                  fireTableCellUpdated(row, col);
               }
               break;

            case PROPS_TBL_QUANTITY_COL:
               if (row >= m_quantities.size())
               {
                  m_quantities.add(value);
                  fireTableRowsInserted(0,1);
               }
               else
               {
                  m_quantities.set(row, value);
                  fireTableCellUpdated(row, col);
               }
               break;
         }
      }

      /**
       * Deletes all the rows in the table.
       */
      public void deleteAllRows()
      {
         int lastRow = m_properties.size();
         if(lastRow > 0)
         {
            m_properties.clear();
            m_quantities.clear();
            fireTableRowsDeleted(0, lastRow-1);
         }
      }

      /**
       * List of column names, never <code>null</code> or empty.
       */
      Vector m_columnNames = parseString(
         m_resBundle.getString("propsTblcolumnHeaders"));

      /**
       * Stores the properties, never <code>null</code>
       */
      List m_properties = new ArrayList();

      /**
       * Stores the quantity of each property, never <code>null</code>
       */
      List m_quantities = new ArrayList();

   }
}