package com.percussion.E2Designer;

import junit.framework.TestCase;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.singletonList;

public class UIFigureFrameTest extends TestCase
{
   public void testIsAcceptableDropFilesList() throws IOException
   {
      final UIFigureFrame frame = new UIFigureFrame("title", new Dimension(), null)
      {
         @Override
         protected FigureFactory getFigureFactory()
         {
            return null;
         }
      };
      
      // no file
      assertFalse(frame.isAcceptableDropFilesList(null));
      assertFalse(frame.isAcceptableDropFilesList(Collections.<File>emptyList()));
      
      final File tmpFile = File.createTempFile("test", ".xml");
      try
      {
         // Ok
         assertTrue(tmpFile.exists());
         assertTrue(frame.isAcceptableDropFilesList(singletonList(tmpFile)));
         
         // more than 1 file
         {
            final List<File> files = new ArrayList<File>();
            files.add(tmpFile);
            files.add(tmpFile);
            assertFalse(frame.isAcceptableDropFilesList(files));
         }
         
         // non-existing file
         tmpFile.delete();
         assertFalse(frame.isAcceptableDropFilesList(singletonList(tmpFile)));
         
         // directory
         tmpFile.mkdir();
         assertTrue(tmpFile.exists());
         assertFalse(frame.isAcceptableDropFilesList(singletonList(tmpFile)));
      }
      finally
      {
         tmpFile.delete();
      }
   }
}
