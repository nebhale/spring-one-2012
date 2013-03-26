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

import static com.jayway.jsonassert.JsonAssert.collectionWithSize;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.nebhale.letsmakeadeal.Door;
import com.nebhale.letsmakeadeal.DoorContent;
import com.nebhale.letsmakeadeal.Game;
import com.nebhale.letsmakeadeal.GameDoesNotExistException;
import com.nebhale.letsmakeadeal.GameRepository;

public final class GamesControllerTest {

    private static final String GAME_LOCATION = "http://localhost/games/0";

    private static final String DOORS_LOCATION = GAME_LOCATION + "/doors";

    private static final String DOOR_LOCATION = DOORS_LOCATION + "/1";

    private final Game game;
    {
        Set<Door> doors = new HashSet<Door>();
        doors.add(new Door(1L, DoorContent.SMALL_FURRY_ANIMAL));
        doors.add(new Door(2L, DoorContent.SMALL_FURRY_ANIMAL));
        doors.add(new Door(3L, DoorContent.JUERGEN));
        game = new Game(0L, doors);
    }

    private final GameRepository gameRepository = mock(GameRepository.class);

    private final MockMvc mockMvc = standaloneSetup(
        new GamesController(gameRepository, new GameResourceAssembler(), new DoorsResourceAssembler(new DoorResourceAssembler()))) //
    .build();

    @Test
    public void createGame() throws Exception {
        when(this.gameRepository.create()).thenReturn(game);

        this.mockMvc.perform(post("/games")) //
        .andExpect(status().isCreated()) //
        .andExpect(header().string("Location", "http://localhost/games/0"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void showGame() throws Exception {
        when(this.gameRepository.retrieve(0L)).thenReturn(game);

        this.mockMvc.perform(get(GAME_LOCATION).accept(MediaType.APPLICATION_JSON)) //
        .andExpect(status().isOk()) //
        .andExpect(jsonPath("$.status").value("AWAITING_INITIAL_SELECTION")) //
        .andExpect(jsonPath("$.links").value(collectionWithSize(equalTo(2)))) //
        .andExpect(jsonPath("$.links[?(@.rel==self)].href[0]").value(GAME_LOCATION)) //
        .andExpect(jsonPath("$.links[?(@.rel==doors)].href[0]").value(GAME_LOCATION + "/doors"));
    }

    @Test
    public void showGameGameDoesNotExist() throws Exception {
        when(this.gameRepository.retrieve(0L)).thenThrow(new GameDoesNotExistException(0L));

        this.mockMvc.perform(get(GAME_LOCATION).accept(MediaType.APPLICATION_JSON)) //
        .andExpect(status().isNotFound()) //
        .andExpect(content().string("Game '0' does not exist"));
    }

    @Test
    public void showGameInvalidAccept() throws Exception {
        this.mockMvc.perform(get(GAME_LOCATION).accept(MediaType.APPLICATION_OCTET_STREAM)) //
        .andExpect(status().isNotAcceptable());
    }

    @Test
    public void destroyGame() throws Exception {
        this.mockMvc.perform(delete(GAME_LOCATION)) //
        .andExpect(status().isOk());

        verify(this.gameRepository).remove(0L);
    }

    @Test
    public void destroyGameGameDoesNotExist() throws Exception {
        doThrow(new GameDoesNotExistException(0L)).when(this.gameRepository).remove(0L);

        this.mockMvc.perform(delete(GAME_LOCATION)) //
        .andExpect(status().isNotFound()) //
        .andExpect(content().string("Game '0' does not exist"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void showDoors() throws Exception {
        when(this.gameRepository.retrieve(0L)).thenReturn(game);

        this.mockMvc.perform(get(DOORS_LOCATION).accept(MediaType.APPLICATION_JSON)) //
        .andExpect(status().isOk()) //
        .andExpect(jsonPath("$.doors").isArray()) //
        .andExpect(jsonPath("$.doors").value(collectionWithSize(equalTo(3)))) //
        .andExpect(jsonPath("$.doors[*].status").value(hasItems("CLOSED"))) //
        .andExpect(jsonPath("$.doors[*].content").value(hasItems("UNKNOWN"))) //
        .andExpect(
            jsonPath("$.doors[*].links[?(@.rel==self)].href").value(hasItems(DOORS_LOCATION + "/1", DOORS_LOCATION + "/2", DOORS_LOCATION + "/3"))) //
        .andExpect(jsonPath("$.links").value(collectionWithSize(equalTo(1)))) //
        .andExpect(jsonPath("$.links[?(@.rel==self)].href[0]").value(DOORS_LOCATION));
    }

    @Test
    public void showDoorsGameDoesNotExist() throws Exception {
        when(this.gameRepository.retrieve(0L)).thenThrow(new GameDoesNotExistException(0L));

        this.mockMvc.perform(get(DOORS_LOCATION)) //
        .andExpect(status().isNotFound()) //
        .andExpect(content().string("Game '0' does not exist"));
    }

    @Test
    public void modifyDoorSelect() throws Exception {
        when(this.gameRepository.retrieve(0L)).thenReturn(game);

        this.mockMvc.perform(
            post(DOOR_LOCATION).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).content(
                getBytes("{ \"status\" : \"SELECTED\" }"))) //
        .andExpect(status().isOk());
    }

    @Test
    public void modifyDoorOpen() throws Exception {
        when(this.gameRepository.retrieve(0L)).thenReturn(game);
        game.select(1L);

        this.mockMvc.perform(
            post(DOOR_LOCATION).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).content(
                getBytes("{ \"status\" : \"OPEN\" }"))) //
        .andExpect(status().isOk());
    }

    @Test
    public void modifyDoorClosed() throws Exception {
        when(this.gameRepository.retrieve(0L)).thenReturn(game);

        this.mockMvc.perform(
            post(DOOR_LOCATION).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).content(
                getBytes("{ \"status\" : \"CLOSED\" }"))) //
        .andExpect(status().isConflict()) //
        .andExpect(content().string("It is illegal to transition door '1' in game '0' to 'CLOSED'"));
    }

    @Test
    public void modifyDoorMissingKey() throws Exception {
        this.mockMvc.perform(post(DOOR_LOCATION).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).content(getBytes("{}"))) //
        .andExpect(status().isBadRequest()) //
        .andExpect(content().string("Payload is missing key 'status'"));
    }

    @Test
    public void modifyDoorGameDoesNotExist() throws Exception {
        when(this.gameRepository.retrieve(0L)).thenThrow(new GameDoesNotExistException(0L));

        this.mockMvc.perform(
            post(DOOR_LOCATION).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).content(
                getBytes("{ \"status\" : \"SELECTED\" }"))) //
        .andExpect(status().isNotFound()) //
        .andExpect(content().string("Game '0' does not exist"));
    }

    @Test
    public void modifyDoorDoorDoesNotExist() throws Exception {
        when(this.gameRepository.retrieve(0L)).thenReturn(game);

        this.mockMvc.perform(
            post("http://localhost/games/0/doors/4").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).content(
                getBytes("{ \"status\" : \"SELECTED\" }"))) //
        .andExpect(status().isNotFound()) //
        .andExpect(content().string("Door '4' in game '0' does not exist"));
    }

    @Test
    public void modifyDoorIllegalArgumentException() throws Exception {
        this.mockMvc.perform(
            post(DOOR_LOCATION).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).content(getBytes("{ \"status\": \"foo\"}"))) //
        .andExpect(status().isBadRequest()) //
        .andExpect(content().string("'foo' is an illegal value for key 'status'"));
    }

    private byte[] getBytes(String s) throws UnsupportedEncodingException {
        return s.getBytes("UTF8");
    }

}
