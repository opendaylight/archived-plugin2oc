/*
 * Copyright (C) 2014 Juniper Networks, Inc.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 */
package org.opendaylight.plugin2oc.neutron;

import net.juniper.contrail.api.ApiConnector;
import net.juniper.contrail.api.ApiConnectorFactory;

import org.apache.felix.dm.Component;
import org.opendaylight.controller.networkconfig.neutron.INeutronFloatingIPAware;
import org.opendaylight.controller.networkconfig.neutron.INeutronNetworkAware;
import org.opendaylight.controller.networkconfig.neutron.INeutronPortAware;
import org.opendaylight.controller.networkconfig.neutron.INeutronRouterAware;
import org.opendaylight.controller.networkconfig.neutron.INeutronSubnetAware;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.core.ComponentActivatorAbstractBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OSGi bundle activator for the plugin2oc Neutron Interface.
 */
public class Activator extends ComponentActivatorAbstractBase {
    static ApiConnector apiConnector = null;
    static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);

    /**
     * Function called when the activator starts just after some initializations
     * are done by the ComponentActivatorAbstractBase.
     */
    @Override
    public void init() {
        LOGGER.info("plugin2oc Plugin service Registered");
        apiConnector = getApiConnection();
    }

    /**
     * Function called to get APIConnector object. porperties must be defined in
     * opendaylight configuration folder inside config.ini at opendaylight/distribution
     * /opendaylight/src/main/resources/configuration/config.ini
     *
     */
    public ApiConnector getApiConnection() {
        String ipAddress = System.getProperty("plugin2oc.apiserver.ipaddress");
        String port = System.getProperty("plugin2oc.apiserver.port");
        int portNumber = 0;
        try {
            portNumber = Integer.parseInt(port);
        } catch (Exception ex) {
            LOGGER.error("Missing entry in Config file of Opendaylight", ex);
        }
        apiConnector = ApiConnectorFactory.build(ipAddress, portNumber);
        return apiConnector;
    }

    /**
     * Function called when the activator stops just before the cleanup done by
     * ComponentActivatorAbstractBase.
     *
     */
    @Override
    public void destroy() {
    }

    /**
     * Function that is used to communicate to dependency manager the list of
     * known implementations for services inside a container.
     *
     * @return An array containing all the CLASS objects that will be
     *         instantiated in order to get an fully working implementation
     *         Object
     */
    @Override
    public Object[] getImplementations() {
        Object[] res = { NetworkHandler.class, SubnetHandler.class, PortHandler.class, RouterHandler.class, FloatingIpHandler.class };
        return res;
    }

    /**
     * Function that is called when configuration of the dependencies is
     * required.
     *
     * @param c
     *            dependency manager Component object, used for configuring the
     *            dependencies exported and imported
     * @param imp
     *            Implementation class that is being configured, needed as long
     *            as the same routine can configure multiple implementations
     * @param containerName
     *            The containerName being configured, this allow also optional
     *            per-container different behavior if needed, usually should not
     *            be the case though.
     */
    @Override
    public void configureInstance(Component c, Object imp, String containerName) {
        if (imp.equals(NetworkHandler.class)) {
            c.setInterface(INeutronNetworkAware.class.getName(), null);
        }
        if (imp.equals(SubnetHandler.class)) {
            c.setInterface(INeutronSubnetAware.class.getName(), null);
        }
        if (imp.equals(PortHandler.class)) {
            c.setInterface(INeutronPortAware.class.getName(), null);
        }
        if (imp.equals(RouterHandler.class)) {
            c.setInterface(INeutronRouterAware.class.getName(), null);
        }
        if (imp.equals(FloatingIpHandler.class)) {
            c.setInterface(INeutronFloatingIPAware.class.getName(), null);
        }
        // Create service dependencies.
        c.add(createServiceDependency().setService(BindingAwareBroker.class).setCallbacks("setBindingAwareBroker", "unsetBindingAwareBroker")
                .setRequired(true));
    }
}
