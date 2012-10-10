# Create a New Game
    >>>
    POST /games
    ---
    201 CREATED
    Location: /games/1
    <<<

# Game Status
    >>>
    GET /games/1
    ---
    200 OK
    {
        "status": "AWAITING_INITIAL_SELECTION",
        "_links": [ {
            "rel": "self",
            "href": ".../games/1"
        }, {
            "rel": "doors",
            "href": ".../games/1/doors"
        }, {
            "rel": "history",
            "href": ".../games/1/history"
        } ]
    }
    <<<

`status` can be one of:

* `AWAITING_INITIAL_SELECTION`
* `AWAITING_FINAL_SELECTION`
* `LOST`
* `WON`

# Doors Status
    >>>
    GET /games/1/doors
    ---
    200 OK
    {
        "doors": [ {                            // note: collection of /games/{id}/doors/{id} representations
            "status": "CLOSED",
            "content": "UNKNOWN",
            "_links": [ {
                "rel": "self",
                "href": ".../games/1/doors/1"
            } ]
        }, {
            "status": "CLOSED",
            "content": "UNKNOWN",
            "_links": [ {
                "rel": "self",
                "href": ".../games/1/doors/2"
            } ]
        }, {
            "status": "CLOSED",
            "content": "UNKNOWN",
            "_links": [ {
                "rel": "self",
                "href": ".../games/1/doors/3"
            } ]
        } ],
        "_links": [ {
            "rel": "self",
            "href": ".../games/1/doors"
        } ]
    }
    <<<

`status` can be one of:

* `CLOSED`
* `OPEN`
* `SELECTED`

`content` can be one of:

* `JUERGEN`
* `SMALL_FURRY_ANIMAL`
* `UNKNOWN`

# Select a Door
    >>>
    POST /games/1/doors/1
    {
        "status": "SELECTED"            // note: not a total representation
    }
    ---
    200 OK
    {
        "status": "CLOSED",             // note: representation use in /games/{id}/doors
        "content": "UNKNOWN",
        "_links": [ {
            "rel": "self",
            "href": ".../games/1/doors/1"
        } ]
    }
    <<<

Alternate response codes:

* `409 CONFLICT`: If the game protocol does not permit the given door to be changed to the requested status at this point in time. (e.g. a `POST` with `{ "status": "CLOSED"}` once a door has already been selected.

# Open a Door
    >>>
    POST /games/1/doors/2
    {
        "status": "OPEN"                // note: not a total representation
    }
    ---
    200 OK
    {
        "status": "OPEN",               // note: representation use in /games/{id}/doors
        "content": "JUERGEN",
        "_links": [ {
            "rel": "self",
            "href": ".../games/1/doors/2"
        } ]
    }
    <<<

Alternate response codes:

* `409 CONFLICT`: If the game protocol does not permit the given door to be changed to the requested status at this point in time. (e.g. a `POST` with `{ "status": "CLOSED"}` once a door has already been opened.

# Game History
    >>>
    GET /games/1/history
    ---
    200 OK
    {
        "history": [ {
            "game": ".../games/1",
            status: "AWAITING_INITIAL_SELECTION"
        }, {
            "door": ".../games/1/doors/1",
            "status": "SELECTED"
        }, {
            "door": ".../games/1/doors/3",
            "status": "OPEN"
        }, {
            "game": ".../games/1",
            status: "AWAITING_FINAL_SELECTION"
        }, {
            "door": ".../games/1/doors/2",
            "status": "OPEN"
        }, {
            "game": ".../games/1",
            status: "WON"
        } ],
        "_links": [ {
            "rel": "self",
            "href": ".../games/1/history"
        } ]
    }
    <<<

`history` entries are one of two types:

* A door type comprised of:
    * `door`: the identity of the door this history is for
    * `status`: a snapshot of the status of the door

* A game type comprised of:
    * `game`: the identity of the game the history is for
    * `status`: a snapshot of the status of the game

# Typical Game Interaction
    POST /games                                     // Create Game
    GET  /games/1                                   // Find link to doors and history
    GET  /games/1/doors                             // Find door identities
    POST /games/1/doors/1  {"status": "SELECTED"}   // Select door
    GET  /games/1/doors                             // Current status of all doors
    POST /games/1/doors/2  {"status": "OPEN"}       // Open door
    GET  /games/1                                   // Find game outcome

