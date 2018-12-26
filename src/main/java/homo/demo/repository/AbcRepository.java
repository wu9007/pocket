package homo.demo.repository;

/**
 * @author wujianchuan 2018/12/26
 */
public interface AbcRepository {

    /**
     * 保存
     *
     * @return 影响行数
     */
    int save();

    /**
     * 更新
     *
     * @return 影响行数
     */
    int update();

    /**
     * 删除
     *
     * @return 影响行数
     */
    int delete();
}
