package cn.wolfcode.car.business.service.impl;

import cn.wolfcode.car.business.domain.Appointment;
import cn.wolfcode.car.business.domain.ServiceItem;
import cn.wolfcode.car.business.domain.Statement;
import cn.wolfcode.car.business.mapper.AppointmentMapper;
import cn.wolfcode.car.business.mapper.ServiceItemMapper;
import cn.wolfcode.car.business.mapper.StatementMapper;
import cn.wolfcode.car.business.query.AppointmentQuery;
import cn.wolfcode.car.business.service.IAppointmentService;
import cn.wolfcode.car.common.base.page.TablePageInfo;
import cn.wolfcode.car.common.exception.BusinessException;
import cn.wolfcode.car.common.util.Convert;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Transactional
public class AppointmentImpl implements IAppointmentService {

    @Autowired
    private AppointmentMapper appointmentMapper;
    @Autowired
    private StatementMapper statementMapper;

    @Override
    public TablePageInfo<Appointment> query(AppointmentQuery qo) {
        PageHelper.startPage(qo.getPageNum(), qo.getPageSize());
        return new TablePageInfo<Appointment>(appointmentMapper.selectForList(qo));
    }


    @Override
    public void save(Appointment appointment) {
        if (appointment == null) {
            throw  new  BusinessException("参数异常");
        }
        appointment.setStatus(Appointment.STATUS_APPOINTMENT);
        appointment.setCreateTime(new Date());
        appointmentMapper.insert(appointment);
    }

    @Override
    public Appointment get(Long id) {
        Appointment appointment = appointmentMapper.selectByPrimaryKey(id);
        if (appointment == null || id == null ) {
            throw new BusinessException("参数异常");
        }

        return appointment;
    }


    @Override
    public void update(Appointment appointment) {
        Appointment obj = appointmentMapper.selectByPrimaryKey(appointment.getId());
        if (obj == null || !Appointment.STATUS_APPOINTMENT.equals(obj.getStatus())) {
            throw  new BusinessException("只有在预约中状态的预约才能修改");
        }

        appointmentMapper.updateByPrimaryKey(appointment);
    }

    @Override
    public void deleteBatch(String ids) {
        Long[] dictIds = Convert.toLongArray(ids);
        for (Long dictId : dictIds) {
            appointmentMapper.deleteByPrimaryKey(dictId);
        }
    }

    @Override
    public List<Appointment> list() {
        return appointmentMapper.selectAll();
    }

    @Override
    public void arrival(Long id) {
        Appointment obj = appointmentMapper.selectByPrimaryKey(id);
        if (obj == null || !Appointment.STATUS_APPOINTMENT.equals(obj.getStatus())) {
            throw  new BusinessException("只有在预约中状态才能确认到店");
        }
        appointmentMapper.updateArrivalTime(new Date(),id);
        appointmentMapper.changeStatues(id,Appointment.STATUS_ARRIVAL);

    }

    @Override
    public void cancel(Long id) {
        Appointment obj = appointmentMapper.selectByPrimaryKey(id);
        if (obj == null || !Appointment.STATUS_APPOINTMENT.equals(obj.getStatus())) {
            throw  new BusinessException("只有在预约中状态才能取消");
        }
        appointmentMapper.changeStatues(id,Appointment.STATUS_CANCEL);

    }

    @Override
    public void deleteBatchSoft(String ids) {
        Long[] dictIds = Convert.toLongArray(ids);
        for (Long dictId : dictIds) {
            appointmentMapper.deleteSoft(dictId);
        }
    }

    @Override
    public Statement generateStatement(Long id) {
        Appointment appointment = appointmentMapper.selectByPrimaryKey(id);
        if (appointment==null) {
            throw new BusinessException("参数异常");
        }
        Statement statement = null;
        if (Appointment.STATUS_ARRIVAL.equals(appointment.getStatus())) {
             statement = new Statement();
            statement.setCustomerName(appointment.getCustomerName());
            statement.setCustomerPhone(appointment.getCustomerPhone().toString());
            statement.setActualArrivalTime(appointment.getActualArrivalTime());
            statement.setLicensePlate(appointment.getLicensePlate());
            statement.setCarSeries(appointment.getCarSeries());
            statement.setInfo(appointment.getInfo());
            statement.setStatus(Statement.STATUS_CONSUME);
            statement.setAppointmentId(appointment.getId());
            statement.setServiceType(appointment.getServiceType()+0l);
            statement.setCreateTime(new Date());
            statementMapper.insert(statement);
            appointmentMapper.changeStatues(appointment.getId(),Appointment.STATUS_SETTLE);
        }else {
             statement = statementMapper.queryByAppointmentID(id);
        }
        return statement;
    }
}
