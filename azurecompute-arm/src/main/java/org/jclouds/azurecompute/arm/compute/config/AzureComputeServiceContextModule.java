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
package org.jclouds.azurecompute.arm.compute.config;

import javax.inject.Named;
import javax.inject.Singleton;

import org.jclouds.azurecompute.arm.compute.AzureComputeServiceAdapter;
import org.jclouds.azurecompute.arm.compute.functions.VMImageToImage;
import org.jclouds.azurecompute.arm.compute.functions.DeploymentToNodeMetadata;
import org.jclouds.azurecompute.arm.compute.functions.VMHardwareToHardware;
import org.jclouds.azurecompute.arm.compute.functions.LocationToLocation;
import org.jclouds.azurecompute.arm.domain.VMDeployment;
import org.jclouds.azurecompute.arm.domain.VMHardware;
import org.jclouds.azurecompute.arm.domain.VMImage;
import org.jclouds.azurecompute.arm.domain.Location;
import org.jclouds.compute.ComputeServiceAdapter;
import org.jclouds.compute.config.ComputeServiceAdapterContextModule;
import org.jclouds.compute.domain.Hardware;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.strategy.CreateNodesInGroupThenAddToSet;
import org.jclouds.azurecompute.arm.compute.strategy.CreateResourceGroupThenCreateNodes;

import com.google.common.base.Function;
import com.google.inject.Inject;
import com.google.inject.TypeLiteral;

import static org.jclouds.azurecompute.arm.config.AzureComputeProperties.RESOURCE_GROUP_NAME;
import static org.jclouds.azurecompute.arm.config.AzureComputeProperties.OPERATION_TIMEOUT;
import static org.jclouds.azurecompute.arm.config.AzureComputeProperties.OPERATION_POLL_INITIAL_PERIOD;
import static org.jclouds.azurecompute.arm.config.AzureComputeProperties.OPERATION_POLL_MAX_PERIOD;
import static org.jclouds.azurecompute.arm.config.AzureComputeProperties.TCP_RULE_FORMAT;
import static org.jclouds.azurecompute.arm.config.AzureComputeProperties.TCP_RULE_REGEXP;

public class AzureComputeServiceContextModule
        extends ComputeServiceAdapterContextModule<VMDeployment, VMHardware, VMImage, Location> {

   @Override
   protected void configure() {
      super.configure();
      bind(new TypeLiteral<ComputeServiceAdapter<VMDeployment, VMHardware, VMImage, Location>>() {
      }).to(AzureComputeServiceAdapter.class);
      bind(new TypeLiteral<Function<VMImage, org.jclouds.compute.domain.Image>>() {
      }).to(VMImageToImage.class);
      bind(new TypeLiteral<Function<VMHardware, Hardware>>() {
      }).to(VMHardwareToHardware.class);
      bind(new TypeLiteral<Function<VMDeployment, NodeMetadata>>() {
      }).to(DeploymentToNodeMetadata.class);
      bind(new TypeLiteral<Function<Location, org.jclouds.domain.Location>>() {
      }).to(LocationToLocation.class);
      install(new LocationsFromComputeServiceAdapterModule<VMDeployment, VMHardware, VMImage, Location>() {
      });


      bind(CreateNodesInGroupThenAddToSet.class).to(CreateResourceGroupThenCreateNodes.class);
   }

   @Singleton
   public static class AzureComputeConstants {

      @Named(OPERATION_TIMEOUT)
      @Inject
      private String operationTimeoutProperty;

      @Named(OPERATION_POLL_INITIAL_PERIOD)
      @Inject
      private String operationPollInitialPeriodProperty;

      @Named(OPERATION_POLL_MAX_PERIOD)
      @Inject
      private String operationPollMaxPeriodProperty;

      @Named(TCP_RULE_FORMAT)
      @Inject
      private String tcpRuleFormatProperty;

      @Named(TCP_RULE_REGEXP)
      @Inject
      private String tcpRuleRegexpProperty;

      @Named(RESOURCE_GROUP_NAME)
      @Inject
      private String azureResourceGroupProperty;

      public Long operationTimeout() {
         return Long.parseLong(operationTimeoutProperty);
      }

      public String azureResourceGroup() {
         return azureResourceGroupProperty;
      }

      public Integer operationPollInitialPeriod() {
         return Integer.parseInt(operationPollInitialPeriodProperty);
      }

      public Integer operationPollMaxPeriod() {
         return Integer.parseInt(operationPollMaxPeriodProperty);
      }

      public String tcpRuleFormat() {
         return tcpRuleFormatProperty;
      }

      public String tcpRuleRegexp() {
         return tcpRuleRegexpProperty;
      }
   }

}