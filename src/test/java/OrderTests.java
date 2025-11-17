import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.util.*;

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
    private final OrderApi orderApi = new OrderApi();

    @BeforeEach
    @DisplayName("Создание нового пользователя перед тестом")
    public void setUpUser() {
        testUser = UserApi.generateRandomUser();

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
        Response orderResponse = orderApi.createOrderWithAuth(accessToken, orderRequest);
        orderResponse.then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("order.number", notNullValue());
    }

    @Test
    @DisplayName("Создание заказа без авторизации")
    public void createOrderWithoutAuth() {
        OrderRequest orderRequest = new OrderRequest(VALID_INGREDIENT_IDS);
        Response orderResponse = orderApi.createOrderWithoutAuth(orderRequest);
        orderResponse.then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("order.number", notNullValue());
    }

    @Test
    @DisplayName("Создание заказа с невалидными ингредиентами")
    public void createOrderWithInvalidIngredients() {
        OrderRequest orderRequest = new OrderRequest(INVALID_INGREDIENT_IDS);
        Response orderResponse = orderApi.createOrderWithAuth(accessToken, orderRequest);
        orderResponse.then()
                .statusCode(500);
        System.out.println(orderResponse.getBody().asString());
    }

    @Test
    @DisplayName("Создание заказа без ингредиентов")
    public void createOrderWithEmptyIngredients() {
        OrderRequest orderRequest = new OrderRequest(Collections.emptyList());
        Response response = orderApi.createOrderEmpty(accessToken, orderRequest);

        response.then()
                .statusCode(400)
                .body("success", equalTo(false))
                .body("message", equalTo("Ingredient ids must be provided"));
    }
}