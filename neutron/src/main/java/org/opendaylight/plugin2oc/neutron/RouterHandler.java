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
        try {
            String projectUUID = router.getTenantID();
            String routerUUID = router.getRouterUUID();
            try {
                if (!(projectUUID.contains("-"))) {
                    projectUUID = Utils.uuidFormater(projectUUID);
                }

                if (!(routerUUID.contains("-"))) {
                    routerUUID = Utils.uuidFormater(routerUUID);
                }
                boolean isValidRouterUUID = Utils.isValidHexNumber(routerUUID);
                boolean isValidprojectUUID = Utils.isValidHexNumber(projectUUID);
                if (!isValidRouterUUID || !isValidprojectUUID) {
                    LOGGER.info("Badly formed Hexadecimal UUID...");
                    return HttpURLConnection.HTTP_BAD_REQUEST;
                }
                projectUUID = UUID.fromString(projectUUID).toString();
                routerUUID = UUID.fromString(routerUUID).toString();
            } catch (Exception ex) {
                LOGGER.error("UUID input incorrect", ex);
                return HttpURLConnection.HTTP_BAD_REQUEST;
            }
            Project project = (Project) apiConnector.findById(Project.class, projectUUID);
            if (project == null) {
                Thread.currentThread();
                Thread.sleep(3000);
                project = (Project) apiConnector.findById(Project.class, projectUUID);
                if (project == null) {
                    LOGGER.error("Could not find projectUUID...");
                    return HttpURLConnection.HTTP_NOT_FOUND;
                }
            }
            String routerByName = apiConnector.findByName(LogicalRouter.class, project, router.getName());
            if (routerByName != null) {
                LOGGER.warn("Router already exists with UUID : " + routerByName);
                return HttpURLConnection.HTTP_FORBIDDEN;
            }
            return HttpURLConnection.HTTP_OK;
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
     */
    private void createRouter(NeutronRouter router) throws IOException {
        LogicalRouter logicalRouter = new LogicalRouter();
        try {
            logicalRouter = mapRouterProperties(router, logicalRouter);
            boolean routerCreated = apiConnector.create(logicalRouter);
            if (!routerCreated) {
                LOGGER.warn("Router creation failed..");
            }
            LOGGER.info("Router : " + logicalRouter.getName() + "  having UUID : " + logicalRouter.getUuid() + "  sucessfully created...");
        } catch (IOException ex) {
            LOGGER.error("IOException :   " + ex);
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
        try {
            createRouter(router);
            String routerUUID = router.getRouterUUID();
            if (!(routerUUID.contains("-"))) {
                routerUUID = Utils.uuidFormater(routerUUID);
            }
            routerUUID = UUID.fromString(routerUUID).toString();
            LogicalRouter logicalRouter = (LogicalRouter) apiConnector.findById(LogicalRouter.class, routerUUID);
            if (logicalRouter != null) {
                LOGGER.info("Router creation verified....");
            } else {
                LOGGER.error("Router creation failed....");
            }
        } catch (IOException ioEx) {
            LOGGER.error("IOException :   " + ioEx);
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
        String routerUUID = router.getRouterUUID();
        try {
            if (!(routerUUID.contains("-"))) {
                routerUUID = Utils.uuidFormater(routerUUID);
            }
            routerUUID = UUID.fromString(routerUUID).toString();
        } catch (Exception ex) {
            LOGGER.error("UUID input incorrect", ex);
            return HttpURLConnection.HTTP_BAD_REQUEST;
        }
        return HttpURLConnection.HTTP_OK;
    }

    /**
     * Invoked to delete the specified Neutron router.
     *
     * @param router
     *            An instance of new Neutron router object.
     */
    private void deleteRouter(String routerUUID) {
        try {
            LogicalRouter logicalRouter = (LogicalRouter) apiConnector.findById(LogicalRouter.class, routerUUID);
            if (logicalRouter != null) {
                apiConnector.delete(logicalRouter);
                LOGGER.info("Router with UUID :  " + routerUUID + "  has been deleted successfully....");
            } else {
                LOGGER.info("No Router exists with UUID :  " + routerUUID);
            }
        } catch (IOException ex) {
            LOGGER.error("Exception :    " + ex);
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
        try {
            String routerUUID = router.getRouterUUID();
            if (!(routerUUID.contains("-"))) {
                routerUUID = Utils.uuidFormater(routerUUID);
            }
            routerUUID = UUID.fromString(routerUUID).toString();
            deleteRouter(routerUUID);
            LogicalRouter logicalRouter = (LogicalRouter) apiConnector.findById(LogicalRouter.class, routerUUID);
            if (logicalRouter == null) {
                LOGGER.info("Router deletion verified....");
            } else {
                LOGGER.error("Router deletion failed....");
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
        String networkUUID = null;
        String routerUUID = router.getRouterUUID();
        try {
            if (deltaRouter.getExternalGatewayInfo() != null) {
                networkUUID = deltaRouter.getExternalGatewayInfo().getNetworkID();
                if (!(networkUUID.contains("-"))) {
                    networkUUID = Utils.uuidFormater(networkUUID);
                }
                networkUUID = UUID.fromString(networkUUID).toString();
            }
        } catch (Exception ex) {
            LOGGER.error("UUID input incorrect", ex);
            return HttpURLConnection.HTTP_BAD_REQUEST;
        }
        try {
            LogicalRouter logicalRouter = (LogicalRouter) apiConnector.findById(LogicalRouter.class, routerUUID);
            if (logicalRouter == null) {
                LOGGER.warn("Router object not found..");
                return HttpURLConnection.HTTP_NOT_FOUND;
            }
            return HttpURLConnection.HTTP_OK;
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
     */
    private int updateRouter(NeutronRouter neutronRouter) throws IOException {
        try {
            String routerUUID = neutronRouter.getRouterUUID();
            String networkUUID = null;
            if (!(routerUUID.contains("-"))) {
                routerUUID = Utils.uuidFormater(routerUUID);
            }
            routerUUID = UUID.fromString(routerUUID).toString();
            if (neutronRouter.getExternalGatewayInfo() != null) {
                networkUUID = neutronRouter.getExternalGatewayInfo().getNetworkID();
                if (!(networkUUID.contains("-"))) {
                    networkUUID = Utils.uuidFormater(networkUUID);
                }
                networkUUID = UUID.fromString(networkUUID).toString();
            }
            LogicalRouter logicalRouter = (LogicalRouter) apiConnector.findById(LogicalRouter.class, routerUUID);
            String routerName = neutronRouter.getName();
            logicalRouter.setDisplayName(routerName);
            if (neutronRouter.getExternalGatewayInfo() != null) {
                try {
                    VirtualNetwork virtualNetwork = (VirtualNetwork) apiConnector.findById(VirtualNetwork.class, networkUUID);
                    logicalRouter.setVirtualNetwork(virtualNetwork);
                } catch (IOException ex) {
                    LOGGER.error("IOException  :    " + ex);
                }
            } else {
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
    public void neutronRouterUpdated(NeutronRouter updatedRouter) {
        try {
            String routerUUID = updatedRouter.getRouterUUID();
            String networkUUID = null;
            if (!(routerUUID.contains("-"))) {
                routerUUID = Utils.uuidFormater(routerUUID);
            }
            routerUUID = UUID.fromString(routerUUID).toString();
            if (updatedRouter.getExternalGatewayInfo() != null) {
                networkUUID = updatedRouter.getExternalGatewayInfo().getNetworkID();
                if (!(networkUUID.contains("-"))) {
                    networkUUID = Utils.uuidFormater(networkUUID);
                }
                networkUUID = UUID.fromString(networkUUID).toString();
            }
            updateRouter(updatedRouter);
            LogicalRouter logicalRouter = (LogicalRouter) apiConnector.findById(LogicalRouter.class, routerUUID);
            if (updatedRouter.getExternalGatewayInfo() != null) {
                if (updatedRouter.getName().matches(logicalRouter.getDisplayName())
                        && networkUUID.matches(logicalRouter.getVirtualNetwork().get(0).getUuid())) {
                    LOGGER.info("Router updatation verified....");
                } else {
                    LOGGER.info("Router updatation failed....");
                }
            } else if (updatedRouter.getName().matches(logicalRouter.getDisplayName())) {
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
        String portUUId = routerInterface.getPortUUID();
        String routerUUId = router.getRouterUUID();
        try {
            if (!(portUUId.contains("-"))) {
                portUUId = Utils.uuidFormater(portUUId);
            }
            if (!(routerUUId.contains("-"))) {
                routerUUId = Utils.uuidFormater(routerUUId);
            }
            portUUId = UUID.fromString(portUUId).toString();
            routerUUId = UUID.fromString(routerUUId).toString();
        } catch (Exception ex) {
            LOGGER.error("UUID input incorrect", ex);
            return HttpURLConnection.HTTP_BAD_REQUEST;
        }
        return HttpURLConnection.HTTP_OK;
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
        String portUUId = routerInterface.getPortUUID();
        String routerUUId = router.getRouterUUID();
        VirtualMachineInterface virtualMachineInterface = null;
        LogicalRouter logicalRouter = null;
        try {
            if (!(portUUId.contains("-"))) {
                portUUId = Utils.uuidFormater(portUUId);
            }
            portUUId = UUID.fromString(portUUId).toString();
            if (!(routerUUId.contains("-"))) {
                routerUUId = Utils.uuidFormater(routerUUId);
            }
            routerUUId = UUID.fromString(routerUUId).toString();
            logicalRouter = (LogicalRouter) apiConnector.findById(LogicalRouter.class, routerUUId);
            virtualMachineInterface = (VirtualMachineInterface) apiConnector.findById(VirtualMachineInterface.class, portUUId);
            if (virtualMachineInterface != null) {
                logicalRouter.addVirtualMachineInterface(virtualMachineInterface);
            }
            // virtualMachineInterface.setDeviceOwner(); // TODO : Support needs
            // to be added
            // virtualMachineInterface.setDeviceId();
            boolean updateVMI = apiConnector.update(virtualMachineInterface);
            if (!updateVMI) {
                LOGGER.warn("virtualMachineInterface updation failed..");
            }
            boolean interfaceAttached = apiConnector.update(logicalRouter);
            if (!interfaceAttached) {
                LOGGER.warn("Interface attachment failed..");
            }
            LOGGER.info("Interface : " + logicalRouter.getName() + "  having UUID : " + logicalRouter.getUuid() + "  sucessfully attached with "
                    + logicalRouter.getVirtualMachineInterface());
        } catch (IOException ioEx) {
            LOGGER.error("IOException :   " + ioEx);
        }

        try {
            if (logicalRouter.getVirtualMachineInterface() == null) {
                List<ObjectReference<ApiPropertyBase>> virtualMachineInterfaceList = logicalRouter.getVirtualMachineInterface();
                for (ObjectReference<ApiPropertyBase> vmiRef : virtualMachineInterfaceList) {
                    String vmiUUID = vmiRef.getUuid();
                    if (vmiUUID.equals(portUUId)) {
                        LOGGER.info("Interface attachment verified to router...");
                        break;
                    } else {
                        LOGGER.info("Interface attachment failed to router...");
                    }
                }
            }
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
        String portUUID = routerInterface.getPortUUID();
        String routerUUID = router.getRouterUUID();
        try {
            if (!(portUUID.contains("-"))) {
                portUUID = Utils.uuidFormater(portUUID);
            }
            portUUID = UUID.fromString(portUUID).toString();
            if (!(routerUUID.contains("-"))) {
                routerUUID = Utils.uuidFormater(routerUUID);
            }
            routerUUID = UUID.fromString(routerUUID).toString();
        } catch (Exception ex) {
            LOGGER.error("UUID input incorrect", ex);
            return HttpURLConnection.HTTP_BAD_REQUEST;
        }
        try {
            LogicalRouter logicalRouter = (LogicalRouter) apiConnector.findById(LogicalRouter.class, routerUUID);
            if (logicalRouter != null) {
                List<ObjectReference<ApiPropertyBase>> vmiList = logicalRouter.getVirtualMachineInterface();
                if (vmiList != null) {
                    for (ObjectReference<ApiPropertyBase> vmiRef : vmiList) {
                        if (vmiRef.getUuid().matches(portUUID)) {
                            return HttpURLConnection.HTTP_OK;
                        } else {
                            LOGGER.error("No interface attached with port ID " + portUUID);
                            return HttpURLConnection.HTTP_BAD_REQUEST;
                        }
                    }
                }
            } else {
                LOGGER.error("No router exists with specified UUID");
                return HttpURLConnection.HTTP_NOT_FOUND;
            }
        } catch (IOException ioEx) {
            LOGGER.error("IOException   : ", ioEx);
            return HttpURLConnection.HTTP_INTERNAL_ERROR;
        } catch (Exception  ex) {
            LOGGER.error("IOException   : ", ex);
            return HttpURLConnection.HTTP_INTERNAL_ERROR;
        }
        return HttpURLConnection.HTTP_OK;
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
        String portUUID = routerInterface.getPortUUID();
        String routerUUID = router.getRouterUUID();
        try {
            if (!(portUUID.contains("-"))) {
                portUUID = Utils.uuidFormater(portUUID);
            }
            portUUID = UUID.fromString(portUUID).toString();
            if (!(routerUUID.contains("-"))) {
                routerUUID = Utils.uuidFormater(routerUUID);
            }
            routerUUID = UUID.fromString(routerUUID).toString();
            LogicalRouter logicalRouter = (LogicalRouter) apiConnector.findById(LogicalRouter.class, routerUUID);
            if (logicalRouter != null) {
                List<ObjectReference<ApiPropertyBase>> vmiList = logicalRouter.getVirtualMachineInterface();
                if (vmiList != null) {
                    for (ObjectReference<ApiPropertyBase> vmiRef : vmiList) {
                        if (vmiRef.getUuid().matches(portUUID)) {
                            vmiList.remove(vmiRef);
                            break;
                        }
                    }
                }
            }
            VirtualMachineInterface virtualMachineInterface = (VirtualMachineInterface) apiConnector
                    .findById(VirtualMachineInterface.class, portUUID);
            // virtualMachineInterface.clearDeviceId(); //TODO - support to be
            // added in OpenContrail
            // virtualMachineInterface.clearDeviceId(); //TODO - support to be
            // added in OpenContrail
            boolean updateVMI = apiConnector.update(virtualMachineInterface);
            if (!updateVMI) {
                LOGGER.warn("virtualMachineInterface updation failed..");
            }
            boolean interfaceDetached = apiConnector.update(logicalRouter);
            if (!interfaceDetached) {
                LOGGER.warn("Interface detachment failed..");
            }
            LOGGER.info("Interface : " + logicalRouter.getName() + "  having UUID : " + logicalRouter.getUuid() + "  sucessfully detached from "
                    + logicalRouter.getVirtualMachineInterface());
        } catch (IOException e) {
            LOGGER.error("IOException  :   " + e);
        }
        try {
            // VirtualMachineInterface virtualMachineInterface =
            // (VirtualMachineInterface)
            // apiConnector.findById(VirtualMachineInterface.class, portUUID);
            LogicalRouter logicalRouter = (LogicalRouter) apiConnector.findById(LogicalRouter.class, routerUUID);
            if (logicalRouter.getVirtualMachineInterface() == null) {
                List<ObjectReference<ApiPropertyBase>> virtualMachineInterfaceList = logicalRouter.getVirtualMachineInterface();
                for (ObjectReference<ApiPropertyBase> vmiRef : virtualMachineInterfaceList) {
                    String vmiUUID = vmiRef.getUuid();
                    if (vmiUUID.equals(portUUID)) {
                        LOGGER.info("Interface detachment failed...");
                        break;
                    } else {
                        LOGGER.info("Interface detachment verified...");
                    }
                }
            } else {
                LOGGER.info("Interface detachment verified...");
            }
            // if(virtualMachineInterface.getVirtualMachine()==null &&
            // virtualMachineInterface.getDeviceOwner==null){
            // LOGGER.info("Interface detachment verified from router..." );
            // }
        } catch (Exception e) {
            LOGGER.error("Exception   :    " + e);
        }
    }

    /**
     * Invoked to map the NeutronRouter object properties to the logicalRouter
     * object.
     * @param neutronRouter
     *            An instance of new Neutron Router object.
     * @param logicalRouter
     *            An instance of new logicalRouter object.
     * @return {@link logicalRouter}
     */
    private LogicalRouter mapRouterProperties(NeutronRouter neutronRouter, LogicalRouter logicalRouter) {
        String routerUUID = neutronRouter.getRouterUUID();
        String routerName = neutronRouter.getName();
        String projectUUID = neutronRouter.getTenantID();
        String networkUUID = null;
        try {
            if (!(projectUUID.contains("-"))) {
                projectUUID = Utils.uuidFormater(projectUUID);
            }
            projectUUID = UUID.fromString(projectUUID).toString();

            if (neutronRouter.getExternalGatewayInfo() != null) {
                networkUUID = neutronRouter.getExternalGatewayInfo().getNetworkID();
                if (!(networkUUID.contains("-"))) {
                    networkUUID = Utils.uuidFormater(networkUUID);
                }
                networkUUID = UUID.fromString(networkUUID).toString();
            }

            if (!(routerUUID.contains("-"))) {
                routerUUID = Utils.uuidFormater(routerUUID);
            }
            routerUUID = UUID.fromString(routerUUID).toString();
            Project project = (Project) apiConnector.findById(Project.class, projectUUID);
            logicalRouter.setParent(project);
            logicalRouter.setUuid(routerUUID);
            logicalRouter.setName(routerName);
            logicalRouter.setDisplayName(routerName);
            VirtualNetwork virtualNetwork = null;
            if (networkUUID != null) {
                try {
                    virtualNetwork = (VirtualNetwork) apiConnector.findById(VirtualNetwork.class, networkUUID);
                    logicalRouter.setVirtualNetwork(virtualNetwork);
                } catch (IOException ex) {
                    LOGGER.error("IOException:    " + ex);
                }
            }
        } catch (Exception ex) {
            LOGGER.error("IOException      :    " + ex);
        }
        return logicalRouter;
    }

}