package com.sangjun.order.container;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MinorTest {

    @Test
    void test() {

        List<Integer> lst = new ArrayList<>(Arrays.asList(1, 2, 3, 4));
        lst.add(5);

        Assertions.assertThat(lst.size()).isEqualTo(5);

    }


}
