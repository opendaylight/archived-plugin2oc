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
import net.juniper.contrail.api.types.FloatingIp;
import net.juniper.contrail.api.types.FloatingIpPool;
import net.juniper.contrail.api.types.Project;
import net.juniper.contrail.api.types.VirtualMachineInterface;
import net.juniper.contrail.api.types.VirtualNetwork;

import org.opendaylight.controller.networkconfig.neutron.INeutronFloatingIPAware;
import org.opendaylight.controller.networkconfig.neutron.NeutronFloatingIP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle requests for Neutron Floating IP.
 */
public class FloatingIpHandler implements INeutronFloatingIPAware {
    /**
     * Logger instance.
     */
    static final Logger LOGGER = LoggerFactory.getLogger(FloatingIpHandler.class);
    static ApiConnector apiConnector;

    /**
     * Invoked when a floating IP creation is requested to check if the
     * specified floating IP can be created.
     *
     * @param floatingip
     *            An instance of proposed new Neutron Floating IP object.
     *
     * @return A HTTP status code to the creation request.
     */
    @Override
    public int canCreateFloatingIP(NeutronFloatingIP fip) {
        apiConnector = Activator.apiConnector;
        if (fip == null) {
            LOGGER.error("Neutron Floating Ip can not be null ");
            return HttpURLConnection.HTTP_BAD_REQUEST;
        }
        if (("").equals(fip.getFloatingIPUUID())) {
            LOGGER.error("Floating Ip UUID can not be null ");
            return HttpURLConnection.HTTP_BAD_REQUEST;
        }
        if (fip.getTenantUUID() == null || ("").equals(fip.getTenantUUID())) {
            LOGGER.error("Floating Ip tenant Id can not be null");
            return HttpURLConnection.HTTP_BAD_REQUEST;
        }
        if (fip.getFloatingIPAddress() == null) {
            LOGGER.error(" Floating Ip address can not be null");
            return HttpURLConnection.HTTP_BAD_REQUEST;
        }
        String fipUUID = fip.getFloatingIPUUID();
        String projectUUID = fip.getTenantUUID();
        String floatingNetworkUUID = fip.getFloatingNetworkUUID();
        try {
            if (!(fipUUID.contains("-"))) {
                fipUUID = Utils.uuidFormater(fipUUID);
            }
            if (!(projectUUID.contains("-"))) {
                projectUUID = Utils.uuidFormater(projectUUID);
            }
            if (!(floatingNetworkUUID.contains("-"))) {
                floatingNetworkUUID = Utils.uuidFormater(floatingNetworkUUID);
            }
            boolean isValidFloatingIPUUID = Utils.isValidHexNumber(fipUUID);
            boolean isValidFloatingNetworkUUID = Utils.isValidHexNumber(floatingNetworkUUID);
            boolean isValidprojectUUID = Utils.isValidHexNumber(projectUUID);
            if (!isValidFloatingIPUUID || !isValidFloatingNetworkUUID || !isValidprojectUUID) {
                LOGGER.info("Badly formed Hexadecimal UUID...");
                return HttpURLConnection.HTTP_BAD_REQUEST;
            }
            fipUUID = UUID.fromString(fipUUID).toString();
            projectUUID = UUID.fromString(projectUUID).toString();
            floatingNetworkUUID = UUID.fromString(floatingNetworkUUID).toString();
        } catch (Exception ex) {
            LOGGER.error("UUID input incorrect", ex);
            return HttpURLConnection.HTTP_BAD_REQUEST;
        }
        try {
            FloatingIp floatingIpByID = (FloatingIp) apiConnector.findById(FloatingIp.class, fipUUID);
            if (floatingIpByID != null) {
                LOGGER.error("Floating IP already exists...");
                return HttpURLConnection.HTTP_NOT_FOUND;
            }
            Project project = (Project) apiConnector.findById(Project.class, projectUUID);
            if (project == null) {
                try {
                    Thread.currentThread();
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    LOGGER.error("InterruptedException :    ", e);
                    return HttpURLConnection.HTTP_BAD_REQUEST;
                }
                project = (Project) apiConnector.findById(Project.class, projectUUID);
                if (project == null) {
                    LOGGER.error("Could not find projectUUID...");
                    return HttpURLConnection.HTTP_NOT_FOUND;
                }
            }
            VirtualNetwork virtualNetwork = (VirtualNetwork) apiConnector.findById(VirtualNetwork.class, floatingNetworkUUID);
            if (virtualNetwork == null) {
                LOGGER.error("Could not find Virtual network...");
                return HttpURLConnection.HTTP_NOT_FOUND;
            }
            String floatingPoolId = virtualNetwork.getFloatingIpPools().get(0).getUuid();
            FloatingIpPool floatingIpPool = (FloatingIpPool) apiConnector.findById(FloatingIpPool.class, floatingPoolId);
            if (floatingIpPool == null) {
                LOGGER.error("Could not find Floating ip pool...");

                return HttpURLConnection.HTTP_NOT_FOUND;
            }
        } catch (IOException ie) {
            LOGGER.error("IOException :   " + ie);
            return HttpURLConnection.HTTP_INTERNAL_ERROR;
        } catch (Exception e) {
            LOGGER.error("Exception :   " + e);
            return HttpURLConnection.HTTP_INTERNAL_ERROR;
        }
        return HttpURLConnection.HTTP_OK;
    }

    /**
     * Invoked when a floating IP creation is requested to create the floating
     * IP
     *
     * @param floatingip
     *            An instance of proposed new Neutron Floating IP object.
     */
    private void createfloatingIp(NeutronFloatingIP neutronFloatingIp) throws IOException {
        String fipUUID = neutronFloatingIp.getFloatingIPUUID();
        String projectUUID = neutronFloatingIp.getTenantUUID();
        String floatingNetworkUUID = neutronFloatingIp.getFloatingNetworkUUID();
        String floatingIpaddress = neutronFloatingIp.getFloatingIPAddress();
        String fipPortUUID = neutronFloatingIp.getPortUUID();
        try {
            if (!(fipUUID.contains("-"))) {
                fipUUID = Utils.uuidFormater(fipUUID);
            }
            fipUUID = UUID.fromString(fipUUID).toString();
            if (!(projectUUID.contains("-"))) {
                projectUUID = Utils.uuidFormater(projectUUID);
            }
            projectUUID = UUID.fromString(projectUUID).toString();
            if (!(floatingNetworkUUID.contains("-"))) {
                floatingNetworkUUID = Utils.uuidFormater(floatingNetworkUUID);
            }
            floatingNetworkUUID = UUID.fromString(floatingNetworkUUID).toString();
            if (neutronFloatingIp.getPortUUID() != null) {
                if (!(fipPortUUID.contains("-"))) {
                    fipPortUUID = Utils.uuidFormater(fipPortUUID);
                }
                fipPortUUID = UUID.fromString(fipPortUUID).toString();
            }
        } catch (Exception ex) {
            LOGGER.error("UUID input incorrect", ex);
        }
        try {
            Project project = (Project) apiConnector.findById(Project.class, projectUUID);
            VirtualNetwork virtualNetwork = (VirtualNetwork) apiConnector.findById(VirtualNetwork.class, floatingNetworkUUID);
            String floatingPoolId = virtualNetwork.getFloatingIpPools().get(0).getUuid();
            FloatingIpPool floatingIpPool = (FloatingIpPool) apiConnector.findById(FloatingIpPool.class, floatingPoolId);
            FloatingIp floatingIp = new FloatingIp();
            floatingIp.setUuid(fipUUID);
            floatingIp.setName(fipUUID);
            floatingIp.setDisplayName(fipUUID);
            floatingIp.setAddress(floatingIpaddress);
            floatingIp.setParent(floatingIpPool);
            floatingIp.setProject(project);
            if (fipPortUUID != null) {
                VirtualMachineInterface virtualMachineInterface = (VirtualMachineInterface) apiConnector.findById(VirtualMachineInterface.class, fipPortUUID);
                if (virtualMachineInterface != null) {
                    floatingIp.addVirtualMachineInterface(virtualMachineInterface);
                }
            }
            boolean floatingIpCreaterd = apiConnector.create(floatingIp);
            if (!floatingIpCreaterd) {
                LOGGER.warn("Floating Ip creation failed..");
            }
            LOGGER.info("Floating Ip : " + floatingIp.getName() + "  having UUID : " + floatingIp.getUuid() + "  sucessfully created...");
        } catch (IOException ioEx) {
            LOGGER.error("Exception : " + ioEx);
        }
    }

    /**
     * Invoked to create a Floating IP and take action after the floating IP has
     * been created.
     *
     * @param floatingIP
     *            An instance of proposed new Neutron Floating IP object.
     *
     */
    @Override
    public void neutronFloatingIPCreated(NeutronFloatingIP neutronFloatingIp) {
        try {
            createfloatingIp(neutronFloatingIp);
        } catch (Exception ex) {
            LOGGER.error("Exception :   " + ex);
        }
        try {
            String fipUUID = neutronFloatingIp.getFloatingIPUUID();
            if (!(fipUUID.contains("-"))) {
                fipUUID = Utils.uuidFormater(fipUUID);
            }
            fipUUID = UUID.fromString(fipUUID).toString();
            FloatingIp floatingIp = (FloatingIp) apiConnector.findById(FloatingIp.class, fipUUID);
            if (floatingIp != null) {
                LOGGER.info("Floating Ip creation verified....");
            } else {
                LOGGER.error("Floating Ip creation failed....");
            }
        } catch (Exception ex) {
            LOGGER.error("Exception :    " + ex);
        }
    }

    /**
     * Invoked when a floating IP update is requested to indicate if the
     * specified floating IP can be changed using the specified delta.
     *
     * @param delta
     *            Updates to the floating IP object using patch semantics.
     * @param original
     *            An instance of the Neutron floating IP object to be updated.
     * @return A HTTP status code to the update request.
     */
    @Override
    public int canUpdateFloatingIP(NeutronFloatingIP deltaFloatingIp, NeutronFloatingIP originalFloatingIp) {
        apiConnector = Activator.apiConnector;
        if (deltaFloatingIp == null || originalFloatingIp == null) {
            LOGGER.error("Neutron Floating Ip can't be null..");
            return HttpURLConnection.HTTP_BAD_REQUEST;
        }
        FloatingIp floatingIp = null;
        String fipUUID = originalFloatingIp.getFloatingIPUUID();

        try {
            if (!(fipUUID.contains("-"))) {
                fipUUID = Utils.uuidFormater(fipUUID);
            }
            fipUUID = UUID.fromString(fipUUID).toString();
        } catch (Exception ex) {
            LOGGER.error("UUID input incorrect", ex);
            return HttpURLConnection.HTTP_BAD_REQUEST;
        }
        try {
            floatingIp = (FloatingIp) apiConnector.findById(FloatingIp.class, fipUUID);
            if (floatingIp == null) {
                LOGGER.error("No floating IP exists for the specified UUID...");
                return HttpURLConnection.HTTP_NOT_FOUND;
            }
        } catch (IOException ex) {
            LOGGER.error("Exception : " + ex);
            return HttpURLConnection.HTTP_INTERNAL_ERROR;
        }
        return HttpURLConnection.HTTP_OK;
    }

    /**
     * Invoked to update the floating IP
     *
     * @param delta_floatingip
     *            An instance of delta floating IP.
     *
     * @throws IOException
     */
    private void updateFloatingIP(NeutronFloatingIP neutronFloatingIp) throws IOException {
        String fipUUID = neutronFloatingIp.getFloatingIPUUID();
        String fipPortUUID = neutronFloatingIp.getPortUUID();
        try {
            if (!(fipUUID.contains("-"))) {
                fipUUID = Utils.uuidFormater(fipUUID);
            }
            fipUUID = UUID.fromString(fipUUID).toString();
            if (neutronFloatingIp.getPortUUID() != null) {
                if (!(fipPortUUID.contains("-"))) {
                    fipPortUUID = Utils.uuidFormater(fipPortUUID);
                }
                fipPortUUID = UUID.fromString(fipPortUUID).toString();
            }
        } catch (Exception ex) {
            LOGGER.error("UUID input incorrect", ex);
        }
        FloatingIp floatingIP = (FloatingIp) apiConnector.findById(FloatingIp.class, fipUUID);
        if (fipPortUUID != null) {
            VirtualMachineInterface virtualMachineInterface = (VirtualMachineInterface) apiConnector.findById(VirtualMachineInterface.class,
                    fipPortUUID);
            if (virtualMachineInterface != null) {
                floatingIP.setVirtualMachineInterface(virtualMachineInterface);
            }
        }
        if (fipPortUUID == null) {
            floatingIP.clearVirtualMachineInterface();
        }
        boolean floatingIpUpdate = apiConnector.update(floatingIP);
        if (!floatingIpUpdate) {
            LOGGER.warn("Floating Ip Updation failed..");
        }
        LOGGER.info("Floating Ip  having UUID : " + floatingIP.getUuid() + "  has been sucessfully updated...");
    }

    /**
     * Invoked to take action after a floating IP has been updated.
     *
     * @param floatingIp
     *            An instance of modified Neutron floating IP object.
     */
    @Override
    public void neutronFloatingIPUpdated(NeutronFloatingIP updatedFloatingIp) {
        try {
            updateFloatingIP(updatedFloatingIp);
            String fipUUID = updatedFloatingIp.getFloatingIPUUID();
            String fipPortUUID = updatedFloatingIp.getPortUUID();
            if (!(fipUUID.contains("-"))) {
                fipUUID = Utils.uuidFormater(fipUUID);
            }
            fipUUID = UUID.fromString(fipUUID).toString();
            if (fipPortUUID != null) {
                if (!(fipPortUUID.contains("-"))) {
                    fipPortUUID = Utils.uuidFormater(fipPortUUID);
                }
                fipPortUUID = UUID.fromString(fipPortUUID).toString();
            }
            FloatingIp floatingIp = (FloatingIp) apiConnector.findById(FloatingIp.class, fipUUID);
            if (fipPortUUID != null) {
                if (floatingIp.getVirtualMachineInterface().get(0).getUuid().matches(fipPortUUID)) {
                    LOGGER.info("Floating Ip with floating UUID " + fipUUID + " is Updated successfully.");
                } else {
                    LOGGER.info("Floating Ip Updation failed..");
                }
            } else if (fipPortUUID == null && floatingIp.getVirtualMachineInterface() == null) {
                LOGGER.info("Floating Ip with floating UUID " + fipUUID + " is Updated successfully.");
            } else {
                LOGGER.info("Floating Ip Updation failed..");
            }
        } catch (Exception e) {
            LOGGER.error("Exception :" + e);
        }
    }

    /**
     * Invoked when a floating IP deletion is requested to indicate if the
     * specified floating IP can be deleted.
     *
     * @param NeutronFloatingIP
     *            An instance of the Neutron {@link FloatingIp} object to be
     *            deleted.
     * @return A HTTP status code to the deletion request.
     */
    @Override
    public int canDeleteFloatingIP(NeutronFloatingIP neutronFloatingIp) {
        apiConnector = Activator.apiConnector;
        String fipUUID = neutronFloatingIp.getFloatingIPUUID();
        try {
            if (!(fipUUID.contains("-"))) {
                fipUUID = Utils.uuidFormater(fipUUID);
            }
            fipUUID = UUID.fromString(fipUUID).toString();
            FloatingIp floatingIp = (FloatingIp) apiConnector.findById(FloatingIp.class, fipUUID);
            if (floatingIp == null) {
                LOGGER.info("No Floating Ip exists with UUID :  " + fipUUID);
                return HttpURLConnection.HTTP_NOT_FOUND;
            }
            return HttpURLConnection.HTTP_OK;
        } catch (IOException ioEx) {
            LOGGER.error("Exception : " + ioEx);
            return HttpURLConnection.HTTP_INTERNAL_ERROR;
        } catch (Exception ex) {
            LOGGER.error("Exception : " + ex);
            return HttpURLConnection.HTTP_INTERNAL_ERROR;
        }
    }

    /**
     * Invoked to take action after a floatingIP has been deleted.
     *
     * @param NeutronfloatingIP
     *            An instance of deleted floatingIP object.
     */
    @Override
    public void neutronFloatingIPDeleted(NeutronFloatingIP neutronFloatingIp) {
        String fipUUID = neutronFloatingIp.getFloatingIPUUID();
        FloatingIp floatingIp = null;
        try {
            if (!(fipUUID.contains("-"))) {
                fipUUID = Utils.uuidFormater(fipUUID);
            }
            fipUUID = UUID.fromString(fipUUID).toString();
            floatingIp = (FloatingIp) apiConnector.findById(FloatingIp.class, fipUUID);
            apiConnector.delete(floatingIp);
            floatingIp = (FloatingIp) apiConnector.findById(FloatingIp.class, fipUUID);
            if (floatingIp == null) {
                LOGGER.info("Floating ip deletion verified....");
            } else {
                LOGGER.info("Floating ip deletion failed....");
            }
        } catch (IOException ioEx) {
            LOGGER.error("Exception : " + ioEx);
        } catch (Exception ex) {
            LOGGER.error("Exception :   " + ex);
        }
    }

}