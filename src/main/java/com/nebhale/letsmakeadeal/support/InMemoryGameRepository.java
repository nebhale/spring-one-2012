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

package com.nebhale.letsmakeadeal.support;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Component;

import com.nebhale.letsmakeadeal.Door;
import com.nebhale.letsmakeadeal.DoorContent;
import com.nebhale.letsmakeadeal.Game;
import com.nebhale.letsmakeadeal.GameDoesNotExistException;
import com.nebhale.letsmakeadeal.GameRepository;

@Component
final class InMemoryGameRepository implements GameRepository {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final Map<Long, Game> games = new HashMap<Long, Game>();

    private final AtomicLong idGenerator = new AtomicLong();

    private final Object monitor = new Object();

    public Game create() {
        synchronized (this.monitor) {
            Long id = this.idGenerator.getAndIncrement();
            Game game = new Game(id, createDoors());

            this.games.put(id, game);

            return game;
        }
    }

    public Game retrieve(Long id) throws GameDoesNotExistException {
        synchronized (this.monitor) {
            if (this.games.containsKey(id)) {
                return this.games.get(id);
            }

            throw new GameDoesNotExistException(id);
        }
    }

    public void remove(Long id) throws GameDoesNotExistException {
        synchronized (this.monitor) {
            if (this.games.containsKey(id)) {
                this.games.remove(id);
            } else {
                throw new GameDoesNotExistException(id);
            }
        }

    }

    private Set<Door> createDoors() {
        Set<Door> doors = new HashSet<Door>();

        int winner = RANDOM.nextInt(3);
        for (int i = 0; i < 3; i++) {
            Long id = this.idGenerator.getAndIncrement();
            DoorContent content = i == winner ? DoorContent.JUERGEN : DoorContent.SMALL_FURRY_ANIMAL;
            doors.add(new Door(id, content));
        }

        return doors;
    }

}
