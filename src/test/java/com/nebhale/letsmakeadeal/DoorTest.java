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

import org.junit.Test;

public final class DoorTest {

    private final Door door = new Door(1L, DoorContent.JUERGEN);

    @Test
    public void test() {
        assertEquals(Long.valueOf(1), this.door.getId());
        assertEquals(DoorContent.JUERGEN, this.door.peekContent());

        assertEquals(DoorStatus.CLOSED, this.door.getStatus());
        assertEquals(DoorContent.UNKNOWN, this.door.getContent());

        this.door.setStatus(DoorStatus.SELECTED);
        assertEquals(DoorStatus.SELECTED, this.door.getStatus());
        assertEquals(DoorContent.UNKNOWN, this.door.getContent());

        this.door.setStatus(DoorStatus.OPEN);
        assertEquals(DoorStatus.OPEN, this.door.getStatus());
        assertEquals(DoorContent.JUERGEN, this.door.getContent());
    }

}
