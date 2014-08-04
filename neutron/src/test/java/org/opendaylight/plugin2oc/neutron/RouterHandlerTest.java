package org.opendaylight.plugin2oc.neutron;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.easymock.PowerMock.expectNew;

import java.io.IOException;
import java.net.HttpURLConnection;

import net.juniper.contrail.api.ApiConnector;
import net.juniper.contrail.api.types.LogicalRouter;
import net.juniper.contrail.api.types.Project;
import net.juniper.contrail.api.types.VirtualMachine;
import net.juniper.contrail.api.types.VirtualMachineInterface;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opendaylight.controller.networkconfig.neutron.NeutronRouter;
import org.opendaylight.controller.networkconfig.neutron.NeutronRouter_Interface;
import org.opendaylight.controller.networkconfig.neutron.NeutronRouter_NetworkReference;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ RouterHandler.class, LogicalRouter.class })
public class RouterHandlerTest {

    RouterHandler routerHandler;
    RouterHandler mockedRouterHandler = mock(RouterHandler.class);
    NeutronRouter mockedNeutronRouter = mock(NeutronRouter.class);
    LogicalRouter mockedLogicalRouter = mock(LogicalRouter.class);
    ApiConnector mockedApiConnector = mock(ApiConnector.class);
    Project mockedProject = mock(Project.class);
    NeutronRouter_Interface mockNeutronRouter_Interface = mock(NeutronRouter_Interface.class);
    VirtualMachineInterface mockVirtualMachineInterface = mock(VirtualMachineInterface.class);

    @Before
    public void beforeTest() {
        routerHandler = new RouterHandler();
        assertNotNull(mockedRouterHandler);
        assertNotNull(mockedApiConnector);
        assertNotNull(mockedNeutronRouter);
        assertNotNull(mockedLogicalRouter);
        assertNotNull(mockedProject);
        assertNotNull(mockNeutronRouter_Interface);
        assertNotNull(mockVirtualMachineInterface);
    }

    @After
    public void AfterTest() {
        routerHandler = null;
        Activator.apiConnector = null;
    }

    /* dummy params for Neutron Router */

    public NeutronRouter defaultNeutronObject() {
        NeutronRouter neutron = new NeutronRouter();
        NeutronRouter_NetworkReference neuRouter_NetworkReference = new NeutronRouter_NetworkReference();
        neuRouter_NetworkReference.setNetworkID("009570f2-17b1-4fc3-99ec-1b7f7778a29b");
        neutron.setAdminStateUp(true);
        neutron.setName("Router-01");
        neutron.setRouterUUID("009570f2-17b1-4fc3-99ec-1b7f7778a29a");
        neutron.setTenantID("119570f2-17b1-4fc3-99ec-1b7f7778a29a");
        neutron.setStatus("ACTIVE");
        neutron.setExternalGatewayInfo(neuRouter_NetworkReference);
        return neutron;
    }

    /* dummy params for Neutron Router update */

    public NeutronRouter deltaNeutronObject() {
        NeutronRouter neutron = new NeutronRouter();
        neutron.setAdminStateUp(true);
        neutron.setName("Router-02");
        neutron.setRouterUUID("009570f2-17b1-4fc3-99ec-1b7f7778a29a");
        neutron.setTenantID("119570f2-17b1-4fc3-99ec-1b7f7778a29a");
        neutron.setStatus("ACTIVE");
        neutron.setExternalGatewayInfo(null);
        return neutron;
    }

    /* dummy params for NeutronRouter_Interface */

    public NeutronRouter_Interface deltaNeutronRouter_Interface() {
        NeutronRouter_Interface neutronRouterInterface = new NeutronRouter_Interface();
        neutronRouterInterface.setID("009570f2-17b1-4fc3-99ec-1b7f7778a29a");
        neutronRouterInterface.setPortUUID("119570f2-17b1-4fc3-99ec-1b7f7778a29a");
        neutronRouterInterface.setSubnetUUID("229570f2-17b1-4fc3-99ec-1b7f7778a29a");
        neutronRouterInterface.setTenantID("339570f2-17b1-4fc3-99ec-1b7f7778a29a");
        return neutronRouterInterface;
    }

    /* Test method to check if neutron router object is null */
    @Test
    public void testCanCreateRouterNull() {
        Activator.apiConnector = mockedApiConnector;
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, routerHandler.canCreateRouter(null));
    }

    /* Test method to check if neutron router UUID is null */
    @Test
    public void testCanCreateRouterNullUUID() {
        Activator.apiConnector = mockedApiConnector;
        when(mockedNeutronRouter.getRouterUUID()).thenReturn(null);
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, routerHandler.canCreateRouter(mockedNeutronRouter));
    }

    /* Test method to check if neutron router name is null */
    @Test
    public void testCanCreateRouterNullName() {
        Activator.apiConnector = mockedApiConnector;
        NeutronRouter neutronRouter = defaultNeutronObject();
        neutronRouter.setName(null);
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, routerHandler.canCreateRouter(neutronRouter));
    }

    /* Test method to check if tenant ID is null */
    @Test
    public void testCanCreateRouterNullTenantID() {
        Activator.apiConnector = mockedApiConnector;
        NeutronRouter neutronRouter = defaultNeutronObject();
        neutronRouter.setTenantID(null);
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, routerHandler.canCreateRouter(neutronRouter));
    }

    /* Test method to check if router Project UUID not found */
    @Test
    public void testCanCreateRouterProjectUUIDnull() throws IOException {
        Activator.apiConnector = mockedApiConnector;
        NeutronRouter neutronRouter = defaultNeutronObject();
        when(mockedApiConnector.findById(Project.class, neutronRouter.getTenantID())).thenReturn(null);
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, routerHandler.canCreateRouter(neutronRouter));
    }

    /* Test method to check if router creation fails */
    @Test
    public void testCanCreateRouterFails() throws Exception {
        Activator.apiConnector = mockedApiConnector;
        NeutronRouter neutronRouter = defaultNeutronObject();
        LogicalRouter lr = PowerMock.createNiceMock(LogicalRouter.class);
        expectNew(LogicalRouter.class).andReturn(lr);
        when(mockedApiConnector.findById(Project.class, neutronRouter.getTenantID())).thenReturn(mockedProject);
        lr.setParent(mockedProject);
        when(mockedApiConnector.create(lr)).thenReturn(false);
        PowerMock.replay(lr, LogicalRouter.class);
        assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, routerHandler.canCreateRouter(neutronRouter));
    }

    /* Test method to check if router created */
    @Test
    public void testCanCreateRouterOK() throws Exception {
        Activator.apiConnector = mockedApiConnector;
        NeutronRouter neutronRouter = defaultNeutronObject();
        LogicalRouter lr = PowerMock.createNiceMock(LogicalRouter.class);
        expectNew(LogicalRouter.class).andReturn(lr);
        when(mockedApiConnector.findById(Project.class, neutronRouter.getTenantID())).thenReturn(mockedProject);
        lr.setParent(mockedProject);
        when(mockedApiConnector.create(lr)).thenReturn(true);
        PowerMock.replay(lr, LogicalRouter.class);
        assertEquals(HttpURLConnection.HTTP_OK, routerHandler.canCreateRouter(neutronRouter));
    }

    /* Test method to check if neutron router is null for delete */
    @Test
    public void testcanDeleteRouterNull() throws IOException {
        Activator.apiConnector = mockedApiConnector;
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, routerHandler.canDeleteRouter(null));
    }

    /* Test method to check if delete router that does not exist */
    @Test
    public void testcanDeleteRouterBadRequest() throws IOException {
        Activator.apiConnector = mockedApiConnector;
        NeutronRouter neutronRouter = defaultNeutronObject();
        when(mockedApiConnector.findById(LogicalRouter.class, neutronRouter.getRouterUUID())).thenReturn(null);
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, routerHandler.canDeleteRouter(neutronRouter));
    }

    /* Test method to check if can update router Null object */
    @Test
    public void testcanUpdateRouterObjNull() throws IOException {
        Activator.apiConnector = mockedApiConnector;
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, routerHandler.canUpdateRouter(null, null));
    }

    /* Test method to check if can update router object not found */
    @Test
    public void testcanUpdateRouterObjNotFound() throws IOException {
        Activator.apiConnector = mockedApiConnector;
        NeutronRouter neutronRouter = defaultNeutronObject();
        NeutronRouter deltaRouter = deltaNeutronObject();
        when(mockedApiConnector.findById(LogicalRouter.class, neutronRouter.getRouterUUID())).thenReturn(null);
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, routerHandler.canUpdateRouter(neutronRouter, deltaRouter));
    }

    /* Test method to check if can update router object not found */
    @Test
    public void testcanUpdateRouterFailed() throws IOException {
        Activator.apiConnector = mockedApiConnector;
        NeutronRouter neutronRouter = defaultNeutronObject();
        NeutronRouter deltaRouter = deltaNeutronObject();
        when(mockedApiConnector.findById(LogicalRouter.class, neutronRouter.getRouterUUID())).thenReturn(mockedLogicalRouter);
        when(mockedApiConnector.update(mockedLogicalRouter)).thenReturn(false);
        assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, routerHandler.canUpdateRouter(neutronRouter, deltaRouter));
    }

    /* Test method to check if can update router object not founf=d */
    @Test
    public void testcanUpdateRouterOK() throws IOException {
        Activator.apiConnector = mockedApiConnector;
        NeutronRouter neutronRouter = defaultNeutronObject();
        NeutronRouter deltaRouter = deltaNeutronObject();
        when(mockedApiConnector.findById(LogicalRouter.class, neutronRouter.getRouterUUID())).thenReturn(mockedLogicalRouter);
        when(mockedApiConnector.update(mockedLogicalRouter)).thenReturn(true);
        assertEquals(HttpURLConnection.HTTP_OK, routerHandler.canUpdateRouter(neutronRouter, deltaRouter));
    }

    /* Test method to check if canAttachInterface update vmi failed */
    @Test
    public void testcanAttachInterfaceVMIfailed() throws Exception {
        Activator.apiConnector = mockedApiConnector;
        NeutronRouter neutronRouter = defaultNeutronObject();
        NeutronRouter_Interface neutronRouterInterface = deltaNeutronRouter_Interface();
        when(mockedApiConnector.findById(LogicalRouter.class, neutronRouter.getRouterUUID())).thenReturn(mockedLogicalRouter);
        when(mockedApiConnector.findById(VirtualMachineInterface.class, neutronRouterInterface.getPortUUID()))
                .thenReturn(mockVirtualMachineInterface);
        mockedLogicalRouter.setVirtualMachineInterface(mockVirtualMachineInterface);
        VirtualMachine vm = PowerMock.createNiceMock(VirtualMachine.class);
        expectNew(VirtualMachine.class).andReturn(vm);
        vm.setName(neutronRouter.getRouterUUID());
        vm.setUuid(neutronRouter.getRouterUUID());
        mockVirtualMachineInterface.setVirtualMachine(vm);
        when(mockedApiConnector.update(mockVirtualMachineInterface)).thenReturn(false);
        PowerMock.replay(vm, VirtualMachine.class);
        assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, routerHandler.canAttachInterface(neutronRouter, neutronRouterInterface));
    }

    /*
     * Test method to check if canAttachInterface update vmi successfully but
     * attach interface failed
     */
    @Test
    public void testcanAttachInterfaceVMIOk() throws Exception {
        Activator.apiConnector = mockedApiConnector;
        NeutronRouter neutronRouter = defaultNeutronObject();
        NeutronRouter_Interface neutronRouterInterface = deltaNeutronRouter_Interface();
        when(mockedApiConnector.findById(LogicalRouter.class, neutronRouter.getRouterUUID())).thenReturn(mockedLogicalRouter);
        when(mockedApiConnector.findById(VirtualMachineInterface.class, neutronRouterInterface.getPortUUID()))
                .thenReturn(mockVirtualMachineInterface);
        mockedLogicalRouter.setVirtualMachineInterface(mockVirtualMachineInterface);
        VirtualMachine vm = PowerMock.createNiceMock(VirtualMachine.class);
        expectNew(VirtualMachine.class).andReturn(vm);
        vm.setName(neutronRouter.getRouterUUID());
        vm.setUuid(neutronRouter.getRouterUUID());
        mockVirtualMachineInterface.setVirtualMachine(vm);
        when(mockedApiConnector.update(mockVirtualMachineInterface)).thenReturn(true);
        PowerMock.replay(vm, VirtualMachine.class);
        when(mockedApiConnector.update(mockedLogicalRouter)).thenReturn(false);
        assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, routerHandler.canAttachInterface(neutronRouter, neutronRouterInterface));
    }

    /* Test method to check if interface is attached successfully */
    @Test
    public void testcanAttachInterfaceOK() throws Exception {
        Activator.apiConnector = mockedApiConnector;
        NeutronRouter neutronRouter = defaultNeutronObject();
        NeutronRouter_Interface neutronRouterInterface = deltaNeutronRouter_Interface();
        when(mockedApiConnector.findById(LogicalRouter.class, neutronRouter.getRouterUUID())).thenReturn(mockedLogicalRouter);
        when(mockedApiConnector.findById(VirtualMachineInterface.class, neutronRouterInterface.getPortUUID()))
                .thenReturn(mockVirtualMachineInterface);
        mockedLogicalRouter.setVirtualMachineInterface(mockVirtualMachineInterface);
        VirtualMachine vm = PowerMock.createNiceMock(VirtualMachine.class);
        expectNew(VirtualMachine.class).andReturn(vm);
        vm.setName(neutronRouter.getRouterUUID());
        vm.setUuid(neutronRouter.getRouterUUID());
        mockVirtualMachineInterface.setVirtualMachine(vm);
        when(mockedApiConnector.update(mockVirtualMachineInterface)).thenReturn(true);
        PowerMock.replay(vm, VirtualMachine.class);
        when(mockedApiConnector.update(mockedLogicalRouter)).thenReturn(true);
        assertEquals(HttpURLConnection.HTTP_OK, routerHandler.canAttachInterface(neutronRouter, neutronRouterInterface));
    }

    /* Test method to check if canDetachInterface update vmi failed */
    @Test
    public void testcanDetachInterfaceVMIfailed() throws Exception {
        Activator.apiConnector = mockedApiConnector;
        NeutronRouter neutronRouter = defaultNeutronObject();
        NeutronRouter_Interface neutronRouterInterface = deltaNeutronRouter_Interface();
        when(mockedApiConnector.findById(LogicalRouter.class, neutronRouter.getRouterUUID())).thenReturn(mockedLogicalRouter);
        when(mockedApiConnector.findById(VirtualMachineInterface.class, neutronRouterInterface.getPortUUID()))
                .thenReturn(mockVirtualMachineInterface);
        when(mockedApiConnector.update(mockVirtualMachineInterface)).thenReturn(false);
        assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, routerHandler.canDetachInterface(neutronRouter, neutronRouterInterface));
    }

    /* Test method to check if interface is detached succesfully */
    @Test
    public void testcanDetachInterfaceOK() throws Exception {
        Activator.apiConnector = mockedApiConnector;
        NeutronRouter neutronRouter = defaultNeutronObject();
        NeutronRouter_Interface neutronRouterInterface = deltaNeutronRouter_Interface();
        when(mockedApiConnector.findById(LogicalRouter.class, neutronRouter.getRouterUUID())).thenReturn(mockedLogicalRouter);
        when(mockedApiConnector.findById(VirtualMachineInterface.class, neutronRouterInterface.getPortUUID()))
                .thenReturn(mockVirtualMachineInterface);
        when(mockedApiConnector.update(mockVirtualMachineInterface)).thenReturn(true);
        when(mockedApiConnector.update(mockedLogicalRouter)).thenReturn(true);
        assertEquals(HttpURLConnection.HTTP_OK, routerHandler.canDetachInterface(neutronRouter, neutronRouterInterface));
    }
}