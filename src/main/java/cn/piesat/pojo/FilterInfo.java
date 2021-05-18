package cn.piesat.pojo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "叠加分析过滤条件")
public class FilterInfo {

    @ApiModelProperty(value = "键",required = true)
    private String key;
    @ApiModelProperty(value = "值",required = true)
    private String value;
    @ApiModelProperty(value = "关系(> = < ...默认为=)")
    private String operator;
}
