/******************************************************************************
 *
 * [ PSTemplateOutputComposite.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.controls;

import com.percussion.E2Designer.PSDlgUtil;
import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSModelException;
import com.percussion.client.PSMultiOperationException;
import com.percussion.client.PSObjectType;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.PSObjectTypes.TemplateSubTypes;
import com.percussion.client.models.IPSCmsModel;
import com.percussion.client.objectstore.PSUiAssemblyTemplate;
import com.percussion.extension.PSExtensionDef;
import com.percussion.services.assembly.IPSAssembler;
import com.percussion.services.assembly.IPSAssemblyTemplate.GlobalTemplateUsage;
import com.percussion.services.assembly.IPSAssemblyTemplate.OutputFormat;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.PSWorkbenchPlugin;
import com.percussion.workbench.ui.util.PSErrorDialog;
import org.apache.commons.lang.ArrayUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static com.percussion.extension.IPSExtension.BINARY_ASSEMBLER;
import static com.percussion.extension.IPSExtension.DATABASE_ASSEMBLER;
import static com.percussion.extension.IPSExtension.DEBUG_ASSEMBLER;
import static com.percussion.extension.IPSExtension.DISPATCH_ASSEMBLER;
import static com.percussion.extension.IPSExtension.KNOWN_ASSEMBLERS;
import static com.percussion.extension.IPSExtension.LEGACY_ASSEMBLER;
import static com.percussion.extension.IPSExtension.VELOCITY_ASSEMBLER;
import static com.percussion.workbench.ui.IPSUiConstants.LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET;
import static com.percussion.workbench.ui.IPSUiConstants.LABEL_HSPACE_OFFSET;
import static com.percussion.workbench.ui.IPSUiConstants.LABEL_VSPACE_OFFSET;

/**
 * Provides user with ability to specify template output format.
 *
 * @author Andriy Palamarchuk
 */
public class PSTemplateOutputComposite extends Composite
{
   /**
    * Creates new control.
    * @param narrow indicates whether the composite should use narrow layout.
    * @param horizontal indicates to show options in horizontal configuration.
    * In this configuration global templates are not shown.
    * @param showAssemblers whether to show assembler selection UI.
    * @param showLegacyGlobalTemplateUsage whether to show "Legacy" global
    * template usage selection.
    */
   public PSTemplateOutputComposite(Composite parent, int style,
         final boolean narrow, boolean horizontal, final boolean showDbOutput,
         final boolean showAssemblers,
         final boolean showLegacyGlobalTemplateUsage)
   {
      super(parent, style);
      m_showDbOutput = showDbOutput;
      m_showGlobalTemplates = !horizontal;
      m_showAssemblers = showAssemblers;
      m_showLegacyGlobalTemplateUsage = showLegacyGlobalTemplateUsage;
      setLayout(new FormLayout());

      try
      {
         initAssemblers();
         initGlobalTemplates();
      }
      catch (PSModelException e)
      {
         PSWorkbenchPlugin.handleException(null, null, null, e);
      }
      catch (PSMultiOperationException e)
      {
         PSWorkbenchPlugin.handleException(null, null, null, e);
      }

      if (m_showAssemblers)
      {
         m_assemblerCombo = createAssemblerComboWithLabel(narrow);
         initAssemblerComboContent();
         m_assemblerCombo.addSelectionListener(new AssemblerComboSelectionListener());
      }

      m_formatRadio = createFormatRadioComposite(
            new FormAttachment(m_assemblerCombo, LABEL_VSPACE_OFFSET), horizontal);
      m_formatRadio.addEntry(FormatChoice.SNIPPET.getLabel());

      addPageFormatEntry();

      m_formatRadio.addEntry(FormatChoice.BINARY.getLabel());
      if (showDbOutput)
      {
         m_formatRadio.addEntry(FormatChoice.DATABASE.getLabel());
      }
      m_formatRadio.layoutControls();
      assert m_formatRadio.getButtonCount() == getShownFormats().length;
      
      makeInitialSelection();
   }
   
   /**
    * Adds an entry into {@link #m_formatRadio} for page format. 
    */
   private void addPageFormatEntry()
   {
      if (m_showGlobalTemplates)
      {
         m_globalTemplateRadio = createGlobalTemplateRadioComposite(m_formatRadio);
         m_globalTemplateRadio.addEntry(
               getMessage("PSTemplateOutputComposite.globalTemplate.default")); //$NON-NLS-1$
         m_globalTemplateRadio.addEntry(
               getMessage("PSTemplateOutputComposite.globalTemplate.specified"), //$NON-NLS-1$
               null,  getGlobalTemplateNames(), null, true);
         m_globalTemplateRadio.addEntry(
               getMessage("PSTemplateOutputComposite.globalTemplate.none")); //$NON-NLS-1$
         if (m_showLegacyGlobalTemplateUsage)
         {
            m_globalTemplateRadio.addEntry(
                  getMessage("PSTemplateOutputComposite.globalTemplate.legacy")); //$NON-NLS-1$
         }
         m_globalTemplateRadio.layoutControls();
         assert m_globalTemplateRadio.getButtonCount() ==
               GlobalTemplateChoice.values().length -
               (m_showLegacyGlobalTemplateUsage ? 0 : 1);
         
         m_formatRadio.addEntry(FormatChoice.PAGE.getLabel(), null, m_globalTemplateRadio);
      }
      else
      {
         m_formatRadio.addEntry(FormatChoice.PAGE.getLabel());
      }
   }

   /**
    * Sets up default control selection.
    */
   private void makeInitialSelection()
   {
      maybeSelectVelocityAssembler();
      if (m_showGlobalTemplates)
      {
         m_globalTemplateRadio.setSelection(GlobalTemplateChoice.DEFAULT.ordinal());
         makeDefaultGlobalTemplatesSelection();
      }
      m_formatRadio.setSelection(0);
   }

   /**
    * Configures initial state for assembler selection UI - selects velocity
    * assembler. If velocity assembler does not exist selects first assembler
    * in the list available.
    */
   private void maybeSelectVelocityAssembler()
   {
      if (!m_showAssemblers || m_assemblerRefs.isEmpty())
      {
         return;
      }
      maybeSelectAssembler(VELOCITY_ASSEMBLER);
   }

   /**
    * Selects the specified assembler.
    */
   void maybeSelectAssembler(final String assemblerName)
   {
      int selection = 0;
      int idx = 0;
      for (final IPSReference ref : m_assemblerRefs)
      {
         if (ref.getName().equals(assemblerName))
         {
            selection = idx;
            break;
         }
         idx++;
      }
      m_assemblerCombo.select(selection);
      assemblerComboSelectionChanged();
   }

   /**
    * Regurns global templates combo.
    */
   Combo getGlobalTemplatesCombo()
   {
      return (Combo) m_globalTemplateRadio.getNestedControl(
            GlobalTemplateChoice.DEFINED.ordinal());
   }

   /**
    * Configures initial state for global template selection UI.
    */
   private void makeDefaultGlobalTemplatesSelection()
   {
      if (!m_showGlobalTemplates)
      {
         return;
      }
      if (m_globalTemplateRefs.isEmpty())
      {
         disableGlobalTemplateChoices(
               Collections.singletonList(GlobalTemplateChoice.DEFINED));
      }
      else
      {
         getGlobalTemplatesCombo().select(0);
      }
   }

   /**
    * Creates group of radio buttons to select global template.
    */
   private PSRadioAndCheckBoxes createGlobalTemplateRadioComposite(
         final Composite container)
   {
      return new PSRadioAndCheckBoxes(container,
            getMessage("PSTemplateOutputComposite.label.globalTemplate"), //$NON-NLS-1$
               SWT.SEPARATOR | SWT.VERTICAL | SWT.RADIO);
   }

   /**
    * Creates drop list to select assembler.
    * @param narrow whether composite should have narrow layout. 
    */
   private Combo createAssemblerComboWithLabel(boolean narrow)
   {
      final Label assemblerLabel = new Label(this, SWT.NONE);
      assemblerLabel.setText(ASSEMBLER_COMBO_LABEL + ':');
      {
         final FormData formData = new FormData();
         formData.left = new FormAttachment(0, 0);
         formData.top = new FormAttachment(
               narrow ? 0 : LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, 0);
         assemblerLabel.setLayoutData(formData);
      }

      final Combo combo = new Combo(this, SWT.READ_ONLY);
      combo.setLayoutData(createAssemblerComboAttachment(narrow, assemblerLabel));
      return combo;
   }

   /**
    * Creates an attachment of assembler combo to the assembler label and
    * container basing on whether configuration should be narrow. 
    */
   private FormData createAssemblerComboAttachment(boolean narrow, final Label assemblerLabel)
   {
      if (narrow)
      {
         final FormData formData = new FormData();
         formData.left = new FormAttachment(0, 0);
         formData.right = new FormAttachment(100, 0);
         formData.top = new FormAttachment(assemblerLabel, 0, SWT.BOTTOM);
         return formData;
      }
      else
      {
         final FormData formData = new FormData();
         formData.left = new FormAttachment(assemblerLabel, LABEL_HSPACE_OFFSET, SWT.RIGHT);
         formData.right = new FormAttachment(100, 0);
         formData.top = new FormAttachment(assemblerLabel,
               -LABEL_ALIGN_WITH_CONTROL_TEXT_VSPACE_OFFSET, SWT.TOP);
         return formData;
      }
   }

   /**
    * Creates group of radio buttons and other controls to select output format.
    */
   private PSRadioAndCheckBoxes createFormatRadioComposite(
         final FormAttachment topAttachment, boolean horizontal)
   {
      final int directionStyle = horizontal ? SWT.HORIZONTAL : SWT.VERTICAL;
      final PSRadioAndCheckBoxes radio =
            new PSRadioAndCheckBoxes(this,
                  OUTPUT_LABEL,
                  SWT.SEPARATOR | directionStyle | SWT.RADIO);
      final FormData formData = new FormData();
      formData.left = new FormAttachment(0, 0);
      formData.right = new FormAttachment(100, 0);
      formData.top = topAttachment;
      radio.setLayoutData(formData);
      return radio;
   }

   /**
    * Is called when assembler combo selection changed.
    */
   protected void assemblerComboSelectionChanged()
   {
      enableAllFormats();
      makeDefaultGlobalTemplatesSelection();
      final List<FormatChoice> formats = new ArrayList<FormatChoice>(
            Arrays.asList(getShownFormats()));
      if (getSelectedAssembler().equals(VELOCITY_ASSEMBLER))
      {
         formats.remove(FormatChoice.SNIPPET);
         formats.remove(FormatChoice.PAGE);
         disableFormatChoices(formats);
      }
      else if (getSelectedAssembler().equals(BINARY_ASSEMBLER))
      {
         formats.remove(FormatChoice.BINARY);
         disableFormatChoices(formats);
      }
      else if (getSelectedAssembler().equals(DISPATCH_ASSEMBLER))
      {
         final List<GlobalTemplateChoice> choices =
               new ArrayList<GlobalTemplateChoice>();
         choices.add(GlobalTemplateChoice.DEFAULT);
         choices.add(GlobalTemplateChoice.DEFINED);
         disableGlobalTemplateChoices(choices);
      }
      else if (getSelectedAssembler().equals(DATABASE_ASSEMBLER))
      {
         if (!m_showDbOutput)
         {
            throw new IllegalArgumentException(
                  "Database assembler should be specified by other means");
         }
         formats.remove(FormatChoice.DATABASE);
         disableFormatChoices(formats);
      }
      else 
      {
         // Custom (unknown) assembler
         assert hasUnknownAssemblers();
         disableFormatChoices(
               Collections.singletonList(FormatChoice.DATABASE));
      }
      reselectFormatRadio();
   }

   /**
    * Reselects format radio button group to trigger selection listeners. 
    * This is used in order to correctly disable global template radio group
    * choice.
    */
   private void reselectFormatRadio()
   {
      m_formatRadio.setSelection(m_formatRadio.getSelectedIndex());
      m_formatRadio.fireSelectionEvent();
   }
   
   /**
    * Disables the specified global template buttons.
    * If necessary moves the radio button selection to the enabled button.
    */
   private void disableGlobalTemplateChoices(
         final List<GlobalTemplateChoice> choices)
   {
      m_globalTemplateRadio.setEnabledButtons(false,
            globalTemplateToIndexes(choices));

      // if current radio selection is disabled
      final List<GlobalTemplateChoice> enabled =
            new ArrayList<GlobalTemplateChoice>(
                  Arrays.asList(GlobalTemplateChoice.values()));
      enabled.removeAll(choices);
      int selectedIdx = m_globalTemplateRadio.getSelectedIndex();
      if (selectedIdx < 0)
      {
         selectedIdx = 0;
      }
      if (!enabled.isEmpty()
            && !enabled.contains(GlobalTemplateChoice.values()[selectedIdx]))
      {
         m_globalTemplateRadio.setSelection(
               enabled.get(0).ordinal());
      }
   }
   
   
   /**
    * Disables the specified format buttons.
    * If necessary moves the radio button selection to the enabled button.
    */
   private void disableFormatChoices(final List<FormatChoice> formats)
   {
      m_formatRadio.setEnabledButtons(false, formatsToIndexes(formats));
      
      // if page is disabled then nested global template radio is disabled too
      if (formats.contains(FormatChoice.PAGE))
      {
         disableGlobalTemplateChoices(
               Arrays.asList(GlobalTemplateChoice.values()));
      }

      // if current radio selection is disabled 
      if (!ArrayUtils.contains(
            m_formatRadio.getEnabledButtons(),
            m_formatRadio.getSelectedIndex()))
      {
         m_formatRadio.setSelection(m_formatRadio.getEnabledButtons()[0]);
      }
   }

   /**
    * Converts list of formats to an array of their indexes.
    */
   private int[] formatsToIndexes(final List<FormatChoice> formats)
   {
      final int[] indexes = new int[formats.size()];
      int idx = 0;
      for (final Enum format : formats)
      {
         indexes[idx] = format.ordinal();
         idx++;
      }
      return indexes;
   }

   /**
    * Converts list of global template choices to an array of their indexes.
    */
   private int[] globalTemplateToIndexes(List<GlobalTemplateChoice> choices)
   {
      final int[] indexes = new int[choices.size()];
      int idx = 0;
      for (final Enum format : choices)
      {
         indexes[idx] = format.ordinal();
         idx++;
      }
      return indexes;
   }

   /**
    * Configures UI. Should be called before displaying it.
    * @param isGlobalTemplate indicates whether the page should show
    * configuration to pick assembler for Global template.
    * This configuration is as following - only assembler combo
    * is visible, only Velocity assembler and unrecognized assemblers are shown.
    */
   public void configureUiBeforeDisplaying(final boolean isGlobalTemplate)
   {
      if (m_isGlobalTemplate != isGlobalTemplate)
      {
         m_formatRadio.setVisible(!isGlobalTemplate);
         m_assemblerRefs.clear();
         m_assemblerRefs.addAll(m_assemblerRefsBackup);
         if (isGlobalTemplate)
         {
            removeAssemblersForGlobalDispalyConfig();
         }
         initAssemblerComboContent();
         maybeSelectVelocityAssembler();
      }
      m_isGlobalTemplate = isGlobalTemplate;
   }

   /**
    * Initializes assembler combo items.
    */
   private void initAssemblerComboContent()
   {
      m_assemblerCombo.removeAll();
      for (final IPSReference ref : m_assemblerRefs)
      {
         m_assemblerCombo.add(ref.getLabelKey());
      }
   }

   /**
    * Makes all format radio buttons enabled.
    */
   private void enableAllFormats()
   {
      m_formatRadio.setEnabledButtons(true, getFormatIndexes());
      m_globalTemplateRadio.setEnabledButtons(true,
            getGlobalTemplateChoiceIndexes());
   }

   /**
    * Only leave assemblers allowed for global template type display config.
    */
   void removeAssemblersForGlobalDispalyConfig()
   {
      for (Iterator<IPSReference> i = m_assemblerRefs.iterator(); i.hasNext();)
      {
         final IPSReference ref = i.next();
         if (ref.getName().equals(VELOCITY_ASSEMBLER))
         {
            continue;
         }
         if (KNOWN_ASSEMBLERS.contains(ref.getName()))
         {
            i.remove();
         }
      }
   }

   /**
    * Indicates that form has unknown assemblers. 
    */
   public boolean hasUnknownAssemblers()
   {
      final Set<String> assemblers = new HashSet<String>();
      for (final IPSReference ref : m_assemblerRefs)
      {
         assemblers.add(ref.getName());
      }
      assemblers.removeAll(KNOWN_ASSEMBLERS);
      return !assemblers.isEmpty();
   }

   /**
    * Loads assemblers.
    */
   private void initAssemblers() throws PSModelException, PSMultiOperationException
   {
      if (!m_showAssemblers)
      {
         return;
      }
      m_assemblerRefs.addAll(loadAssemblers());
      hideLegacyAssembler();
      hideDatabaseAssembler();
      hideDebugAssembler();
      Collections.sort(m_assemblerRefs, new IPSReference.LabelKeyComparator());
      m_assemblerRefsBackup = Collections.unmodifiableList(new ArrayList<IPSReference>(m_assemblerRefs)); 
   }
   
   /**
    * References to extensions which are assemblers.
    * Loads all the extensions and filters out those which are not assemblers.
    */
   List<IPSReference> loadAssemblers() throws PSModelException, PSMultiOperationException
   {
      final List<IPSReference> extensionRefs =
            new ArrayList<IPSReference>(m_extensionModel.catalog());
      final Object[] extensions =
         m_extensionModel.load(extensionRefs.toArray(new IPSReference[0]),
               false, false);
      for (int i = extensions.length - 1; i >= 0 ; i--)
      {
         final PSExtensionDef extension = (PSExtensionDef) extensions[i];
         if (!extension.implementsInterface(IPSAssembler.class.getName()))
         {
            extensionRefs.remove(i);
         }
      }
      return extensionRefs;
   }

   /**
    * Removes the DB assembler from the assembler list.
    */
   private void hideDatabaseAssembler()
   {
      if (m_showDbOutput)
      {
         return;
      }

      hideAssembler(DATABASE_ASSEMBLER);
   }

   /**
    * Removes the debugging assembler from the assembler list.
    */
   private void hideDebugAssembler()
   {
      hideAssembler(DEBUG_ASSEMBLER);
   }

   /**
    * Removes the specified assembler from the list of assemblers.
    */
   private void hideAssembler(final String assembler)
   {
      for (Iterator<IPSReference> i = m_assemblerRefs.iterator(); i.hasNext();)
      {
         final IPSReference ref = i.next();
         if (ref.getName().equals(assembler))
         {
            i.remove();
         }
      }
   }

   /**
    * Removes legacy assembler from the list of assemblers
    *
    */
   private void hideLegacyAssembler()
   {
      hideAssembler(LEGACY_ASSEMBLER);
   }

   /**
    * Updates the provided template with the data from this control.
    */
   public void updateTemplate(final PSUiAssemblyTemplate template)
   {
      updateTemplateForFormatRadioButtons(template);
      updateTemplateForGlobalTemplateUsage(template);
      if (m_showAssemblers)
      {
         String selectedAssembler = getSelectedAssembler();
         template.setAssembler(selectedAssembler);
         if(selectedAssembler.equals(DATABASE_ASSEMBLER))
         {
            template.setMimeType("text/xml");
         }
      }
   }

   /**
    * Loads data from global template usage selection to the controls. 
    */
   private void updateTemplateForGlobalTemplateUsage(PSUiAssemblyTemplate template)
   {
      if (m_isGlobalTemplate || !m_showGlobalTemplates)
      {
         return;
      }
      if (template.getOutputFormat().equals(OutputFormat.Page))
      {
         final GlobalTemplateChoice selection =
            GlobalTemplateChoice.values()[m_globalTemplateRadio.getSelectedIndex()];
         template.setGlobalTemplateUsage(selection.getUsage());
         if (selection.equals(GlobalTemplateChoice.DEFINED))
         {
            final int idx = getGlobalTemplateCombo().getSelectionIndex();
            template.setGlobalTemplate(m_globalTemplateRefs.get(idx).getId());
         }
         else
         {
            template.setGlobalTemplate(null);
         }
      }
      else
      {
         template.setGlobalTemplateUsage(GlobalTemplateUsage.None);
         template.setGlobalTemplate(null);
      }
   }

   /**
    * Reflects state of {@link #m_formatRadio} to the provided template.
    */
   @SuppressWarnings("synthetic-access")
   private void updateTemplateForFormatRadioButtons(
         final PSUiAssemblyTemplate template)
   {
      if (m_isGlobalTemplate)
      {
         return;
      }
      template.setOutputFormat(getSelectedFormatChoice().getOutputFormat());
   }

   /**
    * Currently selected format choice.
    */
   public FormatChoice getSelectedFormatChoice()
   {
      return FormatChoice.values()[m_formatRadio.getSelectedIndex()];
   }
   
   /**
    * Load controls with the template values.
    */
   public void loadControlValues(final PSUiAssemblyTemplate template)
   {
      if (m_showAssemblers)
      {
         loadAssemblerControl(template);
      }
      loadOutputFormat(template);
      if (m_showAssemblers)
      {
         assemblerComboSelectionChanged();
      }
      /* this must occur after 'assemblerComboSelectionChanged()' because the 
       * call to ...SelectionChanged clears the global template selected by 
       * loadGlobalTemplates
       */
      loadGlobalTemplates(template);
   }

   /**
    * Loads template value to the global templates usage controls.
    */
   private void loadGlobalTemplates(final PSUiAssemblyTemplate template)
   {
      final GlobalTemplateChoice choice =
            GlobalTemplateChoice.valueOf(template.getGlobalTemplateUsage());
      m_globalTemplateRadio.setSelection(choice.ordinal());
      loadGlobalTemplateSelection(template);
   }
   
   /**
    * Loads template value to the global selection control.
    */
   private void loadGlobalTemplateSelection(final PSUiAssemblyTemplate template)
   {
      if (template.getGlobalTemplate() == null)
      {
         return;
      }
      int i = 0;
      for (final IPSReference ref : m_globalTemplateRefs)
      {
         if (template.getGlobalTemplate().equals(ref.getId()))
         {
            getGlobalTemplateCombo().select(i);
            return;
         }
         i++;
      }
      handleNonExistingGlobalTemplate(template.getGUID());
   }

   private Combo getGlobalTemplateCombo()
   {
      return ((Combo) m_globalTemplateRadio.getNestedControl(GlobalTemplateChoice.DEFINED.ordinal()));
   }


   /**
    * Loads template value to the output format controls.
    */
   private void loadOutputFormat(final PSUiAssemblyTemplate template)
   {
      if (!m_showDbOutput
            && template.getOutputFormat().equals(OutputFormat.Database))
      {
         throw new IllegalArgumentException(
               "If DB output is not shown the control should not be requested "
               + "to load database output format");
      }
      final FormatChoice choice =
            FormatChoice.valueOf(template.getOutputFormat());
      m_formatRadio.setSelection(choice.ordinal());
      
   }

   /**
    * Loads template value to the assembler controls.
    */
   private void loadAssemblerControl(final PSUiAssemblyTemplate template)
   {
      int i = 0;
      for (final IPSReference ref : m_assemblerRefs)
      {
         if (ref.getName().equals(template.getAssembler()))
         {
            getAssemblerCombo().select(i);
            return;
         }
         i++;
      }
      handleNonExistingAssembler(template.getAssembler());
   }
   
   /**
    * Handles situation when template is found to have non-existing assembler.
    */
   void handleNonExistingAssembler(final String assembler)
   {
      final String message = PSMessages.getString(
               "PSTemplateOutputComposite.error.unknownAssembler",  //$NON-NLS-1$ 
               new Object[]{assembler});
      new PSErrorDialog(getShell(), message).open();
   }

   /**
    * Handles situation when template has reference for non-existing global
    * template.
    * @param guid the global template reference.
    */
   private void handleNonExistingGlobalTemplate(IPSGuid guid)
   {
      final String message = PSMessages.getString(
            "PSTemplateOutputComposite.error.unknownGlobalTemplate",  //$NON-NLS-1$ 
            new Object[]{guid});
      new PSErrorDialog(getShell(), message).open();
   }

   /**
    * Default value for the assembly template model used on this page.
    */
   IPSCmsModel initializeTemplateModel()
   {
      try
      {
         return getCoreFactory().getModel(PSObjectTypes.TEMPLATE);
      }
      catch (PSModelException e)
      {
         PSDlgUtil.showError(e);
         return null;
      }
   }
   
   /**
    * Default value for the extension model used on this page.
    */
   IPSCmsModel initializeExtensionModel()
   {
      try
      {
         return getCoreFactory().getModel(PSObjectTypes.EXTENSION);
      }
      catch (PSModelException e)
      {
         PSDlgUtil.showError(e);
         return null;
      }
   }

   /**
    * Loads global templates.
    */
   private void initGlobalTemplates() throws PSModelException
   {
      if (!m_showGlobalTemplates)
      {
         return;
      }
      m_globalTemplateRefs.addAll(
            m_templateModel.catalog(false, GLOBAL_TEMPLATE_OBJECT_TYPE));
      Collections.sort(m_globalTemplateRefs,
            new IPSReference.LabelKeyComparator());
   }

   /**
    * The singleton core factory instance.
    */
   private PSCoreFactory getCoreFactory()
   {
      return PSCoreFactory.getInstance();
   }
   
   /**
    * Indexes of all the selections.
    */
   private int[] getGlobalTemplateChoiceIndexes()
   {
      return rangeArray(GlobalTemplateChoice.values().length);
   }

   /**
    * Indexes of all available format radio buttons.
    */
   private int[] getFormatIndexes()
   {
      return rangeArray(getShownFormats().length);
   }
   
   /**
    * Creates an int array of the specified length with indexes from 0 to len - 1.
    */
   protected int[] rangeArray(int len)
   {
      final int[] a = new int[len];
      for (int i = 0; i < a.length; i++)
      {
         a[i] = i;
      }
      return a;
   }
   
   /**
    * Currently selected assembler.
    */
   public String getSelectedAssembler()
   {
      final int idx = m_assemblerCombo.getSelectionIndex();
      assert idx != -1 : "Is not expected to be called when there is no selection";
      return m_assemblerRefs.get(idx).getName();
   }

   /**
    * Names of global templates.
    */
   private List<String> getGlobalTemplateNames()
   {
      final List<String> names = new ArrayList<String>();
      for (final IPSReference template : m_globalTemplateRefs)
      {
         names.add(template.getName());
      }
      return names;
   }

   /**
    * Assembler selection UI.
    */
   public Combo getAssemblerCombo()
   {
      return m_assemblerCombo;
   }

   /**
    * Radio button group to select output format.
    */
   public PSRadioAndCheckBoxes getFormatRadio()
   {
      return m_formatRadio;
   }

   /**
    * Convenience method to get message by specified key.
    */
   protected static String getMessage(final String key)
   {
      return PSMessages.getString(key);
   }

   /**
    * Formats which are shown on the page.
    */
   private FormatChoice[] getShownFormats()
   {
      if (m_showDbOutput)
      {
         return FormatChoice.values(); 
      }
      else
      {
         final FormatChoice[] formats = new FormatChoice[FormatChoice.values().length - 1];
         int i = 0;
         for (final FormatChoice choice : FormatChoice.values())
         {
            if (!choice.equals(FormatChoice.DATABASE))
            {
               formats[i] = choice;
               i++;
            }
         }
         assert i == FormatChoice.values().length - 1;
         return formats;
      }
   }

   /**
    * Format button indexes.
    */
   public enum FormatChoice {
      SNIPPET(OutputFormat.Snippet,
            getMessage("PSTemplateOutputComposite.outputFormat.snippet")),    //$NON-NLS-1$
      PAGE(OutputFormat.Page,
            getMessage("PSTemplateOutputComposite.outputFormat.textPage")),   //$NON-NLS-1$
      BINARY(OutputFormat.Binary,
            getMessage("PSTemplateOutputComposite.outputFormat.binaryPage")), //$NON-NLS-1$
      DATABASE(OutputFormat.Database,
            getMessage("PSTemplateOutputComposite.outputFormat.database"));   //$NON-NLS-1$

      private FormatChoice(final OutputFormat outputFormat, final String label)
      {
         m_label = label;
         m_outputFormat = outputFormat;
      }
      
      /**
       * Choice associated with the format.
       */
      public static FormatChoice valueOf(OutputFormat outputFormat)
      {
         for (final FormatChoice format : values())
         {
            if (format.m_outputFormat.equals(outputFormat))
            {
               return format;
            }
         }
         throw new IllegalArgumentException(
               "There is no choice for output format " + outputFormat); //$NON-NLS-1$
      }

      /**
       * Output format associated with this button.
       */
      public OutputFormat getOutputFormat()
      {
         return m_outputFormat;
      }
      
      /**
       * Label to display for this option.
       */
      public String getLabel()
      {
         return m_label;
      }

      private final OutputFormat m_outputFormat;
      private final String m_label;
   }
   
   /**
    * Index for {@link #m_globalTemplateRadio} buttons
    */
   enum GlobalTemplateChoice {
      DEFAULT(GlobalTemplateUsage.Default),
      DEFINED(GlobalTemplateUsage.Defined),
      NONE(GlobalTemplateUsage.None),
      LEGACY(GlobalTemplateUsage.Legacy);
      
      private GlobalTemplateChoice(GlobalTemplateUsage usage)
      {
         this.m_usage = usage;
      }
      
      /**
       * Choice associated with the global template usage.
       */
      public static GlobalTemplateChoice valueOf(GlobalTemplateUsage usage)
      {
         for (final GlobalTemplateChoice choice : values())
         {
            if (choice.getUsage().equals(usage))
            {
               return choice;
            }
         }
         throw new IllegalArgumentException(
               "There is no choice for global template usage " + usage); //$NON-NLS-1$
      }
      
      /**
       * Global template usage associated with the choice.
       */
      public GlobalTemplateUsage getUsage()
      {
         return m_usage;
      }
      
      /**
       * Global template usage associated with the choice.
       */
      private final GlobalTemplateUsage m_usage;
   }

   private class AssemblerComboSelectionListener implements SelectionListener
   {
      @SuppressWarnings("synthetic-access")
      public void widgetSelected(@SuppressWarnings("unused") SelectionEvent e)
      {
         assemblerComboSelectionChanged();
      }

      @SuppressWarnings("synthetic-access")
      public void widgetDefaultSelected(
            @SuppressWarnings("unused") SelectionEvent e)
      {
         assemblerComboSelectionChanged();
      }
   }
   
   /**
    * Assembler combo label text without the column.
    */
   public static final String ASSEMBLER_COMBO_LABEL =
         getMessage("PSTemplateOutputComposite.label.assembler"); //$NON-NLS-1$

   /**
    * Label for the output control.
    */
   public static final String OUTPUT_LABEL =
      getMessage("PSTemplateOutputComposite.label.output"); //$NON-NLS-1$

   /**
    * Object type for global assembly template.
    */
   static final PSObjectType GLOBAL_TEMPLATE_OBJECT_TYPE =
         new PSObjectType(PSObjectTypes.TEMPLATE, TemplateSubTypes.GLOBAL);
   
   /**
    * Indicates whether to show DB output format.
    */
   private final boolean m_showDbOutput;
   
   /**
    * Indicates whether to show global templates dropdown.
    * If <code>true</code> content types UI is not initialized.
    */
   private final boolean m_showGlobalTemplates;

   /**
    * Indicates whether to show assemblers dropdown.
    * If <code>true</code> content types UI is not initialized.
    */
   private final boolean m_showAssemblers;

   /**
    * Indicates whether to show "Legacy" global template usage selection.
    */
   private boolean m_showLegacyGlobalTemplateUsage;

   /**
    * Selecting assemblers.
    */
   private Combo m_assemblerCombo;

   /**
    * Main radio button composite.
    */
   private PSRadioAndCheckBoxes m_formatRadio;

   /**
    * Composite to display global template selection.
    */
   PSRadioAndCheckBoxes m_globalTemplateRadio;
   
   /**
    * Stores last passed value of global assembler display config.
    */
   private boolean m_isGlobalTemplate;

   /**
    * Model used to catalog assemblers.
    */
   IPSCmsModel m_extensionModel = initializeExtensionModel();

   /**
    * Model used to catalog templates. 
    */
   IPSCmsModel m_templateModel = initializeTemplateModel();
   
   /**
    * Templates with {@link OutputFormat#Global} type. Sorted by label.
    */
   final List<IPSReference> m_globalTemplateRefs = new ArrayList<IPSReference>();

   /**
    * Assembler references. Sorted by label. Content can be changed depending
    * on the required UI configuration.
    */
   final List<IPSReference> m_assemblerRefs = new ArrayList<IPSReference>();
   
   /**
    * Keeps original copy of {@link #m_assemblerRefs}.
    */
   List<IPSReference> m_assemblerRefsBackup;
  
}
