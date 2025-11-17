import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class OrderGetTests {

    private String accessToken;
    private UserData testUser;
    private final UserApi userApi = new UserApi();

    @BeforeEach
    @DisplayName("Создание нового пользователя перед тестом")
    public void setUpUser() {
        testUser = new UserData("evgenpharaosha@gmail.com", "12345678", "Rengoku");

        Response registerResponse = UserApi.createNewUser(testUser);
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
    @DisplayName("Получение заказов авторизованного пользователя")
    public void getOrdersWithAuth() {
        Response ordersResponse = userApi.getOrdersWithAuth(accessToken);
        ordersResponse.then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("orders", notNullValue());
    }

    @Test
    @DisplayName("Получение заказов неавторизованного пользователя")
    public void getOrdersWithoutAuth() {
        Response ordersResponse = userApi.getOrdersWithoutAuth();
        ordersResponse.then()
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("You should be authorised"));
    }
}