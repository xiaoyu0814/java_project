package cn.piesat.utils;

import cn.piesat.common.Constants;
import cn.piesat.common.MyException;
import cn.piesat.pojo.FilterInfo;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.vividsolutions.jts.geom.Geometry;
import org.geotools.geojson.geom.GeometryJSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class ServerUtil {

    Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ClientConfiguration clientConfiguration;

    private String param = "f=json&returnGeometry=true";

    @Autowired
    private GeoUtil geoUtil;

    /**
     * 拼接查询参数
     * @param serverUrl
     * @return
     */
    private String paramAppend(String serverUrl,List<FilterInfo> filter){
        String where = "";
        if (CollectionUtils.isEmpty(filter)){
            return serverUrl +"/query?where=1=1&"+param;
        }
        serverUrl += "/query?where=";
        for (FilterInfo filterInfo : filter) {
            String key = filterInfo.getKey();
            String value = filterInfo.getValue();
            String operator = filterInfo.getOperator();
            if (StringUtils.isEmpty(key) || StringUtils.isEmpty(value)) {
                continue;
            }
            where = where + "(" + key + (StringUtils.isEmpty(operator) ? "=" : operator) + value + ")" + "AND";
        }
        where = where.substring(0,where.lastIndexOf("AND"));
        serverUrl += where +"&"+param;
        return serverUrl.replace(" ","%20");
    }

    /**
     * 根据查询条件获取Geometry对象集合
     * @param serverUrl
     * @param filter
     * @return
     * @throws MyException
     */
    public List<Geometry> getGeometryByLayer(String serverUrl, List<FilterInfo> filter) throws MyException {
        // 1. 获取待处理数据geometry数组
        String url = paramAppend(serverUrl, filter);
        log.info("条件拼接url结果："+url);
        JSONObject result = clientConfiguration.sendGet(url, null);
        List<Geometry> geometryList = new ArrayList<>();
        if (!StringUtils.isEmpty(result)){
            JSONObject error = result.getJSONObject("error");
            if (!StringUtils.isEmpty(error)){
                log.error(error.getString("message"));
                throw new MyException("读取图层"+serverUrl+"的面数据失败");
            }else {
                // 2. 获取对象解析转换标准格式
                String geometryType = result.getString(Constants.geometryTypeKey);
                JSONArray featureArray = result.getJSONArray("features");
                // 2.1 转换ArcGIS对象
                log.info("查询到："+featureArray.size()+"个数据");
                if (featureArray.size()>0){
                    switch (geometryType){
                        case Constants.point:
                            for (int i=0;i<featureArray.size();i++){
                                JSONObject geometry = featureArray.getJSONObject(i).getJSONObject("geometry");
                                try {
                                    if (!StringUtils.isEmpty(geometry)&&geometry.size()!=0){
                                        GeometryJSON geometryJSON = new GeometryJSON();
                                        Geometry read = geometryJSON.read(geoUtil.getPointGeoJson(geometry).toJSONString());
                                        read = read.buffer(0.0000001);
                                        geometryList.add(read);
                                    }
                                } catch (IOException e) {
                                    throw new MyException("读取图层"+serverUrl+"的数据失败");
                                }
                            }
                            break;
                        case Constants.polygn:
                            for (int i=0;i<featureArray.size();i++){
                                JSONArray jsonArray = featureArray.getJSONObject(i).getJSONObject("geometry").getJSONArray("rings");
                                try {
                                    if (!CollectionUtils.isEmpty(jsonArray)){
                                        GeometryJSON geometryJSON = new GeometryJSON();
                                        Geometry read = geometryJSON.read(geoUtil.getPolygonGeoJson(jsonArray).toJSONString());
                                        read = read.buffer(0.0000001);
                                        geometryList.add(read);
                                    }
                                } catch (IOException e) {
                                    throw new MyException("读取图层"+serverUrl+"的数据失败");
                                }
                            }
                            break;
                        case Constants.line:
                            for (int i=0;i<featureArray.size();i++){
                                JSONArray jsonArray = featureArray.getJSONObject(i).getJSONObject("geometry").getJSONArray("paths");
                                try {
                                    if (!CollectionUtils.isEmpty(jsonArray)){
                                        GeometryJSON geometryJSON = new GeometryJSON();
                                        Geometry read = geometryJSON.read(geoUtil.getLineGeoJson(jsonArray).toJSONString());
                                        read = read.buffer(0.0000001);
                                        geometryList.add(read);
                                    }
                                } catch (IOException e) {
                                    throw new MyException("读取图层"+serverUrl+"的数据失败");
                                }
                            }
                            break;
                        default:
                            throw new MyException("暂支持点和面的叠加分析");
                    }
                }else {
                    throw new MyException("查询图层 "+serverUrl+" 数据为空");
                }
            }
        }
        return geometryList;
    }


    public String getServerName(String url){
        return url.replaceAll("(?:http://120.52.31.158/arcgis/rest/services/EDATA/|/MapServer/0)", "");
    }


    public static void main(String[] args) throws IOException {
        JSONObject getGeoJson = new JSONObject();
        getGeoJson.put("type","Polygon");
        String s = "[[[108.828, 41.5555],[109.236, 41.4071],[109.141, 41.0998],[108.735, 41.1706],[108.828, 41.5555]]]";
        getGeoJson.put("coordinates",JSONArray.parseArray(s));
        GeometryJSON geometryJSON = new GeometryJSON();
        Geometry read = geometryJSON.read(getGeoJson.toJSONString());
        StringWriter writer = new StringWriter();
        geometryJSON.write(read, writer);
        JSONObject jsonObject = JSONObject.parseObject(writer.toString());
        System.out.println(jsonObject);
    }

}
