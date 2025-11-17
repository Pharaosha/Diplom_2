import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import io.qameta.allure.Step;

public class BaseApi {

    protected RequestSpecification requestSpec;

    public BaseApi() {

        requestSpec = RestAssured.given()
                .baseUri("https://stellarburgers.education-services.ru/")
                .header("Content-Type", "application/json");
    }

    @Step("Удалить пользователя по accessToken (endpoint: /api/auth/user)")
    public void deleteUser(String accessToken) {
        RestAssured.given(requestSpec)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .delete("/api/auth/user")
                .then()
                .assertThat()
                .statusCode(202);
    }
}