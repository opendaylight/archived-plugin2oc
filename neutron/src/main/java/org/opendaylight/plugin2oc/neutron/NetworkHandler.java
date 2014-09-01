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
import net.juniper.contrail.api.types.FloatingIpPool;
import net.juniper.contrail.api.types.Project;
import net.juniper.contrail.api.types.VirtualNetwork;

import org.opendaylight.controller.networkconfig.neutron.INeutronNetworkAware;
import org.opendaylight.controller.networkconfig.neutron.NeutronNetwork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle requests for Neutron Network.
 */
public class NetworkHandler implements INeutronNetworkAware {
    /**
     * Logger instance.
     */
    static final Logger LOGGER = LoggerFactory.getLogger(NetworkHandler.class);
    static ApiConnector apiConnector;

    /**
     * Invoked when a network creation is requested to check if the specified
     * network can be created and then creates the network
     *
     * @param network
     *            An instance of proposed new Neutron Network object.
     *
     * @return A HTTP status code to the creation request.
     */
    @Override
    public int canCreateNetwork(NeutronNetwork network) {
        if (network == null) {
            LOGGER.error("Network object can't be null..");
            return HttpURLConnection.HTTP_BAD_REQUEST;
        }
        LOGGER.debug("Network object " + network);
        apiConnector = Activator.apiConnector;
        if (network.getNetworkUUID() == null || network.getNetworkName() == null || network.getNetworkUUID().equals("")
                || network.getNetworkName().equals("")) {
            LOGGER.error("Network UUID and Network Name can't be null/empty...");
            return HttpURLConnection.HTTP_BAD_REQUEST;
        }
        if (network.getTenantID() == null) {
            LOGGER.error("Network tenant Id can not be null");
            return HttpURLConnection.HTTP_BAD_REQUEST;
        }
        try {
            String networkUUID = network.getNetworkUUID();
            String projectUUID = network.getTenantID();
            try {
                if (!(networkUUID.contains("-"))) {
                    networkUUID = Utils.uuidFormater(networkUUID);
                }
                if (!(projectUUID.contains("-"))) {
                    projectUUID = Utils.uuidFormater(projectUUID);
                }
                boolean isValidNetworkUUID = Utils.isValidHexNumber(networkUUID);
                boolean isValidprojectUUID = Utils.isValidHexNumber(projectUUID);
                if (!isValidNetworkUUID || !isValidprojectUUID) {
                    LOGGER.info("Badly formed Hexadecimal UUID...");
                    return HttpURLConnection.HTTP_BAD_REQUEST;
                }
                projectUUID = UUID.fromString(projectUUID).toString();
                networkUUID = UUID.fromString(networkUUID).toString();
            } catch (Exception ex) {
                LOGGER.error("UUID input incorrect", ex);
                return HttpURLConnection.HTTP_BAD_REQUEST;
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
            VirtualNetwork virtualNetworkById = (VirtualNetwork) apiConnector.findById(VirtualNetwork.class, networkUUID);
            if (virtualNetworkById != null) {
                LOGGER.warn("Network already exists with UUID" + networkUUID);
                return HttpURLConnection.HTTP_FORBIDDEN;
            }
            String virtualNetworkByName = apiConnector.findByName(VirtualNetwork.class, project, network.getNetworkName());
            if (virtualNetworkByName != null) {
                LOGGER.warn("Network already exists with name : " + virtualNetworkByName);
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
     * Invoked to take action after a network has been created.
     *
     * @param network
     *            An instance of new Neutron Network object.
     */
    @Override
    public void neutronNetworkCreated(NeutronNetwork network) {
        try {
            createNetwork(network);
        } catch (IOException ex) {
            LOGGER.warn("Exception  :    " + ex);
        }
        VirtualNetwork virtualNetwork = null;
        try {
            String networkUUID = network.getNetworkUUID();
            if (!(networkUUID.contains("-"))) {
                networkUUID = Utils.uuidFormater(networkUUID);
            }
            networkUUID = UUID.fromString(networkUUID).toString();
            virtualNetwork = (VirtualNetwork) apiConnector.findById(VirtualNetwork.class, networkUUID);
            if (virtualNetwork != null) {
                LOGGER.info("Network creation verified....");
            }
        } catch (Exception e) {
            LOGGER.error("Exception :     " + e);
        }
    }

    /**
     * Invoked to create the specified Neutron Network.
     *
     * @param network
     *            An instance of new Neutron Network object.
     */
    private void createNetwork(NeutronNetwork network) throws IOException {
        VirtualNetwork virtualNetwork = new VirtualNetwork();
        // map neutronNetwork to virtualNetwork
        virtualNetwork = mapNetworkProperties(network, virtualNetwork);
        boolean networkCreated;
        try {
            networkCreated = apiConnector.create(virtualNetwork);
            LOGGER.debug("networkCreated:   " + networkCreated);
            if (!networkCreated) {
                LOGGER.warn("Network creation failed..");
            }
        } catch (IOException ioEx) {
            LOGGER.error("Exception : " + ioEx);
        }
        LOGGER.info("Network : " + virtualNetwork.getName() + "  having UUID : " + virtualNetwork.getUuid() + "  sucessfully created...");
        if (virtualNetwork.getRouterExternal()) {
            FloatingIpPool floatingIpPool = null;
            String fipId = UUID.randomUUID().toString();
            floatingIpPool = new FloatingIpPool();
            floatingIpPool.setName(fipId);
            floatingIpPool.setDisplayName(fipId);
            floatingIpPool.setUuid(fipId);
            floatingIpPool.setParent(virtualNetwork);
            boolean createFloatingIpPool;
            try {
                createFloatingIpPool = apiConnector.create(floatingIpPool);
                if (!createFloatingIpPool) {
                    LOGGER.info("Floating Ip pool creation failed..");
                } else {
                    LOGGER.info("Floating Ip pool created with UUID  : " + floatingIpPool.getUuid());
                }
            } catch (IOException ioEx) {
                LOGGER.error("IOException : " + ioEx);
            }
        }
    }

    /**
     * Invoked to map the NeutronNetwork object properties to the virtualNetwork
     * object.
     *
     * @param neutronNetwork
     *            An instance of new Neutron Network object.
     * @param virtualNetwork
     *            An instance of new virtualNetwork object.
     * @return {@link VirtualNetwork}
     */
    private VirtualNetwork mapNetworkProperties(NeutronNetwork neutronNetwork, VirtualNetwork virtualNetwork) {
        boolean routerExternal = false;
        boolean ishared = false;
        String networkUUID = neutronNetwork.getNetworkUUID();
        String projectUUID = neutronNetwork.getTenantID();
        String networkName = neutronNetwork.getNetworkName();
        try {
            if (!(networkUUID.contains("-"))) {
                networkUUID = Utils.uuidFormater(networkUUID);
            }
            networkUUID = UUID.fromString(networkUUID).toString();
            if (!(projectUUID.contains("-"))) {
                projectUUID = Utils.uuidFormater(projectUUID);
            }
            projectUUID = UUID.fromString(projectUUID).toString();
            Project project = (Project) apiConnector.findById(Project.class, projectUUID);
            virtualNetwork.setParent(project);
        } catch (Exception ex) {
            LOGGER.error("UUID input incorrect", ex);
        }
        if (neutronNetwork.getRouterExternal() != null) {
            routerExternal = neutronNetwork.getRouterExternal();
        }
        if (neutronNetwork.getShared() != null) {
            ishared = neutronNetwork.getShared();
        }
        virtualNetwork.setName(networkName);
        virtualNetwork.setUuid(networkUUID);
        virtualNetwork.setDisplayName(networkName);
        virtualNetwork.setRouterExternal(routerExternal);
        virtualNetwork.setIsShared(ishared);
        return virtualNetwork;
    }

    /**
     * Invoked when a network update is requested to indicate if the specified
     * network can be changed using the specified delta.
     *
     * @param delta
     *            Updates to the network object using patch semantics.
     * @param original
     *            An instance of the Neutron Network object to be updated.
     * @return A HTTP status code to the update request.
     */
    @Override
    public int canUpdateNetwork(NeutronNetwork deltaNetwork, NeutronNetwork originalNetwork) {
        VirtualNetwork virtualnetwork;
        apiConnector = Activator.apiConnector;
        if (deltaNetwork == null || originalNetwork == null) {
            LOGGER.error("Neutron Networks can't be null..");
            return HttpURLConnection.HTTP_BAD_REQUEST;
        }
        String networkUUID = originalNetwork.getNetworkUUID();
        String projectUUID = originalNetwork.getTenantID();
        try {
            if (!(networkUUID.contains("-"))) {
                networkUUID = Utils.uuidFormater(networkUUID);
                networkUUID = UUID.fromString(networkUUID).toString();
            }
            if (!(projectUUID.contains("-"))) {
                projectUUID = Utils.uuidFormater(projectUUID);
                projectUUID = UUID.fromString(projectUUID).toString();
            }
        } catch (Exception ex) {
            LOGGER.error("UUID input incorrect", ex);
        }
        if (("").equals(deltaNetwork.getNetworkName())) {
            LOGGER.error("Neutron Networks name to be update can't be empty..");
            return HttpURLConnection.HTTP_BAD_REQUEST;
        }
        try {
            Project project = (Project) apiConnector.findById(Project.class, projectUUID);
            String virtualNetworkByName = apiConnector.findByName(VirtualNetwork.class, project, deltaNetwork.getNetworkName());
            if (virtualNetworkByName != null) {
                LOGGER.warn("Network with name  " + deltaNetwork.getNetworkName() + "  already exists with UUID : " + virtualNetworkByName);
                return HttpURLConnection.HTTP_FORBIDDEN;
            }
        } catch (IOException ioEx) {
            LOGGER.error("IOException :     " + ioEx);
            return HttpURLConnection.HTTP_INTERNAL_ERROR;
        }
        try {
            virtualnetwork = (VirtualNetwork) apiConnector.findById(VirtualNetwork.class, networkUUID);
        } catch (IOException ex) {
            LOGGER.error("Exception :     " + ex);
            return HttpURLConnection.HTTP_INTERNAL_ERROR;
        }
        if (virtualnetwork == null) {
            LOGGER.error("No network exists for the specified UUID...");
            return HttpURLConnection.HTTP_FORBIDDEN;
        }
        return HttpURLConnection.HTTP_OK;
    }

    /**
     * Invoked to update the network
     *
     * @param delta_network
     *            An instance of Network.
     */
    private void updateNetwork(NeutronNetwork updatedNetwork) throws IOException {
        String networkUUID = updatedNetwork.getNetworkUUID();
        try {
            if (!(networkUUID.contains("-"))) {
                networkUUID = Utils.uuidFormater(networkUUID);
            }
            networkUUID = UUID.fromString(networkUUID).toString();
        } catch (Exception ex) {
            LOGGER.error("UUID input incorrect", ex);
        }
        VirtualNetwork virtualNetwork = (VirtualNetwork) apiConnector.findById(VirtualNetwork.class, networkUUID);
        if (updatedNetwork.getShared() != null) {
            virtualNetwork.setIsShared(updatedNetwork.getShared());
        }
        if (updatedNetwork.getRouterExternal() != null) {
            virtualNetwork.setRouterExternal(updatedNetwork.getRouterExternal());
        }
        virtualNetwork.setDisplayName(updatedNetwork.getNetworkName());
        boolean networkUpdate;
        try {
            networkUpdate = apiConnector.update(virtualNetwork);
            if (!networkUpdate) {
                LOGGER.warn("Network Updation failed..");
            }
        } catch (IOException e) {
            LOGGER.warn("Network Updation failed..");
        }
        LOGGER.info("Network having UUID : " + virtualNetwork.getUuid() + "  has been sucessfully updated...");
        if (updatedNetwork.getRouterExternal()) {
            if (virtualNetwork.getFloatingIpPools() == null) {
                try {
                    FloatingIpPool floatingIpPool = null;
                    String fipId = UUID.randomUUID().toString();
                    floatingIpPool = new FloatingIpPool();
                    floatingIpPool.setName(fipId);
                    floatingIpPool.setDisplayName(fipId);
                    floatingIpPool.setUuid(fipId);
                    floatingIpPool.setParent(virtualNetwork);
                    boolean createFloatingIpPool = apiConnector.create(floatingIpPool);
                    if (!createFloatingIpPool) {
                        LOGGER.info("Floating Ip pool creation failed..");
                    } else {
                        LOGGER.info("Floating Ip pool created with UUID  : " + floatingIpPool.getUuid());
                    }
                } catch (IOException e) {
                    LOGGER.info("Floating Ip pool creation failed..");
                }
            }
        } else {
            if (virtualNetwork.getFloatingIpPools() != null) {
                String floatingPoolId = virtualNetwork.getFloatingIpPools().get(0).getUuid();
                FloatingIpPool floatingIpPool;
                try {
                    floatingIpPool = (FloatingIpPool) apiConnector.findById(FloatingIpPool.class, floatingPoolId);
                    if (floatingIpPool != null) {
                        apiConnector.delete(floatingIpPool);
                    }
                    floatingIpPool = (FloatingIpPool) apiConnector.findById(FloatingIpPool.class, floatingPoolId);
                    if (floatingIpPool == null) {
                        LOGGER.info("Floating Ip pool removed after update network..");
                    } else {
                        LOGGER.info("Floating Ip pool removal failed after update network..");
                    }
                } catch (IOException e) {
                    LOGGER.info("Floating Ip pool is failed to removed after update network..");
                }
            }
        }
    }

    /**
     * Invoked to take action after a network has been updated.
     *
     * @param network
     *            An instance of modified Neutron Network object.
     */
    @Override
    public void neutronNetworkUpdated(NeutronNetwork network) {
        try {
            String networkUUID = network.getNetworkUUID();
            try {
                if (!(networkUUID.contains("-"))) {
                    networkUUID = Utils.uuidFormater(networkUUID);
                }
                networkUUID = UUID.fromString(networkUUID).toString();
            } catch (Exception ex) {
                LOGGER.error("UUID input incorrect", ex);
            }
            updateNetwork(network);
            VirtualNetwork virtualnetwork = (VirtualNetwork) apiConnector.findById(VirtualNetwork.class, networkUUID);
            if (network.getNetworkName().equalsIgnoreCase(virtualnetwork.getDisplayName())
                    && network.getRouterExternal().equals(virtualnetwork.getRouterExternal())) {
                LOGGER.info("Network updatation verified....");
            } else {
                LOGGER.info("Network updatation failed....");
            }
        } catch (Exception e) {
            LOGGER.error("Exception :" + e);
        }
    }

    /**
     * Invoked when a network deletion is requested to indicate if the specified
     * network can be deleted.
     *
     * @param network
     *            An instance of the Neutron Network object to be deleted.
     * @return A HTTP status code to the deletion request.
     */
    @Override
    public int canDeleteNetwork(NeutronNetwork network) {
        apiConnector = Activator.apiConnector;
        VirtualNetwork virtualNetwork = null;
        String networkUUID = network.getNetworkUUID();
        try {
            try {
                if (!(networkUUID.contains("-"))) {
                    networkUUID = Utils.uuidFormater(networkUUID);
                }
                networkUUID = UUID.fromString(networkUUID).toString();
            } catch (Exception ex) {
                LOGGER.error("UUID input incorrect", ex);
                return HttpURLConnection.HTTP_BAD_REQUEST;
            }
            virtualNetwork = (VirtualNetwork) apiConnector.findById(VirtualNetwork.class, networkUUID);
            if (virtualNetwork != null) {
                if (virtualNetwork.getVirtualMachineInterfaceBackRefs() != null || virtualNetwork.getFloatingIpPools() != null
                        || virtualNetwork.getNetworkIpam() != null) {
                    LOGGER.info("Network with UUID :  " + networkUUID
                            + " cannot be deleted as it has subnet(s)/port(s)/FloatingIp Pool(s) associated with it....");
                    return HttpURLConnection.HTTP_FORBIDDEN;
                } else {
                    return HttpURLConnection.HTTP_OK;
                }
            } else {
                LOGGER.info("No Network exists with UUID :  " + networkUUID);
                return HttpURLConnection.HTTP_BAD_REQUEST;
            }
        } catch (Exception e) {
            LOGGER.error("Exception : " + e);
            return HttpURLConnection.HTTP_INTERNAL_ERROR;
        }
    }

    /**
     * Invoked to take action after a network has been deleted.
     *
     * @param network
     *            An instance of deleted Neutron Network object.
     */
    @Override
    public void neutronNetworkDeleted(NeutronNetwork network) {
        VirtualNetwork virtualNetwork = null;
        try {
            String networkUUID = network.getNetworkUUID();
            if (!(networkUUID.contains("-"))) {
                networkUUID = Utils.uuidFormater(networkUUID);
            }
            networkUUID = UUID.fromString(networkUUID).toString();
            virtualNetwork = (VirtualNetwork) apiConnector.findById(VirtualNetwork.class, networkUUID);
            apiConnector.delete(virtualNetwork);
            LOGGER.info("Network with UUID :  " + network.getNetworkUUID() + "  has been deleted successfully....");
            virtualNetwork = (VirtualNetwork) apiConnector.findById(VirtualNetwork.class, networkUUID);
            if (virtualNetwork == null) {
                LOGGER.info("Network deletion verified....");
            } else {
                LOGGER.info("Network deletion failed....");
            }
        } catch (Exception e) {
            LOGGER.error("Exception :   " + e);
        }
    }

}