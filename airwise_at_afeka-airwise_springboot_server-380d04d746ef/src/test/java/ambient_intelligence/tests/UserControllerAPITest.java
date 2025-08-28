package ambient_intelligence.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import ambient_intelligence.logic.boundaries.NewUserBoundary;
import ambient_intelligence.logic.boundaries.UserBoundary;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class UserControllerAPITest extends TestHelper {

    private final int port = 8084;
    private String systemID = "2025b.Avital.Vissoky";

    @Autowired
    private UserCrud userCrud;

    @Autowired
    private CommandCrud commandCrud;

    @Autowired
    private ObjectCrud objectCrud;

    @BeforeEach
    public void setup() {
        super.setup();
        this.webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port + "/ambient-intelligence/users").build();
    }

    public void cleanup() {
        super.cleanup();
    }

    @Test
    public void testCreateAndLoginUser() {
        cleanup();
        
        // Arrange
        NewUserBoundary newUser = new NewUserBoundary();
        newUser.setEmail("test.user@example.com");
        newUser.setRole(UserRole.END_USER);
        newUser.setUsername("TestUser");
        newUser.setAvatar("avatar.png");

        // Act: Create user
        UserBoundary createdUser = webTestClient.post().contentType(MediaType.APPLICATION_JSON).bodyValue(newUser)
                .exchange().expectStatus().isOk().expectBody(UserBoundary.class).returnResult().getResponseBody();

        // Assert: Verify created user
        assertNotNull(createdUser);
        assertEquals("test.user@example.com", createdUser.getUserId().getEmail());
        assertEquals(UserRole.END_USER, createdUser.getRole());
        assertEquals("TestUser", createdUser.getUsername());
        assertEquals("avatar.png", createdUser.getAvatar());

        // Act: Login user
        UserBoundary loggedInUser = webTestClient.get()
                .uri("/login/{systemID}/{userEmail}", systemID, "test.user@example.com").exchange().expectStatus()
                .isOk().expectBody(UserBoundary.class).returnResult().getResponseBody();

        // Assert: Verify logged-in user
        assertNotNull(loggedInUser);
        assertEquals("test.user@example.com", loggedInUser.getUserId().getEmail());
        assertEquals(UserRole.END_USER, loggedInUser.getRole());
    }

    @Test
    public void testUpdateUser() {
        cleanup();
        
        // Arrange: Create a user
        NewUserBoundary newUser = new NewUserBoundary();
        newUser.setEmail("test.user@example.com");
        newUser.setRole(UserRole.END_USER);
        newUser.setUsername("TestUser");
        newUser.setAvatar("avatar.png");

        webTestClient.post().contentType(MediaType.APPLICATION_JSON).bodyValue(newUser).exchange().expectStatus()
                .isOk();

        // Arrange: Update data
        UserBoundary update = new UserBoundary();
        update.setRole(UserRole.OPERATOR);
        update.setUsername("UpdatedUser");
        update.setAvatar("new-avatar.png");

        // Act: Update user
        webTestClient.put().uri("/{systemID}/{userEmail}", systemID, "test.user@example.com")
                .contentType(MediaType.APPLICATION_JSON).bodyValue(update).exchange().expectStatus().isOk();

        // Act: Login to verify update
        UserBoundary updatedUser = webTestClient.get()
                .uri("/login/{systemID}/{userEmail}", systemID, "test.user@example.com").exchange().expectStatus()
                .isOk().expectBody(UserBoundary.class).returnResult().getResponseBody();

        // Assert
        assertNotNull(updatedUser);
        assertEquals(UserRole.OPERATOR, updatedUser.getRole());
        assertEquals("UpdatedUser", updatedUser.getUsername());
        assertEquals("new-avatar.png", updatedUser.getAvatar());
    }
}