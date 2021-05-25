package cn.piesat.controller;

import cn.piesat.common.MyException;
import cn.piesat.common.Result;
import cn.piesat.pojo.AnalysisInfo;
import cn.piesat.service.IShpService;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


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
    // 简单方法
    // public Result test(@RequestParam String a, String b) {
    //     System.out.println(a);
    //     System.out.println(b);
    // };
    // 参数为json字符串用此方法解析
    public Result test(@RequestParam Map<String, Object> map) {

        Result result = new Result(0);

        JSONObject resultJson=new JSONObject();

        resultJson.put("name", "张三");
        resultJson.put("age", 22);

        List<Object> objects = new ArrayList<>();
        if( "1".equals(map.get("a").toString()) && "2".equals(map.get("b").toString())){
            objects.add(resultJson);
            result.setMessage("查询成功");
        }else{
            objects.add(null);
            result.setMessage("查询失败");
        }

        result.setData(objects);
        return result;
    }
    @RequestMapping(value = "/testPost",method = RequestMethod.POST)
    @ApiOperation("测试Post")
    public Result testPost(@RequestParam Map<String, Object> map) {
        Result result = new Result(0);
        result.setData(map);
        result.setMessage("查询成功");
        return result;
    }
}
