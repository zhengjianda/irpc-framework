package irpc.framework.interfaces;

import java.util.LinkedList;
import java.util.List;

public interface DataService {

    /**
     * 发送数据
     */
    String sendData(String  body);

    /**
     * 获取数据
     */
    List<String> getList();
}
