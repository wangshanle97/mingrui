package com.baidu.shop.base;

import com.baidu.shop.utils.StringUtil;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.Data;
import lombok.NonNull;
import org.apache.commons.lang.StringUtils;

/**
 * @ClassName BaseDTO
 * @Description: TODO
 * @Author wangshanle
 * @Date 2020/8/31
 * @Version V1.0
 **/
@Data
@ApiModel(value="BaseDTO用于数据传输,其他dto需要继承他")
public class BaseDTO {



    @ApiModelProperty(value = "当前页",example = "1")
    private Integer page;

    @ApiModelProperty(value = "每页显示多少天",example = "5")
    private Integer rows;

    @ApiModelProperty(value = "排序字段")
    private String sort;

    @ApiModelProperty(value = "是否降序")
    private Boolean desc;

    //隐藏此函数,不在swagger-ui上显示
    @ApiModelProperty(hidden = true)
    public String getOrderByClause(){
        if(StringUtil.isNotEmpty(sort)) return sort + " " + (desc?"desc":"");
        return null;
    }


}
