/*[ PSStaticItemExtractor.java ]***********************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader.extractor;

import com.percussion.loader.PSItemContext;
import com.percussion.loader.PSLoaderUtils;
import com.percussion.loader.objectstore.PSStaticItemExtractorDef;
import com.percussion.loader.ui.PSConfigPanel;
import com.percussion.loader.ui.PSStaticItemExtractorPanel;

import java.io.InputStream;

/**
 * Extracts pieces that are not added to Rhythmyx as items. These are more web
 * resource type things like images, stylesheets, etc
 */
public class PSStaticItemExtractor extends PSExtractor
{
   // see com.percussion.loader.IPSItemExtractor for description
   public int containsInstances( PSItemContext resource )
   {
      if (matchSourceLocation(resource.getResourceId(), 
         resource.getRootResourceId()))
      {
         return super.containsInstances(resource);
      }
      
      return 0;
      
   }

   /**
    * Determines whether the resource-id matches the source-location of the
    * extractor's definition. The source-location is relative to the search
    * root path of the selector definition.
    *
    * @param resourceId    The resource-id of a content tree node, it is an
    *    an absolute path and may not be <code>null</code> or empty.
    *
    * @param rootResourceId  The resource id of the search root in the selector
    *    definition. It may be <code>null</code>.
    *
    * @return <code>true</code> if resourceId is within (or under) the 
    *    source-location; otherwise return <code>false</code>.
    */
   protected boolean matchSourceLocation(String resourceId, 
      String rootResourceId)
   {
      if (resourceId == null || resourceId.trim().length() == 0)
         throw new IllegalArgumentException("resourceId may not be null");
         
      boolean doesMatch = false;

      try
      {
         // get the parent directory of the resourceId
         String resParentDir = resourceId;
         int lastSlash = resourceId.lastIndexOf("/");
         if (lastSlash > 0)
            resParentDir = resourceId.substring(0, lastSlash);

         // normalize the source location, which should be relative to root
         String srcLocation = getExtractorDef().getProperty(
            PSStaticItemExtractorDef.SOURCE_LOCATION).getValue();
         
         //FB: RV_RETURN_VALUE_IGNORED NC 1-17-16
         srcLocation = srcLocation.replace('\\', '/');
         
         if (rootResourceId != null)
            srcLocation = rootResourceId + '/' + srcLocation;
            
         doesMatch = resParentDir.startsWith(srcLocation);
      }
      catch (Exception e)
      {
         e.printStackTrace(); // not expected exception
      }
      return doesMatch;
   }

   // implements the IPSUIPlugin interface
   public PSConfigPanel getConfigurationUI()
   {
      return new PSStaticItemExtractorPanel();
   }

   // see com.percussion.loader.IPSItemExtractor for description
   public PSItemContext[] extractItems(PSItemContext item, InputStream in)
      throws java.io.IOException
   {
      byte[] data = PSLoaderUtils.getRawData(in);
      item.setStaticData(data);

      PSItemContext[] items = new PSItemContext[1];
      items[0] = item;

      return items;
   }
}
