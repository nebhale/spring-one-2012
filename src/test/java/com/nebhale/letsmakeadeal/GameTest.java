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

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public final class GameTest {

    private final Game game = new Game(1L, asSet(new Door(2L, DoorContent.SMALL_FURRY_ANIMAL), new Door(3L, DoorContent.SMALL_FURRY_ANIMAL),
        new Door(4L, DoorContent.JUERGEN)));

    @Test
    public void test() throws DoorDoesNotExistException {
        assertEquals(Long.valueOf(1), this.game.getId());
        assertEquals(GameStatus.AWAITING_INITIAL_SELECTION, this.game.getStatus());
        assertEquals(3, this.game.getDoors().size());
        assertEquals(Long.valueOf(2), this.game.getDoor(2L).getId());
    }

    @Test
    public void win() throws IllegalTransitionException, DoorDoesNotExistException {
        Door door2 = this.game.getDoor(2L);
        Door door3 = this.game.getDoor(3L);
        Door door4 = this.game.getDoor(4L);

        this.game.select(2L);
        assertEquals(GameStatus.AWAITING_FINAL_SELECTION, this.game.getStatus());
        assertEquals(DoorStatus.SELECTED, door2.getStatus());
        assertEquals(DoorStatus.OPEN, door3.getStatus());
        assertEquals(DoorStatus.CLOSED, door4.getStatus());

        this.game.open(4L);
        assertEquals(GameStatus.WON, this.game.getStatus());
        assertEquals(DoorStatus.SELECTED, door2.getStatus());
        assertEquals(DoorStatus.OPEN, door3.getStatus());
        assertEquals(DoorStatus.OPEN, door4.getStatus());
    }

    @Test
    public void lose() throws DoorDoesNotExistException, IllegalTransitionException {
        Door door2 = this.game.getDoor(2L);
        Door door3 = this.game.getDoor(3L);
        Door door4 = this.game.getDoor(4L);

        this.game.select(2L);
        assertEquals(GameStatus.AWAITING_FINAL_SELECTION, this.game.getStatus());
        assertEquals(DoorStatus.SELECTED, door2.getStatus());
        assertEquals(DoorStatus.OPEN, door3.getStatus());
        assertEquals(DoorStatus.CLOSED, door4.getStatus());

        this.game.open(2L);
        assertEquals(GameStatus.LOST, this.game.getStatus());
        assertEquals(DoorStatus.OPEN, door2.getStatus());
        assertEquals(DoorStatus.OPEN, door3.getStatus());
        assertEquals(DoorStatus.CLOSED, door4.getStatus());
    }

    @Test(expected = DoorDoesNotExistException.class)
    public void getDoorDoesNotExist() throws DoorDoesNotExistException {
        this.game.getDoor(5L);
    }

    @Test(expected = IllegalTransitionException.class)
    public void selectAfterSelect() throws IllegalTransitionException, DoorDoesNotExistException {
        this.game.select(2L);
        this.game.select(3L);
    }

    @Test(expected = IllegalTransitionException.class)
    public void selectAfterOpen() throws IllegalTransitionException, DoorDoesNotExistException {
        this.game.select(2L);
        this.game.select(4L);
    }

    @Test(expected = IllegalTransitionException.class)
    public void openBeforeSelect() throws IllegalTransitionException, DoorDoesNotExistException {
        this.game.open(2L);
    }

    @Test(expected = IllegalTransitionException.class)
    public void openAfterOpen() throws IllegalTransitionException, DoorDoesNotExistException {
        this.game.select(2L);
        this.game.open(2L);
        this.game.open(2L);
    }

    @Test(expected = IllegalTransitionException.class)
    public void openOpenedDoor() throws IllegalTransitionException, DoorDoesNotExistException {
        this.game.select(2L);
        this.game.open(3L);
    }

    private static final <T> Set<T> asSet(T... items) {
        Set<T> set = new HashSet<T>();
        for (T item : items) {
            set.add(item);
        }
        return set;
    }

}
