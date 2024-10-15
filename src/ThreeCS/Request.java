package ThreeCS;

import java.io.Serializable;

public class Request implements Serializable {
    private String action;
    private String name;
    private String address;
    private String phone;

    public Request(String action, String name, String address, String phone) {
        this.action = action;
        this.name = name;
        this.address = address;
        this.phone = phone;
    }

    public String getAction() {
        return action;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getPhone() {
        return phone;
    }
}