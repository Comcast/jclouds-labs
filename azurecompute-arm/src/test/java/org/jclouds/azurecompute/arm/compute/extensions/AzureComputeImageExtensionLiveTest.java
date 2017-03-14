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
package org.jclouds.azurecompute.arm.compute.extensions;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.inject.Module;
import org.jclouds.azurecompute.arm.AzureComputeProviderMetadata;
import org.jclouds.azurecompute.arm.config.AzureComputeProperties;
import org.jclouds.azurecompute.arm.internal.AzureLiveTestUtils;
import org.jclouds.compute.RunNodesException;
import org.jclouds.compute.config.ComputeServiceProperties;
import org.jclouds.compute.domain.Image;
import org.jclouds.compute.domain.ImageTemplate;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.OsFamily;
import org.jclouds.compute.domain.Template;
import org.jclouds.compute.domain.TemplateBuilder;
import org.jclouds.compute.extensions.ImageExtension;
import org.jclouds.compute.extensions.internal.BaseImageExtensionLiveTest;
import org.jclouds.providers.ProviderMetadata;
import org.jclouds.sshj.config.SshjSshClientModule;
import org.testng.annotations.Test;

import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.jclouds.compute.config.ComputeServiceProperties.TIMEOUT_NODE_RUNNING;
import static org.jclouds.compute.config.ComputeServiceProperties.TIMEOUT_SCRIPT_COMPLETE;
import static org.jclouds.compute.predicates.NodePredicates.inGroup;
import static org.testng.Assert.assertNotNull;

/**
 * Live tests for the {@link org.jclouds.compute.extensions.ImageExtension} integration.
 */
@Test(groups = "live", singleThreaded = true, testName = "DigitalOcean2ImageExtensionLiveTest")
public class AzureComputeImageExtensionLiveTest extends BaseImageExtensionLiveTest {

   public AzureComputeImageExtensionLiveTest() {
      provider = "azurecompute-arm";
   }

   public static String NAME_PREFIX = "%s";

   @Override
   protected Module getSshModule() {
      return new SshjSshClientModule();
   }

   @Override
   protected Properties setupProperties() {
      Properties properties = super.setupProperties();

      properties.put(ComputeServiceProperties.POLL_INITIAL_PERIOD, 1000);
      properties.put(ComputeServiceProperties.POLL_MAX_PERIOD, 10000);
      properties.setProperty(AzureComputeProperties.OPERATION_TIMEOUT, "46000000");
      properties.setProperty(AzureComputeProperties.OPERATION_POLL_INITIAL_PERIOD, "5");
      properties.setProperty(AzureComputeProperties.OPERATION_POLL_MAX_PERIOD, "15");
      properties.setProperty(AzureComputeProperties.TCP_RULE_FORMAT, "tcp_%s-%s");
      properties.setProperty(AzureComputeProperties.TCP_RULE_REGEXP, "tcp_\\d{1,5}-\\d{1,5}");
      long scriptTimeout = TimeUnit.MILLISECONDS.convert(20, TimeUnit.MINUTES);
      properties.setProperty(TIMEOUT_SCRIPT_COMPLETE, scriptTimeout + "");
      properties.setProperty(TIMEOUT_NODE_RUNNING, scriptTimeout + "");

      AzureLiveTestUtils.defaultProperties(properties);
      checkNotNull(setIfTestSystemPropertyPresent(properties, "jclouds.oauth.resource"), "test.jclouds.oauth.resource");
      checkNotNull(setIfTestSystemPropertyPresent(properties, "oauth.endpoint"), "test.oauth.endpoint");

      return properties;

   }

   public void testImageCopy() throws RunNodesException {
      String nodeId = "";

      final String groupName = String.format(NAME_PREFIX, System.getProperty("user.name").substring(0, 3));
      Set<? extends NodeMetadata> nodes;
      final TemplateBuilder templateBuilder = view.getComputeService().templateBuilder();

      templateBuilder.osFamily(OsFamily.UBUNTU);
      templateBuilder.osVersionMatches("14.04");
      templateBuilder.hardwareId("Standard_A5");
      templateBuilder.locationId("westus");
      final Template template = templateBuilder.build();

      nodes = view.getComputeService().createNodesInGroup(groupName, 1, template);
      assertThat(nodes).hasSize(1);
      NodeMetadata node = nodes.iterator().next();
      nodeId = node.getId();

      // 1. First stop the VM
      view.getComputeService().suspendNode(nodeId);
      NodeMetadata.Status status = view.getComputeService().getNodeMetadata(nodeId).getStatus();
      while (status != NodeMetadata.Status.SUSPENDED) {
         status = view.getComputeService().getNodeMetadata(nodeId).getStatus();
      }

      // 2. Now get Image extension API
      Optional<ImageExtension> imageExtension = view.getComputeService().getImageExtension();

      // check it's implemented for the current provider
      assert imageExtension.isPresent();

      // build an image template from an instance (this will not have side-effects)
      ImageTemplate newImageTemplate = imageExtension.get().buildImageTemplateFromNode(nodeId, nodeId);

      ListenableFuture<Image> imageFuture = null;
      try {
         // create an image from the template (the future's get() will return once the image is available for use)
         imageFuture = imageExtension.get().createImage(newImageTemplate);
      } catch (Exception e) {
      }

      Image img = null;
      try {
         img = imageFuture.get();
      } catch (InterruptedException e) {
         e.printStackTrace();
      } catch (ExecutionException e) {
         e.printStackTrace();
      }

      assertNotNull(img);
      String imageId = img.getId();

      templateBuilder.imageId(imageId);
      templateBuilder.hardwareId("Standard_A5");
      templateBuilder.locationId("westus");
      final Template template2 = templateBuilder.build();

      try {
         nodes = view.getComputeService().createNodesInGroup(groupName, 1, template2);
      } finally {
         assertThat(nodes).hasSize(1);
         view.getComputeService().destroyNodesMatching(inGroup(groupName));
      }
      assertNotNull(img);

   }

   @Override
   protected ProviderMetadata createProviderMetadata() {
      AzureComputeProviderMetadata pm = AzureComputeProviderMetadata.builder().build();
      return pm;
   }

}
