package homo.demo.model;

import homo.common.model.Entity;

import java.math.BigDecimal;

/**
 * @author wujianchuan 2018/12/26
 */
public class Order extends Entity {
    private static final long serialVersionUID = 2560385391551524826L;

    private String code;
    private BigDecimal price;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
