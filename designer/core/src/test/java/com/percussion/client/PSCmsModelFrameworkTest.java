/******************************************************************************
 *
 * [ PSCmsModelFrameworkTest.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client;

import com.percussion.client.models.IPSCmsModel;
import com.percussion.client.models.IPSHierarchyManager;
import com.percussion.client.models.IPSModelListener;
import com.percussion.client.models.PSLockException;
import com.percussion.client.objectstore.IPSUiAssemblyTemplate;
import com.percussion.cms.objectstore.PSAction;
import com.percussion.cms.objectstore.PSDisplayFormat;
import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.cms.objectstore.PSMenuContext;
import com.percussion.cms.objectstore.PSMenuMode;
import com.percussion.cms.objectstore.PSSearch;
import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSApplicationFile;
import com.percussion.design.objectstore.PSContentEditorSystemDef;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.design.objectstore.PSSharedFieldGroup;
import com.percussion.extension.PSExtensionDef;
import com.percussion.i18n.PSLocale;
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.services.assembly.data.PSTemplateSlot;
import com.percussion.services.content.data.PSAutoTranslation;
import com.percussion.services.content.data.PSKeyword;
import com.percussion.services.filter.data.PSItemFilter;
import com.percussion.services.security.IPSAcl;
import com.percussion.services.security.IPSAclEntry;
import com.percussion.services.security.PSPermissions;
import com.percussion.services.security.PSTypedPrincipal;
import com.percussion.services.security.data.PSAclEntryImpl;
import com.percussion.services.security.data.PSCommunity;
import com.percussion.services.system.data.PSMimeContentAdapter;
import com.percussion.services.system.data.PSSharedProperty;
import com.percussion.services.ui.data.PSHierarchyNode;
import com.percussion.services.workflow.data.PSWorkflow;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.security.IPSTypedPrincipal.PrincipalTypes;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.utils.types.PSPair;
import junit.framework.TestCase;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.log4j.PropertyConfigurator;
import org.junit.experimental.categories.Category;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * This unit test covers the following classes:
 * <ol>
 * <li>{@link com.percussion.client.PSCoreFactory}</li>
 * <li>{@link com.percussion.client.models.IPSCmsModel}</li>
 * <li>{@link com.percussion.client.models.impl.PSCmsModel}</li>
 * <li>Partial testing of the classes in the com.percussion.client.proxies
 * package and sub-packages.</li>
 * <ol>
 * The testing walks through all known object types and runs a battery of tests
 * against the types. Type specific knowledge is used to verify correct
 * behavior. todo - add tests for creating standalone objects and adding them
 * back to model
 * 
 * @author paulhoward
 */
@Category(IntegrationTest.class)
public class PSCmsModelFrameworkTest extends TestCase
{
   public PSCmsModelFrameworkTest()
   {
      PropertyConfigurator c = new PropertyConfigurator();
      c.configure(PSCmsModelFrameworkTest.class.getResourceAsStream("/log4j.properties"));

      Level l = Level.ERROR;
      if (System.getProperty("DEBUG") != null)
      {
         l = Level.DEBUG;
      }
      else if (System.getProperty("WARN") != null)
      {
         l = Level.WARN;
      }
      LogManager.getRootLogger().atLevel(l);
   }

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
      PSCoreFactory.getInstance().clearModelProxyCache();
      PSCoreFactory.getInstance().setClientSessionId(ms_sessionid);
      // I want the catalog test to run first
      // TestSuite s = new TestSuite("com.percussion.client.testCatalog");
      // s.addTestSuite(PSCmsModelFrameworkTest.class);
      // return s;
   }

   /**
    * Little interface to make anonymous classes so work can be done against
    * multiple types.
    * 
    * @author paulhoward
    */
   private interface Processor
   {
      public void process(PSObjectType type) throws Exception;
   }

   /**
    * Performs some non-standard cataloging ops. Cataloging is tested via the
    * other tests in this class.
    * 
    * @throws Exception
    */
   public void testCatalog() throws Exception
   {
      Processor p = new Processor()
      {
         public void process(PSObjectType type) throws Exception
         {
            IPSCmsModel model = PSCoreFactory.getInstance().getModel(
               type.getPrimaryType());
            Collection<IPSReference> refs1 = model.catalog(true);
            Collection<IPSReference> refs2 = model.catalog();
            assertTrue(refs1.size() == refs2.size());
            for (IPSReference ref : refs1)
            {
               boolean found = false;
               for (IPSReference ref2 : refs2)
               {
                  if (ref == ref2)
                  {
                     found = true;
                     break;
                  }
               }
               assertTrue(found);
            }

            // shouldn't be from cache
            Collection<IPSReference> refs3 = model.catalog(true);
            assertTrue(refs2.size() == refs3.size());
            for (IPSReference ref2 : refs2)
            {
               boolean found = false;
               for (IPSReference ref3 : refs3)
               {
                  if (ref2 == ref3)
                  {
                     found = true;
                     break;
                  }
               }
               assertFalse(found);
            }

            // filtered cataloging - templates have 2 types
            PSObjectType firstType = null;
            PSObjectType secondType = null;
            int originalSize = refs3.size();
            for (IPSReference ref : refs3)
            {
               if (firstType == null)
                  firstType = ref.getObjectType();
               else
               {
                  if (!ref.getObjectType().equals(firstType))
                  {
                     secondType = ref.getObjectType();
                     break;
                  }
               }
            }
            if (secondType != null)
            {
               refs3 = model.catalog(false, secondType);
               assertTrue(refs3.size() < originalSize);
               for (IPSReference ref : refs3)
               {
                  if (!ref.getObjectType().equals(secondType))
                     fail("Filtered catalog returned wrong type.");
               }
            }
         }
      };
      processTypes(p);
   }

   /**
    * Catalogs existing types and loads and saves them. If the type does not
    * support loading, this method skips it.
    * 
    * @throws Exception
    */
   public void testLoadPersisted() throws Exception
   {
      Processor p = new Processor()
      {
         public void process(PSObjectType type) throws Exception
         {
            if (doesNotSupportLockedLoadAndSave(type.getPrimaryType()))
               return;

            IPSCmsModel model = PSCoreFactory.getInstance().getModel(
               type.getPrimaryType());
            Collection<IPSReference> catalogedRefs = model.catalog(false);

            try
            {
               for (IPSReference testRef : catalogedRefs)
               {
                  Object o = model.load(testRef, false, false);
                  try
                  {
                     // can't save unlocked object
                     model.save(testRef, true);
                     fail();
                  }
                  catch (PSLockException e)
                  {
                     // should get an exception
                  }
                  // first load w/ lock must query server
                  Object o2 = model.load(testRef, true, false);
                  assertFalse(type.toString(), o == o2);

                  IPSGuid preSaveId = testRef.getId();
                  String preSaveName = testRef.getName();
                  // until lock is released, always dealing w/ same, in-memory
                  // object
                  model.save(testRef, false);
                  Object o4 = model.load(testRef, false, false);
                  // additional loads after lock should be same object
                  assertTrue(type.toString(), o2 == o4);

                  // id should be same after save
                  if (preSaveId != null)
                  {
                     assertTrue(type.toString(), preSaveId.equals(testRef
                        .getId()));
                  }

                  // name should be same after save
                  if (preSaveName != null)
                  {
                     assertTrue(type.toString(), preSaveName.equals(testRef
                        .getName()));
                  }

                  // after releasing lock, object must be re-retrieved from
                  // server
                  model.releaseLock(testRef);
                  o4 = model.load(testRef, false, false);
                  assertFalse(type.toString(), o == o4);

                  model.releaseLock(testRef);
               }
            }
            catch (UnsupportedOperationException e)
            {
               fail(type.toString()
                  + ": error or add type to doesNotSupportLockedLoadAndSave()");
            }
         }
      };
      processTypes(p);
   }

   /**
    * Tests some specialized saving scenarios. Basic saving is tested in the
    * load tests. If the type does not support loading, this method returns
    * immediately.
    * 
    * @throws Exception
    */
   public void testSave() throws Exception
   {
      Processor p = new Processor()
      {
         public void process(PSObjectType type) throws Exception
         {
            if (doesNotSupportLockedLoadAndSave(type.getPrimaryType()))
               return;

            IPSCmsModel model = PSCoreFactory.getInstance().getModel(
               type.getPrimaryType());
            Collection<IPSReference> catalogedRefs = model.catalog(false);
            if (isSingletonType(type.getPrimaryType()))
               return;
            assertTrue("Need at least 2 objects for this test: "
               + type.toString(), catalogedRefs.size() > 1);

            try
            {
               Iterator<IPSReference> iter = catalogedRefs.iterator();
               IPSReference ref1 = iter.next();
               IPSReference ref2 = iter.next();
               model.load(ref1, true, false);
               IPSReference[] toSave =
               {
                  ref1, ref2
               };
               // one locked, one unlocked
               try
               {
                  // can't save unlocked object
                  model.save(toSave, true);
                  fail("saved an unlocked ref: " + type.toString());
               }
               catch (PSMultiOperationException e)
               {
                  // should get an exception
                  assertFalse(type.toString(),
                     e.getResults()[0] instanceof Throwable);
                  assertTrue(type.toString(),
                     e.getResults()[1] instanceof PSLockException);
               }

               // both unlocked
               try
               {
                  // can't save unlocked object
                  model.save(toSave, true);
                  fail("saved an unlocked ref: " + type.toString());
               }
               catch (PSMultiOperationException e)
               {
                  // should get an exception
                  assertTrue(type.toString(),
                     e.getResults()[0] instanceof PSLockException);
                  assertTrue(type.toString(),
                     e.getResults()[1] instanceof PSLockException);
               }

               // both locked
               model.load(ref1, true, false);
               model.load(ref2, true, false);
               assertTrue(model.save(toSave, true).length == 2);
            }
            catch (UnsupportedOperationException e)
            {
               fail("error or add type to doesNotSupportLockedLoadAndSave(): "
                  + type.toString());
            }
         }
      };
      processTypes(p);
   }

   /**
    * If the primary type does not support create, this test does nothing. There
    * is another test for these types - {@link #testLoadPersisted}.
    * 
    * @throws Exception
    */
   public void testCreateAndLoad() throws Exception
   {
      Processor p = new Processor()
      {
         public void process(PSObjectType type) throws Exception
         {
            if (doesNotSupportCreate(type.getPrimaryType()))
               return;

            IPSCmsModel model = PSCoreFactory.getInstance().getModel(
               type.getPrimaryType());

            IPSReference[] refs;
            // test contract - null name list
            refs = model.create(type, (List<String>) null);
            assertTrue(refs.length == 1);
            model.releaseLock(refs[0]);

            // test contract - null type
            try
            {
               refs = model.create(null, (List<String>) null);
               fail("allowed null type");
            }
            catch (IllegalArgumentException success)
            {}

            // test contract - invalid type
            try
            {
               IPSPrimaryObjectType pt = PSObjectTypes.COMMUNITY;
               if (pt == type.getPrimaryType())
                  pt = PSObjectTypes.CONTENT_TYPE;
               PSObjectType t = new PSObjectType(pt);
               refs = model.create(t, (List<String>) null);
               fail("allowed invalid type");
            }
            catch (RuntimeException success)
            {
               assertTrue(success instanceof IllegalArgumentException);
            }

            // test contract - null/empty names
            List<String> names = new ArrayList<String>();
            try
            {
               names.add(null);
               names.add("");
               names.add("byfu");
               names.add(" ");
               refs = model.create(type, names);
               fail("allowed invalid names");
            }
            catch (PSMultiOperationException success)
            {
               Object[] results = success.getResults();
               assertTrue(results.length == 4);
               assertTrue(results[0] instanceof IllegalArgumentException);
               assertTrue(results[1] instanceof IllegalArgumentException);
               assertTrue(results[2] instanceof IPSReference);
               model.releaseLock((IPSReference) results[2]);
               assertTrue(results[3] instanceof IllegalArgumentException);
            }

            // dupe names in same request
            try
            {
               names.clear();
               names.add("byfu");
               names.add("byfu");
               refs = model.create(type, names);
               fail("allowed dupe name");
            }
            catch (PSMultiOperationException success)
            {
               Object[] results = success.getResults();
               assertTrue(results.length == 2);
               assertTrue(results[1] instanceof PSDuplicateNameException);
               model.releaseLock((IPSReference) results[0]);
            }

            // dupe name on 2nd create w/o persisting first
            try
            {
               names.clear();
               names.add("fuby");
               refs = model.create(type, names);
               model.create(type, names);
               fail("allowed dupe name");
            }
            catch (PSMultiOperationException success)
            {
               Object[] results = success.getResults();
               assertTrue(results.length == 1);
               assertTrue(results[0] instanceof PSDuplicateNameException);
            }
            finally
            {
               model.releaseLock(refs[0]);
            }

            refs = null;
            names.clear();

            Collection<IPSReference> initialCatalogedRefs = model
               .catalog(false);
            Collection<IPSReference> createdObjects = 
               new ArrayList<IPSReference>();
            IPSReference testRef = null;
            Collection<IPSReference> catalogedRefs = null;
            names.add("foobar2");
            refs = model.create(type, names);
            testRef = refs[0];
            assertFalse(testRef.isPersisted());

            catalogedRefs = model.catalog(false);
            assertFalse(initialCatalogedRefs.contains(testRef));
            assertTrue(catalogedRefs.contains(testRef));

            names.clear();
            names.add("foobar3");
            names.add("foobar4");
            names.add("foobar5");
            names.add("foobar6");
            refs = model.create(type, names);
            createdObjects.addAll(Arrays.asList(refs));

            Collection<IPSReference> cleanup = new ArrayList<IPSReference>();
            try
            {
               Object o = model.load(testRef, false, false);
               Object o2 = model.load(testRef, true, false);
               assertTrue(o == o2);

               Object previous = null;
               for (IPSReference ref : refs)
               {
                  Object o3 = model.load(ref, true, false);
                  assertFalse(o == o3);
                  if (previous != null)
                     assertFalse(previous.equals(o3));

                  previous = o3;
                  assertFalse(o.equals(o3));
               }

               // until lock is released, always dealing w/ same, in-memory
               // object
               model.save(testRef, false);
               cleanup.add(testRef);
               createdObjects.remove(testRef);
               Object o4 = model.load(testRef, false, false);
               assertTrue(o == o4);

               // after releasing lock, object must be re-retrieved from server
               model.releaseLock(testRef);
               o4 = model.load(testRef, false, false);
               assertFalse(o == o4);

               model.delete(testRef);
               cleanup.remove(testRef);
               catalogedRefs = model.catalog(false);
               assertFalse(catalogedRefs.contains(testRef));

               // cleanup
               for (IPSReference ref : createdObjects)
                  model.releaseLock(ref);
            }
            catch (UnsupportedOperationException e)
            {
               // these types don't allow loading
               Enum pt = type.getPrimaryType();
               assertTrue(pt == PSObjectTypes.ROLE
                  || pt == PSObjectTypes.WORKFLOW
                  || pt == PSObjectTypes.SITE);
            }
            finally
            {
               // restore repository to its original state
               for (IPSReference ref : cleanup)
               {
                  model.delete(ref);
               }
            }
         }
      };
      processTypes(p);
   }

   /**
    * Catalogs, then attempts to load and validate the type of each cataloged
    * object.
    */
   public void testLoadObjects()
      throws Exception
   {
      Processor p = new Processor()
      {
         public void process(PSObjectType type) throws Exception
         {
            IPSCmsModel model = PSCoreFactory.getInstance().getModel(
               type.getPrimaryType());

            Collection<IPSReference> refs = model.catalog(false);
            if (refs.size() == 0)
            {
               fail("No design objects for type " + type.toString()
                  + " - betwixt broken?");
            }
            loadObjects(model, refs);
         }
      };
      processTypes(p);
   }

   /**
    * Catalogs, then attempts to load and validate the type of each cataloged
    * object. It does this recursively to a depth of 3.
    */
   public void testLoadHierarchyObjects()
      throws Exception
   {
      Processor p = new Processor()
      {
         public void process(PSObjectType type) throws Exception
         {
            IPSCmsModel model = PSCoreFactory.getInstance().getModel(
               type.getPrimaryType());
            IPSHierarchyManager mgr = model
               .getHierarchyManager(getTreeName(type.getPrimaryType()));
            if (null == mgr && !supportsHierarchyManager(type.getPrimaryType()))
               return;
            else if (mgr == null)
               fail("hierarchy mgr required");

            loadHierarchyObjects(model, mgr, null, 0);
         }
      };
      processFileHierarchyTypes(p);
   }

   /**
    * Catalogs the parent using the supplied manager, then attempts to load all
    * container nodes but only one leaf node in each container. using the
    * supplied model. The method recurses to a depth of 3.
    * 
    * @param model Assumed not <code>null</code>.
    * @param mgr Assumed not <code>null</code>.
    * @param parent Should be <code>null</code> the first time in.
    * @param depth The current depth of the tree.
    */
   private void loadHierarchyObjects(IPSCmsModel model, IPSHierarchyManager mgr, 
         IPSHierarchyNodeRef parent, int depth)
      throws Exception
   {
      Collection<IPSHierarchyNodeRef> refs = mgr.getChildren(parent);
      if (parent == null)
         assertTrue("No design objects - betwixt broken?", refs.size() > 0);
      //Let us keep only one leaf node
      Collection<IPSHierarchyNodeRef> newRefs = new ArrayList<IPSHierarchyNodeRef>();
      boolean addedLeaf = false;
      for (IPSHierarchyNodeRef ref : refs)
      {
         if(ref.isContainer())
         {
            newRefs.add(ref);
         }
         else if(!ref.isContainer() && !addedLeaf)
         {
            newRefs.add(ref);
            addedLeaf = true;
         }
      }
      loadObjects(model, newRefs);
      if (depth > 2)
         return;
      for (IPSHierarchyNodeRef ref : newRefs)
      {
         loadHierarchyObjects(model, mgr, ref, depth + 1);
      }
   }

   /**
    * Loads the associated object for every supplied ref and attempts to cast it
    * to the proper type. <em>Note<em>
    * <p>The data types checked in this method must match what is documented
    * in the {@link PSObjectTypes} enumeration.
    * 
    * @param model Assumed not <code>null</code>.
    * 
    * @param refs Assumed not <code>null</code>. <code>null</code> entries will
    * be skipped.
    */
   @SuppressWarnings("unchecked")
   // we have some casts below that would warn
   private void loadObjects(IPSCmsModel model,
         Collection<? extends IPSReference> refs)
      throws Exception
   {
      if (refs.size() == 0)
         return;

      PSObjectType type = null;
      for (IPSReference ref : refs)
      {
         if (ref == null)
            continue;

         assertTrue(ref.isPersisted());
         
         type = ref.getObjectType();

         Object o = model.load(ref, false, false);
         // some types return null for their data object
         if (type.getPrimaryType() == PSObjectTypes.AUTO_TRANSLATION_SET)
         {
            assertTrue(type.toString(), o instanceof Set);
            Set s = (Set) o;
            if (s.size() > 0)
            {
               assertTrue(type.toString(),
                  s.iterator().next() instanceof PSAutoTranslation);
            }
         }
         else if (type.getPrimaryType() == PSObjectTypes.COMMUNITY)
            assertTrue(type.toString(), o instanceof PSCommunity);
         else if (type.getPrimaryType() == PSObjectTypes.CONFIGURATION_FILE
            || type.getPrimaryType() == PSObjectTypes.LEGACY_CONFIGURATION)
         {
            assertTrue(type.toString(), o instanceof PSMimeContentAdapter);
         }
         else if (type.getPrimaryType() == PSObjectTypes.CONTENT_EDITOR_CONTROLS)
         {
            // all sub types return the same object type
            assertTrue(
               type.toString(),
               o instanceof com.percussion.services.system.data.PSMimeContentAdapter);
         }
         else if (type.getPrimaryType() == PSObjectTypes.CONTENT_TYPE)
            assertTrue(type.toString(), o instanceof PSItemDefinition);
         else if (type.getPrimaryType() == 
            PSObjectTypes.CONTENT_TYPE_SYSTEM_CONFIG)
         {
            assertTrue(type.toString(), o instanceof PSContentEditorSystemDef);
         }
         else if (type.getPrimaryType() == PSObjectTypes.EXTENSION)
            assertTrue(type.toString(), o instanceof PSExtensionDef);
         else if (type.getPrimaryType() == PSObjectTypes.ITEM_FILTER)
            assertTrue(type.toString(), o instanceof PSItemFilter);
         else if (type.getPrimaryType() == PSObjectTypes.KEYWORD)
            assertTrue(type.toString(), o instanceof PSKeyword);
         else if (type.getPrimaryType() == PSObjectTypes.LOCAL_FILE)
         {
            Enum sType = type.getSecondaryType();
            if (sType == PSObjectTypes.FileSubTypes.FOLDER)
               assertNull(type.toString(), o);
            else if (sType == PSObjectTypes.FileSubTypes.FILE)
               assertTrue(type.toString(), o instanceof PSMimeContentAdapter);
            else
               fail(type.toString() + ": should not be possible");
         }
         else if (type.getPrimaryType() == PSObjectTypes.LOCALE)
            assertTrue(type.toString(), o instanceof PSLocale);
         else if (type.getPrimaryType() == PSObjectTypes.RELATIONSHIP_TYPE)
            assertTrue(type.toString(), o instanceof PSRelationshipConfig);
         else if (type.getPrimaryType() == PSObjectTypes.RESOURCE_FILE)
         {
            Enum sType = type.getSecondaryType();
            if (sType == PSObjectTypes.FileSubTypes.FOLDER)
               assertNull(type.toString(), o);
            else if (sType == PSObjectTypes.FileSubTypes.FILE)
               assertTrue(
                  type.toString(),
                  o instanceof com.percussion.services.system.data.PSMimeContentAdapter);
            else
               fail(type.toString() + ": should not be possible");
         }
         else if (type.getPrimaryType() == PSObjectTypes.ROLE)
            assertNull(type.toString(), o);
         else if (type.getPrimaryType() == PSObjectTypes.SHARED_FIELDS)
            assertTrue(type.toString(), o instanceof PSSharedFieldGroup);
         else if (type.getPrimaryType() == PSObjectTypes.SHARED_PROPERTY)
            assertTrue(type.toString(), o instanceof PSSharedProperty);
         else if (type.getPrimaryType() == PSObjectTypes.SLOT)
            assertTrue(type.toString(), o instanceof IPSTemplateSlot);
         else if (type.getPrimaryType() == PSObjectTypes.TEMPLATE)
            assertTrue(type.toString(), o instanceof IPSUiAssemblyTemplate);
         else if (type.getPrimaryType() == PSObjectTypes.UI_ACTION_MENU)
            assertTrue(type.toString(), o instanceof PSAction);
         else if (type.getPrimaryType() == PSObjectTypes.UI_ACTION_MENU_MISC)
         {
            Enum sType = type.getSecondaryType();
            if (sType == 
               PSObjectTypes.UiActionMenuMiscSubTypes.CONTEXT_PARAMETERS)
            {
               assertTrue(type.toString(), o instanceof String);
            }
            else if (sType == PSObjectTypes.UiActionMenuMiscSubTypes.CONTEXTS)
               assertTrue(type.toString(), o instanceof PSMenuContext);
            else if (sType == PSObjectTypes.UiActionMenuMiscSubTypes.MODES)
               assertTrue(type.toString(), o instanceof PSMenuMode);
            else if (sType == 
               PSObjectTypes.UiActionMenuMiscSubTypes.VISIBILITY_CONTEXTS)
            {
               assertTrue(type.toString(), o instanceof Collection);
               Collection c = (Collection) o;
               if (c.size() > 0)
               {
                  Object entry = c.iterator().next();
                  assertTrue(type.toString(), entry instanceof PSPair);
                  PSPair p = (PSPair) entry;
                  assertTrue(type.toString(), p.getFirst() instanceof String);
                  assertTrue(type.toString(), p.getSecond() instanceof String);
               }
            }
            else
               fail(type.toString() + ": should not be possible");
         }
         else if (type.getPrimaryType() == PSObjectTypes.UI_DISPLAY_FORMAT)
            assertTrue(type.toString(), o instanceof PSDisplayFormat);
         else if (type.getPrimaryType() == PSObjectTypes.UI_SEARCH)
            assertTrue(type.toString(), o instanceof PSSearch);
         else if (type.getPrimaryType() == PSObjectTypes.UI_VIEW)
            assertTrue(type.toString(), o instanceof PSSearch);
         else if (type.getPrimaryType() == PSObjectTypes.USER_FILE)
         {
            Enum sType = type.getSecondaryType();
            if (sType == PSObjectTypes.UserFileSubTypes.WORKBENCH_FOLDER)
               assertTrue(type.toString(), o instanceof PSHierarchyNode);
            else if (sType == PSObjectTypes.UserFileSubTypes.PLACEHOLDER)
            {
               assertTrue(type.toString(), o instanceof PSHierarchyNode);
               PSHierarchyNode n = (PSHierarchyNode) o;
               String val = n.getProperty("guid");
               assertTrue(type.toString(), !StringUtils.isBlank(val));
            }
            else
               fail(type.toString() + ": should not be possible");
         }
         else if (type.getPrimaryType() == PSObjectTypes.WORKFLOW)
            assertTrue(type.toString(), o instanceof PSWorkflow);
         else if (type.getPrimaryType() == PSObjectTypes.SITE)
            assertNull(type.toString(), o);
         else if (type.getPrimaryType() == PSObjectTypes.XML_APPLICATION)
            assertTrue(type.toString(), o instanceof PSApplication);
         else if (type.getPrimaryType() == PSObjectTypes.XML_APPLICATION_FILE)
         {
            Enum sType = type.getSecondaryType();
            if (sType == PSObjectTypes.FileSubTypes.FOLDER)
               assertNull(type.toString(), o);
            else if (sType == PSObjectTypes.FileSubTypes.FILE)
               assertTrue(type.toString(), o instanceof PSApplicationFile);
            else
               fail(type.toString() + ": should not be possible");
         }
         else if (type.getPrimaryType() == PSObjectTypes.DB_TYPE)
            assertNull(type.toString(), o);
         else
            fail(type.toString() + ": Unknown object type: ");
      }
   }

   /**
    * Creates a new copy, then creates a copy of that and verifies they differ.
    * Skips types that don't support create.
    * 
    * @throws Exception
    */
   public void testCreateCopies() throws Exception
   {
      Processor p = new Processor()
      {
         public void process(PSObjectType type) throws Exception
         {
            try
            {
               if (doesNotSupportCreate(type.getPrimaryType()))
                  return;

               IPSCmsModel model = PSCoreFactory.getInstance().getModel(
                  type.getPrimaryType());
               Collection<IPSReference> createdObjects = new ArrayList<IPSReference>();
                  new ArrayList<IPSReference>();
               
               IPSReference[] refs = null;
               
               //test contract - null array
               try
               {
                  refs = model.create((IPSReference[]) null, null);
                  fail("allowed null type");
               }
               catch (IllegalArgumentException success)
               {}

               //test contract - empty array
               refs = model.create(new IPSReference[0], null);

              
               List<String> names = new ArrayList<String>();
               names.add("foobar");
               refs = model.create(type, names);
               createdObjects.addAll(Arrays.asList(refs));
               IPSReference[] copy = model.create(refs, null);
               assertFalse(type.toString(), refs[0].equals(copy[0]));
               assertFalse(type.toString(), model.load(refs[0], false, false)
                     .equals(model.load(copy[0], false, false)));

               // cleanup
               for (IPSReference ref : createdObjects)
                  model.releaseLock(ref);

               // test duplicate name
               names.add("foobar");
               try
               {
                  refs = model.create(type, names);
                  fail("Should have caught duplicate names.");
               }
               catch (PSMultiOperationException e)
               {
                  Object[] results = e.getResults();
                  assertFalse(type.toString(), results[0] instanceof Exception);
                  assertTrue(type.toString(),
                        results[1] instanceof PSDuplicateNameException);
                  refs = new IPSReference[] {(IPSReference) results[0]};
               }
               
               //test duplicate names on create
               IPSReference[] copyRef1 = model.create(refs, null);
               IPSReference[] copyRef2 = model.create(refs, null);
               assertFalse(type.toString(), copyRef1.equals(copyRef2));
               //can't use equals because objects may differ by id
               assertFalse(type.toString(), copyRef1[0].getName().equals(
                     copyRef2[0].getName()));
               
               //cleanup
               model.releaseLock(refs[0]);
               model.releaseLock(copyRef1[0]);
               model.releaseLock(copyRef2[0]);
            }
            catch (UnsupportedOperationException e)
            {
               // these types don't allow creation of new ones
               assertTrue(type.toString(), doesNotSupportCreate(type
                     .getPrimaryType()));
            }
         }
      };
      processTypes(p);
   }

   /**
    * There are 5 scenarios:
    * <ol>
    * <li>Object never persisted</li>
    * <li>Persisted object never loaded</li>
    * <li>Persisted object has been loaded, but is not locked.</li>
    * <li>Persisted object has been loaded and locked and then saved</li>
    * <li>Persisted object has been loaded and locked and then discarded</li>
    * <ol>
    */
   public void testRename() throws Exception
   {
      Processor p = new Processor()
      {
         public void process(PSObjectType type) throws Exception
         {
            if (doesNotSupportRename(type.getPrimaryType()))
               return;

            IPSCmsModel model = PSCoreFactory.getInstance().getModel(
               type.getPrimaryType());
            Collection<IPSReference> cleanup = new ArrayList<IPSReference>();
            try
            {
               IPSReference ref = null;
               // test contract - null ref
               try
               {
                  model.rename(null, "feeble");
                  fail(type.toString() + ": allowed null ref");
               }
               catch (IllegalArgumentException success)
               {}

               //test contract - null name
               try
               {
                  ref = model.create(type, "feeble");
                  model.rename(ref, null);
                  fail(type.toString() + ": allowed null name");
               }
               catch (IllegalArgumentException success)
               {}

               //test contract - empty name
               try
               {
                  model.rename(ref, "");
                  fail(type.toString() + ": allowed empty name");
               }
               catch (IllegalArgumentException success)
               {}

               //test contract - whitespace name
               try
               {
                  model.rename(ref, " ");
                  fail(type.toString() + ": allowed whitespace name");
               }
               catch (IllegalArgumentException success)
               {}

               // test contract - name w/ whitespace
               String baseName = "abc";
               model.rename(ref, "  " + baseName + "  ");
               assertTrue(type.toString(), ref.getName().equals(baseName));

               // cleanup
               model.releaseLock(ref);
               ref = null;

               String originalName = "foobar10";
               ref = model.create(type, originalName);
               String name = ref.getName();

               String newName = originalName + "xx";
               model.rename(ref, newName);
               assertFalse(type.toString(), name.equals(ref.getName()));

               model.rename(ref, originalName);
               assertTrue(type.toString(), ref.getName().equals(originalName));
               model.rename(ref, newName);
               assertTrue(type.toString(), ref.getName().equals(newName));

               // can't easily validate the name in the object

               /*
                * renaming a locked object, then not saving it should not lose
                * the name
                */
               model.save(ref, true);
               cleanup.add(ref);
               model.flush(ref);
               ref = findRef(model, newName);
               model.load(ref, true, false);
               assertTrue(type.toString(), ref.getName().equals(newName));
               model.rename(ref, originalName);
               assertTrue(type.toString(), ref.getName().equals(originalName));
               model.releaseLock(ref);
               model.flush(ref);
               assertTrue(type.toString(), findRef(model, originalName) != null);
            }
            catch (UnsupportedOperationException e)
            {
               // these types don't allow creation of new ones
               assertTrue(type.toString(), doesNotSupportCreate(type
                  .getPrimaryType()));
            }
            finally
            {
               // restore repository to its original state
               for (IPSReference ref : cleanup)
               {
                  model.delete(ref);
               }
            }
         }
      };
      processTypes(p);
   }

   /**
    * There are 5 scenarios:
    * <ol>
    * <li>Object never persisted</li>
    * <li>Persisted object never loaded</li>
    * <li>Persisted object has been loaded, but is not locked.</li>
    * <li>Persisted object has been loaded and locked and then saved</li>
    * <li>Persisted object has been loaded and locked and then discarded</li>
    * <ol>
    */
   public void testAcls() throws Exception
   {
      // fixme - implement when acls implemented
      Processor p = new Processor()
      {
         public void process(PSObjectType type) throws Exception
         {
            if(doesNotSupportCreate(type.getPrimaryType()))
            {
               return;
            }
            PSTypedPrincipal ownerPrincipal = new PSTypedPrincipal(
               PSCoreFactory.getInstance().getConnectionInfo().getUserid(),
               PrincipalTypes.USER);
            IPSCmsModel model = PSCoreFactory.getInstance().getModel(
               type.getPrimaryType());
            // scenario 1 - typical wizard case
            // 1. create a new object
            // 2. create a new acl by loading it
            // 3. add an entry to acl
            // 4. save the acl
            // 5. modify the object
            // 6. save the object
            IPSReference owner = model.create(type, "buffalo1");
            IPSAcl acl = (IPSAcl) model.loadAcl(owner, true);
            try
            {
               Enumeration e = acl.entries();
               while (e.hasMoreElements())
               {
                  PSAclEntryImpl entry = (PSAclEntryImpl) e.nextElement();
                  entry.addPermission(PSPermissions.DELETE);
               }
               PSAclEntryImpl entry = new PSAclEntryImpl(new PSTypedPrincipal(
                  "foo", PrincipalTypes.COMMUNITY));
               entry.addPermission(PSPermissions.RUNTIME_VISIBLE);
               acl.addEntry(ownerPrincipal, entry);
               model.saveAcl(owner, true);
               model.save(owner, true);
               // scenario 2 - Normal editing typically with a modal dalog box
               // for acls.
               // 1. load ACL of an existing object
               // 2. remove and add a few entries and change permissions
               // 3. save the acl
               acl = (IPSAcl) model.loadAcl(owner, true);
               e = acl.entries();
               // find the entry name "foo"
               IPSAclEntry remove = null;
               while (e.hasMoreElements())
               {
                  entry = (PSAclEntryImpl) e.nextElement();
                  if (entry.getName().equals("foo"))
                  {
                     remove = entry;
                     break;
                  }
               }
               acl.removeEntry(ownerPrincipal, remove);
               // add a new entry named "bar"
               entry = new PSAclEntryImpl(new PSTypedPrincipal("bar",
                  PrincipalTypes.USER));
               entry.addPermission(PSPermissions.UPDATE);
               acl.addEntry(ownerPrincipal, entry);
               // add a new entry named "barCommunity"
               entry = new PSAclEntryImpl(new PSTypedPrincipal("barCommunity",
                  PrincipalTypes.COMMUNITY));
               entry.addPermission(PSPermissions.RUNTIME_VISIBLE);
               acl.addEntry(ownerPrincipal, entry);
               model.saveAcl(owner, true);
               //more scenarios???
            }
            finally
            {
               // cleanup
               model.delete(owner);
            }
         }
      };
      processTypes(p);
   }

   /**
    * There are several scenarios:
    * <ol>
    * <li>Object loaded unlocked twice - same object</li>
    * <li>Object loaded unlocked, then locked - different object</li>
    * <li>Object loaded locked, then unlocked w/ save - same object</li>
    * <li>Object locked, then unlocked w/o save, then loaded [un]locked -
    * different object</li>
    * <li>Object loaded locked, then saved w/o unlock - same object</li>
    * <ol>
    */
   public void testCache() throws Exception
   {
      Processor p = new Processor()
      {
         public void process(PSObjectType type) throws Exception
         {
            IPSCmsModel model = PSCoreFactory.getInstance().getModel(
               type.getPrimaryType());

            Collection<IPSReference> catalogedRefs = model.catalog(false);
            assertTrue("No objects in catalog for type " + type.toString(),
               catalogedRefs.size() > 0);
            Iterator<IPSReference> it = catalogedRefs.iterator();
            IPSReference ref1 = it.next();

            IPSReference ref2 = null;
            if (it.hasNext())
               ref2 = it.next();
            System.out.println(ref1);
            // test flushing of object
            Object o1 = model.load(ref1, false, false);
            model.flush(ref1);
            Object o2 = model.load(ref1, false, false);
            assertFalse(type.toString(), o1 == o2);

            // test flushing all
            if (ref2 != null)
            {
               o1 = model.load(ref2, false, false);
               model.flush(null);
               assertFalse(type.toString(), model.load(ref1, false, false) == o1);
               assertFalse(type.toString(), model.load(ref2, false, false) == o2);
            }

            // scenario 1
            o1 = model.load(ref1, false, false);
            o2 = model.load(ref1, false, false);
            assertTrue(type.toString(), o1 == o2);

            if (doesNotSupportLockedLoadAndSave(type.getPrimaryType()))
               return;

            // scenario 2
            o2 = model.load(ref1, true, false);
            assertFalse(type.toString(), o1 == o2);
            // cleanup
            model.releaseLock(ref1);

            // scenario 3
            model.flush(ref1);
            o1 = model.load(ref1, true, false);
            model.save(ref1, true);
            o2 = model.load(ref1, false, false);
            assertTrue(type.toString(), o1 == o2);

            // scenario 4
            model.flush(ref1);
            o1 = model.load(ref1, true, false);
            model.releaseLock(ref1);
            o2 = model.load(ref1, false, false);
            assertFalse(type.toString(), o1 == o2);

            // scenario 5
            model.flush(ref1);
            o1 = model.load(ref1, true, false);
            model.save(ref1, false);
            o2 = model.load(ref1, false, false);
            assertTrue(type.toString(), o1 == o2);
            // cleanup for other tests
            model.releaseLock(ref1);
         }
      };
      processTypes(p);
   }

   /**
    * Register for 1 event type. Verify get notified only on that type. Remove
    * listener and verify no notifications. Add listener for all types and make
    * them all happen.
    * 
    * @throws Exception
    */
   public void testListeners() throws Exception
   {
      final StringBuffer buf = new StringBuffer(10);

      IPSModelListener l = new IPSModelListener()
      {
         // see base class method for details
         @SuppressWarnings("unused")
         public void modelChanged(PSModelChangedEvent event)
         {
            buf.append("1");
         }
      };

      PSObjectType type = new PSObjectType(PSObjectTypes.SLOT);
      IPSCmsModel model = PSCoreFactory.getInstance().getModel(
         PSObjectTypes.SLOT);

      Collection<IPSReference> cleanup = new ArrayList<IPSReference>();
      try
      {
         int notifications = PSModelChangedEvent.ModelEvents.CREATED.getFlag();
         model.addListener(l, notifications);

         assertTrue(buf.length() == 0);
         IPSReference ref = model.create(type, "foobar20");
         assertTrue(buf.length() == 1);

         // doesn't notify unless registered
         int i = buf.length();
         model.save(ref, true);
         cleanup.add(ref);
         assertTrue(buf.length() == i);

         model.removeListener(l);
         i = buf.length();
         model.create(type, "foobar21");
         assertTrue(buf.length() == i);

         // register for all types
         model.addListener(l, 0xffffffff);

         i = buf.length();
         ref = model.create(type, "foobar22");
         assertTrue(buf.length() > i);

         i = buf.length();
         model.load(ref, true, false);
         assertTrue(buf.length() > i);

         i = buf.length();
         ref = model.save(ref, false);
         cleanup.add(ref);
         assertTrue(buf.length() > i);

         i = buf.length();
         model.propertyChanged(ref, null);
         assertTrue(buf.length() > i);

         i = buf.length();
         model.releaseLock(ref);
         assertTrue(buf.length() > i);

         i = buf.length();
         model.delete(ref);
         cleanup.remove(ref); // No need to clean up if its already
         // deleted.
         assertTrue(buf.length() > i);

         // fixme - add acl when implemented
      }
      finally
      {
         // restore repository to its original state
         for (IPSReference ref : cleanup)
         {
            model.delete(ref);
         }
      }
   }

   /**
    * Object independent function, so only need to test on 1 object type w/
    * and w/o hierarchy.
    */
   public void testGetReference()
      throws Exception
   {
      //test w/o hierarchy
      IPSCmsModel model = PSCoreFactory.getInstance().getModel(
            PSObjectTypes.SLOT);
      try
      {
         model.getReference((IPSGuid) null);
         fail("contract violated");
      }
      catch (IllegalArgumentException success)
      {}

      try
      {
         model.getReference((String) null);
         fail("contract violated");
      }
      catch (IllegalArgumentException success)
      {}

      IPSReference ref = model.catalog(false).iterator().next();
      IPSGuid id = ref.getId();
      assertTrue(model.getReference(id).equals(ref));
      
      //test with hierarchy
      model = PSCoreFactory.getInstance().getModel(
         PSObjectTypes.USER_FILE);
      
      try
      {
         model.getReference(id);
         fail("contract violated");
      }
      catch (UnsupportedOperationException success)
      {}
      
      IPSHierarchyManager mgr = model.getHierarchyManager("contentTypes");
      //drill down a couple levels
      Collection<IPSHierarchyNodeRef> level0 = mgr.getChildren(null);
      assertTrue(level0.size() == 1);
      IPSHierarchyNodeRef root = buildTree(model, mgr, PSObjectTypes.USER_FILE);
      try
      {
         IPSHierarchyNodeRef testRef = findChild(root, "folder2a");
         assertNotNull(testRef);
         
         id = testRef.getId();
         IPSHierarchyNodeRef tr = mgr.getReference(id);
         assertTrue(tr.equals(testRef));
      }
      finally
      {
         //cleanup
         List<IPSHierarchyNodeRef> rootList = new ArrayList<IPSHierarchyNodeRef>();
         rootList.add(root);
         mgr.removeChildren(rootList);
      }
   }

   /**
    * Creates a tree, validates it, adds a file node w/o saving. Checks for
    * duplicate name handling.
    * 
    * @throws Exception
    */
   public void testHierarchyManagerCreate() throws Exception
   {
      Processor p = new Processor()
      {
         public void process(PSObjectType type) throws Exception
         {
            if (doesNotSupportCreateChildren(type.getPrimaryType()))
               return;
            IPSCmsModel model = PSCoreFactory.getInstance().getModel(
               type.getPrimaryType());
            IPSHierarchyManager mgr = model
               .getHierarchyManager(getTreeName(type.getPrimaryType()));
            if (null == mgr && !supportsHierarchyManager(type.getPrimaryType()))
               return;
            else if (mgr == null)
               fail("hierarchy mgr required");

            Collection<IPSHierarchyNodeRef> roots = mgr.getChildren(null);
            assertTrue(roots.size() > 0);
            IPSHierarchyNodeRef root = findUsableRoot(mgr, type.getPrimaryType());
            IPSHierarchyNodeRef testRoot = buildTree(model, mgr, type
               .getPrimaryType());

            // test duplicate name
            List<String> names = new ArrayList<String>();
            names.add("testROOTFolder"); // different case
            PSObjectType folder = getContainerType(type.getPrimaryType());
            try
            {
               mgr.createChildren(root, folder, names);
               fail("Allowed duplicate folder name.");
            }
            catch (PSMultiOperationException success)
            {
               // should fail with same name folder - single failure
               assertTrue(success.getResults().length == 1);
            }

            names.add("rootFolder2");
            try
            {
               mgr.createChildren(root, folder, names);
               fail("Allowed duplicate folder name.");
            }
            catch (PSMultiOperationException success)
            {
               // mixed results - single failure and single success
               Object[] results = success.getResults();
               assertTrue(results.length == 2);
               assertTrue(results[0] instanceof PSDuplicateNameException);
               assertTrue(results[1] instanceof IPSHierarchyNodeRef);
               // cleanup
               List<IPSHierarchyNodeRef> child = 
                  new ArrayList<IPSHierarchyNodeRef>();
               child.add((IPSHierarchyNodeRef) results[1]);
               mgr.removeChildren(child);
            }

            // dupe names in same request
            IPSHierarchyNodeRef[] refs = null;
            try
            {
               names.clear();
               names.add("byfu");
               names.add("byfu");
               refs = mgr.createChildren(testRoot, type, names);
               fail("allowed dupe name");
            }
            catch (PSMultiOperationException success)
            {
               Object[] results = success.getResults();
               assertTrue(results.length == 2);
               assertTrue(results[1] instanceof PSDuplicateNameException);
               model.releaseLock((IPSReference) results[0]);
            }

            // dupe name on 2nd create w/o persisting first
            try
            {
               names.clear();
               names.add("fuby");
               refs = mgr.createChildren(testRoot, type, names);
               mgr.createChildren(testRoot, type, names);
               fail("allowed dupe name");
            }
            catch (PSMultiOperationException success)
            {
               Object[] results = success.getResults();
               assertTrue(results.length == 1);
               assertTrue(results[0] instanceof PSDuplicateNameException);
            }
            finally
            {
               model.releaseLock(refs[0]);
            }

            // test create file w/o save
            Collection<IPSHierarchyNodeRef> level1Children = mgr
               .getChildren(testRoot);
            IPSHierarchyNodeRef level1Folder = null;
            for (IPSHierarchyNodeRef ref : level1Children)
            {
               if (ref.getName().equals("folder1a"))
                  level1Folder = ref;
            }
            assertTrue(level1Folder != null);
            names.clear();
            names.add("foobar");
            IPSHierarchyNodeRef[] created = mgr.createChildren(level1Folder,
               getLeafType(type.getPrimaryType()), names);
            Collection<IPSHierarchyNodeRef> children = level1Folder
               .getChildren();
            assertTrue(children.size() == 1);
            model.releaseLock(created[0]);
            children = level1Folder.getChildren();
            assertTrue(children.size() == 0);

            // cleanup
            List<IPSHierarchyNodeRef> toRemove = 
               new ArrayList<IPSHierarchyNodeRef>();
            toRemove.add(testRoot);
            mgr.removeChildren(toRemove);
            //must unlock
            if (root != null)
               model.releaseLock(root);
         }
      };
      processFileHierarchyTypes(p);
   }

   /**
    * Creates a tree, moves folders and files around, verifying behavior.
    * 
    * @throws Exception
    */
   public void testHierarchyManagerMove() throws Exception
   {
      Processor p = new Processor()
      {
         public void process(PSObjectType type) throws Exception
         {
            if (doesNotSupportMoveChildren(type.getPrimaryType()))
               return;
             IPSCmsModel model = PSCoreFactory.getInstance().getModel(
               type.getPrimaryType());
            IPSHierarchyManager mgr = model
               .getHierarchyManager(getTreeName(type.getPrimaryType()));
            if (null == mgr && !supportsHierarchyManager(type.getPrimaryType()))
               return;
            else if (mgr == null)
               fail("hierarchy mgr required");

            final IPSHierarchyNodeRef testRoot =
               buildTree(model, mgr, type.getPrimaryType());

            // test move single file
            IPSHierarchyNodeRef folder3aRef = findChild(testRoot, "folder3a");
            List<IPSHierarchyNodeRef> toMove = 
               new ArrayList<IPSHierarchyNodeRef>();
            toMove.add(findChild(testRoot, "file2.txt"));
            mgr.moveChildren(toMove, folder3aRef);
            assertTrue(testRoot.getChildren().size() == 4);
            assertTrue(folder3aRef.getChildren().size() == 1);

            // test duplicate file name
            IPSHierarchyNodeRef folder2aRef = findChild(testRoot, "folder2a");
            toMove.clear();
            for (IPSHierarchyNodeRef ref : folder2aRef.getChildren())
            {
               if (ref.getName().equals("file1.txt"))
                  toMove.add(ref);
            }
            assertTrue(toMove.size() == 1);
            try
            {
               mgr.moveChildren(toMove, testRoot);
               fail("Allowed duplicate name.");
            }
            catch (PSMultiOperationException success)
            {
               assertTrue(success.getResults().length == 1);
               assertTrue(
                     success.getResults()[0] instanceof PSDuplicateNameException);
               assertTrue(folder2aRef.getChildren().size() == 2);
               assertTrue(testRoot.getChildren().size() == 4);
            }

            // move w/ children from different parents
            toMove.add(findChild(testRoot, "folder1c"));
            try
            {
               mgr.moveChildren(toMove, folder2aRef);
               fail("Children don't have same parent.");
            }
            catch (PSModelException success)
            {}

            // pass leaf as target
            try
            {
               mgr.moveChildren(toMove, findChild(testRoot, "file2.txt"));
               fail("Worked w/ file as target container.");
            }
            catch (PSModelException success)
            {}

            // move to self is OK
            toMove.clear();
            toMove.add(findChild(testRoot, "folder1b"));
            mgr.moveChildren(toMove, testRoot);

            // move to sub-folder not OK
            try
            {
               mgr.moveChildren(toMove, folder2aRef);
               fail("Allowed copy to sub-folder.");
            }
            catch (PSModelException success)
            {}

            // move folder w/ children
            IPSHierarchyNodeRef folder1aRef = findChild(testRoot, "folder1a");
            mgr.moveChildren(toMove, folder1aRef);
            assertTrue(testRoot.getChildren().size() == 3);
            assertTrue(folder1aRef.getChildren().size() == 1);
            assertTrue(
                  findChild(folder1aRef, "folder1b").getChildren().size() > 0);

            // try to move locked file
            IPSHierarchyNodeRef file2Ref = findChild(testRoot, "file2.txt");
            model.load(file2Ref, true, false);

            toMove.clear();
            toMove.add(file2Ref);
            try
            {
               mgr.moveChildren(toMove, folder2aRef);
               fail("Allowed copy of item open for edit.");
            }
            catch (PSMultiOperationException success)
            {
               assertTrue(success.getResults().length == 1);
               assertTrue(success.getResults()[0] instanceof PSLockException);
            }
            model.releaseLock(file2Ref);

            // move from root to below root
            List<String> name = new ArrayList<String>();
            final String ROOT_TEST_NAME = "elephantHide";
            name.add(ROOT_TEST_NAME);
            IPSHierarchyNodeRef usableRoot = findUsableRoot(mgr,
                  type.getPrimaryType());
            Collection<IPSHierarchyNodeRef> tmp1 = mgr.getChildren(usableRoot);
            int rootChildCount = tmp1.size();
            IPSHierarchyNodeRef[] rootChild = mgr
                  .createChildren(usableRoot, type, name);
            model.save(rootChild, false);
            assertTrue(mgr.getChildren(usableRoot).size() == rootChildCount + 1);
            List<IPSHierarchyNodeRef> rootChildList = 
               new ArrayList<IPSHierarchyNodeRef>();
            rootChildList.add(rootChild[0]);
            IPSHierarchyNodeRef targetOfMove = findChild(testRoot, "folder1a");
            int targetChildCount = mgr.getChildren(targetOfMove).size();
            // move /<usableroot>/elephantHide -> /<usableroot>/testRootFolder/folder1a
            mgr.moveChildren(rootChildList, targetOfMove);
            Collection<IPSHierarchyNodeRef> tmp = mgr.getChildren(usableRoot);
            assertTrue(tmp.size() == rootChildCount);
            assertTrue(
                  mgr.getChildren(targetOfMove).size() == targetChildCount + 1);
            
            //cleanup - remove elephantHide
            List<IPSHierarchyNodeRef> tmpList = 
               new ArrayList<IPSHierarchyNodeRef>();
            tmpList.add(rootChild[0]);
            mgr.removeChildren(tmpList);
            
            //move from below root to root
            List<IPSHierarchyNodeRef> moveToRoot = 
               new ArrayList<IPSHierarchyNodeRef>();
            IPSHierarchyNodeRef moveToRootSource = findChild(testRoot,
                  "folder1a");
            IPSHierarchyNodeRef moveToRootSourceParent = moveToRootSource
                  .getParent();
            moveToRoot.add(moveToRootSource);
            Collection<IPSHierarchyNodeRef> tmp2 = mgr.getChildren(
                  moveToRootSourceParent, true); 
            int moveToRootSourceCount = tmp2.size();
            // move /<usableroot>/testRootFolder/folder1a -> /<usableroot>
            mgr.moveChildren(moveToRoot, usableRoot);
            tmp = mgr.getChildren(usableRoot, true);
            assertTrue(tmp.size() == rootChildCount + 1);
            tmp1 = mgr.getChildren(moveToRootSourceParent, true);
            assertTrue(tmp1.size() == moveToRootSourceCount - 1);
            
            // cleanup
            List<IPSHierarchyNodeRef> toRemove = 
               new ArrayList<IPSHierarchyNodeRef>();
            toRemove.add(testRoot);
            toRemove.addAll(moveToRoot);
            mgr.removeChildren(toRemove);
         }
      };
      processFileHierarchyTypes(p);
   }

   /**
    * Verifies that a Collection is returned or an exception is thrown, as
    * appropriate.
    */
   public void testHierarchyManagerGetRoots()
      throws Exception
   {
      Processor p = new Processor()
      {
         public void process(PSObjectType type) throws Exception
         {
            IPSCmsModel model = PSCoreFactory.getInstance().getModel(
               type.getPrimaryType());

            Collection<String> roots;
            try
            {
               roots = model.getHierarchyTreeNames();
               if (!supportsHierarchyManager(type.getPrimaryType()))
                  fail("Must throw UnsupportedOperationException.");
               assertNotNull(roots);
               assertTrue(roots.size() >= 1);
               for (String name : roots)
               {
                  IPSHierarchyManager mgr = model.getHierarchyManager(name);
                  assertNotNull(mgr);
                  Collection<IPSHierarchyNodeRef> children = mgr
                     .getChildren(null);
                  assertNotNull(children);
                  if (type.getPrimaryType() == PSObjectTypes.USER_FILE)
                  {
                     if (name.equalsIgnoreCase("slots"))
                     {
                        assertTrue(children.size() > 0);
                     }
                     for (IPSHierarchyNodeRef ref : children)
                     {
                        if (ref.isContainer())
                        {
                           assertNotNull(mgr.getChildren(ref));
                        }
                        else
                        {
                           assertNotNull(mgr.getChildren(ref));
                           assertTrue(mgr.getChildren(ref).size() == 0);
                        }
                     }
                  }
                  else
                  {
                     assertTrue(children.size() > 0);
                     children = mgr.getChildren(children.iterator().next());
                     assertNotNull(children);
                  }
               }
            }
            catch (UnsupportedOperationException ok)
            {
               if (supportsHierarchyManager(type.getPrimaryType()))
                  fail("Must be supported for hierarchy mgr.");
            }
         }
      };
      processTypes(p);
   }

   /**
    * Creates a tree, moves folders and files around, verifying behavior.
    * 
    * @throws Exception
    */
   public void testHierarchyManagerRemove() throws Exception
   {
      Processor p = new Processor()
      {
         public void process(PSObjectType type) throws Exception
         {
            if (doesNotSupportRemoveChildren(type.getPrimaryType()))
               return;
            IPSCmsModel model = PSCoreFactory.getInstance().getModel(
               type.getPrimaryType());
            IPSHierarchyManager mgr = model
               .getHierarchyManager(getTreeName(type.getPrimaryType()));
            if (null == mgr && !supportsHierarchyManager(type.getPrimaryType()))
               return;
            else if (mgr == null)
               fail("hierarchy mgr required");

            Collection<IPSHierarchyNodeRef> roots = mgr.getChildren(null);
            assertTrue(roots.size() > 0);
            IPSHierarchyNodeRef testRoot = buildTree(model, mgr, type
               .getPrimaryType());

            // test remove single file
            List<IPSHierarchyNodeRef> toRemove = 
               new ArrayList<IPSHierarchyNodeRef>();
            toRemove.add(findChild(testRoot, "file1.txt", false));
            mgr.removeChildren(toRemove);
            assertTrue(testRoot.getChildren().size() == 4);
            // make sure it didn't delete same name in different folder
            IPSHierarchyNodeRef folder2aRef = findChild(testRoot, "folder2a");
            assertTrue(folder2aRef.getChildren().size() == 2);

            // test remove single folder
            toRemove.clear();
            toRemove.add(findChild(testRoot, "folder1a"));
            mgr.removeChildren(toRemove);
            assertTrue(testRoot.getChildren().size() == 3);
            assertTrue(findChild(testRoot, "folder1a") == null);

            // test remove multiples w/ same parent
            toRemove.clear();
            toRemove.add(findChild(folder2aRef, "folder3a"));
            toRemove.add(findChild(folder2aRef, "file1.txt"));
            mgr.removeChildren(toRemove);
            assertTrue(folder2aRef.getChildren().size() == 0);

            // test remove multiples w/ different parents
            toRemove.clear();
            toRemove.add(findChild(testRoot, "folder1c"));
            toRemove.add(findChild(testRoot, "file2.txt"));
            toRemove.add(findChild(testRoot, "folder2b"));
            mgr.removeChildren(toRemove);

            toRemove.clear();
            toRemove.add(testRoot);
            mgr.removeChildren(toRemove);
            testRoot = buildTree(model, mgr, type.getPrimaryType());

            // test remove folder is recursive
            toRemove.clear();
            toRemove.add(findChild(testRoot, "folder1b"));
            mgr.removeChildren(toRemove);
            assertTrue(testRoot.getChildren().size() == 4);
            assertTrue(findChild(testRoot, "folder2a") == null);
            assertTrue(findChild(testRoot, "folder3a") == null);

            // cleanup
            toRemove.clear();
            toRemove.add(testRoot);
            mgr.removeChildren(toRemove);
         }
      };
      processFileHierarchyTypes(p);
   }

   /**
    * Validates that nodes can be found by their path and that the mgr can
    * properly return the path of a node.
    * 
    * @throws Exception
    */
   public void testHierarchyManagerPathOps() throws Exception
   {
      Processor p = new Processor()
      {
         public void process(PSObjectType type) throws Exception
         {
            if (doesNotSupportCreateChildren(type.getPrimaryType()))
               return;
            IPSCmsModel model = PSCoreFactory.getInstance().getModel(
               type.getPrimaryType());
            IPSHierarchyManager mgr = model
               .getHierarchyManager(getTreeName(type.getPrimaryType()));
            if (null == mgr && !supportsHierarchyManager(type.getPrimaryType()))
               return;
            else if (mgr == null)
               fail("hierarchy mgr required");

            IPSHierarchyNodeRef testRoot = buildTree(model, mgr, type
               .getPrimaryType());

            // test getPath
            IPSHierarchyNodeRef test = findChild(testRoot, "folder3a");
            String foundPath = mgr.getPath(test);
            String partialPath = mgr.getPath(testRoot) + "/folder1b/folder2a/";
            assertTrue(foundPath.startsWith(partialPath));

            // test get file by path - case matches
            String nodeName = "file1.txt";
            String path = partialPath + nodeName;
            IPSHierarchyNodeRef ref = mgr.getByPath(path);
            assertFalse(ref == null);
            assertTrue(ref.getName().equals(nodeName));
            assertTrue(mgr.getPath(ref).equals(path));

            // test get file by path - case mismatch
            nodeName = "FILE1.txt";
            path = partialPath.replaceFirst("Root", "ROOT") + nodeName;
            ref = mgr.getByPath(path);
            assertFalse(ref == null);
            assertFalse(ref.getName().equals(nodeName));
            assertTrue(ref.getName().equalsIgnoreCase(nodeName));
            assertFalse(mgr.getPath(ref).equals(path));
            assertTrue(mgr.getPath(ref).equalsIgnoreCase(path));

            // test get folder by path - case mismatch
            nodeName = "folder3A";
            path = partialPath.replaceFirst("Root", "ROOT") + nodeName;
            ref = mgr.getByPath(path);
            assertFalse(ref == null);
            assertFalse(ref.getName().equals(nodeName));
            assertTrue(ref.getName().equalsIgnoreCase(nodeName));
            assertFalse(mgr.getPath(ref).equals(path));
            assertTrue(mgr.getPath(ref).equalsIgnoreCase(path));

            // get root
            IPSHierarchyNodeRef rootNode = mgr.getChildren(null).iterator()
               .next();
            assertFalse(ref == null);
            nodeName = rootNode.getName();
            ref = mgr.getByPath("/" + nodeName.toUpperCase());
            assertFalse(ref == null);
            assertTrue(nodeName.equals(ref.getName()));

            // cleanup
            List<IPSHierarchyNodeRef> toRemove = 
               new ArrayList<IPSHierarchyNodeRef>();
            toRemove.add(testRoot);
            mgr.removeChildren(toRemove);
            //Must unlock
            if (testRoot != null)
               model.releaseLock(testRoot);
         }
      };
      processFileHierarchyTypes(p);
   }

   /**
    * Database type model testing is outside of the rest of hierarchy models
    * since this does not support create/move/remove children and hence we have
    * to operate on the existing paths. Tried to simulate the hierarchy to be
    * same in test and connected mode. If the data for the proxy changes, the
    * test could fail
    * 
    * @throws Exception
    */
   public void testDatabaseTypeProxies() throws Exception
   {
      IPSCmsModel model = PSCoreFactory.getInstance().getModel(PSObjectTypes.DB_TYPE);
      IPSHierarchyManager mgr = model
         .getHierarchyManager(getTreeName(PSObjectTypes.DB_TYPE));
      if (null == mgr && !supportsHierarchyManager(PSObjectTypes.DB_TYPE))
         return;
      else if (mgr == null)
         fail("hierarchy mgr required");

      IPSHierarchyNodeRef testRoot = mgr.getChildren(null).iterator().next();

      // test getPath
      IPSHierarchyNodeRef test = findChild(testRoot, "CONTENTADHOCUSERS");
      String foundPath = mgr.getPath(test);
      String partialPath = "/" + testRoot.getName() + "/TABLE/";
      assertTrue(foundPath.startsWith(partialPath));

      // get root
      IPSHierarchyNodeRef rootNode = mgr.getChildren(null).iterator()
         .next();
      String nodeName = rootNode.getName();
      IPSHierarchyNodeRef ref = mgr.getByPath("/" + nodeName.toUpperCase());
      assertFalse(ref == null);
      assertTrue(nodeName.equals(ref.getName()));
   }

   /**
    * Builds a small tree w/ folders and files and validates it. The generated
    * tree has a single folder as the root. If the root already exists, it is
    * removed first. The model is also flushed.
    * <p>The structure looks like this (all files are persisted):
    * <pre>
    * testRootFolder
    *     folder1a
    *     folder1b
    *        folder2a
    *           folder3a
    *           file1.txt
    *        folder2b
    *     folder1c
    *     file1.txt
    *     file2.txt
    * </pre>
    * 
    * @param mgr Assumed not <code>null</code>.
    * @param primaryType Assumed not <code>null</code>.
    * 
    * @return The ref to the first generated folder.
    * @throws PSMultiOperationException
    */
   private IPSHierarchyNodeRef buildTree(IPSCmsModel model,
      IPSHierarchyManager mgr, Enum primaryType) throws Exception
   {
      // don't change names below w/o searching file
      model.flush(null);
      IPSHierarchyNodeRef existingRoot = findUsableRoot(mgr, primaryType);
      Collection<IPSHierarchyNodeRef> rootChildren = mgr
         .getChildren(existingRoot);
      final String TEST_ROOT_NAME = "testRootFolder";
      if (rootChildren.size() > 0)
      {
         Iterator<IPSHierarchyNodeRef> children = rootChildren.iterator();
         while (children.hasNext())
         {
            IPSHierarchyNodeRef ref = children.next();
            // cleanup from previous test
            if (ref.getName().equalsIgnoreCase(TEST_ROOT_NAME))
            {
               List<IPSHierarchyNodeRef> tmp = 
                  new ArrayList<IPSHierarchyNodeRef>();
               tmp.add(ref);
               mgr.removeChildren(tmp);
               children.remove();
            }
         }
      }
      List<String> names = new ArrayList<String>();
      names.add(TEST_ROOT_NAME);
      PSObjectType folder = getContainerType(primaryType);
      final IPSHierarchyNodeRef root = mgr.createChildren(existingRoot,
            folder, names)[0];

      Collection<IPSHierarchyNodeRef> rootChildrenTest = mgr
            .getChildren(existingRoot);
      assertTrue(rootChildrenTest.size() == rootChildren.size() + 1);

      names.clear();
      names.add("folder1a");
      names.add("folder1b");
      names.add("folder1c");
      IPSHierarchyNodeRef[] rootFolders = mgr.createChildren(root, folder,
         names);

      names.clear();
      names.add("folder2a");
      names.add("folder2b");
      IPSHierarchyNodeRef[] level1Folders = mgr.createChildren(rootFolders[1],
         folder, names);

      names.clear();
      names.add("folder3a");
      mgr.createChildren(level1Folders[0], folder, names);

      PSObjectType file = getLeafType(primaryType);
      names.clear();
      names.add("file1.txt");
      names.add("file2.txt");
      IPSHierarchyNodeRef[] childFiles = mgr.createChildren(root, file, names);
      for (IPSHierarchyNodeRef ref : childFiles)
      {
         if (!isPersistedImmediately(ref.getObjectType()))
         {
            assertFalse(ref.isPersisted());
            // PSHierarchyNode placeholder = (PSHierarchyNode) model.load(ref,
            // true, false);
            // //need this because of other tests
            // placeholder.addProperty("guid", "1-1-1");
            model.save(ref, true);
         }
         assertTrue(ref.isPersisted());
      }

      names.clear();
      names.add("file1.txt");
      childFiles = mgr.createChildren(level1Folders[0], file, names);
      for (IPSHierarchyNodeRef ref : childFiles)
      {
         assertFalse(ref.isPersisted());
         // PSHierarchyNode placeholder = (PSHierarchyNode) model.load(ref,
         // true, false);
         // //need this because of other tests
         // placeholder.addProperty("guid", "1-1-1");
         model.save(ref, true);
         assertTrue(ref.isPersisted());
      }

      // catalog to verify
      model.flush(null);
      IPSHierarchyNodeRef node = null;
      Collection<IPSHierarchyNodeRef> children = root.getChildren();
      assertTrue(children.size() == 5);
      for (IPSHierarchyNodeRef ref : children)
      {
         assertTrue("folder1a folder1b folder1c file1.txt file2.txt"
            .indexOf(ref.getName()) >= 0);
         if (ref.getName().equals("folder1b"))
            node = ref;
      }
      assertTrue(node != null);
      children = node.getChildren();
      assertTrue(children.size() == 2);
      node = null;
      for (IPSHierarchyNodeRef ref : children)
      {
         assertTrue("folder2a folder2b".indexOf(ref.getName()) >= 0);
         if (ref.getName().equals("folder2a"))
            node = ref;
      }

      assertTrue(node != null);
      children = node.getChildren();
      assertTrue(children.size() == 2);
      node = null;
      for (IPSHierarchyNodeRef ref : children)
      {
         assertTrue("folder3a file1.txt".indexOf(ref.getName()) >= 0);
         if (ref.getName().equals("folder3a"))
            node = ref;
      }

      assertTrue(node != null);
      children = node.getChildren();
      assertTrue(children.size() == 0);

      return root;
   }

   /**
    * Tries to create a node in the root. If this doesn't work, it moves down 1
    * level and tries that. Some hierarchy managers don't support creation at
    * the root level.
    * 
    * @param mgr Used to create and catalog. Assumed not <code>null</code>.
    * 
    * @return A value which can be passed to the
    * {@link IPSHierarchyManager#getChildren(IPSHierarchyNodeRef)} method.
    */
   private IPSHierarchyNodeRef findUsableRoot(IPSHierarchyManager mgr,
      Enum primaryType) throws Exception
   {
      List<String> name = new ArrayList<String>();
      name.add("xx foo xx");
      IPSHierarchyNodeRef root = null;
      IPSHierarchyNodeRef[] results = null;
      try
      {
         results = mgr.createChildren(null, getLeafType(primaryType), name);
      }
      catch (UnsupportedOperationException e)
      {
         Collection<IPSHierarchyNodeRef> rootChildren = mgr.getChildren(null);
         String desiredRootName = getRootName(primaryType);
         for (IPSHierarchyNodeRef child : rootChildren)
         {
            if (child.getName().equalsIgnoreCase(desiredRootName))
            {
               results = mgr.createChildren(child, getLeafType(primaryType),
                  name);
               root = child;
            }
         }
      }
      finally
      {
         if (results != null)
         {
            List<IPSHierarchyNodeRef> cleanup = new ArrayList<IPSHierarchyNodeRef>();
            cleanup.addAll(Arrays.asList(results));
            mgr.removeChildren(cleanup);
            // if not successful, we will return null
         }
      }
      return root;
   }

   /**
    * Some hierarchy managers don't support creation at the root level. This
    * method returns the name of a root node that should allow creation of new
    * children. If the type is not known, a junit <code>fail</code> is
    * executed.
    * 
    * @param primaryType Generally, one of the {@link PSObjectTypes} enums.
    * Assumed not <code>null</code>.
    * 
    * @return Never <code>null</code> or empty.
    */
   private String getRootName(Enum primaryType)
   {
      if (primaryType == PSObjectTypes.RESOURCE_FILE
         || primaryType == PSObjectTypes.XML_APPLICATION_FILE)
      {
         return "sys_resources";

      }
      else if (primaryType == PSObjectTypes.LOCAL_FILE)
      {
         return "c:";
      }
      else if (primaryType == PSObjectTypes.DB_TYPE)
      {
         return "DATA_SOURCES";
      }
      else
         fail("Unknown object type for getRootName");
      return "";
   }

   /**
    * Checks if the supplied object type is persisted immediately when it is
    * created.
    * 
    * @param type Assumed not <code>null</code>.
    * 
    * @return <code>true</code> if it is, <code>false</code> otherwise.
    */
   private boolean isPersistedImmediately(PSObjectType type)
   {
      return type.getPrimaryType().equals(PSObjectTypes.USER_FILE)
         && type.getSecondaryType().equals(
            PSObjectTypes.UserFileSubTypes.WORKBENCH_FOLDER);
   }

   /**
    * Load object locked and not, save releasing lock and not.
    */
   public void testCheckLock() throws Exception
   {
      // only need to test a single type
      IPSCmsModel model = PSCoreFactory.getInstance().getModel(
         PSObjectTypes.SLOT);

      IPSReference ref1 = model.catalog(true).iterator().next();
      model.load(ref1, false, false);
      assertFalse(model.isLockedInThisSession(ref1));
      model.load(ref1, false, true);
      assertFalse(model.isLockedInThisSession(ref1));
      model.load(ref1, true, false);
      assertTrue(model.isLockedInThisSession(ref1));
      model.save(ref1, false);
      assertTrue(model.isLockedInThisSession(ref1));
      model.save(ref1, true);
      assertFalse(model.isLockedInThisSession(ref1));
   }

   /**
    * Load object, change it, load again - should see changes. Unlock the object
    * w/o saving, load again - should look like original.
    */
   public void testReleaseLock() throws Exception
   {
      // only need to test a single type
      IPSCmsModel model = PSCoreFactory.getInstance().getModel(
         PSObjectTypes.SLOT);

      Collection<IPSReference> cat = model.catalog(true);
      IPSReference ref = cat.iterator().next();
      PSTemplateSlot slot = (PSTemplateSlot) model.load(ref, true, false);
      String TEST_NAME = "XXX";
      String originalName = slot.getName();
      slot.setName(TEST_NAME);
      PSTemplateSlot slot2 = (PSTemplateSlot) model.load(ref, false, false);
      assertTrue(slot2.getName().equals(TEST_NAME));

      model.releaseLock(ref);
      slot2 = (PSTemplateSlot) model.load(ref, false, false);
      assertTrue(slot2.getName().equals(originalName));
   }

   /**
    * Follows same pattern as {@link #testReleaseLock()}.
    */
   public void testReleaseAclLock() throws Exception
   {
      // only need to test a single type
      IPSCmsModel model = PSCoreFactory.getInstance().getModel(
         PSObjectTypes.SLOT);

      Collection<IPSReference> cat = model.catalog(true);
      IPSReference ref = cat.iterator().next();
      try
      {
         model.saveAcl(ref, true);
         fail("allowed acl save w/o lock");
      }
      catch (PSModelException success)
      {}
      
      model.loadAcl(ref, true);
      //make sure it locked
      model.saveAcl(ref, false);
      //try again to make sure the lock was not released
      model.saveAcl(ref, false);

      model.releaseAclLock(ref);
      try
      {
         model.saveAcl(ref, true);
         fail("allowed acl save w/o lock");
      }
      catch (PSModelException success)
      {}
   }

   /**
    * Returns the name of a supported tree for the supplied type if it is a
    * hierarchy type. If a match is not found, a junit <code>fail</code> is
    * executed.
    * 
    * @param primaryType Assumed not <code>null</code>
    * 
    * @return Never <code>null</code> or empty.
    */
   private String getTreeName(Enum primaryType)
   {
      if (primaryType == PSObjectTypes.RESOURCE_FILE)
      {
         return "RESOURCE_FILE_TREE";

      }
      else if (primaryType == PSObjectTypes.USER_FILE)
      {
         return "slots";
      }
      else if (primaryType == PSObjectTypes.XML_APPLICATION_FILE)
      {
         return "XML_FILE_TREE";
      }
      else if (primaryType == PSObjectTypes.LOCAL_FILE)
      {
         return "LOCAL_FILE_TREE";
      }
      else if (primaryType == PSObjectTypes.DB_TYPE)
      {
         return "DATA_SOURCES";
      }
      else
         fail("Unknown object type for getRootName");
      return "";
   }

   /**
    * Returns the sub type that is the container. Fails if the primary type is
    * not known to have a container sub type.
    * 
    * @param primaryType Assumed not <code>null</code>.
    * 
    * @return Never <code>null</code>.
    */
   private PSObjectType getContainerType(Enum primaryType)
   {
      if (primaryType == PSObjectTypes.RESOURCE_FILE
         || primaryType == PSObjectTypes.LOCAL_FILE
         || primaryType == PSObjectTypes.XML_APPLICATION_FILE)
      {
         return new PSObjectType((IPSPrimaryObjectType) primaryType,
            PSObjectTypes.FileSubTypes.FOLDER);
      }
      if (primaryType == PSObjectTypes.USER_FILE)
      {
         return new PSObjectType((IPSPrimaryObjectType) primaryType,
            PSObjectTypes.UserFileSubTypes.WORKBENCH_FOLDER);
      }
      if (primaryType == PSObjectTypes.DB_TYPE)
      {
         return new PSObjectType((IPSPrimaryObjectType) primaryType,
            PSObjectTypes.DataBaseObjectSubTypes.CATEGORY);
      }
      fail("Unknown hierarchical primary typ.e");
      throw new RuntimeException("never get here");
   }

   /**
    * Returns the sub type that is the leaf for the supplied primary type. Fails
    * if the primary type is not known to have a container sub type.
    * 
    * @param primaryType Assumed not <code>null</code>.
    * 
    * @return Never <code>null</code>.
    */
   private PSObjectType getLeafType(Enum primaryType)
   {
      if (primaryType == PSObjectTypes.RESOURCE_FILE
         || primaryType == PSObjectTypes.LOCAL_FILE
         || primaryType == PSObjectTypes.XML_APPLICATION_FILE)
      {
         return new PSObjectType((IPSPrimaryObjectType) primaryType,
            PSObjectTypes.FileSubTypes.FILE);
      }
      if (primaryType == PSObjectTypes.USER_FILE)
      {
         return new PSObjectType((IPSPrimaryObjectType) primaryType,
            PSObjectTypes.UserFileSubTypes.PLACEHOLDER);
      }
      if (primaryType == PSObjectTypes.DB_TYPE)
      {
         return new PSObjectType((IPSPrimaryObjectType) primaryType,
            PSObjectTypes.DataBaseObjectSubTypes.TABLE);
      }
      fail("Unknown hierarchical primary typ.e");
      throw new RuntimeException("never get here");
   }

   /**
    * Convenience method that calls
    * {@link #findChild(IPSHierarchyNodeRef, String, boolean)  findChild(parent,
    * childName, <code>true</code>)}.
    */
   private IPSHierarchyNodeRef findChild(IPSHierarchyNodeRef parent,
      String childName) throws PSModelException
   {
      return findChild(parent, childName, true);
   }

   /**
    * Scans startingParent recursively, looking for a child by the supplied
    * name. If found, the ref is returned, otherwise <code>null</code>.
    * 
    * @param parent Never <code>null</code>.
    * @param childName Never <code>null</code>. Case-insensitive search.
    * @param recurse If <code>true</code>, will process all descendents depth
    * first, otherwise will only look at the children of the supplied ref.
    * 
    * @return A valid ref, or <code>null</code> if not found.
    * @throws PSModelException
    */
   private IPSHierarchyNodeRef findChild(IPSHierarchyNodeRef parent,
      String childName, boolean recurse) throws PSModelException
   {
      IPSHierarchyNodeRef result = null;
      IPSHierarchyNodeRef next = parent;
      Collection<IPSHierarchyNodeRef> children = next.getChildren();
      for (IPSHierarchyNodeRef ref : children)
      {
         if (ref.getName().equalsIgnoreCase(childName))
            result = ref;
         else if (recurse && ref.isContainer())
            result = findChild(ref, childName);
         if (result != null)
            break;
      }
      return result;
   }

   /**
    * Performs a catalog on the supplied model. The resulting refs are scanned
    * to find one that has a name that matches that supplied.
    * 
    * @param model Assumed not <code>null</code>.
    * @param name Assumed not <code>null</code>.
    * 
    * @return The matching ref, or <code>null</code> if one is not found.
    */
   private IPSReference findRef(IPSCmsModel model, String name)
   {
      try
      {
         Collection<IPSReference> refs = model.catalog(true);
         for (IPSReference ref : refs)
         {
            if (ref.getName().equals(name))
               return ref;
         }
      }
      catch (PSModelException e)
      {
         fail("Unexpected PSModelException");
      }
      return null;
   }

   /**
    * Walks the enumeration of all known types and passes them to the supplied
    * processor.
    * <p>
    * In addition, supplies a type with an invalid primary type and one with an
    * invalid sub-type.
    * 
    * @param p Assumed not <code>null</code>.
    */
   private void processTypes(Processor p) throws Exception
   {
      // fixme - walk thru all enum types when proxies complete, just add them
      // as they are finished until all are complete
      /*
       * If a type has sub types, only 1 sub-type needs to appear in this list.
       */
      PSObjectType[] types =
      {
//         PSObjectTypeFactory.getType(PSObjectTypes.AUTO_TRANSLATION_SET),
//
//         PSObjectTypeFactory.getType(PSObjectTypes.COMMUNITY),
//
//         PSObjectTypeFactory.getType(PSObjectTypes.SITE),
//
//         PSObjectTypeFactory.getType(PSObjectTypes.WORKFLOW),
//
//         PSObjectTypeFactory.getType(PSObjectTypes.CONTENT_TYPE),
//
//         PSObjectTypeFactory.getType(PSObjectTypes.ITEM_FILTER),
//
//         PSObjectTypeFactory.getType(PSObjectTypes.KEYWORD),
//
//         PSObjectTypeFactory.getType(PSObjectTypes.LOCALE),
//
//         PSObjectTypeFactory.getType(PSObjectTypes.SLOT),
//
//         PSObjectTypeFactory.getType(PSObjectTypes.TEMPLATE,
//            PSObjectTypes.TemplateSubTypes.LOCAL),
//
//         PSObjectTypeFactory.getType(PSObjectTypes.TEMPLATE,
//            PSObjectTypes.TemplateSubTypes.SHARED),
//
//         PSObjectTypeFactory.getType(PSObjectTypes.TEMPLATE,
//            PSObjectTypes.TemplateSubTypes.GLOBAL),
//
//         PSObjectTypeFactory.getType(PSObjectTypes.UI_DISPLAY_FORMAT),
//
//         PSObjectTypeFactory.getType(PSObjectTypes.UI_VIEW,
//            PSObjectTypes.SearchSubTypes.STANDARD),
//         PSObjectTypeFactory.getType(PSObjectTypes.UI_VIEW,
//            PSObjectTypes.SearchSubTypes.CUSTOM),
//
//         PSObjectTypeFactory.getType(PSObjectTypes.UI_SEARCH,
//            PSObjectTypes.SearchSubTypes.STANDARD),
//         PSObjectTypeFactory.getType(PSObjectTypes.UI_SEARCH,
//            PSObjectTypes.SearchSubTypes.CUSTOM),
//
//         PSObjectTypeFactory.getType(PSObjectTypes.EXTENSION),
//
//         PSObjectTypeFactory.getType(PSObjectTypes.UI_ACTION_MENU,
//               PSObjectTypes.UiActionMenuSubTypes.MENU_CASCADING_SYSTEM),
//         PSObjectTypeFactory.getType(PSObjectTypes.UI_ACTION_MENU,
//               PSObjectTypes.UiActionMenuSubTypes.MENU_CASCADING_USER),
//         PSObjectTypeFactory.getType(PSObjectTypes.UI_ACTION_MENU,
//               PSObjectTypes.UiActionMenuSubTypes.MENU_DYNAMIC_SYSTEM),
//         PSObjectTypeFactory.getType(PSObjectTypes.UI_ACTION_MENU,
//               PSObjectTypes.UiActionMenuSubTypes.MENU_DYNAMIC_USER),
//         PSObjectTypeFactory.getType(PSObjectTypes.UI_ACTION_MENU,
//               PSObjectTypes.UiActionMenuSubTypes.MENU_ENTRY_SYSTEM),
//         PSObjectTypeFactory.getType(PSObjectTypes.UI_ACTION_MENU,
//               PSObjectTypes.UiActionMenuSubTypes.MENU_ENTRY_USER),
//
//         PSObjectTypeFactory.getType(PSObjectTypes.UI_ACTION_MENU_MISC,
//            PSObjectTypes.UiActionMenuMiscSubTypes.CONTEXT_PARAMETERS),
//
         PSObjectTypeFactory.getType(PSObjectTypes.CONTENT_EDITOR_CONTROLS,
            PSObjectTypes.ContentEditorControlSubTypes.SYSTEM),

//         PSObjectTypeFactory.getType(PSObjectTypes.CONTENT_EDITOR_CONTROLS,
//            PSObjectTypes.ContentEditorControlSubTypes.USER),
//
//         PSObjectTypeFactory.getType(PSObjectTypes.CONTENT_TYPE_SYSTEM_CONFIG),
//
//         PSObjectTypeFactory.getType(PSObjectTypes.SHARED_FIELDS),
//
//         PSObjectTypeFactory.getType(PSObjectTypes.CONFIGURATION_FILE,
//            PSObjectTypes.ConfigurationFileSubTypes.SERVER_PAGE_TAG_PROPERTIES),
//
//         PSObjectTypeFactory.getType(PSObjectTypes.CONFIGURATION_FILE,
//            PSObjectTypes.ConfigurationFileSubTypes.LOGGER_PROPERTIES),
//
//         PSObjectTypeFactory.getType(PSObjectTypes.RELATIONSHIP_TYPE),
         PSObjectTypeFactory.getType(PSObjectTypes.XML_APPLICATION,
            PSObjectTypes.XmlApplicationSubTypes.SYSTEM),
         PSObjectTypeFactory.getType(PSObjectTypes.XML_APPLICATION,
            PSObjectTypes.XmlApplicationSubTypes.USER),
      };

      for (PSObjectType type : types)
         p.process(type);
   }

   /**
    * Walks the enumeration of all known types that have file/folder sub types
    * and passes them to the supplied processor.
    * 
    * @param p Assumed not <code>null</code>.
    */
   private void processFileHierarchyTypes(Processor p) throws Exception
   {
      PSObjectType[] types =
      {
         // pick an arbitrary sub-type, it doesn't matter which one
         PSObjectTypeFactory.getType(PSObjectTypes.RESOURCE_FILE,
            PSObjectTypes.FileSubTypes.FILE),

         PSObjectTypeFactory.getType(PSObjectTypes.USER_FILE,
            PSObjectTypes.UserFileSubTypes.PLACEHOLDER),

//         PSObjectTypeFactory.getType(PSObjectTypes.USER_FILE,
//            PSObjectTypes.UserFileSubTypes.WORKBENCH_FOLDER),
//
//         PSObjectTypeFactory.getType(PSObjectTypes.XML_APPLICATION_FILE,
//            PSObjectTypes.FileSubTypes.FILE),
//
//         PSObjectTypeFactory.getType(PSObjectTypes.LOCAL_FILE,
//            PSObjectTypes.FileSubTypes.FILE),
//         PSObjectTypeFactory.getType(PSObjectTypes.DB_TYPE,
//            DataBaseObjectSubTypes.CATEGORY),
//         PSObjectTypeFactory.getType(PSObjectTypes.DB_TYPE,
//            DataBaseObjectSubTypes.DATASOURCE),
//         PSObjectTypeFactory.getType(PSObjectTypes.DB_TYPE,
//            DataBaseObjectSubTypes.TABLE),
//         PSObjectTypeFactory.getType(PSObjectTypes.DB_TYPE,
//            DataBaseObjectSubTypes.VIEW),
      };
      for (PSObjectType type : types)
         p.process(type);
   }

   /**
    * 
    * @param pt Assumed not <code>null</code>.
    * @return <code>true</code> if <code>pt</code> is a type such that
    * calling <code>IPSCmsMode.getHierarchyManager</code> does not return
    * <code>null</code>.
    */
   private boolean supportsHierarchyManager(Enum pt)
   {
      return pt == PSObjectTypes.RESOURCE_FILE
         || pt == PSObjectTypes.LOCAL_FILE || pt == PSObjectTypes.USER_FILE
         || pt == PSObjectTypes.XML_APPLICATION_FILE
         || pt == PSObjectTypes.DB_TYPE;
   }

   /**
    * If the supplied type only has a single object, return <code>true</code>.
    * 
    * @param type Assumed not <code>null</code>.
    * 
    * @return <code>true</code> if the model for this type will only ever
    * return 1 object.
    */
   private boolean isSingletonType(Enum type)
   {
      return 
            type == PSObjectTypes.CONTENT_TYPE_SYSTEM_CONFIG
         || type == PSObjectTypes.LEGACY_CONFIGURATION
         || type == PSObjectTypes.SHARED_FIELDS
         || type == PSObjectTypes.AUTO_TRANSLATION_SET;
   }
   
   /**
    * 
    * @param pt Assumed not <code>null</code>.
    * @return <code>true</code> if <code>pt</code> is a type such that
    * calling <code>IPSCmsMode.create</code> will throw an
    * <code>UnsupportedOperationException</code>.
    */
   private boolean doesNotSupportCreate(Enum pt)
   {
      return 
            pt == PSObjectTypes.AUTO_TRANSLATION_SET
         || pt == PSObjectTypes.CONFIGURATION_FILE
         || pt == PSObjectTypes.CONTENT_EDITOR_CONTROLS
         || pt == PSObjectTypes.CONTENT_TYPE_SYSTEM_CONFIG
         || pt == PSObjectTypes.LEGACY_CONFIGURATION
         || pt == PSObjectTypes.ROLE 
         || pt == PSObjectTypes.SHARED_FIELDS
         || pt == PSObjectTypes.WORKFLOW
         || pt == PSObjectTypes.UI_ACTION_MENU_MISC
         || pt == PSObjectTypes.SITE
         || pt == PSObjectTypes.AUTO_TRANSLATION_SET
         || pt == PSObjectTypes.DB_TYPE;
   }

   /**
    * Does this primary type support create children?
    * 
    * @param pt Assumed not <code>null</code>.
    * @return <code>true</code> if <code>pt</code> is a type such that
    * calling <code>IPSHierarchyManager.createChildren</code> will throw an
    * <code>UnsupportedOperationException</code>.
    */
   private boolean doesNotSupportCreateChildren(Enum pt)
   {
      return pt == PSObjectTypes.DB_TYPE;
   }

   /**
    * Does this primary type support move children?
    * 
    * @param pt Assumed not <code>null</code>.
    * @return <code>true</code> if <code>pt</code> is a type such that
    * calling <code>IPSHierarchyManager.moveChildren</code> will throw an
    * <code>UnsupportedOperationException</code>.
    */
   private boolean doesNotSupportMoveChildren(Enum pt)
   {
      return 
            pt == PSObjectTypes.DB_TYPE;
   }

   /**
    * Does this primary type support remove children?
    * 
    * @param pt Assumed not <code>null</code>.
    * @return <code>true</code> if <code>pt</code> is a type such that
    * calling <code>IPSHierarchyManager.removeChildren</code> will throw an
    * <code>UnsupportedOperationException</code>.
    */
   private boolean doesNotSupportRemoveChildren(Enum pt)
   {
      return 
            pt == PSObjectTypes.DB_TYPE;
   }

   /**
    * If a type does not support locked loading and save, it should be added to
    * this method. The framework uses this to skip certain tests.
    * 
    * @param pt Assumed not <code>null</code>.
    * @return <code>true</code> if <code>pt</code> is a type such that
    * calling <code>IPSCmsMode.load</code>(ref, <code>true</code>,
    * <code>false</code>) or <code>IPSCmsMode.save</code> will throw an
    * <code>UnsupportedOperationException</code>.
    */
   private boolean doesNotSupportLockedLoadAndSave(Enum pt)
   {
      return pt == PSObjectTypes.ROLE 
            || pt == PSObjectTypes.WORKFLOW
            || pt == PSObjectTypes.UI_ACTION_MENU_MISC
            || pt == PSObjectTypes.CONFIGURATION_FILE
            || pt == PSObjectTypes.SITE
            || pt == PSObjectTypes.DB_TYPE;
   }

   /**
    * 
    * @param pt Assumed not <code>null</code>.
    * @return <code>true</code> if <code>pt</code> is a type such that
    * calling <code>IPSCmsMode.load</code> will throw an
    * <code>UnsupportedOperationException</code>.
    */
   private boolean doesNotSupportRename(Enum pt)
   {
      return pt == PSObjectTypes.CONTENT_TYPE_SYSTEM_CONFIG
         || pt == PSObjectTypes.UI_ACTION_MENU_MISC
         || pt == PSObjectTypes.CONFIGURATION_FILE
         || pt == PSObjectTypes.WORKFLOW
         || pt == PSObjectTypes.SITE
         || pt == PSObjectTypes.AUTO_TRANSLATION_SET
         || pt == PSObjectTypes.DB_TYPE;
   }

   /**
    * client sessionid used to lock the objects. We do not want to use multiples
    * of this in the tests' life time.
    */
   static private String ms_sessionid = new Date().getTime()+"";
}
