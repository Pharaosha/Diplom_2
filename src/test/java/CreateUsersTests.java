import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class CreateUsersTests {

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
    public void createUniqueUser() {

        UserData userData = new UserData("evgenpharaosha@gmail.com", "12345678", "Rengoku");
        Response response = createNewUser(userData);
        checkUserCreatedSuccessfully(response, userData);

        accessToken = loginAndGetAccessToken(userData);
    }

    @Test
    @DisplayName("Создание пользователя, который уже зарегистрирован")
    public void createExistingUser() {

        UserData userData = new UserData("evgenpharaosha@gmail.com", "12345678", "Rengoku");
        createNewUser(userData);
        Response response = createNewUser(userData);
        checkUserAlreadyExists(response);
        accessToken = loginAndGetAccessToken(userData);
    }

    @Test
    @DisplayName("Создание пользователя без обязательного поля (без пароля)")
    public void createUserWithoutPassword() {

        UserData userData = new UserData("evgenpharaosha@gmail.com", "", "Rengoku");
        Response response = createNewUser(userData);
        checkUserCreationFailedMissingField(response);
    }



    @Step("Отправить POST-запрос на создание пользователя (endpoint: /api/auth/register)")
    public Response createNewUser(UserData userData) {
        return given()
                .header("Content-type", "application/json")
                .body(userData)
                .when()
                .post("/api/auth/register");
    }

    @Step("Проверить, что пользователь успешно создан (status code = 200, success = true)")
    public void checkUserCreatedSuccessfully(Response response, UserData userData) {
        response.then()
                .assertThat()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("user.email", equalTo(userData.getEmail().toLowerCase()))
                .body("user.name", equalTo(userData.getName()));
    }

    @Step("Проверить, что пользователь уже существует (status code = 403, success = false)")
    public void checkUserAlreadyExists(Response response) {
        response.then()
                .assertThat()
                .statusCode(403)
                .body("success", equalTo(false))
                .body("message", equalTo("User already exists"));
    }

    @Step("Проверить, что создание пользователя без обязательного поля не удалось (status code = 403, success = false)")
    public void checkUserCreationFailedMissingField(Response response) {
        response.then()
                .assertThat()
                .statusCode(403)
                .body("success", equalTo(false))
                .body("message", equalTo("Email, password and name are required fields"));
    }

    @Step("Авторизоваться и получить accessToken (endpoint: /api/auth/login)")
    public static String loginAndGetAccessToken(UserData userData) {
        Response response = given()
                .header("Content-type", "application/json")
                .body(userData)
                .when()
                .post("/api/auth/login");

        response.then()
                .assertThat()
                .statusCode(200)
                .body("success", equalTo(true));

        String token = response.then().extract().path("accessToken");
        return token.replace("Bearer ", "");
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



}