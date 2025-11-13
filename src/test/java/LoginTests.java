import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class LoginTests {

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
    @DisplayName("Создание уникального пользователя")
    public void createUserAndLogin() {
        UserData userData = new UserData("evgenpharaosha@gmail.com", "12345678", "Rengoku");
        Response registerResponse = createNewUser(userData);
        registerResponse.then()
                .assertThat()
                .statusCode(200)
                .body("success", equalTo(true));
        Response loginResponse = loginUser(userData);
        checkUserLoginSuccessfully(loginResponse, userData);
        accessToken = extractAccessToken(loginResponse);
    }


        @Step("Отправить POST-запрос на создание пользователя (endpoint: /api/auth/register)")
        public Response createNewUser (UserData userData){
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

    @Step("Проверить, что пользователь успешно залогинен (status code = 200, success = true)")
    public void checkUserLoginSuccessfully(Response loginResponse, UserData userData) {
        loginResponse.then()
                .assertThat()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("user.email", equalTo(userData.getEmail().toLowerCase()))
                .body("user.name", equalTo(userData.getName()));
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

    @Test
    @DisplayName("Логин с неверным логином или паролем — ошибка 401")
    public void loginWithInvalidCredentials() {
      UserData invalidUser = new UserData("", "wrongPassword", "");
      Response response = loginUser(invalidUser);
      checkLoginFailed(response, "email or password are incorrect");
    }

    @Step("Проверить, что логин неуспешен (status code = 401, success = false)")
    public void checkLoginFailed(Response response, String expectedMessage) {
        response.then()
                .assertThat()
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo(expectedMessage));
    }

}