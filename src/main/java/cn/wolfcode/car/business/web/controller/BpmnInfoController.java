package cn.wolfcode.car.business.web.controller;


import cn.wolfcode.car.business.domain.BpmnInfo;
import cn.wolfcode.car.business.query.BpmnInfoQuery;
import cn.wolfcode.car.business.service.IBpmnInfoService;
import cn.wolfcode.car.common.base.page.TablePageInfo;
import cn.wolfcode.car.common.config.SystemConfig;
import cn.wolfcode.car.common.exception.BusinessException;
import cn.wolfcode.car.common.util.file.FileUploadUtils;
import cn.wolfcode.car.common.web.AjaxResult;
import lombok.Getter;
import org.apache.poi.util.IOUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 岗位控制器
 */
@Controller
@RequestMapping("business/bpmnInfo")
public class BpmnInfoController {
    //模板前缀
    private static final String prefix = "business/bpmnInfo/";

    @Autowired
    private IBpmnInfoService bpmnInfoService;

    //页面------------------------------------------------------------
    //列表
    @RequiresPermissions("business:bpmnInfo:view")
    @RequestMapping("/listPage")
    public String listPage(){
        return prefix + "list";
    }
        /*返回新增页面

         */
    @RequiresPermissions("business:bpmnInfo:deployPage")
    @RequestMapping("/deployPage")
    public String addPage(){
        return prefix + "add";
    }



    @RequiresPermissions("business:bpmnInfo:edit")
    @RequestMapping ("/editPage")
    public String editPage(Long id, Model model){
        model.addAttribute("bpmnInfo", bpmnInfoService.get(id));
        return prefix + "edit";
    }

    //数据-----------------------------------------------------------
    //列表
    @RequiresPermissions("business:bpmnInfo:list")
    @RequestMapping("/query")
    @ResponseBody
    public TablePageInfo<BpmnInfo> query(BpmnInfoQuery qo){
        return bpmnInfoService.query(qo);
    }
    @RequiresPermissions("business:bpmnInfo:upload")
    @RequestMapping("/upload")
    @ResponseBody
    public AjaxResult upload(MultipartFile file) throws IOException {
        if (file == null || file.getSize() <= 0) {
            throw new BusinessException("文件上传错误");
        }
        String ext = FileUploadUtils.getExtension(file);
        if (!("zip".equals(ext.toLowerCase()) ||"bpmn".equals(ext.toLowerCase()) )) {
            throw  new BusinessException("只能上传zip或者bpmn格式文件");
        }

        String data = FileUploadUtils.upload(SystemConfig.getUploadPath(), file);

        return AjaxResult.success("文件上传成功",data);
    }





    //新增部署
    @RequiresPermissions("business:bpmnInfo:deploy")
    @RequestMapping("/deploy")
    @ResponseBody
    public AjaxResult deploy(BpmnInfo bpmnInfo) throws Exception{
        bpmnInfoService.deploy(bpmnInfo);
        return AjaxResult.success();
    }

    //编辑
    @RequiresPermissions("business:bpmnInfo:edit")
    @RequestMapping("/edit")
    @ResponseBody
    public AjaxResult edit(BpmnInfo bpmnInfo){
        bpmnInfoService.updateInfo(bpmnInfo);
        return AjaxResult.success();
    }

    //删除
    @RequiresPermissions("business:bpmnInfo:remove")
    @RequestMapping("/remove")
    @ResponseBody
    public AjaxResult remove(String ids){
        bpmnInfoService.deleteBatch(ids);
        return AjaxResult.success();
    }

    //编辑
    @RequiresPermissions("business:bpmnInfo:delete")
    @RequestMapping("/delete")
    @ResponseBody
    public AjaxResult delete(Long id){
        bpmnInfoService.delete(id);
        return AjaxResult.success();
    }
    //编辑
    @RequiresPermissions("business:bpmnInfo:readResource")
    @RequestMapping("/readResource")
    public void readResource(String deploymentId, String type,HttpServletResponse response) throws IOException {
       InputStream inputStream= bpmnInfoService.readResource(deploymentId,type);
        if ("xml".equalsIgnoreCase(type)) {
            response.setHeader("Content-Disposition","attachment;filename=data.xml");
        }
        IOUtils.copy(inputStream,response.getOutputStream());
    }


}
