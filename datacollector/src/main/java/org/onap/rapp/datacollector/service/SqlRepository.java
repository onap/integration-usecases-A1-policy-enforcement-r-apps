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

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.onap.rapp.datacollector.entity.ves.Event;
import org.springframework.transaction.annotation.Transactional;

@Repository("repository")
@Transactional
public interface SqlRepository extends CrudRepository<Event, String> {
}
