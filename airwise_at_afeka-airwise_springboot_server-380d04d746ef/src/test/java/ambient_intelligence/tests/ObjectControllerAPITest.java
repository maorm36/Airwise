package ambient_intelligence.tests;

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
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
import ambient_intelligence.logic.boundaries.CreatedBy;
import ambient_intelligence.logic.boundaries.NewUserBoundary;
import ambient_intelligence.logic.boundaries.ObjectBoundary;
import ambient_intelligence.logic.boundaries.ObjectChildIdBoundary;
import ambient_intelligence.logic.boundaries.ObjectId;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class ObjectControllerAPITest extends TestHelper {

    private final int port = 8084;
    private String SystemID = "2025b.Avital.Vissoky";
    private String operatorEmail = "operator@email.com";

    @Autowired
    private UserCrud userCrud;

    @Autowired
    private CommandCrud commandCrud;

    @Autowired
    private ObjectCrud objectCrud;

    @BeforeEach
    public void setup() {
        super.setup();
    }

    public void cleanup() {
        super.cleanup();
    }

    @Test
    public void testCreateAndGetObject() {
        cleanup();

        // Arrange: Create operator user
        createUser(operatorEmail, UserRole.OPERATOR, "OperatorUser", "operator_avatar.png");

        // Arrange: Create object
        ObjectBoundary object = new ObjectBoundary();
        ObjectId objectId = new ObjectId();
        objectId.setSystemID(SystemID);
        object.setId(objectId);
        object.setType("AC");
        object.setAlias("Living Room AC");
        object.setStatus("ON");
        object.setActive(true);
        object.setCreationTimestamp("2025-05-23T00:00:00Z");
        object.setCreatedBy(new CreatedBy(SystemID, operatorEmail));
        object.setObjectDetails(new HashMap<>(Map.of("brand", "LG", "power", "1200W")));

        // Act: Create object
        ObjectBoundary createdObject = webTestClient.post().uri("/objects")
                .contentType(MediaType.APPLICATION_JSON).bodyValue(object).exchange().expectStatus().isOk()
                .expectBody(ObjectBoundary.class).returnResult().getResponseBody();

        // Assert: Verify created object
        assertNotNull(createdObject);
        assertNotNull(createdObject.getId());
        assertEquals("AC", createdObject.getType());
        assertEquals("Living Room AC", createdObject.getAlias());
        assertEquals("ON", createdObject.getStatus());
        assertTrue(createdObject.isActive());
        assertNotNull(createdObject.getCreationTimestamp());
        assertEquals(operatorEmail, createdObject.getCreatedBy().getUserId().getEmail());
        assertEquals("LG", createdObject.getObjectDetails().get("brand"));

        // Act: Get specific object
        String objectIdValue = createdObject.getId().getObjectId();
        ObjectBoundary retrievedObject = webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/objects/{systemID}/{objectId}")
                        .queryParam("userSystemID", SystemID).queryParam("userEmail", operatorEmail)
                        .build(SystemID, objectIdValue))
                .exchange().expectStatus().isOk().expectBody(ObjectBoundary.class).returnResult().getResponseBody();

        // Assert: Verify retrieved object
        assertNotNull(retrievedObject);
        assertEquals("AC", retrievedObject.getType());
        assertEquals("Living Room AC", retrievedObject.getAlias());
        assertEquals("ON", retrievedObject.getStatus());
        assertTrue(retrievedObject.isActive());
        assertNotNull(retrievedObject.getCreationTimestamp());
        assertEquals(operatorEmail, retrievedObject.getCreatedBy().getUserId().getEmail());
        assertEquals("LG", retrievedObject.getObjectDetails().get("brand"));
    }

    @Test
    public void testUpdateObject() {
        cleanup();

        // Arrange: Create users
        createUser("admin@example.com", UserRole.ADMIN, "AdminUser", "admin_avatar.png");
        createUser(operatorEmail, UserRole.OPERATOR, "OperatorUser", "operator_avatar.png");
        createUser("endUser@example.com", UserRole.END_USER, "EndUser", "enduser_avatar.png");

        // Arrange: Create object
        ObjectBoundary object = new ObjectBoundary();
        ObjectId objectId = new ObjectId();
        objectId.setSystemID(SystemID);
        object.setId(objectId);
        object.setType("Light");
        object.setAlias("Bedroom Light");
        object.setStatus("ON");
        object.setActive(true);
        object.setCreationTimestamp("2025-05-23T00:00:00Z");
        object.setCreatedBy(new CreatedBy(SystemID, operatorEmail));
        object.setObjectDetails(new HashMap<>(Map.of("color", "White")));

        ObjectBoundary createdObject = webTestClient.post().uri("/objects")
                .contentType(MediaType.APPLICATION_JSON).bodyValue(object).exchange().expectStatus().isOk()
                .expectBody(ObjectBoundary.class).returnResult().getResponseBody();

        // Arrange: Update data
        ObjectBoundary update = new ObjectBoundary();
        update.setType("SmartLight");
        update.setAlias("Updated Bedroom Light");
        update.setStatus("OFF");
        update.setActive(false);
        update.setObjectDetails(new HashMap<>(Map.of("color", "RGB")));
        update.setCreatedBy(object.getCreatedBy());

        // Act: Update object
        String objectIdValue = createdObject.getId().getObjectId();
        webTestClient.put()
                .uri(uriBuilder -> uriBuilder.path("/objects/{systemID}/{objectId}")
                        .queryParam("userSystemID", SystemID).queryParam("userEmail", operatorEmail)
                        .build(SystemID, objectIdValue))
                .contentType(MediaType.APPLICATION_JSON).bodyValue(update).exchange().expectStatus().isOk();

        // Act: Retrieve updated object
        ObjectBoundary updatedObject = webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/objects/{systemID}/{objectId}")
                        .queryParam("userSystemID", SystemID).queryParam("userEmail", operatorEmail)
                        .build(SystemID, objectIdValue))
                .exchange().expectStatus().isOk().expectBody(ObjectBoundary.class).returnResult().getResponseBody();

        // Assert
        assertNotNull(updatedObject);
        assertEquals("SmartLight", updatedObject.getType());
        assertEquals("Updated Bedroom Light", updatedObject.getAlias());
        assertEquals("OFF", updatedObject.getStatus());
        assertFalse(updatedObject.isActive());
        assertEquals("RGB", updatedObject.getObjectDetails().get("color"));
    }

    @Test
    public void testGetAllObjects() {
        cleanup();

        // Arrange: Create users
        createUser("admin@example.com", UserRole.ADMIN, "AdminUser", "admin_avatar.png");
        createUser(operatorEmail, UserRole.OPERATOR, "OperatorUser", "operator_avatar.png");
        createUser("endUser@example.com", UserRole.END_USER, "EndUser", "enduser_avatar.png");

        // Arrange: Create two objects
        ObjectBoundary object1 = new ObjectBoundary();
        ObjectId objectId1 = new ObjectId();
        objectId1.setSystemID(SystemID);
        object1.setId(objectId1);
        object1.setType("AC");
        object1.setAlias("Living Room AC");
        object1.setStatus("ON");
        object1.setActive(true);
        object1.setCreationTimestamp("2025-05-23T00:00:00Z");
        object1.setCreatedBy(new CreatedBy(SystemID, operatorEmail));
        object1.setObjectDetails(new HashMap<>(Map.of("brand", "LG", "power", "1200W")));

        ObjectBoundary object2 = new ObjectBoundary();
        ObjectId objectId2 = new ObjectId();
        objectId2.setSystemID(SystemID);
        object2.setId(objectId2);
        object2.setType("Light");
        object2.setAlias("Kitchen Light");
        object2.setStatus("OFF");
        object2.setActive(true);
        object2.setCreationTimestamp("2025-05-23T00:00:01Z");
        object2.setCreatedBy(new CreatedBy(SystemID, operatorEmail));
        object2.setObjectDetails(new HashMap<>(Map.of("color", "White")));

        webTestClient.post().uri("/objects").contentType(MediaType.APPLICATION_JSON)
                .bodyValue(object1).exchange().expectStatus().isOk();

        webTestClient.post().uri("/objects").contentType(MediaType.APPLICATION_JSON)
                .bodyValue(object2).exchange().expectStatus().isOk();

        // Act: Get all objects as OPERATOR
        List<ObjectBoundary> objectsOperator = webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/objects").queryParam("userSystemID", SystemID)
                        .queryParam("userEmail", operatorEmail).queryParam("size", 10).queryParam("page", 0).build())
                .exchange().expectStatus().isOk().expectBodyList(ObjectBoundary.class).returnResult().getResponseBody();

        // Assert: Verify OPERATOR sees all objects
        assertNotNull(objectsOperator);
        assertEquals(2, objectsOperator.size());
        assertTrue(objectsOperator.stream().anyMatch(obj -> obj.getAlias().equals("Living Room AC")));
        assertTrue(objectsOperator.stream().anyMatch(obj -> obj.getAlias().equals("Kitchen Light")));

        // Act: Get all objects as END_USER
        List<ObjectBoundary> objectsEndUser = webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/objects").queryParam("userSystemID", SystemID)
                        .queryParam("userEmail", "endUser@example.com").queryParam("size", 10).queryParam("page", 0)
                        .build())
                .exchange().expectStatus().isOk().expectBodyList(ObjectBoundary.class).returnResult().getResponseBody();

        // Assert: Verify END_USER sees only active objects
        assertNotNull(objectsEndUser);
        assertEquals(2, objectsEndUser.size());
        assertTrue(objectsEndUser.stream().allMatch(ObjectBoundary::isActive));
    }

    @Test
    public void testBindAndGetChildren() {
        cleanup();

        // Arrange: Create operator user
        createUser(operatorEmail, UserRole.OPERATOR, "OperatorUser", "operator_avatar.png");

        // Arrange: Create parent and child objects
        ObjectBoundary parent = new ObjectBoundary();
        ObjectId parentId = new ObjectId();
        parentId.setSystemID(SystemID);
        parent.setId(parentId);
        parent.setType("Room");
        parent.setAlias("Living Room");
        parent.setStatus("OCCUPIED");
        parent.setActive(true);
        parent.setCreationTimestamp("2025-05-23T00:00:00Z");
        parent.setCreatedBy(new CreatedBy(SystemID, operatorEmail));
        parent.setObjectDetails(new HashMap<>(Map.of("size", "20m²")));

        ObjectBoundary child = new ObjectBoundary();
        ObjectId childId = new ObjectId();
        childId.setSystemID(SystemID);
        child.setId(childId);
        child.setType("AC");
        child.setAlias("Living Room AC");
        child.setStatus("ON");
        child.setActive(true);
        child.setCreationTimestamp("2025-05-23T00:00:01Z");
        child.setCreatedBy(new CreatedBy(SystemID, operatorEmail));
        child.setObjectDetails(new HashMap<>(Map.of("brand", "LG", "power", "1200W")));

        ObjectBoundary parentObject = webTestClient.post().uri("/objects")
                .contentType(MediaType.APPLICATION_JSON).bodyValue(parent).exchange().expectStatus().isOk()
                .expectBody(ObjectBoundary.class).returnResult().getResponseBody();

        ObjectBoundary childObject = webTestClient.post().uri("/objects")
                .contentType(MediaType.APPLICATION_JSON).bodyValue(child).exchange().expectStatus().isOk()
                .expectBody(ObjectBoundary.class).returnResult().getResponseBody();

        // Arrange: Bind parent and child
        ObjectChildIdBoundary childIdBoundary = new ObjectChildIdBoundary();
        childIdBoundary.setChildId(childObject.getId());

        webTestClient.put()
                .uri(uriBuilder -> uriBuilder
                        .path("/objects/{parentSystemID}/{parentObjectId}/children")
                        .queryParam("userSystemID", SystemID).queryParam("userEmail", operatorEmail)
                        .build(SystemID, parentObject.getId().getObjectId()))
                .contentType(MediaType.APPLICATION_JSON).bodyValue(childIdBoundary).exchange().expectStatus().isOk();

        // Act: Get children
        List<ObjectBoundary> children = webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/objects/{parentSystemID}/{parentObjectId}/children")
                        .queryParam("userSystemID", SystemID).queryParam("userEmail", operatorEmail)
                        .queryParam("size", 10).queryParam("page", 0)
                        .build(SystemID, parentObject.getId().getObjectId()))
                .exchange().expectStatus().isOk().expectBodyList(ObjectBoundary.class).returnResult().getResponseBody();

        // Assert
        assertNotNull(children);
        assertEquals(1, children.size());
        assertEquals("Living Room AC", children.get(0).getAlias());
        assertEquals("ON", children.get(0).getStatus());
        assertTrue(children.get(0).isActive());
        assertEquals("LG", children.get(0).getObjectDetails().get("brand"));
    }

    @Test
    public void testGetParents() {
        cleanup();

        // Arrange: Create users
        createUser("admin@example.com", UserRole.ADMIN, "AdminUser", "admin_avatar.png");
        createUser(operatorEmail, UserRole.OPERATOR, "OperatorUser", "operator_avatar.png");
        createUser("endUser@example.com", UserRole.END_USER, "EndUser", "enduser_avatar.png");

        // Arrange: Create parent and child objects
        ObjectBoundary parent = new ObjectBoundary();
        ObjectId parentId = new ObjectId();
        parentId.setSystemID(SystemID);
        parent.setId(parentId);
        parent.setType("Room");
        parent.setAlias("Living Room");
        parent.setStatus("OCCUPIED");
        parent.setActive(true);
        parent.setCreationTimestamp("2025-05-23T00:00:00Z");
        parent.setCreatedBy(new CreatedBy(SystemID, operatorEmail));
        parent.setObjectDetails(new HashMap<>(Map.of("size", "20m²")));

        ObjectBoundary child = new ObjectBoundary();
        ObjectId childId = new ObjectId();
        childId.setSystemID(SystemID);
        child.setId(childId);
        child.setType("AC");
        child.setAlias("Living Room AC");
        child.setStatus("ON");
        child.setActive(true);
        child.setCreationTimestamp("2025-05-23T00:00:01Z");
        child.setCreatedBy(new CreatedBy(SystemID, operatorEmail));
        child.setObjectDetails(new HashMap<>(Map.of("brand", "LG", "power", "1200W")));

        ObjectBoundary parentObject = webTestClient.post().uri("/objects")
                .contentType(MediaType.APPLICATION_JSON).bodyValue(parent).exchange().expectStatus().isOk()
                .expectBody(ObjectBoundary.class).returnResult().getResponseBody();

        ObjectBoundary childObject = webTestClient.post().uri("/objects")
                .contentType(MediaType.APPLICATION_JSON).bodyValue(child).exchange().expectStatus().isOk()
                .expectBody(ObjectBoundary.class).returnResult().getResponseBody();

        // Arrange: Bind parent and child
        webTestClient.put()
                .uri(uriBuilder -> uriBuilder
                        .path("/objects/{parentSystemID}/{parentObjectId}/children")
                        .queryParam("userSystemID", SystemID).queryParam("userEmail", operatorEmail)
                        .build(SystemID, parentObject.getId().getObjectId()))
                .contentType(MediaType.APPLICATION_JSON).bodyValue(new ObjectChildIdBoundary(childObject.getId()))
                .exchange().expectStatus().isOk();

        // Act: Get parents as OPERATOR
        List<ObjectBoundary> parentsOperator = webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/objects/{childSystemID}/{childObjectId}/parents")
                        .queryParam("userSystemID", SystemID).queryParam("userEmail", operatorEmail)
                        .queryParam("size", 10).queryParam("page", 0)
                        .build(SystemID, childObject.getId().getObjectId()))
                .exchange().expectStatus().isOk().expectBodyList(ObjectBoundary.class).returnResult().getResponseBody();

        // Assert: Verify OPERATOR sees parent
        assertNotNull(parentsOperator);
        assertEquals(1, parentsOperator.size());
        assertEquals("Living Room", parentsOperator.get(0).getAlias());
        assertEquals("OCCUPIED", parentsOperator.get(0).getStatus());
        assertTrue(parentsOperator.get(0).isActive());
        assertEquals("20m²", parentsOperator.get(0).getObjectDetails().get("size"));

        // Act: Get parents as END_USER
        List<ObjectBoundary> parentsEndUser = webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/objects/{childSystemID}/{childObjectId}/parents")
                        .queryParam("userSystemID", SystemID).queryParam("userEmail", "endUser@example.com")
                        .queryParam("size", 10).queryParam("page", 0)
                        .build(SystemID, childObject.getId().getObjectId()))
                .exchange().expectStatus().isOk().expectBodyList(ObjectBoundary.class).returnResult().getResponseBody();

        // Assert: Verify END_USER sees active parent
        assertNotNull(parentsEndUser);
        assertEquals(1, parentsEndUser.size());
        assertEquals("Living Room", parentsEndUser.get(0).getAlias());
        assertTrue(parentsEndUser.get(0).isActive());
    }

    @Test
    public void testSearchByAliasPattern() {
        cleanup();

        // Arrange: Create operator user
        createUser(operatorEmail, UserRole.OPERATOR, "OperatorUser", "operator_avatar.png");
        // Arrange: Create enduser user
        createUser("endUser@example.com", UserRole.END_USER, "endUser", "endUser_avatar.png");

        // Arrange: Create two objects
        ObjectBoundary object1 = new ObjectBoundary();
        ObjectId objectId1 = new ObjectId();
        objectId1.setSystemID(SystemID);
        object1.setId(objectId1);
        object1.setType("AC");
        object1.setAlias("Living Room AC");
        object1.setStatus("ON");
        object1.setActive(true);
        object1.setCreationTimestamp("2025-05-23T00:00:00Z");
        object1.setCreatedBy(new CreatedBy(SystemID, operatorEmail));
        object1.setObjectDetails(new HashMap<>(Map.of("brand", "LG", "power", "1200W")));

        ObjectBoundary object2 = new ObjectBoundary();
        ObjectId objectId2 = new ObjectId();
        objectId2.setSystemID(SystemID);
        object2.setId(objectId2);
        object2.setType("Thermostat");
        object2.setAlias("Kitchen Thermostat");
        object2.setStatus("OFF");
        object2.setActive(true);
        object2.setCreationTimestamp("2025-05-23T00:00:01Z");
        object2.setCreatedBy(new CreatedBy(SystemID, operatorEmail));
        object2.setObjectDetails(new HashMap<>(Map.of("model", "SmartThermo")));

        webTestClient.post().uri("/objects").contentType(MediaType.APPLICATION_JSON)
                .bodyValue(object1).exchange().expectStatus().isOk();

        webTestClient.post().uri("/objects").contentType(MediaType.APPLICATION_JSON)
                .bodyValue(object2).exchange().expectStatus().isOk();

        // Act: Search by alias pattern as OPERATOR
        List<ObjectBoundary> resultsOperator = webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/objects/search/byAliasPattern/{pattern}")
                        .queryParam("userSystemID", SystemID).queryParam("userEmail", operatorEmail)
                        .queryParam("size", 10).queryParam("page", 0).build("Room"))
                .exchange().expectStatus().isOk().expectBodyList(ObjectBoundary.class).returnResult().getResponseBody();

        // Assert: Verify OPERATOR sees matching objects
        assertNotNull(resultsOperator);
        assertEquals(1, resultsOperator.size());
        assertEquals("Living Room AC", resultsOperator.get(0).getAlias());
        assertEquals("ON", resultsOperator.get(0).getStatus());
        assertTrue(resultsOperator.get(0).isActive());

        // Act: Search by alias pattern as END_USER
        List<ObjectBoundary> resultsEndUser = webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/objects/search/byAliasPattern/{pattern}")
                        .queryParam("userSystemID", SystemID).queryParam("userEmail", "endUser@example.com")
                        .queryParam("size", 10).queryParam("page", 0).build("Room"))
                .exchange().expectStatus().isOk().expectBodyList(ObjectBoundary.class).returnResult().getResponseBody();

        // Assert: Verify END_USER sees active matching objects
        assertNotNull(resultsEndUser);
        assertEquals(1, resultsEndUser.size());
        assertEquals("Living Room AC", resultsEndUser.get(0).getAlias());
        assertTrue(resultsEndUser.get(0).isActive());
    }

    @Test
    public void testSearchByType() {
        cleanup();

        // Arrange: Create operator user
        createUser(operatorEmail, UserRole.OPERATOR, "OperatorUser", "operator_avatar.png");
        // Arrange: Create enduser user
        createUser("endUser@example.com", UserRole.END_USER, "endUser", "endUser_avatar.png");

        // Arrange: Create two objects
        ObjectBoundary object1 = new ObjectBoundary();
        ObjectId objectId1 = new ObjectId();
        objectId1.setSystemID(SystemID);
        object1.setId(objectId1);
        object1.setType("AC");
        object1.setAlias("Living Room AC");
        object1.setStatus("ON");
        object1.setActive(true);
        object1.setCreationTimestamp("2025-05-23T00:00:00Z");
        object1.setCreatedBy(new CreatedBy(SystemID, operatorEmail));
        object1.setObjectDetails(new HashMap<>(Map.of("brand", "LG", "power", "1200W")));

        ObjectBoundary object2 = new ObjectBoundary();
        ObjectId objectId2 = new ObjectId();
        objectId2.setSystemID(SystemID);
        object2.setId(objectId2);
        object2.setType("Light");
        object2.setAlias("Kitchen Light");
        object2.setStatus("OFF");
        object2.setActive(true);
        object2.setCreationTimestamp("2025-05-23T00:00:01Z");
        object2.setCreatedBy(new CreatedBy(SystemID, operatorEmail));
        object2.setObjectDetails(new HashMap<>(Map.of("color", "White")));

        webTestClient.post().uri("/objects").contentType(MediaType.APPLICATION_JSON)
                .bodyValue(object1).exchange().expectStatus().isOk();

        webTestClient.post().uri("/objects").contentType(MediaType.APPLICATION_JSON)
                .bodyValue(object2).exchange().expectStatus().isOk();

        // Act: Search by type as OPERATOR
        List<ObjectBoundary> resultsOperator = webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/objects/search/byType/{type}")
                        .queryParam("userSystemID", SystemID).queryParam("userEmail", operatorEmail)
                        .queryParam("size", 10).queryParam("page", 0).build("AC"))
                .exchange().expectStatus().isOk().expectBodyList(ObjectBoundary.class).returnResult().getResponseBody();

        // Assert: Verify OPERATOR sees matching objects
        assertNotNull(resultsOperator);
        assertEquals(1, resultsOperator.size());
        assertEquals("AC", resultsOperator.get(0).getType());
        assertEquals("Living Room AC", resultsOperator.get(0).getAlias());
        assertEquals("ON", resultsOperator.get(0).getStatus());

        // Act: Search by type as END_USER
        List<ObjectBoundary> resultsEndUser = webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/objects/search/byType/{type}")
                        .queryParam("userSystemID", SystemID).queryParam("userEmail", "endUser@example.com")
                        .queryParam("size", 10).queryParam("page", 0).build("AC"))
                .exchange().expectStatus().isOk().expectBodyList(ObjectBoundary.class).returnResult().getResponseBody();

        // Assert: Verify END_USER sees active matching objects
        assertNotNull(resultsEndUser);
        assertEquals(1, resultsEndUser.size());
        assertEquals("AC", resultsEndUser.get(0).getType());
        assertTrue(resultsEndUser.get(0).isActive());
    }

    @Test
    public void testSearchByTypeAndStatus() {
        cleanup();

        // Arrange: Create operator user
        createUser(operatorEmail, UserRole.OPERATOR, "OperatorUser", "operator_avatar.png");
        // Arrange: Create enduser user
        createUser("endUser@example.com", UserRole.END_USER, "endUser", "endUser_avatar.png");

        // Arrange: Create two objects
        ObjectBoundary object1 = new ObjectBoundary();
        ObjectId objectId1 = new ObjectId();
        objectId1.setSystemID(SystemID);
        object1.setId(objectId1);
        object1.setType("AC");
        object1.setAlias("Living Room AC");
        object1.setStatus("ON");
        object1.setActive(true);
        object1.setCreationTimestamp("2025-05-23T00:00:00Z");
        object1.setCreatedBy(new CreatedBy(SystemID, operatorEmail));
        object1.setObjectDetails(new HashMap<>(Map.of("brand", "LG", "power", "1200W")));

        ObjectBoundary object2 = new ObjectBoundary();
        ObjectId objectId2 = new ObjectId();
        objectId2.setSystemID(SystemID);
        object2.setId(objectId2);
        object2.setType("AC");
        object2.setAlias("Bedroom AC");
        object2.setStatus("OFF");
        object2.setActive(true);
        object2.setCreationTimestamp("2025-05-23T00:00:01Z");
        object2.setCreatedBy(new CreatedBy(SystemID, operatorEmail));
        object2.setObjectDetails(new HashMap<>(Map.of("brand", "Tornado", "power", "1000W")));

        webTestClient.post().uri("/objects").contentType(MediaType.APPLICATION_JSON)
                .bodyValue(object1).exchange().expectStatus().isOk();

        webTestClient.post().uri("/objects").contentType(MediaType.APPLICATION_JSON)
                .bodyValue(object2).exchange().expectStatus().isOk();

        // Act: Search by type and status as OPERATOR
        List<ObjectBoundary> resultsOperator = webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/objects/search/byTypeAndStatus/{type}/{status}")
                        .queryParam("userSystemID", SystemID).queryParam("userEmail", operatorEmail)
                        .queryParam("size", 10).queryParam("page", 0).build("AC", "ON"))
                .exchange().expectStatus().isOk().expectBodyList(ObjectBoundary.class).returnResult().getResponseBody();

        // Assert: Verify OPERATOR sees matching objects
        assertNotNull(resultsOperator);
        assertEquals(1, resultsOperator.size());
        assertEquals("AC", resultsOperator.get(0).getType());
        assertEquals("ON", resultsOperator.get(0).getStatus());
        assertEquals("Living Room AC", resultsOperator.get(0).getAlias());

        // Act: Search by type and status as END_USER
        List<ObjectBoundary> resultsEndUser = webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/objects/search/byTypeAndStatus/{type}/{status}")
                        .queryParam("userSystemID", SystemID).queryParam("userEmail", "endUser@example.com")
                        .queryParam("size", 10).queryParam("page", 0).build("AC", "ON"))
                .exchange().expectStatus().isOk().expectBodyList(ObjectBoundary.class).returnResult().getResponseBody();

        // Assert: Verify END_USER sees active matching objects
        assertNotNull(resultsEndUser);
        assertEquals(1, resultsEndUser.size());
        assertEquals("AC", resultsEndUser.get(0).getType());
        assertEquals("ON", resultsEndUser.get(0).getStatus());
        assertTrue(resultsEndUser.get(0).isActive());
    }

    @Test
    public void testSearchByExactAlias() {
        cleanup();

        // Arrange: Create users
        createUser("admin@example.com", UserRole.ADMIN, "AdminUser", "admin_avatar.png");
        createUser(operatorEmail, UserRole.OPERATOR, "OperatorUser", "operator_avatar.png");
        createUser("endUser@example.com", UserRole.END_USER, "EndUser", "enduser_avatar.png");

        // Arrange: Create object
        ObjectBoundary object1 = new ObjectBoundary();
        ObjectId objectId1 = new ObjectId();
        objectId1.setSystemID(SystemID);
        object1.setId(objectId1);
        object1.setType("AC");
        object1.setAlias("Living_Room_AC");
        object1.setStatus("ON");
        object1.setActive(true);
        object1.setCreationTimestamp("2025-05-23T00:00:00Z");
        object1.setCreatedBy(new CreatedBy(SystemID, operatorEmail));
        object1.setObjectDetails(new HashMap<>(Map.of("brand", "LG", "power", "1200W")));

        webTestClient.post().uri("/objects").contentType(MediaType.APPLICATION_JSON)
                .bodyValue(object1).exchange().expectStatus().isOk().expectBody(ObjectBoundary.class);

        // Act: Search by exact alias as OPERATOR
        List<ObjectBoundary> resultsOperator = webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/objects/search/byAlias/{alias}")
                        .queryParam("userSystemID", SystemID).queryParam("userEmail", operatorEmail)
                        .queryParam("size", 10).queryParam("page", 0).build("Living_Room_AC"))
                .exchange().expectStatus().isOk().expectBodyList(ObjectBoundary.class).returnResult().getResponseBody();

        // Assert: Verify OPERATOR sees matching object
        assertNotNull(resultsOperator);
        assertEquals(1, resultsOperator.size());
        assertEquals("Living_Room_AC", resultsOperator.get(0).getAlias());
        assertEquals("ON", resultsOperator.get(0).getStatus());
        assertTrue(resultsOperator.get(0).isActive());

        // Act: Search by exact alias as END_USER
        List<ObjectBoundary> resultsEndUser = webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/objects/search/byAlias/{alias}")
                        .queryParam("userSystemID", SystemID).queryParam("userEmail", "endUser@example.com")
                        .queryParam("size", 10).queryParam("page", 0).build("Living_Room_AC"))
                .exchange().expectStatus().isOk().expectBodyList(ObjectBoundary.class).returnResult().getResponseBody();

        // Assert: Verify END_USER sees active matching object
        assertNotNull(resultsEndUser);
        assertEquals(1, resultsEndUser.size());
        assertEquals("Living_Room_AC", resultsEndUser.get(0).getAlias());
        assertTrue(resultsEndUser.get(0).isActive());
    }

    @Test
    public void testSearchByStatus() {
        cleanup();

        // Arrange: Create users
        createUser("admin@example.com", UserRole.ADMIN, "AdminUser", "admin_avatar.png");
        createUser(operatorEmail, UserRole.OPERATOR, "OperatorUser", "operator_avatar.png");
        createUser("endUser@example.com", UserRole.END_USER, "EndUser", "enduser_avatar.png");

        // Arrange: Create two objects
        ObjectBoundary object1 = new ObjectBoundary();
        ObjectId objectId1 = new ObjectId();
        objectId1.setSystemID(SystemID);
        object1.setId(objectId1);
        object1.setType("AC");
        object1.setAlias("Living_Room_AC");
        object1.setStatus("ON");
        object1.setActive(true);
        object1.setCreationTimestamp("2025-05-23T00:00:00Z");
        object1.setCreatedBy(new CreatedBy(SystemID, operatorEmail));
        object1.setObjectDetails(new HashMap<>(Map.of("brand", "LG", "power", "1200W")));

        ObjectBoundary object2 = new ObjectBoundary();
        ObjectId objectId2 = new ObjectId();
        objectId2.setSystemID(SystemID);
        object2.setId(objectId2);
        object2.setType("AC");
        object2.setAlias("Kitchen AC");
        object2.setStatus("OFF");
        object2.setActive(true);
        object2.setCreationTimestamp("2025-05-23T00:00:01Z");
        object2.setCreatedBy(new CreatedBy(SystemID, operatorEmail));
        object2.setObjectDetails(new HashMap<>(Map.of("brand", "Tornado", "power", "1000W")));

        webTestClient.post().uri("/objects").contentType(MediaType.APPLICATION_JSON)
                .bodyValue(object1).exchange().expectStatus().isOk().expectBody(ObjectBoundary.class);

        webTestClient.post().uri("/objects").contentType(MediaType.APPLICATION_JSON)
                .bodyValue(object2).exchange().expectStatus().isOk().expectBody(ObjectBoundary.class);

        // Act: Search by status "ON" as OPERATOR
        List<ObjectBoundary> resultsOnOperator = webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/objects/search/byStatus/{status}")
                        .queryParam("userSystemID", SystemID).queryParam("userEmail", operatorEmail)
                        .queryParam("size", 10).queryParam("page", 0).build("ON"))
                .exchange().expectStatus().isOk().expectBodyList(ObjectBoundary.class).returnResult().getResponseBody();

        // Assert: Verify OPERATOR sees matching objects
        assertNotNull(resultsOnOperator);
        assertEquals(1, resultsOnOperator.size());
        assertEquals("ON", resultsOnOperator.get(0).getStatus());
        assertEquals("Living_Room_AC", resultsOnOperator.get(0).getAlias());

        // Act: Search by status "OFF" as OPERATOR
        List<ObjectBoundary> resultsOffOperator = webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/objects/search/byStatus/{status}")
                        .queryParam("userSystemID", SystemID).queryParam("userEmail", operatorEmail)
                        .queryParam("size", 10).queryParam("page", 0).build("OFF"))
                .exchange().expectStatus().isOk().expectBodyList(ObjectBoundary.class).returnResult().getResponseBody();

        // Assert: Verify OPERATOR sees matching objects
        assertNotNull(resultsOffOperator);
        assertEquals(1, resultsOffOperator.size());
        assertEquals("OFF", resultsOffOperator.get(0).getStatus());
        assertEquals("Kitchen AC", resultsOffOperator.get(0).getAlias());

        // Act: Search by status "ON" as END_USER
        List<ObjectBoundary> resultsOnEndUser = webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/objects/search/byStatus/{status}")
                        .queryParam("userSystemID", SystemID).queryParam("userEmail", "endUser@example.com")
                        .queryParam("size", 10).queryParam("page", 0).build("ON"))
                .exchange().expectStatus().isOk().expectBodyList(ObjectBoundary.class).returnResult().getResponseBody();

        // Assert: Verify END_USER sees active matching objects
        assertNotNull(resultsOnEndUser);
        assertEquals(1, resultsOnEndUser.size());
        assertEquals("ON", resultsOnEndUser.get(0).getStatus());
        assertTrue(resultsOnEndUser.get(0).isActive());

        // Act: Search by status "OFF" as END_USER
        List<ObjectBoundary> resultsOffEndUser = webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/objects/search/byStatus/{status}")
                        .queryParam("userSystemID", SystemID).queryParam("userEmail", "endUser@example.com")
                        .queryParam("size", 10).queryParam("page", 0).build("OFF"))
                .exchange().expectStatus().isOk().expectBodyList(ObjectBoundary.class).returnResult().getResponseBody();

        // Assert: Verify END_USER sees active matching objects
        assertNotNull(resultsOffEndUser);
        assertEquals(1, resultsOffEndUser.size());
        assertEquals("OFF", resultsOffEndUser.get(0).getStatus());
        assertTrue(resultsOffEndUser.get(0).isActive());
    }
}