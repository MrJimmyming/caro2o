package cn.wolfcode.car.business.service.impl;

import cn.wolfcode.car.business.domain.Appointment;
import cn.wolfcode.car.business.domain.Statement;
import cn.wolfcode.car.business.mapper.AppointmentMapper;
import cn.wolfcode.car.business.mapper.StatementMapper;
import cn.wolfcode.car.business.query.StatementQuery;
import cn.wolfcode.car.business.service.IStatementService;
import cn.wolfcode.car.common.base.page.TablePageInfo;
import cn.wolfcode.car.common.exception.BusinessException;
import cn.wolfcode.car.common.util.Convert;
import cn.wolfcode.car.shiro.ShiroUtils;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class StatementImpl implements IStatementService {

    @Autowired
    private StatementMapper statementMapper;
    @Autowired
    private AppointmentMapper appointmentMapper;

    @Override
    public TablePageInfo<Statement> query(StatementQuery qo) {
        PageHelper.startPage(qo.getPageNum(), qo.getPageSize());
        return new TablePageInfo<Statement>(statementMapper.selectForList(qo));
    }


    @Override
    public void save(Statement statement) {
        if (statement == null) {
            throw  new BusinessException("参数异常");
        }
        statement.setStatus(Statement.STATUS_CONSUME);
        statement.setCreateTime(new Date());
        statementMapper.insert(statement);
    }

    @Override
    public Statement get(Long id) {
        Statement statement = statementMapper.selectByPrimaryKey(id);
        if (statement == null || id == null ) {
            throw new BusinessException("参数异常");
        }

        return statement;
    }


    @Override
    public void update(Statement statement) {


        statementMapper.updateByPrimaryKey(statement);
    }

    @Override
    public void deleteBatch(String ids) {
        Long[] dictIds = Convert.toLongArray(ids);
        for (Long dictId : dictIds) {
            statementMapper.deleteByPrimaryKey(dictId);
        }
    }

    @Override
    public List<Statement> list() {
        return statementMapper.selectAll();
    }

    @Override
    public void updateAmount(Long id,BigDecimal total, BigDecimal amount, BigDecimal discount, Integer status) {
        Statement statement = new Statement();
        statement.setId(id);
        statement.setTotalAmount(amount);
        statement.setTotalQuantity(total);
        statement.setDiscountAmount(discount);
        statement.setStatus(status);
        statementMapper.updateAmount( statement);
    }

    @Override
    public void changeStatue(Long id,Integer statusPaid) {
        statementMapper.changeStatue(id,statusPaid);
    }



    @Override
    public void updatePaidStatue(Long id) {
        Statement statement = new Statement();
        statement.setId(id);
        statement.setStatus(Statement.STATUS_PAID);
        statement.setPayeeId(ShiroUtils.getUserId());
        statement.setPayTime(new Date());
        statementMapper.updatePaidStatue(statement);



    }

    @Override
    public void deleteBatchSoft(String ids) {
        Long[] dictIds = Convert.toLongArray(ids);
        for (Long dictId : dictIds) {
            Statement statement = statementMapper.selectByPrimaryKey(dictId);
            Long appointmentId = statement.getAppointmentId();
            if (appointmentId !=null) {
                appointmentMapper.deleteSoft(appointmentId);
            }
            statementMapper.deleteByIdSoft(dictId);

        }

    }
}
