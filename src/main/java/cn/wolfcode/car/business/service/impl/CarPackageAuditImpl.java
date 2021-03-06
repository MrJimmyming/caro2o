package cn.wolfcode.car.business.service.impl;

import cn.wolfcode.car.business.domain.BpmnInfo;
import cn.wolfcode.car.business.domain.CarPackageAudit;
import cn.wolfcode.car.business.domain.ServiceItem;
import cn.wolfcode.car.business.domain.Statement;
import cn.wolfcode.car.business.mapper.CarPackageAuditMapper;
import cn.wolfcode.car.business.mapper.StatementMapper;
import cn.wolfcode.car.business.query.CarPackageAuditQuery;
import cn.wolfcode.car.business.service.IBpmnInfoService;
import cn.wolfcode.car.business.service.ICarPackageAuditService;
import cn.wolfcode.car.business.service.IServiceItemService;
import cn.wolfcode.car.common.base.page.TablePageInfo;
import cn.wolfcode.car.common.exception.BusinessException;
import cn.wolfcode.car.common.util.Convert;
import cn.wolfcode.car.shiro.ShiroUtils;
import com.github.pagehelper.PageHelper;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.task.Task;
import org.activiti.image.ProcessDiagramGenerator;
import org.activiti.image.impl.DefaultProcessDiagramGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class CarPackageAuditImpl implements ICarPackageAuditService {

    @Autowired
    private CarPackageAuditMapper carPackageAuditMapper;
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private IBpmnInfoService bpmnInfoService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private RuntimeService runtimeService;

    @Override
    public TablePageInfo<CarPackageAudit> query(CarPackageAuditQuery qo) {
        PageHelper.startPage(qo.getPageNum(), qo.getPageSize());
        List<CarPackageAudit> carPackageAudits = carPackageAuditMapper.selectForList(qo);
        return new TablePageInfo<CarPackageAudit>(carPackageAuditMapper.selectForList(qo));
    }


    @Override
    public void save(CarPackageAudit carPackageAudit) {

        carPackageAuditMapper.insert(carPackageAudit);
    }

    @Override
    public CarPackageAudit get(Long id) {
        CarPackageAudit carPackageAudit = carPackageAuditMapper.selectByPrimaryKey(id);
        if (carPackageAudit == null || id == null ) {
            throw new BusinessException("????????????");
        }

        return carPackageAudit;
    }


    @Override
    public void update(CarPackageAudit carPackageAudit) {
        CarPackageAudit obj = carPackageAuditMapper.selectByPrimaryKey(carPackageAudit.getId());


        carPackageAuditMapper.updateByPrimaryKey(carPackageAudit);
    }

    @Override
    public void deleteBatch(String ids) {
        Long[] dictIds = Convert.toLongArray(ids);
        for (Long dictId : dictIds) {
            carPackageAuditMapper.deleteByPrimaryKey(dictId);
        }
    }

    @Override
    public List<CarPackageAudit> list() {
        return carPackageAuditMapper.selectAll();
    }

    @Override
    public InputStream queryImg(Long id) {
        CarPackageAudit carPackage = carPackageAuditMapper.selectByPrimaryKey(id);
        InputStream inputStream = null;
        BpmnInfo bpmnInfo = bpmnInfoService.get(carPackage.getBpmnInfoId());

        //?????????????????????????????????????????????
        BpmnModel model = repositoryService.getBpmnModel(bpmnInfo.getActProcessId());
        ProcessDiagramGenerator generator = new DefaultProcessDiagramGenerator();
        if (CarPackageAudit.STATUS_IN_ROGRESS.equals(carPackage.getStatus())) {
            Task task = taskService.createTaskQuery()

                    .processInstanceId(carPackage.getInstanceId()).singleResult();
            List<String> activeActivityIds =
                    runtimeService.getActiveActivityIds(task.getExecutionId());

            inputStream = generator.generateDiagram(model, activeActivityIds,
                    Collections.EMPTY_LIST,
                    "??????","??????","??????");
        }else {
            inputStream = generator.generateDiagram(model, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
                    "??????","??????","??????");
        }

        return inputStream;
    }

    @Override
    public void updateInstanceId(CarPackageAudit carPackageAudit) {
        carPackageAuditMapper.updateInstanceByPrimaryKey(carPackageAudit);
    }

    @Override
    public void audit(Long id, Integer auditStatus, String info) {
        CarPackageAudit audit = carPackageAuditMapper.selectByPrimaryKey(id);
        if (audit ==null || !CarPackageAudit.STATUS_IN_ROGRESS.equals(audit.getStatus())) {
            throw new BusinessException("??????????????????????????????");
        }
        String userName = ShiroUtils.getUser().getUserName();
        //?????????2 ?????????
        String temp = audit.getAuditRecord()+"</br>";
        if(auditStatus == 2){
            temp += "???"+userName+"?????????"+info;
        }else{
            temp += "???"+userName+"?????????"+info;
        }
        audit.setAuditRecord(temp);
        audit.setAuditTime(new Date());
        Task task = taskService.createTaskQuery().processInstanceId(audit.getInstanceId()).singleResult();
        taskService.setVariable(task.getId(),"auditStatus",auditStatus);
        taskService.addComment(task.getId(),audit.getInstanceId(),temp);
        taskService.complete(task.getId());

        //??????
        //?????????????????????
        Task next = taskService.createTaskQuery().processInstanceId(audit.getInstanceId()).singleResult();
        //????????????
        if (CarPackageAudit.STATUS_PASS.equals(auditStatus)){
            if (next!=null) {
                audit.setAuditorId(Long.parseLong(next.getAssignee()));
            }else{
                //?????????????????????????????????
                serviceItemService.changeStatues(audit.getServiceItemId(),ServiceItem.AUDITSTATUS_APPROVED);
                audit.setStatus(CarPackageAudit.STATUS_PASS);
            }
        }else {
            serviceItemService.changeStatues(audit.getServiceItemId(),ServiceItem.AUDITSTATUS_INIT);
            audit.setStatus(CarPackageAudit.STATUS_REJECT);
        }
        carPackageAuditMapper.updateByPrimaryKey(audit);


    }
    @Autowired
    IServiceItemService serviceItemService;

    @Override
    public void cancelApply(Long id) {
        CarPackageAudit audit = carPackageAuditMapper.selectByPrimaryKey(id);
        if (audit==null || !CarPackageAudit.STATUS_IN_ROGRESS.equals(audit.getStatus())) {
            throw new BusinessException("??????????????????????????????");
        }
        audit.setStatus(CarPackageAudit.STATUS_CANCEL);
        carPackageAuditMapper.changeStatus(id,CarPackageAudit.STATUS_CANCEL);
        serviceItemService.changeStatues(audit.getServiceItemId(), ServiceItem.AUDITSTATUS_INIT);
        //????????????
        runtimeService.deleteProcessInstance(audit.getInstanceId(),"???????????????");

    }
}
