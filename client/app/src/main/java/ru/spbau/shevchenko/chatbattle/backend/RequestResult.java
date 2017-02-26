package ru.spbau.shevchenko.chatbattle.backend;

public class RequestResult {
    private String response;
    private Status status;

    public enum Status {OK, FAILED_CONNECTION, ERROR}

    public RequestResult() {
        response = "";
        status = Status.OK;
    }

    public RequestResult(String response) {
        this.response = response;
        status = Status.OK;
    }

    public RequestResult(String response, Status status) {
        this.response = response;
        this.status = status;
    }

    public String getResponse() {
        return response;
    }

    public Status getStatus() {
        return status;
    }
}
