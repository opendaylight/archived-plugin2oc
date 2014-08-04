package org.opendaylight.plugin2oc.neutron;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.net.HttpURLConnection;

import net.juniper.contrail.api.ApiConnector;
import net.juniper.contrail.api.types.FloatingIp;
import net.juniper.contrail.api.types.FloatingIpPool;
import net.juniper.contrail.api.types.Project;
import net.juniper.contrail.api.types.VirtualNetwork;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.when;

import org.opendaylight.controller.networkconfig.neutron.NeutronFloatingIP;

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
        when(mockedNeutronFloatingIP.getFloatingIPUUID()).thenReturn("");
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, floatingIphandler.canCreateFloatingIP(mockedNeutronFloatingIP));
    }

    /* Test method to check if neutron Tenant UUID is null */
    @Test
    public void testCanCreateTenantUUIDNull() {
        Activator.apiConnector = mockedApiConnector;
        when(mockedNeutronFloatingIP.getTenantUUID()).thenReturn(null);
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, floatingIphandler.canCreateFloatingIP(mockedNeutronFloatingIP));
    }

    /* Test method to check if neutron Floating IP Address is null */
    @Test
    public void testCanCreateFloatingIPAddressNull() {
        Activator.apiConnector = mockedApiConnector;
        when(mockedNeutronFloatingIP.getTenantUUID()).thenReturn("100071fe-0216-46bc-a3e6-1ff582fbd329");
        when(mockedNeutronFloatingIP.getFloatingIPAddress()).thenReturn(null);
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, floatingIphandler.canCreateFloatingIP(mockedNeutronFloatingIP));
    }

    /* Test method to check if neutron Floating IP canCreate project is null */
    @Test
    public void testCanCreateFloatingProjectNull() throws IOException {
        Activator.apiConnector = mockedApiConnector;
        NeutronFloatingIP neutronFloatingIP = defaultNeutronObject();
        when(mockedApiConnector.findById(Project.class, neutronFloatingIP.getTenantUUID())).thenReturn(null);
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, floatingIphandler.canCreateFloatingIP(neutronFloatingIP));
    }

    /* Test method to check if neutron Floating IP canCreate project is null */
    @Test
    public void testCanCreateFloatingVirtualNetworkNull() throws IOException {
        Activator.apiConnector = mockedApiConnector;
        NeutronFloatingIP neutronFloatingIP = defaultNeutronObject();
        when(mockedApiConnector.findById(Project.class, neutronFloatingIP.getTenantUUID())).thenReturn(mockProject);
        when(mockedApiConnector.findById(VirtualNetwork.class, neutronFloatingIP.getFloatingNetworkUUID())).thenReturn(null);
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, floatingIphandler.canCreateFloatingIP(neutronFloatingIP));
    }

    // /* Test method to check if neutron Floating IP canCreate project is
    // null*/
    // @Test
    // public void testCanCreateFloatingIpNull() throws IOException {
    // Activator.apiConnector = mockedApiConnector;
    // NeutronFloatingIP neutronFloatingIP = defaultNeutronObject();
    // when(mockedApiConnector.findById(Project.class,
    // neutronFloatingIP.getTenantUUID())).thenReturn(mockProject);
    // when(mockedApiConnector.findById(VirtualNetwork.class,
    // neutronFloatingIP.getFloatingNetworkUUID())).thenReturn(mockedVirtualNetwork);
    // when(mockedApiConnector.findById(FloatingIpPool.class,
    // mockedVirtualNetwork.getFloatingIpPools().get(0).getUuid())).thenReturn(null);
    // assertEquals(HttpURLConnection.HTTP_NOT_FOUND,
    // floatingIphandler.canCreateFloatingIP(neutronFloatingIP));
    // }
    /* Test method to check if can update FloatingIP Null object */
    @Test
    public void testcanUpdateFloatingIPObjNull() throws IOException {
        Activator.apiConnector = mockedApiConnector;
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, floatingIphandler.canUpdateFloatingIP(null, null));
    }

    /* Test method to check if can update FloatingIP Null */
    @Test
    public void testcanUpdateFloatingIPNull() throws IOException {
        Activator.apiConnector = mockedApiConnector;
        NeutronFloatingIP neutronFloatingIP = defaultNeutronObject();
        NeutronFloatingIP deltaNeutronFloatingIP = deltaNeutronObjectUpdate();
        when(mockedApiConnector.findById(FloatingIp.class, neutronFloatingIP.getFloatingIPUUID())).thenReturn(null);
        assertEquals(HttpURLConnection.HTTP_FORBIDDEN, floatingIphandler.canUpdateFloatingIP(deltaNeutronFloatingIP, neutronFloatingIP));
    }

    /* Test method to check if can update FloatingIP failed */
    @Test
    public void testcanUpdateFloatingIPFailed() throws IOException {
        Activator.apiConnector = mockedApiConnector;
        NeutronFloatingIP neutronFloatingIP = defaultNeutronObject();
        NeutronFloatingIP deltaNeutronFloatingIP = deltaNeutronObjectUpdate();
        deltaNeutronFloatingIP.setPortUUID(null);
        when(mockedApiConnector.findById(FloatingIp.class, neutronFloatingIP.getFloatingIPUUID())).thenReturn(mockedFloatingIp);
        when(mockedApiConnector.update(mockedFloatingIp)).thenReturn(false);
        assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, floatingIphandler.canUpdateFloatingIP(deltaNeutronFloatingIP, neutronFloatingIP));
    }

    /* Test method to check if can update FloatingIP updated */
    @Test
    public void testcanUpdateFloatingIPTrue() throws IOException {
        Activator.apiConnector = mockedApiConnector;
        NeutronFloatingIP neutronFloatingIP = defaultNeutronObject();
        NeutronFloatingIP deltaNeutronFloatingIP = deltaNeutronObjectUpdate();
        deltaNeutronFloatingIP.setPortUUID(null);
        when(mockedApiConnector.findById(FloatingIp.class, neutronFloatingIP.getFloatingIPUUID())).thenReturn(mockedFloatingIp);
        when(mockedApiConnector.update(mockedFloatingIp)).thenReturn(true);
        assertEquals(HttpURLConnection.HTTP_OK, floatingIphandler.canUpdateFloatingIP(deltaNeutronFloatingIP, neutronFloatingIP));
    }

    /* Test method to check if can delete FloatingIP Null object */
    @Test
    public void testcanDeleteFloatingIPObjNull() throws IOException {
        Activator.apiConnector = mockedApiConnector;
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, floatingIphandler.canDeleteFloatingIP(null));
    }

    /* Test method to check if can delete FloatingIP not found */
    @Test
    public void testcanDeleteFloatingIPNotFound() throws IOException {
        Activator.apiConnector = mockedApiConnector;
        NeutronFloatingIP neutronFloatingIP = defaultNeutronObject();
        when(mockedApiConnector.findById(FloatingIp.class, neutronFloatingIP.getFloatingIPUUID())).thenReturn(null);
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, floatingIphandler.canDeleteFloatingIP(neutronFloatingIP));
    }
}
