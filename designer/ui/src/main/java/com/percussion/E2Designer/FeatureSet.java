/*[ FeatureSet.java ]**********************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer;

import com.percussion.design.objectstore.PSFeature;
import com.percussion.design.objectstore.PSFeatureSet;
import com.percussion.design.objectstore.PSObjectStore;
import com.percussion.design.objectstore.PSServerConfiguration;
import com.percussion.design.objectstore.PSVersion;

import java.util.Iterator;
import java.util.List;

/**
 * Singleton class to aid in maintaining backward compatiblity with older
 * servers.  Retreives the list of supported features from the server when it
 * is first instantiated.  Other objects use this to determine if a feature
 * that is currently supported in the Workbench is also supported on the server
 * since the server may be running an older version.
 */
public class FeatureSet
{
   /**
    * The name of the feature that includes the new full text search engine.
    * This can be used with {@link isFeatureSupported(String)} to
    * control gui interfaces when talking to older servers.
    * <p>In general, most users should call {@link #isFTSearchEnabled()},
    * which also checks that the feature is installed and licensed. 
    */
   public static final String FEATURE_FTS = PSFeature.FEATURE_FTS;
   
   /**
    * Returns singleton instance of this class stored in m_featureSet.
    * Creates an instance of itself and
    * stores it before returning it to the caller.
    * @param os the PSObjectStore object
    * @return instance of a FeatureSet object.
    */
   public static FeatureSet createFeatureSet(PSObjectStore os)
   {
      if (ms_featureSet == null)
      {
         ms_featureSet = new FeatureSet(os);
      }

      return ms_featureSet;
   }

   /**
    * Returns singleton instance of this class stored in m_featureSet.
    * @return instance of a FeatureSet object.
    * @throws UnitializedException if the configurator has not been successfully
    * created thru a call to createFeatureSet()
    */
   public static FeatureSet getFeatureSet() throws UninitializedException
   {
      if (null == ms_featureSet)
         throw new UninitializedException(
            E2Designer.getResources().getString("featureSetNotInit"));

      return ms_featureSet;
   }

   /**
    * Determines if any version of the requested feature is currently supported
    * by the server.  Does this by checking for the feature in its list of
    * features.  Check is case insensitive.
    *
    * @param featureName The name of the feature.  May be <code>null</code>.
    * @return <code>true</code> if the feature is supported by the server,
    * <code>false</code> if it is not or if a <code>null</code> featureName
    * is supplied.
    * @roseuid 39F9D26E0157
    */
   public static boolean isFeatureSupported(String featureName)
   {
      if (ms_featureList != null)
      {
         // walk list of features and check for a match
         Iterator i = ms_featureList.getFeatureSet();
         while (i.hasNext())
         {
            PSFeature feature = (PSFeature)i.next();
            if (feature.getName().equalsIgnoreCase(featureName))
               return true;
         }
      }
      return false;
   }

   /**
    * Determines if the specified version of the requested feature is currently
    * supported by the server.  Checks for the PSFeature object in its list of
    * features, and if found, gets that features version list and checks for
    * the specified version in that list.  Check for featureName is case
    * insensitive.
    *
    * @param featureName The name of the feature.  May be <code>null</code>.
    * @param versionNumber The specific version of the feature to check for.
    * @return <code>true</code> if the feature is supported by the server,
    * <code>false</code> if not found in the list or if a <code>null</code>
    * featureName is supplied.
    * @roseuid 39FD83A602DE
    */
   public static boolean isFeatureSupported(String featureName, 
                                            int versionNumber)
   {
      if (ms_featureList != null)
      {
         // walk list of features and check for a match
         Iterator i = ms_featureList.getFeatureSet();
         while (i.hasNext())
         {
            PSFeature feature = (PSFeature)i.next();
            if (feature.getName().equalsIgnoreCase(featureName))
            {
               // walk list of versions and check for a match
               Iterator v = feature.getVersionList();
               while (v.hasNext())
               {
                  PSVersion version = (PSVersion)v.next();
                  if (versionNumber == version.getNumber())
                     return true;
               }

               // if we're here, we're done checking
               break;
            }
         }
      }

      return false;
   }

   /**
    * This is a special case feature because it can be present but unlicensed,
    * which usually needs to be treated like it's not present.
    * <p>The feature set is checked first, then if it is present, the
    * server config is checked to see if it is enabled (installed & licensed).
    * 
    * @return <code>true</code> if full text search is a feature of the
    * connected server and it is enabled, <code>false</code> otherwise.
    */
   public static boolean isFTSearchEnabled()
   {
      return ms_ftSearchEnabled; 
   }

   /**
    * Removes the features listed in the list passed in from the feature set.
    * @param features a list of the features (PSFeature objects) to be removed
    * might be <code>null</code>
    */
   public static void removeFeatures (List features)
   {
      Iterator iter =  ms_featureList.getFeatureSet();
      while(iter.hasNext())
      {
         PSFeature feature = (PSFeature)iter.next();
         if(features != null)
         {
            for(int i = 0; i < features.size(); i++)
            {
               PSFeature temp = (PSFeature)features.get(i);
               if(temp.getName().equalsIgnoreCase(feature.getName()))
                  iter.remove();
            }
         }
      }
   }

   /**
    * Returns any error text resulting from error getting feature set from
    * the server. Will be <code>null</code> if no errors occurred.
    *
    * @return the error text or <code>null</code> if no errors occurred.
    */
   public String getErrorText()
   {
      return m_errorText;
   }

   /**
    * List of supported features as PSFeatureSet object. May be
    * <code>null</code> if
    * the server does not support returning this object.
    */
   private static PSFeatureSet ms_featureList = null;

   /**
    * The single instance of this class
    */
   private static FeatureSet ms_featureSet = null;

   /**
    * The error text resulting from error getting feature set from the server.
    * May be <code>null</code> if no errors occurred.
    */
   private String m_errorText = null;

   /**
    * Caches the state of the full text search engine for the currently
    * selected server. Initialized during construction, then never changes.
    * <p>Note that a workbench restart is required to pick up any changes,
    * but since this is such a rare occurrence, I see no benefit to making it
    * dynamic.
    */
   private static boolean ms_ftSearchEnabled = false;

   /**
    * Private constructor for this class - an instance of this class may only
    * be made by a call to the static method createFeatureSet.  getFeatureSet
    * is called to  obtain the instance after it has been initialized.  Requests
    * a PSFeatureSet object from the server.
    * @param os the PSObjectStore object.  May not be <code>null</code>.
    * @throws PSServerException if strServer can't be found
    * @throws PSAuthorizationException If the conn doesn't have access to the
    * object store.
    * @throws PSAuthenticationException If the login to the server failed
    * @throws IllegalArgumentException if os is <code>null</code>.
    * @roseuid 39F9D1CA0119
    */
   private FeatureSet(PSObjectStore os)
   {
      if (os == null)
         throw new IllegalArgumentException("os may not be null.");
      try
      {
         ms_featureList = os.getSupportedFeatureSet();
         PSServerConfiguration sc = os.getServerConfiguration();
         ms_ftSearchEnabled = isFeatureSupported(FEATURE_FTS) 
               && sc.isSearchEngineAvailable();
      }
      catch (Exception e)
      {
         m_errorText = e.toString();
         e.printStackTrace();
      }
   }

}
