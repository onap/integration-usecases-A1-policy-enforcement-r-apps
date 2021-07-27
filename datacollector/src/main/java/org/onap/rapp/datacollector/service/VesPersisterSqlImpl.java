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
import java.util.Optional;
import org.onap.rapp.datacollector.entity.ves.Event;
import org.onap.rapp.datacollector.entity.ves.EventAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("vesPersisterSqlImpl")
@Transactional
public class VesPersisterSqlImpl implements VesPersister {
    private static final Logger logger = LoggerFactory.getLogger(VesPersisterSqlImpl.class);

    private final SqlRepository repository;
    private final SqlRepositoryAPI repositoryAPI;

    @Autowired
    public VesPersisterSqlImpl(SqlRepository repository, SqlRepositoryAPI repositoryAPI) {
        this.repository = repository;
        this.repositoryAPI = repositoryAPI;
    }

    @Override
    public void persists(Event event) {
        logger.debug("persisting event {}", event);
        repository.save(event);
    }

    @Override
    public void persistAll(List<Event> events) {
        logger.debug("persisting all events {}", events);
        repository.saveAll(events);
    }

    @Override
    public List<EventAPI> findTopNVesEvent(int n) {
        logger.debug("finding top {} events", n);
        return repositoryAPI.findTopNVesEvent(n);
    }

    @Override
    public List<EventAPI> findAll() {
        logger.debug("finding all event");
        return (List<EventAPI>)repositoryAPI.findAll();
    }

    @Override
    public Optional<EventAPI> findById(Long id) {
        logger.debug("finding event by id {}", id);
        return repositoryAPI.findById(id);
    }

    @Override
    public void create(Event event) {
        logger.debug("creating event {}", event);
        repository.save(event);
    }

    @Override
    public void update(Event event, Long id) {
        if (!repository.existsById(String.valueOf(id))) {
            throw new RuntimeException("Event not found");
        }
        logger.debug("updating event {} by id {}", event, id);
        repository.save(event);
    }

    @Override
    public List<EventAPI> findEventsByTimeWindow(long startTime, long endTime) {
        logger.debug("finding top {} events", startTime);
        return repositoryAPI.findByLastEpochMicrosecBetweenOrderByLastEpochMicrosecAsc(startTime, endTime);
    }
}
