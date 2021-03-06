package cn.wolfcode.car.business.service;

import cn.wolfcode.car.business.domain.CarPackageAudit;
import cn.wolfcode.car.business.domain.Statement;
import cn.wolfcode.car.business.query.CarPackageAuditQuery;
import cn.wolfcode.car.common.base.page.TablePageInfo;

import java.io.InputStream;
import java.util.List;

/**
 * 岗位服务接口
 */
public interface ICarPackageAuditService {

    /**
     * 分页
     * @param qo
     * @return
     */
    TablePageInfo<CarPackageAudit> query(CarPackageAuditQuery qo);


    /**
     * 查单个
     * @param id
     * @return
     */
    CarPackageAudit get(Long id);


    /**
     * 保存
     * @param carPackageAudit
     */
    void save(CarPackageAudit carPackageAudit);

  
    /**
     * 更新
     * @param carPackageAudit
     */
    void update(CarPackageAudit carPackageAudit);

    /**
     *  批量删除
     * @param ids
     */
    void deleteBatch(String ids);

    /**
     * 查询全部
     * @return
     */
    List<CarPackageAudit> list();


    InputStream queryImg(Long id);

    void updateInstanceId(CarPackageAudit carPackageAudit);


    void audit(Long id, Integer auditStatus, String info);

    void cancelApply(Long id);

}
