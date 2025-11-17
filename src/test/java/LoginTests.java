import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import static org.hamcrest.Matchers.equalTo;

public class LoginTests {

    private String accessToken;
    private UserData testUser;

    private final UserApi userApi = new UserApi();

    @BeforeEach
    public void setUp() {
        testUser = UserApi.generateRandomUser();
        Response createResponse = userApi.createNewUser(testUser);
        createResponse.then()
                .statusCode(200)
                .body("success", equalTo(true));
        Response loginResponse = userApi.loginUser(testUser);
        accessToken = userApi.extractAccessToken(loginResponse);
    }

    @AfterEach
    @DisplayName("Удаление созданного пользователя после теста")
    public void tearDown() {
        if (accessToken != null && !accessToken.isEmpty()) {
            userApi.deleteUser(accessToken);
            accessToken = null;
        }
    }

    @Test
    @DisplayName("Успешный логин с корректными данными")
    public void loginSuccessfully() {

        Response loginResponse = userApi.loginUser(testUser);
        checkUserLoginSuccessfully(loginResponse, testUser);
    }

    @Test
    @DisplayName("Логин с неверным паролем — ошибка 401")
    public void loginWithInvalidPassword() {

        UserData wrongPasswordUser = new UserData(
                testUser.getEmail(),
                "incorrectPasswordValue123",
                testUser.getName()
        );

        Response response = userApi.loginUser(wrongPasswordUser);
        checkLoginFailed(response, "email or password are incorrect");
    }

    @Test
    @DisplayName("Логин с неверным email — ошибка 401")
    public void loginWithInvalidEmail() {

        UserData wrongEmailUser = new UserData(
                "totally.wrong.email@example.com",
                testUser.getPassword(),
                testUser.getName()
        );

        Response response = userApi.loginUser(wrongEmailUser);
        checkLoginFailed(response, "email or password are incorrect");
    }

    @Step("Проверить, что пользователь успешно залогинен")
    public void checkUserLoginSuccessfully(Response loginResponse, UserData userData) {

        loginResponse.then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("user.email", equalTo(userData.getEmail().toLowerCase()))
                .body("user.name", equalTo(userData.getName()));
    }

    @Step("Проверить, что логин неуспешен (status code = 401)")
    public void checkLoginFailed(Response response, String expectedMessage) {

        response.then()
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo(expectedMessage));
    }
}