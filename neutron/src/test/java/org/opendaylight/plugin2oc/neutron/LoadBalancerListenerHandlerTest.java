package org.opendaylight.plugin2oc.neutron;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.HttpURLConnection;

import net.juniper.contrail.api.ApiConnector;
import net.juniper.contrail.api.types.LoadbalancerPool;
import net.juniper.contrail.api.types.Project;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.controller.networkconfig.neutron.NeutronLoadBalancerListener;

/**
 * Test Class for LoadBalancerListenerHandler.
 */

public class LoadBalancerListenerHandlerTest {

    LoadBalancerListenerHandler loadBalancerListenerHandler;
    LoadBalancerListenerHandler mockLoadBalancerListenerHandler = mock(LoadBalancerListenerHandler.class);
    ApiConnector mockApiConnector = mock(ApiConnector.class);
    Project mockProject = mock(Project.class);
    NeutronLoadBalancerListener mockNeutronLoadBalancerListener = mock(NeutronLoadBalancerListener.class);
    LoadbalancerPool mockLoadbalancerPool = mock(LoadbalancerPool.class);
    // LoadbalancerListener mockLoadbalancerListener = mock(LoadbalancerPool.class);

    @Before
    public void beforeTest() {
        loadBalancerListenerHandler = new LoadBalancerListenerHandler();
        assertNotNull(mockLoadBalancerListenerHandler);
        assertNotNull(mockApiConnector);
        assertNotNull(mockProject);
        assertNotNull(mockNeutronLoadBalancerListener);
        // assertNotNull(mockLoadbalancerListener);
    }

    @After
    public void AfterTest() {
        loadBalancerListenerHandler = null;
        Activator.apiConnector = null;
    }

    /* Dummy parameters for Neutron LoadBalancerListener */
    public NeutronLoadBalancerListener defaultNeutronLoadBalancerListenerObject() {
        NeutronLoadBalancerListener neutronObject = new NeutronLoadBalancerListener();
        neutronObject.setLoadBalancerListenerAdminStateIsUp(true);
        neutronObject.setLoadBalancerListenerDescription("loadBalancerListenerDescription");
        neutronObject.setLoadBalancerListenerID("6b9570f2-17b1-4fc3-99ec-1b7f7778a29a");
        neutronObject.setLoadBalancerListenerIsShared(true);
        neutronObject.setLoadBalancerListenerName("loadBalancerListenerName");
        neutronObject.setLoadBalancerListenerStatus("status");
        neutronObject.setLoadBalancerListenerTenantID("000570f2-17b1-4fc3-99ec-1b7f7778a29a");
        neutronObject.setNeutronLoadBalancerListenerDefaultPoolID("001570f2-17b1-4fc3-99ec-1b7f7778a29a");
        neutronObject.setNeutronLoadBalancerListenerLoadBalancerID("002570f2-17b1-4fc3-99ec-1b7f7778a29a");
        neutronObject.setNeutronLoadBalancerListenerProtocol("HTTP");
        neutronObject.setNeutronLoadBalancerListenerProtocolPort("8282");
        return neutronObject;
    }

    /* Dummy parameters for Neutron LoadBalancerListener update */
    public NeutronLoadBalancerListener defaultNeutronLoadBalancerListenerUpdateObject() {
        NeutronLoadBalancerListener neutronObject = new NeutronLoadBalancerListener();
        neutronObject.setLoadBalancerListenerAdminStateIsUp(true);
        neutronObject.setLoadBalancerListenerDescription("loadBalancerListenerDescription");
        neutronObject.setLoadBalancerListenerID("6b9570f2-17b1-4fc3-99ec-1b7f7778a29a");
        neutronObject.setLoadBalancerListenerIsShared(true);
        neutronObject.setLoadBalancerListenerName("loadBalancerListenerName");
        neutronObject.setLoadBalancerListenerStatus("status");
        neutronObject.setLoadBalancerListenerTenantID("000570f2-17b1-4fc3-99ec-1b7f7778a29a");
        neutronObject.setNeutronLoadBalancerListenerDefaultPoolID("001570f2-17b1-4fc3-99ec-1b7f7778a29a");
        neutronObject.setNeutronLoadBalancerListenerLoadBalancerID("002570f2-17b1-4fc3-99ec-1b7f7778a29a");
        neutronObject.setNeutronLoadBalancerListenerProtocol("HTTP");
        neutronObject.setNeutronLoadBalancerListenerProtocolPort("8282");
        return neutronObject;
    }

    /* Test method to check if neutron LoadBalancerListener is null */
    @Test
    public void testCanCreateNeutronLoadBalancerPoolNull() {
        Activator.apiConnector = mockApiConnector;
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST,
                loadBalancerListenerHandler.canCreateNeutronLoadBalancerListener(null));
    }

    /* Test method to check if neutron LoadBalancerListener tenant ID is null */
    @Test
    public void testCanCreateNeutronLoadBalancerPoolTenantIDNull() {
        Activator.apiConnector = mockApiConnector;
        NeutronLoadBalancerListener neutronObject = defaultNeutronLoadBalancerListenerObject();
        neutronObject.setLoadBalancerListenerTenantID(null);
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST,
                loadBalancerListenerHandler.canCreateNeutronLoadBalancerListener(neutronObject));
    }

    /* Test method to check if neutron LoadBalancerListener pool ID is null */
    @Test
    public void testCanCreateNeutronLoadBalancerPoolIDNull() {
        Activator.apiConnector = mockApiConnector;
        NeutronLoadBalancerListener neutronObject = defaultNeutronLoadBalancerListenerObject();
        neutronObject.setNeutronLoadBalancerListenerDefaultPoolID(null);
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST,
                loadBalancerListenerHandler.canCreateNeutronLoadBalancerListener(neutronObject));
    }

    /* Test method to check if neutron LoadBalance ID is null */
    @Test
    public void testCanCreateNeutronLoadBalancerListenerLBIDNull() {
        Activator.apiConnector = mockApiConnector;
        NeutronLoadBalancerListener neutronObject = defaultNeutronLoadBalancerListenerObject();
        neutronObject.setNeutronLoadBalancerListenerLoadBalancerID(null);
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST,
                loadBalancerListenerHandler.canCreateNeutronLoadBalancerListener(neutronObject));
    }

    /* Test method to check if neutron LoadBalanceListener protocol is null */
    @Test
    public void testCanCreateNeutronLoadBalancerListenerProtocolNull() {
        Activator.apiConnector = mockApiConnector;
        NeutronLoadBalancerListener neutronObject = defaultNeutronLoadBalancerListenerObject();
        neutronObject.setNeutronLoadBalancerListenerProtocol(null);
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST,
                loadBalancerListenerHandler.canCreateNeutronLoadBalancerListener(neutronObject));
    }

    /* Test method to check if neutron LoadBalancerPool protocol is invalid */
    @Test
    public void testCanCreateNeutronLoadBalancerListenerProtocolInvalid() {
        Activator.apiConnector = mockApiConnector;
        NeutronLoadBalancerListener neutronObject = defaultNeutronLoadBalancerListenerObject();
        neutronObject.setNeutronLoadBalancerListenerProtocol("xyz");
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST,
                loadBalancerListenerHandler.canCreateNeutronLoadBalancerListener(neutronObject));
    }

    /* Test method to check if neutron LoadBalancerPool project exist */
    @Test
    public void testCanCreateNeutronLoadBalancerListenerProjectNull() throws IOException {
        Activator.apiConnector = mockApiConnector;
        NeutronLoadBalancerListener neutronLoadBalancerListener = defaultNeutronLoadBalancerListenerObject();
        when(mockApiConnector.findById(Project.class, neutronLoadBalancerListener.getLoadBalancerListenerTenantID()))
                .thenReturn(null);
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND,
                loadBalancerListenerHandler.canCreateNeutronLoadBalancerListener(neutronLoadBalancerListener));
    }

    /* Test method to check if neutron default Pool already exist */
    @Test
    public void testCanCreateNeutronLoadBalancerListenerDefaultPoolExists() throws IOException {
        Activator.apiConnector = mockApiConnector;
        NeutronLoadBalancerListener neutronLoadBalancerListener = defaultNeutronLoadBalancerListenerObject();
        when(mockApiConnector.findById(Project.class, neutronLoadBalancerListener.getLoadBalancerListenerTenantID()))
                .thenReturn(mockProject);
        when(
                mockApiConnector.findById(LoadbalancerPool.class,
                        neutronLoadBalancerListener.getNeutronLoadBalancerListenerDefaultPoolID())).thenReturn(null);
        assertEquals(HttpURLConnection.HTTP_FORBIDDEN,
                loadBalancerListenerHandler.canCreateNeutronLoadBalancerListener(neutronLoadBalancerListener));
    }

}
