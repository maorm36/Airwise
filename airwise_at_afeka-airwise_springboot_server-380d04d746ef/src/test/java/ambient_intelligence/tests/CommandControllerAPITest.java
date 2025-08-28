package ambient_intelligence.tests;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
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
import ambient_intelligence.logic.boundaries.NewUserBoundary;
import ambient_intelligence.logic.boundaries.ObjectBoundary;
import ambient_intelligence.logic.boundaries.ObjectId;
import ambient_intelligence.logic.boundaries.TargetObject;
import ambient_intelligence.logic.boundaries.UserId;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class CommandControllerAPITest extends TestHelper {

    private final int port = 8084;
    private String SystemID = "2025b.Avital.Vissoky";
    private String adminEmail = "admin@example.com";
    private String endUserEmail = "enduser@example.com";
    private String operatorEmail = "operator@example.com";

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
    public void testInvokeCommand() {
        cleanup();
        
        // create operator user
        createUser(operatorEmail, UserRole.OPERATOR, "TestUser", "avatar");
        // create enduser user
        createUser(endUserEmail, UserRole.END_USER, "TestUser", "avatar");

        // Act: Create object
        ObjectBoundary object = new ObjectBoundary();
        object.setType("AC");
        object.setAlias("Living Room AC");
        object.setActive(true);
        object.setStatus("some_status");
        object.setCreatedBy(new CreatedBy(SystemID, operatorEmail));
        object.setObjectDetails(Map.of("brand", "LG", "power", "1200W"));
        ObjectBoundary createdObject = webTestClient.post().uri("/objects").contentType(MediaType.APPLICATION_JSON)
                .bodyValue(object).exchange().expectStatus().isOk().expectBody(ObjectBoundary.class).returnResult()
                .getResponseBody();

        // create command
        CommandBoundary command = new CommandBoundary();
        command.setCommand("TURN_ON_AC");
        command.setTargetObject(new TargetObject(createdObject.getId()));
        command.setInvokedBy(new InvokedBy(new UserId(SystemID, endUserEmail)));
        command.setCommandAttributes(Map.of("temperature", 22));

        // Act
        List<Object> response = webTestClient.post().uri("/commands").contentType(MediaType.APPLICATION_JSON)
                .bodyValue(command).exchange().expectStatus().isOk().expectBodyList(Object.class).returnResult()
                .getResponseBody();

        // Assert
        assertNotNull(response);
        assertEquals(1, response.size());

        cleanup();
    }

    @Test
    public void testGetAllCommandsHistory() {
        cleanup();
        
        // create operator user
        createUser(operatorEmail, UserRole.OPERATOR, "TestUser", "avatar");
        // create enduser user
        createUser(endUserEmail, UserRole.END_USER, "TestUser", "avatar");
        // create admin user
        createUser(adminEmail, UserRole.ADMIN, "TestUser", "avatar");

        // Act: Create object
        ObjectBoundary object = new ObjectBoundary();
        object.setType("AC");
        object.setAlias("Living Room AC");
        object.setActive(true);
        object.setStatus("some_status");
        object.setCreatedBy(new CreatedBy(SystemID, operatorEmail));
        object.setObjectDetails(Map.of("brand", "LG", "power", "1200W"));
        ObjectBoundary createdObject = webTestClient.post().uri("/objects").contentType(MediaType.APPLICATION_JSON)
                .bodyValue(object).exchange().expectStatus().isOk().expectBody(ObjectBoundary.class).returnResult()
                .getResponseBody();

        // create command
        CommandBoundary command = new CommandBoundary();
        command.setCommand("TURN_ON_AC");
        command.setTargetObject(new TargetObject(createdObject.getId()));
        command.setInvokedBy(new InvokedBy(new UserId(SystemID, endUserEmail)));
        command.setCommandAttributes(Map.of("temperature", 22));

        // Act: save command
        webTestClient.post().uri("/commands").contentType(MediaType.APPLICATION_JSON).bodyValue(command).exchange()
                .expectStatus().is2xxSuccessful();

        // Act: Get command history
        List<CommandBoundary> commands = webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/admin/commands").queryParam("userSystemID", SystemID)
                        .queryParam("userEmail", adminEmail).queryParam("size", 10).queryParam("page", 0).build())
                .exchange().expectStatus().isOk().expectBodyList(CommandBoundary.class).returnResult()
                .getResponseBody();

        // Assert
        assertNotNull(commands);
        assertEquals(1, commands.size());
        assertEquals("TURN_ON_AC", commands.get(0).getCommand());

        cleanup();
    }
}