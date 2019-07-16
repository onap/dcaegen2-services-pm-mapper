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

package org.onap.dcaegen2.services.pmmapper.utils;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.dcaegen2.services.pmmapper.exceptions.EnvironmentConfigException;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(EnvironmentConfig.class)
public class EnvironmentConfigTest {
    private EnvironmentConfig objUnderTest;

    @Before
    public void before() throws Exception {
        PowerMockito.mockStatic(System.class);
        objUnderTest = new EnvironmentConfig();
    }

    @Test
    public void environmentConfig_is_present_success() throws EnvironmentConfigException {
        String CBS_HOST = "cbs_host";
        PowerMockito.when(System.getenv(EnvironmentConfig.ENV_CBS_HOST_KEY)).thenReturn(CBS_HOST);
        assertEquals(CBS_HOST, objUnderTest.getCBSHostName());
    }

    @Test
    public void environmentConfig_host_not_present() throws EnvironmentConfigException {
        PowerMockito.when(System.getenv(EnvironmentConfig.ENV_CBS_HOST_KEY)).thenReturn(null);
        assertThrows(EnvironmentConfigException.class, objUnderTest::getCBSHostName);
    }

    @Test
    public void environmentConfig_hostname_present() throws EnvironmentConfigException {
        PowerMockito.when(System.getenv(EnvironmentConfig.ENV_SERVICE_NAME_KEY)).thenCallRealMethod();
        assertThrows(EnvironmentConfigException.class, objUnderTest::getCBSHostName);
    }

    @Test
    public void environmentConfig_default_port_is_used() throws EnvironmentConfigException {
        PowerMockito.when(System.getenv(EnvironmentConfig.ENV_CBS_PORT_KEY)).thenReturn(null);
        assertEquals(Integer.valueOf(EnvironmentConfig.DEFAULT_CBS_PORT), objUnderTest.getCBSPort());
    }

    @Test
    public void environmentConfig_port_invalid() throws EnvironmentConfigException {
        PowerMockito.when(System.getenv(EnvironmentConfig.ENV_CBS_PORT_KEY)).thenReturn("Invalid_port number");
        assertThrows(EnvironmentConfigException.class, objUnderTest::getCBSHostName);
    }

    @Test
    public void environmentConfig_service_name_missing() {
        PowerMockito.when(System.getenv(EnvironmentConfig.ENV_SERVICE_NAME_KEY)).thenReturn(null);
        assertThrows(EnvironmentConfigException.class, objUnderTest::getServiceName);
    }
    @Test
    public void environmentConfig_service_name_success() throws EnvironmentConfigException {
        String serviceName = "we the best service";
        PowerMockito.when(System.getenv(EnvironmentConfig.ENV_SERVICE_NAME_KEY)).thenReturn(serviceName);
        assertEquals(serviceName, objUnderTest.getServiceName());
    }
}
