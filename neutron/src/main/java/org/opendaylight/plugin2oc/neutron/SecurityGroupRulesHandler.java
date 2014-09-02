/*
 * Copyright (C) 2014 Juniper Networks, Inc.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 */
package org.opendaylight.plugin2oc.neutron;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.UUID;

import net.juniper.contrail.api.ApiConnector;
import net.juniper.contrail.api.types.PolicyEntriesType;
import net.juniper.contrail.api.types.PolicyEntriesType.PolicyRuleType;
import net.juniper.contrail.api.types.PolicyEntriesType.PolicyRuleType.AddressType;
import net.juniper.contrail.api.types.PolicyEntriesType.PolicyRuleType.ActionListType;
import net.juniper.contrail.api.types.PolicyEntriesType.PolicyRuleType.SequenceType;
import net.juniper.contrail.api.types.SubnetType;
import net.juniper.contrail.api.types.SecurityGroup;

import org.opendaylight.controller.networkconfig.neutron.INeutronSecurityRuleAware;
import org.opendaylight.controller.networkconfig.neutron.NeutronSecurityRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle requests for Security Group.
 */

public class SecurityGroupRulesHandler implements INeutronSecurityRuleAware {
    /**
     * Logger instance.
     */
    static final Logger LOGGER = LoggerFactory.getLogger(SecurityGroupRulesHandler.class);
    static ApiConnector apiConnector;

    /**
     * Invoked when a security group rules creation is requested to check if the
     * specified security group rules can be created and then creates the
     * security group rule
     *
            * @param securityRule
     *            An instance of proposed new Neutron SecurityGroupRules object.
     *
            * @return A HTTP status code to the creation request.
     */
    @Override
    public int canCreateNeutronSecurityRule(NeutronSecurityRule securityRule) {
        SecurityGroup virtualSecurityGroup;
        apiConnector = Activator.apiConnector;
        if (securityRule == null) {
            LOGGER.error("SecurityGroupRule object can't be null..");
            return HttpURLConnection.HTTP_BAD_REQUEST;
        }
        if(securityRule.getSecurityRuleUUID() == null || securityRule.getSecurityRuleUUID() == "")
        {
            LOGGER.error("SecurityGroup Rule UUID can't be null/empty...");
            return HttpURLConnection.HTTP_BAD_REQUEST;
        }
        else
        {
            String secGroupRulesUUID = securityRule.getSecurityRuleUUID();
            if (!(securityRule.getSecurityRuleUUID().contains("-"))) {
                secGroupRulesUUID = Utils.uuidFormater(securityRule.getSecurityRuleUUID());
            }
            boolean isSecGroupRulesUUID = Utils.isValidHexNumber(secGroupRulesUUID);
            if (!isSecGroupRulesUUID) {
                LOGGER.info("Badly formed Hexadecimal UUID...");
                return HttpURLConnection.HTTP_BAD_REQUEST;
            }
            secGroupRulesUUID = UUID.fromString(secGroupRulesUUID).toString();
        }
        if (securityRule.getSecurityRuleDirection() == null || securityRule.getSecurityRuleDirection().equals("")) {
            LOGGER.error("SecurityGroup Rule direction can't be null/empty...");
            return HttpURLConnection.HTTP_BAD_REQUEST;
        }
        if (!(securityRule.getSecurityRuleDirection().equalsIgnoreCase("ingress") || securityRule.getSecurityRuleDirection().equalsIgnoreCase("egress"))) {
            LOGGER.error("SecurityGroup Rule direction invalid : Valid values are ingress/egress");
            return HttpURLConnection.HTTP_BAD_REQUEST;
        }
        if (securityRule.getSecurityRulePortMin() == null || securityRule.getSecurityRulePortMax() == null) {
            LOGGER.error("SecurityGroup port min/max range can't be null/empty...");
            return HttpURLConnection.HTTP_BAD_REQUEST;
        }
        if (securityRule.getSecurityRuleProtocol() == null || ("").equals(securityRule.getSecurityRuleProtocol())) {
            LOGGER.error("Security protocol can't be null/empty...");
            return HttpURLConnection.HTTP_BAD_REQUEST;
        }
        if (!(securityRule.getSecurityRuleProtocol().equalsIgnoreCase("tcp") || securityRule.getSecurityRuleProtocol().equalsIgnoreCase("udp")
                || securityRule.getSecurityRuleProtocol().equalsIgnoreCase("icmp") || securityRule.getSecurityRuleProtocol().equalsIgnoreCase("any"))) {
            LOGGER.error("Security protocol invalid : Valid values are tcp,udp,icmp and any");
            return HttpURLConnection.HTTP_BAD_REQUEST;
        }
        if (securityRule.getSecurityRuleProtocol().equalsIgnoreCase("tcp") || securityRule.getSecurityRuleProtocol().equalsIgnoreCase("udp")) {
            if (securityRule.getSecurityRulePortMin() > securityRule.getSecurityRulePortMax()) {
                LOGGER.error("SecurityGroup port min range can't be greator than port max range");
                return HttpURLConnection.HTTP_BAD_REQUEST;
            }
        }
        if (securityRule.getSecurityRuleProtocol().equalsIgnoreCase("icmp")) {
            if (securityRule.getSecurityRulePortMin() > 255) {
                LOGGER.error("SecurityGroup ICMP type can't be greator 255");
                return HttpURLConnection.HTTP_BAD_REQUEST;
            }
        }
        if (securityRule.getSecurityRuleGroupID() == null || securityRule.getSecurityRuleGroupID().equals("")) {
            LOGGER.error("Security Group ID can't be null/empty...");
            return HttpURLConnection.HTTP_BAD_REQUEST;
        }
        String secGroupRuleUUID = securityRule.getSecurityRuleGroupID();
        if (!(securityRule.getSecurityRuleGroupID().contains("-"))) {
            secGroupRuleUUID = Utils.uuidFormater(securityRule.getSecurityRuleGroupID());
        }
        boolean isSecGroupRuleUUID = Utils.isValidHexNumber(secGroupRuleUUID);
        if (!isSecGroupRuleUUID) {
            LOGGER.info("Badly formed Hexadecimal UUID...");
            return HttpURLConnection.HTTP_BAD_REQUEST;
        }
        secGroupRuleUUID = UUID.fromString(secGroupRuleUUID).toString();
        try {
            virtualSecurityGroup = (SecurityGroup) apiConnector.findById(SecurityGroup.class, secGroupRuleUUID);
        } catch (IOException e) {
            LOGGER.error("Exception :     " + e);
            return HttpURLConnection.HTTP_INTERNAL_ERROR;
        }
        if (virtualSecurityGroup == null) {
            LOGGER.warn("SecurityGroup does not exists for the specified security group ID");
            return HttpURLConnection.HTTP_FORBIDDEN;
        }
        if (virtualSecurityGroup.getEntries() != null) {
            if (virtualSecurityGroup.getEntries().getPolicyRule().isEmpty() == false) {
                ListIterator<PolicyRuleType> i = virtualSecurityGroup.getEntries().getPolicyRule().listIterator();
                PolicyRuleType policyRule;
                while (i.hasNext()) {
                    policyRule = (PolicyRuleType) i.next();
                    if (policyRule.getRuleUuid().equalsIgnoreCase(securityRule.getSecurityRuleUUID())) {
                        LOGGER.warn("SecurityGroup rule already exsist for the specified security group");
                        return HttpURLConnection.HTTP_CONFLICT;
                    }
                }
            }
        }
        if (securityRule.getSecurityRemoteGroupID() == null && securityRule.getSecurityRuleRemoteIpPrefix() == null) {
            LOGGER.error("Both SecurityGroup  remote ID and remote IP prefix can nor be null");
            return HttpURLConnection.HTTP_BAD_REQUEST;
            }
        if(securityRule.getSecurityRuleRemoteIpPrefix() == null && securityRule.getSecurityRemoteGroupID() != null  )
        {
            virtualSecurityGroup = null;
            secGroupRuleUUID = securityRule.getSecurityRemoteGroupID();
            if (!(securityRule.getSecurityRemoteGroupID().contains("-"))) {
                secGroupRuleUUID = Utils.uuidFormater(securityRule.getSecurityRemoteGroupID());
            }
            isSecGroupRuleUUID = Utils.isValidHexNumber(secGroupRuleUUID);
            if (!isSecGroupRuleUUID) {
                LOGGER.info("Badly formed Hexadecimal UUID...");
                return HttpURLConnection.HTTP_BAD_REQUEST;
            }
            secGroupRuleUUID = UUID.fromString(secGroupRuleUUID).toString();
            try {
                virtualSecurityGroup = (SecurityGroup) apiConnector.findById(SecurityGroup.class, secGroupRuleUUID);
            } catch (IOException e) {
                LOGGER.error("Exception :     " + e);
                return HttpURLConnection.HTTP_INTERNAL_ERROR;
            }
            if (virtualSecurityGroup == null) {
                LOGGER.warn("SecurityRemoteGroup does not exists for the specified security group ID");
                return HttpURLConnection.HTTP_FORBIDDEN;
            }
            }
        LOGGER.info("SecurityGroupRules object " + securityRule);
        return HttpURLConnection.HTTP_OK;
    }

    /**
     * Invoked to create the specified Neutron Security Group Rules .
     *
     * @param securityRule
     *             An instance of new NeutronSecurityGroupRule object.
     */
    private void createSecurityGroupRules(NeutronSecurityRule securityRule) throws IOException {
        PolicyEntriesType virtualSecurityGroupRule = null;
        SecurityGroup virtualSecurityGroup = null;

        String secGroupRuleUUID = securityRule.getSecurityRuleGroupID();
        if (!(securityRule.getSecurityRuleGroupID().contains("-"))) {
            secGroupRuleUUID = Utils.uuidFormater(securityRule.getSecurityRuleGroupID());
        }
        secGroupRuleUUID = UUID.fromString(secGroupRuleUUID).toString();
        try {
            virtualSecurityGroup = (SecurityGroup) apiConnector.findById(SecurityGroup.class, secGroupRuleUUID);
        } catch (IOException e) {
            LOGGER.error("Exception :     " + e);
        }
        virtualSecurityGroupRule = virtualSecurityGroup.getEntries();
        if (virtualSecurityGroupRule == null) {
            virtualSecurityGroupRule = new PolicyEntriesType();
        }
        virtualSecurityGroupRule = mapSecurityGroupRuleProperties(securityRule, virtualSecurityGroupRule);
        virtualSecurityGroup.setEntries(virtualSecurityGroupRule);
        try {
            if (apiConnector.update(virtualSecurityGroup)) {
                LOGGER.info("SecurityGroup rule creation success..");
            } else {
                LOGGER.warn("SecurityGroup rule creation failed1..");
                return;
            }
        } catch (IOException e) {
            LOGGER.warn("SecurityGroup rule creation failed2..");
            return;
        }
        LOGGER.info("SecurityGroup : " + virtualSecurityGroup.getName() + "  having UUID : " + virtualSecurityGroup.getUuid()
                + "  sucessfully added with Security Group Rule");
    }
    /**
     * Invoked to create a Security Group Rules and take action after the Security
     * Group Rules has been created.
     *
     * @param securityRule
     *            An instance of new {@link NeutronSecurityRule} object.
     */
    @Override
    public void neutronSecurityRuleCreated(NeutronSecurityRule securityRule) {
        apiConnector = Activator.apiConnector;
        try {
            createSecurityGroupRules(securityRule);
        } catch (IOException e) {
            LOGGER.error("Exception :     " + e);
        }
        SecurityGroup virtualSecurityGroup = null;
        String secGroupRuleUUID = securityRule.getSecurityRuleGroupID();
        if (!(securityRule.getSecurityRuleGroupID().contains("-"))) {
            secGroupRuleUUID = Utils.uuidFormater(securityRule.getSecurityRuleGroupID());
        }
        secGroupRuleUUID = UUID.fromString(secGroupRuleUUID).toString();
        try {
            virtualSecurityGroup = (SecurityGroup) apiConnector.findById(SecurityGroup.class, secGroupRuleUUID);
        } catch (IOException e) {
            LOGGER.error("Exception :     " + e);
        }
        if (virtualSecurityGroup.getEntries() != null) {
            if (virtualSecurityGroup.getEntries().getPolicyRule().isEmpty() == false) {
                Iterator<PolicyRuleType> i = virtualSecurityGroup.getEntries().getPolicyRule().iterator();
                PolicyRuleType policyRule;
                while (i.hasNext()) {
                    policyRule = (PolicyRuleType) i.next();
                    if (policyRule.getRuleUuid().equalsIgnoreCase(securityRule.getSecurityRuleUUID())) {
                        LOGGER.info("SecurityGroup rule creation for the specified security group is verfied");
                    }
                }
            }
        }
    }

    @Override
    public int canUpdateNeutronSecurityRule(NeutronSecurityRule delta, NeutronSecurityRule original) {
        // TODO Auto-generated method stub - Nothing to update
        return 0;
    }

    @Override
    public void neutronSecurityRuleUpdated(NeutronSecurityRule securityRule) {
        // TODO Auto-generated method stub - Nothing to update

    }
    /**
     * Invoked when a security group Rule deletion is requested to indicate if the
     * specified neutron security group  rule can be deleted.
     *
     * @param securityRule
     *            An instance of the {@link NeutronSecurityRule} object to be
     *            deleted.
     *
     * @return A HTTP status code to the deletion request.
     */
    @Override
    public int canDeleteNeutronSecurityRule(NeutronSecurityRule securityRule) {
        apiConnector = Activator.apiConnector;
        SecurityGroup virtualSecurityGroup = null;
        String secGroupRuleUUID = securityRule.getSecurityRuleGroupID();
        if (!(securityRule.getSecurityRuleGroupID().contains("-"))) {
            secGroupRuleUUID = Utils.uuidFormater(securityRule.getSecurityRuleGroupID());
        }
        secGroupRuleUUID = UUID.fromString(secGroupRuleUUID).toString();
        String secRuleUUID = securityRule.getSecurityRuleUUID();
        if (!(securityRule.getSecurityRuleUUID().contains("-"))) {
            secRuleUUID = Utils.uuidFormater(securityRule.getSecurityRuleUUID());
        }
        secRuleUUID = UUID.fromString(secRuleUUID).toString();
        try {
            virtualSecurityGroup = (SecurityGroup) apiConnector.findById(SecurityGroup.class, secGroupRuleUUID);
        } catch (IOException e) {
            LOGGER.error("Exception :     " + e);
            return HttpURLConnection.HTTP_INTERNAL_ERROR;
        }
        if (virtualSecurityGroup == null) {
            LOGGER.warn("SecurityGroup does not exists for the specified security group ID");
            return HttpURLConnection.HTTP_FORBIDDEN;
            }
        boolean flag = false;
        if (virtualSecurityGroup.getEntries() != null) {
            if (virtualSecurityGroup.getEntries().getPolicyRule().isEmpty() == false) {
                ListIterator<PolicyRuleType> i = virtualSecurityGroup.getEntries().getPolicyRule().listIterator();
                PolicyRuleType policyRule;
                while (i.hasNext()) {
                    policyRule = (PolicyRuleType) i.next();
                    if (policyRule.getRuleUuid().equalsIgnoreCase(secRuleUUID)) {
                        LOGGER.info("SecurityGroup rule can be deleted...");
                        flag = true;
                    }
                }
            }
        }
        if (flag == false) {
            LOGGER.warn("SecurityGroup rule does not exist for the specified UUID..");
            return HttpURLConnection.HTTP_NOT_FOUND;
        }
        return HttpURLConnection.HTTP_NO_CONTENT;
    }

    /**
     * Invoked to delete a {@link NeutronSecurityRule} and take action after
     * the {@link NeutronSecurityRule} has been deleted.
     *
     * @param securityRule
     *            An instance of deleted {@link NeutronSecurityRule} object.
     */
    @Override
    public void neutronSecurityRuleDeleted(NeutronSecurityRule securityRule) {
        apiConnector = Activator.apiConnector;
        SecurityGroup virtualSecurityGroup = null;
        String secGroupRuleUUID = securityRule.getSecurityRuleGroupID();
        if (!(securityRule.getSecurityRuleGroupID().contains("-"))) {
            secGroupRuleUUID = Utils.uuidFormater(securityRule.getSecurityRuleGroupID());
        }
        secGroupRuleUUID = UUID.fromString(secGroupRuleUUID).toString();
        try {
            virtualSecurityGroup = (SecurityGroup) apiConnector.findById(SecurityGroup.class, secGroupRuleUUID);
        } catch (IOException e) {
            LOGGER.error("Exception :     " + e);
        }
        String secRuleUUID = securityRule.getSecurityRuleUUID();
        if (!(securityRule.getSecurityRuleUUID().contains("-"))) {
            secRuleUUID = Utils.uuidFormater(securityRule.getSecurityRuleUUID());
        }
        secRuleUUID = UUID.fromString(secRuleUUID).toString();
        if (virtualSecurityGroup.getEntries() != null) {
            if (virtualSecurityGroup.getEntries().getPolicyRule().isEmpty() == false) {
                ListIterator<PolicyRuleType> i = virtualSecurityGroup.getEntries().getPolicyRule().listIterator();
                PolicyRuleType policyRule;
                while (i.hasNext()) {
                    policyRule = (PolicyRuleType) i.next();
                    if (policyRule.getRuleUuid().equalsIgnoreCase(secRuleUUID)) {
                        policyRule.clearDstAddresses();
                        policyRule.clearApplication();
                        policyRule.clearDstPorts();
                        policyRule.clearSrcAddresses();
                        policyRule.clearSrcPorts();
                        i.remove();
                        // virtualSecurityGroupRule.getPolicyRule().remove(policyRule);
                        try {
                            virtualSecurityGroup.setEntries(virtualSecurityGroup.getEntries());
                            if (apiConnector.update(virtualSecurityGroup)) {
                                LOGGER.info("SecurityGroup rule deletion for the specified security group is verfied..");
                            } else {
                                LOGGER.warn("SecurityGroup rule deletion for the specified security group is not verfied..");
                            }
                        } catch (IOException e) {
                            LOGGER.warn("SecurityGroupUpdate deletion failed..");
                        }
                    }
                }
            }
        }
    }

    /**
     * Invoked to map the NeutronSecurityRule object properties to the
     * PolicyEntriesType object.
     *
     * @param neutronSecurityGroupRule
     *            An instance of new NeutronSecurityRule object.
     * @param virtualSecurityGroupRule
     *            An instance of new PolicyEntriesType object.
     * @return {@link PolicyEntriesType}
     */
    private PolicyEntriesType mapSecurityGroupRuleProperties(NeutronSecurityRule neutronSecurityGroupRule, PolicyEntriesType virtualSecurityGroupRule) {
        String remoteGroupID = neutronSecurityGroupRule.getSecurityRemoteGroupID();
        String direction = neutronSecurityGroupRule.getSecurityRuleDirection();
        String uuID = neutronSecurityGroupRule.getSecurityRuleUUID();
        String groupID = neutronSecurityGroupRule.getSecurityRuleGroupID();
        int portMax = neutronSecurityGroupRule.getSecurityRulePortMax();
        int portMin = neutronSecurityGroupRule.getSecurityRulePortMin();
        String protocol = neutronSecurityGroupRule.getSecurityRuleProtocol();
        String remoteIpPrefix = neutronSecurityGroupRule.getSecurityRuleRemoteIpPrefix();
        if (remoteGroupID != null) {
            if (!(remoteGroupID.contains("-"))) {
                remoteGroupID = Utils.uuidFormater(remoteGroupID);
            }
            remoteGroupID = UUID.fromString(remoteGroupID).toString();
        }
        if (uuID != null) {
            if (!(uuID.contains("-"))) {
                uuID = Utils.uuidFormater(uuID);
            }
            uuID = UUID.fromString(uuID).toString();
        }
        if (groupID != null) {
            if (!(groupID.contains("-"))) {
                groupID = Utils.uuidFormater(groupID);
            }
            groupID = UUID.fromString(groupID).toString();
        }
        SecurityGroup virtualSecurityGroup = null;
        String securityGroupQualifiedName = null;
        PolicyRuleType virtualPolicyRuleType = new PolicyRuleType();
        ActionListType actionList = null;
        SubnetType subnet = new SubnetType();
        AddressType addType = new AddressType();
        AddressType endPoint = new AddressType();
        AddressType local = new AddressType();
        AddressType remote = new AddressType();
        String[] ipPrefix = null;
        virtualPolicyRuleType.setProtocol(protocol);
        virtualPolicyRuleType.setRuleUuid(uuID);
        if (remoteIpPrefix != null) {
            if (remoteIpPrefix.contains("/")) {
                ipPrefix = remoteIpPrefix.split("/");
            } else {
                throw new IllegalArgumentException("String " + remoteIpPrefix + " not in correct format..");
            }
            if (ipPrefix != null) {
                subnet.setIpPrefix(ipPrefix[0]);
                subnet.setIpPrefixLen(Integer.valueOf(ipPrefix[1]));
                addType.setSubnet(subnet);
                endPoint = addType;
            }
        } else if (remoteGroupID != null) {
            if (!remoteGroupID.isEmpty()) {
                try {
                    virtualSecurityGroup = (SecurityGroup) apiConnector.findById(SecurityGroup.class, remoteGroupID);
                } catch (IOException e) {
                    LOGGER.error("Exception :     " + e);
                }
                Iterator<String> i = virtualSecurityGroup.getQualifiedName().iterator();
                while (i.hasNext()) {
                    if (securityGroupQualifiedName == null) {
                        securityGroupQualifiedName = (String) i.next();
                    } else {
                        securityGroupQualifiedName = securityGroupQualifiedName + ":" + (String) i.next();
                    }
                }
                LOGGER.info("securityGroupQualifiedName  " + securityGroupQualifiedName);
                addType.setSecurityGroup(securityGroupQualifiedName);
                endPoint = addType;
            }
        }
        if (direction.equals("ingress")) {
            local = endPoint;
            remote.setSecurityGroup("local");
            virtualPolicyRuleType.setDirection(">");
        } else if (direction.equals("egress")) {
            remote = endPoint;
            local.setSecurityGroup("local");
            virtualPolicyRuleType.setDirection(">");
        }
        SequenceType ruleSequence = null;
        virtualPolicyRuleType.addSrcAddresses(local);
        virtualPolicyRuleType.addSrcPorts(0, 65535);
        virtualPolicyRuleType.addDstAddresses(remote);
        virtualPolicyRuleType.addDstPorts(portMin, portMax);
        virtualPolicyRuleType.setActionList(actionList);
        virtualPolicyRuleType.setRuleSequence(ruleSequence);
        virtualPolicyRuleType.addApplication(null);
        virtualSecurityGroupRule.addPolicyRule(virtualPolicyRuleType);
        return virtualSecurityGroupRule;
    }

}
