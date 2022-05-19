package cn.wolfcode.car.business.web.controller;



import cn.wolfcode.car.base.service.IUserService;
import cn.wolfcode.car.business.domain.ServiceItem;
import cn.wolfcode.car.business.domain.Statement;
import cn.wolfcode.car.business.domain.StatementItem;
import cn.wolfcode.car.business.mapper.StatementMapper;
import cn.wolfcode.car.business.query.ServiceItemQuery;
import cn.wolfcode.car.business.query.StatementItemQuery;
import cn.wolfcode.car.business.service.IAppointmentService;
import cn.wolfcode.car.business.service.IServiceItemService;
import cn.wolfcode.car.business.service.IStatementItemService;
import cn.wolfcode.car.business.service.IStatementService;
import cn.wolfcode.car.common.base.page.TablePageInfo;
import cn.wolfcode.car.common.exception.BusinessException;
import cn.wolfcode.car.common.web.AjaxResult;
import cn.wolfcode.car.shiro.ShiroUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.util.List;

/**
 * 结算列表控制器
 */
@Controller
@RequestMapping("business/statementItem")
public class StatementItemController {
    //模板前缀
    private static final String prefix = "business/statementItem/";

    @Autowired
    private IStatementItemService statementItemService;
    @Autowired
    private IStatementService statementService;
    @Autowired
    private IUserService userService;
    @Autowired
    private IAppointmentService appointmentService;


    //页面------------------------------------------------------------
    //列表
    @RequiresPermissions("business:statementItem:view")
    @RequestMapping("/listPage")
    public String listPage(Long statementId,Model model){
        Statement statement = statementService.get(statementId);
        model.addAttribute("statement",statement);

        if (Statement.STATUS_CONSUME.equals(statement.getStatus())) {

            return prefix + "editDetail";
        } else {
            statement.setPayee(userService.get(statement.getPayeeId()));
            return prefix + "showDetail";
        }
       // return prefix + "editDetail";

    }

    @RequiresPermissions("business:statementItem:add")
    @RequestMapping("/addPage")
    public String addPage(){

        return prefix + "add";
    }


    @RequiresPermissions("business:statementItem:edit")
    @RequestMapping("/editPage")
    public String editPage(Long id, Model model){
        model.addAttribute("statementItem", statementItemService.get(id));
        return prefix + "edit";
    }

    //数据-----------------------------------------------------------
    //列表
    @RequiresPermissions("business:statementItem:list")
    @RequestMapping("/query")
    @ResponseBody
    public TablePageInfo<StatementItem> query( StatementItemQuery qo){

        return statementItemService.query(qo);

    }
    //新增单项保养
    @RequiresPermissions("business:statementItem:saveItems")
    @RequestMapping("/saveItems")
    @ResponseBody
    public AjaxResult saveItems(@RequestBody List<StatementItem> items){
        if (items == null) {
            throw new BusinessException("保存失败");
        }
        BigDecimal total = new BigDecimal("0");
        BigDecimal amount = new BigDecimal("0.00");

        StatementItem discount = items.remove(items.size() - 1);
        statementItemService.deleteByStatementId(discount.getStatementId());
        if (items.size()>0) {
            for (StatementItem item : items) {
                statementItemService.save(item);
                    total = total.add(new BigDecimal(item.getItemQuantity()));
                 amount =amount.add(item.getItemPrice().multiply(new BigDecimal(item.getItemQuantity()))) ;
            }


        }
            statementService.updateAmount(
                    discount.getStatementId(),total,amount,amount,Statement.STATUS_CONSUME);



        return AjaxResult.success();

    }

    //付款
    @RequiresPermissions("business:statementItem:payStatement")
    @RequestMapping("/payStatement")
    @ResponseBody
    public AjaxResult payStatement(Long statementId){
        Statement statement = statementService.get(statementId);
        if (statement == null || Statement.STATUS_PAID.equals(statement.getStatus())) {
            throw new BusinessException("已付款过,请勿重复支付");
        }
        statementService.updatePaidStatue(statement.getId());

        return AjaxResult.success();
    }




    //新增
    @RequiresPermissions("business:statementItem:add")
    @RequestMapping("/add")
    @ResponseBody
    public AjaxResult addSave(StatementItem statementItem){
        statementItemService.save(statementItem);
        return AjaxResult.success();
    }

    //编辑
    @RequiresPermissions("business:statementItem:edit")
    @RequestMapping("/edit")
    @ResponseBody
    public AjaxResult edit(StatementItem statementItem){
        statementItemService.update(statementItem);
        return AjaxResult.success();
    }

    //删除
    @RequiresPermissions("business:statementItem:remove")
    @RequestMapping("/remove")
    @ResponseBody
    public AjaxResult remove(String ids){

        statementItemService.deleteBatch(ids);
        return AjaxResult.success();
    }




}
