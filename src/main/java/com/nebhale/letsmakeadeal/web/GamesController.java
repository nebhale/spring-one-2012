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

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.nebhale.letsmakeadeal.DoorDoesNotExistException;
import com.nebhale.letsmakeadeal.DoorStatus;
import com.nebhale.letsmakeadeal.Game;
import com.nebhale.letsmakeadeal.GameDoesNotExistException;
import com.nebhale.letsmakeadeal.GameRepository;
import com.nebhale.letsmakeadeal.IllegalTransitionException;

@Controller
@RequestMapping("/games")
final class GamesController {

    private static final String STATUS_KEY = "status";

    private final GameRepository gameRepository;

    private final GameResourceAssembler gameResourceAssembler;

    private final DoorsResourceAssembler doorsResourceAssembler;

    @Autowired
    GamesController(GameRepository gameRepository, GameResourceAssembler gameResourceAssembler, DoorsResourceAssembler doorsResourceAssembler) {
        this.gameRepository = gameRepository;
        this.gameResourceAssembler = gameResourceAssembler;
        this.doorsResourceAssembler = doorsResourceAssembler;
    }

    @RequestMapping(method = RequestMethod.POST, value = "")
    ResponseEntity<Void> createGame() {
        Game game = this.gameRepository.create();

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(linkTo(GamesController.class).slash(game.getId()).toUri());

        return new ResponseEntity<Void>(headers, HttpStatus.CREATED);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{gameId}", produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_XML_VALUE })
    ResponseEntity<GameResource> showGame(@PathVariable Long gameId) throws GameDoesNotExistException {
        Game game = this.gameRepository.retrieve(gameId);
        GameResource resource = this.gameResourceAssembler.toResource(game);

        return new ResponseEntity<GameResource>(resource, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/{gameId}")
    ResponseEntity<Void> destroyGame(@PathVariable Long gameId) throws GameDoesNotExistException {
        this.gameRepository.remove(gameId);
        return new ResponseEntity<Void>(HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{gameId}/doors", produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_XML_VALUE })
    ResponseEntity<DoorsResource> showDoors(@PathVariable Long gameId) throws GameDoesNotExistException {
        Game game = this.gameRepository.retrieve(gameId);
        DoorsResource resource = this.doorsResourceAssembler.toResource(game);

        return new ResponseEntity<DoorsResource>(resource, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/{gameId}/doors/{doorId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = {
        MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_XML_VALUE })
    ResponseEntity<Void> modifyDoor(@PathVariable Long gameId, @PathVariable Long doorId, @RequestBody Map<String, String> body)
        throws MissingKeyException, GameDoesNotExistException, IllegalTransitionException, DoorDoesNotExistException {
        DoorStatus status = getStatus(body);
        Game game = this.gameRepository.retrieve(gameId);

        if (DoorStatus.SELECTED == status) {
            game.select(doorId);
        } else if (DoorStatus.OPEN == status) {
            game.open(doorId);
        } else {
            throw new IllegalTransitionException(gameId, doorId, status);
        }

        return new ResponseEntity<Void>(HttpStatus.OK);
    }

    @ExceptionHandler({ GameDoesNotExistException.class, DoorDoesNotExistException.class })
    ResponseEntity<String> handleNotFounds(Exception e) {
        return new ResponseEntity<String>(e.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({ IllegalArgumentException.class, MissingKeyException.class })
    ResponseEntity<String> handleBadRequests(Exception e) {
        return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalTransitionException.class)
    ResponseEntity<String> handleConflicts(Exception e) {
        return new ResponseEntity<String>(e.getMessage(), HttpStatus.CONFLICT);
    }

    private DoorStatus getStatus(Map<String, String> body) throws MissingKeyException {
        if (body.containsKey(STATUS_KEY)) {
            String value = body.get(STATUS_KEY);

            try {
                return DoorStatus.valueOf(value.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(String.format("'%s' is an illegal value for key '%s'", value, STATUS_KEY), e);
            }
        }

        throw new MissingKeyException(STATUS_KEY);
    }
}
