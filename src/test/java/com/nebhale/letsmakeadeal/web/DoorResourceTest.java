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

import org.junit.Test;

import com.nebhale.letsmakeadeal.Door;
import com.nebhale.letsmakeadeal.DoorContent;
import com.nebhale.letsmakeadeal.DoorStatus;

public final class DoorResourceTest {

    private final Door door = new Door(1L, DoorContent.JUERGEN);

    private final DoorResource resource = new DoorResource(this.door);

    @Test
    public void test() {
        assertEquals(DoorContent.UNKNOWN, this.resource.getContent());
        assertEquals(DoorStatus.CLOSED, this.resource.getStatus());
    }

}
