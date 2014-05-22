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
import java.util.List;
import java.util.UUID;

import net.juniper.contrail.api.ApiConnector;
import net.juniper.contrail.api.ApiPropertyBase;
import net.juniper.contrail.api.ObjectReference;
import net.juniper.contrail.api.types.InstanceIp;
import net.juniper.contrail.api.types.MacAddressesType;
import net.juniper.contrail.api.types.Project;
import net.juniper.contrail.api.types.VirtualMachine;
import net.juniper.contrail.api.types.VirtualMachineInterface;
import net.juniper.contrail.api.types.VirtualNetwork;
import net.juniper.contrail.api.types.VnSubnetsType;

import org.opendaylight.controller.networkconfig.neutron.INeutronPortAware;
import org.opendaylight.controller.networkconfig.neutron.INeutronSubnetCRUD;
import org.opendaylight.controller.networkconfig.neutron.NeutronCRUDInterfaces;
import org.opendaylight.controller.networkconfig.neutron.NeutronPort;
import org.opendaylight.controller.networkconfig.neutron.Neutron_IPs;
import org.opendaylight.controller.networkconfig.neutron.NeutronSubnet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle requests for Neutron Port.
 */
public class PortHandler implements INeutronPortAware {
    /**
     * Logger instance.
     */
    static final Logger LOGGER = LoggerFactory.getLogger(PortHandler.class);
    static ApiConnector apiConnector;

    /**
     * Invoked when a port creation is requested to check if the specified Port
     * can be created and then creates the port
     *
     * @param NeutronPort
     *            An instance of proposed new Neutron Port object.
     * @return A HTTP status code to the creation request.
     */
    @Override
    public int canCreatePort(NeutronPort neutronPort) {
        apiConnector = Activator.apiConnector;
        if (neutronPort == null) {
            LOGGER.error("NeutronPort object can't be null..");
            return HttpURLConnection.HTTP_BAD_REQUEST;
        }
        if (neutronPort.getID().equals("")) {
            LOGGER.error("Port Device Id or Port Uuid can't be empty/null...");
            return HttpURLConnection.HTTP_BAD_REQUEST;
        }
        if (neutronPort.getTenantID() == null) {
            LOGGER.error("Tenant ID can't be null...");
            return HttpURLConnection.HTTP_BAD_REQUEST;
        }

        List<Neutron_IPs> ips = neutronPort.getFixedIPs();
        if (ips == null) {
            LOGGER.warn("Neutron Fixed Ips can't be null..");
            return HttpURLConnection.HTTP_FORBIDDEN;
        }
        try {
            return createPort(neutronPort);
        } catch (Exception e) {
            LOGGER.error("exception :   ", e);
            return HttpURLConnection.HTTP_INTERNAL_ERROR;
        }
    }

    /**
     * Invoked to create the specified Neutron port.
     *
     * @param network
     *            An instance of new Neutron Port object.
     *
     * @return A HTTP status code to the creation request.
     */
    private int createPort(NeutronPort neutronPort) {
        String networkID = neutronPort.getNetworkUUID();
        String portID = neutronPort.getID();
        String portDesc = neutronPort.getID();
        String deviceID = neutronPort.getDeviceID();
        String projectID = neutronPort.getTenantID();
        String portMACAddress = neutronPort.getMacAddress();
        VirtualMachineInterface virtualMachineInterface = null;
        VirtualMachine virtualMachine = null;
        VirtualNetwork virtualNetwork = null;
        Project project = null;
        MacAddressesType macAddressesType = new MacAddressesType();
        try {
            networkID = UUID.fromString(neutronPort.getNetworkUUID()).toString();
            portID = UUID.fromString(neutronPort.getID()).toString();
            if (neutronPort.getDeviceID() != null && !(("").equals(neutronPort.getDeviceID()))) {
                if (!(deviceID.contains("-"))) {
                    deviceID = uuidFormater(deviceID);
                }
                deviceID = UUID.fromString(deviceID).toString();
            }
            if (!(projectID.contains("-"))) {
                projectID = uuidFormater(projectID);
            }
            projectID = UUID.fromString(projectID).toString();
        } catch (Exception ex) {
            LOGGER.error("exception :   ", ex);
            return HttpURLConnection.HTTP_BAD_REQUEST;
        }
        try {
            LOGGER.debug("portId:    " + portID);
            virtualMachineInterface = (VirtualMachineInterface) apiConnector.findById(VirtualMachineInterface.class, portID);
            if (virtualMachineInterface != null) {
                LOGGER.warn("Port already exist.");
                return HttpURLConnection.HTTP_FORBIDDEN;
            } else {
                if (deviceID != null && !(("").equals(deviceID))) {
                    virtualMachine = (VirtualMachine) apiConnector.findById(VirtualMachine.class, deviceID);
                    LOGGER.debug("virtualMachine:   " + virtualMachine);
                    if (virtualMachine == null) {
                        virtualMachine = new VirtualMachine();
                        virtualMachine.setName(deviceID);
                        virtualMachine.setUuid(deviceID);
                        boolean virtualMachineCreated = apiConnector.create(virtualMachine);
                        LOGGER.debug("virtualMachineCreated: " + virtualMachineCreated);
                        if (!virtualMachineCreated) {
                            LOGGER.warn("virtualMachine creation failed..");
                            return HttpURLConnection.HTTP_INTERNAL_ERROR;
                        }
                        LOGGER.info("virtualMachine : " + virtualMachine.getName() + "  having UUID : " + virtualMachine.getUuid()
                                + "  sucessfully created...");
                    }
                }
                project = (Project) apiConnector.findById(Project.class, projectID);
                if (project == null) {
                    try {
                        Thread.currentThread();
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        LOGGER.error("InterruptedException :      ", e);
                    }
                    project = (Project) apiConnector.findById(Project.class, projectID);
                    if (project == null) {
                        LOGGER.error("Could not find projectUUID...");
                        return HttpURLConnection.HTTP_NOT_FOUND;
                    }
                }
                virtualNetwork = (VirtualNetwork) apiConnector.findById(VirtualNetwork.class, networkID);
                LOGGER.info("virtualNetwork: " + virtualNetwork);
                if (virtualNetwork == null) {
                    LOGGER.warn("virtualNetwork does not exist..");
                    return HttpURLConnection.HTTP_BAD_REQUEST;
                } else {
                    virtualMachineInterface = new VirtualMachineInterface();
                    virtualMachineInterface.setUuid(portID);
                    virtualMachineInterface.setName(portDesc);
                    virtualMachineInterface.setParent(project);
                    virtualMachineInterface.setVirtualNetwork(virtualNetwork);
                    macAddressesType.addMacAddress(portMACAddress);
                    virtualMachineInterface.setMacAddresses(macAddressesType);
                    if (deviceID != null && !(("").equals(deviceID))) {
                        virtualMachineInterface.setVirtualMachine(virtualMachine);
                    }
                    boolean virtualMachineInterfaceCreated = apiConnector.create(virtualMachineInterface);
                    if (!virtualMachineInterfaceCreated) {
                        LOGGER.warn("actual virtualMachineInterface creation failed..");
                        return HttpURLConnection.HTTP_INTERNAL_ERROR;
                    }
                    LOGGER.info("virtualMachineInterface : " + virtualMachineInterface.getName() + "  having UUID : "
                            + virtualMachineInterface.getUuid() + "  sucessfully created...");
                }

            }
            INeutronSubnetCRUD systemCRUD = NeutronCRUDInterfaces.getINeutronSubnetCRUD(this);
            NeutronSubnet subnet = null;
            List<Neutron_IPs> ips = neutronPort.getFixedIPs();
            InstanceIp instanceIp = new InstanceIp();
            String instaneIpUuid = UUID.randomUUID().toString();
            for (Neutron_IPs ipValues : ips) {
                if (ipValues.getIpAddress() == null) {
                    subnet = systemCRUD.getSubnet(ipValues.getSubnetUUID());
                    instanceIp.setAddress(subnet.getLowAddr());
                } else {
                    instanceIp.setAddress(ipValues.getIpAddress());
                }
            }

            instanceIp.setName(instaneIpUuid);
            instanceIp.setUuid(instaneIpUuid);
            instanceIp.setParent(virtualMachineInterface);
            instanceIp.setVirtualMachineInterface(virtualMachineInterface);
            instanceIp.setVirtualNetwork(virtualNetwork);

            boolean instanceIpCreated = apiConnector.create(instanceIp);
            if (!instanceIpCreated) {
                LOGGER.warn("instanceIp addition failed..");
                return HttpURLConnection.HTTP_INTERNAL_ERROR;
            }
            LOGGER.info("Instance IP added sucessfully...");
            return HttpURLConnection.HTTP_OK;
        } catch (IOException ie) {
            LOGGER.error("IOException :    ", ie);
            return HttpURLConnection.HTTP_INTERNAL_ERROR;
        }
    }

    /**
     * Invoked to take action after a port has been created.
     *
     * @param network
     *            An instance of new Neutron port object.
     */
    @Override
    public void neutronPortCreated(NeutronPort neutronPort) {
        VirtualMachineInterface virtualMachineInterface = null;
        try {
            virtualMachineInterface = (VirtualMachineInterface) apiConnector.findById(VirtualMachineInterface.class, neutronPort.getPortUUID());
            if (virtualMachineInterface != null) {
                LOGGER.info("Port creation verified....");
            }
        } catch (Exception e) {
            LOGGER.error("Exception :    " + e);
        }
    }

    /**
     * Invoked when a port deletion is requested to check if the specified Port
     * can be deleted and then delete the port
     *
     * @param NeutronPort
     *            An instance of proposed Neutron Port object.
     * @return A HTTP status code to the deletion request.
     */
    @Override
    public int canDeletePort(NeutronPort neutronPort) {
        if (neutronPort == null) {
            LOGGER.info("Port object can't be null...");
            return HttpURLConnection.HTTP_BAD_REQUEST;
        }
        apiConnector = Activator.apiConnector;
        try {
            return deletePort(neutronPort);
        } catch (Exception e) {
            LOGGER.error("exception :   ", e);
            return HttpURLConnection.HTTP_INTERNAL_ERROR;
        }
    }

    /**
     * Invoked to delete the specified Neutron port.
     *
     * @param network
     *            An instance of new Neutron Port object.
     *
     * @return A HTTP status code to the deletion request.
     */
    private int deletePort(NeutronPort neutronPort) {
        String portID = neutronPort.getID();
        String deviceID = neutronPort.getDeviceID();
        VirtualMachineInterface virtualMachineInterface = null;
        VirtualMachine virtualMachine = null;
        InstanceIp instanceIP = null;
        List<ObjectReference<ApiPropertyBase>> virtualMachineInterfaceBackRefs = null;
        try {
            virtualMachineInterface = (VirtualMachineInterface) apiConnector.findById(VirtualMachineInterface.class, portID);
            if (virtualMachineInterface == null) {
                LOGGER.info("Specified port does not exist...");
                return HttpURLConnection.HTTP_BAD_REQUEST;
            } else {
                List<ObjectReference<ApiPropertyBase>> instanceIPs = virtualMachineInterface.getInstanceIpBackRefs();
                if (instanceIPs != null) {
                    for (ObjectReference<ApiPropertyBase> ref : instanceIPs) {
                        String instanceIPUUID = ref.getUuid();
                        if (instanceIPUUID != null) {
                            instanceIP = (InstanceIp) apiConnector.findById(InstanceIp.class, instanceIPUUID);
                            apiConnector.delete(instanceIP);
                        }
                    }
                }
                apiConnector.delete(virtualMachineInterface);
                virtualMachine = (VirtualMachine) apiConnector.findById(VirtualMachine.class, deviceID);
                if (virtualMachine != null) {
                    virtualMachineInterfaceBackRefs = virtualMachine.getVirtualMachineInterfaceBackRefs();
                    if (virtualMachineInterfaceBackRefs == null) {
                        apiConnector.delete(virtualMachine);
                    }
                }
                LOGGER.info("Specified port deleted sucessfully...");
                return HttpURLConnection.HTTP_OK;
            }
        } catch (IOException io) {
            LOGGER.error("Exception  :   " + io);
            return HttpURLConnection.HTTP_INTERNAL_ERROR;
        } catch (Exception e) {
            LOGGER.error("Exception  :   " + e);
            return HttpURLConnection.HTTP_INTERNAL_ERROR;
        }
    }

    /**
     * Invoked to take action after a port has been deleted.
     *
     * @param network
     *            An instance of new Neutron port object.
     */
    @Override
    public void neutronPortDeleted(NeutronPort neutronPort) {
        VirtualMachineInterface virtualMachineInterface = new VirtualMachineInterface();
        try {
            virtualMachineInterface = (VirtualMachineInterface) apiConnector.findById(VirtualMachineInterface.class, neutronPort.getPortUUID());
            if (virtualMachineInterface == null) {
                LOGGER.info("Port deletion verified....");
            }
        } catch (Exception e) {
            LOGGER.error("Exception :    " + e);
        }
    }

    /**
     * Invoked when a port update is requested to indicate if the specified port
     * can be updated using the specified delta and update the port
     *
     * @param delta
     *            Updates to the port object using patch semantics.
     * @param original
     *            An instance of the Neutron Port object to be updated.
     *
     * @return A HTTP status code to the update request.
     */
    @Override
    public int canUpdatePort(NeutronPort deltaPort, NeutronPort originalPort) {
        apiConnector = Activator.apiConnector;
        VirtualMachineInterface virtualMachineInterface = null;
        if (deltaPort == null || originalPort == null) {
            LOGGER.error("Neutron Port objects can't be null..");
            return HttpURLConnection.HTTP_BAD_REQUEST;
        }
        if (deltaPort.getMacAddress() != null) {
            LOGGER.error("MAC Address for the port can't be updated..");
            return HttpURLConnection.HTTP_BAD_REQUEST;
        }
        try {
            virtualMachineInterface = (VirtualMachineInterface) apiConnector.findById(VirtualMachineInterface.class, originalPort.getPortUUID());
            return updatePort(deltaPort, virtualMachineInterface, originalPort);
        } catch (IOException ie) {
            LOGGER.error("IOException:     " + ie);
            return HttpURLConnection.HTTP_INTERNAL_ERROR;
        } catch (Exception e) {
            LOGGER.error("Exception:     " + e);
            return HttpURLConnection.HTTP_INTERNAL_ERROR;
        }
    }

    /**
     * Invoked to update the port
     *
     * @param delta_network
     *            An instance of Neutron Port.
     * @param An
     *            instance of new {@link VirtualMachineInterface} object.
     *
     * @return A HTTP status code to the updation request.
     */
    private int updatePort(NeutronPort deltaPort, VirtualMachineInterface virtualMachineInterface, NeutronPort originalPort) throws IOException {
        VirtualMachine virtualMachine = null;
        String deviceID = deltaPort.getDeviceID();
        String portName = deltaPort.getName();
        List<Neutron_IPs> fixedIPs = deltaPort.getFixedIPs();
        boolean instanceIpUpdate = false;
        String networkUUID = deltaPort.getNetworkUUID();
        VirtualNetwork virtualnetwork = null;
        if (fixedIPs != null) {
            if (networkUUID == null) {
                for (ObjectReference<ApiPropertyBase> networks : virtualMachineInterface.getVirtualNetwork()) {
                    networkUUID = networks.getUuid();
                }
            }
            boolean subnetExist = false;
            virtualnetwork = (VirtualNetwork) apiConnector.findById(VirtualNetwork.class, networkUUID);
            if (virtualnetwork == null) {
                LOGGER.error(" Virtual network does not exist");
                return HttpURLConnection.HTTP_FORBIDDEN;
            }
            if (virtualnetwork != null && virtualnetwork.getNetworkIpam() != null) {
                for (Neutron_IPs fixedIp : fixedIPs) {
                    for (ObjectReference<VnSubnetsType> ref : virtualnetwork.getNetworkIpam()) {
                        VnSubnetsType vnSubnetsType = ref.getAttr();
                        if (vnSubnetsType != null) {
                            List<VnSubnetsType.IpamSubnetType> subnets = vnSubnetsType.getIpamSubnets();
                            if (subnets != null) {
                                for (VnSubnetsType.IpamSubnetType subnetValue : subnets) {
                                    Boolean doesSubnetExist = subnetValue.getSubnetUuid().matches(fixedIp.getSubnetUUID());
                                    if (doesSubnetExist) {
                                        subnetExist = true;
                                        for (ObjectReference<ApiPropertyBase> instanceIp : virtualMachineInterface.getInstanceIpBackRefs()) {
                                            InstanceIp instanceIpLocal = (InstanceIp) apiConnector.findById(InstanceIp.class, instanceIp.getUuid());
                                            instanceIpLocal.setVirtualNetwork(virtualnetwork);
                                            INeutronSubnetCRUD systemCRUD = NeutronCRUDInterfaces.getINeutronSubnetCRUD(this);
                                            NeutronSubnet subnet = null;
                                            for (Neutron_IPs ip : originalPort.getFixedIPs()) {
                                                subnet = systemCRUD.getSubnet(ip.getSubnetUUID());
                                                subnet.releaseIP(ip.getIpAddress());
                                            }
                                            if (fixedIp.getIpAddress() == null) {
                                                subnet = systemCRUD.getSubnet(fixedIp.getSubnetUUID());
                                                instanceIpLocal.setAddress(subnet.getLowAddr());
                                            } else {
                                                instanceIpLocal.setAddress(fixedIp.getIpAddress());
                                            }
                                            instanceIpUpdate = apiConnector.update(instanceIpLocal);
                                            virtualMachineInterface.setVirtualNetwork(virtualnetwork);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (!subnetExist) {
                LOGGER.error("Subnet UUID must exist in the network..");
                return HttpURLConnection.HTTP_BAD_REQUEST;
            }
        } else if (networkUUID != null && fixedIPs == null) {
            LOGGER.error("Subnet UUID must exist in the network..");
            return HttpURLConnection.HTTP_BAD_REQUEST;
        }
        if (deviceID != null) {
            if (("").equals(deviceID)) {
                virtualMachineInterface.clearVirtualMachine();
            } else {
                deviceID = UUID.fromString(deltaPort.getDeviceID()).toString();
                try {
                    virtualMachine = (VirtualMachine) apiConnector.findById(VirtualMachine.class, deviceID);
                } catch (Exception e) {
                    LOGGER.error("Exception:     " + e);
                    return HttpURLConnection.HTTP_INTERNAL_ERROR;
                }
                if (virtualMachine == null) {
                    virtualMachine = new VirtualMachine();
                    virtualMachine.setName(deviceID);
                    virtualMachine.setUuid(deviceID);
                    boolean virtualMachineCreated = apiConnector.create(virtualMachine);
                    LOGGER.debug("virtualMachineCreated: " + virtualMachineCreated);
                    if (!virtualMachineCreated) {
                        LOGGER.warn("virtualMachine creation failed..");
                        return HttpURLConnection.HTTP_INTERNAL_ERROR;
                    }
                    LOGGER.info("virtualMachine : " + virtualMachine.getName() + "  having UUID : " + virtualMachine.getUuid()
                            + "  sucessfully created...");
                }
                virtualMachineInterface.setVirtualMachine(virtualMachine);
            }
        }
        if (portName != null) {
            virtualMachineInterface.setDisplayName(portName);
        }
        if ((deviceID != null && !(("").equals(deviceID))) || portName != null || instanceIpUpdate) {
            if ((deviceID != null && !(("").equals(deviceID))) || portName != null) {
                boolean portUpdate = apiConnector.update(virtualMachineInterface);
                if (!portUpdate) {
                    LOGGER.warn("Port Updation failed..");
                    return HttpURLConnection.HTTP_INTERNAL_ERROR;
                }
            }
            LOGGER.info("Port having UUID : " + virtualMachineInterface.getUuid() + "  has been sucessfully updated...");
            return HttpURLConnection.HTTP_OK;
        } else {
            LOGGER.info("Nothing to update...");
            return HttpURLConnection.HTTP_BAD_REQUEST;
        }
    }

    /**
     * Invoked to take action after a port has been updated.
     *
     * @param network
     *            An instance of modified Neutron Port object.
     */
    @Override
    public void neutronPortUpdated(NeutronPort neutronPort) {
        try {
            VirtualMachineInterface virtualMachineInterface;
            virtualMachineInterface = (VirtualMachineInterface) apiConnector.findById(VirtualMachineInterface.class, neutronPort.getPortUUID());
            if (("").equals(neutronPort.getDeviceID())) { // TODO : VM Refs not getting cleared correctly - to be fixed
                if (neutronPort.getName().matches(virtualMachineInterface.getDisplayName()) && virtualMachineInterface.getVirtualMachine() == null) {
                    LOGGER.info("Port updatation verified....");
                }
            } else if (neutronPort.getName().matches(virtualMachineInterface.getDisplayName())
                    && neutronPort.getDeviceID().matches(virtualMachineInterface.getVirtualMachine().get(0).getUuid())) {
                LOGGER.info("Port updatation verified....");
            } else {
                LOGGER.info("Port updatation failed....");
            }
        } catch (Exception e) {
            LOGGER.error("Exception :" + e);
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
        try {
            String id1 = uuid.substring(0, 8);
            String id2 = uuid.substring(8, 12);
            String id3 = uuid.substring(12, 16);
            String id4 = uuid.substring(16, 20);
            String id5 = uuid.substring(20, 32);
            uuidPattern = (id1 + "-" + id2 + "-" + id3 + "-" + id4 + "-" + id5);

        } catch (Exception e) {
            LOGGER.error("UUID is not in correct format ");
            LOGGER.error("Exception :" + e);
        }
        return uuidPattern;
    }
}
