/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.dcaegen2.services.pmmapper.filtering;

import lombok.NonNull;
import org.onap.dcaegen2.services.pmmapper.exceptions.*;
import org.onap.dcaegen2.services.pmmapper.mapping.Mapper;
import org.onap.dcaegen2.services.pmmapper.model.Event;
import org.onap.dcaegen2.services.pmmapper.model.EventMetadata;
import org.onap.dcaegen2.services.pmmapper.model.MapperConfig;
import org.onap.dcaegen2.services.pmmapper.model.MeasFilterConfig;
import org.onap.dcaegen2.services.pmmapper.model.MeasFilterConfig.Filter;
import org.onap.dcaegen2.services.pmmapper.utils.DataRouterUtils;
import org.onap.logging.ref.slf4j.ONAPLogAdapter;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public class MetadataFilter {
    private static final ONAPLogAdapter logger = new ONAPLogAdapter(LoggerFactory.getLogger(Mapper.class));
    MapperConfig config;

    public MetadataFilter(MapperConfig config) {
        this.config = config;
    }

    /**
     * Filters events by their metadata against filter object in configuration
     * @param event inbound event
     */
    public boolean filter(@NonNull Event event) {
        String decompressionStatus;
        logger.unwrap().info("Filtering event metadata");
        EventMetadata metadata = event.getMetadata();

        MeasFilterConfig measFilterConfig = config.getFilterConfig();

        List<MeasFilterConfig.Filter> filters = measFilterConfig.getFilters();

        if(metadata.getDecompressionStatus() != null) {
            decompressionStatus = metadata.getDecompressionStatus();
            logger.unwrap().debug("Decompression Status: {}", decompressionStatus);
        }

        if(filters.isEmpty()) {
            logger.unwrap().info("No filter specified in config: {}", filters);
            return true;
        }

        for(Filter filter : filters) {
            if(compareObjects(filter, metadata)) {
                logger.unwrap().info("Metadata matches filter: {}", filter);

                event.setFilter(filter);

                return true;
            } else {
                logger.unwrap().debug("Metadata does not match filter: {}", filter);
            }
        }
        logger.unwrap().info("Metadata does not match any filters, sending process event indicator to DR");
        try {
            DataRouterUtils.processEvent(config, event);
        }catch (ProcessEventException exception) {
            logger.unwrap().error("Process event failure", exception);
        }
        return false;

    }

    /**
     * Compares event metadata against filter object
     * @param filter filter object received from configuration
     * @param metadata metadata from event
     */
     private boolean compareObjects(Filter filter, EventMetadata metadata) {
        List<Validator<Filter, EventMetadata>> validators = Arrays.asList(
                new VendorValidator(),
                new TypeValidator()
        );

        for(Validator<Filter, EventMetadata> validation : validators) {
            if (! validation.validate(filter, metadata)) {
                return false;
            }
        }
        return true;
    }

    interface Validator<A, B> {
        boolean validate(A filter, B metadata);
    }

    class VendorValidator implements Validator<Filter, EventMetadata> {
        @Override
        public boolean validate(Filter filter, EventMetadata metadata) {
            return filter.getVendor().equals(metadata.getVendorName());
        }
    }

    class TypeValidator implements Validator<Filter, EventMetadata> {
        @Override
        public boolean validate(Filter filter, EventMetadata metadata) {
            return filter.getNfType().equals(metadata.getProductName());
        }
    }
}