package cn.wolfcode.car.business.query;

import cn.wolfcode.car.common.base.query.QueryObject;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * 流程列表查询对象
 */
@Setter
@Getter
public class BpmnInfoQuery extends QueryObject {
    @DateTimeFormat(pattern="yyyy-MM-dd")
  private Date beginTime;
    @DateTimeFormat(pattern="yyyy-MM-dd")
  private Date endTime;
}
