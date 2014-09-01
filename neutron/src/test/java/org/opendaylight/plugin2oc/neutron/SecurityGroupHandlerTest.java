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
import net.juniper.contrail.api.types.Project;
import net.juniper.contrail.api.types.SecurityGroup;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.controller.networkconfig.neutron.NeutronSecurityGroup;

import static org.mockito.Mockito.when;

/**
 * Test Class for SecurityGroupHandler.
 */

public class SecurityGroupHandlerTest {

    SecurityGroupHandler securityGroupHandler;
    SecurityGroupHandler mockSecurityGroupHandler = mock(SecurityGroupHandler.class);
    NeutronSecurityGroup mockNeutronSecurityGroup = mock(NeutronSecurityGroup.class);
    SecurityGroup mockSecurityGroup = mock(SecurityGroup.class);
    ApiConnector mockedApiConnector = mock(ApiConnector.class);
    Project mockedProject = mock(Project.class);

    @Before
    public void beforeTest() {
        securityGroupHandler = new SecurityGroupHandler();
        assertNotNull(mockSecurityGroupHandler);
        assertNotNull(mockNeutronSecurityGroup);
        assertNotNull(mockSecurityGroup);
        assertNotNull(mockedApiConnector);
        assertNotNull(mockedProject);
    }

    @After
    public void AfterTest() {
        securityGroupHandler = null;
        Activator.apiConnector = null;
    }

    /* dummy params for SecurityGroupHandler */
    public NeutronSecurityGroup defaultSecurityGroupObject() {
        NeutronSecurityGroup neutron = new NeutronSecurityGroup();
        neutron.setSecurityGroupDescription("new security froup description");
        neutron.setSecurityGroupName("securityGroupName");
        neutron.setSecurityGroupTenantID("6b9570f2-17b1-4fc3-99ec-1b7f7778a29a");
        neutron.setSecurityGroupUUID("900070f2-17b1-4fc3-99ec-1b7f7778a29a");
        return neutron;
    }

    /* Test method to check if neutron SecurityGroup object is null */
    @Test
    public void testCanCreateSecurityGroupNull() {
        Activator.apiConnector = mockedApiConnector;
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, securityGroupHandler.canCreateNeutronSecurityGroup(null));
    }

    /* Test method to check if neutron SecurityGroup name is null */
    @Test
    public void testCanCreateSecurityGroupNameNull() {
        Activator.apiConnector = mockedApiConnector;
        NeutronSecurityGroup neutron = defaultSecurityGroupObject();
        neutron.setSecurityGroupName(null);
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, securityGroupHandler.canCreateNeutronSecurityGroup(neutron));
    }

    /* Test method to check if neutron SecurityGroup Description is null */
    @Test
    public void testCanCreateSecurityGroupDescriptionNull() {
        Activator.apiConnector = mockedApiConnector;
        NeutronSecurityGroup neutron = defaultSecurityGroupObject();
        neutron.setSecurityGroupDescription(null);
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, securityGroupHandler.canCreateNeutronSecurityGroup(neutron));
    }

    /* Test method to check if neutron SecurityGroup TenantId is null */
    @Test
    public void testCanCreateSecurityGroupTenantId() {
        Activator.apiConnector = mockedApiConnector;
        NeutronSecurityGroup neutron = defaultSecurityGroupObject();
        neutron.setSecurityGroupTenantID(null);
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, securityGroupHandler.canCreateNeutronSecurityGroup(neutron));
    }

    /* Test method to check if neutron SecurityGroup already exist */
    @Test
    public void testCanCreateSecurityGroupExist() throws IOException {
        Activator.apiConnector = mockedApiConnector;
        NeutronSecurityGroup neutron = defaultSecurityGroupObject();
        when(mockedApiConnector.findById(SecurityGroup.class, neutron.getSecurityGroupUUID())).thenReturn(mockSecurityGroup);
        assertEquals(HttpURLConnection.HTTP_FORBIDDEN, securityGroupHandler.canCreateNeutronSecurityGroup(neutron));
    }

    /* Test method to check if neutron SecurityGroup project null */
    @Test
    public void testCanCreateSecurityGroupProjectNull() throws IOException {
        Activator.apiConnector = mockedApiConnector;
        NeutronSecurityGroup neutron = defaultSecurityGroupObject();
        when(mockedApiConnector.findById(SecurityGroup.class, neutron.getSecurityGroupUUID())).thenReturn(null);
        when(mockedApiConnector.findById(Project.class, neutron.getSecurityGroupTenantID())).thenReturn(null);
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, securityGroupHandler.canCreateNeutronSecurityGroup(neutron));
    }

    /* Test method to check if neutron SecurityGroup by same name exist */
    @Test
    public void testCanCreateSecurityGroupByName() throws IOException {
        Activator.apiConnector = mockedApiConnector;
        NeutronSecurityGroup neutron = defaultSecurityGroupObject();
        when(mockedApiConnector.findById(SecurityGroup.class, neutron.getSecurityGroupUUID())).thenReturn(null);
        when(mockedApiConnector.findById(Project.class, neutron.getSecurityGroupTenantID())).thenReturn(mockedProject);
        when(mockedApiConnector.findByName(SecurityGroup.class, mockedProject, neutron.getSecurityGroupName())).thenReturn("name");
        assertEquals(HttpURLConnection.HTTP_FORBIDDEN, securityGroupHandler.canCreateNeutronSecurityGroup(neutron));
    }

    /* Test method to check if neutron SecurityGroup returns status 200 ok */
    @Test
    public void testCanCreateSecurityGroupOK() throws IOException {
        Activator.apiConnector = mockedApiConnector;
        NeutronSecurityGroup neutron = defaultSecurityGroupObject();
        when(mockedApiConnector.findById(SecurityGroup.class, neutron.getSecurityGroupUUID())).thenReturn(null);
        when(mockedApiConnector.findById(Project.class, neutron.getSecurityGroupTenantID())).thenReturn(mockedProject);
        when(mockedApiConnector.findByName(SecurityGroup.class, mockedProject, neutron.getSecurityGroupName())).thenReturn(null);
        assertEquals(HttpURLConnection.HTTP_OK, securityGroupHandler.canCreateNeutronSecurityGroup(neutron));
    }

    /* Test method to check if neutron can delete SecurityGroup not found */
    @Test
    public void testCanDeleteSecurityGroupNotFound() throws IOException {
        Activator.apiConnector = mockedApiConnector;
        NeutronSecurityGroup neutron = defaultSecurityGroupObject();
        when(mockedApiConnector.findById(SecurityGroup.class, neutron.getSecurityGroupUUID())).thenReturn(null);
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, securityGroupHandler.canDeleteNeutronSecurityGroup(neutron));
    }

    /* Test method to check if neutron can delete SecurityGroup has port associated with it */
    @Test
    public void testCanDeleteSecurityGroupPortExist() throws IOException {
        Activator.apiConnector = mockedApiConnector;
        NeutronSecurityGroup neutron = defaultSecurityGroupObject();
        when(mockedApiConnector.findById(SecurityGroup.class, neutron.getSecurityGroupUUID())).thenReturn(mockSecurityGroup);
        List<ObjectReference<ApiPropertyBase>> vmiList = new ArrayList<ObjectReference<ApiPropertyBase>>();
        when(mockSecurityGroup.getVirtualMachineInterfaceBackRefs()).thenReturn(vmiList);
        assertEquals(HttpURLConnection.HTTP_FORBIDDEN, securityGroupHandler.canDeleteNeutronSecurityGroup(neutron));
    }

    /* Test method to check if neutron can delete SecurityGroup returns status ok */
    @Test
    public void testCanDeleteSecurityGroupOK() throws IOException {
        Activator.apiConnector = mockedApiConnector;
        NeutronSecurityGroup neutron = defaultSecurityGroupObject();
        when(mockedApiConnector.findById(SecurityGroup.class, neutron.getSecurityGroupUUID())).thenReturn(mockSecurityGroup);
        when(mockSecurityGroup.getVirtualMachineInterfaceBackRefs()).thenReturn(null);
        assertEquals(HttpURLConnection.HTTP_OK, securityGroupHandler.canDeleteNeutronSecurityGroup(neutron));
    }
}
