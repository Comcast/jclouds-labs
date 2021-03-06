/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jclouds.azurecompute.arm.features;

import org.jclouds.azurecompute.arm.domain.Deployment;
import org.jclouds.azurecompute.arm.domain.Deployment.ProvisioningState;
import org.jclouds.azurecompute.arm.internal.BaseAzureComputeApiLiveTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.net.URI;
import java.util.List;

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertNotNull;

@Test(groups = "live", testName = "DeploymentApiLiveTest", singleThreaded = true)
public class DeploymentApiLiveTest extends BaseAzureComputeApiLiveTest {
    private int maxTestDuration = 90;
    private int pollingInterval = 3; // how frequently to poll for create status

    private String resourceGroup;
    private String resourceName;
    private String deploymentName;

    @BeforeClass
    @Override
    public void setup() {
        super.setup();
        resourceGroup = getResourceGroupName();
    }

    private String getPutBody(String template, String mode, String parameters) {
        String body = "{ " +
                      "\"properties\" : " +
                      "  { " +
                      "    \"template\" : " + template + ", " +
                      "    \"mode\" : \"" + mode + "\", " +
                      "    \"parameters\" : " + parameters + " " +
                      "  } " +
                      "}";
        return body;
    }

    @Test(groups = "live")
    public void testCreate() {
        Long now = System.currentTimeMillis();
        resourceName = "jcres" + now;
        deploymentName = "jcdep" + now;
        String template = "{\"$schema\":\"https://schema.management.azure.com/schemas/2015-01-01/deploymentTemplate.json#\",\"contentVersion\":\"1.0.0.0\",\"parameters\":{\"newStorageAccountName\":{\"type\":\"string\",\"metadata\":{\"description\":\"Name of the Storage Account\"}},\"storageAccountType\":{\"type\":\"string\",\"defaultValue\":\"Standard_LRS\",\"allowedValues\":[\"Standard_LRS\",\"Standard_GRS\",\"Standard_ZRS\"],\"metadata\":{\"description\":\"Storage Account type\"}},\"location\":{\"type\":\"string\",\"allowedValues\":[\"East US\",\"West US\",\"West Europe\",\"East Asia\",\"Southeast Asia\"],\"metadata\":{\"description\":\"Location of storage account\"}}},\"resources\":[{\"type\":\"Microsoft.Storage/storageAccounts\",\"name\":\"[parameters('newStorageAccountName')]\",\"apiVersion\":\"2015-05-01-preview\",\"location\":\"[parameters('location')]\",\"properties\":{\"accountType\":\"[parameters('storageAccountType')]\"}}]}";
        String parameters = "{\"newStorageAccountName\":{\"value\":\"" + resourceName + "\"},\"storageAccountType\":{\"value\":\"Standard_LRS\"},\"location\":{\"value\":\"West US\"}}";
        String properties = getPutBody(template, "Incremental", parameters);

        Deployment deployment = api().createDeployment(deploymentName, properties);
        assertNotNull(deployment);

        ProvisioningState state = ProvisioningState.fromString(deployment.properties().provisioningState());
        int testTime = 0;
        while (testTime < maxTestDuration) {
            if ((state == ProvisioningState.SUCCEEDED) ||
                    (state == ProvisioningState.CANCELED) ||
                    (state == ProvisioningState.DELETED) ||
                    (state == ProvisioningState.FAILED)) {
                break;
            }

            // sleep a little bit before polling, timeout after a fixed time
            try {
                Thread.sleep(pollingInterval * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            testTime += pollingInterval;

            deployment = api().getDeployment(deploymentName);
            assertNotNull(deployment);
            state = ProvisioningState.fromString(deployment.properties().provisioningState());
        }
        assertTrue(state == ProvisioningState.SUCCEEDED);
    }

    @Test(dependsOnMethods = "testCreate")
    public void testGetDeployment() {
        Deployment deployment = api().getDeployment(deploymentName);
        assertNotNull(deployment);
        ProvisioningState state = ProvisioningState.fromString(deployment.properties().provisioningState());
        assertTrue(state == ProvisioningState.SUCCEEDED);
    }

    @Test(dependsOnMethods = "testCreate")
    public void testListDeployments() {
        List<Deployment> deployments = api().listDeployments();
        assertTrue(deployments.size() > 0);
    }

    @AfterClass(alwaysRun = true)
    public void testDelete() throws Exception {
        List<Deployment> deployments = api().listDeployments();
        for (Deployment d : deployments) {
            if (d.name().contains("jcdep")) {
                URI uri =  api().deleteDeployment(d.name());
                if (uri != null){
                    assertTrue(uri.toString().contains("api-version"));
                    assertTrue(uri.toString().contains("operationresults"));
                }
            }
        }
    }

    private DeploymentApi api() {
        return api.getDeploymentApi(resourceGroup);
    }
}
