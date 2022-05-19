package cn.wolfcode.car.business.service.impl;

import cn.wolfcode.car.business.domain.BpmnInfo;
import cn.wolfcode.car.business.mapper.BpmnInfoMapper;
import cn.wolfcode.car.business.query.BpmnInfoQuery;
import cn.wolfcode.car.business.service.IBpmnInfoService;
import cn.wolfcode.car.common.base.page.TablePageInfo;
import cn.wolfcode.car.common.config.SystemConfig;
import cn.wolfcode.car.common.exception.BusinessException;
import cn.wolfcode.car.common.util.Convert;
import com.github.pagehelper.PageHelper;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.image.ProcessDiagramGenerator;
import org.activiti.image.impl.DefaultProcessDiagramGenerator;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipInputStream;

@Service
@Transactional
public class BpmnInfoServiceImpl implements IBpmnInfoService {

    @Autowired
    private BpmnInfoMapper bpmnInfoMapper;


    @Override
    public TablePageInfo<BpmnInfo> query(BpmnInfoQuery qo) {
        PageHelper.startPage(qo.getPageNum(), qo.getPageSize());
        return new TablePageInfo<BpmnInfo>(bpmnInfoMapper.selectForList(qo));
    }


    @Override
    public void save(BpmnInfo bpmnInfo) {

        bpmnInfoMapper.insert(bpmnInfo);
    }

    @Override
    public BpmnInfo get(Long id) {
        BpmnInfo bpmnInfo = bpmnInfoMapper.selectByPrimaryKey(id);
        if (bpmnInfo == null || id == null ) {
            throw new BusinessException("参数异常");
        }

        return bpmnInfo;
    }


    @Override
    public void update(BpmnInfo bpmnInfo) {

        bpmnInfoMapper.updateByPrimaryKey(bpmnInfo);
    }

    @Override
    public void deleteBatch(String ids) {
        Long[] dictIds = Convert.toLongArray(ids);
        for (Long dictId : dictIds) {
            bpmnInfoMapper.deleteByPrimaryKey(dictId);
        }
    }

    @Override
    public List<BpmnInfo> list() {
        return bpmnInfoMapper.selectAll();
    }

    @Autowired
    private RepositoryService repositoryService;

    @Override
    public void deploy(BpmnInfo bpmnInfo) throws FileNotFoundException {
        String extension = FilenameUtils.getExtension(bpmnInfo.getBpmnPath());
        DeploymentBuilder builder = repositoryService.createDeployment();
        File file = new File(SystemConfig.getProfile(),bpmnInfo.getBpmnPath());
        Deployment deploy = null;
       if ("zip".equals(extension.toLowerCase())){
            deploy = builder.addZipInputStream(new ZipInputStream(new FileInputStream(file))).deploy();
       }else if("bpmn".equals(extension.toLowerCase())){
            deploy = builder.addInputStream(bpmnInfo.getBpmnPath(), new FileInputStream(file)).deploy();
       }else {
           throw  new BusinessException("文件流程部署失败");
       }

        bpmnInfo.setDeploymentId(deploy.getId());
        bpmnInfo.setDeployTime(deploy.getDeploymentTime());
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery().deploymentId(deploy.getId()).singleResult();
        bpmnInfo.setActProcessId(definition.getId());
        bpmnInfo.setActProcessKey(definition.getKey());
        bpmnInfo.setBpmnName(definition.getName());
        bpmnInfoMapper.insert(bpmnInfo);
    }

    @Override
    public void updateInfo(BpmnInfo bpmnInfo) {
        bpmnInfoMapper.updateInfo(bpmnInfo);
    }
    @Autowired
    RuntimeService runtimeService;
    @Override
    public void delete(Long id) {
        /*
        1.删除部署关联的流程实例
        2.删除deploy
        3.删除bpmnInfo表格内容
         */
        BpmnInfo bpmnInfo = bpmnInfoMapper.selectByPrimaryKey(id);
        if (bpmnInfo == null) {
            throw new BusinessException("删除失败");
        }
        List<ProcessInstance> list = runtimeService.createProcessInstanceQuery().
                processInstanceId(bpmnInfo.getActProcessId()).list();
        for (ProcessInstance instance : list) {
           // 1.删除部署关联的流程实例

        }

        repositoryService.deleteDeployment(bpmnInfo.getDeploymentId(),true);


        bpmnInfoMapper.deleteByPrimaryKey(id);
    }

    @Override
    public InputStream readResource(String deploymentId, String type) {
        InputStream inputStream = null;
        ProcessDefinition definition = repositoryService.
                createProcessDefinitionQuery().
                    deploymentId(deploymentId).latestVersion().singleResult();
        if ("xml".equals(type.toLowerCase())) {
            inputStream = repositoryService.getResourceAsStream(deploymentId, definition.getResourceName());
        }else  if("png".equals(type.toLowerCase())){
            BpmnModel model = repositoryService.getBpmnModel(definition.getId());
            ProcessDiagramGenerator generator = new DefaultProcessDiagramGenerator();
            //generateDiagram(流程模型,需要高亮的节点,需要高亮的线条,后面三个参数都表示是字体)
             inputStream = generator.generateDiagram(model, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
                    "宋体","宋体","宋体");

        }else {
            throw  new BusinessException("参数错误");
        }
        return inputStream;
    }

    @Override
    public BpmnInfo queryByType(String type) {
        return bpmnInfoMapper.selectByType(type);

    }
}
