package com.poe.cache.dao;

import java.util.List;
import java.util.Optional;

/**
 * 通用 CRUD 仓库接口，为所有 DAO 定义统一的数据访问契约。
 *
 * @param <T>  实体类型
 * @param <ID> 主键类型
 */
public interface CrudRepository<T, ID> {

    /** 插入单条记录 */
    void insert(T entity);

    /**
     * 批量插入，在单个事务内执行。任一行失败则整体回滚。
     */
    void batchInsert(List<T> entities);

    /** 按主键查询，不存在时返回 {@link Optional#empty()} */
    Optional<T> findById(ID id);

    /** 返回表中所有记录 */
    List<T> findAll();

    /** 按主键删除 */
    void deleteById(ID id);

    /** 返回表中记录总数 */
    int count();
}
