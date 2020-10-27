/*[ OSPipeHelper.java ]********************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer;

import com.percussion.design.objectstore.PSPipe;
import com.percussion.design.objectstore.PSQueryPipe;
import com.percussion.design.objectstore.PSUpdatePipe;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Properties;

/**
 * A wrapper around the PSQueryPipe objects that provides all data and its access for
 * the UIQueryPipe objects.
 */
////////////////////////////////////////////////////////////////////////////////
public class OSPipeHelper implements Serializable
{
   /**
    * Default constructor.
    */
   //////////////////////////////////////////////////////////////////////////////
   public OSPipeHelper()
   {
   }

   //////////////////////////////////////////////////////////////////////////////
  // IPersist interface implementation
   public boolean load(PSPipe osPipe, Object store, Properties config,
                                UIFigure owner)
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
      System.out.println("In OSPipeHelper .....load()");

    try
    {
       // load data
         OSDataset dataset = (OSDataset) store;
      if (osPipe instanceof OSQueryPipe)
      {
         OSQueryPipe query = (OSQueryPipe) osPipe;
        query.copyFrom((PSQueryPipe) dataset.getPipe());
            query.setDatasetDescription(dataset.getDescription());
            query.setDatasetName(dataset.getName());
      }
      else
      {
         OSUpdatePipe update = (OSUpdatePipe) osPipe;
        update.copyFrom((PSUpdatePipe) dataset.getPipe());
            update.setDatasetDescription(dataset.getDescription());
            update.setDatasetName(dataset.getName());
      }

      // restore GUI information
      OSLoadSaveHelper.loadOwner(osPipe.getId(), config, owner);

      return true;
    }
    catch (Exception e)
    {
       e.printStackTrace();
    }

      return false;
   }

   //////////////////////////////////////////////////////////////////////////////
  // IPersist interface implementation
   public void save(PSPipe osPipe, Object store, Properties config,
                           UIFigure owner)
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
//      System.out.println("In OSPipeHelper .....save()");

      try
    {
         // store current id temporary and create a new unique id for this object
         int currentId = osPipe.getId();
         osPipe.setId(Util.getUniqueId());

         // save this pipe into the provided dataset
         OSDataset dataset = (OSDataset) store;
         PSPipe pipe = dataset.getPipe();
         if ( null != pipe && pipe instanceof OSQueryPipe )
         {
            /* get the sorted columns from the pager and put them in the
               selector */
            OSDataSelector sourceSelector = (OSDataSelector) ((OSQueryPipe) pipe).getDataSelector();
            OSDataSelector targetSelector = (OSDataSelector) ((OSQueryPipe) osPipe).getDataSelector();
            if ( null != sourceSelector && null != targetSelector )
               targetSelector.setSortedColumns( sourceSelector.getSortedColumns());
            else if ( null != sourceSelector && null == targetSelector )
               ((OSQueryPipe) osPipe).setDataSelector( sourceSelector );
         }
         dataset.setPipe(osPipe);
         if(osPipe instanceof OSQueryPipe)
         {
            OSQueryPipe query = (OSQueryPipe) osPipe;
            dataset.setName(query.getDatasetName());
            dataset.setDescription(query.getDatasetDescription());
         }
         else
         {
            OSUpdatePipe update = (OSUpdatePipe) osPipe;
            dataset.setName(update.getDatasetName());
            dataset.setDescription(update.getDatasetDescription());
         }
            

      // save GUI information
      OSLoadSaveHelper.saveOwner(currentId, osPipe.getId(), config, owner);
    }
    catch (Exception e)
    {
       e.printStackTrace();
    }
   }

  //////////////////////////////////////////////////////////////////////////////
}

