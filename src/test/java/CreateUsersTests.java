import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.github.javafaker.Faker;

import static org.hamcrest.Matchers.equalTo;

public class CreateUsersTests {

    private static String accessToken;
    private final UserApi userApi = new UserApi();
    private static final Faker faker = new Faker();

    @AfterAll
    @DisplayName("Удаление пользователя после тестов")
    public static void tearDown() {
        if (accessToken != null && !accessToken.isEmpty()) {
            new UserApi().deleteUser(accessToken);
            accessToken = null;
        }
    }

    @Test
    @DisplayName("Создание уникального пользователя")
    public void createUniqueUser() {
        UserData userData = generateRandomUser();
        Response response = userApi.createNewUser(userData);
        checkUserCreatedSuccessfully(response, userData);

        Response loginResponse = userApi.loginUser(userData);
        accessToken = userApi.extractAccessToken(loginResponse);
    }

    @Test
    @DisplayName("Создание пользователя, который уже зарегистрирован")
    public void createExistingUser() {
        UserData userData = generateRandomUser();
        userApi.createNewUser(userData);
        Response response = userApi.createNewUser(userData);
        checkUserAlreadyExists(response);

        Response loginResponse = userApi.loginUser(userData);
        accessToken = userApi.extractAccessToken(loginResponse);
    }

    @Test
    @DisplayName("Создание пользователя без обязательного поля (без email)")
    public void createUserWithoutEmail() {
        UserData userData = new UserData("", faker.internet().password(8, 12), faker.name().firstName());
        Response response = userApi.createNewUser(userData);
        checkUserCreationFailedMissingField(response);
    }

    @Test
    @DisplayName("Создание пользователя без обязательного поля (без пароля)")
    public void createUserWithoutPassword() {
        UserData userData = new UserData(faker.internet().emailAddress(), "", faker.name().firstName());
        Response response = userApi.createNewUser(userData);
        checkUserCreationFailedMissingField(response);
    }

    private UserData generateRandomUser() {
        String email = faker.internet().emailAddress();
        String password = faker.internet().password(8, 12);
        String name = faker.name().firstName();
        return new UserData(email, password, name);
    }

    @Step("Проверить, что пользователь успешно создан")
    public void checkUserCreatedSuccessfully(Response response, UserData userData) {
        response.then().statusCode(200)
                .body("success", equalTo(true))
                .body("user.email", equalTo(userData.getEmail().toLowerCase()))
                .body("user.name", equalTo(userData.getName()));
    }

    @Step("Проверить, что пользователь уже существует")
    public void checkUserAlreadyExists(Response response) {
        response.then().statusCode(403)
                .body("success", equalTo(false))
                .body("message", equalTo("User already exists"));
    }

    @Step("Проверить, что создание пользователя без обязательного поля не удалось")
    public void checkUserCreationFailedMissingField(Response response) {
        response.then().statusCode(403)
                .body("success", equalTo(false))
                .body("message", equalTo("Email, password and name are required fields"));
    }
}