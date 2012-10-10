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

package com.nebhale.letsmakeadeal.web;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.hateoas.Link;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.nebhale.letsmakeadeal.Door;
import com.nebhale.letsmakeadeal.DoorContent;
import com.nebhale.letsmakeadeal.DoorDoesNotExistException;
import com.nebhale.letsmakeadeal.DoorStatus;
import com.nebhale.letsmakeadeal.Game;
import com.nebhale.letsmakeadeal.GameStatus;
import com.nebhale.letsmakeadeal.IllegalTransitionException;

public class HistoryResourceFactoryTest {

    private final Game game = new Game(1L, asSet(new Door(2L, DoorContent.SMALL_FURRY_ANIMAL), new Door(3L, DoorContent.SMALL_FURRY_ANIMAL)));

    private final HistoryResourceFactory resourceFactory = new HistoryResourceFactory();

    @Test
    public void testCreate() throws IllegalTransitionException, DoorDoesNotExistException {
        game.select(2L);
        HistoryResource resource = this.resourceFactory.create(game);
        assertEquals(1, resource.getLinks().size());
        assertEquals(new Link("http://localhost/games/1/history"), resource.getLink(Link.REL_SELF));

        List<Object> historyItems = resource.getHistory();
        assertEquals(4, historyItems.size());

        GameHistoryItemResource historyItem0 = (GameHistoryItemResource) historyItems.get(0);
        assertEquals("http://localhost/games/1", historyItem0.getGame());
        assertEquals(GameStatus.AWAITING_INITIAL_SELECTION, historyItem0.getStatus());

        DoorHistoryItemResource historyItem1 = (DoorHistoryItemResource) historyItems.get(1);
        assertEquals("http://localhost/games/1/doors/2", historyItem1.getDoor());
        assertEquals(DoorStatus.SELECTED, historyItem1.getStatus());
        
        DoorHistoryItemResource historyItem2 = (DoorHistoryItemResource) historyItems.get(2);
        assertEquals("http://localhost/games/1/doors/3", historyItem2.getDoor());
        assertEquals(DoorStatus.OPEN, historyItem2.getStatus());

        GameHistoryItemResource historyItem3 = (GameHistoryItemResource) historyItems.get(3);
        assertEquals("http://localhost/games/1", historyItem3.getGame());
        assertEquals(GameStatus.AWAITING_FINAL_SELECTION, historyItem3.getStatus());
    }

    @BeforeClass
    public static void setRequestContext() {
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));
    }

    @AfterClass
    public static void clearRequestContext() {
        RequestContextHolder.resetRequestAttributes();
    }

    private static final <T> Set<T> asSet(T... items) {
        Set<T> set = new HashSet<T>();
        for (T item : items) {
            set.add(item);
        }
        return set;
    }

}
