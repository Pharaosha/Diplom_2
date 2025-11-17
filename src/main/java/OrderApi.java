import io.qameta.allure.Step;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class OrderApi {

    @Step("Создание заказа с авторизацией (POST /api/orders)")
    public Response createOrderWithAuth(String token, OrderRequest orderRequest) {
        return given()
                .header("Authorization", "Bearer " + token)
                .header("Content-type", "application/json")
                .body(orderRequest)
                .when()
                .post("/api/orders");
    }

    @Step("Создание заказа без авторизации (POST /api/orders)")
    public Response createOrderWithoutAuth(OrderRequest orderRequest) {
        return given()
                .header("Content-type", "application/json")
                .body(orderRequest)
                .when()
                .post("/api/orders");
    }

    @Step("Создание заказа без ингредиентов (POST /api/orders)")
    public Response createOrderEmpty(String token, OrderRequest orderRequest) {
        return given()
                .header("Authorization", "Bearer " + token)
                .header("Content-type", "application/json")
                .body(orderRequest)
                .when()
                .post("/api/orders");
    }
}