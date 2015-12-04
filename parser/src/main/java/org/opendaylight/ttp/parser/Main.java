/*
 * Copyright (c) 2015 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

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
import org.opendaylight.yang.gen.v1.urn.onf.ttp.rev140711.table.type.pattern.properties.flow_tables.BuiltInFlowMods;
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
 * format &lt;ttp-file&gt;               // pretty print the ttp to stdout");
 * validate &lt;ttp-file&gt;             // check the syntax of the ttp");
 * compare &lt;ttp-file1&gt; &lt;ttp-file2&gt; // check 2 ttps for common flow paths");
 * dot &lt;ttp-file&gt;                  // generate dot formatted table map graphic to stdout");
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
            throw Throwables.propagate(e);
        }
    }

    public static void main(String args[]) throws WebApplicationException, IOException {

        if (args.length > 0) {
            switch (args[0]) {
            default:
            case "help":
                System.err.println("<cmd> syntax:");
                System.err.println("<cmd> help                            // print this message");
                System.err.println("<cmd> format <ttp-file>               // pretty print the ttp to stdout");
                System.err.println("<cmd> validate <ttp-file>             // check the syntax of the ttp");
                System.err.println("<cmd> compare <ttp-file1> <ttp-file2> // check 2 ttps for common flow paths");
                System.err.println("<cmd> dot <ttp-file>                  // generate dot formatted table map graphic to stdout");
                System.err.println("<cmd> flowpaths <ttp-file>            // generate list of flowpaths in ttp");
                break;
            case "format":
            case "validate":
                if (args.length != 2) {
                    System.err.println("<cmd> "+args[0]+" <ttp-file>                    // check the syntax of the ttp");
                }
                else {
                    OpendaylightTtps odlTTPs = readTTPFromFile(args[1]);
                    prettyPrintTTP(odlTTPs);
                }
                break;
            case "flowpaths":
                if (args.length != 2) {
                    System.err.println("<cmd> "+args[0]+" <ttp-file>                    // check the syntax of the ttp");
                }
                else {
                    OpendaylightTtps odlTTPs = readTTPFromFile(args[1]);
                    printFlowPaths(odlTTPs.getTableTypePatterns().getTableTypePattern().get(0));
                }
                break;
            case "dot":
                if (args.length != 2) {
                    System.err.println("<cmd> "+args[0]+" <ttp-file>                    // check the syntax of the ttp");
                }
                else {
                    OpendaylightTtps odlTTPs = readTTPFromFile(args[1]);
                    printDOT(odlTTPs.getTableTypePatterns().getTableTypePattern().get(0));
                }
                break;
            case "compare":
                System.err.println(args[0]+" not yet supported");
                break;
            }
        }
    }

    static OpendaylightTtps readTTPFromFile(String fileName) throws IOException {
        System.err.println("About to read in the ttp file: "+ fileName);
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
        Map<String, List<String>> legalTableHops = new HashMap<>();
        for (FlowTables flowTable : ttp.getFlowTables()) {
            List<String> dests = new ArrayList<>();
            if( flowTable.getFlowModTypes() != null ){
                for (FlowModTypes fmt : flowTable.getFlowModTypes()) {
                    dests.addAll(getGotoDests(fmt.getInstructionSet(), flowTable));
                }
            }
            if (flowTable.getBuiltInFlowMods() != null) {
                for (BuiltInFlowMods bifm : flowTable.getBuiltInFlowMods()) {
                    dests.addAll(getGotoDests(bifm.getInstructionSet(), flowTable));
                }
            }
            if (!dests.isEmpty()) {
                System.out.println("adding dest list to table hops " + flowTable.getName());

                legalTableHops.put(flowTable.getName(), dests);
            }
        }
        System.out.println("digraph ttp {");
        for (String source : legalTableHops.keySet()) {
            List<String> dests = legalTableHops.get(source);
            for (String dest : dests) {
                System.out.println("  \"" + source + "\" -> \"" + dest + "\";");
            }
        }
        System.out.println("}");
    }

    static List<String> getGotoDests(List<InstructionSet> inslist,
            FlowTables flowTable) {
        List<String> dests = new ArrayList<>();

        for (InstructionSet ins : inslist) {

            // get all the goto_table instructions even if they are in meta
            // keywords
            List<InstructionSetProperties> gotoTables = getGotoTables(ins);

            // add a legal hop from the flow table to the action listed in the
            // goto table
            for (InstructionSetProperties gotoTable : gotoTables) {
                String dest = gotoTable.getTable();
                if (dests.contains(dest)) {
                    System.out.println("dest already in list: " + dest);
                } else {
                    System.out.println("adding goto_table to table dests "
                            + flowTable.getName() + "," + gotoTable.getTable());
                    dests.add(dest);
                }
            }
        }
        return dests;
    }

    static List<InstructionSetProperties> getGotoTables(InstructionSet ins) {
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
        return gotoTables;
    }

    static void nextFlowPathHop(TableTypePattern ttp, String Path, String tableName, int recurse) {
        boolean nameFound = false;
        String noGoTo = "__NOGOTO__";

        if (recurse++ > 12) {
            System.out.println("excessive recursion, Path = " + Path);
            return;
        }
        if (tableName == noGoTo) {
            System.out.println(Path);
            return;
        }
        Path = Path.concat(tableName);
        for (FlowTables flowTable : ttp.getFlowTables()) {
            if (flowTable.getName().equals(tableName)) {
                nameFound = true;
//                System.out.println("found " + tableName);
                Map<String, Integer> destCounts = new HashMap<>();
                for (FlowModTypes fmt : flowTable.getFlowModTypes()) {
                    for (InstructionSet ins : fmt.getInstructionSet()) {
                        List<InstructionSetProperties> gotoTables = getGotoTables(ins);
                        if (gotoTables.isEmpty()) {
                            if (!destCounts.containsKey(noGoTo))
                                destCounts.put(noGoTo,1);
                            else
                                destCounts.put(noGoTo, destCounts.get(noGoTo) + 1);
                        }
                        for (InstructionSetProperties gotoTable : gotoTables) {
                            String dest = gotoTable.getTable();
                            if (destCounts.containsKey(dest))
                                destCounts.put(dest, destCounts.get(dest) + 1);
                            else
                                destCounts.put(dest,1);
                        }
                    }
                }
                if (destCounts.isEmpty())
                    System.out.println(Path + "[LAST TABLE has no FMTs!]");
                List<BuiltInFlowMods> bifmlist = flowTable.getBuiltInFlowMods();
                if (bifmlist != null) {
                    for (BuiltInFlowMods bifm : bifmlist) {
                        for (InstructionSet ins : bifm.getInstructionSet()) {
                            List<InstructionSetProperties> gotoTables = getGotoTables(ins);
                            if (gotoTables.isEmpty()) {
                                if (!destCounts.containsKey(noGoTo))
                                    destCounts.put(noGoTo,1);
                                else
                                    destCounts.put(noGoTo, destCounts.get(noGoTo) + 1);
                            }
                            for (InstructionSetProperties gotoTable : gotoTables) {
                                String dest = gotoTable.getTable();
                                if (destCounts.containsKey(dest))
                                    destCounts.put(dest, destCounts.get(dest) + 1);
                                else
                                    destCounts.put(dest,1);
                            }
                        }
                    }
                }
                for (String dest : destCounts.keySet())
                    nextFlowPathHop(ttp, Path.concat(" -> [" + destCounts.get(dest) + "]"), dest, recurse);
            }
        }
        if (!nameFound)
            System.out.println(Path + " [LAST TABLE NAME NOT FOUND!]");
    }

    static void printFlowPaths(TableTypePattern ttp) {
        String Path = "";
        System.out.println("Begin table names");
        for (FlowTables ftLoop : ttp.getFlowTables()) {
            System.out.println(ftLoop.getName());
        }
        System.out.println("End table names");
        System.out.println("");
        FlowTables flowTable = ttp.getFlowTables().get(0);
        String tableName = flowTable.getName();
        nextFlowPathHop(ttp, Path, tableName, 0);
    }
}
