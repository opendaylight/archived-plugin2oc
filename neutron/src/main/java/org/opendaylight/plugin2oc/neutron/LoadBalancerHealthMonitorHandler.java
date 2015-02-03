package org.opendaylight.plugin2oc.neutron;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.UUID;

import net.juniper.contrail.api.ApiConnector;
import net.juniper.contrail.api.types.LoadbalancerHealthmonitor;
import net.juniper.contrail.api.types.LoadbalancerHealthmonitorType;
import net.juniper.contrail.api.types.Project;

import org.opendaylight.controller.networkconfig.neutron.INeutronLoadBalancerHealthMonitorAware;
import org.opendaylight.controller.networkconfig.neutron.NeutronLoadBalancerHealthMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle requests for Neutron LoadBalancerHealthMonitor.
 */
public class LoadBalancerHealthMonitorHandler implements INeutronLoadBalancerHealthMonitorAware {

    /**
     * Logger instance.
     */
    static final Logger LOGGER = LoggerFactory.getLogger(LoadBalancerHealthMonitorHandler.class);
    static ApiConnector apiConnector;

    /**
     * Invoked when a health monitor creation is requested to check if the specified
     * health monitor can be created and then creates the health monitor
     *
     * @param loadBalancerHealthMonitor
     *            An instance of proposed new NeutronLoadBalancerHealthMonitor object.
     *
     * @return A HTTP status code to the creation request.
     */
    @Override
    public int canCreateNeutronLoadBalancerHealthMonitor(NeutronLoadBalancerHealthMonitor loadBalancerHealthMonitor) {
        if (loadBalancerHealthMonitor == null) {
            LOGGER.error("LoadBalancerHealthMonitor object can't be null..");
            return HttpURLConnection.HTTP_BAD_REQUEST;
        }
        apiConnector = Activator.apiConnector;
        if (loadBalancerHealthMonitor.getLoadBalancerHealthMonitorTenantID() == null) {
            LOGGER.error("LoadBalancerHealthMonitor tenant Id can not be null");
            return HttpURLConnection.HTTP_BAD_REQUEST;
        }
        try {
            String loadBalancerHealthMonitorUUID = loadBalancerHealthMonitor.getLoadBalancerHealthMonitorID();
            String projectUUID = loadBalancerHealthMonitor.getLoadBalancerHealthMonitorTenantID();
            try {
                if (!(loadBalancerHealthMonitorUUID.contains("-"))) {
                    loadBalancerHealthMonitorUUID = Utils.uuidFormater(loadBalancerHealthMonitorUUID);
                }
                if (!(projectUUID.contains("-"))) {
                    projectUUID = Utils.uuidFormater(projectUUID);
                }
                boolean isValidLoadBalancerHealthMonitorUUID = Utils.isValidHexNumber(loadBalancerHealthMonitorUUID);
                boolean isValidprojectUUID = Utils.isValidHexNumber(projectUUID);
                if (!isValidLoadBalancerHealthMonitorUUID || !isValidprojectUUID) {
                    LOGGER.info("Badly formed Hexadecimal UUID...");
                    return HttpURLConnection.HTTP_BAD_REQUEST;
                }
                projectUUID = UUID.fromString(projectUUID).toString();
                loadBalancerHealthMonitorUUID = UUID.fromString(loadBalancerHealthMonitorUUID).toString();
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
            LoadbalancerHealthmonitor virtualHealthMonitorByID = (LoadbalancerHealthmonitor) apiConnector.findById(
                    LoadbalancerHealthmonitor.class, loadBalancerHealthMonitorUUID);
            if (virtualHealthMonitorByID != null) {
                LOGGER.warn("LoadBalancerHealthMonitor already exists with UUID" + virtualHealthMonitorByID);
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
     * Invoked to take action after a health monitor has been created.
     *
     * @param loadBalancerHealthMonitor
     *            An instance of new NeutronLoadBalancerHealthMonitor object.
     */
    @Override
    public void neutronLoadBalancerHealthMonitorCreated(NeutronLoadBalancerHealthMonitor loadBalancerHealthMonitor) {
        try {
            createHealthMonitor(loadBalancerHealthMonitor);
        } catch (IOException ex) {
            LOGGER.warn("Exception  :    " + ex);
        }
        LoadbalancerHealthmonitor virtuaLoadbalancerHealthMonitor = null;
        try {
            String loadBalancerHealthMonitorUUID = loadBalancerHealthMonitor.getLoadBalancerHealthMonitorID();
            if (!(loadBalancerHealthMonitorUUID.contains("-"))) {
                loadBalancerHealthMonitorUUID = Utils.uuidFormater(loadBalancerHealthMonitorUUID);
            }
            loadBalancerHealthMonitorUUID = UUID.fromString(loadBalancerHealthMonitorUUID).toString();
            virtuaLoadbalancerHealthMonitor = (LoadbalancerHealthmonitor) apiConnector.findById(
                    LoadbalancerHealthmonitor.class, loadBalancerHealthMonitorUUID);
            if (virtuaLoadbalancerHealthMonitor != null) {
                LOGGER.info("LoadBalancerHealthMonitor creation verified....");
            } else {
                LOGGER.info("LoadBalancerHealthMonitor creation failed...");
            }
        } catch (Exception e) {
            LOGGER.error("Exception :     " + e);
        }
    }

    /**
     * Invoked to create the specified NeutronLoadBalancerHealthMonitor.
     *
     * @param loadBalancerHealthMonitor
     *            An instance of new NeutronLoadBalancerHealthMonitor object.
     */
    private void createHealthMonitor(NeutronLoadBalancerHealthMonitor loadBalancerHealthMonitor) throws IOException {
        LoadbalancerHealthmonitor virtualLoadBalancerHealthMonitor = new LoadbalancerHealthmonitor();
        virtualLoadBalancerHealthMonitor = mapLoadBalancerHealthMonitorProperties(loadBalancerHealthMonitor,
                virtualLoadBalancerHealthMonitor);
        boolean loadBalancerHealthMonitorCreated;
        try {
            loadBalancerHealthMonitorCreated = apiConnector.create(virtualLoadBalancerHealthMonitor);
            LOGGER.debug("loadBalancerHealthMonitor:   " + loadBalancerHealthMonitorCreated);
            if (!loadBalancerHealthMonitorCreated) {
                LOGGER.info("loadBalancerHealthMonitor creation failed..");
            }
        } catch (Exception Ex) {
            LOGGER.error("Exception : " + Ex);
        }
        LOGGER.info("loadBalancerHealthMonitor:" + loadBalancerHealthMonitor.getLoadBalancerHealthMonitorID()
                + "succesfully created.");
    }

    @Override
    public int canUpdateNeutronLoadBalancerHealthMonitor(NeutronLoadBalancerHealthMonitor delta,
            NeutronLoadBalancerHealthMonitor original) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void neutronLoadBalancerHealthMonitorUpdated(NeutronLoadBalancerHealthMonitor loadBalancerHealthMonitor) {
        // TODO Auto-generated method stub

    }

    @Override
    public int canDeleteNeutronLoadBalancerHealthMonitor(NeutronLoadBalancerHealthMonitor loadBalancerHealthMonitor) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void neutronLoadBalancerHealthMonitorDeleted(NeutronLoadBalancerHealthMonitor loadBalancerHealthMonitor) {
        // TODO Auto-generated method stub

    }

    /**
     * Invoked to map the NeutronLoadBalancerHealthMonitor object properties to the LoadbalancerHealthmonitor
     * object.
     *
     * @param loadBalancerHealthMonitor
     *            An instance of new NeutronLoadBalancerHealthMonitor object.
     * @param virtualLoadBalancerHealthMonitor
     *            An instance of new LoadbalancerHealthmonitor object.
     * @return {@link LoadbalancerHealthmonitor}
     */
    private LoadbalancerHealthmonitor mapLoadBalancerHealthMonitorProperties(
            NeutronLoadBalancerHealthMonitor loadBalancerHealthMonitor,
            LoadbalancerHealthmonitor virtualLoadBalancerHealthMonitor) {
        String loadBalancerHealthMonitorUUID = loadBalancerHealthMonitor.getLoadBalancerHealthMonitorID();
        String projectUUID = loadBalancerHealthMonitor.getLoadBalancerHealthMonitorTenantID();
        try {
            if (!(loadBalancerHealthMonitorUUID.contains("-"))) {
                loadBalancerHealthMonitorUUID = Utils.uuidFormater(loadBalancerHealthMonitorUUID);
            }
            if (!(projectUUID.contains("-"))) {
                projectUUID = Utils.uuidFormater(projectUUID);
            }
            projectUUID = UUID.fromString(projectUUID).toString();
            loadBalancerHealthMonitorUUID = UUID.fromString(loadBalancerHealthMonitorUUID).toString();
            Project project = (Project) apiConnector.findById(Project.class, projectUUID);
            virtualLoadBalancerHealthMonitor.setParent(project);
        } catch (Exception ex) {
            LOGGER.error("UUID input incorrect", ex);
        }
        virtualLoadBalancerHealthMonitor.setDisplayName(loadBalancerHealthMonitorUUID);
        virtualLoadBalancerHealthMonitor.setName(loadBalancerHealthMonitorUUID);
        virtualLoadBalancerHealthMonitor.setUuid(loadBalancerHealthMonitorUUID);
        LoadbalancerHealthmonitorType loadbalancer_healthmonitor_properties = new LoadbalancerHealthmonitorType();
        if (loadBalancerHealthMonitor.getLoadBalancerHealthMonitorAdminStateIsUp() != null) {
            loadbalancer_healthmonitor_properties.setAdminState(loadBalancerHealthMonitor
                    .getLoadBalancerHealthMonitorAdminStateIsUp());
        } else {
            loadbalancer_healthmonitor_properties.setAdminState(true);
        }
        if (loadBalancerHealthMonitor.getLoadBalancerHealthMonitorDelay() != null) {
            loadbalancer_healthmonitor_properties.setDelay(loadBalancerHealthMonitor
                    .getLoadBalancerHealthMonitorDelay());
        }
        if (loadBalancerHealthMonitor.getLoadBalancerHealthMonitorExpectedCodes() != null) {
            loadbalancer_healthmonitor_properties.setExpectedCodes(loadBalancerHealthMonitor
                    .getLoadBalancerHealthMonitorExpectedCodes());
        }
        if (loadBalancerHealthMonitor.getLoadBalancerHealthMonitorHttpMethod() != null) {
            loadbalancer_healthmonitor_properties.setHttpMethod(loadBalancerHealthMonitor
                    .getLoadBalancerHealthMonitorHttpMethod());
        }
        if (loadBalancerHealthMonitor.getLoadBalancerHealthMonitorMaxRetries() != null) {
            loadbalancer_healthmonitor_properties.setMaxRetries(loadBalancerHealthMonitor
                    .getLoadBalancerHealthMonitorMaxRetries());
        }
        if (loadBalancerHealthMonitor.getLoadBalancerHealthMonitorTimeout() != null) {
            loadbalancer_healthmonitor_properties.setTimeout(loadBalancerHealthMonitor
                    .getLoadBalancerHealthMonitorTimeout());
        }
        if (loadBalancerHealthMonitor.getLoadBalancerHealthMonitorType() != null) {
            loadbalancer_healthmonitor_properties.setType(loadBalancerHealthMonitor.getLoadBalancerHealthMonitorType());
        }
        if (loadBalancerHealthMonitor.getLoadBalancerHealthMonitorUrlPath() != null) {
            loadbalancer_healthmonitor_properties.setUrlPath(loadBalancerHealthMonitor
                    .getLoadBalancerHealthMonitorUrlPath());
        }
        virtualLoadBalancerHealthMonitor.setProperties(loadbalancer_healthmonitor_properties);
        return virtualLoadBalancerHealthMonitor;
    }
}
