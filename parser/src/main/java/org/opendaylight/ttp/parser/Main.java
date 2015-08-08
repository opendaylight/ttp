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
import org.opendaylight.yang.gen.v1.urn.onf.ttp.rev140711.table.type.pattern.properties.flow_tables.FlowModTypes;
import org.opendaylight.yang.gen.v1.urn.onf.ttp.rev140711.table.type.pattern.properties.flow_tables.BuiltInFlowMods;
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
                System.out.println("<cmd> flowpaths <ttp-file>            // generate list of flowpaths in ttp");
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
            case "flowpaths":
                if (args.length != 2) {
                    System.out.println("<cmd> "+args[0]+" <ttp-file>                    // check the syntax of the ttp");
                }
                else {
                    OpendaylightTtps odlTTPs = readTTPFromFile(args[1]);
                    printFlowPaths(odlTTPs.getTableTypePatterns().getTableTypePattern().get(0));
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
        Map<String, List<String>> legalTableHops = new HashMap<>();
        for (FlowTables flowTable : ttp.getFlowTables()) {
            List<String> dests = new ArrayList<>();
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
                        String dest = gotoTable.getTable();
                        if (dests.contains(dest)) {
                            System.out.println("dest already in list: " + dest);
                        }
                        else {
                            System.out.println("adding goto_table to table dests " + flowTable.getName() + "," + gotoTable.getTable());
                            dests.add(dest);
                        }

                    }
                }
            }
            for (BuiltInFlowMods bifm : flowTable.getBuiltInFlowMods()) {// ft.getFlowModTypes()) {
                for (InstructionSet ins : bifm.getInstructionSet()) {

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
                        String dest = gotoTable.getTable();
                        if (dests.contains(dest)) {
                            System.out.println("dest already in list: " + dest);
                        }
                        else {
                            System.out.println("adding goto_table to table dests " + flowTable.getName() + "," + gotoTable.getTable());
                            dests.add(dest);
                        }

                    }
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

    static void nextFlowPathHop(TableTypePattern ttp, String Path, String tableName, int recurse) {
        boolean tableNameValid = false;

        if (recurse > 12) {
            System.out.println("excessive recursion, Path = " + Path);
            return;
        }
        recurse++;
        Path = Path.concat(tableName);
//  System.out.println("nextFlowPathHop called, table= " + tableName + " recurse= " + recurse);
        for (FlowTables flowTable : ttp.getFlowTables()) {
            if (flowTable.getName().equals(tableName)) {
                tableNameValid = true;
                int fmtCount = 0;
                List<String> dests = new ArrayList<>();
                for (FlowModTypes fmt : flowTable.getFlowModTypes()) {
                    boolean gotGoTo = false;
                    if (fmtCount++ > 100) {
                       System.out.println("more than 100 flowmodtypes in a table");
                       break;
                    }
                    for (InstructionSet ins : fmt.getInstructionSet()) {
                        if (ins.getInstruction() != null) {
                    if (ins.getInstruction().equalsIgnoreCase("goto_table")) {
                                gotGoTo = true;
                            String destName = ins.getTable();
                                if (!dests.contains(destName)) {
                                    dests.add(destName);
                                    nextFlowPathHop(ttp, Path.concat(" -> "), destName, recurse);
                                }
                    }
                    }
                    if (ins.getExactlyOne() != null) {
                    for (ExactlyOne eo : ins.getExactlyOne()) {
                        if (eo.getInstruction().equalsIgnoreCase("goto_table")) {
                                    gotGoTo = true;
                                String destName = eo.getTable();
                                    if (!dests.contains(destName)) {
                                        dests.add(destName);
                                        nextFlowPathHop(ttp, Path.concat(" -> "), destName, recurse);
                                    }
                        }
                    }
                    }
                    if (ins.getZeroOrOne() != null) {
                    for (ZeroOrOne zoo : ins.getZeroOrOne()) {
                        if (zoo.getInstruction().equalsIgnoreCase("goto_table")) {
                                    gotGoTo = true;
                                String destName = zoo.getTable();
                                    if (!dests.contains(destName)) {
                                        dests.add(destName);
                                        nextFlowPathHop(ttp, Path.concat(" -> "), destName, recurse);
                                    }
                        }
                    }
                    }

            }
                    if (!gotGoTo) {
                        if (!dests.contains("__NOGOTO__")) {
                            dests.add("__NOGOTO__");
                            System.out.println(Path);
                        }
                    }
            }
                if (fmtCount == 0) {
                    System.out.println(Path + "[LAST TABLE has no FMTs!]");
                }
//                System.out.println("beginning BuiltIns for " + tableName);
                for (BuiltInFlowMods bifm : flowTable.getBuiltInFlowMods()) {
                    boolean gotGoTo = false;
                    if (fmtCount++ > 100) {
                       System.out.println("more than 100 flowmodtypes and builtins in a table");
                       break;
                    }
                    for (InstructionSet ins : bifm.getInstructionSet()) {
                        if (ins.getInstruction() != null) {
                    if (ins.getInstruction().equalsIgnoreCase("goto_table")) {
                                gotGoTo = true;
                            String destName = ins.getTable();
                                if (!dests.contains(destName)) {
                                    dests.add(destName);
                                    nextFlowPathHop(ttp, Path.concat(" -> "), destName, recurse);
                                }
                    }
                    }
                    if (ins.getExactlyOne() != null) {
                    for (ExactlyOne eo : ins.getExactlyOne()) {
                        if (eo.getInstruction().equalsIgnoreCase("goto_table")) {
                                    gotGoTo = true;
                                String destName = eo.getTable();
                                    if (!dests.contains(destName)) {
                                        dests.add(destName);
                                        nextFlowPathHop(ttp, Path.concat(" -> "), destName, recurse);
                                    }
                        }
                    }
                    }
                    if (ins.getZeroOrOne() != null) {
                    for (ZeroOrOne zoo : ins.getZeroOrOne()) {
                        if (zoo.getInstruction().equalsIgnoreCase("goto_table")) {
                                    gotGoTo = true;
                                String destName = zoo.getTable();
                                    if (!dests.contains(destName)) {
                                        dests.add(destName);
                                        nextFlowPathHop(ttp, Path.concat(" -> "), destName, recurse);
                                    }
                        }
                    }
                    }

            }
                    if (!gotGoTo) {
                        if (!dests.contains("__NOGOTO__")) {
                            dests.add("__NOGOTO__");
                            System.out.println(Path);
                        }
                    }
            }
        }
        }
        if (!tableNameValid) {
            System.out.println(Path + " [LAST TABLE NOT FOUND!]");
        }
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
