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

import java.util.List;
import org.onap.rapp.datacollector.entity.ves.EventAPI;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository("repositoryAPI")
@Transactional
public interface SqlRepositoryAPI extends CrudRepository<EventAPI, Long> {
    @Query(value = "SELECT * FROM ves_measurement order by id desc limit :limit", nativeQuery = true)
    List<EventAPI> findTopNVesEvent(@Param("limit") int limit);

    List<EventAPI> findByLastEpochMicrosecBetweenOrderByLastEpochMicrosecAsc(Long startTime, Long endTime);
}
