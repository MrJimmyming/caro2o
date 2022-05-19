package cn.wolfcode.car.business.query;

import cn.wolfcode.car.common.base.query.QueryObject;
import lombok.Getter;
import lombok.Setter;

/**
 * 岗位查询对象
 */
@Setter
@Getter
public class AppointmentQuery extends QueryObject {
    private Integer status ;
    private String customerName;
    private Long customerPhone;
}
