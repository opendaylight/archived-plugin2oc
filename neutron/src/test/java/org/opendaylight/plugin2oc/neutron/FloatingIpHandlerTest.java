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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import net.juniper.contrail.api.ApiConnector;
import net.juniper.contrail.api.ApiPropertyBase;
import net.juniper.contrail.api.ObjectReference;
import net.juniper.contrail.api.types.FloatingIp;
import net.juniper.contrail.api.types.FloatingIpPool;
import net.juniper.contrail.api.types.Project;
import net.juniper.contrail.api.types.VirtualNetwork;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.when;

import org.opendaylight.controller.networkconfig.neutron.NeutronFloatingIP;
/**
 * Test Class for Neutron FloatingIp.
 */
public class FloatingIpHandlerTest {

    FloatingIpHandler floatingIphandler;
    NeutronFloatingIP mockedNeutronFloatingIP = mock(NeutronFloatingIP.class);
    ApiConnector mockedApiConnector = mock(ApiConnector.class);
    VirtualNetwork mockedVirtualNetwork = mock(VirtualNetwork.class);
    FloatingIp mockedFloatingIp = mock(FloatingIp.class);
    Project mockProject = mock(Project.class);
    FloatingIpPool mockFloatingIpPool = mock(FloatingIpPool.class);

    @Before
    public void beforeTest() {
        floatingIphandler = new FloatingIpHandler();
        assertNotNull(mockedApiConnector);
        assertNotNull(mockedNeutronFloatingIP);
        assertNotNull(mockedVirtualNetwork);
        assertNotNull(mockProject);
        assertNotNull(mockFloatingIpPool);
    }

    @After
    public void AfterTest() {
        floatingIphandler = null;
        Activator.apiConnector = null;
    }

    /* dummy params for Neutron Floating Ip */
    public NeutronFloatingIP defaultNeutronObject() {
        NeutronFloatingIP neutronFloatingIp = new NeutronFloatingIP();
        neutronFloatingIp.setFixedIPAddress("10.0.0.254");
        neutronFloatingIp.setFloatingIPAddress("10.0.1.254");
        neutronFloatingIp.setFloatingIPUUID("6b9570f2-17b1-4fc3-99ec-1b7f7778a29a");
        neutronFloatingIp.setFloatingNetworkUUID("54a271fe-0216-46bc-a3e6-1ff582fbd324");
        neutronFloatingIp.setPortUUID("64a271fe-0216-46bc-a3e6-1ff582fbd324");
        neutronFloatingIp.setTenantUUID("100071fe-0216-46bc-a3e6-1ff582fbd324");
        return neutronFloatingIp;
    }

    /* dummy params for Neutron Floating Ip for update */
    public NeutronFloatingIP deltaNeutronObjectUpdate() {
        NeutronFloatingIP neutronFloatingIp = new NeutronFloatingIP();
        neutronFloatingIp.setFixedIPAddress("10.0.0.254");
        neutronFloatingIp.setFloatingIPAddress("10.0.1.254");
        neutronFloatingIp.setFloatingIPUUID("6b9570f2-17b1-4fc3-99ec-1b7f7778a29a");
        neutronFloatingIp.setFloatingNetworkUUID("54a271fe-0216-46bc-a3e6-1ff582fbd324");
        neutronFloatingIp.setPortUUID("64a271fe-0216-46bc-a3e6-1ff582fbd324");
        neutronFloatingIp.setTenantUUID("100071fe-0216-46bc-a3e6-1ff582fbd324");
        return neutronFloatingIp;
    }

    /* dummy params for Neutron Floating Ip Pools */
    public FloatingIpPool defaultFloatingIpPoolObject() {
        FloatingIpPool neutronFloatingIpPool = new FloatingIpPool();
        neutronFloatingIpPool.setDisplayName("name");
        neutronFloatingIpPool.setName("name");
        neutronFloatingIpPool.setUuid("6b9570f2-17b1-4fc3-99ec-1b7f7778a29a");
        return neutronFloatingIpPool;
    }

    /* Test method to check if neutron floating ip is null */
    @Test
    public void testCanCreateFloatingIPNull() {
        Activator.apiConnector = mockedApiConnector;
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, floatingIphandler.canCreateFloatingIP(null));
    }

    /* Test method to check if neutron floating ip UUID is null */
    @Test
    public void testCanCreateFloatingIPUuidNull() {
        Activator.apiConnector = mockedApiConnector;
        NeutronFloatingIP neutronFloatingIP = defaultNeutronObject();
        neutronFloatingIP.setFloatingIPUUID("");
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, floatingIphandler.canCreateFloatingIP(neutronFloatingIP));
    }

    /* Test method to check if neutron Tenant UUID is null */
    @Test
    public void testCanCreateTenantUUIDNull() {
        Activator.apiConnector = mockedApiConnector;
        NeutronFloatingIP neutronFloatingIP = defaultNeutronObject();
        neutronFloatingIP.setTenantUUID(null);
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, floatingIphandler.canCreateFloatingIP(neutronFloatingIP));
    }

    /* Test method to check if neutron Floating IP Address is null */
    @Test
    public void testCanCreateFloatingIPAddressNull() {
        Activator.apiConnector = mockedApiConnector;
        NeutronFloatingIP neutronFloatingIP = defaultNeutronObject();
        neutronFloatingIP.setFloatingIPAddress(null);
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, floatingIphandler.canCreateFloatingIP(neutronFloatingIP));
    }

    /* Test method to check if neutron Floating IP canCreate FIP already exist */
    @Test
    public void testCanCreateFloatingIPExist() throws IOException {
        Activator.apiConnector = mockedApiConnector;
        NeutronFloatingIP neutronFloatingIP = defaultNeutronObject();
        when(mockedApiConnector.findById(FloatingIp.class, neutronFloatingIP.getFloatingNetworkUUID())).thenReturn(mockedFloatingIp);
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, floatingIphandler.canCreateFloatingIP(neutronFloatingIP));
    }

    /* Test method to check if neutron Floating IP canCreate project is null */
    @Test
    public void testCanCreateFloatingProjectNull() throws IOException {
        Activator.apiConnector = mockedApiConnector;
        NeutronFloatingIP neutronFloatingIP = defaultNeutronObject();
        when(mockedApiConnector.findById(FloatingIp.class, neutronFloatingIP.getFloatingNetworkUUID())).thenReturn(null);
        when(mockedApiConnector.findById(Project.class, neutronFloatingIP.getTenantUUID())).thenReturn(null);
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, floatingIphandler.canCreateFloatingIP(neutronFloatingIP));
    }

    /*Test method to check if neutron Floating IP canCreate Virtual network not found*/
    @Test
    public void testCanCreateFloatingVirtualNetNotFound() throws IOException {
        Activator.apiConnector = mockedApiConnector;
        NeutronFloatingIP neutronFloatingIP = defaultNeutronObject();
        when(mockedApiConnector.findById(FloatingIp.class, neutronFloatingIP.getFloatingNetworkUUID())).thenReturn(mockedFloatingIp);
        when(mockedApiConnector.findById(Project.class, neutronFloatingIP.getTenantUUID())).thenReturn(mockProject);
        when(mockedApiConnector.findById(VirtualNetwork.class, neutronFloatingIP.getFloatingNetworkUUID())).thenReturn(null);
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, floatingIphandler.canCreateFloatingIP(neutronFloatingIP));
    }

    /* Test method to check if canCreate neutron Floating ip pool cannot not find */
    @Test
       public void testCanCreateFloatingIpPoolNull() throws IOException {
               Activator.apiConnector = mockedApiConnector;
               NeutronFloatingIP neutronFloatingIP = defaultNeutronObject();
               when(mockedApiConnector.findById(FloatingIp.class,neutronFloatingIP.getFloatingIPUUID())).thenReturn(null);
               when(mockedApiConnector.findById(Project.class,neutronFloatingIP.getTenantUUID())).thenReturn(mockProject);
               when(mockedApiConnector.findById(VirtualNetwork.class,neutronFloatingIP.getFloatingNetworkUUID())).thenReturn(mockedVirtualNetwork);
               List<ObjectReference<ApiPropertyBase>> fipPoolList = new ArrayList<ObjectReference<ApiPropertyBase>>();
               fipPoolList.add(new ObjectReference<ApiPropertyBase>(mockFloatingIpPool.getQualifiedName(), null));
               fipPoolList.get(0).setReference(mockFloatingIpPool.getQualifiedName(), null, "", "119570f2-17b1-4fc3-99ec-1b7f7778a29a");
               when(mockedVirtualNetwork.getFloatingIpPools()).thenReturn(fipPoolList);
               when(mockedApiConnector.findById(FloatingIpPool.class,mockedVirtualNetwork.getFloatingIpPools().get(0).getUuid())).thenReturn(null);
               assertEquals(HttpURLConnection.HTTP_NOT_FOUND,floatingIphandler.canCreateFloatingIP(neutronFloatingIP));
       }

    /* Test method to check if canCreate neutron Floating ip pool return 200 ok */
    @Test
       public void testCanCreateFloatingIpOk() throws IOException {
               Activator.apiConnector = mockedApiConnector;
               NeutronFloatingIP neutronFloatingIP = defaultNeutronObject();
               when(mockedApiConnector.findById(FloatingIp.class,neutronFloatingIP.getFloatingIPUUID())).thenReturn(null);
               when(mockedApiConnector.findById(Project.class,neutronFloatingIP.getTenantUUID())).thenReturn(mockProject);
               when(mockedApiConnector.findById(VirtualNetwork.class,neutronFloatingIP.getFloatingNetworkUUID())).thenReturn(mockedVirtualNetwork);
               List<ObjectReference<ApiPropertyBase>> fipPoolList = new ArrayList<ObjectReference<ApiPropertyBase>>();
               fipPoolList.add(new ObjectReference<ApiPropertyBase>(mockFloatingIpPool.getQualifiedName(), null));
               fipPoolList.get(0).setReference(mockFloatingIpPool.getQualifiedName(), null, "", "119570f2-17b1-4fc3-99ec-1b7f7778a29a");
               when(mockedVirtualNetwork.getFloatingIpPools()).thenReturn(fipPoolList);
               when(mockedApiConnector.findById(FloatingIpPool.class,mockedVirtualNetwork.getFloatingIpPools().get(0).getUuid())).thenReturn(mockFloatingIpPool);
               assertEquals(HttpURLConnection.HTTP_OK,floatingIphandler.canCreateFloatingIP(neutronFloatingIP));
       }

    /* Test method to check if can update FloatingIP Null object */
    @Test
    public void testcanUpdateFloatingIPObjNull() throws IOException {
        Activator.apiConnector = mockedApiConnector;
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, floatingIphandler.canUpdateFloatingIP(null, null));
    }

    /* Test method to check if can update FloatingIP obj not found */
    @Test
    public void testcanUpdateFloatingIPNull() throws IOException {
        Activator.apiConnector = mockedApiConnector;
        NeutronFloatingIP neutronFloatingIP = defaultNeutronObject();
        NeutronFloatingIP deltaNeutronFloatingIP = deltaNeutronObjectUpdate();
        when(mockedApiConnector.findById(FloatingIp.class, neutronFloatingIP.getFloatingIPUUID())).thenReturn(null);
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, floatingIphandler.canUpdateFloatingIP(deltaNeutronFloatingIP, neutronFloatingIP));
    }

    /* Test method to check if can update FloatingIP return status OK */
    @Test
    public void testcanUpdateFloatingIPOK() throws IOException {
        Activator.apiConnector = mockedApiConnector;
        NeutronFloatingIP neutronFloatingIP = defaultNeutronObject();
        NeutronFloatingIP deltaNeutronFloatingIP = deltaNeutronObjectUpdate();
        when(mockedApiConnector.findById(FloatingIp.class, neutronFloatingIP.getFloatingIPUUID())).thenReturn(mockedFloatingIp);
        assertEquals(HttpURLConnection.HTTP_OK, floatingIphandler.canUpdateFloatingIP(deltaNeutronFloatingIP, neutronFloatingIP));
    }

    /* Test method to check if can delete FloatingIP not found */
    @Test
    public void testcanDeleteFloatingIPNotFound() throws IOException {
        Activator.apiConnector = mockedApiConnector;
        NeutronFloatingIP neutronFloatingIP = defaultNeutronObject();
        when(mockedApiConnector.findById(FloatingIp.class, neutronFloatingIP.getFloatingIPUUID())).thenReturn(null);
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, floatingIphandler.canDeleteFloatingIP(neutronFloatingIP));
    }

    /* Test method to check if can delete FloatingIP returns status 200 ok */
    @Test
    public void testcanDeleteFloatingIPOK() throws IOException {
        Activator.apiConnector = mockedApiConnector;
        NeutronFloatingIP neutronFloatingIP = defaultNeutronObject();
        when(mockedApiConnector.findById(FloatingIp.class, neutronFloatingIP.getFloatingIPUUID())).thenReturn(mockedFloatingIp);
        assertEquals(HttpURLConnection.HTTP_OK, floatingIphandler.canDeleteFloatingIP(neutronFloatingIP));
    }
}
