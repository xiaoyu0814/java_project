package cn.piesat.controller;

import cn.piesat.common.MyException;
import cn.piesat.common.Result;
import cn.piesat.pojo.AnalysisInfo;
import cn.piesat.service.IShpService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;


@RestController
@CrossOrigin
@RequestMapping("/v1/")
@Api(tags = "空间分析")
public class ShpController {

    @Autowired
    private IShpService iShpService;

    @RequestMapping(value = "/overlay",method = RequestMethod.POST)
    @ApiOperation("叠加分析")
    public Result superpositionAnalysis(@RequestBody AnalysisInfo analysisInfo) throws MyException {
        Result result = new Result(-1);
        if (StringUtils.isEmpty(analysisInfo)){
            result.setMessage("参数不能为空");
        }else if(StringUtils.isEmpty(analysisInfo.getGeometry()) && analysisInfo.getAnalysisInfo().size()<=1){
            result.setMessage("至少两个图层才可以进行叠加分析");
        }else {
            result = iShpService.superpositionAnalysis(analysisInfo);
        }
        return result;
    }

    @RequestMapping(value = "/test",method = RequestMethod.GET)
    @ApiOperation("测试")
    public Result test() {
        Result result = new Result(0);
        List<Object> objects = new ArrayList<>();
        List<Object> obj = new ArrayList<>();
        obj.add("啥也没有，气不气");
        objects.add(obj);
        result.setMessage("查询成功");
        result.setData(objects);
        return result;
    }
}
