package de.mpg.imeji.rest.resources.test.integration;

import de.mpg.imeji.logic.controller.CollectionController;
import de.mpg.imeji.logic.vo.CollectionImeji;
import de.mpg.imeji.logic.vo.User;
import de.mpg.imeji.presentation.util.ImejiFactory;
import de.mpg.imeji.rest.api.CollectionService;
import de.mpg.imeji.rest.api.ItemService;
import de.mpg.imeji.rest.process.BasicAuthentication;
import de.mpg.imeji.rest.process.CollectionProcess;
import de.mpg.imeji.rest.process.RestProcessUtils;
import de.mpg.imeji.rest.resources.CollectionResource;
import de.mpg.imeji.rest.resources.test.TestUtils;
import de.mpg.imeji.rest.to.CollectionTO;
import de.mpg.imeji.rest.to.ItemTO;
import de.mpg.imeji.rest.to.ItemWithFileTO;
import de.mpg.imeji.rest.to.JSONResponse;
import de.mpg.j2j.exceptions.NotFoundException;

import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.JenaUtil;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CollectionTest extends ImejiRestTest {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(CollectionTest.class);

	private static String pathPrefix = "/collections";

	private static String collId = null;

	public static String profileId = null;

	private static final String TEST_IMAGE = "./src/test/resources/storage/test.png";

	private static File file = null;

	@BeforeClass
	public static void specificSetup() {
		profileId = createProfile(adminUser);
	}

	@Test
	public void test_1_CreateCollection_1_DefaultProfile() throws IOException {
		Path jsonPath = Paths
				.get("src/test/resources/rest/createCollection.json");
		String jsonString = new String(Files.readAllBytes(jsonPath), "UTF-8");

		Response response = target(pathPrefix)
				.register(authAsAdmin)
				.register(MultiPartFeature.class)
				.request(MediaType.APPLICATION_JSON_TYPE)
				.post(Entity
						.entity(jsonString, MediaType.APPLICATION_JSON_TYPE));
		assertEquals(response.getStatus(), Status.CREATED.getStatusCode());
		Map<String, Object> collData = TestUtils.jsonToPOJO(response);
		assertNotNull("Created collection is null", collData);
		collId = (String) collData.get("id");
		assertThat("Empty collection id", collId, not(isEmptyOrNullString()));
		System.out.println(collId);
	}

	@Test
	public void test_1_CreateCollection_2_CopyProfile() throws IOException {
		Path jsonPath = Paths
				.get("src/test/resources/rest/createCollectionWithProfile.json");
		String jsonString = new String(Files.readAllBytes(jsonPath), "UTF-8");
		jsonString = jsonString.replace("___PROFILE_ID___", profileId).replace(
				"___METHOD___", "copy");

		Response response = target(pathPrefix)
				.register(authAsAdmin)
				.register(MultiPartFeature.class)
				.request(MediaType.APPLICATION_JSON_TYPE)
				.post(Entity
						.entity(jsonString, MediaType.APPLICATION_JSON_TYPE));

		assertEquals(response.getStatus(), Status.CREATED.getStatusCode());
		Map<String, Object> collData = TestUtils.jsonToPOJO(response);
		assertNotNull("Created collection is null", collData);
		collId = (String) collData.get("id");
		assertThat("Empty collection id", collId, not(isEmptyOrNullString()));
	}

	@Test
	public void test_1_CreateCollection_3_ReferenceProfile() throws IOException {
		Path jsonPath = Paths
				.get("src/test/resources/rest/createCollectionWithProfile.json");
		String jsonString = new String(Files.readAllBytes(jsonPath), "UTF-8");
		jsonString = jsonString.replace("___PROFILE_ID___", profileId).replace(
				"___METHOD___", "reference");

		Response response = target(pathPrefix)
				.register(authAsAdmin)
				.request(MediaType.APPLICATION_JSON_TYPE)
				.post(Entity
						.entity(jsonString, MediaType.APPLICATION_JSON_TYPE));

		assertEquals(response.getStatus(), Status.CREATED.getStatusCode());
		Map<String, Object> collData = TestUtils.jsonToPOJO(response);
		assertNotNull("Created collection is null", collData);
		collId = (String) collData.get("id");
		assertThat("Empty collection id", collId, not(isEmptyOrNullString()));
	}

	@Test
	public void test_1_CreateCollection_4_NotExistedReferenceProfile()
			throws IOException {
		Path jsonPath = Paths
				.get("src/test/resources/rest/createCollectionWithProfile.json");
		String jsonString = new String(Files.readAllBytes(jsonPath), "UTF-8");
		jsonString = jsonString.replace("___PROFILE_ID___",
				profileId + "shmarrn").replace("___METHOD___", "reference");
		Response response = target(pathPrefix)
				.register(authAsAdmin)
				.request(MediaType.APPLICATION_JSON_TYPE)
				.post(Entity
						.entity(jsonString, MediaType.APPLICATION_JSON_TYPE));

		assertEquals(response.getStatus(), Status.BAD_REQUEST.getStatusCode());

	}

	@Test
	public void test_2_ReadCollection_1() throws IOException {
		Response response = target(pathPrefix).path(collId)
				.register(authAsAdmin).request(MediaType.APPLICATION_JSON)
				.get();
		String jsonString = response.readEntity(String.class);
		assertThat("Empty collection", jsonString, not(isEmptyOrNullString()));
	}

	@Test
	public void test_2_ReadCollection_2_BadRequest() throws IOException {
		Response response = target(pathPrefix).path(collId + "schmarrn")
				.register(authAsAdmin).request(MediaType.APPLICATION_JSON)
				.get();
		assertThat(response.getStatus(),
				equalTo(Status.BAD_REQUEST.getStatusCode()));
	}

	@Test
	public void test_2_ReadCollection_3_Unathorized() throws IOException {
		Response response = target(pathPrefix).path(collId)
				.request(MediaType.APPLICATION_JSON).get();
		// String jsonString = response.readEntity(String.class);
		// assertThat("Authentication should fail!", jsonString,
		// containsString("<div class=\"header\">Unauthorized</div>"));
		assertThat(response.getStatus(),
				equalTo(Status.UNAUTHORIZED.getStatusCode()));

	}

	@Test
	public void test_2_ReadCollection_4_Forbidden() throws IOException {
		Response response = target(pathPrefix).path(collId)
				.register(authAsUser).request(MediaType.APPLICATION_JSON).get();
		assertThat(response.getStatus(),
				equalTo(Status.FORBIDDEN.getStatusCode()));
	}

	@Test
	public void test_3_ReleaseCollection_1_WithAdmin() throws Exception {


//		JSONResponse resp = new JSONResponse();
//
//		CollectionTO to = null;
//		CollectionService ccrud = new CollectionService();
//		to = ccrud.read(collId, adminUser);
//		resp.setObject(to);
//
//		ObjectWriter ow = new ObjectMapper().writer()
//				.withDefaultPrettyPrinter();
//		String json = "";
//		try {
//			json = ow.writeValueAsString(resp.getObject());
//		} catch (JsonProcessingException e) {
//
//			e.printStackTrace();
//		}
//		System.out.println(json);

		// c.setStatus(de.mpg.imeji.logic.vo.Properties.Status.RELEASED);
		//

		Response response = target(pathPrefix).path("/" + collId + "/release")
				.register(authAsAdmin).request(MediaType.APPLICATION_JSON_TYPE)
				.put(Entity.json("{}"));
		//
		assertEquals(response.getStatus(), Status.OK.getStatusCode());
		CollectionService s = new CollectionService();
		assertEquals("RELEASED",s.read(collId, JenaUtil.testUser).getStatus());
		//
		// System.out.println(c.getStatus());

	}

}
