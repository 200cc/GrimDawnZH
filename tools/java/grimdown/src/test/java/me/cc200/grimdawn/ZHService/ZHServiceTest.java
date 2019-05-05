package me.cc200.grimdawn.ZHService;

import lombok.extern.slf4j.Slf4j;
import me.cc200.grimdawn.service.ZHService;
import org.junit.Test;

@Slf4j
public class ZHServiceTest {

    ZHService zhService = new ZHService();

    @Test
    public void test_zipZH() {
        zhService.zipZH();
    }

    @Test
    public void test_beautify() {
        zhService.beautify();
    }
}
