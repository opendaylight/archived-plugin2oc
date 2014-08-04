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
    NetworkHandler mockednetworkHandler = mock(NetworkHandler.class);
    NeutronNetwork mockedNeutronNetwork = mock(NeutronNetwork.class);
    ApiConnector mockedApiConnector = mock(ApiConnector.class);
    ApiConnector mockedApiConnector1 = Mockito.mock(ApiConnector.class);
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

    /* dummy params for Nfloating IP */
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

    /* Test method to check neutron network with virtual network Existence */
    @Test
    public void testCanCreateNetworkVirtualNetworkExists() throws IOException {
        Activator.apiConnector = mockedApiConnector;
        NeutronNetwork neutronNetwork = defaultNeutronObject();
        when(mockedApiConnector.findById(VirtualNetwork.class, neutronNetwork.getNetworkUUID())).thenReturn(mockedVirtualNetwork);
        when(mockedApiConnector.findById(Project.class, neutronNetwork.getTenantID())).thenReturn(mockedProject);
        assertEquals(HttpURLConnection.HTTP_FORBIDDEN, networkHandler.canCreateNetwork(neutronNetwork));
    }

    /* Test method to check neutron network create Failed */
    @Test
    public void testCanCreateNetworkFailed() throws Exception {
        Activator.apiConnector = mockedApiConnector;
        NeutronNetwork neutronNetwork = defaultNeutronObject();
        when(mockedApiConnector.findById(Project.class, neutronNetwork.getTenantID())).thenReturn(mockedProject);
        when(mockedApiConnector.findById(VirtualNetwork.class, neutronNetwork.getNetworkUUID())).thenReturn(null);
        VirtualNetwork mockedVirtualNet = PowerMock.createNiceMock(VirtualNetwork.class);
        expectNew(VirtualNetwork.class).andReturn(mockedVirtualNet);
        when(mockedApiConnector.findById(Project.class, neutronNetwork.getTenantID())).thenReturn(mockedProject);
        when(mockedApiConnector.create(mockedVirtualNet)).thenReturn(false);
        PowerMock.replay(mockedVirtualNet, VirtualNetwork.class);
        assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, networkHandler.canCreateNetwork(neutronNetwork));
    }

    /* Test method to check neutron network create with HTTP OK */
    @Test
    public void testCanCreateNetworkOK() throws Exception {
        Activator.apiConnector = mockedApiConnector;
        NeutronNetwork neutronNetwork = defaultNeutronObject();
        when(mockedApiConnector.findById(Project.class, neutronNetwork.getTenantID())).thenReturn(mockedProject);
        when(mockedApiConnector.findById(VirtualNetwork.class, neutronNetwork.getNetworkUUID())).thenReturn(null);
        VirtualNetwork mockedVirtualNet = PowerMock.createNiceMock(VirtualNetwork.class);
        expectNew(VirtualNetwork.class).andReturn(mockedVirtualNet);
        when(mockedApiConnector.findById(Project.class, neutronNetwork.getTenantID())).thenReturn(mockedProject);
        when(mockedApiConnector.create(mockedVirtualNet)).thenReturn(true);
        PowerMock.replay(mockedVirtualNet, VirtualNetwork.class);
        assertEquals(HttpURLConnection.HTTP_OK, networkHandler.canCreateNetwork(neutronNetwork));
    }

    /* Test method to check if neutron network is null */
    @Test
    public void testCanUpdateNetworkNull() {
        Activator.apiConnector = mockedApiConnector;
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, networkHandler.canUpdateNetwork(null, null));
    }

    /*
     * Test method to check if neutron network update with null virtual network
     */
    @Test
    public void testCanUpdateNetworkVirtualNetworkNull() throws IOException {
        Activator.apiConnector = mockedApiConnector;
        NeutronNetwork neutronNetwork = defaultNeutronObject();
        NeutronNetwork deltaNeutronNetwork = defaultNeutronObjectUpdate();
        when(mockedApiConnector.findById(VirtualNetwork.class, neutronNetwork.getNetworkUUID())).thenReturn(null);
        assertEquals(HttpURLConnection.HTTP_FORBIDDEN, networkHandler.canUpdateNetwork(deltaNeutronNetwork, neutronNetwork));
    }

    /* Test method to check if neutron network update OK */
    @Test
    public void testCanUpdateNetworkFailed() throws Exception {
        Activator.apiConnector = mockedApiConnector;
        NeutronNetwork neutronNetwork = defaultNeutronObject();
        NeutronNetwork deltaNeutronNetwork = defaultNeutronObjectUpdate();
        when(mockedApiConnector.findById(VirtualNetwork.class, neutronNetwork.getNetworkUUID())).thenReturn(mockedVirtualNetwork);
        when(mockedApiConnector.update(mockedVirtualNetwork)).thenReturn(false);
        assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, networkHandler.canUpdateNetwork(deltaNeutronNetwork, neutronNetwork));
    }

    /* Test method to check if neutron network update create Foating IP failed */
    @Test
    public void testCanUpdateNetworkCreateFloatingIpFailed() throws Exception {
        Activator.apiConnector = mockedApiConnector;
        NeutronNetwork neutronNetwork = defaultNeutronObject();
        NeutronNetwork deltaNeutronNetwork = defaultNeutronObjectUpdate();
        when(mockedApiConnector.findById(VirtualNetwork.class, neutronNetwork.getNetworkUUID())).thenReturn(mockedVirtualNetwork);
        when(mockedApiConnector.update(mockedVirtualNetwork)).thenReturn(true);
        neutronNetwork.setRouterExternal(false);
        deltaNeutronNetwork.setRouterExternal(true);
        when(mockedApiConnector.findById(VirtualNetwork.class, mockedVirtualNetwork.getUuid())).thenReturn(mockedVirtualNetwork);
        FloatingIpPool floatingIpPool = PowerMock.createNiceMock(FloatingIpPool.class);
        expectNew(FloatingIpPool.class).andReturn(floatingIpPool);
        floatingIpPool.setParent(mockedVirtualNetwork);
        when(mockedApiConnector.create(floatingIpPool)).thenReturn(false);
        PowerMock.replay(floatingIpPool, FloatingIpPool.class);
        assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, networkHandler.canUpdateNetwork(deltaNeutronNetwork, neutronNetwork));

    }

    /*
     * Test method to check neutron network updated successfully after
     * floatingIP created
     */
    @Test
    public void testCanUpdateNetworkOKif() throws Exception {
        Activator.apiConnector = mockedApiConnector;
        NeutronNetwork neutronNetwork = defaultNeutronObject();
        NeutronNetwork deltaNetwork = defaultNeutronObjectUpdate();
        when(mockedApiConnector.findById(VirtualNetwork.class, neutronNetwork.getNetworkUUID())).thenReturn(mockedVirtualNetwork);
        mockedVirtualNetwork.setName(deltaNetwork.getNetworkName());
        mockedVirtualNetwork.setDisplayName(deltaNetwork.getNetworkName());
        when(mockedApiConnector.update(mockedVirtualNetwork)).thenReturn(true);
        neutronNetwork.setRouterExternal(false);
        deltaNetwork.setRouterExternal(true);
        when(mockedApiConnector.findById(VirtualNetwork.class, mockedVirtualNetwork.getUuid())).thenReturn(mockedVirtualNetwork);
        FloatingIpPool floatingIpPool = PowerMock.createNiceMock(FloatingIpPool.class);
        expectNew(FloatingIpPool.class).andReturn(floatingIpPool);
        String fipId = UUID.randomUUID().toString();
        floatingIpPool.setName(fipId);
        floatingIpPool.setDisplayName(fipId);
        floatingIpPool.setUuid(fipId);
        floatingIpPool.setParent(mockedVirtualNetwork);
        when(mockedApiConnector.create(floatingIpPool)).thenReturn(true);
        PowerMock.replay(floatingIpPool, FloatingIpPool.class);
        assertEquals(HttpURLConnection.HTTP_OK, networkHandler.canUpdateNetwork(deltaNetwork, neutronNetwork));
    }

    // /* Test method to check neutron network updated failed due to Floating Ip
    // pool is failed to removed after update network..*/
    // @Test
    // public void testupdateNetworkFloatingIPremoveFailed() throws Exception {
    // Activator.apiConnector = mockedApiConnector;
    // NeutronNetwork neutronNetwork = defaultNeutronObject();
    // NeutronNetwork deltaNetwork =defaultNeutronObjectUpdate();
    // when(mockedApiConnector.findById(VirtualNetwork.class,neutronNetwork.getNetworkUUID())).thenReturn(mockedVirtualNetwork);
    // mockedVirtualNetwork.setName(deltaNetwork.getNetworkName());
    // mockedVirtualNetwork.setDisplayName(deltaNetwork.getNetworkName());
    // when(mockedApiConnector.update(mockedVirtualNetwork)).thenReturn(true);
    // neutronNetwork.setRouterExternal(true);
    // deltaNetwork.setRouterExternal(false);
    // ObjectReference<ApiPropertyBase> ref = new ObjectReference<>();
    // List<ObjectReference<ApiPropertyBase>> pool = new
    // ArrayList<ObjectReference<ApiPropertyBase>>();
    // FloatingIpPool fp = new FloatingIpPool();
    // List<String> temp = new ArrayList<String>();
    // for (int i = 0; i < 1; i++) {
    // String fipId = UUID.randomUUID().toString();
    // fp.setDisplayName(fipId);
    // fp.setName(fipId);
    // fp.setUuid(fipId);
    // ref.setReference(temp,"", "",""); //To do do not have api property base
    // reference
    // pool.add(i, ref);
    // }
    // when(mockedVirtualNetwork.getFloatingIpPools().get(0).getUuid()).thenReturn("000570f2-17b1-4fc3-99ec-1b7f7778a29a");
    // when(mockedApiConnector.findById(FloatingIpPool.class,mockedVirtualNetwork.getFloatingIpPools().get(0).getUuid())).thenReturn(mockedFloatingIpPool);
    // when(mockedApiConnector.findById(FloatingIpPool.class,mockedVirtualNetwork.getFloatingIpPools().get(0).getUuid())).thenReturn(mockedFloatingIpPool);
    // PowerMock.replay(floatingIpPool, FloatingIpPool.class);
    // assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR,networkHandler.canUpdateNetwork(deltaNetwork,
    // neutronNetwork));
    // }

    // /* Test method to check neutron network updated successfully after
    // floatingIP removal..*/
    // @Test
    // public void testupdateNetworkOKelse() throws Exception {
    // Activator.apiConnector = mockedApiConnector;
    // NeutronNetwork neutronNetwork = defaultNeutronObject();
    // NeutronNetwork deltaNetwork =defaultNeutronObjectUpdate();
    // when(mockedApiConnector.findById(VirtualNetwork.class,neutronNetwork.getNetworkUUID())).thenReturn(mockedVirtualNetwork);
    // mockedVirtualNetwork.setName(deltaNetwork.getNetworkName());
    // mockedVirtualNetwork.setDisplayName(deltaNetwork.getNetworkName());
    // when(mockedApiConnector.update(mockedVirtualNetwork)).thenReturn(true);
    // neutronNetwork.setRouterExternal(true);
    // deltaNetwork.setRouterExternal(false);
    // ObjectReference<ApiPropertyBase> ref = new ObjectReference<>();
    // List<ObjectReference<ApiPropertyBase>> pool = new
    // ArrayList<ObjectReference<ApiPropertyBase>>();
    // FloatingIpPool fp = new FloatingIpPool();
    // List<String> temp = new ArrayList<String>();
    // for (int i = 0; i < 1; i++) {
    // String fipId = UUID.randomUUID().toString();
    // fp.setDisplayName(fipId);
    // fp.setName(fipId);
    // fp.setUuid(fipId);
    // ref.setReference(temp,"", "","");//To do do not have api property base
    // reference
    // pool.add(i, ref);
    // }
    // when(mockedVirtualNetwork.getFloatingIpPools().get(0).getUuid()).thenReturn("000570f2-17b1-4fc3-99ec-1b7f7778a29a");
    // when(mockedApiConnector.findById(FloatingIpPool.class,mockedVirtualNetwork.getFloatingIpPools().get(0).getUuid())).thenReturn(mockedFloatingIpPool);
    // when(mockedApiConnector.findById(FloatingIpPool.class,mockedVirtualNetwork.getFloatingIpPools().get(0).getUuid())).thenReturn(null);
    //
    // assertEquals(HttpURLConnection.HTTP_OK,
    // networkHandler.canUpdateNetwork(deltaNetwork, neutronNetwork));
    // }

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
     * Test method to check neutron network creation of virtual network failed
     */
    @Test
    public void testcreateNetworkVirtualNetworkFalse() throws Exception {
        Activator.apiConnector = mockedApiConnector;
        NeutronNetwork neutronNetwork = defaultNeutronObject();
        VirtualNetwork mockedVirtualNet = PowerMock.createNiceMock(VirtualNetwork.class);
        expectNew(VirtualNetwork.class).andReturn(mockedVirtualNet);
        when(mockedApiConnector.findById(Project.class, neutronNetwork.getTenantID())).thenReturn(mockedProject);
        when(mockedApiConnector.create(mockedVirtualNet)).thenReturn(false);
        PowerMock.replay(mockedVirtualNet, VirtualNetwork.class);
        assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, networkHandler.canCreateNetwork(neutronNetwork));
    }

}
