import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.util.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;


public class OrderTests {

    private static String accessToken;

    private static final List<String> VALID_INGREDIENT_IDS =
            Arrays.asList("61c0c5a71d1f82001bdaaa6d", "61c0c5a71d1f82001bdaaa6f");
    private static final List<String> INVALID_INGREDIENT_IDS =
            Collections.singletonList("invalid_hash_1234567890");


    @BeforeEach
    public void setUp() {
        RestAssured.baseURI = "https://stellarburgers.education-services.ru/";
    }

    @AfterAll
    @DisplayName("Удаление пользователя после тестов")
    public static void tearDown() {
        if (accessToken != null && !accessToken.isEmpty()) {
            deleteUser(accessToken);
            accessToken = null;
        }

    }

    @Test
    @DisplayName("Создание заказа с авторизацией и валидными ингредиентами")
    public void createOrderWithAuth() {
        UserData userData = new UserData("evgenpharaosha@gmail.com", "12345678", "Rengoku");

        Response registerResponse = createNewUser(userData);
        registerResponse.then().statusCode(200).body("success", equalTo(true));

        Response loginResponse = loginUser(userData);
        accessToken = extractAccessToken(loginResponse);

        Response orderResponse = makeOrderWithAuth(accessToken, VALID_INGREDIENT_IDS);
        orderResponse.then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("order.number", notNullValue());
    }

    @Step("Отправить POST-запрос на создание пользователя (endpoint: /api/auth/register)")
    public Response createNewUser(UserData userData) {
        return given()
                .header("Content-type", "application/json")
                .body(userData)
                .when()
                .post("/api/auth/register");
    }

    @Step("Отправить POST-запрос на логин пользователя (endpoint: /api/auth/login)")
    public Response loginUser(UserData userData) {
        return given()
                .header("Content-type", "application/json")
                .body(userData)
                .when()
                .post("/api/auth/login");
    }

    @Step("Извлечь accessToken из ответа")
    public String extractAccessToken(Response response) {
        String token = response.then().extract().path("accessToken");
        return token != null ? token.replace("Bearer ", "") : null;
    }

    @Step("Удалить пользователя по accessToken (endpoint: /api/auth/user)")
    public static void deleteUser(String accessToken) {
        Response response = given()
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .delete("/api/auth/user");

        if (response.statusCode() != 202) {
            System.out.println("Ошибка при удалении пользователя:");
            response.prettyPrint();
        }

        response.then().assertThat().statusCode(202);


    }

    @Step("Создание заказа с авторизацией (POST /api/orders)")
    public Response makeOrderWithAuth(String token, List<String> ingredientIds) {

        Map<String, Object> body = new HashMap<>();
        body.put("ingredients", ingredientIds);

        return given()
                .header("Authorization", "Bearer " + token)
                .header("Content-type", "application/json")
                .body(body)
                .when()
                .post("/api/orders");
    }

    @Test
    @DisplayName("Создание заказа без авторизации")
    public void createOrderWithoutAuth() {

        Response orderResponse = makeOrderWithoutAuth(VALID_INGREDIENT_IDS);
        orderResponse.then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("order.number", notNullValue());


    }

    @Step("Создание заказа без авторизации (POST /api/orders)")
    public Response makeOrderWithoutAuth(List<String> ingredientIds) {
        Map<String, Object> body = new HashMap<>();
        body.put("ingredients", ingredientIds);

        return given()
                .header("Content-type", "application/json")
                .body(body)
                .when()
                .post("/api/orders");
    }

    @Test
    @DisplayName("Создание заказа с невалидными ингредиентами")
    public void createOrderWithInvalidIngredients() {
        Response orderResponse = makeOrderWithAuth(accessToken, INVALID_INGREDIENT_IDS);
        orderResponse.then()
                .statusCode(500);
        System.out.println(orderResponse.getBody().asString());
    }

    @Test
    @DisplayName("Создание заказа без ингредиентов")
    public void createOrderWithEmptyIngredients() {
        Map<String, Object> body = new HashMap<>();
        body.put("ingredients", new ArrayList<>());

        given()
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-type", "application/json")
                .body(body)
                .when()
                .post("/api/orders")
                .then()
                .statusCode(400)
                .body("success", equalTo(false))
                .body("message", equalTo("Ingredient ids must be provided"));

    }



}

