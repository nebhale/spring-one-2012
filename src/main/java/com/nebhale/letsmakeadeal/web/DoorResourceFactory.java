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

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.nebhale.letsmakeadeal.Door;
import com.nebhale.letsmakeadeal.DoorDoesNotExistException;
import com.nebhale.letsmakeadeal.Game;

@Component
final class DoorResourceFactory {

    DoorResource create(Game game, Long doorId) throws DoorDoesNotExistException {
        Door door = game.getDoor(doorId);
        return addLinks(new DoorResource(door), game, door);
    }

    private DoorResource addLinks(DoorResource resource, Game game, Door door) {
        resource.add(linkTo(GamesController.class).slash(game).slash("doors").slash(door).withSelfRel());
        return resource;
    }

    DoorsResource create(Game game) {
        Set<DoorResource> doorResources = new HashSet<DoorResource>();
        for (Door door : game.getDoors()) {
            doorResources.add(addLinks(new DoorResource(door), game, door));
        }
        return addLinks(new DoorsResource(doorResources), game);
    }

    private DoorsResource addLinks(DoorsResource resource, Game game) {
        resource.add(linkTo(GamesController.class).slash(game).slash("doors").withSelfRel());
        return resource;
    }

}
