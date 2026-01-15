package at.ac.hcw.chatty.model;

public class ConnectionInfo {
    private static ConnectionInfo instance;
    private String host;
    private int port;
    private boolean isServer;

    private ConnectionInfo() {}

    public static ConnectionInfo getInstance() {
        if (instance == null) {
            instance = new ConnectionInfo();
        }
        return instance;
    }

    public void setConnection(String host, int port, boolean isServer) {
        this.host = host;
        this.port = port;
        this.isServer = isServer;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public boolean isServer() {
        return isServer;
    }

    public String getDisplayString() {
        return host + ":" + port + (isServer ? " (Server)" : "");
    }
}
