/*
 * Copyright (C) 2021 Samsung Electronics
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package org.onap.rapp.datacollector;

import static java.util.Objects.nonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import lombok.experimental.UtilityClass;

@UtilityClass
public class TestHelpers {

    /**
     * Get empty string event
     *
     * @return empty event, without header and measurement data
     */
    public static String getEmptyEvent() {
        return "{\"event\":{}}";
    }

    /**
     * Get Event as string from file
     *
     * @param fileName location of test file
     * @return Event file as string
     */
    public static String getTestEventFromFile(String fileName) {
        InputStream in = TestHelpers.class.getResourceAsStream(fileName);
        if (nonNull(in)) {
            try (in) {
                BufferedReader inr = new BufferedReader(new InputStreamReader(in));
                return inr.lines().collect(Collectors.joining(" "));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

}
