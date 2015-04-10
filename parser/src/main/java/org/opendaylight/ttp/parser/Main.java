package org.opendaylight.ttp.parser;

import com.google.common.base.Throwables;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import javax.ws.rs.WebApplicationException;
import org.opendaylight.ttp.utils.TTPUtils;
import org.opendaylight.yang.gen.v1.urn.onf.ttp.rev140711.OpendaylightTtps;
import org.opendaylight.yang.gen.v1.urn.onf.ttp.rev140711.opendaylight.ttps.TableTypePatterns;
import org.opendaylight.yang.gen.v1.urn.onf.ttp.rev140711.opendaylight.ttps.table.type.patterns.TableTypePattern;
import org.opendaylight.yang.gen.v1.urn.onf.ttp.rev140711.table.type.pattern.properties.NDMMetadata;
import org.opendaylight.yang.gen.v1.urn.onf.ttp.rev140711.table.type.pattern.properties.NDMMetadataBuilder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.util.BindingReflections;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

public class Main {

    public static void main(String args[]) throws WebApplicationException, IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        final TTPUtils ttpUtils;
        try {
            ttpUtils = new TTPUtils(Collections.singleton(BindingReflections.getModuleInfo(OpendaylightTtps.class)));
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }


        String ttpToParse = "{\"onf-ttp:opendaylight-ttps\": {\"table-type-patterns\": {\"table-type-pattern\": [ {\"NDM_metadata\": {\"authority\": \"org.opennetworking.fawg\",\"type\": \"TTPv1\",\"name\": \"L2-L3-ACLs\",\"version\": \"1.0.0\",\"OF_protocol_version\": \"1.3.3\",\"doc\": [\"Example of a TTP supporting L2 (unicast, multicast, flooding), L3 (unicast only),\", \"and an ACL table.\"]}}]}}}";
        NormalizedNode<?, ?> n = ttpUtils.normalizedNodeFromJsonString(ttpToParse);
        DataObject dataObj = ttpUtils.dataObjectFromNormalizedNode(n);
        if(dataObj instanceof OpendaylightTtps){
            System.out.println("It's a TTP!");
            String ttpInJSON = ttpUtils.jsonStringFromDataObject(
                    InstanceIdentifier.create(OpendaylightTtps.class), dataObj);
            System.out.println(ttpInJSON);
        }else{
            System.out.println("Bummer, not a TTP!");
        }

        String expectedJson = "{\"NDM_metadata\": {\"authority\": \"org.opennetworking.fawg\",\"type\": \"TTPv1\",\"name\": \"L2-L3-ACLs\",\"version\": \"1.0.0\",\"OF_protocol_version\": \"1.3.3\",\"doc\": [\"Example of a TTP supporting L2 (unicast, multicast, flooding), L3 (unicast only),\", \"and an ACL table.\"]}}";
        InstanceIdentifier<NDMMetadata> metaPath = InstanceIdentifier.create(OpendaylightTtps.class)
                .child(TableTypePatterns.class).child(TableTypePattern.class).child(NDMMetadata.class);
        NDMMetadata NDMmeta = new NDMMetadataBuilder()
                .setAuthority("org.opennetworking.fawg")
                .setType("TTPv1")
                .setName("L2-L3-ACLs")
                .setVersion("1.0.0")
                .setOFProtocolVersion("1.3.3")
                .setDoc(Arrays
                        .asList("Example of a TTP supporting L2 (unicast, multicast, flooding), L3 (unicast only),",
                                "and an ACL table.")).build();

        String jsonString = ttpUtils.jsonStringFromDataObject(metaPath,NDMmeta);
        JsonParser jp = new JsonParser();
        JsonElement je = jp.parse(expectedJson);
        String prettyExpectedJson = gson.toJson(je);
        System.out.println("Generated JSON:\n" + jsonString);
        System.out.println("Expected JSON:\n" + prettyExpectedJson);
        // ignore all the nulls
        /*
         * FIXME: Add implementation of XML using XmlNormalizedNodeStreamWriter
         */
    }
}
