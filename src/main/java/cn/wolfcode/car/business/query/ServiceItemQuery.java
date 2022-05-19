package cn.wolfcode.car.business.query;

import cn.wolfcode.car.common.base.query.QueryObject;
import lombok.Getter;
import lombok.Setter;

/**
 * 岗位查询对象
 */
@Setter
@Getter
public class ServiceItemQuery extends QueryObject {
    private String name;
    private Boolean carPackage;
    private Integer serviceCatalog;
    private Integer auditStatus ;
    private Integer saleStatus ;
}
