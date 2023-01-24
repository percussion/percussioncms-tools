/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
