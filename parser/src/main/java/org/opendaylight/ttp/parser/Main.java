package org.opendaylight.ttp.parser;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;

import javax.ws.rs.WebApplicationException;

import org.opendaylight.ttp.utils.TTPUtils;
import org.opendaylight.yang.gen.v1.urn.onf.ttp.rev140711.OpendaylightTtps;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.util.BindingReflections;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

import com.google.common.base.Throwables;

/**
 * Parser main function for command line manipulation of TTP files
 * <p>
 * Behavior depends on first argument
 * help                            // print this message");
 * format <ttp-file>               // pretty print the ttp to stdout");
 * validate <ttp-file>             // check the syntax of the ttp");
 * compare <ttp-file1> <ttp-file2> // check 2 ttps for common flow paths");
 * dot <ttp-file>                  // generate dot formatted table map graphic to stdout");
 * <p>
 * @author curtbeckmann
 *
 */
public class Main {

    public static void main(String args[]) throws WebApplicationException, IOException {

        final TTPUtils ttpUtils;
        try {
            ttpUtils = new TTPUtils(Collections.singleton(BindingReflections.getModuleInfo(OpendaylightTtps.class)));
        } catch (Exception e) {
            System.out.println("Exception!");
            throw Throwables.propagate(e);
        }

        if (args.length > 0) {
            switch (args[0]) {
            default:
            case "help":
                System.out.println("<cmd> syntax:");
                System.out.println("<cmd> help                            // print this message");
                System.out.println("<cmd> format <ttp-file>               // pretty print the ttp to stdout");
                System.out.println("<cmd> validate <ttp-file>             // check the syntax of the ttp");
                System.out.println("<cmd> compare <ttp-file1> <ttp-file2> // check 2 ttps for common flow paths");
                System.out.println("<cmd> dot <ttp-file>                  // generate dot formatted table map graphic to stdout");
                break;
            case "validate":
                if (args.length != 2) {
                    System.out.println("<cmd> validate <ttp-file>                    // check the syntax of the ttp");
                }
                else {
                    System.out.println("About to stream in the ttp file: "+ args[1]);
                    BufferedReader d = new BufferedReader(new
                            InputStreamReader(new FileInputStream(args[1])));
                    String count;
                    String ttpCore = "";
                    while((count = d.readLine()) != null){
                        ttpCore = ttpCore.concat(count+"\n");
                    }
                    d.close();

                    String ttpPrepend = "{\"onf-ttp:opendaylight-ttps\": {\"table-type-patterns\": {\"table-type-pattern\": [";
                    String ttpPostpend = "]}}}";
                    String ttpToParse =  ttpPrepend + ttpCore + ttpPostpend;
                    System.out.println("ttpToParse is:");
                    System.out.println(ttpToParse);
                    DataObject dataObj;
                    try {
                        NormalizedNode<?, ?> n = ttpUtils.normalizedNodeFromJsonString(ttpToParse);
                        System.out.println("Created the NormalizedNode");
                        dataObj = ttpUtils.dataObjectFromNormalizedNode(n);
                    } catch (Exception e) {
                        System.out.println("Exception creating DataObject from JSON string!");
                        throw Throwables.propagate(e);
                    }
                    if(dataObj instanceof OpendaylightTtps){
                        System.out.println("It's a TTP!");
                        // FIXME: Add implementation of XML using XmlNormalizedNodeStreamWriter
                        String ttpInJSON = ttpUtils.jsonStringFromDataObject(
                                InstanceIdentifier.create(OpendaylightTtps.class), dataObj, true);
                        System.out.println(ttpInJSON);
                    }else{
                        System.out.println("Bummer, not a TTP!");
                    }
                }
                break;
            case "format":
            case "compare":
            case "dot":
                System.out.println(args[0]+" not yet written");
                break;
            }
        }
    }
}
