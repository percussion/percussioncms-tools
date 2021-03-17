package de.byteaction.velocity.editor;

import de.byteaction.velocity.dtd.parser.DTD;
import de.byteaction.velocity.dtd.parser.DTDElement;
import de.byteaction.velocity.dtd.parser.DTDParser;
import de.byteaction.velocity.scanner.XMLElementGuesser;
import de.byteaction.velocity.ui.editor.xml.VelocityAutoIndentStrategy;
import de.byteaction.velocity.vaulttec.ui.IPreferencesConstants;
import de.byteaction.velocity.vaulttec.ui.VelocityColorProvider;
import de.byteaction.velocity.vaulttec.ui.VelocityPlugin;
import de.byteaction.velocity.vaulttec.ui.editor.VelocityConfiguration;
import de.byteaction.velocity.vaulttec.ui.editor.VelocityDocumentProvider;
import de.byteaction.velocity.vaulttec.ui.editor.VelocityEditorEnvironment;
import de.byteaction.velocity.vaulttec.ui.editor.VelocityReconcilingStrategy;
import de.byteaction.velocity.vaulttec.ui.editor.actions.FormatAction;
import de.byteaction.velocity.vaulttec.ui.editor.actions.GotoDefinitionAction;
import de.byteaction.velocity.vaulttec.ui.editor.actions.IVelocityActionConstants;
import de.byteaction.velocity.vaulttec.ui.editor.actions.IVelocityActionDefinitionIds;
import de.byteaction.velocity.vaulttec.ui.editor.actions.JTidyAction;
import de.byteaction.velocity.vaulttec.ui.editor.actions.ToggleCommentAction;
import de.byteaction.velocity.vaulttec.ui.editor.outline.VelocityOutlinePage;
import de.byteaction.velocity.vaulttec.ui.editor.parser.VelocityMacro;
import de.byteaction.velocity.vaulttec.ui.editor.text.VelocityTextGuesser;
import de.byteaction.velocity.vaulttec.ui.editor.text.VelocityTextGuesser.Type;
import de.byteaction.velocity.vaulttec.ui.model.ITreeNode;
import de.byteaction.velocity.vaulttec.ui.model.ModelTools;

import java.io.IOException;
import java.net.URL;
import java.util.*;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextInputListener;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorActionBarContributor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.EditorActionBarContributor;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.MarkerAnnotation;
import org.eclipse.ui.texteditor.TextOperationAction;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

/**
 * Main class for the Editor part.
 * 
 * @author <a href="mailto:doug_rand@percussion.com">Doug Rand</a>
 * @author <a href="mailto:as@byteaction.de">Akmal Sarhan </a>
 */
@SuppressWarnings("synthetic-access")
public class VelocityEditor extends TextEditor
      implements
         IPropertyChangeListener
{

   private static final String KEY = "vedit";

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.ui.texteditor.AbstractDecoratedTextEditor#createAnnotationAccess()
    */
   private static final String TEMPLATE_PROPOSALS = "template_proposals_action"; //$NON-NLS-1$

   public static Browser definitionBrowser;

   private static final String PREFIX = "VelocityEditor.";

   private static DTD dtd = null;

   public static final Map<String, String> ms_taginfomap = new HashMap<>(
         100);
   static
   {
      ms_taginfomap.put("A", getDocLocation() + "wdghtml40/special/a.html");
      ms_taginfomap
            .put("ABBR", getDocLocation() + "wdghtml40/phrase/abbr.html");
      ms_taginfomap.put("ACRONYM", getDocLocation()
            + "wdghtml40/phrase/acronym.html");
      ms_taginfomap.put("ADDRESS", getDocLocation()
            + "wdghtml40/block/address.html");
      ms_taginfomap.put("APPLET", getDocLocation()
            + "wdghtml40/special/applet.html");
      ms_taginfomap.put("AREA", getDocLocation()
            + "wdghtml40/special/area.html");
      ms_taginfomap.put("B", getDocLocation() + "wdghtml40/fontstyle/b.html");
      ms_taginfomap.put("BASE", getDocLocation() + "wdghtml40/head/base.html");
      ms_taginfomap.put("BASEFONT", getDocLocation()
            + "wdghtml40/special/basefont.html");
      ms_taginfomap.put("BDO", getDocLocation() + "wdghtml40/special/bdo.html");
      ms_taginfomap.put("BIG", getDocLocation()
            + "wdghtml40/fontstyle/big.html");
      ms_taginfomap.put("BLOCKQUOTE", getDocLocation()
            + "wdghtml40/block/blockquote.html");
      ms_taginfomap.put("BODY", getDocLocation() + "wdghtml40/html/body.html");
      ms_taginfomap.put("BR", getDocLocation() + "wdghtml40/special/br.html");
      ms_taginfomap.put("BUTTON", getDocLocation()
            + "wdghtml40/forms/button.html");
      ms_taginfomap.put("CAPTION", getDocLocation()
            + "wdghtml40/tables/caption.html");
      ms_taginfomap.put("CENTER", getDocLocation()
            + "wdghtml40/block/center.html");
      ms_taginfomap
            .put("CITE", getDocLocation() + "wdghtml40/phrase/cite.html");
      ms_taginfomap
            .put("CODE", getDocLocation() + "wdghtml40/phrase/code.html");
      ms_taginfomap.put("COL", getDocLocation() + "wdghtml40/tables/col.html");
      ms_taginfomap.put("COLGROUP", getDocLocation()
            + "wdghtml40/tables/colgroup.html");
      ms_taginfomap.put("DD", getDocLocation() + "wdghtml40/lists/dd.html");
      ms_taginfomap.put("DEL", getDocLocation() + "wdghtml40/phrase/del.html");
      ms_taginfomap.put("DFN", getDocLocation() + "wdghtml40/phrase/dfn.html");
      ms_taginfomap.put("DIR", getDocLocation() + "wdghtml40/lists/dir.html");
      ms_taginfomap.put("DIV", getDocLocation() + "wdghtml40/block/div.html");
      ms_taginfomap.put("DL", getDocLocation() + "wdghtml40/lists/dl.html");
      ms_taginfomap.put("DT", getDocLocation() + "wdghtml40/lists/dt.html");
      ms_taginfomap.put("EM", getDocLocation() + "wdghtml40/phrase/em.html");
      ms_taginfomap.put("FIELDSET", getDocLocation()
            + "wdghtml40/forms/fieldset.html");
      ms_taginfomap.put("FONT", getDocLocation()
            + "wdghtml40/special/font.html");
      ms_taginfomap.put("FORM", getDocLocation() + "wdghtml40/forms/form.html");
      ms_taginfomap.put("FRAME", getDocLocation()
            + "wdghtml40/frames/frame.html");
      ms_taginfomap.put("FRAMESET", getDocLocation()
            + "wdghtml40/frames/frameset.html");
      ms_taginfomap.put("H1", getDocLocation() + "wdghtml40/block/h1.html");
      ms_taginfomap.put("H2", getDocLocation() + "wdghtml40/block/h2.html");
      ms_taginfomap.put("H3", getDocLocation() + "wdghtml40/block/h3.html");
      ms_taginfomap.put("H4", getDocLocation() + "wdghtml40/block/h4.html");
      ms_taginfomap.put("H5", getDocLocation() + "wdghtml40/block/h5.html");
      ms_taginfomap.put("H6", getDocLocation() + "wdghtml40/block/h6.html");
      ms_taginfomap.put("HEAD", getDocLocation() + "wdghtml40/head/head.html");
      ms_taginfomap.put("HR", getDocLocation() + "wdghtml40/block/hr.html");
      ms_taginfomap.put("HTML", getDocLocation() + "wdghtml40/html/html.html");
      ms_taginfomap.put("I", getDocLocation() + "wdghtml40/fontstyle/i.html");
      ms_taginfomap.put("IFRAME", getDocLocation()
            + "wdghtml40/special/iframe.html");
      ms_taginfomap.put("IMG", getDocLocation() + "wdghtml40/special/img.html");
      ms_taginfomap.put("INPUT", getDocLocation()
            + "wdghtml40/forms/input.html");
      ms_taginfomap.put("INS", getDocLocation() + "wdghtml40/phrase/ins.html");
      ms_taginfomap.put("ISINDEX", getDocLocation()
            + "wdghtml40/block/isindex.html");
      ms_taginfomap.put("KBD", getDocLocation() + "wdghtml40/phrase/kbd.html");
      ms_taginfomap.put("LABEL", getDocLocation()
            + "wdghtml40/forms/label.html");
      ms_taginfomap.put("LEGEND", getDocLocation()
            + "wdghtml40/forms/legend.html");
      ms_taginfomap.put("LI", getDocLocation() + "wdghtml40/lists/li.html");
      ms_taginfomap.put("LINK", getDocLocation() + "wdghtml40/head/link.html");
      ms_taginfomap.put("MAP", getDocLocation() + "wdghtml40/special/map.html");
      ms_taginfomap.put("MENU", getDocLocation() + "wdghtml40/lists/menu.html");
      ms_taginfomap.put("META", getDocLocation() + "wdghtml40/head/meta.html");
      ms_taginfomap.put("NOFRAMES", getDocLocation()
            + "wdghtml40/frames/noframes.html");
      ms_taginfomap.put("NOSCRIPT", getDocLocation()
            + "wdghtml40/block/noscript.html");
      ms_taginfomap.put("OBJECT", getDocLocation()
            + "wdghtml40/special/object.html");
      ms_taginfomap.put("OL", getDocLocation() + "wdghtml40/lists/ol.html");
      ms_taginfomap.put("OPTGROUP", getDocLocation()
            + "wdghtml40/forms/optgroup.html");
      ms_taginfomap.put("OPTION", getDocLocation()
            + "wdghtml40/forms/option.html");
      ms_taginfomap.put("P", getDocLocation() + "wdghtml40/block/p.html");
      ms_taginfomap.put("PARAM", getDocLocation()
            + "wdghtml40/special/param.html");
      ms_taginfomap.put("PRE", getDocLocation() + "wdghtml40/block/pre.html");
      ms_taginfomap.put("Q", getDocLocation() + "wdghtml40/special/q.html");
      ms_taginfomap.put("S", getDocLocation() + "wdghtml40/fontstyle/s.html");
      ms_taginfomap
            .put("SAMP", getDocLocation() + "wdghtml40/phrase/samp.html");
      ms_taginfomap.put("SCRIPT", getDocLocation()
            + "wdghtml40/special/script.html");
      ms_taginfomap.put("SELECT", getDocLocation()
            + "wdghtml40/forms/select.html");
      ms_taginfomap.put("SMALL", getDocLocation()
            + "wdghtml40/fontstyle/small.html");
      ms_taginfomap.put("SPAN", getDocLocation()
            + "wdghtml40/special/span.html");
      ms_taginfomap.put("STRIKE", getDocLocation()
            + "wdghtml40/fontstyle/strike.html");
      ms_taginfomap.put("STRONG", getDocLocation()
            + "wdghtml40/phrase/strong.html");
      ms_taginfomap
            .put("STYLE", getDocLocation() + "wdghtml40/head/style.html");
      ms_taginfomap.put("SUB", getDocLocation() + "wdghtml40/special/sub.html");
      ms_taginfomap.put("SUP", getDocLocation() + "wdghtml40/special/sup.html");
      ms_taginfomap.put("TABLE", getDocLocation()
            + "wdghtml40/tables/table.html");
      ms_taginfomap.put("TBODY", getDocLocation()
            + "wdghtml40/tables/tbody.html");
      ms_taginfomap.put("TD", getDocLocation() + "wdghtml40/tables/td.html");
      ms_taginfomap.put("TEXTAREA", getDocLocation()
            + "wdghtml40/forms/textarea.html");
      ms_taginfomap.put("TFOOT", getDocLocation()
            + "wdghtml40/tables/tfoot.html");
      ms_taginfomap.put("TH", getDocLocation() + "wdghtml40/tables/th.html");
      ms_taginfomap.put("THEAD", getDocLocation()
            + "wdghtml40/tables/thead.html");
      ms_taginfomap
            .put("TITLE", getDocLocation() + "wdghtml40/head/title.html");
      ms_taginfomap.put("TR", getDocLocation() + "wdghtml40/tables/tr.html");
      ms_taginfomap.put("TT", getDocLocation() + "wdghtml40/fontstyle/tt.html");
      ms_taginfomap.put("U", getDocLocation() + "wdghtml40/fontstyle/u.html");
      ms_taginfomap.put("UL", getDocLocation() + "wdghtml40/lists/ul.html");
      ms_taginfomap.put("VAR", getDocLocation() + "wdghtml40/phrase/var.html");
      ms_taginfomap.put("BACKGROUND", getDocLocation()
            + "wdghtml40/css.html#background");
      ms_taginfomap.put("BACKGROUND-ATTACHMENT", getDocLocation()
            + "wdghtml40/css.html#background-attachment");
      ms_taginfomap.put("BACKGROUND-COLOR", getDocLocation()
            + "wdghtml40/css.html#background-color");
      ms_taginfomap.put("BACKGROUND-IMAGE", getDocLocation()
            + "wdghtml40/css.html#background-image");
      ms_taginfomap.put("BACKGROUND-POSITION", getDocLocation()
            + "wdghtml40/css.html#background-position");
      ms_taginfomap.put("BACKGROUND-REPEAT", getDocLocation()
            + "wdghtml40/css.html#background-repeat");
      ms_taginfomap.put("BORDER", getDocLocation()
            + "wdghtml40/css.html#border");
      ms_taginfomap.put("BORDER-BOTTOM", getDocLocation()
            + "wdghtml40/css.html#border-bottom");
      ms_taginfomap.put("BORDER-BOTTOM-WIDTH", getDocLocation()
            + "wdghtml40/css.html#border-bottom-width");
      ms_taginfomap.put("BORDER-COLOR", getDocLocation()
            + "wdghtml40/css.html#border-color");
      ms_taginfomap.put("BORDER-LEFT", getDocLocation()
            + "wdghtml40/css.html#border-left");
      ms_taginfomap.put("BORDER-LEFT-WIDTH", getDocLocation()
            + "wdghtml40/css.html#border-left-width");
      ms_taginfomap.put("BORDER-RIGHT", getDocLocation()
            + "wdghtml40/css.html#border-right");
      ms_taginfomap.put("BORDER-RIGHT-WIDTH", getDocLocation()
            + "wdghtml40/css.html#border-right-width");
      ms_taginfomap.put("BORDER-STYLE", getDocLocation()
            + "wdghtml40/css.html#border-style");
      ms_taginfomap.put("BORDER-TOP", getDocLocation()
            + "wdghtml40/css.html#border-top");
      ms_taginfomap.put("BORDER-TOP-WIDTH", getDocLocation()
            + "wdghtml40/css.html#border-top-width");
      ms_taginfomap.put("BORDER-WIDTH", getDocLocation()
            + "wdghtml40/css.html#border-width");
      ms_taginfomap.put("BOX", getDocLocation() + "wdghtml40/css.html#box");
      ms_taginfomap.put("CLASSIFICATION", getDocLocation()
            + "wdghtml40/css.html#classification");
      ms_taginfomap.put("CLEAR", getDocLocation() + "wdghtml40/css.html#clear");
      ms_taginfomap.put("COLOR", getDocLocation() + "wdghtml40/css.html#color");
      ms_taginfomap.put("COLOR-BACKGROUND", getDocLocation()
            + "wdghtml40/css.html#color-background");
      ms_taginfomap.put("COLOR-UNITS", getDocLocation()
            + "wdghtml40/css.html#color-units");
      ms_taginfomap.put("DISPLAY", getDocLocation()
            + "wdghtml40/css.html#display");
      ms_taginfomap.put("FLOAT", getDocLocation() + "wdghtml40/css.html#float");
      ms_taginfomap.put("FONT", getDocLocation() + "wdghtml40/css.html#font");
      ms_taginfomap.put("FONT-FAMILY", getDocLocation()
            + "wdghtml40/css.html#font-family");
      ms_taginfomap.put("FONT-PROPERTIES", getDocLocation()
            + "wdghtml40/css.html#font-properties");
      ms_taginfomap.put("FONT-SIZE", getDocLocation()
            + "wdghtml40/css.html#font-size");
      ms_taginfomap.put("FONT-STYLE", getDocLocation()
            + "wdghtml40/css.html#font-style");
      ms_taginfomap.put("FONT-VARIANT", getDocLocation()
            + "wdghtml40/css.html#font-variant");
      ms_taginfomap.put("FONT-WEIGHT", getDocLocation()
            + "wdghtml40/css.html#font-weight");
      ms_taginfomap.put("HEIGHT", getDocLocation()
            + "wdghtml40/css.html#height");
      ms_taginfomap.put("LENGTH", getDocLocation()
            + "wdghtml40/css.html#length");
      ms_taginfomap.put("LETTER-SPACING", getDocLocation()
            + "wdghtml40/css.html#letter-spacing");
      ms_taginfomap.put("LINE-HEIGHT", getDocLocation()
            + "wdghtml40/css.html#line-height");
      ms_taginfomap.put("LIST-STYLE", getDocLocation()
            + "wdghtml40/css.html#list-style");
      ms_taginfomap.put("LIST-STYLE-IMAGE", getDocLocation()
            + "wdghtml40/css.html#list-style-image");
      ms_taginfomap.put("LIST-STYLE-POSITION", getDocLocation()
            + "wdghtml40/css.html#list-style-position");
      ms_taginfomap.put("LIST-STYLE-TYPE", getDocLocation()
            + "wdghtml40/css.html#list-style-type");
      ms_taginfomap.put("MARGIN", getDocLocation()
            + "wdghtml40/css.html#margin");
      ms_taginfomap.put("MARGIN-BOTTOM", getDocLocation()
            + "wdghtml40/css.html#margin-bottom");
      ms_taginfomap.put("MARGIN-LEFT", getDocLocation()
            + "wdghtml40/css.html#margin-left");
      ms_taginfomap.put("MARGIN-RIGHT", getDocLocation()
            + "wdghtml40/css.html#margin-right");
      ms_taginfomap.put("MARGIN-TOP", getDocLocation()
            + "wdghtml40/css.html#margin-top");
      ms_taginfomap.put("PADDING", getDocLocation()
            + "wdghtml40/css.html#padding");
      ms_taginfomap.put("PADDING-BOTTOM", getDocLocation()
            + "wdghtml40/css.html#padding-bottom");
      ms_taginfomap.put("PADDING-LEFT", getDocLocation()
            + "wdghtml40/css.html#padding-left");
      ms_taginfomap.put("PADDING-RIGHT", getDocLocation()
            + "wdghtml40/css.html#padding-right");
      ms_taginfomap.put("PADDING-TOP", getDocLocation()
            + "wdghtml40/css.html#padding-top");
      ms_taginfomap.put("PERCENTAGE", getDocLocation()
            + "wdghtml40/css.html#percentage");
      ms_taginfomap.put("TEXT", getDocLocation() + "wdghtml40/css.html#text");
      ms_taginfomap.put("TEXT-ALIGN", getDocLocation()
            + "wdghtml40/css.html#text-align");
      ms_taginfomap.put("TEXT-DECORATION", getDocLocation()
            + "wdghtml40/css.html#text-decoration");
      ms_taginfomap.put("TEXT-INDENT", getDocLocation()
            + "wdghtml40/css.html#text-indent");
      ms_taginfomap.put("TEXT-TRANSFORM", getDocLocation()
            + "wdghtml40/css.html#text-transform");
      ms_taginfomap.put("UNITS", getDocLocation() + "wdghtml40/css.html#units");
      ms_taginfomap.put("URLS", getDocLocation() + "wdghtml40/css.html#urls");
      ms_taginfomap.put("VERTICAL-ALIGN", getDocLocation()
            + "wdghtml40/css.html#vertical-align");
      ms_taginfomap.put("WHITE-SPACE", getDocLocation()
            + "wdghtml40/css.html#white-space");
      ms_taginfomap.put("WIDTH", getDocLocation() + "wdghtml40/css.html#width");
      ms_taginfomap.put("WORD-SPACING", getDocLocation()
            + "wdghtml40/css.html#word-spacing");
   }

   private VelocityColorProvider cp;

   // private AnnotationModel fAnnotationModel;
   private ModelTools fModelTools;

   private VelocityReconcilingStrategy fReconcilingStrategy;

   private VelocityOutlinePage fOutlinePage;

   /**
    * {@link #setCompletionData(IResource)} for details. May be <code>null</code>
    * if not transmitted by invoker
    */
   public List<String[]> fVariableCompletions = null;

   /**
    * {@link #setCompletionData(IResource)} for details. May be <code>null</code>
    * if not transmitted by invoker
    */
   public List<Object[]> fMethodCompletions = null;

   /**
    * {@link #setCompletionData(IResource)} for details. May be <code>null</code>
    * if not transmitted by invoker
    */
   public List<String[]> fFieldCompletions = null;
   
   /**
    * {@link #setCompletionData(IResource)} for details. May be <code>null</code>
    * if not transmitted by invoker
    */
   public List<String> fMacroCompletions = null;

   /**
    * Last cursor position (line) handled in
    * <code>handleCursorPositionChanged()</code>
    */
   private int fLastCursorLine;

   VelocityConfiguration m_vc = null;

   private MouseClickListener fMouseListener;

   private ProjectionSupport projectionSupport;

   /**
    * The multipage editor parent, never <code>null</code> after construction
    * is complete.
    */
   private MultiPageEditor fMultiPageEditor;

   /**
    * Set to <code>true</code> if the multipage editor shouldn't show the
    * preview
    */
   private boolean fHidePreviewPage;

   public VelocityEditor() {
      fModelTools = new ModelTools(this);
      fReconcilingStrategy = new VelocityReconcilingStrategy(this);
      cp = VelocityEditorEnvironment.getColorProvider();
      VelocityPlugin.getDefault().getPreferenceStore()
            .addPropertyChangeListener(this);
   }

   private void openBrowser(final Display display, final String url)
   {
      Shell shell = new Shell(display, SWT.SHELL_TRIM | SWT.APPLICATION_MODAL);
      shell.setText("Definition Browser");
      shell.setLayout(new FillLayout());
      Browser browser = new Browser(shell, SWT.NONE);
      browser.setUrl(url);
      shell.open();
   }

   private static String getDocLocation()
   {
      String loc = "";
      URL url = VelocityPlugin.getDefault().getBundle().getEntry("/");
      try
      {
         loc = Platform.resolve(url).toString();
      }
      catch (IOException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      return loc;
   }

   /**
    * DOCUMENT ME!
    * 
    * @return DOCUMENT ME!
    */
   public String getDefaultDTD()
   {
      return getPreferenceStore().getString(IPreferencesConstants.DTD);
   }

   /**
    * DOCUMENT ME!
    * 
    * @param name DOCUMENT ME!
    * @return DOCUMENT ME!
    */
   public static DTDElement getHTMLElement(String name)
   {
      // String temp = getDefaultDTD();
      DTDElement element = null;
      String defaultdtd = "2";
      if ((defaultdtd != null) && (Integer.valueOf(defaultdtd).intValue() > 0))
      {
      }
      else
      {
         defaultdtd = "1";
      }
      if (dtd == null)
      {
         try
         {
            final URL url = VelocityEditor.class.getResource("dtd/xhtml1-strict.dtd");
            final DTDParser parser = new DTDParser(url, false);
            dtd = parser.parse(false);
         }
         catch (IOException e)
         {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
      }
      element = (DTDElement) dtd.elements.get(name);
      return element;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.ui.editors.text.TextEditor#initializeEditor()
    */
   protected void initializeEditor()
   {
      super.initializeEditor();
      VelocityEditorEnvironment.connect();
      setDocumentProvider(new VelocityDocumentProvider());
      VelocityConfiguration vc = new VelocityConfiguration(this);
      setSourceViewerConfiguration(vc);
   }

   // /*
   // * (non-Javadoc)
   // *
   // * @see
   // org.eclipse.ui.editors.text.TextEditor#initializeKeyBindingScopes()
   // */
   // protected void initializeKeyBindingScopes()
   // {
   // setKeyBindingScopes(new String[] {
   // "de.byteaction.velocity.vaulttec.ui.velocityEditorScope" });
   // }
   private void enableBrowserLikeLinks()
   {
      if (fMouseListener == null)
      {
         fMouseListener = new MouseClickListener();
         fMouseListener.install();
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.ui.texteditor.AbstractTextEditor#createActions()
    */
   protected void createActions()
   {
      super.createActions();
      // Add goto definition action
      IAction action = new GotoDefinitionAction(VelocityPlugin.getDefault()
            .getResourceBundle(), PREFIX + "GotoDefinition.", this);
      action
            .setActionDefinitionId(IVelocityActionDefinitionIds.GOTO_DEFINITION);
      setAction(IVelocityActionConstants.GOTO_DEFINITION, action);
      action = new TextOperationAction(
            VelocityPlugin.getDefault().getResourceBundle(),
            "ContentAssistProposal.", this, ISourceViewer.CONTENTASSIST_PROPOSALS); //$NON-NLS-1$
      action
            .setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
      // setAction("ContentAssistProposal", action); //$NON-NLS-1$
      setAction(IVelocityActionConstants.CONTENT_ASSIST, action);
      // jtidy
      action = new JTidyAction(VelocityPlugin.getDefault().getResourceBundle(),
            PREFIX + "JTidy.", this);
      action.setActionDefinitionId(IVelocityActionDefinitionIds.JTIDY);
      setAction(IVelocityActionConstants.JTIDY, action);
      // TOGGLE_COMMENT
      action = new ToggleCommentAction(VelocityPlugin.getDefault()
            .getResourceBundle(), PREFIX + "ToggleComment.", this);
      action.setActionDefinitionId(IVelocityActionDefinitionIds.TOGGLE_COMMENT);
      setAction(IVelocityActionConstants.TOGGLE_COMMENT, action);
      // FormatEditor
      action = new FormatAction(
            VelocityPlugin.getDefault().getResourceBundle(), PREFIX
                  + "FormatEditor.", this);
      action.setActionDefinitionId(IVelocityActionDefinitionIds.FORMAT);
      setAction(IVelocityActionConstants.FORMAT, action);
      // template proposal
      action = new TextOperationAction(TemplateMessages.getResourceBundle(),
            "Editor." + TEMPLATE_PROPOSALS + ".", //$NON-NLS-1$ //$NON-NLS-2$
            this, ISourceViewer.CONTENTASSIST_PROPOSALS);
      action
            .setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
      setAction(TEMPLATE_PROPOSALS, action);
      markAsStateDependentAction(TEMPLATE_PROPOSALS, true);
      enableBrowserLikeLinks();
      configureToggleCommentAction();
   }

   /**
    * Get the outline page if requested.
    * 
    * @see org.eclipse.core.runtime.IAdaptable
    */
   public Object getAdapter(Class aClass)
   {
      // Object adapter;
      // if (aClass.equals(IContentOutlinePage.class))
      // {
      // if ((fOutlinePage == null) || fOutlinePage.isDisposed())
      // {
      // fOutlinePage = new VelocityOutlinePage(this);
      // if (getEditorInput() != null)
      // {
      // fOutlinePage.setInput(getEditorInput());
      // }
      // }
      // adapter = fOutlinePage;
      // } else
      // {
      // adapter = super.getAdapter(aClass);
      // }
      // return adapter;
      // folding
      if (IContentOutlinePage.class.equals(aClass))
      {
         if (fOutlinePage == null || fOutlinePage.isDisposed())
         {
            fOutlinePage = new VelocityOutlinePage(this);
            if (getEditorInput() != null)
            {
               fOutlinePage.setInput(getEditorInput());
            }
         }
         return fOutlinePage;
      }
      if (projectionSupport != null)
      {
         Object adapter = projectionSupport.getAdapter(getSourceViewer(),
               aClass);
         if (adapter != null)
            return adapter;
      }
      return super.getAdapter(aClass);
   }

   /**
    * Disconnect from editor environment and dispose outline page.
    * 
    * @see org.eclipse.ui.IWorkbenchPart#dispose()
    */
   public void dispose()
   {
      if ((fOutlinePage != null) && !fOutlinePage.isDisposed())
      {
         fOutlinePage.dispose();
         fOutlinePage = null;
      }
      VelocityEditorEnvironment.disconnect();
      super.dispose();
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.ui.texteditor.AbstractTextEditor#editorContextMenuAboutToShow(org.eclipse.jface.action.IMenuManager)
    */
   protected void editorContextMenuAboutToShow(IMenuManager aMenu)
   {
      super.editorContextMenuAboutToShow(aMenu);
      addAction(aMenu, IWorkbenchActionConstants.MB_ADDITIONS,
            IVelocityActionConstants.CONTENT_ASSIST);
      addAction(aMenu, IWorkbenchActionConstants.MB_ADDITIONS,
            IVelocityActionConstants.TOGGLE_COMMENT);
      addAction(aMenu, IWorkbenchActionConstants.MB_ADDITIONS,
            IVelocityActionConstants.FORMAT);
      addAction(aMenu, IWorkbenchActionConstants.MB_ADDITIONS,
            IVelocityActionConstants.JTIDY);
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.ui.texteditor.AbstractTextEditor#handleCursorPositionChanged()
    */
   protected void handleCursorPositionChanged()
   {
      super.handleCursorPositionChanged();
      int line = getCursorLine();
      if ((line > 0) && (line != fLastCursorLine))
      {
         fLastCursorLine = line;
         if ((fOutlinePage != null) && !fOutlinePage.isDisposed())
         {
            fOutlinePage.selectNode(line, false);
         }
      }
   }

   /**
    * DOCUMENT ME!
    * 
    * @return DOCUMENT ME!
    */
   public IDocument getDocument()
   {
      ISourceViewer viewer = getSourceViewer();
      if (viewer != null)
      {
         return viewer.getDocument();
      }
      return null;
   }

   /**
    * DOCUMENT ME!
    * 
    * @param anOffset DOCUMENT ME!
    * @return DOCUMENT ME!
    */
   public int getLine(int anOffset)
   {
      int line;
      try
      {
         line = getDocument().getLineOfOffset(anOffset) + 1;
      }
      catch (BadLocationException e)
      {
         line = -1;
      }
      return line;
   }

   /**
    * DOCUMENT ME!
    * 
    * @return DOCUMENT ME!
    */
   public int getCursorLine()
   {
      int line = -1;
      ISourceViewer sourceViewer = getSourceViewer();
      if (sourceViewer != null)
      {
         StyledText styledText = sourceViewer.getTextWidget();
         int caret = widgetOffset2ModelOffset(sourceViewer, styledText
               .getCaretOffset());
         IDocument document = sourceViewer.getDocument();
         if (document != null)
         {
            try
            {
               line = document.getLineOfOffset(caret) + 1;
            }
            catch (BadLocationException e)
            {
               VelocityPlugin.log(e);
            }
         }
      }
      return line;
   }

   /**
    * DOCUMENT ME!
    * 
    * @param aNode DOCUMENT ME!
    * @param aMoveCursor DOCUMENT ME!
    */
   public void highlightNode(ITreeNode aNode, boolean aMoveCursor)
   {
      resetHighlightRange();
      IDocument doc = getDocument();
      try
      {
         int offset = doc.getLineOffset(aNode.getStartLine() - 1);
         IRegion endLine = doc.getLineInformation(aNode.getEndLine() - 1);
         int length = (endLine.getOffset() + endLine.getLength()) - offset;
         setHighlightRange(offset, length + 1, aMoveCursor);
      }
      catch (BadLocationException e)
      {
         resetHighlightRange();
      }
   }

   /**
    * DOCUMENT ME!
    * 
    * @param offset DOCUMENT ME!
    * @param aMoveCursor DOCUMENT ME!
    */
   public void highlightOpenTag(int offset, boolean aMoveCursor)
   {
      resetHighlightRange();
      IDocument doc = getDocument();
      int linenr = -1;
      try
      {
         markInNavigationHistory();
         linenr = doc.getLineOfOffset(offset);
         IRegion endLine = doc.getLineInformation(linenr);
         int length = (endLine.getOffset() + endLine.getLength()) - offset;
         setHighlightRange(offset, length + 1, aMoveCursor);
         markInNavigationHistory();
      }
      catch (BadLocationException e)
      {
         resetHighlightRange();
      }
   }

   /**
    * DOCUMENT ME!
    * 
    * @param aNode DOCUMENT ME!
    */
   public void revealNode(ITreeNode aNode)
   {
      ISourceViewer viewer = getSourceViewer();
      if (viewer != null)
      {
         IDocument doc = getDocument();
         try
         {
            int offset = doc.getLineOffset(aNode.getStartLine() - 1);
            IRegion endLine = doc.getLineInformation(aNode.getEndLine() - 1);
            int length = (endLine.getOffset() + endLine.getLength()) - offset;
            // Reveal segment's text area in document
            StyledText widget = getSourceViewer().getTextWidget();
            widget.setRedraw(false);
            viewer.revealRange(offset, length);
            widget.setRedraw(true);
         }
         catch (BadLocationException e)
         {
            resetHighlightRange();
         }
      }
   }

   /**
    * DOCUMENT ME!
    * 
    * @param aLine DOCUMENT ME!
    * @return DOCUMENT ME!
    */
   public ITreeNode getNodeByLine(int aLine)
   {
      return fModelTools.getNodeByLine(aLine);
   }

   /**
    * DOCUMENT ME!
    * 
    * @param aRegion DOCUMENT ME!
    * @return DOCUMENT ME!
    */
   public String getDefinitionLine(IRegion aRegion)
   {
      int tagOffset = -1;
      String guessed = null;
      if (aRegion != null)
      {
         XMLElementGuesser xmlguess = new XMLElementGuesser(getDocument(),
               aRegion.getOffset(), true);
         guessed = xmlguess.getText();
         Type xmltype = xmlguess.getType();
         tagOffset = xmlguess.getTagOffset();
         if ((guessed != null)
               && ms_taginfomap.containsKey(guessed.toUpperCase()))
         {
            if (xmltype == Type.TAG_DIRECTIVE)
            {
               guessed = "Ctrl+Alt+Mouseclick to show definition of " + guessed;
            }
            else if (xmltype == Type.TAG_CLOSE)
            {
               int i = VelocityAutoIndentStrategy.findMatchingOpenTagBefore(
                     tagOffset, getDocument());
               if (i > -1)
               {
                  int lnr = -1;
                  try
                  {
                     lnr = getDocument().getLineOfOffset(i);
                  }
                  catch (BadLocationException e)
                  {
                     // TODO Auto-generated catch block
                     e.printStackTrace();
                  }
                  guessed = "Ctrl+Alt+Mouseclick to go to matching tag of "
                        + guessed
                        + ((lnr > -1) ? ("  -> " + (lnr + 1) + "") : "");
               }
               else
               {
                  guessed = "this tag is incorrectly closed";
               }
            }
            else
            {
               guessed = null;
            }
         }
         else
         {
            guessed = null;
            VelocityTextGuesser guess = new VelocityTextGuesser(getDocument(),
                  aRegion.getOffset(), true);
            // Check if guessed text references an externally defined macro
            if (guess.getType() == Type.TYPE_DIRECTIVE)
            {
               VelocityMacro macro = VelocityEditorEnvironment.getParser()
                     .getLibraryMacro(guess.getText());
               if (macro != null)
               {
                  String template = ((IFileEditorInput) getEditorInput())
                        .getFile().getName();
                  if (!macro.getTemplate().equals(template))
                  {
                     StringBuffer buf = new StringBuffer();
                     buf.append("#macro (");
                     buf.append(macro.getName());
                     buf.append(") - ");
                     buf.append(macro.getTemplate());
                     return buf.toString();
                  }
               }
            }
            else if (guess.getType() == Type.TYPE_END)
            {
               int i = VelocityAutoIndentStrategy.findStartVeloBefore(guess
                     .getTagOffset(), getDocument());
               int lnr = -1;
               if (i > -1)
               {
                  try
                  {
                     lnr = getDocument().getLineOfOffset(i);
                  }
                  catch (BadLocationException e)
                  {
                     // TODO Auto-generated catch block
                     e.printStackTrace();
                  }
               }
               guessed = "Ctrl+Alt+Mouseclick to go to directive start "
                     + ((lnr > -1) ? ("  -> " + (lnr + 1) + "") : "");
            }
            // Look through model tree for guessed text
            ITreeNode node = fModelTools.getNodeByGuess(guess);
            if (node != null)
            {
               IDocument doc = getDocument();
               try
               {
                  aRegion = doc.getLineInformation(node.getStartLine() - 1);
                  StringBuffer buf = new StringBuffer();
                  buf.append(node.getStartLine());
                  buf.append(": ");
                  buf.append(doc.get(aRegion.getOffset(), aRegion.getLength())
                        .trim());
                  return buf.toString();
               }
               catch (BadLocationException e)
               {
               }
            }
         }
      }
      return guessed;
   }

   /**
    * DOCUMENT ME!
    * 
    * @param aRegion DOCUMENT ME!
    */
   public void gotoDefinition(IRegion aRegion)
   {
      int tagOffset = -1;
      if (aRegion != null)
      {
         XMLElementGuesser xmlguess = new XMLElementGuesser(getDocument(),
               aRegion.getOffset(), true);
         String guessed = xmlguess.getText();
         Type xmltype = xmlguess.getType();
         tagOffset = xmlguess.getTagOffset();
         if ((guessed != null)
               && ms_taginfomap.containsKey(guessed.toUpperCase()))
         {
            if (xmltype == Type.TAG_DIRECTIVE)
            {
               try
               {
                  guessed = (String) ms_taginfomap.get(guessed.toUpperCase());
                  if (VelocityPlugin.isBrowserSupported)
                  {
                     openBrowser(getSite().getShell().getDisplay(), guessed);
                  }
                  return;
               }
               catch (Exception e)
               {
                  // TODO Auto-generated catch block
                  e.printStackTrace();
               }
            }
            else if (xmltype == Type.TAG_CLOSE)
            {
               int i = VelocityAutoIndentStrategy.findMatchingOpenTagBefore(
                     tagOffset, getDocument());
               if (i > -1)
               {
                  highlightOpenTag(i, true);
               }
            }
            else
            {
               try
               {
                  guessed = (String) ms_taginfomap.get(guessed.toUpperCase());
                  if (VelocityPlugin.isBrowserSupported)
                  {
                     openBrowser(getSite().getShell().getDisplay(), guessed);
                  }
                  return;
               }
               catch (Exception e)
               {
                  // TODO Auto-generated catch block
                  e.printStackTrace();
               }
            }
         }
         else
         {
            VelocityTextGuesser guess = new VelocityTextGuesser(getDocument(),
                  aRegion.getOffset(), true);
            // Check if guessed text references an externally defined macro
            if ((guess.getType() == Type.TYPE_DIRECTIVE)
                  || (guess.getType() == Type.TYPE_VARIABLE))
            {

               VelocityMacro macro = VelocityEditorEnvironment.getParser()
                     .getLibraryMacro(guess.getText());
               // TODO we have to be able to open the file if it belongs
               // to a project
               if (macro != null)
               {
                  String template = ((IFileEditorInput) getEditorInput())
                        .getFile().getName();
                  if (!macro.getTemplate().equals(template))
                  {
                     // try
                     // {
                     // Template templatee =
                     // VelocityEditorEnvironment.getParser().getTemplate(macro.getTemplate());
                     // InputStream resourceStream =
                     // templatee.getResourceLoader().getResourceStream(macro.getTemplate())
                     // ;
                     //                                
                     // }
                     // catch (Exception e)
                     // {
                     // // TODO Auto-generated catch block
                     // e.printStackTrace();
                     // }

                     return;
                  }
               }
               // Look through model tree for guessed text
               ITreeNode node = fModelTools.getNodeByGuess(guess);
               if (node != null)
               {
                  markInNavigationHistory();
                  highlightNode(node, true);
                  markInNavigationHistory();
               }
            }
            else if (guess.getType() == Type.TYPE_END)
            {
               int i = VelocityAutoIndentStrategy.findStartVeloBefore(guess
                     .getTagOffset(), getDocument());
               if (i > -1)
               {
                  highlightOpenTag(i, true);
               }
            }
         }
         getSourceViewer().invalidateTextPresentation();
      }
   }

   /**
    * Returns true if specified line belongs to a <code>#foreach</code> block.
    */
   public boolean isLineWithinLoop(int aLine)
   {
      return fModelTools.isLineWithinLoop(aLine);
   }

   /**
    * DOCUMENT ME!
    * 
    * @param aLine DOCUMENT ME!
    * @return DOCUMENT ME!
    */
   public List getVariables(int aLine)
   {
      return fModelTools.getVariables(aLine);
   }

   /**
    * DOCUMENT ME!
    * 
    * @return DOCUMENT ME!
    */
   public List getMacros()
   {
      return fModelTools.getMacros();
   }

   /**
    * DOCUMENT ME!
    * 
    * @return DOCUMENT ME!
    */
   public VelocityReconcilingStrategy getReconcilingStrategy()
   {
      return fReconcilingStrategy;
   }

   /**
    * DOCUMENT ME!
    * 
    * @return DOCUMENT ME!
    */
   public Object[] getRootElements()
   {
      return fReconcilingStrategy.getRootElements();
   }

   /**
    * DOCUMENT ME!
    * 
    * @return DOCUMENT ME!
    */
   public ITreeNode getRootNode()
   {
      return fReconcilingStrategy.getRootNode();
   }

   /**
    * DOCUMENT ME!
    * 
    * @return DOCUMENT ME!
    */
   public ITreeNode getLastRootNode()
   {
      return fReconcilingStrategy.getLastRootNode();
   }

   /**
    * DOCUMENT ME!
    */
   public void updateOutlinePage()
   {
      if (fOutlinePage != null)
      {
         fOutlinePage.update();
      }
   }

   /**
    * DOCUMENT ME!
    * 
    * @param aLine DOCUMENT ME!
    */
   public void moveCursor(int aLine)
   {
      ISourceViewer sourceViewer = getSourceViewer();
      try
      {
         int offset = getDocument().getLineOffset(aLine - 1);
         sourceViewer.setSelectedRange(offset, 0);
         sourceViewer.revealRange(offset, 0);
      }
      catch (BadLocationException e)
      {
      }
   }

   /**
    * Determines if the specified character may be part of a Velocity reference.
    * A character may be part of a Velocity directive if and only if it is one
    * of the following:
    * <ul>
    * <li>a letter (a..z, A..Z)
    * <li>a digit (0..9)
    * <li>a hyphen ("-")
    * <li>a connecting punctuation character ("_")
    * </ul>
    * 
    * @param aChar the character to be tested.
    * @return true if the character may be part of a Velocity reference; false
    *         otherwise.
    * @see java.lang.Character#isLetterOrDigit(char)
    */
   public static boolean isReferencePart(char aChar)
   {
      return Character.isLetterOrDigit(aChar) || (aChar == '-')
            || (aChar == '_');
   }

   /**
    * Returns the desktop's StatusLineManager.
    */
   protected IStatusLineManager getStatusLineManager()
   {
      IStatusLineManager manager;
      IEditorActionBarContributor contributor = getEditorSite()
            .getActionBarContributor();
      if ((contributor != null)
            && contributor instanceof EditorActionBarContributor)
      {
         manager = ((EditorActionBarContributor) contributor).getActionBars()
               .getStatusLineManager();
      }
      else
      {
         manager = null;
      }
      return manager;
   }

   /**
    * Displays an error message in editor's status line.
    */
   public void displayErrorMessage(String aMessage)
   {
      IStatusLineManager manager = getStatusLineManager();
      if (manager != null)
      {
         manager.setErrorMessage(aMessage);
      }
   }

   /**
    * DOCUMENT ME!
    * 
    * @param aMessage DOCUMENT ME!
    * @param aLine DOCUMENT ME!
    */
   public void addProblemMarker(String aMessage, int aLine, int severity)
   {
      IFile file = ((IFileEditorInput) getEditorInput()).getFile();
      try
      {
         IMarker marker = file.createMarker(IMarker.PROBLEM);
         marker.setAttribute(IMarker.SEVERITY, severity);
         marker.setAttribute(IMarker.MESSAGE, aMessage);
         marker.setAttribute(IMarker.LINE_NUMBER, aLine);
         Position pos = new Position(getDocument().getLineOffset(aLine - 1));
         getSourceViewer().getAnnotationModel().addAnnotation(
               new MarkerAnnotation(marker), pos);
      }
      catch (Exception e)
      {
         VelocityPlugin.log(e);
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.ui.editors.text.TextEditor#handlePreferenceStoreChanged(org.eclipse.jface.util.PropertyChangeEvent)
    */
   protected void handlePreferenceStoreChanged(PropertyChangeEvent event)
   {
      cp.handlePreferenceStoreChanged(event);
      super.handlePreferenceStoreChanged(event);
   }

   // /*
   // * @see AbstractTextEditor#doSetInput(IEditorInput)
   // */
   // protected void doSetInput(IEditorInput input) throws CoreException
   // {
   // super.doSetInput(input);
   // // configureToggleCommentAction();
   // }
   private void configureToggleCommentAction()
   {
      IAction action = getAction(IVelocityActionConstants.TOGGLE_COMMENT); //$NON-NLS-1$
      if (action instanceof ToggleCommentAction)
      {
         ISourceViewer sourceViewer = getSourceViewer();
         SourceViewerConfiguration configuration = getSourceViewerConfiguration();
         ((ToggleCommentAction) action).configure(sourceViewer, configuration);
      }
   }

   /**
    * @return
    */
   public VelocityConfiguration getVelocityConfiguration()
   {
      return m_vc;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
    */
   public void propertyChange(PropertyChangeEvent event)
   {
      cp.handlePreferenceStoreChanged(event);
      ISourceViewer viewer = getSourceViewer();
      if (viewer != null)
      {
         viewer.invalidateTextPresentation();
      }
   }

   /**
    * DOCUMENT ME!
    * 
    * @param token DOCUMENT ME!
    * @return DOCUMENT ME!
    */
   public static int findLocalizedModifier(String token)
   {
      if (token == null)
      {
         return 0;
      }
      if (token.equalsIgnoreCase(Action.findModifierString(SWT.CTRL)))
      {
         return SWT.CTRL;
      }
      if (token.equalsIgnoreCase(Action.findModifierString(SWT.SHIFT)))
      {
         return SWT.SHIFT;
      }
      if (token.equalsIgnoreCase(Action.findModifierString(SWT.ALT)))
      {
         return SWT.ALT;
      }
      if (token.equalsIgnoreCase(Action.findModifierString(SWT.COMMAND)))
      {
         return SWT.COMMAND;
      }
      return 0;
   }

   class MouseClickListener
         implements
            KeyListener,
            MouseListener,
            MouseMoveListener,
            FocusListener,
            PaintListener,
            IPropertyChangeListener,
            IDocumentListener,
            ITextInputListener
   {

      /** The session is active. */
      private boolean fActive;

      /** The currently active style range. */
      private IRegion fActiveRegion;

      /** The currently active style range as position. */
      private Position fRememberedPosition;

      /** The hand cursor. */
      private Cursor fCursor;

      /** The link color. */
      private Color fColor;

      /** The key modifier mask. */
      private int fKeyModifierMask;

      public void deactivate()
      {
         deactivate(false);
      }

      public void deactivate(boolean redrawAll)
      {
         if (!fActive)
         {
            return;
         }
         repairRepresentation(redrawAll);
         fActive = false;
      }

      public void install()
      {
         ISourceViewer sourceViewer = getSourceViewer();
         if (sourceViewer == null)
         {
            return;
         }
         StyledText text = sourceViewer.getTextWidget();
         if ((text == null) || text.isDisposed())
         {
            return;
         }
         updateColor(sourceViewer);
         sourceViewer.addTextInputListener(this);
         IDocument document = sourceViewer.getDocument();
         if (document != null)
         {
            document.addDocumentListener(this);
         }
         text.addKeyListener(this);
         text.addMouseListener(this);
         text.addMouseMoveListener(this);
         text.addFocusListener(this);
         text.addPaintListener(this);
         updateKeyModifierMask();
         IPreferenceStore preferenceStore = getPreferenceStore();
         preferenceStore.addPropertyChangeListener(this);
      }

      private void updateKeyModifierMask()
      {
         fKeyModifierMask = computeStateMask("Ctrl+Alt");
         if (fKeyModifierMask == -1)
         {
         }
      }

      private int computeStateMask(String modifiers)
      {
         if (modifiers == null)
         {
            return -1;
         }
         if (modifiers.length() == 0)
         {
            return SWT.NONE;
         }
         int stateMask = 0;
         StringTokenizer modifierTokenizer = new StringTokenizer(modifiers,
               ",;.:+-* "); //$NON-NLS-1$
         while (modifierTokenizer.hasMoreTokens())
         {
            int modifier = findLocalizedModifier(modifierTokenizer.nextToken());
            if ((modifier == 0) || ((stateMask & modifier) == modifier))
            {
               return -1;
            }
            stateMask = stateMask | modifier;
         }
         return stateMask;
      }

      public void uninstall()
      {
         if (fColor != null)
         {
            fColor.dispose();
            fColor = null;
         }
         if (fCursor != null)
         {
            fCursor.dispose();
            fCursor = null;
         }
         ISourceViewer sourceViewer = getSourceViewer();
         if (sourceViewer == null)
         {
            return;
         }
         sourceViewer.removeTextInputListener(this);
         IDocument document = sourceViewer.getDocument();
         if (document != null)
         {
            document.removeDocumentListener(this);
         }
         IPreferenceStore preferenceStore = getPreferenceStore();
         if (preferenceStore != null)
         {
            preferenceStore.removePropertyChangeListener(this);
         }
         StyledText text = sourceViewer.getTextWidget();
         if ((text == null) || text.isDisposed())
         {
            return;
         }
         text.removeKeyListener(this);
         text.removeMouseListener(this);
         text.removeMouseMoveListener(this);
         text.removeFocusListener(this);
         text.removePaintListener(this);
      }

      private void updateColor(ISourceViewer viewer)
      {
         if (fColor != null)
         {
            fColor.dispose();
         }
         StyledText text = viewer.getTextWidget();
         if ((text == null) || text.isDisposed())
         {
            return;
         }
         fColor = new Color(Display.getCurrent(), 0, 0, 255);
      }

      private void repairRepresentation()
      {
         repairRepresentation(false);
      }

      private void repairRepresentation(boolean redrawAll)
      {
         if (fActiveRegion == null)
         {
            return;
         }
         ISourceViewer viewer = getSourceViewer();
         if (viewer != null)
         {
            resetCursor(viewer);
            int offset = fActiveRegion.getOffset();
            int length = fActiveRegion.getLength();
            // remove style
            if (!redrawAll && viewer instanceof ITextViewerExtension2)
            {
               ((ITextViewerExtension2) viewer).invalidateTextPresentation(
                     offset, length);
            }
            else
            {
               viewer.invalidateTextPresentation();
            }
            // remove underline
            if (viewer instanceof ITextViewerExtension5)
            {
               ITextViewerExtension5 extension = (ITextViewerExtension5) viewer;
               offset = extension.modelOffset2WidgetOffset(offset);
            }
            else
            {
               offset -= viewer.getVisibleRegion().getOffset();
            }
            StyledText text = viewer.getTextWidget();
            try
            {
               text.redrawRange(offset, length, true);
            }
            catch (IllegalArgumentException x)
            {
            }
         }
         fActiveRegion = null;
      }

      private IRegion selectWord(IDocument document, int anchor)
      {
         try
         {
            int offset = anchor;
            char c;
            while (offset >= 0)
            {
               c = document.getChar(offset);
               if (!Character.isJavaIdentifierPart(c)) // || (c == '$'))
               {
                  break;
               }
               --offset;
            }
            int start = offset;
            offset = anchor;
            int length = document.getLength();
            while (offset < length)
            {
               c = document.getChar(offset);
               if (!Character.isJavaIdentifierPart(c)) // || (c == '$'))
               {
                  break;
               }
               ++offset;
            }
            int end = offset;
            if (start == end)
            {
               return new Region(start, 0);
            }
            else
            {
               return new Region(start + 1, end - start - 1);
            }
         }
         catch (BadLocationException x)
         {
            return null;
         }
      }

      IRegion getCurrentTextRegion(ISourceViewer viewer)
      {
         int offset = getCurrentTextOffset(viewer);
         if (offset == -1)
         {
            return null;
         }
         try
         {
            return selectWord(viewer.getDocument(), offset);
         }
         catch (Exception e)
         {
            return null;
         }
      }

      private int getCurrentTextOffset(ISourceViewer viewer)
      {
         try
         {
            StyledText text = viewer.getTextWidget();
            if ((text == null) || text.isDisposed())
            {
               return -1;
            }
            Display display = text.getDisplay();
            Point absolutePosition = display.getCursorLocation();
            Point relativePosition = text.toControl(absolutePosition);
            int widgetOffset = text.getOffsetAtLocation(relativePosition);
            if (viewer instanceof ITextViewerExtension5)
            {
               ITextViewerExtension5 extension = (ITextViewerExtension5) viewer;
               return extension.widgetOffset2ModelOffset(widgetOffset);
            }
            else
            {
               return widgetOffset + viewer.getVisibleRegion().getOffset();
            }
         }
         catch (IllegalArgumentException e)
         {
            return -1;
         }
      }

      private void highlightRegion(ISourceViewer viewer, IRegion region)
      {
         if (region.equals(fActiveRegion))
         {
            return;
         }
         repairRepresentation();
         StyledText text = viewer.getTextWidget();
         if ((text == null) || text.isDisposed())
         {
            return;
         }
         // highlight region
         int offset = 0;
         int length = 0;
         if (viewer instanceof ITextViewerExtension5)
         {
            ITextViewerExtension5 extension = (ITextViewerExtension5) viewer;
            IRegion widgetRange = extension.modelRange2WidgetRange(region);
            if (widgetRange == null)
            {
               return;
            }
            offset = widgetRange.getOffset();
            length = widgetRange.getLength();
         }
         else
         {
            offset = region.getOffset() - viewer.getVisibleRegion().getOffset();
            length = region.getLength();
         }
         StyleRange oldStyleRange = text.getStyleRangeAtOffset(offset);
         Color foregroundColor = fColor;
         Color backgroundColor = (oldStyleRange == null)
               ? text.getBackground()
               : oldStyleRange.background;
         StyleRange styleRange = new StyleRange(offset, length,
               foregroundColor, backgroundColor);
         text.setStyleRange(styleRange);
         // underline
         text.redrawRange(offset, length, true);
         fActiveRegion = region;
      }

      private void activateCursor(ISourceViewer viewer)
      {
         StyledText text = viewer.getTextWidget();
         if ((text == null) || text.isDisposed())
         {
            return;
         }
         Display display = text.getDisplay();
         if (fCursor == null)
         {
            fCursor = new Cursor(display, SWT.CURSOR_HAND);
         }
         text.setCursor(fCursor);
      }

      private void resetCursor(ISourceViewer viewer)
      {
         StyledText text = viewer.getTextWidget();
         if ((text != null) && !text.isDisposed())
         {
            text.setCursor(null);
         }
         if (fCursor != null)
         {
            fCursor.dispose();
            fCursor = null;
         }
      }

      /*
       * @see org.eclipse.swt.events.KeyListener#keyPressed(org.eclipse.swt.events.KeyEvent)
       */
      public void keyPressed(KeyEvent event)
      {
         if (fActive)
         {
            deactivate();
            return;
         }
         if (event.keyCode != fKeyModifierMask)
         {
            deactivate();
            return;
         }
         fActive = true;
         // removed for #25871
         //
         // ISourceViewer viewer= getSourceViewer();
         // if (viewer == null)
         // return;
         //			
         // IRegion region= getCurrentTextRegion(viewer);
         // if (region == null)
         // return;
         //			
         // highlightRegion(viewer, region);
         // activateCursor(viewer);
      }

      /*
       * @see org.eclipse.swt.events.KeyListener#keyReleased(org.eclipse.swt.events.KeyEvent)
       */
      public void keyReleased(KeyEvent event)
      {
         if (!fActive)
         {
            return;
         }
         deactivate();
      }

      /*
       * @see org.eclipse.swt.events.MouseListener#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
       */
      public void mouseDoubleClick(MouseEvent e)
      {
      }

      /*
       * @see org.eclipse.swt.events.MouseListener#mouseDown(org.eclipse.swt.events.MouseEvent)
       */
      public void mouseDown(MouseEvent event)
      {
         if (!fActive)
         {
            return;
         }
         if (event.stateMask != fKeyModifierMask)
         {
            deactivate();
            return;
         }
         if (event.button != 1)
         {
            deactivate();
            return;
         }
      }

      /*
       * @see org.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.MouseEvent)
       */
      public void mouseUp(MouseEvent e)
      {
         if (!fActive)
         {
            return;
         }
         if (e.button != 1)
         {
            deactivate();
            return;
         }
         boolean wasActive = fCursor != null;
         deactivate();
         if (wasActive)
         {
            IAction action = getAction("GotoDefinition"); //$NON-NLS-1$
            if (action != null)
            {
               action.run();
               // repairRepresentation(true);
               // getSourceViewer().invalidateTextPresentation();
            }
         }
      }

      /*
       * @see org.eclipse.swt.events.MouseMoveListener#mouseMove(org.eclipse.swt.events.MouseEvent)
       */
      public void mouseMove(MouseEvent event)
      {
         if (event.widget instanceof Control
               && !((Control) event.widget).isFocusControl())
         {
            deactivate();
            return;
         }
         if (!fActive)
         {
            if (event.stateMask != fKeyModifierMask)
            {
               return;
            }
            // modifier was already pressed
            fActive = true;
         }
         ISourceViewer viewer = getSourceViewer();
         if (viewer == null)
         {
            deactivate();
            return;
         }
         StyledText text = viewer.getTextWidget();
         if ((text == null) || text.isDisposed())
         {
            deactivate();
            return;
         }
         if (((event.stateMask & SWT.BUTTON1) != 0)
               && (text.getSelectionCount() != 0))
         {
            deactivate();
            return;
         }
         IRegion region = getCurrentTextRegion(viewer);
         if ((region == null) || (region.getLength() == 0))
         {
            repairRepresentation();
            getSourceViewer().invalidateTextPresentation();
            return;
         }
         highlightRegion(viewer, region);
         activateCursor(viewer);
      }

      /*
       * @see org.eclipse.swt.events.FocusListener#focusGained(org.eclipse.swt.events.FocusEvent)
       */
      public void focusGained(FocusEvent e)
      {
      }

      /*
       * @see org.eclipse.swt.events.FocusListener#focusLost(org.eclipse.swt.events.FocusEvent)
       */
      public void focusLost(FocusEvent event)
      {
         deactivate();
      }

      /*
       * @see org.eclipse.jface.text.IDocumentListener#documentAboutToBeChanged(org.eclipse.jface.text.DocumentEvent)
       */
      public void documentAboutToBeChanged(DocumentEvent event)
      {
         if (fActive && (fActiveRegion != null))
         {
            fRememberedPosition = new Position(fActiveRegion.getOffset(),
                  fActiveRegion.getLength());
            try
            {
               event.getDocument().addPosition(fRememberedPosition);
            }
            catch (BadLocationException x)
            {
               fRememberedPosition = null;
            }
         }
      }

      /*
       * @see org.eclipse.jface.text.IDocumentListener#documentChanged(org.eclipse.jface.text.DocumentEvent)
       */
      public void documentChanged(DocumentEvent event)
      {
         if ((fRememberedPosition != null) && !fRememberedPosition.isDeleted())
         {
            event.getDocument().removePosition(fRememberedPosition);
            fActiveRegion = new Region(fRememberedPosition.getOffset(),
                  fRememberedPosition.getLength());
         }
         fRememberedPosition = null;
         ISourceViewer viewer = getSourceViewer();
         if (viewer != null)
         {
            StyledText widget = viewer.getTextWidget();
            if ((widget != null) && !widget.isDisposed())
            {
               widget.getDisplay().asyncExec(new Runnable()
               {

                  public void run()
                  {
                     deactivate();
                  }
               });
            }
         }
      }

      /*
       * @see org.eclipse.jface.text.ITextInputListener#inputDocumentAboutToBeChanged(org.eclipse.jface.text.IDocument,
       *      org.eclipse.jface.text.IDocument)
       */
      public void inputDocumentAboutToBeChanged(IDocument oldInput,
            IDocument newInput)
      {
         if (oldInput == null)
         {
            return;
         }
         deactivate();
         oldInput.removeDocumentListener(this);
      }

      /*
       * @see org.eclipse.jface.text.ITextInputListener#inputDocumentChanged(org.eclipse.jface.text.IDocument,
       *      org.eclipse.jface.text.IDocument)
       */
      public void inputDocumentChanged(IDocument oldInput, IDocument newInput)
      {
         if (newInput == null)
         {
            return;
         }
         newInput.addDocumentListener(this);
      }

      /*
       * @see PaintListener#paintControl(PaintEvent)
       */
      public void paintControl(PaintEvent event)
      {
         if (fActiveRegion == null)
         {
            return;
         }
         ISourceViewer viewer = getSourceViewer();
         if (viewer == null)
         {
            return;
         }
         StyledText text = viewer.getTextWidget();
         if ((text == null) || text.isDisposed())
         {
            return;
         }
         int offset = 0;
         int length = 0;
         if (viewer instanceof ITextViewerExtension5)
         {
            ITextViewerExtension5 extension = (ITextViewerExtension5) viewer;
            IRegion widgetRange = extension.modelRange2WidgetRange(new Region(
                  offset, length));
            if (widgetRange == null)
            {
               return;
            }
            offset = widgetRange.getOffset();
            length = widgetRange.getLength();
         }
         else
         {
            IRegion region = viewer.getVisibleRegion();
            if (!includes(region, fActiveRegion))
            {
               return;
            }
            offset = fActiveRegion.getOffset() - region.getOffset();
            length = fActiveRegion.getLength();
         }
         // support for bidi
         Point minLocation = getMinimumLocation(text, offset, length);
         Point maxLocation = getMaximumLocation(text, offset, length);
         int x1 = minLocation.x;
         int x2 = (minLocation.x + maxLocation.x) - minLocation.x - 1;
         int y = (minLocation.y + text.getLineHeight()) - 1;
         GC gc = event.gc;
         if ((fColor != null) && !fColor.isDisposed())
         {
            gc.setForeground(fColor);
         }
         gc.drawLine(x1, y, x2, y);
      }

      private boolean includes(IRegion region, IRegion position)
      {
         return (position.getOffset() >= region.getOffset())
               && ((position.getOffset() + position.getLength()) <= (region
                     .getOffset() + region.getLength()));
      }

      private Point getMinimumLocation(StyledText text, int offset, int length)
      {
         Point minLocation = new Point(Integer.MAX_VALUE, Integer.MAX_VALUE);
         for (int i = 0; i <= length; i++)
         {
            Point location = text.getLocationAtOffset(offset + i);
            if (location.x < minLocation.x)
            {
               minLocation.x = location.x;
            }
            if (location.y < minLocation.y)
            {
               minLocation.y = location.y;
            }
         }
         return minLocation;
      }

      private Point getMaximumLocation(StyledText text, int offset, int length)
      {
         Point maxLocation = new Point(Integer.MIN_VALUE, Integer.MIN_VALUE);
         for (int i = 0; i <= length; i++)
         {
            Point location = text.getLocationAtOffset(offset + i);
            if (location.x > maxLocation.x)
            {
               maxLocation.x = location.x;
            }
            if (location.y > maxLocation.y)
            {
               maxLocation.y = location.y;
            }
         }
         return maxLocation;
      }

      /*
       * (non-Javadoc)
       * 
       * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
       */
      public void propertyChange(PropertyChangeEvent event)
      {
         // TODO Auto-generated method stub
      }
   }

   // folding
   // private RecipeOccurrencesUpdater fOccurrencesUpdater;
   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.ui.texteditor.AbstractDecoratedTextEditor#createPartControl(org.eclipse.swt.widgets.Composite)
    */
   public void createPartControl(Composite parent)
   {
      super.createPartControl(parent);
      ProjectionViewer projectionViewer = (ProjectionViewer) getSourceViewer();
      projectionSupport = new ProjectionSupport(projectionViewer,
            getAnnotationAccess(), getSharedColors());
      projectionSupport.install();
      projectionViewer.doOperation(ProjectionViewer.TOGGLE);
      // fOccurrencesUpdater= new RecipeOccurrencesUpdater(this);
   }

   protected ISourceViewer createSourceViewer(Composite parent,
         IVerticalRuler ruler, int styles)
   {
      fAnnotationAccess = createAnnotationAccess();
      fOverviewRuler = createOverviewRuler(getSharedColors());
      ISourceViewer viewer = new ProjectionViewer(parent, ruler,
            fOverviewRuler, true, styles);
      // ensure decoration support has been created and configured:
      getSourceViewerDecorationSupport(viewer);
      return viewer;
   }

   public void fold(int foldingStart, int foldingEnd)
   {
      Position foldingPosition = new Position(foldingStart, foldingEnd
            - foldingStart);
      if (getSourceViewer().getAnnotationModel() != null)
      {
         getSourceViewer().getAnnotationModel().addAnnotation(
               new ProjectionAnnotation(), foldingPosition);
      }
   }

   public static IEditorPart openInEditor(IFile file)
   {
      if (file != null)
      {
         IWorkbenchPage p = VelocityPlugin.getActiveWorkbenchWindow()
               .getActivePage();
         if (p != null)
         {
            IEditorPart editorPart = null;
            try
            {
               editorPart = IDE.openEditor(p, file, true);
            }
            catch (PartInitException e)
            {
               // TODO Auto-generated catch block
               e.printStackTrace();
            }
            // initializeHighlightRange(editorPart);
            return editorPart;
         }
      }
      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.ui.editors.text.TextEditor#doSetInput(org.eclipse.ui.IEditorInput)
    */
   @Override
   protected void doSetInput(IEditorInput input) throws CoreException
   {
      super.doSetInput(input);

      IResource r = (IResource) input.getAdapter(IResource.class);
      setCompletionData(r);

      String v = (String) r.getSessionProperty(new QualifiedName(KEY,
            "hidepreviewpage"));
      if (v != null && v.equalsIgnoreCase("true"))
      {
         fHidePreviewPage = true;
      }
   }

   /**
    * Get the completion data. The completion data is stored on session
    * variables (if present). The variables are named as follows (names given
    * using xml style syntax. All data in the arrays are given as simple
    * strings.
    * <ul>
    * <li>rx:fields - List<String[]> - each list element is an object array
    * with either one or two components. The first component is the field name.
    * The second, optional, component is a field description.</li>
    * <li>rx:variables - List<String[]> - each list element is an object array
    * with either two or three components. The first component is the variable
    * name. The second component is the type of the variable if known, or empty
    * is not known. The third, optional, component is a description.</li>
    * <li>rx:methods - List<Object[]> - each list element is an object array
    * with three required elements. The first is the name of the method. The
    * second is the return type of the method or "void" if the method doesn't
    * return a value. The third is a description of the method. This is followed
    * by one or more parameter descriptions. Each parameter description is an
    * object array with three values, the name of the parameter, the type of the
    * parameter and a description of the parameter. The type and description can
    * be empty.
    * <li>rx:macros - List<String> - Each available macro is the string value, 
    * i.e. #foo()
    * </ul>
    * 
    * @param file File
    */
   @SuppressWarnings("unchecked")
   private void setCompletionData(IResource file)
   {
      try
      {
         fFieldCompletions = getListFromProperty(file, "fields");
         fMethodCompletions = getListFromProperty(file, "methods");
         fVariableCompletions = getListFromProperty(file, "variables");
         fMacroCompletions = getListFromProperty(file, "macros");
      }
      catch (CoreException e)
      {
         // 
      }
   }

   /**
    * Retreives list from the resource property specified by the provided key.
    * If such property does not exist returns an empty list.
    * @param resource the resource to read property from.
    * Assumed not <code>null</code>.
    * @param subkey the property key in the set of velocity editor properties.
    * Assumed not <code>null</code>.
    * @return the list specified by the key or an empty list if such property
    * does not exist or is not a list. Never <code>null</code>.
    * @throws CoreException on property retrieval failure.
    */
   private List getListFromProperty(IResource resource, String subkey)
         throws CoreException
   {
      final Object value =
            resource.getSessionProperty(new QualifiedName(KEY, subkey));
      return value instanceof List
            ? (List) value
            : Collections.emptyList();
   }

   /**
    * Insert the template text at the current caret or replacing the current
    * selection in the editor
    * 
    * @param expansion the expansion text, never <code>null</code>
    */
   public void insertTemplateAtCursor(String expansion)
   {
      if (expansion == null || expansion.trim().length() == 0)
      {
         throw new IllegalArgumentException("Expansion cannot be empty");
      }

      ISourceViewer sourceViewer = getSourceViewer();
      if (sourceViewer != null)
      {
         StyledText styledText = sourceViewer.getTextWidget();
         styledText.insert(expansion);
      }
   }

   /**
    * Store the multi page parent editor so it can be modified as the input is
    * defined. The input can tell the multipage editor to only display the
    * velocity editor page
    * 
    * @param editor the multipage editor, never <code>null</code>
    */
   public void setMultiPartEditor(MultiPageEditor editor)
   {
      if (editor == null)
      {
         throw new IllegalArgumentException("editor may not be null");
      }
      fMultiPageEditor = editor;

      fMultiPageEditor.hidePreview(fHidePreviewPage);
   }
}