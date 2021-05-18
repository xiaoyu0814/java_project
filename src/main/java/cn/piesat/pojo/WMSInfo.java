package cn.piesat.pojo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(value = "wms图层信息类")
public class WMSInfo {

    @ApiModelProperty(value = "wms图层列表",required = true)
    private List<String> layerList;
    @ApiModelProperty(value = "范围(lon,lat;lon,lat....)",required = true)
    private String polygon;
    @ApiModelProperty(value = "分辨率",required = true)
    private Double resolution;
}
