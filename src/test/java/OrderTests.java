import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.util.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class OrderTests {

    private String accessToken;
    private UserData testUser;

    private static final List<String> VALID_INGREDIENT_IDS =
            Arrays.asList("61c0c5a71d1f82001bdaaa6d", "61c0c5a71d1f82001bdaaa6f");
    private static final List<String> INVALID_INGREDIENT_IDS =
            Collections.singletonList("invalid_hash_1234567890");

    private final UserApi userApi = new UserApi();

    @BeforeEach
    @DisplayName("Создание нового пользователя перед тестом")
    public void setUpUser() {
        testUser = new UserData("evgenpharaosha@gmail.com", "12345678", "Rengoku");

        Response registerResponse = userApi.createNewUser(testUser);
        registerResponse.then().statusCode(200).body("success", equalTo(true));

        Response loginResponse = userApi.loginUser(testUser);
        accessToken = userApi.extractAccessToken(loginResponse);
    }

    @AfterEach
    @DisplayName("Удаление пользователя после теста")
    public void tearDownUser() {
        if (accessToken != null && !accessToken.isEmpty()) {
            userApi.deleteUser(accessToken);
            accessToken = null;
        }
    }

    @Test
    @DisplayName("Создание заказа с авторизацией и валидными ингредиентами")
    public void createOrderWithAuth() {
        OrderRequest orderRequest = new OrderRequest(VALID_INGREDIENT_IDS);
        Response orderResponse = makeOrderWithAuth(accessToken, orderRequest);
        orderResponse.then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("order.number", notNullValue());
    }

    @Test
    @DisplayName("Создание заказа без авторизации")
    public void createOrderWithoutAuth() {
        OrderRequest orderRequest = new OrderRequest(VALID_INGREDIENT_IDS);
        Response orderResponse = makeOrderWithoutAuth(orderRequest);
        orderResponse.then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("order.number", notNullValue());
    }

    @Test
    @DisplayName("Создание заказа с невалидными ингредиентами")
    public void createOrderWithInvalidIngredients() {
        OrderRequest orderRequest = new OrderRequest(INVALID_INGREDIENT_IDS);
        Response orderResponse = makeOrderWithAuth(accessToken, orderRequest);
        orderResponse.then()
                .statusCode(500);
        System.out.println(orderResponse.getBody().asString());
    }

    @Test
    @DisplayName("Создание заказа без ингредиентов")
    public void createOrderWithEmptyIngredients() {
        OrderRequest orderRequest = new OrderRequest(Collections.emptyList());

        given()
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-type", "application/json")
                .body(orderRequest)
                .when()
                .post("/api/orders")
                .then()
                .statusCode(400)
                .body("success", equalTo(false))
                .body("message", equalTo("Ingredient ids must be provided"));
    }

    @Step("Создание заказа с авторизацией (POST /api/orders)")
    public Response makeOrderWithAuth(String token, OrderRequest orderRequest) {
        return given()
                .header("Authorization", "Bearer " + token)
                .header("Content-type", "application/json")
                .body(orderRequest)
                .when()
                .post("/api/orders");
    }

    @Step("Создание заказа без авторизации (POST /api/orders)")
    public Response makeOrderWithoutAuth(OrderRequest orderRequest) {
        return given()
                .header("Content-type", "application/json")
                .body(orderRequest)
                .when()
                .post("/api/orders");
    }


    public static class OrderRequest {
        private List<String> ingredients;

        public OrderRequest(List<String> ingredients) {
            this.ingredients = ingredients;
        }

        public List<String> getIngredients() {
            return ingredients;
        }

        public void setIngredients(List<String> ingredients) {
            this.ingredients = ingredients;
        }
    }
}