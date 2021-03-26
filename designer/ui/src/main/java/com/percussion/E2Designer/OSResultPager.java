/*[ OSResultPager.java ]*******************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer;

import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSQueryPipe;
import com.percussion.design.objectstore.PSResultPager;
import com.percussion.design.objectstore.PSSortedColumn;
import com.percussion.util.PSCollection;

import java.text.MessageFormat;
import java.util.Properties;

/**
 * The server's data class is extended for future requirements of the GUI.
 */
public class OSResultPager extends PSResultPager implements IGuiLink, IPersist
{
   public OSResultPager()
   {
      super();
   }

   /*
    */
   public OSResultPager( PSResultPager pager )
   {
      copyFrom( pager );
   }

   /**
    * Performs a shallow copy of the data in the supplied component to this
    * component. Derived classes should implement this method for their data,
    * calling the base class method first. <p>
    * The owner is not copied by this method. If the caller wants to change
    * owners, they should call <code>setFigure( src.getFigure())</code> after
    * this method completes.
    *
    * @param pager a valid OSResultPager.
    */
   public void copyFrom( OSResultPager pager )
   {
      copyFrom((PSResultPager) pager );
      // assume object is valid
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

   /**
    * Sorted columns are used to tell the database how to sort a result set.
    *
    * @return a collection that contains PSSortedColumn objects
    *
    * @see #setSortedColumns
   **/
   public PSCollection getSortedColumns()
   {
      PSCollection cols = null;
      if ( null != m_sortedColumns )
      {
         cols = new PSCollection( m_sortedColumns.get(0).getClass());
         int size = m_sortedColumns.size();
         for ( int i = 0; i < size; ++i )
            cols.add( m_sortedColumns.get(i));
      }

      return cols;
   }

   /**
    * Sets the columns that are to be used for sorting the result set returned
    * by the selector attached to the same pipe as this object. The collection
    * must contain objects of type PSSortedColumn. If it is desired to add to
    * the existing columns, use <code>getSortedColumns</code> to get the
    * current collection, then modify that set and pass it to this method.
    *
    * @param sortedColumns a set of PSSortedColumn objects, or null to clear
    * all sorted columns. If an empty set is supplied, this will act the same
    * as passing in null. The passed in collection is used directly, so any
    * changes made after passing it to this method will affect the data in
    * this object.
   **/
   public void setSortedColumns( PSCollection sortedColumns )
   {
      if ( null != sortedColumns )
      {
         if ( 0 == sortedColumns.size())
            sortedColumns = null;
         else if ( !( sortedColumns.get(0) instanceof PSSortedColumn ))
            throw new IllegalArgumentException();
      }
      m_sortedColumns = sortedColumns;
   }

   //////////////////////////////////////////////////////////////////////////////
  // IPersist interface implementation
   public boolean load(PSApplication app, Object store, Properties config)
   {
      if (null == store || null == config)
         throw new IllegalArgumentException();

      if (!(store instanceof OSDataset))
      {
         String [] astrParams =
         {
            "OSDataset"
         };
         throw new IllegalArgumentException(MessageFormat.format(
            E2Designer.getResources().getString("IncorrectType"), astrParams));
      }

      try
      {
         OSDataset dataset = (OSDataset) store;
         this.copyFrom(dataset.getResultPager());
         setSortedColumns(((PSQueryPipe) dataset.getPipe()).getDataSelector().getSortedColumns());

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
         String [] astrParams =
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

         // save this object into the provided dataset
         OSDataset dataset = (OSDataset) store;
         dataset.setResultPager((PSResultPager) this);

         /* See if the selector has been saved yet. If there is one, save
            the columns into it, otherwise create a new, default one. */
         PSQueryPipe pipe = (PSQueryPipe) dataset.getPipe();
         if ( null == pipe )
         {
            pipe = new OSQueryPipe();
            dataset.setPipe( pipe );
         }
         OSDataSelector sel = (OSDataSelector) pipe.getDataSelector();
         if ( null == sel )
         {
            sel = new OSDataSelector();
            pipe.setDataSelector( sel );
         }
         sel.setSortedColumns( m_sortedColumns );


         // save GUI information
         OSLoadSaveHelper.saveOwner(currentId, this.getId(), config, m_owner);
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

  //////////////////////////////////////////////////////////////////////////////
   // private storage
   private UIFigure m_owner = null;
   private PSCollection m_sortedColumns = null;
}
