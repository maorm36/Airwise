package ambient_intelligence.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import ambient_intelligence.dal.CommandCrud;
import ambient_intelligence.dal.ObjectCrud;
import ambient_intelligence.dal.UserCrud;
import ambient_intelligence.data.UserRole;
import ambient_intelligence.logic.boundaries.CommandBoundary;
import ambient_intelligence.logic.boundaries.CreatedBy;
import ambient_intelligence.logic.boundaries.InvokedBy;
import ambient_intelligence.logic.boundaries.ObjectBoundary;
import ambient_intelligence.logic.boundaries.TargetObject;
import ambient_intelligence.logic.boundaries.UserBoundary;
import ambient_intelligence.logic.boundaries.UserId;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class AdminControllerAPITest extends TestHelper {

	private final int port = 8084;
	private String SystemID = "2025b.Avital.Vissoky";
	private String adminEmail = "admin@example.com";

	@Autowired
	private UserCrud userCrud;

	@Autowired
	private CommandCrud commandCrud;

	@Autowired
	private ObjectCrud objectCrud;

	@BeforeEach
	public void setup() {
		super.setup();
		this.webTestClient = WebTestClient.bindToServer().baseUrl("http://localhost:" + port + "/ambient-intelligence")
				.build();
	}

	public void cleanup() {
		super.cleanup();
	}

	@Test
	public void testGetAllUsersAndDeleteCommands() {
		cleanup();

		// Arrange: Create users
		createUser(adminEmail, UserRole.ADMIN, "TestUser1", "avatar1");
		createUser("operator@email.com", UserRole.OPERATOR, "TestUser2", "avatar2");
		createUser("endUser@email.com", UserRole.END_USER, "TestUser3", "avatar3");

		// Act: Get all users
		List<UserBoundary> users = webTestClient.get()
				.uri(uriBuilder -> uriBuilder.path("/admin/users").queryParam("userSystemID", SystemID)
						.queryParam("userEmail", adminEmail).queryParam("size", 10).queryParam("page", 0).build())
				.exchange().expectStatus().isOk().expectBodyList(UserBoundary.class).returnResult().getResponseBody();

		// Assert: Verify users list
		assertNotNull(users);
		assertEquals(3, users.size());
		assertEquals("admin@example.com", users.get(0).getUserId().getEmail());
		assertEquals("operator@email.com", users.get(1).getUserId().getEmail());
		assertEquals("endUser@email.com", users.get(2).getUserId().getEmail());

		// Act: Create object
		ObjectBoundary object = new ObjectBoundary();
		object.setType("AC");
		object.setAlias("Living Room AC");
		object.setActive(true);
		object.setStatus("some_status");
		object.setCreatedBy(new CreatedBy(SystemID, "operator@email.com"));
		object.setObjectDetails(Map.of("brand", "LG", "power", "1200W"));
		ObjectBoundary createdObject = webTestClient.post().uri("/objects").contentType(MediaType.APPLICATION_JSON)
				.bodyValue(object).exchange().expectStatus().isOk().expectBody(ObjectBoundary.class).returnResult()
				.getResponseBody();

		// create command
		CommandBoundary command = new CommandBoundary();
		command.setCommand("TURN_ON_AC");
		command.setTargetObject(new TargetObject(createdObject.getId()));
		command.setInvokedBy(new InvokedBy(new UserId(SystemID, "endUser@email.com")));
		command.setCommandAttributes(Map.of("temperature", 22));
		webTestClient.post().uri("/commands").contentType(MediaType.APPLICATION_JSON).bodyValue(command).exchange()
				.expectStatus().is2xxSuccessful();

		// Act: Delete all commands
		webTestClient.delete().uri(uriBuilder -> uriBuilder.path("/admin/commands").queryParam("userSystemID", SystemID)
				.queryParam("userEmail", adminEmail).build()).exchange().expectStatus().isNoContent();

	}

	@Test
	public void testDeleteAllObjects() {
		cleanup();

		// Arrange: Create users
		createUser(adminEmail, UserRole.ADMIN, "TestUser1", "avatar1");
		createUser("operator@email.com", UserRole.OPERATOR, "TestUser2", "avatar2");
		createUser("endUser@email.com", UserRole.END_USER, "TestUser3", "avatar3");

		// Act: Create object
		ObjectBoundary object = new ObjectBoundary();
		object.setType("AC");
		object.setAlias("Living Room AC");
		object.setActive(true);
		object.setStatus("some_status");
		object.setCreatedBy(new CreatedBy(SystemID, "operator@email.com"));
		object.setObjectDetails(Map.of("brand", "LG", "power", "1200W"));
		ObjectBoundary createdObject = webTestClient.post().uri("/objects").contentType(MediaType.APPLICATION_JSON)
				.bodyValue(object).exchange().expectStatus().isOk().expectBody(ObjectBoundary.class).returnResult()
				.getResponseBody();

		// Act: Delete all objects
		webTestClient.delete().uri(uriBuilder -> uriBuilder.path("/admin/objects").queryParam("userSystemID", SystemID)
				.queryParam("userEmail", adminEmail).build()).exchange().expectStatus().isNoContent();

		// Act: Verify objects are deleted
		List<ObjectBoundary> objects = webTestClient.get()
				.uri(uriBuilder -> uriBuilder.path("/objects").queryParam("userSystemID", SystemID)
						.queryParam("userEmail", "operator@email.com").queryParam("size", 10).queryParam("page", 0)
						.build())
				.exchange().expectStatus().isOk().expectBodyList(ObjectBoundary.class).returnResult().getResponseBody();

		// Assert
		assertNotNull(objects);
		assertEquals(0, objects.size());

	}
}