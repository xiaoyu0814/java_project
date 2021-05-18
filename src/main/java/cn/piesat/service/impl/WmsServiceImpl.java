package cn.piesat.service.impl;

import cn.piesat.common.Result;
import cn.piesat.pojo.WMSInfo;
import cn.piesat.service.IWMSService;
import org.springframework.stereotype.Service;

@Service
public class WmsServiceImpl implements IWMSService {

    @Override
    public Result gridToVector(WMSInfo wmsInfo) {
        Result result = new Result();
        String polygon = wmsInfo.getPolygon();
        String[] split = polygon.split(";");
        return result;
    }
}
