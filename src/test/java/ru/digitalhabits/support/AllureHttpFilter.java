package ru.digitalhabits.support;

import io.qameta.allure.Allure;
import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

import java.util.Map;

public class AllureHttpFilter implements Filter {

    @Override
    public Response filter(FilterableRequestSpecification request,
                           FilterableResponseSpecification responseSpec,
                           FilterContext ctx) {
        Allure.addAttachment("HTTP-запрос", "text/plain", describeRequest(request), "txt");
        Response response = ctx.next(request, responseSpec);
        Allure.addAttachment("HTTP-ответ", "text/plain", describeResponse(response), "txt");
        return response;
    }

    private String describeRequest(FilterableRequestSpecification request) {
        StringBuilder sb = new StringBuilder();
        sb.append(request.getMethod()).append(' ').append(request.getURI()).append('\n');
        for (Map.Entry<String, String> header : request.getHeaders().asList().stream()
                .collect(java.util.stream.Collectors.toMap(
                        h -> h.getName(), h -> String.valueOf(h.getValue()), (a, b) -> a + ", " + b))
                .entrySet()) {
            sb.append(header.getKey()).append(": ").append(header.getValue()).append('\n');
        }
        Object body = request.getBody();
        if (body != null) {
            sb.append('\n').append(body);
        }
        return sb.toString();
    }

    private String describeResponse(Response response) {
        StringBuilder sb = new StringBuilder();
        sb.append("HTTP ").append(response.statusCode())
                .append(' ').append(response.statusLine()).append('\n');
        sb.append(response.getHeaders().asList().isEmpty() ? "" : "");
        String body = response.getBody() == null ? "" : response.getBody().asString();
        sb.append('\n').append(body);
        return sb.toString();
    }
}
