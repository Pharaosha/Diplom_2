import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.util.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class OrderGetTests {

    private static String accessToken;

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
    @DisplayName("Получение заказов конкретного авторизованного пользователя:")
    public void getOrderWithAuth() {
        UserData userData = new UserData("evgenpharaosha@gmail.com", "12345678", "Rengoku");

        Response registerResponse = createNewUser(userData);
        registerResponse.then().statusCode(200).body("success", equalTo(true));

        Response loginResponse = loginUser(userData);
        accessToken = extractAccessToken(loginResponse);

        Response ordersResponse = getOrdersWithAuth(accessToken);
        ordersResponse.then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("orders", notNullValue());

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

    @Step("Получение заказов с авторизацией (GET /api/orders)")
    public Response getOrdersWithAuth(String accessToken) {
        return given()
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-type", "application/json")
                .when()
                .get("/api/orders");
    }

    @Test
    @DisplayName("Получение заказов неавторизованного пользователя:")
    public void getOrderWithoutAuth() {
        Response ordersResponse = getOrdersWithoutAuth();
        ordersResponse.then()
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("You should be authorised"));


    }

    @Step("Получение заказов без авторизации (GET /api/orders)")
    public Response getOrdersWithoutAuth() {
        return given()
                .header("Content-type", "application/json")
                .when()
                .get("/api/orders");
    }

}