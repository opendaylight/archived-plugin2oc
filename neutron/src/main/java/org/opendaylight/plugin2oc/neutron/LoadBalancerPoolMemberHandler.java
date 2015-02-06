package org.opendaylight.plugin2oc.neutron;

import java.util.List;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.UUID;

import net.juniper.contrail.api.ApiConnector;
import net.juniper.contrail.api.ApiPropertyBase;
import net.juniper.contrail.api.ObjectReference;
import net.juniper.contrail.api.types.InstanceIp;
import net.juniper.contrail.api.types.LoadbalancerMember;
import net.juniper.contrail.api.types.LoadbalancerMemberType;
import net.juniper.contrail.api.types.LoadbalancerPool;
import net.juniper.contrail.api.types.Project;
import net.juniper.contrail.api.types.VirtualMachineInterface;

import org.opendaylight.controller.networkconfig.neutron.INeutronLoadBalancerPoolMemberAware;
import org.opendaylight.controller.networkconfig.neutron.NeutronLoadBalancerPoolMember;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle requests for Neutron LoadbalancerPoolMember.
 */

public class LoadBalancerPoolMemberHandler implements INeutronLoadBalancerPoolMemberAware {
    /**
     * Logger instance.
     */
    static final Logger LOGGER = LoggerFactory.getLogger(LoadBalancerPoolMemberHandler.class);
    static ApiConnector apiConnector;

    /**
     * Invoked when a member creation is requested to check if the specified
     * member can be created and then creates the member
     *
     * @param loadBalancerPoolMember
     *            An instance of proposed new NeutronLoadBalancerPoolMember object.
     *
     * @return A HTTP status code to the creation request.
     */
    @Override
    public int canCreateNeutronLoadBalancerPoolMember(NeutronLoadBalancerPoolMember loadBalancerPoolMember) {
        if (loadBalancerPoolMember == null) {
            LOGGER.error("LoadBalancerPool Member object can't be null..");
            return HttpURLConnection.HTTP_BAD_REQUEST;
        }
        apiConnector = Activator.apiConnector;
        if (loadBalancerPoolMember.getPoolMemberTenantID() == null
                || loadBalancerPoolMember.getPoolMemberSubnetID() == null) {
            LOGGER.error("LoadBalancerPool Member TenanID/SubnetID can not be null");
            return HttpURLConnection.HTTP_BAD_REQUEST;
        }
        try {
            String loadBalancerPoolID = loadBalancerPoolMember.getPoolID();
            String loadBalancerPoolMemberUUID = loadBalancerPoolMember.getPoolMemberID();
            String projectUUID = loadBalancerPoolMember.getPoolMemberTenantID();
            try {
                if (!(loadBalancerPoolMemberUUID.contains("-"))) {
                    loadBalancerPoolMemberUUID = Utils.uuidFormater(loadBalancerPoolMemberUUID);
                }
                if (!(projectUUID.contains("-"))) {
                    projectUUID = Utils.uuidFormater(projectUUID);
                }
                boolean isValidLoadBalancerPoolMemberUUID = Utils.isValidHexNumber(loadBalancerPoolMemberUUID);
                boolean isValidprojectUUID = Utils.isValidHexNumber(projectUUID);
                if (!isValidLoadBalancerPoolMemberUUID || !isValidprojectUUID) {
                    LOGGER.info("Badly formed Hexadecimal UUID...");
                    return HttpURLConnection.HTTP_BAD_REQUEST;
                }
                projectUUID = UUID.fromString(projectUUID).toString();
                loadBalancerPoolMemberUUID = UUID.fromString(loadBalancerPoolMemberUUID).toString();
                if (!(loadBalancerPoolID.contains("-"))) {
                    loadBalancerPoolID = Utils.uuidFormater(loadBalancerPoolID);
                }
                loadBalancerPoolID = UUID.fromString(loadBalancerPoolID).toString();
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
            if (project.getVirtualMachineInterfaces() != null) {
                List<ObjectReference<ApiPropertyBase>> vmiList = project.getVirtualMachineInterfaces();
                for (ObjectReference<ApiPropertyBase> ref : vmiList) {
                    String vmiUUID = ref.getUuid();
                    VirtualMachineInterface vmi = (VirtualMachineInterface) apiConnector.findById(
                            VirtualMachineInterface.class, vmiUUID);
                    List<ObjectReference<ApiPropertyBase>> iip = vmi.getInstanceIpBackRefs();
                    for (ObjectReference<ApiPropertyBase> iipRef : iip) {
                        String iipUUID = iipRef.getUuid();
                        InstanceIp instanceIP = (InstanceIp) apiConnector.findById(InstanceIp.class, iipUUID);
                        if (!(loadBalancerPoolMember.getPoolMemberAddress().equals(instanceIP.getAddress()))) {
                            LOGGER.warn("LoadbalancerPool Member address does not exists...");
                            return HttpURLConnection.HTTP_FORBIDDEN;
                        }
                    }
                }
            } else {
                LOGGER.warn("No Servers available to create a member...");
                return HttpURLConnection.HTTP_FORBIDDEN;
            }
            LoadbalancerMember virtualLoadbalancerPoolMemberById = (LoadbalancerMember) apiConnector.findById(
                    LoadbalancerMember.class, loadBalancerPoolMemberUUID);
            if (virtualLoadbalancerPoolMemberById != null) {
                LOGGER.warn("LoadbalancerPool Member already exists with UUID" + loadBalancerPoolMemberUUID);
                return HttpURLConnection.HTTP_FORBIDDEN;
            }
            LoadbalancerPool virtualLoadbalancerPool = (LoadbalancerPool) apiConnector.findById(LoadbalancerPool.class,
                    loadBalancerPoolID);
            if (virtualLoadbalancerPool == null) {
                LOGGER.warn("LoadbalancerPool does not exist" + loadBalancerPoolID);
                return HttpURLConnection.HTTP_FORBIDDEN;
            }
            if (!(loadBalancerPoolMember.getPoolMemberTenantID().equals(virtualLoadbalancerPool.getParentUuid()))) {
                LOGGER.warn("Member with UUID: " + loadBalancerPoolID + "and Pool with UUID: " + loadBalancerPoolID
                        + " does not belong to same tenant");
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
     * Invoked to take action after a member has been created.
     *
     * @param loadBalancerPoolMember
     *            An instance of new NeutronLoadBalancerPoolMember object.
     */
    @Override
    public void neutronLoadBalancerPoolMemberCreated(NeutronLoadBalancerPoolMember loadBalancerPoolMember) {
        try {
            createLoadBalancerMember(loadBalancerPoolMember);
        } catch (IOException ex) {
            LOGGER.warn("Exception  :    " + ex);
        }
        LoadbalancerMember loadbalancerMember = null;
        try {
            String loadBalancerPoolMemberUUID = loadBalancerPoolMember.getPoolMemberID();
            if (!(loadBalancerPoolMemberUUID.contains("-"))) {
                loadBalancerPoolMemberUUID = Utils.uuidFormater(loadBalancerPoolMemberUUID);
            }
            loadBalancerPoolMemberUUID = UUID.fromString(loadBalancerPoolMemberUUID).toString();
            loadbalancerMember = (LoadbalancerMember) apiConnector.findById(LoadbalancerMember.class,
                    loadBalancerPoolMemberUUID);
            if (loadbalancerMember != null) {
                LOGGER.info("LoadbalancerPool Member creation verified for Member with UUID--"
                        + loadBalancerPoolMemberUUID);
            }
        } catch (Exception e) {
            LOGGER.error("Exception :     " + e);
        }

    }

    /**
     * Invoked to create the specified Neutron Member.
     *
     * @param loadBalancerPoolMember
     *            An instance of new NeutronLoadBalancerPoolMember object.
     */
    private void createLoadBalancerMember(NeutronLoadBalancerPoolMember loadBalancerPoolMember) throws IOException {
        LoadbalancerMember virtualLoadBalancerMember = new LoadbalancerMember();
        virtualLoadBalancerMember = mapLoadBalancerMemberProperties(loadBalancerPoolMember, virtualLoadBalancerMember);
        boolean loadBalancerMemberCreated;
        try {
            loadBalancerMemberCreated = apiConnector.create(virtualLoadBalancerMember);
            LOGGER.debug("loadBalancerPool member:   " + loadBalancerMemberCreated);
            if (!loadBalancerMemberCreated) {
                LOGGER.info("loadBalancerPool member creation failed..");
            }
        } catch (Exception Ex) {
            LOGGER.error("Exception : " + Ex);
        }
        LOGGER.info("Member having UUID " + loadBalancerPoolMember.getPoolMemberID() + " sucessfully created");
    }

    @Override
    public int canUpdateNeutronLoadBalancerPoolMember(NeutronLoadBalancerPoolMember delta,
            NeutronLoadBalancerPoolMember original) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void neutronLoadBalancerPoolMemberUpdated(NeutronLoadBalancerPoolMember loadBalancerPoolMember) {
        // TODO Auto-generated method stub

    }

    /**
     * Invoked when a member deletion is requested to indicate if the specified
     * member can be deleted.
     *
     * @param loadBalancerPoolMember
     *            An instance of the NeutronLoadBalancerPoolMember object to be deleted.
     * @return A HTTP status code to the deletion request.
     */
    @Override
    public int canDeleteNeutronLoadBalancerPoolMember(NeutronLoadBalancerPoolMember loadBalancerPoolMember) {
        apiConnector = Activator.apiConnector;
        LoadbalancerMember virtualLoadBalancerMember = null;
        String loadBalancerMemberUUID = loadBalancerPoolMember.getPoolMemberID();
        try {
            if (!(loadBalancerMemberUUID.contains("-"))) {
                loadBalancerMemberUUID = Utils.uuidFormater(loadBalancerMemberUUID);
            }
            loadBalancerMemberUUID = UUID.fromString(loadBalancerMemberUUID).toString();
        } catch (Exception ex) {
            LOGGER.error("UUID input incorrect", ex);
            return HttpURLConnection.HTTP_BAD_REQUEST;
        }
        try {
            virtualLoadBalancerMember = (LoadbalancerMember) apiConnector.findById(LoadbalancerMember.class,
                    loadBalancerMemberUUID);
            if (virtualLoadBalancerMember == null) {
                LOGGER.info("No LoadbalancerPoolMember exists with ID :  " + loadBalancerMemberUUID);
                return HttpURLConnection.HTTP_BAD_REQUEST;
            }
            return HttpURLConnection.HTTP_OK;
        } catch (Exception e) {
            LOGGER.error("Exception : " + e);
            return HttpURLConnection.HTTP_INTERNAL_ERROR;
        }
    }

    /**
     * Invoked to take action after a member has been deleted.
     *
     * @param loadBalancerPoolMember
     *            An instance of deleted NeutronLoadBalancerPoolMember object.
     */
    @Override
    public void neutronLoadBalancerPoolMemberDeleted(NeutronLoadBalancerPoolMember loadBalancerPoolMember) {
        LoadbalancerMember virtualLoadBalancerMember = null;
        try {
            String loadBalancerMemberUUID = loadBalancerPoolMember.getPoolMemberID();
            if (!(loadBalancerMemberUUID.contains("-"))) {
                loadBalancerMemberUUID = Utils.uuidFormater(loadBalancerMemberUUID);
            }
            loadBalancerMemberUUID = UUID.fromString(loadBalancerMemberUUID).toString();
            virtualLoadBalancerMember = (LoadbalancerMember) apiConnector.findById(LoadbalancerMember.class,
                    loadBalancerMemberUUID);
            apiConnector.delete(virtualLoadBalancerMember);
            if (virtualLoadBalancerMember == null) {
                LOGGER.info("LoadbalancerPoolMember deletion verified....");
            } else {
                LOGGER.info("LoadbalancerPoolMember with ID :  " + loadBalancerMemberUUID + "deletion failed");
            }
        } catch (Exception ex) {
            LOGGER.error("Exception :   " + ex);
        }
    }

    /**
     * Invoked to map the NeutronLoadBalancerPoolMember object properties to the loadBalancerPoolMember
     * object.
     *
     * @param loadBalancerPoolMember
     *            An instance of new NeutronLoadBalancerPoolMember object.
     * @param virtualLoadBalancerMember
     *            An instance of new LoadbalancerMember object.
     * @return {@link LoadbalancerMember}
     */
    private LoadbalancerMember mapLoadBalancerMemberProperties(NeutronLoadBalancerPoolMember loadBalancerPoolMember,
            LoadbalancerMember virtualLoadBalancerMember) {
        String loadBalancerMemberUUID = loadBalancerPoolMember.getPoolMemberID();
        try {
            if (!(loadBalancerMemberUUID.contains("-"))) {
                loadBalancerMemberUUID = Utils.uuidFormater(loadBalancerMemberUUID);
            }
            loadBalancerMemberUUID = UUID.fromString(loadBalancerMemberUUID).toString();
            LoadbalancerPool lbp = (LoadbalancerPool) apiConnector.findById(LoadbalancerPool.class,
                    loadBalancerPoolMember.getPoolID());
            virtualLoadBalancerMember.setParent(lbp);
        } catch (Exception ex) {
            LOGGER.error("UUID input incorrect", ex);
        }
        LoadbalancerMemberType lbmType = new LoadbalancerMemberType();
        lbmType.setAddress(loadBalancerPoolMember.getPoolMemberAddress());
        lbmType.setProtocolPort(loadBalancerPoolMember.getPoolMemberProtoPort());
        if (loadBalancerPoolMember.getPoolMemberStatus() != null) {
            lbmType.setStatus(loadBalancerPoolMember.getPoolMemberStatus());
        }
        if (loadBalancerPoolMember.getPoolMemberWeight() != null) {
            lbmType.setWeight(loadBalancerPoolMember.getPoolMemberWeight());
        }
        if (loadBalancerPoolMember.getPoolMemberAdminStateIsUp() != null) {
            lbmType.setAdminState(loadBalancerPoolMember.getPoolMemberAdminStateIsUp());
        } else {
            lbmType.setAdminState(true);
        }
        virtualLoadBalancerMember.setProperties(lbmType);
        virtualLoadBalancerMember.setUuid(loadBalancerMemberUUID);
        virtualLoadBalancerMember.setName(loadBalancerMemberUUID);
        virtualLoadBalancerMember.setDisplayName(loadBalancerMemberUUID);
        return virtualLoadBalancerMember;
    }

}
