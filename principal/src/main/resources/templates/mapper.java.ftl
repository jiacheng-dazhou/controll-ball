package ${package.Mapper};

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import ${package.Entity}.${entity}Entity;

/**
 * <p>
 * ${table.comment!} Dao 接口
 * </p>
 *
 * @author ${author}
 * @since ${date}
 */
<#if kotlin>
interface ${table.mapperName} : BaseMapper<${entity}Entity>
<#else>
public interface ${table.mapperName} extends BaseMapper<${entity}Entity> {

}
</#if>
