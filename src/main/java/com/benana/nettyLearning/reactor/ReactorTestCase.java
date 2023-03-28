package com.benana.nettyLearning.reactor;

import java.io.IOException;

/**
 * Reactor模式测试用例
 * @author Benana
 * @date 2023/3/28 21:11
 */
public class ReactorTestCase {

    public static void main(String[] args) {
        try {
            Reactor reactor = new Reactor(123);
            Thread thread = new Thread(reactor);
            thread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
