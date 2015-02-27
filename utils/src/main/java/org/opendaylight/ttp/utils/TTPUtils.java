package org.opendaylight.ttp.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.WebApplicationException;

import javassist.ClassPool;

import org.opendaylight.controller.sal.rest.impl.StructuredDataToJsonProvider;
import org.opendaylight.controller.sal.restconf.impl.StructuredData;
import org.opendaylight.yangtools.sal.binding.generator.impl.ModuleInfoBackedContext;
import org.opendaylight.yangtools.sal.binding.generator.impl.RuntimeGeneratedMappingServiceImpl;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;
import org.opendaylight.yangtools.yang.binding.util.BindingReflections;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.CompositeNode;
import org.opendaylight.yangtools.yang.data.impl.codec.BindingIndependentMappingService;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

public class TTPUtils {

    public static final SchemaContext getSchemaContext() {
        System.out.println("Building context");
        Iterable<YangModuleInfo> moduleInfos;
        // TODO: make this load fewer things
        moduleInfos = BindingReflections.loadModuleInfos();
        ModuleInfoBackedContext moduleContext = ModuleInfoBackedContext.create();
        moduleContext.addModuleInfos(moduleInfos);
        SchemaContext ret = moduleContext.tryToCreateSchemaContext().get();
        System.out.println("Context built");
        return ret;
    }

    /**
     *
     * @param d
     * @return
     * @throws WebApplicationException
     * @throws IOException
     */
    public static final String jsonStringFromStructuredData(StructuredData d)
            throws WebApplicationException, IOException {
        ByteArrayOutputStream s = new ByteArrayOutputStream();
        StructuredDataToJsonProvider.INSTANCE.writeTo(d, null, null, null, null, null, s);
        return s.toString();
    }

    public static final BindingIndependentMappingService getMappingService(SchemaContext context) {
        context = getSchemaContext();
        System.out.println("Building mapping service");
        BindingIndependentMappingService mappingService = new RuntimeGeneratedMappingServiceImpl(
                ClassPool.getDefault());
        ((RuntimeGeneratedMappingServiceImpl) mappingService).onGlobalContextUpdated(context);
        System.out.println("Mapping service built");
        return mappingService;
    }

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
    public static final String jsonStringFromDataObject(DataObject d,
            BindingIndependentMappingService mappingService, SchemaContext context)
            throws WebApplicationException, IOException {
        return jsonStringFromStructuredData(structuredDataFromDataObject(d, mappingService, context));
    }

    /**
     *
     * @param d
     * @return
     */
    public static final StructuredData structuredDataFromDataObject(DataObject d,
            BindingIndependentMappingService mappingService, SchemaContext context) {
        DataSchemaNode NDM_metadata = null;
        NDM_metadata = getSchemaNodeForDataObject(context, d);
        return new StructuredData(compositeNodeFromDataObject(d, mappingService), NDM_metadata,
                null, true);
    }

    public static final Set<DataSchemaNode> getAllTheNode(SchemaContext context) {
        Set<DataSchemaNode> nodes = new HashSet<DataSchemaNode>();
        getAllTheNodesHelper(context, nodes);
        return nodes;
    }

    private static final void getAllTheNodesHelper(DataNodeContainer dcn, Set<DataSchemaNode> nodes) {
        for (DataSchemaNode dsn : dcn.getChildNodes()) {
            if (dsn instanceof DataNodeContainer) {
                getAllTheNodesHelper((DataNodeContainer) dsn, nodes);
            }
            nodes.add(dsn);
        }
    }

    /**
     * DON'T CALL THIS IN PRODUCTION CODE EVER!!! UNTIL IT IS FIXED!
     * <p/>
     * Return the {@link DataSchemaNode}
     *
     * @param context
     * @param d
     * @deprecated
     */
    public static final DataSchemaNode getSchemaNodeForDataObject(SchemaContext context,
            DataObject d) {
        QName qn = BindingReflections.findQName(d.getClass());

        Set<DataSchemaNode> allTheNodes = getAllTheNode(context);

        // TODO: create a map to make this faster!!!!
        for (DataSchemaNode dsn : allTheNodes) {
            if (dsn instanceof DataNodeContainer) {
                allTheNodes.addAll(((DataNodeContainer) dsn).getChildNodes());
            }
            if (dsn.getQName().equals(qn)) {
                return dsn;
            }
        }
        return null;
    }

    /**
     *
     * @param d
     * @return
     */
    public static final CompositeNode compositeNodeFromDataObject(DataObject d,
            BindingIndependentMappingService mappingSerivce) {
        return mappingSerivce.toDataDom(d);
    }

}
