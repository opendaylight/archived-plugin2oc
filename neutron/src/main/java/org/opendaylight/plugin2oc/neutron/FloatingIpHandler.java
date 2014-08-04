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
    static final Logger LOGGER = LoggerFactory.getLogger(PortHandler.class);
    static ApiConnector apiConnector;

    /**
     * Invoked when a floating ip creation is requested to check if the
     * specified floating ip can be created.
     *
     * @param floatingip
     *            An instance of proposed new Neutron Floating ip object.
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
        try {
            return createfloatingIp(fip);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("Exception :   " + e);
            return HttpURLConnection.HTTP_INTERNAL_ERROR;
        }
    }

    /**
     * Invoked when a floating ip creation is requested to create the floating
     * ip
     *
     * @param floatingip
     *            An instance of proposed new Neutron Floating ip object.
     *
     * @return A HTTP status code to the creation request.
     */
    private int createfloatingIp(NeutronFloatingIP neutronFloatingIp) throws IOException {
        String projectUUID = null;
        String floatingPoolNetworkId = null;
        String fipId = neutronFloatingIp.getID();
        String floatingIpaddress = neutronFloatingIp.getFloatingIPAddress();
        try {
            floatingPoolNetworkId = neutronFloatingIp.getFloatingNetworkUUID();
            projectUUID = neutronFloatingIp.getTenantUUID().toString();
            if (!(projectUUID.contains("-"))) {
                projectUUID = uuidFormater(projectUUID);
            }
            projectUUID = UUID.fromString(projectUUID).toString();
        } catch (Exception ex) {
            LOGGER.error("UUID input incorrect", ex);
            return HttpURLConnection.HTTP_INTERNAL_ERROR;
        }
        Project project;
        try {
            project = (Project) apiConnector.findById(Project.class, projectUUID);
            if (project == null) {
                try {
                    Thread.currentThread();
                    Thread.sleep(3000);
                } catch (InterruptedException interruptedException) {
                    LOGGER.error("InterruptedException :    ", interruptedException);
                    return HttpURLConnection.HTTP_INTERNAL_ERROR;
                }
                project = (Project) apiConnector.findById(Project.class, projectUUID);
                if (project == null) {
                    LOGGER.error("Could not find projectUUID...");
                    return HttpURLConnection.HTTP_NOT_FOUND;
                }
            }
            VirtualNetwork virtualNetwork = (VirtualNetwork) apiConnector.findById(VirtualNetwork.class, floatingPoolNetworkId);
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
            FloatingIp floatingIp = new FloatingIp();
            floatingIp.setUuid(fipId);
            floatingIp.setName(fipId);
            floatingIp.setDisplayName(fipId);
            floatingIp.setAddress(floatingIpaddress);
            floatingIp.setParent(floatingIpPool);
            floatingIp.setProject(project);
            if (neutronFloatingIp.getPortUUID() != null) {
                VirtualMachineInterface virtualMachineInterface = (VirtualMachineInterface) apiConnector.findById(VirtualMachineInterface.class,
                        neutronFloatingIp.getPortUUID());
                if (virtualMachineInterface != null) {
                    floatingIp.addVirtualMachineInterface(virtualMachineInterface);
                }
            }
            boolean floatingIpCreaterd = apiConnector.create(floatingIp);
            if (!floatingIpCreaterd) {
                LOGGER.warn("Floating Ip creation failed..");
                return HttpURLConnection.HTTP_INTERNAL_ERROR;
            }
            LOGGER.info("Floating Ip : " + floatingIp.getName() + "  having UUID : " + floatingIp.getUuid() + "  sucessfully created...");
            return HttpURLConnection.HTTP_OK;
        } catch (IOException ioEx) {
            LOGGER.error("Exception : " + ioEx);
            return HttpURLConnection.HTTP_INTERNAL_ERROR;
        }
    }

    /**
     * Invoked to take action after a floating ip has been created.
     *
     * @param floatingip
     *            An instance of proposed new Neutron Floating ip object.
     *
     * @return A HTTP status code to the creation request.
     */
    @Override
    public void neutronFloatingIPCreated(NeutronFloatingIP neutronFloatingIp) {
        FloatingIp floatingIp = null;
        try {
            floatingIp = (FloatingIp) apiConnector.findById(FloatingIp.class, neutronFloatingIp.getFloatingIPUUID());
            if (floatingIp != null) {
                LOGGER.info("Floating Ip creation verified....");
            }
        } catch (Exception ex) {
            LOGGER.error("Exception :    " + ex);
        }

    }

    /**
     * Invoked when a floating ip update is requested to indicate if the
     * specified floating ip can be changed using the specified delta.
     *
     * @param delta
     *            Updates to the floating ip object using patch semantics.
     * @param original
     *            An instance of the Neutron floating ip object to be updated.
     * @return A HTTP status code to the update request.
     */
    @Override
    public int canUpdateFloatingIP(NeutronFloatingIP deltaFloatingIp, NeutronFloatingIP originalFloatingIp) {
        apiConnector = Activator.apiConnector;
        FloatingIp floatingIP = null;
        apiConnector = Activator.apiConnector;
        if (deltaFloatingIp == null || originalFloatingIp == null) {
            LOGGER.error("Neutron Floating Ip can't be null..");
            return HttpURLConnection.HTTP_BAD_REQUEST;
        }
        try {
            floatingIP = (FloatingIp) apiConnector.findById(FloatingIp.class, originalFloatingIp.getFloatingIPUUID());
        } catch (IOException e) {
            LOGGER.error("Exception :     " + e);
            return HttpURLConnection.HTTP_INTERNAL_ERROR;
        }
        if (floatingIP == null) {
            LOGGER.error("No network exists for the specified UUID...");
            return HttpURLConnection.HTTP_FORBIDDEN;
        }
        try {
            return updateFloatingIP(originalFloatingIp.getFloatingIPUUID(), deltaFloatingIp);
        } catch (IOException ex) {
            LOGGER.error("Exception : " + ex);
            return HttpURLConnection.HTTP_INTERNAL_ERROR;
        }

    }

    /**
     * Invoked to update the floating ip
     *
     * @param string
     *            An instance of floating ip UUID.
     * @param delta_floatingip
     *            An instance of delta floating ip.
     *
     * @return A boolean to the update request.
     * @throws IOException
     */
    private int updateFloatingIP(String floatingIpUUID, NeutronFloatingIP deltaFloatingIp) throws IOException {
        FloatingIp floatingIP = (FloatingIp) apiConnector.findById(FloatingIp.class, floatingIpUUID);
        String virtualMachineInterfaceUUID = deltaFloatingIp.getPortUUID();
        if (deltaFloatingIp.getPortUUID() != null) {
            VirtualMachineInterface virtualMachineInterface = (VirtualMachineInterface) apiConnector.findById(VirtualMachineInterface.class,
                    virtualMachineInterfaceUUID);
            if (virtualMachineInterface != null) {
                floatingIP.setVirtualMachineInterface(virtualMachineInterface);
            }
        }
        if (virtualMachineInterfaceUUID == null) {
            floatingIP.clearVirtualMachineInterface();
        }
        boolean floatingIpUpdate = apiConnector.update(floatingIP);
        if (!floatingIpUpdate) {
            LOGGER.warn("Floating Ip Updation failed..");
            return HttpURLConnection.HTTP_INTERNAL_ERROR;
        }
        LOGGER.info("Floating Ip  having UUID : " + floatingIP.getUuid() + "  has been sucessfully updated...");
        return HttpURLConnection.HTTP_OK;
    }

    /**
     * Invoked to take action after a floating ip has been updated.
     *
     * @param floatingIp
     *            An instance of modified Neutron floating ip object.
     */
    @Override
    public void neutronFloatingIPUpdated(NeutronFloatingIP neutronFloatingIp) {
        try {
            FloatingIp floatingIp = (FloatingIp) apiConnector.findById(FloatingIp.class, neutronFloatingIp.getFloatingIPUUID());
            if (neutronFloatingIp.getPortUUID() != null) {
                if (floatingIp.getVirtualMachineInterface().get(0).getUuid().matches(neutronFloatingIp.getPortUUID())) {
                    LOGGER.info("Floating Ip with floating UUID " + neutronFloatingIp.getFloatingIPUUID() + " is Updated successfully.");
                } else {
                    LOGGER.info("Floating Ip Updation failed..");
                }
            } else if (neutronFloatingIp.getPortUUID() == null && floatingIp.getVirtualMachineInterface() == null) {
                LOGGER.info("Floating Ip with floating UUID " + neutronFloatingIp.getFloatingIPUUID() + " is Updated successfully.");
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
        if (neutronFloatingIp == null) {
            LOGGER.error("Neutron Floating Ip can not be null.. ");
            return HttpURLConnection.HTTP_BAD_REQUEST;
        }
        try {
            return deleteFloatingIP(neutronFloatingIp.getFloatingIPUUID());
        } catch (IOException ioEx) {
            LOGGER.error("Exception : " + ioEx);
            return HttpURLConnection.HTTP_INTERNAL_ERROR;
        } catch (Exception ex) {
            LOGGER.error("Exception : " + ex);
            return HttpURLConnection.HTTP_INTERNAL_ERROR;
        }
    }

    /**
     * Invoked to delete the specified Neutron floating ip.
     *
     * @param String
     *            An instance of floating ip UUID.
     *
     * @return A boolean to the delete request.
     * @throws IOException
     */
    private int deleteFloatingIP(String neutronFloatingIp) throws IOException {
        FloatingIp floatingIp = (FloatingIp) apiConnector.findById(FloatingIp.class, neutronFloatingIp);
        if (floatingIp != null) {
            apiConnector.delete(floatingIp);
            LOGGER.info("Floating Ip with UUID :  " + floatingIp.getUuid() + "  has been deleted successfully....");
            return HttpURLConnection.HTTP_OK;
        } else {
            LOGGER.info("No Floating Ip exists with UUID :  " + neutronFloatingIp);
            return HttpURLConnection.HTTP_NOT_FOUND;
        }
    }

    /**
     * Invoked to take action after a floatingIP has been deleted.
     *
     * @param NeutronfloatingIP
     *            An instance of deleted floatingIP Network object.
     */
    @Override
    public void neutronFloatingIPDeleted(NeutronFloatingIP neutronFloatingIp) {
        FloatingIp fip = null;
        try {
            fip = (FloatingIp) apiConnector.findById(FloatingIp.class, neutronFloatingIp.getFloatingIPUUID());
            if (fip == null) {
                LOGGER.info("Floating ip deletion verified....");
            }
        } catch (Exception e) {
            LOGGER.error("Exception :   " + e);
        }

    }

    /**
     * Invoked to format the UUID if UUID is not in correct format.
     *
     * @param String
     *            An instance of UUID string.
     *
     * @return Correctly formated UUID string.
     */
    private String uuidFormater(String uuid) {
        String uuidPattern = null;
        String id1 = uuid.substring(0, 8);
        String id2 = uuid.substring(8, 12);
        String id3 = uuid.substring(12, 16);
        String id4 = uuid.substring(16, 20);
        String id5 = uuid.substring(20, 32);
        uuidPattern = (id1 + "-" + id2 + "-" + id3 + "-" + id4 + "-" + id5);
        return uuidPattern;
    }

}
