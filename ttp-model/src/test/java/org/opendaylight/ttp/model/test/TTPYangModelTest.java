/*
 * Copyright (c) 2014 Brocade Communications Systems others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.ttp.model.test;

import com.google.common.base.Throwables;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.ttp.utils.TTPUtils;
import org.opendaylight.yang.gen.v1.urn.onf.ttp.rev140711.MatchSetProperties.MatchType;
import org.opendaylight.yang.gen.v1.urn.onf.ttp.rev140711.OpendaylightTtps;
import org.opendaylight.yang.gen.v1.urn.onf.ttp.rev140711.flow_mod.properties.InstructionSet;
import org.opendaylight.yang.gen.v1.urn.onf.ttp.rev140711.flow_mod.properties.InstructionSetBuilder;
import org.opendaylight.yang.gen.v1.urn.onf.ttp.rev140711.flow_mod.properties.MatchSet;
import org.opendaylight.yang.gen.v1.urn.onf.ttp.rev140711.flow_mod.properties.MatchSetBuilder;
import org.opendaylight.yang.gen.v1.urn.onf.ttp.rev140711.instruction_set.properties.ActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.onf.ttp.rev140711.opendaylight.ttps.TableTypePatterns;
import org.opendaylight.yang.gen.v1.urn.onf.ttp.rev140711.opendaylight.ttps.TableTypePatternsBuilder;
import org.opendaylight.yang.gen.v1.urn.onf.ttp.rev140711.opendaylight.ttps.table.type.patterns.TableTypePattern;
import org.opendaylight.yang.gen.v1.urn.onf.ttp.rev140711.opendaylight.ttps.table.type.patterns.TableTypePatternBuilder;
import org.opendaylight.yang.gen.v1.urn.onf.ttp.rev140711.table.type.pattern.properties.Features;
import org.opendaylight.yang.gen.v1.urn.onf.ttp.rev140711.table.type.pattern.properties.FeaturesBuilder;
import org.opendaylight.yang.gen.v1.urn.onf.ttp.rev140711.table.type.pattern.properties.FlowPaths;
import org.opendaylight.yang.gen.v1.urn.onf.ttp.rev140711.table.type.pattern.properties.FlowPathsBuilder;
import org.opendaylight.yang.gen.v1.urn.onf.ttp.rev140711.table.type.pattern.properties.FlowTables;
import org.opendaylight.yang.gen.v1.urn.onf.ttp.rev140711.table.type.pattern.properties.FlowTablesBuilder;
import org.opendaylight.yang.gen.v1.urn.onf.ttp.rev140711.table.type.pattern.properties.GroupEntryTypes;
import org.opendaylight.yang.gen.v1.urn.onf.ttp.rev140711.table.type.pattern.properties.GroupEntryTypesBuilder;
import org.opendaylight.yang.gen.v1.urn.onf.ttp.rev140711.table.type.pattern.properties.Identifiers;
import org.opendaylight.yang.gen.v1.urn.onf.ttp.rev140711.table.type.pattern.properties.IdentifiersBuilder;
import org.opendaylight.yang.gen.v1.urn.onf.ttp.rev140711.table.type.pattern.properties.MeterTable;
import org.opendaylight.yang.gen.v1.urn.onf.ttp.rev140711.table.type.pattern.properties.MeterTableBuilder;
import org.opendaylight.yang.gen.v1.urn.onf.ttp.rev140711.table.type.pattern.properties.NDMMetadata;
import org.opendaylight.yang.gen.v1.urn.onf.ttp.rev140711.table.type.pattern.properties.NDMMetadataBuilder;
import org.opendaylight.yang.gen.v1.urn.onf.ttp.rev140711.table.type.pattern.properties.Parameters;
import org.opendaylight.yang.gen.v1.urn.onf.ttp.rev140711.table.type.pattern.properties.ParametersBuilder;
import org.opendaylight.yang.gen.v1.urn.onf.ttp.rev140711.table.type.pattern.properties.Security;
import org.opendaylight.yang.gen.v1.urn.onf.ttp.rev140711.table.type.pattern.properties.SecurityBuilder;
import org.opendaylight.yang.gen.v1.urn.onf.ttp.rev140711.table.type.pattern.properties.TableMap;
import org.opendaylight.yang.gen.v1.urn.onf.ttp.rev140711.table.type.pattern.properties.TableMapBuilder;
import org.opendaylight.yang.gen.v1.urn.onf.ttp.rev140711.table.type.pattern.properties.flow_tables.BuiltInFlowMods;
import org.opendaylight.yang.gen.v1.urn.onf.ttp.rev140711.table.type.pattern.properties.flow_tables.BuiltInFlowModsBuilder;
import org.opendaylight.yang.gen.v1.urn.onf.ttp.rev140711.table.type.pattern.properties.flow_tables.FlowModTypes;
import org.opendaylight.yang.gen.v1.urn.onf.ttp.rev140711.table.type.pattern.properties.flow_tables.FlowModTypesBuilder;
import org.opendaylight.yang.gen.v1.urn.onf.ttp.rev140711.table.type.pattern.properties.group_entry_types.BucketTypes;
import org.opendaylight.yang.gen.v1.urn.onf.ttp.rev140711.table.type.pattern.properties.group_entry_types.BucketTypesBuilder;
import org.opendaylight.yang.gen.v1.urn.onf.ttp.rev140711.table.type.pattern.properties.group_entry_types.bucket_types.ActionSet;
import org.opendaylight.yang.gen.v1.urn.onf.ttp.rev140711.table.type.pattern.properties.group_entry_types.bucket_types.ActionSetBuilder;
import org.opendaylight.yang.gen.v1.urn.onf.ttp.rev140711.table.type.pattern.properties.meter_table.BuiltInMeters;
import org.opendaylight.yang.gen.v1.urn.onf.ttp.rev140711.table.type.pattern.properties.meter_table.BuiltInMetersBuilder;
import org.opendaylight.yang.gen.v1.urn.onf.ttp.rev140711.table.type.pattern.properties.meter_table.MeterTypes;
import org.opendaylight.yang.gen.v1.urn.onf.ttp.rev140711.table.type.pattern.properties.meter_table.MeterTypesBuilder;
import org.opendaylight.yang.gen.v1.urn.onf.ttp.rev140711.table.type.pattern.properties.meter_table.meter_types.Bands;
import org.opendaylight.yang.gen.v1.urn.onf.ttp.rev140711.table.type.pattern.properties.meter_table.meter_types.Bands.Type;
import org.opendaylight.yang.gen.v1.urn.onf.ttp.rev140711.table.type.pattern.properties.meter_table.meter_types.BandsBuilder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.util.BindingReflections;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.w3c.dom.Document;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

/**
 * This test actually tests the YANG model for TTPs to ensure that it matches the expected JSON from
 * the ONF TTP spec. For now, many of the examples are loosely borrowed from the spec's L2-L3-ACL
 * example.
 * <p>
 * There are a few places where the ODL, YANG-modeled TTP differs from the spec including:
 * <li> "doc" entries must always be a list of strings and cannot be a single string
 * <li> the table_map format differs significantly
 * <li> in flow_paths, repeated tables are represented as "[table_name]" instead of ["table_name"]
 * <p>
 * TODO: test reading from JSON
 */
public class TTPYangModelTest {

    /*
     * Used only for pretty-printing expected JSON strings
     */

    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    JsonParser jp = new JsonParser();
    private static final TTPUtils TTP_UTILS;


    /*
     * State setup. This had to be static to work with @BeforeClass and we had to use that instead
     * of @Before in order to amortize the cost of this stuff.
     */

    private static final InstanceIdentifier<TableTypePatterns> TTP_PATTERNS =
            InstanceIdentifier.create(OpendaylightTtps.class)
                .child(TableTypePatterns.class);


    static {

        try {
            TTP_UTILS = new TTPUtils(Collections.singleton(BindingReflections.getModuleInfo(OpendaylightTtps.class)));
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    static final String ttpWrapperBefore = "{\"table-type-patterns\": {\"table-type-pattern\":[";
    static final String ttpWrapperAfter = "]}}";

    static String wrapJSONStringInTTP(final String json){
        return ttpWrapperBefore+json+ttpWrapperAfter;
    }

    /*
     * TTP YANG Model Tests
     */

    @Test
    public void testNDMMetadata() throws Exception {
        /*
         * test NDM_metadata slug JSON conversion
         */
        final String expectedJson = "{\"NDM_metadata\": {\"authority\": \"org.opennetworking.fawg\",\"type\": \"TTPv1\",\"name\": \"L2-L3-ACLs\",\"version\": \"1.0.0\",\"OF_protocol_version\": \"1.3.3\",\"doc\": [\"Example of a TTP supporting L2 (unicast, multicast, flooding), L3 (unicast only),\", \"and an ACL table.\"]}}";
        final NDMMetadata NDMmeta = new NDMMetadataBuilder()
                .setAuthority("org.opennetworking.fawg")
                .setType("TTPv1")
                .setName("L2-L3-ACLs")
                .setVersion("1.0.0")
                .setOFProtocolVersion("1.3.3")
                .setDoc(Arrays
                        .asList("Example of a TTP supporting L2 (unicast, multicast, flooding), L3 (unicast only),",
                                "and an ACL table.")).build();
        assertConvertedJSONEquals(wrapJSONStringInTTP(expectedJson), new TableTypePatternBuilder().setNDMMetadata(NDMmeta).build());
        // FIXME: Add XML examle
        InstanceIdentifier<NDMMetadata> metaPath = InstanceIdentifier.create(OpendaylightTtps.class)
                .child(TableTypePatterns.class).child(TableTypePattern.class).child(NDMMetadata.class);

        String jsonString = TTP_UTILS.jsonStringFromDataObject(metaPath,NDMmeta);
        Assert.assertNotNull(jsonString);
    }

    @Test
    public void testIdentifiers() throws Exception {
        final Identifiers id1 = new IdentifiersBuilder().setVar("<subnet_VID>")
                .setDoc(Arrays.asList("The VLAN ID of a locally attached L2 subnet on a Router."))
                .build();
        final Identifiers id2 = new IdentifiersBuilder()
                .setVar("<<group_entry_types:name>>")
                .setDoc(Arrays.asList(
                        "An OpenFlow group identifier (integer) identifying a group table entry",
                        "of the type indicated by the variable name")).build();
        final TableTypePattern ttp = new TableTypePatternBuilder().setIdentifiers(Arrays.asList(id1, id2)).build();
        // Note that to create a list of identifiers it has to be inside the TableTypePattern
        final String expectedStr = "{\"identifiers\": [{\"var\": \"<subnet_VID>\",\"doc\": [\"The VLAN ID of a locally attached L2 subnet on a Router.\"]},{\"var\": \"<<group_entry_types:name>>\",\"doc\": [\"An OpenFlow group identifier (integer) identifying a group table entry\", \"of the type indicated by the variable name\"]}]}";
        assertConvertedJSONEquals(wrapJSONStringInTTP(expectedStr), ttp);
    }

    @Test
    public void testFeatures() throws Exception {
        final Features f1 = new FeaturesBuilder()
                .setFeature("ext187")
                .setDoc(Arrays
                        .asList("Flow entry notification Extension â€“ notification of changes in flow entries"))
                .build();
        final Features f2 = new FeaturesBuilder()
                .setFeature("ext235")
                .setDoc(Arrays
                        .asList("Group notifications Extension â€“ notification of changes in group or meter entries"))
                .build();
        // Note that to create a list if features it has to be inside the TableTypePattern
        final TableTypePattern ttp = new TableTypePatternBuilder().setFeatures(Arrays.asList(f1, f2))
                .build();
        final String expectedStr = "{\"features\": [{\"feature\": \"ext187\",\"doc\": [\"Flow entry notification Extension â€“ notification of changes in flow entries\"]}, {\"feature\": \"ext235\",\"doc\": [\"Group notifications Extension â€“ notification of changes in group or meter entries\"]} ]}";
        assertConvertedJSONEquals(wrapJSONStringInTTP(expectedStr), ttp);
    }

    @Test
    public void testTableMap() throws Exception {
        final TableMap tm1 = new TableMapBuilder().setName("ControlFrame").setNumber((short) 0).build();
        final TableMap tm2 = new TableMapBuilder().setName("IngressVLAN").setNumber((short) 10).build();
        final TableMap tm3 = new TableMapBuilder().setName("MacLearning").setNumber((short) 20).build();
        final TableMap tm4 = new TableMapBuilder().setName("ACL").setNumber((short) 30).build();
        final TableMap tm5 = new TableMapBuilder().setName("L2").setNumber((short) 40).build();
        final TableMap tm6 = new TableMapBuilder().setName("ProtoFilter").setNumber((short) 50).build();
        final TableMap tm7 = new TableMapBuilder().setName("IPv4").setNumber((short) 60).build();
        final TableMap tm8 = new TableMapBuilder().setName("IPv6").setNumber((short) 80).build();
        final TableTypePattern ttp = new TableTypePatternBuilder().setTableMap(
                Arrays.asList(tm1, tm2, tm3, tm4, tm5, tm6, tm7, tm8)).build();
        final String expectedStr = "{\"table_map\": [{\"name\": \"ControlFrame\", \"number\": 0},{\"name\": \"IngressVLAN\", \"number\": 10},{\"name\": \"MacLearning\", \"number\": 20},{\"name\": \"ACL\", \"number\": 30},{\"name\": \"L2\", \"number\": 40},{\"name\": \"ProtoFilter\", \"number\": 50},{\"name\": \"IPv4\", \"number\": 60},{\"name\": \"IPv6\", \"number\": 80}]}";
        assertConvertedJSONEquals(wrapJSONStringInTTP(expectedStr), ttp);
    }

    @Test
    public void testMeterTable() throws Exception {
        final Bands band1 = new BandsBuilder().setType(Type.DROP).setRate("1000..10000")
                .setBurst("50..200").build();
        final MeterTypes meterType1 = new MeterTypesBuilder().setBands(Arrays.asList(band1))
                .setName("ControllerMeterType").build();

        final Bands band2 = new BandsBuilder().setType(Type.DSCPREMARK).setRate("10000..500000")
                .setBurst("50..500").build();
        final Bands band3 = new BandsBuilder().setType(Type.DROP).setRate("10000..500000")
                .setBurst("50..500").build();
        final MeterTypes meterType2 = new MeterTypesBuilder().setBands(Arrays.asList(band2, band3))
                .setName("TrafficMeter").build();

        final org.opendaylight.yang.gen.v1.urn.onf.ttp.rev140711.table.type.pattern.properties.meter_table.built_in_meters.Bands builtInBand1 =
                new org.opendaylight.yang.gen.v1.urn.onf.ttp.rev140711.table.type.pattern.properties.meter_table.built_in_meters.BandsBuilder()
                .setRate((long) 2000).setBurst((long) 75).build();
        final BuiltInMeters builtInMeter1 = new BuiltInMetersBuilder().setName("ControllerMeter")
                .setMeterId((long) 1).setType("ControllerMeterType")
                .setBands(Arrays.asList(builtInBand1)).build();
        final org.opendaylight.yang.gen.v1.urn.onf.ttp.rev140711.table.type.pattern.properties.meter_table.built_in_meters.Bands builtInBand2 =
                new org.opendaylight.yang.gen.v1.urn.onf.ttp.rev140711.table.type.pattern.properties.meter_table.built_in_meters.BandsBuilder()
                .setRate((long) 1000).setBurst((long) 50).build();
        final BuiltInMeters builtInMeter2 = new BuiltInMetersBuilder().setName("AllArpMeter")
                .setMeterId((long) 2).setType("ControllerMeterType")
                .setBands(Arrays.asList(builtInBand2)).build();

        final MeterTable meterTable = new MeterTableBuilder()
                .setMeterTypes(Arrays.asList(meterType1, meterType2))
                .setBuiltInMeters(Arrays.asList(builtInMeter1, builtInMeter2)).build();

        final String expectedStr = "{\"meter_table\": {\"meter_types\": [{\"name\": \"ControllerMeterType\",\"bands\": [{\"type\": \"DROP\", \"rate\": \"1000..10000\", \"burst\": \"50..200\"}]},{\"name\": \"TrafficMeter\",\"bands\": [{\"type\": \"DSCP_REMARK\", \"rate\": \"10000..500000\", \"burst\": \"50..500\"}, {\"type\": \"DROP\", \"rate\": \"10000..500000\", \"burst\": \"50..500\"}]} ],\"built_in_meters\": [{\"name\": \"ControllerMeter\", \"meter_id\": 1,\"type\": \"ControllerMeterType\", \"bands\": [{\"rate\": 2000, \"burst\": 75}]}, {\"name\": \"AllArpMeter\", \"meter_id\": 2,\"type\": \"ControllerMeterType\", \"bands\": [{\"rate\": 1000, \"burst\": 50}]} ]}}";
        assertConvertedJSONEquals(wrapJSONStringInTTP(expectedStr), new TableTypePatternBuilder().setMeterTable(meterTable).build());
    }

    @Test
    public void testFlowTables() throws Exception {
        // TODO: write test
        final MatchSet matchSet1 = new MatchSetBuilder().setField("ETH_TYPE")
                .setMatchType(MatchType.AllOrExact).build();
        final MatchSet matchSet2 = new MatchSetBuilder().setField("ETH_DST")
                .setMatchType(MatchType.Exact).build();

        final InstructionSet instSet1 = new InstructionSetBuilder()
                .setInstruction("METER")
                .setMeterName("ControllerMeter")
                .setDoc(Arrays.asList(
                        "This meter may be used to limit the rate of PACKET_IN frames",
                        "sent to the controller")).build();
        final InstructionSet instSet2 = new InstructionSetBuilder()
                .setInstruction("APPLY_ACTIONS")
                .setActions(
                        Arrays.asList(new ActionsBuilder().setAction("OUTPUT")
                                .setPort("CONTROLLER").build())).build();

        final FlowModTypes flowModType = new FlowModTypesBuilder()
                .setName("Frame-To-Controller")
                .setDoc(Arrays.asList(
                        "This match/action pair allows for flow_mods that match on either",
                        "ETH_TYPE or ETH_DST (or both) and send the packet to the",
                        "controller, subject to metering."))
                .setMatchSet(Arrays.asList(matchSet1, matchSet2))
                .setInstructionSet(Arrays.asList(instSet1, instSet2)).build();

        final BuiltInFlowMods builtInFlowMod1 = new BuiltInFlowModsBuilder()
                .setName("Control-Frame-Filter")
                .setDoc(Arrays
                        .asList("Mandatory filtering of control frames with C-VLAN Bridge reserved DA."))
                .setPriority("1")
                .setMatchSet(
                        Arrays.asList(new MatchSetBuilder().setField("ETH_DST")
                                .setMask("0xfffffffffff0").setValue("0x0180C2000000").build()))
                .build();
        final BuiltInFlowMods builtInFlowMod2 = new BuiltInFlowModsBuilder()
                .setName("Non-Control-Frame")
                .setDoc(Arrays
                        .asList("Mandatory miss flow_mod, sends packets to IngressVLAN table."))
                .setPriority("0")
                .setMatchSet(new ArrayList<MatchSet>())
                .setInstructionSet(
                        Arrays.asList(new InstructionSetBuilder().setInstruction("GOTO_TABLE")
                                .setTable("IngressVLAN").build())).build();

        final FlowTables flowTable = new FlowTablesBuilder()
                .setName("ControlFrame")
                .setDoc(Arrays.asList("Filters L2 control reserved destination addresses and",
                        "may forward control packets to the controller.",
                        "Directs all other packets to the Ingress VLAN table."))
                .setFlowModTypes(Arrays.asList(flowModType))
                .setBuiltInFlowMods(Arrays.asList(builtInFlowMod1, builtInFlowMod2)).build();

        // TODO: for reasons that are entirely unclear to me, serializing this resulted in the
        //       "flow_table" key being omitted. My guess is using the wrong serialization.
        // TableTypePattern ttp = new TableTypePatternBuilder()
        //         .setFlowTables(Arrays.asList(flowTable)).build();

        final String expectedStr = "{\"name\": \"ControlFrame\",\"doc\": [\"Filters L2 control reserved destination addresses and\",\"may forward control packets to the controller.\",\"Directs all other packets to the Ingress VLAN table.\"],\"flow_mod_types\": [{\"name\": \"Frame-To-Controller\",\"doc\": [\"This match/action pair allows for flow_mods that match on either\",\"ETH_TYPE or ETH_DST (or both) and send the packet to the\",\"controller, subject to metering.\"],\"match_set\": [{\"field\": \"ETH_TYPE\",\"match_type\": \"all_or_exact\"},{\"field\": \"ETH_DST\",\"match_type\": \"exact\"}],\"instruction_set\": [{\"instruction\": \"METER\",\"meter_name\": \"ControllerMeter\",\"doc\": [\"This meter may be used to limit the rate of PACKET_IN frames\",\"sent to the controller\"]},{\"instruction\": \"APPLY_ACTIONS\",\"actions\": [{\"action\": \"OUTPUT\",\"port\": \"CONTROLLER\"}]}]}],\"built_in_flow_mods\": [{\"name\": \"Control-Frame-Filter\",\"doc\": [\"Mandatory filtering of control frames with C-VLAN Bridge reserved DA.\"],\"priority\": \"1\",\"match_set\": [{\"field\": \"ETH_DST\",\"mask\": \"0xfffffffffff0\",\"value\": \"0x0180C2000000\"}]},{\"name\": \"Non-Control-Frame\",\"doc\": [\"Mandatory miss flow_mod, sends packets to IngressVLAN table.\"],\"priority\": \"0\",\"instruction_set\": [{\"instruction\": \"GOTO_TABLE\",\"table\": \"IngressVLAN\"}]}]}";
        assertConvertedJSONEquals(wrapJSONStringInTTP("{\"flow_tables\":["+expectedStr+"]}"), new TableTypePatternBuilder()
            .setFlowTables(Collections.singletonList(flowTable))
            .build());
    }

    @Test
    public void testGroupTableEntries() throws Exception {
        final ActionSet actSet1 = new ActionSetBuilder().setAction("OUTPUT").setPort("<port_no>").build();
        final BucketTypes bucket1 = new BucketTypesBuilder().setName("OutputTagged")
                .setActionSet(Arrays.asList(actSet1)).build();

        final ActionSet actSet2 = new ActionSetBuilder().setAction("POP_VLAN").build();
        final ActionSet actSet3 = new ActionSetBuilder().setAction("OUTPUT").setPort("<port_no>").build();
        final BucketTypes bucket2 = new BucketTypesBuilder().setName("OutputUntagged")
                .setActionSet(Arrays.asList(actSet2, actSet3)).build();

        final ActionSet actSet4 = new ActionSetBuilder().setAction("SET_FIELD").setField("VLAN_VID")
                .setValue("<local_vid>").build();
        final ActionSet actSet5 = new ActionSetBuilder().setAction("OUTPUT").setPort("<port_no>").build();
        final BucketTypes bucket3 = new BucketTypesBuilder().setName("OutputVIDTranslate")
                .setOptTag("VID-X").setActionSet(Arrays.asList(actSet4, actSet5)).build();

        final GroupEntryTypes groupEntries = new GroupEntryTypesBuilder()
                .setDoc(Arrays.asList("Output to a port, removing VLAN tag if needed.",
                        "Entry per port, plus entry per untagged VID per port."))
                .setName("EgressPort").setGroupType("INDIRECT")
                .setBucketTypes(Arrays.asList(bucket1, bucket2, bucket3)).build();

        final TableTypePattern ttp = new TableTypePatternBuilder().setGroupEntryTypes(
                Arrays.asList(groupEntries)).build();

        final String expectedStr = "{\"group_entry_types\": [{\"name\": \"EgressPort\",\"doc\": [\"Output to a port, removing VLAN tag if needed.\",\"Entry per port, plus entry per untagged VID per port.\"],\"group_type\": \"INDIRECT\",\"bucket_types\": [{\"name\": \"OutputTagged\",\"action_set\": [{\"action\": \"OUTPUT\", \"port\": \"<port_no>\"}]},{\"name\": \"OutputUntagged\",\"action_set\": [{\"action\": \"POP_VLAN\"},{\"action\": \"OUTPUT\", \"port\": \"<port_no>\" }]},{\"opt_tag\": \"VID-X\",\"name\": \"OutputVIDTranslate\",\"action_set\": [{\"action\": \"SET_FIELD\", \"field\": \"VLAN_VID\", \"value\": \"<local_vid>\"},{\"action\": \"OUTPUT\", \"port\": \"<port_no>\" }]} ]}]}";

        assertConvertedJSONEquals(wrapJSONStringInTTP(expectedStr), ttp);
    }

    @Test
    public void testPacketOut() throws Exception {
        //TODO: write test
        // Not testing yet as there are no examples in the spec
    }

    @Test
    public void testParameters() throws Exception {
        /*
         * test parameters slug JSON conversion
         */
        final Parameters p = new ParametersBuilder().setDoc(Arrays.asList("documentation"))
                .setName("Showing-curt-how-this-works").setType("type1").build();
        final String expectedStr = "{\"parameters\":[{\"doc\":[\"documentation\"],\"name\":\"Showing-curt-how-this-works\",\"type\":\"type1\"}]}";
        assertConvertedJSONEquals(wrapJSONStringInTTP(expectedStr), new TableTypePatternBuilder()
            .setParameters(Collections.singletonList(p))
            .build());
    }

    @Test
    public void testFlowPaths() throws Exception {
        final FlowPaths flowPath1 = new FlowPathsBuilder()
                .setDoc(Arrays.asList(
                        "This object contains just a few examples of flow paths, it is not",
                        "a comprehensive list of the flow paths required for this TTP.  It is",
                        "intended that the flow paths array could include either a list of",
                        "required flow paths or a list of specific flow paths that are not",
                        "required (whichever is more concise or more useful."))
                .setName("L2-2")
                .setPath(
                        Arrays.asList("Non-Control-Frame", "IV-pass", "Known-MAC", "ACLskip",
                                "L2-Unicast", "EgressPort")).build();
        final FlowPaths flowPath2 = new FlowPathsBuilder()
                .setName("L2-3")
                .setPath(
                        Arrays.asList("Non-Control-Frame", "IV-pass", "Known-MAC", "ACLskip",
                                "L2-Multicast", "L2Mcast", "[EgressPort]")).build();
        final FlowPaths flowPath3 = new FlowPathsBuilder()
                .setName("L2-4")
                .setPath(
                        Arrays.asList("Non-Control-Frame", "IV-pass", "Known-MAC", "ACL-skip",
                                "VID-flood", "VIDflood", "[EgressPort]")).build();
        final FlowPaths flowPath4 = new FlowPathsBuilder()
                .setName("L2-5")
                .setPath(
                        Arrays.asList("Non-Control-Frame", "IV-pass", "Known-MAC", "ACLskip",
                                "L2-Drop")).build();
        final FlowPaths flowPath5 = new FlowPathsBuilder()
                .setName("v4-1")
                .setPath(
                        Arrays.asList("Non-Control-Frame", "IV-pass", "Known-MAC", "ACLskip",
                                "L2-Router-MAC", "IPv4", "v4-Unicast", "NextHop", "EgressPort"))
                .build();
        final FlowPaths flowPath6 = new FlowPathsBuilder()
                .setName("v4-2")
                .setPath(
                        Arrays.asList("Non-Control-Frame", "IV-pass", "Known-MAC", "ACLskip",
                                "L2-Router-MAC", "IPv4", "v4-Unicast-ECMP", "L3ECMP", "NextHop",
                                "EgressPort")).build();

        final TableTypePattern ttp = new TableTypePatternBuilder().setFlowPaths(Arrays.asList(flowPath1, flowPath2, flowPath3,
                flowPath4, flowPath5, flowPath6)).build();

        final String expectedStr = "{\"flow_paths\": [{\"doc\": [\"This object contains just a few examples of flow paths, it is not\",\"a comprehensive list of the flow paths required for this TTP.  It is\",\"intended that the flow paths array could include either a list of\",\"required flow paths or a list of specific flow paths that are not\",\"required (whichever is more concise or more useful.\"],\"name\": \"L2-2\",\"path\": [\"Non-Control-Frame\",\"IV-pass\",\"Known-MAC\",\"ACLskip\",\"L2-Unicast\",\"EgressPort\"]},{\"name\": \"L2-3\",\"path\": [\"Non-Control-Frame\",\"IV-pass\",\"Known-MAC\",\"ACLskip\",\"L2-Multicast\", \"L2Mcast\", \"[EgressPort]\" ]},{\"name\": \"L2-4\",\"path\": [\"Non-Control-Frame\",\"IV-pass\",\"Known-MAC\",\"ACL-skip\",\"VID-flood\", \"VIDflood\", \"[EgressPort]\" ]},{\"name\": \"L2-5\",\"path\": [\"Non-Control-Frame\",\"IV-pass\",\"Known-MAC\",\"ACLskip\",\"L2-Drop\"] },{\"name\": \"v4-1\",\"path\": [\"Non-Control-Frame\",\"IV-pass\",\"Known-MAC\",\"ACLskip\",\"L2-Router-MAC\",\"IPv4\",\"v4-Unicast\",\"NextHop\", \"EgressPort\"]},{\"name\": \"v4-2\",\"path\": [\"Non-Control-Frame\",\"IV-pass\",\"Known-MAC\",\"ACLskip\",\"L2-Router-MAC\", \"IPv4\",\"v4-Unicast-ECMP\",\"L3ECMP\", \"NextHop\", \"EgressPort\"]}]}";

        assertConvertedJSONEquals(wrapJSONStringInTTP(expectedStr), ttp);
    }

    @Test
    public void testSecurity() throws Exception {
        final Security sec = new SecurityBuilder().setDoc(Arrays.asList(
                "This TTP is not published for use by ONF. It is an example and for",
                "illustrative purposes only.",
                "If this TTP were published for use it would include",
                "guidance as to any security considerations in this doc member.")).build();
        final String expectedStr = "{\"security\": {\"doc\": [\"This TTP is not published for use by ONF. It is an example and for\",\"illustrative purposes only.\",\"If this TTP were published for use it would include\",\"guidance as to any security considerations in this doc member.\"]}}";
        assertConvertedJSONEquals(wrapJSONStringInTTP(expectedStr), new TableTypePatternBuilder().setSecurity(sec).build());
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
    @Deprecated
    public DataSchemaNode getSchemaNodeForDataObject(final SchemaContext context, final DataObject d) {


        final QName qn = BindingReflections.findQName(d.getClass());

        final Set<DataSchemaNode> allTheNodes = getAllTheNode(context);

        // TODO: create a map to make this faster!!!!
        for ( final DataSchemaNode dsn : allTheNodes ) {
            if(dsn instanceof DataNodeContainer) {
                allTheNodes.addAll(((DataNodeContainer)dsn).getChildNodes());
            }
            if (dsn.getQName().equals(qn)) {
                return dsn;
            }
        }
        return null;
    }

    public Set<DataSchemaNode> getAllTheNode(final SchemaContext context) {
        final Set<DataSchemaNode> nodes = new HashSet<DataSchemaNode>();
        getAllTheNodesHelper(context, nodes);
        return nodes;
    }

    private void getAllTheNodesHelper(final DataNodeContainer dcn, final Set<DataSchemaNode> nodes) {
        for ( final DataSchemaNode dsn : dcn.getChildNodes() ) {
            if (dsn instanceof DataNodeContainer){
                getAllTheNodesHelper((DataNodeContainer)dsn, nodes);
            }
            nodes.add(dsn);
        }
    }


    private  void assertConvertedJSONEquals(final String expectedJson, final TableTypePattern data) throws Exception {
        List<TableTypePattern> value = Collections.singletonList(data);
        final String jsonString = jsonStringFromDataObject(TTP_PATTERNS, new TableTypePatternsBuilder().setTableTypePattern(value).build());

        final JsonElement je = jp.parse(expectedJson);
        final String prettyExpectedJson = gson.toJson(je);

        System.out.println("Generated JSON:\n" + jsonString);
        System.out.println("Expected JSON:\n" + prettyExpectedJson);
        JSONAssert.assertEquals(expectedJson, jsonString, JSONCompareMode.STRICT);
    }

    public static String formatDocumentAsXMLString(final Document document) throws Exception {
        final DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
        final DOMImplementationLS impl = (DOMImplementationLS) registry.getDOMImplementation("LS");
        final LSSerializer writer = impl.createLSSerializer();
        writer.getDomConfig().setParameter("format-pretty-print", Boolean.TRUE);
        return writer.writeToString(document);
    }

    private String jsonStringFromDataObject(final InstanceIdentifier<?> path, final DataObject object) {
        return TTP_UTILS.jsonStringFromDataObject(path, object);
    }
}