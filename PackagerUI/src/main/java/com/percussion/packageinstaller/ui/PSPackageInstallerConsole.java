/******************************************************************************
 *
 * [ PSPackageInstallerFrame.java ]
 * 
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.packageinstaller.ui;

import com.percussion.deployer.client.IPSDeployJobControl;
import com.percussion.deployer.client.PSDeploymentManager;
import com.percussion.deployer.objectstore.PSArchive;
import com.percussion.deployer.objectstore.PSArchiveDetail;
import com.percussion.deployer.objectstore.PSArchiveInfo;
import com.percussion.deployer.objectstore.PSExportDescriptor;
import com.percussion.deployer.objectstore.PSImportDescriptor;
import com.percussion.deployer.objectstore.PSImportPackage;
import com.percussion.deployer.objectstore.PSValidationResult;
import com.percussion.deployer.objectstore.PSValidationResults;
import com.percussion.error.IPSDeploymentErrors;
import com.percussion.error.PSDeployException;
import com.percussion.packageinstaller.ui.managers.PSInstallerServerConnectionManager;
import com.percussion.packager.ui.PSResourceUtils;
import com.percussion.packager.ui.data.PSServerRegistration;
import com.percussion.utils.collections.PSMultiValueHashMap;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Luis
 *
 */
public class PSPackageInstallerConsole
{
   public PSPackageInstallerConsole()
   { 

   }
   
   /**
    * Invoked by the selection view when a new package file
    * path has been selected.
    */
   private void consoleSetPackageFile(String file)
   {
      // Attempt to load the specified package to get info and
      // determine if it is valid archive.
      m_packageFile = new File(file); 
      
      PSArchive archive;
      try
      {
         archive = new PSArchive(m_packageFile);
         m_archiveInfo = archive.getArchiveInfo(true);
         m_archiveDetail = m_archiveInfo.getArchiveDetail();
         m_exportDescriptor = m_archiveDetail.getExportDescriptor();  
      }
      catch (PSDeployException e1)
      {
         e1.printStackTrace();
      }
   }
   /**
    * Installs package from command line
    * 
    * @param file - file path
    */
   void doConsoleInstall(String file)
   {
         System.out.println("Installing: " + file);
         consoleSetPackageFile(file);

         final PSInstallerServerConnectionManager connMgr = PSInstallerServerConnectionManager
               .getInstance();

         PSServerRegistration server;

         server = PSPackageInstallerClient.getServerReg();

         // Attempt to get connection
         try
         {

            try
            {
               connMgr.initConnection(server, false);
            }
            catch (Exception e)
            {
               if (e.getMessage().startsWith(
                     "An unexpected error has occurred:"))
               {
                  throw new PSDeployException(
                        IPSDeploymentErrors.UNABLE_TO_CONNECT_TO_SERVER);
               }
               else
               {
                  throw e;
               }
            }

            m_importDescriptor = createImportDescriptor();
            try
            {

               final PSDeploymentManager dm = PSInstallerServerConnectionManager
                     .getInstance().getDeploymentManager();
               // Step 1 -- Validation

               // Do package level validation first
               m_hasErrors = false;
               boolean hasWarnings = false;
               PSMultiValueHashMap<String, String> pkgValResults = dm
                     .validateArchive(m_archiveInfo, false);
               final List<String> pkgErrors = pkgValResults.get("Error");
               final List<String> pkgWarns = pkgValResults.get("Warning");
               m_hasErrors = !pkgErrors.isEmpty();
               hasWarnings = !pkgWarns.isEmpty();

               // If errors or warnings show them and wait

               consoleShowErrorAndWarnings(pkgErrors, pkgWarns);
               if (!pkgErrors.isEmpty())
               {
                  System.out.print("\nExiting\n");
                  System.exit(1);
               }
               if (!PSPackageInstallerClient.acceptWarnings()
                     && !pkgWarns.isEmpty())
               {
                  System.out.print("\nExiting\n");
                  System.exit(2);
               }

               m_job = dm.runValidationJob(m_importDescriptor);

               while (m_job.getStatus() < 100 && m_job.getStatus() != -1)
               {
                  Thread.sleep(250);
               }

               // Step 2 -- Process Validation Results

               dm.loadValidationResults(m_importDescriptor);
               m_hasErrors = false;
               hasWarnings = false;
               final PSMultiValueHashMap<String, String> errors = new PSMultiValueHashMap<String, String>();
               final PSMultiValueHashMap<String, String> warns = new PSMultiValueHashMap<String, String>();
               for (PSImportPackage pkg : m_importDescriptor
                     .getImportPackageList())
               {
                  PSValidationResults results = pkg.getValidationResults();
                  if (results != null)
                  {
                     Iterator<PSValidationResult> entries = results
                           .getResults();
                     while (entries.hasNext())
                     {
                        PSValidationResult entry = entries.next();
                        String name = entry.getDependency().getDisplayName();
                        String type = entry.getDependency().getObjectType();
                        String oType = type.equals("sys_UserDependency")
                              ? getResourceString("type.file.resource")
                              : entry.getDependency().getObjectTypeName();
                        String key = oType + " -- " + name;
                        if (entry.isError())
                        {
                           m_hasErrors = true;
                           if (!errors.containsValue(key, entry.getMessage()))
                              errors.put(key, entry.getMessage());
                        }
                        else
                        {
                           hasWarnings = true;
                           if (!warns.containsValue(key, entry.getMessage()))
                              warns.put(key, entry.getMessage());
                        }
                     }
                  }
               }

               // If errors or warnings show them and wait

               consoleShowErrorAndWarnings(errors, warns);
               if (!errors.isEmpty())
               {
                  System.out.print("\nExiting\n");
                  System.exit(1);
               }
               if (!PSPackageInstallerClient.acceptWarnings()
                     && !warns.isEmpty())
               {
                  System.out.print("\nExiting\n");
                  System.exit(2);
               }

               // Step 3 -- Copy Package to Server

               m_job = dm.copyArchiveToServer(m_exportDescriptor.getName(),
                     m_packageFile);

               while (m_job.getStatus() < 100 && m_job.getStatus() != -1)
               {
                  Thread.sleep(250);
               }

               // Step 4 -- Install Package on Server

               m_job = dm.runImportJob(m_importDescriptor);

               while (m_job.getStatus() < 100 && m_job.getStatus() != -1)
               {
                  Thread.sleep(250);
               }

               if (m_job.getStatus() == -1)
               {
                  System.out.println("Install finished with errors");
               }
               else
               {
                  System.out.println("Install finished successfully");
               }
            }
            catch (final Exception e)
            {
               System.out.print(e);
            }
            finally
            {
               System.exit(0);
            }
         }
         catch (Exception e)
         {
            System.out.print(e);
         }
   } 
   
   /**
    * Helper method to show error and warning messages
    * 
    * @param errors assumed not <code>null</code>.
    * @param warns assumed not <code>null</code>.
    */
   private void consoleShowErrorAndWarnings(List<String> errors,
      List<String> warns)
   {

      if(!errors.isEmpty())
      {  
         System.out.print("Package Errors: \n");
         for(String msg : errors)
         {
            System.out.println(msg);
         }
         System.out.print("Package: " + m_packageFile + " finished with Errors\n\n");
      }
      if(!warns.isEmpty())
      {
         System.out.print("Package Warnings: \n");
         for(String msg : warns)
         {
            System.out.println(msg);
         }
         System.out.print("Package: " + m_packageFile + " finished with Warnings\n\n");
      }
   }
   
   /**
    * Helper method show error and warning messages
    * 
    * @param errors assumed not <code>null</code>.
    * @param warns assumed not <code>null</code>.
    */
   private void consoleShowErrorAndWarnings(PSMultiValueHashMap<String, String> errors,
      PSMultiValueHashMap<String, String> warns)
   {
      Set<String> eTypes = new TreeSet<String>(errors.keySet());
      Set<String> wTypes = new TreeSet<String>(warns.keySet());
      
      if(!eTypes.isEmpty())
      {
         System.out.print("Package Errors: \n");
         for(String type : eTypes)
         {
            List<String> errs = errors.get(type);
            for(String msg : errs)
            {
               System.out.println(msg);
            }
         }
         System.out.print("Package: " + m_packageFile + " finished with Errors\n\n");
      }
      if(!wTypes.isEmpty())
      {
         System.out.print("Package Warnings: \n");
         for(String type : wTypes)
         {
            for(String msg : warns.get(type))
            {
               System.out.println(msg);
            }
         }
         System.out.print("Package: " + m_packageFile + " finished with Warnings\n\n");
      }
   }
   
   /**
    * Create the import descriptor from the archive.
    * @return import descriptor, never <code>null</code>.
    */
   @SuppressWarnings("unchecked")
   private PSImportDescriptor createImportDescriptor()
   {
      
      PSImportDescriptor desc = PSImportDescriptor.configureFromArchive(m_archiveInfo);
      
      return desc;
   
   }   
   
   /**
    * 
    * @param key
    * @return
    */
   private static String getResourceString(String key)
   {
      return PSResourceUtils.getResourceString(PSPackageInstallerFrame.class, key);
   }
   
   PSArchiveInfo m_archiveInfo;
   PSArchiveDetail m_archiveDetail;
   PSExportDescriptor m_exportDescriptor;
   PSImportDescriptor m_importDescriptor;
   private File m_packageFile; 
   
   private IPSDeployJobControl m_job;
   private boolean m_hasErrors;
   private boolean m_suspend;
   private boolean m_cancelRequested;
   
   private boolean m_currentPkgIsValid;

}
