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
import java.util.Set;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.hateoas.Link;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.nebhale.letsmakeadeal.Door;
import com.nebhale.letsmakeadeal.DoorDoesNotExistException;
import com.nebhale.letsmakeadeal.Game;

public class DoorResourceFactoryTest {

    private final Game game = new Game(Long.valueOf(1), asSet(new Door(2L, null)));

    private final DoorResourceFactory resourceFactory = new DoorResourceFactory();

    @Test
    public void testDoor() throws DoorDoesNotExistException {
        DoorResource resource = this.resourceFactory.create(this.game, 2L);
        assertEquals(1, resource.getLinks().size());
        assertEquals(new Link("http://localhost/games/1/doors/2"), resource.getLink(Link.REL_SELF));
    }

    @Test
    public void testDoors() {
        DoorsResource doorsResource = this.resourceFactory.create(this.game);
        assertEquals(1, doorsResource.getLinks().size());
        assertEquals(new Link("http://localhost/games/1/doors"), doorsResource.getLink(Link.REL_SELF));

        DoorResource doorResources = single(doorsResource.getDoors());
        assertEquals(1, doorResources.getLinks().size());
        assertEquals(new Link("http://localhost/games/1/doors/2"), doorResources.getLink(Link.REL_SELF));
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

    private final <T> T single(Set<T> set) {
        return set.iterator().next();
    }

}
