package com.example.hiddenboss;

// A JavaBean class for comments made by users on a tournament. JSON data from requesting comments from the server is unpacked into this.
public class Comment {
    private String user;
    private String message;
    private String id;

    public Comment(String user, String message, String id){
        this.user = user;
        this.message = message;
        this.id = id;
    }

    // Simple getters for retrieving the class's variables

    public String getId() {
        return id;
    }

    public String getUser() {
        return user;
    }

    public String getMessage() {
        return message;
    }

}