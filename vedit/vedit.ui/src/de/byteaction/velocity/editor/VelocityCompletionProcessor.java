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

package de.byteaction.velocity.editor;

import de.byteaction.velocity.dtd.parser.DTDAttribute;
import de.byteaction.velocity.dtd.parser.DTDChoice;
import de.byteaction.velocity.dtd.parser.DTDElement;
import de.byteaction.velocity.dtd.parser.DTDEnumeration;
import de.byteaction.velocity.dtd.parser.DTDItem;
import de.byteaction.velocity.dtd.parser.DTDMixed;
import de.byteaction.velocity.dtd.parser.DTDName;
import de.byteaction.velocity.dtd.parser.DTDNotationList;
import de.byteaction.velocity.dtd.parser.DTDSequence;
import de.byteaction.velocity.preferences.GeneralPreferencePage;
import de.byteaction.velocity.ui.VeloContextType;
import de.byteaction.velocity.ui.editor.xml.IEditorConfiguration;
import de.byteaction.velocity.ui.editor.xml.IHTMLConstants;
import de.byteaction.velocity.utils.IdUtils;
import de.byteaction.velocity.vaulttec.ui.VelocityPlugin;
import de.byteaction.velocity.vaulttec.ui.VelocityPluginImages;
import de.byteaction.velocity.vaulttec.ui.editor.VelocityConfiguration;
import de.byteaction.velocity.vaulttec.ui.editor.VelocityEditorEnvironment;
import de.byteaction.velocity.vaulttec.ui.editor.parser.VelocityMacro;
import de.byteaction.velocity.vaulttec.ui.editor.text.VelocityTextGuesser;
import de.byteaction.velocity.vaulttec.ui.editor.text.VelocityTextGuesser.Type;
import de.byteaction.velocity.vaulttec.ui.model.Directive;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ContextInformation;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateCompletionProcessor;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.swt.graphics.Image;

/**
 * DOCUMENT ME!
 * 
 * @version $Revision: 1.1 $
 * @author <a href="mailto:as@byteaction.de">Akmal Sarhan </a>
 */
@SuppressWarnings("unchecked")
public class VelocityCompletionProcessor extends TemplateCompletionProcessor
      implements
         IContentAssistProcessor,
         IHTMLConstants
{

   boolean upperCaseEnabled;

   private static final String DEFAULT_IMAGE = "icons/template.gif"; //$NON-NLS-1$

   private static HashSet FLAT_VALUEs_SET;

   private static Set fEMPTY_TAG_SET;

   private static Set TAG_SET;

   private static Comparator PROPOSAL_COMPARATOR = new Comparator()
   {
      public int compare(Object aProposal1, Object aProposal2)
      {
         String text1 = ((CompletionProposal) aProposal1).getDisplayString();
         String text2 = ((CompletionProposal) aProposal2).getDisplayString();
         return text1.compareTo(text2);
      }

      public boolean equals(Object aProposal)
      {
         return false;
      }
   };

   private static final String[] ms_flatValues = new String[]
   {"compact", "checked", "declare", "readonly", "disabled", "selected",
         "defer", "ismap", "nohref", "noshade", "nowrap", "multiple",
         "noresize"};

   public static final String[] ms_tags = new String[]
   {"AREA", "BASE", "BASEFONT", "BR", "COL", "FRAME", "HR", "IMG", "INPUT",
         "ISINDEX", "LINK", "META", "PARAM", "A", "ADDRESS", "APPLET", "B",
         "BIG", "BLOCKQUOTE", "BODY", "CAPTION", "CENTER", "CITE", "CODE",
         "COL", "COLGROUP", "DD", "DFN", "DIR", "DIV", "DL", "DT", "EM",
         "FONT", "FORM", "FRAMESET", "H1", "H2", "H3", "H4", "H5", "H6",
         "HEAD", "HTML", "I", "KBD", "LI", "MAP", "MENU", "NOBR", "NOFRAMES",
         "OBJECT", "OL", "OPTION", "P", "PRE", "SAMP", "SCRIPT", "SELECT",
         "SMALL", "SPAN", "STRIKE", "S", "STRONG", "STYLE", "SUB", "SUP",
         "TABLE", "TD", "TEXTAREA", "TH", "TITLE", "TR", "TT", "U", "UL", "VAR"};

   static
   {
      fEMPTY_TAG_SET = new HashSet();
      TAG_SET = new HashSet();
      for (int i = 0; i < ms_tags.length; i++)
      {
         TAG_SET.add(ms_tags[i]);
      }
      for (int i = 0; i < IHTMLConstants.EMPTY_TAGS.length; i++)
      {
         fEMPTY_TAG_SET.add(IHTMLConstants.EMPTY_TAGS[i]);
      }
      FLAT_VALUEs_SET = new HashSet();
      for (int i = 0; i < ms_flatValues.length; i++)
      {
         FLAT_VALUEs_SET.add(ms_flatValues[i]);
      }
   }

   private VelocityEditor fEditor;

   private boolean fCompleteDirectives = true;

   protected int fTabWidth = 4;

   public VelocityCompletionProcessor(VelocityEditor anEditor,
         boolean aCompleteDirectives) {
      upperCaseEnabled = VelocityPlugin.getDefault().getPreferenceStore()
            .getBoolean(GeneralPreferencePage.P_CASE);
      fEditor = anEditor;
      fCompleteDirectives = aCompleteDirectives;
   }

   /**
    * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#computeCompletionProposals(org.eclipse.jface.text.ITextViewer,
    *      int)
    */
   public ICompletionProposal[] computeCompletionProposals(ITextViewer aViewer,
         int anOffset)
   {
      ICompletionProposal[] proposals = null;
      IDocument doc = aViewer.getDocument();
      VelocityTextGuesser prefix = new VelocityTextGuesser(doc, anOffset, false);
      @SuppressWarnings("unused")
      String temp = null;
      try
      {
         temp = doc.get(anOffset - 1, 1);
      }
      catch (Exception e)
      {
         // TODO: handle exception
      }
      if (prefix.getType() == Type.TYPE_DIRECTIVE)
      {
         if (fCompleteDirectives)
         {
            proposals = getDirectiveProposals(prefix.getText(), anOffset
                  - prefix.getText().length());
         }
      }
      else if (prefix.getType() == Type.TYPE_VARIABLE)
      {
         proposals = getVariableProposals(prefix.getText(), anOffset
               - prefix.getText().length());
      }
      else if (prefix.getType() == Type.TAG_DIRECTIVE)
      {
         proposals = getTAGProposals(doc, prefix.getText(), anOffset
               - prefix.getText().length());
      }
      else if (prefix.getType() == Type.TAG_CLOSE)
      {
         proposals = getTAGCloseProposals(doc, prefix.getText(), anOffset
               - prefix.getText().length());
      }
      else if (prefix.getType() == Type.TYPE_APOSTROPHE)
      {
         proposals = getAttributeValuesProposals(doc, prefix.getText(),
               anOffset - prefix.getText().length());
      }
      else if (prefix.getType() == Type.TYPE_FIELD)
      {
         proposals = getFieldProposals(doc, prefix.getText(), anOffset
               - prefix.getText().length());
      }
      else if (EditorsUtil.isInsidePartition(anOffset, new String[]
      {IEditorConfiguration.TAG_PARTITION,
            IEditorConfiguration.SCRIPT_PARTITION}, true, doc))
      {
         proposals = getAttributesProposals(doc, prefix.getText(), anOffset
               - prefix.getText().length());
      }
      else
      {
         proposals = getWordsProposals(doc, prefix.getText(), anOffset
               - prefix.getText().length());
      }
      if (proposals == null)
      {
         proposals = super.computeCompletionProposals(aViewer, anOffset);
      }
      return proposals;
   }

   private ICompletionProposal[] getFieldProposals(@SuppressWarnings("unused")
   IDocument doc, String text, int pos)
   {
      List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
      if (fEditor.fFieldCompletions != null)
      {
         for (String[] completion : fEditor.fFieldCompletions)
         {
            String name = completion[0];
            if (name.startsWith(text))
            {
               String replacement = name;
               String description = completion.length > 1
                     ? completion[1]
                     : name;

               CompletionProposal prop = new CompletionProposal(replacement,
                     pos, text.length(), replacement.length(), null,
                     replacement, null, description);

               proposals.add(prop);
            }
         }
      }
      Collections.sort(proposals, PROPOSAL_COMPARATOR);
      ICompletionProposal[] result = (ICompletionProposal[]) proposals
            .toArray(new ICompletionProposal[proposals.size()]);
      return result;
   }

   /**
    * @param string
    * @param i
    * @return May be <code>null</code>.
    */
   private ICompletionProposal[] getAttributesProposals(IDocument doc,
         String aPrefix, int anOffset)
   {
      String string = null;
      try
      {
         ITypedRegion partition = doc.getPartition(anOffset);
         int i = partition.getOffset();
         int b = partition.getLength();
         string = doc.get(i, b);
      }
      catch (BadLocationException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      String s = findMatchingTagIdentifierBefore(anOffset, doc);
      List variables = null;
      if (s != null)
      {
         DTDElement element = VelocityEditor.getHTMLElement(s.toLowerCase());
         if (element == null)
            return null;
         Hashtable table = element.attributes;
         Collection collection = table.values();
         variables = new ArrayList();
         if (collection.size() > 0)
         {
            for (Iterator iter = collection.iterator(); iter.hasNext();)
            {
               DTDAttribute attrib = (DTDAttribute) iter.next();
               if (FLAT_VALUEs_SET.contains(attrib.getName()))
               {
                  if ((string != null)
                        && (string.indexOf(attrib.getName() + " ") == -1))
                  {
                     variables.add(attrib.getName());
                  }
               }
               else
               {
                  if ((string != null)
                        && (string.indexOf(attrib.getName() + "=") == -1))
                  {
                     variables.add(attrib.getName());
                  }
               }
            }
         }
      }
      ICompletionProposal[] result = null;
      CompletionProposal proposal = null;
      if (variables != null)
      {
         List proposals = new ArrayList();
         Iterator iter = variables.iterator();
         while (iter.hasNext())
         {
            String variable = (String) iter.next();
            String append = "=\"\"";
            int cur = 2;
            if (variable.startsWith(aPrefix.toLowerCase()))
            {
               if (FLAT_VALUEs_SET.contains(variable.toLowerCase()))
               {
                  append = " ";
                  cur = 1;
               }
               proposal = new CompletionProposal(variable + append, anOffset,
                     aPrefix.length(), variable.length() + cur, null, variable,
                     null, null);
               proposals.add(proposal);
            }
         }
         Collections.sort(proposals, PROPOSAL_COMPARATOR);
         result = (ICompletionProposal[]) proposals
               .toArray(new ICompletionProposal[proposals.size()]);
      }
      return result;
   }

   private ICompletionProposal[] getAttributeValuesProposals(IDocument doc,
         String aPrefix, int anOffset)
   {
      String s = findMatchingTagIdentifierBefore(anOffset - 2, doc);
      if (s == null)
         return new ICompletionProposal[0];
      String attribute = getAttribute(doc, anOffset - 3);
      DTDElement element = VelocityEditor.getHTMLElement(s.toLowerCase());
      Hashtable table = element.attributes;
      Collection collection = table.values();
      Set variables = new HashSet();
      if (collection.size() > 0)
      {
         for (Iterator iter = collection.iterator(); iter.hasNext();)
         {
            DTDAttribute attrib = (DTDAttribute) iter.next();
            if (attrib.getName().equalsIgnoreCase(attribute))
            {
               dumpAttribute(attrib, variables);
            }
         }
      }
      ICompletionProposal[] result = null;
      CompletionProposal proposal = null;
      if (!variables.isEmpty())
      {
         List proposals = new ArrayList();
         Iterator iter = variables.iterator();
         while (iter.hasNext())
         {
            String variable = (String) iter.next();
            if (variable.startsWith(aPrefix.toLowerCase()))
            {
               proposal = new CompletionProposal(variable + "\" ", anOffset,
                     aPrefix.length() + 1, variable.length() + 2, null,
                     variable, null, null);
               proposals.add(proposal);
            }
         }
         Collections.sort(proposals, PROPOSAL_COMPARATOR);
         result = (ICompletionProposal[]) proposals
               .toArray(new ICompletionProposal[proposals.size()]);
      }
      return result;
   }

   private Set extractWords(String input)
   {
      StringTokenizer stringTokenizer = new StringTokenizer(input,
            "#=:?\t <>,;()$.'}{][&%!*-/\\\n\"\'");
      Set set = new HashSet();
      while (stringTokenizer.hasMoreTokens())
      {
         set.add(stringTokenizer.nextToken());
      }
      return set;
   }

   private ICompletionProposal[] getWordsProposals(IDocument doc,
         String aPrefix, int anOffset)
   {
      if (aPrefix == null || aPrefix.length() == 0)
         return null;
      ICompletionProposal[] result = null;
      CompletionProposal proposal = null;
      Set variables = extractWords(doc.get());
      if (!variables.isEmpty())
      {
         List proposals = new ArrayList();
         Iterator iter = variables.iterator();
         while (iter.hasNext())
         {
            String variable = (String) iter.next();
            if (variable.startsWith(aPrefix) && !variable.equals(aPrefix))
            {
               proposal = new CompletionProposal(variable, anOffset, aPrefix
                     .length(), variable.length(), null, variable, null, null);
               proposals.add(proposal);
            }
         }
         if (proposals.isEmpty())
            return null;
         Collections.sort(proposals, PROPOSAL_COMPARATOR);
         result = (ICompletionProposal[]) proposals
               .toArray(new ICompletionProposal[proposals.size()]);
      }
      return result;
   }

   protected int findMatchingOpenTagBefore(int end, IDocument doc)
   {
      int level = 1;
      try
      {
         char prev = '\0';
         for (end--; end >= 0; end--)
         {
            if (EditorsUtil.isInsidePartition(end,
                  VelocityConfiguration.CDATA_PARTITIONS, true, doc))
            {
               end = doc.getPartition(end).getOffset() - 1;
            }
            else if (EditorsUtil.isInsidePartition(end,
                  VelocityConfiguration.ESCAPED_PARTITIONS, doc))
            {
               end = doc.getPartition(end).getOffset();
            }
            char c = doc.getChar(end);
            if (c == '<')
            {
               if (prev == '/')
               {
                  level++;
               }
               else if ((prev != '!') && (prev != '?') && (prev != '%')
                     && (prev != '#'))
               {
                  String id = getIdentifier(doc, end + 1, doc.getLength());
                  if (!fEMPTY_TAG_SET.contains(id.toUpperCase()))
                  {
                     level--;
                  }
                  if (level == 0)
                  {
                     return end;
                  }
               }
            }
            else if ((c == '"') || (c == '\''))
            {
               end = getStringStart(doc, end, c);
            }
            prev = c;
         }
      }
      catch (BadLocationException e)
      {
      }
      return -1;
   }

   private ICompletionProposal[] getTAGCloseProposals(IDocument doc,
         String aPrefix, int anOffset)
   {
      String closetag = ">";
      String s = findMatchingOpenTagIdentifierBefore(anOffset - 2, doc);
      int lasttagOffset = findMatchingOpenTagBefore(anOffset - 2, doc);
      // // int indent = 0;
      //
      // try
      // {
      // // indent = getIndentOfLine(doc, doc.getLineOfOffset(lasttagOffset));
      // }
      // catch (BadLocationException e)
      // {
      // // TODO Auto-generated catch block
      // e.printStackTrace();
      // }
      int start = 0;
      try
      {
         start = doc.getLineOffset(doc.getLineOfOffset(anOffset));
      }
      catch (BadLocationException e2)
      {
         // TODO Auto-generated catch block
         e2.printStackTrace();
      }
      String ddd = null;
      int dlength = 0;
      try
      {
         ddd = sameIndentAs(lasttagOffset, doc.getLineOfOffset(anOffset), doc,
               anOffset, s + closetag);
         dlength = ddd.length();
      }
      catch (BadLocationException e1)
      {
         // TODO Auto-generated catch block
         e1.printStackTrace();
      }
      // int curPo = start + dlength;
      ICompletionProposal[] result = null;
      String[] tags = new String[]
      {"A", "ADDRESS", "APPLET", "B", "BIG", "BLOCKQUOTE", "BODY", "CAPTION",
            "CENTER", "CITE", "CODE", "COL", "COLGROUP", "DD", "DFN", "DIR",
            "DIV", "DL", "DT", "EM", "FONT", "FORM", "FRAMESET", "H1", "H2",
            "H3", "H4", "H5", "H6", "HEAD", "HTML", "I", "KBD", "LI", "MAP",
            "MENU", "NOBR", "NOFRAMES", "OBJECT", "OL", "OPTION", "P", "PRE",
            "SAMP", "SCRIPT", "SELECT", "SMALL", "SPAN", "STRIKE", "S",
            "STRONG", "STYLE", "SUB", "SUP", "TABLE", "TD", "TEXTAREA", "TH",
            "TITLE", "TR", "TT", "U", "UL", "VAR"};
      List variables = Arrays.asList(tags);
      List proposals = new ArrayList();
      CompletionProposal proposal = null;
      if ((s != null) && (s.length() > 0))
      {
         proposal = new CompletionProposal(ddd, start, aPrefix.length()
               + (anOffset - start), dlength, null, s, null, "test3");
         proposals.add(proposal);
         result = (ICompletionProposal[]) proposals
               .toArray(new ICompletionProposal[proposals.size()]);
      }
      else if (!variables.isEmpty())
      {
         Iterator iter = variables.iterator();
         String variable = null;
         while (iter.hasNext())
         {
            variable = (String) iter.next();
            if (variable.startsWith(aPrefix.toUpperCase()))
            {
               String opentag = variable;
               proposal = new CompletionProposal(opentag + closetag, anOffset,
                     aPrefix.length(), variable.length() + 1, null, variable,
                     null, "test3");
               proposals.add(proposal);
            }
         }
         Collections.sort(proposals, PROPOSAL_COMPARATOR);
         result = (ICompletionProposal[]) proposals
               .toArray(new ICompletionProposal[proposals.size()]);
      }
      return result;
   }

   /**
    * @param string
    * @param i
    * @return
    */
   private ICompletionProposal[] getTAGProposals(IDocument doc, String aPrefix,
         int anOffset)
   {
      Set list = new HashSet();
      if (anOffset > 1)
      {
         String s = findMatchingOpenTagIdentifierBefore(anOffset - 1, doc);
         if (s != null)
         {
            DTDElement element = VelocityEditor.getHTMLElement(s.toLowerCase());
            if (element != null)
            {
               DTDItem item = element.getContent();
               list = new HashSet();
               dumpDTDItem(item, list);
            }
         }
      }
      if (list.isEmpty())
      {
         list = null;
      }
      ICompletionProposal[] result = null;
      Set variables = null;
      if (list != null)
      {
         variables = list;
      }
      else
      {
         variables = TAG_SET;
      }
      CompletionProposal proposal = null;
      if (!variables.isEmpty())
      {
         List proposals = new ArrayList();
         Iterator iter = variables.iterator();
         while (iter.hasNext())
         {
            String variable = (String) iter.next();
            if (variable.startsWith(aPrefix.toUpperCase()))
            {
               String closetag = " />";
               if (!fEMPTY_TAG_SET.contains(variable))
               {
                  int lineOfOffset = 0;
                  try
                  {
                     lineOfOffset = doc.getLineOfOffset(anOffset);
                  }
                  catch (BadLocationException e)
                  {
                     // TODO Auto-generated catch block
                     e.printStackTrace();
                  }
                  closetag = " >\n"
                        + getIndentString(getIndentOfLine(doc, lineOfOffset))
                        + "</" + variable + ">";
               }
               int compromize = 1;
               String opentag = variable + closetag;
               if (!VelocityPlugin.getDefault().isUppercaseEnabled())
                  opentag = opentag.toLowerCase();
               proposal = new CompletionProposal(opentag, anOffset, aPrefix
                     .length(), variable.length() + compromize, null, variable,
                     null, null);
               proposals.add(proposal);
            }
         }
         Collections.sort(proposals, PROPOSAL_COMPARATOR);
         result = (ICompletionProposal[]) proposals
               .toArray(new ICompletionProposal[proposals.size()]);
      }
      return result;
   }

   /**
    * Returns proposals from all directives with given prefix.
    */
   private ICompletionProposal[] getDirectiveProposals(String aPrefix,
         int anOffset)
   {
      List proposals = new ArrayList();
      // Add system directives
      String[] directives = Directive.DIRECTIVES;
      for (int i = directives.length - 1; i >= 0; i--)
      {
         String directive = directives[i];
         if (directive.substring(1).startsWith(aPrefix))
         {
            int cursorPos;
            if ((i == Directive.TYPE_ELSE) || (i == Directive.TYPE_END)
                  || (i == Directive.TYPE_STOP))
            {
               cursorPos = directive.length() - 1;
            }
            else
            {
               directive += "()";
               cursorPos = directive.length() - 2;
            }
            proposals.add(new CompletionProposal(directive.substring(1),
                  anOffset, aPrefix.length(), cursorPos, VelocityPluginImages
                        .get(VelocityPluginImages.IMG_OBJ_SYSTEM_DIRECTIVE),
                  directive, null, null));
         }
      }
      // Add predefined macros
      List<String> predefMacros = fEditor.fMacroCompletions;
      if (predefMacros != null)
      {
         for(String macro : predefMacros)
         {
            if (macro.substring(1).startsWith(aPrefix))
            {
               int cursorPos = macro.length() - 2;
               proposals.add(new CompletionProposal(macro.substring(1),
                     anOffset, aPrefix.length(), cursorPos, VelocityPluginImages
                           .get(VelocityPluginImages.IMG_OBJ_SYSTEM_DIRECTIVE),
                           macro, null, null));
            }
         }
      }
      // Add Velocity library macros
      Iterator macros = VelocityEditorEnvironment.getParser()
            .getLibraryMacros().iterator();
      while (macros.hasNext())
      {
         VelocityMacro macro = ((VelocityMacro) macros.next());
         String name = macro.getName();
         if (name.startsWith(aPrefix))
         {
            String insert = name + "()";
            int cursorPos;
            StringBuffer buffer = new StringBuffer();
            buffer.append('#');
            buffer.append(name);
            buffer.append('(');
            if (macro.getArguments().length == 1)
            {
               cursorPos = insert.length();
               buffer.append(')');
            }
            else
            {
               cursorPos = insert.length() - 1;
               String[] args = macro.getArguments();
               for (int i = 1; i < args.length; i++)
               {
                  buffer.append('$');
                  buffer.append(args[i]);
                  if (i < (args.length - 1))
                  {
                     buffer.append(" ");
                  }
               }
               buffer.append(')');
            }
            buffer.append(" - ");
            buffer.append(macro.getTemplate());
            proposals.add(new CompletionProposal(insert, anOffset, aPrefix
                  .length(), cursorPos, VelocityPluginImages
                  .get(VelocityPluginImages.IMG_OBJ_MACRO), buffer.toString(),
                  null, null));
         }
      }
      // Add user directives
      Iterator userDirectives = VelocityEditorEnvironment.getParser()
            .getUserDirectives().iterator();
      while (userDirectives.hasNext())
      {
         String directive = ((String) userDirectives.next());
         if (directive.substring(1).startsWith(aPrefix))
         {
            directive += "()";
            int cursorPos = directive.length() - 1;
            proposals.add(new CompletionProposal(directive.substring(1),
                  anOffset, aPrefix.length(), cursorPos, VelocityPluginImages
                        .get(VelocityPluginImages.IMG_OBJ_USER_DIRECTIVE),
                  directive, null, null));
         }
      }
      Collections.sort(proposals, PROPOSAL_COMPARATOR);
      return (ICompletionProposal[]) proposals
            .toArray(new ICompletionProposal[proposals.size()]);
   }

   /**
    * Returns proposals from all variables with given prefix.
    */
   private ICompletionProposal[] getVariableProposals(String aPrefix,
         int anOffset)
   {
      anOffset = IdUtils.skipToIdStart(anOffset, fEditor.getDocument());
      ICompletionProposal[] result = null;
      List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
      List variables = fEditor.getVariables(fEditor.getLine(anOffset));
      if (!variables.isEmpty())
      {

         Iterator iter = variables.iterator();
         while (iter.hasNext())
         {
            String variable = (String) iter.next();
            if (variable.substring(1).startsWith(aPrefix))
            {
               proposals.add(new CompletionProposal(variable.substring(1),
                     anOffset, aPrefix.length(), variable.length() - 1, null,
                     variable, null, null));
            }
         }
      }
      for (String[] completion : fEditor.fVariableCompletions)
      {
         String name = completion[0];
         String replacement = name;
         int end = IdUtils.findEndOfIdentifier(fEditor.getDocument(),
               anOffset + 1, false);
         int delta = end - anOffset;
         if (replacement.startsWith(aPrefix))
         {
            String type = completion.length > 1 ? completion[1] : "";
            String desc = completion.length > 2 ? completion[2] : name;

            if (type.trim().length() > 0)
            {
               desc = "[" + type + "] " + desc;
            }

            CompletionProposal prop = new CompletionProposal(replacement,
                  anOffset, delta, replacement.length(), null, "$" + name
                        + "   " + desc, null, null);

            proposals.add(prop);
         }
      }
      for (Object[] completion : fEditor.fMethodCompletions)
      {
         String name = (String) completion[0];
         String replacement = name;
         int end = IdUtils.findEndOfIdentifier(fEditor.getDocument(),
               anOffset + 1, true);
         int delta = end - anOffset;
         if (!aPrefix.startsWith("$"))
         {
            aPrefix = "$" + aPrefix;
         }
         if (replacement.startsWith(aPrefix))
         {
            String type = (String) (completion.length > 1 ? completion[1] : "");
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
                  anOffset - 1, delta + 1, replacement.length(), null, name
                        + "   " + desc, null, desc);

            proposals.add(prop);
         }
      }
      Collections.sort(proposals, PROPOSAL_COMPARATOR);
      result = (ICompletionProposal[]) proposals
            .toArray(new ICompletionProposal[proposals.size()]);
      return result;
   }

   /**
    * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#computeContextInformation(org.eclipse.jface.text.ITextViewer,
    *      int)
    */
   public IContextInformation[] computeContextInformation(ITextViewer viewer,
         int documentOffset)
   {
      return null;
   }

   /**
    * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getCompletionProposalAutoActivationCharacters()
    */
   public char[] getCompletionProposalAutoActivationCharacters()
   {
      return new char[]
      {'#', '$', '<', '/'};
   }

   /**
    * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getContextInformationAutoActivationCharacters()
    */
   public char[] getContextInformationAutoActivationCharacters()
   {
      return (new char[]
      {'>'});
   }

   /**
    * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getContextInformationValidator()
    */
   public IContextInformationValidator getContextInformationValidator()
   {
      return null;
   }

   /**
    * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getErrorMessage()
    */
   public String getErrorMessage()
   {
      return null;
   }

   protected String findMatchingOpenTagIdentifierBefore(int end, IDocument doc)
   {
      int level = 1;
      String identifier = null;
      try
      {
         char prev = '\0';
         for (end--; end >= 0; end--)
         {
            if (EditorsUtil.isInsidePartition(end,
                  VelocityConfiguration.CDATA_PARTITIONS, true, doc))
            {
               end = doc.getPartition(end).getOffset() - 1;
            }
            // else if (EditorsUtil.isInsidePartition(end,
            // VelocityConfiguration.ESCAPED_PARTITIONS, doc))
            // {
            // end = doc.getPartition(end).getOffset();
            // }
            char c = doc.getChar(end);
            if (c == '<')
            {
               if (prev == '/')
               {
                  level++;
               }
               else if ((prev != '!') && (prev != '?') && (prev != '%')
                     && (prev != '#'))
               {
                  String id = getIdentifier(doc, end + 1, doc.getLength());
                  if (!fEMPTY_TAG_SET.contains(id.toUpperCase()))
                  {
                     level--;
                  }
                  if (level == 0)
                  {
                     identifier = id;
                     return identifier;
                  }
               }
            }
            else if ((c == '"') || (c == '\''))
            {
               end = getStringStart(doc, end, c);
            }
            prev = c;
         }
      }
      catch (BadLocationException e)
      {
      }
      return identifier;
   }

   protected String findMatchingTagIdentifierBefore(int end, IDocument doc)
   {
      int level = 1;
      String identifier = null;
      try
      {
         char prev = '\0';
         for (end--; end >= 0; end--)
         {
            if (EditorsUtil.isInsidePartition(end,
                  VelocityConfiguration.CDATA_PARTITIONS, true, doc))
            {
               end = doc.getPartition(end).getOffset() - 1;
            }
            else if (EditorsUtil.isInsidePartition(end,
                  VelocityConfiguration.ESCAPED_PARTITIONS, doc))
            {
               end = doc.getPartition(end).getOffset();
            }
            char c = doc.getChar(end);
            if (c == '<')
            {
               if (prev == '/')
               {
                  level++;
               }
               else if ((prev != '!') && (prev != '?') && (prev != '%')
                     && (prev != '#'))
               {
                  String id = getIdentifier(doc, end + 1, doc.getLength());
                  identifier = id;
                  return identifier;
               }
            }
            else if ((c == '"') || (c == '\''))
            {
               end = getStringStart(doc, end, c);
            }
            prev = c;
         }
      }
      catch (BadLocationException e)
      {
      }
      return identifier;
   }

   protected int getStringStart(IDocument document, int end, char quote)
   {
      try
      {
         for (end--; end >= 0; end--)
         {
            char c = document.getChar(end);
            if (c == quote)
            {
               if ((end == 0) || (document.getChar(end - 1) != '\\'))
               {
                  return end;
               }
               end--;
            }
         }
      }
      catch (BadLocationException e)
      {
      }
      return -1;
   }

   private String getIdentifier(IDocument document, int start, int end)
   {
      start = skipWhitespace(document, start, end);
      if (start >= end)
      {
         return "";
      }
      StringBuffer ret = new StringBuffer();
      try
      {
         char c = document.getChar(start++);
         if (isIdentifierStart(c))
         {
            while (start < end)
            {
               ret.append(c);
               c = document.getChar(start++);
               if (!isIdentifierChar(c))
               {
                  break;
               }
            }
         }
      }
      catch (BadLocationException e)
      {
      }
      return ret.toString();
   }

   private String getAttribute(IDocument document, int offset)
   {
      StringBuffer ret = new StringBuffer();
      try
      {
         char c = document.getChar(offset--);
         if (Character.isLetter(c))
         {
            while ((c != ' ') && Character.isLetter(c))
            {
               ret.append(c);
               c = document.getChar(offset--);
            }
         }
      }
      catch (BadLocationException e)
      {
      }
      return ret.reverse().toString();
   }

   private int skipWhitespace(IDocument document, int start, int end)
   {
      try
      {
         for (char c = document.getChar(start); (start < end)
               && Character.isWhitespace(c); c = document.getChar(++start))
         {
         }
      }
      catch (BadLocationException e)
      {
      }
      return start;
   }

   private boolean isIdentifierStart(char c)
   {
      return Character.isLetter(c);
   }

   private boolean isIdentifierChar(char c)
   {
      return Character.isLetterOrDigit(c) || c == '.';
   }

   protected int getIndentOfLine(IDocument d, int line)
   {
      try
      {
         if (line >= 0)
         {
            int start = d.getLineOffset(line);
            int end = (start + d.getLineLength(line)) - 1;
            int whiteend = findEndOfWhiteSpace(d, start, end);
            return indentWidthOf(d.get(start, whiteend - start), fTabWidth);
         }
         else
         {
            return 0;
         }
      }
      catch (Exception e)
      {
         return 0;
      }
   }

   /**
    * DOCUMENT ME!
    * 
    * @param str DOCUMENT ME!
    * @param tabwidth DOCUMENT ME!
    * 
    * @return DOCUMENT ME!
    */
   public static int indentWidthOf(String str, int tabwidth)
   {
      return indentWidthOf(str, 0, tabwidth);
   }

   /**
    * DOCUMENT ME!
    * 
    * @param str DOCUMENT ME!
    * @param start DOCUMENT ME!
    * @param tabwidth DOCUMENT ME!
    * 
    * @return DOCUMENT ME!
    */
   public static int indentWidthOf(String str, int start, int tabwidth)
   {
      int ret = 0;
      for (int len = str.length(); start < len; start++)
      {
         char c = str.charAt(start);
         if (c == '\t')
         {
            ret = ((ret / tabwidth) + 1) * tabwidth;
            continue;
         }
         if ((c == ' ') || (c == '\240'))
         {
            ret++;
            continue;
         }
         if ((c != '\n') && (c != '\r'))
         {
            break;
         }
         ret = 0;
      }
      return ret;
   }

   protected int findEndOfWhiteSpace(IDocument document, int offset, int end)
         throws BadLocationException
   {
      while (offset < end)
      {
         char c = document.getChar(offset);
         if ((c != ' ') && (c != '\t'))
         {
            return offset;
         }
         offset++;
      }
      return end;
   }

   protected StringBuffer getIndentString(int indent)
   {
      StringBuffer ret = new StringBuffer();
      int n = indent / fTabWidth;
      for (int i = 0; i < n; i++)
      {
         ret.append("\t");
      }
      n = indent - (n * fTabWidth);
      for (int i = 0; i < n; i++)
      {
         ret.append(' ');
      }
      return ret;
   }

   protected String sameIndentAs(int matchoffset, int line, IDocument document,
         int actualOffset, String variable)
   {
      try
      {
         int indent = getIndentOfLine(document, document
               .getLineOfOffset(matchoffset));
         StringBuffer buf = getIndentString(indent);
         int start = (line < 0) ? 0 : document.getLineOffset(line);
         int whiteend = findEndOfWhiteSpace(document, start, actualOffset);
         buf.append(document.get(whiteend, actualOffset - whiteend));
         buf.append(variable);
         return buf.toString();
      }
      catch (BadLocationException e)
      {
      }
      return null;
   }

   /**
    * DOCUMENT ME!
    * 
    * @param item DOCUMENT ME!
    * @param list DOCUMENT ME!
    */
   public static void dumpDTDItem(DTDItem item, Set list)
   {
      if (item == null)
      {
         return;
      }
      else if (item instanceof DTDName)
      {
         list.add(((DTDName) item).value.toUpperCase());
      }
      else if (item instanceof DTDChoice)
      {
         DTDItem[] items = ((DTDChoice) item).getItems();
         for (int i = 0; i < items.length; i++)
         {
            dumpDTDItem(items[i], list);
         }
      }
      else if (item instanceof DTDSequence)
      {
         DTDItem[] items = ((DTDSequence) item).getItems();
         for (int i = 0; i < items.length; i++)
         {
            dumpDTDItem(items[i], list);
         }
      }
      else if (item instanceof DTDMixed)
      {
         DTDItem[] items = ((DTDMixed) item).getItems();
         for (int i = 0; i < items.length; i++)
         {
            dumpDTDItem(items[i], list);
         }
      }
   }

   /**
    * DOCUMENT ME!
    * 
    * @param attr DOCUMENT ME!
    * @param list DOCUMENT ME!
    */
   public static void dumpAttribute(DTDAttribute attr, Set list)
   {
      if (attr.type instanceof DTDEnumeration)
      {
         String[] items = ((DTDEnumeration) attr.type).getItems();
         for (int i = 0; i < items.length; i++)
         {
            list.add(items[i]);
         }
      }
      else if (attr.type instanceof DTDNotationList)
      {
         String[] items = ((DTDNotationList) attr.type).getItems();
         for (int i = 0; i < items.length; i++)
         {
            list.add(items[i]);
         }
      }
   }

   protected Template[] getTemplates(String contextTypeId)
   {
      return TemplateEditorUI.getDefault().getTemplateStore().getTemplates();
   }

   /**
    * Return the XML context type that is supported by this plugin.
    */
   // protected ContextType getContextType(ITextViewer viewer, IRegion region)
   // {
   // return
   // TemplateEditorUI.getDefault().getContextTypeRegistry().getContextType(VeloContextType.XML_CONTEXT_TYPE);
   // }
   /**
    * Always return the default image.
    */
   protected Image getImage(Template template)
   {
      ImageRegistry registry = TemplateEditorUI.getDefault().getImageRegistry();
      Image image = registry.get(DEFAULT_IMAGE);
      if (image == null)
      {
         ImageDescriptor desc = TemplateEditorUI.imageDescriptorFromPlugin(
               "de.byteaction.velocity.ui", DEFAULT_IMAGE); //$NON-NLS-1$
         registry.put(DEFAULT_IMAGE, desc);
         image = registry.get(DEFAULT_IMAGE);
      }
      return image;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.jface.text.templates.TemplateCompletionProcessor#getContextType(org.eclipse.jface.text.ITextViewer,
    *      org.eclipse.jface.text.IRegion)
    */
   protected TemplateContextType getContextType(ITextViewer viewer,
         IRegion region)
   {
      return TemplateEditorUI.getDefault().getContextTypeRegistry()
            .getContextType(VeloContextType.XML_CONTEXT_TYPE);
      // return null;
   }
}