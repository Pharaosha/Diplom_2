import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;


public class UpgradeUserTests {

    private static String accessToken;
    private static UserData userData;

    @BeforeAll
    public static void setUp() {
        RestAssured.baseURI = "https://stellarburgers.education-services.ru/";
        userData = new UserData("evgenpharaosha@gmail.com", "12345678", "Rengoku");

        Response registerResponse = createNewUser(userData);
        registerResponse.then()
                .assertThat()
                .statusCode(200)
                .body("success", equalTo(true));

        System.out.println("Пользователь успешно создан:");
        registerResponse.prettyPrint();
        Response loginResponse = loginUser(userData);
        accessToken = extractAccessToken(loginResponse);
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
    @DisplayName("Изменение данных пользователя с авторизацией")
    public void updateUserDataWithAuth() {
        UserData updatedData = new UserData("updated_" + userData.getEmail(),"87654321","NewRengoku");

        Response response = updateUserWithAuth(accessToken, updatedData);
        checkUserUpdatedSuccessfully(response, updatedData);
        System.out.println("Пользователь успешно изменен:");
        response.prettyPrint();
    }

    @Step("Создать пользователя (POST /api/auth/register)")
    public static Response createNewUser(UserData userData) {
        return given()
                .header("Content-type", "application/json")
                .body(userData)
                .when()
                .post("/api/auth/register");
    }

    @Step("Авторизоваться пользователем (POST /api/auth/login)")
    public static Response loginUser(UserData userData) {
        return given()
                .header("Content-type", "application/json")
                .body(userData)
                .when()
                .post("/api/auth/login");
    }

    @Step("Изменить данные пользователя с авторизацией (PATCH /api/auth/user)")
    public Response updateUserWithAuth(String token, UserData updatedData) {
        return given()
                .header("Authorization", "Bearer " + token)
                .header("Content-type", "application/json")
                .body(updatedData)
                .when()
                .patch("/api/auth/user");
    }

    @Step("Проверить, что данные пользователя успешно изменены (status code = 200, success = true)")
    public void checkUserUpdatedSuccessfully(Response response, UserData updatedData) {
        response.then()
                .assertThat()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("user.name", equalTo(updatedData.getName()))
                .body("user.email", equalTo(updatedData.getEmail().toLowerCase()));
    }

    @Step("Извлечь accessToken из ответа")
    public static String extractAccessToken(Response response) {
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
    @DisplayName("Попытка изменения данных пользователя без авторизации")
    public void updateUserDataWithoutAuth() {
        UserData updatedData = new UserData("unauth_" + userData.getEmail(),"99999999", "Ghost");
        Response response = updateUserWithoutAuth(updatedData);
        checkUpdateFailedWithoutAuth(response);
    }

    @Step("Попробовать изменить данные пользователя без авторизации (PATCH /api/auth/user)")
    public Response updateUserWithoutAuth(UserData updatedData) {
        return given()
                .header("Content-type", "application/json")
                .body(updatedData)
                .when()
                .patch("/api/auth/user");
    }

    @Step("Проверить, что изменение данных без авторизации возвращает ошибку 401")
    public void checkUpdateFailedWithoutAuth(Response response) {
        response.then()
                .assertThat()
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("You should be authorised"));

        System.out.println("You should be authorised");
        response.prettyPrint();

    }
}