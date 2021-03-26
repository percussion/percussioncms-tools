/*[ OSTransaction.java ]*******************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer;

import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSDataSet;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Properties;

class OSTransaction implements Serializable, IGuiLink, IPersist
{
   public void copyFrom( PSDataSet ds )
   {
      m_bEnabled = !ds.isTransactionDisabled();
      if ( ds.isTransactionForAllRows())
         m_transactionType = TT_ALLROWS;
      else if ( ds.isTransactionForRow())
         m_transactionType = TT_ROW;
      else
         Debug.assertTrue( false, E2Designer.getResources(), "NewTransactionType", null );
   }

   public int getId()
   {
      return m_id;
   }

   public void setId(int id)
   {
      m_id = id;
   }

   /*************
      IGuiLink interface implementation
   *************/
   public void setFigure( UIFigure fig )
   {
      m_owner = fig;
   }

   public void release()
   {
      m_owner = null;
   }

   public UIFigure getFigure()
   {
      return m_owner;
   }

   //////////////////////////////////////////////////////////////////////////////
  // IPersist interface implementation
   public boolean load(PSApplication app, Object store, Properties config)
   {
      if (null == store || null == config)
         throw new IllegalArgumentException();

      if (!(store instanceof OSDataset))
      {
         Object[] astrParams = 
         {
            "OSDataset"
         };
         throw new IllegalArgumentException(MessageFormat.format(
            E2Designer.getResources().getString("IncorrectType"), astrParams));
      }

    try
    {
         OSDataset dataset = (OSDataset) store;
      if (dataset.isTransactionForRow())
         setRowLevel();
      else
         setAllRows();

      // restore extra GUI data
         String strId = new Integer(this.getId()).toString();
         m_bEnabled = false;
         if (config.getProperty(KEY_ENABLED + strId) != null)
            m_bEnabled = Boolean.valueOf(config.getProperty(KEY_ENABLED + strId)).booleanValue();

      // restore GUI information
      OSLoadSaveHelper.loadOwner(this.getId(), config, m_owner);

      return true;
    }
    catch (Exception e)
    {
       e.printStackTrace();
    }

      return false;
   }

   /**
    * Interface method unimplemented.
    * @see com.percussion.E2Designer.IPersist#cleanup(com.percussion.
    * E2Designer.OSApplication)
    */
   public void cleanup(OSApplication app)
   {
   }

   //////////////////////////////////////////////////////////////////////////////
  // IPersist interface implementation
   public void save(PSApplication app, Object store, Properties config )
   {
      if (null == store || null == config)
         throw new IllegalArgumentException();

      if (!(store instanceof OSDataset))
      {
         Object[] astrParams =
         {
            "OSDataset"
         };
         throw new IllegalArgumentException(MessageFormat.format(
            E2Designer.getResources().getString("IncorrectType"), astrParams));
      }

      try
    {
         // store current id temporary and create a new unique id for this object
         int currentId = this.getId();
         this.setId(Util.getUniqueId());

      OSDataset dataset = (OSDataset) store;
      m_bEnabled = true;
      if (isRowLevel())
         dataset.setTransactionForRow();
      else
         dataset.setTransactionForAllRows();

         String strId = new Integer(this.getId()).toString();
         config.setProperty(KEY_ENABLED + strId, Boolean.valueOf(m_bEnabled).toString());

      // save GUI information
      OSLoadSaveHelper.saveOwner(currentId, this.getId(), config, m_owner);
    }
    catch (Exception e)
    {
       e.printStackTrace();
    }
   }

   /**
    * Set transaction manager for all rows.
    */
  public void setAllRows()
  {
     m_transactionType = TT_ALLROWS;
  }

   /**
    * Set transaction manager for one row.
    */
  public void setRowLevel()
  {
     m_transactionType = TT_ROW;
  }

   /**
    * Returns true if transaction manager is enabled for all rows.
   *
   * @return boolean   row status
    */
  public boolean isAllRows()
  {
     return (m_transactionType == TT_ALLROWS);
  }

   /**
    * Returns true if transaction manager is enabled for one row.
   *
   * @return boolean   row status
    */
  public boolean isRowLevel()
  {
     return (m_transactionType == TT_ROW);
  }

   /**
    * Constants used to define the transaction type.
    */
   private static final int    TT_ALLROWS = 1;
   private static final int    TT_ROW = 2;


   // private storage 
   private UIFigure m_owner = null;
  private int m_id = 0;

   private boolean m_bEnabled = true;
   private int m_transactionType = TT_ALLROWS;
  /*
   * the user configuration property key
   */
   private static final String KEY_ENABLED = new String("enabled");
}
