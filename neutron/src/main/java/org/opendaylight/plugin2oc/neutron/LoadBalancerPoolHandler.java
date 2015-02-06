package org.opendaylight.plugin2oc.neutron;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.UUID;

import net.juniper.contrail.api.ApiConnector;
import net.juniper.contrail.api.types.LoadbalancerMember;
import net.juniper.contrail.api.types.LoadbalancerPool;
import net.juniper.contrail.api.types.LoadbalancerPoolType;
import net.juniper.contrail.api.types.Project;

import org.opendaylight.controller.networkconfig.neutron.INeutronLoadBalancerPoolAware;
import org.opendaylight.controller.networkconfig.neutron.NeutronLoadBalancerPool;
import org.opendaylight.controller.networkconfig.neutron.NeutronLoadBalancerPoolMember;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle requests for Neutron LoadbalancerPool.
 */

public class LoadBalancerPoolHandler implements INeutronLoadBalancerPoolAware {
    /**
     * Logger instance.
     */
    static final Logger LOGGER = LoggerFactory.getLogger(LoadBalancerPoolHandler.class);
    static ApiConnector apiConnector;

    /**
     * Invoked when a pool creation is requested to check if the specified
     * pool can be created and then creates the pool
     *
     * @param loadBalancerPool
     *            An instance of proposed new Neutron Pool object.
     *
     * @return A HTTP status code to the creation request.
     */
    @Override
    public int canCreateNeutronLoadBalancerPool(NeutronLoadBalancerPool loadBalancerPool) {
        if (loadBalancerPool == null) {
            LOGGER.error("LoadBalancerPool object can't be null..");
            return HttpURLConnection.HTTP_BAD_REQUEST;
        }
        apiConnector = Activator.apiConnector;
        if (loadBalancerPool.getLoadBalancerPoolTenantID() == null) {
            LOGGER.error("LoadBalancerPool tenant Id can not be null");
            return HttpURLConnection.HTTP_BAD_REQUEST;
        }
        if (loadBalancerPool.getLoadBalancerPoolLbAlgorithm() == null) {
            LOGGER.error("LoadBalancerPool Algorithm can not be null");
            return HttpURLConnection.HTTP_BAD_REQUEST;
        }
        if (!(loadBalancerPool.getLoadBalancerPoolLbAlgorithm().equals("ROUND_ROBIN")
                || loadBalancerPool.getLoadBalancerPoolLbAlgorithm().equals("LEAST_CONNECTIONS") || loadBalancerPool
                .getLoadBalancerPoolLbAlgorithm().equals("Source IP"))) {
            LOGGER.error("LoadBalancerPool Algorithm can not be anything other than ROUND_ROBIN and LEAST_CONNECTIONS and Source IP");
            return HttpURLConnection.HTTP_BAD_REQUEST;
        }
        if (loadBalancerPool.getLoadBalancerPoolProtocol() == null) {
            LOGGER.error("LoadBalancerPool protocol can not be null");
            return HttpURLConnection.HTTP_BAD_REQUEST;
        }
        if (!(loadBalancerPool.getLoadBalancerPoolProtocol().equals("TCP")
                || loadBalancerPool.getLoadBalancerPoolProtocol().equals("HTTP") || loadBalancerPool
                .getLoadBalancerPoolProtocol().equals("HTTPS"))) {
            LOGGER.error("LoadBalancerPool Protocol can not be other than TCP/HTTP/HTTPS");
            return HttpURLConnection.HTTP_BAD_REQUEST;
        }
        if (loadBalancerPool.getLoadBalancerPoolMembers() != null) {
            List<NeutronLoadBalancerPoolMember> i = loadBalancerPool.getLoadBalancerPoolMembers();
            for (NeutronLoadBalancerPoolMember ref : i) {
                String poolmemberID = ref.getPoolMemberID();
                String tenantID = ref.getPoolMemberTenantID();
                if (!(tenantID.equals(loadBalancerPool.getLoadBalancerPoolTenantID()))) {
                    LOGGER.error("Member and pool does not belong to same tenant");
                    return HttpURLConnection.HTTP_BAD_REQUEST;
                }
                try {
                    LoadbalancerMember lbpm = (LoadbalancerMember) apiConnector.findById(LoadbalancerMember.class,
                            poolmemberID);
                    if (lbpm != null) {
                        LOGGER.error("Member already exist with UUID: " + poolmemberID);
                        return HttpURLConnection.HTTP_BAD_REQUEST;
                    }
                } catch (IOException e) {
                    LOGGER.error("IOException :   " + e);
                    return HttpURLConnection.HTTP_INTERNAL_ERROR;
                }
            }
        }
        try {
            String loadBalancerPoolUUID = loadBalancerPool.getLoadBalancerPoolID();
            String projectUUID = loadBalancerPool.getLoadBalancerPoolTenantID();
            try {
                if (!(loadBalancerPoolUUID.contains("-"))) {
                    loadBalancerPoolUUID = Utils.uuidFormater(loadBalancerPoolUUID);
                }
                if (!(projectUUID.contains("-"))) {
                    projectUUID = Utils.uuidFormater(projectUUID);
                }
                boolean isValidLoadBalancerPoolUUID = Utils.isValidHexNumber(loadBalancerPoolUUID);
                boolean isValidprojectUUID = Utils.isValidHexNumber(projectUUID);
                if (!isValidLoadBalancerPoolUUID || !isValidprojectUUID) {
                    LOGGER.info("Badly formed Hexadecimal UUID...");
                    return HttpURLConnection.HTTP_BAD_REQUEST;
                }
                projectUUID = UUID.fromString(projectUUID).toString();
                loadBalancerPoolUUID = UUID.fromString(loadBalancerPoolUUID).toString();
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
            String virtualLoadbalancerPoolByName = apiConnector.findByName(LoadbalancerPool.class, project,
                    loadBalancerPool.getLoadBalancerPoolName());
            if (virtualLoadbalancerPoolByName != null) {
                LOGGER.warn("POOL already exists with name : " + virtualLoadbalancerPoolByName);
                return HttpURLConnection.HTTP_FORBIDDEN;
            }
            LoadbalancerPool virtualLoadbalancerPoolById = (LoadbalancerPool) apiConnector.findById(
                    LoadbalancerPool.class, loadBalancerPoolUUID);
            if (virtualLoadbalancerPoolById != null) {
                LOGGER.warn("LoadbalancerPool already exists with UUID" + loadBalancerPoolUUID);
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
     * Invoked to take action after a pool has been created.
     *
     * @param loadBalancerPool
     *            An instance of new Neutron Pool object.
     */
    @Override
    public void neutronLoadBalancerPoolCreated(NeutronLoadBalancerPool loadBalancerPool) {
        try {
            createLoadBalancerPool(loadBalancerPool);
        } catch (IOException ex) {
            LOGGER.warn("Exception  :    " + ex);
        }
        LoadbalancerPool virtualLoadBalancerPool = null;
        try {
            String loadBalancerPoolUUID = loadBalancerPool.getLoadBalancerPoolID();
            if (!(loadBalancerPoolUUID.contains("-"))) {
                loadBalancerPoolUUID = Utils.uuidFormater(loadBalancerPoolUUID);
            }
            loadBalancerPoolUUID = UUID.fromString(loadBalancerPoolUUID).toString();
            virtualLoadBalancerPool = (LoadbalancerPool) apiConnector.findById(LoadbalancerPool.class,
                    loadBalancerPoolUUID);
            if (virtualLoadBalancerPool != null) {
                LOGGER.info("LoadbalancerPool creation verified....");
            } else {
                LOGGER.info("LoadbalancerPool creation failed...");
            }
        } catch (Exception e) {
            LOGGER.error("Exception :     " + e);
        }
    }

    /**
     * Invoked to create the specified Neutron Pool.
     *
     * @param loadBalancerPool
     *            An instance of new Neutron Pool object.
     */
    private void createLoadBalancerPool(NeutronLoadBalancerPool loadBalancerPool) throws IOException {
        LoadbalancerPool virtualLoadBalancerPool = new LoadbalancerPool();
        virtualLoadBalancerPool = mapLoadBalancerPoolProperties(loadBalancerPool, virtualLoadBalancerPool);
        boolean loadBalancerPoolCreated;
        try {
            loadBalancerPoolCreated = apiConnector.create(virtualLoadBalancerPool);
            LOGGER.debug("loadBalancerPool:   " + loadBalancerPoolCreated);
            if (!loadBalancerPoolCreated) {
                LOGGER.info("loadBalancerPool creation failed..");
            }
        } catch (Exception Ex) {
            LOGGER.error("Exception : " + Ex);
        }
        LOGGER.info("loadBalancerPool:" + loadBalancerPool.getLoadBalancerPoolName() + "having ID"
                + loadBalancerPool.getLoadBalancerPoolID() + "succesfully created.");
        if (loadBalancerPool.getLoadBalancerPoolMembers() != null) {
            List<NeutronLoadBalancerPoolMember> i = loadBalancerPool.getLoadBalancerPoolMembers();
            for (NeutronLoadBalancerPoolMember ref : i) {
                LoadBalancerPoolMemberHandler lbmh = new LoadBalancerPoolMemberHandler();
                int value = lbmh.canCreateNeutronLoadBalancerPoolMember(ref);
                if (value == 200) {
                    lbmh.neutronLoadBalancerPoolMemberCreated(ref);
                } else {
                    LOGGER.error("NeutronLoadBalancerPool Member creation failed");
                }
            }
        }
    }

    /**
     * Invoked when a pool update is requested to indicate if the specified
     * pool can be changed using the specified delta.
     *
     * @param delta
     *            Updates to the pool object using patch semantics.
     * @param original
     *            An instance of the Neutron pool object to be updated.
     * @return A HTTP status code to the update request.
     */
    @Override
    public int canUpdateNeutronLoadBalancerPool(NeutronLoadBalancerPool delta, NeutronLoadBalancerPool original) {
        apiConnector = Activator.apiConnector;
        LoadbalancerPool virtualLoadBalancerPool;
        if (delta == null || original == null) {
            LOGGER.error("NeutronLoadBalancerPool objects cant be empty or null");
            return HttpURLConnection.HTTP_BAD_REQUEST;
        }
        String loadBalancerPoolUUID = original.getLoadBalancerPoolID();
        try {
            if (!(loadBalancerPoolUUID.contains("-"))) {
                loadBalancerPoolUUID = Utils.uuidFormater(loadBalancerPoolUUID);
            }
            loadBalancerPoolUUID = UUID.fromString(loadBalancerPoolUUID).toString();
        } catch (Exception ex) {
            LOGGER.error("UUID input incorrect", ex);
            return HttpURLConnection.HTTP_BAD_REQUEST;
        }
        try {
            virtualLoadBalancerPool = (LoadbalancerPool) apiConnector.findById(LoadbalancerPool.class,
                    loadBalancerPoolUUID);
            if (virtualLoadBalancerPool == null) {
                LOGGER.error("No LoadbalancerPool exists for the specified ID...");
                return HttpURLConnection.HTTP_BAD_REQUEST;
            }
        } catch (IOException e) {
            return HttpURLConnection.HTTP_INTERNAL_ERROR;
        }

        return HttpURLConnection.HTTP_OK;
    }

    @Override
    public void neutronLoadBalancerPoolUpdated(NeutronLoadBalancerPool loadBalancerPool) {
        // TODO Auto-generated method stub
    }

    /**
     * Invoked when a pool deletion is requested to indicate if the specified
     * pool can be deleted.
     *
     * @param loadBalancerPool
     *            An instance of the NeutronLoadBalancerPool object to be deleted.
     * @return A HTTP status code to the deletion request.
     */
    @Override
    public int canDeleteNeutronLoadBalancerPool(NeutronLoadBalancerPool loadBalancerPool) {
        apiConnector = Activator.apiConnector;
        LoadbalancerPool virtualLoadBalancerPool = null;
        String loadBalancerPoolUUID = loadBalancerPool.getLoadBalancerPoolID();
        try {
            if (!(loadBalancerPoolUUID.contains("-"))) {
                loadBalancerPoolUUID = Utils.uuidFormater(loadBalancerPoolUUID);
            }
            loadBalancerPoolUUID = UUID.fromString(loadBalancerPoolUUID).toString();
        } catch (Exception ex) {
            LOGGER.error("UUID input incorrect", ex);
            return HttpURLConnection.HTTP_BAD_REQUEST;
        }
        try {
            virtualLoadBalancerPool = (LoadbalancerPool) apiConnector.findById(LoadbalancerPool.class,
                    loadBalancerPoolUUID);
            if (virtualLoadBalancerPool == null) {
                LOGGER.info("No LoadbalancerPool exists with ID :  " + loadBalancerPoolUUID);
                return HttpURLConnection.HTTP_BAD_REQUEST;
            }
            return HttpURLConnection.HTTP_OK;
        } catch (Exception e) {
            LOGGER.error("Exception : " + e);
            return HttpURLConnection.HTTP_INTERNAL_ERROR;
        }
    }

    /**
     * Invoked to take action after a pool has been deleted.
     *
     * @param loadBalancerPool
     *            An instance of deleted NeutronLoadBalancerPool object.
     */
    @Override
    public void neutronLoadBalancerPoolDeleted(NeutronLoadBalancerPool loadBalancerPool) {
        LoadbalancerPool virtualLoadBalancerPool = null;
        try {
            String loadBalancerPoolUUID = loadBalancerPool.getLoadBalancerPoolID();
            if (!(loadBalancerPoolUUID.contains("-"))) {
                loadBalancerPoolUUID = Utils.uuidFormater(loadBalancerPoolUUID);
            }
            loadBalancerPoolUUID = UUID.fromString(loadBalancerPoolUUID).toString();
            virtualLoadBalancerPool = (LoadbalancerPool) apiConnector.findById(LoadbalancerPool.class,
                    loadBalancerPoolUUID);
            apiConnector.delete(virtualLoadBalancerPool);
            if (virtualLoadBalancerPool == null) {
                LOGGER.info("LoadbalancerPool deletion verified....");
            } else {
                LOGGER.info("LoadbalancerPool with ID :  " + loadBalancerPoolUUID + "deletion failed");
            }
        } catch (Exception ex) {
            LOGGER.error("Exception :   " + ex);
        }
    }

    /**
     * Invoked to map the NeutronLoadBalancerPool object properties to the LoadbalancerPool
     * object.
     *
     * @param loadBalancerPool
     *            An instance of new NeutronLoadBalancerPool object.
     * @param virtualLoadBalancerPool
     *            An instance of new LoadbalancerPool object.
     * @return {@link LoadbalancerPool}
     */
    private LoadbalancerPool mapLoadBalancerPoolProperties(NeutronLoadBalancerPool loadBalancerPool,
            LoadbalancerPool virtualLoadBalancerPool) {
        String loadBalancerPoolUUID = loadBalancerPool.getLoadBalancerPoolID();
        String loadBalancerPoolName = loadBalancerPool.getLoadBalancerPoolName();
        String projectUUID = loadBalancerPool.getLoadBalancerPoolTenantID();
        try {
            if (!(loadBalancerPoolUUID.contains("-"))) {
                loadBalancerPoolUUID = Utils.uuidFormater(loadBalancerPoolUUID);
            }
            loadBalancerPoolUUID = UUID.fromString(loadBalancerPoolUUID).toString();
            if (!(projectUUID.contains("-"))) {
                projectUUID = Utils.uuidFormater(projectUUID);
            }
            projectUUID = UUID.fromString(projectUUID).toString();
            Project project = (Project) apiConnector.findById(Project.class, projectUUID);
            virtualLoadBalancerPool.setParent(project);
        } catch (Exception ex) {
            LOGGER.error("UUID input incorrect", ex);
        }
        LoadbalancerPoolType loadbalancer_pool_properties = new LoadbalancerPoolType();
        loadbalancer_pool_properties.setLoadbalancerMethod(loadBalancerPool.getLoadBalancerPoolLbAlgorithm());
        loadbalancer_pool_properties.setProtocol(loadBalancerPool.getLoadBalancerPoolProtocol());
        if (loadBalancerPool.getLoadBalancerPoolAdminIsStateIsUp() != null) {
            loadbalancer_pool_properties.setAdminState(loadBalancerPool.getLoadBalancerPoolAdminIsStateIsUp());
        } else {
            loadbalancer_pool_properties.setAdminState(true);
        }
        if (loadBalancerPool.getLoadBalancerPoolStatus() != null) {
            loadbalancer_pool_properties.setStatus(loadBalancerPool.getLoadBalancerPoolStatus());
        }
        if (loadBalancerPool.getLoadBalancerPoolDescription() != null) {
            loadbalancer_pool_properties.setStatusDescription(loadBalancerPool.getLoadBalancerPoolDescription());
        }
        virtualLoadBalancerPool.setUuid(loadBalancerPoolUUID);
        virtualLoadBalancerPool.setName(loadBalancerPoolName);
        virtualLoadBalancerPool.setDisplayName(loadBalancerPoolName);
        virtualLoadBalancerPool.setProperties(loadbalancer_pool_properties);
        /* haproxy is the provider for loadbalancer pool in OpenContrail */
        virtualLoadBalancerPool.setProvider("haproxy");
        return virtualLoadBalancerPool;
    }

}
