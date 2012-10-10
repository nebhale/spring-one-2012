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

import java.util.Collections;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.hateoas.Link;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.nebhale.letsmakeadeal.Door;
import com.nebhale.letsmakeadeal.Game;

public class GameResourceFactoryTest {

    private final Game game = new Game(1L, Collections.<Door> emptySet());

    private final GameResourceFactory resourceFactory = new GameResourceFactory();

    @Test
    public void test() {
        GameResource resource = this.resourceFactory.create(this.game);
        assertEquals(3, resource.getLinks().size());
        assertEquals(new Link("http://localhost/games/1", Link.REL_SELF), resource.getLink(Link.REL_SELF));
        assertEquals(new Link("http://localhost/games/1/doors", "doors"), resource.getLink("doors"));
        assertEquals(new Link("http://localhost/games/1/history", "history"), resource.getLink("history"));
    }

    @BeforeClass
    public static void setRequestContext() {
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));
    }

    @AfterClass
    public static void clearRequestContext() {
        RequestContextHolder.resetRequestAttributes();
    }

}
