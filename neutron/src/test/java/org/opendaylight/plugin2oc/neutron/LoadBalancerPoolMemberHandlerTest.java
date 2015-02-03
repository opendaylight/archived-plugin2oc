package org.opendaylight.plugin2oc.neutron;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import net.juniper.contrail.api.ApiConnector;
import net.juniper.contrail.api.types.LoadbalancerMember;
import net.juniper.contrail.api.types.LoadbalancerPool;
import net.juniper.contrail.api.types.Project;
import net.juniper.contrail.api.types.VirtualMachineInterface;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.controller.networkconfig.neutron.NeutronLoadBalancerPool;
import org.opendaylight.controller.networkconfig.neutron.NeutronLoadBalancerPoolMember;
import org.opendaylight.controller.networkconfig.neutron.NeutronPort;
import org.opendaylight.controller.networkconfig.neutron.Neutron_IPs;

/**
 * Test Class for LoadBalancerPoolMember.
 */
public class LoadBalancerPoolMemberHandlerTest {

    LoadBalancerPoolMemberHandler loadBalancerPoolMemberHandler;
    LoadBalancerPoolMemberHandler mockLoadBalancerPoolMemberHandler = mock(LoadBalancerPoolMemberHandler.class);
    ApiConnector mockApiConnector = mock(ApiConnector.class);
    Project mockProject = mock(Project.class);
    NeutronLoadBalancerPool mockNeutronLoadBalancerPool = mock(NeutronLoadBalancerPool.class);
    NeutronLoadBalancerPoolMember mockNeutronLoadBalancerPoolMember = mock(NeutronLoadBalancerPoolMember.class);
    LoadbalancerPool mockLoadbalancerPool = mock(LoadbalancerPool.class);
    LoadbalancerMember mockLoadbalancerMember = mock(LoadbalancerMember.class);
    VirtualMachineInterface mockedVirtualMachineInterface = mock(VirtualMachineInterface.class);
    Neutron_IPs mockNeutron_IPs = mock(Neutron_IPs.class);

    @Before
    public void beforeTest() {
        loadBalancerPoolMemberHandler = new LoadBalancerPoolMemberHandler();
        assertNotNull(mockLoadBalancerPoolMemberHandler);
        assertNotNull(mockNeutronLoadBalancerPool);
        assertNotNull(mockNeutronLoadBalancerPoolMember);
        assertNotNull(mockLoadbalancerPool);
        assertNotNull(mockLoadbalancerMember);
        assertNotNull(mockApiConnector);
        assertNotNull(mockProject);
    }

    @After
    public void AfterTest() {
        loadBalancerPoolMemberHandler = null;
        Activator.apiConnector = null;
    }

    /* Dummy parameters for Neutron LoadBalancerPoolMember */
    public NeutronLoadBalancerPoolMember defaultNeutronLoadBalancerMemberObject() {
        NeutronLoadBalancerPoolMember neutronObject = new NeutronLoadBalancerPoolMember();
        neutronObject.setPoolID("6b9570f2-17b1-4fc3-99ec-1b7f7778a29a");
        neutronObject.setPoolMemberAdminStateIsUp(true);
        neutronObject.setPoolMemberID("779570f2-17b1-4fc3-99ec-1b7f7778a29a");
        neutronObject.setPoolMemberProtoPort(80);
        neutronObject.setPoolMemberStatus("poolMemberStatus");
        neutronObject.setPoolMemberSubnetID("000570f2-17b1-4fc3-99ec-1b7f7778a29a");
        neutronObject.setPoolMemberTenantID("001570f2-17b1-4fc3-99ec-1b7f7778a29a");
        neutronObject.setPoolMemberWeight(20);
        return neutronObject;
    }

    /* dummy params for Neutron Port */
    public NeutronPort defaultNeutronPortObject() {
        NeutronPort neutronPort = new NeutronPort();
        neutronPort.setPortUUID("64a271fe-0216-46bc-a3e6-1ff582fbd324");
        neutronPort.setNetworkUUID("54a271fe-0216-46bc-a3e6-1ff582fbd324");
        neutronPort.setMacAddress("02:70:72:93:4d:d6");
        neutronPort.setName("port12");
        neutronPort.setDeviceID("100071fe-0216-46bc-a3e6-1ff582fbd324");
        neutronPort.setTenantID("001570f2-17b1-4fc3-99ec-1b7f7778a29a");
        List<Neutron_IPs> ips = new ArrayList<Neutron_IPs>();
        ips.add(mockNeutron_IPs);
        neutronPort.setFixedIPs(ips);
        return neutronPort;
    }

    /* Test method to check if neutron LoadBalancerPoolMember is null */
    @Test
    public void testCanCreateNeutronLoadBalancerMemberNull() {
        Activator.apiConnector = mockApiConnector;
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST,
                loadBalancerPoolMemberHandler.canCreateNeutronLoadBalancerPoolMember(null));
    }

    /*
     * Test method to check if neutron LoadBalancerPoolMember tenant ID/SUbnetID
     * is null
     */
    @Test
    public void testCanCreateNeutronLoadBalancerMemberTenantIDNull() {
        Activator.apiConnector = mockApiConnector;
        NeutronLoadBalancerPoolMember neutronObject = new NeutronLoadBalancerPoolMember();
        neutronObject.setPoolMemberTenantID(null);
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST,
                loadBalancerPoolMemberHandler.canCreateNeutronLoadBalancerPoolMember(neutronObject));
    }

    /* Test method to check if neutron LoadBalancerPoolMember project exist */
    @Test
    public void testCanCreateNeutronLoadBalancerMemberProjectNull() throws IOException {
        Activator.apiConnector = mockApiConnector;
        NeutronLoadBalancerPoolMember neutronLoadBalancerMember = defaultNeutronLoadBalancerMemberObject();
        when(mockApiConnector.findById(Project.class, neutronLoadBalancerMember.getPoolMemberTenantID())).thenReturn(
                null);
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND,
                loadBalancerPoolMemberHandler.canCreateNeutronLoadBalancerPoolMember(neutronLoadBalancerMember));
    }

    /*
     * Test method to check if neutron LoadBalancerPoolMember has no VM
     * available
     */
    @Test
    public void testCanCreateNeutronLoadBalancerMemberNoVMAvailable() throws IOException {
        Activator.apiConnector = mockApiConnector;
        NeutronLoadBalancerPoolMember neutronLoadBalancerMember = defaultNeutronLoadBalancerMemberObject();
        when(mockApiConnector.findById(Project.class, neutronLoadBalancerMember.getPoolMemberTenantID())).thenReturn(
                mockProject);
        when(mockProject.getVirtualMachineInterfaces()).thenReturn(null);
        assertEquals(HttpURLConnection.HTTP_FORBIDDEN,
                loadBalancerPoolMemberHandler.canCreateNeutronLoadBalancerPoolMember(neutronLoadBalancerMember));
    }

    /* Test method to check if neutron LoadBalancerPool already exist */
    @Test
    public void testCanCreateNeutronLoadBalancerMemberExists() throws IOException {
        Activator.apiConnector = mockApiConnector;
        NeutronLoadBalancerPoolMember neutronLoadBalancerMember = defaultNeutronLoadBalancerMemberObject();
        when(mockApiConnector.findById(Project.class, neutronLoadBalancerMember.getPoolMemberTenantID())).thenReturn(
                mockProject);
        when(mockApiConnector.findById(LoadbalancerMember.class, neutronLoadBalancerMember.getPoolMemberID()))
                .thenReturn(mockLoadbalancerMember);
        assertEquals(HttpURLConnection.HTTP_FORBIDDEN,
                loadBalancerPoolMemberHandler.canCreateNeutronLoadBalancerPoolMember(neutronLoadBalancerMember));
    }

    /* Test method to check if neutron LoadBalancerPool already exist */
    @Test
    public void testCanCreateNeutronLoadBalancerPoolExists() throws IOException {
        Activator.apiConnector = mockApiConnector;
        NeutronLoadBalancerPoolMember neutronLoadBalancerMember = defaultNeutronLoadBalancerMemberObject();
        when(mockApiConnector.findById(Project.class, neutronLoadBalancerMember.getPoolMemberTenantID())).thenReturn(
                mockProject);
        when(mockApiConnector.findById(LoadbalancerMember.class, neutronLoadBalancerMember.getPoolMemberID()))
                .thenReturn(null);
        when(mockApiConnector.findById(LoadbalancerPool.class, neutronLoadBalancerMember.getPoolID())).thenReturn(null);
        assertEquals(HttpURLConnection.HTTP_FORBIDDEN,
                loadBalancerPoolMemberHandler.canCreateNeutronLoadBalancerPoolMember(neutronLoadBalancerMember));
    }

    /* Test method to check if neutron Member and Pool have same TenantID */
    @Test
    public void testCanCreateNeutronLoadBalancerMemberTenantIDConflict() throws IOException {
        Activator.apiConnector = mockApiConnector;
        NeutronLoadBalancerPoolMember neutronLoadBalancerMember = defaultNeutronLoadBalancerMemberObject();
        when(mockApiConnector.findById(Project.class, neutronLoadBalancerMember.getPoolMemberTenantID())).thenReturn(
                mockProject);
        when(mockApiConnector.findById(LoadbalancerMember.class, neutronLoadBalancerMember.getPoolMemberID()))
                .thenReturn(null);
        when(mockApiConnector.findById(LoadbalancerPool.class, neutronLoadBalancerMember.getPoolID())).thenReturn(
                mockLoadbalancerPool);
        when(mockLoadbalancerPool.getParentUuid()).thenReturn("111570f2-17b1-4fc3-99ec-1b7f7778a29a");
        assertEquals(HttpURLConnection.HTTP_FORBIDDEN,
                loadBalancerPoolMemberHandler.canCreateNeutronLoadBalancerPoolMember(neutronLoadBalancerMember));
    }

    /* Test method to check if neutron Member and Pool have same TenantID */
    @Test
    public void testCanCreateNeutronLoadBalancerMemberOk() throws IOException {
        Activator.apiConnector = mockApiConnector;
        NeutronLoadBalancerPoolMember neutronLoadBalancerMember = defaultNeutronLoadBalancerMemberObject();
        when(mockApiConnector.findById(Project.class, neutronLoadBalancerMember.getPoolMemberTenantID())).thenReturn(
                mockProject);
        when(mockApiConnector.findById(LoadbalancerMember.class, neutronLoadBalancerMember.getPoolMemberID()))
                .thenReturn(null);
        when(mockApiConnector.findById(LoadbalancerPool.class, neutronLoadBalancerMember.getPoolID())).thenReturn(
                mockLoadbalancerPool);
        when(mockLoadbalancerPool.getParentUuid()).thenReturn("001570f2-17b1-4fc3-99ec-1b7f7778a29a");
        assertEquals(HttpURLConnection.HTTP_OK,
                loadBalancerPoolMemberHandler.canCreateNeutronLoadBalancerPoolMember(neutronLoadBalancerMember));
    }
    /* Test method to check if neutron LoadBalancerPool member not found */
    @Test
    public void testCanDeleteNeutronLoadBalancerMemberNotFound() throws IOException {
        Activator.apiConnector = mockApiConnector;
        NeutronLoadBalancerPoolMember neutronLoadBalancerMember = defaultNeutronLoadBalancerMemberObject();
        when(mockApiConnector.findById(LoadbalancerPool.class, neutronLoadBalancerMember.getPoolMemberID()))
                .thenReturn(null);
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST,
                loadBalancerPoolMemberHandler.canDeleteNeutronLoadBalancerPoolMember(neutronLoadBalancerMember));
    }

    /* Test method to check if neutron LoadBalancerPool returns status OK */
    @Test
    public void testCanDeleteNeutronLoadBalancerPoolOK() throws IOException {
        Activator.apiConnector = mockApiConnector;
        NeutronLoadBalancerPoolMember neutronLoadBalancerMember = defaultNeutronLoadBalancerMemberObject();
        when(mockApiConnector.findById(LoadbalancerPool.class, neutronLoadBalancerMember.getPoolMemberID()))
                .thenReturn(mockLoadbalancerPool);
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST,
                loadBalancerPoolMemberHandler.canDeleteNeutronLoadBalancerPoolMember(neutronLoadBalancerMember));
    }
}
