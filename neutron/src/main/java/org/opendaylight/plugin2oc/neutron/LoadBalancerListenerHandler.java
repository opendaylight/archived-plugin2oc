package org.opendaylight.plugin2oc.neutron;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.UUID;

import net.juniper.contrail.api.ApiConnector;
import net.juniper.contrail.api.types.LoadbalancerPool;
import net.juniper.contrail.api.types.Project;

import org.opendaylight.controller.networkconfig.neutron.INeutronLoadBalancerListenerAware;
import org.opendaylight.controller.networkconfig.neutron.NeutronLoadBalancerListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle requests for Neutron LoadBalancerListener.
 */
public class LoadBalancerListenerHandler implements INeutronLoadBalancerListenerAware {
    /**
     * Logger instance.
     */
    static final Logger LOGGER = LoggerFactory.getLogger(LoadBalancerListenerHandler.class);
    static ApiConnector apiConnector;

    /**
     * Invoked when a listener creation is requested to check if the specified
     * listener can be created and then creates the listener
     *
     * @param loadBalancerListener
     *            An instance of proposed new NeutronLoadBalancerListener
     *            object.
     *
     * @return A HTTP status code to the creation request.
     */
    @Override
    public int canCreateNeutronLoadBalancerListener(NeutronLoadBalancerListener loadBalancerListener) {
        if (loadBalancerListener == null) {
            LOGGER.error("loadBalancerListener object can't be null..");
            return HttpURLConnection.HTTP_BAD_REQUEST;
        }
        apiConnector = Activator.apiConnector;
        if (loadBalancerListener.getLoadBalancerListenerTenantID() == null) {
            LOGGER.error("LoadBalancerListener tenant Id can not be null");
            return HttpURLConnection.HTTP_BAD_REQUEST;
        }
        if (loadBalancerListener.getNeutronLoadBalancerListenerDefaultPoolID() == null) {
            LOGGER.error("LoadBalancerListener DefaultPoolID can not be null");
            return HttpURLConnection.HTTP_BAD_REQUEST;
        }
        if (loadBalancerListener.getNeutronLoadBalancerListenerLoadBalancerID() == null) {
            LOGGER.error("LoadBalancerListener ID can not be null");
            return HttpURLConnection.HTTP_BAD_REQUEST;
        }
        if (loadBalancerListener.getNeutronLoadBalancerListenerProtocol() == null) {
            LOGGER.error("LoadBalancerListener protocol can not be null");
            return HttpURLConnection.HTTP_BAD_REQUEST;
        }
        if (!(loadBalancerListener.getNeutronLoadBalancerListenerProtocol().equals("TCP")
                || loadBalancerListener.getNeutronLoadBalancerListenerProtocol().equals("HTTP") || loadBalancerListener
                .getNeutronLoadBalancerListenerProtocol().equals("HTTPS"))) {
            LOGGER.error("LoadBalancerListener Protocol can not be other than TCP/HTTP/HTTPS");
            return HttpURLConnection.HTTP_BAD_REQUEST;
        }
        try {
            String loadBalancerListenerUUID = loadBalancerListener.getLoadBalancerListenerID();
            String loadBalancerDefaultPoolID = loadBalancerListener.getNeutronLoadBalancerListenerDefaultPoolID();
            String loadBalancerUUID = loadBalancerListener.getNeutronLoadBalancerListenerLoadBalancerID();
            String projectUUID = loadBalancerListener.getLoadBalancerListenerTenantID();
            try {
                if (!(loadBalancerListenerUUID.contains("-"))) {
                    loadBalancerListenerUUID = Utils.uuidFormater(loadBalancerListenerUUID);
                }
                if (!(loadBalancerDefaultPoolID.contains("-"))) {
                    loadBalancerDefaultPoolID = Utils.uuidFormater(loadBalancerDefaultPoolID);
                }
                if (!(loadBalancerUUID.contains("-"))) {
                    loadBalancerUUID = Utils.uuidFormater(loadBalancerUUID);
                }
                if (!(projectUUID.contains("-"))) {
                    projectUUID = Utils.uuidFormater(projectUUID);
                }
                boolean isValidLoadBalancerListenerUUID = Utils.isValidHexNumber(loadBalancerListenerUUID);
                boolean isValidLoadBalancerDefaultPoolID = Utils.isValidHexNumber(loadBalancerDefaultPoolID);
                boolean isValidLoadBalancerUUID = Utils.isValidHexNumber(loadBalancerUUID);
                boolean isValidprojectUUID = Utils.isValidHexNumber(projectUUID);
                if (!isValidLoadBalancerListenerUUID || !isValidLoadBalancerDefaultPoolID || !isValidLoadBalancerUUID
                        || !isValidprojectUUID) {
                    LOGGER.info("Badly formed Hexadecimal UUID...");
                    return HttpURLConnection.HTTP_BAD_REQUEST;
                }
                loadBalancerListenerUUID = UUID.fromString(loadBalancerListenerUUID).toString();
                loadBalancerDefaultPoolID = UUID.fromString(loadBalancerDefaultPoolID).toString();
                loadBalancerUUID = UUID.fromString(loadBalancerUUID).toString();
                projectUUID = UUID.fromString(projectUUID).toString();
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
            // String virtualLoadbalancerListenerByName =
            // apiConnector.findByName(LoadbalancerListener.class, project,
            // loadBalancerListener.getLoadBalancerListenerName());
            // if (virtualLoadbalancerListenerByName != null) {
            // LOGGER.warn("Lsitener already exists with name : " +
            // virtualLoadbalancerListenerByName);
            // return HttpURLConnection.HTTP_FORBIDDEN;
            // }
            // LoadBalancerListener virtualLoadbalancerListenerById =
            // (LoadBalancerListener) apiConnector.findById(
            // LoadBalancerListener.class, loadBalancerListenerUUID);
            // if (virtualLoadbalancerListenerById != null) {
            // LOGGER.warn("LoadbalancerListener already exists with UUID" +
            // loadBalancerListenerUUID);
            // return HttpURLConnection.HTTP_FORBIDDEN;
            // }
            LoadbalancerPool virtualLoadBalancerDefaultPoolID = (LoadbalancerPool) apiConnector.findById(
                    LoadbalancerPool.class, loadBalancerDefaultPoolID);
            if (virtualLoadBalancerDefaultPoolID == null) {
                LOGGER.warn("Default LoadbalancerPool does not exists with UUID");
                return HttpURLConnection.HTTP_FORBIDDEN;
            }
            // LoadBalancer virtualLoadBalancerListenerID = (LoadBalancer)
            // apiConnector.findById(
            // LoadBalancer.class, loadBalancerUUID);
            // if (virtualLoadBalancerListenerID== null) {
            // LOGGER.warn("Listener does not exists");
            // return HttpURLConnection.HTTP_FORBIDDEN;
            // }
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
     * Invoked to take action after a listener has been created.
     *
     * @param loadBalancerListener
     *            An instance of new NeutronLoadBalancerListener object.
     */
    @Override
    public void neutronLoadBalancerListenerCreated(NeutronLoadBalancerListener loadBalancerListener) {
        try {
            createLoadBalancerListener(loadBalancerListener);
        } catch (IOException ex) {
            LOGGER.warn("Exception  :    " + ex);
        }
        // LoadBalancerListener LoadBalancerListener = null;
        try {
            String loadBalancerListenerUUID = loadBalancerListener.getLoadBalancerListenerID();
            if (!(loadBalancerListenerUUID.contains("-"))) {
                loadBalancerListenerUUID = Utils.uuidFormater(loadBalancerListenerUUID);
            }
            loadBalancerListenerUUID = UUID.fromString(loadBalancerListenerUUID).toString();
            // LoadBalancerListener loadbalancerListener= (LoadbalancerListener)
            // apiConnector.findById(LoadbalancerListener.class,
            // loadBalancerListenerUUID);
            // if (loadbalancerListener != null) {
            // LOGGER.info("LoadbalancerListener creation verified....");
            // } else {
            // LOGGER.info("LoadbalancerListener creation failed...");
            // }
        } catch (Exception e) {
            LOGGER.error("Exception :     " + e);
        }

    }

    /**
     * Invoked to create the specified NeutronLoadBalancerListener.
     *
     * @param loadBalancerListener
     *            An instance of new NeutronLoadBalancerListener object.
     */
    private void createLoadBalancerListener(NeutronLoadBalancerListener loadBalancerListener) throws IOException {
        // TODO: write code once v2 support is available with opencontrail
    }

    @Override
    public int canUpdateNeutronLoadBalancerListener(NeutronLoadBalancerListener delta,
            NeutronLoadBalancerListener original) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void neutronLoadBalancerListenerUpdated(NeutronLoadBalancerListener loadBalancerListener) {
        // TODO Auto-generated method stub

    }

    @Override
    public int canDeleteNeutronLoadBalancerListener(NeutronLoadBalancerListener loadBalancerListener) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void neutronLoadBalancerListenerDeleted(NeutronLoadBalancerListener loadBalancerListener) {
        // TODO Auto-generated method stub

    }
    /**
     * Invoked to map the NeutronLoadBalancerListener object properties to the
     * LoadBalancerListener object.
     *
     * @param loadBalancerListener
     *            An instance of new NeutronLoadBalancerListener object.
     * @param virtualLoadBalancerListener
     *            An instance of new LoadBalancerListener object.
     * @return {@link LoadBalancerListener}
     */
    // private LoadBalancerListener
    // mapLoadBalancerListenerProperties(NeutronLoadBalancerListener
    // loadBalancerListener,
    // LoadBalancerListener virtualLoadBalancerListener) {
    // TODO: map properties between ODL-Neutron-Listener object and Opencontrail
    // Listener object
    // return virtualLoadBalancerListener;
    // }
}
