/*[ ActionsPropertiesPanel.java ]**************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer.browser;

import com.percussion.cms.objectstore.PSAction;
import com.percussion.cms.objectstore.PSActionProperties;
import com.percussion.cms.objectstore.PSActionProperty;
import com.percussion.guitools.PropertyTablePanel;

import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;


/**
 * This panel allows designers to add any property to a menu action.
 */
public class ActionsPropertiesPanel extends PropertyTablePanel
{
   /**
    * Constructs the panel.
    *
    * @param colNames, a string array of column names, may not be <code>null
    * </code> or empty ands should have only two entries spcifying the two
    * column names.
    *
    * @param rows, number of rows in the table
    */
   public ActionsPropertiesPanel(String[] colNames, int rows)
   {
      super(colNames, rows, true);
      setScrollPaneSize(new Dimension(0, 100));
   }


   public boolean update(Object data, boolean isLoad)
   {
      PSAction action = null;
      if (data instanceof PSAction)
      {
         action = (PSAction)data;
         PSActionProperties props = action.getProperties();
         Iterator itr = props.iterator();
         PSActionProperty prop = null;
         Vector vec = null;
         DefaultTableModel model = (DefaultTableModel)getTableModel();
         if (isLoad)
         {
            clearAllRows();
            while(itr.hasNext())
            {
               prop = (PSActionProperty)itr.next();
               if (toBeDisplayed(prop.getName()))
               {
                  vec = new Vector();
                  vec.add(0, prop.getName());
                  vec.add(1, prop.getValue());
                  model.addRow(vec);
               }
            }
            if (model.getRowCount() == 0)
               addRow();
         }
         else
         {
            stopExtTableEditing();
            //save deleted rows
            int sz = model.getRowCount();
            String name = "";
            String value = "";
            List newProps = new ArrayList();
            for (int k = 0; k < sz; k++)
            {
               name = (String)model.getValueAt(k, 0);
               value = (String)model.getValueAt(k, 1);
               if (name == null || name.trim().length() == 0)
                  continue;//todo: remove from model

               newProps.add(name.toLowerCase());
               if (toBeDisplayed(name))
               {
                  //todo: errmsg: Can't use reserved name
                  System.out.println("Attempt to use reserved property name.");
               }
               props.setProperty(name, value);
            }

            //remove all entries in properties collection that user removed
            Iterator existingProps = props.iterator();
            while (existingProps.hasNext())
            {
               prop = (PSActionProperty) existingProps.next();
               name = prop.getName().toLowerCase();
               if (!newProps.contains(name) && toBeDisplayed(name))
               {
                  props.remove(prop);
                  existingProps = props.iterator();
               }
            }
         }
      }
      return true;
   }

   private boolean toBeDisplayed(String str)
   {
      if (str.equalsIgnoreCase(PSAction.PROP_ACCEL_KEY))
         return false;
      else if (str.equalsIgnoreCase(PSAction.PROP_LAUNCH_NEW_WND))
         return false;
      else if (str.equalsIgnoreCase(PSAction.PROP_MNEM_KEY))
         return false;
      else if (str.equalsIgnoreCase(PSAction.PROP_MUTLI_SELECT))
         return false;
      else if (str.equalsIgnoreCase(PSAction.PROP_REFRESH_HINT))
         return false;
      else if (str.equalsIgnoreCase(PSAction.PROP_SHORT_DESC))
         return false;
      else if (str.equalsIgnoreCase(PSAction.PROP_SMALL_ICON))
         return false;
      return true;
   }

   public boolean validateData()
   {
      return true;
   }

   //test code
 /**  public static void main(String[] arg)
   {
      JFrame f = new JFrame("BoxLayoutDemo");
      Container contentPane = f.getContentPane();
      ActionsPropertiesPanel ac = new ActionsPropertiesPanel(new String[]
               {"Name", "Value"}, 1);
      contentPane.add(ac, BorderLayout.CENTER);
      f.addWindowListener(new WindowAdapter() {
         public void windowClosing(WindowEvent e) {
            System.exit(0);
         }
      });
      f.pack();
      f.setVisible(true);
   }*/
   //end
}