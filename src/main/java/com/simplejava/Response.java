package com.simplejava;

public record Response(int statusCode, String contentType, String body) {
}
