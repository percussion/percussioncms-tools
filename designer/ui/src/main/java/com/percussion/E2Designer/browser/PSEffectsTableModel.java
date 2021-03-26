/******************************************************************************
 *
 * [ PSEffectsTableModel.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.E2Designer.browser;

import com.percussion.E2Designer.OSExtensionCall;
import com.percussion.design.objectstore.PSConditionalEffect;
import com.percussion.design.objectstore.PSExtensionCall;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.guitools.PSTableModel;
import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * Table model that represents 'Effects' of the relationship configuration.
 */
public class PSEffectsTableModel extends PSTableModel
{
   /**
    * Endpoint column index.
    */
   public static final int COL_EXT_ENDPOINT = 0;
   /**
    * Extension name column index.
    */
   public static final int COL_EXT_NAME = 1;
   /**
    * Extension name column index.
    */
   public static final int COL_EXT_COND = 2;

   /**
    * Constructs this model with supplied parameters.
    *
    * @param extensions the list of extensions to edit/view, may not be
    * <code>null</code>.
    */
   public PSEffectsTableModel(Iterator extensions)
   {
      if(extensions == null)
         throw new IllegalArgumentException("extensions may not be null.");

      Vector<Vector<Object>> data = buildEffectsData(extensions);

      setDataVector(data, getColumnNames());

      //manages to have minimum number of rows in model.
      if(data.size() < PSRelationshipEditorDialog.MIN_ROWS)
         setNumRows(PSRelationshipEditorDialog.MIN_ROWS);
   }

   private Vector<Vector<Object>> buildEffectsData(Iterator extensions)
   {
      Vector<Vector<Object>> data = new Vector<Vector<Object>>();
      while(extensions.hasNext())
      {
         Object obj = extensions.next();
         Vector<Object> element = new Vector<Object>();
         data.add(element);

         if(!(obj instanceof PSConditionalEffect))
         {
            throw new IllegalArgumentException(
               "all elements in effects must be instances of" +
               " PSConditionalEffect");
         }

         PSConditionalEffect effect = (PSConditionalEffect) obj;
         final String endpoint = effect.getActivationEndPoint();

         if (StringUtils.isNotBlank(endpoint))
            element.add(translateEndpointName(endpoint, true));
         else
            element.add(translateEndpointName(
                  PSRelationshipConfig.ACTIVATION_ENDPOINT_OWNER, true)); //default

         element.add( new OSExtensionCall(effect.getEffect()) );
         element.add(IteratorUtils.toList(effect.getConditions()));
      }
      return data;
   }

   /**
    * Translates activation endpoint name from internal name to the UI
    * representaion and back from UI to the internal name.
    * @param name endpoint name, never <code>null</code>, never <code>empty</code>.
    * @param toUI <code>true</code> indicates that the given endpoint name is an
    * internal name that has to be translated into the UI representation,
    * <code>false</code> is a reverse of the above.
    * @return translated endpoint name, never <code>null</code>,
    * never <code>empty</code>.
    */
   public static String translateEndpointName(String endpoint, boolean toUI)
   {
      if (endpoint==null || endpoint.trim().length()<1)
         throw new IllegalArgumentException("endpoint name may not be null or empty");
      final String prefix = "common.direction.";
      if (toUI)
      {
         //from internal name to UI
         if (endpoint.equals(PSRelationshipConfig.ACTIVATION_ENDPOINT_OWNER))
            return PSMessages.getString(prefix + "down");
         else if (endpoint.equals(PSRelationshipConfig.ACTIVATION_ENDPOINT_DEPENDENT))
            return PSMessages.getString(prefix + "up");
         else if (endpoint.equals(PSRelationshipConfig.ACTIVATION_ENDPOINT_EITHER))
            return PSMessages.getString(prefix + "either");
         else
            throw new IllegalArgumentException("failed to translate endpoint name:"
               + endpoint);
      }
      else
      {
         //from UI back to internal name
         if (endpoint.equals(PSMessages.getString(prefix + "down")))
            return PSRelationshipConfig.ACTIVATION_ENDPOINT_OWNER;
         else if (endpoint.equals(PSMessages.getString(prefix + "up")))
            return PSRelationshipConfig.ACTIVATION_ENDPOINT_DEPENDENT;
         else if (endpoint.equals(PSMessages.getString(prefix + "either")))
            return PSRelationshipConfig.ACTIVATION_ENDPOINT_EITHER;
         else
            throw new IllegalArgumentException("failed to translate endpoint name:"
               + endpoint);
      }
   }

   /**
    * Gets the extensions or effecs set on this table model.
    *
    * @return the list of extensions, never <code>null</code>, may be empty.
    */
   public Iterator getData()
   {
      List<PSConditionalEffect> extensions =
            new ArrayList<PSConditionalEffect>();

      for (int i = 0; i < getRowCount(); i++)
      {
         PSConditionalEffect condEff;

         String endpoint = (String)getValueAt(i, COL_EXT_ENDPOINT);
         Object obj = getValueAt(i, COL_EXT_NAME);
         Object conds = getValueAt(i, COL_EXT_COND);

         if(obj instanceof PSExtensionCall)
         {
            condEff = new PSConditionalEffect((PSExtensionCall)obj);
            if(conds instanceof List)
            {
               condEff.setConditions(((List)conds).iterator());
            }

            if (endpoint!=null && endpoint.trim().length()>0)
            {
               condEff.setActivationEndPoint(
                  translateEndpointName(endpoint, false));
            }

            extensions.add(condEff);
         }
      }

      return extensions.iterator();
   }

   /**
    * Sets the value at the specified cell. Overriden to set empty list of
    * conditions in the column <code>COL_EXT_COND</code> for that row if it
    * is not already set, when an extension is chosen in the column <code>
    * COL_EXT_NAME</code>. Useful to render a 'conditions' button when an
    * extension chosen in a new row. See super's description for parameter
    * description.
    */
   public void setValueAt(Object value, int row, int col)
   {
      if(value != null)
      {
         if(col == COL_EXT_NAME)
         {
            List conds = (List)getValueAt(row, COL_EXT_COND);
            if(conds == null)
            {
               super.setValueAt(
                  new ArrayList(), row, COL_EXT_COND);
            }
         }
      }
      super.setValueAt(value, row, col);
   }

   /**
    * Checks whether the supplied cell is editable or not. Overridden to make
    * the <code>COL_EXT_COND</code> is editable only if the extension/effect
    * in that row is set.
    *
    * @param row the row index of value to get, must be >= 0 and less than
    * {@link #getRowCount() rowcount} of this model.
    * @param col the column index of value to get, must be >= 0 and less than
    * {@link #getColumnCount() columncount} of this model.
    *
    * @return <code>true</code> if the cell is editable, otherwise <code>
    * false</code>
    */
   public boolean isCellEditable(int row, int col)
   {
      if(col == COL_EXT_COND)
      {
         if(getValueAt(row, COL_EXT_NAME) == null)
            return false;
      }

      return true;
   }

   /**
    * Gets the column names of the model based on type of extensions it is
    * representing.
    *
    * @return the list of column names, never <code>null</code> or empty.
    */
   private Vector getColumnNames()
   {
      return PSRelationshipEditorDialog.ms_effColumns;
   }

   //implements interface method.
   public String getDescription(int row)
   {
      String description = "";
      Object obj = getValueAt(row, COL_EXT_NAME);
      if(obj instanceof OSExtensionCall)
      {
         OSExtensionCall call = (OSExtensionCall)obj;
         IPSExtensionDef def = call.getExtensionDef();
         description = def.getInitParameter(
            IPSExtensionDef.INIT_PARAM_DESCRIPTION);
      }

      return description;
   }

   //overridden to return true always.
   public boolean allowRemove()
   {
      return true;
   }

   //overridden to return true always.
   public boolean allowMove()
   {
      return true;
   }

}
