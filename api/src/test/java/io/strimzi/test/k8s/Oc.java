/*
 * Copyright 2017-2018, Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.test.k8s;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftClient;
import io.strimzi.test.TestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static org.junit.Assert.fail;

/**
 * A {@link KubeClient} implementation wrapping {@code oc}.
 */
public class Oc<K extends Oc<K>> extends BaseKubeClient<Oc<K>> {

    private static final Logger LOGGER = LogManager.getLogger(Oc.class);
    private final OpenShiftClient client = new DefaultOpenShiftClient();

    private static final String OC = "oc";
    static final long GLOBAL_POLL_INTERVAL = 1000;
    static final long GLOBAL_TIMEOUT = 300000;

    public Oc() {

    }

    @Override
    protected Context adminContext() {
        String previous = Exec.exec(Oc.OC, "whoami").out().trim();
        String admin = "system:admin";
        LOGGER.trace("Switching from login {} to {}", previous, admin);
        Exec.exec(Oc.OC, "login", "-u", admin);
        return new Context() {
            @Override
            public void close() {
                LOGGER.trace("Switching back to login {} from {}", previous, admin);
                Exec.exec(Oc.OC, "login", "-u", previous);
            }
        };
    }

    @Override
    public Oc clientWithAdmin() {
        return new AdminOc();
    }

    @Override
    public String defaultNamespace() {
        return "myproject";
    }

    @Override
    public Oc createNamespace(String name) {
        try (Context context = defaultContext()) {
            Exec.exec(cmd(), "new-project", name);
        }
        return this;
    }

    public Oc newApp(String template) {
        return newApp(template, emptyMap());
    }

    public Oc newApp(String template, Map<String, String> params) {
        List<String> cmd = namespacedCommand("new-app", template);
        for (Map.Entry<String, String> entry : params.entrySet()) {
            cmd.add("-p");
            cmd.add(entry.getKey() + "=" + entry.getValue());
        }

        Exec.exec(cmd);
        return this;
    }

    public Oc newProject(String name) {
        Exec.exec(namespacedCommand("new-project", name));
        return this;
    }

    @Override
    public String logs(String podName, String containerName) {
        if (containerName != null) {
            return client.pods().withName(podName).getLog();
        } else {
            return client.pods().withName(podName).inContainer(containerName).getLog();
        }
    }

    @Override
    public K deletePod(String podName) {
        client.pods().withName(podName).delete();
        return (K) this;
    }

    @Override
    public K waitForPod(String name) {
        // TODO
        return (K) this;
    };

    @Override
    public K waitForPodDeletion(String podName) {
        LOGGER.info("Waiting when Pod {} will be deleted", podName);

        TestUtils.waitFor("pod " + podName + "will be deleted", GLOBAL_POLL_INTERVAL, GLOBAL_TIMEOUT,
                () -> client.pods().withName(podName).get() == null);
        return (K) this;
    }

    @Override
    public List<Pod> getPodsByLabel(Map<String, String> labels) {
        List<Pod> pods = client.pods().withLabels(labels).list().getItems();
        if (pods.size() != 1) {
            fail("There are " + pods.size() +  " pods with labels " + labels);
        }
        return pods;
    }

    @Override
    protected String cmd() {
        return OC;
    }

    /**
     * An {@code Oc} which uses the admin context.
     */
    private class AdminOc extends Oc {

        @Override
        public String namespace() {
            return Oc.this.namespace();
        }

        @Override
        protected Context defaultContext() {
            return adminContext();
        }

        @Override
        public Oc clientWithAdmin() {
            return this;
        }
    }
}
