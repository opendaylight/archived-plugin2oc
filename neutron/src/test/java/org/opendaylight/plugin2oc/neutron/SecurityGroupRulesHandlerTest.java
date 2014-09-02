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

import net.juniper.contrail.api.ApiConnector;
import net.juniper.contrail.api.types.SecurityGroup;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.controller.networkconfig.neutron.NeutronSecurityRule;

/**
 * Test Class for SecurityGroupRules.
 */
public class SecurityGroupRulesHandlerTest {
    SecurityGroupRulesHandler securityRulesHandler;
    SecurityGroup mockedSecurityGroup = mock(SecurityGroup.class);
    SecurityGroupRulesHandler mockedSecurityRulesHandler = mock(SecurityGroupRulesHandler.class);
    ApiConnector mockedApiConnector = mock(ApiConnector.class);

    @Before
    public void beforeTest() {
        securityRulesHandler = new SecurityGroupRulesHandler();
        assertNotNull(mockedApiConnector);
        assertNotNull(mockedSecurityGroup);
        assertNotNull(mockedSecurityRulesHandler);
    }

    @After
    public void afterTest() {
        securityRulesHandler = null;
        Activator.apiConnector = null;
    }

    /* dummy params for Neutron Rule */
    public NeutronSecurityRule defaultSecurityRulesObject() {
        NeutronSecurityRule securityRule = new NeutronSecurityRule();
        securityRule.setSecurityRuleDirection("ingress");
        securityRule.setSecurityRuleEthertype("IPV4");
        securityRule.setSecurityRuleGroupID("85cc3048-abc3-43cc-89b3-377341426ac5");
        securityRule.setSecurityRemoteGroupID("a7734e61-b545-452d-a3cd-0189cbd9747a");
        securityRule.setSecurityRuleRemoteIpPrefix(null);
        securityRule.setSecurityRulePortMax(80);
        securityRule.setSecurityRulePortMin(80);
        securityRule.setSecurityRuleProtocol("tcp");
        securityRule.setSecurityRuleUUID("2bc0accf-312e-429a-956e-e4407625eb62");
        return securityRule;
    }

    /* Test method to check if neutron rule is null */
    @Test
    public void testCanCreateSecurityRuleNull() {
        Activator.apiConnector = mockedApiConnector;
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, securityRulesHandler.canCreateNeutronSecurityRule(null));
    }

    /* Test method to check if neutron rule direction is null */
    @Test
    public void testCanCreateSecurityRuleDirectionNull() {
        Activator.apiConnector = mockedApiConnector;
        NeutronSecurityRule securityRule = defaultSecurityRulesObject();
        securityRule.setSecurityRuleDirection(null);
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, securityRulesHandler.canCreateNeutronSecurityRule(securityRule));
    }

    /* Test method to check if neutron rule direction is invalid */
    @Test
    public void testCanCreateSecurityRuleDirectionInValid() {
        Activator.apiConnector = mockedApiConnector;
        NeutronSecurityRule securityRule = defaultSecurityRulesObject();
        securityRule.setSecurityRuleDirection("invalid");
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, securityRulesHandler.canCreateNeutronSecurityRule(securityRule));
    }

    /* Test method to check if neutron rule protocol is null */
    @Test
    public void testCanCreateSecurityRuleProtocolNull() {
        Activator.apiConnector = mockedApiConnector;
        NeutronSecurityRule securityRule = defaultSecurityRulesObject();
        securityRule.setSecurityRuleProtocol(null);
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, securityRulesHandler.canCreateNeutronSecurityRule(securityRule));
    }

    /* Test method to check if neutron rule protocol is invalid */
    @Test
    public void testCanCreateSecurityRuleProtocolInvalid() {
        Activator.apiConnector = mockedApiConnector;
        NeutronSecurityRule securityRule = defaultSecurityRulesObject();
        securityRule.setSecurityRuleProtocol("invalid");
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, securityRulesHandler.canCreateNeutronSecurityRule(securityRule));
    }

    /* Test method to check if neutron rule start port is null */
    @Test
    public void testCanCreateSecurityRuleStartPortNull() {
        Activator.apiConnector = mockedApiConnector;
        NeutronSecurityRule securityRule = defaultSecurityRulesObject();
        securityRule.setSecurityRulePortMax(null);
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, securityRulesHandler.canCreateNeutronSecurityRule(securityRule));
    }

    /* Test method to check if neutron rule end port is null */
    @Test
    public void testCanCreateSecurityRuleEndPortNull() {
        Activator.apiConnector = mockedApiConnector;
        NeutronSecurityRule securityRule = defaultSecurityRulesObject();
        securityRule.setSecurityRulePortMin(null);
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, securityRulesHandler.canCreateNeutronSecurityRule(securityRule));
    }

    /* Test method to check if neutron rule end port is invalid */
    @Test
    public void testCanCreateSecurityRulePortInValid() {
        Activator.apiConnector = mockedApiConnector;
        NeutronSecurityRule securityRule = defaultSecurityRulesObject();
        securityRule.setSecurityRuleProtocol("tcp");
        securityRule.setSecurityRulePortMax(20);
        securityRule.setSecurityRulePortMin(30);
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, securityRulesHandler.canCreateNeutronSecurityRule(securityRule));
    }

    /* Test method to check if neutron rule ICMP Type invalid */
    @Test
    public void testCanCreateSecurityRuleICMPTypeInValid() {
        Activator.apiConnector = mockedApiConnector;
        NeutronSecurityRule securityRule = defaultSecurityRulesObject();
        securityRule.setSecurityRuleProtocol("icmp");
        securityRule.setSecurityRulePortMin(256);
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, securityRulesHandler.canCreateNeutronSecurityRule(securityRule));
    }

    /* Test method to check if neutron rule SecurityRuleGroup ID is null */
    @Test
    public void testCanCreateSecurityRuleGroupIDNull() {
        Activator.apiConnector = mockedApiConnector;
        NeutronSecurityRule securityRule = defaultSecurityRulesObject();
        securityRule.setSecurityRuleGroupID(null);
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, securityRulesHandler.canCreateNeutronSecurityRule(securityRule));
    }

    /* Test method to check if neutron security group ID does not exist */
    @Test
    public void testCanCreateSecurityRuleGroupIDNotExsist() throws IOException {
        Activator.apiConnector = mockedApiConnector;
        NeutronSecurityRule securityRule = defaultSecurityRulesObject();
        when(mockedApiConnector.findById(SecurityGroup.class, securityRule.getSecurityRuleGroupID())).thenReturn(null);
        assertEquals(HttpURLConnection.HTTP_FORBIDDEN, securityRulesHandler.canCreateNeutronSecurityRule(securityRule));
    }

    /* Test method to check if neutron SecurityRuleCIDR Remote ID does not exist*/
    @Test
    public void testCanCreateSecurityRuleCIDR_RemoteIDNotExsist() throws IOException {
        Activator.apiConnector = mockedApiConnector;
        NeutronSecurityRule securityRule = defaultSecurityRulesObject();
        when(mockedApiConnector.findById(SecurityGroup.class, securityRule.getSecurityRuleGroupID())).thenReturn(mockedSecurityGroup);
        securityRule.setSecurityRemoteGroupID(null);
        securityRule.setSecurityRuleRemoteIpPrefix(null);
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, securityRulesHandler.canCreateNeutronSecurityRule(securityRule));
    }

    /* Test method to check if neutron security group return status 200 OK*/
    @Test
    public void testCanCreateSecurityRuleOK() throws IOException {
        Activator.apiConnector = mockedApiConnector;
        NeutronSecurityRule securityRule = defaultSecurityRulesObject();
        securityRule.setSecurityRuleRemoteIpPrefix("192.168.0.0/24");
        securityRule.setSecurityRemoteGroupID(null);
        when(mockedApiConnector.findById(SecurityGroup.class, securityRule.getSecurityRuleGroupID())).thenReturn(mockedSecurityGroup);
        assertEquals(HttpURLConnection.HTTP_OK, securityRulesHandler.canCreateNeutronSecurityRule(securityRule));
    }

    /* Test method to check if neutron security group ID does not exist */
    @Test
    public void testCanDeleteSecurityRuleGroupIDNotExist() throws IOException {
        Activator.apiConnector = mockedApiConnector;
        NeutronSecurityRule securityRule = defaultSecurityRulesObject();
        when(mockedApiConnector.findById(SecurityGroup.class, securityRule.getSecurityRuleGroupID())).thenReturn(null);
        assertEquals(HttpURLConnection.HTTP_FORBIDDEN, securityRulesHandler.canDeleteNeutronSecurityRule(securityRule));
    }

    /* Test method to check if neutron SecurityGroup rule does not exist for the specified UUID. */
    @Test
    public void testCanDeleteSecurityRule() throws IOException {
        Activator.apiConnector = mockedApiConnector;
        NeutronSecurityRule securityRule = defaultSecurityRulesObject();
        when(mockedApiConnector.findById(SecurityGroup.class, securityRule.getSecurityRuleGroupID())).thenReturn(mockedSecurityGroup);
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, securityRulesHandler.canDeleteNeutronSecurityRule(securityRule));
    }

}