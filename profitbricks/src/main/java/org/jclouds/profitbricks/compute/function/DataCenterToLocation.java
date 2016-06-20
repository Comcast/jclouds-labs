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
package org.jclouds.profitbricks.compute.function;

import static com.google.common.base.Preconditions.checkNotNull;

import org.jclouds.domain.Location;
import org.jclouds.domain.LocationBuilder;
import org.jclouds.domain.LocationScope;
import org.jclouds.profitbricks.domain.DataCenter;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;

public class DataCenterToLocation implements Function<DataCenter, Location> {

   private final Function<org.jclouds.profitbricks.domain.Location, Location> fnRegion;

   @Inject
   DataCenterToLocation(Function<org.jclouds.profitbricks.domain.Location, Location> fnRegion) {
      this.fnRegion = fnRegion;
   }

   @Override
   public Location apply(DataCenter dataCenter) {
      checkNotNull(dataCenter, "Null dataCenter");

      LocationBuilder builder = new LocationBuilder()
              .id(dataCenter.id())
              .description(dataCenter.name())
              .scope(LocationScope.ZONE)
              .metadata(ImmutableMap.<String, Object>of(
                              "version", dataCenter.version(),
                              "state", dataCenter.state()));
      if (dataCenter.location() != null)
         builder.parent(fnRegion.apply(dataCenter.location()));
      return builder.build();
   }
}