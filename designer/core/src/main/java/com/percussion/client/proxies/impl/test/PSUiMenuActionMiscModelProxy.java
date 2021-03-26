/******************************************************************************
*
* [ PSUiMenuActionMiscModelProxy.java ]
*
* COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
* All rights reserved. This material contains unpublished, copyrighted
* work including confidential and proprietary information of Percussion.
*
*****************************************************************************/
package com.percussion.client.proxies.impl.test;

import com.percussion.client.proxies.PSUninitializedConnectionException;
import com.percussion.cms.objectstore.PSMenuContext;
import com.percussion.cms.objectstore.PSMenuMode;
import com.percussion.utils.types.PSPair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PSUiMenuActionMiscModelProxy extends 
   com.percussion.client.proxies.impl.PSUiMenuActionMiscModelProxy
{

   public PSUiMenuActionMiscModelProxy()
      throws PSUninitializedConnectionException
   {
      super();
      init();
   }   

   /**
    * Initializes the test objects
    */
   private void init()
   {
      createModes();
      createContexts();
   }   
      
   /**
    * @return collection of all menu modes, never <code>null</code>, may be
    * empty.
    */
   protected Collection<PSMenuMode> getMenuModes()
   {
      return m_modes;
   }
   
   /**
    * @return collection of all menu contexts, never <code>null</code>, may be
    * empty.
    */
   protected Collection<PSMenuContext> getMenuContexts()
   {
      return m_contexts;
   }
   
   protected Collection<String> getContextParams()
   {
      Collection<String> coll = new ArrayList<String>();
      coll.add("$sys_contentid");
      coll.add("$sys_revision");
      coll.add("$sys_contenttypeid");
      coll.add("$sys_mode");
      coll.add("$sys_slotid");
      coll.add("$sys_variantid");
      coll.add("$sys_relationshipid");
      return coll;
   }
   
   protected Collection<PSPair<String, String>> getVisibilityContexts()
   {
      Collection<PSPair<String, String>> coll = 
         new ArrayList<PSPair<String, String>>();
      coll.add(new PSPair<String, String>("1", "Assignment Types"));
      coll.add(new PSPair<String, String>("3", "Content Types"));
      coll.add(new PSPair<String, String>("4", "Object Types"));
      coll.add(new PSPair<String, String>("5", "Client Contexts"));
      coll.add(new PSPair<String, String>("6", "Checkout Status"));
      coll.add(new PSPair<String, String>("7", "Roles"));
      coll.add(new PSPair<String, String>("8", "Locales"));
      coll.add(new PSPair<String, String>("9", "Workflows"));
      coll.add(new PSPair<String, String>("10", "Publishable"));
      coll.add(new PSPair<String, String>("10", "Folder Security"));
      return coll;
   }
   
    
   protected Collection<PSPair<String, String>> getAssignmentTypes()
   {
      Collection<PSPair<String, String>> coll = 
         new ArrayList<PSPair<String, String>>();
      coll.add(new PSPair<String, String>("1", "None"));
      coll.add(new PSPair<String, String>("2", "Reader"));
      coll.add(new PSPair<String, String>("3", "Assignee"));
      return coll;
   }
   
   protected Collection<PSPair<String, String>> getCommunities()
   {
      Collection<PSPair<String, String>> coll = 
         new ArrayList<PSPair<String, String>>();
      coll.add(new PSPair<String, String>("10", "Default"));
      coll.add(new PSPair<String, String>("1001", "Enterprise Investments"));
      coll.add(new PSPair<String, String>("1002", "Enterprise Investments Admin"));
      coll.add(new PSPair<String, String>("1003", "Corporate Investments"));
      coll.add(new PSPair<String, String>("1004", "Corporate Investments Admin"));
      return coll;
   }
   
   protected Collection<PSPair<String, String>> getContentTypes()
   {      
      Collection<PSPair<String, String>> coll = 
         new ArrayList<PSPair<String, String>>();
      coll.add(new PSPair<String, String>("101", "Folder"));
      coll.add(new PSPair<String, String>("301", "Auto Index"));
      coll.add(new PSPair<String, String>("302", "Brief"));
      coll.add(new PSPair<String, String>("303", "Caledar"));
      coll.add(new PSPair<String, String>("304", "Category"));
      coll.add(new PSPair<String, String>("305", "Contacts"));
      coll.add(new PSPair<String, String>("306", "Event"));
      coll.add(new PSPair<String, String>("307", "Image"));
      coll.add(new PSPair<String, String>("308", "External Link"));
      coll.add(new PSPair<String, String>("309", "File"));
      coll.add(new PSPair<String, String>("310", "Generic Word"));
      coll.add(new PSPair<String, String>("311", "Generic"));
      coll.add(new PSPair<String, String>("312", "Home"));
      coll.add(new PSPair<String, String>("313", "NavImage"));
      coll.add(new PSPair<String, String>("314", "Navon"));
      coll.add(new PSPair<String, String>("315", "NavTree"));
      coll.add(new PSPair<String, String>("316", "Press Release"));
      return coll;
   }
   
   protected Collection<PSPair<String, String>> getObjectTypes()
   {
      Collection<PSPair<String, String>> coll = 
         new ArrayList<PSPair<String, String>>();
      coll.add(new PSPair<String, String>("1", "Item"));
      coll.add(new PSPair<String, String>("2", "Folder"));
      return coll;
   }
       
   protected Collection<PSPair<String, String>> getCheckoutStatus()
   {
      Collection<PSPair<String, String>> coll = 
         new ArrayList<PSPair<String, String>>();
      coll.add(new PSPair<String, String>("Checked In", "Checked In"));
      coll.add(new PSPair<String, String>("Checked Out by Me", "Checked Out by Me"));
      coll.add(new PSPair<String, String>("Checked Out", "Checked Out"));
      return coll;
   }
   
   protected Collection<PSPair<String, String>> getRoles()
   {
      Collection<PSPair<String, String>> coll = 
         new ArrayList<PSPair<String, String>>();
      coll.add(new PSPair<String, String>("1", "Admin"));
      coll.add(new PSPair<String, String>("2", "Author"));
      coll.add(new PSPair<String, String>("3", "Editor"));
      coll.add(new PSPair<String, String>("4", "QA"));
      return coll;
   }
   
   protected Collection<PSPair<String, String>> getLocales()
   {
      Collection<PSPair<String, String>> coll = 
         new ArrayList<PSPair<String, String>>();
      coll.add(new PSPair<String, String>("1", "US English"));
      return coll;
   }
   
   protected Collection<PSPair<String, String>> getWorkflows()
   {
      Collection<PSPair<String, String>> coll = 
         new ArrayList<PSPair<String, String>>();
      coll.add(new PSPair<String, String>("4", "Simple Workflow"));
      coll.add(new PSPair<String, String>("5", "Standard Workflow"));
      return coll;
   }
   
   protected Collection<PSPair<String, String>> getPublishableContexts()
   {
      Collection<PSPair<String, String>> coll = 
         new ArrayList<PSPair<String, String>>();
      coll.add(new PSPair<String, String>("n", "Unpublish"));
      coll.add(new PSPair<String, String>("y", "Publish"));
      coll.add(new PSPair<String, String>("i", "Ignore"));
      return coll;
   }
   
   protected Collection<PSPair<String, String>> getFolderSecurityContexts()
   {
      Collection<PSPair<String, String>> coll = 
         new ArrayList<PSPair<String, String>>();
      coll.add(new PSPair<String, String>("Read", "Read"));
      coll.add(new PSPair<String, String>("Write", "Write"));
      return coll;
   }
   
   private void createModes()
   {
      m_modes.add(new PSMenuMode(1,"SiteCentric", "Site Explorer", "Site Explorer"));
      m_modes.add(new PSMenuMode(3,"CXNAV", "Content Explorer - Navigation Pane", 
         "Content Explorer - Navigation Pane"));
      m_modes.add(new PSMenuMode(4,"CXMAIN", "Content Explorer - Main Display Area", 
         "Content Explorer - Main Display Area"));
      m_modes.add(new PSMenuMode(5,"IANAV", "Active Assembly for Documents - Navigation Pane", 
         "Active Assembly for Documents - Navigation Pane"));
      m_modes.add(new PSMenuMode(6,"IAMAIN", "Active Assembly for Documents - Main Display Area",
         "Active Assembly for Documents - Main Display Area"));
      m_modes.add(new PSMenuMode(7,"DTNAV", "Impact Analyzer", "Impact Analyzer"));
      m_modes.add(new PSMenuMode(8,"RCNAV", "Related Content Search - Main Display Area",
         "Related Content Search - Main Display Area"));
      m_modes.add(new PSMenuMode(9,"RCMAIN", "Related Content Search - Navigation Pane",
         "Related Content Search - Navigation Pane"));
      m_modes.add(new PSMenuMode(10,"PORTALS", "Portal integrations - Portlets",
         "Portal integrations - Portlets"));      
   }
   
   private void createContexts()
   {
      m_contexts.add(new PSMenuContext(1,"Parent","Parent Item","Parent Item"));
      m_contexts.add(new PSMenuContext(2,"Slot","Slot","Slot"));
      m_contexts.add(new PSMenuContext(3,"Item","Content Item","Content Item"));
      m_contexts.add(new PSMenuContext(4,"Field","Field","Field"));
      m_contexts.add(new PSMenuContext(5,"Revision","Revision","Revision"));
      m_contexts.add(new PSMenuContext(6,"SystemFolder","System Folder","System Folder"));
      m_contexts.add(new PSMenuContext(7,"SystemCategory","System Category","System Category"));
      m_contexts.add(new PSMenuContext(8,"SystemView","System View","System View"));
      m_contexts.add(new PSMenuContext(9,"Folder","Folder","Folder"));
      m_contexts.add(new PSMenuContext(10,"View","View","View"));
      m_contexts.add(new PSMenuContext(11,"Category","Category","Category"));
      m_contexts.add(new PSMenuContext(12,"NewSearch","New Search","New Search"));
      m_contexts.add(new PSMenuContext(13,"SavedSearch","Saved Search","Saved Search"));
      m_contexts.add(new PSMenuContext(14,"SlotItem","Slot Item","Slot Item"));
      m_contexts.add(new PSMenuContext(15,"SystemSite","System Site","System Site"));
      m_contexts.add(new PSMenuContext(16,"DTItem","Item","Item"));
      m_contexts.add(new PSMenuContext(17,"CustomSearch","Custom Search","Custom Search"));
      m_contexts.add(new PSMenuContext(18,"StandardSearch","Standard Search","Standard Search"));
      m_contexts.add(new PSMenuContext(19,"EmptySearch","Empty Search","Empty Search"));
      m_contexts.add(new PSMenuContext(20,"Site","Site","Site"));
      m_contexts.add(new PSMenuContext(21,"SiteSubfolder","Site Subfolder","Site Subfolder"));
      m_contexts.add(new PSMenuContext(22,"FolderRef","Folder Reference","Folder Reference"));
   }
   
   private List<PSMenuMode> m_modes = new ArrayList<PSMenuMode>();
   private List<PSMenuContext> m_contexts = new ArrayList<PSMenuContext>();
   
   
}
