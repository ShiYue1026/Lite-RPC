package Client.circuitBreaker;

import java.util.HashMap;
import java.util.Map;

public class CircuitBreakerFactory {

    private Map<String, CircuitBreaker> circuitBreakers = new HashMap<>();

    public synchronized CircuitBreaker getCircuitBreaker(String methodSignature) {
        CircuitBreaker circuitBreaker;
        if(circuitBreakers.containsKey(methodSignature)) {
            return circuitBreaker = circuitBreakers.get(methodSignature);
        }
        System.out.println("方法" + methodSignature + "创建一个新的熔断器");
        circuitBreaker = new CircuitBreaker(1, 0.5, 10000);
        circuitBreakers.put(methodSignature, circuitBreaker);
        return circuitBreaker;
    }


}
