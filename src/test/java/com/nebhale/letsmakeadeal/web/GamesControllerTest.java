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
import static org.springframework.test.web.server.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.server.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.server.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.server.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.server.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.server.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.server.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.server.setup.MockMvcBuilders.xmlConfigSetup;

import java.io.UnsupportedEncodingException;

import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.server.MockMvc;
import org.springframework.test.web.server.MvcResult;

import com.jayway.jsonpath.JsonPath;

public final class GamesControllerTest {

    private static final String LINK_FORMAT = "$.links[?(@.rel==%s)].href[0]";

    private final MockMvc mockMvc = xmlConfigSetup("file:src/main/webapp/WEB-INF/spring/root-context.xml",
        "file:src/main/webapp/WEB-INF/spring/appServlet/servlet-context.xml").build();

    @Test
    public void createGame() throws Exception {
        this.mockMvc.perform(post("/games")) //
        .andExpect(status().isCreated()) //
        .andExpect(header().string("Location", "http://localhost/games/0"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void showGame() throws Exception {
        String gameLocation = getLocation(this.mockMvc.perform(post("/games")).andReturn());

        this.mockMvc.perform(get(gameLocation).accept(MediaType.APPLICATION_JSON)) //
        .andExpect(status().isOk()) //
        .andExpect(jsonPath("$.status").value("AWAITING_INITIAL_SELECTION")) //
        .andExpect(jsonPath("$.links").value(collectionWithSize(equalTo(3)))) //
        .andExpect(jsonPath("$.links[?(@.rel==self)].href[0]").value(gameLocation)) //
        .andExpect(jsonPath("$.links[?(@.rel==doors)].href[0]").value(gameLocation + "/doors")) //
        .andExpect(jsonPath("$.links[?(@.rel==history)].href[0]").value(gameLocation + "/history"));
    }

    @Test
    public void showGameDoesNotExist() throws Exception {
        String gameLocation = getLocation(this.mockMvc.perform(post("/games")).andReturn());
        this.mockMvc.perform(delete(gameLocation)).andReturn();

        this.mockMvc.perform(get(gameLocation).accept(MediaType.APPLICATION_JSON)) //
        .andExpect(status().isNotFound()) //
        .andExpect(content().string("Game '0' does not exist"));
    }

    @Test
    public void showGameInvalidAccept() throws Exception {
        String gameLocation = getLocation(this.mockMvc.perform(post("/games")).andReturn());

        this.mockMvc.perform(get(gameLocation).accept(MediaType.APPLICATION_XML)) //
        .andExpect(status().isMethodNotAllowed());
    }

    @Test
    public void destroyGame() throws Exception {
        String gameLocation = getLocation(this.mockMvc.perform(post("/games")).andReturn());

        this.mockMvc.perform(delete(gameLocation)) //
        .andExpect(status().isOk());
    }

    @Test
    public void destroyGameDoesNotExist() throws Exception {
        String gameLocation = getLocation(this.mockMvc.perform(post("/games")).andReturn());
        this.mockMvc.perform(delete(gameLocation)).andReturn();

        this.mockMvc.perform(delete(gameLocation)) //
        .andExpect(status().isNotFound()) //
        .andExpect(content().string("Game '0' does not exist"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void showDoors() throws Exception {
        String gameLocation = getLocation(this.mockMvc.perform(post("/games")).andReturn());
        String doorsLocation = getLinkedLocation(gameLocation, "doors");

        this.mockMvc.perform(get(doorsLocation).accept(MediaType.APPLICATION_JSON)) //
        .andExpect(status().isOk()) //
        .andExpect(jsonPath("$.doors").isArray()) //
        .andExpect(jsonPath("$.doors").value(collectionWithSize(equalTo(3)))) //
        .andExpect(jsonPath("$.doors[*].status").value(hasItems("CLOSED"))) //
        .andExpect(jsonPath("$.doors[*].content").value(hasItems("UNKNOWN"))) //
        .andExpect(
            jsonPath("$.doors[*].links[?(@.rel==self)].href").value(hasItems(doorsLocation + "/1", doorsLocation + "/2", doorsLocation + "/3"))) //
        .andExpect(jsonPath("$.links").value(collectionWithSize(equalTo(1)))) //
        .andExpect(jsonPath("$.links[?(@.rel==self)].href[0]").value(doorsLocation));
    }

    @Test
    public void showDoorsGameDoesNotExist() throws Exception {
        String gameLocation = getLocation(this.mockMvc.perform(post("/games")).andReturn());
        String doorsLocation = getLinkedLocation(gameLocation, "doors");
        this.mockMvc.perform(delete(gameLocation)).andReturn();

        this.mockMvc.perform(get(doorsLocation)) //
        .andExpect(status().isNotFound()) //
        .andExpect(content().string("Game '0' does not exist"));
    }

    @Test
    public void modifyDoorSelect() throws Exception {
        String gameLocation = getLocation(this.mockMvc.perform(post("/games")).andReturn());
        String doorsLocation = getLinkedLocation(gameLocation, "doors");
        String doorLocation = getDoorLocation(doorsLocation);

        this.mockMvc.perform(
            post(doorLocation).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).body(
                getBytes("{ \"status\" : \"SELECTED\" }"))) //
        .andExpect(status().isOk()) //
        .andExpect(jsonPath("$.status").value("SELECTED"));
    }

    @Test
    public void modifyDoorOpen() throws Exception {
        String gameLocation = getLocation(this.mockMvc.perform(post("/games")).andReturn());
        String doorsLocation = getLinkedLocation(gameLocation, "doors");
        String doorLocation = getDoorLocation(doorsLocation);

        this.mockMvc.perform(
            post(doorLocation).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).body(
                getBytes("{ \"status\" : \"SELECTED\" }"))) //
        .andExpect(status().isOk()) //
        .andExpect(jsonPath("$.status").value("SELECTED"));

        this.mockMvc.perform(
            post(doorLocation).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).body(getBytes("{ \"status\" : \"OPEN\" }"))) //
        .andExpect(status().isOk()) //
        .andExpect(jsonPath("$.status").value("OPEN"));
    }

    @Test
    public void modifyDoorClosed() throws Exception {
        String gameLocation = getLocation(this.mockMvc.perform(post("/games")).andReturn());
        String doorsLocation = getLinkedLocation(gameLocation, "doors");
        String doorLocation = getDoorLocation(doorsLocation);

        this.mockMvc.perform(
            post(doorLocation).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).body(
                getBytes("{ \"status\" : \"CLOSED\" }"))) //
        .andExpect(status().isConflict()) //
        .andExpect(content().string("It is illegal to transition door '1' in game '0' to 'CLOSED'"));
    }

    @Test
    public void modifyDoorNoStatus() throws UnsupportedEncodingException, Exception {
        String gameLocation = getLocation(this.mockMvc.perform(post("/games")).andReturn());
        String doorsLocation = getLinkedLocation(gameLocation, "doors");
        String doorLocation = getDoorLocation(doorsLocation);

        this.mockMvc.perform(post(doorLocation).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).body(getBytes("{}"))) //
        .andExpect(status().isBadRequest()) //
        .andExpect(content().string("Payload is missing key 'status'"));
    }

    @Test
    public void modifyDoorBadStatus() throws UnsupportedEncodingException, Exception {
        String gameLocation = getLocation(this.mockMvc.perform(post("/games")).andReturn());
        String doorsLocation = getLinkedLocation(gameLocation, "doors");
        String doorLocation = getDoorLocation(doorsLocation);

        this.mockMvc.perform(
            post(doorLocation).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).body(getBytes("{ \"status\" : \"foo\" }"))) //
        .andExpect(status().isBadRequest()) //
        .andExpect(content().string("'foo' is an illegal value for key 'status'"));
    }

    @Test
    public void modifyDoorIllegalTransition() throws Exception {
        String gameLocation = getLocation(this.mockMvc.perform(post("/games")).andReturn());
        String doorsLocation = getLinkedLocation(gameLocation, "doors");
        String doorLocation = getDoorLocation(doorsLocation);

        this.mockMvc.perform(
            post(doorLocation).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).body(getBytes("{ \"status\" : \"open\" }"))) //
        .andExpect(status().isConflict()) //
        .andExpect(content().string("It is illegal to transition game '0' from 'AWAITING_INITIAL_SELECTION' to 'WON'"));
    }

    private String getLocation(MvcResult mvcResult) {
        return mvcResult.getResponse().getHeader("Location");
    }

    private String getLinkedLocation(String location, String rel) throws Exception {
        String json = this.mockMvc.perform(get(location)).andReturn().getResponse().getContentAsString();
        return JsonPath.read(json, String.format(LINK_FORMAT, rel));
    }

    private byte[] getBytes(String s) throws UnsupportedEncodingException {
        return s.getBytes("UTF8");
    }

    private String getDoorLocation(String location) throws UnsupportedEncodingException, Exception {
        String json = this.mockMvc.perform(get(location)).andReturn().getResponse().getContentAsString();
        return JsonPath.read(json, "$.doors[*].links[?(@.rel==self)].href[0]");

    }

}
