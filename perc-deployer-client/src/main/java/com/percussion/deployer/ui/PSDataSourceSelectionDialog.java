/******************************************************************************
 *
 * [ PSDataSourceSelectionDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.deployer.ui;

import com.percussion.deployer.objectstore.PSDatasourceMap;
import com.percussion.deployer.objectstore.PSDbmsMapping;
import com.percussion.guitools.PSDialog;
import com.percussion.guitools.PSPropertyPanel;
import com.percussion.validation.StringConstraint;
import com.percussion.validation.ValidationConstraint;

import javax.swing.*;
import java.awt.*;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * The dialog for adding or editing DBMS credentials.
 */
public class PSDataSourceSelectionDialog extends  PSDialog
{
   /**
    * Construct this class with all required parameters.
    *
    * @param parent the parent window of this dialog, may be <code>null</code>.
    *
    * @param sourceDS  may be code>null</code> if a new dbms entry is being
    * added, but if an entry is being edited it cannot be code>null</code>.
    *
    * @param targetDS  may be code>null</code> if a new dbms entry is being
    * added, but if an entry is being edited it cannot be code>null</code>.

    * @param srcDatasrcs the list of source datasources as <code>String</code>s 
    * to choose from, may not be <code>null</code> or empty.
    *
    * @param tgtDatasrcs the list of target datasources as <code>String</code>s 
    * to choose from, may not be <code>null</code> or empty.
    *
    * @param dbmsMappings the existing mappings
    * @throws IllegalArgumentException if any of the required parameters
    * are invalid.
    */
   public PSDataSourceSelectionDialog(Dialog parent, String sourceDS, 
         String targetDS, List srcDatasrcs, List tgtDatasrcs,  
         ArrayList<PSDbmsMapping> dbmsMappings)
   {
      super(parent);

      if ( srcDatasrcs == null )
         throw new IllegalArgumentException("Source Datasource list cannot be null");
      if ( srcDatasrcs.isEmpty() && sourceDS != null )
         srcDatasrcs.add(sourceDS);

      if ( srcDatasrcs.isEmpty() )
         throw new IllegalArgumentException("Source Datasource list cannot be empty");
      
      if ( tgtDatasrcs == null || tgtDatasrcs.isEmpty() )
         throw new IllegalArgumentException("Target Datasource list cannot be empty");
      
      setSrcDatasource(sourceDS);
      setTgtDatasource(targetDS);
      m_srcDatasources = srcDatasrcs;
      m_tgtDatasources = tgtDatasrcs;
      m_dbmsMappings   = dbmsMappings;
      initDialog();
   }

   /**
    * Initializes the dialog framework.
    */
   private void initDialog()
   {
      setTitle(getResourceString("title"));
      JPanel mainpanel = new JPanel();

      mainpanel.setLayout(new BoxLayout(mainpanel, BoxLayout.Y_AXIS));
      mainpanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

      PSPropertyPanel bodyPanel = new PSPropertyPanel();

      m_srcCombo = new JComboBox(m_srcDatasources.toArray());
      JComponent[] src = {m_srcCombo};
      bodyPanel.addPropertyRow(getResourceString("source"), src);
      
      m_tgtCombo = new JComboBox(m_tgtDatasources.toArray());
      JComponent[] tgt = {m_tgtCombo};
      bodyPanel.addPropertyRow(getResourceString("target"), tgt);

      if ( getSrcDatasource() != null )
         m_srcCombo.setSelectedItem((Object)getSrcDatasource());
      if ( getTgtDatasource() != null )
         m_tgtCombo.setSelectedItem((Object)getTgtDatasource());
      
      mainpanel.add(bodyPanel);
      JPanel cmdPanel = new JPanel();
      cmdPanel.add(Box.createHorizontalGlue());
      cmdPanel.add(createCommandPanel(SwingConstants.HORIZONTAL, true));
      cmdPanel.add(Box.createHorizontalGlue());
      mainpanel.add(cmdPanel);
      getContentPane().add(mainpanel);
      init();
      center();
      setResizable(true);
      pack();
   }

   /**
    * Updates the dialog with existing dbms information.
    */
   private void init()
   {
      if (m_srcDatasource != null)
         m_srcCombo.setSelectedItem(m_srcDatasource);
      if (m_tgtDatasource != null)
         m_srcCombo.setSelectedItem(m_tgtDatasource);   

   }

  /**
   * Creates the validation framework and sets it in the parent dialog. Sets
   * the following validations.
   * <ol>
   * <li>server field is not empty </li>
   * </ol>
   */
   private void initValidationFramework()
   {
      List<JComboBox> components = new ArrayList<JComboBox>();
      List<StringConstraint> constraints = new ArrayList<StringConstraint>();
      components.add(m_srcCombo);
      constraints.add((StringConstraint)new StringConstraint());
      setValidationFramework(
        (Component[])components.toArray(new Component[components.size()]),
        (ValidationConstraint[])constraints.toArray(
         new ValidationConstraint[constraints.size()]));
   }

   /**
    *  Calls super's <code>onOk()</code> to dispose off the dialog.
    */
   public void onOk()
   {
      setSrcDatasource((String)m_srcCombo.getSelectedItem());
      setTgtDatasource((String)m_tgtCombo.getSelectedItem());
      PSDbmsMapping nMap = new PSDbmsMapping(
            new PSDatasourceMap(getSrcDatasource(), getTgtDatasource()));
      if (isMappingExists(nMap) )
      {
         JOptionPane.showMessageDialog(this,
               MessageFormat.format(getResourceString("usrErrMsg"), 
                     new Object[] {getSrcDatasource(), getTgtDatasource()}),
               getResourceString("errTitle"), JOptionPane.ERROR_MESSAGE);
               return;
      }
      else   
         super.onOk();
   }

   /**
    * 
    * @param nMap checks if the mapping already exists 
    * @return
    */
   private boolean isMappingExists(PSDbmsMapping nMap)
   {
      return m_dbmsMappings.contains((Object)nMap);
   }
   /**
    * Gets a new or edited Dbms credential.
    * If a <code>null</code> <code>PSDbmsInfo</code> object is supplied to the
    * ctor then a new one is created or else the supplied one is edited.
    * @return dbms credential, never <code>null</code>.
    */
   public PSDatasourceMap getDatasourceMap()
   {
      return new PSDatasourceMap(getSrcDatasource(), getTgtDatasource());
   }

   /**********************************************************
    *          ACCESSORS AND MUTATORS                        *
    **********************************************************/
   
   /**
    * accessor for getting the source datasource
    */
   public String getSrcDatasource()
   {
      return m_srcDatasource;
   }

   /**
    * mutator to set the source datasource may be <code>null</code>
    * @param srcDatasource
    */
   public void setSrcDatasource(String srcDatasource)
   {
      m_srcDatasource = srcDatasource;
   }

   /**
    * Accessor to get target datasource
    * @return
    */
   public String getTgtDatasource()
   {
      return m_tgtDatasource;
   }

   /**
    * Sets the target datasource may be <code>null</code>
    * @param tgtDatasource
    */
   public void setTgtDatasource(String tgtDatasource)
   {
      m_tgtDatasource = tgtDatasource;
   }
   
   
   /**
    * List of datasources, which are <code>String</code> objects, initialized in
    * the constructor and never <code>null</code> or modified after that.
    */
   private List m_srcDatasources;
   
   /**
    * List of target datasources that are available
    */
   private List m_tgtDatasources;
   
   /**
    * The dbmsMappingsd that are already existing, new ones will be added to 
    * this list
    */
   private ArrayList<PSDbmsMapping> m_dbmsMappings;
   
   /**
    * a new mapping can be described with src and target datasources as a 
    * PSDbmsMapping
    */
   private String m_srcDatasource;
   
   /**
    * target datasource as a string. This is other half of the DBMSMapping
    */
   private String m_tgtDatasource;

   /**
    * Holds list of driver names. Initialised in <code>initDialog()</code>,
    * never <code>null</code> or empty after that. Not modified.
    */
   private JComboBox m_srcCombo;
   
   
   /**
    * A combo box to display the target datasources
    */
   private JComboBox m_tgtCombo;
}
