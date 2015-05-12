package org.opendaylight.ttp.parser;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.WebApplicationException;

import org.opendaylight.ttp.utils.TTPUtils;
import org.opendaylight.yang.gen.v1.urn.onf.ttp.rev140711.InstructionSetProperties;
import org.opendaylight.yang.gen.v1.urn.onf.ttp.rev140711.OpendaylightTtps;
import org.opendaylight.yang.gen.v1.urn.onf.ttp.rev140711.flow_mod.properties.InstructionSet;
import org.opendaylight.yang.gen.v1.urn.onf.ttp.rev140711.flow_mod.properties.instruction_set.ExactlyOne;
import org.opendaylight.yang.gen.v1.urn.onf.ttp.rev140711.flow_mod.properties.instruction_set.ZeroOrOne;
import org.opendaylight.yang.gen.v1.urn.onf.ttp.rev140711.opendaylight.ttps.table.type.patterns.TableTypePattern;
import org.opendaylight.yang.gen.v1.urn.onf.ttp.rev140711.table.type.pattern.properties.FlowTables;
import org.opendaylight.yang.gen.v1.urn.onf.ttp.rev140711.table.type.pattern.properties.flow_tables.FlowModTypes;
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

    static final TTPUtils ttpUtils;
    static {
        try {
            ttpUtils = new TTPUtils(Collections.singleton(BindingReflections
                    .getModuleInfo(OpendaylightTtps.class)));
        } catch (Exception e) {
            System.out.println("Exception!");
            throw Throwables.propagate(e);
        }
    }

    public static void main(String args[]) throws WebApplicationException, IOException {

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
            case "format":
            case "validate":
                if (args.length != 2) {
                    System.out.println("<cmd> "+args[0]+" <ttp-file>                    // check the syntax of the ttp");
                }
                else {
                    OpendaylightTtps odlTTPs = readTTPFromFile(args[1]);
                    prettyPrintTTP(odlTTPs);
                }
                break;
            case "dot":
                if (args.length != 2) {
                    System.out.println("<cmd> "+args[0]+" <ttp-file>                    // check the syntax of the ttp");
                }
                else {
                    OpendaylightTtps odlTTPs = readTTPFromFile(args[1]);
                    printDOT(odlTTPs.getTableTypePatterns().getTableTypePattern().get(0));
                }
                break;
            case "compare":
                System.out.println(args[0]+" not yet written");
                break;
            }
        }
    }

    static OpendaylightTtps readTTPFromFile(String fileName) throws IOException {
        System.out.println("About to read in the ttp file: "+ fileName);
        BufferedReader d = new BufferedReader(new
                InputStreamReader(new FileInputStream(fileName)));
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
            return (OpendaylightTtps)dataObj;
        }else{
            System.out.println("Bummer, not a TTP!");
            throw new IOException("The the TTP was msiformatted");
        }
    }

    static void prettyPrintTTP(OpendaylightTtps odlTTPs){
        String ttpInJSON = ttpUtils.jsonStringFromDataObject(
                InstanceIdentifier.create(OpendaylightTtps.class), odlTTPs, true);
        System.out.println(ttpInJSON);
    }

    static void printDOT(TableTypePattern ttp) {
        Map<String, String> legalTableHops = new HashMap<>();
        for (FlowTables flowTable : ttp.getFlowTables()) {
            for (FlowModTypes fmt : flowTable.getFlowModTypes()) {// ft.getFlowModTypes()) {
                for (InstructionSet ins : fmt.getInstructionSet()) {

                    // get all the goto_table instructions even if they are in meta keywords
                    List<InstructionSetProperties> gotoTables = new ArrayList<>();
                    if (ins.getInstruction() != null) {
                        if (ins.getInstruction().equalsIgnoreCase("goto_table")) {
                            gotoTables.add(ins);
                        }
                    }
                    if (ins.getExactlyOne() != null) {
                        for (ExactlyOne eo : ins.getExactlyOne()) {
                            if (eo.getInstruction().equalsIgnoreCase("goto_table")) {
                                gotoTables.add(eo);
                            }
                        }
                    }
                    if (ins.getZeroOrOne() != null) {
                        for (ZeroOrOne zoo : ins.getZeroOrOne()) {
                            if (zoo.getInstruction().equalsIgnoreCase("goto_table")) {
                                gotoTables.add(zoo);
                            }
                        }
                    }

                    // add a legal hop from the flow table to the action listed in the goto table
                    for (InstructionSetProperties gotoTable : gotoTables) {
                        legalTableHops.put(flowTable.getName(), gotoTable.getTable());
                    }
                }
            }
        }
        System.out.println("digraph ttp {");
        for (Map.Entry<String, String> hop : legalTableHops.entrySet()) {
            System.out.println("  \"" + hop.getKey() + "\" -> \"" + hop.getValue() + "\";");
        }
        System.out.println("}");
    }

}
