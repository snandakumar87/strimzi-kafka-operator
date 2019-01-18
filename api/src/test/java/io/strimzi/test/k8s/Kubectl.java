/*
 * Copyright 2017-2018, Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.test.k8s;

import io.fabric8.kubernetes.api.model.Pod;

import java.util.List;
import java.util.Map;

/**
 * A {@link KubeClient} wrapping {@code kubectl}.
 */
public class Kubectl extends BaseKubeClient<Kubectl> {

    public static final String KUBECTL = "kubectl";

    @Override
    public String defaultNamespace() {
        return "default";
    }

    @Override
    public Kubectl deletePod(String podName) {
        return null;
    }

    @Override
    protected String cmd() {
        return KUBECTL;
    }

    @Override
    public Kubectl clientWithAdmin() {
        return this;
    }

    @Override
    public Kubectl waitForPodDeletion(String podName) {
        return null;
    }

    @Override
    public List<Pod> getPodsByLabel(Map<String, String> labels) {
        return null;
    }
}
