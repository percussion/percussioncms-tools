/******************************************************************************
 *
 * [ PSLocalObjectStoreTest.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.proxies;

import com.percussion.client.PSModelException;
import com.percussion.conn.PSServerException;
import com.percussion.content.IPSMimeContent;
import com.percussion.content.PSMimeContentAdapter;
import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSApplicationFile;
import com.percussion.design.objectstore.PSNotLockedException;
import com.percussion.share.service.exception.PSValidationException;
import com.percussion.error.PSIllegalStateException;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.util.IOTools;
import junit.framework.TestCase;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PSLocalObjectStoreTest extends TestCase
{
   public void test01UnimplementedMethods() throws Exception
   {
      // if you implement any new method when removing method from this test
      // please create new unit test for it
      final PSLocalObjectStore store = new PSLocalObjectStore();
      try
      {
         store.createApplication();
         fail();
      }
      catch (UnsupportedOperationException success) {}

      try
      {
         store.createApplication(null);
         fail();
      }
      catch (UnsupportedOperationException success) {}

      try
      {
         store.extendApplicationLock(null);
         fail();
      }
      catch (UnsupportedOperationException success) {}

      try
      {
         store.extendApplicationLock(null, 0);
         fail();
      }
      catch (UnsupportedOperationException success) {}

      try
      {
         store.extendApplicationLock(null, 0, true);
         fail();
      }
      catch (UnsupportedOperationException success) {}

      try
      {
         store.extendApplicationLock(null, 0, true);
         fail();
      }
      catch (UnsupportedOperationException success) {}

      try
      {
         store.extendServerConfigurationLock(null);
         fail();
      }
      catch (UnsupportedOperationException success) {}

      try
      {
         store.extendServerConfigurationLock(null, 0);
         fail();
      }
      catch (UnsupportedOperationException success) {}

      try
      {
         store.extendServerConfigurationLock(null, 0, true);
         fail();
      }
      catch (UnsupportedOperationException success) {}

      try
      {
         store.getApplicationFiles(null);
         fail();
      }
      catch (UnsupportedOperationException success) {}

      try
      {
         store.getApplicationLog((PSApplication) null, null, null);
         fail();
      }
      catch (UnsupportedOperationException success) {}

      try
      {
         store.getApplicationLog((String) null, null, null);
         fail();
      }
      catch (UnsupportedOperationException success) {}

      try
      {
         store.getCatalogerConfigs(true);
         fail();
      }
      catch (UnsupportedOperationException success) {}

      try
      {
         store.getCharacterSetMap();
         fail();
      }
      catch (UnsupportedOperationException success) {}

      try
      {
         store.getConnectionDetail(null);
         fail();
      }
      catch (UnsupportedOperationException success) {}

      try
      {
         store.getContentEditorSharedDef(true, true);
         fail();
      }
      catch (UnsupportedOperationException success) {}

      try
      {
         store.getContentEditorSystemDef(true, true);
         fail();
      }
      catch (UnsupportedOperationException success) {}

      try
      {
         store.getDatasourceConfigs(true);
         fail();
      }
      catch (UnsupportedOperationException success) {}

      try
      {
         store.getHibernateDialectConfig(true);
         fail();
      }
      catch (UnsupportedOperationException success) {}

      try
      {
         store.getJndiDatasources(true);
         fail();
      }
      catch (UnsupportedOperationException success) {}

      try
      {
         store.getRoleConfiguration(true, true, true);
         fail();
      }
      catch (UnsupportedOperationException success) {}

      try
      {
         store.getRxConfiguration(null, true, true, true);
         fail();
      }
      catch (UnsupportedOperationException success) {}

      try
      {
         store.getServerConfiguration();
         fail();
      }
      catch (UnsupportedOperationException success) {}

      try
      {
         store.getServerConfiguration(true);
         fail();
      }
      catch (UnsupportedOperationException success) {}

      try
      {
         store.getServerConfiguration(true, true);
         fail();
      }
      catch (UnsupportedOperationException success) {}

      try
      {
         store.getSupportedFeatureSet();
         fail();
      }
      catch (UnsupportedOperationException success) {}

      try
      {
         store.getUserConfiguration();
         fail();
      }
      catch (UnsupportedOperationException success) {}

      try
      {
         store.loadExtensionDef(null);
         fail();
      }
      catch (UnsupportedOperationException success) {}

      try
      {
         store.releaseServerConfigurationLock(null);
         fail();
      }
      catch (UnsupportedOperationException success) {}

      try
      {
         store.removeApplication((PSApplication) null);
         fail();
      }
      catch (UnsupportedOperationException success) {}

      try
      {
         store.removeApplication((String) null);
         fail();
      }
      catch (UnsupportedOperationException success) {}

      try
      {
         store.removeExtension(null);
         fail();
      }
      catch (UnsupportedOperationException success) {}

      try
      {
         store.removeUserConfiguration();
         fail();
      }
      catch (UnsupportedOperationException success) {}

      try
      {
         store.removeUserConfiguration();
         fail();
      }
      catch (UnsupportedOperationException success) {}

      try
      {
         store.renameApplication((PSApplication) null, null);
         fail();
      }
      catch (UnsupportedOperationException success) {}

      try
      {
         store.renameApplication((String) null, null);
         fail();
      }
      catch (UnsupportedOperationException success) {}

      try
      {
         store.saveApplication(null, true, true, true);
         fail();
      }
      catch (UnsupportedOperationException success) {}

      try
      {
         store.saveCatalogerConfigs(null);
         fail();
      }
      catch (UnsupportedOperationException success) {}

      try
      {
         store.saveDatsourceConfigs(null);
         fail();
      }
      catch (UnsupportedOperationException success) {}

      try
      {
         store.saveExtension(null, null, null, true);
         fail();
      }
      catch (UnsupportedOperationException success) {}

      try
      {
         store.saveHibernateDialectConfig(null);
         fail();
      }
      catch (UnsupportedOperationException success) {}

      try
      {
         store.saveJndiDatasources(null);
         fail();
      }
      catch (UnsupportedOperationException success) {}

      try
      {
         store.saveRoleConfiguration(null, true);
         fail();
      }
      catch (UnsupportedOperationException success) {}

      try
      {
         store.saveRxConfiguration(null, null, true);
         fail();
      }
      catch (UnsupportedOperationException success) {}

      try
      {
         store.saveServerConfiguration(null);
         fail();
      }
      catch (UnsupportedOperationException success) {}

      try
      {
         store.saveServerConfiguration(null, true);
         fail();
      }
      catch (UnsupportedOperationException success) {}

      try
      {
         store.saveTableDefinitions(null, null);
         fail();
      }
      catch (UnsupportedOperationException success) {}

      try
      {
         store.saveUserConfiguration(null);
         fail();
      }
      catch (UnsupportedOperationException success) {}

      try
      {
         store.setClientGeneratedSessionId(null);
         fail();
      }
      catch (UnsupportedOperationException success) {}
   }
   
   public void test02Locking()
   {
      final PSLocalObjectStore store = new PSLocalObjectStore();
      // locking operations do nothing
      store.extendApplicationLock(null, true);
      store.releaseApplicationLock(null);
   }
   
   public void test03GetApplication()
   {
      final PSLocalObjectStore store = new PSLocalObjectStore();
      final PSApplication application = store.getApplication(NAME, true, true);
      assertEquals(NAME, application.getName());
      assertEquals(NAME, application.getRequestRoot());
   }
   
   @SuppressWarnings("unused")
   public void test04GetApplicationFiles() throws IOException, PSModelException
   {
      final PSLocalObjectStore store = new PSLocalObjectStore();
      final PSApplication application = store.getApplication(NAME, true, true);
      
      final File localDir = new File("./build/testDir");
      IOTools.deleteFile(localDir);
      store.setLocalDir(localDir);
      final File appDir = new File(localDir, NAME);
      // level 1 files
      final File dir1 = new File(appDir, "dir1");
      final File dir2 = new File(appDir, "dir2");
      final File file3 = new File(appDir, "file3");
      
      // level 2 files
      final File dir1_1 = new File(dir1, "dir1_1");
      final File file1_2 = new File(dir1, "file1_2");

      try
      {
         localDir.mkdirs();
         appDir.mkdirs();
         dir1.mkdirs();
         dir2.mkdirs();
         writeToFile(file3, STR1);
         dir1_1.mkdirs();
         writeToFile(file1_2, STR2);
         
         // wrong dir
         try
         {
            final PSApplicationFile file = new PSApplicationFile(new File("Unknown"));
            store.getApplicationFiles(application, file);
            fail();
         }
         catch (IllegalArgumentException success) {}
         
         // root level files
         final PSApplicationFile dir1AppFile;
         final PSApplicationFile dir2AppFile;
         final PSApplicationFile file3AppFile;
         {
            final Collection<PSApplicationFile> files = store.getApplicationFiles(application, null);
            assertEquals(files.toString(), 3, files.size());
            
            dir1AppFile = getAppFileForName(files, "dir1");
            dir2AppFile = getAppFileForName(files, "dir2");
            file3AppFile = getAppFileForName(files, "file3");
            
            assertTrue(dir1AppFile.isFolder());
            assertTrue(dir2AppFile.isFolder());
            assertFalse(file3AppFile.isFolder());
            assertTrue(dir1AppFile.isNull());
            assertTrue(dir2AppFile.isNull());
            assertTrue(file3AppFile.isNull());
         }

         // subfolder
         {
            final Collection<PSApplicationFile> files1 =
               store.getApplicationFiles(application, dir1AppFile);
            assertEquals(2, files1.size());
            final PSApplicationFile dir1_1AppFile = getAppFileForName(files1, "dir1_1");
            final PSApplicationFile file1_2AppFile = getAppFileForName(files1, "file1_2");

            assertTrue(dir1_1AppFile.isFolder());
            assertFalse(file1_2AppFile.isFolder());
            assertTrue(dir1_1AppFile.isNull());
            assertTrue(file1_2AppFile.isNull());
         }
         
         // file, not a folder
         try
         {
            store.getApplicationFiles(application, file3AppFile);
            fail();
         }
         catch (IllegalArgumentException success) {}
         
         // empty subfolder
         assertTrue(store.getApplicationFiles(application, dir2AppFile).isEmpty());
      }
      finally
      {
         dir1_1.delete();
         file1_2.delete();
         dir1.delete();
         dir2.delete();
         file3.delete();
         appDir.delete();
         localDir.delete();
      }
   }
   
   /**
    * Returns application file for the specified name with the collection.
    * If there is no such file the method throws {@link IllegalArgumentException}.
    */
   private PSApplicationFile getAppFileForName(Collection<PSApplicationFile> files, String fileName)
   {
      for (final PSApplicationFile file : files)
      {
         if (file.getFileName().getName().equals(fileName))
         {
            return file;
         }
      }
      throw new IllegalArgumentException("No file \"" + fileName +
            "\" in collection " + files);
   }

   /**
    * Rewrites the file with the specified string.
    */
   private void writeToFile(final File file, final String str) throws IOException
   {
      final Writer writer = new FileWriter(file);
      writer.append(str);
      writer.close();
   }
   
   public void test05MoveApplicationFile() throws IOException
   {
      final PSLocalObjectStore store = new PSLocalObjectStore();
      final PSApplication application1 = store.getApplication(NAME, true, true);
      final PSApplication application2 = store.getApplication(NAME2, true, true);
      
      final File localDir = new File("./build/testDir");
      store.setLocalDir(localDir);
      final File appDir1 = new File(localDir, application1.getRequestRoot());
      final File appDir2 = new File(localDir, application2.getRequestRoot());
      // level 1 files
      final File dir1 = new File(appDir1, "dir1");
      final File dir2 = new File(appDir2, "dir2");
      
      // level 2 files
      final File dir1_1 = new File(dir1, "dir1_1");
      final File file1_2 = new File(dir1, "file1_2");

      try
      {
         localDir.mkdirs();
         appDir1.mkdirs();
         appDir2.mkdirs();
         dir1.mkdirs();
         dir2.mkdirs();
         dir1_1.mkdirs();
         writeToFile(file1_2, STR2);
         
         // wrong dir
         try
         {
            final PSApplicationFile file = new PSApplicationFile(new File("Unknown"));
            store.moveApplicationFile(application1, file, application2, new PSApplicationFile(dir2, true), true);
            fail();
         }
         catch (IllegalArgumentException success) {}
         
         // move dir1 into dir2
         final PSApplicationFile dir1AppFile = getAppFileForName(
               store.getApplicationFiles(application1, null), "dir1");
         
         final PSApplicationFile dir2AppFile =
            getAppFileForName(store.getApplicationFiles(application2, null), "dir2");
         final PSApplicationFile movedDir1AppFile =
            new PSApplicationFile(new File(dir2AppFile.getFileName(), "dir1"), true);
         store.moveApplicationFile(application1, dir1AppFile,
               application2, movedDir1AppFile, true);
         assertFalse(dir1.exists());
         assertTrue(new File(dir2, dir1.getName()).exists());
         
         // move back for deletion
         store.moveApplicationFile(application2, movedDir1AppFile, application1,
               dir1AppFile, true);
         assertTrue(dir1.exists());
         assertFalse(new File(dir2, dir1.getName()).exists());
      }
      finally
      {
         dir1_1.delete();
         file1_2.delete();
         dir1.delete();
         dir2.delete();
         appDir2.delete();
         appDir1.delete();
         localDir.delete();
      }
   }
   
   public void test06RenameApplicationFile()
         throws PSServerException, PSAuthorizationException,
         PSAuthenticationFailedException, PSNotLockedException,
         PSValidationException
   {
      // make sure base class functionality calls overloaded moveApplicationFile 
      final PSLocalObjectStore store = new PSLocalObjectStore()
      {
         @SuppressWarnings("unused")
         @Override
         public void moveApplicationFile(PSApplication app,
               PSApplicationFile appFile, PSApplication targetApp,
               PSApplicationFile targetAppFile, boolean releaseLock)
         {
            m_moveApplicationFileWasCalled = true;
         }
      };
      final PSApplication application = store.getApplication(NAME, true, true);
      final PSApplicationFile appFile = new PSApplicationFile(new File("dir"), true);

      m_moveApplicationFileWasCalled = false;
      store.renameApplicationFile(application, appFile, "newname", true);
      assertTrue(m_moveApplicationFileWasCalled);
   }
   
   public void test07RemoveApplicationFile() throws IOException
   {
      final PSLocalObjectStore store = new PSLocalObjectStore();
      final PSApplication application = store.getApplication(NAME, true, true);
      
      final File localDir = new File("./build/testDir");
      store.setLocalDir(localDir);
      final File appDir = new File(localDir, NAME);
      // level 1 files
      final File dir1 = new File(appDir, "dir1");
      
      // level 2 files
      final File dir1_1 = new File(dir1, "dir1_1");
      final File file1_2 = new File(dir1, "file1_2");

      try
      {
         localDir.mkdirs();
         appDir.mkdirs();
         dir1.mkdirs();
         dir1_1.mkdirs();
         writeToFile(file1_2, STR2);
         
         // non-existing file
         try
         {
            final PSApplicationFile file = new PSApplicationFile(new File("Unknown"));
            store.removeApplicationFile(application, file, true);
            fail();
         }
         catch (IllegalArgumentException success) {}
         
         // root level dir
         try
         {
            final PSApplicationFile file = new PSApplicationFile(null);
            store.removeApplicationFile(application, file, true);
            fail();
         }

         catch (IllegalArgumentException success) {}
         assertTrue(dir1.exists());
         final Collection<PSApplicationFile> files = store.getApplicationFiles(application, null);
         assertEquals(files.toString(), 1, files.size());
         
         final PSApplicationFile dir1AppFile = getAppFileForName(files, "dir1");
         store.removeApplicationFile(application, dir1AppFile, true);
         assertFalse(dir1.exists());
      }
      finally
      {
         dir1_1.delete();
         file1_2.delete();
         dir1.delete();
         appDir.delete();
         localDir.delete();
      }
   }
   
   public void test08SaveLoadApplicationFile() throws IllegalStateException, PSIllegalStateException, IOException
   {
      final PSLocalObjectStore store = new PSLocalObjectStore();
      final PSApplication application = store.getApplication(NAME, true, true);
      
      final File localDir = new File("./build/testDir");
      store.setLocalDir(localDir);
      final File appDir = new File(localDir, NAME);
      // level 1 files
      final File dir1 = new File(appDir, "dir1");
      
      // level 2 files
      final File dir1_1 = new File(dir1, "dir1_1");
      final File file1_2 = new File(dir1, FILE_NAME);

      try
      {
         localDir.mkdirs();
         appDir.mkdirs();
         
         final PSApplicationFile dir1AppFile =
            new PSApplicationFile(new File("dir1"), true);
         final PSApplicationFile dir1_1AppFile =
            new PSApplicationFile(new File("dir1/dir1_1"), true);

         // save file to a non-existing dir
         try
         {
            store.saveApplicationFile(application, createAppFileForStr(STR1), true, true);
            fail();
         }
         catch (IllegalArgumentException success) {}

         // save non-existing dir
         try
         {
            store.saveApplicationFile(application, dir1_1AppFile, true, true);
            fail();
         }
         catch (IllegalArgumentException success) {}

         // load non-existing file 
         try
         {
            store.loadApplicationFile(application, new PSApplicationFile(new File("unknown"), true));
            fail();
         }
         catch (IllegalArgumentException success) {}

         // create a dir
         assertFalse(dir1.exists());
         store.saveApplicationFile(application, dir1AppFile, true, true);
         assertTrue(dir1.exists());

         // create a subdir
         assertFalse(dir1_1.exists());
         store.saveApplicationFile(application, dir1_1AppFile, true, true);
         assertTrue(dir1_1.exists());
         
         // create a file
         assertFalse(file1_2.exists());
         store.saveApplicationFile(application, createAppFileForStr(STR1), true, true);
         assertTrue(file1_2.exists());
         final long length1 = file1_2.length();
         
         // overwrite a file
         store.saveApplicationFile(application, createAppFileForStr(STR1 + "sss"), true, true);
         final long length2 = file1_2.length();
         assertTrue(length2 > length1);
         
         // try to save the file without overwriting
         try
         {
            store.saveApplicationFile(application, createAppFileForStr(STR1 + "sss"), false, true);
            fail();
         }
         catch (IllegalArgumentException success) {}
         
         // try to save directory without overwriting
         store.saveApplicationFile(application, dir1AppFile, true, true);
         try
         {
            store.saveApplicationFile(application, dir1AppFile, false, true);
            fail();
         }
         catch (IllegalArgumentException success) {}
         
         // load a directory
         {
            final PSApplicationFile appFile = store.loadApplicationFile(application, dir1AppFile);
            assertEquals(dir1AppFile.getFileName(), appFile.getFileName());
            assertEquals(dir1AppFile.isFolder(), appFile.isFolder());
            assertTrue(appFile.isNull());
         }
         
         // load a file
         {
            final PSApplicationFile file =
                  new PSApplicationFile(new File("dir1/" + FILE_NAME), false);
            final PSApplicationFile appFile =
                  store.loadApplicationFile(application, file);
            assertFalse(appFile.isNull());
            appFile.getContent().getContent().close();
         }
      }
      finally
      {
         dir1_1.delete();
         file1_2.delete();
         dir1.delete();
         appDir.delete();
         localDir.delete();
      }
   }

   /**
    * Creates application file containing application string in the content.
    */
   private PSApplicationFile createAppFileForStr(final String str)
   {
      final PSApplicationFile fileAppFile1;
      final ByteArrayInputStream in = new ByteArrayInputStream(str.getBytes());
      final IPSMimeContent content = new PSMimeContentAdapter(in, null, null, null, -1);
      fileAppFile1 = new PSApplicationFile(content, new File("dir1/" + FILE_NAME));
      return fileAppFile1;
   }
   
   /**
    * Sample name.
    */
   private static final String NAME = "Name1";
   
   /**
    * Sample name.
    */
   private static final String NAME2 = "Name2";

   /**
    * Sample string
    */
   private static final String STR1 = "HELLO 3";
   
   /**
    * Sample string
    */
   private static final String STR2 = "HELLO 1_2";

   /**
    * Sample file name. 
    */
   private static final String FILE_NAME = "file1_2.txt";

   /**
    * Indicates whether method moveApplicationFile was called.
    */
   private boolean m_moveApplicationFileWasCalled;

}
