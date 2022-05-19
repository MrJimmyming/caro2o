package cn.wolfcode.car.business.query;

import cn.wolfcode.car.common.base.query.QueryObject;
import lombok.Getter;
import lombok.Setter;

/**
 * 明细单查询对象
 */
@Setter
@Getter
public class StatementItemQuery extends QueryObject {
    private Long statementId ;

}
