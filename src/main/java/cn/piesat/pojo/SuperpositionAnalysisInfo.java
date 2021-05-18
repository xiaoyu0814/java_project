package cn.piesat.pojo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(value = "叠加分析实体信息")
public class SuperpositionAnalysisInfo {

    @ApiModelProperty(value = "服务地址",required = true)
    private String serverUrl;
    @ApiModelProperty(value = "过滤条件",required = false)
    private List<FilterInfo> filter;
}
