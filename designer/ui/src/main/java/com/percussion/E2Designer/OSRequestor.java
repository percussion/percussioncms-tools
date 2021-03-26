/******************************************************************************
 *
 * [ OSRequestor.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.E2Designer;

import com.percussion.design.objectstore.IPSReplacementValue;
import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSConditional;
import com.percussion.design.objectstore.PSHtmlParameter;
import com.percussion.design.objectstore.PSRequestor;
import com.percussion.design.objectstore.PSTextLiteral;
import com.percussion.util.PSCollection;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Properties;

/**
 * A wrapper around the PSRequestor. This is used to store / restore the
 * dataset input connector properties.
 */
////////////////////////////////////////////////////////////////////////////////
public class OSRequestor extends PSRequestor implements IGuiLink, IPersist,
                                                        IDataCataloger
{
   /**
    * Default constructor
    */
   //////////////////////////////////////////////////////////////////////////////
   public OSRequestor()
   {
      HashMap<String, PSTextLiteral> mimeMap = new HashMap<String, PSTextLiteral>();
      mimeMap.put( "htm", new PSTextLiteral( "text/html" ));
      mimeMap.put( "html", new PSTextLiteral( "text/html" ));
      setMimeProperties( mimeMap );
      setCharacterEncoding( "UTF-8" );
   }

   /**
    * Copy constructor
    */
   //////////////////////////////////////////////////////////////////////////////
   public OSRequestor(OSRequestor source)
   {
        copyFrom(source);
   }

   public OSRequestor(PSRequestor source)
   {
        super.copyFrom(source);
   }

   /**
    * Copy the passed object into this object. <p>
    * The owner is not copied by this method. If the caller wants to change
    * owners, they should call <code>setFigure( src.getFigure())</code> after
    * this method completes.
    *
    * @param   source   the source object.
    */
   //////////////////////////////////////////////////////////////////////////////
   public void copyFrom(OSRequestor source)
   {
      super.copyFrom(source);

      m_root = source.m_root;
   }

   /**
    * Get the application root
    *
    * @return String
    */
   //////////////////////////////////////////////////////////////////////////////
   public String getApplicationRoot()
   {
      return m_root;
   }

   /**
    * Set the application root
    *
    * @return String
    */
   //////////////////////////////////////////////////////////////////////////////
   public void setApplicationRoot(String root)
   {
      m_root = root;
   }

   /**
    * Get the active paged datatank.
    *
    * @return OSPageDatatank the page datatank
    */
   //////////////////////////////////////////////////////////////////////////////
   public OSPageDatatank getPageDatatank()
   {
      return m_pageDatatank;
   }

   //////////////////////////////////////////////////////////////////////////////
   // implementation for IPersist
   public boolean load(PSApplication app, Object store, Properties config)
   {
      if (null == store || null == config)
         throw new IllegalArgumentException();

      if (!(store instanceof OSDataset))
      {
         Object[] astrParams =
         {
            "OSDataset or OSContentEditor"
         };
         throw new IllegalArgumentException(MessageFormat.format(
            E2Designer.getResources().getString("IncorrectType"), astrParams));
      }

      try
      {
         PSRequestor requestor = null;
         // load data
         if(store instanceof OSDataset)
         {
            OSDataset dataset = (OSDataset) store;
            requestor = dataset.getRequestor();
            if (requestor != null)
               copyFrom(requestor);

            // we need a reference to the page tank for the value selector
            if (dataset.getPageDataTank() != null)
               m_pageDatatank = new OSPageDatatank(dataset.getPageDataTank());
         }
         // restore GUI information
         OSLoadSaveHelper.loadOwner(this.getId(), config, m_owner);

         // set label information
         setInternalName( requestor.getRequestPage() );

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
   // implementation for IPersist
   public void save(PSApplication app, Object store, Properties config)
   {
      if ( null == store || null == config )
         throw new IllegalArgumentException();

      if (!(store instanceof OSDataset))
      {
         Object[] astrParams =
         {
            "OSDataset or OSContentEditor"
         };
         throw new IllegalArgumentException(MessageFormat.format(
            E2Designer.getResources().getString("IncorrectType"), astrParams));
      }

      try
      {
         // store current id temporary and create a new unique id for this object
         int currentId = this.getId();
         this.setId(Util.getUniqueId());

         // save this encryptor into the provided dataset
         if(store instanceof OSDataset)
         {
            OSDataset dataset = (OSDataset) store;
            dataset.setRequestor(this);
         }
         // save GUI information
         OSLoadSaveHelper.saveOwner(currentId, this.getId(), config, m_owner);
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   public void setInternalName(String strName)
   {
      m_strInternalName = strName;
      if ( null != m_owner )
      {
         m_owner.invalidateLabel();

         // updating PipeFrame's title
         UIConnectableFigure figure = ((UIFlexibleConnectionPoint)m_owner).getOwner();
         if (null != figure.getEditor())
         {
            ((UIPipeFrame)figure.getEditor()).setTitle(m_strInternalName);
         }
      }
   }

   public String getInternalName()
   {
      return m_strInternalName;
   }

   //////////////////////////////////////////////////////////////////////////////
   // implementation for IDataCataloger
   public void catalogData(ObjectType iObjType, CatalogReceiver container)
   {
      if (iObjType == ObjectType.HTML_PARAM)
      {
         PSCollection criteria = getSelectionCriteria();
         PSCollection rules = getValidationRules();

         // go thru all conditionals and append all variables/values that are
         // HtmlParameters to the paramList; sort and "uniquify" later
         if ( null != criteria )
         {
            for (int i = 0; i < criteria.size(); i++)
            {
               PSConditional aConditional = (PSConditional)criteria.get(i);
               IPSReplacementValue param = aConditional.getVariable();
               if (param instanceof PSHtmlParameter)
                  container.add( ((PSHtmlParameter)param).getName() );

               param = aConditional.getValue();
               if (param instanceof PSHtmlParameter)
                  container.add( ((PSHtmlParameter)param).getName() );
            }
         }
         if ( null != rules )
         {
            for (int i = 0; i < rules.size(); i++)
            {
               PSConditional aConditional = (PSConditional)rules.get(i);
               IPSReplacementValue param = aConditional.getVariable();
               if (param instanceof PSHtmlParameter)
                  container.add( ((PSHtmlParameter)param).getName() );

               param = aConditional.getValue();
               if (param instanceof PSHtmlParameter)
                  container.add( ((PSHtmlParameter)param).getName() );
            }
         }
      }
   }

   //////////////////////////////////////////////////////////////////////////////
   // implementations for IGuiLink
   public void setFigure(UIFigure fig) { m_owner = fig; }
   public void release() { m_owner = null; }
   public UIFigure getFigure() { return m_owner; }

   // member used explicitly withing the IGuiLink interface
   private UIFigure m_owner;

   //////////////////////////////////////////////////////////////////////////////
   /**
    * the selection parameters
    */
   String m_root = "rootDummy";
   /**
    * the page datatank
    */
   private OSPageDatatank m_pageDatatank;
   /**
    * the internal string name used to name the resources and the pipe frame title.
    */
   private String m_strInternalName = "";
}

