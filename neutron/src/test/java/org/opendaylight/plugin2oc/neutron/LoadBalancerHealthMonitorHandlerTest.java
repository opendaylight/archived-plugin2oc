package org.opendaylight.plugin2oc.neutron;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.HttpURLConnection;

import net.juniper.contrail.api.ApiConnector;
import net.juniper.contrail.api.types.LoadbalancerHealthmonitor;
import net.juniper.contrail.api.types.Project;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.controller.networkconfig.neutron.NeutronLoadBalancerHealthMonitor;

/**
 * Test Class for LoadBalancerHealthMonitorHandler.
 */
public class LoadBalancerHealthMonitorHandlerTest {

    LoadBalancerHealthMonitorHandler loadBalancerHealthMonitorHandler;
    LoadBalancerHealthMonitorHandler mockLoadBalancerHealthMonitorHandler = mock(LoadBalancerHealthMonitorHandler.class);
    NeutronLoadBalancerHealthMonitor mockNeutronLoadBalancerHealthMonitor = mock(NeutronLoadBalancerHealthMonitor.class);
    LoadbalancerHealthmonitor mockLoadbalancerHealthmonitor = mock(LoadbalancerHealthmonitor.class);
    ApiConnector mockApiConnector = mock(ApiConnector.class);
    Project mockProject = mock(Project.class);

    @Before
    public void beforeTest() {
        loadBalancerHealthMonitorHandler = new LoadBalancerHealthMonitorHandler();
        assertNotNull(mockLoadBalancerHealthMonitorHandler);
        assertNotNull(mockNeutronLoadBalancerHealthMonitor);
        assertNotNull(mockApiConnector);
        assertNotNull(mockProject);
        assertNotNull(mockLoadbalancerHealthmonitor);
    }

    @After
    public void AfterTest() {
        loadBalancerHealthMonitorHandler = null;
        Activator.apiConnector = null;
    }

    /* Dummy parameters for Neutron LoadBalancer HealthMonitor object */
    public NeutronLoadBalancerHealthMonitor defaultNeutronLoadBalancerHealthMonitorObject() {
        NeutronLoadBalancerHealthMonitor neutronObject = new NeutronLoadBalancerHealthMonitor();
        neutronObject.setLoadBalancerHealthMonitorAdminStateIsUp(null);
        neutronObject.setLoadBalancerHealthMonitorDelay(30);
        neutronObject.setLoadBalancerHealthMonitorExpectedCodes("loadBalancerHealthMonitorExpectedCodes");
        neutronObject.setLoadBalancerHealthMonitorHttpMethod("HTTP");
        neutronObject.setLoadBalancerHealthMonitorID("6b9570f2-17b1-4fc3-99ec-1b7f7778a29a");
        neutronObject.setLoadBalancerHealthMonitorMaxRetries(10);
        neutronObject.setLoadBalancerHealthMonitorStatus("loadBalancerHealthMonitorStatus");
        neutronObject.setLoadBalancerHealthMonitorTenantID("779570f2-17b1-4fc3-99ec-1b7f7778a29a");
        neutronObject.setLoadBalancerHealthMonitorTimeout(5);
        neutronObject.setLoadBalancerHealthMonitorType("loadBalancerHealthMonitorType");
        neutronObject.setLoadBalancerHealthMonitorUrlPath("/check");
        return neutronObject;
    }

    /* Dummy parameters for Neutron LoadBalancer update */
    public NeutronLoadBalancerHealthMonitor defaultUpdateNeutronLoadBalancerHealthMonitorObject() {
        NeutronLoadBalancerHealthMonitor neutronUpdateObject = new NeutronLoadBalancerHealthMonitor();
        neutronUpdateObject.setLoadBalancerHealthMonitorAdminStateIsUp(null);
        neutronUpdateObject.setLoadBalancerHealthMonitorDelay(30);
        neutronUpdateObject.setLoadBalancerHealthMonitorExpectedCodes("loadBalancerHealthMonitorExpectedCodes");
        neutronUpdateObject.setLoadBalancerHealthMonitorHttpMethod("HTTP");
        neutronUpdateObject.setLoadBalancerHealthMonitorID("6b9570f2-17b1-4fc3-99ec-1b7f7778a29a");
        neutronUpdateObject.setLoadBalancerHealthMonitorMaxRetries(10);
        neutronUpdateObject.setLoadBalancerHealthMonitorStatus("loadBalancerHealthMonitorStatus");
        neutronUpdateObject.setLoadBalancerHealthMonitorTenantID("779570f2-17b1-4fc3-99ec-1b7f7778a29a");
        neutronUpdateObject.setLoadBalancerHealthMonitorTimeout(5);
        neutronUpdateObject.setLoadBalancerHealthMonitorType("loadBalancerHealthMonitorType");
        neutronUpdateObject.setLoadBalancerHealthMonitorUrlPath("/check");
        return neutronUpdateObject;
    }

    /* Test method to check if neutron LoadBalancerHealthMonitor is null */
    @Test
    public void testCanCreateNeutronLoadBalancerNull() {
        Activator.apiConnector = mockApiConnector;
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST,
                loadBalancerHealthMonitorHandler.canCreateNeutronLoadBalancerHealthMonitor(null));
    }

    /*
     * Test method to check if neutron LoadBalancer health monitor tenant ID is
     * null
     */
    @Test
    public void testCanCreateNeutronLoadBalancerHMTenantIDNull() {
        Activator.apiConnector = mockApiConnector;
        NeutronLoadBalancerHealthMonitor neutronObject = new NeutronLoadBalancerHealthMonitor();
        neutronObject.setLoadBalancerHealthMonitorTenantID(null);
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST,
                loadBalancerHealthMonitorHandler.canCreateNeutronLoadBalancerHealthMonitor(neutronObject));
    }

    /* Test method to check if neutron LoadBalancer health monitor project exist */
    @Test
    public void testCanCreateNeutronLoadBalancerProjectNull() throws IOException {
        Activator.apiConnector = mockApiConnector;
        NeutronLoadBalancerHealthMonitor neutronObject = defaultNeutronLoadBalancerHealthMonitorObject();
        when(mockApiConnector.findById(Project.class, neutronObject.getLoadBalancerHealthMonitorTenantID()))
                .thenReturn(null);
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND,
                loadBalancerHealthMonitorHandler.canCreateNeutronLoadBalancerHealthMonitor(neutronObject));
    }

    /* Test method to check if neutron LoadBalancer health monitor exist */
    @Test
    public void testCanCreateNeutronLoadBalancerHMExist() throws IOException {
        Activator.apiConnector = mockApiConnector;
        NeutronLoadBalancerHealthMonitor neutronObject = defaultNeutronLoadBalancerHealthMonitorObject();
        when(mockApiConnector.findById(Project.class, neutronObject.getLoadBalancerHealthMonitorTenantID()))
                .thenReturn(mockProject);
        when(mockApiConnector.findById(LoadbalancerHealthmonitor.class, neutronObject.getLoadBalancerHealthMonitorID()))
                .thenReturn(mockLoadbalancerHealthmonitor);
        assertEquals(HttpURLConnection.HTTP_FORBIDDEN,
                loadBalancerHealthMonitorHandler.canCreateNeutronLoadBalancerHealthMonitor(neutronObject));
        }

    /* Test method to check if neutron LoadBalancer health monitor returns Ok */
    @Test
    public void testCanCreateNeutronLoadBalancerHMOk() throws IOException {
        Activator.apiConnector = mockApiConnector;
        NeutronLoadBalancerHealthMonitor neutronObject = defaultNeutronLoadBalancerHealthMonitorObject();
        when(mockApiConnector.findById(Project.class, neutronObject.getLoadBalancerHealthMonitorTenantID()))
                .thenReturn(mockProject);
        when(mockApiConnector.findById(LoadbalancerHealthmonitor.class, neutronObject.getLoadBalancerHealthMonitorID()))
                .thenReturn(null);
        assertEquals(HttpURLConnection.HTTP_OK,
                loadBalancerHealthMonitorHandler.canCreateNeutronLoadBalancerHealthMonitor(neutronObject));
    }
}
