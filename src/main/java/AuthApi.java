import io.qameta.allure.Step;
import io.restassured.response.Response;

public class AuthApi extends BaseApi {

    @Step("Создание нового пользователя (POST /api/auth/register)")
    public Response createUser(UserData userData) {
        return requestSpec
                .body(userData)
                .when()
                .post("/api/auth/register");
    }

    @Step("Логин пользователя (POST /api/auth/login)")
    public Response loginUser(UserData userData) {
        return requestSpec
                .body(userData)
                .when()
                .post("/api/auth/login");
    }
}