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

import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IAutoIndentStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.reconciler.MonoReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.DefaultPartitioner;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import de.byteaction.velocity.editor.VelocityCompletionProcessor;
import de.byteaction.velocity.editor.VelocityEditor;
import de.byteaction.velocity.scanner.HTMLScanner;
import de.byteaction.velocity.scanner.HTMLScriptScanner;
import de.byteaction.velocity.scanner.HTMLTagScanner;
import de.byteaction.velocity.scanner.NonRuleBasedDamagerRepairer;
import de.byteaction.velocity.scanner.VelocityPartitionScanner;
import de.byteaction.velocity.ui.editor.xml.IEditorConfiguration;
import de.byteaction.velocity.ui.editor.xml.VelocityAutoIndentStrategy;
import de.byteaction.velocity.ui.editor.xml.WholePartitionDamagerRepairer;
import de.byteaction.velocity.vaulttec.ui.IColorConstants;
import de.byteaction.velocity.vaulttec.ui.VelocityColorProvider;

/**
 * DOCUMENT ME!
 * 
 * @version $Revision: 1.1 $
 * @author <a href="mailto:as@byteaction.de">Akmal Sarhan </a>
 */
public class VelocityConfiguration extends SourceViewerConfiguration implements IEditorConfiguration
{

    public static final String[]       PREFORMATTED_PARTITIONS = { "__script_partition", "__cdata_partition", "__proc_inst_partition" };
    // public static final String[] ESCAPED_PARTITIONS = { "__script_partition",
    // "__cdata_partition", "__proc_inst_partition", "__comment_partition" };
    public static final String[]       ESCAPED_PARTITIONS      = { "__cdata_partition", "__proc_inst_partition", "__comment_partition" }; // look
                                                                                                                                            // at
                                                                                                                                            // the
                                                                                                                                            // commented
                                                                                                                                            // out
                                                                                                                                            // code
                                                                                                                                            // line
                                                                                                                                            // 760
                                                                                                                                            // in
                                                                                                                                            // VelocityCompletionProcessor
    public static final String[]       CDATA_PARTITIONS        = { "__cdata_partition" };
    private static Set                 fEMPTY_TAG_SET;
    static
    {
        fEMPTY_TAG_SET = new HashSet();
        for (int i = 0; i < de.byteaction.velocity.ui.editor.xml.IHTMLConstants.EMPTY_TAGS.length; i++)
        {
            fEMPTY_TAG_SET.add(de.byteaction.velocity.ui.editor.xml.IHTMLConstants.EMPTY_TAGS[i]);
        }
    }
    private VelocityEditor             fEditor;
    private VelocityAutoIndentStrategy fAutoIndentStrategy;
    private HTMLTagScanner             fTagScanner;
    private HTMLScriptScanner          fScriptScanner;
    private HTMLScanner                fScanner;
    VelocityColorProvider              cp                      = null;

    public VelocityConfiguration(VelocityEditor anEditor)
    {
        fEditor = anEditor;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getConfiguredContentTypes(org.eclipse.jface.text.source.ISourceViewer)
     */
    public String[] getConfiguredContentTypes(ISourceViewer aSourceViewer)
    {
        return VelocityPartitionScanner.TYPES;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public IDocumentPartitioner createDocumentPartitioner()
    {
        return new DefaultPartitioner(new VelocityPartitionScanner(), VelocityPartitionScanner.TYPES);
    }

    /**
     * DOCUMENT ME!
     * 
     * @param sourceViewer
     *            DOCUMENT ME!
     * @param contentType
     *            DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public IAutoIndentStrategy getAutoIndentStrategy(ISourceViewer sourceViewer, String contentType)
    {
        if (fAutoIndentStrategy == null)
        {
            fAutoIndentStrategy = new VelocityAutoIndentStrategy(this, sourceViewer);
        }
        return fAutoIndentStrategy;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getTextHover(org.eclipse.jface.text.source.ISourceViewer,
     *      java.lang.String)
     */
    public ITextHover getTextHover(ISourceViewer aSourceViewer, String aContentType)
    {
        ITextHover hover;
        if (aContentType.equals(IDocument.DEFAULT_CONTENT_TYPE) || aContentType.equals(IEditorConfiguration.TAG_PARTITION) || aContentType.equals(IEditorConfiguration.PARSED_STRING))
        {
            hover = new VelocityTextHover(fEditor);
        } else
        {
            hover = null;
        }
        return hover;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getAnnotationHover(org.eclipse.jface.text.source.ISourceViewer)
     */
    public IAnnotationHover getAnnotationHover(ISourceViewer aSourceViewer)
    {
        return new VelocityAnnotationHover();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getContentAssistant(org.eclipse.jface.text.source.ISourceViewer)
     */
    public IContentAssistant getContentAssistant(ISourceViewer aSourceViewer)
    {
        ContentAssistant assistant = new ContentAssistant();
        assistant.setContentAssistProcessor(new VelocityCompletionProcessor(fEditor, true), IDocument.DEFAULT_CONTENT_TYPE);
        assistant.setContentAssistProcessor(new VelocityCompletionProcessor(fEditor, false), IEditorConfiguration.PARSED_STRING);
        assistant.setContentAssistProcessor(new VelocityCompletionProcessor(fEditor, false), IEditorConfiguration.CDATA_PARTITION);
        assistant.setContentAssistProcessor(new VelocityCompletionProcessor(fEditor, false), IEditorConfiguration.DOC_COMMENT);
        assistant.setContentAssistProcessor(new VelocityCompletionProcessor(fEditor, false), IEditorConfiguration.MULTI_LINE_COMMENT);
        assistant.setContentAssistProcessor(new VelocityCompletionProcessor(fEditor, false), IEditorConfiguration.PROC_PARTITION);
        assistant.setContentAssistProcessor(new VelocityCompletionProcessor(fEditor, true), IEditorConfiguration.SCRIPT_PARTITION);
        assistant.setContentAssistProcessor(new VelocityCompletionProcessor(fEditor, false), IEditorConfiguration.SINGLE_LINE_COMMENT);
        assistant.setContentAssistProcessor(new VelocityCompletionProcessor(fEditor, true), IEditorConfiguration.TAG_PARTITION);
        assistant.enableAutoInsert(true);
        assistant.enableAutoActivation(true);
        return assistant;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getDoubleClickStrategy(org.eclipse.jface.text.source.ISourceViewer,
     *      java.lang.String)
     */
    public ITextDoubleClickStrategy getDoubleClickStrategy(ISourceViewer aSourceViewer, String aContentType)
    {
        return VelocityEditorEnvironment.getDoubleClickStrategy();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getDefaultPrefixes(org.eclipse.jface.text.source.ISourceViewer,
     *      java.lang.String)
     */
    public String[] getDefaultPrefixes(ISourceViewer aSourceViewer, String aContentType)
    {
        return new String[] { "##", "" };
    }

    protected HTMLScanner getHTMLScanner()
    {
        if (fScanner == null)
        {
            cp = VelocityEditorEnvironment.getColorProvider();
            fScanner = new HTMLScanner();
            fScanner.setDefaultReturnToken(cp.getToken(IColorConstants.HTML_String));
        }
        return fScanner;
    }

    protected HTMLTagScanner getHTMLTagScanner()
    {
        if (fTagScanner == null)
        {
            cp = VelocityEditorEnvironment.getColorProvider();
            fTagScanner = new HTMLTagScanner(cp);
        }
        return fTagScanner;
    }

    protected HTMLScriptScanner getScriptScanner()
    {
        if (fScriptScanner == null)
        {
            cp = VelocityEditorEnvironment.getColorProvider();
            fScriptScanner = new HTMLScriptScanner(cp);
        }
        return fScriptScanner;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getPresentationReconciler(org.eclipse.jface.text.source.ISourceViewer)
     */
    public IPresentationReconciler getPresentationReconciler(ISourceViewer aSourceViewer)
    {
        cp = VelocityEditorEnvironment.getColorProvider();
        PresentationReconciler rec = new PresentationReconciler();
        NonRuleBasedDamagerRepairer ndr = null;
        DefaultDamagerRepairer dr = null;
        dr = new DefaultDamagerRepairer(VelocityEditorEnvironment.getCodeScanner());
        rec.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
        rec.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);
        dr = new DefaultDamagerRepairer(getHTMLTagScanner());
        rec.setDamager(dr, IEditorConfiguration.TAG_PARTITION);
        rec.setRepairer(dr, IEditorConfiguration.TAG_PARTITION);
        ndr = new NonRuleBasedDamagerRepairer(cp.getTextAttribute(IColorConstants.COMMENT));
        rec.setDamager(ndr, IEditorConfiguration.SINGLE_LINE_COMMENT);
        rec.setRepairer(ndr, IEditorConfiguration.SINGLE_LINE_COMMENT);
        ndr = new NonRuleBasedDamagerRepairer(cp.getTextAttribute(IColorConstants.DOC_COMMENT));
        rec.setDamager(ndr, IEditorConfiguration.DOC_COMMENT);
        rec.setRepairer(ndr, IEditorConfiguration.DOC_COMMENT);
        ndr = new NonRuleBasedDamagerRepairer(cp.getTextAttribute(IColorConstants.STRING));
        rec.setDamager(ndr, IEditorConfiguration.UNPARSED_STRING);
        rec.setRepairer(ndr, IEditorConfiguration.UNPARSED_STRING);
        dr = new DefaultDamagerRepairer(VelocityEditorEnvironment.getStringScanner());
        rec.setDamager(dr, IEditorConfiguration.PARSED_STRING);
        rec.setRepairer(dr, IEditorConfiguration.PARSED_STRING);
        WholePartitionDamagerRepairer wdr = new WholePartitionDamagerRepairer(getScriptScanner());
        rec.setDamager(wdr, IEditorConfiguration.SCRIPT_PARTITION);
        rec.setRepairer(wdr, IEditorConfiguration.SCRIPT_PARTITION);
        ndr = new NonRuleBasedDamagerRepairer(cp.getTextAttribute(IColorConstants.COMMENT));
        rec.setDamager(ndr, IEditorConfiguration.MULTI_LINE_COMMENT);
        rec.setRepairer(ndr, IEditorConfiguration.MULTI_LINE_COMMENT);
        return rec;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getReconciler(org.eclipse.jface.text.source.ISourceViewer)
     */
    public IReconciler getReconciler(ISourceViewer aSourceViewer)
    {
        return new MonoReconciler(fEditor.getReconcilingStrategy(), false);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getIndentPrefixes(org.eclipse.jface.text.source.ISourceViewer,
     *      java.lang.String)
     */
    public String[] getIndentPrefixes(ISourceViewer aSourceViewer, String aContentType)
    {
        Vector prefixes = new Vector();
        // Create prefixes from JDT preferences settings
        int tabWidth = getTabWidth(aSourceViewer);
        // IPreferenceStore prefs =
        // JavaPlugin.getDefault().getPreferenceStore();
        boolean useSpaces = true; // prefs.getBoolean(PreferenceConstants.EDITOR_SPACES_FOR_TABS);
        // prefix[0] is either '\t' or ' ' x tabWidth, depending on useSpaces
        for (int i = 0; i <= tabWidth; i++)
        {
            StringBuffer prefix = new StringBuffer();
            if (useSpaces)
            {
                for (int j = 0; (j + i) < tabWidth; j++)
                {
                    prefix.append(' ');
                }
                if (i != 0)
                {
                    prefix.append('\t');
                }
            } else
            {
                for (int j = 0; j < i; j++)
                {
                    prefix.append(' ');
                }
                if (i != tabWidth)
                {
                    prefix.append('\t');
                }
            }
            prefixes.add(prefix.toString());
        }
        prefixes.add("");
        return (String[]) prefixes.toArray(new String[prefixes.size()]);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getTabWidth(org.eclipse.jface.text.source.ISourceViewer)
     */
    public int getTabWidth(ISourceViewer aSourceViewer)
    {
        // Get tab width from JDT preferences settings
        // IPreferenceStore prefs =
        // JavaPlugin.getDefault().getPreferenceStore();
        // return prefs.getInt(PreferenceConstants.EDITOR_TAB_WIDTH);
        return 4;
    }

    /*
     * (non-Javadoc)
     * @see de.byteaction.velocity.vaulttec.ui.editor.xml.IEditorConfiguration#getPreferences()
     */
    public IPreferenceStore getPreferences()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see de.byteaction.velocity.vaulttec.ui.editor.xml.IEditorConfiguration#getLineWidth(org.eclipse.jface.text.source.ISourceViewer)
     */
    public int getLineWidth(ISourceViewer isourceviewer)
    {
        // TODO Auto-generated method stub
        return 80;
    }

    /*
     * (non-Javadoc)
     * @see de.byteaction.velocity.vaulttec.ui.editor.xml.IEditorConfiguration#getTab(org.eclipse.jface.text.source.ISourceViewer)
     */
    public String getTab(ISourceViewer isourceviewer)
    {
        return "\t";
    }
    // public IPresentationReconciler getPresentationReconciler(ISourceViewer
    // sourceViewer)
    // {
    // PresentationReconciler reconciler = new PresentationReconciler();
    // DefaultDamagerRepairer dr = new DefaultDamagerRepairer(getHTMLScanner());
    // reconciler.setDamager(dr, "__dftl_partition_content_type");
    // reconciler.setRepairer(dr, "__dftl_partition_content_type");
    // dr = new DefaultDamagerRepairer(getHTMLTagScanner());
    // reconciler.setDamager(dr, "__tag_partition");
    // reconciler.setRepairer(dr, "__tag_partition");
    // WholePartitionDamagerRepairer wdr = new
    // WholePartitionDamagerRepairer(getScriptScanner());
    // reconciler.setDamager(wdr, "__script_partition");
    // reconciler.setRepairer(wdr, "__script_partition");
    // wdr = new WholePartitionDamagerRepairer(getScriptScanner());
    // reconciler.setDamager(wdr, "__cdata_partition");
    // reconciler.setRepairer(wdr, "__cdata_partition");
    // NonRuleBasedDamagerRepairer ndr = new NonRuleBasedDamagerRepairer(new
    // TextAttribute(EditorsPlugin.getColor("HTMLProcessInstruction")));
    // reconciler.setDamager(ndr, "__proc_inst_partition");
    // reconciler.setRepairer(ndr, "__proc_inst_partition");
    // ndr = new NonRuleBasedDamagerRepairer(new
    // TextAttribute(EditorsPlugin.getColor("HTMLComment")));
    // reconciler.setDamager(ndr, "__comment_partition");
    // reconciler.setRepairer(ndr, "__comment_partition");
    // return reconciler;
    // }
}
