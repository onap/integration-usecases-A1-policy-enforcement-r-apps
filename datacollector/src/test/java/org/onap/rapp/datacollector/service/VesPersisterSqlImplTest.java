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

package org.onap.rapp.datacollector.service;

import org.onap.rapp.datacollector.entity.ves.Event;
import org.onap.rapp.datacollector.entity.ves.EventTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.verification.VerificationModeFactory;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class VesPersisterSqlImplTest {

    @InjectMocks
    private VesPersisterSqlImpl vesPersisterSql;

    @Mock
    private SqlRepository repository;

    final Event event = EventTest.createDumyEvent();

    @Test
    public void persists() {

        vesPersisterSql.persists(event);

        // verify
        BDDMockito
                .verify(repository, VerificationModeFactory.times(1))
                .save(event);
    }
}