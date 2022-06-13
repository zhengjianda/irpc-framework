package irpc.framework.core.common.event;

import org.omg.CORBA.ObjectHelper;

public interface IRpcEvent {

    Object getData();

    IRpcEvent setData(Object data);
}
