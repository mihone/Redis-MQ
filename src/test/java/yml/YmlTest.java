package yml;

import com.mihone.redismq.yaml.YmlUtils;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

public class YmlTest {
    @Test
    public void ymlTest1(){

        Yaml yml = new Yaml();
        InputStream in = YmlUtils.class.getClassLoader().getResourceAsStream("application.yml");
        HashMap hashMap = yml.loadAs(in, HashMap.class);
        Object orginal = hashMap.get("orginal");
        System.out.println(orginal);


        InputStream in2 = YmlUtils.class.getClassLoader().getResourceAsStream("application-dev.yml");
        HashMap hashMap2 = yml.loadAs(in2, HashMap.class);
        Object orginal2 = hashMap2.get("abc");
        System.out.println(orginal2);

    }
    @Test
    public void ymlTest2(){

        Yaml yml = new Yaml();
        InputStream in = YmlUtils.class.getClassLoader().getResourceAsStream("application.yml");

        HashMap<String,Object> hashMap = yml.loadAs(in, HashMap.class);
        Set<Map.Entry<String, Object>> entries = hashMap.entrySet();
        HashMap<String, Object> result = new HashMap<>();
        setInMap(result, hashMap,null);
        System.out.println(result);
    }


    private void setInMap(HashMap<String, Object> newMap,HashMap<String, Object> hashMap, String key) {
        Set<Map.Entry<String, Object>> entries = hashMap.entrySet();
        for (Map.Entry<String, Object> entry : entries) {
            if (!(entry.getValue() instanceof Map)) {
                if (key == null) {
                    newMap.put(entry.getKey(), entry.getValue());
                }else{
                    newMap.put(key+"."+entry.getKey(), entry.getValue());
                }
            }else{
                setInMap(newMap,(HashMap<String, Object>)entry.getValue(),key==null?""+entry.getKey():key+"."+entry.getKey());
            }
        }
    }

    @Test
    public void propertiesTest1() throws IOException {

        Properties prop = new Properties();
        InputStream in = YmlUtils.class.getClassLoader().getResourceAsStream("application.properties");
        prop.load(in);
        Object orginal = prop.get("orginal");
        System.out.println(orginal);


        InputStream in2 = YmlUtils.class.getClassLoader().getResourceAsStream("application-dev.properties");
        prop.load(in2);
        Object orginal2 = prop.get("abc");
        System.out.println(orginal2);

    }

    @Test
    public void mapTest(){
        HashMap<String, Integer> map1 = new HashMap<>();
        map1.put("aa",10);
        map1.put("bb",20);
        map1.put("cc",20);
        HashMap<String, Integer> map2 = new HashMap<>();
        map2.put("dd",10);
        map2.put("ee",20);
        map2.put("ff",20);
        HashMap<String, Integer> map3 = new HashMap<>();
        map3.put("gg",10);
        map3.put("hh",20);
        map3.put("ii",20);
        List<HashMap<String, Integer>> list = new ArrayList<>();
        list.add(map1);
        list.add(map2);
        list.add(map3);
        System.out.println("===============");
        List<Map.Entry<String, Integer>> collect = list.stream().flatMap(map -> map.entrySet().stream()).collect(Collectors.toList());
        System.out.println(collect);
    }

    @Test
    public void ymlUtilsTest(){
        Object value = YmlUtils.getValue("orginal");
        System.out.println(value.getClass());
        System.out.println(value);
    }
}
