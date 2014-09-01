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
import java.util.ArrayList;
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
        if (neutronPort.getPortUUID() == null || neutronPort.getPortUUID().equals("")) {
            LOGGER.error("Port Uuid can't be empty/null...");
            return HttpURLConnection.HTTP_BAD_REQUEST;
        }
        if (neutronPort.getTenantID() == null) {
            LOGGER.error("Tenant ID can't be null...");
            return HttpURLConnection.HTTP_BAD_REQUEST;
        }
        if (neutronPort.getName() == null || neutronPort.getName().equals("")) {
            LOGGER.error("Port Name can't be empty/null...");
            return HttpURLConnection.HTTP_BAD_REQUEST;
        }
        try {
            String portUUID = neutronPort.getPortUUID();
            String projectUUID = neutronPort.getTenantID();
            String deviceUUID = neutronPort.getDeviceID();
            String networkUUID = neutronPort.getNetworkUUID();
            try {
                if (!(portUUID.contains("-"))) {
                    portUUID = Utils.uuidFormater(portUUID);
                }
                if (!(projectUUID.contains("-"))) {
                    projectUUID = Utils.uuidFormater(projectUUID);
                }
                if (!(networkUUID.contains("-"))) {
                    networkUUID = Utils.uuidFormater(networkUUID);
                }
                if (deviceUUID != null && !(("").equals(deviceUUID))) {
                    if (!(deviceUUID.contains("-"))) {
                        deviceUUID = Utils.uuidFormater(deviceUUID);
                    }
                    boolean isValidDeviceUUID = Utils.isValidHexNumber(deviceUUID);
                    if (!isValidDeviceUUID) {
                        LOGGER.info("Badly formed Hexadecimal UUID...");
                        return HttpURLConnection.HTTP_BAD_REQUEST;
                    }
                    deviceUUID = UUID.fromString(deviceUUID).toString();
                }
                boolean isValidNetworkUUID = Utils.isValidHexNumber(networkUUID);
                boolean isValidProjectUUID = Utils.isValidHexNumber(projectUUID);
                boolean isValidPortUUID = Utils.isValidHexNumber(portUUID);
                if (!isValidPortUUID || !isValidProjectUUID || !isValidNetworkUUID) {
                    LOGGER.info("Badly formed Hexadecimal UUID...");
                    return HttpURLConnection.HTTP_BAD_REQUEST;
                }
                portUUID = UUID.fromString(portUUID).toString();
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
            VirtualNetwork virtualNetwork = (VirtualNetwork) apiConnector.findById(VirtualNetwork.class, networkUUID);
            if (!virtualNetwork.getParentUuid().matches(projectUUID)) {
                LOGGER.info("Port and Network should belong to same tenant...");
                return HttpURLConnection.HTTP_BAD_REQUEST;
            }
            VirtualMachineInterface virtualMAchineInterfaceByID = (VirtualMachineInterface)apiConnector.findById(VirtualMachineInterface.class, portUUID);
            if (virtualMAchineInterfaceByID != null) {
                LOGGER.warn("Port already exists with UUID : " + virtualMAchineInterfaceByID);
                return HttpURLConnection.HTTP_FORBIDDEN;
            }
            String virtualMAchineInterfaceByName = apiConnector.findByName(VirtualMachineInterface.class, project, neutronPort.getName());
            if (virtualMAchineInterfaceByName != null) {
                LOGGER.warn("Port already exists with Name : " + virtualMAchineInterfaceByName);
                return HttpURLConnection.HTTP_FORBIDDEN;
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
     * Invoked to create the specified Neutron port.
     *
     * @param neutronPort
     *            An instance of new Neutron Port object.
     */
    private void createPort(NeutronPort neutronPort) {
        String networkUUID = neutronPort.getNetworkUUID();
        String portUUID = neutronPort.getPortUUID();
        String deviceUUID = neutronPort.getDeviceID();
        String projectUUID = neutronPort.getTenantID();
        String portMACAddress = neutronPort.getMacAddress();
        String portName = neutronPort.getName();
        VirtualMachineInterface virtualMachineInterface = null;
        VirtualMachine virtualMachine = null;
        VirtualNetwork virtualNetwork = null;
        Project project = null;
        MacAddressesType macAddressesType = new MacAddressesType();
        try {
            if (!(networkUUID.contains("-"))) {
                networkUUID = Utils.uuidFormater(networkUUID);
            }
            networkUUID = UUID.fromString(networkUUID).toString();
            if (deviceUUID != null && !(("").equals(deviceUUID))) {
                if (!(deviceUUID.contains("-"))) {
                    deviceUUID = Utils.uuidFormater(deviceUUID);
                }
                deviceUUID = UUID.fromString(deviceUUID).toString();
            }
            if (!(projectUUID.contains("-"))) {
                projectUUID = Utils.uuidFormater(projectUUID);
            }
            projectUUID = UUID.fromString(projectUUID).toString();
            if (!(portUUID.contains("-"))) {
                portUUID = Utils.uuidFormater(portUUID);
            }
            portUUID = UUID.fromString(portUUID).toString();
        } catch (Exception ex) {
            LOGGER.error("UUID input incorrect", ex);
        }
        try {
            LOGGER.info("portId:    " + portUUID);
            if (deviceUUID != null && !(("").equals(deviceUUID))) {
                virtualMachine = (VirtualMachine) apiConnector.findById(VirtualMachine.class, deviceUUID);
                LOGGER.debug("virtualMachine:   " + virtualMachine);
                if (virtualMachine == null) {
                    virtualMachine = new VirtualMachine();
                    virtualMachine.setName(deviceUUID);
                    virtualMachine.setUuid(deviceUUID);
                    boolean virtualMachineCreated = apiConnector.create(virtualMachine);
                    LOGGER.debug("virtualMachineCreated: " + virtualMachineCreated);
                    if (!virtualMachineCreated) {
                        LOGGER.warn("virtualMachine creation failed..");
                    }
                    LOGGER.info("virtualMachine : " + virtualMachine.getName() + "  having UUID : " + virtualMachine.getUuid()
                            + "  sucessfully created...");
                }
            }
            project = (Project) apiConnector.findById(Project.class, projectUUID);
            virtualNetwork = (VirtualNetwork) apiConnector.findById(VirtualNetwork.class, networkUUID);
            LOGGER.debug("virtualNetwork: " + virtualNetwork);
            virtualMachineInterface = new VirtualMachineInterface();
            virtualMachineInterface.setUuid(portUUID);
            virtualMachineInterface.setName(portName);
            virtualMachineInterface.setDisplayName(portName);
            virtualMachineInterface.setParent(project);
            virtualMachineInterface.setVirtualNetwork(virtualNetwork);
            macAddressesType.addMacAddress(portMACAddress);
            virtualMachineInterface.setMacAddresses(macAddressesType);
            if (deviceUUID != null && !(("").equals(deviceUUID))) {
                virtualMachineInterface.setVirtualMachine(virtualMachine);
            }
            boolean virtualMachineInterfaceCreated = apiConnector.create(virtualMachineInterface);
            if (!virtualMachineInterfaceCreated) {
                LOGGER.warn("actual virtualMachineInterface creation failed..");
            }
            LOGGER.info("virtualMachineInterface : " + virtualMachineInterface.getName() + "  having UUID : " + virtualMachineInterface.getUuid()
                    + "  sucessfully created...");
            List<Neutron_IPs> ips = neutronPort.getFixedIPs();
            if (ips != null) {
                for (Neutron_IPs ipValues : ips) {
                    INeutronSubnetCRUD systemCRUD = NeutronCRUDInterfaces.getINeutronSubnetCRUD(this);
                    NeutronSubnet subnet = null;
                    InstanceIp instanceIp = new InstanceIp();
                    String instaneIpUuid = UUID.randomUUID().toString();
                    if (ipValues.getIpAddress() == null) {
                        subnet = systemCRUD.getSubnet(ipValues.getSubnetUUID());
                        instanceIp.setAddress(subnet.getLowAddr());
                    } else {
                        instanceIp.setAddress(ipValues.getIpAddress());
                    }
                    instanceIp.setName(instaneIpUuid);
                    instanceIp.setUuid(instaneIpUuid);
                    instanceIp.setParent(virtualMachineInterface);
                    instanceIp.setVirtualMachineInterface(virtualMachineInterface);
                    instanceIp.setVirtualNetwork(virtualNetwork);

                    boolean instanceIpCreated = apiConnector.create(instanceIp);
                    if (!instanceIpCreated) {
                        LOGGER.warn("instanceIp addition failed..");
                    }
                    LOGGER.info("Instance IP " + instanceIp.getAddress() + " added sucessfully...");
                }
            }
        } catch (IOException ie) {
            LOGGER.error("IOException :    ", ie);
        }
    }

    /**
     * Invoked to create a port and take action after the port has been created.
     *
     * @param network
     *            An instance of new Neutron port object.
     */
    @Override
    public void neutronPortCreated(NeutronPort neutronPort) {
        try {
            createPort(neutronPort);
            String portUUID = neutronPort.getPortUUID();
            if (!(portUUID.contains("-"))) {
                portUUID = Utils.uuidFormater(portUUID);
            }
            portUUID = UUID.fromString(portUUID).toString();
            VirtualMachineInterface virtualMachineInterface = (VirtualMachineInterface) apiConnector
                    .findById(VirtualMachineInterface.class, portUUID);
            if (virtualMachineInterface != null) {
                LOGGER.info("Port creation verified....");
            }
        } catch (Exception ex) {
            LOGGER.error("Exception :    " + ex);
        }
    }

    /**
     * Invoked when a port deletion is requested to check if the specified Port
     * can be deleted
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
        String portUUID = neutronPort.getPortUUID();
        if (!(portUUID.contains("-"))) {
            portUUID = Utils.uuidFormater(portUUID);
        }
        portUUID = UUID.fromString(portUUID).toString();
        try {
            VirtualMachineInterface virtualMachineInterface = (VirtualMachineInterface) apiConnector
                    .findById(VirtualMachineInterface.class, portUUID);

            if (virtualMachineInterface == null) {
                LOGGER.error("No port exists for specified UUID...");
                return HttpURLConnection.HTTP_NOT_FOUND;
            } else {
                List<ObjectReference<ApiPropertyBase>> vmi = new ArrayList<ObjectReference<ApiPropertyBase>>();
                vmi = virtualMachineInterface.getFloatingIpBackRefs();
                if (vmi != null) {
                    LOGGER.info("Port has floating Ip associated with it...");
                    return HttpURLConnection.HTTP_BAD_REQUEST;
                }
                return HttpURLConnection.HTTP_OK;
            }
        } catch (Exception ioEx) {
            LOGGER.error("IOException :   ", ioEx);
            return HttpURLConnection.HTTP_INTERNAL_ERROR;
        }
    }

    /**
     * Invoked to delete the specified Neutron port.
     *
     * @param neutronPort
     *            An instance of new Neutron Port object.
     */
    private void deletePort(NeutronPort neutronPort) {
        String portUUID = neutronPort.getPortUUID();
        String deviceUUID = neutronPort.getDeviceID();
        VirtualMachineInterface virtualMachineInterface = null;
        VirtualMachine virtualMachine = null;
        InstanceIp instanceIP = null;
        List<ObjectReference<ApiPropertyBase>> virtualMachineInterfaceBackRefs = null;
        try {
            try {
                if (deviceUUID != null && !(("").equals(deviceUUID))) {
                    if (!(deviceUUID.contains("-"))) {
                        deviceUUID = Utils.uuidFormater(deviceUUID);
                    }
                    deviceUUID = UUID.fromString(deviceUUID).toString();
                }
                if (!(portUUID.contains("-"))) {
                    portUUID = Utils.uuidFormater(portUUID);
                }
                portUUID = UUID.fromString(portUUID).toString();
            } catch (Exception ex) {
                LOGGER.error("UUID input incorrect", ex);
            }
            virtualMachineInterface = (VirtualMachineInterface) apiConnector.findById(VirtualMachineInterface.class, portUUID);
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
            virtualMachine = (VirtualMachine) apiConnector.findById(VirtualMachine.class, deviceUUID);
            if (virtualMachine != null) {
                virtualMachineInterfaceBackRefs = virtualMachine.getVirtualMachineInterfaceBackRefs();
                if (virtualMachineInterfaceBackRefs == null) {
                    apiConnector.delete(virtualMachine);
                }
            }
            LOGGER.info("Specified port deleted sucessfully...");
        } catch (IOException io) {
            LOGGER.error("Exception  :   " + io);
        } catch (Exception e) {
            LOGGER.error("Exception  :   " + e);
        }
    }

    /**
     * Invoked to take action after a port has been deleted.
     *
     * @param neutronPort
     *            An instance of new Neutron port object.
     */
    @Override
    public void neutronPortDeleted(NeutronPort neutronPort) {
        try {
            deletePort(neutronPort);
            String portUUID = neutronPort.getPortUUID();
            if (!(portUUID.contains("-"))) {
                portUUID = Utils.uuidFormater(portUUID);
            }
            portUUID = UUID.fromString(portUUID).toString();
            VirtualMachineInterface virtualMachineInterface = (VirtualMachineInterface) apiConnector
                    .findById(VirtualMachineInterface.class, portUUID);
            if (virtualMachineInterface == null) {
                LOGGER.info("Port deletion verified....");
            }
        } catch (IOException ioEx) {
            LOGGER.error("Exception :    " + ioEx);
        } catch (Exception e) {
            LOGGER.error("Exception :    " + e);
        }
    }

    /**
     * Invoked when a port update is requested to indicate if the specified port
     * can be updated using the specified delta
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
        if (deltaPort == null || originalPort == null) {
            LOGGER.error("Neutron Port objects can't be null..");
            return HttpURLConnection.HTTP_BAD_REQUEST;
        }
        if (deltaPort.getMacAddress() != null) {
            LOGGER.error("MAC Address for the port can't be updated..");
            return HttpURLConnection.HTTP_BAD_REQUEST;
        }
        try {
            String portUUID = originalPort.getPortUUID();
            String networkUUID = deltaPort.getNetworkUUID();
            String projectUUID = originalPort.getTenantID();
            List<Neutron_IPs> fixedIPs = deltaPort.getFixedIPs();
            if (!(portUUID.contains("-"))) {
                portUUID = Utils.uuidFormater(portUUID);
            }
            portUUID = UUID.fromString(portUUID).toString();
            if (!(projectUUID.contains("-"))) {
                projectUUID = Utils.uuidFormater(projectUUID);
            }
            projectUUID = UUID.fromString(projectUUID).toString();
            Project project = (Project) apiConnector.findById(Project.class, projectUUID);
            VirtualMachineInterface virtualMachineInterface = (VirtualMachineInterface) apiConnector
                    .findById(VirtualMachineInterface.class, portUUID);
            if (virtualMachineInterface == null) {
                LOGGER.error("No port exists for specified UUID...");
                return HttpURLConnection.HTTP_NOT_FOUND;
            }
            String virtualMachineInterfaceByName = apiConnector.findByName(VirtualMachineInterface.class, project, deltaPort.getName());
            if (virtualMachineInterfaceByName != null) {
                LOGGER.warn("Port already exists with UUID : " + virtualMachineInterfaceByName);
                return HttpURLConnection.HTTP_FORBIDDEN;
            }
            if (networkUUID != null && !(("").equals(networkUUID))) {
                if (!(networkUUID.contains("-"))) {
                    networkUUID = Utils.uuidFormater(networkUUID);
                }
                networkUUID = UUID.fromString(networkUUID).toString();
            }
            VirtualNetwork virtualnetwork = (VirtualNetwork) apiConnector.findById(VirtualNetwork.class, networkUUID);
            if (networkUUID != null && fixedIPs == null) {
                LOGGER.error("Subnet UUID must exist in the network..");
                return HttpURLConnection.HTTP_BAD_REQUEST;
            }
            if (fixedIPs != null) {
                if (virtualnetwork.getNetworkIpam() != null) {
                    for (Neutron_IPs fixedIp : fixedIPs) {
                        for (ObjectReference<VnSubnetsType> ref : virtualnetwork.getNetworkIpam()) {
                            VnSubnetsType vnSubnetsType = ref.getAttr();
                            if (vnSubnetsType != null) {
                                List<VnSubnetsType.IpamSubnetType> subnets = vnSubnetsType.getIpamSubnets();
                                if (subnets != null) {
                                    for (VnSubnetsType.IpamSubnetType subnetValue : subnets) {
                                        String subnetUUID = fixedIp.getSubnetUUID();
                                        if (!(subnetUUID.contains("-"))) {
                                            subnetUUID = Utils.uuidFormater(subnetUUID);
                                        }
                                        subnetUUID = UUID.fromString(subnetUUID).toString();
                                        Boolean doesSubnetExist = subnetValue.getSubnetUuid().matches(subnetUUID);
                                        if (!doesSubnetExist) {
                                            LOGGER.error("Subnet UUID must exist in the network..");
                                            return HttpURLConnection.HTTP_BAD_REQUEST;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
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
     * Invoked to update the port
     *
     * @param delta_neutronPort
     *            An instance of Neutron Port.
     */
    private void updatePort(NeutronPort neutronPort) throws IOException {
        String deviceUUID = neutronPort.getDeviceID();
        String portName = neutronPort.getName();
        String portUUID = neutronPort.getPortUUID();
        String networkUUID = neutronPort.getNetworkUUID();
        List<Neutron_IPs> fixedIPs = neutronPort.getFixedIPs();
        boolean instanceIpUpdate = false;
        VirtualMachineInterface virtualMachineInterface = null;
        VirtualMachine virtualMachine = null;
        VirtualNetwork virtualnetwork = null;
        try {
            if (!(portUUID.contains("-"))) {
                portUUID = Utils.uuidFormater(portUUID);
            }
            portUUID = UUID.fromString(portUUID).toString();
            if (!(networkUUID.contains("-"))) {
                networkUUID = Utils.uuidFormater(networkUUID);
            }
            networkUUID = UUID.fromString(networkUUID).toString();
            if (deviceUUID != null) {
                if (!(deviceUUID.contains("-"))) {
                    deviceUUID = Utils.uuidFormater(deviceUUID);
                }
                deviceUUID = UUID.fromString(deviceUUID).toString();
            }
            virtualMachineInterface = (VirtualMachineInterface) apiConnector.findById(VirtualMachineInterface.class, portUUID);

            if (fixedIPs != null) {
                virtualnetwork = (VirtualNetwork) apiConnector.findById(VirtualNetwork.class, networkUUID);
                if (virtualnetwork.getNetworkIpam() != null) {
                    for (Neutron_IPs fixedIp : fixedIPs) {
                        for (ObjectReference<VnSubnetsType> ref : virtualnetwork.getNetworkIpam()) {
                            VnSubnetsType vnSubnetsType = ref.getAttr();
                            if (vnSubnetsType != null) {
                                List<VnSubnetsType.IpamSubnetType> subnets = vnSubnetsType.getIpamSubnets();
                                if (subnets != null) {
                                    for (VnSubnetsType.IpamSubnetType subnetValue : subnets) {
                                        String subnetUUID = fixedIp.getSubnetUUID();
                                        if (!(subnetUUID.contains("-"))) {
                                            subnetUUID = Utils.uuidFormater(subnetUUID);
                                        }
                                        subnetUUID = UUID.fromString(subnetUUID).toString();
                                        Boolean doesSubnetExist = subnetValue.getSubnetUuid().matches(subnetUUID);
                                        if (doesSubnetExist) {
                                            // subnetExist = true;
                                            for (ObjectReference<ApiPropertyBase> instanceIp : virtualMachineInterface.getInstanceIpBackRefs()) {
                                                InstanceIp instanceIpLocal = (InstanceIp) apiConnector.findById(InstanceIp.class,
                                                        instanceIp.getUuid());
                                                instanceIpLocal.setVirtualNetwork(virtualnetwork);
                                                INeutronSubnetCRUD systemCRUD = NeutronCRUDInterfaces.getINeutronSubnetCRUD(this);
                                                NeutronSubnet subnet = null;
                                                for (Neutron_IPs ip : neutronPort.getFixedIPs()) {
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
            }
            if (deviceUUID != null) {
                if (("").equals(deviceUUID)) {
                    virtualMachineInterface.clearVirtualMachine();
                } else {
                    try {
                        virtualMachine = (VirtualMachine) apiConnector.findById(VirtualMachine.class, deviceUUID);
                    } catch (IOException ioEx) {
                        LOGGER.error("Exception:     " + ioEx);
                    } catch (Exception ex) {
                        LOGGER.error("Exception:     " + ex);
                    }
                    if (virtualMachine == null) {
                        virtualMachine = new VirtualMachine();
                        virtualMachine.setName(deviceUUID);
                        virtualMachine.setUuid(deviceUUID);
                        boolean virtualMachineCreated = apiConnector.create(virtualMachine);
                        LOGGER.debug("virtualMachineCreated: " + virtualMachineCreated);
                        if (!virtualMachineCreated) {
                            LOGGER.warn("virtualMachine creation failed..");
                        }
                        LOGGER.info("virtualMachine : " + virtualMachine.getName() + "  having UUID : " + virtualMachine.getUuid()
                                + "  sucessfully created...");
                    }
                    virtualMachineInterface.setVirtualMachine(virtualMachine);
                }
            }
            if (deviceUUID == null) {
                virtualMachineInterface.clearVirtualMachine();
            }
            if (portName != null) {
                virtualMachineInterface.setDisplayName(portName);
            }
            if ((deviceUUID != null && !(("").equals(deviceUUID))) || portName != null || instanceIpUpdate) {
                if ((deviceUUID != null && !(("").equals(deviceUUID))) || portName != null) {
                    boolean portUpdate = apiConnector.update(virtualMachineInterface);
                    if (!portUpdate) {
                        LOGGER.warn("Port Updation failed..");
                    }
                }
                LOGGER.info("Port having UUID : " + virtualMachineInterface.getUuid() + "  has been sucessfully updated...");
            } else {
                LOGGER.info("Nothing to update...");
            }
        } catch (Exception ex) {
            LOGGER.error("Exception  :    " + ex);
        }
    }

    /**
     * Invoked to take action after a port has been updated.
     *
     * @param updatedPort
     *            An instance of modified Neutron Port object.
     */
    @Override
    public void neutronPortUpdated(NeutronPort updatedPort) {
        String deviceUUID = updatedPort.getDeviceID();
        String portUUID = updatedPort.getPortUUID();
        try {
            updatePort(updatedPort);
            try {
                if (!(portUUID.contains("-"))) {
                    portUUID = Utils.uuidFormater(portUUID);
                }
                portUUID = UUID.fromString(portUUID).toString();
                if (deviceUUID != null) {
                    if (!(deviceUUID.contains("-"))) {
                        deviceUUID = Utils.uuidFormater(deviceUUID);
                    }
                    deviceUUID = UUID.fromString(deviceUUID).toString();
                }
                VirtualMachineInterface virtualMachineInterface = (VirtualMachineInterface) apiConnector.findById(VirtualMachineInterface.class,
                        portUUID);
                if (deviceUUID == null || ("").equals(deviceUUID)) {
                    if (updatedPort.getName().matches(virtualMachineInterface.getDisplayName())
                            && virtualMachineInterface.getVirtualMachine() == null) {
                        LOGGER.info("Port updation verified....");
                    }
                } else {
                    if (updatedPort.getName().matches(virtualMachineInterface.getDisplayName())
                            && deviceUUID.matches(virtualMachineInterface.getVirtualMachine().get(0).getUuid())) {
                        LOGGER.info("Port updatation verified....");
                    }
                }
            } catch (Exception e) {
                LOGGER.error("Exception :" + e);
            }
        } catch (IOException ioEx) {
            LOGGER.error("Exception :" + ioEx);
        } catch (Exception ex) {
            LOGGER.error("Exception :" + ex);
        }
    }
}