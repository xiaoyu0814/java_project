package cn.piesat.service;

import cn.piesat.common.MyException;
import cn.piesat.common.Result;
import cn.piesat.pojo.AnalysisInfo;


public interface IShpService {
    Result superpositionAnalysis(AnalysisInfo analysisInfo) throws MyException;
}
