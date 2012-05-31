/**
 * Licensed to jclouds, Inc. (jclouds) under one or more
 * contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  jclouds licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jclouds.openstack.glance.v1_0.features;

import java.io.InputStream;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.jclouds.concurrent.Timeout;
import org.jclouds.io.Payload;
import org.jclouds.javax.annotation.Nullable;
import org.jclouds.openstack.glance.v1_0.domain.DiskFormat;
import org.jclouds.openstack.glance.v1_0.domain.Image;
import org.jclouds.openstack.glance.v1_0.domain.ImageDetails;
import org.jclouds.openstack.glance.v1_0.options.CreateImageOptions;
import org.jclouds.openstack.glance.v1_0.options.UpdateImageOptions;
import org.jclouds.openstack.glance.v1_0.options.ListImageOptions;

/**
 * Image Services
 *
 * @author Adrian Cole
 * @author Adam Lowe
 * @see ImageAsyncClient
 * @see <a href="http://glance.openstack.org/glanceapi.html">api doc</a>
 */
@Timeout(duration = 180, timeUnit = TimeUnit.SECONDS)
public interface ImageClient {
   /**
    * Returns a set of brief metadata about images
    */
   Set<Image> list(ListImageOptions... options);

   /**
    * Returns a set of detailed metadata about images
    */
   Set<ImageDetails> listInDetail(ListImageOptions... options);

   /**
    * Return metadata about an image with id
    */
   @Nullable
   ImageDetails show(String id);

   /**
    * Return image data for image with id
    */
   @Nullable
   InputStream getAsStream(String id);

   /**
    * Create a new image
    *
    * @return detailed metadata about the newly stored image
    */
   ImageDetails create(String name, Payload imageData, CreateImageOptions... options);

   /**
    * Reserve a new image to be uploaded later
    *
    * @return detailed metadata about the newly stored image
    * @see #upload
    */
   ImageDetails reserve(String name, CreateImageOptions... options);

   /**
    * Adjust the metadata stored for an existing image
    *
    * @return detailed metadata about the updated image
    */
   ImageDetails update(String id, UpdateImageOptions... options);

   /**
    * Upload image data for a previously-reserved image
    * <p/>
    * If an image was previously reserved, and thus is in the queued state, then image data can be added using this method.
    * If the image already as data associated with it (e.g. not in the queued state), then you will receive a 409
    * Conflict exception.
    *
    * @param imageData the new image to upload
    * @param options   can be used to adjust the metadata stored for the image in the same call
    * @return detailed metadata about the updated image
    * @see #reserve
    */
   ImageDetails upload(String id, Payload imageData, UpdateImageOptions... options);

   /**
    * Delete the image with the specified id
    *
    * @return true if successful
    */
   Boolean delete(String id);
}
