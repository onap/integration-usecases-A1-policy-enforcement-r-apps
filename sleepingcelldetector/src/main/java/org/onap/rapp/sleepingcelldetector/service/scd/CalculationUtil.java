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

package org.onap.rapp.sleepingcelldetector.service.scd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

public class CalculationUtil {

    private static final Logger logger = LoggerFactory.getLogger(CalculationUtil .class);
    public static final int MIN_PERCENTAGE_OF_DATA_FILLING = 30;

    private CalculationUtil(){
    }

    public static Integer calculateAverage(List<Integer> values){
        OptionalDouble average = values.stream().mapToDouble(l -> l).average();

        if (average.isPresent()) {
            return (int) average.getAsDouble();
        } else {
            throw new ArithmeticException("Can't calculate average");
        }
    }

    public static List<Integer> fillGaps(List<Integer> values){
        verifyDataFilling(values);
        Integer average = calculateAverage(values.stream().filter(Objects::nonNull).collect(Collectors.toList()));

        for (int i=0; i<values.size(); i++){
            if(values.get(i) == null){
                values.set(i, average);
            }
        }
        return values;
    }

    public static void verifyDataFilling(List<Integer> values) {
        double measurementsNumber = values.stream().filter(Objects::nonNull).count();
        double listSize = values.size();
        double dataFillingPercentage = measurementsNumber/listSize * 100;
        if (dataFillingPercentage < MIN_PERCENTAGE_OF_DATA_FILLING){
            logger.warn("Not enough data to make prediction, must be at least 30%; Data filling: {}%", dataFillingPercentage);
            throw new ArithmeticException("Not enough performance data for prediction, data filling: " + dataFillingPercentage + "%");
        }
    }

}
