package com.simplejava;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

public class HelloController implements RestController {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Response handle() {
        try {
            return new Response(200, "application/json; charset=UTF-8",
                    objectMapper.writeValueAsString(new UserDto("hello")));
        } catch (IOException e) {
            return new Response(500, "text/plain; charset=UTF-8", "Internal Server Error");
        }
    }
}
