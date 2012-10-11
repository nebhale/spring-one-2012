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

import static org.junit.Assert.assertSame;

import org.junit.Test;

import com.nebhale.letsmakeadeal.Game;
import com.nebhale.letsmakeadeal.GameDoesNotExistException;

public final class InMemoryGameRepositoryTest {

    private final InMemoryGameRepository gameRepository = new InMemoryGameRepository();

    @Test
    public void test() throws GameDoesNotExistException {
        Game game = this.gameRepository.create();
        assertSame(game, this.gameRepository.retrieve(game.getId()));
        this.gameRepository.remove(game.getId());
    }

    @Test(expected = GameDoesNotExistException.class)
    public void retrieveDoesNotExist() throws GameDoesNotExistException {
        this.gameRepository.retrieve(Long.MAX_VALUE);
    }

    @Test(expected = GameDoesNotExistException.class)
    public void removeDoesNotExist() throws GameDoesNotExistException {
        this.gameRepository.remove(Long.MAX_VALUE);
    }

}
