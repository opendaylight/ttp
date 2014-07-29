package org.opendaylight.ttp.model.test;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

import javassist.ClassPool;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.controller.sal.rest.impl.StructuredDataToJsonProvider;
import org.opendaylight.controller.sal.rest.impl.StructuredDataToXmlProvider;
import org.opendaylight.controller.sal.restconf.impl.StructuredData;
import org.opendaylight.yang.gen.v1.urn.onf.ttp.rev140711.opendaylight.ttps.TableTypePatterns;
import org.opendaylight.yang.gen.v1.urn.onf.ttp.rev140711.opendaylight.ttps.TableTypePatternsBuilder;
import org.opendaylight.yang.gen.v1.urn.onf.ttp.rev140711.table.type.pattern.NDMMetadata;
import org.opendaylight.yang.gen.v1.urn.onf.ttp.rev140711.table.type.pattern.NDMMetadataBuilder;
import org.opendaylight.yang.gen.v1.urn.onf.ttp.rev140711.table.type.pattern.Parameters;
import org.opendaylight.yang.gen.v1.urn.onf.ttp.rev140711.table.type.pattern.ParametersBuilder;
import org.opendaylight.yang.gen.v1.urn.onf.ttp.rev140711.table.type.pattern.Variables;
import org.opendaylight.yang.gen.v1.urn.onf.ttp.rev140711.table.type.pattern.VariablesBuilder;
import org.opendaylight.yangtools.sal.binding.generator.impl.ModuleInfoBackedContext;
import org.opendaylight.yangtools.sal.binding.generator.impl.RuntimeGeneratedMappingServiceImpl;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;
import org.opendaylight.yangtools.yang.binding.util.BindingReflections;
import org.opendaylight.yangtools.yang.data.api.CompositeNode;
import org.opendaylight.yangtools.yang.data.impl.codec.BindingIndependentMappingService;
import org.opendaylight.yangtools.yang.data.impl.codec.xml.XmlDocumentUtils;
import org.opendaylight.yangtools.yang.data.impl.codec.xml.XmlUtils;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.w3c.dom.Document;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

public class TTPYangModelTest {

    SchemaContext context = null;
    BindingIndependentMappingService mappingService = null;

    @Before
    public void setup() {
        System.out.println("Building context");
        context = getSchemaContext();
        System.out.println("Context built");
        System.out.println("Building mapping service");
        mappingService = new RuntimeGeneratedMappingServiceImpl(ClassPool.getDefault());
        ((RuntimeGeneratedMappingServiceImpl)mappingService).onGlobalContextUpdated(context);
        System.out.println("Mapping service built");
    }

    public static final SchemaContext getSchemaContext() {
        Iterable<YangModuleInfo> moduleInfos;
        // TODO: make this load fewer things
        moduleInfos = BindingReflections.loadModuleInfos();
        ModuleInfoBackedContext moduleContext = ModuleInfoBackedContext.create();
        moduleContext.addModuleInfos(moduleInfos);
        return moduleContext.tryToCreateSchemaContext().get();
    }

    public static String join(String list[], String separator) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < (list.length - 1); i++) {
            sb.append(list[i]).append(separator);
        }
        sb.append(list[list.length - 1]);
        return sb.toString();
    }

    @Test
    public void test() throws Exception {

        /*
         * test NDM_metadata slug JSON conversion
         */
        String expectedXML = "<NDM_metadata xmlns=\"urn:onf:ttp\">\n    <authority>org.opennetworking.fawg</authority>\n</NDM_metadata>";
        String expectedJson = "{\n    \"NDM_metadata\": {\n        \"authority\": \"org.opennetworking.fawg\"\n    }\n}";
        // TODO: add more set methods
        NDMMetadata NDMmeta = new NDMMetadataBuilder()
                .setDoc(Arrays
                        .asList("TTP supporting L2 VLANs (unicast, multicast, flooding) with optional VID translation."))
                .setAuthority("org.opennetworking.fawg").build();
        testJsonConversion(expectedJson, NDMmeta);

        /*
         * test NDM_metadata slug XML conversion via Document and Document writers
         */
        System.out.println("Generating document");
        Document doc = documentFromDataObject(NDMmeta);
        System.out.println("Generated document");

        System.out.println("Formatted XML From Document:");
        String docXML = formatDocumentAsXMLString(doc);
        //TODO: fix Assert to use basic XML parser to do equality
        //Assert.assertTrue(docXML.contains(expectedXML));
        System.out.println(docXML);

        /*
         * test NDM_metadata slug XML conversion via StructuredDataToXmlProvider
         */
        ByteArrayOutputStream s = new ByteArrayOutputStream();
        StructuredData sd = structuredDataFromDataObject(NDMmeta);
        // ignore all the nulls
        StructuredDataToXmlProvider.INSTANCE.writeTo(sd, null, null, null, null, null, s);
        System.out.println("Formatted XML From StructuredDataToXmlProvider:");
        String structuredDataXML = s.toString();
        //TODO: fix Assert to use basic XML parser to do equality
        //Assert.assertTrue(structuredDataXML.contains(expectedXML));
        System.out.println(structuredDataXML);

        /*
         * test parameters slug JSON conversion
         */
		Parameters p = new ParametersBuilder()
				.setDoc(Arrays.asList("documentation"))
				.setName("Showing-curt-how-this-works").setType("type1")
				.build();
		String expectedStr = "\"parameters\":{\"doc\":\"documentation\",\"type\":\"type1\"}";
		testJsonConversion(expectedStr, p);

        /*
         * test variables
         */
        TableTypePatternsBuilder t = new TableTypePatternsBuilder();
        Variables p1 = new VariablesBuilder().setDoc(Arrays.asList("documentation"))
                .setName("Showing-curt-how-this-works").build();
        Variables p2 = new VariablesBuilder().setDoc(Arrays.asList("documentation"))
                .setName("Showing-curt-how-this-works").build();
        t.setVariables(Arrays.asList(p1));
        TableTypePatterns builtTTP = t.build();
        testJsonConversion(expectedStr, builtTTP);
        System.out.println(builtTTP.getVariables().getClass().getName());
    }

    private Document documentFromDataObject(DataObject data) {
        return documentFromCompositeNode(compositeNodeFromDataObject(data));
    }

    private Document documentFromCompositeNode(CompositeNode compNode) {
        return XmlDocumentUtils.toDocument(compNode, XmlUtils.DEFAULT_XML_CODEC_PROVIDER);
    }

    private CompositeNode compositeNodeFromDataObject(DataObject d) {
        return mappingService.toDataDom(d);
    }

    private StructuredData structuredDataFromCompositeNode(CompositeNode compNode) {
        // Create structured data, the null is an unused mountpoint
        // final true/false is to turn on/off pretty printing
        return new StructuredData(compNode, context, null, true);
    }

    private StructuredData structuredDataFromDataObject(DataObject d) {
        return structuredDataFromCompositeNode(compositeNodeFromDataObject(d));
    }

    private void testJsonConversion(String expectedJson, DataObject data) throws Exception {
        StructuredData d = structuredDataFromDataObject(data);

        ByteArrayOutputStream s = new ByteArrayOutputStream();
        // ignore all the nulls
        StructuredDataToJsonProvider.INSTANCE.writeTo(d, null, null, null, null, null, s);
        System.out.println("Formatted JSON from "
                + StructuredDataToJsonProvider.class.getName() + ":");
        String structuredDataJSON = s.toString();
        System.out.println(structuredDataJSON);

        // TODO: get simple JSON parser to do JSON equality
        //Assert.assertEquals(expectedJson, structuredDataJSON);
        System.out.println("Expected JSON:\n"+expectedJson);
    }

    public static void main(String args[]) {
        long start = System.currentTimeMillis();
        try {
            new TTPYangModelTest().test();
        } catch (Exception e) {
            e.printStackTrace();
        }
        long duration = System.currentTimeMillis() - start;
        System.out.println("Took " + duration + " milliseconds");
    }

    public static String formatDocumentAsXMLString(Document document) throws Exception {
        final DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
        final DOMImplementationLS impl = (DOMImplementationLS) registry.getDOMImplementation("LS");
        final LSSerializer writer = impl.createLSSerializer();
        writer.getDomConfig().setParameter("format-pretty-print", Boolean.TRUE);
        return writer.writeToString(document);
    }
}
