/*
 * Copyright (C) 2014 Juniper Networks, Inc.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 */
package org.opendaylight.plugin2oc.neutron;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import net.juniper.contrail.api.ApiConnector;
import net.juniper.contrail.api.ObjectReference;
import net.juniper.contrail.api.types.InstanceIp;
import net.juniper.contrail.api.types.Project;
import net.juniper.contrail.api.types.SubnetType;
import net.juniper.contrail.api.types.VirtualMachine;
import net.juniper.contrail.api.types.VirtualMachineInterface;
import net.juniper.contrail.api.types.VirtualNetwork;
import net.juniper.contrail.api.types.VnSubnetsType;

import org.opendaylight.controller.networkconfig.neutron.NeutronPort;
import org.opendaylight.controller.networkconfig.neutron.Neutron_IPs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.controller.networkconfig.neutron.NeutronNetwork;

/**
 * Test Class for Neutron Port.
 */

public class PortHandlerTest {
    PortHandler porthandler;
    PortHandler mockedporthandler = mock(PortHandler.class);
    NeutronNetwork mockedNeutronNetwork = mock(NeutronNetwork.class);
    ApiConnector mockedApiConnector = mock(ApiConnector.class);
    VirtualNetwork mockedVirtualNetwork = mock(VirtualNetwork.class);
    NeutronPort mockedNeutronPort = mock(NeutronPort.class);
    VirtualMachineInterface mockedVirtualMachineInterface = mock(VirtualMachineInterface.class);
    VirtualMachine mockedvirtualMachine = mock(VirtualMachine.class);
    Project mockedProject = mock(Project.class);
    Neutron_IPs mockNeutron_IPs = mock(Neutron_IPs.class);
    InstanceIp mockInstanceIp = mock(InstanceIp.class);

    @Before
    public void beforeTest() {
        porthandler = new PortHandler();
        assertNotNull(mockedApiConnector);
        assertNotNull(mockedNeutronNetwork);
        assertNotNull(mockedVirtualNetwork);
        assertNotNull(mockedNeutronPort);
        assertNotNull(mockedVirtualMachineInterface);
        assertNotNull(mockedvirtualMachine);
        assertNotNull(mockNeutron_IPs);
        assertNotNull(mockInstanceIp);
        assertNotNull(mockedProject);
    }

    @After
    public void AfterTest() {
        porthandler = null;
        Activator.apiConnector = null;
    }

    /* dummy params for Neutron Port */
    public NeutronPort defaultNeutronPortObject() {
        NeutronPort neutronPort = new NeutronPort();
        neutronPort.setPortUUID("64a271fe-0216-46bc-a3e6-1ff582fbd324");
        neutronPort.setNetworkUUID("54a271fe-0216-46bc-a3e6-1ff582fbd324");
        neutronPort.setMacAddress("02:70:72:93:4d:d6");
        neutronPort.setName("port12");
        neutronPort.setDeviceID("100071fe-0216-46bc-a3e6-1ff582fbd324");
        neutronPort.setTenantID("100071fe-0216-46bc-a3e6-1ff582fbd324");
        List<Neutron_IPs> ips = new ArrayList<Neutron_IPs>();
        ips.add(mockNeutron_IPs);
        neutronPort.setFixedIPs(ips);
        return neutronPort;
    }

    /* dummy params for Update Neutron Port */
    public NeutronPort detaNeutronPort() {
        NeutronPort dummyNeutronPort = new NeutronPort();
        dummyNeutronPort.setPortUUID("64a271fe-0216-46bc-a3e6-1ff582fbd324");
        dummyNeutronPort.setNetworkUUID("54a271fe-0216-46bc-a3e6-1ff582fbd324");
        dummyNeutronPort.setName("port01");
        dummyNeutronPort.setDeviceID("100071fe-0216-46bc-a3e6-1ff582fbd324");
        dummyNeutronPort.setTenantID("100071fe-0216-46bc-a3e6-1ff582fbd324");
        List<Neutron_IPs> ips = new ArrayList<Neutron_IPs>();
        ips.add(mockNeutron_IPs);
        dummyNeutronPort.setFixedIPs(ips);
        return dummyNeutronPort;
    }

    /* Test method to check if neutron port is null */

    @Test
    public void testCanCreatePortNull() {
        Activator.apiConnector = mockedApiConnector;
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, porthandler.canCreatePort(null));
    }

    /* Test method to check if neutron port PortUUID is empty */
    @Test
    public void testCanCreatePortIdEmtpy() {
        Activator.apiConnector = mockedApiConnector;
        NeutronPort neutronPort = defaultNeutronPortObject();
        neutronPort.setPortUUID("");
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, porthandler.canCreatePort(neutronPort));
    }

    /* Test method to check if neutron port tenant id is null */
    @Test
    public void testCanCreatePortTenantIdNull() {
        Activator.apiConnector = mockedApiConnector;
        NeutronPort neutronPort = defaultNeutronPortObject();
        neutronPort.setTenantID(null);
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, porthandler.canCreatePort(neutronPort));
    }

    /* Test method to check if neutron port name is null */
    @Test
    public void testCanCreatePortNameNull() {
        Activator.apiConnector = mockedApiConnector;
        NeutronPort neutronPort = defaultNeutronPortObject();
        neutronPort.setName("");
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, porthandler.canCreatePort(neutronPort));
    }

    /* Test method to check if neutron port and network belongs to same tenant */
    @Test
    public void testCanCreatePortSameTenant() throws IOException {
        Activator.apiConnector = mockedApiConnector;
        NeutronPort neutronPort = defaultNeutronPortObject();
        when(mockedVirtualNetwork.getParentUuid()).thenReturn("f00071fe-0216-46bc-a3e6-1ff582fbd324");
        when(mockedApiConnector.findById(Project.class, neutronPort.getTenantID())).thenReturn(mockedProject);
        when(mockedApiConnector.findById(VirtualNetwork.class, neutronPort.getNetworkUUID())).thenReturn(mockedVirtualNetwork);
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, porthandler.canCreatePort(neutronPort));
    }

    /* Test method to check if Project is not available */
    @Test
    public void testCanCreatePortProjectNotFound() throws Exception {
        Activator.apiConnector = mockedApiConnector;
        NeutronPort neutronPort = defaultNeutronPortObject();
        when(mockedApiConnector.findById(Project.class, neutronPort.getTenantID())).thenReturn(null);
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, porthandler.canCreatePort(neutronPort));
    }

    /* Test method to check if neutron port create exist */
    @Test
    public void testCanCreatePortExist() throws IOException {
        Activator.apiConnector = mockedApiConnector;
        NeutronPort neutronPort = defaultNeutronPortObject();
        when(mockedVirtualNetwork.getParentUuid()).thenReturn(neutronPort.getTenantID());
        when(mockedApiConnector.findById(Project.class, neutronPort.getTenantID())).thenReturn(mockedProject);
        when(mockedApiConnector.findById(VirtualNetwork.class, neutronPort.getNetworkUUID())).thenReturn(mockedVirtualNetwork);
        when(mockedApiConnector.findById(VirtualMachineInterface.class, neutronPort.getPortUUID())).thenReturn(mockedVirtualMachineInterface);
        assertEquals(HttpURLConnection.HTTP_FORBIDDEN, porthandler.canCreatePort(neutronPort));
    }

    /* Test method to check if neutron port create exist with same name */
    @Test
    public void testCanCreatePortExistSameName() throws IOException {
        Activator.apiConnector = mockedApiConnector;
        NeutronPort neutronPort = defaultNeutronPortObject();
        when(mockedVirtualNetwork.getParentUuid()).thenReturn(neutronPort.getTenantID());
        when(mockedApiConnector.findById(Project.class, neutronPort.getTenantID())).thenReturn(mockedProject);
        when(mockedApiConnector.findById(VirtualNetwork.class, neutronPort.getNetworkUUID())).thenReturn(mockedVirtualNetwork);
        when(mockedApiConnector.findById(VirtualMachineInterface.class, neutronPort.getPortUUID())).thenReturn(null);
        when(mockedApiConnector.findByName(VirtualMachineInterface.class, mockedProject, neutronPort.getName())).thenReturn("PORT-001");
        assertEquals(HttpURLConnection.HTTP_FORBIDDEN, porthandler.canCreatePort(neutronPort));
    }

    /* Test method to check if neutron port create ok */
    @Test
    public void testCanCreatePortOK() throws IOException {
        Activator.apiConnector = mockedApiConnector;
        NeutronPort neutronPort = defaultNeutronPortObject();
        when(mockedVirtualNetwork.getParentUuid()).thenReturn(neutronPort.getTenantID());
        when(mockedApiConnector.findById(Project.class, neutronPort.getTenantID())).thenReturn(mockedProject);
        when(mockedApiConnector.findById(VirtualNetwork.class, neutronPort.getNetworkUUID())).thenReturn(mockedVirtualNetwork);
        when(mockedApiConnector.findById(VirtualMachineInterface.class, neutronPort.getPortUUID())).thenReturn(null);
        when(mockedApiConnector.findByName(VirtualMachineInterface.class, mockedProject, neutronPort.getName())).thenReturn(null);
        assertEquals(HttpURLConnection.HTTP_OK, porthandler.canCreatePort(neutronPort));
    }

    /* Test method to check if neutron port is null for delete */
    @Test
    public void testcanDeletePortNull() throws IOException {
        Activator.apiConnector = mockedApiConnector;
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, porthandler.canDeletePort(null));
    }

    /* Test method to check if virtual machine interface is null for delete */
    @Test
    public void testcanDeletePortVirtualMachineInterfaceNull() throws IOException {
        Activator.apiConnector = mockedApiConnector;
        NeutronPort neutronPort = defaultNeutronPortObject();
        when(mockedApiConnector.findById(VirtualMachineInterface.class, neutronPort.getID())).thenReturn(null);
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, porthandler.canDeletePort(neutronPort));
    }

    /* Test method to check if can delete return status 200 OK */
    @Test
    public void testcanDeletePortOk() throws IOException {
        Activator.apiConnector = mockedApiConnector;
        NeutronPort neutronPort = defaultNeutronPortObject();
        when(mockedApiConnector.findById(VirtualMachineInterface.class, neutronPort.getID())).thenReturn(mockedVirtualMachineInterface);
        when(mockedVirtualMachineInterface.getFloatingIpBackRefs()).thenReturn(null);
        assertEquals(HttpURLConnection.HTTP_OK, porthandler.canDeletePort(neutronPort));
    }

    /* Test method to update port with null neutron port and delta port obj*/
    @Test
    public void testcanUpdatePortNull() throws IOException {
        Activator.apiConnector = mockedApiConnector;
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, porthandler.canUpdatePort(null, null));
    }

    /* Test method to update port with Mac address */
    @Test
    public void testcanUpdatePortMacAddress() throws IOException {
        Activator.apiConnector = mockedApiConnector;
        NeutronPort neutronPort = defaultNeutronPortObject();
        NeutronPort dummyNeutronPort = detaNeutronPort();
        dummyNeutronPort.setMacAddress("00:70:72:93:4d:d6");
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, porthandler.canUpdatePort(dummyNeutronPort, neutronPort));
    }

    /* Test method to update port when port not found */
    @Test
    public void testcanUpdatePortVMInotFound() throws IOException {
        Activator.apiConnector = mockedApiConnector;
        NeutronPort neutronPort = defaultNeutronPortObject();
        NeutronPort dummyNeutronPort = detaNeutronPort();
        when(mockedApiConnector.findById(Project.class, neutronPort.getTenantID())).thenReturn(mockedProject);
        when(mockedApiConnector.findById(VirtualMachineInterface.class, neutronPort.getPortUUID())).thenReturn(null);
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, porthandler.canUpdatePort(dummyNeutronPort, neutronPort));
    }

    /* Test method to update port when another port already exist with same name */
    @Test
    public void testcanUpdatePortVMIbyName() throws IOException {
        Activator.apiConnector = mockedApiConnector;
        NeutronPort neutronPort = defaultNeutronPortObject();
        NeutronPort dummyNeutronPort = detaNeutronPort();
        when(mockedApiConnector.findById(Project.class, neutronPort.getTenantID())).thenReturn(mockedProject);
        when(mockedApiConnector.findById(VirtualMachineInterface.class, neutronPort.getPortUUID())).thenReturn(mockedVirtualMachineInterface);
        when(mockedApiConnector.findByName(VirtualMachineInterface.class, mockedProject, dummyNeutronPort.getName())).thenReturn("network-001");
        assertEquals(HttpURLConnection.HTTP_FORBIDDEN, porthandler.canUpdatePort(dummyNeutronPort, neutronPort));
    }

    /*
     * Test method to update port when check for Subnet UUID must exist in the
     * network
     */
    @Test
    public void testcanUpdatePortCheck() throws IOException {
        Activator.apiConnector = mockedApiConnector;
        NeutronPort neutronPort = defaultNeutronPortObject();
        NeutronPort dummyNeutronPort = detaNeutronPort();
        dummyNeutronPort.setFixedIPs(null);
        when(mockedApiConnector.findById(Project.class, neutronPort.getTenantID())).thenReturn(mockedProject);
        when(mockedApiConnector.findById(VirtualMachineInterface.class, neutronPort.getPortUUID())).thenReturn(mockedVirtualMachineInterface);
        when(mockedApiConnector.findByName(VirtualMachineInterface.class, mockedProject, dummyNeutronPort.getName())).thenReturn(null);
        when(mockedApiConnector.findById(VirtualNetwork.class, neutronPort.getNetworkUUID())).thenReturn(mockedVirtualNetwork);
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, porthandler.canUpdatePort(dummyNeutronPort, neutronPort));
    }

    /* Test method to update port when check for FixedIPs Not Null */
    @Test
    public void testcanUpdatePortFixedIPsNotNull() throws IOException {
        Activator.apiConnector = mockedApiConnector;
        NeutronPort neutronPort = defaultNeutronPortObject();
        NeutronPort dummyNeutronPort = detaNeutronPort();
        when(mockedApiConnector.findById(Project.class, neutronPort.getTenantID())).thenReturn(mockedProject);
        when(mockedApiConnector.findById(VirtualMachineInterface.class, neutronPort.getPortUUID())).thenReturn(mockedVirtualMachineInterface);
        when(mockedApiConnector.findByName(VirtualMachineInterface.class, mockedProject, dummyNeutronPort.getName())).thenReturn(null);
        List<Neutron_IPs> ips = new ArrayList<Neutron_IPs>();
        Neutron_IPs fixedIP = new Neutron_IPs();
        fixedIP.setSubnetUUID("9b9570f2-17b1-4fc3-99ec-1b7f7778a29b");
        ips.add(fixedIP);
        dummyNeutronPort.setFixedIPs(ips);
        when(mockedApiConnector.findById(VirtualNetwork.class, neutronPort.getNetworkUUID())).thenReturn(mockedVirtualNetwork);
        VnSubnetsType vnSubnetType = new VnSubnetsType();
        ObjectReference<VnSubnetsType> ref = new ObjectReference<>();
        List<ObjectReference<VnSubnetsType>> ipamRefs = new ArrayList<ObjectReference<VnSubnetsType>>();
        List<VnSubnetsType.IpamSubnetType> subnets = new ArrayList<VnSubnetsType.IpamSubnetType>();
        VnSubnetsType.IpamSubnetType subnetType = new VnSubnetsType.IpamSubnetType();
        SubnetType type = new SubnetType();
        List<String> temp = new ArrayList<String>();
        for (int i = 0; i < 1; i++) {
            subnetType.setSubnet(type);
            subnetType.setSubnetUuid("0b9570f2-17b1-4fc3-99ec-1b7f7778a29b");
            subnetType.getSubnet().setIpPrefix("10.0.0.0");
            subnetType.getSubnet().setIpPrefixLen(24);
            subnets.add(subnetType);
            vnSubnetType.addIpamSubnets(subnetType);
            ref.setReference(temp, vnSubnetType, "", "");
            ipamRefs.add(ref);
        }
        when(mockedVirtualNetwork.getNetworkIpam()).thenReturn(ipamRefs);
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, porthandler.canUpdatePort(dummyNeutronPort, neutronPort));
    }

    /* Test method to update port when check for FixedIPs Not Null */
    @Test
    public void testcanUpdatePortok() throws IOException {
        Activator.apiConnector = mockedApiConnector;
        NeutronPort neutronPort = defaultNeutronPortObject();
        NeutronPort dummyNeutronPort = detaNeutronPort();
        when(mockedApiConnector.findById(Project.class, neutronPort.getTenantID())).thenReturn(mockedProject);
        when(mockedApiConnector.findById(VirtualMachineInterface.class, neutronPort.getPortUUID())).thenReturn(mockedVirtualMachineInterface);
        when(mockedApiConnector.findByName(VirtualMachineInterface.class, mockedProject, dummyNeutronPort.getName())).thenReturn(null);
        when(mockedApiConnector.findById(VirtualNetwork.class, dummyNeutronPort.getNetworkUUID())).thenReturn(null);
        dummyNeutronPort.setNetworkUUID(null);
        dummyNeutronPort.setFixedIPs(null);
        assertEquals(HttpURLConnection.HTTP_OK, porthandler.canUpdatePort(dummyNeutronPort, neutronPort));
    }
}