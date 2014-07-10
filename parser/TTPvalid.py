#!/usr/local/bin/python

# Copyright (c) 2014 Brocade Communications Systems others. All rights reserved.
#
# This program and the accompanying materials are made available under the
# terms of the Eclipse Public License v1.0 which accompanies this distribution,
# and is available at http://www.eclipse.org/legal/epl-v10.html

import os
import json

def processDict(k,D):
    '''Doc string: process top level TTP Dictionary member'''
    Dkeys = list(D.keys())
    print (k, Dkeys)

def processList(k,L):
    '''Doc string: process top level TTP List member'''
    print (k, 'len=', len(L), "names:\n    ")
    for j in L[:]:
        if isinstance(j,dict):
            if "name" in j.keys():
                print (j["name"])
    print()

#should change "gather" function to fully process / validate Flow tables?
#so that FTList includes completely structured flow tables? (no missing members)
def gatherFTnames(FTlist):
    '''Doc string: return them name members of all flow tables'''
    ftNames = []
    for i in range(len(FTlist)):
        ft = FTlist[i]
        ftkeys = list(ft.keys())
        if "name" not in ftkeys:
            print("missing 'name' member in flow table #:",i)
        else:
            ftName = ft["name"]
            print("flow table",ftName,"members",ftkeys)
            if ftName in ftNames:
                print("Flow table name", ftName, "appears more than once!")
            else:
                ftNames.append(ftName)
    return ftNames
#
def gatherFTnums(tableMap,ftNames):
    '''check that some table maps to 0, and that number is unique'''
    ftNums=[]
    for k2, v2 in tableMap.items():
        if k2 not in ftNames:
            print ("Table map name not among flow table names:",k2)
        else:
    # check that v2 is an integer from 0 to 254?
            if v2 in ftNums:
                print("duplicate table number in Table Map!")
            else:
                ftNums.append(v2)
    if 0 not in ftNums:
        print ("Table Map does not include a zero table!")
    return ftNums;
#
def getDest(fm):
    dest = ()
    gotDest = False
    fmKeys = fm.keys()
#    print("getDest has len(iset) = ", len(isets))
    if "name" not in fmKeys:
        print("flow_mod_type missing name")
        return {}
    fmtName = fm["name"]
    if "instruction_set" not in fmKeys:
        print("flow mod has no 'instruction_set'... is that legal?")
        return ()
    iset = fm["instruction_set"]
    if not isinstance(iset,list):
        return ()
    for k in range(len(iset)):
        if not isinstance(iset[k],dict):
            return ()
        instKeys = iset[k].keys()
        if "instruction" not in instKeys:
            print("instruction not in instruction set element keys?")
            print("iset = ", iset, "k =", k)
            continue
        if iset[k]['instruction'] != "GOTO_TABLE":
            continue
        if "table" not in instKeys:
            print("missing 'table' member for GOTO inst")
            continue
        if len(instKeys) != 2:
            print("GOTO instruction has wrong number of members")
            continue
        if gotDest:
            print("more than 1 GOTO instruction")
            continue
        dest = fmtName,iset[k]['table']
        gotDest = True
    return dest
#
def gatherFTdests(flowTable):
    '''gather all goto destinations for a flow table'''
    dests={}
    ftKeys = flowTable.keys()
    if "flow_mod_types" in ftKeys:
        fmts = flowTable["flow_mod_types"]
        for j in range(len(fmts)):
            fmtKeys = fmts[j].keys()
            supportMeta = False
            if "all" in fmtKeys or "one_or_more" in fmtKeys or "zero_or_more" in fmtKeys:
#                print("fmtKeys=", fmtKeys)
                for meta in fmtKeys:
                    if meta not in {"all","one_or_more","zero_or_more"}:
                        print("meta situation, but non meta keyword")
                        continue
                    metaFmts = fmts[j][meta]
                    for k in range(len(metaFmts)):
                        dest = getDest(metaFmts[k])
                        if dest != ():
#                            print("metaFmtDest=", dest)
                            fmtName = dest[0]
                            # getDest should check for dup fmt names, add dest?
                            # that would mean dests would be a global... Hmm
                            if fmtName in dests.keys():
                                print("duplicate flow mod type name:", fmtName)
                                return {}
                            dests[fmtName]=dest[1]
            else:
                dest = getDest(fmts[j])
                if dest != ():
    #                print("fmtDest=", dest)
                    fmtName = dest[0]
                    if fmtName in dests.keys():
                        print("duplicate flow mod type name:", fmtName)
                        return {}
                    dests[fmtName]=dest[1]
    if "built_in_flow_mods" in ftKeys:
        bifms = flowTable["built_in_flow_mods"]
        for j in range(len(bifms)):
            dest = getDest(bifms[j])
            if dest != ():
#                print("bifmDest=", dest)
                fmtName = dest[0]
                if fmtName in dests.keys():
                    print("duplicate flow mod type name:", fmtName)
                    return {}
                dests[fmtName]=dest[1]
    return dests
#
# Need to pass the fmtNames and associated dests
#
#  getHop and gatherFTHhops NOT FINISHED!  WORK HERE!
def getHop(th, ftDests):
    thKeys = th.keys()
#    print("getDest has len(iset) = ", len(isets))
    if "name" not in thKeys:
        print("table_hop missing name")
        return ()
    hopName = th["name"]
    if "hops" not in thKeys:
        print("table hop", hopName, "has no hops member!")
        return ()
    hset = th["hops"]
    if not isinstance(hset,list):
        print ("table hops", hopName,"has a hops member that is not a list?")
        return ()
    thDestSet = False
    for k in hset:
        if k not in ftDests.keys():
            if not thDestSet:
                hopDest = None
                thDestSet = True
                continue
            elif thDestSet and hopDest == None:
                continue
            else:
                print("table hop",hopName,"hop member destinations not in agreement")
                return ()
        if not thDestSet:
            hopDest = ftDests[k]
            thDestSet = True
        else:
            if hopDest != ftDests[k]:
                print("table hop",hopName,"hop member destinations not in agreement")
                return ()
    return (hopName,hset)
#
# "MISS" is just a normal flowmod, should be handled with flow-mod-type name?
#
def gatherFThops(flowTable, ftDests):
    '''gather all table hops for a flow table'''
    hops={}
    ftKeys = flowTable.keys()
    if "table_hops" in ftKeys:
        ths = flowTable["table_hops"]
        for j in range(len(ths)):
            hop = getHop(ths[j], ftDests)
            if hop != ():
                print("hop =", hop)
                hopName = hop[0]
                if hopName in hops.keys():
                    print("duplicate table hop name:", hopName)
                    return {}
                hops[hopName] = hop[1]
    return hops

#
#
# MAIN PROGRAM BEGINS HERE
#
#should use command line argument (or prompt for) filename
f = open('L2-Simple-v1.0.0-d2.ttp', 'r')
#f = open('L2-L3-ACLs-v1.0.0.ttp', 'r')
TTP = json.load(f)
f.close()
TTPkeys = list(TTP.keys())
acceptedTTPkeys = ['NDM_metadata', 'variables', 'table_map', 'meter_table', 'flow_tables', 'group_entry_types', 'parameters', 'flow_paths', 'packet_out']
reqdTTPkeys = ['NDM_metadata', 'variables', 'table_map', 'meter_table', 'flow_tables', 'parameters', 'flow_paths', 'packet_out']
#first check that all keys are recognized
for k in TTPkeys:
    if k not in acceptedTTPkeys:
        print("unrecognized TTP member:",k)
#then walk thru required keys in order
#Note: if no map, can we just assign tables in order received?
#JSON does not preserve order, so that does not work
for k in reqdTTPkeys:
    if k not in TTPkeys:
        print ("missing required TTP member:",k)
        continue;
    v = TTP[k];
    vtype = type(v)
    if isinstance(v,dict):
        processDict(k,v)
    elif isinstance(v,list):
        processList(k,v)
# save table map and flow table for additional checks later
    if k == "table_map":
        tableMap = v;
    elif k == "flow_tables":
        if not isinstance(v,list): # what if it is a single instance?  List of length one, right?
            print("flow_tables instance should be a list!")
        flowTables = v;

ftNames = gatherFTnames(flowTables) # gather flow table names
if len(tableMap) != len(ftNames): # table map size same as # of FT names found?
    print ("table_map and flow_table are different lengths!")

ftNums = gatherFTnums(tableMap, ftNames)
dests = []
destFtNames = []
goodGotos = True
for i in range(len(flowTables)):
    ftName = flowTables[i]["name"]
    ftDests = gatherFTdests(flowTables[i])
    ftHops = gatherFThops(flowTables[i], ftDests)
    ftNum = tableMap[ftName]
    print("FT#", ftNum,"named",ftName)
    if len(ftDests) != 0:
        dests.append(ftDests)
        print(" has these FM:dest combos")
        if ftName in tableMap.keys():
            for k, v in ftDests.items():
                print("...",k,":",v)
                if v not in tableMap.keys():
                    print("Problem: flow_mod",k,"goes to unknown flow table",v)
                    goodGotos = False
                    continue
                toNum = tableMap[v]
                if toNum <= ftNum:
                    goodGotos = False
                    print("Problem: flow_mod",k,"goes to same or earlier table",v,"#",toNum)
                if v not in destFtNames:
                    destFtNames.append(v)
    else:
        print(" is terminal")
for ftName in ftNames:
    if tableMap[ftName] == 0:
        continue       # we start at table 0, it doesn't need to be a dest
    if ftName not in destFtNames:
        print ("Flow Table ", ftName, "cannot be reached (not a destination)")
        goodGotos = False

if goodGotos:
    print("Gotos look good")
else:
    print("Gotos have some issues")
exit

# Rewrite this validator to use a walk a tree that represents what works?
## Or is that less interesting if some schema-driven JSON validator already ran?
## Can this python tool actually invoke the schema-driven validator?
## Thus making the full validation a single step from users perspective?
#
# Other:
# generate table graph (using hops, if possible) in some form?
# possibly separate step, generate table graph in GraphML?
#Do all flow-mods and built-ins? (Maybe only where special processing needed)
    #(Done for Gotos, need to do fully...)
#check that built-ins are fully specified? (How much does JSON schema do on this one?)
#identify flow-mods, group-mods, etc, that are fully (over?) specified? (
#recognize variable names in flowmods, etc? (check that they are all defined)
#list variables and group by type
    # for simulation or test generation, variables could be given value ranges
    # tool could generate flow-mods and packets that assign values within the ranges
    # (can recognize if variables of same type have overlapping allowed ranges)
#check each main member more vigorously: no extra cruft, etc?
#
# generate TTP skeleton?  Provide schemes to add flesh to skeleton?

#DONE stuff:
#build the graph from goto's (check flow-mod-types and built-ins)
#each flow mod has at most one GOTO (DONE?)
#associate goto dest with flow mod name (DONE?)
##list flowtable destinations by name as processing (DONE?)
##check that all dest names are FT names (DONE?)
        #map dests to table number (DONE)
##check that all gotos are forward (DONE)
##check that all non-zero tables are reachable (DONE)
##list terminal tables? (DONE)
#gather table_hops for each flow_table for efficient visualization
##all flow_mods within a table hop must goto same dest (right?)
