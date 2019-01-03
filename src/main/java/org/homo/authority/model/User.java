package org.homo.authority.model;

import org.homo.core.annotation.HomoColumn;
import org.homo.core.annotation.HomoEntity;
import org.homo.core.model.BaseEntity;

/**
 * @author wujianchuan 2018/12/27
 */
@HomoEntity(table = "USER")
public class User extends BaseEntity {
    private static final long serialVersionUID = -8894255862274388288L;

    @HomoColumn(name = "AVATAR")
    private String avatar;
    @HomoColumn(name = "NAME")
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
