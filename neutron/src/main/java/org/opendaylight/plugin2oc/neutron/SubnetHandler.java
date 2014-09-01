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
import java.util.List;
import java.util.UUID;

import net.juniper.contrail.api.ApiConnector;
import net.juniper.contrail.api.ObjectReference;
import net.juniper.contrail.api.types.NetworkIpam;
import net.juniper.contrail.api.types.SubnetType;
import net.juniper.contrail.api.types.VirtualNetwork;
import net.juniper.contrail.api.types.VnSubnetsType;
import net.juniper.contrail.api.types.VnSubnetsType.IpamSubnetType;

import org.apache.commons.net.util.SubnetUtils;
import org.apache.commons.net.util.SubnetUtils.SubnetInfo;
import org.opendaylight.controller.networkconfig.neutron.INeutronSubnetAware;
import org.opendaylight.controller.networkconfig.neutron.NeutronSubnet;
import org.opendaylight.controller.networkconfig.neutron.NeutronSubnet_IPAllocationPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle requests for Neutron Subnet.
 */
public class SubnetHandler implements INeutronSubnetAware {
    /**
     * Logger instance.
     */
    static final Logger LOGGER = LoggerFactory.getLogger(SubnetHandler.class);
    static ApiConnector apiConnector = Activator.apiConnector;

    /**
     * Invoked when a subnet creation is requested to check if the specified
     * subnet can be created and then creates the subnet.
     *
     * @param subnet
     *            An instance of proposed new Neutron Subnet object.
     *
     * @return A HTTP status code to the creation request.
     **/
    @Override
    public int canCreateSubnet(NeutronSubnet subnet) {
        VirtualNetwork virtualnetwork = new VirtualNetwork();
        apiConnector = Activator.apiConnector;
        if (subnet == null) {
            LOGGER.error("Neutron Subnet can't be null..");
            return HttpURLConnection.HTTP_BAD_REQUEST;
        }
        if (subnet.getCidr() == null || ("").equals(subnet.getCidr())) {
            LOGGER.info("Subnet Cidr can not be empty or null...");
            return HttpURLConnection.HTTP_BAD_REQUEST;
        }
        boolean isvalidGateway = validGatewayIP(subnet, subnet.getGatewayIP());
        if (!isvalidGateway) {
            LOGGER.error("Incorrect gateway IP....");
            return HttpURLConnection.HTTP_BAD_REQUEST;
        }
        String networkUUID = subnet.getNetworkUUID();
        String subnetUUID = subnet.getSubnetUUID();
        try {
            if (!(networkUUID.contains("-"))) {
                networkUUID = Utils.uuidFormater(networkUUID);
            }
            if (!(subnetUUID.contains("-"))) {
                subnetUUID = Utils.uuidFormater(subnetUUID);
            }
            boolean isValidNetworkUUID = Utils.isValidHexNumber(networkUUID);
            boolean isValidSubnetUUID = Utils.isValidHexNumber(subnetUUID);
            if (!isValidNetworkUUID || !isValidSubnetUUID) {
                LOGGER.info("Badly formed Hexadecimal UUID...");
                return HttpURLConnection.HTTP_BAD_REQUEST;
            }
            networkUUID = UUID.fromString(networkUUID).toString();
            subnetUUID = UUID.fromString(subnetUUID).toString();
        } catch (Exception ex) {
            LOGGER.error("UUID input incorrect", ex);
            return HttpURLConnection.HTTP_BAD_REQUEST;
        }
        try {
            virtualnetwork = (VirtualNetwork) apiConnector.findById(VirtualNetwork.class, networkUUID);
        } catch (IOException e) {
            LOGGER.error("Exception : " + e);
            return HttpURLConnection.HTTP_INTERNAL_ERROR;
        }
        if (virtualnetwork == null) {
            LOGGER.error("No network exists for the specified UUID...");
            return HttpURLConnection.HTTP_FORBIDDEN;
        } else {
            try {
                boolean ifSubnetExist = subnetExists(virtualnetwork.getNetworkIpam(), subnet);
                if (ifSubnetExist) {
                    LOGGER.error("The subnet already exists..");
                    return HttpURLConnection.HTTP_FORBIDDEN;
                }
            } catch (Exception e) {
                LOGGER.error("Exception:  " + e);
                return HttpURLConnection.HTTP_INTERNAL_ERROR;
            }
            return HttpURLConnection.HTTP_OK;
        }
    }

    private boolean subnetExists(List<ObjectReference<VnSubnetsType>> ipamRefs, NeutronSubnet subnet) {
        if (ipamRefs != null) {
            for (ObjectReference<VnSubnetsType> ref : ipamRefs) {
                VnSubnetsType vnSubnetsType = ref.getAttr();
                if (vnSubnetsType != null) {
                    List<VnSubnetsType.IpamSubnetType> subnets = vnSubnetsType.getIpamSubnets();
                    if (subnets != null) {
                        for (VnSubnetsType.IpamSubnetType subnetValue : subnets) {
                            String[] ipPrefix = getIpPrefix(subnet);
                            Boolean doesSubnetExist = subnetValue.getSubnet().getIpPrefix().matches(ipPrefix[0]);
                            if (doesSubnetExist) {
                                return doesSubnetExist;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Invoked to create the subnet
     *
     * @param subnet
     *            An instance of new Subnet Type object.
     */
    @Override
    public void neutronSubnetCreated(NeutronSubnet subnet) {
        String networkUUID = subnet.getNetworkUUID();
        String subnetUUID = subnet.getSubnetUUID();
        try {
            if (!(networkUUID.contains("-"))) {
                networkUUID = Utils.uuidFormater(networkUUID);
            }
            networkUUID = UUID.fromString(networkUUID).toString();
            if (!(subnetUUID.contains("-"))) {
                subnetUUID = Utils.uuidFormater(subnetUUID);
            }
            subnetUUID = UUID.fromString(subnetUUID).toString();
            createSubnet(subnet);
            VirtualNetwork virtualNetwork = (VirtualNetwork) apiConnector.findById(VirtualNetwork.class, networkUUID);
            boolean ifSubnetExists = subnetExists(virtualNetwork.getNetworkIpam(), subnet);
            if (ifSubnetExists) {
                LOGGER.info("Subnet creation verified...");
            }
        } catch (IOException ie) {
            LOGGER.error("IOException :   " + ie);
        } catch (Exception ex) {
            LOGGER.error("Exception :   ", ex);
        }
    }

    /**
     * Invoked to create the subnet
     *
     * @param subnet
     *            An instance of new Subnet Type object.
     * @param virtualNetwork
     *            An instance of new virtualNetwork object.
     *
     * @return A HTTP status code to the creation request.
     */
    private void createSubnet(NeutronSubnet subnet) throws IOException {
        // add subnet properties to the virtual-network object
        String networkUUID = subnet.getNetworkUUID();
        try {
            if (!(networkUUID.contains("-"))) {
                networkUUID = Utils.uuidFormater(networkUUID);
            }
            networkUUID = UUID.fromString(networkUUID).toString();
        } catch (Exception ex) {
            LOGGER.error("UUID input incorrect", ex);
        }
        try {
            VirtualNetwork virtualNetwork = (VirtualNetwork) apiConnector.findById(VirtualNetwork.class, networkUUID);
            virtualNetwork = mapSubnetProperties(subnet, virtualNetwork);
            boolean subnetCreate = apiConnector.update(virtualNetwork);
            if (!subnetCreate) {
                LOGGER.warn("Subnet creation failed..");
            } else {
                LOGGER.info("Subnet " + subnet.getCidr() + " sucessfully added to the network having UUID : " + virtualNetwork.getUuid());
            }
        } catch (IOException ioEx) {
            LOGGER.error("IOException   : ", ioEx);
        } catch (Exception ex) {
            LOGGER.error("IOException   : ", ex);
        }
    }

    /**
     * Invoked to add the NeutronSubnet properties to the virtualNetwork object.
     *
     * @param subnet
     *            An instance of new Neutron Subnet object.
     * @param virtualNetwork
     *            An instance of new virtualNetwork object.
     *
     * @return {@link VirtualNetwork}
     */
    private VirtualNetwork mapSubnetProperties(NeutronSubnet subnet, VirtualNetwork vn) {
        String subnetUUID = subnet.getSubnetUUID();
        String[] ipPrefix = null;
        NetworkIpam ipam = null;
        VnSubnetsType vnSubnetsType = new VnSubnetsType();
        SubnetType subnetType = new SubnetType();
        try {
            try {
                if (!(subnetUUID.contains("-"))) {
                    subnetUUID = Utils.uuidFormater(subnetUUID);
                }
                subnetUUID = UUID.fromString(subnetUUID).toString();
            } catch (Exception ex) {
                LOGGER.error("UUID input incorrect", ex);
            }
            if (subnet.getCidr().contains("/")) {
                ipPrefix = subnet.getCidr().split("/");
            } else {
                throw new IllegalArgumentException("String " + subnet.getCidr() + " not in correct format..");
            }
            // Find default-network-ipam
            String ipamId = apiConnector.findByName(NetworkIpam.class, null, "default-network-ipam");
            ipam = (NetworkIpam) apiConnector.findById(NetworkIpam.class, ipamId);
        } catch (IOException ex) {
            LOGGER.error("IOException :     " + ex);
        } catch (Exception ex) {
            LOGGER.error("Exception :      " + ex);
        }
        if (ipPrefix != null) {
            subnetType.setIpPrefix(ipPrefix[0]);
            subnetType.setIpPrefixLen(Integer.valueOf(ipPrefix[1]));
            IpamSubnetType ipamSubnetType = new IpamSubnetType();
            ipamSubnetType.setSubnet(subnetType);
            ipamSubnetType.setDefaultGateway(subnet.getGatewayIP());
            ipamSubnetType.setSubnetUuid(subnetUUID);
            ipamSubnetType.setSubnetName(subnet.getName());
            ipamSubnetType.setEnableDhcp(subnet.isEnableDHCP());
            if (vn.getNetworkIpam() != null) {
                for (ObjectReference<VnSubnetsType> ref : vn.getNetworkIpam()) {
                    vnSubnetsType = ref.getAttr();
                    vnSubnetsType.addIpamSubnets(ipamSubnetType);
                }
            } else {
                vnSubnetsType.addIpamSubnets(ipamSubnetType);
            }
            vn.setNetworkIpam(ipam, vnSubnetsType);
        }
        return vn;
    }

    /**
     * Invoked to get the IP Prefix from the Neutron Subnet object.
     *
     * @param subnet
     *            An instance of new Neutron Subnet object.
     *
     * @return IP Prefix
     * @throws Exception
     */
    String[] getIpPrefix(NeutronSubnet subnet) {
        String[] ipPrefix = null;
        String cidr = subnet.getCidr();
        if (cidr.contains("/")) {
            ipPrefix = cidr.split("/");
        } else {
            throw new IllegalArgumentException("String " + cidr + " not in correct format..");
        }
        return ipPrefix;
    }

    /**
     * Invoked when a subnet update is requested to indicate if the specified
     * subnet can be changed using the specified delta.
     *
     * @param delta
     *            Updates to the subnet object using patch semantics.
     * @param original
     *            An instance of the Neutron Subnet object to be updated.
     * @return A HTTP status code to the update request.
     */
    @Override
    public int canUpdateSubnet(NeutronSubnet deltaSubnet, NeutronSubnet originalSubnet) {
        if (deltaSubnet == null || originalSubnet == null) {
            LOGGER.error("Neutron Subnets can't be null..");
            return HttpURLConnection.HTTP_BAD_REQUEST;
        }
        // if (deltaSubnet.getGatewayIP() == null ||
        // ("").equals(deltaSubnet.getGatewayIP().toString())) {
        // LOGGER.error("Gateway IP can't be empty/null`..");
        // return HttpURLConnection.HTTP_BAD_REQUEST;
        // }
        if (deltaSubnet.getGatewayIP() != null) { // cant update gateway IP in
                                                  // OpenContrail
            if (!originalSubnet.getGatewayIP().matches(deltaSubnet.getGatewayIP())) {
                // boolean isvalidGateway = validGatewayIP(originalSubnet,
                // deltaSubnet.getGatewayIP());
                // if (!isvalidGateway) {
                LOGGER.error(" Cannot update gateway IP..");
                return HttpURLConnection.HTTP_BAD_REQUEST;
            }
        }
        apiConnector = Activator.apiConnector;
        VirtualNetwork virtualnetwork;
        try {
            String networkUUID = originalSubnet.getNetworkUUID();
            String subnetUUID = originalSubnet.getSubnetUUID();
            if (!(networkUUID.contains("-"))) {
                networkUUID = Utils.uuidFormater(networkUUID);
            }
            networkUUID = UUID.fromString(networkUUID).toString();
            if (!(subnetUUID.contains("-"))) {
                subnetUUID = Utils.uuidFormater(subnetUUID);
            }
            subnetUUID = UUID.fromString(subnetUUID).toString();
            virtualnetwork = (VirtualNetwork) apiConnector.findById(VirtualNetwork.class, networkUUID);
            List<ObjectReference<VnSubnetsType>> ipamRefs = virtualnetwork.getNetworkIpam();
            if (ipamRefs != null) {
                for (ObjectReference<VnSubnetsType> ref : ipamRefs) {
                    VnSubnetsType vnSubnetsType = ref.getAttr();
                    if (vnSubnetsType != null) {
                        List<VnSubnetsType.IpamSubnetType> subnets = vnSubnetsType.getIpamSubnets();
                        for (VnSubnetsType.IpamSubnetType subnetValue : subnets) {
                            boolean doesSubnetExist = subnetValue.getSubnetUuid().matches(subnetUUID);
                            if (doesSubnetExist) {
                                return HttpURLConnection.HTTP_OK;
                            } else {
                                LOGGER.warn(" No subnet exists for specified UUID..");
                                return HttpURLConnection.HTTP_BAD_REQUEST;
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.error("Exception :     " + e);
            return HttpURLConnection.HTTP_INTERNAL_ERROR;
        }
        LOGGER.warn("Subnet updation failed..");
        return HttpURLConnection.HTTP_BAD_REQUEST;
    }

    /**
     * Invoked to take action after a subnet has been updated.
     *
     * @param subnet
     *            An instance of modified Neutron Subnet object.
     */
    @Override
    public void neutronSubnetUpdated(NeutronSubnet subnet) {
        try {
            boolean ifSubnetExist = false;
            String networkUUID = subnet.getNetworkUUID();
            String subnetUUID = subnet.getSubnetUUID();
            if (!(networkUUID.contains("-"))) {
                networkUUID = Utils.uuidFormater(networkUUID);
            }
            networkUUID = UUID.fromString(networkUUID).toString();
            if (!(subnetUUID.contains("-"))) {
                subnetUUID = Utils.uuidFormater(subnetUUID);
            }
            subnetUUID = UUID.fromString(subnetUUID).toString();
            VirtualNetwork virtualnetwork = (VirtualNetwork) apiConnector.findById(VirtualNetwork.class, networkUUID);
            List<ObjectReference<VnSubnetsType>> ipamRefs = virtualnetwork.getNetworkIpam();
            if (ipamRefs != null) {
                for (ObjectReference<VnSubnetsType> ref : ipamRefs) {
                    VnSubnetsType vnSubnetsType = ref.getAttr();
                    if (vnSubnetsType != null) {
                        List<VnSubnetsType.IpamSubnetType> subnets = vnSubnetsType.getIpamSubnets();
                        for (VnSubnetsType.IpamSubnetType subnetValue : subnets) {
                            boolean doesSubnetExist = subnetValue.getSubnetUuid().matches(subnetUUID);
                            if (doesSubnetExist) {
                                // if(deltaSubnet.getGatewayIP() != null){
                                // //Cannot update default gateway, enableDHCP
                                // and cidr
                                // subnetValue.setDefaultGateway(deltaSubnet.getGatewayIP());
                                // }
                                // if(deltaSubnet.getEnableDHCP() != null){
                                // subnetValue.setEnableDhcp(deltaSubnet.isEnableDHCP());
                                // }
                                if (subnet.getName() != null) {
                                    subnetValue.setSubnetName(subnet.getName());
                                }
                                ifSubnetExist = true;
                            }
                        }
                    }
                }
            }
            if (ifSubnetExist) {
                boolean subnetUpdate = apiConnector.update(virtualnetwork);
                if (!subnetUpdate) {
                    LOGGER.warn("Subnet upadtion failed..");
                } else {
                    LOGGER.info(" Subnet " + subnet.getCidr() + " has been sucessfully updated. ");
                }
            } else {
                LOGGER.warn("Subnet upadtion failed..");
            }
            virtualnetwork = (VirtualNetwork) apiConnector.findById(VirtualNetwork.class, networkUUID);
            List<ObjectReference<VnSubnetsType>> ipamRef = virtualnetwork.getNetworkIpam();
            if (ipamRef != null) {
                for (ObjectReference<VnSubnetsType> ref : ipamRef) {
                    VnSubnetsType vnSubnetsType = ref.getAttr();
                    if (vnSubnetsType != null) {
                        List<VnSubnetsType.IpamSubnetType> subnets = vnSubnetsType.getIpamSubnets();
                        for (VnSubnetsType.IpamSubnetType subnetValue : subnets) {
                            boolean isSubnetUpdated = subnetValue.getSubnetName().matches(subnet.getName());
                            if (isSubnetUpdated) {
                                LOGGER.info("Subnet upadtion verified..");
                            } else {
                                LOGGER.warn("Subnet upadtion failed..");
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            LOGGER.error("Exception :     " + ex);
        }
    }

    /**
     * Invoked when a subnet deletion is requested to indicate if the specified
     * subnet can be deleted and then delete the subnet.
     *
     * @param subnet
     *            An instance of the Neutron Subnet object to be deleted.
     *
     * @return A HTTP status code to the deletion request.
     */
    @Override
    public int canDeleteSubnet(NeutronSubnet subnet) {
        apiConnector = Activator.apiConnector;
        VirtualNetwork virtualNetwork = null;
        String networkUUID = subnet.getNetworkUUID();
        String subnetUUID = subnet.getSubnetUUID();
        try {
            if (!(networkUUID.contains("-"))) {
                networkUUID = Utils.uuidFormater(networkUUID);
            }
            networkUUID = UUID.fromString(networkUUID).toString();
            if (!(subnetUUID.contains("-"))) {
                subnetUUID = Utils.uuidFormater(subnetUUID);
            }
            subnetUUID = UUID.fromString(subnetUUID).toString();
            virtualNetwork = (VirtualNetwork) apiConnector.findById(VirtualNetwork.class, networkUUID);
            boolean doesSubnetExist = subnetExists(virtualNetwork.getNetworkIpam(), subnet);
            if(virtualNetwork.getNetworkIpam()!=null){
                if (virtualNetwork.getNetworkIpam().get(0).getAttr().getIpamSubnets().size() == 1 && virtualNetwork.getFloatingIpPools() != null) {
                    LOGGER.error("Cannot Delete subnet / IP Block, Floating Pool(s) in use...");
                    return HttpURLConnection.HTTP_BAD_REQUEST;
                }
                }
            if (!doesSubnetExist) {
                LOGGER.error("No subnet exists with specified UUID...");
                return HttpURLConnection.HTTP_BAD_REQUEST;
            } else {
                return HttpURLConnection.HTTP_OK;
            }
        } catch (Exception ex) {
            LOGGER.error("Exception :     " + ex);
            return HttpURLConnection.HTTP_INTERNAL_ERROR;
        }
    }

    /**
     * Invoked to delete a specified subnet.
     *
     * @param subnet
     *            An instance of the Neutron Subnet object to be deleted.
     *
     * @param virtualNetwork
     *            An instance of the Virtual network object.
     *
     * @return A HTTP status code to the deletion request.
     */
    private boolean deleteSubnet(NeutronSubnet subnet, VirtualNetwork virtualNetwork) {
        try {
            VnSubnetsType.IpamSubnetType subnetVmType = null;
            VnSubnetsType vnSubnetsType = null;
            List<VnSubnetsType.IpamSubnetType> subnets = null;
            List<ObjectReference<VnSubnetsType>> ipamRefs = virtualNetwork.getNetworkIpam();
            if (ipamRefs != null) {
                for (ObjectReference<VnSubnetsType> ref : ipamRefs) {
                    vnSubnetsType = ref.getAttr();
                    if (vnSubnetsType != null) {
                        subnets = vnSubnetsType.getIpamSubnets();
                        for (VnSubnetsType.IpamSubnetType subnetValue : subnets) {
                            String[] ipPrefix = getIpPrefix(subnet);
                            boolean doesSubnetExist = subnetValue.getSubnet().getIpPrefix().matches(ipPrefix[0]);
                            if (doesSubnetExist) {
                                subnetVmType = subnetValue;
                            }
                        }
                    }
                }
                vnSubnetsType.clearIpamSubnets();
                for (VnSubnetsType.IpamSubnetType subnetVal : subnets) {
                    if (!subnetVal.getSubnet().getIpPrefix().matches(subnetVmType.getSubnet().getIpPrefix())) {
                        vnSubnetsType.addIpamSubnets(subnetVal);
                    }
                }
                if (vnSubnetsType.getIpamSubnets() != null) {
                    virtualNetwork.clearNetworkIpam();
                    String ipamId = apiConnector.findByName(NetworkIpam.class, null, "default-network-ipam");
                    NetworkIpam ipam = (NetworkIpam) apiConnector.findById(NetworkIpam.class, ipamId);
                    virtualNetwork.addNetworkIpam(ipam, vnSubnetsType);
                } else {
                    virtualNetwork.clearNetworkIpam();
                }
                return apiConnector.update(virtualNetwork);
            } else {
                LOGGER.error("Subnet deletion failed...");
                return false;
            }
        } catch (IOException ioEx) {
            LOGGER.error("Exception     : " + ioEx);
            return false;
        } catch (Exception ex) {
            LOGGER.error("Exception     : " + ex);
            return false;
        }
    }

    /**
     * Invoked to take action after a subnet has been deleted.
     *
     * @param subnet
     *            An instance of deleted Neutron Subnet object.
     */
    @Override
    public void neutronSubnetDeleted(NeutronSubnet subnet) {
        String networkUUID = subnet.getNetworkUUID();
        String subnetUUID = subnet.getSubnetUUID();
        VirtualNetwork virtualNetwork;
        try {
            if (!(networkUUID.contains("-"))) {
                networkUUID = Utils.uuidFormater(networkUUID);
            }
            networkUUID = UUID.fromString(networkUUID).toString();
            if (!(subnetUUID.contains("-"))) {
                subnetUUID = Utils.uuidFormater(subnetUUID);
            }
            subnetUUID = UUID.fromString(subnetUUID).toString();
            virtualNetwork = (VirtualNetwork) apiConnector.findById(VirtualNetwork.class, networkUUID);
            boolean subnetDelete = deleteSubnet(subnet, virtualNetwork);
            if (!subnetDelete) {
                LOGGER.error("Subnet deletion failed..");
            } else {
                LOGGER.info("Subnet " + subnet.getCidr() + " sucessfully deleted from network  : " + virtualNetwork.getUuid());
            }

            virtualNetwork = (VirtualNetwork) apiConnector.findById(VirtualNetwork.class, networkUUID);
            boolean ifSubnetExist = subnetExists(virtualNetwork.getNetworkIpam(), subnet);
            if (!ifSubnetExist) {
                LOGGER.info("Subnet deletion verified..");
            } else {
                LOGGER.warn("Subnet deletion failed..");
            }
        } catch (Exception ex) {
            LOGGER.error("Exception :    " + ex);
        }
    }

    boolean validGatewayIP(NeutronSubnet subnet, String ipAddress) {
        try {

            SubnetUtils util = new SubnetUtils(subnet.getCidr());
            SubnetInfo info = util.getInfo();
            boolean inRange = info.isInRange(ipAddress);
            if (!inRange) {
                return false;
            } else {
                // ip available in allocation pool
                Iterator<NeutronSubnet_IPAllocationPool> i = subnet.getAllocationPools().iterator();
                while (i.hasNext()) {
                    NeutronSubnet_IPAllocationPool pool = i.next();
                    if (pool.contains(ipAddress)) {
                        return true;
                    }
                }
                return true;
            }
        } catch (Exception e) {
            LOGGER.error("Exception  :  " + e);
            return false;
        }
    }
}