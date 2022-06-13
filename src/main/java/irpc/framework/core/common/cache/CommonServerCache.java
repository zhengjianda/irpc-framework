package irpc.framework.core.common.cache;

import irpc.framework.core.registry.URL;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CommonServerCache {

    // 提供的服务集合，key为服务名，value为对应的服务
    public static final Map<String,Object> PROVIDER_CLASS_MAP = new HashMap<>();

    //服务提供者的URL集合
    public static final Set<URL> PROVIDER_URL_SET = new HashSet<>();
}
