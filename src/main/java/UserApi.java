import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import com.github.javafaker.Faker;

import static io.restassured.RestAssured.given;

public class UserApi extends BaseApi {

    private static final String BASE_URI = "https://stellarburgers.education-services.ru/";
    private static Faker faker;
    

    static {
        RestAssured.baseURI = BASE_URI;
        faker = new Faker();
    }

    @Step("Создать нового пользователя")
    public static Response createNewUser(UserData userData) {
        return given()
                .header("Content-type", "application/json")
                .body(userData)
                .post("/api/auth/register");
    }

    @Step("Создать рандомные юзерские параметры")
    public static UserData generateRandomUser() {
        String email = faker.internet().emailAddress();
        String password = faker.internet().password(8, 12);
        String name = faker.name().firstName();
        return new UserData(email, password, name);
    }

    @Step("Логин пользователя")
    public static Response loginUser(UserData userData) {
        return given()
                .header("Content-type", "application/json")
                .body(userData)
                .post("/api/auth/login");
    }

    @Step("Удалить пользователя по accessToken")
    public void deleteUser(String accessToken) {
        Response response = given()
                .header("Authorization", "Bearer " + accessToken)
                .delete("/api/auth/user");

        response.then().statusCode(202);
    }

    @Step("Извлечь accessToken из ответа")
    public static String extractAccessToken(Response response) {
        String token = response.then().extract().path("accessToken");
        return token != null ? token.replace("Bearer ", "") : null;
    }

    @Step("Получение заказов с авторизацией (GET /api/orders)")
    public Response getOrdersWithAuth(String accessToken) {
        return given()
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-type", "application/json")
                .when()
                .get("/api/orders");
    }

    @Step("Получение заказов без авторизации (GET /api/orders)")
    public Response getOrdersWithoutAuth() {
        return given()
                .header("Content-type", "application/json")
                .when()
                .get("/api/orders");
    }
}