/******************************************************************************
 *
 * [ PSBindingEditor.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.editors.form;

import com.percussion.workbench.ui.help.IPSHelpProvider;
import com.percussion.workbench.ui.help.PSHelpManager;
import org.eclipse.jface.contentassist.SubjectControlContentAssistant;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ContextInformation;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A subclass of the text widget that creates an editor suitable for bindings.
 * The editor creates a content assistent and registers the appropriate pieces
 * to allow the user to complete references to variables and methods.
 * 
 * @author dougrand
 * 
 */
public class PSBindingEditor extends SourceViewer
      implements
         IContentAssistProcessor,
         IContextInformationValidator,
         KeyListener,
         IPSHelpProvider
{
   public IContentAssistProcessor mi_processor;

   /**
    * Configuration object that ties the completion information into the source
    * viewer.
    */
   public class BindingEditorConfiguration extends SourceViewerConfiguration
   {

      /*
       * (non-Javadoc)
       * 
       * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getContentAssistant(org.eclipse.jface.text.source.ISourceViewer)
       */
      @Override
      public IContentAssistant getContentAssistant(
            @SuppressWarnings("unused") ISourceViewer sourceViewer)
      {

         final SubjectControlContentAssistant assistant = new SubjectControlContentAssistant();

         assistant.setContentAssistProcessor(mi_processor, VAR);
         assistant.setContentAssistProcessor(mi_processor, PARAM);
         assistant.setContentAssistProcessor(mi_processor,
               IDocument.DEFAULT_CONTENT_TYPE);
         assistant.enableAutoInsert(true);
         assistant.enableAutoActivation(true);
         assistant.setAutoActivationDelay(500);
         assistant
               .setProposalPopupOrientation(IContentAssistant.PROPOSAL_OVERLAY);
         assistant
               .setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_ABOVE);
         return assistant;
      }

   }

   private static final String VAR = "var";

   private static final String PARAM = "param";

   private static Comparator ms_comparator = new Comparator()
   {
      public int compare(Object aProposal1, Object aProposal2)
      {
         String text1 = ((CompletionProposal) aProposal1).getDisplayString();
         String text2 = ((CompletionProposal) aProposal2).getDisplayString();
         return text1.compareTo(text2);
      }
   };

   private List<String[]> m_varCompletions = new ArrayList<String[]>();

   private List<Object[]> m_methodCompletions = new ArrayList<Object[]>();

   private int m_lastCompletionOffset = -1;

   private String m_currentContext = null;

   /**
    * Ctor
    * @param parent the swt parent of this object, never <code>null</code>
    * @param style the style of this object, see {@link SourceViewer} for
    * information about the valid styles for the object
    */
   public PSBindingEditor(Composite parent, int style) {
      super(parent, null, style);

      // Set an initial empty document
      IDocument doc = new Document("");
      this.setDocument(doc);
      mi_processor = this;

      this.getControl().addKeyListener(this);

      configure(new BindingEditorConfiguration());
   }

   /**
    * @return Returns the methodCompletions.
    */
   public List<Object[]> getMethodCompletions()
   {
      return m_methodCompletions;
   }

   /**
    * @param methodCompletions The methodCompletions to set.
    */
   public void setMethodCompletions(List<Object[]> methodCompletions)
   {
      m_methodCompletions = methodCompletions;
   }

   /**
    * @return Returns the varCompletions.
    */
   public List<String[]> getVarCompletions()
   {
      return m_varCompletions;
   }

   /**
    * @param varCompletions The varCompletions to set.
    */
   public void setVarCompletions(List<String[]> varCompletions)
   {
      m_varCompletions = varCompletions;
   }

   @SuppressWarnings("unchecked")
   public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer,
         int offset)
   {
      if (m_currentContext == null)
      {
         IContextInformation info[] = computeContextInformation(viewer, offset);
         if (info != null && info.length > 0)
         {
            m_currentContext = info[0].getContextDisplayString();
         }
      }

      char ch;
      try
      {
         ch = viewer.getDocument().getChar(offset-1);
      }
      catch (BadLocationException e)
      {
         ch = 0;
      }
      if (ch == '$')
      {
         m_currentContext = VAR;
      }
      
      if (m_currentContext == null)
      {
         // If still null, we don't know what to do
         return null;
      }

      if (m_currentContext.equals(VAR))
      {
         return computeVarAndMethodProposals(viewer, offset);
      }
      else if (m_currentContext.equals(PARAM))
      {
         return computeParameterProposals(viewer, offset);
      }
      else
      {
         return null;
      }
   }
   
   /**
    * While not currently implemented, the intent is to compute parameter
    * proposals here.
    * 
    * @param viewer the viewer, never <code>null</code>
    * @param offset the current character offset
    * @return an array of possible completions, may be <code>null</code> or
    * empty
    */
   @SuppressWarnings({"unchecked","unused"})
   public ICompletionProposal[] computeParameterProposals(
         ITextViewer viewer, int offset)
   {
      return null;
   }

   /**
    * Compute the variables and methods that would complete the current 
    * selection.
    * 
    * @param viewer the viewer, never <code>null</code>
    * @param offset the current character offset
    * @return an array of possible completions, may be <code>null</code> or
    * empty
    */
   @SuppressWarnings({"unchecked","unused"})
   public ICompletionProposal[] computeVarAndMethodProposals(
         ITextViewer viewer, int offset)
   {
      m_lastCompletionOffset = offset;

      List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
      try
      {
         IDocument doc = getDocument();
         if (offset >= doc.getLength())
         {
            offset--;
         }
         int start = findIdStart(offset);
         int end = findIdEnd(offset);
         int delta = end - start;

         String prefix = doc.get(start, offset - start);
         
         if (doc.getLength() <= end)
         {
            // Add a space so the completion will work
            doc.replace(end,0," ");
         }
         
         String varpre;
         if (prefix.startsWith("$"))
            varpre = prefix.substring(1);
         else
            varpre = prefix;

         for (String[] completion : m_varCompletions)
         {
            String name = completion[0];
            String replacement = name;

            if (replacement.startsWith(varpre))
            {
               String type = completion.length > 1 ? completion[1] : "";
               String desc = completion.length > 2 ? completion[2] : name;

               if (type.trim().length() > 0)
               {
                  desc = "[" + type + "] " + desc;
               }

               CompletionProposal prop = new CompletionProposal("$" + replacement,
                     start, delta, replacement.length() + 1, null, name
                           + "   " + desc, null, null);

               proposals.add(prop);
            }
         }
         for (Object[] completion : m_methodCompletions)
         {
            String name = (String) completion[0];
            String replacement = name;
            if (replacement.startsWith(prefix))
            {
               String type = (String) (completion.length > 1
                     ? completion[1]
                     : "");
               String desc = (String) (completion.length > 2
                     ? completion[2]
                     : name);

               if (type.trim().length() > 0)
               {
                  desc = "[" + type + "] " + desc;
               }

               // Add method parameters
               replacement = replacement + "(";
               boolean first = true;
               for (int i = 3; i < completion.length; i++)
               {
                  String pdata[] = (String[]) completion[i];
                  if (!first)
                  {
                     replacement = replacement + ",";
                  }
                  first = false;
                  replacement = replacement + pdata[0];
               }
               replacement = replacement + ")";
               CompletionProposal prop = new CompletionProposal(replacement,
                     start, delta, replacement.length(), null, name + "   "
                           + desc, null, desc);

               proposals.add(prop);
            }
         }
         Collections.sort(proposals, ms_comparator);
         return proposals.toArray(new ICompletionProposal[proposals.size()]);
      }
      catch (BadLocationException e)
      {
         throw new RuntimeException();
      }
   }

   /**
    * Find the last id character, see {@link #findIdStart(int)} for id character
    * rules
    * 
    * @param offset the starting offset
    * @return the last position
    * @throws BadLocationException
    */
   private int findIdEnd(int offset) throws BadLocationException
   {
      IDocument doc = getDocument();

      while (offset < doc.getLength())
      {
         char ch = doc.getChar(offset);
         if (!(Character.isLetterOrDigit(ch) || ch == '.' || ch == '$'))
            return offset;
         offset++;
      }
      return doc.getLength();
   }

   /**
    * Find the first id character, an id starts with a dollar sign, then has an
    * arbitrary sequence of letters, digits and periods.
    * 
    * @param offset starting position
    * @return if first position, or 0 if none found
    * @throws BadLocationException
    */
   private int findIdStart(int offset) throws BadLocationException
   {
      IDocument doc = getDocument();

      while (offset > 0)
      {
         char ch = doc.getChar(offset);
         if (ch == '$')
            return offset;
         offset--;
      }

      return 0;
   }

   private static final IContextInformation ms_jexlcontexts[] =
   {new ContextInformation(VAR, VAR)};

   private static final IContextInformation ms_paramcontexts[] =
   {new ContextInformation(PARAM, PARAM)};

   private static final char[] ms_completionCharacters =
   {'$', '.'};

   private static final char[] ms_contextCharacters =
   {'(', '"', ')'};

   public IContextInformation[] computeContextInformation(
         @SuppressWarnings("unused") ITextViewer viewer,
         int offset)
   {
      m_lastCompletionOffset = offset;

      // Look for last "interesting" character
      char last;
      try
      {
         last = findLastContextChar(offset);
      }
      catch (BadLocationException e)
      {
         throw new RuntimeException();
      }

      if (last == '$')
         return ms_jexlcontexts;
      else if (last == '(' || last == '"' || last == '\'' || last == ',')
         return ms_paramcontexts;
      else
         return null;
   }
   
   

   /* 
    * @see com.percussion.workbench.ui.help.IPSHelpProvider#
    * getHelpKey(org.eclipse.swt.widgets.Control)
    */
   public String getHelpKey(@SuppressWarnings("unused") Control control)
   {
       return getClass().getName();
   }

   /* 
    * @see org.eclipse.jface.text.source.SourceViewer#createControl(
    * org.eclipse.swt.widgets.Composite, int)
    */
   @Override
   protected void createControl(Composite parent, int styles)
   {      
      super.createControl(parent, styles);
      m_helpManager = new PSHelpManager(this, parent);
   }

   /**
    * Find the last character that indicates what context we're in. This first
    * figures out if the offset is within a quoted string (for which it searches
    * from the last newline to the current position).
    * 
    * @param offset
    * @return the last character
    * @throws BadLocationException
    */
   private char findLastContextChar(int offset) throws BadLocationException
   {
      IDocument doc = getDocument();
      int pos = offset;

      if (pos >= doc.getLength())
      {
         pos = doc.getLength() - 1;
      }

      while (pos >= 0)
      {
         char ch = doc.getChar(pos);

         if (ch == '\'' || ch == '"' || ch == ',' || ch == '(')
         {
            return ch;
         }
         else if (ch == '$')
         {
            return ch;
         }
         pos--;
      }

      return 0;
   }

   public char[] getCompletionProposalAutoActivationCharacters()
   {
      return ms_completionCharacters;
   }

   public char[] getContextInformationAutoActivationCharacters()
   {
      return ms_contextCharacters;
   }

   public String getErrorMessage()
   {
      return null;
   }

   public IContextInformationValidator getContextInformationValidator()
   {
      return this;
   }

   public void install(IContextInformation info, 
         @SuppressWarnings("unused") ITextViewer viewer, 
         @SuppressWarnings("unused") int offset)
   {
      m_currentContext = info != null ? info.getContextDisplayString() : null;
   }

   public boolean isContextInformationValid(int offset)
   {
      return offset == m_lastCompletionOffset;
   }

   public void keyPressed(KeyEvent e)
   {
      if (e.stateMask == SWT.CONTROL && e.character == ' ')
      {
         doOperation(CONTENTASSIST_PROPOSALS);
      }
   }

   @SuppressWarnings("unused")
   public void keyReleased(KeyEvent e)
   {
   }
   
   /**
    * The help manager for this editor, Initialized in
    * {@link #createControl(Composite, int)}, never
    * <code>null</code> after that.
    */
   @SuppressWarnings("unused")
   private PSHelpManager m_helpManager;

}
