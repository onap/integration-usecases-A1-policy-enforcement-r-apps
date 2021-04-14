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

import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Test;
import java.util.List;

public class CalculationUtilTest {

    @Test
    public void calculateAverageTest(){
        List<Integer> integers = Lists.list(1,2,3,4,5,6,7,8,9);
        Assert.assertEquals(CalculationUtil.calculateAverage(integers), Integer.valueOf(5));
    }

    @Test
    public void calculateAverageWithSameValuesTest(){
        List<Integer> integers = Lists.list(1,1,1,1,1,1,1,1,1);
        Assert.assertEquals(CalculationUtil.calculateAverage(integers), Integer.valueOf(1));
    }

    @Test
    public void fillGapsInBeginningTest(){
        List<Integer> integers = Lists.list(null,null,null,1,2,3,4,5,6,7,8,9);
        List<Integer> integersToCompare = List.of(5,5,5,1,2,3,4,5,6,7,8,9);
        Assert.assertEquals(CalculationUtil.fillGaps(integers), integersToCompare);
    }

    @Test
    public void fillGapsInMiddleTest(){
        List<Integer> integers = Lists.list(1,2,3,4,null,null,null,5,6,7,8,9);
        List<Integer> integersToCompare = List.of(1,2,3,4,5,5,5,5,6,7,8,9);
        Assert.assertEquals(CalculationUtil.fillGaps(integers), integersToCompare);
    }

    @Test
    public void fillGapsInEndTest(){
        List<Integer> integers = Lists.list(1,2,3,4,5,6,7,8,9,null,null,null);
        List<Integer> integersToCompare = List.of(1,2,3,4,5,6,7,8,9,5,5,5);
        Assert.assertEquals(CalculationUtil.fillGaps(integers), integersToCompare);
    }

    @Test
    public void fillGapsWithNotEnoughDataTest(){
        List<Integer> integers = Lists.list(1,2,3,null,null,null,null,null,null,null,null,null);
        Exception exception = Assert.assertThrows(ArithmeticException.class, () -> CalculationUtil.fillGaps(integers));

        String expectedMessage = "Not enough performance data for prediction, data filling: 25.0%";
        String actualMessage = exception.getMessage();

        Assert.assertEquals(actualMessage, expectedMessage);
    }


}
