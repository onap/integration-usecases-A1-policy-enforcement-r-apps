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
