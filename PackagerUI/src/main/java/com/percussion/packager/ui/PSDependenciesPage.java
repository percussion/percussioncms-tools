/******************************************************************************
 *
 * [ PSDependenciesPage.java ]
 * 
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.packager.ui;

import com.percussion.packager.ui.data.PSPackageDependency;
import com.percussion.packager.ui.data.PSPackageDescriptorMetaInfo;
import com.percussion.packager.ui.model.PSPackagerClientModel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author erikserating
 *
 */
public class PSDependenciesPage extends JPanel implements IPSPage
{
   
   public PSDependenciesPage()
   {
      init();
   }
   
   private void init()
   {
      MigLayout layout = new MigLayout("fill", "[]", "[top]");
      setLayout(layout);
      this.add(getMainPanel(), "grow");
   }
   
   /**
    *  Bottom Panel
    */
   private JPanel getMainPanel()
   {
      MigLayout layout = new MigLayout(
            "fill, wrap 1",
            "[]",
            "[]20[][]");
      
      JPanel mainPanel = new JPanel();
      mainPanel.setLayout(layout);
      
      m_titleLabel = new JLabel(getResourceString("title"));
      
      mainPanel.add(m_titleLabel, "span");
      mainPanel.add(getTopPanel(), "growy 50, growx");
      mainPanel.add(getBottomPanel(), "growy 50, growx");
      return mainPanel;
   }
   
   /**
    *  Top Panel
    */
   private JPanel getTopPanel()
   {
      MigLayout layout = new MigLayout(
            "fill, wrap 1",
            "[]",
            "[]");
      
      JPanel mainPanel = new JPanel();
      mainPanel.setLayout(layout);
      



      // show list
      m_topPkgList.setEnabled(false);

      JScrollPane scroller =
          new JScrollPane (m_topPkgList,
                          ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                          ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
      JLabel heading = new JLabel(" " + getResourceString("label.reqdep"));
      heading.setBackground(Color.GRAY);
      heading.setOpaque(true);
      heading.setPreferredSize(new Dimension(2000, 20));
      scroller.setColumnHeaderView(heading);
      JLabel corner = new JLabel();
      corner.setBackground(Color.GRAY);
      corner.setOpaque(true);
      scroller.setCorner(JScrollPane.UPPER_RIGHT_CORNER, corner);    
      
      mainPanel.add(scroller,  "h 50:150, grow");
      return mainPanel;
   }
   
   /**
    *  Bottom Panel
    */
   private JPanel getBottomPanel()
   {
      MigLayout layout = new MigLayout(
            "fill, wrap 1",
            "[]",
            "[]");
      
      JPanel mainPanel = new JPanel();
      mainPanel.setLayout(layout);
      



      // show list
      JScrollPane scroller =
          new JScrollPane (m_bottomPkgList,
                          ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                          ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
      JLabel heading = new JLabel(" " + getResourceString("label.optdep"));
      heading.setBackground(Color.GRAY);
      heading.setOpaque(true);
      heading.setPreferredSize(new Dimension(2000, 20));
      scroller.setColumnHeaderView(heading);
      JLabel corner = new JLabel();
      corner.setBackground(Color.GRAY);
      corner.setOpaque(true);
      scroller.setCorner(JScrollPane.UPPER_RIGHT_CORNER, corner);
      
      mainPanel.add(scroller, "h 50:150, grow");
      return mainPanel;
   }

   /* (non-Javadoc)
    * @see com.percussion.packager.ui.IPSPage#load(java.lang.Object)
    */
   public void load(final PSPackagerClientModel model)
   {  
            
      PSPackageDescriptorMetaInfo info = model.getDescriptorMetaInfo();
      m_titleLabel.setText(
         getResourceString("title") + " [" + info.getName() + "]");
      
            
      List<PSPackageDependency> pkgDeps = model.getPackageDependencies();
      List<JCheckBox> topList = new ArrayList<JCheckBox>();
      List<JCheckBox> bottomList = new ArrayList<JCheckBox>();
            
      
      if (pkgDeps != null && !pkgDeps.isEmpty())
      {
         Iterator<PSPackageDependency> pkgDepsIt = pkgDeps.iterator();
        
         while(pkgDepsIt.hasNext())
         {
            PSPackageDependency pkgDep = pkgDepsIt.next();
            if(!model.getDescriptorMetaInfo().getName().equalsIgnoreCase(
                  pkgDep.getPackageName())) //remove self from list
            {
               PSCheckBox checkbox = new PSCheckBox(pkgDep);
               if(pkgDep.isImpliedDependency())
               {
                  //Required PKG List Top Panel
                  checkbox.setEnabled(false);
                  checkbox.setSelected(true);
                  topList.add(checkbox);
               }
               else
               {
                  //Optional PKG List Bottom Panel
                  checkbox.setSelected(pkgDep.isSelected());
                  bottomList.add(checkbox);
                  checkbox.addItemListener(new ItemListener()
                     {
                        public void itemStateChanged(ItemEvent event)
                        {                           
                           PSCheckBox cbox = (PSCheckBox)event.getSource();
                           cbox.getPkgDepend().setSelected(cbox.isSelected());
                           model.setAsDirty();
                        }                     
                     });
               }
            }

         }
         m_topPkgList.setListData(topList.toArray());
         m_bottomPkgList.setListData(bottomList.toArray());
         
      }
   }

   /* (non-Javadoc)
    * @see com.percussion.packager.ui.IPSPage#update(java.lang.Object)
    */
   public void update(@SuppressWarnings("unused")
      PSPackagerClientModel model)
   {
     // no-op
      
   }

   /* (non-Javadoc)
    * @see com.percussion.packager.ui.IPSPage#validateData()
    */
   public List<String> validateData()
   {
      //No Validation
      return null;
   }
   
   /**
    * Get resource text
    * 
    * @param key
    * @return text
    */
   private String getResourceString(String key)
   {
      return PSResourceUtils.getResourceString(this.getClass(), key);
   }
   
   /**
    * Extended JCheckBox class to add the ability
    * to hold a reference to the <code>PSPackageDependency</code>
    * object passed in.
    */
   class PSCheckBox extends JCheckBox
   {

      /**
       * 
       */
      public PSCheckBox(PSPackageDependency depend)
      {
         super();
         mi_depend = depend;
         setText(depend.getPackageName() + 
            " (" + depend.getPackageVersion() + ")");
      }
      
      public PSPackageDependency getPkgDepend()
      {
         return mi_depend;
      }
      
      private PSPackageDependency mi_depend;
      
   }
   
   /**
    * Title label.
    */
   private JLabel m_titleLabel;

   /*
    * Top CheckBoxJList
    */
   PSSimpleCheckBoxList m_topPkgList = new PSSimpleCheckBoxList();   
      
   /*
    * Bottom CheckBoxJList
    */
   PSSimpleCheckBoxList m_bottomPkgList = new PSSimpleCheckBoxList();
   
   

   
}
