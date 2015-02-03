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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.controller.networkconfig.neutron.NeutronLoadBalancerPool;
import org.opendaylight.controller.networkconfig.neutron.NeutronLoadBalancerPoolMember;

/**
 * Test Class for LoadBalancerPoolHandler.
 */
public class LoadBalancerPoolHandlerTest {

    LoadBalancerPoolHandler loadBalancerPoolHandler;
    LoadBalancerPoolHandler mockLoadBalancerPoolHandler = mock(LoadBalancerPoolHandler.class);
    ApiConnector mockApiConnector = mock(ApiConnector.class);
    Project mockProject = mock(Project.class);
    NeutronLoadBalancerPool mockNeutronLoadBalancerPool = mock(NeutronLoadBalancerPool.class);
    LoadbalancerPool mockLoadbalancerPool = mock(LoadbalancerPool.class);
    LoadbalancerMember mockLoadbalancerMember = mock(LoadbalancerMember.class);

    @Before
    public void beforeTest() {
        loadBalancerPoolHandler = new LoadBalancerPoolHandler();
        assertNotNull(mockLoadBalancerPoolHandler);
        assertNotNull(mockApiConnector);
        assertNotNull(mockProject);
        assertNotNull(mockNeutronLoadBalancerPool);
        assertNotNull(mockLoadbalancerPool);
    }

    @After
    public void AfterTest() {
        loadBalancerPoolHandler = null;
        Activator.apiConnector = null;
    }

    /* Dummy parameters for Neutron LoadBalancerPool */
    public NeutronLoadBalancerPool defaultNeutronLoadBalancerPoolObject() {
        NeutronLoadBalancerPool neutronObject = new NeutronLoadBalancerPool();
        neutronObject.setLoadBalancerPoolAdminStateIsUp(false);
        neutronObject.setLoadBalancerPoolDescription("loadBalancerPoolDescription");
        neutronObject.setLoadBalancerPoolID("6b9570f2-17b1-4fc3-99ec-1b7f7778a29a");
        neutronObject.setLoadBalancerPoolLbAlgorithm("ROUND_ROBIN");
        neutronObject.setLoadBalancerPoolName("loadBalancerPoolName");
        neutronObject.setLoadBalancerPoolProtocol("HTTP");
        neutronObject.setLoadBalancerPoolStatus("PENDING_CREATE");
        neutronObject.setLoadBalancerPoolTenantID("779570f2-17b1-4fc3-99ec-1b7f7778a29a");
        List<NeutronLoadBalancerPoolMember> loadBalancerPoolMembers = new ArrayList<NeutronLoadBalancerPoolMember>();
        neutronObject.setLoadBalancerPoolMembers(loadBalancerPoolMembers);
        neutronObject.setNeutronLoadBalancerPoolHealthMonitorID("889570f2-17b1-4fc3-99ec-1b7f7778a29a");
        return neutronObject;
    }

    /* Dummy parameters for Neutron LoadBalancerPool update */
    public NeutronLoadBalancerPool defaultUpdateNeutronLoadBalancerPoolObject() {
        NeutronLoadBalancerPool neutronUpdateObject = new NeutronLoadBalancerPool();
        neutronUpdateObject.setLoadBalancerPoolAdminStateIsUp(false);
        neutronUpdateObject.setLoadBalancerPoolDescription("loadBalancerPoolDescription");
        neutronUpdateObject.setLoadBalancerPoolID("6b9570f2-17b1-4fc3-99ec-1b7f7778a29a");
        neutronUpdateObject.setLoadBalancerPoolLbAlgorithm("ROUND_ROBIN");
        neutronUpdateObject.setLoadBalancerPoolName("loadBalancerPoolName");
        neutronUpdateObject.setLoadBalancerPoolProtocol("HTTP");
        neutronUpdateObject.setLoadBalancerPoolStatus("PENDING_CREATE");
        neutronUpdateObject.setLoadBalancerPoolTenantID("779570f2-17b1-4fc3-99ec-1b7f7778a29a");
        neutronUpdateObject.setNeutronLoadBalancerPoolHealthMonitorID("889570f2-17b1-4fc3-99ec-1b7f7778a29a");
        return neutronUpdateObject;
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
        neutronObject.setPoolMemberTenantID("779570f2-17b1-4fc3-99ec-1b7f7778a29a");
        neutronObject.setPoolMemberWeight(20);
        return neutronObject;
    }

    /* Test method to check if neutron LoadBalancerPool is null */
    @Test
    public void testCanCreateNeutronLoadBalancerPoolNull() {
        Activator.apiConnector = mockApiConnector;
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, loadBalancerPoolHandler.canCreateNeutronLoadBalancerPool(null));
    }

    /* Test method to check if neutron LoadBalancerPool tenant ID is null */
    @Test
    public void testCanCreateNeutronLoadBalancerPoolTenantIDNull() {
        Activator.apiConnector = mockApiConnector;
        NeutronLoadBalancerPool neutronObject = new NeutronLoadBalancerPool();
        neutronObject.setLoadBalancerPoolTenantID(null);
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST,
                loadBalancerPoolHandler.canCreateNeutronLoadBalancerPool(neutronObject));
    }

    /* Test method to check if neutron LoadBalancerPool alogorithm is null */
    @Test
    public void testCanCreateNeutronLoadBalancerPoolAlgorithmNull() {
        Activator.apiConnector = mockApiConnector;
        NeutronLoadBalancerPool neutronObject = defaultNeutronLoadBalancerPoolObject();
        neutronObject.setLoadBalancerPoolLbAlgorithm(null);
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST,
                loadBalancerPoolHandler.canCreateNeutronLoadBalancerPool(neutronObject));
    }

    /* Test method to check if neutron LoadBalancerPool algorithm is invalid */
    @Test
    public void testCanCreateNeutronLoadBalancerPoolAlgorithmInvalid() {
        Activator.apiConnector = mockApiConnector;
        NeutronLoadBalancerPool neutronObject = defaultNeutronLoadBalancerPoolObject();
        neutronObject.setLoadBalancerPoolLbAlgorithm("xyz");
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST,
                loadBalancerPoolHandler.canCreateNeutronLoadBalancerPool(neutronObject));
    }

    /* Test method to check if neutron LoadBalancerPool protocol is null */
    @Test
    public void testCanCreateNeutronLoadBalancerPoolProtocolNull() {
        Activator.apiConnector = mockApiConnector;
        NeutronLoadBalancerPool neutronObject = defaultNeutronLoadBalancerPoolObject();
        neutronObject.setLoadBalancerPoolProtocol(null);
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST,
                loadBalancerPoolHandler.canCreateNeutronLoadBalancerPool(neutronObject));
    }

    /* Test method to check if neutron LoadBalancerPool protocol is invalid */
    @Test
    public void testCanCreateNeutronLoadBalancerPoolProtocolInvalid() {
        Activator.apiConnector = mockApiConnector;
        NeutronLoadBalancerPool neutronObject = defaultNeutronLoadBalancerPoolObject();
        neutronObject.setLoadBalancerPoolProtocol("xyz");
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST,
                loadBalancerPoolHandler.canCreateNeutronLoadBalancerPool(neutronObject));
    }

    /*
     * Test method to check if neutron LoadBalancerPool and Member belong to
     * same tenant
     */
    @Test
    public void testCanCreateNeutronLoadBalancerPoolMemberTenantIdCheck() {
        Activator.apiConnector = mockApiConnector;
        NeutronLoadBalancerPool neutronPoolObject = defaultNeutronLoadBalancerPoolObject();
        NeutronLoadBalancerPoolMember neutronMemberObject = defaultNeutronLoadBalancerMemberObject();
        neutronPoolObject.addLoadBalancerPoolMember(neutronMemberObject);
        neutronPoolObject.setLoadBalancerPoolTenantID("779570f2-17b1-4fc3-99ec-1b7f7778a29a");
        neutronMemberObject.setPoolMemberTenantID("009570f2-17b1-4fc3-99ec-1b7f7778a29a");
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST,
                loadBalancerPoolHandler.canCreateNeutronLoadBalancerPool(neutronPoolObject));
    }

    /*
     * Test method to check if neutron LoadBalancerPool Member does not already
     * exist
     */
    @Test
    public void testCanCreateNeutronLoadBalancerPoolMemberExist() throws IOException {
        Activator.apiConnector = mockApiConnector;
        NeutronLoadBalancerPool neutronPoolObject = defaultNeutronLoadBalancerPoolObject();
        NeutronLoadBalancerPoolMember neutronMemberObject = defaultNeutronLoadBalancerMemberObject();
        neutronPoolObject.addLoadBalancerPoolMember(neutronMemberObject);
        when(mockApiConnector.findById(LoadbalancerMember.class, neutronMemberObject.getPoolMemberID())).thenReturn(
                mockLoadbalancerMember);
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST,
                loadBalancerPoolHandler.canCreateNeutronLoadBalancerPool(neutronPoolObject));
    }

    /* Test method to check if neutron LoadBalancerPool project exist */
    @Test
    public void testCanCreateNeutronLoadBalancerPoolProjectNull() throws IOException {
        Activator.apiConnector = mockApiConnector;
        NeutronLoadBalancerPool neutronLoadBalancerPool = defaultNeutronLoadBalancerPoolObject();
        when(mockApiConnector.findById(Project.class, neutronLoadBalancerPool.getLoadBalancerPoolTenantID()))
                .thenReturn(null);
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND,
                loadBalancerPoolHandler.canCreateNeutronLoadBalancerPool(neutronLoadBalancerPool));
    }

    /* Test method to check if neutron LoadBalancerPool already exist */
    @Test
    public void testCanCreateNeutronLoadBalancerPoolExistsByID() throws IOException {
        Activator.apiConnector = mockApiConnector;
        NeutronLoadBalancerPool neutronLoadBalancerPool = defaultNeutronLoadBalancerPoolObject();
        when(mockApiConnector.findById(Project.class, neutronLoadBalancerPool.getLoadBalancerPoolTenantID()))
                .thenReturn(mockProject);
        when(mockApiConnector.findById(LoadbalancerPool.class, neutronLoadBalancerPool.getLoadBalancerPoolID()))
                .thenReturn(mockLoadbalancerPool);
        assertEquals(HttpURLConnection.HTTP_FORBIDDEN,
                loadBalancerPoolHandler.canCreateNeutronLoadBalancerPool(neutronLoadBalancerPool));
    }

    /* Test method to check if neutron LoadBalancerPool already with same name */
    @Test
    public void testCanCreateNeutronLoadBalancerPoolExistsByName() throws IOException {
        Activator.apiConnector = mockApiConnector;
        NeutronLoadBalancerPool neutronLoadBalancerPool = defaultNeutronLoadBalancerPoolObject();
        when(mockApiConnector.findById(Project.class, neutronLoadBalancerPool.getLoadBalancerPoolTenantID()))
                .thenReturn(mockProject);
        when(mockApiConnector.findById(LoadbalancerPool.class, neutronLoadBalancerPool.getLoadBalancerPoolID()))
                .thenReturn(null);
        when(
                mockApiConnector.findByName(LoadbalancerPool.class, mockProject,
                        neutronLoadBalancerPool.getLoadBalancerPoolName())).thenReturn("name");
        assertEquals(HttpURLConnection.HTTP_FORBIDDEN,
                loadBalancerPoolHandler.canCreateNeutronLoadBalancerPool(neutronLoadBalancerPool));
    }

    /* Test method to check if neutron LoadBalancerPool returns status OK */
    @Test
    public void testCanCreateNeutronLoadBalancerPoolOK() throws IOException {
        Activator.apiConnector = mockApiConnector;
        NeutronLoadBalancerPool neutronLoadBalancerPool = defaultNeutronLoadBalancerPoolObject();
        when(mockApiConnector.findById(Project.class, neutronLoadBalancerPool.getLoadBalancerPoolTenantID()))
                .thenReturn(mockProject);
        when(mockApiConnector.findById(LoadbalancerPool.class, neutronLoadBalancerPool.getLoadBalancerPoolID()))
                .thenReturn(null);
        assertEquals(HttpURLConnection.HTTP_OK,
                loadBalancerPoolHandler.canCreateNeutronLoadBalancerPool(neutronLoadBalancerPool));
    }

    /* Test method to check if neutron LoadBalancerPool is null */
    @Test
    public void testCanUpdateNeutronLoadBalancerPoolNull() {
        Activator.apiConnector = mockApiConnector;
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST,
                loadBalancerPoolHandler.canUpdateNeutronLoadBalancerPool(null, null));
    }

    /* Test method to check if neutron LoadBalancerPool does not exist */
    @Test
    public void testCanUpdateNeutronLoadBalancerPoolNotFound() throws IOException {
        Activator.apiConnector = mockApiConnector;
        NeutronLoadBalancerPool neutronLoadBalancerPool = defaultNeutronLoadBalancerPoolObject();
        NeutronLoadBalancerPool neutronUpdateLoadBalancerPool = defaultUpdateNeutronLoadBalancerPoolObject();
        when(mockApiConnector.findById(LoadbalancerPool.class, neutronLoadBalancerPool.getLoadBalancerPoolID()))
                .thenReturn(null);
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, loadBalancerPoolHandler.canUpdateNeutronLoadBalancerPool(
                neutronUpdateLoadBalancerPool, neutronLoadBalancerPool));
    }

    /* Test method to check if neutron LoadBalancerPool returns status ok */
    @Test
    public void testCanUpdateNeutronLoadBalancerPoolOK() throws IOException {
        Activator.apiConnector = mockApiConnector;
        NeutronLoadBalancerPool neutronLoadBalancerPool = defaultNeutronLoadBalancerPoolObject();
        NeutronLoadBalancerPool neutronUpdateLoadBalancerPool = defaultUpdateNeutronLoadBalancerPoolObject();
        when(mockApiConnector.findById(LoadbalancerPool.class, neutronLoadBalancerPool.getLoadBalancerPoolID()))
                .thenReturn(mockLoadbalancerPool);
        assertEquals(HttpURLConnection.HTTP_OK, loadBalancerPoolHandler.canUpdateNeutronLoadBalancerPool(
                neutronUpdateLoadBalancerPool, neutronLoadBalancerPool));
    }

    /* Test method to check if neutron LoadBalancerPool does not exists */
    @Test
    public void testCanDeleteNeutronLoadBalancerPoolNotFound() throws IOException {
        Activator.apiConnector = mockApiConnector;
        NeutronLoadBalancerPool neutronLoadBalancerPool = defaultNeutronLoadBalancerPoolObject();
        when(mockApiConnector.findById(LoadbalancerPool.class, neutronLoadBalancerPool.getLoadBalancerPoolID()))
                .thenReturn(null);
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST,
                loadBalancerPoolHandler.canDeleteNeutronLoadBalancerPool(neutronLoadBalancerPool));
    }

    /* Test method to check if neutron LoadBalancerPool returns status OK */
    @Test
    public void testCanDeleteNeutronLoadBalancerPoolOK() throws IOException {
        Activator.apiConnector = mockApiConnector;
        NeutronLoadBalancerPool neutronLoadBalancerPool = defaultNeutronLoadBalancerPoolObject();
        when(mockApiConnector.findById(LoadbalancerPool.class, neutronLoadBalancerPool.getLoadBalancerPoolID()))
                .thenReturn(mockLoadbalancerPool);
        assertEquals(HttpURLConnection.HTTP_OK,
                loadBalancerPoolHandler.canDeleteNeutronLoadBalancerPool(neutronLoadBalancerPool));
    }
}
