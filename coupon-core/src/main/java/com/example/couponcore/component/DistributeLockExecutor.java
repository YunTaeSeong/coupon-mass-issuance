package com.example.couponcore.component;

import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class DistributeLockExecutor {

    // Redis에 연결해서 락 객체(RLock)를 만들고, 락 획득/해제를 수행
    private final RedissonClient redissonClient;
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    // waitMilliSecond : 락을 얻기 위한 최대 얼마나 기다릴지 대기시간
    // leaseMilliSecond : 락을 얻은 후 자동으로 락이 풀리기까지의 시간(TTL)
    // Runnable : 락을 얻은 후 실제 실행할 로직
    public void execute(String lockName, long waitMilliSecond, long leaseMilliSecond, Runnable logic) {
        RLock lock = redissonClient.getLock(lockName);
        try {
            boolean isLocked = lock.tryLock(waitMilliSecond, leaseMilliSecond, TimeUnit.MILLISECONDS);
            if (!isLocked){
               throw new IllegalAccessError("[" + lock + "] lock 획득 실패");
            }
            logic.run();
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
