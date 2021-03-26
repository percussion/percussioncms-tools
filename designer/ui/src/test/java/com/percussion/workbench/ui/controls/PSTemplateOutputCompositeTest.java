/******************************************************************************
 *
 * [ PSTemplateOutputCompositeTest.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.controls;

import com.percussion.client.IPSReference;
import com.percussion.client.PSModelException;
import com.percussion.client.PSObjectTypeFactory;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.impl.PSReference;
import com.percussion.client.models.IPSCmsModel;
import com.percussion.client.objectstore.PSUiAssemblyTemplate;
import com.percussion.services.assembly.IPSAssemblyTemplate.GlobalTemplateUsage;
import com.percussion.services.assembly.IPSAssemblyTemplate.OutputFormat;
import com.percussion.services.assembly.IPSAssemblyTemplate.TemplateType;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.workbench.ui.PSUiTestBase;
import com.percussion.workbench.ui.controls.PSTemplateOutputComposite.FormatChoice;
import com.percussion.workbench.ui.controls.PSTemplateOutputComposite.GlobalTemplateChoice;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.jmock.cglib.Mock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.percussion.extension.IPSExtension.BINARY_ASSEMBLER;
import static com.percussion.extension.IPSExtension.DATABASE_ASSEMBLER;
import static com.percussion.extension.IPSExtension.DISPATCH_ASSEMBLER;
import static com.percussion.extension.IPSExtension.KNOWN_ASSEMBLERS;
import static com.percussion.extension.IPSExtension.VELOCITY_ASSEMBLER;
import static com.percussion.workbench.ui.controls.PSTemplateOutputComposite.GLOBAL_TEMPLATE_OBJECT_TYPE;

/**
 * To run this test under Eclipse, you need to do the following:
 * <p>
 * Modify the default launch configuration. Add the following javaVM param:
 * <pre>
 * -Djava.library.path=C:\eclipse\configuration\org.eclipse.osgi\bundles\61\1\.cp
 * <pre>
 * modifying the path for your machine.
 * <p>
 * On the Classpath tab, add all the jars found under &lt;devroot>/System/Tools/eclipse.
 * <p>
 * conn_rxserver.properties must be modified. Change the  
 * <code>useTestCreds</code> property to <code>true</code> and change the port 
 * to match your local server.
 * <p>
 * Copy &lt;devroot>/System/ear/config/spring/workbench-beans.xml to
 * &lt;devroot>/designer/ui
 * <p>
 * The Rx server must be running.
 */
public class PSTemplateOutputCompositeTest extends PSUiTestBase
{
   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
      m_assemblerRefs = null;
      m_mockExtensionModel = new Mock(IPSCmsModel.class);
      m_globalTemplates = Collections.singleton(createRef(NAME0));
   }

   public void testBasics()
   {
      final PSTemplateOutputComposite control =
            new PSTemplateOutputComposite(m_shell, SWT.NONE, false, false,
                  false, true, false);
      assertEquals(FormatChoice.SNIPPET.ordinal(), control.getFormatRadio().getSelectedIndex());
   }
   
   public void testAssemblerDropdownInitialization()
   {
      m_mockExtensionModel = new Mock(IPSCmsModel.class);
      
      // everything is fine - non-empty assembler list
      {
         final IPSReference ref0 = createRef(NAME0);
         final IPSReference ref1 = createRef(NAME1);
         final IPSReference ref2 = createRef(NAME2);
         m_assemblerRefs = new ArrayList<IPSReference>();
         m_assemblerRefs.add(ref1);
         m_assemblerRefs.add(ref2);
         m_assemblerRefs.add(ref0);
         
         final PSTemplateOutputComposite control =
               new TestImpl(m_shell, SWT.NONE, false, false);

         assertEquals(m_assemblerRefs.size(), control.getAssemblerCombo().getItemCount());
         assertEquals(ref0.getLabelKey(), control.getAssemblerCombo().getItem(0));
         assertEquals(ref1.getLabelKey(), control.getAssemblerCombo().getItem(1));
         assertEquals(ref2.getLabelKey(), control.getAssemblerCombo().getItem(2));

         assertTrue(control.getAssemblerCombo().isEnabled());
         assertEquals(0, control.getAssemblerCombo().getSelectionIndex());
      }
      
      // database assembler is *not* avail. to the user
      {
         m_assemblerRefs =
               new ArrayList<IPSReference>(createKnownCatalogedAssemblers());
         final PSTemplateOutputComposite control =
               new TestImpl(m_shell, SWT.NONE, false, false);
         
         final IPSReference binaryRef = createRef(BINARY_ASSEMBLER);
         final IPSReference dbRef = createRef(DATABASE_ASSEMBLER);
         final Set<String> assemblerNames = new HashSet<String>();
         for (final IPSReference ref : createKnownCatalogedAssemblers())
         {
            assemblerNames.add(ref.getName());
         }
         assertTrue(assemblerNames.contains(DATABASE_ASSEMBLER));
         assertFalse(ArrayUtils.contains(control.getAssemblerCombo().getItems(),
               dbRef.getLabelKey()));
         assertTrue(ArrayUtils.contains(control.getAssemblerCombo().getItems(),
               binaryRef.getLabelKey()));
         assertEquals(createKnownCatalogedAssemblers().size()
                     - (HIDDEN_ASSEMBLERS_COUNT + 1),
               control.getAssemblerCombo().getItemCount());
      }

      // database assembler is avail. to the user
      {
         m_assemblerRefs =
               new ArrayList<IPSReference>(createKnownCatalogedAssemblers());
         final PSTemplateOutputComposite control =
               new TestImpl(m_shell, SWT.NONE, false, true);
         
         final IPSReference binaryRef = createRef(BINARY_ASSEMBLER);
         final IPSReference dbRef = createRef(DATABASE_ASSEMBLER);
         final Set<String> assemblerNames = new HashSet<String>();
         for (final IPSReference ref : createKnownCatalogedAssemblers())
         {
            assemblerNames.add(ref.getName());
         }
         assertTrue(assemblerNames.contains(DATABASE_ASSEMBLER));
         assertTrue(ArrayUtils.contains(control.getAssemblerCombo().getItems(),
               dbRef.getLabelKey()));
         assertTrue(ArrayUtils.contains(control.getAssemblerCombo().getItems(),
               binaryRef.getLabelKey()));
         assertEquals(
               createKnownCatalogedAssemblers().size() - HIDDEN_ASSEMBLERS_COUNT,
               control.getAssemblerCombo().getItemCount());
      }
   }
   
   public void testGlobalTemplateComboInitialization()
   {
      final Mock mockTemplateModel = new Mock(IPSCmsModel.class);
      m_assemblerRefs = new ArrayList<IPSReference>();
      m_assemblerRefs.add(createRef(NAME1));
      m_assemblerRefs.add(createRef(NAME2));
      m_assemblerRefs.add(createRef(NAME0));
      
      // global templates exist
      {
         final List<IPSReference> templateRefs = new ArrayList<IPSReference>();
         templateRefs.add(createRef(NAME1));
         templateRefs.add(createRef(NAME2));
         templateRefs.add(createRef(NAME0));
         
         mockTemplateModel.expects(once()).method("catalog")
               .with(eq(false), eq(GLOBAL_TEMPLATE_OBJECT_TYPE))
               .will(returnValue(templateRefs));
         final PSTemplateOutputComposite control =
            new TestImpl(m_shell, SWT.NONE, false, false)
         {
            @Override
            IPSCmsModel initializeTemplateModel()
            {
               return (IPSCmsModel) mockTemplateModel.proxy();
            }
         };

         assertEquals(templateRefs.size(), control.getGlobalTemplatesCombo().getItemCount());
         assertEquals(NAME0, control.getGlobalTemplatesCombo().getItem(0));
         assertEquals(NAME1, control.getGlobalTemplatesCombo().getItem(1));
         assertEquals(NAME2, control.getGlobalTemplatesCombo().getItem(2));
      }
      
      mockTemplateModel.verify();
   }

   /**
    * Creates a collection of assemblers consisting of known assemblers
    * returned by call to mock assembler cataloging method.
    */
   private List<IPSReference> createKnownCatalogedAssemblers()
   {
      final List<IPSReference> refs = new ArrayList<IPSReference>();
      for (final String refName : KNOWN_ASSEMBLERS)
      {
         refs.add(createRef(refName));
      }
      return refs;
   }
   
   /**
    * Checks connection between assembler and available output types as specified
    * by table 4 of "Rhythmyx Workbench for Rhino" functional specification. 
    */
   public void testAssemblerFormats()
   {
      final Mock mockTemplateModel = new Mock(IPSCmsModel.class);

      m_assemblerRefs = createKnownCatalogedAssemblers();
      m_assemblerRefs.add(createRef(UNKNOWN_ASSEMBLER));
      
      {
         final PSTemplateOutputComposite control =
               createControlWithModel(mockTemplateModel, false);

         // database - nothing happens because it is not in the list
         control.maybeSelectAssembler(DATABASE_ASSEMBLER);
         assertFalse(control.getSelectedAssembler().equals(DATABASE_ASSEMBLER));
      }

      final PSTemplateOutputComposite control =
            createControlWithModel(mockTemplateModel, true);

      // initial selection
      assertEquals(VELOCITY_ASSEMBLER, control.getSelectedAssembler());

      // velocity
      assertEquals(FormatChoice.SNIPPET.ordinal(),
            control.getFormatRadio().getSelectedIndex());
      assertButtonsEnabled(control, true, true, false, false);
      control.getFormatRadio().setSelection(FormatChoice.PAGE.ordinal());
      assertAllButtonsAreEnabled(control.m_globalTemplateRadio);

      // binary
      control.maybeSelectAssembler(BINARY_ASSEMBLER);
      assertEquals(FormatChoice.BINARY.ordinal(), control.getFormatRadio().getSelectedIndex());
      assertButtonsEnabled(control, false, false, true, false);

      // dispatch
      control.maybeSelectAssembler(DISPATCH_ASSEMBLER);
      // BINARY is still selected as all choices remain enabled
      assertEquals(FormatChoice.BINARY.ordinal(), control.getFormatRadio().getSelectedIndex());
      assertButtonsEnabled(control, true, true, true, true);
      control.getFormatRadio().setSelection(FormatChoice.PAGE.ordinal());
      
      final int[] noneTemplateChoice = new int[] {GlobalTemplateChoice.NONE.ordinal()}; 
      assertTrue(
            "Expected: " + ArrayUtils.toString(noneTemplateChoice) + " but got "
            + ArrayUtils.toString(control.m_globalTemplateRadio.getEnabledButtons()),
            ArrayUtils.isEquals(
                  noneTemplateChoice,
                  control.m_globalTemplateRadio.getEnabledButtons()));

      // custom
      control.maybeSelectAssembler(UNKNOWN_ASSEMBLER);
      assertButtonsEnabled(control, true, true, true, false);
      assertAllButtonsAreEnabled(control.m_globalTemplateRadio);
      
      // database
      control.maybeSelectAssembler(DATABASE_ASSEMBLER);
      assertEquals(FormatChoice.DATABASE.ordinal(), control.getFormatRadio().getSelectedIndex());
      assertButtonsEnabled(control, false, false, false, true);
      control.getFormatRadio().setSelection(FormatChoice.PAGE.ordinal());
      
      mockTemplateModel.verify();
   }

   /**
    * Creates control using the provided model.
    */
   private PSTemplateOutputComposite createControlWithModel(
         final Mock mockTemplateModel, final boolean showDbOutput)
   {
      mockTemplateModel.expects(once()).method("catalog")
            .with(eq(false), eq(GLOBAL_TEMPLATE_OBJECT_TYPE))
            .will(returnValue(m_globalTemplates));
      final PSTemplateOutputComposite control =
         new TestImpl(m_shell, SWT.NONE, false, showDbOutput)
      {
         @Override
         IPSCmsModel initializeTemplateModel()
         {
            return (IPSCmsModel) mockTemplateModel.proxy();
         }
         
      };
      return control;
   }

   /**
    * Checks enabled state of all format buttons.
    * @param enabled enabled/disabled specification for the button.
    * Output formats are specified in following order: SNIPPET, PAGE, BINARY, DATABASE. 
    */
   private void assertButtonsEnabled(PSTemplateOutputComposite control, boolean... enabled)
   {
      assertEquals(FormatChoice.values().length, enabled.length);
      for (final FormatChoice choice : FormatChoice.values())
      {
         assertEquals("Unexpected enabled state of format " + choice,
               enabled[choice.ordinal()],
               ArrayUtils.contains(
                     control.getFormatRadio().getEnabledButtons(), choice.ordinal()));
      }
   }

   /**
    * Makes sure all the buttons are enabled.
    */
   private void assertAllButtonsAreEnabled(final PSRadioAndCheckBoxes radioAndCheckBoxes)
   {
      assertEquals(radioAndCheckBoxes.getButtonCount(), radioAndCheckBoxes.getEnabledButtons().length);
   }
   
   public void testUpdateTemplate()
   {
      m_assemblerRefs = Collections.singletonList(createRef(NAME0));
      
      m_globalTemplates = new HashSet<IPSReference>();
      m_globalTemplates.add(createRef(NAME0, 2));
      final PSReference ref1 = createRef(NAME1, 3);
      m_globalTemplates.add(ref1);
      m_globalTemplates.add(createRef(NAME2, 4));

      final Mock mockTemplateModel = new Mock(IPSCmsModel.class);
      final PSTemplateOutputComposite control =
            createControlWithModel(mockTemplateModel, true);

      final PSUiAssemblyTemplate template = new PSUiAssemblyTemplate();
      assertEquals(TemplateType.Shared, template.getTemplateType());

      assertFalse(StringUtils.equals(NAME0, template.getAssembler()));
      control.updateTemplate(template);
      assertEquals(NAME0, template.getAssembler());
      
      checkFormatRadiosForUpdateDesignerObject(control, template,
            FormatChoice.PAGE, OutputFormat.Page);
      
      final PSRadioAndCheckBoxes globalTemplateRadio =
            control.m_globalTemplateRadio;
      
      // Default global template usage selection  
      globalTemplateRadio.setSelection(GlobalTemplateChoice.DEFAULT.ordinal());
      control.updateTemplate(template);
      assertEquals(GlobalTemplateUsage.Default, template.getGlobalTemplateUsage());
      assertNull(template.getGlobalTemplate());
      
      // None global template usage selection  
      globalTemplateRadio.setSelection(GlobalTemplateChoice.NONE.ordinal());
      control.updateTemplate(template);
      assertEquals(GlobalTemplateUsage.None, template.getGlobalTemplateUsage());
      assertNull(template.getGlobalTemplate());
      
      // Defined global template usage selection
      globalTemplateRadio.setSelection(GlobalTemplateChoice.DEFINED.ordinal());
      control.getGlobalTemplatesCombo().select(1);
      control.updateTemplate(template);
      assertEquals(GlobalTemplateUsage.Defined,
            template.getGlobalTemplateUsage());
      assertEquals(ref1.getId(), template.getGlobalTemplate());
      
      checkFormatRadiosForUpdateDesignerObject(control, template,
            FormatChoice.SNIPPET, OutputFormat.Snippet);
      // no global template usage is saved if format is not page
      assertEquals(GlobalTemplateUsage.None, template.getGlobalTemplateUsage());
      assertNull(template.getGlobalTemplate());
      
      checkFormatRadiosForUpdateDesignerObject(control, template,
            FormatChoice.BINARY, OutputFormat.Binary);
      
      mockTemplateModel.verify();
   }

   /**
    * Checks how radio button state affects the design object.
    */
   private void checkFormatRadiosForUpdateDesignerObject(
         PSTemplateOutputComposite control, PSUiAssemblyTemplate template, FormatChoice selection,
         OutputFormat outputFormat)
   {
      control.getFormatRadio().setSelection(selection.ordinal());
      control.updateTemplate(template);
      assertEquals(outputFormat, template.getOutputFormat());
   }
   
   public void testLoadControlValues_AssemblerOutputFormatSync()
   {
      final boolean[] wasCalled = new boolean[1];

      m_assemblerRefs = new ArrayList<IPSReference>();
      m_assemblerRefs.add(createRef(NAME1));
      m_assemblerRefs.add(createRef(NAME2));
      m_assemblerRefs.add(createRef(NAME0));
      m_assemblerRefs.add(createRef(DATABASE_ASSEMBLER));
      
      final PSUiAssemblyTemplate template = new PSUiAssemblyTemplate();

      // DB assembler works when DB assembler is enabled
      {
         template.setAssembler(DATABASE_ASSEMBLER);
         final TestImpl control = new TestImpl(m_shell, SWT.NONE, false, true);
         control.loadControlValues(template);
         assertEquals(DATABASE_ASSEMBLER, control.getSelectedAssembler());
      }
      
      // DB assembler does not work when DB assembler is disabled
      {
         template.setAssembler(DATABASE_ASSEMBLER);
         final TestImpl control = new TestImpl(m_shell, SWT.NONE, false, false)
         {
            @Override
            void handleNonExistingAssembler(String assembler)
            {
               wasCalled[0] = true;
               assertEquals(DATABASE_ASSEMBLER, assembler);
            }
         };
         wasCalled[0] = false;
         control.loadControlValues(template);
         assertTrue(wasCalled[0]);
      }

      // database output format works when DB assembler is enabled
      {
         template.setOutputFormat(OutputFormat.Database);
         final TestImpl control = new TestImpl(m_shell, SWT.NONE, false, true);
         control.loadControlValues(template);
         assertEquals(FormatChoice.DATABASE.ordinal(),
               control.getFormatRadio().getSelectedIndex());
      }

      //  database output format doesn't work when DB assembler is disabled
      {
         final TestImpl control = new TestImpl(m_shell, SWT.NONE, false, false);
         template.setAssembler(NAME0);
         template.setOutputFormat(OutputFormat.Database);
         try
         {
            control.loadControlValues(template);
            fail();
         }
         catch (IllegalArgumentException success) {}
      }
      
      template.setOutputFormat(OutputFormat.Binary);
      

      // unknown assembler
      {
         final String NON_EXISTING_ASSEMBLER_NAME = "Unknown Assembler";
         template.setAssembler(NON_EXISTING_ASSEMBLER_NAME);
         final TestImpl control = new TestImpl(m_shell, SWT.NONE, false, false)
         {
            @Override
            void handleNonExistingAssembler(String assembler)
            {
               wasCalled[0] = true;
               assertEquals(template.getAssembler(), assembler);
            }
         };
         wasCalled[0] = false;
         control.loadControlValues(template);
         assertTrue(wasCalled[0]);
         assertEquals(NAME0, control.getSelectedAssembler());
      }

      final PSTemplateOutputComposite control =
         new TestImpl(m_shell, SWT.NONE, false, false);
      
      // assembler
      template.setAssembler(NAME2);
      control.loadControlValues(template);
      assertEquals(NAME2, control.getSelectedAssembler());
      
      // some other output format
      template.setOutputFormat(OutputFormat.Binary);
      control.loadControlValues(template);
      assertEquals(FormatChoice.BINARY.ordinal(),
            control.getFormatRadio().getSelectedIndex());
   }

   public void testLoadControlValues_GlobalTemplate()
   {
      m_assemblerRefs = new ArrayList<IPSReference>();
      m_assemblerRefs.add(createRef(NAME1));
      m_assemblerRefs.add(createRef(VELOCITY_ASSEMBLER));

      final Mock mockTemplateModel = new Mock(IPSCmsModel.class);

      
      final PSUiAssemblyTemplate template = new PSUiAssemblyTemplate();
      template.setAssembler(VELOCITY_ASSEMBLER);
      template.setOutputFormat(OutputFormat.Page);

      // None
      {
         final PSTemplateOutputComposite control =
            createControlWithModel(mockTemplateModel, true);
         template.setGlobalTemplateUsage(GlobalTemplateUsage.None);
         control.loadControlValues(template);
         assertEquals(FormatChoice.PAGE.ordinal(),
               control.getFormatRadio().getSelectedIndex());
         assertEquals(GlobalTemplateChoice.NONE.ordinal(),
               control.m_globalTemplateRadio.getSelectedIndex());
      }
      
      // Default
      {
         final PSTemplateOutputComposite control =
            createControlWithModel(mockTemplateModel, true);
         template.setGlobalTemplateUsage(GlobalTemplateUsage.Default);
         control.loadControlValues(template);
         assertEquals(GlobalTemplateChoice.DEFAULT.ordinal(),
               control.m_globalTemplateRadio.getSelectedIndex());
      }
      
      // Specified
      {
         m_globalTemplates = new HashSet<IPSReference>();
         m_globalTemplates.add(createRef(NAME0, 2));
         final PSReference ref1 = createRef(NAME1, 3);
         m_globalTemplates.add(ref1);
         m_globalTemplates.add(createRef(NAME2, 4));
         
         final PSTemplateOutputComposite control =
            createControlWithModel(mockTemplateModel, true);
         template.setGlobalTemplateUsage(GlobalTemplateUsage.Defined);
         template.setGlobalTemplate(ref1.getId());
         control.loadControlValues(template);
         assertEquals(GlobalTemplateChoice.DEFINED.ordinal(),
               control.m_globalTemplateRadio.getSelectedIndex());
         final int templateIdx =
               control.getGlobalTemplatesCombo().getSelectionIndex();
         assertEquals(NAME1,
               control.m_globalTemplateRefs.get(templateIdx).getName());
      }
      
      mockTemplateModel.verify();
   }

   private PSReference createRef(final String name, final int id) throws AssertionError
   {
      final PSReference ref = new PSReference();
      ref.setName(name);
      ref.setLabelKey("LABEL: " + name);
      try
      {
         ref.setObjectType(PSObjectTypeFactory.getType(PSObjectTypes.TEMPLATE,
               PSObjectTypes.TemplateSubTypes.SHARED));
      }
      catch (PSModelException e)
      {
         throw new AssertionError(e);
      }
      ref.setId(new PSGuid(0, PSTypeEnum.TEMPLATE, id));
      return ref;
   }

   /**
    * Makes sure extension model behaves as expected.
    */
   public void testExtensionModelRequirements() throws PSModelException
   {
      final PSTemplateOutputComposite control =
            new PSTemplateOutputComposite(m_shell, SWT.NONE, false, false,
                  false, true, false);

      final Collection<IPSReference> extensions = control.m_extensionModel.catalog();
      assertTrue(extensions.size() > 0);

      final Set<String> extensionNames = new HashSet<String>();
      for (final IPSReference reference : extensions)
      {
         extensionNames.add(reference.getName());
      }
      assertTrue(
            extensionNames.containsAll(KNOWN_ASSEMBLERS));
   }
   
   public void testRemoveAssemblersForGlobalDispalyConfig()
   {
      m_assemblerRefs = createKnownCatalogedAssemblers();
      m_assemblerRefs.add(createRef(UNKNOWN_ASSEMBLER));

      final PSTemplateOutputComposite page =
            new TestImpl(m_shell, SWT.NONE, false, false);
      page.removeAssemblersForGlobalDispalyConfig();

      // contains only the unknown assembler and  
      assertEquals(2, page.m_assemblerRefs.size());
      final Set<String> assemblerNames = new HashSet<String>();
      for (final IPSReference ref : page.m_assemblerRefs)
      {
         assemblerNames.add(ref.getName());  
      }
      assertTrue(assemblerNames.contains(UNKNOWN_ASSEMBLER));
      assertTrue(assemblerNames.contains(VELOCITY_ASSEMBLER));
   }
   
   private class TestImpl extends PSTemplateOutputComposite
   {
      public TestImpl(Composite parent, int style, boolean narrow, boolean showDbOutput)
      {
         super(parent, style, narrow, false, showDbOutput, true, false);
      }
      
      @Override
      List<IPSReference> loadAssemblers()
      {
         return PSTemplateOutputCompositeTest.this.m_assemblerRefs;
      }

      @Override
      IPSCmsModel initializeExtensionModel()
      {
         return (IPSCmsModel) m_mockExtensionModel.proxy();
      }
   }

   /**
    * Sample names.
    */
   private static final String NAME0 = "Name0!";
   private static final String NAME1 = "Name1!";
   private static final String NAME2 = "Name2!";
   
   /**
    * Name for an unknown assembler.
    */
   final String UNKNOWN_ASSEMBLER = "UnknownAssembler!";

   /**
    * Number of assemblers which are always hidden from the user.
    */
   private static final int HIDDEN_ASSEMBLERS_COUNT = 2;

   /**
    * References to be returned by {@link TestImpl#loadAssemblers()}.
    */
   private List<IPSReference> m_assemblerRefs;
   
   /**
    * Model returned by  {@link TestImpl#initializeExtensionModel()}.
    */
   private Mock m_mockExtensionModel;

   /**
    * Global templates provided to
    * {@link #createControlWithModel(Mock, boolean)}.
    * Initialized in {@link #setUp()};
    */
   private Set<IPSReference> m_globalTemplates;
}
