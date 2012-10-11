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

public final class GameHistoryItem {

    private final Long id;

    private final GameStatus status;

    GameHistoryItem(Long id, GameStatus status) {
        this.id = id;
        this.status = status;
    }

    public Long getId() {
        return this.id;
    }

    public GameStatus getStatus() {
        return this.status;
    }

}
