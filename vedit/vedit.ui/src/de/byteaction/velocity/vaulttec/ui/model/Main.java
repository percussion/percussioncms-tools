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

package de.byteaction.velocity.vaulttec.ui.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class Main
{

    public static void main(String[] args)
    {
        File a = new File("/tmp/a");
        File b = new File("/tmp/b");
        copyFile(a, b);
        System.err.println("copied");
    }

    public static void copyFile(File source, File dest)
    {
        try
        {
            FileChannel in = new FileInputStream(source).getChannel();
            if (!dest.getParentFile().exists()) dest.getParentFile().mkdirs();
            FileChannel out = new FileOutputStream(dest).getChannel();
            in.transferTo(0, in.size(), out);
            in.close();
            out.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}