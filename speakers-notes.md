	@Controller
	@RequestMapping("/games")

---

	@RequestMapping(method = RequestMethod.POST, value = "")
	ResponseEntity<Void> createGame() {
	    Game game = this.gameRepository.create();

	    HttpHeaders headers = new HttpHeaders();
	    headers.setLocation(linkTo(GamesController.class).slash(game.getId()).toUri());

	    return new ResponseEntity<Void>(headers, HttpStatus.CREATED);
	}


* `Location` header
* `CREATED` return code

---

	@RequestMapping(method = RequestMethod.GET, value = "/{gameId}", produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_XML_VALUE })
	ResponseEntity<GameResource> showGame(@PathVariable Long gameId) throws GameDoesNotExistException {
	    Game game = this.gameRepository.retrieve(gameId);
	    GameResource resource = this.gameResourceAssembler.toResource(game);

	    return new ResponseEntity<GameResource>(resource, HttpStatus.OK);
	}

* `produces`
* **SINGLE `produces`**
* **DON'T use resources**

---

	@RequestMapping(method = RequestMethod.PUT, value = "/{gameId}/doors/{doorId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = {
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

* `consumes`

---

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

* `@ExceptionHandler` can handle single or multiple types
* Useful information in the response body

---

	GameResource resource = createResource(game);
	resource.status = game.getStatus();
	resource.add(linkTo(GamesController.class).slash(game).slash("doors").withRel("doors"));
	return resource;

* `ResourceSupport` (don't want to pollute domain model)
* `ResourceAssemblerSupport`
* `linkTo` based on Controller `@RequestMapping`

---

	DoorResource resource = new DoorResource();
	resource.status = door.getStatus();
	resource.content = door.getContent();
	resource.add(linkTo(GamesController.class).slash(game).slash("doors").slash(door).withSelfRel());

	return resource;

* Extension of `ResourceAssemblerSupport` not required
* Have to manually create instance and add self rel
* **USE Resource where avoided earlier**
* **MULTIPLE `produces` and demo**

---

	private final GameRepository gameRepository = mock(GameRepository.class);

	private final MockMvc mockMvc = standaloneSetup(
	    new GamesController(gameRepository, new GameResourceAssembler(), new DoorsResourceAssembler(new DoorResourceAssembler()))) //
	.build();

* Setting up a _unit test_ using `standaloneSetup`

---

	@Test
	public void createGame() throws Exception {
	    when(this.gameRepository.create()).thenReturn(game);

	    this.mockMvc.perform(post("/games")) //
	    .andExpect(status().isCreated()) //
	    .andExpect(header().string("Location", "http://localhost/games/0"));
	}

* `perform` verbs against a full set of infrastructure
* all normal dispatch, type conversion, error handling
* assert values in headers
* unit testing is done against mocks, just as you'd expect

---

	@Test
	@SuppressWarnings("unchecked")
	public void showGame() throws Exception {
	    when(this.gameRepository.retrieve(0L)).thenReturn(game);

	    this.mockMvc.perform(get(GAME_LOCATION).accept(MediaType.APPLICATION_JSON)) //
	    .andExpect(status().isOk()) //
	    .andExpect(jsonPath("$.status").value("AWAITING_INITIAL_SELECTION")) //
	    .andExpect(jsonPath("$.links").value(collectionWithSize(equalTo(2)))) //
	    .andExpect(jsonPath("$.links[?(@.rel==self)].href[0]").value(GAME_LOCATION)) //
	    .andExpect(jsonPath("$.links[?(@.rel==doors)].href[0]").value(DOORS_LOCATION));
	}

* payloads validated using hamcrest matchers
* JSON payloads validated using JSONPath

---

	@Test
	public void showGameGameDoesNotExist() throws Exception {
	    when(this.gameRepository.retrieve(0L)).thenThrow(new GameDoesNotExistException(0L));

	    this.mockMvc.perform(get(GAME_LOCATION).accept(MediaType.APPLICATION_JSON)) //
	    .andExpect(status().isNotFound()) //
	    .andExpect(content().string("Game '0' does not exist"));
	}

* `status` and `content` assertions in the case of failures

---

	@RunWith(SpringJUnit4ClassRunner.class)
	@WebAppConfiguration
	@ContextConfiguration({ "file:src/main/webapp/WEB-INF/spring/root-context.xml", "file:src/main/webapp/WEB-INF/spring/appServlet/servlet-context.xml" })

	...

	@Autowired
	private volatile WebApplicationContext webApplicationContext;

	private volatile MockMvc mockMvc;

	@Before
	public void before() {
	    this.mockMvc = webAppContextSetup(this.webApplicationContext).build();
	}

* Setting up an _integration test_ using `webAppContextSetup`
* Delegates all the way to the back end so we can actually play an entire game, outside of the container
