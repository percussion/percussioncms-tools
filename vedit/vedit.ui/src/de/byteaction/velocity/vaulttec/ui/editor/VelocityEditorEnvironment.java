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

package de.byteaction.velocity.vaulttec.ui.editor;

import org.eclipse.jface.text.DefaultTextDoubleClickStrategy;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import de.byteaction.velocity.vaulttec.ui.VelocityColorProvider;
import de.byteaction.velocity.vaulttec.ui.VelocityPlugin;
import de.byteaction.velocity.vaulttec.ui.editor.parser.VelocityParser;
import de.byteaction.velocity.vaulttec.ui.editor.text.VelocityCodeScanner;
import de.byteaction.velocity.vaulttec.ui.editor.text.VelocityStringScanner;

/**
 * This class maintains resources used by multiple instances of the editor.
 * <p>
 * To use this environment an editor has to <code>connect()</code> first.
 * Before disposing an editor a call to <code>disconnect()</code> is
 * necessary.
 */
public class VelocityEditorEnvironment
{

    private static VelocityParser           fgVelocityParser;
    private static VelocityColorProvider    fgColorProvider;
    private static RuleBasedScanner         fgCodeScanner;
    private static RuleBasedScanner         fgStringScanner;
    private static ITextDoubleClickStrategy fgDoubleClickStrategy;
    private static int                      fgRefCount = 0;

    /**
     * A connection has occurred - initialize all resources if it is the first
     * activation.
     */
    public static void connect()
    {
        if (++fgRefCount == 1)
        {
            createVelocityParser();
            fgColorProvider = new VelocityColorProvider();
            fgCodeScanner = new VelocityCodeScanner(fgColorProvider);
            fgStringScanner = new VelocityStringScanner(fgColorProvider);
            fgDoubleClickStrategy = new DefaultTextDoubleClickStrategy();
        }
    }

    /**
     * If an editor is connected then creates a new instance of the Velocity
     * parser and initializes it with data specified in Velocity preferences.
     */
    public static void createVelocityParser()
    {
        if (fgRefCount > 0)
        {
            fgVelocityParser = new VelocityParser();
            try
            {
                fgVelocityParser.init();
            }
            catch (Exception e)
            {
                VelocityPlugin.log(e);
            }
        }
    }

    /**
     * A disconnection has occurred - clear all resources if it is the last
     * deactivation.
     */
    public static void disconnect()
    {
        if (--fgRefCount == 0)
        {
            fgDoubleClickStrategy = null;
            fgStringScanner = null;
            fgCodeScanner = null;
            fgColorProvider.dispose();
            fgColorProvider = null;
            fgVelocityParser = null;
        }
    }

    /**
     * Returns the singleton color provider.
     */
    public static VelocityColorProvider getColorProvider()
    {
        return fgColorProvider;
    }

    /**
     * Returns the singleton code scanner.
     */
    public static RuleBasedScanner getCodeScanner()
    {
        return fgCodeScanner;
    }

    /**
     * Returns the singleton string scanner.
     */
    public static RuleBasedScanner getStringScanner()
    {
        return fgStringScanner;
    }

    /**
     * Returns the singleton double-click strategy.
     */
    public static ITextDoubleClickStrategy getDoubleClickStrategy()
    {
        return fgDoubleClickStrategy;
    }

    /**
     * Returns the singleton Velocity parser.
     */
    public static VelocityParser getParser()
    {
        return fgVelocityParser;
    }
}
