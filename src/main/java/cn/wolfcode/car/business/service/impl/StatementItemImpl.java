package cn.wolfcode.car.business.service.impl;

import cn.wolfcode.car.business.domain.StatementItem;
import cn.wolfcode.car.business.mapper.StatementItemMapper;
import cn.wolfcode.car.business.query.StatementItemQuery;
import cn.wolfcode.car.business.service.IStatementItemService;
import cn.wolfcode.car.common.base.page.TablePageInfo;
import cn.wolfcode.car.common.exception.BusinessException;
import cn.wolfcode.car.common.util.Convert;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class StatementItemImpl implements IStatementItemService {

    @Autowired
    private StatementItemMapper statementItemMapper;


    @Override
    public TablePageInfo<StatementItem> query(StatementItemQuery qo) {
        PageHelper.startPage(qo.getPageNum(), qo.getPageSize());
        return new TablePageInfo<StatementItem>(statementItemMapper.selectForList(qo));
    }


    @Override
    public void save(StatementItem statementItem) {
        if (statementItem == null) {
            throw  new BusinessException("参数异常");
        }

        statementItemMapper.insert(statementItem);
    }

    @Override
    public StatementItem get(Long id) {
        StatementItem statementItem = statementItemMapper.selectByPrimaryKey(id);
        if (statementItem == null || id == null ) {
            throw new BusinessException("参数异常");
        }

        return statementItem;
    }


    @Override
    public void update(StatementItem statementItem) {


        statementItemMapper.updateByPrimaryKey(statementItem);
    }

    @Override
    public void deleteBatch(String ids) {
        Long[] dictIds = Convert.toLongArray(ids);
        for (Long dictId : dictIds) {
            statementItemMapper.deleteByPrimaryKey(dictId);
        }
    }

    @Override
    public List<StatementItem> list() {
        return statementItemMapper.selectAll();
    }

    @Override
    public List<StatementItem> queryByStatementId(Long statementId) {

        return statementItemMapper.selectByStatementId(statementId);
    }

    @Override
    public void deleteByStatementId(Long id) {
           statementItemMapper.deleteByStatementId(id);
    }


}
