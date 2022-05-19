package cn.wolfcode.car.business.query;

import cn.wolfcode.car.common.base.query.QueryObject;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * 岗位查询对象
 */
@Setter
@Getter
public class CarPackageAuditQuery extends QueryObject {
    @DateTimeFormat(pattern="yyyy-MM-dd")
    private Date beginTime;
    @DateTimeFormat(pattern="yyyy-MM-dd")
    private Date endTime;
    private Long auditId;
    private  String userName;
    private Integer status;
}
