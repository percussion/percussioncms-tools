package de.byteaction.velocity.vaulttec.ui.editor;

import de.byteaction.velocity.editor.VelocityEditor;
import de.byteaction.velocity.editor.VelocityFoldingStructureProvider;
import de.byteaction.velocity.vaulttec.ui.VelocityPlugin;
import de.byteaction.velocity.vaulttec.ui.editor.parser.NodeVisitor;
import de.byteaction.velocity.vaulttec.ui.model.ITreeNode;
import de.byteaction.velocity.vaulttec.ui.model.ITreeVisitor;
import de.byteaction.velocity.vaulttec.ui.model.Template;
import org.apache.velocity.runtime.RuntimeInstance;
import org.apache.velocity.runtime.parser.ParseException;
import org.apache.velocity.runtime.parser.Token;
import org.apache.velocity.runtime.parser.node.SimpleNode;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IFileEditorInput;
import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.Tag;
import org.htmlparser.util.NodeIterator;
import org.htmlparser.util.ParserException;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Reconciler strategy which parses the whole editor's content (a Velocity
 * template) on a document change.
 */
public class VelocityReconcilingStrategy implements IReconcilingStrategy, IReconcilingStrategyExtension
{

    private VelocityEditor                   fEditor;
    private Template                         fTemplate;
    private Template                         fLastTemplate;
    private String                           fError;
    private IDocument                        fDocument;
    private IProgressMonitor                 fProgressMonitor;
    private VelocityFoldingStructureProvider fFoldingStructureProvider;

    public VelocityReconcilingStrategy(VelocityEditor anEditor)
    {
        fEditor = anEditor;
        fFoldingStructureProvider = new VelocityFoldingStructureProvider(fEditor);
    }

    /**
     * DOCUMENT ME!
     * 
     * @param aDocument
     *            DOCUMENT ME!
     */
    public void setDocument(IDocument aDocument)
    {
        fDocument = aDocument;
        fFoldingStructureProvider.setDocument(fDocument);
        parse();
    }

    /**
     * DOCUMENT ME!
     * 
     * @param aDirtyRegion
     *            DOCUMENT ME!
     * @param aRegion
     *            DOCUMENT ME!
     */
    public void reconcile(DirtyRegion aDirtyRegion, IRegion aRegion)
    {
        parse();
    }

    /**
     * DOCUMENT ME!
     * 
     * @param aPartition
     *            DOCUMENT ME!
     */
    public void reconcile(IRegion aPartition)
    {
        parse();
    }

    private List htmlTags = null;

    private void parse()
    {
        htmlTags = new ArrayList();
        IFile file = ((IFileEditorInput) fEditor.getEditorInput()).getFile();
        String name = file.getName();
        Reader reader = new StringReader(fEditor.getDocument().get());
        Template template = null;
        try
        {
            file.deleteMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
            RuntimeInstance runtime = VelocityEditorEnvironment.getParser();
            parseHtml();
            // end html
            org.apache.velocity.Template velTemplate = runtime.getTemplate(name, "UTF-8");
            SimpleNode root = runtime.parse(reader, String.valueOf(velTemplate));
            // Create tree model
            NodeVisitor visitor = new NodeVisitor(name);
            root.jjtAccept(visitor, null);
            template = visitor.getTemplate();
            fError = "";
        }
        catch (ParseException e)
        {
            if (e.getMessage() != null)
            {
                fError = e.getMessage();
                Token token = e.currentToken;
                if (token != null)
                {
                    fEditor.addProblemMarker(e.getMessage(), token.next.beginLine, IMarker.SEVERITY_ERROR);
                }
            } else
            {
                fError = "";
            }
        }
        catch (Exception e)
        {
            fError = "";
            VelocityPlugin.log(e);
        }
        finally
        {
            try
            {
                reader.close();
            }
            catch (IOException e)
            {
                VelocityPlugin.log(e);
            }
        }
        // Replace saved template with the new parsed one
        synchronized (this)
        {
            if (template != null)
            {
                fTemplate = template;
                // Save last successful parse tree
                fLastTemplate = template;
            } else
            {
                fTemplate = null;
            }
        }
        // Update outline view and display error message in status line
        Display.getDefault().syncExec(new Runnable() {

            public void run()
            {
                fEditor.updateOutlinePage();
                fEditor.displayErrorMessage(fError);
            }
        });
        reconcile();
    }

    private class HtmlNode implements ITreeNode
    {

        // VHtmlNodeVisitor htmlNodeVisitor = new VHtmlNodeVisitor();
        private Tag arg0 = null;

        /**
         * 
         */
        public HtmlNode(Tag arg0)
        {
            this.arg0 = arg0;
        }

        public String getName()
        {
            return null;
        }

        public Object getParent()
        {
            // return arg0.getParent();
            return null;
        }

        public boolean hasChildren()
        {
            // return arg0.getChildren()!=null;
            return false;
            // return !htmlNodeVisitor.getNodeChildren().isEmpty();
        }

        public Object[] getChildren()
        {
            // arg0.accept(htmlNodeVisitor);
            // NodeList children = arg0.getChildren();
            // try
            // {
            // for (NodeIterator iter = children.elements();
            // iter.hasMoreNodes();)
            // {
            // Node element = (Node) iter.nextNode();
            //                    
            // }
            // }
            // catch (ParserException e)
            // {
            // // TODO Auto-generated catch block
            // e.printStackTrace();
            // }
            // return htmlNodeVisitor.getNodeChildren().isEmpty() ? NO_CHILDREN
            // : htmlNodeVisitor.getNodeChildren().toArray();
            return NO_CHILDREN;
        }

        public String getUniqueID()
        {
            return arg0.toString();
        }

        public int getStartLine()
        {
            return arg0.getStartingLineNumber() + 1;
        }

        public int getEndLine()
        {
            int nr = arg0.getStartingLineNumber() + 1;
            int nrE = nr;
            Tag endTag = arg0.getEndTag();
            if (endTag != null)
            {
                nrE = endTag.getEndingLineNumber();
                int offset = endTag.getStartPosition() - endTag.getEndPosition();
                if (offset == 0) fEditor.addProblemMarker(endTag.getTagName().toLowerCase() + " is not correctly closed proposed line for closing is line " + nrE, nr, IMarker.SEVERITY_WARNING);
            }
            return nrE;
        }

        public boolean accept(ITreeVisitor aVisitor)
        {
            // TODO Auto-generated method stub
            return false;
        }
    }

    private class VHtmlNodeVisitor extends org.htmlparser.visitors.NodeVisitor
    {

        // private List nodeChildren = new ArrayList();
        // private HtmlNode rootNode = null;
        public void visitTag(final Tag arg0)
        {
            // htmlTags.add(new HtmlNode(arg0));
            // if (rootNode == null)
            // {
            // rootNode = new HtmlNode(arg0);
            // } else
            // {
            // nodeChildren.add(new HtmlNode(arg0));
            // }
            htmlTags.add(new HtmlNode(arg0));
        }
        /*
         * (non-Javadoc)
         * @see org.htmlparser.visitors.NodeVisitor#visitEndTag(org.htmlparser.Tag)
         */
        // public void visitEndTag(Tag arg0)
        // {
        // int nr = arg0.getStartingLineNumber() + 1;
        // Node parent = arg0.getParent();
        // if(arg0.isEndTag() && parent==null || (parent instanceof Tag &&
        // !((Tag) parent).getTagName().equalsIgnoreCase(arg0.getTagName())))
        // {
        // fEditor.addProblemMarker(arg0.getTagName().toLowerCase()+" is not
        // correctly opened", nr,IMarker.SEVERITY_WARNING);
        //                
        // }
        // }
        // /**
        // * @return Returns the nodeChildren.
        // */
        // public List getNodeChildren()
        // {
        // return nodeChildren;
        // }
        //
        // /**
        // * @return Returns the rootNode.
        // */
        // public HtmlNode getRootNode()
        // {
        // return rootNode;
        // }
    }

    /**
     * @throws ParserException
     */
    private void parseHtml() throws ParserException
    {
        // start html
        Parser parser = new Parser();
        parser.setInputHTML(fDocument.get());
        for (NodeIterator e = parser.elements(); e.hasMoreNodes();)
        {
            Node node = e.nextNode();
            VHtmlNodeVisitor htmlNodeVisitor = new VHtmlNodeVisitor();
            node.accept(htmlNodeVisitor);
            // VelocityReconcilingStrategy.HtmlNode rootNode =
            // htmlNodeVisitor.getRootNode();
            // if (rootNode != null)
            // {
            // htmlTags.add(rootNode);
            // }
        }
    }

    /**
     * Returns root elements of current parse tree.
     */
    public Object[] getRootElements()
    {
        return ((fTemplate != null) ? fTemplate.getChildren() : ITreeNode.NO_CHILDREN);
    }

    /**
     * Returns root node of current parse tree.
     */
    public ITreeNode getRootNode()
    {
        return fTemplate;
    }

    /**
     * Returns last successful parse tree.
     */
    public ITreeNode getLastRootNode()
    {
        return fLastTemplate;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension#initialReconcile()
     */
    public void initialReconcile()
    {
        reconcile();
    }

    private void reconcile()
    {
        // fFoldingStructureProvider.updateFoldingRegions(fTemplate);
        if (fTemplate != null) htmlTags.add(fTemplate);
        fFoldingStructureProvider.updateFoldingRegions(htmlTags.toArray());
    }

    /**
     * 
     */
    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension#setProgressMonitor(org.eclipse.core.runtime.IProgressMonitor)
     */
    public void setProgressMonitor(IProgressMonitor monitor)
    {
        // TODO Auto-generated method stub
    }
}
