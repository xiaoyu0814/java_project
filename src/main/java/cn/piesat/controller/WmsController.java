package cn.piesat.controller;

import cn.piesat.common.Result;
import cn.piesat.pojo.WMSInfo;
import cn.piesat.service.IWMSService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
//@Api(tags = "WMS服务模块")
@RequestMapping("/v1/grid/")
public class WmsController {

    @Autowired
    private IWMSService iwmsService;

    @RequestMapping(value = "/toVector",method = RequestMethod.POST)
//    @ApiOperation("栅格矢量化")
    public Result gridToVector(@RequestBody WMSInfo wmsInfo){
        Result result = new Result(-1);
        if (StringUtils.isEmpty(wmsInfo)){
            result.setMessage("参数不能为空");
            return result;
        }
        String polygon = wmsInfo.getPolygon();
        List<String> wmsLayerList = wmsInfo.getLayerList();
        if(CollectionUtils.isEmpty(wmsLayerList)||StringUtils.isEmpty(polygon)){
            result.setMessage("wms图层列表/范围/分辨率不能为空");
            return result;
        }
        return iwmsService.gridToVector(wmsInfo);
    }
}
