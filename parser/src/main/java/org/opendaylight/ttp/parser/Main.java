package org.opendaylight.ttp.parser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import javax.ws.rs.WebApplicationException;

import org.opendaylight.controller.sal.rest.impl.StructuredDataToXmlProvider;
import org.opendaylight.controller.sal.restconf.impl.StructuredData;
import org.opendaylight.ttp.utils.TTPUtils;
import org.opendaylight.yang.gen.v1.urn.onf.ttp.rev140711.table.type.pattern.properties.NDMMetadata;
import org.opendaylight.yang.gen.v1.urn.onf.ttp.rev140711.table.type.pattern.properties.NDMMetadataBuilder;
import org.opendaylight.yangtools.yang.data.impl.codec.BindingIndependentMappingService;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class Main {
    public static void main(String args[]) throws WebApplicationException, IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        SchemaContext schemaContext = TTPUtils.getSchemaContext();
        BindingIndependentMappingService mappingService = TTPUtils.getMappingService(schemaContext);

        String expectedJson = "{\"NDM_metadata\": {\"authority\": \"org.opennetworking.fawg\",\"type\": \"TTPv1\",\"name\": \"L2-L3-ACLs\",\"version\": \"1.0.0\",\"OF_protocol_version\": \"1.3.3\",\"doc\": [\"Example of a TTP supporting L2 (unicast, multicast, flooding), L3 (unicast only),\", \"and an ACL table.\"]}}";
        NDMMetadata NDMmeta = new NDMMetadataBuilder()
                .setAuthority("org.opennetworking.fawg")
                .setType("TTPv1")
                .setName("L2-L3-ACLs")
                .setVersion("1.0.0")
                .setOFProtocolVersion("1.3.3")
                .setDoc(Arrays
                        .asList("Example of a TTP supporting L2 (unicast, multicast, flooding), L3 (unicast only),",
                                "and an ACL table.")).build();

        String jsonString = TTPUtils.jsonStringFromDataObject(NDMmeta, mappingService, schemaContext);
        JsonParser jp = new JsonParser();
        JsonElement je = jp.parse(expectedJson);
        String prettyExpectedJson = gson.toJson(je);
        System.out.println("Generated JSON:\n" + jsonString);
        System.out.println("Expected JSON:\n" + prettyExpectedJson);

        ByteArrayOutputStream s = new ByteArrayOutputStream();
        StructuredData sd = TTPUtils.structuredDataFromDataObject(NDMmeta, mappingService,
                schemaContext);
        // ignore all the nulls
        StructuredDataToXmlProvider.INSTANCE.writeTo(sd, null, null, null, null, null, s);
        System.out.println("Formatted XML From StructuredDataToXmlProvider:");
        String structuredDataXML = s.toString();
        System.out.println(structuredDataXML);
    }
}
