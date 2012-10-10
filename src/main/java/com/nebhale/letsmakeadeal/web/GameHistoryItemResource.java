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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.nebhale.letsmakeadeal.GameStatus;

@XmlRootElement(name = "history-item")
final class GameHistoryItemResource {

    private final String game;

    private final GameStatus status;

    GameHistoryItemResource() {
        this(null, null);
    }

    GameHistoryItemResource(String game, GameStatus status) {
        this.game = game;
        this.status = status;
    }

    @XmlAttribute
    public String getGame() {
        return this.game;
    }

    @XmlAttribute
    public GameStatus getStatus() {
        return this.status;
    }
}
