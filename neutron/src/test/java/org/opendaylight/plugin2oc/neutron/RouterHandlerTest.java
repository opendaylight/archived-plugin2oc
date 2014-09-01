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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import net.juniper.contrail.api.ApiConnector;
import net.juniper.contrail.api.ApiPropertyBase;
import net.juniper.contrail.api.ObjectReference;
import net.juniper.contrail.api.types.LogicalRouter;
import net.juniper.contrail.api.types.Project;
import net.juniper.contrail.api.types.VirtualMachineInterface;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.controller.networkconfig.neutron.NeutronRouter;
import org.opendaylight.controller.networkconfig.neutron.NeutronRouter_Interface;
import org.opendaylight.controller.networkconfig.neutron.NeutronRouter_NetworkReference;
/**
 * Test Class for Neutron Router.
 */
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

    /* Test method to check if tenant ID is null */
    @Test
    public void testCanCreateRouterNullTenantID() {
        Activator.apiConnector = mockedApiConnector;
        NeutronRouter neutronRouter = defaultNeutronObject();
        neutronRouter.setTenantID(null);
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, routerHandler.canCreateRouter(neutronRouter));
    }

    /* Test method to check if neutron router name is null */
    @Test
    public void testCanCreateRouterNullName() {
        Activator.apiConnector = mockedApiConnector;
        NeutronRouter neutronRouter = defaultNeutronObject();
        neutronRouter.setName(null);
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, routerHandler.canCreateRouter(neutronRouter));
    }

    /*
     * Test method to check neutron network create with same name
     */
    @Test
    public void testcanCreateRouterByNameExist() throws IOException {
        Activator.apiConnector = mockedApiConnector;
        NeutronRouter neutronRouter = defaultNeutronObject();
        when(mockedApiConnector.findById(Project.class, neutronRouter.getTenantID())).thenReturn(mockedProject);
        when(mockedApiConnector.findByName(LogicalRouter.class, mockedProject, neutronRouter.getName())).thenReturn("Router-01");
        assertEquals(HttpURLConnection.HTTP_FORBIDDEN, routerHandler.canCreateRouter(neutronRouter));
    }

    /*
     * Test method to check neutron network create return status 200 Ok
     */
    @Test
    public void testcanCreateRouterOK() throws IOException {
        Activator.apiConnector = mockedApiConnector;
        NeutronRouter neutronRouter = defaultNeutronObject();
        when(mockedApiConnector.findById(Project.class, neutronRouter.getTenantID())).thenReturn(mockedProject);
        when(mockedApiConnector.findByName(LogicalRouter.class, mockedProject, neutronRouter.getName())).thenReturn(null);
        assertEquals(HttpURLConnection.HTTP_OK, routerHandler.canCreateRouter(neutronRouter));
    }

    /* Test method to check if neutron router object is null for delete */
    @Test
    public void testcanDeleteRouterNull() throws IOException {
        Activator.apiConnector = mockedApiConnector;
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, routerHandler.canDeleteRouter(null));
    }

    /* Test method to check if delete router with status 200 ok */
    @Test
    public void testcanDeleteRouterOK() throws IOException {
        Activator.apiConnector = mockedApiConnector;
        NeutronRouter neutronRouter = defaultNeutronObject();
        when(mockedApiConnector.findById(LogicalRouter.class, neutronRouter.getRouterUUID())).thenReturn(mockedLogicalRouter);
        assertEquals(HttpURLConnection.HTTP_OK, routerHandler.canDeleteRouter(neutronRouter));
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

    /* Test method to check if can update router returns status 200 ok */
    @Test
    public void testcanUpdateRouterOK() throws IOException {
        Activator.apiConnector = mockedApiConnector;
        NeutronRouter neutronRouter = defaultNeutronObject();
        NeutronRouter deltaRouter = deltaNeutronObject();
        when(mockedApiConnector.findById(LogicalRouter.class, neutronRouter.getRouterUUID())).thenReturn(mockedLogicalRouter);
        assertEquals(HttpURLConnection.HTTP_OK, routerHandler.canUpdateRouter(neutronRouter, deltaRouter));
    }

    /*
     * Test method to check if canAttachInterface update vmi successfully but
     * attach interface failed
     */
    @Test
    public void testcanAttachInterfaceOk() throws Exception {
        Activator.apiConnector = mockedApiConnector;
        NeutronRouter neutronRouter = defaultNeutronObject();
        NeutronRouter_Interface neutronRouterInterface = deltaNeutronRouter_Interface();
        assertEquals(HttpURLConnection.HTTP_OK, routerHandler.canAttachInterface(neutronRouter, neutronRouterInterface));
    }

    /* Test method to check if canDetachInterface when no such Router exist */
    @Test
    public void testcanDetachInterfaceRouterNotFound() throws Exception {
        Activator.apiConnector = mockedApiConnector;
        NeutronRouter neutronRouter = defaultNeutronObject();
        NeutronRouter_Interface neutronRouterInterface = deltaNeutronRouter_Interface();
        when(mockedApiConnector.findById(LogicalRouter.class, neutronRouter.getRouterUUID())).thenReturn(null);
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, routerHandler.canDetachInterface(neutronRouter, neutronRouterInterface));
    }

    /* Test method to check if canDetachInterface return status 200 OK */
    @Test
    public void testcanDetachInterfaceOK() throws Exception {
        Activator.apiConnector = mockedApiConnector;
        NeutronRouter neutronRouter = defaultNeutronObject();
        NeutronRouter_Interface neutronRouterInterface = deltaNeutronRouter_Interface();
        when(mockedApiConnector.findById(LogicalRouter.class, neutronRouter.getRouterUUID())).thenReturn(mockedLogicalRouter);
        when(mockedApiConnector.findById(VirtualMachineInterface.class, neutronRouterInterface.getPortUUID()))
                .thenReturn(mockVirtualMachineInterface);
        assertEquals(HttpURLConnection.HTTP_OK, routerHandler.canDetachInterface(neutronRouter, neutronRouterInterface));
    }

    /* Test method to check if interface is attached with port ID*/
    @Test
    public void testcanDetachInterfacePortExist() throws Exception {
            Activator.apiConnector = mockedApiConnector;
            NeutronRouter neutronRouter = defaultNeutronObject();
            NeutronRouter_Interface neutronRouterInterface = deltaNeutronRouter_Interface();
            when(mockedApiConnector.findById(LogicalRouter.class,neutronRouter.getRouterUUID())).thenReturn(mockedLogicalRouter);
            when(mockedApiConnector.findById(VirtualMachineInterface.class,neutronRouterInterface.getPortUUID())).thenReturn(mockVirtualMachineInterface);
            List<ObjectReference<ApiPropertyBase>> vmiList = new ArrayList<ObjectReference<ApiPropertyBase>>();
            vmiList.add(new ObjectReference<ApiPropertyBase>(mockVirtualMachineInterface.getQualifiedName(), null));
            vmiList.get(0).setReference(mockVirtualMachineInterface.getQualifiedName(), null, "", "119570f2-17b1-4fc3-99ec-1b7f7778a29a");
            when(mockedLogicalRouter.getVirtualMachineInterface()).thenReturn(vmiList);
            assertEquals(HttpURLConnection.HTTP_OK,routerHandler.canDetachInterface(neutronRouter,neutronRouterInterface));
    }

    /* Test method to check if No interface is attached with port ID */
     @Test
     public void testcanDetachInterfacePortDoesNotExist() throws Exception {
         Activator.apiConnector = mockedApiConnector;
         NeutronRouter neutronRouter = defaultNeutronObject();
         NeutronRouter_Interface neutronRouterInterface = deltaNeutronRouter_Interface();
         when(mockedApiConnector.findById(LogicalRouter.class,neutronRouter.getRouterUUID())).thenReturn(mockedLogicalRouter);
         when(mockedApiConnector.findById(VirtualMachineInterface.class,neutronRouterInterface.getPortUUID())).thenReturn(mockVirtualMachineInterface);
         List<ObjectReference<ApiPropertyBase>> vmiList = new ArrayList<ObjectReference<ApiPropertyBase>>();
         vmiList.add(new ObjectReference<ApiPropertyBase>(mockVirtualMachineInterface.getQualifiedName(), null));
         vmiList.get(0).setReference(mockVirtualMachineInterface.getQualifiedName(), null, "", "ff9570f2-17b1-4fc3-99ec-1b7f7778a29a");
         when(mockedLogicalRouter.getVirtualMachineInterface()).thenReturn(vmiList);
         assertEquals(HttpURLConnection.HTTP_BAD_REQUEST,routerHandler.canDetachInterface(neutronRouter,neutronRouterInterface));
     }
}
