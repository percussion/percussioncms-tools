/*[ OSDataSelector.java ]******************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer;

import com.percussion.design.objectstore.IPSReplacementValue;
import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSDataSelector;
import com.percussion.design.objectstore.PSHtmlParameter;
import com.percussion.design.objectstore.PSQueryPipe;
import com.percussion.design.objectstore.PSWhereClause;
import com.percussion.util.PSCollection;

import java.text.MessageFormat;
import java.util.Properties;

/**
 * A wrapper around the PSDataSelector object that provides all data and its
 * access for the UIFigure objects.
 */
////////////////////////////////////////////////////////////////////////////////
public class OSDataSelector extends PSDataSelector implements IGuiLink,
                                                              IPersist,
                                                              IDataCataloger
{
  /**
   * Default constructor.
   */
   //////////////////////////////////////////////////////////////////////////////
   public OSDataSelector()
   {
     super();

   }

  /**
   * Copy constructor.
   *
   * @param   source   the source object.
   */
   //////////////////////////////////////////////////////////////////////////////
   public OSDataSelector(OSDataSelector source)
   {
      copyFrom(source);
   }

   public OSDataSelector( PSDataSelector source )
   {
      super.copyFrom(source);
   }

   /**
    * Copy the passed object into this object.<p>
    * The owner is not copied by this method. If the caller wants to change
    * owners, they should call <code>setFigure( src.getFigure())</code> after
    * this method completes.
    *
    * @param   source   the source object.
    */
   //////////////////////////////////////////////////////////////////////////////
   public void copyFrom(OSDataSelector source)
   {
      super.copyFrom((PSDataSelector) source);
   }

  //////////////////////////////////////////////////////////////////////////////
  // implementation for IDataCataloger
  public void catalogData(ObjectType iObjType, CatalogReceiver container)
  {
    if (iObjType == ObjectType.HTML_PARAM)
    {
       // make sure we have a where clause collection to use
      if (isSelectByWhereClause())
      {
        PSCollection clauses = getWhereClauses();
        if (null == clauses) // no where clauses exist, return;
          return;

        // go thru all clauses and append all variables/values that are
        // HtmlParameters to the paramList; sort later
        for (int i = 0; i < clauses.size(); i++)
        {
          PSWhereClause aClause = (PSWhereClause)clauses.get(i);
          IPSReplacementValue param = aClause.getVariable();
          if (param instanceof PSHtmlParameter)
            container.add( ((PSHtmlParameter)param).getName() );

          param = aClause.getValue();
          if (param instanceof PSHtmlParameter)
            container.add( ((PSHtmlParameter)param).getName() );
        }
      }
    }
  }

   //////////////////////////////////////////////////////////////////////////////
   // implementation for IPersist
   public boolean load(PSApplication app, Object store, Properties config)
   {
      if (null == store || null == config)
         throw new IllegalArgumentException();

      if (!(store instanceof PSQueryPipe))
      {
         Object[] astrParams =
         {
            "PSPipe"
         };
         throw new IllegalArgumentException(MessageFormat.format(
            E2Designer.getResources().getString("IncorrectType"), astrParams));
      }

      try
      {
         PSQueryPipe pipe = (PSQueryPipe) store;
         this.copyFrom(pipe.getDataSelector());

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
    * @see com.percussion.E2Designer.IPersist#cleanup(com.percussion.E2Designer.OSApplication)
    */
   public void cleanup(OSApplication app)
   {
   }

   //////////////////////////////////////////////////////////////////////////////
   // implementation for IPersist
   public void save(PSApplication app, Object store, Properties config)
   {
      if (null == store || null == config)
         throw new IllegalArgumentException();

      if (!(store instanceof PSQueryPipe))
      {
         Object[] astrParams =
         {
            "PSQueryPipe"
         };
         throw new IllegalArgumentException(MessageFormat.format(
            E2Designer.getResources().getString("IncorrectType"), astrParams));
      }

      try
    {
         // store current id temporary and create a new unique id for this object
         int currentId = this.getId();
         this.setId(Util.getUniqueId());

         // save this backendtank into the provided pipe
         PSQueryPipe pipe = (PSQueryPipe) store;
         pipe.setDataSelector(this);

      // save GUI information
      OSLoadSaveHelper.saveOwner(currentId, this.getId(), config, m_owner);
    }
    catch (Exception e)
    {
       e.printStackTrace();
    }
   }

   //////////////////////////////////////////////////////////////////////////////
   // implementations for IGuiLink
   public void setFigure(UIFigure fig) { m_owner = fig; }
   public void release() { m_owner = null; }
   public UIFigure getFigure() { return m_owner; }
  // member used explicitly withing the IGuiLink interface
  private UIFigure m_owner = null;
}

