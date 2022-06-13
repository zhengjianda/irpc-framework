package irpc.framework.core.common.utils;

import com.sun.corba.se.spi.ior.IdentifiableFactory;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public class CommonUtils {

    /**
     * 获取目标对象的实现接口
     */
    public static List<Class<?>> getAllInterfaces(Class targetClass){
        if (targetClass==null){
            throw new IllegalArgumentException("targetClass is null!");
        }
        Class[] clazz = targetClass.getInterfaces();
        if (clazz.length==0){
            return Collections.emptyList();
        }
        List<Class<?>> classes = new ArrayList<>(clazz.length);
        for(Class aClass:clazz){
            classes.add(aClass);
        }
        return classes;
    }

    public static String getIpAddress(){
        try {
            // 枚举类，
            Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            InetAddress ip = null;
            while (allNetInterfaces.hasMoreElements()){
                NetworkInterface networkInterface = allNetInterfaces.nextElement();
                if (networkInterface.isLoopback() || networkInterface.isVirtual() ||!networkInterface.isUp()){
                    continue;
                }
                else {
                    Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                    while (addresses.hasMoreElements()){
                        ip = addresses.nextElement();
                        if (ip instanceof Inet4Address){
                            return ip.getHostAddress();
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("IP地址获取失败"+e.toString());
        }
        return "";
    }

    public static boolean isEmpty(String str){
        return str == null || str.length()==0;
    }

    public static boolean isEmptyList(List list){
        if (list==null || list.size()==0){
            return true;
        }
        return false;
    }

    public static boolean isNotEmptyList(List list){
        return !isEmptyList(list);
    }
}
