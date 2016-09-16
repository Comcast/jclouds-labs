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
package org.apache.jclouds.profitbricks.rest.domain;

import com.google.auto.value.AutoValue;
import com.google.common.base.Enums;
import com.google.common.collect.ImmutableList;
import java.util.List;
import org.jclouds.javax.annotation.Nullable;

@AutoValue
public abstract class Firewall {

   public enum Protocol {

      TCP, UDP, ICMP, ANY, UNRECOGNIZED;

      public static Protocol fromValue(String value) {
         return Enums.getIfPresent(Protocol.class, value).or(UNRECOGNIZED);
      }
   }

   @Nullable
   public abstract String id();

   @Nullable
   public abstract String nicId();

   @Nullable
   public abstract Boolean active();

   @Nullable
   public abstract State state();

   @Nullable
   public abstract List<Rule> rules();

   public static Builder builder() {
      return new AutoValue_Firewall.Builder()
              .rules(ImmutableList.<Rule>of());
   }

   public abstract Builder toBuilder();

   @AutoValue.Builder
   public abstract static class Builder {

      public abstract Builder id(String id);

      public abstract Builder nicId(String nicId);

      public abstract Builder active(Boolean active);

      public abstract Builder state(State state);

      public abstract Builder rules(List<Rule> rules);

      abstract Firewall autoBuild();

      public Firewall build() {
         Firewall built = autoBuild();

         return built.toBuilder()
                 .rules(ImmutableList.copyOf(built.rules()))
                 .autoBuild();
      }
   }

   public static final class Request {

      public static AddRulePayload createAddRulePayload(String nicId, List<Rule> rules) {
         return new AutoValue_Firewall_Request_AddRulePayload(nicId, ImmutableList.copyOf(rules));
      }

      @AutoValue
      public abstract static class AddRulePayload {

         public abstract String nicId();

         public abstract List<Rule> rules();

      }
   }

   @AutoValue
   public abstract static class Rule {

      @Nullable
      public abstract String id();

      @Nullable
      public abstract String name();

      @Nullable
      public abstract Integer portRangeEnd();

      @Nullable
      public abstract Integer portRangeStart();

      @Nullable
      public abstract Protocol protocol();

      @Nullable
      public abstract String sourceIp();

      @Nullable
      public abstract String sourceMac();

      @Nullable
      public abstract String targetIp();

      @Nullable
      public abstract Integer icmpCode();

      @Nullable
      public abstract Integer icmpType();

   }
}
