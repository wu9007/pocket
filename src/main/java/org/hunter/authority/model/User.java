package org.hunter.authority.model;

import org.hunter.pocket.annotation.Column;
import org.hunter.pocket.annotation.Entity;
import org.hunter.pocket.model.BaseEntity;

/**
 * @author wujianchuan 2018/12/27
 */
@Entity(table = "TBL_USER")
public class User extends BaseEntity {
    private static final long serialVersionUID = -8894255862274388288L;

    @Column(name = "AVATAR")
    private String avatar;
    @Column(name = "NAME")
    private String name;

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
