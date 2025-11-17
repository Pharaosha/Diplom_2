import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.github.javafaker.Faker;

import static org.hamcrest.Matchers.equalTo;

public class LoginTests {

    private static String accessToken;
    private static final UserApi userApi = new UserApi();
    private static final Faker faker = new Faker();

    @AfterAll
    @DisplayName("Удаление пользователя после тестов")
    public static void tearDown() {
        if (accessToken != null && !accessToken.isEmpty()) {
            userApi.deleteUser(accessToken);
            accessToken = null;
        }
    }

    @Test
    @DisplayName("Создание уникального пользователя и логин")
    public void createUserAndLogin() {

        String email = faker.internet().emailAddress();
        UserData userData = new UserData(email, "12345678", "Rengoku");

        Response createResponse = userApi.createNewUser(userData);
        createResponse.then()
                .statusCode(200)
                .body("success", equalTo(true));

        Response loginResponse = userApi.loginUser(userData);
        checkUserLoginSuccessfully(loginResponse, userData);

        accessToken = userApi.extractAccessToken(loginResponse);
    }

    @Test
    @DisplayName("Логин с неверным паролем — ошибка 401")
    public void loginWithInvalidPassword() {

        UserData userWithWrongPassword = new UserData("correct@example.com", "wrongPassword", "");
        Response response = userApi.loginUser(userWithWrongPassword);
        checkLoginFailed(response, "email or password are incorrect");
    }

    @Test
    @DisplayName("Логин с неверным email — ошибка 401")
    public void loginWithInvalidEmail() {

        UserData userWithWrongEmail = new UserData("wrong@example.com", "correctPassword", "");
        Response response = userApi.loginUser(userWithWrongEmail);
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