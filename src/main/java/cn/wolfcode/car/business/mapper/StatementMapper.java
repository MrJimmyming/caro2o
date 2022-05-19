package cn.wolfcode.car.business.mapper;

import cn.wolfcode.car.business.domain.Statement;
import cn.wolfcode.car.business.query.StatementQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface StatementMapper {
    int deleteByPrimaryKey(Long id);

    int insert(Statement record);

    Statement selectByPrimaryKey(Long id);

    List<Statement> selectAll();

    int updateByPrimaryKey(Statement record);

    List<Statement> selectForList(StatementQuery qo);

    void updateAmount(Statement statement);

    void changeStatue(@Param("id") Long id, @Param("status") Integer statusPaid);

    void updatePaidStatue( Statement statement);

    Statement queryByAppointmentID(Long id);

    void deleteByIdSoft(Long dictId);

}