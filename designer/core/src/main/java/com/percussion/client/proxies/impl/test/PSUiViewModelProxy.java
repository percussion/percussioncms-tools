/******************************************************************************
 *
 * [ PSUiViewModelProxy.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
/**
 * 
 */
package com.percussion.client.proxies.impl.test;

import com.percussion.client.PSObjectTypes;
import com.percussion.cms.objectstore.PSSearch;

import java.io.File;

/**
 * @author erikserating
 *
 */
public class PSUiViewModelProxy extends PSUiSearchModelProxy
{
   /**
    * Default ctor. Invokes base class ctor
    * {@link PSUiSearchModelProxy#PSUiSearchModelProxy(PSObjectTypes)} with
    * {@link PSObjectTypes#UI_VIEW}.
    */
   public PSUiViewModelProxy()
   {
      super(PSObjectTypes.UI_VIEW);     
   }   
      
   /**
    * Load the existing objects from the repository.
    * @throws PSProxyTestException  
    */
   @SuppressWarnings("unchecked")
   protected void loadFromRepository() throws PSProxyTestException 
   {
      m_repositoryMap.clear();
      if (!getRepositoryFile().exists())
      {
         // If repository does not exist
         // create a few to start with
         for (int i = 0; i < 5; i++)
         {
            String[] names = new String[]{
               "customInbox", "customOutbox", "stdRecent", "stdSession", "stdAll"};   //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$ //$NON-NLS-4$//$NON-NLS-5$
            PSSearch def = createNewObject(names[i]);
            switch (i)
            {
               case 0:
               case 1:
                  def.setCustom(true);
                  def.setUrl("http://Rhythmyx/searchapp/findit" + i + ".html");
                  break;
            }
            m_repositoryMap.put(objectToReference(def), def);
         }
         // and save to repository
         saveRepository(m_repositoryMap, getRepositoryFile());
      }
      else
      {
         m_repositoryMap = 
            (PSRepositoryMap)loadRepository(getRepositoryFile());
      }
   }
   
   /* 
    * @see com.percussion.client.proxies.impl.test.PSUiSearchModelProxy#
    * getRepositoryFile()
    */
   @Override
   protected File getRepositoryFile()
   {
      return new File("UiView_repository.xml"); //$NON-NLS-1$
   }  
   
   
   

   

}
