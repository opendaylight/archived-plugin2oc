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
import static org.powermock.api.easymock.PowerMock.expectNew;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import net.juniper.contrail.api.ApiConnector;
import net.juniper.contrail.api.ApiPropertyBase;
import net.juniper.contrail.api.ObjectReference;
import net.juniper.contrail.api.types.Project;
import net.juniper.contrail.api.types.VirtualNetwork;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.opendaylight.controller.networkconfig.neutron.NeutronNetwork;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * Test Class for Neutron Network.
 */
@PrepareForTest({ NetworkHandler.class, VirtualNetwork.class })
@RunWith(PowerMockRunner.class)
public class NetworkHandlerTest {
    NetworkHandler networkHandler;
    NeutronNetwork mockedNeutronNetwork = mock(NeutronNetwork.class);
    ApiConnector mockedApiConnector = mock(ApiConnector.class);
    ApiConnector mockedApiConnector1 = Mockito.mock(ApiConnector.class);
    VirtualNetwork mockedVirtualNetwork = mock(VirtualNetwork.class);
    Project mockProject = mock(Project.class);

    @Before
    public void beforeTest() {
        networkHandler = new NetworkHandler();
        assertNotNull(mockedApiConnector);
        assertNotNull(mockedNeutronNetwork);
        assertNotNull(mockedVirtualNetwork);
        assertNotNull(mockProject);
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
        neutron.setTenantID("019570f2-17b1-4fc3-99ec-1b7f7778a29a");
        neutron.setProviderSegmentationID("2");
        neutron.setAdminStateUp(true);
        neutron.setProviderNetworkType("gre");
        neutron.setProviderSegmentationID("2");
        neutron.setShared(false);
        neutron.setRouterExternal(false);
        return neutron;
    }

    /* dummy params for Neutron Network for update */
    public NeutronNetwork defaultNeutronObjectUpdate() {
        NeutronNetwork delta_neutron = new NeutronNetwork();
        delta_neutron.setNetworkName("Virtual-Network-2");
        delta_neutron.setTenantID("cfedfe89b66e406aad56052873c683e7");
        delta_neutron.setProviderSegmentationID("3");
        delta_neutron.setAdminStateUp(true);
        delta_neutron.setProviderNetworkType("gre");
        delta_neutron.setProviderSegmentationID("4");
        delta_neutron.setShared(false);
        delta_neutron.setRouterExternal(false);
        return delta_neutron;
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

    /* Test method to check if neutron network uuid is empty or name is null */
    @Test
    public void testCanCreateNetworkUuidEmpty() {
        Activator.apiConnector = mockedApiConnector;
        NeutronNetwork neutron = new NeutronNetwork();
        neutron.setNetworkUUID("");
        neutron.setNetworkName(null);
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, networkHandler.canCreateNetwork(neutron));
    }

    /* Test method to check if neutron network tenant id is null */
    @Test
    public void testCanCreateNetworkTenantIdNull() {
        Activator.apiConnector = mockedApiConnector;
        NeutronNetwork neutronNetwork = defaultNeutronObject();
        neutronNetwork.setTenantID(null);
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, networkHandler.canCreateNetwork(neutronNetwork));
    }

    /* Test method to check neutron network with virtual network Existence */
    @Test
    public void testCanCreateNetworkVirtualNetworkExists() throws IOException {
        Activator.apiConnector = mockedApiConnector;
        NeutronNetwork neutronNetwork = defaultNeutronObject();
        when(mockedApiConnector.findById(VirtualNetwork.class, neutronNetwork.getNetworkUUID())).thenReturn(mockedVirtualNetwork);
        when(mockedApiConnector.findById(Project.class, neutronNetwork.getTenantID())).thenReturn(mockProject);
        assertEquals(HttpURLConnection.HTTP_FORBIDDEN, networkHandler.canCreateNetwork(neutronNetwork));
    }

    /*
     * Test method to check neutron network creation fails with Internal Server
     * Error
     */
    @Test
    public void testCanCreateNetworkInternalError() throws Exception {
        VirtualNetwork mockInstance = PowerMock.createNiceMock(VirtualNetwork.class);
        Activator.apiConnector = mockedApiConnector;
        NeutronNetwork neutronNetwork = defaultNeutronObject();
        expectNew(VirtualNetwork.class).andReturn(mockInstance);
        when(mockedApiConnector.findById(VirtualNetwork.class, neutronNetwork.getNetworkUUID())).thenReturn(null);
        when(mockedApiConnector.findById(Project.class, neutronNetwork.getTenantID())).thenReturn(mockProject);
        when(mockedApiConnector.create(mockInstance)).thenReturn(false);
        PowerMock.replay(mockInstance, VirtualNetwork.class);
        assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, networkHandler.canCreateNetwork(neutronNetwork));
    }

    /* Test method to check network is created */
    @Test
    public void testCanCreateNetworkHttpOk() throws Exception {
        VirtualNetwork mockInstance = PowerMock.createNiceMock(VirtualNetwork.class);
        Activator.apiConnector = mockedApiConnector;
        NeutronNetwork neutronNetwork = defaultNeutronObject();
        expectNew(VirtualNetwork.class).andReturn(mockInstance);
        when(mockedApiConnector.findById(VirtualNetwork.class, neutronNetwork.getNetworkUUID())).thenReturn(null);
        when(mockedApiConnector.findById(Project.class, neutronNetwork.getTenantID())).thenReturn(mockProject);
        when(mockedApiConnector.create(mockInstance)).thenReturn(true);
        PowerMock.replay(mockInstance, VirtualNetwork.class);
        assertEquals(HttpURLConnection.HTTP_OK, networkHandler.canCreateNetwork(neutronNetwork));
    }

    /* Test method to check neutron network with virtual network Existence */
    @Test
    public void testcanCreateNetworkProjectNull() throws IOException {
        Activator.apiConnector = mockedApiConnector;
        NeutronNetwork neutronNetwork = defaultNeutronObject();
        when(mockedApiConnector.findById(Project.class, neutronNetwork.getTenantID())).thenReturn(null);
        when(mockedApiConnector.findById(Project.class, neutronNetwork.getTenantID())).thenReturn(null);
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, networkHandler.canCreateNetwork(neutronNetwork));
    }

    /* Test method to check neutron network with virtual network Existence */
    @Test
    public void testcanDeleteNetwork() throws IOException {
        Activator.apiConnector = mockedApiConnector;
        NeutronNetwork neutronNetwork = defaultNeutronObject();
        when(mockedApiConnector.findById(VirtualNetwork.class, neutronNetwork.getNetworkUUID())).thenReturn(null);
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, networkHandler.canDeleteNetwork(neutronNetwork));
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

    /* Test method to check if neutron network is null */
    @Test
    public void testCanUpdateNetworkNull() {
        Activator.apiConnector = mockedApiConnector;
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, networkHandler.canUpdateNetwork(null, null));
    }

    /* Test method to check if neutron network is null */
    @Test
    public void testCanUpdateNetworkEmptyName() {
        Activator.apiConnector = mockedApiConnector;
        NeutronNetwork neutronNetwork = defaultNeutronObject();
        NeutronNetwork delta_neutronNetwork = defaultNeutronObjectUpdate();
        delta_neutronNetwork.setNetworkName("");
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, networkHandler.canUpdateNetwork(delta_neutronNetwork, neutronNetwork));
    }

    /* Test method to check neutron network with virtual network Existence */
    @Test
    public void testCanUpdateNetworkVirtualNetworkNotExists() throws IOException {
        Activator.apiConnector = mockedApiConnector;
        NeutronNetwork neutronNetwork = defaultNeutronObject();
        NeutronNetwork delta_neutronNetwork = defaultNeutronObjectUpdate();
        when(mockedApiConnector.findById(VirtualNetwork.class, neutronNetwork.getNetworkUUID())).thenReturn(null);
        assertEquals(HttpURLConnection.HTTP_FORBIDDEN, networkHandler.canUpdateNetwork(delta_neutronNetwork, neutronNetwork));
    }

    /*
     * Test method to check neutron network update fails with Internal Server
     * Error
     */
    @Test
    public void testUpdateNetworkInternalError() throws IOException {
        Activator.apiConnector = mockedApiConnector;
        NeutronNetwork neutronNetwork = defaultNeutronObject();
        NeutronNetwork delta_neutronNetwork = defaultNeutronObjectUpdate();
        when(mockedApiConnector.findById(VirtualNetwork.class, neutronNetwork.getNetworkUUID())).thenReturn(mockedVirtualNetwork);
        when(mockedApiConnector.update(mockedVirtualNetwork)).thenReturn(false);
        assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, networkHandler.canUpdateNetwork(delta_neutronNetwork, neutronNetwork));
    }

    /* Test method to check neutron network update with HTTP OK */
    @Test
    public void testUpdateNetwork() throws IOException {
        Activator.apiConnector = mockedApiConnector;
        NeutronNetwork neutronNetwork = defaultNeutronObject();
        NeutronNetwork delta_neutronNetwork = defaultNeutronObjectUpdate();
        mockedVirtualNetwork.setName(delta_neutronNetwork.getNetworkName());
        when(mockedApiConnector.findById(VirtualNetwork.class, neutronNetwork.getNetworkUUID())).thenReturn(mockedVirtualNetwork);
        when(mockedApiConnector.update(mockedVirtualNetwork)).thenReturn(true);
        assertEquals(HttpURLConnection.HTTP_OK, networkHandler.canUpdateNetwork(delta_neutronNetwork, neutronNetwork));
    }
}