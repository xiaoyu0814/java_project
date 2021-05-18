package cn.piesat.pojo;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(value = "自定义叠加分析实体信息")
public class AnalysisInfo {
    @ApiModelProperty(value = "自定义的面",name = "geometry",required = true)
    private JSONObject geometry;

    @ApiModelProperty(value = "叠加分析实体信息",name = "analysisInfo",required = true)
    private List<SuperpositionAnalysisInfo> analysisInfo;
}
