package homo.authority.model;

import homo.common.model.BaseEntity;

/**
 * @author wujianchuan 2018/12/27
 */
public class User extends BaseEntity {
    private static final long serialVersionUID = -8894255862274388288L;

    private String avatar;
    private String name;

    private User() {
    }

    public static User newInstance(String avatar, String name) {
        User user = new User();
        user.setAvatar(avatar);
        user.setName(name);
        return user;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
