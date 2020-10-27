/******************************************************************************
 *
 * [ PSFileEditorHelper.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.util;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.internal.registry.EditorDescriptor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Helps in finding and opening editor for a file.
 *
 * @author Andriy Palamarchuk
 */
public class PSFileEditorHelper
{
   private final static Logger ms_log = Logger.getLogger(PSFileEditorHelper.class);
   
   /**
    * Determines content type for the provided file from the file name and content.
    * @param file existing file to determin content type. Can't be <code>null</code>.
    * @return the file content type. Can be <code>null</code>.
    */
   private IContentType getContentType(final IFile file)
   {
      try
      {
         return getContentTypeForStream(file.getContents(), file.getName()); 
      }
      catch (CoreException e)
      {
         ms_log.warn("Failed while determining content type", e); //$NON-NLS-1$
         return null;
      }
   }

   /**
    * Determines content type for the provided file from the file name and content.
    * @param file existing file to determin content type. Can't be <code>null</code>.
    * @return the file content type. Can be <code>null</code>.
    */
   public IContentType getContentType(final File file)
   {
      try
      {
         return getContentTypeForStream(new FileInputStream(file), file.getName());
      }
      catch (FileNotFoundException e)
      {
         logDeterminingContentTypeFailure(e);
         return null;
      }
   }

   /**
    * Defines content type for the specified stream and file name.
    * It closes the provided stream.
    * @param stream stream to define content type for. Not <code>null</code>.
    * @param fileName non-empty file name to define content type for.
    * The file extension affects the content type returned by this methos.
    * @return never <code>null</code>
    */
   private IContentType getContentTypeForStream(
         InputStream stream, final String fileName)
   {
      assert stream != null;
      try
      {
         return Platform.getContentTypeManager()
               .findContentTypeFor(stream, fileName);
      }
      catch (IOException e)
      {
         logDeterminingContentTypeFailure(e);
         return null;
      }
      finally
      {
         try
         {
            stream.close();
         }
         catch (IOException e)
         {
            logDeterminingContentTypeFailure(e);
         }
      }
   }

   /**
    * Logs error message for IO exception during determining content type.
    * @param e exception to log.
    */
   private void logDeterminingContentTypeFailure(IOException e)
   {
      ms_log.warn("Failed while determining content type", e); //$NON-NLS-1$
   }

   /**
    * Creates temporary file and copies provided content to that file.
    * The temporary file is created in default temporary directory and is
    * removed upon completion of the application.
    * @param fileName the original file name of the file. 
    * @param content file data
    * @return the file pointing to the newly created temporary file. 
    */
   public File copyToTempFile(final String fileName, final InputStream content)
         throws IOException, FileNotFoundException
   {
      final File file = createTempFile(fileName);
      final OutputStream fileOut = new FileOutputStream(file);
      try
      {
         IOUtils.copy(content, fileOut);
      }
      finally
      {
         fileOut.close();
      }
      return file;
   }

   /**
    * Creates temporary file with the name starting with the name of provided
    * file name and extension of the provided file name. ".tmp" extension is
    * used if the provided file name does not have any extension. 
    * The file is deleted automatically on application completion.
    */
   public File createTempFile(final String fileName) throws IOException
   {
      final int MIN_PREFIX_SIZE = 3;

      final String origFileName = new File(fileName).getName();
      final String prefix;
      final String suffix;
      if (origFileName.contains(".")) //$NON-NLS-1$
      {
         final int lastDot = origFileName.lastIndexOf('.');
         prefix = origFileName.substring(0, lastDot);
         suffix = origFileName.substring(lastDot);
      }
      else
      {
         prefix = origFileName;
         suffix = null;
      }
      final File file = File.createTempFile(
            StringUtils.rightPad(prefix, MIN_PREFIX_SIZE, 'a'), suffix);
      file.deleteOnExit();
      return file;
   }
   
   /**
    * Creates editor part to edit the specified file.
    */
   public IEditorPart createEditorPartToEdit(final IFile file) throws CoreException
   {
      return ((EditorDescriptor) findEditorDescriptor(file)).createEditor();
   }
   
   /**
    * Finds and returns {@link IEditorDescriptor} for internal editor which can
    * handle the file.
    */
   private IEditorDescriptor findEditorDescriptor(final IFile file) {
      final IEditorDescriptor[] descriptors =
            getEditorRegistry().getEditors(file.getName(), getContentType(file));
      for (final IEditorDescriptor descriptor : descriptors)
      {
         // Andriy: work around what seems to be Eclipse bug
         // when null value is returned, particular when the app launcher
         // is configured to clean up workspace before launching
         if (descriptor == null)
         {
            continue;
         }
         if (descriptor != null && descriptor.isInternal())
         {
            return descriptor;
         }
      }
      return getEditorRegistry().findEditor(EditorsUI.DEFAULT_TEXT_EDITOR_ID);
   }

   /**
    * Returns current editor registry.
    */
   private IEditorRegistry getEditorRegistry()
   {
      return getWorkbench().getEditorRegistry();
   }

   /**
    * Editor worbench.
    */
   private IWorkbench getWorkbench()
   {
      return PlatformUI.getWorkbench(); 
   }
}
