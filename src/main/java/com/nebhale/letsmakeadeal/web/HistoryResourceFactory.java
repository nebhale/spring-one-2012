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

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.nebhale.letsmakeadeal.DoorHistoryItem;
import com.nebhale.letsmakeadeal.Game;
import com.nebhale.letsmakeadeal.GameHistoryItem;

@Component
final class HistoryResourceFactory {

    HistoryResource create(Game game) {
        List<Object> historyItemResources = new ArrayList<Object>();
        for (Object historyItem : game.getHistory()) {
            if (historyItem instanceof GameHistoryItem) {
                GameHistoryItem gameHistoryItem = (GameHistoryItem) historyItem;
                String gameUri = linkTo(GamesController.class).slash(gameHistoryItem.getId()).toUri().toString();
                historyItemResources.add(new GameHistoryItemResource(gameUri, gameHistoryItem.getStatus()));
            } else {
                DoorHistoryItem doorHistoryItem = (DoorHistoryItem) historyItem;
                String doorUri = linkTo(GamesController.class).slash(doorHistoryItem.getGameId()).slash("doors").slash(doorHistoryItem.getDoorId()).toUri().toString();
                historyItemResources.add(new DoorHistoryItemResource(doorUri, doorHistoryItem.getStatus()));
            }
        }
        return addLinks(new HistoryResource(historyItemResources), game);
    }

    private HistoryResource addLinks(HistoryResource resource, Game game) {
        resource.add(linkTo(GamesController.class).slash(game).slash("history").withSelfRel());
        return resource;
    }

}
