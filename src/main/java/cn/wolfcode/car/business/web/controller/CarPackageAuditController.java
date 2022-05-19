package cn.wolfcode.car.business.web.controller;


import cn.wolfcode.car.business.domain.CarPackageAudit;
import cn.wolfcode.car.business.domain.Statement;
import cn.wolfcode.car.business.query.CarPackageAuditQuery;
import cn.wolfcode.car.business.service.ICarPackageAuditService;
import cn.wolfcode.car.common.base.page.TablePageInfo;
import cn.wolfcode.car.common.web.AjaxResult;
import cn.wolfcode.car.shiro.ShiroUtils;
import org.apache.poi.util.IOUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

/**
 * 岗位控制器
 */
@Controller
@RequestMapping("business/carPackageAudit")
public class CarPackageAuditController {
    //模板前缀
    private static final String prefix = "business/carPackageAudit/";

    @Autowired
    private ICarPackageAuditService carPackageAuditService;

    //页面------------------------------------------------------------
    //列表
    @RequiresPermissions("business:carPackageAudit:view")
    @RequestMapping("/listPage")
    public String listPage(){

        return prefix + "list";
    }

    @RequiresPermissions("business:carPackageAudit:add")
    @RequestMapping("/addPage")
    public String addPage(){
        return prefix + "add";
    }


    @RequiresPermissions("business:carPackageAudit:edit")
    @RequestMapping("/editPage")
    public String editPage(Long id, Model model){
        model.addAttribute("carPackageAudit", carPackageAuditService.get(id));
        return prefix + "edit";
    }


    @RequiresPermissions("business:processImg:list")
    @RequestMapping("/processImg")
    public void processImg(Long id, HttpServletResponse response) throws IOException {
      InputStream inputStream = carPackageAuditService.queryImg( id);
        IOUtils.copy(inputStream,response.getOutputStream());
    }

    //@RequiresPermissions("business:carPackageAudit:todoPage")
    @RequestMapping("/todoPage")
    public String todoPage(){
        return prefix + "todoPage";
    }
    //@RequiresPermissions("business:carPackageAudit:auditPage")
    @RequestMapping("/auditPage")
    public String auditPage( Long id, Model model){
        model.addAttribute("id",id);
        return prefix + "auditPage";
    }


    //@RequiresPermissions("business:carPackageAudit:donePage")
    @RequestMapping("/donePage")
    public String donePage( ){

        return prefix + "donePage";
    }
    //数据-----------------------------------------------------------
    //列表
    @RequiresPermissions("business:carPackageAudit:list")
    @RequestMapping("/query")
    @ResponseBody
    public TablePageInfo<CarPackageAudit> query(CarPackageAuditQuery qo){
        return carPackageAuditService.query(qo);
    }


    //@RequiresPermissions("business:carPackageAudit:todoQuery")
    @RequestMapping("/todoQuery")
    @ResponseBody
    public TablePageInfo<CarPackageAudit> todoQuery(CarPackageAuditQuery qo){
        qo.setAuditId(ShiroUtils.getUserId());
        qo.setStatus(CarPackageAudit.STATUS_IN_ROGRESS);
        return carPackageAuditService.query(qo);
    }
    //处理审核同意或者拒绝
    //@RequiresPermissions("business:carPackageAudit:audit")
    @RequestMapping("/audit")
    @ResponseBody
    public AjaxResult audit(Long id,Integer auditStatus,String info){
        carPackageAuditService.audit(id,auditStatus,info);
        return AjaxResult.success();
    }



    //处理撤销
    //@RequiresPermissions("business:carPackageAudit:cancelApply")
    @RequestMapping("/cancelApply")
    @ResponseBody
    public AjaxResult audit(Long id){
        carPackageAuditService.cancelApply(id);
        return AjaxResult.success();
    }

    //处理撤销
    //@RequiresPermissions("business:carPackageAudit:doneQuery")
    @RequestMapping("/doneQuery")
    @ResponseBody
    public TablePageInfo<CarPackageAudit> doneQuery(CarPackageAuditQuery qo){
        qo.setUserName(ShiroUtils.getUser().getUserName());
        return carPackageAuditService.query(qo);
    }


    //新增
    @RequiresPermissions("business:carPackageAudit:add")
    @RequestMapping("/add")
    @ResponseBody
    public AjaxResult addSave(CarPackageAudit carPackageAudit){
        carPackageAuditService.save(carPackageAudit);
        return AjaxResult.success();
    }

    //编辑
    @RequiresPermissions("business:carPackageAudit:edit")
    @RequestMapping("/edit")
    @ResponseBody
    public AjaxResult edit(CarPackageAudit carPackageAudit){
        carPackageAuditService.update(carPackageAudit);
        return AjaxResult.success();
    }

    //删除
    @RequiresPermissions("business:carPackageAudit:remove")
    @RequestMapping("/remove")
    @ResponseBody
    public AjaxResult remove(String ids){


        return AjaxResult.success();
    }



}
