package cn.wolfcode.car.business.service.impl;

import cn.wolfcode.car.base.service.IUserService;
import cn.wolfcode.car.base.service.impl.UserServiceImpl;
import cn.wolfcode.car.business.domain.BpmnInfo;
import cn.wolfcode.car.business.domain.CarPackageAudit;
import cn.wolfcode.car.business.domain.ServiceItem;
import cn.wolfcode.car.business.domain.StatementItem;
import cn.wolfcode.car.business.mapper.CarPackageAuditMapper;
import cn.wolfcode.car.business.mapper.ServiceItemMapper;
import cn.wolfcode.car.business.query.ServiceItemQuery;
import cn.wolfcode.car.business.service.IBpmnInfoService;
import cn.wolfcode.car.business.service.ICarPackageAuditService;
import cn.wolfcode.car.business.service.IServiceItemService;
import cn.wolfcode.car.common.base.page.TablePageInfo;
import cn.wolfcode.car.common.exception.BusinessException;
import cn.wolfcode.car.common.util.Convert;
import cn.wolfcode.car.shiro.ShiroUtils;
import com.github.pagehelper.PageHelper;
import jdk.jshell.execution.Util;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.PortUnreachableException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Service
@Transactional
public class ServiceItemServiceImpl implements IServiceItemService {

    @Autowired
    private ServiceItemMapper serviceItemMapper;


    @Override
    public TablePageInfo<ServiceItem> query(ServiceItemQuery qo) {
        PageHelper.startPage(qo.getPageNum(), qo.getPageSize());
        return new TablePageInfo<ServiceItem>(serviceItemMapper.selectForList(qo));
    }


    @Override
    public void save(ServiceItem serviceItem) {
        if (serviceItem == null || serviceItem.getCarPackage() == null) {
            throw new BusinessException("参数错误");
        }
        //设置创建时间
        serviceItem.setCreateTime(new Date());
        //判断是否套餐,赋值上架状态和审核状态
        serviceItem.setAuditStatus(serviceItem.getCarPackage()?
                ServiceItem.AUDITSTATUS_INIT
                :ServiceItem.AUDITSTATUS_NO_REQUIRED);

        serviceItem.setSaleStatus(ServiceItem.SALESTATUS_OFF);
        serviceItemMapper.insert(serviceItem);
    }

    @Override
    public ServiceItem get(Long id) {
        ServiceItem serviceItem = serviceItemMapper.selectByPrimaryKey(id);
        if (serviceItem == null || id == null ) {
            throw new BusinessException("参数异常");
        }

        return serviceItem;
    }


    @Override
    public void update(ServiceItem serviceItem) {
        ServiceItem temp = serviceItemMapper.selectByPrimaryKey(serviceItem.getId());
        if (temp == null  ) {
            throw new BusinessException("参数异常");
        }
        //已上架服务 和审核中服务不能更改
        if (ServiceItem.SALESTATUS_ON.equals(temp.getSaleStatus())) {
            throw  new BusinessException("产品已上架无法修改信息");
        }
        if (ServiceItem.AUDITSTATUS_AUDITING.equals(temp.getAuditStatus())) {
            throw  new BusinessException("审核中商品中无法修改");
        }
        serviceItemMapper.updateByPrimaryKey(serviceItem);
    }

    @Override
    public void deleteBatch(String ids) {
        Long[] dictIds = Convert.toLongArray(ids);
        for (Long dictId : dictIds) {
            serviceItemMapper.deleteByPrimaryKey(dictId);
        }
    }

    @Override
    public List<ServiceItem> list() {
        return serviceItemMapper.selectAll();
    }

    @Override
    public void saleOn(Long id) {
        ServiceItem serviceItem = serviceItemMapper.selectByPrimaryKey(id);
        if (serviceItem == null || ServiceItem.SALESTATUS_ON.equals(serviceItem.getSaleStatus()) ) {
            throw new BusinessException("参数异常");
        }

        if (serviceItem.getCarPackage() && !ServiceItem.AUDITSTATUS_AUDITING.equals(serviceItem.getAuditStatus())) {
            throw  new BusinessException("未审核通过的套餐无法上架");
        }

        serviceItemMapper.updateSaleStatus(id,ServiceItem.SALESTATUS_ON);
    }

    @Override
    public void saleOff(Long id) {
        ServiceItem serviceItem = serviceItemMapper.selectByPrimaryKey(id);
        if (serviceItem == null || ServiceItem.SALESTATUS_OFF.equals(serviceItem.getSaleStatus()) ) {
            throw new BusinessException("参数异常");
        }
        serviceItemMapper.updateSaleStatus(id,ServiceItem.SALESTATUS_OFF);
    }

    @Override
    public TablePageInfo<ServiceItem> selectAllSaleOnList(ServiceItemQuery qo) {
                qo.setSaleStatus(ServiceItem.SALESTATUS_ON);
        return new TablePageInfo<ServiceItem>(serviceItemMapper.selectForList(qo));
    }
    @Autowired
    private ICarPackageAuditService carPackageAuditService;
    @Autowired
    private  RuntimeService runtimeService;
    @Autowired
    private IBpmnInfoService bpmnInfoService;
    @Autowired
    private IUserService userService;
    @Override
    public void startAudit(Long id, Long bpmnInfoId, Long director, Long finance, String info) {
        ServiceItem serviceItem = serviceItemMapper.selectByPrimaryKey(id);
        if (serviceItem == null || !serviceItem.getCarPackage() ||!ServiceItem.AUDITSTATUS_INIT.equals(serviceItem.getAuditStatus())) {
            throw new BusinessException("只有初始化的套餐才能审核");
        }
        CarPackageAudit carPackageAudit = new CarPackageAudit();
        //serviceIem
        carPackageAudit.setServiceItemId(serviceItem.getId());
        carPackageAudit.setServiceItemInfo(serviceItem.getInfo());
        carPackageAudit.setServiceItemPrice(serviceItem.getDiscountPrice());
        //creatMessage
        carPackageAudit.setAuditorId(director);
        carPackageAudit.setCreateTime(new Date());
        carPackageAudit.setCreator(ShiroUtils.getLoginName());
        carPackageAudit.setInfo(info);
        //statue and BpmnID
        carPackageAudit.setBpmnInfoId(bpmnInfoId);
        carPackageAudit.setStatus(CarPackageAudit.STATUS_IN_ROGRESS);
        carPackageAuditService.save(carPackageAudit);
        //启动流程
        HashMap<String, Object> map = new HashMap<>();
        if(director != null){
            map.put("director", director.toString());
        }
        if(finance != null){
            map.put("finance", finance.toString());
        }
        map.put("discountPrice", serviceItem.getDiscountPrice().longValue());
        BpmnInfo bpmnInfo = bpmnInfoService.get(bpmnInfoId);

        ProcessInstance instance = runtimeService.startProcessInstanceById(bpmnInfo.getActProcessId(),
                carPackageAudit.getId().toString(),
                map);
        carPackageAudit.setInstanceId(instance.getId());
        carPackageAuditService.updateInstanceId(carPackageAudit);
        serviceItemMapper.changeStatues(id,ServiceItem.AUDITSTATUS_AUDITING);
    }

    @Override
    public void changeStatues(Long serviceItemId, Integer statues) {
        serviceItemMapper.changeStatues(serviceItemId,statues);
    }
}
