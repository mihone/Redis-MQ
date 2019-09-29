package start;

import com.mihone.redismq.reflect.RedisMQ;
import org.junit.Test;

public class StartTest {
    @Test
    public void startTest(){
        RedisMQ.start(RedisMQ.class);
    }
}
