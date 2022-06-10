package irpc.framework.core.common.config;

public class ClientConfig {

    private Integer port;

    private String serverAddr;

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getServerAddr() {
        return serverAddr;
    }

    public void setServerAddr(String serverAddr) {
        this.serverAddr = serverAddr;
    }

    @Override
    public String toString() {
        return "ClientConfig{" +
                "port=" + port +
                ", serverAddr='" + serverAddr + '\'' +
                '}';
    }
}
