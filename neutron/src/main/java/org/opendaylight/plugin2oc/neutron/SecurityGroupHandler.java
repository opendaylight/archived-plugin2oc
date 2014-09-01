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
import java.util.UUID;

import net.juniper.contrail.api.ApiConnector;
import net.juniper.contrail.api.types.Project;
import net.juniper.contrail.api.types.SecurityGroup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opendaylight.controller.networkconfig.neutron.INeutronSecurityGroupAware;
import org.opendaylight.controller.networkconfig.neutron.NeutronSecurityGroup;

/**
 * Handle requests for Security Group.
 */
public class SecurityGroupHandler implements INeutronSecurityGroupAware {
    /**
     * Logger instance.
     */
    static final Logger LOGGER = LoggerFactory.getLogger(SecurityGroupHandler.class);
    static ApiConnector apiConnector;

    /**
     * Invoked when a security group creation is requested to check if the
     * specified security group can be created
     *
     * @param securityGroup
     *            An instance of proposed new Neutron SecurityGroup object.
     *
     * @return A HTTP status code to the creation request.
     */
    @Override
    public int canCreateNeutronSecurityGroup(NeutronSecurityGroup neutronSecurityGroup) {
        apiConnector = Activator.apiConnector;
        if (neutronSecurityGroup == null) {
            LOGGER.error("SecurityGroup object can't be null..");
            return HttpURLConnection.HTTP_BAD_REQUEST;
        }
        if (neutronSecurityGroup.getSecurityGroupName() == null || neutronSecurityGroup.getSecurityGroupName().equals("")) {
            LOGGER.error("SecurityGroup Name can't be null/empty...");
            return HttpURLConnection.HTTP_BAD_REQUEST;
        }
        if (neutronSecurityGroup.getSecurityGroupDescription() == null || neutronSecurityGroup.getSecurityGroupDescription().equals("")) {
            LOGGER.error("SecurityGroup Description can't be null/empty...");
            return HttpURLConnection.HTTP_BAD_REQUEST;
        }
        if (neutronSecurityGroup.getSecurityGroupTenantID() == null) {
            LOGGER.error("SecurityGroup TenantID can't be null...");
            return HttpURLConnection.HTTP_BAD_REQUEST;
        }
        LOGGER.debug("SecurityGroup object " + neutronSecurityGroup);
        try {
            String secGroupUUID = neutronSecurityGroup.getSecurityGroupUUID();
            String projectID = neutronSecurityGroup.getSecurityGroupTenantID();
            try {
                if (!(secGroupUUID.contains("-"))) {
                    secGroupUUID = Utils.uuidFormater(secGroupUUID);
                }
                if (!(projectID.contains("-"))) {
                    projectID = Utils.uuidFormater(projectID);
                }
                boolean isValidSecurityGroupUUID = Utils.isValidHexNumber(secGroupUUID);
                boolean isValidprojectUUID = Utils.isValidHexNumber(projectID);
                if (!isValidSecurityGroupUUID || !isValidprojectUUID) {
                    LOGGER.info("Badly formed Hexadecimal UUID...");
                    return HttpURLConnection.HTTP_BAD_REQUEST;
                }
                secGroupUUID = UUID.fromString(secGroupUUID).toString();
                projectID = UUID.fromString(projectID).toString();
            } catch (Exception ex) {
                LOGGER.error("UUID input incorrect", ex);
                return HttpURLConnection.HTTP_BAD_REQUEST;
            }
            SecurityGroup securityGroup = (SecurityGroup) apiConnector.findById(SecurityGroup.class, secGroupUUID);
            if (securityGroup != null) {
                LOGGER.warn("SecurityGroup already exists..");
                return HttpURLConnection.HTTP_FORBIDDEN;
            }
            Project project = (Project) apiConnector.findById(Project.class, projectID);
            if (project == null) {
                try {
                    Thread.currentThread();
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    LOGGER.error("InterruptedException :    ", e);
                    return HttpURLConnection.HTTP_BAD_REQUEST;
                }
                project = (Project) apiConnector.findById(Project.class, projectID);
                if (project == null) {
                    LOGGER.error("Could not find projectUUID...");
                    return HttpURLConnection.HTTP_NOT_FOUND;
                }
            }
            String securityGroupByName = apiConnector.findByName(SecurityGroup.class, project, neutronSecurityGroup.getSecurityGroupName());
            if (securityGroupByName != null) {
                LOGGER.warn("SecurityGroup already exists with UUID : " + securityGroupByName);
                return HttpURLConnection.HTTP_FORBIDDEN;
            }
            return HttpURLConnection.HTTP_OK;
        } catch (IOException ie) {
            LOGGER.error("IOException :   " + ie);
            return HttpURLConnection.HTTP_INTERNAL_ERROR;
        } catch (Exception e) {
            LOGGER.error("Exception :   " + e);
            return HttpURLConnection.HTTP_INTERNAL_ERROR;
        }
    }

    /**
     * Invoked to create a Security Group and take action after the Security
     * Group has been created.
     *
     * @param neutronSecurityGroup
     *            An instance of new {@link NeutronSecurityGroup} object.
     */
    @Override
    public void neutronSecurityGroupCreated(NeutronSecurityGroup neutronSecurityGroup) {
        try {
            String secGroupUUID = neutronSecurityGroup.getSecurityGroupUUID();
            if (!(secGroupUUID.contains("-"))) {
                secGroupUUID = Utils.uuidFormater(secGroupUUID);
            }
            createSecurityGroup(neutronSecurityGroup);
            SecurityGroup securityGroup = (SecurityGroup) apiConnector.findById(SecurityGroup.class, secGroupUUID);
            if (securityGroup != null) {
                LOGGER.info("SecurityGroup creation verified....");
            } else {
                LOGGER.info("SecurityGroup creation failed....");
            }
        } catch (Exception e) {
            LOGGER.error("Exception :     " + e);
        }
    }

    /**
     * Invoked to create the specified Neutron Security Group.
     *
     * @param security
     *            Group An instance of new NeutronSecurityGroup object.
     */
    private void createSecurityGroup(NeutronSecurityGroup neutronSecurityGroup) throws IOException {
        // map neutronSecurityGroup to securityGroup
        SecurityGroup securityGroup = mapSecurityGroupProperties(neutronSecurityGroup);
        boolean securityGroupCreated;
        try {
            securityGroupCreated = apiConnector.create(securityGroup);
            LOGGER.debug("SecurityGroupCreated:   " + securityGroupCreated);
            if (!securityGroupCreated) {
                LOGGER.warn("SecurityGroup creation failed..");
            }
        } catch (IOException ioEx) {
            LOGGER.error("Exception : " + ioEx);
        }
        LOGGER.info("SecurityGroup : " + securityGroup.getName() + "  having UUID : " + securityGroup.getUuid() + "  sucessfully created...");
    }

    /**
     * Invoked to map the NeutronSecurityGroup object properties to the neutron
     * security Group object.
     *
     * @param neutronSecurityGroup
     *            An instance of new Neutron SecurityGroup object.
     * @param securityGroup
     *            An instance of new securityGroup object.
     * @return {@link securityGroup}
     */
    private SecurityGroup mapSecurityGroupProperties(NeutronSecurityGroup neutronSecurityGroup) {
        SecurityGroup securityGroup = new SecurityGroup();
        try {
            String secGroupUUID = neutronSecurityGroup.getSecurityGroupUUID();
            String projectUUID = neutronSecurityGroup.getSecurityGroupTenantID();
            if (!(secGroupUUID.contains("-"))) {
                secGroupUUID = Utils.uuidFormater(secGroupUUID);
            }
            secGroupUUID = UUID.fromString(secGroupUUID).toString();
            if (!(projectUUID.contains("-"))) {
                projectUUID = Utils.uuidFormater(projectUUID);
            }
            projectUUID = UUID.fromString(projectUUID).toString();
            Project project = (Project) apiConnector.findById(Project.class, projectUUID);
            securityGroup.setParent(project);
            securityGroup.setName(neutronSecurityGroup.getSecurityGroupName());
            securityGroup.setDisplayName(neutronSecurityGroup.getSecurityGroupName());
            securityGroup.setUuid(secGroupUUID);
        } catch (Exception ex) {
            LOGGER.error("Exception  :  ", ex);
        }
        return securityGroup;
    }

    /**
     * Invoked when a security group update is requested to indicate if the
     * specified Security Group can be changed using the specified delta.
     *
     * @param deltaSecurityGroup
     *            Updates to the {@link NeutronSecurityGroup} object using patch
     *            semantics.
     * @param originalSecurityGroup
     *            An instance of the {@link NeutronSecurityGroup} object to be
     *            updated.
     * @return A HTTP status code to the update request.
     */
    @Override
    public int canUpdateNeutronSecurityGroup(NeutronSecurityGroup deltaSecurityGroup, NeutronSecurityGroup originalSecurityGroup) {
        apiConnector = Activator.apiConnector;
        if (deltaSecurityGroup == null || originalSecurityGroup == null) {
            LOGGER.error("Neutron SecurityGroup can't be null..");
            return HttpURLConnection.HTTP_BAD_REQUEST;
        }
        String secGroupUUID = originalSecurityGroup.getSecurityGroupUUID();
        try {
            if (!(secGroupUUID.contains("-"))) {
                secGroupUUID = Utils.uuidFormater(secGroupUUID);
            }
            secGroupUUID = UUID.fromString(secGroupUUID).toString();
        } catch (Exception ex) {
            LOGGER.error("UUID input incorrect", ex);
            return HttpURLConnection.HTTP_BAD_REQUEST;
        }
        try {
            SecurityGroup securityGroup = (SecurityGroup) apiConnector.findById(SecurityGroup.class, secGroupUUID);
            if (securityGroup == null) {
                LOGGER.warn("SecurityGroup does not exist for the specified UUID..");
                return HttpURLConnection.HTTP_NOT_FOUND;
            }
            return HttpURLConnection.HTTP_OK;
        } catch (IOException ie) {
            LOGGER.error("IOException:     " + ie);
            return HttpURLConnection.HTTP_INTERNAL_ERROR;
        } catch (Exception e) {
            LOGGER.error("Exception:     " + e);
            return HttpURLConnection.HTTP_INTERNAL_ERROR;
        }
    }

    /**
     * Invoked to update a Security Group and take action after the Security
     * group has been updated.
     *
     * @param updatedSecurityGroup
     *            An instance of modified {@link NeutronSecurityGroup} object.
     */
    @Override
    public void neutronSecurityGroupUpdated(NeutronSecurityGroup updatedSecurityGroup) {
        try {
            String secGroupUUID = updatedSecurityGroup.getSecurityGroupUUID();
            if (!(secGroupUUID.contains("-"))) {
                secGroupUUID = Utils.uuidFormater(secGroupUUID);
            }
            secGroupUUID = UUID.fromString(secGroupUUID).toString();
            updateSecurityGroup(updatedSecurityGroup);
            SecurityGroup securityGroup = (SecurityGroup) apiConnector.findById(SecurityGroup.class, secGroupUUID);
            if (securityGroup.getDisplayName().matches(updatedSecurityGroup.getSecurityGroupName())) {
                LOGGER.info("SecurityGroup updatation verified....");
            } else {
                LOGGER.info("SecurityGroup updatation failed....");
            }
        } catch (Exception ex) {
            LOGGER.error("Exception :" + ex);
        }
    }

    /**
     * Invoked to update the securityGroup
     *
     * @param neutronSecurityGroup
     *            An instance of securityGroup.
     */
    private void updateSecurityGroup(NeutronSecurityGroup neutronSecurityGroup) throws IOException {
        String secGroupUUID = neutronSecurityGroup.getSecurityGroupUUID();
        try {
            if (!(secGroupUUID.contains("-"))) {
                secGroupUUID = Utils.uuidFormater(secGroupUUID);
            }
            secGroupUUID = UUID.fromString(secGroupUUID).toString();
            SecurityGroup securityGroup = (SecurityGroup) apiConnector.findById(SecurityGroup.class, secGroupUUID);
            securityGroup.setDisplayName(neutronSecurityGroup.getSecurityGroupName());
            boolean securityGroupUpdate;
            securityGroupUpdate = apiConnector.update(securityGroup);
            if (!securityGroupUpdate) {
                LOGGER.warn("SecurityGroup Updation failed..");
            } else {
                LOGGER.info("SecurityGroup having UUID : " + securityGroup.getUuid() + "  has been sucessfully updated...");
            }
        } catch (IOException ioEx) {
            LOGGER.error("Exception  :  " + ioEx);
        } catch (Exception ex) {
            LOGGER.warn("Exception  :  " + ex);
        }
    }

    /**
     * Invoked when a security group deletion is requested to indicate if the
     * specified neutron security group can be deleted.
     *
     * @param neutronSecurityGroup
     *            An instance of the {@link NeutronSecurityGroup} object to be
     *            deleted.
     *
     * @return A HTTP status code to the deletion request.
     */
    @Override
    public int canDeleteNeutronSecurityGroup(NeutronSecurityGroup neutronSecurityGroup) {
        apiConnector = Activator.apiConnector;
        String secGroupUUID = neutronSecurityGroup.getSecurityGroupUUID();
        try {
            if (!(secGroupUUID.contains("-"))) {
                secGroupUUID = Utils.uuidFormater(secGroupUUID);
            }
            secGroupUUID = UUID.fromString(secGroupUUID).toString();
        } catch (Exception ex) {
            LOGGER.error("UUID input incorrect", ex);
            return HttpURLConnection.HTTP_BAD_REQUEST;
        }
        try {
            SecurityGroup securityGroup = (SecurityGroup) apiConnector.findById(SecurityGroup.class, secGroupUUID);
            if (securityGroup == null) {
                LOGGER.warn("SecurityGroup does not exist for the specified UUID..");
                return HttpURLConnection.HTTP_NOT_FOUND;
            } else {
                if (securityGroup.getVirtualMachineInterfaceBackRefs() != null) {
                    LOGGER.info("SecurityGroup with UUID :  " + secGroupUUID + " cannot be deleted as it has port(s) associated with it....");
                    return HttpURLConnection.HTTP_FORBIDDEN;
                }
                return HttpURLConnection.HTTP_OK;
            }
        } catch (Exception e) {
            LOGGER.error("Exception : " + e);
            return HttpURLConnection.HTTP_INTERNAL_ERROR;
        }
    }

    /**
     * Invoked to delete a {@link NeutronSecurityGroup} and take action after
     * the {@link NeutronSecurityGroup} has been deleted.
     *
     * @param neutronSecurityGroup
     *            An instance of deleted {@link NeutronSecurityGroup} object.
     */
    @Override
    public void neutronSecurityGroupDeleted(NeutronSecurityGroup neutronSecurityGroup) {
        String secGroupUUID = neutronSecurityGroup.getSecurityGroupUUID();
        try {
            if (!(secGroupUUID.contains("-"))) {
                secGroupUUID = Utils.uuidFormater(secGroupUUID);
            }
            secGroupUUID = UUID.fromString(secGroupUUID).toString();
            SecurityGroup securityGroup = (SecurityGroup) apiConnector.findById(SecurityGroup.class, secGroupUUID);
            apiConnector.delete(securityGroup);
            LOGGER.info("SecurityGroup with UUID :  " + secGroupUUID + "  has been deleted successfully....");
            securityGroup = (SecurityGroup) apiConnector.findById(SecurityGroup.class, secGroupUUID);
            if (securityGroup == null) {
                LOGGER.info("SecurityGroup deletion verified....");
            } else {
                LOGGER.info("SecurityGroup deletion failed....");
            }
        } catch (Exception e) {
            LOGGER.error("Exception :   " + e);
        }
    }

}
