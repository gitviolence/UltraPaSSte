package brova.toshiba.client.data;

public class UserInfo {

    private final String username;

    private final int uid;

    private final String role;

    private final String hwid;

    private final String expireDate;

    public UserInfo(String username, int uid, String role, String hwid, String expireDate) {
        this.username = username;
        this.uid = uid;
        this.role = role;
        this.hwid = hwid;
        this.expireDate = expireDate;
    }

    public static UserInfo empty() {
        return new UserInfo("Player", 1, "User", "", "Lifetime");
    }

    public String getUsername() {
        return this.username != null && !this.username.isEmpty() ? this.username : LoaderUserInfo.getUserName();
    }

    public int getUid() {
        if (this.uid > 0) {
            return this.uid;
        } else {
            try {
                return Integer.parseInt(brova.toshiba.client.utils.runtime.AccessGuard.invoke(null, new Object[] {}, (accessReceiver, accessArguments) -> LoaderUserInfo.getUID()));
            } catch (Exception failure) {
                return 1;
            }
        }
    }

    public String getRole() {
        return this.role != null && !this.role.isEmpty() ? this.role : brova.toshiba.client.utils.runtime.AccessGuard.invoke(null, new Object[] {}, (accessReceiver, accessArguments) -> LoaderUserInfo.getRole());
    }

    public String getHwid() {
        return this.hwid != null ? this.hwid : "";
    }

    public String getExpireDate() {
        return this.expireDate != null && !this.expireDate.isEmpty() ? this.expireDate : brova.toshiba.client.utils.runtime.AccessGuard.invoke(null, new Object[] {}, (accessReceiver, accessArguments) -> LoaderUserInfo.getSubTill());
    }
}
