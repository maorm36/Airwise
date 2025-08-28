package ambient_intelligence.tests;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import ambient_intelligence.dal.CommandCrud;
import ambient_intelligence.dal.ObjectCrud;
import ambient_intelligence.dal.UserCrud;
import ambient_intelligence.data.UserRole;
import ambient_intelligence.logic.boundaries.CreatedBy;
import ambient_intelligence.logic.boundaries.NewUserBoundary;
import ambient_intelligence.logic.boundaries.ObjectBoundary;
import ambient_intelligence.logic.boundaries.ObjectId;

public class TestHelper {

    private final int port = 8084;
    protected final String systemID = "2025b.Avital.Vissoky";
    protected WebTestClient webTestClient;

    @Autowired
    protected UserCrud userCrud;

    @Autowired
    protected CommandCrud commandCrud;

    @Autowired
    protected ObjectCrud objectCrud;

    public void setup() {
        this.webTestClient = WebTestClient.bindToServer().baseUrl("http://localhost:" + port + "/ambient-intelligence").build();
    }

    public void cleanup() {
        this.userCrud.deleteAll();
        this.commandCrud.deleteAll();
        this.objectCrud.deleteAll();
    }

    public void createUser(String email, UserRole role, String username, String avatar) {
        NewUserBoundary user = new NewUserBoundary();
        user.setEmail(email);
        user.setRole(role);
        user.setUsername(username);
        user.setAvatar(avatar);
        webTestClient.post().uri("/users").contentType(MediaType.APPLICATION_JSON).bodyValue(user).exchange()
                .expectStatus().is2xxSuccessful();
    }

    public ObjectBoundary createObject(String type, String alias, String status, boolean active, String creatorEmail) {
        ObjectBoundary object = new ObjectBoundary();
        ObjectId objectId = new ObjectId();
        objectId.setSystemID(systemID);
        object.setId(objectId);
        object.setType(type);
        object.setAlias(alias);
        object.setStatus(status);
        object.setActive(active);
        object.setCreationTimestamp("2025-05-23T00:00:00Z");
        object.setCreatedBy(new CreatedBy(systemID, creatorEmail));
        object.setObjectDetails(new HashMap<>(Map.of("brand", "LG", "power", "1200W")));
        return webTestClient.post().uri("/objects").contentType(MediaType.APPLICATION_JSON).bodyValue(object).exchange()
                .expectStatus().isOk().expectBody(ObjectBoundary.class).returnResult().getResponseBody();
    }
}