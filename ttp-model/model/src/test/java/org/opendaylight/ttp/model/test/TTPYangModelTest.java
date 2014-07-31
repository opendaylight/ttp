package org.opendaylight.ttp.model.test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javassist.ClassPool;

import javax.ws.rs.WebApplicationException;

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
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.CompositeNode;
import org.opendaylight.yangtools.yang.data.impl.codec.BindingIndependentMappingService;
import org.opendaylight.yangtools.yang.data.impl.codec.xml.XmlDocumentUtils;
import org.opendaylight.yangtools.yang.data.impl.codec.xml.XmlUtils;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaContextListener;
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
        Parameters p = new ParametersBuilder().setDoc(Arrays.asList("documentation"))
                .setName("Showing-curt-how-this-works").setType("type1").build();
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
        t.setVariables(Arrays.asList(p1, p2));
        TableTypePatterns builtTTP = t.build();
        testJsonConversion(expectedStr, builtTTP);
        System.out.println(builtTTP.getVariables().getClass().getName());
    }

    /**
     * DON'T CALL THIS IN PRODUCTION CODE EVER!!! UNTIL IT IS FIXED!
     * <p/>
     * Return the {@link DataSchemaNode}
     *
     * @param context
     * @param d
     */
    public DataSchemaNode getSchemaNodeForDataObject(SchemaContext context, DataObject d) {
        // let's assume I can get this
        //QName qn = mappingService.registry.serialize(d.getClass());
        QName qn = BindingReflections.findQName(d.getClass());

        System.out.println("d.qn: "+qn);

        Set<DataSchemaNode> allTheNodes = getAllTheNode(context);


        // TODO: create a map to make this faster!!!!
        for ( DataSchemaNode dsn : allTheNodes ) {
            if(dsn instanceof DataNodeContainer) {
                allTheNodes.addAll(((DataNodeContainer)dsn).getChildNodes());
            }
            System.out.println("    >>>> "+dsn.getQName());
            if (dsn.getQName().equals(qn)) {
                return dsn;
            }
        }
        return null;
    }

    public Set<DataSchemaNode> getAllTheNode(SchemaContext context) {
        Set<DataSchemaNode> nodes = new HashSet<DataSchemaNode>();
        getAllTheNodesHelper(context, nodes);
        return nodes;
    }

    private void getAllTheNodesHelper(DataNodeContainer dcn, Set<DataSchemaNode> nodes) {
        for ( DataSchemaNode dsn : dcn.getChildNodes() ) {
            if (dsn instanceof DataNodeContainer){
                getAllTheNodesHelper((DataNodeContainer)dsn, nodes);
            }
            nodes.add(dsn);
        }
    }

    private void testJsonConversion(String expectedJson, DataObject data) throws Exception {
        String jsonString = jsonStringFromDataObject(data);
        System.out.println(jsonString);

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

    /*
     * Helper functions around converting between DataObjects, CompositeNodes,
     * Documents, StructuredData, and JSON strings
     */

    /**
     *
     * @param data
     * @return
     */
    public Document documentFromDataObject(DataObject data) {
        return documentFromCompositeNode(compositeNodeFromDataObject(data));
    }

    /**
     *
     * @param compNode
     * @return
     */
    public Document documentFromCompositeNode(CompositeNode compNode) {
        return XmlDocumentUtils.toDocument(compNode, XmlUtils.DEFAULT_XML_CODEC_PROVIDER);
    }

    /**
     *
     * @param d
     * @return
     */
    public CompositeNode compositeNodeFromDataObject(DataObject d) {
        return mappingService.toDataDom(d);
    }

    /**
     *
     * @param compNode
     * @return
     */
//    private StructuredData structuredDataFromCompositeNode(CompositeNode compNode) {
//        // Create structured data, the null is an unused mountpoint
//        // final true/false is to turn on/off pretty printing
//        DataSchemaNode NDM_metadata = null;
//        //first solution
//        NDM_metadata = getSchemaNodeForDataObject(context, null);
//        //second solution
//        ControllerContext controllerContext = ControllerContext.getInstance();
//        controllerContext.setSchemas(context);
//        InstanceIdWithSchemaNode iiAndSchema = controllerContext.toInstanceIdentifier("/onf-ttp:opendaylight-ttps/table-type-patterns/table-type-pattern/NDM_metadata");
//        NDM_metadata = iiAndSchema.getSchemaNode();
//
//        return new StructuredData(compNode, NDM_metadata, null, true);
//    }

    /**
     *
     * @param d
     * @return
     */
    public StructuredData structuredDataFromDataObject(DataObject d) {
        DataSchemaNode NDM_metadata = null;
        NDM_metadata = getSchemaNodeForDataObject(context, d);
        return new StructuredData(compositeNodeFromDataObject(d), NDM_metadata, null, true);
    }

    /**
     *
     * @param d
     * @return
     * @throws WebApplicationException
     * @throws IOException
     */
    public String jsonStringFromStructuredData(StructuredData d) throws WebApplicationException, IOException{
        ByteArrayOutputStream s = new ByteArrayOutputStream();
        StructuredDataToJsonProvider.INSTANCE.writeTo(d, null, null, null, null, null, s);
        return s.toString();
    }

    /**
     *
     * @param cn
     * @return
     * @throws WebApplicationException
     * @throws IOException
     */
//    public String jsonStringFromCompositeNode(CompositeNode cn) throws WebApplicationException, IOException{
//        return jsonStringFromStructuredData(structuredDataFromCompositeNode(cn));
//    }


    /**
     * I want this:
     *   DataObject ==???===> JSON String
     *
     * To do that, you hve to do this:
     *   DataObject => CompositeNode => StructuredData => JSON String
     *                                       |
     *            SchemaContext ==??==> DataNodeSchema
     */

    /**
     * Converts a {@link DataObject} to a JSON representation in a string using the relevant YANG
     * schema if it is present. This defaults to using a {@link SchemaContextListener} if running an
     * OSGi environment or {@link BindingReflections#loadModuleInfos()} if run while not in an OSGi
     * environment or if the schema isn't available via {@link SchemaContextListener}.
     *
     * @param d
     * @return
     * @throws WebApplicationException
     * @throws IOException
     */
    public String jsonStringFromDataObject(DataObject d) throws WebApplicationException, IOException{
        return jsonStringFromStructuredData(structuredDataFromDataObject(d));
    }
}
