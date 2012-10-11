/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nebhale.letsmakeadeal;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.hateoas.Identifiable;

public final class Game implements Identifiable<Long> {

    private final Long id;

    private final Map<Long, Door> doors;

    private final Object monitor = new Object();

    private volatile GameStatus status;

    public Game(Long id, Set<Door> doors) {
        this.id = id;

        this.doors = new HashMap<Long, Door>();
        for (Door door : doors) {
            this.doors.put(door.getId(), door);
        }

        this.status = GameStatus.AWAITING_INITIAL_SELECTION;
    }

    public void select(Long doorId) throws IllegalTransitionException, DoorDoesNotExistException {
        synchronized (this.monitor) {
            if (this.status != GameStatus.AWAITING_INITIAL_SELECTION) {
                throw new IllegalTransitionException(this.id, this.status, GameStatus.AWAITING_FINAL_SELECTION);
            }

            Door door = getDoor(doorId);
            door.setStatus(DoorStatus.SELECTED);

            openHintDoor();

            this.status = GameStatus.AWAITING_FINAL_SELECTION;
        }
    }

    public void open(Long doorId) throws IllegalTransitionException, DoorDoesNotExistException {
        synchronized (this.monitor) {
            if (this.status != GameStatus.AWAITING_FINAL_SELECTION) {
                throw new IllegalTransitionException(this.id, this.status, GameStatus.WON);
            }

            Door door = getDoor(doorId);
            if (DoorStatus.OPEN == door.getStatus()) {
                throw new IllegalTransitionException(this.id, doorId, door.getStatus(), DoorStatus.OPEN);
            }

            door.setStatus(DoorStatus.OPEN);

            if (DoorContent.JUERGEN == door.getContent()) {
                this.status = GameStatus.WON;
            } else {
                this.status = GameStatus.LOST;
            }
        }
    }

    public Long getId() {
        return this.id;
    }

    public Door getDoor(Long doorId) throws DoorDoesNotExistException {
        if (this.doors.containsKey(doorId)) {
            return this.doors.get(doorId);
        }

        throw new DoorDoesNotExistException(this.id, doorId);
    }

    public Set<Door> getDoors() {
        return new HashSet<Door>(this.doors.values());
    }

    public GameStatus getStatus() {
        synchronized (this.monitor) {
            return this.status;
        }
    }

    private void openHintDoor() {
        for (Door door : getDoors()) {
            if (DoorStatus.CLOSED == door.getStatus() && DoorContent.SMALL_FURRY_ANIMAL == door.peekContent()) {
                door.setStatus(DoorStatus.OPEN);
                break;
            }
        }
    }
}
