import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class UpgradeUserTests {

    private UserData testUser;
    private String accessToken;

    @BeforeEach
    public void setUp() {
        testUser = UserApi.generateRandomUser();
        Response createResponse = UserApi.createNewUser(testUser);
        accessToken = UserApi.extractAccessToken(createResponse);
        createResponse.then().statusCode(200)
                .body("success", equalTo(true))
                .body("user.email", equalTo(testUser.getEmail().toLowerCase()));
    }

    @AfterEach
    public void tearDown() {
        if (accessToken != null) {
            new UserApi().deleteUser(accessToken);
        }
    }

    @Test
    @DisplayName("Обновление данных авторизованного пользователя")
    public void upgradeUserDataWithAuth() {
        UserData updatedUser = new UserData(
                UserApi.generateRandomUser().getEmail(),
                testUser.getPassword(),
                "UpdatedName"
        );

        Response updateResponse = given()
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-type", "application/json")
                .body(updatedUser)
                .patch("https://stellarburgers.education-services.ru/api/auth/user");

        updateResponse.then().statusCode(200)
                .body("success", equalTo(true))
                .body("user.email", equalTo(updatedUser.getEmail().toLowerCase()))
                .body("user.name", equalTo("UpdatedName"));
    }

    @Test
    @DisplayName("Попытка обновить данные без авторизации")
    public void upgradeUserDataWithoutAuth() {

        UserData updatedUser = new UserData(
                UserApi.generateRandomUser().getEmail(),
                UserApi.generateRandomUser().getPassword(),
                "UpdatedName"
        );

        Response updateResponse = given()
                .header("Content-type", "application/json")
                .body(updatedUser)
                .patch("https://stellarburgers.education-services.ru/api/auth/user");

        updateResponse.then().statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("You should be authorised"));
    }
}