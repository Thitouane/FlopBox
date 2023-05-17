package com.helle.flopbox;

public class Server {
    private String name;
    private String address;
    private int port;
    private String user;
    private String password;

    public Server(String name, String address) {
        this.name = name;
        this.port = 21;
        this.address = address;
        this.user = "anonymous";
        this.password = "anonymous";
    }

    public Server(String name, String user, int port, String address, String password) {
        this.name = name;
        this.port = port;
        this.address = address;
        this.user = user;
        this.password = password;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public String getAddress() {
        return this.address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getPort() {
        return this.port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUser() {
        return this.user;
    }

    public String getPass() {
        return this.password;
    }
}
