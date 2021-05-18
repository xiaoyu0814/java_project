package cn.piesat.utils;

import cn.piesat.common.MyException;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.operation.polygonize.Polygonizer;
import org.geotools.geojson.geom.GeometryJSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

@Component
public class GeoUtil {

    private Logger log = LoggerFactory.getLogger(this.getClass());


    /**
     * 获取两个geometry集合的相交面
     * @param geometryList1
     * @param geometryList2
     * @return
     */
    public List<Geometry> getIntersectionGeometry(List<Geometry> geometryList1,List<Geometry> geometryList2){
        List<Geometry> geometryList = new ArrayList<>();
        Geometry intersection;
        for (int i = 0;i<geometryList1.size();i++){
            for (int j = 0; j <geometryList2.size();j++) {
                intersection = getInterSection(geometryList1.get(i),geometryList2.get(j));
                if (!StringUtils.isEmpty(intersection)&&!intersection.isEmpty()){
                    geometryList.add(intersection);
                }
            }
        }
        return geometryList;
    }

    private Geometry getInterSection(Geometry geometry1,Geometry geometry2){
        Geometry intersection = null;
        try {
            if (!StringUtils.isEmpty(geometry1)){
                intersection =  geometry1.intersection(geometry1).intersection(geometry2.intersection(geometry2));
            }
        }catch (Exception e){
            log.error("面相交异常: "+e);
        }
        return intersection;
    }

    JSONObject getPolygonGeoJson(JSONArray geo){
        JSONObject geometry = new JSONObject();
        geometry.put("type","Polygon");
        geometry.put("coordinates",geo);
        return geometry;
    }


    JSONObject getPointGeoJson(JSONObject geo){
        JSONObject geometry = new JSONObject();
        geometry.put("type","Point");
        geometry.put("coordinates",Arrays.asList(geo.getDoubleValue("x"),geo.getDoubleValue("y")));
        return geometry;
    }

    JSONObject getLineGeoJson(JSONArray geo){
        JSONObject geometry = new JSONObject();
        geometry.put("type","LineString");
        geometry.put("coordinates",geo.getJSONArray(0));
        return geometry;
    }

    /**
     * 将geometry集合的转换为标准的geojson
     * @param geometryResList
     * @return
     */
    public JSONObject geometryListToGeoJson(List<Geometry> geometryResList ){
        JSONObject geoJson = new JSONObject();
        geoJson.put("type","FeatureCollection");
        JSONArray jsonArray = new JSONArray();
        geometryResList.forEach(a->{
            GeometryJSON geometryJSON = new GeometryJSON();
            StringWriter writer = new StringWriter();
            try {
                geometryJSON.write(a, writer);
                String s = writer.toString();
                JSONObject features = new JSONObject();
                features.put("type","Feature");
                features.put("geometry",JSONObject.parseObject(s));
                jsonArray.add(features);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        geoJson.put("features",jsonArray);
        return geoJson;
    }

    /**
     * 将geometry集合的转换为feature对象
     * @param geometryResList
     * @param serverName 服务名称
     * @return
     */
    public JSONArray geometryListToFeatures(List<Geometry> geometryResList,String serverName) throws MyException {
        JSONArray jsonArray = new JSONArray();
        JSONObject properties = null;
        GeometryJSON geometryJSON = null;
        StringWriter writer = null;
        JSONObject features = null;
        for (Geometry geometry:geometryResList) {
            try {
                geometryJSON = new GeometryJSON();
                writer = new StringWriter();
                geometryJSON.write(geometry, writer);
                features = new JSONObject();
                features.put("type","Feature");
                features.put("geometry",JSONObject.parseObject(writer.toString()));
                properties = new JSONObject();
                properties.put("serverName",serverName);
                features.put("properties",properties);
                jsonArray.add(features);
            }catch (Exception e){
                throw new MyException("叠加分析异常");
            }
        }
        return jsonArray;
    }

    public List<Geometry> getCustomGeometry(JSONObject customGeometry) throws MyException {
        List<Geometry> geometryList = null;
        try {
            geometryList = new ArrayList<>();
            GeometryJSON geometryJSON = new GeometryJSON();
            Geometry read = geometryJSON.read(customGeometry.toJSONString());
            read = read.buffer(0.0000001);
            geometryList.add(read);
        } catch (Exception e) {
            throw new MyException("自定义范围格式错误");
        }
        return geometryList;
    }

    public static Geometry validate(Geometry geom){
        if(geom instanceof MultiPolygon){
            System.out.println("多面");
            if(geom.isValid()){
                geom.normalize(); // validate does not pick up rings in the wrong order - this will fix that
                return geom; // If the multipolygon is valid just return it
            }
            Polygonizer polygonizer = new Polygonizer();
            for(int n = geom.getNumGeometries(); n-- > 0;){
                addPolygon((Polygon)geom.getGeometryN(n), polygonizer);
            }
            return toPolygonGeometry(polygonizer.getPolygons(), geom.getFactory());
        }else{
            return geom; // In my case, I only care about polygon / multipolygon geometries
        }
    }


    private static void addPolygon(Polygon polygon, Polygonizer polygonizer){
        addLineString(polygon.getExteriorRing(), polygonizer);
        for(int n = polygon.getNumInteriorRing(); n-- > 0;){
            addLineString(polygon.getInteriorRingN(n), polygonizer);
        }
    }

    private static void addLineString(LineString lineString, Polygonizer polygonizer){
        if(lineString instanceof LinearRing){ // LinearRings are treated differently to line strings : we need a LineString NOT a LinearRing
            lineString = lineString.getFactory().createLineString(lineString.getCoordinateSequence());
        }
        Point point = lineString.getFactory().createPoint(lineString.getCoordinateN(0));
        Geometry toAdd = lineString.union(point);
        polygonizer.add(toAdd);
    }

    /**
     * Get a geometry from a collection of polygons.
     *
     * @param polygons collection
     * @param factory factory to generate MultiPolygon if required
     * @return null if there were no polygons, the polygon if there was only one, or a MultiPolygon containing all polygons otherwise
     */
    static Geometry toPolygonGeometry(Collection<Polygon> polygons, GeometryFactory factory){
        switch(polygons.size()){
            case 0:
                return null; // No valid polygons!
            case 1:
                return polygons.iterator().next(); // single polygon - no need to wrap
            default:
                return factory.createMultiPolygon(polygons.toArray(new Polygon[polygons.size()])); // multiple polygons - wrap them
        }
    }

    public static void main(String[] args) throws IOException, ParseException {
//        GeometryJSON geometryJSON1 = new GeometryJSON();
//        Geometry read1 = geometryJSON1.read("{\"type\":\"MultiPolygon\",\"coordinates\":[[[[109.2041015625,30.088107753367257],[115.02685546875,30.088107753367257],[115.02685546875,32.7872745269555],[109.2041015625,32.7872745269555],[109.2041015625,30.088107753367257]]],[[[112.9833984375,26.82407078047018],[116.69677734375,26.82407078047018],[116.69677734375,29.036960648558267],[112.9833984375,29.036960648558267],[112.9833984375,26.82407078047018]]]]}");        System.out.println(read1);
        GeometryJSON geometryJSON = new GeometryJSON();
        Geometry read = geometryJSON.read("{\"type\":\"MultiPolygon\",\"coordinates\":[[[109.2041015625,30.088107753367257],[115.02685546875,30.088107753367257],[115.02685546875,32.7872745269555],[109.2041015625,32.7872745269555],[109.2041015625,30.088107753367257]],[[112.9833984375,26.82407078047018],[116.69677734375,26.82407078047018],[116.69677734375,29.036960648558267],[112.9833984375,29.036960648558267],[112.9833984375,26.82407078047018]]]}");
        System.out.println(read);

        WKTReader reader = new WKTReader();
        Geometry geometry = reader.read("MULTIPOLYGON (((109.2041015625 30.088107753367257, 115.02685546875 30.088107753367257, 115.02685546875 32.7872745269555, 109.2041015625 32.7872745269555, 109.2041015625 30.088107753367257), (112.9833984375 26.82407078047018, 116.69677734375 26.82407078047018, 116.69677734375 29.036960648558267, 112.9833984375 29.036960648558267, 112.9833984375 26.82407078047018)))");
        StringWriter writer = new StringWriter();
        GeometryJSON g = new GeometryJSON();
        g.write(geometry, writer);
        System.out.println(writer.toString());
    }
}
