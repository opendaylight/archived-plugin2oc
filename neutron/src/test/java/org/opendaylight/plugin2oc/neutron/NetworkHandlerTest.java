/*
 * Copyright (C) 2014 Juniper Networks, Inc.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 */
package org.opendaylight.plugin2oc.neutron;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.juniper.contrail.api.ApiConnector;
import net.juniper.contrail.api.ApiPropertyBase;
import net.juniper.contrail.api.ObjectReference;
import net.juniper.contrail.api.types.FloatingIpPool;
import net.juniper.contrail.api.types.Project;
import net.juniper.contrail.api.types.VirtualNetwork;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.opendaylight.controller.networkconfig.neutron.NeutronNetwork;

/**
 * Test Class for Neutron Network.
 */

public class NetworkHandlerTest {
    NetworkHandler networkHandler;
    NetworkHandler mockednetworkHandler = mock(NetworkHandler.class);
    NeutronNetwork mockedNeutronNetwork = mock(NeutronNetwork.class);
    ApiConnector mockedApiConnector = mock(ApiConnector.class);
    VirtualNetwork mockedVirtualNetwork = mock(VirtualNetwork.class);
    Project mockedProject = mock(Project.class);
    FloatingIpPool mockedFloatingIpPool = mock(FloatingIpPool.class);

    @Before
    public void beforeTest() {
        networkHandler = new NetworkHandler();
        assertNotNull(mockedApiConnector);
        assertNotNull(mockedNeutronNetwork);
        assertNotNull(mockedVirtualNetwork);
        assertNotNull(mockedProject);
        assertNotNull(mockedFloatingIpPool);
    }

    @After
    public void AfterTest() {
        networkHandler = null;
        Activator.apiConnector = null;
    }

    /* dummy params for Neutron Network */
    public NeutronNetwork defaultNeutronObject() {
        NeutronNetwork neutron = new NeutronNetwork();
        neutron.setNetworkName("Virtual-Network");
        neutron.setNetworkUUID("6b9570f2-17b1-4fc3-99ec-1b7f7778a29a");
        neutron.setProviderNetworkType("gre");
        neutron.setTenantID("123570f2-17b1-4fc3-99ec-1b7f7778a29a");
        neutron.setProviderSegmentationID("2");
        neutron.setAdminStateUp(true);
        neutron.setProviderNetworkType("gre");
        neutron.setProviderSegmentationID("2");
        neutron.setShared(false);
        neutron.setRouterExternal(false);
        neutron.setRouterExternal(true);
        return neutron;
    }

    /* dummy params for Neutron Network for update */
    public NeutronNetwork defaultNeutronObjectUpdate() {
        NeutronNetwork deltaNetwork = new NeutronNetwork();
        deltaNetwork.setNetworkName("Virtual-Network-2");
        deltaNetwork.setTenantID("123570f2-17b1-4fc3-99ec-1b7f7778a29a");
        deltaNetwork.setProviderSegmentationID("3");
        deltaNetwork.setAdminStateUp(true);
        deltaNetwork.setProviderNetworkType("gre");
        deltaNetwork.setProviderSegmentationID("4");
        deltaNetwork.setShared(true);
        deltaNetwork.setRouterExternal(false);
        deltaNetwork.setRouterExternal(true);
        return deltaNetwork;
    }

    /* dummy params for floating IP */
    public FloatingIpPool defaultFloatingIpPoolObject() {
        FloatingIpPool floatingIpPoolObj = new FloatingIpPool();
        String fipId = UUID.randomUUID().toString();
        floatingIpPoolObj.setName(fipId);
        floatingIpPoolObj.setDisplayName(fipId);
        floatingIpPoolObj.setUuid(fipId);
        return floatingIpPoolObj;
    }

    /* Test method to check if neutron network is null */
    @Test
    public void testCanCreateNetworkNull() {
        Activator.apiConnector = mockedApiConnector;
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, networkHandler.canCreateNetwork(null));
    }

    /* Test method to check if neutron network uuid or name is null */
    @Test
    public void testCanCreateNetworkUuidNameNull() {
        Activator.apiConnector = mockedApiConnector;
        NeutronNetwork neutron = new NeutronNetwork();
        neutron.setNetworkUUID(null);
        neutron.setNetworkName(null);
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, networkHandler.canCreateNetwork(neutron));
    }

    /*
     * Test method to check if neutron network uuid is empty or name is null
     */
    @Test
    public void testCanCreateNetworkUuidEmpty() {
        Activator.apiConnector = mockedApiConnector;
        NeutronNetwork neutron = new NeutronNetwork();
        neutron.setNetworkUUID("");
        neutron.setNetworkName("");
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, networkHandler.canCreateNetwork(neutron));
    }

    /* Test method to check if neutron network TenantID is null */
    @Test
    public void testCanCreateNetworkTenantIDNull() {
        Activator.apiConnector = mockedApiConnector;
        NeutronNetwork neutron = new NeutronNetwork();
        neutron.setNetworkName("net-1");
        neutron.setNetworkUUID("6b9570f2-17b1-4fc3-99ec-1b7f7778a29a");
        neutron.setTenantID(null);
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, networkHandler.canCreateNetwork(neutron));
    }

    /*
     * Test method to check neutron network with virtual project UUID Existence
     */
    @Test
    public void testcanCreateNetworkProjectUUIDNull() throws IOException {
        Activator.apiConnector = mockedApiConnector;
        NeutronNetwork neutronNetwork = defaultNeutronObject();
        when(mockedApiConnector.findById(Project.class, neutronNetwork.getTenantID())).thenReturn(null);
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, networkHandler.canCreateNetwork(neutronNetwork));
    }

    /*
     * Test method to check neutron network create with same name
     */
    @Test
    public void testcanCreateNetworkByNameExist() throws IOException {
        Activator.apiConnector = mockedApiConnector;
        NeutronNetwork neutronNetwork = defaultNeutronObject();
        when(mockedApiConnector.findById(Project.class, neutronNetwork.getTenantID())).thenReturn(mockedProject);
        when(mockedApiConnector.findByName(VirtualNetwork.class, mockedProject, neutronNetwork.getNetworkName())).thenReturn("network-001");
        assertEquals(HttpURLConnection.HTTP_FORBIDDEN, networkHandler.canCreateNetwork(neutronNetwork));
    }

    /*
     * Test method to check neutron can create network OK
     */
    @Test
    public void testcanCreateNetworkOK() throws IOException {
        Activator.apiConnector = mockedApiConnector;
        NeutronNetwork neutronNetwork = defaultNeutronObject();
        when(mockedApiConnector.findById(Project.class, neutronNetwork.getTenantID())).thenReturn(mockedProject);
        when(mockedApiConnector.findByName(VirtualNetwork.class, mockedProject, neutronNetwork.getNetworkName())).thenReturn(null);
        assertEquals(HttpURLConnection.HTTP_OK, networkHandler.canCreateNetwork(neutronNetwork));
    }

    /* Test method to check if neutron network object is null */
    @Test
    public void testCanUpdateNetworkNull() {
        Activator.apiConnector = mockedApiConnector;
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, networkHandler.canUpdateNetwork(null, null));
    }

    /* Test method to check if neutron network name is empty string */
    @Test
    public void testCanUpdateNetworkNameEmpty() {
        Activator.apiConnector = mockedApiConnector;
        NeutronNetwork neutronNetwork = defaultNeutronObject();
        NeutronNetwork deltaNeutronNetwork = defaultNeutronObjectUpdate();
        deltaNeutronNetwork.setNetworkName("");
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, networkHandler.canUpdateNetwork(deltaNeutronNetwork, neutronNetwork));
    }

    /*
     * Test method to check neutron network update with same name
     */
    @Test
    public void testcanUpdateNetworkByNameExist() throws IOException {
        Activator.apiConnector = mockedApiConnector;
        NeutronNetwork neutronNetwork = defaultNeutronObject();
        NeutronNetwork deltaNeutronNetwork = defaultNeutronObjectUpdate();
        when(mockedApiConnector.findById(Project.class, neutronNetwork.getTenantID())).thenReturn(mockedProject);
        when(mockedApiConnector.findByName(VirtualNetwork.class, mockedProject, neutronNetwork.getNetworkName())).thenReturn("network-001");
        assertEquals(HttpURLConnection.HTTP_FORBIDDEN, networkHandler.canUpdateNetwork(deltaNeutronNetwork, neutronNetwork));
    }

    /*
     * Test method to check if neutron network update with null virtual network
     */
    @Test
    public void testCanUpdateNetworkVirtualNetworkNull() throws IOException {
        Activator.apiConnector = mockedApiConnector;
        NeutronNetwork neutronNetwork = defaultNeutronObject();
        NeutronNetwork deltaNeutronNetwork = defaultNeutronObjectUpdate();
        when(mockedApiConnector.findById(Project.class, neutronNetwork.getTenantID())).thenReturn(mockedProject);
        when(mockedApiConnector.findByName(VirtualNetwork.class, mockedProject, neutronNetwork.getNetworkName())).thenReturn(null);
        when(mockedApiConnector.findById(VirtualNetwork.class, neutronNetwork.getNetworkUUID())).thenReturn(null);
        assertEquals(HttpURLConnection.HTTP_FORBIDDEN, networkHandler.canUpdateNetwork(deltaNeutronNetwork, neutronNetwork));
    }

    /*
     * Test method to check if neutron network update with OK
     */
    @Test
    public void testCanUpdateNetworkVirtualNetworkOK() throws IOException {
        Activator.apiConnector = mockedApiConnector;
        NeutronNetwork neutronNetwork = defaultNeutronObject();
        NeutronNetwork deltaNeutronNetwork = defaultNeutronObjectUpdate();
        when(mockedApiConnector.findById(Project.class, neutronNetwork.getTenantID())).thenReturn(mockedProject);
        when(mockedApiConnector.findByName(VirtualNetwork.class, mockedProject, neutronNetwork.getNetworkName())).thenReturn(null);
        when(mockedApiConnector.findById(VirtualNetwork.class, neutronNetwork.getNetworkUUID())).thenReturn(mockedVirtualNetwork);
        assertEquals(HttpURLConnection.HTTP_OK, networkHandler.canUpdateNetwork(deltaNeutronNetwork, neutronNetwork));
    }

    /* Test method to check delete network with when Port exist */
    @Test
    public void testcanDeleteNetworkPortExists() throws IOException {
        Activator.apiConnector = mockedApiConnector;
        NeutronNetwork neutronNetwork = defaultNeutronObject();
        when(mockedApiConnector.findById(VirtualNetwork.class, neutronNetwork.getNetworkUUID())).thenReturn(mockedVirtualNetwork);
        List<ObjectReference<ApiPropertyBase>> test = new ArrayList<ObjectReference<ApiPropertyBase>>();
        when(mockedVirtualNetwork.getVirtualMachineInterfaceBackRefs()).thenReturn(test);
        assertEquals(HttpURLConnection.HTTP_FORBIDDEN, networkHandler.canDeleteNetwork(neutronNetwork));
    }

    /*
     * Test method to check neutron network deletion with virtual network
     * Existence
     */
    @Test
    public void testcanDeleteNetwork() throws IOException {
        Activator.apiConnector = mockedApiConnector;
        NeutronNetwork neutronNetwork = defaultNeutronObject();
        when(mockedApiConnector.findById(VirtualNetwork.class, neutronNetwork.getNetworkUUID())).thenReturn(null);
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, networkHandler.canDeleteNetwork(neutronNetwork));
    }

    /*
     * Test method to check neutron network deletion with Ok
     */
    @Test
    public void testcanDeleteNetworkOK() throws IOException {
        Activator.apiConnector = mockedApiConnector;
        NeutronNetwork neutronNetwork = defaultNeutronObject();
        when(mockedApiConnector.findById(VirtualNetwork.class, neutronNetwork.getNetworkUUID())).thenReturn(mockedVirtualNetwork);
        List<ObjectReference<ApiPropertyBase>> test = new ArrayList<ObjectReference<ApiPropertyBase>>();
        when(mockedVirtualNetwork.getVirtualMachineInterfaceBackRefs()).thenReturn(test);
        assertEquals(HttpURLConnection.HTTP_FORBIDDEN, networkHandler.canDeleteNetwork(neutronNetwork));
    }

}
