package com.simplejava;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

public class HelloWorldController implements RestController {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Response handle() {
        try {
            return new Response(200, "application/json; charset=UTF-8",
                    objectMapper.writeValueAsString(new UserDto("Hello, World!")));
        } catch (IOException e) {
            return new Response(500, "application/json; charset=UTF-8", "{\"message\":\"Internal Server Error\"}");
        }
    }
}