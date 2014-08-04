package org.opendaylight.plugin2oc.neutron;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.UUID;

import net.juniper.contrail.api.ApiConnector;
import net.juniper.contrail.api.ApiPropertyBase;
import net.juniper.contrail.api.ObjectReference;
import net.juniper.contrail.api.types.LogicalRouter;
import net.juniper.contrail.api.types.Project;
import net.juniper.contrail.api.types.VirtualMachineInterface;
import net.juniper.contrail.api.types.VirtualNetwork;

import org.opendaylight.controller.networkconfig.neutron.INeutronRouterAware;
import org.opendaylight.controller.networkconfig.neutron.NeutronRouter;
import org.opendaylight.controller.networkconfig.neutron.NeutronRouter_Interface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle requests for Neutron Router.
 */
public class RouterHandler implements INeutronRouterAware {

    /**
     * Logger instance.
     */

    static final Logger LOGGER = LoggerFactory.getLogger(RouterHandler.class);
    static ApiConnector apiConnector;

    /**
     * Invoked when a router creation is requested to check if the specified
     * router can be created and then creates the router
     *
     * @param router
     *            An instance of proposed new Neutron Router object.
     *
     * @return A HTTP status code to the creation request.
     */
    @Override
    public int canCreateRouter(NeutronRouter router) {
        apiConnector = Activator.apiConnector;
        if (router == null) {
            LOGGER.error("Router object can't be null/empty.");
            return HttpURLConnection.HTTP_BAD_REQUEST;
        }
        if (router.getRouterUUID() == null || ("").equals(router.getRouterUUID())) {
            LOGGER.error("Router UUID can't be null/empty.");
            return HttpURLConnection.HTTP_BAD_REQUEST;
        }
        if (router.getTenantID() == null || ("").equals(router.getTenantID())) {
            LOGGER.error("Tenant can't be null/empty.");
            return HttpURLConnection.HTTP_BAD_REQUEST;
        }
        if (router.getName() == null || ("").equals(router.getName())) {
            LOGGER.error("Router name can't be null/empty.");
            return HttpURLConnection.HTTP_BAD_REQUEST;
        }
        String projectUUID = router.getTenantID();
        if (!(projectUUID.contains("-"))) {
            projectUUID = uuidFormater(projectUUID);
        }
        projectUUID = UUID.fromString(projectUUID).toString();
        Project project;
        try {
            project = (Project) apiConnector.findById(Project.class, projectUUID);

            if (project == null) {
                Thread.currentThread();
                Thread.sleep(3000);
                project = (Project) apiConnector.findById(Project.class, projectUUID);
                if (project == null) {
                    LOGGER.error("Could not find projectUUID...");
                    return HttpURLConnection.HTTP_NOT_FOUND;
                }
            }
            return createRouter(router);
        } catch (InterruptedException interruptedException) {
            LOGGER.error("InterruptedException :    ", interruptedException);
            return HttpURLConnection.HTTP_INTERNAL_ERROR;
        } catch (IOException ie) {
            LOGGER.error("IOException :   " + ie);
            return HttpURLConnection.HTTP_INTERNAL_ERROR;
        }
    }

    /**
     * Invoked to create the specified Neutron Router.
     *
     * @param router
     *            An instance of new Neutron Router object.
     *
     * @return A HTTP status code to the creation request.
     */
    private int createRouter(NeutronRouter router) throws IOException {
        LogicalRouter logicalRouter = new LogicalRouter();
        logicalRouter = mapRouterProperties(router, logicalRouter);
        String projectUUID = router.getTenantID();
        if (!(projectUUID.contains("-"))) {
            projectUUID = uuidFormater(projectUUID);
        }
        projectUUID = UUID.fromString(projectUUID).toString();
        try {
            Project project = (Project) apiConnector.findById(Project.class, projectUUID);
            logicalRouter.setParent(project);
            if (router.getExternalGatewayInfo() != null) {
                try {
                    VirtualNetwork virtualNetwork = (VirtualNetwork) apiConnector.findById(VirtualNetwork.class, router.getExternalGatewayInfo().getNetworkID());
                    logicalRouter.setVirtualNetwork(virtualNetwork);
                } catch (IOException ex) {
                    LOGGER.error("IOException  :    " + ex);
                }
            }
            boolean routerCreated = apiConnector.create(logicalRouter);
            if (!routerCreated) {
                LOGGER.warn("Router creation failed..");
                return HttpURLConnection.HTTP_INTERNAL_ERROR;
            }
            LOGGER.info("Router : " + logicalRouter.getName() + "  having UUID : " + logicalRouter.getUuid() + "  sucessfully created...");
            return HttpURLConnection.HTTP_OK;
        } catch (IOException ex) {
            LOGGER.error("IOException :   " + ex);
            return HttpURLConnection.HTTP_INTERNAL_ERROR;
        }
    }

    /**
     * Invoked to take action after a router has been created.
     *
     * @param router
     *            An instance of new Neutron Router object.
     */
    @Override
    public void neutronRouterCreated(NeutronRouter router) {
        LogicalRouter logicalRouter = null;
        try {
            logicalRouter = (LogicalRouter) apiConnector.findById(LogicalRouter.class, router.getRouterUUID());
            if (logicalRouter != null) {
                LOGGER.info("Router creation verified....");
            }
        } catch (Exception e) {
            LOGGER.error("Exception :    " + e);
        }
    }

    /**
     * Invoked when a router deletion is requested to indicate if the specified
     * router can be deleted.
     *
     * @param router
     *            An instance of the Neutron Router object to be deleted.
     *
     * @return A HTTP status code to the deletion request.
     */
    @Override
    public int canDeleteRouter(NeutronRouter router) {
        apiConnector = Activator.apiConnector;
        if (router == null) {
            LOGGER.info("Router object can't be null...");
            return HttpURLConnection.HTTP_BAD_REQUEST;
        }
        try {
            return deleteRouter(router.getRouterUUID());
        } catch (Exception ex) {
            LOGGER.error("Exception :   ", ex);
            return HttpURLConnection.HTTP_INTERNAL_ERROR;
        }
    }

    /**
     * Invoked to delete the specified Neutron router.
     *
     * @param router
     *            An instance of new Neutron router object.
     *
     * @return A HTTP status code to the deletion request.
     */
    private int deleteRouter(String routerUUID) {
        LogicalRouter logicalRouter = null;
        try {
            logicalRouter = (LogicalRouter) apiConnector.findById(LogicalRouter.class, routerUUID);
            if (logicalRouter != null) {
                apiConnector.delete(logicalRouter);
                LOGGER.info("Router with UUID :  " + routerUUID + "  has been deleted successfully....");
                return HttpURLConnection.HTTP_OK;
            } else {
                LOGGER.info("No Router exists with UUID :  " + routerUUID);
                return HttpURLConnection.HTTP_BAD_REQUEST;
            }
        } catch (IOException ex) {
            LOGGER.error("Exception :    " + ex);
            return HttpURLConnection.HTTP_INTERNAL_ERROR;
        }
    }

    /**
     * Invoked to take action after a Router has been deleted.
     *
     * @param router
     *            An instance of deleted Neutron Router object.
     */
    @Override
    public void neutronRouterDeleted(NeutronRouter router) {
        LogicalRouter logicalRouter = null;
        try {
            logicalRouter = (LogicalRouter) apiConnector.findById(LogicalRouter.class, router.getRouterUUID());
            if (logicalRouter != null) {
                LOGGER.info("Router deletion verified....");
            }
        } catch (IOException ex) {
            LOGGER.error("Exception :    " + ex);
        }

    }

    /**
     * Invoked when a router update is requested to indicate if the specified
     * router can be changed using the specified delta.
     *
     * @param delta
     *            Updates to the router object using patch semantics.
     * @param router
     *            An instance of the Neutron router object to be updated.
     * @return A HTTP status code to the update request.
     */
    @Override
    public int canUpdateRouter(NeutronRouter deltaRouter, NeutronRouter router) {
        apiConnector = Activator.apiConnector;
        if (deltaRouter == null || router == null) {
            LOGGER.error("Neutron Router object can't be null..");
            return HttpURLConnection.HTTP_BAD_REQUEST;
        }
        try {
            LogicalRouter logicalRouter = (LogicalRouter) apiConnector.findById(LogicalRouter.class, router.getRouterUUID());
            if (logicalRouter == null) {
                LOGGER.warn("Router object not found..");
                return HttpURLConnection.HTTP_NOT_FOUND;
            }
            return updateRouter(logicalRouter, deltaRouter);
        } catch (IOException ioEx) {
            LOGGER.error("Exception :    " + ioEx);
            return HttpURLConnection.HTTP_INTERNAL_ERROR;
        }
    }

    /**
     * Invoked to update the router
     *
     * @param deltaRouter
     *            An instance of Router.
     * @param originalRouter
     *            An instance of new logicalRouter object.
     *
     * @return A HTTP status code to the creation request.
     */
    private int updateRouter(LogicalRouter logicalRouter, NeutronRouter deltaRouter) throws IOException {
        try {
            String routerName = deltaRouter.getName();
            logicalRouter.setName(routerName);
            logicalRouter.setDisplayName(routerName);
            VirtualNetwork virtualNetwork = null;
            if (deltaRouter.getExternalGatewayInfo() != null) {
                try {
                    virtualNetwork = (VirtualNetwork) apiConnector.findById(VirtualNetwork.class, deltaRouter.getExternalGatewayInfo().getNetworkID());
                    logicalRouter.setVirtualNetwork(virtualNetwork);
                } catch (IOException ex) {
                    LOGGER.error("IOException  :    " + ex);
                }
            } else{
            logicalRouter.clearVirtualNetwork();
            }
            boolean routerUpdate = apiConnector.update(logicalRouter);
            if (!routerUpdate) {
                LOGGER.warn("Router Updation failed..");
                return HttpURLConnection.HTTP_INTERNAL_ERROR;
            }
            LOGGER.info("Router having UUID : " + logicalRouter.getUuid() + "  has been sucessfully updated...");
            return HttpURLConnection.HTTP_OK;
        } catch (Exception e) {
            LOGGER.error("Exception :    " + e);
            return HttpURLConnection.HTTP_INTERNAL_ERROR;
        }
    }

    /**
     * Invoked to take action after a router has been updated.
     *
     * @param router
     *            An instance of modified Neutron router object.
     */
    @Override
    public void neutronRouterUpdated(NeutronRouter router) {
        try {
            LogicalRouter logicalRouter = new LogicalRouter();
            logicalRouter = (LogicalRouter) apiConnector.findById(LogicalRouter.class, router.getRouterUUID());

            if (router.getName().equalsIgnoreCase(logicalRouter.getName())) {
                LOGGER.info("Router updatation verified....");
            } else {
                LOGGER.info("Router updatation failed....");
            }

        } catch (Exception ex) {
            LOGGER.error("Exception :    " + ex);
        }

    }

    /**
     * Invoked to attach interface to the specified Neutron Router.
     *
     * @param router
     *            An instance of new Neutron Router object.
     *
     * @param routerInterface
     *            An instance of NeutronRouter_Interface object to be attached.
     *
     * @return A HTTP status code to the attach request.
     */
    @Override
    public int canAttachInterface(NeutronRouter router, NeutronRouter_Interface routerInterface) {
        apiConnector = Activator.apiConnector;
        String portId = routerInterface.getPortUUID();
        String routerId = router.getRouterUUID();
        VirtualMachineInterface virtualMachineInterface = null;
        LogicalRouter logicalRouter = null;
        try {
            logicalRouter = (LogicalRouter) apiConnector.findById(LogicalRouter.class, routerId);
            virtualMachineInterface = (VirtualMachineInterface) apiConnector.findById(VirtualMachineInterface.class, portId);
            if (virtualMachineInterface != null) {
                logicalRouter.addVirtualMachineInterface(virtualMachineInterface);
            }
//            logicalRouter.setDeviceOwner();  // TODO : Support needs to be added
            boolean updateVMI = apiConnector.update(virtualMachineInterface);
            if (!updateVMI) {
                LOGGER.warn("virtualMachineInterface updation failed..");
                return HttpURLConnection.HTTP_INTERNAL_ERROR;
            }
            boolean interfaceAttached = apiConnector.update(logicalRouter);
            if (!interfaceAttached) {
                LOGGER.warn("Interface attachment failed..");
                return HttpURLConnection.HTTP_INTERNAL_ERROR;
            }
            LOGGER.info("Interface : " + logicalRouter.getName() + "  having UUID : " + logicalRouter.getUuid() + "  sucessfully attached with..."
                    + logicalRouter.getVirtualMachineInterface());
            return HttpURLConnection.HTTP_OK;
        } catch (IOException ioEx) {
            LOGGER.error("IOException :   " + ioEx);
            return HttpURLConnection.HTTP_INTERNAL_ERROR;
        }
    }

    /**
     * Invoked to take action after a router interface has been attached.
     *
     * @param router
     *            An instance of new Neutron Router object.
     *
     * @param routerInterface
     *            An instance of NeutronRouter_Interface object to be attached.
     */
    @Override
    public void neutronRouterInterfaceAttached(NeutronRouter router, NeutronRouter_Interface routerInterface) {
        String portId = routerInterface.getPortUUID();
        String routerId = router.getRouterUUID();
        VirtualMachineInterface virtualMachineInterface = null;
        try {
            virtualMachineInterface = (VirtualMachineInterface) apiConnector.findById(VirtualMachineInterface.class, portId);
            List<ObjectReference<ApiPropertyBase>> virtualMachineList = virtualMachineInterface.getVirtualMachine();
            if (virtualMachineList != null) {
                for (ObjectReference<ApiPropertyBase> ref : virtualMachineList) {
                    String deviceId = ref.getUuid();
                    if (deviceId.equals(routerId)) {
                        LOGGER.info("Interface attachment verified to router...");
                    }
                }
            }
            // if(virtualMachineInterface.getVirtualMachine()!=null &&
            // something.getDeviceOwner.equals("network:router_interface")){
            // LOGGER.info("Interface attachment verified to router..." );
            // }
        } catch (Exception ex) {
            LOGGER.error("Exception :    " + ex);
        }

    }

    /**
     * Invoked to detach interface to the specified Neutron Router.
     *
     * @param router
     *            An instance of new Neutron Router object.
     *
     * @param routerInterface
     *            An instance of NeutronRouter_Interface object to be detached.
     *
     * @return A HTTP status code to the detach request.
     */
    @Override
    public int canDetachInterface(NeutronRouter router, NeutronRouter_Interface routerInterface) {
        apiConnector = Activator.apiConnector;
        String portId = routerInterface.getPortUUID();
        String routerId = router.getRouterUUID();
        VirtualMachineInterface virtualMachineInterface = null;
        LogicalRouter logicalRouter = null;
        try {
            logicalRouter = (LogicalRouter) apiConnector.findById(LogicalRouter.class, routerId);
            List<ObjectReference<ApiPropertyBase>> vmiList = logicalRouter.getVirtualMachineInterface();
            for (ObjectReference<ApiPropertyBase> vmiRef : vmiList) {
                if(vmiRef.getUuid().matches(portId)){
                    vmiList.remove(vmiRef);
                    break;
                }
            }
            virtualMachineInterface = (VirtualMachineInterface) apiConnector.findById(VirtualMachineInterface.class, portId);
//            virtualMachineInterface.clearDeviceId(); //TODO - support to be added in OpenContrail
            // virtualMachineInterface.clearDeviceId(); //TODO - support to be added in OpenContrail
            boolean updateVMI = apiConnector.update(virtualMachineInterface);
            if (!updateVMI) {
                LOGGER.warn("virtualMachineInterface updation failed..");
                return HttpURLConnection.HTTP_INTERNAL_ERROR;
            }
            boolean interfaceDetached = apiConnector.update(logicalRouter);
            if (!interfaceDetached) {
                LOGGER.warn("Interface detachment failed..");
                return HttpURLConnection.HTTP_INTERNAL_ERROR;
            }
            LOGGER.info("Interface : " + logicalRouter.getName() + "  having UUID : " + logicalRouter.getUuid() + "  sucessfully detached from..."
                    + logicalRouter.getVirtualMachineInterface());
            return HttpURLConnection.HTTP_OK;
        } catch (IOException e) {
            LOGGER.error("IOException :   " + e);
            return HttpURLConnection.HTTP_INTERNAL_ERROR;
        }
    }

    /**
     * Invoked to take action after a router interface has been detached.
     *
     * @param router
     *            An instance of new Neutron Router object.
     *
     * @param routerInterface
     *            An instance of NeutronRouter_Interface object to be attached.
     */
    @Override
    public void neutronRouterInterfaceDetached(NeutronRouter router, NeutronRouter_Interface routerInterface) {
        String routerId = router.getRouterUUID();
        String portId = routerInterface.getPortUUID();
        try {
            VirtualMachineInterface virtualMachineInterface = (VirtualMachineInterface) apiConnector.findById(VirtualMachineInterface.class, portId);
            LogicalRouter logicalRouter = (LogicalRouter) apiConnector.findById(LogicalRouter.class, routerId);
            // if(virtualMachineInterface.getVirtualMachine()==null &&
            // virtualMachineInterface.getDeviceOwner==null){
            // LOGGER.info("Interface detachment verified from router..." );
            // }
        } catch (Exception e) {
            LOGGER.error("Exception :    " + e);
        }
    }

    /**
     * Invoked to map the NeutronRouter object properties to the logicalRouter
     * object.
     *
     * @param neutronRouter
     *            An instance of new Neutron Router object.
     * @param logicalRouter
     *            An instance of new logicalRouter object.
     * @return {@link logicalRouter}
     */
    private LogicalRouter mapRouterProperties(NeutronRouter neutronRouter, LogicalRouter logicalRouter) {
        String routerUUID = neutronRouter.getRouterUUID();
        String routerName = neutronRouter.getName();
        logicalRouter.setUuid(routerUUID);
        logicalRouter.setName(routerName);
        logicalRouter.setDisplayName(routerName);
        VirtualNetwork virtualNetwork = null;
        if (neutronRouter.getExternalGatewayInfo() != null) {
            try {
                virtualNetwork = (VirtualNetwork) apiConnector.findById(VirtualNetwork.class, neutronRouter.getExternalGatewayInfo().getNetworkID());
                logicalRouter.setVirtualNetwork(virtualNetwork);
            } catch (IOException ex) {
                LOGGER.error("IOException  :    " + ex);
            }
        }
        return logicalRouter;
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
