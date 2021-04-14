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

package org.onap.rapp.sleepingcelldetector.service.scd.condition;

public class ConditionFactory {

    private static final Condition less = new Less();
    private static final Condition more = new More();
    private static final Condition equal = new Equal();
    private static final Condition lessOrEqual = new LessOrEqual();
    private static final Condition moreOrEqual= new MoreOrEqual();


    public static Condition getCondition(ConditionEnum condition) {
        switch (condition) {
            case LESS:
                return less;
            case MORE:
                return more;
            case EQUAL:
                return equal;
            case LESS_OR_EQUAL:
                return lessOrEqual;
            case MORE_OR_EQUAL:
                return moreOrEqual;
            default:
                throw new RuntimeException("Can't find operation for condition \" " + condition + "\"");
        }
    }
}
