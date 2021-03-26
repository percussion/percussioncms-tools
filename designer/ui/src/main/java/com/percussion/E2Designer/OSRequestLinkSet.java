/*[ OSRequestLinkSet.java ]****************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer;

import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSRequestLink;
import com.percussion.error.PSIllegalArgumentException;
import com.percussion.util.PSCollection;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Properties;

public class OSRequestLinkSet implements IGuiLink, IPersist, Serializable
{
   /**
      * default constructor
    */
   public OSRequestLinkSet()
   {
      try
      {
         m_requestLinks = new PSCollection(sREQUEST_LINK_CLASS_NAME);
         PSRequestLink link = new PSRequestLink(E2Designer.getResources().getString( "Dummy" ));
         m_requestLinks.add(link);
      }
      catch(ClassNotFoundException e)
      {
         System.out.println(e.toString());
      }
      catch(PSIllegalArgumentException e)
      {
         System.out.println(e.toString());
      }
   }


   /**
    * copy constructor
    */
   public OSRequestLinkSet(OSRequestLinkSet linkSet)
   {
      this();
      this.copyFrom(linkSet);
   }

   
   /**
    * copy from method
    */
   public void copyFrom( OSRequestLinkSet linkSet )
   {
      PSCollection cLinks = linkSet.getRequestLinks();
      m_requestLinks.clear();

      if(cLinks.size() > 0)
         m_strTargetDataset = ((PSRequestLink)cLinks.get(0)).getTargetDataSet();

      for(int i=0; i<cLinks.size(); i++)
      {
         PSRequestLink link = (PSRequestLink)cLinks.get(i);
               // must point to the same dataset
         if(!(link.getTargetDataSet().equals(m_strTargetDataset)))
            throw new IllegalArgumentException("Target Datasets are not the same");
      }

      for(int i=0; i<cLinks.size(); i++)
      {
         PSRequestLink link = (PSRequestLink)cLinks.get(i);
         PSRequestLink copyOfLink = null;
         try
         {
            copyOfLink = new PSRequestLink(link.getTargetDataSet());
            copyOfLink.copyFrom(link);
            m_requestLinks.add(copyOfLink);
         }
         catch(PSIllegalArgumentException e) 
         {
            e.printStackTrace();
         }
      }
      setId(linkSet.getId());
   }

   public int getId()
   {
      return m_id;
   }

   public void setId(int id)
   {
      m_id = id;
   }

   public void setTargetDataset(String strDataset)
   {
      m_strTargetDataset = strDataset;
   }

   public String getTargetDataset()
   {
      return m_strTargetDataset;
   }

   public PSCollection getRequestLinks()
   {
      return m_requestLinks;
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

      if (!(store instanceof PSCollection))
      {
         String [] astrParams =
         {
            "PSCollection"
         };
         throw new IllegalArgumentException(MessageFormat.format(
            E2Designer.getResources().getString("IncorrectType"), astrParams));
      }

      PSCollection cLinks = (PSCollection)store;

      if(!(cLinks.getMemberClassName().equals(sREQUEST_LINK_CLASS_NAME)))
            throw new IllegalArgumentException("Collection does not contain PSRequestLink objects");

      m_requestLinks.clear();

      if(cLinks.size() > 0)
      {
         // get the first elem in the coll and get the target data set and id and store locally
         PSRequestLink requestLink = (PSRequestLink)cLinks.get(0);
         m_strTargetDataset = requestLink.getTargetDataSet();
         m_id = requestLink.getId();

         for(int i=0; i<cLinks.size(); i++)
         {
            PSRequestLink link = (PSRequestLink)cLinks.get(i);
            if(link.getTargetDataSet().equals(m_strTargetDataset))
               m_requestLinks.add(link);
            else
               throw new IllegalArgumentException("Target dataset name "+link.getTargetDataSet()+" does not match "+m_strTargetDataset);
         }
      }

    try
    {
      // restore GUI information
      OSLoadSaveHelper.loadOwner(this.getId() , config, m_owner);

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
   public void save(PSApplication app, Object store, Properties config)
   {
      if ( null == store || null == config )
         throw new IllegalArgumentException();

      if (!(store instanceof PSCollection))   // check for PSRequest link, dont clear collection(store), add to collection from member collection
      {
         String [] astrParams = 
         {
            "PSCollection"
         };
         throw new IllegalArgumentException(MessageFormat.format(
            E2Designer.getResources().getString("IncorrectType"), astrParams));
      }

      PSCollection cLinks = (PSCollection)store;

      if(!(cLinks.getMemberClassName().equals(sREQUEST_LINK_CLASS_NAME)))
         throw new IllegalArgumentException("Collection does not contain PSRequestLink objects");

      if(m_requestLinks.size() > 0)
      {

         try
         {
            // store current id temporary and create a new unique id for this object
            int currentId = this.getId();
            int uniqueId = Util.getUniqueId();

            //   getUniqueId() and set it in each link in collection
            //   copy all links from member coll to passed in coll
            for(int i=0; i< m_requestLinks.size(); i++)
            {
               PSRequestLink link = (PSRequestLink)m_requestLinks.get(i);
               link.setTargetDataSet(m_strTargetDataset);
               link.setId(uniqueId);
               cLinks.add(link);
            }

            // save GUI information
            OSLoadSaveHelper.saveOwner(currentId, this.getId(), config, m_owner);
         }
         catch (Exception e)
         {
          e.printStackTrace();
         }
      }
   }

// PSCollection object to store collection
   private PSCollection m_requestLinks = null;
   
   private int m_id = -1;

   private String m_strTargetDataset = "";

   private String sREQUEST_LINK_CLASS_NAME = "com.percussion.design.objectstore.PSRequestLink";


   // private storage
   private UIFigure m_owner = null;
}
