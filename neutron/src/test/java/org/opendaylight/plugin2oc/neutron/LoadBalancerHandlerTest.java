package org.opendaylight.plugin2oc.neutron;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.HttpURLConnection;

import net.juniper.contrail.api.ApiConnector;
import net.juniper.contrail.api.types.Project;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.controller.networkconfig.neutron.NeutronLoadBalancer;

/**
 * Test Class for LoadBalancerHandler.
 */
public class LoadBalancerHandlerTest {

    LoadBalancerHandler loadBalancerHandler;
    LoadBalancerHandler mockLoadBalancerHandler = mock(LoadBalancerHandler.class);
    ApiConnector mockApiConnector = mock(ApiConnector.class);
    Project mockProject = mock(Project.class);
    NeutronLoadBalancer mockNeutronLoadBalancer = mock(NeutronLoadBalancer.class);

    @Before
    public void beforeTest() {
        loadBalancerHandler = new LoadBalancerHandler();
        assertNotNull(mockLoadBalancerHandler);
        assertNotNull(mockApiConnector);
        assertNotNull(mockProject);
        assertNotNull(mockNeutronLoadBalancer);
    }

    @After
    public void AfterTest() {
        loadBalancerHandler = null;
        Activator.apiConnector = null;
    }

    /* Dummy parameters for Neutron LoadBalancer object */
    public NeutronLoadBalancer defaultNeutronLoadBalancerObject() {
        NeutronLoadBalancer neutronObject = new NeutronLoadBalancer();
        neutronObject.setLoadBalancerDescription("loadBalancerDescription");
        neutronObject.setLoadBalancerID("6b9570f2-17b1-4fc3-99ec-1b7f7778a29a");
        neutronObject.setLoadBalancerName("loadBalancerName");
        neutronObject.setLoadBalancerStatus(null);
        neutronObject.setLoadBalancerTenantID("779570f2-17b1-4fc3-99ec-1b7f7778a29a");
        neutronObject.setLoadBalancerVipAddress("10.10.10.10");
        neutronObject.setLoadBalancerVipSubnetID("889570f2-17b1-4fc3-99ec-1b7f7778a29a");
        return neutronObject;
    }

    /* Dummy parameters for Neutron LoadBalancer update */
    public NeutronLoadBalancer defaultUpdateNeutronLoadBalancerObject() {
        NeutronLoadBalancer neutronUpdateObject = new NeutronLoadBalancer();
        neutronUpdateObject.setLoadBalancerDescription("loadBalancerDescription");
        neutronUpdateObject.setLoadBalancerID("6b9570f2-17b1-4fc3-99ec-1b7f7778a29a");
        neutronUpdateObject.setLoadBalancerName("loadBalancerName");
        neutronUpdateObject.setLoadBalancerStatus(null);
        neutronUpdateObject.setLoadBalancerTenantID("779570f2-17b1-4fc3-99ec-1b7f7778a29a");
        neutronUpdateObject.setLoadBalancerVipAddress("10.10.10.10");
        neutronUpdateObject.setLoadBalancerVipSubnetID("889570f2-17b1-4fc3-99ec-1b7f7778a29a");
        return neutronUpdateObject;
    }
    /* Test method to check if neutron LoadBalancer is null */
    @Test
    public void testCanCreateNeutronLoadBalancerNull() {
        Activator.apiConnector = mockApiConnector;
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, loadBalancerHandler.canCreateNeutronLoadBalancer(null));
    }

    /* Test method to check if neutron LoadBalancer tenant ID is null */
    @Test
    public void testCanCreateNeutronLoadBalancerTenantIDNull() {
        Activator.apiConnector = mockApiConnector;
        NeutronLoadBalancer neutronObject = new NeutronLoadBalancer();
        neutronObject.setLoadBalancerTenantID(null);
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST,
                loadBalancerHandler.canCreateNeutronLoadBalancer(neutronObject));
    }

    /* Test method to check if neutron LoadBalancer project exist */
    @Test
    public void testCanCreateNeutronLoadBalancerPoolProjectNull() throws IOException {
        Activator.apiConnector = mockApiConnector;
        NeutronLoadBalancer neutronLoadBalancer = defaultNeutronLoadBalancerObject();
        when(mockApiConnector.findById(Project.class, neutronLoadBalancer.getLoadBalancerTenantID()))
                .thenReturn(null);
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND,
                loadBalancerHandler.canCreateNeutronLoadBalancer(neutronLoadBalancer));
        }

}
