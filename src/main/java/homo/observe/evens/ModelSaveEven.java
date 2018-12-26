package homo.observe.evens;

import org.springframework.context.ApplicationEvent;

/**
 * @author wujianchuan 2018/12/26
 */
public class ModelSaveEven extends ApplicationEvent {
    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public ModelSaveEven(Object source) {
        super(source);
    }
}
