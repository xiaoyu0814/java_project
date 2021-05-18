package cn.piesat.service;

import cn.piesat.common.Result;
import cn.piesat.pojo.WMSInfo;

public interface IWMSService {

    Result gridToVector(WMSInfo wmsInfo);
}
