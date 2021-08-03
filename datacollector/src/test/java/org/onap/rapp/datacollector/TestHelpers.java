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

    public static String getEmptyEvent() {
        return "{\"event\":{}}";
    }

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
