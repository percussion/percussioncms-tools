/******************************************************************************
*
* [ PSMockGuidManager.java ]
*
* COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
* All rights reserved. This material contains unpublished, copyrighted
* work including confidential and proprietary information of Percussion.
*
*****************************************************************************/
/**
 * 
 */
package com.percussion.client.impl;

import com.percussion.design.objectstore.PSLocator;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.utils.guid.IPSGuid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class is used by the workbench to create zero value guids
 * that must be replaced on the server side.
 */
public class PSMockGuidManager implements IPSGuidManager
{

   /* 
    * @see com.percussion.services.guidmgr.IPSGuidManager#createGuid(
    * com.percussion.services.catalog.PSTypeEnum)
    */
   public IPSGuid createGuid(PSTypeEnum type)
   {
      return new PSGuid(getHostId(), type, 0);
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.guidmgr.IPSGuidManager#convertToGuid(long,
    *      com.percussion.services.catalog.PSTypeEnum)
    */
   public IPSGuid makeGuid(long raw, PSTypeEnum type)
   {
      return new PSGuid(type, raw);
   }

   /* 
    * @see com.percussion.services.guidmgr.IPSGuidManager#createGuids(
    * com.percussion.services.catalog.PSTypeEnum, int)
    */
   public List<IPSGuid> createGuids(PSTypeEnum type, int count)
   {
      List<IPSGuid> guids = new ArrayList<IPSGuid>(count);
      for(int i = 0; i < count; i++)
         guids.add(createGuid(type));
      return guids;
   }

   /* 
    * @see com.percussion.services.guidmgr.IPSGuidManager#createGuid(
    * byte, com.percussion.services.catalog.PSTypeEnum)
    */
   public IPSGuid createGuid(@SuppressWarnings("unused") byte repositoryId, 
         PSTypeEnum type)
   {
      return createGuid(type);
   }

   /* 
    * @see com.percussion.services.guidmgr.IPSGuidManager#createGuids(
    * byte, com.percussion.services.catalog.PSTypeEnum, int)
    */
   public List<IPSGuid> createGuids(
         @SuppressWarnings("unused") byte repositoryId, 
         PSTypeEnum type, int count)
   {
      return createGuids(type, count);
   }   

   /* 
    * @see com.percussion.services.guidmgr.IPSGuidManager#getHostId()
    */
   public long getHostId()
   {
      return 0;
   }

   public int createId(@SuppressWarnings("unused") String key)
   {
      return 0;
   }

   public int[] createIdBlock(@SuppressWarnings("unused") String key, 
         int blocksize)
   {
      int rval[] = new int[blocksize];
      return rval;
   }

   public IPSGuid makeGuid(String raw, PSTypeEnum type)
   {
      return new PSGuid(type, raw);
   }

   public IPSGuid makeGuid(PSLocator loc)
   {
      return new PSLegacyGuid(loc);
   }

   public PSLocator makeLocator(IPSGuid guid)
   {
      return ((PSLegacyGuid) guid).getLocator();
   }
   
   public IPSGuid makeGuid(String raw)
   {
      PSGuid guid = new PSGuid(raw);
      if (guid.getType() == PSTypeEnum.LEGACY_CONTENT.getOrdinal() ||
            guid.getType() == PSTypeEnum.LEGACY_CHILD.getOrdinal())
      {
         return new PSLegacyGuid(guid);
      }
      return guid;
   }

   public List<Integer> extractContentIds(List<IPSGuid> guids)
   {
      if (guids == null || guids.size() == 0)
      {
         throw new IllegalArgumentException("guids may not be null or empty");
      }
      for(IPSGuid g : guids)
      {
         if (!(g instanceof PSLegacyGuid))
         {
            throw new IllegalArgumentException("guids must be content guids");
         }
      }
      
      if (guids.size() == 1)
      {
         PSLegacyGuid g = (PSLegacyGuid) guids.get(0);
         return Collections.singletonList(g.getContentId());
      }
      else
      {
         List<Integer> rval = new ArrayList<Integer>();
         for(IPSGuid g : guids)
         {
            PSLegacyGuid lg = (PSLegacyGuid) g;
            rval.add(lg.getContentId());
         }
         return rval;
      }
   }

   public long createLongId(PSTypeEnum type)
   {
      return 0;
   }


/**
 * Transactional method to update the nextn number in the db
 * @param key
 * @param blocksize
 * @return
 */
@Override
public int updateNextNumber(String key, int blocksize, long setValue )
{
    // TODO Auto-generated method stub
    return 0;
}


@Override
public long updateNextLong(Integer key)
{
    // TODO Auto-generated method stub
    return 0;
}

@Override
public int fixNextNumber(String key, int value)
{
   return 0;
}


@Override
public void loadHostId()
{
    // TODO Auto-generated method stub
    
}


@Override
public int peekNextNumber(String nnkey)
{
   return 0;
}

}
