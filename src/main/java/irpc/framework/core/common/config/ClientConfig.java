package irpc.framework.core.common.config;

public class ClientConfig {

    private String applicationName;

    private String registerAddr;

    private String proxyType;

    public String getProxyType() {
        return proxyType;
    }

    public void setProxyType(String proxyType) {
        this.proxyType = proxyType;
    }

    public String getRegisterAddr() {
        return registerAddr;
    }

    @Override
    public String toString() {
        return "ClientConfig{" +
                "applicationName='" + applicationName + '\'' +
                ", registerAddr='" + registerAddr + '\'' +
                ", proxyType='" + proxyType + '\'' +
                '}';
    }

    public void setRegisterAddr(String registerAddr) {
        this.registerAddr = registerAddr;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }
}
